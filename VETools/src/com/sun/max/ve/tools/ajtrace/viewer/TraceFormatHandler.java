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
package com.sun.max.ve.tools.ajtrace.viewer;


public abstract class TraceFormatHandler {
    public final  String method;
    public final int numArgs;
    public final int param;
    
    /**
     * Define a transform for given parameter of given method.
     * @param method fully qualified method name
     * @param numArgs poor man's overloading mechanism
     * @param param parameter to transform, numbering from 1.
     */
    protected TraceFormatHandler(String method, int numArgs, int param) {
        if (param > numArgs) {
            throw new IllegalArgumentException("param number " + param + " out of range");
        }
        this.method = method;
        this.numArgs = numArgs;
        this.param = param;
    }
    
    public abstract String transform(String value);
    
    @Override
    public int hashCode() {
        return method.hashCode() ^ numArgs;
    }
    
    @Override
    public boolean equals(Object other) {
        TraceFormatHandler otherHandler = (TraceFormatHandler) other;
        return method.equals(otherHandler.method) && numArgs == otherHandler.numArgs;
    }
    
    static class Check extends TraceFormatHandler {
        Check(String method, int numArgs) {
            super(method, numArgs, numArgs);
        }
        public String transform(String value) {
            return value;
        }
        
    }
}
