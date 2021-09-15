package com.chrislomeli.modernjava.futures;

import lombok.Data;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/*
  Async producer
  This is the case where we implement the "promise" in the server and pass a future back to the caller

 */
public class AsyncPriceClient {

    @Data
    static class Shop {
        Random random = new Random();
        String name;

        public void delay() {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public Shop(String bestPrice) {
            this.name = bestPrice;
        }

        public double calculatePrice(String product) {
            delay();
            return random.nextDouble() * product.charAt(0) + product.charAt(1);
        }

        public double getPrice(String product) {
            return calculatePrice(product);
        }
    }


    static List<Shop> shops = List.of(new Shop("BestPrice"),
            new Shop("LetsSaveBig"),
            new Shop("MyFavoriteShop"),
            new Shop("BuyItAll"));

    public static List<String> findPricesStream(String product) {
        return shops.stream()
                .map(shop -> String.format("%s price is %.2f",
                        shop.getName(), shop.getPrice(product)))
                .collect(toList());
    }

    /*
        Example one - basic stream
        Get prices from each store
     */
    public static void tryStream(String product) {
        long start = System.nanoTime();
        var price = shops.stream()
                .map(shop -> String.format("\t%s price is %.2f\n",
                        shop.getName(), shop.getPrice(product)))
                .collect(toList());

        long duration = (System.nanoTime() - start) / 1_000_000;
        price.forEach(System.out::printf);
        System.out.printf("Stream Duration: %d msecs\n", duration);
    }

    /*
        Example one - parallel stream
        One technique is to convert to a stream and do parallel operations such as map
        -- Good for CPU bound tasks becase CPU's are tasked and there's no additional capacity anyways
        - each CPU gets a shop
        Get prices from each store
        BUT - only scale to the available CPU's - otherwise starts blocking CPU capacity
            - same issue as before - the excess tasks have to wait for an open CPU
     */
    public static void tryParallelStream(String product) {
        long start = System.nanoTime();
        var price = shops.parallelStream()
                .map(shop -> String.format("\t%s price is %.2f\n",
                        shop.getName(), shop.getPrice(product)))
                .collect(toList());

        long duration = (System.nanoTime() - start) / 1_000_000;
        price.forEach(System.out::printf);
        System.out.printf("Parallel Stream Duration: %d msecs\n", duration);
    }

    /*   Completeable futures
        Iterate over the collection ans spawn Compleable futures
           -- Good for IO (waiting) scenarios

        - comparable performance for tasks <= CPU's but runs better than parallel when there are more tasks
     */
    public static void tryFuturePrices(String product) {
        long start = System.nanoTime();

        // were are just creating the competeable future in the client
        List<CompletableFuture<String>> priceFutures =
                shops.stream()
                        .map(shop -> CompletableFuture.supplyAsync(() -> "\t"+shop.getName() + " price is " + shop.getPrice(product) + "\n"))
                        .collect(Collectors.toList());

        // then, joining the outcomes - when all are 'comlpete' then we move on
        var price =  priceFutures.stream()
                .map(CompletableFuture::join)
                .collect(toList());

        // print the results
        long duration = (System.nanoTime() - start) / 1_000_000;
        price.forEach(System.out::printf);
        System.out.printf("Completeable futures Duration: %d msecs\n", duration);
    }

    /*
    Completable future but tweaking the threads
     Example - maybe squeeze a little more efficiency if we create a tpol with the exact amount of shops
  */
    private static final Executor customThreadPool =
            Executors.newFixedThreadPool(Math.min(shops.size(), 100),
                    (Runnable r) -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        return t;
                    });

    public static void tryCustomerExecutorStream(String product) {
        long start = System.nanoTime();
        List<CompletableFuture<String>> priceFutures =
                shops.stream()
                        .map(shop -> CompletableFuture.supplyAsync(() -> "\t"+shop.getName() + " price is " + shop.getPrice(product) + "\n", customThreadPool))
                        .collect(Collectors.toList());

        // then, joining the outcomes - when all are 'comlpete' then we move on
        var price =  priceFutures.stream()
                .map(CompletableFuture::join)
                .collect(toList());

        // print the results
        long duration = (System.nanoTime() - start) / 1_000_000;
        price.forEach(System.out::printf);
        System.out.printf("Custom executor Duration: %d msecs\n", duration);
    }



    /*
        Example one - basic stream
        Get prices from each store
     */
    public static void fullMontey(String product) {
        long start = System.nanoTime();
        var price = shops.stream()
                .map(shop -> String.format("\t%s price is %.2f\n",
                        shop.getName(), shop.getPrice(product)))
                .collect(toList());

        long duration = (System.nanoTime() - start) / 1_000_000;
        price.forEach(System.out::printf);
        System.out.printf("Stream Duration: %d msecs\n", duration);
    }

    public static void main(String[] args) {
        tryStream("myPhone27S");
        tryParallelStream("myPhone27S");  // good for CPU count >= shops
        tryFuturePrices("myPhone27S");  // better for CPU count < shops
        tryCustomerExecutorStream("myPhone27S");  // efficiency if CPU count < shops and we know how many shops
    }

}
