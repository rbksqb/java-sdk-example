package io.relayr.java.example;

import java.util.concurrent.TimeUnit;

import io.relayr.java.RelayrJavaSdk;
import io.relayr.java.model.User;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class Main {

    public static void main(String[] args) {

        //DEBUG mode
        //new RelayrJavaSdk.Builder().inMockMode(true).build();

        //RELEASE mode
        //Obtain your token as described here https://github.com/relayr/java-sdk/blob/master/README.md
        new RelayrJavaSdk.Builder().setToken("Bearer your_token").cacheModels(true).build();

        //Gets user, starts RelayrEntities object to display all user entities.
        //After 3 seconds it starts the RelayrThermometer object and subscribes for data
        RelayrJavaSdk.getUser()
                .flatMap(new Func1<User, Observable<User>>() {
                    @Override public Observable<User> call(User user) {
                        new RelayrEntities(user).start();
                        return Observable.just(user);
                    }
                })
                .delay(3, TimeUnit.SECONDS)
                .subscribe(new Action1<User>() {
                    @Override public void call(User user) {
                        new RelayrThermometer(user).start();
                    }
                }, new Action1<Throwable>() {
                    @Override public void call(Throwable e) {
                        System.out.println("Example failed.");
                        e.printStackTrace();
                    }
                });
    }
}
