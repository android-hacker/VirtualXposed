package com.lody.virtual.server.memory;

import com.lody.virtual.helper.utils.VLog;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Lody
 */
public class MemoryScanEngine {

    private List<MappedMemoryRegion> regions;

    private int pid;
    private ProcessMemory memory;
    private static final int PAGE = 4096;
    private List<Match> matches;

    public MemoryScanEngine(int pid) throws IOException {
        this.pid = pid;
        this.memory = new ProcessMemory(pid);
        updateMemoryLayout();
    }

    public void updateMemoryLayout() {
        try {
            regions = MemoryRegionParser.getMemoryRegions(pid);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<Match> getMatches() {
        return matches;
    }

    public void search(MemoryValue value) throws IOException {
        matches = new LinkedList<>();
        byte[] bytes = new byte[PAGE];
        byte[] valueBytes = value.toBytes();
        for (MappedMemoryRegion region : regions) {
            long start = region.startAddress;
            long end = region.endAddress;
            try {
                while (start < end) {
                    int read = Math.min(bytes.length, (int) (end - start));
                    read = memory.read(start, bytes, read);
                    matches.addAll(matchBytes(region, start, bytes, read, valueBytes));
                    start += PAGE;
                }
            } catch (IOException e) {
                VLog.e(getClass().getSimpleName(), "Unable to read region : " + region.description);
            }
        }
    }

    public void modify(Match match, MemoryValue value) throws IOException {
        memory.write(match.address, value.toBytes());
    }

    public void modifyAll(MemoryValue value) throws IOException {
        for (Match match : matches) {
            modify(match, value);
        }
    }

    public class Match {
        MappedMemoryRegion region;
        long address;
        int len;

        public Match(MappedMemoryRegion region, long address, int len) {
            this.region = region;
            this.address = address;
            this.len = len;
        }
    }


    private List<Match> matchBytes(MappedMemoryRegion region, long startAddress, byte[] page, int read, byte[] value) {
        List<Match> matches = new LinkedList<>();
        int start = 0;
        int len = value.length;
        int step = 2;
        while (start < read) {
            boolean match = true;
            for (int i = 0; i < len && i + start < read; i++) {
                if (page[start + i] != value[i]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                matches.add(new Match(region, startAddress + start, len));
            }
            start += step;
        }
        return matches;
    }


    public void close() {
        try {
            memory.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
