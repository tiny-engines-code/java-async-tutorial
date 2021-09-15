package com.chrislomeli.modernjava.tinyengines;

public class Util {

    public static void delay(long milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
