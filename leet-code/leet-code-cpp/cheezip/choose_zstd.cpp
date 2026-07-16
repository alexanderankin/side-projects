#include <httplib.h>
#include <nlohmann/json.hpp>
#include <zstd.h>

#include <cstdint>
#include <cstdlib>
#include <iostream>
#include <limits>
#include <memory>
#include <mutex>
#include <stdexcept>
#include <string>

using json = nlohmann::json;

namespace {

constexpr int kCompressionLevel = 9;
constexpr int kWindowLog = 27;  // 128 MiB maximum window.
constexpr std::size_t kMaxRequestBytes = 256ULL * 1024ULL * 1024ULL;

struct ContextState {
    std::string data;
    std::size_t compressed_size;
};

class ContextStore {
public:
    ContextStore()
        : state_(std::make_shared<const ContextState>(
              ContextState{"", compressedSize("")})) {}

    void replace(std::string data) {
        const std::size_t baseline = compressedSize(data);
        auto replacement = std::make_shared<const ContextState>(
            ContextState{std::move(data), baseline});

        std::lock_guard<std::mutex> lock(mutex_);
        state_ = std::move(replacement);
    }

    std::shared_ptr<const ContextState> snapshot() const {
        std::lock_guard<std::mutex> lock(mutex_);
        return state_;
    }

    static std::size_t compressedSize(const std::string& input) {
        ZSTD_CCtx* raw = ZSTD_createCCtx();
        if (raw == nullptr) {
            throw std::runtime_error("ZSTD_createCCtx failed");
        }

        const auto deleter = [](ZSTD_CCtx* context) {
            ZSTD_freeCCtx(context);
        };
        std::unique_ptr<ZSTD_CCtx, decltype(deleter)> context(raw, deleter);

        setParameter(context.get(), ZSTD_c_compressionLevel, kCompressionLevel,
                     "compression level");
        setParameter(context.get(), ZSTD_c_windowLog, kWindowLog,
                     "window size");
        setParameter(context.get(), ZSTD_c_nbWorkers, 0,
                     "worker count");
        setParameter(context.get(), ZSTD_c_contentSizeFlag, 1,
                     "content-size flag");

        std::string output;
        output.resize(ZSTD_compressBound(input.size()));

        const void* source = input.empty() ? nullptr : input.data();
        const std::size_t result = ZSTD_compress2(
            context.get(),
            output.data(),
            output.size(),
            source,
            input.size());

        check(result, "compressing input");
        return result;
    }

private:
    static void check(std::size_t result, const char* operation) {
        if (ZSTD_isError(result)) {
            throw std::runtime_error(
                std::string(operation) + ": " + ZSTD_getErrorName(result));
        }
    }

    static void setParameter(
        ZSTD_CCtx* context,
        ZSTD_cParameter parameter,
        int value,
        const char* name) {
        check(ZSTD_CCtx_setParameter(context, parameter, value), name);
    }

    mutable std::mutex mutex_;
    std::shared_ptr<const ContextState> state_;
};

std::string parseDataField(const httplib::Request& request) {
    return request.body;
//    json body;
//    try {
//        body = json::parse(request.body);
//    } catch (const json::parse_error& error) {
//        throw std::invalid_argument(
//            std::string("invalid JSON: ") + error.what());
//    }
//
//    if (!body.is_object() || !body.contains("data") ||
//        !body.at("data").is_string()) {
//        throw std::invalid_argument(
//            "request body must be a JSON object containing string field 'data'");
//    }
//
//    return body.at("data").get<std::string>();
}

void setJsonError(
    httplib::Response& response,
    int status,
    const std::string& message) {
    response.status = status;
    response.set_content(json{{"error", message}}.dump(), "application/json");
}

}  // namespace

int main(int argc, char* argv[]) {
    try {
        int port = 8080;
        if (argc == 2) {
            port = std::stoi(argv[1]);
            if (port < 1 || port > 65535) {
                throw std::invalid_argument("port must be between 1 and 65535");
            }
        } else if (argc > 2) {
            throw std::invalid_argument("usage: zstd_delta_server [port]");
        }

        ContextStore store;
        httplib::Server server;
        server.set_payload_max_length(kMaxRequestBytes);

        server.Post("/context", [&](const httplib::Request& request,
                                    httplib::Response& response) {
            try {
                std::string data = parseDataField(request);
                const std::size_t bytes = data.size();
                store.replace(std::move(data));

                response.status = 200;
                response.set_content(
                    json{{"context_bytes", bytes}}.dump(),
                    "application/json");
            } catch (const std::invalid_argument& error) {
                setJsonError(response, 400, error.what());
            } catch (const std::exception& error) {
                setJsonError(response, 500, error.what());
            }
        });

        server.Post("/test-input", [&](const httplib::Request& request,
                                       httplib::Response& response) {
            try {
                const std::string candidate = parseDataField(request);
                const auto state = store.snapshot();

                if (state->data.size() >
                    std::numeric_limits<std::size_t>::max() - candidate.size()) {
                    throw std::overflow_error("context plus input is too large");
                }

                std::string combined;
                combined.reserve(state->data.size() + candidate.size());
                combined.append(state->data);
                combined.append(candidate);

                const std::size_t combined_size =
                    ContextStore::compressedSize(combined);

                const std::int64_t delta =
                    static_cast<std::int64_t>(combined_size) -
                    static_cast<std::int64_t>(state->compressed_size);

                // A JSON response whose entire value is the delta number.
                response.status = 200;
                response.set_content(json(delta).dump(), "application/json");
            } catch (const std::invalid_argument& error) {
                setJsonError(response, 400, error.what());
            } catch (const std::exception& error) {
                setJsonError(response, 500, error.what());
            }
        });

        server.Get("/health", [](const httplib::Request&,
                                 httplib::Response& response) {
            response.set_content("{\"ok\":true}", "application/json");
        });

        server.set_error_handler([](const httplib::Request&,
                                    httplib::Response& response) {
            if (response.status == 413) {
                setJsonError(response, 413, "request body is too large");
            }
        });

        std::cout << "Listening on http://127.0.0.1:" << port << '\n';
        if (!server.listen("127.0.0.1", port)) {
            throw std::runtime_error("failed to bind HTTP server");
        }

        return EXIT_SUCCESS;
    } catch (const std::exception& error) {
        std::cerr << "Error: " << error.what() << '\n';
        return EXIT_FAILURE;
    }
}
