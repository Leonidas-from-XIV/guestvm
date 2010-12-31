package com.sun.max.ve.net;

/**
 * Support for tracing the network stack using the GUKTrace mechanism.
 *
 * @author Mick Jordan
 *
 */
public class Trace {
    public static final byte[] TCP_INPUT_ENTER = "TCP_INPUT_ENTER\0".getBytes();
    public static final byte[] TCP_INPUT_EXIT = "TCP_INPUT_EXIT\0".getBytes();
    public static final byte[] TCP_OUTPUT_ENTER = "TCP_OUTPUT_ENTER\0".getBytes();
    public static final byte[] TCP_OUTPUT_EXIT = "TCP_OUPUT_EXIT\0".getBytes();
}
