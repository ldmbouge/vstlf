package edu.uconn.vstlf.block;

import java.util.Date;

import com.web_tomorrow.utils.suntimes.SunTimes;
import com.web_tomorrow.utils.suntimes.SunTimesException;
import com.web_tomorrow.utils.suntimes.Time;

import edu.uconn.vstlf.config.Items;
import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;

public class SunsetIndexBlock extends InputBlock {

	private Calendar _gmt = new Calendar("GMT");
	private double[] sunsetIndex_;
	
	public SunsetIndexBlock(Date t, Calendar cal) throws SunTimesException {
		//sunset stuff
		double[] sunHr = new double[3];
		double[] sunMin = new double[12];
		int zYr = _gmt.getYear(t);
		int zMonth = _gmt.getMonth(t)+1;
		int zDay = _gmt.getDate(t);
		double longitude = new Double(Items.Longitude.value());
		double latitude = new Double(Items.Latitude.value());
		Time zTime = SunTimes.getSunsetTimeUTC(zYr, zMonth, zDay, longitude, latitude, SunTimes.CIVIL_ZENITH);
		Date zDate = _gmt.newDate(zYr, zMonth-1, zDay, zTime.getHour(), zTime.getMinute(), zTime.getSecond());
		zDate = cal.lastTick(300, zDate);
		int tHour = cal.getHour(t), zHour = cal.getHour(zDate);
		sunHr[0] = (tHour+1==zHour)?1:0;  sunHr[1] = (tHour==zHour)?1:0;  sunHr[2] = (tHour-1==zHour)?1:0;
		if(sunHr[1]==1){
			int zMin = cal.getMinute(zDate)/5; sunMin[zMin] = 1;
		}
		
		int len = sunHr.length + sunMin.length;
		sunsetIndex_ = new double[len];
		System.arraycopy(sunHr, 0, sunsetIndex_, 0, sunHr.length);
		System.arraycopy(sunMin, 0, sunsetIndex_, sunHr.length, sunMin.length);
	}
	

	@Override
	public Series getInput() throws Exception {
		// TODO Auto-generated method stub
		return new Series(sunsetIndex_, false);
	}

}
