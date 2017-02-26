#ifndef MEMHELPER_H
#define MEMHELPER_H

#include <fstream>
#include <sys/mman.h>

namespace GodinHook {
  class MemHelper{
  public:
    /**
     * @brief isFunctionAddr
     *  判断所给地址是否是一个函数地址
     * @param addr
     *
     * @return
     *  是函数地址返回true，反之返回false。
     */
    static bool isFunctionAddr(size_t addr);

    /**
     * @brief unProtectMemory
     *  将起始地址处开始的size大小去保护，即添加写权限。
     *  此函数可能真正去保护的部分要比size大。
     *
     * @param addr
     *  起始地址
     * @param size
     *  要去保护的内存空间大小
     *
     * @return
     *  执行成功返回true，失败返回false
     */
    static bool unProtectMemory(size_t addr,int size);

    /**
     * @brief protectMemory
     *  将起始地址处开始的size大小添加保护，即取消写权限。
     *  此函数可能真正添加保护的部分要比size大。
     *
     * @param addr
     *  起始地址
     * @param size
     *  要添加保护的内存空间大小
     *
     * @return
     *  执行成功返回true，失败返回false
     */
    static bool protectMemory(size_t addr,int size);

    /**
     * @brief createExecMemory
     * 创建一个内存页大小的可执行内存区域
     * @return
     * 可执行区域的起始地址
     */
    static void * createExecMemory();

    /**
     * @brief freeExecMemory
     * 释放一个内存页大小的可执行区域
     * @param address
     * 要释放的可执行内存起始地址
     */
    static void freeExecMemory(void * address);

  };
}
#endif // MEMHELPER_H
