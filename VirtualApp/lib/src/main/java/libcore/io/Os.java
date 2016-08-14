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

public interface Os {
    FileDescriptor accept(FileDescriptor fd, InetSocketAddress peerAddress) throws ErrnoException, SocketException;
    boolean access(String path, int mode) throws ErrnoException;
    InetAddress[] android_getaddrinfo(String node, StructAddrinfo hints, int netId) throws GaiException;
    void bind(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException;
    void bind(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException;
    void chmod(String path, int mode) throws ErrnoException;
    void chown(String path, int uid, int gid) throws ErrnoException;
    void close(FileDescriptor fd) throws ErrnoException;
    void connect(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException;
    void connect(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException;
    FileDescriptor dup(FileDescriptor oldFd) throws ErrnoException;
    FileDescriptor dup2(FileDescriptor oldFd, int newFd) throws ErrnoException;
    String[] environ();
    void execv(String filename, String[] argv) throws ErrnoException;
    void execve(String filename, String[] argv, String[] envp) throws ErrnoException;
    void fchmod(FileDescriptor fd, int mode) throws ErrnoException;
    void fchown(FileDescriptor fd, int uid, int gid) throws ErrnoException;
    int fcntlFlock(FileDescriptor fd, int cmd, StructFlock arg) throws ErrnoException, InterruptedIOException;
    int fcntlInt(FileDescriptor fd, int cmd, int arg) throws ErrnoException;
    int fcntlVoid(FileDescriptor fd, int cmd) throws ErrnoException;
    void fdatasync(FileDescriptor fd) throws ErrnoException;
    StructStat fstat(FileDescriptor fd) throws ErrnoException;
    StructStatVfs fstatvfs(FileDescriptor fd) throws ErrnoException;
    void fsync(FileDescriptor fd) throws ErrnoException;
    void ftruncate(FileDescriptor fd, long length) throws ErrnoException;
    String gai_strerror(int error);
    int getegid();
    int geteuid();
    int getgid();
    String getenv(String name);
    String getnameinfo(InetAddress address, int flags) throws GaiException;
    SocketAddress getpeername(FileDescriptor fd) throws ErrnoException;
    int getpgid(int pid) throws ErrnoException;
    int getpid();
    int getppid();
    StructPasswd getpwnam(String name) throws ErrnoException;
    StructPasswd getpwuid(int uid) throws ErrnoException;
    SocketAddress getsockname(FileDescriptor fd) throws ErrnoException;
    int getsockoptByte(FileDescriptor fd, int level, int option) throws ErrnoException;
    InetAddress getsockoptInAddr(FileDescriptor fd, int level, int option) throws ErrnoException;
    int getsockoptInt(FileDescriptor fd, int level, int option) throws ErrnoException;
    StructLinger getsockoptLinger(FileDescriptor fd, int level, int option) throws ErrnoException;
    StructTimeval getsockoptTimeval(FileDescriptor fd, int level, int option) throws ErrnoException;
    StructUcred getsockoptUcred(FileDescriptor fd, int level, int option) throws ErrnoException;
    int gettid();
    int getuid();
    int getxattr(String path, String name, byte[] outValue) throws ErrnoException;
    String if_indextoname(int index);
    InetAddress inet_pton(int family, String address);
    InetAddress ioctlInetAddress(FileDescriptor fd, int cmd, String interfaceName) throws ErrnoException;
    int ioctlInt(FileDescriptor fd, int cmd, MutableInt arg) throws ErrnoException;
    boolean isatty(FileDescriptor fd);
    void kill(int pid, int signal) throws ErrnoException;
    void lchown(String path, int uid, int gid) throws ErrnoException;
    void link(String oldPath, String newPath) throws ErrnoException;
    void listen(FileDescriptor fd, int backlog) throws ErrnoException;
    long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException;
    StructStat lstat(String path) throws ErrnoException;
    void mincore(long address, long byteCount, byte[] vector) throws ErrnoException;
    void mkdir(String path, int mode) throws ErrnoException;
    void mkfifo(String path, int mode) throws ErrnoException;
    void mlock(long address, long byteCount) throws ErrnoException;
    long mmap(long address, long byteCount, int prot, int flags, FileDescriptor fd, long offset) throws ErrnoException;
    void msync(long address, long byteCount, int flags) throws ErrnoException;
    void munlock(long address, long byteCount) throws ErrnoException;
    void munmap(long address, long byteCount) throws ErrnoException;
    FileDescriptor open(String path, int flags, int mode) throws ErrnoException;
    FileDescriptor[] pipe2(int flags) throws ErrnoException;
    /* TODO: if we used the non-standard ppoll(2) behind the scenes, we could take a long timeout. */
    int poll(StructPollfd[] fds, int timeoutMs) throws ErrnoException;
    void posix_fallocate(FileDescriptor fd, long offset, long length) throws ErrnoException;
    int prctl(int option, long arg2, long arg3, long arg4, long arg5) throws ErrnoException;
    int pread(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException;
    int pread(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException;
    int pwrite(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException;
    int pwrite(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException;
    int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException;
    int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException;
    String readlink(String path) throws ErrnoException;
    int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException;
    int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException;
    int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException;
    void remove(String path) throws ErrnoException;
    void removexattr(String path, String name) throws ErrnoException;
    void rename(String oldPath, String newPath) throws ErrnoException;
    int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException;
    int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException;
    int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, SocketAddress address) throws ErrnoException, SocketException;
    long sendfile(FileDescriptor outFd, FileDescriptor inFd, MutableLong inOffset, long byteCount) throws ErrnoException;
    void setegid(int egid) throws ErrnoException;
    void setenv(String name, String value, boolean overwrite) throws ErrnoException;
    void seteuid(int euid) throws ErrnoException;
    void setgid(int gid) throws ErrnoException;
    void setpgid(int pid, int pgid) throws ErrnoException;
    void setregid(int rgid, int egid) throws ErrnoException;
    void setreuid(int ruid, int euid) throws ErrnoException;
    int setsid() throws ErrnoException;
    void setsockoptByte(FileDescriptor fd, int level, int option, int value) throws ErrnoException;
    void setsockoptIfreq(FileDescriptor fd, int level, int option, String value) throws ErrnoException;
    void setsockoptInt(FileDescriptor fd, int level, int option, int value) throws ErrnoException;
    void setsockoptIpMreqn(FileDescriptor fd, int level, int option, int value) throws ErrnoException;
    void setsockoptGroupReq(FileDescriptor fd, int level, int option, StructGroupReq value) throws ErrnoException;
    void setsockoptGroupSourceReq(FileDescriptor fd, int level, int option, StructGroupSourceReq value) throws ErrnoException;
    void setsockoptLinger(FileDescriptor fd, int level, int option, StructLinger value) throws ErrnoException;
    void setsockoptTimeval(FileDescriptor fd, int level, int option, StructTimeval value) throws ErrnoException;
    void setuid(int uid) throws ErrnoException;
    void setxattr(String path, String name, byte[] value, int flags) throws ErrnoException;
    void shutdown(FileDescriptor fd, int how) throws ErrnoException;
    FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException;
    void socketpair(int domain, int type, int protocol, FileDescriptor fd1, FileDescriptor fd2) throws ErrnoException;
    StructStat stat(String path) throws ErrnoException;
    StructStatVfs statvfs(String path) throws ErrnoException;
    String strerror(int errno);
    String strsignal(int signal);
    void symlink(String oldPath, String newPath) throws ErrnoException;
    long sysconf(int name);
    void tcdrain(FileDescriptor fd) throws ErrnoException;
    void tcsendbreak(FileDescriptor fd, int duration) throws ErrnoException;
    int umask(int mask);
    StructUtsname uname();
    void unsetenv(String name) throws ErrnoException;
    int waitpid(int pid, MutableInt status, int options) throws ErrnoException;
    int write(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException;
    int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException;
    int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException;
}
