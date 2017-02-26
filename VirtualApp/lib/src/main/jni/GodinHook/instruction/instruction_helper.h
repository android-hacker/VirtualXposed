#ifndef INSTRUCTIONHELPER_H
#define INSTRUCTIONHELPER_H

//#include <godin_type.h>
#include <stdint.h>
#include "../hookinfo.h"
#include <iostream>
#include "../hookinfo.h"


namespace GodinHook {


/**
 * @brief 指令集基类
 *
 * 其直接子类负责具体的指令集，比如thumb2,arm,arm64.
 * 该类为抽象类，其子类需负责实现其中的纯虚函数。
 */
class InstructionHelper{
public:
  /**
   * @brief ~InstructionHelper
   *  虚构造函数，析构时可以调用其子类的析构函数，防止析构不彻底
   */
  virtual ~InstructionHelper(){}

  /**
  * @brief createStub
  *   在原方法机器码起始处创建跳转至新方法的跳转指令
  *
  * @param originalAddress
  *   原方法地址,需考虑兼容32/64
  * @param targetAddress
  *   新方法地址,需考虑兼容32/64
  */
  virtual void createStub(HookInfo * info)=0;


  virtual void * createCallOriginalIns(HookInfo * info)=0;

  virtual int getRepairInstruction(size_t ins)=0;

  virtual void repairBackInstructionsOfStub(HookInfo * info,size_t * calloriginal)=0;


  virtual void isResetStubSize(size_t originalAddress){}

 /**
   * @brief sizeofStub
   *  计算stub机器指令所占空间
   *
   * @return
   *  stub机器指令所占字节大小
   */
  virtual int sizeofStub()=0;


  /**
   * @brief getBackOfStub
   * 备份被stub覆盖的机器指令，该函数内部申请的堆内存空间，需要调用者手动释放。
   *
   * @param targetAddress
   * 机器指令起始地址
   * @return
   * 返回存储备份指令的数组地址
   */
  uint8_t * getBackOfStub(size_t targetAddress );

  /**
   * @brief getFunctionType
   *   获得该函数的指令集类型
   *
   * @param functionAddr
   *   必须是一个函数的地址
   *
   * @return
   *   该函数指令集类型，ARM,THUMB or ARMV8
   */
  static FunctionType getFunctionType(size_t functionAddr);
  
  /**
   * @brief valueToMem
   *  修正地址，以便内存操作
   * @param addr
   *  函数地址info->getOriginalAddr()
   * @return 
   *  修正后的值
   */
  static size_t valueToMem(size_t addr);
  
  
  /**
   * @brief valueToPc
   *  修正地址，以便正确运行
   * @param addr
   *  机器码地址
   * @return 
   *  修正后的值
   */
  static size_t valueToPc(size_t addr);






private:
  //static const unsigned char TargetJump[16];
};

}
#endif // INSTRUCTIONHELPER_H
