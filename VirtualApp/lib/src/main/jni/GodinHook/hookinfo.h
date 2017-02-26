#ifndef HOOKINFO_H
#define HOOKINFO_H

//#include <godin_type.h>
#include <stdint.h>
#include <iostream>
namespace GodinHook {


  enum HookStatus{
    ERRSTATUS = 0,    /*!< 错误状态 */
    REGISTERED,
    HOOKED,           /*!< 已经被hook */
  };

  enum FunctionType{
    ERRTYEPE  = 0,    /*!< 错误类型 */
    ARM,              /*!< ARM指令集 */
    THUMB ,           /*!< thumb指令集 */
    ARM64,            /*!< ARMV8指令集*/
  };

class HookInfo{


public :

  /**
   * @brief HookInfo
   *  唯一的构造函数
   * @param originalAddr
   *  原方法地址
   * @param hookAddr
   *  新方法地址
   * @param callOriginalAddr
   *  存储回调原方法的地址
   */
  HookInfo(size_t originalAddr,size_t hookAddr,size_t ** callOriginalAddr)
    :original_addr_(originalAddr),hook_addr_(hookAddr),call_original_addr_(callOriginalAddr),
    original_stub_back_(NULL),back_len_(0),call_original_ins_(NULL),hook_status_(ERRSTATUS),
    original_function_type_(ERRTYEPE),hook_function_type_(ERRTYEPE),count(0){}

private:
  /// 构造函数中负责初始化这三个字段
  size_t original_addr_;                    /*!< 原方法地址 */
  size_t hook_addr_;                        /*!< 新方法地址 */
  size_t **call_original_addr_;             /*!< 回调原方法 */

  /// registerAndHook初始化这三个字段,
  /// unhook回收资源时要用到
  uint8_t *original_stub_back_;             /*!< 存储原方法被stub覆盖的机器码 */
  int    back_len_;                         /*!< 原方法被stub覆盖的机器指令大小，亦即stub大小 */
  uint8_t *call_original_ins_;              /*!< 存储经过修正过的原方法被stub覆盖的机器指令，以及追加跳转至原方法剩余机器指令的跳转指令*/

  HookStatus hook_status_;                  /*!< 当前hook状态*/
  FunctionType original_function_type_;     /*!< 原方法指令集类型*/
  FunctionType hook_function_type_;         /*!< 新方法指令集类型*/

public:
  /// 为了保证线程安全
  int orig_boundaries[8];
  int trampoline_boundaries[32];
  int count;

public:

  void setOriginalAddr(size_t addr){
    this->original_addr_ = addr;
  }
  size_t getOriginalAddr(){
    return this->original_addr_;
  }

  void setHookAddr(size_t addr){
    this->hook_addr_ = addr;
  }
  size_t getHookAddr(){
    return this->hook_addr_;
  }

  void setCallOriginalAddr(size_t ** addr){
     this->call_original_addr_ = addr;
  }
  size_t ** getCallOriginalAddr(){
    return this->call_original_addr_;
  }



  void setOriginalStubBack(uint8_t* addr){
    this->original_stub_back_ = addr;
  }
 uint8_t* getOriginalStubBack(){
    return this->original_stub_back_;
  }

  void setBackLen(int len){
    this->back_len_ = len;
  }
  size_t getBackLen(){
    return this->back_len_;
  }

  void setCallOriginalIns(uint8_t* addr){
     this->call_original_ins_ = addr;
  }
  uint8_t* getCallOriginalIns(){
    return this->call_original_ins_;
  }


  void setHookStatus(HookStatus status){
    this->hook_status_ = status;
  }
  HookStatus getHookStatus(){
    return this->hook_status_;
  }

  void setOriginalFunctionType(FunctionType type){
    this->original_function_type_ = type;
  }
  FunctionType getOriginalFunctiontype(){
    return this->original_function_type_;
  }

  void setHookFunctionType(FunctionType type){
    this->hook_function_type_ = type;
  }
  FunctionType getHookFunctionType(){
    return this->hook_function_type_;
  }

};

}
#endif // HOOKINFO_H
