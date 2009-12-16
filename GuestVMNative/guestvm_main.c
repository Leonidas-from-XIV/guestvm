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
#include <arch_sched.h>
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
char * environ[1];     // environment variables - we dont have any but maxine wants this symbol
char vmpath[1];        // path to "executable" - no meaning in this context but needed by VM

char * getenv(const char *name) {
	return NULL;
}

static char *process_arg(char *arg, int len) {
//	char *result = check_varocc(arg, len);
//	check_vardef(result);
	char *result = malloc(len);
	strcpy(result, arg);
	return result;
}

 static int create_argv(char *cmd_line, int argc) {
  int cmdx = 0;
  if (cmd_line == NULL) {
    return 0;
  } else {
    while (cmd_line[cmdx] != 0) {
      /* process an argument */
      char argvc[1024];
      int vcx = 0;
      int vcxs = vcx;
      /* skip spaces */
      while (cmd_line[cmdx] == ' ' && cmd_line[cmdx] != 0) cmdx++;
      while (cmd_line[cmdx] != ' ' && cmd_line[cmdx] != 0) {
	      argvc[vcx++] = cmd_line[cmdx++];
      }
      if (vcx > vcxs) {
    	argvc[vcx++] = 0;
    	argv[argc++] = process_arg(argvc, vcx - vcxs);
      } else break;
    }
  }
  return argc;
}

// If the args exceed the 1024 Xen limit they will have been placed in a file and passed as a ramdisk.
#define RAMARGS "-XX:GVMRamArgs"

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
  argc = create_argv(aargs->cmd_line, 1);
  // check for more args in the ram disk
  if (strstr(aargs->cmd_line, RAMARGS) != NULL) {
	  char *p = (char *)aargs->si_info->mod_start;
	  if (p == NULL) {
		  printk("-XX:GVMRamArgs set, but no ramdisk in configuration!\n");
		  ok_exit();
	  }
	  int  count = 0;
	  char ch;
	  while ((ch = *p++) != '\n') {
		  count = count * 10 + (ch - '0');
	  }
	  p[count] = 0; // ensure null terminated
	  argc = create_argv(p, argc);
  }

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

/* Code to support the use of variables in arguments that
 * are defined using -XX:GVMVar:name=value, and expand
 * occurrences here. This was one possible solution to
 * the Xen 1024 command line limit now superceded by the
 * ramdisk mechanism.
 *

struct vardef {
	char *name;
	char *value;
	struct list_head vardef_list;
};

static LIST_HEAD(vardef_list);

#define VARDEF "-XX:GVMArgVar"
#define VAROCC "${"
#define VARTERM "}"

static int check_vardef(char *arg) {
	if (strstr(arg, VARDEF) == arg) {
		int len = strlen(arg);
		char * sep = arg + strlen(VARDEF);
		char * val = strstr(arg, "=");
		if (sep[0] == ':' && val && val > sep  + 1) {
			char * name = malloc(val - sep);
			char *value = malloc(arg + len - val);
			int i;
			for (i = 0; i < val - sep - 1; i++) name[i] = sep[i + 1];
			name[i] = 0;
			for (i = 0; i < arg + len - val; i++) value[i] = val[i + 1];
			value[i] = 0;
			struct vardef *vardef = xmalloc(struct vardef);
			vardef->name = name;
			vardef->value = value;
			list_add_tail(&vardef->vardef_list, &vardef_list);
			printk("adding variable %s:%s\n", name, value);
			return 1;
		} else {
			printk("bad syntax for argvar %s, should be %s:name=value\n", arg, VARDEF);
			ok_exit();
		}
    }
	return 0;
}

static char *check_varocc(char *arg, int len) {
	char *result;
	char  *argsrc = arg;
	char *p = arg;
	char expanded[1024];
	char *e = &expanded[0];
	char *v, *t  = NULL;
	while ((v = strstr(p, VAROCC)) != NULL) {
		t = strstr(v, VARTERM);
		char *var;
		struct vardef *vardef;
		struct list_head *list_head;
		char *varval = NULL;
		int i;
		if (!t || t <= v + 2) {
			printk("bad argvar occurrence\n");
			ok_exit();
		}
		var = malloc(t - (v + 1));
		for (i = 0; i < t - (v + 2); i++) {
			var[i] = v[i + 2];
		}
		var[i] = 0;
		list_for_each(list_head, &vardef_list) {
			vardef = list_entry(list_head, struct vardef, vardef_list);
			if (strcmp(var, vardef->name) == 0) {
				varval = vardef->value;
				break;
			}
		}
		if (varval == NULL) {
			printk("no definition found for arg variable %s\n", var);
			ok_exit();
		}
		// copy up to var
		xprintk("copying %d to %lx\n", v-p, e);
		strncpy(e, p, v - p);
		e += v - p;
		strcpy(e, varval);
		p = t + 1; e += strlen(varval);
	}
	if (t != NULL) {
		// append anything remaining after last argvar
		xprintk("appending '%s' at %lx\n", t + 1, e);
		strcpy(e, t + 1);
		len = strlen(expanded) + 1;
		argsrc = &expanded[0];
	}
	result = malloc(len);
	strcpy(result, argsrc);
	return result;
}
*/


