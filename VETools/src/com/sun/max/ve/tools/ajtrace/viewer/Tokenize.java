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
