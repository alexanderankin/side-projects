package side.dist.mergesort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(DistributedMergeSortTestApplication.TestcontainersConfiguration.class)
@ActiveProfiles("itest")
@AutoConfigureWebTestClient(timeout = "PT24H")
public abstract class DistributedMergeSortITest {
    // @BeforeAll
    // static void configureDockerApi() {
    //     System.setProperty("docker.api.version", "1.44");
    // }

    @Autowired
    protected WebTestClient webTestClient;
}
