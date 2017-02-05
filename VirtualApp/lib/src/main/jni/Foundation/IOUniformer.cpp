//
// VirtualApp Native Project
//
#include <InlineHook/util.h>
#include "IOUniformer.h"

static std::map<std::string/*orig_path*/, std::string/*new_path*/> IORedirectMap;
static std::map<std::string/*orig_path*/, std::string/*new_path*/> RootIORedirectMap;


static inline void
hook_template(const char *lib_so, const char *symbol, void *new_func, void **old_func) {
    void *handle = dlopen(lib_so, RTLD_GLOBAL | RTLD_LAZY);
    if (handle == NULL) {
        LOGW("Error: unable to find the SO : %s.", lib_so);
        return;
    }
    void *addr = dlsym(handle, symbol);
    if (addr == NULL) {
        LOGW("Error: unable to find the Symbol : %s.", symbol);
        return;
    }
    inlineHookDirect((unsigned int) (addr), new_func, old_func);
    dlclose(handle);
}


void onSoLoaded(const char *name, void *handle);


static inline bool startWith(const std::string &str, const std::string &prefix) {
    return str.compare(0, prefix.length(), prefix) == 0;
}


static inline bool endWith(const std::string &str, const char &suffix) {
    return *(str.end() - 1) == suffix;
}

static void add_pair(const char *_orig_path, const char *_new_path) {
    std::string origPath = std::string(_orig_path);
    std::string newPath = std::string(_new_path);
    IORedirectMap.insert(std::pair<std::string, std::string>(origPath, newPath));
    if (endWith(origPath, '/')) {
        RootIORedirectMap.insert(
                std::pair<std::string, std::string>(
                        origPath.substr(0, origPath.length() - 1),
                        newPath.substr(0, newPath.length() - 1))
        );
    }
}


const char *match_redirected_path(const char *_path) {
    if (_path == NULL) {
        return NULL;
    }
    std::string path(_path);
    if (path.length() <= 1) {
        return _path;
    }
    std::map<std::string, std::string>::iterator iterator;
    iterator = RootIORedirectMap.find(path);
    if (iterator != RootIORedirectMap.end()) {
        return strdup(iterator->second.c_str());
    }

    for (iterator = IORedirectMap.begin(); iterator != IORedirectMap.end(); iterator++) {
        const std::string &prefix = iterator->first;
        const std::string &new_prefix = iterator->second;
        if (startWith(path, prefix)) {
            std::string new_path = new_prefix + path.substr(prefix.length(), path.length());
            return strdup(new_path.c_str());
        }
    }
    return _path;
}


void IOUniformer::redirect(const char *orig_path, const char *new_path) {
    LOGI("Start redirect : from %s to %s", orig_path, new_path);
    add_pair(orig_path, new_path);
}

const char *IOUniformer::query(const char *orig_path) {
    return match_redirected_path(orig_path);
}


const char *IOUniformer::restore(const char *_path) {
    if (_path == NULL) {
        return NULL;
    }
    std::string path(_path);
    if (path.length() <= 1) {
        return _path;
    }
    std::map<std::string, std::string>::iterator iterator;
    iterator = RootIORedirectMap.find(path);
    if (iterator != RootIORedirectMap.end()) {
        return strdup(iterator->second.c_str());
    }
    for (iterator = RootIORedirectMap.begin(); iterator != RootIORedirectMap.end(); iterator++) {
        const std::string &origin = iterator->first;
        const std::string &redirected = iterator->second;
        if (path == redirected) {
            return strdup(origin.c_str());
        }
    }

    for (iterator = IORedirectMap.begin(); iterator != IORedirectMap.end(); iterator++) {
        const std::string &prefix = iterator->first;
        const std::string &new_prefix = iterator->second;
        if (startWith(path, new_prefix)) {
            std::string origin_path = prefix + path.substr(new_prefix.length(), path.length());
            return strdup(origin_path.c_str());
        }
    }
    return _path;
}


__BEGIN_DECLS



//size_t	 fwrite(const void *, size_t, size_t, FILE *);
HOOK_DEF(size_t, fwrite, const void *data, size_t start, size_t len, FILE *file) {
    return orig_fwrite(data, start, len, file);
}


