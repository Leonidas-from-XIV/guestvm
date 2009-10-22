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
package com.sun.guestvm.fs;

/**
 * Standard "errno" error decoding.
 * TODO: flesh it out
 *
 * @author Mick Jordan
 *
 */
public class ErrorDecoder {

    public enum Code {
        ENOENT(2, "No such file or directory"),
        EINTR(4, "Interrupted system call"),
        EIO(5, "I/O error"),
        EBADF(9, "Bad file number"),
        EAGAIN(11, "Resource temporarily unavailable"),
        EACCES(13, "Permission denied"),
        EISDIR(21, "Is a directory"),
        EROFS(30, "Read only file system"),
        EPIPE(32, "Broken pipe");

        private int _code;
        private String _message;

        Code(int code, String message) {
            _code = code;
            _message = message;
        }

        public int getCode() {
            return _code;
        }

        public String getMessage() {
            return _message;
        }

    }

    public static String getMessage(int errno) {
        for (Code c : Code.values()) {
            if (errno == c.getCode()) {
                return c.getMessage();
            }
        }
        return "unknown error code: " + errno;
    }

    public static String getFileMessage(int errno, String name) {
        return name + " (" + getMessage(errno) + ")";
    }

}
