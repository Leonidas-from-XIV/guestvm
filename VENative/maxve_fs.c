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
 * Shim to the GUK console I/O and sibling guest file system I/O.
 *
 * Author: Mick Jordan, Sun Microsystems Inc.
 */

#include <os.h>
#include <hypervisor.h>
#include <types.h>
#include <lib.h>
#include <fs.h>
#include <jni.h>

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeWriteBytes(JNIEnv *env, jclass c, char *data, int length) {
    guk_printbytes(data, length);
    return length;
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeWrite(JNIEnv *env, jclass c, int b) {
    char buf[1];
    buf[0] = b;
    guk_printbytes(buf, 1);
    return 1;
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeReadBytes(JNIEnv *env, jclass c, char *data, int length) {
    return guk_console_readbytes(data, length);
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeRead(JNIEnv *env, jclass c) {
    char buf[1];
    int n = guk_console_readbytes(buf, 1);
    return n == 1 ? buf[0] : 0;
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_open(JNIEnv *env, jclass c,
							 struct fs_import *import, char *path, int flags) {
  int fd = guk_fs_open(import, path, flags);
  return fd;
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_close0(JNIEnv *env, jclass c,
							   struct fs_import *import, int fd) {
  return guk_fs_close(import, fd);
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_writeBytes(JNIEnv *env, jclass c,
							       struct fs_import *import, int fd,
							       char *data, int length, jlong offset) {
  return guk_fs_write(import, fd, data, length, offset);
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_write(JNIEnv *env, jclass c,
							  struct fs_import *import,  int fd, int b, jlong offset) {
  char buf[1];
  buf[0] = b;
  return guk_fs_write(import, fd, &buf, 1, offset);
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_read(JNIEnv *env, jclass c,
							 struct fs_import *import, int fd, jlong offset) {
  char buf[1];
  size_t n = guk_fs_read(import, fd, &buf, 1, offset);
  if (n <= 0) return n;
  else return buf[0];
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_readBytes(JNIEnv *env, jclass c,
							      struct fs_import *import, int fd,
							      char *buf, jint length, jlong offset) {
  size_t n = guk_fs_read(import, fd, buf, length, offset);
  return n;
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getNumImports(JNIEnv *env, jclass c) {
  struct list_head *imports = guk_fs_get_imports();
  if (imports == NULL) return 0;
  struct list_head *entry;
  int result = 0;
  list_for_each(entry, imports) {
    result++;
  }
  return result;
}

JNIEXPORT void* JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getImport(JNIEnv *env, jclass c, jint index) {
  struct list_head *imports = guk_fs_get_imports();
  struct list_head *entry;
  struct fs_import *import;
  int i = 0;
  list_for_each(entry, imports) {
    import = list_entry(entry, struct fs_import, list);
    if (i == index) return import;
    i++;
  }
  return NULL;
}

JNIEXPORT void* JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getPath(JNIEnv *env, jclass c,
							    struct fs_import *import) {
  return import->path;
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getLength(JNIEnv *env, jclass c,
							      struct fs_import *import, char *path) {
  struct fsif_stat stat;
  int rc = guk_fs_stat(import, path, &stat);
  if (rc < 0) return rc;
  else return stat.st_size;
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getMode(JNIEnv *env, jclass c,
							    struct fs_import *import, char *path) {
  struct fsif_stat stat;
  int rc = guk_fs_stat(import, path, &stat);
  if (rc < 0) return rc;
  else return stat.st_mode;
}

JNIEXPORT void * JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_list(JNIEnv *env, jclass c,
							 struct fs_import *import, char *path,
							 jint offset, jarray nFilesA, jarray hasMoreA) {
  jint nFiles;
  int hasMore;
  char ** files = guk_fs_list(import, path, offset, &nFiles, &hasMore);
  if (files) {
    (*env)->SetBooleanArrayRegion(env, hasMoreA, 0, 1, (jboolean*)&hasMore);
    (*env)->SetIntArrayRegion(env, nFilesA, 0, 1, &nFiles);
  }
  return files;
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_delete(JNIEnv *env, jclass c,
							   struct fs_import *import, char *path) {
  return guk_fs_remove(import, path);
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_rename(JNIEnv *env, jclass c,
							   struct fs_import *import,
							   char *path1, char *path2) {
  return guk_fs_rename(import, path1, path2);
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_createDirectory(JNIEnv *env, jclass c,
								    struct fs_import *import, char *path) {
  return guk_fs_create(import, path, 1, 0777);
}

JNIEXPORT int JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_createFileExclusively(JNIEnv *env, jclass c,
									  struct fs_import *import,
									  char *path) {
  return guk_fs_create(import, path, 0, 0666);
}

JNIEXPORT long JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getLastModifiedTime(JNIEnv *env, jclass c,
									struct fs_import *import, char *path) {
  struct fsif_stat stat;
  guk_fs_stat(import, path, &stat);
  return stat.st_mtim;
}

JNIEXPORT long JNICALL
Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getLengthFd(JNIEnv *env, jclass c,
								struct fs_import *import, int fd) {
  struct fsif_stat stat;
  int rc = guk_fs_fstat(import, fd, &stat);
  if (rc < 0) return rc;
  else return stat.st_size;
}

void *fs_dlsym(const char *symbol) {
    if (strcmp(symbol, "Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeWriteBytes") == 0)
      return Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeWriteBytes;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeWrite") == 0)
      return Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeWrite;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeReadBytes") == 0)
      return Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeReadBytes;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeRead") == 0)
      return Java_com_sun_max_ve_fs_console_ConsoleFileSystem_nativeRead;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_writeBytes") == 0)
      return Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_writeBytes;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_open") == 0)
      return Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_open;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_write") == 0)
      return Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_write;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_close0") == 0)
        return Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_close0;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_read") == 0)
        return Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_read;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_readBytes") == 0)
        return Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_readBytes;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getNumImports") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getNumImports;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getImport") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getImport;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getPath") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getPath;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getLength") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getLength;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getLengthFd") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getLengthFd;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getMode") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getMode;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_list") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_list;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_delete") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_delete;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_rename") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_rename;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getLastModifiedTime") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_getLastModifiedTime;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_createDirectory") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_createDirectory;
    else if (strcmp(symbol, "Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_createFileExclusively") == 0)
        return  Java_com_sun_max_ve_fs_sg_SiblingFileSystemNatives_createFileExclusively;
    else
        return 0;
}