// int faccessat(int dirfd, const char *pathname, int mode, int flags);
HOOK_DEF(int, faccessat, int dirfd, const char *pathname, int mode, int flags) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_faccessat, dirfd, redirect_path, mode, flags);
    FREE(redirect_path, pathname);
    return ret;
}


// int fchmodat(int dirfd, const char *pathname, mode_t mode, int flags);
HOOK_DEF(int, fchmodat, int dirfd, const char *pathname, mode_t mode, int flags) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_fchmodat, dirfd, redirect_path, mode, flags);
    FREE(redirect_path, pathname);
    return ret;
}
// int fchmod(const char *pathname, mode_t mode);
HOOK_DEF(int, fchmod, const char *pathname, mode_t mode) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_chmod, redirect_path, mode);
    FREE(redirect_path, pathname);
    return ret;
}


// int fstatat(int dirfd, const char *pathname, struct stat *buf, int flags);
HOOK_DEF(int, fstatat, int dirfd, const char *pathname, struct stat *buf, int flags) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_fstatat64, dirfd, redirect_path, buf, flags);
    FREE(redirect_path, pathname);
    return ret;
}
// int fstat(const char *pathname, struct stat *buf, int flags);
HOOK_DEF(int, fstat, const char *pathname, struct stat *buf) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_fstat64, redirect_path, buf);
    FREE(redirect_path, pathname);
    return ret;
}


// int mknodat(int dirfd, const char *pathname, mode_t mode, dev_t dev);
HOOK_DEF(int, mknodat, int dirfd, const char *pathname, mode_t mode, dev_t dev) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_mknodat, dirfd, redirect_path, mode, dev);
    FREE(redirect_path, pathname);
    return ret;
}
// int mknod(const char *pathname, mode_t mode, dev_t dev);
HOOK_DEF(int, mknod, const char *pathname, mode_t mode, dev_t dev) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_mknod, redirect_path, mode, dev);
    FREE(redirect_path, pathname);
    return ret;
}


// int utimensat(int dirfd, const char *pathname, const struct timespec times[2], int flags);
HOOK_DEF(int, utimensat, int dirfd, const char *pathname, const struct timespec times[2],
         int flags) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_utimensat, dirfd, redirect_path, times, flags);
    FREE(redirect_path, pathname);
    return ret;
}


// int fchownat(int dirfd, const char *pathname, uid_t owner, gid_t group, int flags);
HOOK_DEF(int, fchownat, int dirfd, const char *pathname, uid_t owner, gid_t group, int flags) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_fchownat, dirfd, redirect_path, owner, group, flags);
    FREE(redirect_path, pathname);
    return ret;
}

// int chroot(const char *pathname);
HOOK_DEF(int, chroot, const char *pathname) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_chroot, redirect_path);
    FREE(redirect_path, pathname);
    return ret;
}


// int renameat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath);
HOOK_DEF(int, renameat, int olddirfd, const char *oldpath, int newdirfd, const char *newpath) {
    const char *redirect_path_old = match_redirected_path(oldpath);
    const char *redirect_path_new = match_redirected_path(newpath);
    int ret = syscall(__NR_renameat, olddirfd, redirect_path_old, newdirfd, redirect_path_new);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}
// int rename(const char *oldpath, const char *newpath);
HOOK_DEF(int, rename, const char *oldpath, const char *newpath) {
    const char *redirect_path_old = match_redirected_path(oldpath);
    const char *redirect_path_new = match_redirected_path(newpath);
    int ret = syscall(__NR_rename, redirect_path_old, redirect_path_new);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}


// int unlinkat(int dirfd, const char *pathname, int flags);
HOOK_DEF(int, unlinkat, int dirfd, const char *pathname, int flags) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_unlinkat, dirfd, redirect_path, flags);
    FREE(redirect_path, pathname);
    return ret;
}
// int unlink(const char *pathname);
HOOK_DEF(int, unlink, const char *pathname) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_unlink, redirect_path);
    FREE(redirect_path, pathname);
    return ret;
}


