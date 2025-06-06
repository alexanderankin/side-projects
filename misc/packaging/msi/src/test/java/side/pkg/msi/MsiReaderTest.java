package side.pkg.msi;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

class MsiReaderTest {

    MsiReader msiReader = new MsiReader();

    @Test
    void test() {
        msiReader.read(Paths.get(System.getProperty("user.home"), "Downloads", "7z2409-x64.msi").toFile());
        // msiReader.read(Paths.get(System.getProperty("user.home"), "Downloads", "node-v22.16.0-x64.msi").toFile());
    }
}
