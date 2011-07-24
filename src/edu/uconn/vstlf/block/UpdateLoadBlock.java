package edu.uconn.vstlf.block;


import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.prediction.DataFeed;

public class UpdateLoadBlock extends UpdateBlock {
	private int lvl_;
	private int nLvls_;
	private Series refOut_;
	private Series lastMaxInput_;
	
	public UpdateLoadBlock(Series refOut, Series lastMaxInput,
			int lvl, int nLvls)
	{
		lvl_ = lvl;
		nLvls_ = nLvls;
		refOut_ = refOut;
		lastMaxInput_ = lastMaxInput;
	}
	
	@Override
	public Series getUpdateInput() throws Exception {
		// TODO Auto-generated method stub
		if (lvl_ != nLvls_)
			return DataFeed.getNormFuncs()[lvl_].imageOf(refOut_);
		else {
			Series targSet = lastMaxInput_.append(refOut_);
			return DataFeed.getNormFuncs()[lvl_].imageOf(targSet.differentiate().suffix(refOut_.length()));
		}
	}

}