// int symlinkat(const char *oldpath, int newdirfd, const char *newpath);
HOOK_DEF(int, symlinkat, const char *oldpath, int newdirfd, const char *newpath) {
    const char *redirect_path_old = match_redirected_path(oldpath);
    const char *redirect_path_new = match_redirected_path(newpath);
    int ret = syscall(__NR_symlinkat, redirect_path_old, newdirfd, redirect_path_new);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}
// int symlink(const char *oldpath, const char *newpath);
HOOK_DEF(int, symlink, const char *oldpath, const char *newpath) {
    const char *redirect_path_old = match_redirected_path(oldpath);
    const char *redirect_path_new = match_redirected_path(newpath);
    int ret = syscall(__NR_symlink, redirect_path_old, redirect_path_new);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}


// int linkat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath, int flags);
HOOK_DEF(int, linkat, int olddirfd, const char *oldpath, int newdirfd, const char *newpath,
         int flags) {
    const char *redirect_path_old = match_redirected_path(oldpath);
    const char *redirect_path_new = match_redirected_path(newpath);
    int ret = syscall(__NR_linkat, olddirfd, redirect_path_old, newdirfd, redirect_path_new, flags);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}
// int link(const char *oldpath, const char *newpath);
HOOK_DEF(int, link, const char *oldpath, const char *newpath) {
    const char *redirect_path_old = match_redirected_path(oldpath);
    const char *redirect_path_new = match_redirected_path(newpath);
    int ret = syscall(__NR_link, redirect_path_old, redirect_path_new);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}


// int utimes(const char *filename, const struct timeval *tvp);
HOOK_DEF(int, utimes, const char *pathname, const struct timeval *tvp) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_utimes, redirect_path, tvp);
    FREE(redirect_path, pathname);
    return ret;
}


// int access(const char *pathname, int mode);
HOOK_DEF(int, access, const char *pathname, int mode) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_access, redirect_path, mode);
    FREE(redirect_path, pathname);
    return ret;
}


// int chmod(const char *path, mode_t mode);
HOOK_DEF(int, chmod, const char *pathname, mode_t mode) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_chmod, redirect_path, mode);
    FREE(redirect_path, pathname);
    return ret;
}


// int chown(const char *path, uid_t owner, gid_t group);
HOOK_DEF(int, chown, const char *pathname, uid_t owner, gid_t group) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_chown, redirect_path, owner, group);
    FREE(redirect_path, pathname);
    return ret;
}


// int lstat(const char *path, struct stat *buf);
HOOK_DEF(int, lstat, const char *pathname, struct stat *buf) {
    char *redirect_path = const_cast<char *>(match_redirected_path(pathname));
    int ret = syscall(__NR_lstat64, redirect_path, buf);
    FREE(redirect_path, pathname);
    return ret;
}


// int stat(const char *path, struct stat *buf);
HOOK_DEF(int, stat, const char *pathname, struct stat *buf) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_stat64, redirect_path, buf);
    FREE(redirect_path, pathname);
    return ret;
}


// int mkdirat(int dirfd, const char *pathname, mode_t mode);
HOOK_DEF(int, mkdirat, int dirfd, const char *pathname, mode_t mode) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_mkdirat, dirfd, redirect_path, mode);
    FREE(redirect_path, pathname);
    return ret;
}
// int mkdir(const char *pathname, mode_t mode);
HOOK_DEF(int, mkdir, const char *pathname, mode_t mode) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_mkdir, redirect_path, mode);
    FREE(redirect_path, pathname);
    return ret;
}


// int rmdir(const char *pathname);
HOOK_DEF(int, rmdir, const char *pathname) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_rmdir, redirect_path);
    FREE(redirect_path, pathname);
    return ret;
}


