//
// VirtualApp Native Project
//
#include <unistd.h>
#include <stdlib.h>
#include <fb/include/fb/ALog.h>
#include <Substrate/SubstrateHook.h>

//extern "C" {
//#include <HookZz/include/hookzz.h>
//}


#include "IOUniformer.h"
#include "SandboxFs.h"
#include "Path.h"
#include "SymbolFinder.h"

bool iu_loaded = false;

void IOUniformer::init_env_before_all() {
    if (iu_loaded)
        return;
    char *api_level_chars = getenv("V_API_LEVEL");
    char *preview_api_level_chars = getenv("V_PREVIEW_API_LEVEL");
    if (api_level_chars) {
        ALOGE("Enter init before all.");
        int api_level = atoi(api_level_chars);
        int preview_api_level;
        preview_api_level = atoi(preview_api_level_chars);
        char keep_env_name[25];
        char forbid_env_name[25];
        char replace_src_env_name[25];
        char replace_dst_env_name[25];
        int i = 0;
        while (true) {
            sprintf(keep_env_name, "V_KEEP_ITEM_%d", i);
            char *item = getenv(keep_env_name);
            if (!item) {
                break;
            }
            add_keep_item(item);
            i++;
        }
        i = 0;
        while (true) {
            sprintf(forbid_env_name, "V_FORBID_ITEM_%d", i);
            char *item = getenv(forbid_env_name);
            if (!item) {
                break;
            }
            add_forbidden_item(item);
            i++;
        }
        i = 0;
        while (true) {
            sprintf(replace_src_env_name, "V_REPLACE_ITEM_SRC_%d", i);
            char *item_src = getenv(replace_src_env_name);
            if (!item_src) {
                break;
            }
            sprintf(replace_dst_env_name, "V_REPLACE_ITEM_DST_%d", i);
            char *item_dst = getenv(replace_dst_env_name);
            add_replace_item(item_src, item_dst);
            i++;
        }
        startUniformer(getenv("V_SO_PATH"),api_level, preview_api_level);
        iu_loaded = true;
    }
}


static inline void
hook_function(void *handle, const char *symbol, void *new_func, void **old_func) {
    void *addr = dlsym(handle, symbol);
    if (addr == NULL) {
        return;
    }
    MSHookFunction(addr, new_func, old_func);
}


void onSoLoaded(const char *name, void *handle);

void IOUniformer::redirect(const char *orig_path, const char *new_path) {
    add_replace_item(orig_path, new_path);
}

const char *IOUniformer::query(const char *orig_path) {
    return reverse_relocate_path(orig_path);
}

void IOUniformer::whitelist(const char *_path) {
    add_keep_item(_path);
}

void IOUniformer::forbid(const char *_path) {
    add_forbidden_item(_path);
}


const char *IOUniformer::reverse(const char *_path) {
    return reverse_relocate_path(_path);
}


__BEGIN_DECLS

#define FREE(ptr, org_ptr) { if ((void*) ptr != NULL && (void*) ptr != (void*) org_ptr) { free((void*) ptr); } }

// int faccessat(int dirfd, const char *pathname, int mode, int flags);
HOOK_DEF(int, faccessat, int dirfd, const char *pathname, int mode, int flags) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_faccessat, dirfd, redirect_path, mode, flags);
    FREE(redirect_path, pathname);
    return ret;
}


// int fchmodat(int dirfd, const char *pathname, mode_t mode, int flags);
HOOK_DEF(int, fchmodat, int dirfd, const char *pathname, mode_t mode, int flags) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_fchmodat, dirfd, redirect_path, mode, flags);
    FREE(redirect_path, pathname);
    return ret;
}
// int fchmod(const char *pathname, mode_t mode);
HOOK_DEF(int, fchmod, const char *pathname, mode_t mode) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_chmod, redirect_path, mode);
    FREE(redirect_path, pathname);
    return ret;
}


// int fstatat(int dirfd, const char *pathname, struct stat *buf, int flags);
HOOK_DEF(int, fstatat, int dirfd, const char *pathname, struct stat *buf, int flags) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_fstatat64, dirfd, redirect_path, buf, flags);
    FREE(redirect_path, pathname);
    return ret;
}

