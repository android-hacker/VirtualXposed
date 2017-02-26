#ifndef NATIVE_HOOK_H
#define NATIVE_HOOK_H

#include <map>
#include <string>
#include <hookinfo.h>

using namespace std;

namespace  GodinHook {

  enum GODINHOOK_STATUS {
    GODINHOOK_ERROR_UNKNOWN = -1,
    GODINHOOK_OK = 0,
    GODINHOOK_ERROR_NOT_INITIALIZED,
    GODINHOOK_ERROR_NOT_EXECUTABLE,
    GODINHOOK_ERROR_NOT_REGISTERED,
    GODINHOOK_ERROR_NOT_HOOKED,
    GODINHOOK_ERROR_ALREADY_REGISTERED,
    GODINHOOK_ERROR_ALREADY_HOOKED,
    GODINHOOK_ERROR_SO_NOT_FOUND,
    GODINHOOK_ERROR_FUNCTION_NOT_FOUND,
    GODINHOOK_ERROR_MEMORY
  };

  class NativeHook{

  public:

    /**
     * @brief registeredHook
     *  注册成功后，就对相关的指令进行了修正，以及创建好回调原方法的环境，主要是为了线程安全做准备。
     *
     * @param originalFunAddress
     *  原函数地址
     * @param newFunAddress
     *  新函数地址
     * @param callOriginal
     *  存储回调原函数的地址
     *
     * @return
     *  返回 GODINHOOK_OK，表示注册成功;
     *  返回 GODINHOOK_ERROR_ALREADY_REGISTERED，表示已经注册；
     *  返回 GODINHOOK_ERROR_ALREADY_HOOKED，表示已经hook.
     *  返回其他,则注册失败。
     */
    static int registeredHook(size_t originalFunAddress,size_t newFunAddress,size_t ** callOriginal);

    /**
     * @brief hook
     *  对外提供的hook函数的接口，执行成功即完成了hook。
     *
     *  此方法中要对当前进程中的线程进行检查，并对正在运行被hook的
     *  函数的线程，进行必要的修正。
     *
     * @param originalFunAddress
     *  原函数地址
     *
     * @return
     *  返回 GODINHOOK_OK，表示hook成功;
     *  返回 GODINHOOK_ERROR_ALREADY_HOOKED，表示已经hook.
     *  返回其他，则hook失败
     */
    static int hook(size_t originalFunAddress);

    /**
     * @brief isAlreadyHooked
     * 判断该方法是否被hook了
     * @param originalFunAddress
     * 方法地址
     * @return
     * NULL，表明未被HOOK；
     * 否则返回hook函数的地址；
     */
    static void* isAlreadyHooked(size_t originalFunAddress);

    /**
     * @brief getHookedCount
     * 当前被hook的方法的数量
     * @return
     * 返回当前被hook的方法的数量
     */
    static int getHookedCount();

    /**
     * @brief unHook
     * 卸载hook
     * @param originalFunAddress
     * 原方法地址
     * @return
     * true表示卸载成功；
     * 反之卸载失败。
     */
    static bool unHook(size_t originalFunAddress);


    /**
     * @brief getAllHookInfo
     * 得到当前所有的HookInfo,需要手动释放返回值指向的空间
     * @return
     * 返回存储有当前所有HookInfo地址的数组
     */
    static HookInfo ** getAllHookInfo();

    /**
     * @brief hookAllRegistered
     * 将注册的所有方法进行hook操作
     */
    static void hookAllRegistered();

    /**
     * @brief unHookAll
     * 卸载所有的hook
     */
    static void unHookAll();



  private:
    /**
     * @brief getFunctionStatus
     *  获取函数当前的状态
     *
     * @param functionAddr
     *  必须是一个函数的地址
     *
     * @return
     * UNREGISTERED,REGISTERED,HOOKED or UNHOOK.
     */
    static HookStatus getFunctionStatus(size_t functionAddr);

    /**
     * @brief addHookInfo
     *  向hook_map_中添加新成员
     * @param info
     *  要加入的hook_map_的info
     */
    static void addHookInfo(HookInfo * info);

    /**
     * @brief registerAndHook
     * 注册并进行hook操作
     * @param info
     * HookInfo类型的对象指针
     * @return
     * 成功返回true;
     * 失败返回false.
     */
    static bool Hook(HookInfo* info);

    static bool UnHook(HookInfo* info);


    static HookInfo * getHookInfo(size_t functionAddr);

  private:

    /// 记录哪些方法被hook,key为原方法的地址
    typedef map<size_t,HookInfo*> hook_map;
    static hook_map hook_map_;
  };

}

#endif // NATIVE_HOOK_H
