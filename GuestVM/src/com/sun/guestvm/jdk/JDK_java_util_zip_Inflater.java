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

/**
 * This version is an almost pure delegation to the GNU Classpath version. All the public methods in
 * java.util.zip.Inflater are substituted and call the GNU version, except those that directly forward to another public
 * method without accessing the implementation state.
 * 
 * N.B. This code depends on the implementation of {@link java.util.zip.Inflater}. We substitutes the native "init"
 * method and use the field in {@code Inflater} that normally holds the native handle to index an array of GNU Inflater
 * instances.
 * 
 * N.B. The implementation of Inflater changed at JDK1.6.0_19 to use a separate class {@link java.util.zip.ZStreamRef}
 * to hold the native handle. We support both versions.
 * 
 * @author Mick Jordan
 */

import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import com.sun.guestvm.error.GuestVMError;
import com.sun.max.annotate.CONSTANT_WHEN_NOT_ZERO;
import com.sun.max.annotate.INLINE;
import com.sun.max.annotate.METHOD_SUBSTITUTIONS;
import com.sun.max.annotate.SUBSTITUTE;
import com.sun.max.vm.actor.holder.ClassActor;
import com.sun.max.vm.actor.member.FieldActor;
import com.sun.max.vm.classfile.constant.SymbolTable;
import com.sun.max.vm.object.TupleAccess;

@SuppressWarnings("unused")
@METHOD_SUBSTITUTIONS(Inflater.class)
public class JDK_java_util_zip_Inflater {

    private static List<gnu.java.util.zip.Inflater> _gnuInflaters = new ArrayList<gnu.java.util.zip.Inflater>();

    /**
     * This always holds the {@link FieldActor} for the native handle that we use to store the index to the GNU inflater.
     */
    @CONSTANT_WHEN_NOT_ZERO
    private static FieldActor _nativeHandleFieldActor;

    /**
     * This is {@code null} unless there is a "zsRef" indirection object in {@link java.util.zip.Inflater}.
     */
    @CONSTANT_WHEN_NOT_ZERO
    private static FieldActor _zsRefFieldActor;
    
    static FieldActor nativeHandleFieldActor() {
        if (_nativeHandleFieldActor == null) {
            final ClassActor inflaterClassActor = ClassActor.fromJava(Inflater.class);
            _nativeHandleFieldActor = (FieldActor) inflaterClassActor.findFieldActor(SymbolTable.makeSymbol("strm"));
            if (_nativeHandleFieldActor == null) {
                // held in "address" field of "zsRef" (ZStreamRef) field
                try {
                    _zsRefFieldActor = (FieldActor) inflaterClassActor.findFieldActor(SymbolTable.makeSymbol("zsRef"));
                    if (_zsRefFieldActor == null) {
                        throw new NullPointerException();
                    }
                    _nativeHandleFieldActor = ClassActor.fromJava(Class.forName("java.util.zip.ZStreamRef")).findFieldActor(SymbolTable.makeSymbol("address"));
                } catch (Exception ex) {
                    GuestVMError.unexpected("inconsistency with java.util.zip.Inflater implementation");
                }
            }
        }
        return _nativeHandleFieldActor;
    }

    static int getIndex() {
        final int size = _gnuInflaters.size();
        for (int i = 0; i < size; i++) {
            if (_gnuInflaters.get(i) == null) {
                return i;
            }
        }
        _gnuInflaters.add(null);
        return size;
    }

    static gnu.java.util.zip.Inflater getGNUInflater(Object inflater) {
        int index;
        final FieldActor nhf = nativeHandleFieldActor();  // force initialization
        if (_zsRefFieldActor == null) {
            index = (int) TupleAccess.readLong(inflater, nhf.offset());
        } else {
            final Object zsRef = TupleAccess.readObject(inflater, _zsRefFieldActor.offset());
            index = (int) TupleAccess.readLong(zsRef, nhf.offset());
        }
        return _gnuInflaters.get(index);
    }

    @SUBSTITUTE
    private static void initIDs() {
    }

    @SUBSTITUTE
    private static long init(boolean nowrap) {
        final gnu.java.util.zip.Inflater gnuInflater = new gnu.java.util.zip.Inflater(nowrap);
        synchronized (_gnuInflaters) {
            final int index = getIndex();
            _gnuInflaters.set(index, gnuInflater);
            return index;
        }
    }

    @SUBSTITUTE
    private void setInput(byte[] b, int off, int len) {
        final gnu.java.util.zip.Inflater gnuInflater = getGNUInflater(this);
        gnuInflater.setInput(b, off, len);
    }

    @SUBSTITUTE
    private void setDictionary(byte[] b, int off, int len) {
        final gnu.java.util.zip.Inflater gnuInflater = getGNUInflater(this);
        gnuInflater.setDictionary(b, off, len);
    }

    @SUBSTITUTE
    private int getRemaining() {
        return getGNUInflater(this).getRemaining();
    }

    @SUBSTITUTE
    private boolean needsInput() {
        return getGNUInflater(this).needsInput();
    }

    @SUBSTITUTE
    private boolean needsDictionary() {
        return getGNUInflater(this).needsDictionary();
    }

    @SUBSTITUTE
    private boolean finished() {
        return getGNUInflater(this).finished();
    }

    @SUBSTITUTE
    private int inflate(byte[] b, int off, int len) throws DataFormatException {
        return getGNUInflater(this).inflate(b, off, len);
    }

    @SUBSTITUTE
    private int getAdler() {
        return getGNUInflater(this).getAdler();
    }

    @SUBSTITUTE
    private long getBytesRead() {
        return getGNUInflater(this).getBytesRead();
    }

    @SUBSTITUTE
    private long getBytesWritten() {
        return getGNUInflater(this).getBytesWritten();
    }

    @SUBSTITUTE
    private void reset() {
        getGNUInflater(this).reset();
    }

    @SUBSTITUTE
    private void end() {
        final int strm = (int) TupleAccess.readLong(this, nativeHandleFieldActor().offset());
        if (strm != 0) {
            synchronized (_gnuInflaters) {
                _gnuInflaters.get(strm).end();
                TupleAccess.writeLong(this, nativeHandleFieldActor().offset(), 0);
                _gnuInflaters.set(strm, null);
            }
        }
    }

}
