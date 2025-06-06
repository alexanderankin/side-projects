package side.pkg.msi;

import lombok.SneakyThrows;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.*;

class MsiReader {

    @SneakyThrows
    private static boolean isCompoundFile(RandomAccessFile file) {
        file.seek(0);
        byte[] header = new byte[8];
        file.readFully(header);
        // Compound file signature: D0 CF 11 E0 A1 B1 1A E1
        return Arrays.equals(header, new byte[]{
                (byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0,
                (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1
        });
    }

    @SneakyThrows
    private static List<String> listStreamNames(RandomAccessFile file) {
        // A real implementation needs to parse the FAT and directory sectors,
        // but for this simple use-case, we can scan for ASCII names in the file.
        // Not accurate, but works well enough for simple detection.
        file.seek(0);
        byte[] data = new byte[(int) Math.min(file.length(), 50 * 1024 * 1024)]; // Read up to 1MB
        file.readFully(data);
        Set<String> names = new HashSet<>();
        for (int i = 0; i < data.length - 64; i++) {
            // Look for UTF-16LE strings aligned on 2 bytes
            if (data[i] != 0 && data[i + 1] == 0) {
                int start = i;
                int len = 0;
                while (i + 1 < data.length && data[i + 1] == 0 && len < 32) {
                    len++;
                    i += 2;
                }
                if (len >= 4) {
                    byte[] nameBytes = Arrays.copyOfRange(data, start, start + len * 2);
                    String name = new String(nameBytes, StandardCharsets.UTF_16LE);
                    names.add(name.trim());
                }
            }
        }
        return new ArrayList<>(names);
    }

    @SneakyThrows
    void read(File inputFile) {
        String msiPath = inputFile.getPath();
        String tableName = "AdminExecuteSequence";

        try (RandomAccessFile file = new RandomAccessFile(msiPath, "r")) {
            if (!isCompoundFile(file)) {
                System.err.println("Not a valid MSI (OLE compound file)");
                System.exit(1);
            }

            List<String> streamNames = listStreamNames(file);
            boolean found = streamNames.contains(tableName);
            System.out.println("Table '" + tableName + "' " + (found ? "exists" : "not found"));
        }
    }
}
