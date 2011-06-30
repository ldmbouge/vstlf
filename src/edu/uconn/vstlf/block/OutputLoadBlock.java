package edu.uconn.vstlf.block;

import edu.uconn.vstlf.data.doubleprecision.NormalizingFunction;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.prediction.DataFeed;

public class OutputLoadBlock extends OutputBlock {

	private Series maxLvlInput_;
	private int lvl_, nLvls_;
	
	public OutputLoadBlock(Series maxLvlInput, int lvl, int nLvls)
	{
		lvl_ = lvl;
		nLvls_ = nLvls;
		maxLvlInput_ = maxLvlInput;
	}
	
	@Override
	public Series getOutput(Series output) throws Exception {
		// TODO Auto-generated method stub
		Series nnOut = output;
		NormalizingFunction denorm = DataFeed.getDenormFuncs()[lvl_];
		Series result;
		if (lvl_ != nLvls_)
			result = denorm.imageOf(nnOut);
		else {
			Series dOut = denorm.imageOf(nnOut);
			result = dOut.undifferentiate(maxLvlInput_.suffix(1).element(1));
		}
		return result;
	}

}
