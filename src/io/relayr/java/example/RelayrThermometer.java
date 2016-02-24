package io.relayr.java.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.relayr.java.RelayrJavaSdk;
import io.relayr.java.model.Device;
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

    private static final String DEVICE_ID = "your_device_id";

    private User user;

    public RelayrThermometer(User user) {
        this.user = user;
    }

    public void start() {
        //Load user devices
        System.out.println("Start thermometer example.");
        user.getDevices()
                .subscribe(new Observer<List<Device>>() {
                    @Override public void onCompleted() {}

                    @Override public void onError(Throwable e) {
                        System.out.println("Problem while getting transmitter data");
                        e.printStackTrace();
                    }

                    @Override public void onNext(List<Device> devices) {
                        System.out.println("Find thermometer");
                        boolean found = false;
                        for (Device device : devices) {
                            //iterate though all user devices and find device by id
                            //Here you must provide your deviceId
                            if (device.getId().equals(DEVICE_ID)) {
                                found = true;
                                //Simple method
                                subscribeToReadings(device);
                                //Complex method. Shows how to parse any device data with provided device model
                                //subscribeToReadingsUsingModel(device);
                            }
                        }
                        if (!found) System.out.println("Thermometer NOT found");
                    }
                });
    }

    /**
     * Simple example method for subscribing to cloud data through Device entity.
     */
    private void subscribeToReadings(final Device device) {
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
                        if (reading.meaning.equals("temperature"))
                            System.out.println("Temperature " + reading.value);
                    }
                });
    }

    /**
     * Method shows how to use relayr DeviceModel to parse Thermoneter data
     */
    private void subscribeToReadingsUsingModel(final Device device) {
        final Map<String, DeviceReading> modelReadings = new HashMap<>();

        //Check if cache is loaded
        //Cache is not loaded by default. It must be set with Builder argument:
        //RelayrJavaSdk.Builder()...cacheModels(true).build();
        if (RelayrJavaSdk.getDeviceModelsCache().isLoading()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.printf("Cache loading...");
                    subscribeToReadingsUsingModel(device);
                }
            }, 1000);
        } else {
            //Get device model for thermometer sensor and list all possible readings
            try {
                DeviceModel model = RelayrJavaSdk.getDeviceModelsCache().getModelByName("Wunderbar Thermometer", false);
                DeviceFirmware firmware = model.getLatestFirmware();
                Transport transport = firmware.getDefaultTransport();

                System.out.printf("Device model found");

                //Every device reading defines ValueSchema that describes value type and other details
                for (DeviceReading reading : transport.getReadings())
                    modelReadings.put(reading.getMeaning(), reading);
            } catch (DeviceModelsException e) {
                System.out.printf("Device model not found");
                e.printStackTrace();
            }

            System.out.println("Subscribe to readings using model.");

            device.subscribeToCloudReadings()
                    .timeout(10, TimeUnit.SECONDS)
                    .subscribe(new Observer<Reading>() {
                        @Override public void onCompleted() {}

                        @Override public void onError(Throwable e) {
                            if (e instanceof TimeoutException)
                                System.out.println("subscribeToCloudReadings - error.");
                            else
                                System.out.println("subscribeToCloudReadings - timeout.");

                            e.printStackTrace();
                            System.exit(0);
                        }

                        @Override
                        public void onNext(Reading reading) {
                            DeviceReading deviceReading = modelReadings.get(reading.meaning);

                            if (deviceReading.getValueSchema().getSchemaType() == SchemaType.NUMBER)
                                if (deviceReading.getMeaning().equals("temperature"))
                                    System.out.println("Temperature " + reading.value);
                        }
                    });
        }
    }
}
