#include <mem_helper.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/mman.h>
#include <stdlib.h>
#include <asm/unistd.h>
#include <sys/syscall.h>


using namespace std;

#define maps "/proc/self/maps"
#define MAX_BUF 512

bool GodinHook::MemHelper::isFunctionAddr(size_t addr)
{
  char buf[MAX_BUF]={0};

     FILE * fp = fopen(maps,"r");

     if(NULL == fp){
         return false;
     }

     while(fgets(buf,MAX_BUF,fp)){

         /*
          *可执行程序和so库对应的属性段是“r-xp”
          * */
         if(strstr(buf,"r-xp")!=NULL){
            size_t startAddr = strtoul(strtok(buf,"-"),NULL,16);
            size_t endAddr   = strtoul(strtok(NULL," "),NULL,16);
             if(addr>=startAddr && addr<=endAddr){
                 fclose(fp);
                 //printf("startAddr = 0x%x \n",startAddr);
                 //printf("endAddr = 0x%x \n",endAddr);
                 return true;
             }

         }
     }

     fclose(fp);
     perror("this functionAddr is not a function!\n");
     return false;
}

bool GodinHook::MemHelper::unProtectMemory(size_t addr, int size)
{
  /// 获得当前系统的内存页大小
  int pageSize = sysconf(_SC_PAGESIZE);

  /// 计算所在内存页中的偏移
  int align = addr % pageSize;

  int ret  = mprotect((void*)(addr-align),(size_t)(size+align),PROT_READ|PROT_WRITE|PROT_EXEC);
  if(-1 == ret){
      perror("mprotect");
      return false;
    }
  return true;

}

bool GodinHook::MemHelper::protectMemory(size_t addr, int size)
{
  /// 获得当前系统的内存页大小
  int pageSize = sysconf(_SC_PAGESIZE);

  /// 计算所在内存页的偏移
  int align = addr % pageSize;

  int ret  = syscall(__NR_mprotect,(void*)(addr-align),(size_t)(size+align),PROT_READ|PROT_EXEC);
  if(-1 == ret){
      perror("mprotect");
      return false;
    }
  return true;

}

void *GodinHook::MemHelper::createExecMemory()
{
  /// 获得当前系统的内存页大小
  int pageSize = sysconf(_SC_PAGESIZE);

  return mmap(NULL,pageSize,PROT_READ | PROT_WRITE | PROT_EXEC,MAP_ANONYMOUS | MAP_PRIVATE,0,0);
}

void GodinHook::MemHelper::freeExecMemory(void *address)
{
  /// 获得当前系统的内存页大小
  int pageSize = sysconf(_SC_PAGESIZE);
  munmap(address,pageSize);
}


