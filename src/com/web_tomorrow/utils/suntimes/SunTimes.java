package com.web_tomorrow.utils.suntimes;

/**
Java class for calculating sunset and sunrise times.
<p>
This class  exposes two methods only: getSunsetTimeUTC and getSunriseTimeUTC.
Both take all the parameters necessary to make the calculation; that
is, nothing is assumed, and no system information is used.
If the caller wants to make, for example,
the current year or geographical location the default, then it has 
to do that itself. The caller also needs to supply the Sun's zenith,
which is subject to some interpretation. For most purposes, the constant
ZENITH defined in this class will do the trick for true sunrise and
sunset. There are other zenith constants for various types of
twilight.
<p>
The methods in this class return a UTC time, not a local time.
This allows its operation to be independent of the system. If the
caller requires a local time, which is likely, it needs to convert it.
Methods are provided in the Java <code>TimeZone</code> class to do 
this, but they rely on the system time zone and clock to be correct.
<p>
See the overview for details of how to use this class<br>
PS: for testing purposes, London UK is latitude 51, longitude 0
<p>
(c)2001 Kevin Boone/Web-Tomorrow, all rights reserved
*/
public class SunTimes
{
/**
Default value for Sun's zenith and true rise/set
*/
public static final double ZENITH = 90 + 50.0/60.0;

/**
Sun's zenith at civil twilight
*/
public static final double CIVIL_ZENITH = 96;

/**
Sun's zenith at nautical twilight
*/
public static final double NAUTICAL_ZENITH = 102; 

/**
Sun's zenith at astronomical twilight
*/
public static final double ASTRONOMICAL_ZENITH = 108; 

private static final int TYPE_SUNRISE = 0;
private static final int TYPE_SUNSET = 1;
// DEG_PER_HOUR is the number of degrees of longitude
//  that corresponds to one hour time difference.
private static final double DEG_PER_HOUR = 360.0 / 24.0;

/**
sin of an angle in degrees
*/
private static double sinDeg (double deg)
  {
  return Math.sin (deg * 2.0 * Math.PI / 360.0);
  }

/**
acos of an angle, result in degrees
*/
private static double acosDeg (double x)
  {
  return Math.acos (x) * 360.0 / (2 * Math.PI);
  }

/**
asin of an angle, result in degrees
*/
private static double asinDeg (double x)
  {
  return Math.asin (x) * 360.0 / (2 * Math.PI);
  }

/**
tan of an angle in degrees
*/
private static double tanDeg (double deg)
  {
  return Math.tan (deg * 2.0 * Math.PI / 360.0);
  }

/**
cos of an angle in degrees
*/
private static double cosDeg (double deg)
  {
  return Math.cos (deg * 2.0 * Math.PI / 360.0);
  }

/**
Calculate the day of the year, where Jan 1st is day 1.
Note that this method needs to know the year, because
leap years have an impact here
*/
private static int getDayOfYear (int year, int month, int day)
  {
  int N1 = 275 * month / 9;
  int N2 = (month + 9) / 12;
  int N3 = (1 + ((year - 4 * (year / 4) + 2) / 3));
  int N = N1 - (N2 * N3) + day - 30;
  return N;
  }

/**
Get time difference between location's longitude 
and the Meridian, in hours. West of Meridian has
a negative time difference
*/
private static double getHoursFromMeridian (double longitude)
  {
  return longitude / DEG_PER_HOUR;
  }

/**
Gets the approximate time of sunset or sunrise
In _days_ since midnight Jan 1st, assuming 6am
and 6pm events. We need this figure to derive
the Sun's mean anomaly
*/
private static double getApproxTimeDays 
     (int dayOfYear, double hoursFromMeridian, int type)
  {
  if (type == TYPE_SUNRISE)
    return dayOfYear + ((6.0 - hoursFromMeridian) / 24);
  else
    return dayOfYear + ((18.0 - hoursFromMeridian) / 24);
  }

/**
 Calculate the Sun's mean anomaly in degrees, 
at sunrise or sunset, given the longitude
in degrees
*/
private static double getMeanAnomaly (int dayOfYear, double longitude, int type)
  {
  return (0.9856 * getApproxTimeDays
     (dayOfYear, getHoursFromMeridian (longitude), type)) - 3.289;
  }

/**
Calculates the Sun's true longitude in degrees. The result
is an angle gte 0 and lt 360. Requires the Sun's mean
anomaly, also in degrees
*/
private static double getSunTrueLongitude (double sunMeanAnomaly)
  {
  double l = sunMeanAnomaly + (1.916 * sinDeg(sunMeanAnomaly)) 
		+ (0.020 * sinDeg(2 * sunMeanAnomaly)) + 282.634;

  // get longitude into 0-360 degree range
  if (l >= 360.0) l = l - 360.0;
  if (l < 0) l = l + 360.0;
  return l;
  }

/**
Calculates the Sun's right ascension in hours, given
the Sun's true longitude in degrees. 
Input and output are angles gte 0 and lt 360. 
*/
private static double getSunRightAscensionHours (double sunTrueLongitude)
  {
  double a = 0.91764 * tanDeg(sunTrueLongitude);
  double ra = 360.0 / (2.0 * Math.PI) * Math.atan(a);
  // get result into 0-360 degree range
  //if (ra >= 360.0) ra = ra - 360.0;
  //if (ra < 0) ra = ra + 360.0;

  double lQuadrant  = Math.floor(sunTrueLongitude/90.0) * 90.0;
  double raQuadrant = Math.floor(ra/90.0) * 90.0;
  ra = ra + (lQuadrant - raQuadrant);
	
  return ra/DEG_PER_HOUR; //convert to hours
  }

/**
Gets the cosine of the Sun's local hour angle
*/
private static double getCosLocalHourAngle (double sunTrueLongitude, double latitude, double zenith)
  {
  double sinDec = 0.39782 * sinDeg(sunTrueLongitude);
  double cosDec = cosDeg(asinDeg(sinDec));
	
  double cosH = (cosDeg(zenith) - (sinDec * sinDeg(latitude))) / (cosDec * cosDeg(latitude));
	
  // Check bounds

  return cosH;
  }


/**
Calculate local mean time of rising or setting. By `local' is meant the exact
time at the location, assuming that there were no time zone. That is, 
the time difference between the location and the Meridian depended entirely
on the longitude. We can't do anything with this time directly; we must
convert it to UTC and then to a locale time. The result is expressed
as a fractional number of hours since midnight
*/
private static double getLocalMeanTime (double localHour, double
    sunRightAscensionHours, double approxTimeDays)
  {
  return localHour + sunRightAscensionHours - (0.06571 * approxTimeDays) - 6.622;
  }

/**
Get sunrise or sunset time in UTC, according to flag. Result is returned
in a Time object
<pre>
year	   4-digit year
month      month, 1-12
day        day of month, 1-31
longitute  in degrees, longitudes west of Meridian are negative
latitute   in degrees, latitudes south of equator are negative
zenith     Sun's zenith, in degrees
type       type of calculation to carry out
</pre>
*/
private static Time getTimeUTC (int year, int month, int day, double longitude,
      double latitude, double zenith, int type)
    throws SunTimesException
  {
  int dayOfYear = getDayOfYear (year, month, day);
  double sunMeanAnomaly = getMeanAnomaly (dayOfYear, longitude, type);  
  double sunTrueLong = getSunTrueLongitude (sunMeanAnomaly);
  double sunRightAscensionHours = getSunRightAscensionHours (sunTrueLong);
  double cosLocalHourAngle = getCosLocalHourAngle (sunTrueLong, latitude, zenith);

  double localHourAngle;
  if (type == TYPE_SUNRISE)
    {
    if (cosLocalHourAngle > 1)
      throw new SunTimesException 
        ("Sun never rises on the specified date at the specified location");
    localHourAngle = 360.0 - acosDeg(cosLocalHourAngle);
    }
  else if (type == TYPE_SUNSET)
    {
    if (cosLocalHourAngle < -1)
      throw new SunTimesException 
        ("Sun never sets on the specified date at the specified location");
    localHourAngle = acosDeg(cosLocalHourAngle);
    }
  else
      throw new SunTimesException ("Internal error");

  double localHour = localHourAngle / DEG_PER_HOUR;

  double localMeanTime = getLocalMeanTime (localHour, sunRightAscensionHours, 
    getApproxTimeDays(dayOfYear, getHoursFromMeridian(longitude), type));

  return new Time(localMeanTime - getHoursFromMeridian(longitude));
  }

/**
GetSunriseTimeUTC gets the time of sunrise at the specified
latitude and longitude on the specified day. Result is returned
in a Time object
<pre>
year	   4-digit year
month      month, 1-12
day        day of month, 1-31
longitute  in degrees, longitudes west of Meridian are negative
latitute   in degrees, latitudes south of equator are negative
zenith     Sun's zenith, in degrees
</pre>
*/
public static Time getSunriseTimeUTC (int year, int month, int day, 
      double longitude, double latitude, double zenith)
    throws SunTimesException
  {
  return getTimeUTC(year, month, day, longitude, latitude, zenith, TYPE_SUNRISE);
  }

/**
getSunsetTimeUTC gets the time of sunrise at the specified
latitude and longitude on the specified day. Result is returned
in a Time object 
<pre>
year	   4-digit year
month      month, 1-12
day        day of month, 1-31
longitute  in degrees, longitudes west of Meridian are negative
latitute   in degrees, latitudes south of equator are negative
zenith     Sun's zenith, in degrees
</pre>
*/
public static Time getSunsetTimeUTC (int year, int month, int day, 
      double longitude, double latitude, double zenith)
    throws SunTimesException
  {
  return getTimeUTC(year, month, day, longitude, latitude, zenith, TYPE_SUNSET);
  }
}
