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
#include <os.h>
#include <hypervisor.h>
#include <types.h>
#include <list.h>
#include <sched.h>
#include <trace.h>
#include <lib.h>
#include <guestvmXen.h>
#include <mm.h>
#include <trace.h>
#include <spinlock.h>
#include <bitmap.h>

typedef unsigned long Address;
typedef unsigned long Size;

/* This is a copy from maxine/threadLocals.h.
 * We can't include that header directly because it pulls in inappropriate host-dependent
 * include files. That should be fixed.
 */

/*
 * Code to handle the allocation and mapping of Java thread stacks.
 * Stacks are high in the 64 bit virtual address space (2TB onwards)
 * and sparsely mapped to real (physical) memory as needed.
 * N.B. All stacks are of the same virtual size, although that size can vary
 * with each run of the JVM. We use a global bit map to indicate that a given
 * virtual address area is in use or not.
 *
 * Author: Mick Jordan
 */

/* This is a copy from maxine/threadLocals.h.
 * We can't include that header directly because it pulls in inappropriate host-dependent
 * include files. That should be fixed.
 */
typedef struct {
    jint id; //  0: denotes the primordial thread
             // >0: denotes a VmThread
    Address stackBase;
    Size stackSize;
    Address refMapArea;
    Address stackYellowZone; // unmapped to cause a trap on access
    Address stackRedZone;    // unmapped always - fatal exit if accessed

    /*
     * The blue zone is a page that is much closer to the base of the stack and is optionally protected.
     * This can be used, e.g., to determine the actual stack size needed by a thread, or to avoid
     * reserving actual real memory until it is needed.
     */
    Address stackBlueZone;

    /*
     * Place to hang miscellaneous OS dependent record keeping data.
     */
    void *osData;  //
} NativeThreadLocalsStruct, *NativeThreadLocals;

static unsigned long *alloc_bitmap;
static int max_threads;

static DEFINE_SPINLOCK(bitmap_lock);

#define STACK_INCREMENT_PAGES 8
#define STACK_INCREMENT_SIZE  (STACK_INCREMENT_PAGES * PAGE_SIZE)

#define THREAD_STACK_BASE (2L * 1024L *1024L * 1024L * 1024L)  // 2TB

extern void *malloc(size_t n);
extern void *calloc(int nelems, size_t n);
extern void free(void *p);

unsigned long thread_stack_base;
unsigned long thread_stack_size = 0;

unsigned long guestvmXen_stackPoolBase(void) {
  return thread_stack_base;
}

unsigned long guestvmXen_stackPoolSize(void) {
  return max_threads;
}

void *guestvmXen_stackPoolBitmap(void) {
  return alloc_bitmap;
}

unsigned long guestvmXen_stackRegionSize(void) {
  return thread_stack_size;
}

void init_thread_stacks(void) {
	// conservative estimate
  max_threads = (guk_maximum_reservation() * PAGE_SIZE) / STACK_INCREMENT_SIZE;
  int bitmap_size = round_pgup(map_size_in_bytes(max_threads));
  thread_stack_base = THREAD_STACK_BASE;
  alloc_bitmap = (unsigned long *) allocate_pages(bitmap_size / PAGE_SIZE, DATA_VM);
  memset(alloc_bitmap, 0, bitmap_size);
}


static unsigned long allocate_page(NativeThreadLocals nativeThreadLocals, unsigned long addr) {
	unsigned long pfn = virt_to_pfn(allocate_pages(1, STACK_VM));
	//guk_printk("stack_allocate_page %lx %lx\n", addr, pfn);
	return pfn;
}


