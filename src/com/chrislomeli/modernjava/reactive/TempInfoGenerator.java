package com.chrislomeli.modernjava.reactive;

import java.util.Random;

public class TempInfoGenerator {

    public static final Random random = new Random();

    public static TempInfo fetch(String town) {
        if (random.nextInt(10) == 0)
            throw new RuntimeException("Error!");
        return new TempInfo(town, random.nextInt(100));
    }


}