// int fstatat64(int dirfd, const char *pathname, struct stat *buf, int flags);
HOOK_DEF(int, fstatat64, int dirfd, const char *pathname, struct stat *buf, int flags) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_fstatat64, dirfd, redirect_path, buf, flags);
    FREE(redirect_path, pathname);
    return ret;
}


// int fstat(const char *pathname, struct stat *buf, int flags);
HOOK_DEF(int, fstat, const char *pathname, struct stat *buf) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_fstat64, redirect_path, buf);
    FREE(redirect_path, pathname);
    return ret;
}


// int mknodat(int dirfd, const char *pathname, mode_t mode, dev_t dev);
HOOK_DEF(int, mknodat, int dirfd, const char *pathname, mode_t mode, dev_t dev) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_mknodat, dirfd, redirect_path, mode, dev);
    FREE(redirect_path, pathname);
    return ret;
}
// int mknod(const char *pathname, mode_t mode, dev_t dev);
HOOK_DEF(int, mknod, const char *pathname, mode_t mode, dev_t dev) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_mknod, redirect_path, mode, dev);
    FREE(redirect_path, pathname);
    return ret;
}


// int utimensat(int dirfd, const char *pathname, const struct timespec times[2], int flags);
HOOK_DEF(int, utimensat, int dirfd, const char *pathname, const struct timespec times[2],
         int flags) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_utimensat, dirfd, redirect_path, times, flags);
    FREE(redirect_path, pathname);
    return ret;
}


// int fchownat(int dirfd, const char *pathname, uid_t owner, gid_t group, int flags);
HOOK_DEF(int, fchownat, int dirfd, const char *pathname, uid_t owner, gid_t group, int flags) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_fchownat, dirfd, redirect_path, owner, group, flags);
    FREE(redirect_path, pathname);
    return ret;
}

// int chroot(const char *pathname);
HOOK_DEF(int, chroot, const char *pathname) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_chroot, redirect_path);
    FREE(redirect_path, pathname);
    return ret;
}


// int renameat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath);
HOOK_DEF(int, renameat, int olddirfd, const char *oldpath, int newdirfd, const char *newpath) {
    int res_old;
    int res_new;
    const char *redirect_path_old = relocate_path(oldpath, &res_old);
    const char *redirect_path_new = relocate_path(newpath, &res_new);
    int ret = syscall(__NR_renameat, olddirfd, redirect_path_old, newdirfd, redirect_path_new);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}
// int rename(const char *oldpath, const char *newpath);
HOOK_DEF(int, rename, const char *oldpath, const char *newpath) {
    int res_old;
    int res_new;
    const char *redirect_path_old = relocate_path(oldpath, &res_old);
    const char *redirect_path_new = relocate_path(newpath, &res_new);
    int ret = syscall(__NR_rename, redirect_path_old, redirect_path_new);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}


// int unlinkat(int dirfd, const char *pathname, int flags);
HOOK_DEF(int, unlinkat, int dirfd, const char *pathname, int flags) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_unlinkat, dirfd, redirect_path, flags);
    FREE(redirect_path, pathname);
    return ret;
}
// int unlink(const char *pathname);
HOOK_DEF(int, unlink, const char *pathname) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_unlink, redirect_path);
    FREE(redirect_path, pathname);
    return ret;
}


// int symlinkat(const char *oldpath, int newdirfd, const char *newpath);
HOOK_DEF(int, symlinkat, const char *oldpath, int newdirfd, const char *newpath) {
    int res_old;
    int res_new;
    const char *redirect_path_old = relocate_path(oldpath, &res_old);
    const char *redirect_path_new = relocate_path(newpath, &res_new);
    int ret = syscall(__NR_symlinkat, redirect_path_old, newdirfd, redirect_path_new);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}
// int symlink(const char *oldpath, const char *newpath);
HOOK_DEF(int, symlink, const char *oldpath, const char *newpath) {
    int res_old;
    int res_new;
    const char *redirect_path_old = relocate_path(oldpath, &res_old);
    const char *redirect_path_new = relocate_path(newpath, &res_new);
    int ret = syscall(__NR_symlink, redirect_path_old, redirect_path_new);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}


