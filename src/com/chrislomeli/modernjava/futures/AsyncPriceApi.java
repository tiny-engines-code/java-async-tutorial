package com.chrislomeli.modernjava.futures;

import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/*
  Async producer
 */
public class AsyncPriceApi {

    Random random =  new Random();
    String whydoIcare;

    public AsyncPriceApi(String product) {
        this.whydoIcare = product;
    }

    public static void delay() {
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

     public double calculatePrice(String product) {
        delay();
        return random.nextDouble() * product.charAt(0) + product.charAt(1);
    }

    /*
       Blocking get prices
     */
    public double getPrice(String product) {
        return calculatePrice(product);
    }

    /* Aysync - longhand (ForkJoin pool)
      Create the competabl future
      Open a new thread - having the completable future
      return the completable future to the caller
      -- when the price is calulated - the compleable future will be "completed"
     */
    public Future<Double> getPriceAsyncLong(String product) {
        CompletableFuture<Double> futurePrice = new CompletableFuture<>();
        new Thread (() -> {
            double price =  calculatePrice(product);    // blocking task
            futurePrice.complete(price);   // when the price is calulated - the compleable future will be "completed"
        }).start();

        return futurePrice;  // return the completable immediately
    }

    /* Aysync - shorthand (ForkJoin pool)
       the supplyAsync() function does all of the above for us
        which puts the supplier function (calculatePrice()) on a thread in the ForkJoin pool
     */
    public Future<Double> getPriceAsyncShort(String product) {
        return CompletableFuture.supplyAsync(() -> calculatePrice(product));
    }

    public static void main(String[] args) {
        AsyncPriceApi shop = new AsyncPriceApi("Best buy");
        long start = System.nanoTime();
        Future<Double> futurePrice = shop.getPriceAsyncShort("my favorite product");   // query
        long invocationTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Invocation returned after " + invocationTime+" msecs");
// Do some more tasks, like querying other shops

// while the price of the product is being calculated
        while (!futurePrice.isDone() && !futurePrice.isCancelled() ) {
            System.out.println("Waiting .... but could be dong other stuff");
            delay();
        }

        try {
            double price = futurePrice.get();
            System.out.printf("Price is %.2f%n", price);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long retrievalTime = ((System.nanoTime() - start) / 1_000_000);
        System.out.println("Price returned after " + retrievalTime + " msecs");
    }

}
