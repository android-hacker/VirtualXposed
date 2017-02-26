#include "ARM.h"
#include "PosixMemory.h"

void ARM::SubstrateHookFunctionARM(SubstrateProcessRef process, void *symbol, void *replace, void **result) {
    if (symbol == NULL)
        return;

    uint32_t *area(reinterpret_cast<uint32_t *>(symbol));
    uint32_t *arm(area);

    const size_t used(8);

    uint32_t backup[used / sizeof(uint32_t)] = {arm[0], arm[1]};

    if (MSDebug) {
        char name[16];
        sprintf(name, "%p", area);
        MSLogHexEx(area, used + sizeof(uint32_t), 4, name);
    }

    if (result != NULL) {

    if (backup[0] == A$ldr_rd_$rn_im$(A$pc, A$pc, 4 - 8)) {
        *result = reinterpret_cast<void *>(backup[1]);
        return;
    }

    size_t length(used);
    for (unsigned offset(0); offset != used / sizeof(uint32_t); ++offset)
        if (A$pcrel$r(backup[offset])) {
            if ((backup[offset] & 0x02000000) == 0 || (backup[offset] & 0x0000f000 >> 12) != (backup[offset] & 0x0000000f))
                length += 2 * sizeof(uint32_t);
            else
                length += 4 * sizeof(uint32_t);
        }

    length += 2 * sizeof(uint32_t);

    uint32_t *buffer(reinterpret_cast<uint32_t *>(mmap(
        NULL, length, PROT_READ | PROT_WRITE, MAP_ANON | MAP_PRIVATE, -1, 0
    )));

    if (buffer == MAP_FAILED) {
        MSLog(MSLogLevelError, "MS:Error:mmap() = %d", errno);
        *result = NULL;
        return;
    }

    if (false) fail: {
        munmap(buffer, length);
        *result = NULL;
        return;
    }

    size_t start(0), end(length / sizeof(uint32_t));
    uint32_t *trailer(reinterpret_cast<uint32_t *>(buffer + end));
    for (unsigned offset(0); offset != used / sizeof(uint32_t); ++offset)
        if (A$pcrel$r(backup[offset])) {
            union {
                uint32_t value;

                struct {
                    uint32_t rm : 4;
                    uint32_t : 1;
                    uint32_t shift : 2;
                    uint32_t shiftamount : 5;
                    uint32_t rd : 4;
                    uint32_t rn : 4;
                    uint32_t l : 1;
                    uint32_t w : 1;
                    uint32_t b : 1;
                    uint32_t u : 1;
                    uint32_t p : 1;
                    uint32_t mode : 1;
                    uint32_t type : 2;
                    uint32_t cond : 4;
                };
            } bits = {backup[offset+0]}, copy(bits);

            bool guard;
            if (bits.mode == 0 || bits.rd != bits.rm) {
                copy.rn = bits.rd;
                guard = false;
            } else {
                copy.rn = bits.rm != A$r0 ? A$r0 : A$r1;
                guard = true;
            }

            if (guard)
                buffer[start++] = A$stmdb_sp$_$rs$((1 << copy.rn));

            buffer[start+0] = A$ldr_rd_$rn_im$(copy.rn, A$pc, (end-1 - (start+0)) * 4 - 8);
            buffer[start+1] = copy.value;

            start += 2;

            if (guard)
                buffer[start++] = A$ldmia_sp$_$rs$((1 << copy.rn));

            *--trailer = reinterpret_cast<uint32_t>(area + offset) + 8;
            end -= 1;
        } else
            buffer[start++] = backup[offset];

    buffer[start+0] = A$ldr_rd_$rn_im$(A$pc, A$pc, 4 - 8);
    buffer[start+1] = reinterpret_cast<uint32_t>(area + used / sizeof(uint32_t));

    if (mprotect(buffer, length, PROT_READ | PROT_EXEC) == -1) {
        MSLog(MSLogLevelError, "MS:Error:mprotect():%d", errno);
        goto fail;
    }

    *result = buffer;

    if (MSDebug) {
        char name[16];
        sprintf(name, "%p", *result);
        MSLogHexEx(buffer, length, 4, name);
    }

    }

    {
        SubstrateHookMemory code(process, symbol, used);

        arm[0] = A$ldr_rd_$rn_im$(A$pc, A$pc, 4 - 8);
        arm[1] = reinterpret_cast<uint32_t>(replace);
    }

    if (MSDebug) {
        char name[16];
        sprintf(name, "%p", area);
        MSLogHexEx(area, used + sizeof(uint32_t), 4, name);
    }
}


