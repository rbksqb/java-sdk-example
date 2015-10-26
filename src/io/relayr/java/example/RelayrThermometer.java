package io.relayr.java.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import io.relayr.java.RelayrJavaSdk;
import io.relayr.java.model.Transmitter;
import io.relayr.java.model.TransmitterDevice;
import io.relayr.java.model.User;
import io.relayr.java.model.action.Reading;
import io.relayr.java.model.models.DeviceFirmware;
import io.relayr.java.model.models.DeviceModel;
import io.relayr.java.model.models.error.DeviceModelsException;
import io.relayr.java.model.models.schema.SchemaType;
import io.relayr.java.model.models.transport.DeviceReading;
import io.relayr.java.model.models.transport.Transport;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;

public class RelayrThermometer {

    private User user;

    public RelayrThermometer(User user) {
        this.user = user;
    }

    public void start() {
        user.getTransmitters()
                .flatMap(new Func1<List<Transmitter>, Observable<List<TransmitterDevice>>>() {
                    @Override
                    public Observable<List<TransmitterDevice>> call(List<Transmitter> transmitters) {
                        if (transmitters.size() > 0) return transmitters.get(0).getDevices();
                        else return Observable.empty();
                    }
                })
                .subscribe(new Observer<List<TransmitterDevice>>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        System.out.println("Problem while getting transmitter data");
                        e.printStackTrace();
                    }

                    @Override public void onNext(List<TransmitterDevice> devices) {
                        System.out.println("Find thermometer");
                        for (TransmitterDevice device : devices)
                            if (device.getId().equals("5ea42b57-4679-4a86-9aff-49096e5995e9"))
                                subscribeToReadingsUsingModel(device);
                    }
                });
    }

    private void subscribeToReadings(final TransmitterDevice device) {
        System.out.println("Subscribe to readings.");

        device.subscribeToCloudReadings()
                .subscribe(new Observer<Reading>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        System.out.println("Problem while subscribing for data");
                        e.printStackTrace();
                        System.exit(0);
                    }

                    @Override
                    public void onNext(Reading reading) {
                        System.out.printf("Value " + reading.value);
                    }
                });
    }

    //Method shows how to use relayr DeviceModel to parse data
    private void subscribeToReadingsUsingModel(final TransmitterDevice device) {
        final Map<String, DeviceReading> modelReadings = new HashMap<>();

        if (RelayrJavaSdk.getDeviceModelsCache().isLoading()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    subscribeToReadingsUsingModel(device);
                }
            }, 1000);
        } else {
            try {
                DeviceModel model = RelayrJavaSdk.getDeviceModelsCache().getModelByName("Wunderbar Thermometer", false);
                DeviceFirmware firmware = model.getLatestFirmware();
                Transport transport = firmware.getDefaultTransport();

                for (DeviceReading reading : transport.getReadings())
                    modelReadings.put(reading.getMeaning(), reading);
            } catch (DeviceModelsException e) {
                e.printStackTrace();
            }

            System.out.println("Subscribe to readings using model.");

            device.subscribeToCloudReadings()
                    .timeout(5, TimeUnit.SECONDS)
                    .subscribe(new Observer<Reading>() {
                        @Override public void onCompleted() {}

                        @Override public void onError(Throwable e) {
                            System.out.println("Problem while subscribing for data");
                            e.printStackTrace();
                            System.exit(0);
                        }

                        @Override
                        public void onNext(Reading reading) {
                            DeviceReading deviceReading = modelReadings.get(reading.meaning);

                            if (deviceReading.getValueSchema().getSchemaType() == SchemaType.NUMBER)
                                System.out.printf("Value " + ((Double) reading.value).intValue());
                        }
                    });
        }
    }
}
