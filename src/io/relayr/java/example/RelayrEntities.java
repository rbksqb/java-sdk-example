package io.relayr.java.example;

import java.util.List;

import io.relayr.java.model.Device;
import io.relayr.java.model.Transmitter;
import io.relayr.java.model.User;
import io.relayr.java.model.groups.Group;
import rx.Observer;

public class RelayrEntities {

    private User user;

    public RelayrEntities(User user) {
        this.user = user;
    }

    public void start() {
        getGroups();
        getTransmitters();
        getDevices();
    }

    private void getGroups() {
        user.getGroups()
                .subscribe(new Observer<List<Group>>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        System.err.println("Something went wrong while fetching groups.");
                        e.printStackTrace();
                    }

                    @Override public void onNext(List<Group> groups) {
                        System.out.println(groups.size() + " GROUPS");

                        for (Group group : groups)
                            System.out.println(group.getName());
                    }
                });
    }

    private void getTransmitters() {
        user.getTransmitters()
                .subscribe(new Observer<List<Transmitter>>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        System.err.println("Something went wrong while fetching transmitters.");
                        e.printStackTrace();
                    }

                    @Override public void onNext(List<Transmitter> transmitters) {
                        System.out.println(transmitters.size() + " TRANSMITTERS");

                        for (Transmitter transmitter : transmitters)
                            System.out.println(transmitter.getName());
                    }
                });
    }

    private void getDevices() {
        user.getDevices()
                .subscribe(new Observer<List<Device>>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        System.err.println("Something went wrong while fetching devices.");
                        e.printStackTrace();
                    }

                    @Override public void onNext(List<Device> devices) {
                        System.out.println(devices.size() + " DEVICES");

                        for (Device device : devices)
                            System.out.println(device.getName());
                    }
                });
    }
}
