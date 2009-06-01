/*
 * Copyright (c)  2009 Sun Microsystems, Inc., 4150 Network Circle, Santa
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
// from Maxine
#include <image.h>

/*
 * Code to handle the allocation of Maxine code regions.
 * Code regions are high in the virtual address space, above the
 * thread stack area, starting at 3TB. We use a global bit map to indicate
 * that a given virtual address area is in use or not.
 *
 * N.B. Currently code regions are at 2GB not 3TB because they must be
 * with 2GB of the boot code region.
 *
 * Author: Mick Jordan, Sun Microsystems Inc.
 */

static unsigned long *alloc_bitmap;
static int max_code_regions;

#define MAX_CODE_REGIONS 64

static DEFINE_SPINLOCK(bitmap_lock);

#define BOOT_CODE_REGION_BASE  (3L * 1024L *1024L * 1024L * 1024L)  // 3TB

#define CODE_REGIONS_BASE  (2L * 1024L *1024L * 1024L)  // 2GB

static unsigned long code_region_base;
static unsigned long _code_region_size = 0;

unsigned long guestvmXen_codePoolBase(void) {
  return code_region_base;
}

unsigned long guestvmXen_codePoolSize(void) {
  return max_code_regions;
}

void *guestvmXen_codePoolBitmap(void) {
  return alloc_bitmap;
}

unsigned long guestvmXen_codePoolRegionSize(void) {
	return _code_region_size;
}

unsigned long guestvmXen_remap_boot_code_region(unsigned long base, size_t size) {
	  struct pfn_alloc_env pfn_frame_alloc_env = {
	    .pfn_alloc = pfn_alloc_alloc
	  };
	  struct pfn_alloc_env pfn_boot_code_region_env = {
	    .pfn_alloc = pfn_linear_alloc
	  };
	  // The boot code is mapped 1-1 with physical memory where it was placed by the linker
	  // So we map the same physical page frames at our preferred address.
	  pfn_boot_code_region_env.pfn = virt_to_pfn(base);
	  build_pagetable(BOOT_CODE_REGION_BASE,  BOOT_CODE_REGION_BASE + size, &pfn_boot_code_region_env, &pfn_frame_alloc_env);
	  return BOOT_CODE_REGION_BASE;
}

static void init_code_regions(int region_size) {
	_code_region_size = region_size;
	max_code_regions = MAX_CODE_REGIONS;
    int bitmap_size = round_pgup(map_size_in_bytes(max_code_regions)); // in bytes
    code_region_base = image_code_end();
    alloc_bitmap = (unsigned long *) allocate_pages(bitmap_size / PAGE_SIZE, DATA_VM);
    memset(alloc_bitmap, 0, bitmap_size);
}

/* allocate a code region of size n pages at vaddr.
 * All code regions are of the same size. */
unsigned long allocate_code_region(int n, unsigned long vaddr) {
	  int slot;
	  int vsize = n * PAGE_SIZE;
	  if (code_region_base == 0) {
		  init_code_regions(vsize);
	  }
	  //guk_printk("allocate_code_region %lx %lx\n", vaddr, n);
	  slot = (vaddr - code_region_base) / _code_region_size;
	  spin_lock(&bitmap_lock);
	  if (allocated_in_map(alloc_bitmap, slot)) {
		  guk_printk("code region slot at %lx is already allocated\n", vaddr);
		  crash_exit();
	  } else {
	      set_map(alloc_bitmap, slot);
	  }
	  spin_unlock(&bitmap_lock);
	  struct pfn_alloc_env pfn_frame_alloc_env = {
	    .pfn_alloc = pfn_alloc_alloc
	  };
	  struct pfn_alloc_env pfn_code_region_env = {
	    .pfn_alloc = pfn_linear_alloc
	  };
	  // allocate n consecutive physical pages
	  unsigned long pfn = virt_to_pfn(allocate_pages(n, CODE_VM));
	  pfn_code_region_env.pfn = pfn;
	  build_pagetable(vaddr, vaddr + vsize, &pfn_code_region_env, &pfn_frame_alloc_env);
	  //guk_printk("allocate_code_region: %lx, %lx\n", vaddr, pfn);
	  return vaddr;
}

void *code_pool_dlsym(const char * symbol) {
  if (strcmp(symbol, "guestvmXen_codePoolBase") == 0) return guestvmXen_codePoolBase;
  else if (strcmp(symbol, "guestvmXen_codePoolSize") == 0) return guestvmXen_codePoolSize;
  else if (strcmp(symbol, "guestvmXen_codePoolBitmap") == 0) return guestvmXen_codePoolBitmap;
  else if (strcmp(symbol, "guestvmXen_codePoolRegionSize") == 0) return guestvmXen_codePoolRegionSize;
  else return 0;
}
