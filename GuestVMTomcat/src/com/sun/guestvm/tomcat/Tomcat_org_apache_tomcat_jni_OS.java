package com.sun.guestvm.tomcat;

import java.util.*;

import com.sun.max.annotate.*;

import org.apache.tomcat.jni.*;

@SuppressWarnings("unused")
@METHOD_SUBSTITUTIONS(OS.class)
public class Tomcat_org_apache_tomcat_jni_OS {

    private static final int UNIX      = 1;
    private static final int LINUX     = 5;
    private static final Random _random = new Random();

	@SUBSTITUTE
	private static boolean is(int ostype) {
		return ostype == UNIX || ostype == LINUX;
	}

    @SUBSTITUTE
    private static int random(byte[] buf, int len) {
    	_random.nextBytes(buf);
    	return len;
    }
}
