package com.chrislomeli.modernjava.futures;

import lombok.Data;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/*
  Async producer
  This is the case where we implement the "promise" in the server and pass a future back to the caller

 */
public class FullAsyncPriceClient {

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

    private static final Executor executor =
            Executors.newFixedThreadPool(Math.min(shops.size(), 100),
                    (Runnable r) -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        return t;
                    });

    public Stream<CompletableFuture<String>> getPricesStream(String product) {
        return shops.stream()
                .map(shop -> CompletableFuture.supplyAsync( () -> shop.getPrice(product), executor)
                        .orTimeout(20, TimeUnit.SECONDS)
                        .thenApply( future -> String.format("Shop xxx - price %f", future)));

    }

    public void findPrices() {
        long start = System.nanoTime();
        CompletableFuture[] futures = getPricesStream("myPhone27S")
                .map(f -> f.thenAccept( s -> System.out.println(s + " (done in " + ((System.nanoTime() - start) / 1_000_000) + " msecs)")))
                .toArray(size -> new CompletableFuture[size]);

        CompletableFuture.allOf(futures).join();
        System.out.println("All shops have now responded in "
                + ((System.nanoTime() - start) / 1_000_000) + " msecs");

          }

    public static void main(String[] args) {
        new FullAsyncPriceClient().findPrices();
    }

}
