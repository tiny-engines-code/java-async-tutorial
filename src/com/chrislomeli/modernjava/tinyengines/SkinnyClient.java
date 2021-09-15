package com.chrislomeli.modernjava.tinyengines;

import java.util.concurrent.Future;

public class SkinnyClient {

    public static void callServer() {
        // create the new server (SDK)
        AsyncServerService server = new AsyncServerService();

        // start time
        long start = System.nanoTime();

        // call the async server API
        Future<Integer> createResourceResult = server.createResourceSDK("resource1");   // query

        // return a reference to a future immediately
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Server returned a future after " + invocationTime+" msecs");

        // while the id is being retreived, do other work
        while (!createResourceResult.isDone() && !createResourceResult.isCancelled() ) {
            System.out.println("Working on other important tasks .... ");
            Util.delay(1000L);
        }

        // get the returned value off of the future
        try {
            var id = createResourceResult.get();
            System.out.printf("findWidget returned %d%n", id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long retrievalTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Id returned after " + retrievalTime + " msecs");

    }

    public static void main(String[] args) {
        callServer();
    }
}