// int linkat(int olddirfd, const char *oldpath, int newdirfd, const char *newpath, int flags);
HOOK_DEF(int, linkat, int olddirfd, const char *oldpath, int newdirfd, const char *newpath,
         int flags) {
    int res_old;
    int res_new;
    const char *redirect_path_old = relocate_path(oldpath, &res_old);
    const char *redirect_path_new = relocate_path(newpath, &res_new);
    int ret = syscall(__NR_linkat, olddirfd, redirect_path_old, newdirfd, redirect_path_new, flags);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}
// int link(const char *oldpath, const char *newpath);
HOOK_DEF(int, link, const char *oldpath, const char *newpath) {
    int res_old;
    int res_new;
    const char *redirect_path_old = relocate_path(oldpath, &res_old);
    const char *redirect_path_new = relocate_path(newpath, &res_new);
    int ret = syscall(__NR_link, redirect_path_old, redirect_path_new);
    FREE(redirect_path_old, oldpath);
    FREE(redirect_path_new, newpath);
    return ret;
}


// int utimes(const char *filename, const struct timeval *tvp);
HOOK_DEF(int, utimes, const char *pathname, const struct timeval *tvp) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_utimes, redirect_path, tvp);
    FREE(redirect_path, pathname);
    return ret;
}


// int access(const char *pathname, int mode);
HOOK_DEF(int, access, const char *pathname, int mode) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_access, redirect_path, mode);
    FREE(redirect_path, pathname);
    return ret;
}


// int chmod(const char *path, mode_t mode);
HOOK_DEF(int, chmod, const char *pathname, mode_t mode) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_chmod, redirect_path, mode);
    FREE(redirect_path, pathname);
    return ret;
}


// int chown(const char *path, uid_t owner, gid_t group);
HOOK_DEF(int, chown, const char *pathname, uid_t owner, gid_t group) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_chown, redirect_path, owner, group);
    FREE(redirect_path, pathname);
    return ret;
}


// int lstat(const char *path, struct stat *buf);
HOOK_DEF(int, lstat, const char *pathname, struct stat *buf) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_lstat64, redirect_path, buf);
    FREE(redirect_path, pathname);
    return ret;
}


// int stat(const char *path, struct stat *buf);
HOOK_DEF(int, stat, const char *pathname, struct stat *buf) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_stat64, redirect_path, buf);
    FREE(redirect_path, pathname);
    return ret;
}


// int mkdirat(int dirfd, const char *pathname, mode_t mode);
HOOK_DEF(int, mkdirat, int dirfd, const char *pathname, mode_t mode) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_mkdirat, dirfd, redirect_path, mode);
    FREE(redirect_path, pathname);
    return ret;
}
// int mkdir(const char *pathname, mode_t mode);
HOOK_DEF(int, mkdir, const char *pathname, mode_t mode) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_mkdir, redirect_path, mode);
    FREE(redirect_path, pathname);
    return ret;
}


// int rmdir(const char *pathname);
HOOK_DEF(int, rmdir, const char *pathname) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_rmdir, redirect_path);
    FREE(redirect_path, pathname);
    return ret;
}


// int readlinkat(int dirfd, const char *pathname, char *buf, size_t bufsiz);
HOOK_DEF(int, readlinkat, int dirfd, const char *pathname, char *buf, size_t bufsiz) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_readlinkat, dirfd, redirect_path, buf, bufsiz);
    FREE(redirect_path, pathname);
    return ret;
}
// ssize_t readlink(const char *path, char *buf, size_t bufsiz);
HOOK_DEF(ssize_t, readlink, const char *pathname, char *buf, size_t bufsiz) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    ssize_t ret = syscall(__NR_readlink, redirect_path, buf, bufsiz);
    FREE(redirect_path, pathname);
    return ret;
}


// int __statfs64(const char *path, size_t size, struct statfs *stat);
HOOK_DEF(int, __statfs64, const char *pathname, size_t size, struct statfs *stat) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_statfs64, redirect_path, size, stat);
    FREE(redirect_path, pathname);
    return ret;
}


// int truncate(const char *path, off_t length);
HOOK_DEF(int, truncate, const char *pathname, off_t length) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_truncate, redirect_path, length);
    FREE(redirect_path, pathname);
    return ret;
}

#define RETURN_IF_FORBID if(res == FORBID) return -1;

