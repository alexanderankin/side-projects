package side.scratch.pq.http.client;

import io.netty.handler.codec.http.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import picocli.AutoComplete;
import picocli.CommandLine;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Slf4j
@CommandLine.Command(
        name = "http",
        description = "basic http cli client which can test pqc (X25519Kyber768Draft00)",
        version = "0.0.1",
        mixinStandardHelpOptions = true,
        sortOptions = false,
        scope = CommandLine.ScopeType.INHERIT,
        subcommands = {
                AutoComplete.GenerateCompletion.class,
        }
)
public class Http implements Runnable {
    static final Set<CliHttpMethod> DATA_ALLOWED = Set.of(CliHttpMethod.POST, CliHttpMethod.PUT);

    @CommandLine.Option(names = {"-m", "--method"}, defaultValue = "GET")
    CliHttpMethod cliHttpMethod;
    @CommandLine.Option(names = {"-d", "--data"})
    String data;
    @CommandLine.Option(names = {"-H", "--header"})
    List<String> headers;
    @CommandLine.Parameters(arity = "1")
    URI uri;

    public static void main(String[] args) {
        System.exit(new CommandLine(Http.class).execute(args));
    }

    @Override
    public void run() {
        HttpClient httpClient = PqHttpClientScratch.httpClient(null);

        httpClient
                .headers(httpHeaders -> {
                    if (headers == null)
                        return;
                    for (String header : headers) {
                        int idx = header.indexOf(':');
                        if (idx <= 0)
                            continue;
                        String key = header.substring(0, idx).trim();
                        String val = header.substring(idx + 1).trim();
                        httpHeaders.add(key, val);
                    }
                })
                .request(cliHttpMethod.toNetty())
                .uri(uri.toString())
                .send((req, out) -> {
                    if (StringUtils.hasText(data)) {
                        if (DATA_ALLOWED.contains(cliHttpMethod))
                            return out.sendString(Mono.just(data));

                        log.warn("Unsupported http method: {} for data, which is not empty, supported methods: {}", cliHttpMethod, DATA_ALLOWED);
                    }
                    return out;
                })
                .responseSingle((res, buf) -> buf
                        .asString()
                        .map(body -> "HTTP " + res.status().code() + " " + res.status().reasonPhrase() + "\n" + body))
                .doOnError(Throwable::printStackTrace)
                .doOnNext(System.out::println)
                .block();
    }

    public enum CliHttpMethod {
        GET, POST, PUT, DELETE, HEAD, OPTIONS;

        HttpMethod toNetty() {
            return HttpMethod.valueOf(name());
        }
    }
}
