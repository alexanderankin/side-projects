package side.pkg.msi;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MsiCFBFParser {
    static final int HEADER_SIZE = 512;
    static final int DIR_ENTRY_SIZE = 128;
    static final int SECTOR_SIZE = 512; // Most MSIs use 512-byte sectors
    static final int ENDOFCHAIN = 0xFFFFFFFE;

    public static void main(String[] args) throws IOException {
        String msiPath = Paths.get(System.getProperty("user.home"), "Downloads", "7z2409-x64.msi").toString();
        String targetName = "AdminExecuteSequence";

        try (RandomAccessFile raf = new RandomAccessFile(msiPath, "r")) {
            byte[] header = new byte[HEADER_SIZE];
            raf.readFully(header);

            ByteBuffer hdr = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
            if (hdr.getLong(0) != 0xE11AB1A1E011CFD0L) {
                System.err.println("Not a valid CFBF file");
                return;
            }

            int sectorShift = 1 << hdr.getShort(30);
            int dirSectorStart = hdr.getInt(48);
            int numFATSectors = hdr.getInt(44);
            int[] fatSectors = new int[numFATSectors];
            for (int i = 0; i < numFATSectors; i++) {
                fatSectors[i] = hdr.getInt(76 + i * 4);
            }

            // Step 1: Load FAT
            int[] fat = readFAT(raf, fatSectors, sectorShift);

            // Step 2: Walk directory chain
            List<Integer> dirSectors = followChain(fat, dirSectorStart);
            List<String> streamNames = readDirectoryEntries(raf, dirSectors, sectorShift);

            // System.out.println("streamNames: " + streamNames);
            //
            // for (String name : streamNames) {
            //     if (name.equalsIgnoreCase(targetName)) {
            //         System.out.println("Found table: " + targetName);
            //         return;
            //     }
            // }
            // System.out.println("Table not found: " + targetName);


            Map<String, StreamData> streams = getStreams(raf, dirSectors, 512);
            System.out.println("Available streams:");
            streams.forEach((name, stream) -> {
                System.out.printf("  %s -> sector %d, size %d bytes\n", name, stream.startSector, stream.size);
            });
        }
    }

    static int[] readFAT(RandomAccessFile raf, int[] fatSectors, int sectorSize) throws IOException {
        int entriesPerSector = sectorSize / 4;
        List<Integer> fatEntries = new ArrayList<>();
        for (int sector : fatSectors) {
            raf.seek(HEADER_SIZE + (long) sector * sectorSize);
            ByteBuffer bb = ByteBuffer.allocate(sectorSize).order(ByteOrder.LITTLE_ENDIAN);
            raf.readFully(bb.array());
            for (int i = 0; i < entriesPerSector; i++) {
                fatEntries.add(bb.getInt(i * 4));
            }
        }
        return fatEntries.stream().mapToInt(Integer::intValue).toArray();
    }

    static List<Integer> followChain(int[] fat, int startSector) {
        List<Integer> chain = new ArrayList<>();
        int sector = startSector;
        while (sector != ENDOFCHAIN && sector >= 0 && sector < fat.length) {
            chain.add(sector);
            sector = fat[sector];
        }
        return chain;
    }

    static List<String> readDirectoryEntries(RandomAccessFile raf, List<Integer> dirSectors, int sectorSize) throws IOException {
        List<String> names = new ArrayList<>();
        byte[] sectorData = new byte[sectorSize];

        for (int sector : dirSectors) {
            raf.seek(HEADER_SIZE + (long) sector * sectorSize);
            raf.readFully(sectorData);

            for (int offset = 0; offset + DIR_ENTRY_SIZE <= sectorSize; offset += DIR_ENTRY_SIZE) {
                // Read name length
                int nameLength = ((sectorData[offset + 0x40] & 0xFF) | ((sectorData[offset + 0x41] & 0xFF) << 8));

                if (nameLength < 2 || nameLength > 64 || nameLength % 2 != 0) continue;

                // Extract name from first 64 bytes
                String name = new String(sectorData, offset, nameLength - 2, "UTF-16LE");
                names.add(name);
                /*
                 */

                /*
                int nameLength = ((sectorData[offset + 0x40] & 0xFF) | ((sectorData[offset + 0x41] & 0xFF) << 8));

                System.out.printf("Entry at offset %d: nameLength = %d bytes\n", offset, nameLength);
                System.out.print("Raw bytes: ");
                for (int i = 0; i < 64; i++) {
                    System.out.printf("%02X ", sectorData[offset + i]);
                }
                System.out.println();

                if (nameLength >= 2 && nameLength <= 64 && nameLength % 2 == 0) {
                    try {
                        String name = new String(sectorData, offset, nameLength - 2, "UTF-16LE");
                        System.out.println("Decoded: " + name);
                        names.add(name);
                    } catch (Exception e) {
                        System.out.println("Decode error: " + e.getMessage());
                    }
                }
                System.out.println();
                */

                // System.out.printf("[%d] '%s'\n", i, name);
            }
        }

        return names;
    }

    static Map<String, StreamData> getStreams(RandomAccessFile raf, List<Integer> dirSectors, int sectorSize) throws IOException {
        Map<String, StreamData> streams = new LinkedHashMap<>();
        byte[] sectorData = new byte[sectorSize];

        for (int sector : dirSectors) {
            raf.seek(HEADER_SIZE + (long) sector * sectorSize);
            raf.readFully(sectorData);

            for (int offset = 0; offset + DIR_ENTRY_SIZE <= sectorSize; offset += DIR_ENTRY_SIZE) {
                ByteBuffer entry = ByteBuffer.wrap(sectorData, offset, DIR_ENTRY_SIZE).order(ByteOrder.LITTLE_ENDIAN);
                int nameLength = entry.getShort(64) & 0xFFFF;

                if (nameLength < 2 || nameLength > 64 || nameLength % 2 != 0) continue;

                String name = new String(sectorData, offset, nameLength - 2, "UTF-16LE");

                int type = entry.get(offset + 66); // object type (2 = stream)
                if (type != 2) continue;

                int startSector = entry.getInt(116); // offset 0x74
                int size = entry.getInt(120);        // offset 0x78

                StreamData data = new StreamData();
                data.name = name;
                data.startSector = startSector;
                data.size = size;

                streams.put(name, data);
            }
        }

        return streams;
    }

    @Data
    @Accessors(chain = true)
    static class StreamData {
        String name;
        int startSector;
        int size;
    }

}
