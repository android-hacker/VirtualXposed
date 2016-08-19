/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.io;

import android.system.ErrnoException;
import android.system.GaiException;
import android.system.StructAddrinfo;
import android.system.StructFlock;
import android.system.StructGroupReq;
import android.system.StructGroupSourceReq;
import android.system.StructLinger;
import android.system.StructPasswd;
import android.system.StructPollfd;
import android.system.StructStat;
import android.system.StructStatVfs;
import android.system.StructTimeval;
import android.system.StructUcred;
import android.system.StructUtsname;
import android.util.MutableInt;
import android.util.MutableLong;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * Subclass this if you want to override some {@link Os} methods but otherwise delegate.
 */
public class ForwardingOs implements Os {
    protected final Os os;

    public ForwardingOs(Os os) {
        this.os = os;
    }

    public FileDescriptor accept(FileDescriptor fd, InetSocketAddress peerAddress) throws ErrnoException, SocketException { return os.accept(fd, peerAddress); }
    public boolean access(String path, int mode) throws ErrnoException { return os.access(path, mode); }
    public InetAddress[] android_getaddrinfo(String node, StructAddrinfo hints, int netId) throws GaiException { return os.android_getaddrinfo(node, hints, netId); }
    public void bind(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException { os.bind(fd, address, port); }
    public void bind(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException { os.bind(fd, address); }
    public void chmod(String path, int mode) throws ErrnoException { os.chmod(path, mode); }
    public void chown(String path, int uid, int gid) throws ErrnoException { os.chown(path, uid, gid); }
    public void close(FileDescriptor fd) throws ErrnoException { os.close(fd); }
    public void connect(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException { os.connect(fd, address, port); }
    public void connect(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException { os.connect(fd, address); }
    public FileDescriptor dup(FileDescriptor oldFd) throws ErrnoException { return os.dup(oldFd); }
    public FileDescriptor dup2(FileDescriptor oldFd, int newFd) throws ErrnoException { return os.dup2(oldFd, newFd); }
    public String[] environ() { return os.environ(); }
    public void execv(String filename, String[] argv) throws ErrnoException { os.execv(filename, argv); }
    public void execve(String filename, String[] argv, String[] envp) throws ErrnoException { os.execve(filename, argv, envp); }
    public void fchmod(FileDescriptor fd, int mode) throws ErrnoException { os.fchmod(fd, mode); }
    public void fchown(FileDescriptor fd, int uid, int gid) throws ErrnoException { os.fchown(fd, uid, gid); }
    public int fcntlFlock(FileDescriptor fd, int cmd, StructFlock arg) throws ErrnoException, InterruptedIOException { return os.fcntlFlock(fd, cmd, arg); }
    public int fcntlInt(FileDescriptor fd, int cmd, int arg) throws ErrnoException { return os.fcntlInt(fd, cmd, arg); }
    public int fcntlVoid(FileDescriptor fd, int cmd) throws ErrnoException { return os.fcntlVoid(fd, cmd); }
    public void fdatasync(FileDescriptor fd) throws ErrnoException { os.fdatasync(fd); }
    public StructStat fstat(FileDescriptor fd) throws ErrnoException { return os.fstat(fd); }
    public StructStatVfs fstatvfs(FileDescriptor fd) throws ErrnoException { return os.fstatvfs(fd); }
    public void fsync(FileDescriptor fd) throws ErrnoException { os.fsync(fd); }
    public void ftruncate(FileDescriptor fd, long length) throws ErrnoException { os.ftruncate(fd, length); }
    public String gai_strerror(int error) { return os.gai_strerror(error); }
    public int getegid() { return os.getegid(); }
    public int geteuid() { return os.geteuid(); }
    public int getgid() { return os.getgid(); }
    public String getenv(String name) { return os.getenv(name); }
    public String getnameinfo(InetAddress address, int flags) throws GaiException { return os.getnameinfo(address, flags); }
    public SocketAddress getpeername(FileDescriptor fd) throws ErrnoException { return os.getpeername(fd); }
    public int getpgid(int pid) throws ErrnoException { return os.getpgid(pid); }
    public int getpid() { return os.getpid(); }
    public int getppid() { return os.getppid(); }
    public StructPasswd getpwnam(String name) throws ErrnoException { return os.getpwnam(name); }
    public StructPasswd getpwuid(int uid) throws ErrnoException { return os.getpwuid(uid); }
    public SocketAddress getsockname(FileDescriptor fd) throws ErrnoException { return os.getsockname(fd); }
    public int getsockoptByte(FileDescriptor fd, int level, int option) throws ErrnoException { return os.getsockoptByte(fd, level, option); }
    public InetAddress getsockoptInAddr(FileDescriptor fd, int level, int option) throws ErrnoException { return os.getsockoptInAddr(fd, level, option); }
    public int getsockoptInt(FileDescriptor fd, int level, int option) throws ErrnoException { return os.getsockoptInt(fd, level, option); }
    public StructLinger getsockoptLinger(FileDescriptor fd, int level, int option) throws ErrnoException { return os.getsockoptLinger(fd, level, option); }
    public StructTimeval getsockoptTimeval(FileDescriptor fd, int level, int option) throws ErrnoException { return os.getsockoptTimeval(fd, level, option); }
    public StructUcred getsockoptUcred(FileDescriptor fd, int level, int option) throws ErrnoException { return os.getsockoptUcred(fd, level, option); }
    public int gettid() { return os.gettid(); }
    public int getuid() { return os.getuid(); }
    public int getxattr(String path, String name, byte[] outValue) throws ErrnoException { return os.getxattr(path, name, outValue); }
    public String if_indextoname(int index) { return os.if_indextoname(index); }
    public InetAddress inet_pton(int family, String address) { return os.inet_pton(family, address); }
    public InetAddress ioctlInetAddress(FileDescriptor fd, int cmd, String interfaceName) throws ErrnoException { return os.ioctlInetAddress(fd, cmd, interfaceName); }
    public int ioctlInt(FileDescriptor fd, int cmd, MutableInt arg) throws ErrnoException { return os.ioctlInt(fd, cmd, arg); }
    public boolean isatty(FileDescriptor fd) { return os.isatty(fd); }
    public void kill(int pid, int signal) throws ErrnoException { os.kill(pid, signal); }
    public void lchown(String path, int uid, int gid) throws ErrnoException { os.lchown(path, uid, gid); }
    public void link(String oldPath, String newPath) throws ErrnoException { os.link(oldPath, newPath); }
    public void listen(FileDescriptor fd, int backlog) throws ErrnoException { os.listen(fd, backlog); }
    public long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException { return os.lseek(fd, offset, whence); }
    public StructStat lstat(String path) throws ErrnoException { return os.lstat(path); }
    public void mincore(long address, long byteCount, byte[] vector) throws ErrnoException { os.mincore(address, byteCount, vector); }
    public void mkdir(String path, int mode) throws ErrnoException { os.mkdir(path, mode); }
    public void mkfifo(String path, int mode) throws ErrnoException { os.mkfifo(path, mode); }
    public void mlock(long address, long byteCount) throws ErrnoException { os.mlock(address, byteCount); }
    public long mmap(long address, long byteCount, int prot, int flags, FileDescriptor fd, long offset) throws ErrnoException { return os.mmap(address, byteCount, prot, flags, fd, offset); }
    public void msync(long address, long byteCount, int flags) throws ErrnoException { os.msync(address, byteCount, flags); }
    public void munlock(long address, long byteCount) throws ErrnoException { os.munlock(address, byteCount); }
    public void munmap(long address, long byteCount) throws ErrnoException { os.munmap(address, byteCount); }
    public FileDescriptor open(String path, int flags, int mode) throws ErrnoException { return os.open(path, flags, mode); }
    public FileDescriptor[] pipe2(int flags) throws ErrnoException { return os.pipe2(flags); }
    public int poll(StructPollfd[] fds, int timeoutMs) throws ErrnoException { return os.poll(fds, timeoutMs); }
    public void posix_fallocate(FileDescriptor fd, long offset, long length) throws ErrnoException { os.posix_fallocate(fd, offset, length); }
    public int prctl(int option, long arg2, long arg3, long arg4, long arg5) throws ErrnoException { return os.prctl(option, arg2, arg3, arg4, arg5); };
    public int pread(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException { return os.pread(fd, buffer, offset); }
    public int pread(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException { return os.pread(fd, bytes, byteOffset, byteCount, offset); }
    public int pwrite(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException { return os.pwrite(fd, buffer, offset); }
    public int pwrite(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException { return os.pwrite(fd, bytes, byteOffset, byteCount, offset); }
    public int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException { return os.read(fd, buffer); }
    public int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException { return os.read(fd, bytes, byteOffset, byteCount); }
    public String readlink(String path) throws ErrnoException { return os.readlink(path); }
    public int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException { return os.readv(fd, buffers, offsets, byteCounts); }
    public int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException { return os.recvfrom(fd, buffer, flags, srcAddress); }
    public int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException { return os.recvfrom(fd, bytes, byteOffset, byteCount, flags, srcAddress); }
    public void remove(String path) throws ErrnoException { os.remove(path); }
    public void removexattr(String path, String name) throws ErrnoException { os.removexattr(path, name); }
    public void rename(String oldPath, String newPath) throws ErrnoException { os.rename(oldPath, newPath); }
    public long sendfile(FileDescriptor outFd, FileDescriptor inFd, MutableLong inOffset, long byteCount) throws ErrnoException { return os.sendfile(outFd, inFd, inOffset, byteCount); }
    public int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException { return os.sendto(fd, buffer, flags, inetAddress, port); }
    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException { return os.sendto(fd, bytes, byteOffset, byteCount, flags, inetAddress, port); }
    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, SocketAddress address) throws ErrnoException, SocketException { return os.sendto(fd, bytes, byteOffset, byteCount, flags, address); }
    public void setegid(int egid) throws ErrnoException { os.setegid(egid); }
    public void setenv(String name, String value, boolean overwrite) throws ErrnoException { os.setenv(name, value, overwrite); }
    public void seteuid(int euid) throws ErrnoException { os.seteuid(euid); }
    public void setgid(int gid) throws ErrnoException { os.setgid(gid); }
    public void setpgid(int pid, int pgid) throws ErrnoException { os.setpgid(pid, pgid); }
    public void setregid(int rgid, int egid) throws ErrnoException { os.setregid(rgid, egid); }
    public void setreuid(int ruid, int euid) throws ErrnoException { os.setregid(ruid, euid); }
    public int setsid() throws ErrnoException { return os.setsid(); }
    public void setsockoptByte(FileDescriptor fd, int level, int option, int value) throws ErrnoException { os.setsockoptByte(fd, level, option, value); }
    public void setsockoptIfreq(FileDescriptor fd, int level, int option, String value) throws ErrnoException { os.setsockoptIfreq(fd, level, option, value); }
    public void setsockoptInt(FileDescriptor fd, int level, int option, int value) throws ErrnoException { os.setsockoptInt(fd, level, option, value); }
    public void setsockoptIpMreqn(FileDescriptor fd, int level, int option, int value) throws ErrnoException { os.setsockoptIpMreqn(fd, level, option, value); }
    public void setsockoptGroupReq(FileDescriptor fd, int level, int option, StructGroupReq value) throws ErrnoException { os.setsockoptGroupReq(fd, level, option, value); }
    public void setsockoptGroupSourceReq(FileDescriptor fd, int level, int option, StructGroupSourceReq value) throws ErrnoException { os.setsockoptGroupSourceReq(fd, level, option, value); }
    public void setsockoptLinger(FileDescriptor fd, int level, int option, StructLinger value) throws ErrnoException { os.setsockoptLinger(fd, level, option, value); }
    public void setsockoptTimeval(FileDescriptor fd, int level, int option, StructTimeval value) throws ErrnoException { os.setsockoptTimeval(fd, level, option, value); }
    public void setuid(int uid) throws ErrnoException { os.setuid(uid); }
    public void setxattr(String path, String name, byte[] value, int flags) throws ErrnoException { os.setxattr(path, name, value, flags); }
    public void shutdown(FileDescriptor fd, int how) throws ErrnoException { os.shutdown(fd, how); }
    public FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException { return os.socket(domain, type, protocol); }
    public void socketpair(int domain, int type, int protocol, FileDescriptor fd1, FileDescriptor fd2) throws ErrnoException { os.socketpair(domain, type, protocol, fd1, fd2); }
    public StructStat stat(String path) throws ErrnoException { return os.stat(path); }
    public StructStatVfs statvfs(String path) throws ErrnoException { return os.statvfs(path); }
    public String strerror(int errno) { return os.strerror(errno); }
    public String strsignal(int signal) { return os.strsignal(signal); }
    public void symlink(String oldPath, String newPath) throws ErrnoException { os.symlink(oldPath, newPath); }
    public long sysconf(int name) { return os.sysconf(name); }
    public void tcdrain(FileDescriptor fd) throws ErrnoException { os.tcdrain(fd); }
    public void tcsendbreak(FileDescriptor fd, int duration) throws ErrnoException { os.tcsendbreak(fd, duration); }
    public int umask(int mask) { return os.umask(mask); }
    public StructUtsname uname() { return os.uname(); }
    public void unsetenv(String name) throws ErrnoException { os.unsetenv(name); }
    public int waitpid(int pid, MutableInt status, int options) throws ErrnoException { return os.waitpid(pid, status, options); }
    public int write(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException { return os.write(fd, buffer); }
    public int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException { return os.write(fd, bytes, byteOffset, byteCount); }
    public int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException { return os.writev(fd, buffers, offsets, byteCounts); }
}
