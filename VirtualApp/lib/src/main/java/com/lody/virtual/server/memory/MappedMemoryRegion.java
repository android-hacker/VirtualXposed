package com.lody.virtual.server.memory;

/**
 * @author Lody
 */
public class MappedMemoryRegion {

    public static class FileMapping {
        public final long offset;
        public final long majorDeviceNumber;
        public final long minorDeviceNumber;
        public final long inode;

        public FileMapping(long off, long major, long minor, long inode) {
            this.offset = off;
            this.majorDeviceNumber = major;
            this.minorDeviceNumber = minor;
            this.inode = inode;
        }
    }

    public final long startAddress;
    public final long endAddress;
    public final boolean isReadable;
    public final boolean isWritable;
    public final boolean isExecutable;
    public final boolean isShared;

    public final FileMapping fileMapInfo;

    public final String description;

    public MappedMemoryRegion(long start, long end, boolean read, boolean write, boolean exec, boolean shared, long off, long majorDevNum, long minorDevNum, long inode, String desc) {
        this.startAddress = start;
        this.endAddress = end;
        this.isReadable = read;
        this.isWritable = write;
        this.isExecutable = exec;
        this.isShared = shared;
        this.fileMapInfo = (inode == 0) ? null : new FileMapping(off, majorDevNum, minorDevNum, inode);
        this.description = desc;
    }

    public boolean isMappedFromFile() {
        return this.fileMapInfo != null;
    }
}