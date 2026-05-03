package side.notes.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest")
@AutoConfigureWebTestClient(timeout = "PT24H")
@Import({NotesBackendTestApplication.class, NotesBackendTestApplication.TestcontainersConfiguration.class})
public abstract class NotesBackendITest {
    @Autowired
    protected WebTestClient webTestClient;
}
