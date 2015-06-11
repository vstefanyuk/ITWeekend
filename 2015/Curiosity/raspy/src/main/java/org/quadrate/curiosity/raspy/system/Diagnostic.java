package org.quadrate.curiosity.raspy.system;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;

public class Diagnostic {
    public static void main(final String[] args) throws Exception {
        final GpioController gpioController = GpioFactory.getInstance();

        System.out.println("Connected GPIO");

        try {
            final I2CBus i2cBus = I2CFactory.getInstance(I2CBus.BUS_1);

            System.out.println("Connected I2C");

            try {
                final Leds leds = new Leds(gpioController);

                System.out.println("Turning lights on");
                leds.turnLights(true);
                Thread.sleep(3000);

                System.out.println("Turning lights off");
                leds.turnLights(false);
                Thread.sleep(3000);

                System.out.println("Turning status RED");
                leds.showStatus(Leds.Status.RED);
                Thread.sleep(3000);

                System.out.println("Turning status GREEN");
                leds.showStatus(Leds.Status.GREEN);
                Thread.sleep(3000);

                System.out.println("Turning status BLUE");
                leds.showStatus(Leds.Status.BLUE);
                Thread.sleep(3000);

                System.out.println("Turning status OFF");
                leds.showStatus(Leds.Status.OFF);
                Thread.sleep(3000);

                final Compass compass = new Compass(i2cBus);

                System.out.println("Reading compass's azimuth...");
                for(int attempt = 1;attempt <= 10;attempt++) {
                    System.out.println("Attempt " + attempt + ":" + compass.readAzimuth());
                    Thread.sleep(3000);
                }

                final AccelGyroThermoMeter accelGyroThermoMeter = new AccelGyroThermoMeter(i2cBus);

                System.out.println("Reading accelGyroThermoMeter measurement...");
                for(int attempt = 1;attempt <= 10;attempt++) {
                    final AccelGyroThermoMeter.Measurement measurement = accelGyroThermoMeter.readMeasurement();
                    System.out.println("Attempt " + attempt + ". " +
                        "Pitch " + measurement.getPitch() + ", Roll " + measurement.getRoll() + ". " +
                        "Temperature: " + measurement.getTemperature()
                    );
                    Thread.sleep(3000);
                }

                final Radar radar = new Radar(gpioController);

                final double temperature = accelGyroThermoMeter.getLastMeasurement().getTemperature();
                System.out.println("Reading radar's distance (at temperature " + temperature + "C) ...");
                for(int attempt = 1;attempt <= 10;attempt++) {
                    System.out.println("Attempt " + attempt + ":" + radar.readDistance(temperature));
                    Thread.sleep(3000);
                }

                final Engine engine = new Engine(gpioController);

                final AutoPilot autoPilot = new AutoPilot(engine, compass, accelGyroThermoMeter);

                System.out.println("Starting auto-pilot...");
                autoPilot.start();

                System.out.println("Go auto-pilot, go!");
                autoPilot.moveOn(0.1, 0.9);

                Thread.sleep(60000);

                System.out.println("Stopping auto-pilot...");
                autoPilot.stop();

                final BatteryMeter batteryMeter = new BatteryMeter();

                System.out.println("Driving engine...");
                for(double speed = 0.33;speed <= 1.0;speed += 0.33) {
                    System.out.println("Speed " + speed);
                    for(Engine.Direction direction : Engine.Direction.values()) {
                        if (direction != Engine.Direction.NEUTRAL) {
                            System.out.println("Direction " + direction);

                            engine.drive(direction, speed);

                            Thread.sleep(3000);

                            if (direction == Engine.Direction.FORWARD) {
                                System.out.println("Brain battery level " + (batteryMeter.readBatteryLevel(BatteryMeter.Battery.BRAIN)) * 100.0);
                                Thread.sleep(3000);
                                System.out.println("Engine battery level " + (batteryMeter.readBatteryLevel(BatteryMeter.Battery.ENGINE)) * 100.0);
                            }
                        }
                    }
                    engine.drive(Engine.Direction.NEUTRAL, 0.0);
                }

                /*final ThermoBaroMeter thermoBaroMeter = new ThermoBaroMeter(i2cBus);

                System.out.println("Reading thermoBaroMeter's measurement...");
                for(int attempt = 1;attempt <= 10;attempt++) {
                    final ThermoBaroMeter.Measurement measurement = thermoBaroMeter.readMeasurement();
                    System.out.println("Attempt " + attempt + ": temperature " + measurement.getTemperature() + ", pressure " + measurement.getPressure() + ", altitude " + measurement.getAltitude());
                    Thread.sleep(3000);
                }*/
            } finally {
                i2cBus.close();
            }
        } finally {
            gpioController.shutdown();
        }
    }
}
