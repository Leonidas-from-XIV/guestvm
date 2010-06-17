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
package test.util;

/**
 * Utterly trivial argument processing for simple test programs.
 * The following keywords are recognised:
 *
 * a1 val   set argument a1
 * a2 val   set argument a2
 * a3 val   set argument a3
 * a4 val   set argument a4
 * op c      set command c
 * v           set verbose
 * gt ord    set GUK tracing flag "ord" to true (see {@link GUKTrace.Name}.
 *
 * Up to ten sets of command/arguments area support.
 * Arguments must precede the command and are available in the corresponding array, e.g. {@link _opArgs1}.
 * Unchanged arguments propagate to subsequent commands.
 *
 * The verbose and GUK trace arguments are global. The former sets {@link _verbose} and the latter
 * is acted upon immediately (and may be repeated with different trace ordinals).
 *
 * @author Mick Jordan
 *
 */
public final class ArgsHandler {

    public final String[] _ops = new String[10];
    public final String[] _opArgs1 = new String[10];
    public final String[] _opArgs2 = new String[10];
    public final String[] _opArgs3 = new String[10];
    public final String[] _opArgs4 = new String[10];
    public int _opCount = 0;
    public boolean _verbose;

    public static ArgsHandler process(String[] args) {
        return new ArgsHandler(args);
    }

    private ArgsHandler(String[] args) {
        // Checkstyle: stop modified control variable check
        for (int i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (arg.equals("a") || arg.equals("a1")) {
                _opArgs1[_opCount] = args[++i];
            } else if (arg.equals("a2")) {
                _opArgs2[_opCount] = args[++i];
            } else if (arg.equals("a3")) {
                _opArgs3[_opCount] = args[++i];
            } else if (arg.equals("a4")) {
                _opArgs4[_opCount] = args[++i];
            } else if (arg.equals("op")) {
                _ops[_opCount++] = args[++i];
                _opArgs1[_opCount] = _opArgs1[_opCount - 1];
                _opArgs2[_opCount] = _opArgs2[_opCount - 1];
                _opArgs3[_opCount] = _opArgs3[_opCount - 1];
                _opArgs4[_opCount] = _opArgs4[_opCount - 1];
            } else if (arg.equals("v")) {
                _verbose = true;
            } else if (arg.equals("gt")) {
                final int ord = Integer.parseInt(args[++i]);
                OSSpecific.setTraceState(ord, true);
            }
        }
        // Checkstyle: resume modified control variable check

    }
}
