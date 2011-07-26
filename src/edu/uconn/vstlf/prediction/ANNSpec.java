package edu.uconn.vstlf.prediction;

import java.util.Vector;

import edu.uconn.vstlf.block.BlockSpec;
import edu.uconn.vstlf.block.InputBlock;
//import edu.uconn.vstlf.block.InputLoadSpec;
import edu.uconn.vstlf.block.OutputBlock;
//import edu.uconn.vstlf.block.OutputLoadSpec;
import edu.uconn.vstlf.block.UpdateBlock;
import edu.uconn.vstlf.block.UpdateLoadSpec;

public class ANNSpec {

	private Vector<BlockSpec> inputSpecs_;
	private BlockSpec outputSpecs_;
	private BlockSpec updateSpecs_;
	private int[] lyrSz_;
	
	private int trainSecs_;
	
	//private InputLoadSpec inLoadSpec_;
	//private OutputLoadSpec outLoadSpec_;
	private UpdateLoadSpec updLoadSpec_;
	
	public int[] getLayerSize() { return lyrSz_; }
	
	public ANNSpec(Vector<BlockSpec> inputSpecs,
			BlockSpec outputSpecs, BlockSpec updateSpecs,
			int[] lyrSz, int trainSecs)
	{
		inputSpecs_ = inputSpecs;
		outputSpecs_ = outputSpecs;
		updateSpecs_ = updateSpecs;
		lyrSz_ = lyrSz;
		trainSecs_ = trainSecs;
		
		/*
		for (int i = 0; i < inputSpecs.size(); ++i)
			if ((InputLoadSpec)(inputSpecs.get(i)) != null) {
				inLoadSpec_ = (InputLoadSpec)(inputSpecs.get(i));
				break;
			}
		
		outLoadSpec_ = (OutputLoadSpec)(outputSpecs);
		*/
		updLoadSpec_ = (UpdateLoadSpec)(updateSpecs);
	}
	
	public Vector<InputBlock> getInputBlocks(DataFeed feed) throws Exception
	{
		Vector<InputBlock> blks = new Vector<InputBlock>();
		for (BlockSpec inSpec : inputSpecs_)
			blks.add(inSpec.getInputBlock(feed));
		return blks;
	}
	
	public OutputBlock getOutputBlock(DataFeed feed) throws Exception
	{
		return outputSpecs_.getOutputBlock(feed);
	}
	
	public int getInputHours()
	{
		return updLoadSpec_.getInputHours();
	}
	
	public int getOutputHours()
	{
		return updLoadSpec_.getOutputHours();
	}
	
	public UpdateBlock getUpdateBlock(DataFeed feed) throws Exception
	{
		return updateSpecs_.getUpdateBlock(feed);
	}
	
	public int getTrainSecs() 
	{
		return trainSecs_;
	}
}
