package io.relayr.java.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                            if (device.getId().equals("2aa3bb2a-c4c0-4ad7-acd5-4918986df0bb"))
                                subscribeToReadings(device);
                    }
                });
    }

    private void subscribeToReadings(final TransmitterDevice device) {
        final Map<String, DeviceReading> readings = new HashMap<>();

        try {
            DeviceModel model = RelayrJavaSdk.getDeviceModelsCache().getModel(device.getModelId());
            DeviceFirmware firmware = model.getLatestFirmware();
            Transport transport = firmware.getDefaultTransport();

            for (DeviceReading reading : transport.getReadings())
                readings.put(reading.getMeaning(), reading);
        } catch (DeviceModelsException e) {
            e.printStackTrace();
        }

        System.out.println("Subscribe to readings.");

        device.subscribeToCloudReadings()
                .subscribe(new Observer<Reading>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        System.out.println("Problem while subscribing for data");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Reading reading) {
                        DeviceReading deviceReading = readings.get(reading.meaning);

                        if (deviceReading.getValueSchema().getSchemaType() == SchemaType.NUMBER ||
                                deviceReading.getValueSchema().getSchemaType() == SchemaType.INTEGER)
                            System.out.printf("Value " + ((Double) reading.value).intValue());
                    }
                });
    }
}
