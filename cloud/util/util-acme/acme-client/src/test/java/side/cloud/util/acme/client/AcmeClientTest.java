package side.cloud.util.acme.client;

import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import java.net.URI;

class AcmeClientTest {

    String link(String url, String relation) {
        return String.format("<%s>;rel=\"%s\"", url, relation);
    }

    @Test
    void test() {
        var linkString = link("http://localhost:8080/page/2", "next");
        if (linkString != null) {
            Links link = Links.parse(linkString);
            URI next = link.getLink("next").map(Link::getHref).map(URI::create).orElse(null);
            System.out.println(next);
        }
    }
}
