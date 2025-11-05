package side;

import jakarta.mail.internet.MimeMessage;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import picocli.CommandLine;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@CommandLine.Command(name = "SendEmailWithMailgun", mixinStandardHelpOptions = true, scope = CommandLine.ScopeType.INHERIT, sortOptions = false)
class SendEmailWithMailgun {
    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        System.exit(new CommandLine(new SendEmailWithMailgun()).execute(args));
    }

    @CommandLine.Command(name = "send")
    void send(
            @CommandLine.Option(names = {"-p", "--proto"}, defaultValue = "HTTP") MgEmailProtocol protocol,
            @CommandLine.Option(names = {"-f", "--from"}, required = true) String from,
            @CommandLine.Option(names = {"-t", "--to"}, required = true) String to,
            @CommandLine.Option(names = {"-cc", "--cc"}, required = true) String cc,
            @CommandLine.Option(names = {"-s", "--subject"}, required = true) String subject,
            @CommandLine.ArgGroup(multiplicity = "1") Auth auth,
            @CommandLine.ArgGroup(multiplicity = "1") Content content,
            @CommandLine.Option(names = {"--html"}, required = true, negatable = true, description = "content is html or not") boolean html
    ) {
        switch (protocol) {
            case HTTP -> sendWithHttp(from, to, cc, subject, auth.getHttp(), content, html);
            case SMTP -> sendWithSmtp(from, to, cc, subject, auth.getSmtpAuth(), content, html);
        }
    }

    @SneakyThrows
    void sendWithSmtp(String from, String to, String cc, String subject, Auth.SmtpAuth auth, Content content, boolean html) {
        log.info("Sending email with parameters: subject: {}, to: {}, cc: {}, html: {}, content: {}", subject, to, cc, html, content);
        var j = new JavaMailSenderImpl();
        j.setDefaultEncoding(StandardCharsets.UTF_8.name());
        j.setHost(auth.getHost());
        j.setPort(auth.getPort());
        j.setUsername(auth.getUser());
        j.setPassword(auth.getPass());
        j.setProtocol("smtps");

        Properties p = j.getJavaMailProperties();
        p.setProperty("mail.transport.protocol", "smtps");
        p.setProperty("mail.smtp.auth", "true");
        p.setProperty("mail.smtp.starttls.enable", "true");
        p.setProperty("mail.debug", "true");

        MimeMessage mimeMessage = j.createMimeMessage();
        var message = new MimeMessageHelper(mimeMessage, "utf-8");
        message.setFrom(from);
        message.setTo(to);
        message.setCc(cc);
        message.setSubject(subject);
        message.setText(content.actualContent(), html);
        j.send(mimeMessage);
    }

    void sendWithHttp(String from, String to, String cc, String subject, Auth.HttpAuth auth, Content content, boolean html) {
        if (cc != null) {
            throw new UnsupportedOperationException("cc is not null but cc is not implemented");
        }
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
                .defaultHeaders(headers -> headers.setBasicAuth("api", Objects.requireNonNull(System.getenv(auth.getEnvVarName()), "SENDING_KEY")))
                .build();

        ResponseEntity<String> response;
        try {
            // noinspection Convert2Diamond
            response = restClient.post()
                    .uri("/v3/{domain}/messages", auth.getDomain())
                    .body(new MultiValueMapAdapter<String, String>(Map.ofEntries(
                            Map.entry("from", List.of(from)),
                            Map.entry("to", List.of(to)),
                            Map.entry("subject", List.of(subject)),
                            Map.entry(html ? "html" : "text", List.of(content.actualContent()))
                    )))
                    .retrieve()
                    .toEntity(String.class);
        } catch (HttpClientErrorException e) {
            log.error("MailGun API Exception: {}: {} (response headers: {})", e.getStatusCode(), e.getResponseBodyAsString(), e.getResponseHeaders());
            throw e;
        }

        log.info("response: {}", response);
        log.info("response body: {}", response.getBody());
    }

    enum MgEmailProtocol { HTTP, SMTP }

    @Data
    @Accessors(chain = true)
    static class Content {
        @CommandLine.Option(names = {"-c", "--content"})
        String content;
        @CommandLine.Option(names = {"-cf", "--content-file"})
        Path contentFile;

        @SneakyThrows
        public String actualContent() {
            return content == null ? Files.readString(contentFile) : content;
        }
    }

    @Data
    @Accessors(chain = true)
    static class Auth {
        @CommandLine.ArgGroup(exclusive = false)
        HttpAuth http;
        @CommandLine.ArgGroup(exclusive = false)
        SmtpAuth smtpAuth;

        @Data
        @Accessors(chain = true)
        static class HttpAuth {
            @CommandLine.Option(names = {"-d", "--domain"}, required = true)
            String domain;
            @CommandLine.Option(names = {"-akev", "--api-key-env-var"}, defaultValue = "SENDING_KEY")
            String envVarName;
        }

        @Data
        @Accessors(chain = true)
        static class SmtpAuth {
            @CommandLine.Option(names = "--smtp-user", required = true)
            String user;
            @CommandLine.Option(names = "--smtp-pass", required = true)
            String pass;
            @CommandLine.Option(names = "--smtp-host", required = true)
            String host;
            @CommandLine.Option(names = "--smtp-port", required = true)
            int port;
        }
    }
}
/*
java -jar misc/scratch/send-email-mailgun/build/libs/send-email-mailgun-all.jar send -p SMTP -f '' -s "Test email 1" -cf /tmp/to-send  --html --smtp-user $EMAIL_USERNAME --smtp-pass $EMAIL_PASSWORD --smtp-host $EMAIL_HOST --smtp-port "$EMAIL_PORT" -t ""
*/
