package com.chrislomeli.modernjava.tinyengines;

public class Util {

    public static long delaySeconds = 1000L;

    public static void delay() {
        try {
            Thread.sleep(delaySeconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
