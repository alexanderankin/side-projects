package side.pkg.gradle2maven;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class RepoDirConverter {
    /**
     * @see <a href="https://docs.gradle.org/current/userguide/directory_layout.html#dir:gradle_user_home">Gradle User Home (Gradle docs)</a>
     */
    private static final Path GRADLE_HOME = Optional.ofNullable(System.getenv("GRADLE_USER_HOME")).map(Path::of)
            .orElse(Path.of(System.getProperty("user.home"), ".gradle"));
    private static final Path RELATIVE_DEFAULT_REPO_DIR = Path.of("caches", "modules-2", "files-2.1");
    private static final Path DEFAULT_REPO_DIR = GRADLE_HOME.resolve(RELATIVE_DEFAULT_REPO_DIR);
    private static final Set<String> JAVA_LIBRARY_FILE_EXTENSIONS = Set.of("pom", "jar", "module");

    static List<String> pathToParts(Path path) {
        var result = new ArrayList<String>(path.getNameCount());
        for (int i = 0; i < path.getNameCount(); i++) {
            result.add(path.getName(i).toString());
        }
        return result;
    }

    static Path getDestination(Path output, List<String> gradlePath) {
        var group = gradlePath.getFirst();
        var archive = gradlePath.get(1);
        var version = gradlePath.get(2);
        var fileName = gradlePath.getLast();

        var groupParts = group.split("\\.");
        var groupOutDir = Path.of(groupParts[0], Arrays.copyOfRange(groupParts, 1, groupParts.length));
        var relativeOut = Path.of(archive, version, fileName);
        return output.resolve(groupOutDir).resolve(relativeOut);
    }

    public Path getDefaultRepoDir() {
        return DEFAULT_REPO_DIR;
    }

    @SneakyThrows
    public void convert(Path input, Path output) {
        try (Stream<Path> walk = Files.walk(input)) {
            getPathToMoveStream(input, output, walk, true).forEach(e -> copyFile(e.from(), e.to()));
        }
    }

    @SneakyThrows
    public void convertDryRun(Path input, Path output) {
        try (Stream<Path> walk = Files.walk(input)) {
            getPathToMoveStream(input, output, walk, false).forEach(e -> log.info("dry run, would copy {} to {}", e.from(), e.to()));
        }
    }

    private Stream<PathToMove> getPathToMoveStream(Path input, Path output, Stream<Path> walk, boolean parallel) {
        // why not
        var walkParallel = parallel ? walk.parallel() : walk;

        return walkParallel
                // filter for relevant files
                .filter(this::isJavaLibraryFile)
                .map((Path sourceFile) -> {
                    var strings = pathToParts(input.relativize(sourceFile));

                    var destination = getDestination(output, strings);

                    log.trace("would copy {} to {}", sourceFile, destination);
                    return new PathToMove(sourceFile, destination);
                });
    }

    @SneakyThrows
    private void copyFile(Path from, Path to) {
        if (Files.exists(to)) {
            log.trace("ignoring, already exists: {}", to);
            return;
        }

        to.getParent().toFile().mkdirs();
        Files.copy(from, to);
    }

    private boolean isJavaLibraryFile(Path path) {
        if (path.toFile().isDirectory()) return false;

        String fileName = path.getFileName().toString();
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex == -1) return false;
        var extension = fileName.substring(lastIndex + 1).toLowerCase();

        return JAVA_LIBRARY_FILE_EXTENSIONS.contains(extension);
    }

    private record PathToMove(Path from, Path to) {
    }
}
