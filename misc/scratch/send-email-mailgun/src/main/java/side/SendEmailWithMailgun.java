package side;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.client.RestClient;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@CommandLine.Command(name = "SendEmailWithMailgun", mixinStandardHelpOptions = true, scope = CommandLine.ScopeType.INHERIT, sortOptions = false)
class SendEmailWithMailgun {
    public static void main(String[] args) {
        System.exit(new CommandLine(new SendEmailWithMailgun()).execute(args));
    }

    @CommandLine.Command(name = "send")
    void send(
            @CommandLine.Option(names = {"-d", "--domain"}, required = true) String domain,
            @CommandLine.Option(names = {"-f", "--from"}, required = true) String from,
            @CommandLine.Option(names = {"-t", "--to"}, required = true) String to,
            @CommandLine.Option(names = {"-s", "--subject"}, required = true) String subject,
            @CommandLine.ArgGroup(multiplicity = "1") Content content,
            @CommandLine.Option(names = {"--html"}, required = true, defaultValue = "true", negatable = true, description = "content is html or not") boolean html
    ) {
        /*
            curl -s --user 'api:API_KEY' \
              https://api.mailgun.net/v3/mycompany.com/messages \
              -F from='Mailgun Sandbox <postmaster@mycompany.com>' \
              -F to='Recipient <someone@mycompany.com>' \
              -F subject='Hello Recipient' \
              -F text='Congratulations Recipient, you just sent an email with Mailgun! You are truly awesome!' \
         */
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.mailgun.net")
                .defaultHeaders(headers -> headers.setBasicAuth("api", Objects.requireNonNull(System.getenv("SENDING_KEY"), "SENDING_KEY")))
                .build();

        var response = restClient.post()
                .uri("/v3/{domain}/messages", domain)
                .body(new MultiValueMapAdapter<>(Map.ofEntries(
                        Map.entry("from", List.of(from)),
                        Map.entry("to", List.of(to)),
                        Map.entry("subject", List.of(subject)),
                        Map.entry(html ? "html" : "text", List.of(content))
                )))
                .retrieve()
                .toEntity(String.class);

        log.info("response: {}", response);
        log.info("response body: {}", response.getBody());
    }

    @Data
    @Accessors(chain = true)
    static class Content {
        @CommandLine.Option(names = {"-c", "--content"})
        String content;
        @CommandLine.Option(names = {"-cf", "--content-file"})
        Path contentFile;
    }
}