// int readlinkat(int dirfd, const char *pathname, char *buf, size_t bufsiz);
HOOK_DEF(int, readlinkat, int dirfd, const char *pathname, char *buf, size_t bufsiz) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_readlinkat, dirfd, redirect_path, buf, bufsiz);
    FREE(redirect_path, pathname);
    return ret;
}
// ssize_t readlink(const char *path, char *buf, size_t bufsiz);
HOOK_DEF(ssize_t, readlink, const char *pathname, char *buf, size_t bufsiz) {
    const char *redirect_path = match_redirected_path(pathname);
    ssize_t ret = static_cast<ssize_t>(syscall(__NR_readlink, redirect_path, buf, bufsiz));
    FREE(redirect_path, pathname);
    return ret;
}


// int __statfs64(const char *path, size_t size, struct statfs *stat);
HOOK_DEF(int, __statfs64, const char *pathname, size_t size, struct statfs *stat) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_statfs64, redirect_path, size, stat);
    FREE(redirect_path, pathname);
    return ret;
}


// int truncate(const char *path, off_t length);
HOOK_DEF(int, truncate, const char *pathname, off_t length) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_truncate, redirect_path, length);
    FREE(redirect_path, pathname);
    return ret;
}

// int truncate64(const char *pathname, off_t length);
HOOK_DEF(int, truncate64, const char *pathname, off_t length) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_truncate64, redirect_path, length);
    FREE(redirect_path, pathname);
    return ret;
}


// int chdir(const char *path);
HOOK_DEF(int, chdir, const char *pathname) {
    LOGE("chdir, orig %s", pathname);
    const char *redirect_path = match_redirected_path(pathname);
    LOGE("chdir, new %s", redirect_path);
    int ret = syscall(__NR_chdir, redirect_path);
    FREE(redirect_path, pathname);
    return ret;
}


// int __getcwd(char *buf, size_t size);
HOOK_DEF(int, __getcwd, char *buf, size_t size) {
    const char *redirect_path = match_redirected_path(buf);
    int ret = syscall(__NR_getcwd, redirect_path, static_cast<size_t>(strlen(redirect_path)));
    FREE(redirect_path, buf);
    return ret;
}


// int __openat(int fd, const char *pathname, int flags, int mode);
HOOK_DEF(int, __openat, int fd, const char *pathname, int flags, int mode) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_openat, fd, redirect_path, flags, mode);
    FREE(redirect_path, pathname);
    return ret;
}
// int __open(const char *pathname, int flags, int mode);
HOOK_DEF(int, __open, const char *pathname, int flags, int mode) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_open, redirect_path, flags, mode);
    FREE(redirect_path, pathname);
    return ret;
}

// int lchown(const char *pathname, uid_t owner, gid_t group);
HOOK_DEF(int, lchown, const char *pathname, uid_t owner, gid_t group) {
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_lchown, redirect_path, owner, group);
    FREE(redirect_path, pathname);
    return ret;
}

// int (*origin_execve)(const char *pathname, char *const argv[], char *const envp[]);
HOOK_DEF(int, execve, const char *pathname, char *const argv[], char *const envp[]) {
    LOGD("execve: %s", pathname);
    for (int i = 0; argv[i] != NULL; ++i) {
        LOGD("argv[%i] : %s", i, argv[i]);
    }
    for (int i = 0; envp[i] != NULL; ++i) {
        LOGD("envp[%i] : %s", i, envp[i]);
    }
    const char *redirect_path = match_redirected_path(pathname);
    int ret = syscall(__NR_execve, redirect_path, argv, envp);
    FREE(redirect_path, pathname);
    return ret;
}

HOOK_DEF(void*, dlopen, const char *filename, int flag) {
    const char *redirect_path = match_redirected_path(filename);
    void *ret = orig_dlopen(redirect_path, flag);
    onSoLoaded(filename, ret);
    LOGD("dlopen : %s, return : %p.", redirect_path, ret);
    FREE(redirect_path, filename);
    return ret;
}

HOOK_DEF(void*, do_dlopen_V19, const char *filename, int flag, const void *extinfo) {
    const char *redirect_path = match_redirected_path(filename);
    void *ret = orig_do_dlopen_V19(redirect_path, flag, extinfo);
    onSoLoaded(filename, ret);
    LOGD("do_dlopen : %s, return : %p.", redirect_path, ret);
    FREE(redirect_path, filename);
    return ret;
}

