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
package com.sun.guestvm.jdk;

import java.io.*;

import com.sun.max.annotate.*;
import com.sun.max.vm.actor.holder.*;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.classfile.constant.*;

/**
 * Substitutions for @see java.io.FileDescriptor.
 * @author Mick Jordan
 *
 */
@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(FileDescriptor.class)
public class JDK_java_io_FileDescriptor {

    @SUBSTITUTE
    private static void initIDs() {
    }

    /**
     * Return a @see ReferenceFieldActor for a @see FileDescriptor field named "fd" in the given class.
     * @param klass
     * @return the @see ReferenceFieldActor
     */
    public static ReferenceFieldActor fileDescriptorFieldActor(Class<?> klass) {
        return (ReferenceFieldActor) ClassActor.fromJava(klass).findFieldActor(SymbolTable.makeSymbol("fd"));
    }

    /**
     * Return an @see IntFieldActor for the "fd" field in the @see FileDescriptor class.
     * @return the @see IntFieldActor
     */
    public static IntFieldActor fdFieldActor() {
        //_fdFieldActor = (IntFieldActor) FieldActor.fromJava(Classes.getDeclaredField(getClass(), "fd", int.class));
        return (IntFieldActor) ClassActor.fromJava(FileDescriptor.class).findFieldActor(SymbolTable.makeSymbol("fd"));
    }


}
