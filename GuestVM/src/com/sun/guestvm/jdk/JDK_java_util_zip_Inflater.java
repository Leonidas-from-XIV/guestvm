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
 * {@link java.util.zip.Inflater} are substituted and call the GNU version, except those that directly forward to
 * another public method without accessing the implementation state.
 * 
 * We use an injected field to store the {@link gnu.java.util.zip.Inflater} instance in the
 * {@link java.util.zip.Inflater} instance. We are somewhat dependent on the implementation
 * of {@link java.util.zip.Inflater} but independent of its state, which is good because this
 * changed in JDK 1.6.0_19. The trick is to substitute the constructor, which allows
 * us to store the {@link gnu.java.util.zip.Inflater} in the injected field. We can't do this
 * in the {@code init} method because it is, sigh, {@code static}.
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
import com.sun.max.vm.actor.member.InjectedReferenceFieldActor;
import com.sun.max.vm.classfile.constant.SymbolTable;

@SuppressWarnings("unused")
@METHOD_SUBSTITUTIONS(Inflater.class)
public class JDK_java_util_zip_Inflater {

    /**
     * A field of type {@link gnu.java.util.zip.Inflater} injected into {@link java.util.zip.Inflater}.
     */
    private static final InjectedReferenceFieldActor<gnu.java.util.zip.Inflater> Inflater_gnuInflater = new InjectedReferenceFieldActor<gnu.java.util.zip.Inflater>(java.util.zip.Inflater.class, gnu.java.util.zip.Inflater.class) {
    };
    
    @INLINE
    static gnu.java.util.zip.Inflater getGNUInflater(Object inflater) {
       return (gnu.java.util.zip.Inflater) Inflater_gnuInflater.getObject(inflater);
    }

    @SUBSTITUTE
    private static void initIDs() {
    }
    
    @SUBSTITUTE(constructor=true)
    private void constructor(boolean nowrap) {
        final gnu.java.util.zip.Inflater gnuInflater = new gnu.java.util.zip.Inflater(nowrap);
        Inflater_gnuInflater.setObject(this, gnuInflater);
    }

    @SUBSTITUTE
    private static long init(boolean nowrap) {
        // having substituted the constructor this should never be called, but just to be safe! 
        GuestVMError.unexpected("java.util.zip.Inflater.init should never be called!");
        return 0;
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
        getGNUInflater(this).end();
        Inflater_gnuInflater.setObject(this, null);
    }
    
    
}
