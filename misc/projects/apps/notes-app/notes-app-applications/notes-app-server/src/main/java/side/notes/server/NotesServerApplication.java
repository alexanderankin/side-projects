package side.notes.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import side.notes.backend.NotesBackendProperties;

@SpringBootApplication(scanBasePackageClasses = NotesBackendProperties.class)
public class NotesServerApplication {
    static void main(String[] args) {
        SpringApplication.run(NotesServerApplication.class, args);
    }
}
