package de.kulawik.autoino;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import at.abraxas.amarino.Amarino;
import de.kulawik.autoino.accelerometer.AccelerometerListener;
import de.kulawik.autoino.accelerometer.AccelerometerManager;

public class Autoino extends Activity implements OnSeekBarChangeListener, AccelerometerListener {
	private static final String DEVICE_ADDRESS = "00:11:12:05:03:96";
	private static Context context;
	private final int DELAY = 150;
	SeekBar redSB;
	Button send;
	View colorIndicator;

	int red;
	long lastChange;

	public static Context getContext() {
		return context;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		context = this;

		Amarino.connect(this, DEVICE_ADDRESS);

		// get references to views defined in our main.xml layout file
		redSB = (SeekBar) findViewById(R.id.SeekBarRed);
		colorIndicator = findViewById(R.id.ColorIndicator);
		send = (Button) findViewById(R.id.button1);

		// register listeners
		redSB.setOnSeekBarChangeListener(this);
		send.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText et = (EditText) findViewById(R.id.editText1);
				Amarino.sendDataToArduino(context, DEVICE_ADDRESS, 'd', et.getText().toString());

			}
		});

	}

	protected void onResume() {
		super.onResume();
		if (AccelerometerManager.isSupported()) {
			AccelerometerManager.startListening(this);
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		if (AccelerometerManager.isListening()) {
			AccelerometerManager.stopListening();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();

		// load last state
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		red = prefs.getInt("red", 0);
		redSB.setProgress(red);
	}

	@Override
	protected void onStop() {
		super.onStop();

		PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("red", red).commit();
		Amarino.disconnect(this, DEVICE_ADDRESS);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// do not send to many updates, Arduino can't handle so much
		if (System.currentTimeMillis() - lastChange > DELAY) {
			updateState(seekBar);
			lastChange = System.currentTimeMillis();
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		lastChange = System.currentTimeMillis();
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updateState(seekBar);
	}

	private void updateState(final SeekBar seekBar) {
		switch (seekBar.getId()) {
			case R.id.SeekBarRed:
				red = seekBar.getProgress();
				updateRed();
				break;

		}
	}

	private void updateRed() {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'o', red);
	}

	@Override
	public void onAccelerationChanged(float x, float y, float z) {
		if (System.currentTimeMillis() - lastChange > DELAY) {
			int lr = 85 + ((int) x * -3);
			int vh = 90 + ((int) y * -2);

			Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 's', lr);
			Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'm', vh);

			((TextView) findViewById(R.id.x)).setText(String.valueOf(lr));
			((TextView) findViewById(R.id.y)).setText(String.valueOf(vh));
			((TextView) findViewById(R.id.z)).setText(String.valueOf(z));

			lastChange = System.currentTimeMillis();
		}
	}
}