package com.sun.guestvm.logging;


public class SystemOutHandler extends Handler {

    @Override
    public void println(String msg) {
        System.out.println(msg);

    }

}
