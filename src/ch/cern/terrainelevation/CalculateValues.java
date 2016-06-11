package ch.cern.terrainelevation;

import android.hardware.SensorEvent;

public interface CalculateValues {

	public float calculateTemperatureResults(SensorEvent event);

	public float calculatePressureResults(SensorEvent event);
}
