package com.sun.max.ve.logging;


public class SystemOutHandler extends Handler {

    @Override
    public void println(String msg) {
        System.out.println(msg);

    }

}
