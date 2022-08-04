package com.lody.virtual.server.memory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;

/**
 * @author Lody
 */
public class ProcessMemory {

    private int pid;
    private RandomAccessFile memFile;

    public ProcessMemory(int pid) throws IOException {
        this.pid = pid;
        this.memFile = new RandomAccessFile(String.format(Locale.ENGLISH, "/proc/%d/mem", pid), "rw");
    }

    public void write(long offset, byte[] bytes) throws IOException {
        memFile.seek(offset);
        memFile.write(bytes);
    }

    public int read(long offset, byte[] bytes, int len) throws IOException {
        memFile.seek(offset);
        return memFile.read(bytes, 0, len);
    }

    public void close() throws IOException {
        memFile.close();
    }
}
