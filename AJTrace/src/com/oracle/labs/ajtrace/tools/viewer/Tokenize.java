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

import java.io.*;

public final class Tokenize {

    public static void main(String[] args) {
        try {
            new Tokenize(args[0]).doTestTokenize();
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
        }
    }

    private StreamTokenizer _st = null;

    private Tokenize(String dataFile) throws IOException {
        this._st = new StreamTokenizer(new BufferedReader(new FileReader(dataFile)));
        resetStreamTokenizer(_st);
    }

    private void doTestTokenize() throws IOException {
        _st.nextToken();
        while (_st.ttype != StreamTokenizer.TT_EOF) {
            switch (_st.ttype) {
                case StreamTokenizer.TT_WORD:
                    System.out.println("TT_WORD: " + _st.sval);
                    break;
                case StreamTokenizer.TT_EOL:
                    System.out.println("TT_EOL");
                    break;
                case StreamTokenizer.TT_NUMBER:
                    System.out.println("TT_NUMBER: " + _st.nval);
                    break;
                default:
                    System.out.println("OTHER: " + _st.ttype + " " + _st.sval);
            }
            _st.nextToken();
        }
    }

    public static void resetStreamTokenizer(StreamTokenizer xst) {
        xst.resetSyntax();
        xst.wordChars('a', 'z');
        xst.wordChars('A', 'Z');
        xst.wordChars(128 + 32, 255);
        xst.whitespaceChars(' ', ' ');
        xst.whitespaceChars('\t', '\t');
        xst.whitespaceChars('\t', '\t');
        xst.whitespaceChars(',', ',');
        xst.whitespaceChars('=', '=');
        xst.whitespaceChars(';', ';');
        xst.whitespaceChars(';', ';');
        xst.whitespaceChars(':', ':');
        xst.commentChar('#');
        xst.quoteChar('"');
        xst.quoteChar('\'');
        xst.eolIsSignificant(true);
        xst.wordChars('0', '9');
        xst.wordChars('[', '[');
        xst.wordChars(']', ']');
        xst.wordChars('@', '@');
        xst.wordChars('.', '.');
        xst.wordChars('-', '-');
        xst.wordChars('$', '$');
        xst.wordChars('_', '_');
    }
}
