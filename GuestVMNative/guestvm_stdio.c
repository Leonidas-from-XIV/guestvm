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
 * Standard I/O functions needed by Maxine, mostly by log.c and image.c
 */

#include <os.h>
#include <hypervisor.h>
#include <types.h>
#include <spinlock.h>
#include <lib.h>
#include <mm.h>
#include <time.h>
#include <fs.h>
#include <jni.h>
#include <xmalloc.h>

extern void print(int direct, const char *fmt, va_list args);  // console.c

/*
 * Implicitly accessed in log_* on Solaris via stdout/stderr
 */

#ifdef solaris
FILE __iob[_NFILE];
#endif

#ifdef linux
FILE *stdout;
FILE *stderr;
#endif

/*
 * Used in image.c
 */
int fprintf(FILE *file, const char *format, ...) {
    va_list       args;
    va_start(args, format);
    guk_cprintk(0, format, args);
    va_end(args);
    return 0;
}

/*
 * Used in log_*
 */
int printf(const char *format, ...) {
    va_list       args;
    va_start(args, format);
    guk_cprintk(0, format, args);
    va_end(args);
    return 0;
}


/*
 * Used in log_println
 */
int vprintf(const char *format, va_list args) {
    guk_cprintk(0, format, args);
    return 0;
}

/*
 * Used in log_exit
 */
int vfprintf(FILE *file, const char *format, va_list args) {
	guk_cprintk(0, format, args);
	return 0;
}

/*
 * Used in log_*
 */
int fflush(FILE *file) {
  return 0;
}


/*
 * Used by Maxine log_* when compiled under Solaris
 */
#ifdef solaris
int putc(int a1, FILE *a2) {
  char buf[2];
  buf[0] = a1; buf[1] = 0;
  guk_printk("%s", buf);
  return 0;
}
#endif

/*
 * Used by Maxine log_* when compiled under Linux
 */
#ifdef linux
int _IO_putc(int a1, FILE *a2) {
	  char buf[2];
	  buf[0] = a1; buf[1] = 0;
	  guk_printk("%s", buf);
	  return 0;
}
#endif

/*
 * A mystery wqhy we need this. Some of the zlib object files
 * have this as an undefined symbol. However, neither gcc -E nor objdump
 * provide any indication as to where the reference actually is.
 */
#ifdef linux
size_t fwrite(const void *ptr, size_t size, size_t nmemb, FILE *stream) {
    guk_crash_exit_msg("fwrite called!");
    return 0;
}
#endif
/*
 * Used in maxine, aka maxine main method
 */
void close(int fd) {
}

/*
 * Used in log_lock/unlock
 */
char * strerror(int errnum) {
	return "strerror not implemented";
}

/*
 * used in memory.c
 */
void perror(const char *msg) {
	guk_printk("%s\n", msg);
}
