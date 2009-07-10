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
#include <sched.h>
#include <spinlock.h>
#include <xenbus.h>
#include <db.h>
#include <dbif.h>
#include <xmalloc.h>
#include <trace.h>
#include <db.h>
#include <guestvmXen.h>

extern int main(int argc, char *argv[]);
extern void init_malloc(void);
extern void init_thread_stacks(void);
extern void init_code_regions(void);

static char* argv[64]; // place to store canonical command line **argv
static char argvc[1024];
char * environ[1];     // environment variables - we dont have any but maxine wants this symbol
char vmpath[1];        // path to "executable" - no meaning in this context but needed by VM

// TODO needs rewriting to cope with arbitrary length command lines and number of arguments
// using malloc and not static arrays. Not important at this time because Xen cannot accept
// a command line greater than 1024.
static int create_argv(char *cmd_line) {
  int cmdx = 0;
  int vcx = 0;
  int argc = 1;
  int vcxs = 0;
  if (cmd_line == NULL) {
    return 0;
  } else {
    while (cmd_line[cmdx] != 0) {
      /* process an argument */
      vcxs = vcx;
      /* skip spaces */
      while (cmd_line[cmdx] == ' ' && cmd_line[cmdx] != 0) cmdx++;
      while (cmd_line[cmdx] != ' ' && cmd_line[cmdx] != 0) {
	      argvc[vcx++] = cmd_line[cmdx++];
      }
      if (vcx > vcxs) {
	    argvc[vcx++] = 0;  /*terminate*/
	    argv[argc++] = &argvc[vcxs];
      } else break;
    }
  }
  return argc;
}

static void maxine_start(void *p) {
  struct app_main_args *aargs = (struct app_main_args *)p;
  int argc = 0;
  char *msg;
  char *name;

  msg = xenbus_read(XBT_NIL, "name", &name);
  init_malloc();
  environ[0] = NULL;
  vmpath[0] = '\0';
  argv[0] = name;
  argc = create_argv(aargs->cmd_line);
  /* Block if we run in the debug mode, let the debugger resume us */
  if (guk_debugging())
  {
      preempt_disable();
      set_debug_suspend(current);
      block(current);
      preempt_enable();
      schedule();
  } else {
	  /* This seems to avoid a startup bug involving xm and the console */
	  guk_sleep(500);
  }
  init_thread_stacks();
  maxine(argc, argv, NULL);
  free(aargs);
  ok_exit();
}


int guk_app_main(struct app_main_args *args) {
    struct app_main_args *aargs;

    aargs = xmalloc(struct app_main_args);
    memcpy(aargs, args, sizeof(struct app_main_args));

    /* The primordial Maxine thread needs a larger stack that a typical ukernel thread
     * because it executes quite a lot of Java code during initialization.
     */
    void *stack = (void *)allocate_pages(16, STACK_VM);
    guk_create_thread_with_stack("maxine", maxine_start, UKERNEL_FLAG, stack, (PAGE_SIZE * 16), aargs);
    return 0;
}

void guestvmXen_native_props(native_props_t *native_props) {
	native_props->user_name = "guestvm";
	native_props->user_home = "/tmp";
	native_props->user_dir = "/tmp";
}

extern int image_load(char *file);
extern unsigned long image_heap(void);

void guk_dispatch_app_specific1_request(
        struct dbif_request *req, struct dbif_response *rsp)
{
    image_load(NULL);
    rsp->ret_val = image_heap();
}

void guestvmXen_register_fault_handler(int fault, fault_handler_t fault_handler) {
  guk_register_fault_handler(fault, fault_handler);
}
