package io.relayr.java.example;

import io.relayr.java.RelayrJavaSdk;
import io.relayr.java.model.User;
import rx.functions.Action1;

public class Main {

    public static void main(String[] args) {

        //DEBUG mode
        //new RelayrJavaSdk.Builder().inMockMode(true).build();

        //RELEASE mode
        //Obtain your token as described here https://github.com/relayr/java-sdk/blob/master/README.md
        new RelayrJavaSdk.Builder().setToken("Bearer 1b6Moxpv88hFGJlptbDvirShPiKexj7P").build();

        RelayrJavaSdk.getUser().subscribe(new Action1<User>() {
            @Override public void call(User user) {
                new RelayrEntities(user).start();
                new RelayrThermometer(user).start();

            }
        });


    }
}
