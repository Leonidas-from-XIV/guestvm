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
package com.sun.max.ve.jdk;

import com.sun.max.annotate.*;
import com.sun.max.ve.error.*;
import com.sun.max.vm.actor.holder.ClassActor;
import com.sun.max.vm.heap.Heap;
import com.sun.cri.bytecode.INTRINSIC;
import static com.sun.cri.bytecode.Bytecodes.*;

/**
 * MaxVE also uses a UnixFileSystem object as the FileSystem.
 * getFileSystem is native in the Hotspot JDK so we substitute it here.
 *
 * @author Mick Jordan
 */

@METHOD_SUBSTITUTIONS(className = "java.io.FileSystem")
public final class JDK_java_io_FileSystem {

    private static Object _singleton;
    
    @INTRINSIC(UNSAFE_CAST)
    static native JDK_java_io_FileSystem asThis(Object t);
   
    @ALIAS(declaringClassName = "java.io.UnixFileSystem", name="<init>")
    private native void init();

    @SUBSTITUTE
    private static/* FileSystem */Object getFileSystem() {
        // return new UnixFileSystem();
        if (_singleton == null) {
            try {
                final Object fileSystem = Heap.createTuple(ClassActor.fromJava(Class.forName("java.io.UnixFileSystem")).dynamicHub());
                JDK_java_io_FileSystem thisFileSystem = asThis(fileSystem);
                thisFileSystem.init();
                _singleton = fileSystem;
            } catch (Exception ex) {
                VEError.unexpected("failed to construct java.io.UnixFileSystem: " + ex);
                return null;
            }
        }
        return _singleton;
    }

}
