#ifndef LIBHOOK_H_
#define LIBHOOK_H_

#define HOOK_FAILED -1
#define HOOK_SUCCESS 0

#ifdef __cplusplus
extern "C" {
#endif

int elfHook(const char *soname, const char *symbol, void *replace_func, void **old_func);
int elfHookDirect(unsigned int addr, void *replace_func,void **old_func);

#ifdef __cplusplus
}
#endif
#endif /* LIBHOOK_HOOK2_H_ */
