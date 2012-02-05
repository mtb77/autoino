package de.kulawik.autoino;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;
import de.kulawik.autoino.accelerometer.AccelerometerListener;
import de.kulawik.autoino.accelerometer.AccelerometerManager;

public class AutoinoActivity extends Activity implements AccelerometerListener {
	private static final String DEVICE_ADDRESS = "00:11:12:05:03:96";
	private static Context context;
	private PowerManager.WakeLock wl;
	private boolean engineStatus = false;

	public static Context getContext() {
		return context;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerReceiver(connectionStateReceiver, new IntentFilter(AmarinoIntent.ACTION_CONNECTED));
		setContentView(R.layout.main);
		context = this;

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");

		SeekBar barLight = (SeekBar) findViewById(R.id.barLight);
		barLight.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			private long lastChange;
			private final int DELAY = 100;

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (System.currentTimeMillis() - lastChange > DELAY) {
					updateState(seekBar);
					lastChange = System.currentTimeMillis();
				}
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
					case R.id.barLight:
						int light = seekBar.getProgress();
						Amarino.sendDataToArduino(context, DEVICE_ADDRESS, 'o', light);
						break;
				}
			}
		});

		ToggleButton toggleEngine = (ToggleButton) findViewById(R.id.toggleEngine);
		toggleEngine.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleEngine();
			}
		});

		ToggleButton togglePolice = (ToggleButton) findViewById(R.id.togglePolice);

		TextView statusText = (TextView) findViewById(R.id.txtStatus);
		statusText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				EditText et = (EditText) findViewById(R.id.txtStatus);
				Amarino.sendDataToArduino(context, DEVICE_ADDRESS, 'd', et.getText().toString());
			}
		});
	}

	private BroadcastReceiver connectionStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				String action = intent.getAction();
				if (AmarinoIntent.ACTION_CONNECTED.equals(action)) {
					Amarino.sendDataToArduino(context, DEVICE_ADDRESS, 'd', "CONNECTED");
				}
			}
		}
	};

	private void toggleEngine() {
		if (engineStatus) {
			//Stopping Engine!
			Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 's', 127);
			Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'm', 127);
			Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'd', "DISCONNECTED");
			Amarino.disconnect(this, DEVICE_ADDRESS);
		} else {
			//Starting Engine!
			Amarino.connect(this, DEVICE_ADDRESS);
		}
		engineStatus = !engineStatus;
	}

	protected void onResume() {
		super.onResume();
		if (AccelerometerManager.isSupported()) {
			AccelerometerManager.startListening(this);
		}
		wl.acquire();

		ToggleButton toggleEngine = (ToggleButton) findViewById(R.id.toggleEngine);
		if (toggleEngine.isChecked()) {
			toggleEngine.setChecked(false);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		wl.release();

		ToggleButton toggleEngine = (ToggleButton) findViewById(R.id.toggleEngine);
		if (toggleEngine.isChecked()) {
			toggleEngine.setChecked(false);
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
	}

	@Override
	protected void onStop() {
		super.onStop();
		engineStatus = true;
		toggleEngine();
	}

	@Override
	public void onAccelerationChanged(int xx, int yy, int zz) {
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 's', yy);
		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'm', xx);

		((TextView) findViewById(R.id.x)).setText("Servo : " + yy);
		((TextView) findViewById(R.id.y)).setText("Motor : " + xx);

	}
}