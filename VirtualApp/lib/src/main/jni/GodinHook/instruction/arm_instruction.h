#ifndef ARM_INSTRUCTION_H
#define ARM_INSTRUCTION_H

#include "instruction_helper.h"
#include <stdint.h>
namespace GodinHook {

  class ArmInstruction:public InstructionHelper{



  public:

    ~ArmInstruction(){}

    /**
     * @brief sizeofStub
     *  计算stub指令所需空间大小，单位字节
     * @return
     *  返回stub所需字节数
     */
    int sizeofStub()
    {
      return 8;
    }

    /**
     * @brief createStub
     *  构造stub指令，亦即构造跳转指令,特别注意需要刷新指令缓存
     * @param originalAddress
     *  原方法地址
     * @param targetAddress
     *  跳转的目标地址
     * @return
     *  存储stub指令的空间地址
     */
    void  createStub(HookInfo * info);


    int getRepairInstruction(size_t ins);

    void  repairBackInstructionsOfStub(HookInfo * info,size_t * calloriginal);
    void *createCallOriginalIns(HookInfo * info);

   private:
    static uint8_t ldr[4];
    enum RepairIns{
      BLX_ARM,      /*!< BLX <label>*/
      BL_ARM,       /*!< BL <label>*/
      B_ARM,        /*!< B <label>*/
      BX_ARM,       /*!< BX PC*/
      ADD_ARM,      /*!< ADD Rd, PC, Rm (Rd != PC, Rm != PC)*/
      ADR1_ARM,     /*!< ADR Rd, <label>*/
      ADR2_ARM,     /*!< ADR Rd, <label>*/
      MOV_ARM,      /*!< MOV Rd, PC*/
      LDR_ARM,      /*!<LDR Rt, <label>*/

      UNDEFINE,
    };







  };


}
#endif // ARM_INSTRUCTION_H
