package ch.cern.terrainelevation;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Service;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This is class that will measure the temperature and the pressure using the
 * thermometer and barometer of the device. It will take 1000 measurements and
 * then just make an average from this and calculate variance.
 */
public class MainActivity extends Activity implements CalculateValues {

	// Service for getting GPS values
	private GPSTracker gps;

	/**
	 * Constants used in barometric formula
	 ************************************** 
	 */

	// universal gas constant
	public static final double Rc = 8.31432;
	// gravitational acceleration
	public static final double g = 9.80665;
	// molar mass of Earth's air
	public static final double M = 0.0289644;

	/**
	 ****************************************
	 */

	/**
	 * GUI elements appearing on the screen
	 *************************************** 
	 */

	// text for the count down timer
	private TextView countDownTimer;
	// text for the results
	private TextView results;
	// button for starting initial pressure measuring
	private Button firstPressureBtn;
	// button which triggers measurements of temperature and pressure
	private Button measureBtn;
	// button for showing details
	private Button showDetails;

	/**
	 ****************************************
	 */

	// the sensor manager
	private SensorManager sensorManager;

	private Timer timer;

	// the results from measurements
	private float temperatureResults;
	private float pressureResults;

	// initial pressure
	private float initialPressure = 0;
	private float averageHeight = 0;
	private float averageHeightSquared = 0;

	// latitude and longitude values
	double latitude;
	double longitude;

	// list of all average measurement of the height
	private List<Float> averageHeightLst;
	// list of the variants of height
	private List<Float> varianceLst;

	// counter
	public int counter = 0;

	/**
	 * Indicates whether the initial pressure is measured or the height after
	 * the initial pressure is known. If false, measure initial pressure, if
	 * true measure height
	 */
	private boolean measuring = false;

	private int millis = 10000;

	private static final int period = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		countDownTimer = (TextView) findViewById(R.id.countDownTimer);
		results = (TextView) findViewById(R.id.results);

		measureBtn = (Button) findViewById(R.id.measureBtn);
		measureBtn.setClickable(false);

		showDetails = (Button) findViewById(R.id.showDetailsBtn);
		showDetails.setClickable(false);

		firstPressureBtn = (Button) findViewById(R.id.firstPressureBtn);

		sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);

		averageHeightLst = new ArrayList<Float>();

		varianceLst = new ArrayList<Float>();

		gps = new GPSTracker(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		registerSensor(sensorManager
				.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE));

		registerSensor(sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE));
	}

	@Override
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(sensorEventListener);
	}

	/**
	 * Function for registering sensors
	 */
	private void registerSensor(Sensor sensor) {
		if (sensor != null) {
			sensorManager.registerListener(sensorEventListener, sensor,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	/**
	 * Event listener for clicking the button for measuring initial pressure
	 */
	public void measureInitialPressure(View view) {
		timer = new Timer("Timer");
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				takeSamples();

			}
		}, 0, period);
	}

	/**
	 * Event listener for clicking the button for measuring height
	 */
	public void measure(View view) {
		measureBtn.setClickable(false);
		timer = new Timer("Timer");
		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				takeSamples();
			}
		}, 0, period);
	}

	/**
	 * Event listener for clicking the button for restarting
	 */
	public void restart(View view) {
		measuring = false;
		initialPressure = 0;
		averageHeight = 0;
		millis = 10000;
		counter = 0;
		measureBtn.setClickable(false);
		varianceLst.clear();
		firstPressureBtn.setClickable(true);
		results.setText("");
		latitude = 0;
		longitude = 0;
	}

	/**
	 * Event listener for clicking the button for showing details
	 */
	public void showDetails(View view) {
		StringBuilder builder = new StringBuilder();
		for (float f : varianceLst) {
			builder.append(f).append("\n");
		}
		results.setText(builder.toString());
	}

	private void takeSamples() {

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// measuring initial pressure
				if (!measuring) {
					counter++;
					initialPressure += pressureResults;
					countDownTimer.setText(String
							.valueOf((millis -= period) / 1000));
					countDownTimer.invalidate();
					if (counter == 1000) {
						measuring = true;
						countDownTimer.invalidate();
						initialPressure = (float) initialPressure / counter;
						if (gps.canGetLocation()) {
							latitude = gps.getLatitude();
							longitude = gps.getLongitude();

							results.setText(String.valueOf(latitude));
						} else {
							gps.showSettingsAlert();
						}
						counter = 0;
						millis = 10000;
						measureBtn.setClickable(true);
						firstPressureBtn.setClickable(false);
						timer.cancel();
					}
				}
				// measuring height
				else {
					counter++;
					float res = calculateHeight(temperatureResults,
							pressureResults, initialPressure);

					averageHeight += res;
					averageHeightSquared += Math.pow(res, 2);

					countDownTimer.setText(String
							.valueOf((millis -= period) / 1000));
					countDownTimer.invalidate();
					if (counter == 1000) {
						StringBuilder builder = new StringBuilder();
						gps = new GPSTracker(MainActivity.this);
						builder.append("Height: ")
								.append(String.valueOf(averageHeight / counter))
								.append("\n")
								.append("X: ")
								.append(String.valueOf(Math.abs(gps
										.getLatitude() - latitude)))
								.append("\n")
								.append("Y: ")
								.append(String.valueOf(Math.abs(gps
										.getLongitude() - longitude)));
						results.setText(builder.toString());
						millis = 10000;
						averageHeight /= counter;
						averageHeightSquared /= counter;
						averageHeightLst.add(averageHeight);
						varianceLst.add(averageHeightSquared
								- (float) Math.pow(averageHeight, 2));
						averageHeight = 0;
						averageHeightSquared = 0;
						counter = 0;
						measureBtn.setClickable(true);
						showDetails.setClickable(true);
						timer.cancel();
					}
				}
			}
		});
	}

	/**
	 * 
	 * @param temp
	 *            is the current temperature
	 * @param p
	 *            is the current pressure
	 * @param p0
	 *            is initial pressure
	 * @return height relevant to initial point
	 */
	private float calculateHeight(float temp, float p, float p0) {
		return (float) (-((Rc * temp) / (M * g)) * Math.log(p / p0));
	}

	/**
	 * Event listener for listening for changes in sensor results
	 */
	private final SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			synchronized (this) {
				if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
					temperatureResults = calculateTemperatureResults(event);
				} else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
					pressureResults = calculatePressureResults(event);
				}
			}

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	@Override
	/**
	 * Overridden function from interface CalculateValues for calculating current temperature
	 */
	public float calculateTemperatureResults(SensorEvent event) {
		return (float) (event.values[0] + 273.15);
	}

	@Override
	/**
	 * Overridden function from interface CalculateValues for calculating current pressure
	 */
	public float calculatePressureResults(SensorEvent event) {
		return (float) event.values[0];
	}

}
