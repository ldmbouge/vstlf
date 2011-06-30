package edu.uconn.vstlf.block;


import org.w3c.dom.Element;

import edu.uconn.vstlf.prediction.DataFeed;

public abstract class BlockSpec {	
	public static BlockSpec createFromElement(Element e) throws Exception
	{
		BlockType typ = BlockType.valueOf(e.getAttribute("type"));
		switch (typ)
		{
		case InputLoad: {
			int lvl = Integer.parseInt(e.getAttribute("lvl"));
			return new InputLoadSpec(lvl);
		}
		case OutputLoad: {
			int lvl = Integer.parseInt(e.getAttribute("lvl"));
			return new OutputLoadSpec(lvl);
		}
		case UpdateLoad: {
			int lvl = Integer.parseInt(e.getAttribute("lvl"));
			return new UpdateLoadSpec(lvl);
		}
		case SunsetIndex: {
			return new SunsetIndexSpec();
		}
		case WeekIndex: {
			return new WeekIndexSpec();
		}
		case MonthIndex: {
			return new MonthIndexSpec();
		}
		case HourIndex: {
			return new HourIndexSpec();
		}
		
		default:
			throw new Exception("Cannot create the block specification for type: " + typ);
		}
	}
	
	public abstract InputBlock getInputBlock(DataFeed feed) throws Exception;
	public abstract OutputBlock getOutputBlock(DataFeed feed) throws Exception;
	public abstract UpdateBlock getUpdateBlock(DataFeed feed) throws Exception;
}
