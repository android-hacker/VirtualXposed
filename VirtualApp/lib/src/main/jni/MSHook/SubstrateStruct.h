/*
 * SubstrateMacro.h
 *
 *  Created on: 2016Äê2ÔÂ22ÈÕ
 *      Author: peng
 */

#ifndef SUBSTRATEMACRO_H_
#define SUBSTRATEMACRO_H_

#include <stdlib.h>
typedef struct __SubstrateProcess *SubstrateProcessRef;
typedef void *SubstrateAllocatorRef;
typedef struct SubstrateMemory {
    void *address_;
    size_t width_;
	SubstrateMemory(void *address, size_t width):address_(address), width_(width) {}
}*SubstrateMemoryRef;

#endif /* SUBSTRATEMACRO_H_ */
