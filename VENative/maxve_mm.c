/*
 * Copyright (c) 2009 Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, California 95054, U.S.A. All rights reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are
 * subject to the Sun Microsystems, Inc. standard license agreement and
 * applicable provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * Parts of the product may be derived from Berkeley BSD systems,
 * licensed from the University of California. UNIX is a registered
 * trademark in the U.S.  and in other countries, exclusively licensed
 * through X/Open Company, Ltd.
 *
 * Sun, Sun Microsystems, the Sun logo and Java are trademarks or
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other
 * countries.
 *
 * This product is covered and controlled by U.S. Export Control laws and
 * may be subject to the export or import laws in other
 * countries. Nuclear, missile, chemical biological weapons or nuclear
 * maritime end uses or end users, whether direct or indirect, are
 * strictly prohibited. Export or reexport to countries subject to
 * U.S. embargo or to entities identified on U.S. export exclusion lists,
 * including, but not limited to, the denied persons and specially
 * designated nationals lists is strictly prohibited.
 *
 */

/*
 * Implementation of native methods from the Maxine Memory/VirtualMemory classes and malloc etc.
 *
 * Author: Mick Jordan, Sun Microsystems Inc.
 */
#include <os.h>
#include <sched.h>
#include <hypervisor.h>
#include <types.h>
#include <lib.h>
#include <xmalloc.h>
#include <jni.h>
#include <virtualMemory.h>

#undef malloc
#undef free
#undef realloc

/*
 * This version just forwards to GUK xmalloc
 * TODO: not exit here when out of heap
 */
void init_malloc(void) {
}

void *malloc(size_t n) {
  void *result = guk_xmalloc(n, 4);
  if (result == NULL) {
    guk_xprintk("malloc: out of heap, request is %lx\n", n);
    guk_printk("malloc: out of heap, request is %lx\n", n);
    guk_crash_exit_backtrace();
  }
  return result;
}

void free(void *a1) {
  guk_xfree(a1);
}

void *realloc(void *p, size_t n) {
  void *result = guk_xrealloc(p, n, 4);
  if (result == NULL) {
    guk_xprintk("realloc: out of heap, request is %lx\n", n);
    guk_crash_exit();
  }
  return result;
}

void *calloc(int nelems, size_t n) {
  size_t count = n * nelems;
  void *result = malloc(count);
  memset(result, 0, count);
//  ttprintk("calloc %d %d %lx\n", nelems, n , result);
  return result;
}

void *maxve_virtualMemory_allocate(size_t size, int type);
void *valloc(size_t size) {
	return maxve_virtualMemory_allocate(size, DATA_VM);
}

extern void *allocate_heap_region(int n);
extern void *extend_allocate_heap_region(unsigned long vaddr, int n);
extern void *deallocate_heap_region(unsigned long vaddr, size_t size);
extern void *allocate_code_region(int n, unsigned long vaddr);

void *maxve_virtualMemory_allocate(size_t size, int type) {
   int pages = PAGE_ALIGN(size) / PAGE_SIZE;
   void * result = NULL;
   switch (type) {
   case STACK_VM:
   case CODE_VM:
     guk_printk("maxve_virtualMemory_allocate called with type = %d", type);
     crash_exit();
     break;
   case HEAP_VM:
     result = allocate_heap_region(pages);
     break;
   default:
     result = (void*) allocate_pages(pages, type);
   }
   return result;
}

void *maxve_virtualMemory_allocateIn31BitSpace(size_t size, int type) {
  return (void*) allocate_pages(size, type);
}

void *maxve_virtualMemory_allocateAtFixedAddress(unsigned long vaddr, size_t size, int type) {
  if (type == CODE_VM) {
       int pages = PAGE_ALIGN(size) / PAGE_SIZE;
       return (void*) allocate_code_region(pages, vaddr);
  } else {
       guk_printk("maxve_virtualMemory_allocateAtFixedAddress called with type = %d", type);
       crash_exit();
       return NULL;
  }
}

void maxve_virtualMemory_deallocate(void *start, size_t size, int type) {
  int pages = PAGE_ALIGN(size) / PAGE_SIZE;
     switch (type) {
     case STACK_VM:
     case CODE_VM:
       guk_printk("maxve_virtualMemory_deallocate called with type = %d", type);
       crash_exit();;
     case HEAP_VM:
       deallocate_heap_region((unsigned long) start, pages);
       break;
     case DATA_VM:
       deallocate_pages(start, pages, type);
       break;
     }


}

int maxve_virtualMemory_pageSize(void) {
  return PAGE_SIZE;
}

extern void check_stack_protectPages(unsigned long address, int count);
void maxve_virtualMemory_protectPages(unsigned long address, int count) {
  check_stack_protectPages(address, count);
}

extern void check_stack_unProtectPages(unsigned long address, int count);
void maxve_virtualMemory_unProtectPages(unsigned long address, int count) {
  check_stack_unProtectPages(address, count);
}

extern void* memory_allocate(size_t size);
extern void* memory_deallocate(void *p);
extern void* memory_reallocate(size_t size);

void *maxine_mm_dlsym(const char *symbol) {
    if (strcmp(symbol, "memory_allocate") == 0) return memory_allocate;
    else if (strcmp(symbol, "memory_deallocate") == 0) return memory_deallocate;
    else if (strcmp(symbol, "memory_reallocate") == 0) return memory_reallocate;
    else if (strcmp(symbol, "virtualMemory_deallocate") == 0) return virtualMemory_deallocate;
    else if (strcmp(symbol, "virtualMemory_allocate") == 0) return virtualMemory_allocate;
    else if (strcmp(symbol, "virtualMemory_allocateIn31BitSpace") == 0) return virtualMemory_allocateIn31BitSpace;
    else if (strcmp(symbol, "virtualMemory_allocateAtFixedAddress") == 0) return virtualMemory_allocateAtFixedAddress;
    else if (strcmp(symbol, "virtualMemory_pageAlign") == 0) return virtualMemory_pageAlign;
    else if (strcmp(symbol, "virtualMemory_protectPages") == 0) return virtualMemory_protectPages;
    else if (strcmp(symbol, "virtualMemory_unprotectPages") == 0) return virtualMemory_unprotectPages;
    else return 0;
}
