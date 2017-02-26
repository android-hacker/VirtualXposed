#include "thread_helper.h"
#include <sys/types.h>
#include <dirent.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "instruction/instruction_helper.h"
#include "native_hook.h"

pid_t GodinHook::ThreadHealper::freezzAndRepairThread(GodinHook::HookInfo *info, int action)
{
  int count;
  pid_t tids[1024];
  pid_t pid;

  pid = -1;
  count = getAllTids(getpid(), tids);
  if (count > 0) {

      /// 创建进程
      pid = fork();

      /// 子进程进行冻结和修复工作
      if (pid == 0) {
          int i;

          for (i = 0; i < count; ++i) {
              if (ptrace(PTRACE_ATTACH, tids[i], NULL, NULL) == 0) {
                  //子进程进入暂停，立即返回
                  waitpid(tids[i], NULL, WUNTRACED);
                  repairThreadPc(tids[i], info, action);
                }
            }
          //把信号发送给(进程)自身.
          raise(SIGSTOP);

          // 收到SIGCONT后，解除冻结状态
          for (i = 0; i < count; ++i) {
              ptrace(PTRACE_DETACH, tids[i], NULL, NULL);
            }

          exit(0);
        }
      else if (pid > 0) {
          //等待前面创建的子进程暂停
          waitpid(pid, NULL, WUNTRACED);
        }
    }

  return pid;
}

void GodinHook::ThreadHealper::unFreeze(pid_t pid)
{
  if(pid < 0)
    return;

  /// 向执行冻结工作的进程发送信号，使其继续执行
  kill(pid,SIGCONT);

  /// 等待子进程退出
  waitpid(pid,NULL,0);

}

int GodinHook::ThreadHealper::getAllTids(pid_t pid, pid_t *tids)
{
  char dir_path[32];
  DIR *dir;
  int i;
  struct dirent *entry;
  pid_t tid;

  if (pid < 0) {
      snprintf(dir_path, sizeof(dir_path), "/proc/self/task");
    }
  else {
      snprintf(dir_path, sizeof(dir_path), "/proc/%d/task", pid);
    }

  dir = opendir(dir_path);
  if (dir == NULL) {
      return 0;
    }

  i = 0;
  while((entry = readdir(dir)) != NULL) {
      tid = atoi(entry->d_name);
      if (tid != 0 && tid != getpid()) {
          tids[i++] = tid;
        }
    }
  closedir(dir);
  return i;
}

void GodinHook::ThreadHealper::repairThreadPc(pid_t tid, GodinHook::HookInfo *info, int action)
{
  struct pt_regs regs;

  if((NULL != info) || NativeHook::getHookedCount()>0){
      if (ptrace(PTRACE_GETREGS, tid, NULL, &regs) == 0) {
          if (info == NULL) {
              int pos;
              HookInfo ** infos = NativeHook::getAllHookInfo();
              for (pos = 0; pos < NativeHook::getHookedCount(); ++pos) {
                  if (doRepairThreadPC(infos[pos], &regs, action) == true) {
                      break;
                    }
                }
              free(infos);
            }
          else {
              doRepairThreadPC(info, &regs, action);
            }

          ptrace(PTRACE_SETREGS, tid, NULL, &regs);
        }
   }
}

bool GodinHook::ThreadHealper::doRepairThreadPC(GodinHook::HookInfo *info, pt_regs *regs, int action)
{
  int offset;
  int i;
  switch (action)
    {
    /// 进行hook的时候，线程执行到正在被hook的函数，将其纠正到hook框架构建的调用原方法的对应机器指令上
    case ACTION_ENABLE:
      offset = regs->ARM_pc - InstructionHelper::valueToMem(info->getOriginalAddr());
      for (i = 0; i < info->count; ++i) {
          if (offset == info->orig_boundaries[i]) {
              regs->ARM_pc = (uint32_t) info->getCallOriginalIns()+ info->trampoline_boundaries[i];
              return true;
            }
        }
      break;
    ///  进行unhook的时候，线程正在执行被hook函数的新函数，将其纠正到unhook之后原方法的机器指令上
    case ACTION_DISABLE:
      offset = regs->ARM_pc - (int) info->getCallOriginalIns();
      for (i = 0; i < info->count; ++i) {
          if (offset == info->trampoline_boundaries[i]) {
              regs->ARM_pc = InstructionHelper::valueToMem(info->getOriginalAddr()) + info->orig_boundaries[i];
              return true;
            }
        }
      break;
    }

  return false;
}


