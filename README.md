# Terrain-Elevation

### Synopsis
This is simple prototype Android Application for measuring elevation, developed during the [CERN Webfest hackathon]
(https://webfest.web.cern.ch/) in August 2014.

### Description
The aim of this application is to determine the elevation of one point in the space with respect to some initial point.
This measuring is done by using the most recent smarphone sensors, i.e. the sensors for measuring the ambient temperature
and the air pressure.

To calculate the height, the [Barometric Formula](https://en.wikipedia.org/wiki/Atmospheric_pressure) is used, which relates
the pressure, temperature and the height. Given the pressure in the initial point and the pressure and temperature in the
current point, the relative height with respect to the initial point can be calculated. In order to do that and to have
more stable measurements, the application is calculating the average of 1000 measurements in some predefined time interval.

### Use Case

The use case of the application is consistent of two steps:

1. The user presses the button named *"Measure initial pressure"* for measuring the pressure in the initial point.
After few seconds, the application captures the average of the pressure measurements and the user can start moving.
This step is done only once, in the beginning.

2. After moving to another point, the user presses the button named *"Measure"* to measure the relative height of
the current point. Again, after few seconds, the application calculates the average of the height measurements,
and shows the measurement in the history of measurements on the screen.This step can be repeated as much as it is needed.

After the finishing of taking the results, the user can press the button named *"Restart"*, in order to start a new
cycle of measurements.


### Limitations

However, due to the noise in the measurements, the calculations are not 100% correct, but still they give an approximate
change in the elevation. Another drawback of this application, is that the smartphone should be fixed on one position during
the process of taking the samples.
