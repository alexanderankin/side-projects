package info.ankin.simplebackups.backup;

import com.fasterxml.uuid.Generators;
import com.github.robtimus.filesystems.sftp.SFTPEnvironment;
import com.github.robtimus.filesystems.sftp.SimpleUserInfo;
import com.google.common.jimfs.Jimfs;
import com.jcraft.jsch.JSch;
import info.ankin.simplebackups.SimpleBackupsITest;
import info.ankin.simplebackups.config.AppSettings;
import info.ankin.simplebackups.config.BackupConfiguration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;

import static com.google.common.jimfs.Configuration.unix;

@Slf4j
class BackupServiceITest extends SimpleBackupsITest {

    static final FileSystem POSIX_FS = Jimfs.newFileSystem(unix().toBuilder()
            .setAttributeViews("basic", "owner", "posix", "unix")
            .build());
    private BackupService backupService;

    @TempDir
    private Path tempDir;
    private FileSystem fs;

    @SneakyThrows
    @BeforeEach
    void setUp() {
        backupService = new BackupService();
        var cred = testcontainersConfig.getSftpContainerCredentials();
        Path hk = POSIX_FS.getPath("/hostkeys");
        Files.createFile(hk);
        Files.setPosixFilePermissions(hk, PosixFilePermissions.fromString("rw-rw-rw-"));
        Files.createDirectories(POSIX_FS.getPath("/home"));
        // !?!?!?!?!?!? frustrating - why do i have to set up the whole file system for this?
        var env = new SFTPEnvironment()
                .withAgentForwarding(false)
                .withHostKeyRepository(new AcceptNewHostKeys(new JSch(), hk)) // why do i need a file, i just want StrictHostKeyChecking=no UserKnownHostsFile=/dev/null
                .withUsername(cred.username())
                .withUserInfo(new SimpleUserInfo(cred.password().toCharArray()));
        fs = FileSystems.newFileSystem(testcontainersConfig.getSftpContainerUri(), env);
    }

    @SneakyThrows
    @Test
    void test() {
        var user = testcontainersConfig.getSftpContainerCredentials().username();

        Path targetDirPosixPath = POSIX_FS.getPath("/home", user, "target-dir");
        Path targetDirSftpPath = fs.getPath("/home", user, "target-dir");
        Files.createDirectories(targetDirSftpPath);
        Files.writeString(targetDirSftpPath.resolve("test-file"), "test file contents");

        var messages = new ArrayList<String>();
        backupService.backup(
                new BackupConfiguration(
                        Generators.nameBasedGenerator().generate("test"),
                        "test",
                        UriComponentsBuilder.fromUri(testcontainersConfig.getSftpContainerUri())
                                .path(targetDirPosixPath.toString())
                                .build().toUri(),
                        tempDir.resolve("backups").toUri()
                ),
                AppSettings.defaults(),
                messages::add
        );

        log.info("messages received: {}", messages);
    }
}
