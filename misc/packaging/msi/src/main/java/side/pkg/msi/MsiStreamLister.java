package side.pkg.msi;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.*;

public class MsiStreamLister {
    static final int HEADER_SIZE = 512;
    static final int DIR_ENTRY_SIZE = 128;

    public static void main(String[] args) throws IOException {
        File msi = new File("/home/toor/Downloads/7z2409-x64.msi");
        try (RandomAccessFile raf = new RandomAccessFile(msi, "r")) {
            byte[] header = new byte[HEADER_SIZE];
            raf.readFully(header);
            ByteBuffer hdr = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);

            int sectorSize = 1 << hdr.getShort(0x1E);
            int dirSectorStart = hdr.getInt(0x30);

            List<Integer> fat = readFAT(raf, hdr, sectorSize);
            List<Integer> dirSectors = followChain(fat, dirSectorStart);

            Set<String> streamNames = new LinkedHashSet<>();
            for (int sectorNum : dirSectors) {
                long offset = HEADER_SIZE + (long) sectorNum * sectorSize;
                raf.seek(offset);
                byte[] sector = new byte[sectorSize];
                raf.readFully(sector);

                for (int i = 0; i + DIR_ENTRY_SIZE <= sector.length; i += DIR_ENTRY_SIZE) {
                    ByteBuffer entry = ByteBuffer.wrap(sector, i, DIR_ENTRY_SIZE).order(ByteOrder.LITTLE_ENDIAN);
                    int nameLen = entry.getShort(0x40); // Length in bytes
                    if (nameLen < 2 || nameLen > 64) continue;
                    String name = new String(sector, i, nameLen - 2, Charset.forName("UTF-16LE"));
                    streamNames.add(name);
                }
            }

            for (String name : streamNames) {
                System.out.println(name);
            }
        }
    }

    static List<Integer> readFAT(RandomAccessFile raf, ByteBuffer hdr, int sectorSize) throws IOException {
        int numFATSectors = hdr.getInt(0x2C);
        int[] fatSectorIndices = new int[numFATSectors];
        for (int i = 0; i < numFATSectors; i++) {
            fatSectorIndices[i] = hdr.getInt(0x4C + i * 4);
        }

        List<Integer> fat = new ArrayList<>();
        for (int sectorIdx : fatSectorIndices) {
            long offset = HEADER_SIZE + (long) sectorIdx * sectorSize;
            raf.seek(offset);
            byte[] data = new byte[sectorSize];
            raf.readFully(data);
            ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            while (buf.hasRemaining()) fat.add(buf.getInt());
        }
        return fat;
    }

    static List<Integer> followChain(List<Integer> fat, int start) {
        List<Integer> chain = new ArrayList<>();
        int current = start;
        while (current >= 0 && current < fat.size()) {
            chain.add(current);
            int next = fat.get(current);
            if (next == -2 || next == -1 || chain.contains(next)) break;
            current = next;
        }
        return chain;
    }
}
