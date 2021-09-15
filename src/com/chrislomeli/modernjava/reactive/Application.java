package com.chrislomeli.modernjava.reactive;

import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;


public class Application {

    public static void main(String[] args) {
        simple();
//        getTemperatures("New York").subscribe(new TempSubscriber());


    }

    public static void simple() {
        var town = "New York";
        // Subscriber
        var tempsSubscriber = new TempSubscriber();
        // Subscription
        var tempSubscription = new TempSubscription(tempsSubscriber, town);
        // a puplisher
        Publisher<TempInfo> tempsPublisher = new Publisher<TempInfo>() {

            @Override
            public void subscribe(Subscriber<? super TempInfo> subscriber) {
                subscriber.onSubscribe(tempSubscription);
            }
        };

        tempsPublisher.subscribe(tempsSubscriber);

    }


    private static Publisher<TempInfo> getTemperatures(String town) {
        return subscriber -> subscriber.onSubscribe(
                new TempSubscription(subscriber, town));
    }
}



