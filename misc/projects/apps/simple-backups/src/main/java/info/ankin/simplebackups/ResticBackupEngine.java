package info.ankin.simplebackups;

import info.ankin.projects.system.exec.Exec;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
final class ResticBackupEngine implements BackupEngine {
    // Deliberately fixed: encryption is not a goal of this hobby application.
    static final String PASSWORD = "side-projects-simple-backups";

    @Override public void create(Path source, Path output, Consumer<String> progress) throws IOException {
        Path repository = output.resolve("restic").toAbsolutePath();
        progress.accept("Initializing restic repository");
        run(source, repository, List.of("init"));
        progress.accept("Running restic backup");
        run(source, repository, List.of("backup", "--quiet", "."));
    }

    private void run(Path source, Path repository, List<String> arguments) throws IOException {
        var environment = new HashMap<>(System.getenv());
        // Keep ambient restic configuration from changing the selected repository or prompting for secrets.
        environment.keySet().removeIf(k -> k.startsWith("RESTIC_"));
        environment.put("RESTIC_PASSWORD", PASSWORD);
        environment.put("RESTIC_REPOSITORY", repository.toString());
        var config = new Exec.ProcessConfig().setCommand("restic").setArguments(arguments)
                .setWorkingDirectory(source).setEnvironment(environment).setCaptureOutput(true)
                .setStandardOutput(Exec.ProcessConfig.ProcessStream.DiscardProcessStream.INSTANCE)
                .setStandardError(Exec.ProcessConfig.ProcessStream.DiscardProcessStream.INSTANCE)
                .setStandardOutputCharset(StandardCharsets.UTF_8).setStandardErrorCharset(StandardCharsets.UTF_8);
        try {
            log.info("Running restic {}", arguments.getFirst());
            new Exec().run(config);
        } catch (Exec.ExecExitCodeException e) {
            throw new IOException("Restic failed (exit " + e.getExitCode() + "): "
                    + e.getCompletedProcess().getStandardErrorString(), e);
        } catch (Exception e) {
            throw new IOException("Could not run restic. Install it and make sure it is on PATH. " + e.getMessage(), e);
        }
    }

    @Override public void restore(SnapshotStore store, Snapshot snapshot, Path destination) {
        throw new UnsupportedOperationException("Restic restore is not implemented. Use the restic CLI with this snapshot's repository.");
    }

    @Override public List<String> diff(SnapshotStore store, Snapshot snapshot, Path source) {
        throw new UnsupportedOperationException("Restic diff is not implemented. Diff is available for tar snapshots only.");
    }
}
