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

public final class Posix implements Os {
    Posix() { }


    @Override
    public FileDescriptor accept(FileDescriptor fd, InetSocketAddress peerAddress) throws ErrnoException, SocketException {
        return null;
    }

    @Override
    public boolean access(String path, int mode) throws ErrnoException {
        return false;
    }

    @Override
    public InetAddress[] android_getaddrinfo(String node, StructAddrinfo hints, int netId) throws GaiException {
        return new InetAddress[0];
    }

    @Override
    public void bind(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException {

    }

    @Override
    public void bind(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException {

    }

    @Override
    public void chmod(String path, int mode) throws ErrnoException {

    }

    @Override
    public void chown(String path, int uid, int gid) throws ErrnoException {

    }

    @Override
    public void close(FileDescriptor fd) throws ErrnoException {

    }

    @Override
    public void connect(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException {

    }

    @Override
    public void connect(FileDescriptor fd, SocketAddress address) throws ErrnoException, SocketException {

    }

    @Override
    public FileDescriptor dup(FileDescriptor oldFd) throws ErrnoException {
        return null;
    }

    @Override
    public FileDescriptor dup2(FileDescriptor oldFd, int newFd) throws ErrnoException {
        return null;
    }

    @Override
    public String[] environ() {
        return new String[0];
    }

    @Override
    public void execv(String filename, String[] argv) throws ErrnoException {

    }

    @Override
    public void execve(String filename, String[] argv, String[] envp) throws ErrnoException {

    }

    @Override
    public void fchmod(FileDescriptor fd, int mode) throws ErrnoException {

    }

    @Override
    public void fchown(FileDescriptor fd, int uid, int gid) throws ErrnoException {

    }

    @Override
    public int fcntlFlock(FileDescriptor fd, int cmd, StructFlock arg) throws ErrnoException, InterruptedIOException {
        return 0;
    }

    @Override
    public int fcntlInt(FileDescriptor fd, int cmd, int arg) throws ErrnoException {
        return 0;
    }

    @Override
    public int fcntlVoid(FileDescriptor fd, int cmd) throws ErrnoException {
        return 0;
    }

    @Override
    public void fdatasync(FileDescriptor fd) throws ErrnoException {

    }

    @Override
    public StructStat fstat(FileDescriptor fd) throws ErrnoException {
        return null;
    }

    @Override
    public StructStatVfs fstatvfs(FileDescriptor fd) throws ErrnoException {
        return null;
    }

    @Override
    public void fsync(FileDescriptor fd) throws ErrnoException {

    }

    @Override
    public void ftruncate(FileDescriptor fd, long length) throws ErrnoException {

    }

    @Override
    public String gai_strerror(int error) {
        return null;
    }

    @Override
    public int getegid() {
        return 0;
    }

    @Override
    public int geteuid() {
        return 0;
    }

    @Override
    public int getgid() {
        return 0;
    }

    @Override
    public String getenv(String name) {
        return null;
    }

    @Override
    public String getnameinfo(InetAddress address, int flags) throws GaiException {
        return null;
    }

    @Override
    public SocketAddress getpeername(FileDescriptor fd) throws ErrnoException {
        return null;
    }

    @Override
    public int getpgid(int pid) throws ErrnoException {
        return 0;
    }

    @Override
    public int getpid() {
        return 0;
    }

    @Override
    public int getppid() {
        return 0;
    }

    @Override
    public StructPasswd getpwnam(String name) throws ErrnoException {
        return null;
    }

    @Override
    public StructPasswd getpwuid(int uid) throws ErrnoException {
        return null;
    }

    @Override
    public SocketAddress getsockname(FileDescriptor fd) throws ErrnoException {
        return null;
    }

    @Override
    public int getsockoptByte(FileDescriptor fd, int level, int option) throws ErrnoException {
        return 0;
    }

    @Override
    public InetAddress getsockoptInAddr(FileDescriptor fd, int level, int option) throws ErrnoException {
        return null;
    }

    @Override
    public int getsockoptInt(FileDescriptor fd, int level, int option) throws ErrnoException {
        return 0;
    }

    @Override
    public StructLinger getsockoptLinger(FileDescriptor fd, int level, int option) throws ErrnoException {
        return null;
    }

    @Override
    public StructTimeval getsockoptTimeval(FileDescriptor fd, int level, int option) throws ErrnoException {
        return null;
    }

    @Override
    public StructUcred getsockoptUcred(FileDescriptor fd, int level, int option) throws ErrnoException {
        return null;
    }

    @Override
    public int gettid() {
        return 0;
    }

    @Override
    public int getuid() {
        return 0;
    }

    @Override
    public int getxattr(String path, String name, byte[] outValue) throws ErrnoException {
        return 0;
    }

    @Override
    public String if_indextoname(int index) {
        return null;
    }

    @Override
    public InetAddress inet_pton(int family, String address) {
        return null;
    }

    @Override
    public InetAddress ioctlInetAddress(FileDescriptor fd, int cmd, String interfaceName) throws ErrnoException {
        return null;
    }

    @Override
    public int ioctlInt(FileDescriptor fd, int cmd, MutableInt arg) throws ErrnoException {
        return 0;
    }

    @Override
    public boolean isatty(FileDescriptor fd) {
        return false;
    }

    @Override
    public void kill(int pid, int signal) throws ErrnoException {

    }

    @Override
    public void lchown(String path, int uid, int gid) throws ErrnoException {

    }

    @Override
    public void link(String oldPath, String newPath) throws ErrnoException {

    }

    @Override
    public void listen(FileDescriptor fd, int backlog) throws ErrnoException {

    }

    @Override
    public long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException {
        return 0;
    }

    @Override
    public StructStat lstat(String path) throws ErrnoException {
        return null;
    }

    @Override
    public void mincore(long address, long byteCount, byte[] vector) throws ErrnoException {

    }

    @Override
    public void mkdir(String path, int mode) throws ErrnoException {

    }

    @Override
    public void mkfifo(String path, int mode) throws ErrnoException {

    }

    @Override
    public void mlock(long address, long byteCount) throws ErrnoException {

    }

    @Override
    public long mmap(long address, long byteCount, int prot, int flags, FileDescriptor fd, long offset) throws ErrnoException {
        return 0;
    }

    @Override
    public void msync(long address, long byteCount, int flags) throws ErrnoException {

    }

    @Override
    public void munlock(long address, long byteCount) throws ErrnoException {

    }

    @Override
    public void munmap(long address, long byteCount) throws ErrnoException {

    }

    @Override
    public FileDescriptor open(String path, int flags, int mode) throws ErrnoException {
        return null;
    }

    @Override
    public FileDescriptor[] pipe2(int flags) throws ErrnoException {
        return new FileDescriptor[0];
    }

    @Override
    public int poll(StructPollfd[] fds, int timeoutMs) throws ErrnoException {
        return 0;
    }

    @Override
    public void posix_fallocate(FileDescriptor fd, long offset, long length) throws ErrnoException {

    }

    @Override
    public int prctl(int option, long arg2, long arg3, long arg4, long arg5) throws ErrnoException {
        return 0;
    }

    @Override
    public int pread(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException {
        return 0;
    }

    @Override
    public int pread(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException {
        return 0;
    }

    @Override
    public int pwrite(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException, InterruptedIOException {
        return 0;
    }

    @Override
    public int pwrite(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset) throws ErrnoException, InterruptedIOException {
        return 0;
    }

    @Override
    public int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException {
        return 0;
    }

    @Override
    public int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException {
        return 0;
    }

    @Override
    public String readlink(String path) throws ErrnoException {
        return null;
    }

    @Override
    public int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException {
        return 0;
    }

    @Override
    public int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
        return 0;
    }

    @Override
    public int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
        return 0;
    }

    @Override
    public void remove(String path) throws ErrnoException {

    }

    @Override
    public void removexattr(String path, String name) throws ErrnoException {

    }

    @Override
    public void rename(String oldPath, String newPath) throws ErrnoException {

    }

    @Override
    public int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
        return 0;
    }

    @Override
    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
        return 0;
    }

    @Override
    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, SocketAddress address) throws ErrnoException, SocketException {
        return 0;
    }

    @Override
    public long sendfile(FileDescriptor outFd, FileDescriptor inFd, MutableLong inOffset, long byteCount) throws ErrnoException {
        return 0;
    }

    @Override
    public void setegid(int egid) throws ErrnoException {

    }

    @Override
    public void setenv(String name, String value, boolean overwrite) throws ErrnoException {

    }

    @Override
    public void seteuid(int euid) throws ErrnoException {

    }

    @Override
    public void setgid(int gid) throws ErrnoException {

    }

    @Override
    public void setpgid(int pid, int pgid) throws ErrnoException {

    }

    @Override
    public void setregid(int rgid, int egid) throws ErrnoException {

    }

    @Override
    public void setreuid(int ruid, int euid) throws ErrnoException {

    }

    @Override
    public int setsid() throws ErrnoException {
        return 0;
    }

    @Override
    public void setsockoptByte(FileDescriptor fd, int level, int option, int value) throws ErrnoException {

    }

    @Override
    public void setsockoptIfreq(FileDescriptor fd, int level, int option, String value) throws ErrnoException {

    }

    @Override
    public void setsockoptInt(FileDescriptor fd, int level, int option, int value) throws ErrnoException {

    }

    @Override
    public void setsockoptIpMreqn(FileDescriptor fd, int level, int option, int value) throws ErrnoException {

    }

    @Override
    public void setsockoptGroupReq(FileDescriptor fd, int level, int option, StructGroupReq value) throws ErrnoException {

    }

    @Override
    public void setsockoptGroupSourceReq(FileDescriptor fd, int level, int option, StructGroupSourceReq value) throws ErrnoException {

    }

    @Override
    public void setsockoptLinger(FileDescriptor fd, int level, int option, StructLinger value) throws ErrnoException {

    }

    @Override
    public void setsockoptTimeval(FileDescriptor fd, int level, int option, StructTimeval value) throws ErrnoException {

    }

    @Override
    public void setuid(int uid) throws ErrnoException {

    }

    @Override
    public void setxattr(String path, String name, byte[] value, int flags) throws ErrnoException {

    }

    @Override
    public void shutdown(FileDescriptor fd, int how) throws ErrnoException {

    }

    @Override
    public FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException {
        return null;
    }

    @Override
    public void socketpair(int domain, int type, int protocol, FileDescriptor fd1, FileDescriptor fd2) throws ErrnoException {

    }

    @Override
    public StructStat stat(String path) throws ErrnoException {
        return null;
    }

    @Override
    public StructStatVfs statvfs(String path) throws ErrnoException {
        return null;
    }

    @Override
    public String strerror(int errno) {
        return null;
    }

    @Override
    public String strsignal(int signal) {
        return null;
    }

    @Override
    public void symlink(String oldPath, String newPath) throws ErrnoException {

    }

    @Override
    public long sysconf(int name) {
        return 0;
    }

    @Override
    public void tcdrain(FileDescriptor fd) throws ErrnoException {

    }

    @Override
    public void tcsendbreak(FileDescriptor fd, int duration) throws ErrnoException {

    }

    @Override
    public int umask(int mask) {
        return 0;
    }

    @Override
    public StructUtsname uname() {
        return null;
    }

    @Override
    public void unsetenv(String name) throws ErrnoException {

    }

    @Override
    public int waitpid(int pid, MutableInt status, int options) throws ErrnoException {
        return 0;
    }

    @Override
    public int write(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException, InterruptedIOException {
        return 0;
    }

    @Override
    public int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount) throws ErrnoException, InterruptedIOException {
        return 0;
    }

    @Override
    public int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts) throws ErrnoException, InterruptedIOException {
        return 0;
    }
}
