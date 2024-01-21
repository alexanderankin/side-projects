package totp4j.plumbing;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public interface Storage<T> {
    static Storage<Path> folderPerTokenStorage() {
        return new FolderPerTokenStorage();
    }

    void init(T config);

    Properties read(String name);

    void store(String name, Properties data);

    void remove(String name);

    List<String> list();
}
