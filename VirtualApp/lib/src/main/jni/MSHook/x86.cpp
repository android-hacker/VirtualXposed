#include "x86.h"
#include "x86_64.h"

static size_t MSGetInstructionWidthIntel(void *start) {
    hde64s decode;
    return hde64_disasm(start, &decode);
}

void x86::SubstrateHookFunctionx86(SubstrateProcessRef process, void *symbol, void *replace, void **result){
    if (MSDebug)
        MSLog(MSLogLevelNotice, "SubstrateHookFunctionx86(process:%p, symbol:%p, replace:%p, result:%p)", process, symbol, replace, result);
    if (symbol == NULL)
        return;

    uintptr_t source(reinterpret_cast<uintptr_t>(symbol));
    uintptr_t target(reinterpret_cast<uintptr_t>(replace));

    uint8_t *area(reinterpret_cast<uint8_t *>(symbol));

    size_t required(MSSizeOfJump(target, source));

    if (MSDebug) {
        char name[16];
        sprintf(name, "%p", area);
        MSLogHex(area, 32, name);
    }

    size_t used(0);
    while (used < required) {
        size_t width(MSGetInstructionWidthIntel(area + used));
        if (width == 0) {
            MSLog(MSLogLevelError, "MS:Error:MSGetInstructionWidthIntel(%p) == 0", area + used);
            return;
        }

        used += width;
    }

    size_t blank(used - required);

    if (MSDebug) {
        char name[16];
        sprintf(name, "%p", area);
        MSLogHex(area, used + sizeof(uint16_t), name);
    }

    uint8_t backup[used];
    memcpy(backup, area, used);

    if (result != NULL) {

    if (backup[0] == 0xe9) {
        *result = reinterpret_cast<void *>(source + 5 + *reinterpret_cast<uint32_t *>(backup + 1));
        return;
    }

    if (!ia32 && backup[0] == 0xff && backup[1] == 0x25) {
        *result = *reinterpret_cast<void **>(source + 6 + *reinterpret_cast<uint32_t *>(backup + 2));
        return;
    }

    size_t length(used + MSSizeOfJump(source + used));

    for (size_t offset(0), width; offset != used; offset += width) {
        hde64s decode;
        hde64_disasm(backup + offset, &decode);
        width = decode.len;
        //_assert(width != 0 && offset + width <= used);

#ifdef __LP64__
        if ((decode.modrm & 0xc7) == 0x05) {
            if (decode.opcode == 0x8b) {
                void *destiny(area + offset + width + int32_t(decode.disp.disp32));
                uint8_t reg(decode.rex_r << 3 | decode.modrm_reg);
                length -= decode.len;
                length += MSSizeOfPushPointer(destiny);
                length += MSSizeOfPop(reg);
                length += MSSizeOfMove64();
            } else {
                MSLog(MSLogLevelError, "MS:Error: Unknown RIP-Relative (%.2x %.2x)", decode.opcode, decode.opcode2);
                continue;
            }
        } else
#endif

        if (backup[offset] == 0xe8) {
            int32_t relative(*reinterpret_cast<int32_t *>(backup + offset + 1));
            void *destiny(area + offset + decode.len + relative);

            if (relative == 0) {
                length -= decode.len;
                length += MSSizeOfPushPointer(destiny);
            } else {
                length += MSSizeOfSkip();
                length += MSSizeOfJump(destiny);
            }
        } else if (backup[offset] == 0xeb) {
            length -= decode.len;
            length += MSSizeOfJump(area + offset + decode.len + *reinterpret_cast<int8_t *>(backup + offset + 1));
        } else if (backup[offset] == 0xe9) {
            length -= decode.len;
            length += MSSizeOfJump(area + offset + decode.len + *reinterpret_cast<int32_t *>(backup + offset + 1));
        } else if (
            backup[offset] == 0xe3 ||
            (backup[offset] & 0xf0) == 0x70
            // XXX: opcode2 & 0xf0 is 0x80?
        ) {
            length += decode.len;
            length += MSSizeOfJump(area + offset + decode.len + *reinterpret_cast<int8_t *>(backup + offset + 1));
        }
    }

    uint8_t *buffer(reinterpret_cast<uint8_t *>(mmap(
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

    {
        uint8_t *current(buffer);

        for (size_t offset(0), width; offset != used; offset += width) {
            hde64s decode;
            hde64_disasm(backup + offset, &decode);
            width = decode.len;
            //_assert(width != 0 && offset + width <= used);

#ifdef __LP64__
            if ((decode.modrm & 0xc7) == 0x05) {
                if (decode.opcode == 0x8b) {
                    void *destiny(area + offset + width + int32_t(decode.disp.disp32));
                    uint8_t reg(decode.rex_r << 3 | decode.modrm_reg);
                    MSPushPointer(current, destiny);
                    MSWritePop(current, reg);
                    MSWriteMove64(current, reg, reg);
                } else {
                    MSLog(MSLogLevelError, "MS:Error: Unknown RIP-Relative (%.2x %.2x)", decode.opcode, decode.opcode2);
                    goto copy;
                }
            } else
#endif

            if (backup[offset] == 0xe8) {
                int32_t relative(*reinterpret_cast<int32_t *>(backup + offset + 1));
                if (relative == 0)
                    MSPushPointer(current, area + offset + decode.len);
                else {
                    MSWrite<uint8_t>(current, 0xe8);
                    MSWrite<int32_t>(current, MSSizeOfSkip());
                    void *destiny(area + offset + decode.len + relative);
                    MSWriteSkip(current, MSSizeOfJump(destiny, current + MSSizeOfSkip()));
                    MSWriteJump(current, destiny);
                }
            } else if (backup[offset] == 0xeb)
                MSWriteJump(current, area + offset + decode.len + *reinterpret_cast<int8_t *>(backup + offset + 1));
            else if (backup[offset] == 0xe9)
                MSWriteJump(current, area + offset + decode.len + *reinterpret_cast<int32_t *>(backup + offset + 1));
            else if (
                backup[offset] == 0xe3 ||
                (backup[offset] & 0xf0) == 0x70
            ) {
                MSWrite<uint8_t>(current, backup[offset]);
                MSWrite<uint8_t>(current, 2);
                MSWrite<uint8_t>(current, 0xeb);
                void *destiny(area + offset + decode.len + *reinterpret_cast<int8_t *>(backup + offset + 1));
                MSWrite<uint8_t>(current, MSSizeOfJump(destiny, current + 1));
                MSWriteJump(current, destiny);
            } else
#ifdef __LP64__
                copy:
#endif
            {
                MSWrite(current, backup + offset, width);
            }
        }

        MSWriteJump(current, area + used);
    }

    if (mprotect(buffer, length, PROT_READ | PROT_EXEC) == -1) {
        MSLog(MSLogLevelError, "MS:Error:mprotect():%d", errno);
        goto fail;
    }

    *result = buffer;

    if (MSDebug) {
        char name[16];
        sprintf(name, "%p", *result);
        MSLogHex(buffer, length, name);
    }

    }

    {
        SubstrateHookMemory code(process, area, used);

        uint8_t *current(area);
        MSWriteJump(current, target);
        for (unsigned offset(0); offset != blank; ++offset)
            MSWrite<uint8_t>(current, 0x90);
    }

    if (MSDebug) {
        char name[16];
        sprintf(name, "%p", area);
        MSLogHex(area, used + sizeof(uint16_t), name);
    }
}

