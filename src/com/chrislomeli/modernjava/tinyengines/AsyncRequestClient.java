package com.chrislomeli.modernjava.tinyengines;

import lombok.Data;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/*
  Async producer
  This is the case where we implement the "promise" in the server and pass a future back to the caller

 */
public class AsyncRequestClient {


    static List<String> requests = IntStream.range(1, 100).boxed().map( x -> String.format("request%d", x)).collect(Collectors.toList());

    public static List<String> createResources() {
        return requests.stream()
                .map(request -> String.format("%s price is %.2f",
                        ServerWorker.createResourceService(request)))
                .collect(toList());
    }

    /*
        Example one - basic stream
        Get prices from each store
     */
    public static void tryStream() {
        long start = System.nanoTime();
        var responses = requests.stream()
                .map(ServerWorker::createResourceService)
                .collect(toList());

        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("Processed %d requsts in %d msecs\n", responses.size(), duration);
    }

    /*
        Example one - parallel stream

        if this was a cpu intensive ops - then this is good, becuase all CPU are engaged doing real work, the sum of all that work is then joined
        but, if these are io intensive - and the limited number of parallel stream SHOULD be a poor performr compared to futures
        how can we prove this?
        we need "hangd time" weights of some sort - where we measure how much CPU was being used,???

        One technique is to convert to a stream and do parallel operations such as map
        -- Good for CPU bound tasks becase CPU's are tasked and there's no additional capacity anyways
        - each CPU gets a shop
        Get prices from each store
        BUT - only scale to the available CPU's - otherwise starts blocking CPU capacity
            - same issue as before - the excess tasks have to wait for an open CPU
     */
    public static void tryParallelStream() {
        long start = System.nanoTime();
        var responses = requests.parallelStream()
                .map(ServerWorker::createResourceService)
                .collect(toList());

        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("Processed %d requsts in %d msecs\n", responses.size(), duration);
    }

    /*   Completeable futures
        Iterate over the collection ans spawn Compleable futures
           -- Good for IO (waiting) scenarios

        - comparable performance for tasks <= CPU's but runs better than parallel when there are more tasks
     */
    public static void tryFuturePrices() {
        long start = System.nanoTime();

        // were are just creating the competeable future in the client
        List<CompletableFuture<Integer>> responses =
                requests.stream()
                        .map(request -> CompletableFuture.supplyAsync(() -> ServerWorker.createResourceService(request)))
                        .collect(Collectors.toList());

        // then, joining the outcomes - when all are 'comlpete' then we move on
        var ids =  responses.stream()
                .map(CompletableFuture::join)
                .collect(toList());

        // print the results
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("Joined %d Completeable futures Duration: %d msecs\n", ids.size(), duration);
    }

    /*
    Completable future but tweaking the threads
     Example - maybe squeeze a little more efficiency if we create a tpol with the exact amount of shops
  */
    private static final Executor customThreadPool =
            Executors.newFixedThreadPool(Math.min(requests.size(), 100),
                    (Runnable r) -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        return t;
                    });

    public static void tryCustomerExecutorStream() {
        long start = System.nanoTime();
        List<CompletableFuture<Integer>> responses =
                requests.stream()
                        .map(request -> CompletableFuture.supplyAsync(() -> ServerWorker.createResourceService(request)))
                        .collect(Collectors.toList());

        // then, joining the outcomes - when all are 'comlpete' then we move on
        var ids =  responses.stream()
                .map(CompletableFuture::join)
                .collect(toList());

        // print the results
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("Joined %d Completeable futures Duration: %d msecs\n", ids.size(), duration);
    }



    /*
        Example one - basic stream
        Get prices from each store

        Todo -- not sure where this one is goin
     */
    public static void fullMontey() {
        long start = System.nanoTime();
        var responses =  requests.stream()
                .map(request -> CompletableFuture.supplyAsync(() -> ServerWorker.createResourceService(request)))
                .collect(Collectors.toList());

        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("Joined %d Completeable futures Duration: %d msecs\n", responses.size(), duration);
    }

    public static void main(String[] args) {
        tryStream();
        tryParallelStream();  // good for CPU count >= shops
        tryFuturePrices();  // better for CPU count < shops
        tryCustomerExecutorStream();  // efficiency if CPU count < shops and we know how many shops
    }

}
