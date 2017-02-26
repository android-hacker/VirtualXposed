
#include "instruction_helper.h"
#include <stdlib.h>
#include <string.h>


uint8_t *GodinHook::InstructionHelper::getBackOfStub(size_t targetAddress)
{
  int len = sizeofStub();
  uint8_t * back = (uint8_t *) calloc(1, (size_t) len);
  if(NULL == back)
    return NULL;
  memcpy(back, (const void *) targetAddress, (size_t) len);
  return back;
}

///TODO 添加对ARM 64 指令集支持
GodinHook::FunctionType GodinHook::InstructionHelper::getFunctionType(size_t functionAddr)
{
  if(0 == functionAddr)
    return ERRTYEPE;
    if(functionAddr % 4 == 0)
      return ARM;
    else if(functionAddr % 4 == 1)
      return THUMB;

    return THUMB;
}

size_t GodinHook::InstructionHelper::valueToMem(size_t addr)
{
  return addr &(~0x1L);
}

size_t GodinHook::InstructionHelper::valueToPc(size_t addr)
{
  return valueToMem(addr)+1;
}