HOOK_DEF(void*, do_dlopen_V24, const char *name, int flags, const void *extinfo,
         void *caller_addr) {
    const char *redirect_path = match_redirected_path(name);
    void *ret = orig_do_dlopen_V24(redirect_path, flags, extinfo, caller_addr);
    onSoLoaded(name, ret);
    LOGD("do_dlopen : %s, return : %p.", redirect_path, ret);
    FREE(redirect_path, name);
    return ret;
}

// int kill(pid_t pid, int sig);
HOOK_DEF(int, kill, pid_t pid, int sig) {
    extern JavaVM *g_vm;
    extern jclass g_jclass;
    JNIEnv *env = NULL;
    g_vm->GetEnv((void **) &env, JNI_VERSION_1_4);
    g_vm->AttachCurrentThread(&env, NULL);
    jmethodID method = env->GetStaticMethodID(g_jclass, "onKillProcess", "(II)V");
    env->CallStaticVoidMethod(g_jclass, method, pid, sig);
    int ret = syscall(__NR_kill, pid, sig);
    return ret;
}

__END_DECLS
// end IO DEF


void onSoLoaded(const char *name, void *handle) {
}


void hook_dlopen(int api_level) {
    void *symbol = NULL;
    if (api_level > 23) {
        if (findSymbol("__dl__Z9do_dlopenPKciPK17android_dlextinfoPv", "linker",
                       (unsigned long *) &symbol) == 0) {
            inlineHookDirect((unsigned int) symbol, (void *) new_do_dlopen_V24,
                             (void **) &orig_do_dlopen_V24);
        }
    } else if (api_level >= 19) {
        if (findSymbol("__dl__Z9do_dlopenPKciPK17android_dlextinfo", "linker",
                       (unsigned long *) &symbol) == 0) {
            inlineHookDirect((unsigned int) symbol, (void *) new_do_dlopen_V19,
                             (void **) &orig_do_dlopen_V19);
        }
    } else {
        if (findSymbol("__dl_dlopen", "linker",
                       (unsigned long *) &symbol) == 0) {
            inlineHookDirect((unsigned int) symbol, (void *) new_dlopen, (void **) &orig_dlopen);
        }
    }

}


extern "C" size_t strlen(const char *str) {
    if (str == NULL) return 0;
    size_t len = 0;
    for (; *str++ != '\0';) {
        len++;
    }
    return len;
}


void IOUniformer::startUniformer(int api_level) {

    HOOK_IO(__getcwd);
    HOOK_IO(chdir);
    HOOK_IO(truncate);
    HOOK_IO(__statfs64);
    HOOK_IO(lchown);
    HOOK_IO(chroot);
    HOOK_IO(truncate64);
    HOOK_IO(kill);
    HOOK_IO(execve);

    if (api_level < ANDROID_L) {
        HOOK_IO(link);
        HOOK_IO(symlink);
        HOOK_IO(readlink);
        HOOK_IO(unlink);
        HOOK_IO(rmdir);
        HOOK_IO(rename);
        HOOK_IO(mkdir);
        HOOK_IO(stat);
        HOOK_IO(lstat);
        HOOK_IO(chown);
        HOOK_IO(chmod);
        HOOK_IO(access);
        HOOK_IO(utimes);
        HOOK_IO(__open);
        HOOK_IO(mknod);
    } else {
        HOOK_IO(__open);
        HOOK_IO(stat);
        HOOK_IO(lstat);
        HOOK_IO(chown);
        HOOK_IO(chmod);
        HOOK_IO(access);
        HOOK_IO(rmdir);
        HOOK_IO(rename);

        HOOK_IO(__openat);
        HOOK_IO(linkat);
        HOOK_IO(unlinkat);
        HOOK_IO(symlinkat);
        HOOK_IO(readlinkat);
        HOOK_IO(renameat);
        HOOK_IO(mkdirat);
        HOOK_IO(mknodat);
        HOOK_IO(utimensat);
        HOOK_IO(fchownat);
        HOOK_IO(fstatat);
        HOOK_IO(fchmodat);
        HOOK_IO(faccessat);
    }
    hook_dlopen(api_level);
}
