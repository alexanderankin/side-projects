#include <zstd.h>

#include <algorithm>
#include <cstdint>
#include <cstdlib>
#include <fstream>
#include <iostream>
#include <stdexcept>
#include <string>
#include <string_view>
#include <vector>

using Bytes = std::vector<std::uint8_t>;

struct CompressionResult {
    Bytes data;
    std::size_t size() const noexcept {
        return data.size();
    }
};

[[noreturn]] void throwZstdError(std::size_t code, const char* operation) {
    throw std::runtime_error(
        std::string(operation) + ": " + ZSTD_getErrorName(code)
    );
}

void checkZstd(std::size_t code, const char* operation) {
    if (ZSTD_isError(code)) {
        throwZstdError(code, operation);
    }
}

Bytes toBytes(std::string_view text) {
    return Bytes(text.begin(), text.end());
}

Bytes concatenate(const Bytes& prefix, const Bytes& suffix) {
    Bytes result;
    result.reserve(prefix.size() + suffix.size());
    result.insert(result.end(), prefix.begin(), prefix.end());
    result.insert(result.end(), suffix.begin(), suffix.end());
    return result;
}

/*
 * Compresses a complete input into one independent zstd frame.
 *
 * Because every trial uses the same parameters and the same prefix, comparing
 * the resulting sizes tells us which candidate compresses better at this point.
 */
CompressionResult compressZstd(
    const Bytes& input,
    int compressionLevel,
    int windowLog
) {
    ZSTD_CCtx* context = ZSTD_createCCtx();
    if (context == nullptr) {
        throw std::runtime_error("ZSTD_createCCtx failed");
    }

    try {
        checkZstd(
            ZSTD_CCtx_setParameter(
                context,
                ZSTD_c_compressionLevel,
                compressionLevel
            ),
            "setting compression level"
        );

        checkZstd(
            ZSTD_CCtx_setParameter(
                context,
                ZSTD_c_windowLog,
                windowLog
            ),
            "setting windowLog"
        );

        // Include the original size in the frame header.
        checkZstd(
            ZSTD_CCtx_setParameter(
                context,
                ZSTD_c_contentSizeFlag,
                1
            ),
            "setting content-size flag"
        );

        // Single-threaded mode makes behavior straightforward and deterministic.
        checkZstd(
            ZSTD_CCtx_setParameter(
                context,
                ZSTD_c_nbWorkers,
                0
            ),
            "setting worker count"
        );

        const std::size_t maximumSize = ZSTD_compressBound(input.size());
        Bytes compressed(maximumSize);

        const void* source = input.empty() ? nullptr : input.data();

        const std::size_t compressedSize = ZSTD_compress2(
            context,
            compressed.data(),
            compressed.size(),
            source,
            input.size()
        );

        checkZstd(compressedSize, "compressing data");
        compressed.resize(compressedSize);

        ZSTD_freeCCtx(context);
        return CompressionResult{std::move(compressed)};
    } catch (...) {
        ZSTD_freeCCtx(context);
        throw;
    }
}

enum class CandidateChoice {
    First,
    Second
};

struct ChoiceResult {
    CandidateChoice choice;
    std::size_t firstCompressedSize;
    std::size_t secondCompressedSize;
};

/*
 * Tests prefix+A and prefix+B, then commits the smaller alternative to prefix.
 *
 * On a tie, it chooses A.
 */
ChoiceResult chooseAndCommit(
    Bytes& committedPrefix,
    const Bytes& candidateA,
    const Bytes& candidateB,
    int compressionLevel,
    int windowLog
) {
    const Bytes trialA = concatenate(committedPrefix, candidateA);
    const Bytes trialB = concatenate(committedPrefix, candidateB);

    const CompressionResult compressedA =
        compressZstd(trialA, compressionLevel, windowLog);

    const CompressionResult compressedB =
        compressZstd(trialB, compressionLevel, windowLog);

    const bool chooseA = compressedA.size() <= compressedB.size();
    const Bytes& selected = chooseA ? candidateA : candidateB;

    committedPrefix.insert(
        committedPrefix.end(),
        selected.begin(),
        selected.end()
    );

    return ChoiceResult{
        chooseA ? CandidateChoice::First : CandidateChoice::Second,
        compressedA.size(),
        compressedB.size()
    };
}

