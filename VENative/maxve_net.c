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
 * Network shim.
 * Author: Mick Jordan, Sun Microsystems Inc.
 */

#include <os.h>
#include <sched.h>
#include <hypervisor.h>
#include <types.h>
#include <spinlock.h>
#include <lib.h>
#include <mm.h>
#include <time.h>
#include <jni.h>
#include <xmalloc.h>

static int mac_address_set = 0;
static unsigned char mac_address[6];
static unsigned char nic_name[32];
static int net_available = 0;
static int net_started = 0;

typedef void (*GUKNetDeviceCopyPacketMethod)(void *p, int len, long ts);
static GUKNetDeviceCopyPacketMethod copy_packet_method;

/*
 * This is the Guest VM override (strong) definition of the GUK netfront function
 * that is called after network initialization. A NULL value for mac implies
 * that there is no network available.
 */
void guk_net_app_main(unsigned char *mac, char *nic) {
    if (mac != NULL) {
        memcpy(mac_address, mac, 6);
        net_available = 1;
    }
    if (nic != NULL) {
        int i = 0;
        while (nic[i] != 0 && i < 31) {
	      nic_name[i] = nic[i];
          i++;
        }
        if (i == 31) nic_name[i] = 0;
    }
    mac_address_set = 1;
}
/*
 * This is the Guest VM override (strong) definition of the GUK netfront function
 * that is called whenever a packet is taken off the ring.
 * Note that packets may arrive before the Guest VM driver is set up - they are ignored.
 * We mark the interrupted thread as needing rescheduling to get the packet
 * handling thread running with minimal latency (the scheduler may override this).
 */
void guk_netif_rx(unsigned char* data, int len) {
  if (net_started) {
	  struct thread *current;
    (*copy_packet_method)(data, len, NOW());
    current = guk_not_idle_or_stepped();
    if (current != NULL) {
    	set_need_resched(current);
    }
  }
}

unsigned char *maxve_getMacAddress(void) {
  while (mac_address_set == 0) {
    sleep(1000);
  }
  return mac_address;
}

unsigned char *maxve_getNicName(void) {
  while (mac_address_set == 0) {
    sleep(1000);
  }
  return nic_name;
}

int maxve_netStart(GUKNetDeviceCopyPacketMethod m) {
  while (mac_address_set == 0) {
    sleep(1000);
  }
  if (!net_available) return 0;
  net_started = 1;
  copy_packet_method = m;
  return 1;
}

extern void guk_netfront_xmit(unsigned char *, int length);  // netfront

void *net_dlsym(const char *symbol) {
    if (strcmp(symbol, "maxve_getMacAddress") == 0) return maxve_getMacAddress;
    else if (strcmp(symbol, "maxve_getNicName") == 0) return maxve_getNicName;
    else if (strcmp(symbol, "maxve_netStart") == 0) return maxve_netStart;
    else return 0;
}
