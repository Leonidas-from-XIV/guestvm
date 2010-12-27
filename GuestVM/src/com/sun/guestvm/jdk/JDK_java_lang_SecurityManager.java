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

import com.sun.max.annotate.*;
import com.sun.max.vm.actor.holder.*;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.classfile.constant.*;
import com.sun.max.vm.jni.*;
import com.sun.guestvm.error.*;

/**
 * Implementation of native methods for java.lang.SecirityManager.
 *
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(SecurityManager.class)
public class JDK_java_lang_SecurityManager {

    @CONSTANT_WHEN_NOT_ZERO
    private static FieldActor _initFieldActor;

    private static boolean check(Object self) {
        if (_initFieldActor == null) {
            _initFieldActor = (FieldActor) ClassActor.fromJava(SecurityManager.class).findFieldActor(SymbolTable.makeSymbol("initialized"), null);
        }
        final boolean initialized = _initFieldActor.getBoolean(self);
        if (!initialized) {
            throw new SecurityException("security manager not initialized.");
        }
        return true;
    }

    @SUBSTITUTE
    private Class[] getClassContext() {
        check(this);
        return JVMFunctions.GetClassContext();
    }

    @SUBSTITUTE
    private ClassLoader currentClassLoader0() {
        unimplemented("currentClassLoader0");
        return null;
    }

    @SUBSTITUTE
    private int classDepth(String name) {
        unimplemented("classDepth");
        return 0;
    }

    @SUBSTITUTE
    private int classLoaderDepth0() {
        unimplemented("classLoaderDepth0");
        return 0;
    }

    @SUBSTITUTE
    private Class currentLoadedClass0() {
        unimplemented("currentLoadedClass0");
        return null;
    }

    private static void unimplemented(String w) {
        GuestVMError.unimplemented("java.lang.SecurityManager."+ w);
    }

}
