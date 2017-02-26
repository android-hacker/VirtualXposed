

#include "native_hook.h"
#include "mem_helper.h"
#include "instruction/instruction_helper.h"
#include "instruction/arm_instruction.h"
#include "instruction/thumb_instruction.h"
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "thread_helper.h"



GodinHook::NativeHook::hook_map GodinHook::NativeHook::hook_map_;

int GodinHook::NativeHook::registeredHook(size_t originalFunAddress, size_t newFunAddress, size_t **callOriginal)
{
  bool flag = false;

  /// 首先判断original和new为函数地址
  if (!MemHelper::isFunctionAddr(originalFunAddress) || !MemHelper::isFunctionAddr(newFunAddress))
    return GODINHOOK_ERROR_NOT_EXECUTABLE;

  /// 尝试得到HookInfo
  HookInfo * info = NULL;
  info = getHookInfo(originalFunAddress);

  if(NULL != info){
      /// 判断original的hook状态
      HookStatus hookStatus = info->getHookStatus();
      if(HOOKED == hookStatus)
        return GODINHOOK_ERROR_ALREADY_HOOKED;
      if(REGISTERED == hookStatus)
        return GODINHOOK_ERROR_ALREADY_REGISTERED;
  }

  /// info == NULL
  /// 没有注册过，需要创建和初始化HookInfo对象，并添加到hook_map_中
  info = new HookInfo(originalFunAddress,newFunAddress,callOriginal);
  if(NULL == info)
    return GODINHOOK_ERROR_MEMORY;

  /// 设置original方法指令集
  FunctionType type = InstructionHelper::getFunctionType(originalFunAddress);
  if(ERRTYEPE == type)
    return false;
  info->setOriginalFunctionType(type);

  /// 设置hook方法指令集
  info->setHookFunctionType(InstructionHelper::getFunctionType(newFunAddress));


  /// 创建指令集处理对象
  InstructionHelper *insHelper = NULL ;
  if(ARM == type){
      insHelper =  new ArmInstruction();
     printf("arm----------------\n");
  }else if(THUMB == type){
     insHelper = new ThumbInstruction();
     insHelper->isResetStubSize(originalFunAddress);
     printf("thumb---------len-----%d--\n",insHelper->sizeofStub());
  }else if(ARM64 == type){
      // TODO
  }
  /// 备份stub覆盖的指令
  uint8_t *back = insHelper->getBackOfStub(InstructionHelper::valueToMem(originalFunAddress));
  if(NULL == back){
    free(insHelper);
    return GODINHOOK_ERROR_MEMORY;
  }
  /// 设置hookinfo
  info->setBackLen(insHelper->sizeofStub());
  info->setOriginalStubBack(back);


  /// 创建call original instruction ins

  void * callOriginalFun = insHelper->createCallOriginalIns(info);
  if(NULL == callOriginalFun){
    free(back);
    free(insHelper);
    return GODINHOOK_ERROR_MEMORY;
  }
  info->setCallOriginalIns((uint8_t *) callOriginalFun);

  /// 修改hookstatus
  addHookInfo(info);
  info->setHookStatus(REGISTERED);

  free(insHelper);
  return GODINHOOK_OK;
}



int GodinHook::NativeHook::hook(size_t originalFunAddress)
{

  HookInfo * info = getHookInfo(originalFunAddress);

  if(NULL == info)
    return GODINHOOK_ERROR_NOT_REGISTERED;
  if(info->getHookStatus() == HOOKED)
    return GODINHOOK_ERROR_ALREADY_HOOKED;
  else if(info->getHookStatus() == REGISTERED){

    /// 暂停当前其他线程；
    /// 并且纠正正在运行被hook的函数的线程的PC

    pid_t pid = ThreadHealper::freezzAndRepairThread(info,ACTION_ENABLE);

    /// 进行hook
    if(Hook(info)){
      /// 恢复当前其他线程
      ThreadHealper::unFreeze(pid);
      return GODINHOOK_OK;
    }
    else{
      /// 恢复当前其他线程
      ThreadHealper::unFreeze(pid);
      return GODINHOOK_ERROR_MEMORY;
    }
  }else
    return GODINHOOK_ERROR_UNKNOWN;
}

void *GodinHook::NativeHook::isAlreadyHooked(size_t originalFunAddress)
{
  hook_map::iterator it = hook_map_.find(originalFunAddress);
  if(it == hook_map_.end())
    return NULL;
  else {
      HookInfo * info = it->second;
      if(NULL != info && (info->getHookAddr() != NULL))
       return (void *) info->getHookAddr();
  }
  return NULL;
}

int GodinHook::NativeHook::getHookedCount()
{
  return hook_map_.size();
}

bool GodinHook::NativeHook::unHook(size_t originalFunAddress)
{
  hook_map::iterator it = hook_map_.find(originalFunAddress);
  if(it == hook_map_.end())
    return true;
  else{
       HookInfo * info = it->second;
       if(info != NULL || info->getHookStatus() == HOOKED){
          size_t addr = InstructionHelper::valueToMem(originalFunAddress);

          pid_t pid;
          int i;
          /// 冻结线程
          pid = ThreadHealper::freezzAndRepairThread(info, ACTION_DISABLE);
          /// 去保护,还原，加保护
          if(MemHelper::unProtectMemory(addr,info->getBackLen())){
              memcpy((void *) addr, info->getOriginalStubBack(), info->getBackLen());
              MemHelper::protectMemory(addr,info->getBackLen());

           /// 刷新指令缓存
           cacheflush(addr, addr+info->getBackLen(), 0);

           ///恢复线程
           ThreadHealper::unFreeze(pid);
           /// 释放资源
            if(info->getCallOriginalIns() != NULL)
              MemHelper::freeExecMemory(info->getCallOriginalIns());
            if(info->getOriginalStubBack() !=NULL )
              free(info->getOriginalStubBack());
            if(info->getCallOriginalAddr() !=NULL )
              (*(info->getCallOriginalAddr()))=NULL;
            hook_map_.erase(it);
            free(info);
            info = NULL;
            return true;
        }else
            return false;

      }
  }
  return false;
}

