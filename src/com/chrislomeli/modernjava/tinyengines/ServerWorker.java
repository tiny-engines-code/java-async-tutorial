package com.chrislomeli.modernjava.tinyengines;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/*
  Async producer
 */
class ServerWorker {

    static Random random =  new Random();

    /*
       A mock worker that just waits a while to do its "work" then returns with a random integer
     */
     public static Integer createResourceService(String name) {
        Util.delay(2000L);
        return random.nextInt(100);
    }



}
