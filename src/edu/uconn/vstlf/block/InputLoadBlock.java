package edu.uconn.vstlf.block;

import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.prediction.DataFeed;

public class InputLoadBlock extends InputBlock {

	private Series input_;
	private int lvl_;
	private int nLvls_;
	
	public InputLoadBlock(Series input, int lvl, int nLvls)
	{
		input_ = input;
		lvl_ = lvl;
		nLvls_ = nLvls;
	}
	
	@Override
	public Series getInput() throws Exception {
		if (lvl_ != nLvls_)
			return DataFeed.getNormFuncs()[lvl_].imageOf(input_);
		else {
			// Special cases for the maximum level
			int n_1 = input_.length()-1;
			Series diff = input_.prefix(1).append(input_.differentiate().suffix(n_1));
			Series lastNormVals = DataFeed.getNormFuncs()[lvl_].imageOf(diff.suffix(n_1));
			Series firstNormVal = DataFeed.getAbsNormFunc().imageOf(diff.prefix(1));
			return firstNormVal.append(lastNormVals);
		}
	}

}
