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
package com.sun.max.ve.net.ip;

public class IPAddress {

    private int _address;

    public IPAddress(int a, int b, int c, int d) {
        _address = to32(a, b, c, d);
    }

    public IPAddress(int[] a) {
        this(a[0], a[1], a[2], a[3]);
    }

    public IPAddress(byte[] a) {
        this(a[0], a[1], a[2], a[3]);
    }

    public IPAddress(int a) {
        _address = a;
    }

    public static int byteToInt(byte[] a) {
        return to32(a[0], a[1], a[2], a[3]);
    }

    private static int to32(int a, int b, int c, int d) {
        return ((a & 0xff) << 24) | ((b & 0xff) << 16) | ((c & 0xff) << 8) | (d & 0xff);
    }

    public int addressAsInt() {
        return _address;
    }

    public static String toString(int a) {
        StringBuilder sb = new StringBuilder();
        sb.append((a >> 24) & 0xff).append('.').append((a >> 16) & 0xff).append('.').append((a >> 8) & 0xff).append('.').append(a & 0xff);
        return sb.toString();
    }

    public static String toReverseString(int a) {
        StringBuilder sb = new StringBuilder();
        sb.append(a & 0xff).append('.').append((a >> 8) & 0xff).append('.').append((a >> 16) & 0xff).append('.').append((a >> 24)  & 0xff);
        return sb.toString();
    }

    public String toString() {
        return toString(_address);
    }

    private static IPAddress _loopback;
    public static IPAddress loopback() {
        if (_loopback == null) {
            _loopback = new IPAddress(127, 0, 0, 1);
        }
        return _loopback;
    }

    /**
     * Utility to convert a dotted-decimal string to an IP address.
     *
     */
    public static IPAddress parse(String s)
                                throws java.lang.NumberFormatException {
        int val = 0;
        int idx = 0;


        fail: while (true) {
            for (int pos = 0 ; pos < 4 ; pos++) {        // loop through 4 bytes
                int n = 0;
                boolean firstDigit = true;
                while (idx < s.length()) {
                    char c = s.charAt(idx);

                    // ensure at least one digit
                    if (firstDigit && !Character.isDigit(c)) {
                        break fail;
                    }

                    // terminator must be . for 1st three bytes
                    if (c == '.' && pos < 3) {
                        idx++;
                        break;        // done with this position
                    }

                    // any non digit is bad
                    if (!Character.isDigit(c)) {
                        break fail;
                    }

                    // use digit
                    n = n*10 + Character.digit(c, 10);
                    idx++;

                    // range check result
                    if (n > 255) {
                        break fail;
                    }
                    firstDigit = false;
                } // while

                // if we used the entire string but didn't do
                // all four bytes, we've got a problem
                if (idx == s.length() && pos != 3) {
                    break fail;
                }
                val += (n << ((3-pos) * 8));
            } // for

            return new IPAddress(val);
        }

        throw new java.lang.NumberFormatException("Illegal IP address");
    }

}
