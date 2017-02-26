
#include "arm_instruction.h"
#include <stdlib.h>
#include <string.h>
#include "../mem_helper.h"
#include <unistd.h>//android cacheflush


#define MAX_REPAIR_INS_LEN 64

uint8_t GodinHook::ArmInstruction::ldr[4]={
  0x04,0xf0,0x1f,0xe5,
};
static void clearcache(char* begin, char *end)
{
    const int syscall = 0xf0002;
    __asm __volatile (
        "mov     r0, %0\n"
        "mov     r1, %1\n"
        "mov     r7, %2\n"
        "mov     r2, #0x0\n"
        "svc     0x00000000\n"
        :
        :"r" (begin), "r" (end), "r" (syscall)
        :"r0","r1","r7"
        );
}
void GodinHook::ArmInstruction::createStub(HookInfo * info)
{
  size_t originalAddress = info->getOriginalAddr();
  size_t targetAddress = info->getHookAddr();
  int len = sizeofStub();
  /// 去保护，并修改指令
  if(MemHelper::unProtectMemory(originalAddress,len)){
     memcpy((void*)originalAddress,ldr,4);
     memcpy((void*)(originalAddress+4),&targetAddress,4);
  }else
    return;
  //clearcache(originalAddress,originalAddress+len);
  /// 加保护
  MemHelper::protectMemory(originalAddress,len);
  /// 刷新指令缓存
  cacheflush(originalAddress,originalAddress+len,0);



}

int GodinHook::ArmInstruction::getRepairInstruction(size_t instruction)
{
  if ((instruction & 0xFE000000) == 0xFA000000) {
          return BLX_ARM;
  }
  if ((instruction & 0xF000000) == 0xB000000) {
          return BL_ARM;
  }
  if ((instruction & 0xF000000) == 0xA000000) {
          return B_ARM;
  }
  if ((instruction & 0xFF000FF) == 0x120001F) {
          return BX_ARM;
  }
  if ((instruction & 0xFEF0010) == 0x8F0000) {
          return ADD_ARM;
  }
  if ((instruction & 0xFFF0000) == 0x28F0000) {
          return ADR1_ARM;
  }
  if ((instruction & 0xFFF0000) == 0x24F0000) {
          return ADR2_ARM;
  }
  if ((instruction & 0xE5F0000) == 0x41F0000) {
          return LDR_ARM;
  }
  if ((instruction & 0xFE00FFF) == 0x1A0000F) {
          return MOV_ARM;
  }
  return UNDEFINE;
}

void GodinHook::ArmInstruction::repairBackInstructionsOfStub(HookInfo * info,size_t * calloriginal)
{
  size_t originalAddress = info->getOriginalAddr();
  uint8_t *back = info->getOriginalStubBack();
  size_t * ins = (size_t *)back;
  size_t * repair = calloriginal;
  if(NULL == repair)
    return;
  int pos = 0;

  /// 得到原始指令起始处的pc值
  size_t originalPc = originalAddress + 8;
  size_t originalLr = originalAddress +sizeofStub();


  /**
   * 需要修正的是那些机器指令内部存储的是要操作数据基于当前PC的偏移值；
   * 修正思路，计算出绝对地址，构造跳转指令。
   */
  for(int i=0;i<sizeofStub()/(sizeof(size_t));i++){
  /// 为了线程安全
  info->orig_boundaries[info->count] = i * sizeof(uint32_t);
  info->trampoline_boundaries[info->count] = pos * sizeof(uint32_t);
  info->count +=1;


  int type = getRepairInstruction(ins[i]);
  size_t x = 0;
  int top_bit;
  size_t imm32;
  size_t value;
  switch(type){
    case BLX_ARM:
    case BL_ARM:
      repair[pos++] = 0xE28FE004;	// ADD LR, PC, #4
    case B_ARM:
    case BX_ARM:
    {
      repair[pos++] = 0xE51FF004;  	// LDR PC, [PC, #-4]
      if(BLX_ARM == type)
        x = (((ins[i]) & 0xFFFFFF) << 2) | (((ins[i]) & 0x1000000) >> 23);
      else if (type == BL_ARM || type == B_ARM){
          x = ((ins[i]) & 0xFFFFFF) << 2;
        }
      else {
          x = 0;
      }
      top_bit = x >> 25;
      imm32 = top_bit ? (x | (0xFFFFFFFF << 26)) : x;
      if (type == BLX_ARM) {
          value = originalPc + imm32 + 1;
        }
      else {
          value = originalPc + imm32;
        }
      repair[pos++] = value;
      break;
    }
    case ADD_ARM:
    {
        int rd;
        int rm;
        int r;
        rd = ((ins[i]) & 0xF000) >> 12;
        rm = (ins[i]) & 0xF;
        for (r = 12; ; --r) {
            if (r != rd && r != rm) {
                break;
              }
          }
        repair[pos++] = 0xE52D0004 | (r << 12);	// PUSH {Rr}
        repair[pos++] = 0xE59F0008 | (r << 12);	// LDR Rr, [PC, #8]
        repair[pos++] = ((ins[i]) & 0xFFF0FFFF) | (r << 16);
        repair[pos++] = 0xE49D0004 | (r << 12);	// POP {Rr}
        repair[pos++] = 0xE28FF000;	// ADD PC, PC
        repair[pos++] = originalPc;
        break;
    }
    case ADR1_ARM:
    case ADR2_ARM:
    case LDR_ARM:
    case MOV_ARM:
    {
        int r;
        uint32_t value;

        r = ((ins[i]) & 0xF000) >> 12;

        if (type == ADR1_ARM || type == ADR2_ARM || type == LDR_ARM) {
            uint32_t imm32;

            imm32 = (ins[i]) & 0xFFF;
            if (type == ADR1_ARM) {
                value = originalPc + imm32;
              }
            else if (type == ADR2_ARM) {
                value = originalPc - imm32;
              }
            else if (type == LDR_ARM) {
                int is_add;

                is_add = ((ins[i]) & 0x800000) >> 23;
                if (is_add) {
                    value = ((size_t *) (originalPc + imm32))[0];
                  }
                else {
                    value = ((size_t *) (originalPc - imm32))[0];
                  }
              }
          }
        else {
            value = originalPc;
          }
        repair[pos++] = 0xE51F0000 | (r << 12);	// LDR Rr, [PC]
        repair[pos++] = 0xE28FF000;	// ADD PC, PC
        repair[pos++] = value;
        break;
    }
    default:
    {
      ///无需修正
      repair[pos++] = ins[i];
    }
  }
  originalPc +=sizeof(size_t);
  //ins++;
 }
  repair[pos++] = 0xe51ff004;	// LDR PC, [PC, #-4]
  repair[pos++] = originalLr;
}

void *GodinHook::ArmInstruction::createCallOriginalIns(HookInfo * info)
{
  void * fun = MemHelper::createExecMemory();


  //int len = sizeofStub();
  /**
   *修正指令，需要对备份的机器指令中与pc相关的进行修正
   */

  ///创建指令
  //memcpy(fun,back,len);
  //memcpy((size_t)fun+len,ldr,4);
  //memcpy((void*)((size_t)fun+len+4),&originalAddress,4);

  repairBackInstructionsOfStub(info, (size_t *) fun);
  return fun;
}
