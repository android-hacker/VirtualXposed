//
// VirtualApp Native Project
//

#ifndef NDK_LOG_H
#define NDK_LOG_H

#include <fb/include/fb/fbjni.h>

#define NATIVE_METHOD(func_ptr, func_name, signature) { func_name, signature, reinterpret_cast<void*>(func_ptr) }

class ScopeUtfString {
public:
    ScopeUtfString(jstring j_str) : _j_str(j_str),
                                    _c_str(facebook::jni::Environment::current()->GetStringUTFChars(j_str, NULL)) {
    }

    const char *c_str() {
        return _c_str;
    }

    ~ScopeUtfString() {
        facebook::jni::Environment::current()->ReleaseStringUTFChars(_j_str, _c_str);
    }

private:
    jstring _j_str;
    const char *_c_str;
};

#endif //NDK_LOG_H
