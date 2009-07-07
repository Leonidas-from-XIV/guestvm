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

import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

import com.sun.max.annotate.*;
import com.sun.max.vm.actor.holder.ClassActor;
import com.sun.max.vm.actor.member.*;
import com.sun.max.vm.object.*;
import com.sun.max.vm.classfile.constant.SymbolTable;

/**
 * Substitutions for  @see java.util.zip.Deflater.
 * This version is an almost pure delegation to the GNU Classpath version.
 * All the public methods in java.util.zip.Deflater are substituted and call the
 * GNU version, except those that directly forward to another public method
 * without accessing the implementation state.
 *
 * N.B. This code depends on the implementation of java.util.zip.Deflater.
 * It substitutes the native "init" method and uses the "strm" field to index an
 * array of GNU Deflater instances.
 *
 * @author Mick Jordan
 *
 */

@SuppressWarnings("unused")

@METHOD_SUBSTITUTIONS(Deflater.class)
public class JDK_java_util_zip_Deflater {

    @SUBSTITUTE
    private static void initIDs() {
    }

    private static List<gnu.java.util.zip.Deflater> _gnuDeflaters = new ArrayList<gnu.java.util.zip.Deflater>();

    @CONSTANT_WHEN_NOT_ZERO
    private static FieldActor _strmFieldActor;

    @INLINE
    static FieldActor strmFieldActor() {
        if (_strmFieldActor == null) {
            _strmFieldActor = (FieldActor) ClassActor.fromJava(Deflater.class).findFieldActor(SymbolTable.makeSymbol("strm"));
        }
        return _strmFieldActor;
    }

    static int getIndex() {
        final int size = _gnuDeflaters.size();
        for (int i = 0; i < size; i++) {
            if (_gnuDeflaters.get(i) == null) {
                return i;
            }
        }
        _gnuDeflaters.add(null);
        return size;
    }

    static gnu.java.util.zip.Deflater getGNUDeflater(Object deflater) {
        return _gnuDeflaters.get((int) TupleAccess.readLong(deflater, strmFieldActor().offset()));
    }

    @SUBSTITUTE
    private static long init(int level, int strategy, boolean nowrap) {
        final gnu.java.util.zip.Deflater gnuDeflater = new gnu.java.util.zip.Deflater(level, nowrap);
        synchronized (_gnuDeflaters) {
            final int index = getIndex();
            _gnuDeflaters.add(index, gnuDeflater);
            return index;
        }
    }

    @SUBSTITUTE
    private void setInput(byte[] b, int off, int len) {
        final gnu.java.util.zip.Deflater gnuDeflater = getGNUDeflater(this);
        gnuDeflater.setInput(b, off, len);
    }

    @SUBSTITUTE
    private void setDictionary(byte[] b, int off, int len) {
        final gnu.java.util.zip.Deflater gnuDeflater = getGNUDeflater(this);
        gnuDeflater.setDictionary(b, off, len);
    }

    @SUBSTITUTE
    private void setStrategy(int strategy) {
        getGNUDeflater(this).setStrategy(strategy);
    }

    @SUBSTITUTE
    private boolean needsInput() {
        return getGNUDeflater(this).needsInput();
    }

    @SUBSTITUTE
    private void setLevel(int level) {
        getGNUDeflater(this).setLevel(level);
    }

    @SUBSTITUTE
    private boolean finished() {
        return getGNUDeflater(this).finished();
    }

    @SUBSTITUTE
    private void finish() {
        getGNUDeflater(this).finish();
    }

    @SUBSTITUTE
    private int deflate(byte[] b, int off, int len) throws DataFormatException {
        return getGNUDeflater(this).deflate(b, off, len);
    }

    @SUBSTITUTE
    private int getAdler() {
        return getGNUDeflater(this).getAdler();
    }

    @SUBSTITUTE
    private long getBytesRead() {
        return getGNUDeflater(this).getBytesRead();
    }

    @SUBSTITUTE
    private long getBytesWritten() {
        return getGNUDeflater(this).getBytesWritten();
    }

    @SUBSTITUTE
    private void reset() {
        getGNUDeflater(this).reset();
    }

    @SUBSTITUTE
    private void end() {
        final int strm = (int) TupleAccess.readLong(this, strmFieldActor().offset());
        if (strm != 0) {
            _gnuDeflaters.get(strm).end();
            TupleAccess.writeLong(this, strmFieldActor().offset(), 0);
            _gnuDeflaters.set(strm, null);
        }
    }

}