static unsigned long pfn_alloc_thread_stack(pfn_alloc_env_t *env, unsigned long addr) {
	NativeThreadLocals nativeThreadLocals = (NativeThreadLocals) env->data;
	Address address = (Address) addr;
	int map = 0;
	/*
	 * This is called from build_pagetable in all phases of the stack setup.
	 * In the first phase, the initial stack allocation, the NativeThreadLocals struct is not filled in
	 * and we are just mapping the top of the stack (above blue zone) to get started.
	 * In the second phase we are mapping the areas at the bottom of the stack.
	 * In the third and subsequent calls we are extending the stack down from the blue zone.
	 * Identifying the phases:
	 *   Phase 1: nativeThreadLocals->stackBase == 0
	 *   Phase 2: nativeThreadLocals->stackBase != 0 && nativeThreadLocals->stackBlueZone == 0;
	 *   Phase 3: nativeThreadLocals->stackBase != 0 && nativeThreadLocals->stackBlueZone != 0;
	 */
	if (nativeThreadLocals->stackBase == 0) {
		map = 1;
	} else {
		if (nativeThreadLocals->stackBlueZone == 0) {
			/* The area between nativeThreadLocals->stackBase + PAGE_SIZE and nativeThreadLocals->stackRedZone
			 * and the page at nativeThreadLocals->stackYellowZone needs to be mapped.
			 */
			if (((address >= nativeThreadLocals->stackBase + PAGE_SIZE) && (address <  nativeThreadLocals->stackRedZone)) ||
					(address == nativeThreadLocals->stackYellowZone)) {
			  map = 1;
			}
   	    } else {
   	    	// lowering blue zone
   	    	map = 1;
   	    }
	}
	return map ? pfn_to_mfn(allocate_page(nativeThreadLocals, addr)) : 0;
}


void extend_stack(NativeThreadLocals nativeThreadLocals, unsigned long start_address, unsigned long end_address);

/* allocate the virtual memory (only) for a thread stack of size n pages */
unsigned long allocate_thread_stack(NativeThreadLocals nativeThreadLocals, int n) {
  unsigned long stackbase = 0;
  unsigned long stackend = 0;
  int slot;
  int vsize = n * PAGE_SIZE;
  spin_lock(&bitmap_lock);
  thread_stack_size = vsize;
  for (slot = 0; slot < max_threads; slot++) {
    if (!allocated_in_map(alloc_bitmap, slot)) {
      set_map(alloc_bitmap, slot);
      stackbase = thread_stack_base + slot * (vsize);
      stackend =  stackbase + (vsize);
      break;
    }
  }
  spin_unlock(&bitmap_lock);
  /*
   * We have to map the top of the stack because initStack does not get called
   * until the thread has actually started running.
   */
  extend_stack(nativeThreadLocals, stackend - STACK_INCREMENT_SIZE, stackend);
  return stackbase;
}

void guk_free_thread_stack(void *specifics, void *stack, unsigned long stack_size) {
	NativeThreadLocals nativeThreadLocals = (NativeThreadLocals) specifics;
	unsigned long stackBase = (unsigned long) stack;
	unsigned long stackEnd = stackBase + stack_size;
    while (stackBase < stackEnd) {
		unsigned long pte;
		long pfn = guk_not11_virt_to_pfn(stackBase, &pte);
		if (pfn > 0) {
			guk_clear_pte(stackBase);
			free_page(pfn_to_virt(pfn));
		}
    	stackBase += PAGE_SIZE;
    }
    int slot = ((unsigned long ) stack - thread_stack_base) / stack_size;
    spin_lock(&bitmap_lock);
    clear_map(alloc_bitmap, slot);
    spin_unlock(&bitmap_lock);
    free(nativeThreadLocals);
}

void extend_stack(NativeThreadLocals nativeThreadLocals, unsigned long start_address, unsigned long end_address) {
	  struct pfn_alloc_env pfn_frame_alloc_env = {
	    .pfn_alloc = pfn_alloc_alloc
	  };
	  struct pfn_alloc_env pfn_thread_stack_env = {
	    .pfn_alloc = pfn_alloc_thread_stack
	  };
	  pfn_thread_stack_env.data = nativeThreadLocals;
	  build_pagetable(start_address, end_address, &pfn_thread_stack_env, &pfn_frame_alloc_env);
}

int check_stack_protectPage(unsigned long addr);

