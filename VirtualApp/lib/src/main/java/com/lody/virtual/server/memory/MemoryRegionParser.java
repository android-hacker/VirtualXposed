package com.lody.virtual.server.memory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lody
 */
public class MemoryRegionParser {
    /**
     * Regular Expression for /proc/self/maps line is
     * <p>
     * ([0-9a-f]+)  -   ([0-9a-f]+)  \s  ([r-]) ([w-]) ([x-]) ([sp])   \s    ([0-9a-f]+)   \s  ([0-9a-f]+) :   ([0-9a-f]+)     \s  (\d+)   \s?  (.*)
     * StartAddress        EndAddress    Read   Write  Execute Shared        Filemap Offset   Major devnum    Minor devnum         Inode       Description
     */
    public static final String PATTERN = "([0-9a-f]+)-([0-9a-f]+)\\s([r-])([w-])([x-])([sp])\\s([0-9a-f]+)\\s([0-9a-f]+):([0-9a-f]+)\\s(\\d+)\\s?(.*)";
    public final static Pattern MAPS_LINE_PATTERN = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);

    private static long parseHex(String s) {
        return Long.parseLong(s, 16);
    }

    private static MappedMemoryRegion parseMapLine(String line) {
        line = line.trim();
        Matcher m = MAPS_LINE_PATTERN.matcher(line);
        if (!m.matches()) {
            throw new IllegalArgumentException(String.format("The provided line does not match the pattern for /proc/$pid/maps lines. Given: %s", line));
        }

        if (m.groupCount() != 11) // group(0) not included in this.
        {
            throw new InternalError(String.format(Locale.ENGLISH, "Invalid group count: Found %d, but expected %d", m.groupCount(), 12));
        }

        long start = parseHex(m.group(1));
        long end = parseHex(m.group(2));
        boolean read = m.group(3).equals("r");
        boolean write = m.group(4).equals("w");
        boolean exec = m.group(5).equals("x");
        boolean shared = m.group(6).equals("s");
        long fileOffset = parseHex(m.group(7));
        long majorDevNum = parseHex(m.group(8));
        long minorDevNum = parseHex(m.group(9));
        long inode = parseHex(m.group(10));
        String desc = m.group(11);

        return new MappedMemoryRegion(start, end, read, write, exec, shared, fileOffset, majorDevNum, minorDevNum, inode, desc);
    }


    public static List<MappedMemoryRegion> getMemoryRegions(int pid) throws IOException {
        List<MappedMemoryRegion> list = new LinkedList<>();
        BufferedReader reader = new BufferedReader(new FileReader(String.format(Locale.ENGLISH, "/proc/%d/maps", pid)));
        String line;
        while ((line = reader.readLine()) != null) {
            MappedMemoryRegion region = parseMapLine(line);
            if (region.isReadable && region.isWritable && !region.description.endsWith("(deleted)")) {
                list.add(region);
            }
        }
        return list;
    }
}