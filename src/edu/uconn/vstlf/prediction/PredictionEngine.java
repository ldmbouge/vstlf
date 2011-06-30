package edu.uconn.vstlf.prediction;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;


import edu.uconn.vstlf.block.InputBlock;
import edu.uconn.vstlf.block.OutputBlock;
import edu.uconn.vstlf.block.UpdateBlock;
import edu.uconn.vstlf.config.ANNConfig;
import edu.uconn.vstlf.config.DaubConfig;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.neuro.ANNBank;

/*
 * A prediction engine takes a set of inputs
 * and outputs the prediction
 */
public class PredictionEngine {

	private DaubSpec daubSpec_;
	private ANNSpec[] annSpecs_;
	
	private ANNBank[] annBanks_;
	
	public PredictionEngine(ANNBank[] banks) throws Exception, IOException
	{
		annBanks_ = banks;
		
		daubSpec_ = DaubConfig.getInstance().getDaubSpec();
		annSpecs_ = ANNConfig.getInstance().getANNSpecs();
	}
	
	/*
	 * Perform a prediction with a load series
	 */
	public Series predict(LoadSeries loadSeries) throws Exception
	{
		// Create data source
		DataFeed feed = new DataFeed(loadSeries, daubSpec_);
		
		// Select bank
		int off = loadSeries.getCal().getMinute(loadSeries.getCurTime());
		ANNBank bank = annBanks_[off/5];
		
		// Prepare inputs
		int numAnns = daubSpec_.getNumLevels()+1;
		Series[] input = new Series[numAnns];
		OutputBlock[] outBlks = new OutputBlock[numAnns];
		
		for (int i = 0; i < numAnns; ++i) {
			ANNSpec annSpec = annSpecs_[i];
			// Get the input and output blocks from ANN specification
			Vector<InputBlock> inputBlks = annSpec.getInputBlocks(feed);
			outBlks[i] = annSpec.getOutputBlock(feed);
			
			// Create inputs
			Series inputS = new Series();
			for (InputBlock inBlk : inputBlks)
				inputS = inputS.append(inBlk.getInput());
			input[i] = inputS;
		}
		
		// Prediction
		Series[] out = bank.execute(input);
		
		// Denormalize the outputs
		Series pred = new Series(12);
		for (int i = 0; i < numAnns; ++i)
			pred.plus(outBlks[i].getOutput(out[i]));
		
		return pred;
	}
	
	/*
	 * Preform an update with the known load in the load series
	 */
	public void update(LoadSeries loadSeries) throws Exception
	{
		Date prevHour = loadSeries.getCal().addHoursTo(loadSeries.getCurTime(), -1);
		LoadSeries prevSeries = loadSeries.getSubSeries(prevHour);
		
		// run a prediction starting from the previous hour
		predict(prevSeries);
		
		
		///// run an update by using the current load
		///// as the target load
		
		// Create data source
		DataFeed feed = new DataFeed(loadSeries, daubSpec_);
		
		
		// Select bank (use the bank for the previous hour)
		int off = prevSeries.getCal().getMinute(prevSeries.getCurTime());
		ANNBank bank = annBanks_[off/5];
		
		// Prepare target output
		int numAnns = daubSpec_.getNumLevels()+1;
		Series[] targOut = new Series[numAnns];
		
		for (int i = 0; i < numAnns; ++i) {
			ANNSpec annSpec = annSpecs_[i];
			// Get the update block from ANN specification
			UpdateBlock updBlk = annSpec.getUpdateBlock(feed);
			
			// Create target outputs
			targOut[i] = updBlk.getUpdateInput();
		}
		
		bank.update(targOut);
	}
}
