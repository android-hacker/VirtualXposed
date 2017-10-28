//
// VirtualApp Native Project
//

#ifndef NDK_LOG_H
#define NDK_LOG_H


#define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))

#define NATIVE_METHOD(func_ptr, func_name, signature) { func_name, signature, reinterpret_cast<void*>(func_ptr) }

#endif //NDK_LOG_H
