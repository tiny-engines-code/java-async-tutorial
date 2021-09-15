package com.chrislomeli.modernjava.tinyengines;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/*
  Async producer
 */
public class ServerService {

    Random random =  new Random();

     public Integer daoFindWidget(String name) {
        Util.delay();
        return random.nextInt(100);
    }


    /* from Modern Java
      This is a lonhand version of an async SERVER function (as you might find in an SDK)
      The server:
        * [x] creates the completable future
        * [x] calls the worker on a separate thread
        * [x] hands the completable future back immediately so is pointed to by both client and the server
        * [x] updates the completable future
     */
    public Future<Integer> findWidgetSDK(String name) {
        CompletableFuture<Integer> futurePrice = new CompletableFuture<>();  // create the completeable future

        new Thread (() -> {  // call the blocking worker on it's own thread
            var price =  daoFindWidget(name);
            futurePrice.complete(price);   // once we are done - update the future -- since the client already has it and will get the answer when the server updates
        }).start();

        return futurePrice;  // return the completable immediately
    }


    /*
      The supply Aysync function handles does everything above for us
      The server:
        * [x] creates the completable future
        * [x] calls the daoFindWidget() on a separate thread in the ForkJoin pool
        * [x] hands the completable future back immediately so is pointed to by both client and the server
        * [x] updates the completable future
        which puts the daoFindWidget() on a thread in the ForkJoin pool
     */
    public Future<Integer> findWidgetSDKSugar(String name) {
        return CompletableFuture.supplyAsync(() -> daoFindWidget(name));
    }

    public static void main(String[] args) {

        // create the new server (SDK)
        ServerService server = new ServerService();

        // start time
        long start = System.nanoTime();

        // call the async server API
        Future<Integer> futurePrice = server.findWidgetSDK("widget1");   // query

        // return a reference to a future immediately
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Server returned a future after " + invocationTime+" msecs");

        // while the id is being retreived, do other work
        while (!futurePrice.isDone() && !futurePrice.isCancelled() ) {
            System.out.println("Working on other important tasks .... ");
            Util.delay();
        }

        // get the returned value off of the future
        try {
            var id = futurePrice.get();
            System.out.printf("findWidget returned %d%n", id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long retrievalTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Id returned after " + retrievalTime + " msecs");
    }

}