// int truncate64(const char *pathname, off_t length);
HOOK_DEF(int, truncate64, const char *pathname, off_t length) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    RETURN_IF_FORBID
    int ret = syscall(__NR_truncate64, redirect_path, length);
    FREE(redirect_path, pathname);
    return ret;
}


// int chdir(const char *path);
HOOK_DEF(int, chdir, const char *pathname) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    RETURN_IF_FORBID
    int ret = syscall(__NR_chdir, redirect_path);
    FREE(redirect_path, pathname);
    return ret;
}


// int __getcwd(char *buf, size_t size);
HOOK_DEF(int, __getcwd, char *buf, size_t size) {
    int ret = syscall(__NR_getcwd, buf, size);
    if (!ret) {

    }
    return ret;
}


// int __openat(int fd, const char *pathname, int flags, int mode);
HOOK_DEF(int, __openat, int fd, const char *pathname, int flags, int mode) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_openat, fd, redirect_path, flags, mode);
    FREE(redirect_path, pathname);
    return ret;
}
// int __open(const char *pathname, int flags, int mode);
HOOK_DEF(int, __open, const char *pathname, int flags, int mode) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_open, redirect_path, flags, mode);
    FREE(redirect_path, pathname);
    return ret;
}

// int __statfs (__const char *__file, struct statfs *__buf);
HOOK_DEF(int, __statfs, __const char *__file, struct statfs *__buf) {
    int res;
    const char *redirect_path = relocate_path(__file, &res);
    int ret = syscall(__NR_statfs, redirect_path, __buf);
    FREE(redirect_path, __file);
    return ret;
}

// int lchown(const char *pathname, uid_t owner, gid_t group);
HOOK_DEF(int, lchown, const char *pathname, uid_t owner, gid_t group) {
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    int ret = syscall(__NR_lchown, redirect_path, owner, group);
    FREE(redirect_path, pathname);
    return ret;
}

int inline getArrayItemCount(char *const array[]) {
    int i;
    for (i = 0; array[i]; ++i);
    return i;
}


char **build_new_env(char *const envp[]) {
    char *provided_ld_preload = NULL;
    int provided_ld_preload_index = -1;
    int orig_envp_count = getArrayItemCount(envp);

    for (int i = 0; i < orig_envp_count; i++) {
        if (strstr(envp[i], "LD_PRELOAD")) {
            provided_ld_preload = envp[i];
            provided_ld_preload_index = i;
        }
    }
    char ld_preload[200];
    char *so_path = getenv("V_SO_PATH");
    if (provided_ld_preload) {
        sprintf(ld_preload, "LD_PRELOAD=%s:%s", so_path, provided_ld_preload + 11);
    } else {
        sprintf(ld_preload, "LD_PRELOAD=%s", so_path);
    }
    int new_envp_count = orig_envp_count
                         + get_keep_item_count()
                         + get_forbidden_item_count()
                         + get_replace_item_count() * 2 + 1;
    if (provided_ld_preload) {
        new_envp_count--;
    }
    char **new_envp = (char **) malloc(new_envp_count * sizeof(char *));
    int cur = 0;
    new_envp[cur++] = ld_preload;
    for (int i = 0; i < orig_envp_count; ++i) {
        if (i != provided_ld_preload_index) {
            new_envp[cur++] = envp[i];
        }
    }
    for (int i = 0; environ[i]; ++i) {
        if (environ[i][0] == 'V' && environ[i][1] == '_') {
            new_envp[cur++] = environ[i];
        }
    }
    new_envp[cur] = NULL;
    return new_envp;
}