GodinHook::HookInfo **GodinHook::NativeHook::getAllHookInfo()
{
  int count = getHookedCount();

  HookInfo ** infos = (HookInfo **) calloc(count, sizeof(HookInfo*));

  hook_map::iterator it = hook_map_.begin();
  for(int i=0;it!=hook_map_.end();++it,++i){
    infos[i] = it->second;
  }
  return infos;
}

void GodinHook::NativeHook::hookAllRegistered()
{
  pid_t pid;
  int i;
  pid = ThreadHealper::freezzAndRepairThread(NULL, ACTION_ENABLE);
  HookInfo ** infos = NativeHook::getAllHookInfo();
  for (i = 0; i < getHookedCount(); ++i) {
      if (infos[i]->getHookStatus() == REGISTERED) {
         Hook(infos[i]);
        }
    }
  ThreadHealper::unFreeze(pid);
}

void GodinHook::NativeHook::unHookAll()
{
  pid_t pid;
  int i;

  pid = ThreadHealper::freezzAndRepairThread(NULL, ACTION_DISABLE);
  HookInfo ** infos = NativeHook::getAllHookInfo();
  int count = getHookedCount();
  for (i = 0; i < count; ++i) {
      if (infos[i]->getHookStatus() == HOOKED) {
        // printf("-------unhookall count %d\n",getHookedCount());
         UnHook(infos[i]);
        }
    }
  ThreadHealper::unFreeze(pid);
  free(infos);
}

GodinHook::HookStatus GodinHook::NativeHook::getFunctionStatus(size_t functionAddr)
{
   hook_map::iterator it = hook_map_.find(functionAddr);
   if(it == hook_map_.end())
     return ERRSTATUS;
   else {
       HookInfo * info = it->second;
       if(NULL != info)
        return info->getHookStatus();
   }
   return ERRSTATUS;
}

void GodinHook::NativeHook::addHookInfo(GodinHook::HookInfo *info)
{
  if(NULL == info)
   return;
  hook_map_.insert(hook_map::value_type(info->getOriginalAddr(),info));
}

bool GodinHook::NativeHook::Hook(HookInfo *info)
{

  /// 获取original方法指令集
  FunctionType type = info->getOriginalFunctiontype();
  if(ERRTYEPE == type)
    return false;

  /// 创建指令集处理对象
  InstructionHelper *insHelper = NULL ;
  if(ARM == type){
      insHelper =  new ArmInstruction();
  }else if(THUMB == type){
     insHelper = new ThumbInstruction();
     insHelper->isResetStubSize(info->getOriginalAddr());
  }else if(ARM64 == type){

  }

  /// 创建stub
  insHelper->createStub(info);

  /// 创建call original instruction
  /// 此时说明使用者不需要回调原方法
  if(NULL == info->getCallOriginalAddr())
    return true;
  else{
      void * callOriginal =info->getCallOriginalIns();
      if(THUMB == type)
        *(info->getCallOriginalAddr()) = (size_t *) InstructionHelper::valueToPc((size_t)callOriginal);
      else
        *(info->getCallOriginalAddr()) = (size_t *) callOriginal;
  }
  /// 设置状态
  info->setHookStatus(HOOKED);
  /// 再次刷新指令缓存
  cacheflush((long) InstructionHelper::valueToMem(info->getOriginalAddr()),
             (long) (InstructionHelper::valueToMem(info->getOriginalAddr()) + 12), 0);
  free(insHelper);
  return true;
}

bool GodinHook::NativeHook::UnHook(GodinHook::HookInfo *info)
{
  size_t addr = InstructionHelper::valueToMem(info->getOriginalAddr());
  /// 去保护,还原，加保护
  if(MemHelper::unProtectMemory(addr,info->getBackLen())){
        memcpy((void *) addr, info->getOriginalStubBack(), info->getBackLen());
        MemHelper::protectMemory(addr,info->getBackLen());

        /// 刷新指令缓存
        cacheflush(addr, (long) (addr + info->getBackLen()), 0);

        /// 释放资源
        if(info->getCallOriginalIns() != NULL)
          MemHelper::freeExecMemory(info->getCallOriginalIns());
        if(info->getOriginalStubBack() !=NULL )
          free(info->getOriginalStubBack());
        if(info->getCallOriginalAddr() !=NULL )
          (*(info->getCallOriginalAddr()))=NULL;

        hook_map::iterator it = hook_map_.find(info->getOriginalAddr());
        if(it != hook_map_.end())
          hook_map_.erase(it);
        free(info);
        info = NULL;
        return true;
   }else
    return false;
}

GodinHook::HookInfo *GodinHook::NativeHook::getHookInfo(size_t functionAddr)
{
  hook_map::iterator it = hook_map_.find(functionAddr);
  if(it == hook_map_.end())
    return NULL;
  else {
      HookInfo * info = it->second;
      if(NULL != info)
       return info;
  }
  return NULL;
}


