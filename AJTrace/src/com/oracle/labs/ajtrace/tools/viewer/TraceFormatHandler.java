/*
 * Copyright (c) 2010, 2011, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.labs.ajtrace.tools.viewer;


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