void writeFile(const std::string& filename, const Bytes& data) {
    std::ofstream output(filename, std::ios::binary);

    if (!output) {
        throw std::runtime_error("Could not open output file: " + filename);
    }

    output.write(
        reinterpret_cast<const char*>(data.data()),
        static_cast<std::streamsize>(data.size())
    );

    if (!output) {
        throw std::runtime_error("Could not write output file: " + filename);
    }
}

Bytes decompressZstd(const Bytes& compressed, std::size_t expectedSize) {
    Bytes decompressed(expectedSize);

    const std::size_t result = ZSTD_decompress(
        decompressed.data(),
        decompressed.size(),
        compressed.data(),
        compressed.size()
    );

    checkZstd(result, "decompressing verification frame");

    if (result != expectedSize) {
        throw std::runtime_error("Unexpected decompressed size");
    }

    return decompressed;
}

int main() {
    try {
        /*
         * windowLog=27 means a requested maximum window of 2^27 bytes,
         * which is 128 MiB.
         */
        constexpr int compressionLevel = 9;
        constexpr int windowLog = 27;

        Bytes committed = toBytes(
            "Header: example stream\n"
            "Repeated vocabulary: customer transaction account balance\n"
        );

        // First decision.
        const Bytes candidate1A = toBytes(
            "customer transaction account balance "
            "customer transaction account balance\n"
        );

        const Bytes candidate1B = toBytes(
            "completely unrelated random-looking material: 7f2a91c4\n"
        );

        const ChoiceResult decision1 = chooseAndCommit(
            committed,
            candidate1A,
            candidate1B,
            compressionLevel,
            windowLog
        );

        std::cout
            << "Decision 1:\n"
            << "  prefix + A: " << decision1.firstCompressedSize << " bytes\n"
            << "  prefix + B: " << decision1.secondCompressedSize << " bytes\n"
            << "  selected: "
            << (decision1.choice == CandidateChoice::First ? "A" : "B")
            << "\n\n";

        // Second decision, now using the previously selected data as history.
        const Bytes candidate2A = toBytes(
            "customer account balance transaction customer account\n"
        );

        const Bytes candidate2B = toBytes(
            "qzxv-1847-jump-lantern-orbit-marble-cactus\n"
        );

        const ChoiceResult decision2 = chooseAndCommit(
            committed,
            candidate2A,
            candidate2B,
            compressionLevel,
            windowLog
        );

        std::cout
            << "Decision 2:\n"
            << "  prefix + A: " << decision2.firstCompressedSize << " bytes\n"
            << "  prefix + B: " << decision2.secondCompressedSize << " bytes\n"
            << "  selected: "
            << (decision2.choice == CandidateChoice::First ? "A" : "B")
            << "\n\n";

        // Produce one final frame containing all selected data.
        const CompressionResult finalFrame = compressZstd(
            committed,
            compressionLevel,
            windowLog
        );

        writeFile("output.zst", finalFrame.data);

        // Verify that the generated frame round-trips correctly.
        const Bytes restored = decompressZstd(
            finalFrame.data,
            committed.size()
        );

        if (restored != committed) {
            throw std::runtime_error("Round-trip verification failed");
        }

        std::cout
            << "Uncompressed size: " << committed.size() << " bytes\n"
            << "Compressed size:   " << finalFrame.size() << " bytes\n"
            << "Wrote output.zst\n"
            << "Round-trip verification succeeded\n";

        return EXIT_SUCCESS;
    } catch (const std::exception& error) {
        std::cerr << "Error: " << error.what() << '\n';
        return EXIT_FAILURE;
    }
}
