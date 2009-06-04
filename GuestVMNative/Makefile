#
# Copyright (c) 2009 Sun Microsystems, Inc., 4150 Network Circle, Santa
# Clara, California 95054, U.S.A. All rights reserved.
# 
# U.S. Government Rights - Commercial software. Government users are
# subject to the Sun Microsystems, Inc. standard license agreement and
# applicable provisions of the FAR and its supplements.
# 
# Use is subject to license terms.
# 
# This distribution may include materials developed by third parties.
# 
# Parts of the product may be derived from Berkeley BSD systems,
# licensed from the University of California. UNIX is a registered
# trademark in the U.S.  and in other countries, exclusively licensed
# through X/Open Company, Ltd.
# 
# Sun, Sun Microsystems, the Sun logo and Java are trademarks or
# registered trademarks of Sun Microsystems, Inc. in the U.S. and other
# countries.
# 
# This product is covered and controlled by U.S. Export Control laws and
# may be subject to the export or import laws in other
# countries. Nuclear, missile, chemical biological weapons or nuclear
# maritime end uses or end users, whether direct or indirect, are
# strictly prohibited. Export or reexport to countries subject to
# U.S. embargo or to entities identified on U.S. export exclusion lists,
# including, but not limited to, the denied persons and specially
# designated nationals lists is strictly prohibited.
# 
#
# Makefile for Guest VM guest
debug ?= y

ifndef XEN_ROOT 
  $(error "Must set XEN_ROOT environment variable to the root of Xen tree")
endif

ifndef JAVA_HOME 
  $(error "Must set JAVA_HOME environment variable to your JDK home directory")
endif

include $(XEN_ROOT)/Config.mk
OSNAME ?= $(shell uname -s)
ifeq ($(OSNAME),Linux)
  HOSTOS_LC = linux
else
ifeq ($(OSNAME),SunOS)
  HOSTOS_LC = solaris
else
  $(error "Unknown operating system")
endif
endif

# Set TARGET_ARCH
# override TARGET_ARCH     := $(XEN_TARGET_ARCH)
TARGET_ARCH = x86_64

XEN_INTERFACE_VERSION := 0x00030203

PROJECT_ROOT = ../..
GUK_ROOT = $(PROJECT_ROOT)/guk
MAXINE_NATIVE = $(PROJECT_ROOT)/maxine/Native
MAXINE_SUBSTRATE = $(MAXINE_NATIVE)/generated/guestvm
MAXINE_JNI = $(MAXINE_NATIVE)/jni
MAXINE_SHARE = $(MAXINE_NATIVE)/share
MAXINE_INCL = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/$(HOSTOS_LC) -I$(MAXINE_NATIVE)/share -I$(MAXINE_NATIVE)/platform -I$(MAXINE_NATIVE)/substrate
LIB_MAXINE = libjvm.a

FS_DIR = $(GUK_ROOT)/tools/fs-back
DB_DIR = $(GUK_ROOT)/tools/db-front

# NB. '-Wcast-qual' is nasty, so I omitted it.
CFLAGS := -fno-builtin -Wall -Werror -Wredundant-decls -Wno-format
CFLAGS += -Wstrict-prototypes -Wnested-externs -Wpointer-arith -Winline
CFLAGS += -D__XEN_INTERFACE_VERSION__=$(XEN_INTERFACE_VERSION)

ASFLAGS = -D__ASSEMBLY__

LDLIBS :=
LDS := guestvm_$(TARGET_ARCH).lds
LDFLAGS_FINAL := -N -T $(LDS)

# For possible special source directories.
EXTRA_SRC =
# For possible special header directories.
EXTRA_INC =

# Standard name for architecture specific subdirectories.
TARGET_ARCH_DIR = $(TARGET_ARCH)
# This is used for architecture specific links.
ARCH_LINKS =

ifeq ($(TARGET_ARCH),x86_64)
  ifeq ($(OSNAME),Linux)
  CFLAGS += -fno-stack-protector 
  endif
CFLAGS += -m64 -mno-red-zone -fpic -fno-reorder-blocks
CFLAGS += -fno-asynchronous-unwind-tables
LDFLAGS += -m elf_x86_64
TARGET_ARCH_DIR = x86
EXTRA_INC += $(TARGET_ARCH_DIR)/$(TARGET_ARCH)
EXTRA_SRC += arch/$(EXTRA_INC)
endif

ifeq ($(debug),y)
CFLAGS += -g
else
CFLAGS += -O3
endif

# Add the special header directories to the include paths.
extra_incl := $(foreach dir,$(EXTRA_INC),-I$(GUK_ROOT)/include/$(dir))
override CPPFLAGS := -I. -I$(FS_DIR) -I$(DB_DIR) -I$(GUK_ROOT)/include $(CPPFLAGS) -I$(GUK_ROOT)/include/$(TARGET_ARCH_DIR) $(extra_incl) $(MAXINE_INCL)

CFLAGS += -DCONFIG_SMP -DCONFIG_PREEMPT -D$(HOSTOS_LC)
TARGET := guestvm
ifdef TARGET_NAME
  TARGET:=$(TARGET)-$(TARGET_NAME)
endif

UKERNEL_OBJ := $(GUK_ROOT)/guk.o
OBJS := $(patsubst %.c,objs/%.o,$(wildcard *.c))

LDLIBS += ${MAXINE_SUBSTRATE}/${LIB_MAXINE}

HDSR := $(wildcard *.h)
HDRS += $(wildcard $(GUK_ROOT)/include/*.h)
HDRS += $(wildcard $(GUK_ROOT)/include/xen/*.h)
HDRS += $(wildcard $(GUK_ROOT)/include/$(TARGET_ARCH_DIR)/*.h)
# For special wanted header directories.
extra_heads := $(foreach dir,$(EXTRA_INC),$(wildcard $(dir)/*.h))
HDRS += $(extra_heads)

.PHONY: default
default: $(TARGET)

$(TARGET):  $(OBJS) $(UKERNEL_OBJ) ${MAXINE_SUBSTRATE}/${LIB_MAXINE} $(LDS)
	$(LD) $(LDFLAGS_FINAL) $(UKERNEL_OBJ) $(OBJS) $(LDLIBS) -o $@
#	gzip -f -9 -c $@ >$@.gz

.PHONY: clean
clean:
	find . -type f -name '*.o' | xargs rm -f
	rm -f *.o *~ core $(TARGET).elf $(TARGET).raw $(TARGET) $(TARGET).gz
	find . -type l | xargs rm -f
	rm -f tags TAGS

objs/%.o: %.c $(HDRS) Makefile
	mkdir -p objs
	$(CC) $(CFLAGS) $(CPPFLAGS) -c $< -o $@

%.E: %.c $(HDRS)
	$(CC) $(CFLAGS) $(CPPFLAGS) -E $< -o $@

define all_sources
     ( find . -follow -name SCCS -prune -o -name '*.[chS]' -print )
endef

.PHONY: cscope
cscope:
	$(all_sources) > cscope.files
	cscope -k -b -q

.PHONY: tags
tags:
	$(all_sources) | xargs ctags
