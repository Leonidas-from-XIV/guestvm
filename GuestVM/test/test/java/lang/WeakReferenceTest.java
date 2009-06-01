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
package test.java.lang;

import java.lang.ref.*;
import java.util.*;

public class WeakReferenceTest {
    private static ReferenceQueue<T> _refQueue = new ReferenceQueue<T>();;

    public static void main(String[] args) {
        T t = new T();
        System.out.println("t = " + t);
        t = null;
        System.gc();
        NT q;
        while ((q = (NT) _refQueue.poll()) == null) {
            continue;
        }
        System.out.println("q = " + q);
    }

    static class T {
        @SuppressWarnings("unused")
        private NT _native;
        T() {
            _native = new NT(this);
        }
    }

    static class NT extends WeakReference<T> {
        private static List<NT> _refList = new ArrayList<NT>();
        NT(T t) {
            super(t, _refQueue);
            _refList.add(this);
        }
    }


}
