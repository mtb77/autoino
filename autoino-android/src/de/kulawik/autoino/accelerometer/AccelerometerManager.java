package de.kulawik.autoino.accelerometer;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import de.kulawik.autoino.Autoino;

public class AccelerometerManager {
	private static Sensor sensor;
	private static SensorManager sensorManager;
	// you could use an OrientationListener array instead
	// if you plans to use more than one listener
	private static AccelerometerListener listener;

	/** indicates whether or not Accelerometer Sensor is supported */
	private static Boolean supported;
	/** indicates whether or not Accelerometer Sensor is running */
	private static boolean running = false;

	/**
	 * Returns true if the manager is listening to orientation changes
	 */
	public static boolean isListening() {
		return running;
	}

	/**
	 * Unregisters listeners
	 */
	public static void stopListening() {
		running = false;
		try {
			if (sensorManager != null && sensorEventListener != null) {
				sensorManager.unregisterListener(sensorEventListener);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Returns true if at least one Accelerometer sensor is available
	 */
	public static boolean isSupported() {
		if (supported == null) {
			if (Autoino.getContext() != null) {
				sensorManager = (SensorManager) Autoino.getContext().getSystemService(Context.SENSOR_SERVICE);
				List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
				supported = new Boolean(sensors.size() > 0);
			} else {
				supported = Boolean.FALSE;
			}
		}
		return supported;
	}

	/**
	 * Registers a listener and start listening
	 * @param accelerometerListener
	 *             callback for accelerometer events
	 */
	public static void startListening(AccelerometerListener accelerometerListener) {
		sensorManager = (SensorManager) Autoino.getContext().getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (sensors.size() > 0) {
			sensor = sensors.get(0);
			running = sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_GAME);
			listener = accelerometerListener;
		}
	}

	/**
	 * The listener that listen to events from the accelerometer listener
	 */
	private static SensorEventListener sensorEventListener = new SensorEventListener() {
		private float sumX, sumY, sumZ;
		private int count = 0;
		private int lastXX = 0, lastYY = 0, lastZZ = 0;
		private int NOISE = 1;
		private long lastChange;
		private final int DELAY = 150;

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		public void onSensorChanged(SensorEvent event) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];

			// we just want to send updates after 150ms 
			if (System.currentTimeMillis() - lastChange > DELAY) {
				lastChange = System.currentTimeMillis();

				// get the values which have been gathered in that time, create an avg value
				// and normalize the values in a range between 0-511
				int xx = (int) (sumX / count * 25.6) + 255;
				int yy = (int) (sumY / count * 25.6) + 255;
				int zz = (int) (sumZ / count * 25.6) + 255;
				if (xx > 511) xx = 511;
				if (yy > 511) yy = 511;
				if (zz > 511) zz = 511;
				if (xx < 0) xx = 0;
				if (yy < 0) yy = 0;
				if (zz < 0) zz = 0;

				// check the delta and verify the noise. If the value has been changed
				// more then the noise, send the update to the listener
				float deltaX = Math.abs(lastXX - xx);
				float deltaY = Math.abs(lastYY - yy);
				float deltaZ = Math.abs(lastZZ - zz);
				if (deltaX > NOISE || deltaY > NOISE || deltaZ > NOISE) {
					lastXX = xx;
					lastYY = yy;
					lastZZ = zz;
					sumX = 0;
					sumY = 0;
					sumZ = 0;
					count = 0;
					// normalize again to 0-255
					xx = xx / 2;
					yy = yy / 2;
					zz = zz / 2;
					listener.onAccelerationChanged(xx, yy, zz);
				}

			} else {
				// just summarize
				sumX += x;
				sumY += y;
				sumZ += z;
				count++;
			}

		}
	};

}