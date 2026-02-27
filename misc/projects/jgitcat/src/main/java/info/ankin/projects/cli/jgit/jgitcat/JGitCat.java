package info.ankin.projects.cli.jgit.jgitcat;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import picocli.CommandLine;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @see <a href="https://stackoverflow.com/a/54486558">Original Answer on Stack Overflow</a>
 */
@Slf4j
@CommandLine.Command(mixinStandardHelpOptions = true, description = "jgitcat")
public class JGitCat implements Runnable {
    public static final String repositoryDescription = "repository that will be cloned in memory";
    public static final String branchDescription = "branch/commit whose tree will be walked";
    public static final String filenameDescription = "file to find in the tree and print";
    public static final String outputDescription = "where to place the contents (otherwise write to System.out)";

    @CommandLine.Option(names = {"-r", "--repository"}, description = repositoryDescription, required = true)
    String repository;

    @CommandLine.Option(names = {"-b", "--branch"}, description = branchDescription, defaultValue = "main")
    String branch;

    @CommandLine.Option(names = {"-f", "--filename"}, description = filenameDescription, defaultValue = "README.md")
    String filename;

    @CommandLine.Option(names = {"-o", "--output"}, description = outputDescription)
    String output;

    public static void main(String[] args) {
        System.exit(new CommandLine(new JGitCat()).execute(args));
    }

    @SneakyThrows
    @Override
    public void run() {
        try (OutputStream outputStream = outputStream()) {
            ObjectLoader objectLoader = loadRemote(repository, branch, filename);
            objectLoader.copyTo(outputStream);
        } catch (FileNotFoundInBranchException fnfEx) {
            log.error("Could not walk to {} on {} in {}", filename, branch, repository, fnfEx);
        }
    }

    @SneakyThrows
    private OutputStream outputStream() {
        if (output != null) return new BufferedOutputStream(new FileOutputStream(output));

        return System.out;
    }

    private ObjectLoader loadRemote(String uri, String branch, String filename) throws Exception {
        DfsRepositoryDescription repoDesc = new DfsRepositoryDescription();
        try (InMemoryRepository repo = new InMemoryRepository(repoDesc);
             Git git = new Git(repo)) {

            git.fetch()
                    .setRemote(uri)
                    .setRefSpecs(new RefSpec("+refs/heads/*:refs/heads/*"))
                    .call();

            InMemoryRepository.MemObjDatabase objectDatabase = repo.getObjectDatabase();
            log.trace("objectDatabase for this repository: {}", objectDatabase);

            ObjectId lastCommitId = repo.resolve("refs/heads/" + branch);
            RevWalk revWalk = new RevWalk(repo);
            RevCommit commit = revWalk.parseCommit(lastCommitId);
            RevTree tree = commit.getTree();
            TreeWalk treeWalk = new TreeWalk(repo);
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filename));
            if (!treeWalk.next()) {
                throw new FileNotFoundInBranchException();
            }
            ObjectId objectId = treeWalk.getObjectId(0);
            return repo.open(objectId);
        }
    }

    private static class FileNotFoundInBranchException extends RuntimeException {
    }

}
