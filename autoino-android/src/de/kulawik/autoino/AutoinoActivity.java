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

public class AutoinoActivity extends Activity implements OnSeekBarChangeListener, AccelerometerListener {
	private static final String DEVICE_ADDRESS = "00:11:12:05:03:96";
	private static Context context;
	SeekBar redSB;
	Button send;

	int red;

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
		redSB = (SeekBar) findViewById(R.id.bar);
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
		/*
		if (System.currentTimeMillis() - lastChange > DELAY) {
			updateState(seekBar);
			lastChange = System.currentTimeMillis();
		}*/
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		updateState(seekBar);
	}

	private void updateState(final SeekBar seekBar) {
		switch (seekBar.getId()) {
			case R.id.bar:
				red = seekBar.getProgress();
				updateRed();
				break;

		}
	}

	private void updateRed() {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'o', red);
	}

	@Override
	public void onAccelerationChanged(int xx, int yy, int zz) {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 's', yy);
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'm', xx);
		//Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'z', zz);

		((TextView) findViewById(R.id.x)).setText("x (servo) : " + yy);
		((TextView) findViewById(R.id.y)).setText("y (motor) : " + xx);
		((TextView) findViewById(R.id.z)).setText(String.valueOf(zz));

	}
}