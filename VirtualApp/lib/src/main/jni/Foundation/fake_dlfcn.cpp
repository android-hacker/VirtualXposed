// Copyright (c) 2016 avs333
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
//		of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
//		to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//		copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
//		The above copyright notice and this permission notice shall be included in all
//		copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// 		AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

#include <stdio.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <elf.h>
#include <android/log.h>
#include "fake_dlfcn.h"

#define TAG_NAME	"test2:fake_dlfcn"

#define log_info(fmt,args...) __android_log_print(ANDROID_LOG_INFO, TAG_NAME, (const char *) fmt, ##args)
#define log_err(fmt,args...) __android_log_print(ANDROID_LOG_ERROR, TAG_NAME, (const char *) fmt, ##args)

//#ifdef LOG_DBG
//#define log_dbg log_info
//#else
#define log_dbg(...)
#define log_info(...)
//#define log_err(...)
//#endif

#ifdef __arm__
#define Elf_Ehdr Elf32_Ehdr
#define Elf_Shdr Elf32_Shdr
#define Elf_Sym  Elf32_Sym
#elif defined(__aarch64__)
#define Elf_Ehdr Elf64_Ehdr
#define Elf_Shdr Elf64_Shdr
#define Elf_Sym  Elf64_Sym
#elif  defined(__i386__)
#define Elf_Ehdr Elf32_Ehdr
#define Elf_Shdr Elf32_Shdr
#define Elf_Sym  Elf32_Sym
#else
//#error "Arch unknown, please port me"
#endif

struct ctx {
	void *load_addr;
	void *dynstr;
	void *dynsym;
	int nsyms;
	off_t bias;
};

extern "C" {
int fake_dlclose(void *handle) {
	if (handle) {
		struct ctx *ctx = (struct ctx *) handle;
		if (ctx->dynsym) free(ctx->dynsym);    /* we're saving dynsym and dynstr */
		if (ctx->dynstr) free(ctx->dynstr);    /* from library file just in case */
		free(ctx);
	}
	return 0;
}

/* flags are ignored */

void *fake_dlopen(const char *libpath, int flags) {
	FILE *maps;
	char buff[256];
	struct ctx *ctx = 0;
	off_t load_addr, size;
	int k, fd = -1, found = 0;
	void *shoff;
	Elf_Ehdr *elf = (Elf_Ehdr *) MAP_FAILED;

#define fatal(fmt, args...) do { log_err(fmt,##args); goto err_exit; } while(0)

	maps = fopen("/proc/self/maps", "r");
	if (!maps) fatal("failed to open maps");

	while (!found && fgets(buff, sizeof(buff), maps))
		if (strstr(buff, "r-xp") && strstr(buff, libpath)) found = 1;

	fclose(maps);

	if (!found) fatal("%s not found in my userspace", libpath);

	if (sscanf(buff, "%lx", &load_addr) != 1)
		fatal("failed to read load address for %s", libpath);

	log_info("%s loaded in Android at 0x%08lx", libpath, load_addr);

	/* Now, mmap the same library once again */

	fd = open(libpath, O_RDONLY);
	if (fd < 0) fatal("failed to open %s", libpath);

	size = lseek(fd, 0, SEEK_END);
	if (size <= 0) fatal("lseek() failed for %s", libpath);

	elf = (Elf_Ehdr *) mmap(0, size, PROT_READ, MAP_SHARED, fd, 0);
	close(fd);
	fd = -1;

	if (elf == MAP_FAILED) fatal("mmap() failed for %s", libpath);

	ctx = (struct ctx *) calloc(1, sizeof(struct ctx));
	if (!ctx) fatal("no memory for %s", libpath);

	ctx->load_addr = (void *) load_addr;
	shoff = ((char *) elf) + elf->e_shoff;

	for (k = 0; k < elf->e_shnum; k++, shoff = (char*)shoff + elf->e_shentsize) {

		Elf_Shdr *sh = (Elf_Shdr *) shoff;
		log_dbg("%s: k=%d shdr=%p type=%x", __func__, k, sh, sh->sh_type);

		switch (sh->sh_type) {

			case SHT_DYNSYM:
				if (ctx->dynsym) fatal("%s: duplicate DYNSYM sections", libpath); /* .dynsym */
				ctx->dynsym = malloc(sh->sh_size);
				if (!ctx->dynsym) fatal("%s: no memory for .dynsym", libpath);
				memcpy(ctx->dynsym, ((char *) elf) + sh->sh_offset, sh->sh_size);
				ctx->nsyms = (sh->sh_size / sizeof(Elf_Sym));
				break;

			case SHT_STRTAB:
				if (ctx->dynstr) break;    /* .dynstr is guaranteed to be the first STRTAB */
				ctx->dynstr = malloc(sh->sh_size);
				if (!ctx->dynstr) fatal("%s: no memory for .dynstr", libpath);
				memcpy(ctx->dynstr, ((char *) elf) + sh->sh_offset, sh->sh_size);
				break;

			case SHT_PROGBITS:
				if (!ctx->dynstr || !ctx->dynsym) break;
				/* won't even bother checking against the section name */
				ctx->bias = (off_t) sh->sh_addr - (off_t) sh->sh_offset;
				k = elf->e_shnum;  /* exit for */
				break;
		}
	}

	munmap(elf, size);
	elf = 0;

	if (!ctx->dynstr || !ctx->dynsym) fatal("dynamic sections not found in %s", libpath);

#undef fatal

	log_dbg("%s: ok, dynsym = %p, dynstr = %p", libpath, ctx->dynsym, ctx->dynstr);

	return ctx;

	err_exit:
	if (fd >= 0) close(fd);
	if (elf != MAP_FAILED) munmap(elf, size);
	fake_dlclose(ctx);
	return 0;
}

void *fake_dlsym(void *handle, const char *name) {
	int k;
	struct ctx *ctx = (struct ctx *) handle;
	Elf_Sym *sym = (Elf_Sym *) ctx->dynsym;
	char *strings = (char *) ctx->dynstr;

	for (k = 0; k < ctx->nsyms; k++, sym++)
		if (strcmp(strings + sym->st_name, name) == 0) {
			/*  NB: sym->st_value is an offset into the section for relocatables,
            but a VMA for shared libs or exe files, so we have to subtract the bias */
			void *ret = (char*)ctx->load_addr + sym->st_value - ctx->bias;
			log_info("%s found at %p", name, ret);
			return ret;
		}
	return 0;
}
}