char **build_new_argv(char *const envp[]) {
    char *provided_ld_preload = NULL;
    int provided_ld_preload_index = -1;
    int orig_envp_count = getArrayItemCount(envp);

    for (int i = 0; i < orig_envp_count; i++) {
        if (strstr(envp[i], "compiler-filter")) {
            provided_ld_preload = envp[i];
            provided_ld_preload_index = i;
        }
    }
    char ld_preload[40];
    if (provided_ld_preload) {
        sprintf(ld_preload, "--compiler-filter=%s", "everything");
    }

    char *api_level_char = getenv("V_API_LEVEL");
    int api_level = atoi(api_level_char);

    int new_envp_count = orig_envp_count + 4;
    char **new_envp = (char **) malloc(new_envp_count * sizeof(char *));
    int cur = 0;
    for (int i = 0; i < orig_envp_count; ++i) {
        if (i != provided_ld_preload_index) {
            new_envp[cur++] = envp[i];
        } else {
            new_envp[i] = ld_preload;
            cur++;
        }
    }

    if (api_level >= 22) {
        new_envp[cur++] = (char *) "--compile-pic";
    }
    if (api_level >= 23) {
        new_envp[cur++] = (char *) (api_level > 25 ? "--inline-max-code-units=0" : "--inline-depth-limit=0");
    }
    if (api_level >= 28) {
        new_envp[cur++] = (char *) "--debuggable";
    }
    new_envp[cur] = NULL;

//    int n = getArrayItemCount(new_envp);
//    for (int i = 0; i < n; i++) {
//        ALOGE("dex2oat : %s", new_envp[i]);
//    }

    return new_envp;
}

// int (*origin_execve)(const char *pathname, char *const argv[], char *const envp[]);
HOOK_DEF(int, execve, const char *pathname, char *argv[], char *const envp[]) {
    /**
     * CANNOT LINK EXECUTABLE "/system/bin/cat": "/data/app/io.virtualapp-1/lib/arm/libva-native.so" is 32-bit instead of 64-bit.
     *
     * We will support 64Bit to adopt it.
     */
    // ALOGE("execve : %s", pathname); // any output can break exec. See bug: https://issuetracker.google.com/issues/109448553
    int res;
    const char *redirect_path = relocate_path(pathname, &res);
    char *ld = getenv("LD_PRELOAD");
    if (ld) {
        if (strstr(ld, "libNimsWrap.so") || strstr(ld, "stamina.so")) {
            int ret = syscall(__NR_execve, redirect_path, argv, envp);
            FREE(redirect_path, pathname);
            return ret;
        }
    }
    if (strstr(pathname, "dex2oat")) {
        char **new_envp = build_new_env(envp);
        char **new_argv = build_new_argv(argv);
        int ret = syscall(__NR_execve, redirect_path, new_argv, new_envp);
        FREE(redirect_path, pathname);
        free(new_envp);
        free(new_argv);
        return ret;
    }
    int ret = syscall(__NR_execve, redirect_path, argv, envp);
    FREE(redirect_path, pathname);
    return ret;
}


HOOK_DEF(void*, dlopen, const char *filename, int flag) {
    int res;
    const char *redirect_path = relocate_path(filename, &res);
    void *ret = orig_dlopen(redirect_path, flag);
    onSoLoaded(filename, ret);
    ALOGD("dlopen : %s, return : %p.", redirect_path, ret);
    FREE(redirect_path, filename);
    return ret;
}

HOOK_DEF(void*, do_dlopen_V19, const char *filename, int flag, const void *extinfo) {
    int res;
    const char *redirect_path = relocate_path(filename, &res);
    void *ret = orig_do_dlopen_V19(redirect_path, flag, extinfo);
    onSoLoaded(filename, ret);
    ALOGD("do_dlopen : %s, return : %p.", redirect_path, ret);
    FREE(redirect_path, filename);
    return ret;
}

HOOK_DEF(void*, do_dlopen_V24, const char *name, int flags, const void *extinfo,
         void *caller_addr) {
    int res;
    const char *redirect_path = relocate_path(name, &res);
    void *ret = orig_do_dlopen_V24(redirect_path, flags, extinfo, caller_addr);
    onSoLoaded(name, ret);
    ALOGD("do_dlopen : %s, return : %p.", redirect_path, ret);
    FREE(redirect_path, name);
    return ret;
}



//void *dlsym(void *handle,const char *symbol)
HOOK_DEF(void*, dlsym, void *handle, char *symbol) {
    ALOGD("dlsym : %p %s.", handle, symbol);
    return orig_dlsym(handle, symbol);
}

// int kill(pid_t pid, int sig);
HOOK_DEF(int, kill, pid_t pid, int sig) {
    ALOGD(">>>>> kill >>> pid: %d, sig: %d.", pid, sig);
    int ret = syscall(__NR_kill, pid, sig);
    return ret;
}