void guestvmXen_initStack(NativeThreadLocals nativeThreadLocals) {
	/* At this point, only the top STACK_INCREMENT_SIZE  of the stack has been mapped.
	 *  There are some additional areas that need mapping.
	 */
    struct pfn_alloc_env pfn_frame_alloc_env = {
      .pfn_alloc = pfn_alloc_alloc
    };

     struct pfn_alloc_env pfn_thread_stack_env = {
      .pfn_alloc = pfn_alloc_thread_stack
    };

	pfn_thread_stack_env.data = nativeThreadLocals;
    build_pagetable(nativeThreadLocals->stackBase, nativeThreadLocals->stackBase + nativeThreadLocals->stackSize - STACK_INCREMENT_SIZE, &pfn_thread_stack_env, &pfn_frame_alloc_env);

    nativeThreadLocals->stackBlueZone = nativeThreadLocals->stackBase + nativeThreadLocals->stackSize - STACK_INCREMENT_SIZE;
    check_stack_protectPage(nativeThreadLocals->stackBlueZone);
    check_stack_protectPage(nativeThreadLocals->stackYellowZone);
}

unsigned long get_pfn_for_address(unsigned long address) {
	unsigned long pte;
	long pfn = guk_not11_virt_to_pfn(address, &pte);
	if (pfn < 0) {
		guk_xprintk("get_pfn_for_addr %lx, thread %d failed\n", address, current->id);
		crash_exit();
	}
	return pfn;
}

/*
 * Lowers the blue zone page by STACK_INCREMENT_SIZE but only if greater than
 * yellow zone page.
 */
void lower_blue_zone(NativeThreadLocals nativeThreadLocals) {
  Address nbz = nativeThreadLocals->stackBlueZone - STACK_INCREMENT_SIZE;
  unsigned long start_address;
  unsigned long end_address = nativeThreadLocals->stackBlueZone;
  if (nbz > nativeThreadLocals->stackYellowZone) {
    nativeThreadLocals->stackBlueZone = nbz;
	start_address = nbz;
  } else {
    nativeThreadLocals->stackBlueZone = nativeThreadLocals->stackYellowZone;
    start_address = nativeThreadLocals->stackYellowZone + PAGE_SIZE;
  }
  /* Need to allocate and map new pages */
  //guk_printk(" nbz %lx\n", start_address);
  if (end_address > start_address) {
    extend_stack(nativeThreadLocals, start_address, end_address);
    /* There must be at least two mapped pages above the yellow zone for the stack check code to work.
     * If we are in the last increment no point in unmapping the blue zone page. */
    if (start_address  > nativeThreadLocals->stackYellowZone + STACK_INCREMENT_SIZE) {
    	guk_unmap_page_pfn(start_address, get_pfn_for_address(start_address));
    }
  }
}

void guestvmXen_blue_zone_trap(NativeThreadLocals nativeThreadLocals) {
  //guk_printk("blue zone trap bz %lx, yz %lx", nativeThreadLocals->stackBlueZone, nativeThreadLocals->stackYellowZone);
  guk_remap_page_pfn(nativeThreadLocals->stackBlueZone, get_pfn_for_address(nativeThreadLocals->stackBlueZone));
  lower_blue_zone(nativeThreadLocals);
}

int check_stack_protectPage(unsigned long address) {
	if (address > thread_stack_base) {
		return guk_unmap_page_pfn(address, get_pfn_for_address(address));;
	} else {
		return guk_unmap_page(address);
	}
}

int check_stack_unProtectPage(unsigned long address) {
	if (address > thread_stack_base) {
		return guk_remap_page_pfn(address, get_pfn_for_address(address));
	} else {
		return guk_remap_page(address);
	}

}

void *thread_stack_pool_dlsym(const char * symbol) {
  if (strcmp(symbol, "guestvmXen_stackPoolBase") == 0) return guestvmXen_stackPoolBase;
  else if (strcmp(symbol, "guestvmXen_stackPoolSize") == 0) return guestvmXen_stackPoolSize;
  else if (strcmp(symbol, "guestvmXen_stackPoolBitmap") == 0) return guestvmXen_stackPoolBitmap;
  else if (strcmp(symbol, "guestvmXen_stackRegionSize") == 0) return guestvmXen_stackRegionSize;
  else return 0;
}
