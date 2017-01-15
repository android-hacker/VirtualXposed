#ifndef HOOKER_H_
#define HOOKER_H_

#include <sys/mman.h>
#include <errno.h>
#include <stdio.h>
#include <string.h>

#include "Debug.h"
#include "Log.h"
#include "PosixMemory.h"
#include "CydiaSubstrate.h"

namespace Cydia{

	void MSHookFunction(const char *soname, const char *symbol, void *replace_func, void **old_func);
	void MSHookFunction(void *symbol, void *replace, void **result);
}
#endif /* HOOKER_H_ */