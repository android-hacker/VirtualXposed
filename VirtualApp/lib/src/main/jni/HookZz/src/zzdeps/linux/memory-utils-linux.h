#ifndef zzdeps_linux_memory_utils_linux_h
#define zzdeps_linux_memory_utils_linux_h

#include <err.h>
#include <stdio.h>
#include <stdlib.h>

#include "../zz.h"

zpointer zz_linux_vm_search_code_cave(zaddr address, zsize range_size, zsize size);

#endif