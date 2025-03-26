package info.ankin.debmaker;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.Transferable;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DebMakerITest {
    @TempDir
    Path tempDir;

    @SneakyThrows
    Path tempFile(String fileName, String contents) {
        Path path = tempDir.resolve(fileName);
        Files.writeString(path, contents);
        return path;
    }

    @SneakyThrows
    @Test
    void test() {
        Map.Entry<Path, InputStream> output = new DebMaker.Build()
                .setOutputDirectory(tempDir)
                .run(new DebMaker.Configuration()
                        .setName("DebMakerITest.test")
                        .setVersion("0.0.1")
                        .setFiles(List.of(
                                new DebMaker.Configuration.InstalledFile()
                                        .setBuildPath(tempFile("tmp", "tmp1"))
                                        .setInstalledPath(Path.of("/tmp/1"))
                        ))
                );
        var contents = output.getValue().readAllBytes();

        try (GenericContainer<?> container = new GenericContainer<>("debian:12-slim")
                .withCommand("tail", "-f", "/dev/stdout")
                .withCopyToContainer(Transferable.of(contents), "/tmp/pkg.deb")) {
            container.start();
            // assertThat(container.execInContainer("perl", "-MMIME::Base64", "-i", "-pe", "$_ = decode_base64($_)", "/tmp/pkg.deb").getExitCode(), is(0));
            Container.ExecResult result = container.execInContainer("dpkg", "-i", "/tmp/pkg.deb");

            System.out.println(result.getStdout());
            System.out.println(result.getStderr());
            assertThat(result.getExitCode(), is(0));
        }
    }
}
