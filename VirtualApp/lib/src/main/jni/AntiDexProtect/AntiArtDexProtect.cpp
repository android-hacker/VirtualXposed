//
// Created by Lody on 2017/1/14.
//

#include <Foundation/VMPatch.h>
#include "AntiArtDexProtect.h"


const void *
(*artDexFileOpenMemorV19)(unsigned char const *base, unsigned int size, std::string const &location,
                          unsigned int location_checksum, void *mem_map) = NULL;

const void *
(*artDexFileOpenMemoryV21)(const uint8_t *base, size_t size, const std::string &location,
                           uint32_t location_checksum, void *mem_map,
                           std::string *error_msg) = NULL;

const void *
(*artDexFileOpenMemoryV22)(const uint8_t *base, size_t size, const std::string &location,
                           uint32_t location_checksum, void *mem_map,
                           const void *oat_file, std::string *error_msg) = NULL;

static int api_level = -1;
static bool resolved = false;

int resolveSymbol(int api_level) {

    void *handle = getVMHandle();
    if (api_level <= 19) {
        artDexFileOpenMemorV19 = (const void *(*)(const unsigned char *, unsigned int,
                                                  const std::string &,
                                                  unsigned int, void *)) dlsym(handle,
                                                                               "_ZN3art7DexFile10OpenMemoryEPKhjRKSsjPNS_6MemMapE");
        if (artDexFileOpenMemorV19 == NULL) {
            return -1;
        }

    } else if (api_level <= 21) {
        artDexFileOpenMemoryV21 = (const void *(*)(const uint8_t *, size_t, const std::string &,
                                                   uint32_t, void *, std::string *)) dlsym(handle,
                                                                                           "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPS9_");
        return -1;
    } else if (api_level <= 22) {
        artDexFileOpenMemoryV22 = (const void *(*)(const uint8_t *, size_t, const std::string &,
                                                   uint32_t, void *, const void *,
                                                   std::string *)) dlsym(handle,
                                                                         "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_7OatFileES2_PS9_");
        if (artDexFileOpenMemoryV22 == NULL) {
            artDexFileOpenMemoryV22 = (const void *(*)(const uint8_t *, size_t, const std::string &,
                                                       uint32_t, void *, const void *,
                                                       std::string *)) dlsym(handle,
                                                                             "_ZN3art7DexFile10OpenMemoryEPKhjRKNSt3__112basic_stringIcNS3_11char_traitsIcEENS3_9allocatorIcEEEEjPNS_6MemMapEPKNS_7OatFileES2_PS9_.constprop.183");
        }
        if (artDexFileOpenMemoryV22 == NULL) {
            return -1;
        }
    }
    resolved = true;
}

const void *loadMemory(const uint8_t *base, size_t size) {
    if (!resolved) {
        return NULL;
    }
    std::string location = "";
    std::string err_msg;

    const DexHeader *hdr = reinterpret_cast<const DexHeader *>(base);
    uint32_t checksum = hdr->checksum_;
    if (api_level <= 19) {
        return artDexFileOpenMemorV19(base, size, location, checksum, NULL);
    } else if (api_level <= 21) {
        return artDexFileOpenMemoryV21(base, size, location, checksum, NULL, &err_msg);
    } else if (api_level <= 22) {
        return artDexFileOpenMemoryV22(base, size, location, checksum, NULL, NULL, &err_msg);
    }
    return NULL;
}