HOOK_DEF(pid_t, vfork) {
    return fork();
}

__END_DECLS
// end IO DEF


void onSoLoaded(const char *name, void *handle) {
}

int findSymbol(const char *name, const char *libn,
               unsigned long *addr) {
    return find_name(getpid(), name, libn, addr);
}

void hook_dlopen(int api_level) {
    void *symbol = NULL;
    if (api_level > 25) {
        if (findSymbol("__dl__Z9do_dlopenPKciPK17android_dlextinfoPKv", "linker",
                       (unsigned long *) &symbol) == 0) {
            MSHookFunction(symbol, (void *) new_do_dlopen_V24,
                           (void **) &orig_do_dlopen_V24);
        }
    } else if (api_level > 23) {
        if (findSymbol("__dl__Z9do_dlopenPKciPK17android_dlextinfoPv", "linker",
                       (unsigned long *) &symbol) == 0) {
            MSHookFunction(symbol, (void *) new_do_dlopen_V24,
                          (void **) &orig_do_dlopen_V24);
        }
    } else if (api_level >= 19) {
        if (findSymbol("__dl__Z9do_dlopenPKciPK17android_dlextinfo", "linker",
                       (unsigned long *) &symbol) == 0) {
            MSHookFunction(symbol, (void *) new_do_dlopen_V19,
                          (void **) &orig_do_dlopen_V19);
        }
    } else {
        if (findSymbol("__dl_dlopen", "linker",
                       (unsigned long *) &symbol) == 0) {
            MSHookFunction(symbol, (void *) new_dlopen, (void **) &orig_dlopen);
        }
    }
}


void IOUniformer::startUniformer(const char *so_path, int api_level, int preview_api_level) {
    char api_level_chars[5];
    setenv("V_SO_PATH", so_path, 1);
    sprintf(api_level_chars, "%i", api_level);
    setenv("V_API_LEVEL", api_level_chars, 1);
    sprintf(api_level_chars, "%i", preview_api_level);
    setenv("V_PREVIEW_API_LEVEL", api_level_chars, 1);

    void *handle = dlopen("libc.so", RTLD_NOW);
    if (handle) {
        HOOK_SYMBOL(handle, faccessat);
        HOOK_SYMBOL(handle, __openat);
        HOOK_SYMBOL(handle, fchmodat);
        HOOK_SYMBOL(handle, fchownat);
        HOOK_SYMBOL(handle, renameat);
        HOOK_SYMBOL(handle, fstatat64);
        HOOK_SYMBOL(handle, __statfs);
        HOOK_SYMBOL(handle, __statfs64);
        HOOK_SYMBOL(handle, mkdirat);
        HOOK_SYMBOL(handle, mknodat);
        HOOK_SYMBOL(handle, truncate);
        HOOK_SYMBOL(handle, linkat);
        HOOK_SYMBOL(handle, readlinkat);
        HOOK_SYMBOL(handle, unlinkat);
        HOOK_SYMBOL(handle, symlinkat);
        HOOK_SYMBOL(handle, utimensat);
        HOOK_SYMBOL(handle, __getcwd);
//        HOOK_SYMBOL(handle, __getdents64);
        HOOK_SYMBOL(handle, chdir);
        HOOK_SYMBOL(handle, execve);
        if (api_level <= 20) {
            HOOK_SYMBOL(handle, access);
            HOOK_SYMBOL(handle, __open);
            HOOK_SYMBOL(handle, stat);
            HOOK_SYMBOL(handle, lstat);
            HOOK_SYMBOL(handle, fstatat);
            HOOK_SYMBOL(handle, chmod);
            HOOK_SYMBOL(handle, chown);
            HOOK_SYMBOL(handle, rename);
            HOOK_SYMBOL(handle, rmdir);
            HOOK_SYMBOL(handle, mkdir);
            HOOK_SYMBOL(handle, mknod);
            HOOK_SYMBOL(handle, link);
            HOOK_SYMBOL(handle, unlink);
            HOOK_SYMBOL(handle, readlink);
            HOOK_SYMBOL(handle, symlink);
//            HOOK_SYMBOL(handle, getdents);
//            HOOK_SYMBOL(handle, execv);
        }
        dlclose(handle);
    }
    // hook_dlopen(api_level);
}
