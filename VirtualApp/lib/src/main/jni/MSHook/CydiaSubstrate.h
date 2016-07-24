#ifndef CYDIASUBSTRATE_H_
#define CYDIASUBSTRATE_H_

#include <dlfcn.h>
#include <stdlib.h>

#define _finline \
    inline __attribute__((__always_inline__))
#define _disused \
    __attribute__((__unused__))
#define _extern \
    extern "C" __attribute__((__visibility__("default")))

#include "SubstrateStruct.h"
#endif /* CYDIASUBSTRATE_H_ */
