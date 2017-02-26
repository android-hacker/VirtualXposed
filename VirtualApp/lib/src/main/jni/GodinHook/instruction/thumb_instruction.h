#ifndef THUMB2_INSTRUCTION_H
#define THUMB2_INSTRUCTION_H

#include "instruction_helper.h"



namespace GodinHook {
  /**
   * @brief The Thumb2Instruction class
   * 仅支持ARMv5T之后支持thumb2指令集；
   *
   * 另外thumb2指令集中的ldr指令需要4字节对齐；
   * 传入pc中的指令地址末尾bit要为1；
   *
   */
  class ThumbInstruction :public InstructionHelper{


    // InstructionHelper interface
  public:
   ThumbInstruction():stub_len_(12){}

   /**
    * @brief sizeofStub
    *  计算stub指令所需空间大小，单位字节
    * @return
    *  返回stub所需字节数
    */
    void createStub(HookInfo * info);


    void *createCallOriginalIns(HookInfo * info);
    int getRepairInstruction(size_t ins);
    void repairBackInstructionsOfStub(HookInfo * info,size_t * calloriginal);
    /**
     * @brief sizeofStub
     * 计算thumb中stub块大小；
     *
     * @return
     * 返回stub块所需字节数
     */
    int sizeofStub();
    void isResetStubSize(size_t originalAddress);
  private:
    int repairThumb32Instruction(uint32_t pc, uint16_t high_instruction, uint16_t low_instruction, uint16_t *respair);
    int repairThumb16Instruction(uint32_t pc, uint16_t instruction, uint16_t *respair);
    bool isPcNeedAlgin(size_t address);

    /**
     * @brief isThumb2Instruction
     * 判断当前指令是thumb还是thumb2
     * @param ins
     * 指令
     * @return
     * 返回true是thumb2;
     * 返回false是thumb;
     */
    bool isThumb2Instruction(uint16_t ins);

    void setStubSize(int len)
    {
      stub_len_ = len;
    }

    int stub_len_;
    enum RepairIns{
          // B <label>
          B1_THUMB16 = 0,
          // B <label>
          B2_THUMB16,
          // BX PC
          BX_THUMB16,
          // ADD <Rdn>, PC (Rd != PC, Rn != PC) 在对ADD进行修正时，
          //采用了替换PC为Rr的方法，当Rd也为PC时，由于之前更改了Rr的值，
          //可能会影响跳转后的正常功能。
          ADD_THUMB16,
          // MOV Rd, PC
          MOV_THUMB16,
          // ADR Rd, <label>
          ADR_THUMB16,
          // LDR Rt, <label>
          LDR_THUMB16,

          // BLX <label>
          BLX_THUMB32,
          // BL <label>
          BL_THUMB32,
          // B.W <label>
          B1_THUMB32,
          // B.W <label>
          B2_THUMB32,
          // ADR.W Rd, <label>
          ADR1_THUMB32,
          // ADR.W Rd, <label>
          ADR2_THUMB32,
          // LDR.W Rt, <label>
          LDR_THUMB32,
          // TBB [PC, Rm]
          TBB_THUMB32,
          // TBH [PC, Rm, LSL #1]
          TBH_THUMB32,

          UNDEFINE,
    };
  };
}



#endif // THUMB2_INSTRUCTION_H
