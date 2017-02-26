#ifndef THREAD_HEALPER_H
#define THREAD_HEALPER_H

#include <sys/wait.h>
#include <sys/ptrace.h>
#include "hookinfo.h"


namespace GodinHook {

  #define ACTION_ENABLE         0
  #define ACTION_DISABLE	1

  class ThreadHealper{

  public:
    /**
     * @brief freezzAndRepairThread
     * 冻结和修复线程
     * @param info
     * 当HookInfo为NULL时，表示检查所有的HookInfo
     * @param action
     * ACTION_ENABLE或者ACTION_DISABLE
     * @return
     * 返回执行冻结和修复工作的进程ID
     */
    static pid_t freezzAndRepairThread(HookInfo * info,int action);

    /**
     * @brief unFreeze
     * 解除冻结状态，线程恢复运行
     * @param pid
     * 执行冻结工作的进程
     */
    static void unFreeze(pid_t pid);
  private:

    /**
     * @brief getAllTids
     * 获取当前进程中除主线程之外的所有线程
     * @param pid
     * 当前进程
     * @param tids
     * 存储线程号的数组
     * @return
     * 除主线程之外的其他线程的数量
     */
    static int getAllTids(pid_t pid, pid_t *tids);

    static void repairThreadPc(pid_t tid, HookInfo *info, int action);

    static bool doRepairThreadPC(HookInfo *info, struct pt_regs *regs, int action);



  };

}

#endif // THREAD_HEALPER_H
