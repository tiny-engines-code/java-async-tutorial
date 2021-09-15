package com.chrislomeli.modernjava.tinyengines;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;


public class AsyncServerService {


    /* from Modern Java
      This is a lonhand version of an async SERVER function (as you might find in an SDK)
      The server:
        * [x] creates the completable future
        * [x] calls the worker on a separate thread
        * [x] hands the completable future back immediately so is pointed to by both client and the server
        * [x] updates the completable future
     */
    public Future<Integer> createResourceSDKExample(String name) {
        CompletableFuture<Integer> futurePrice = new CompletableFuture<>();  // create the completeable future

        new Thread (() -> {  // call the blocking worker on it's own thread
            var price =  ServerWorker.createResourceService(name);
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
    public Future<Integer> createResourceSDK(String name) {
        return CompletableFuture.supplyAsync(() -> ServerWorker.createResourceService(name));
    }

}
