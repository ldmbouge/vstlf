package edu.uconn.vstlf.data.message;

import java.util.logging.Level;

public class LogMessage extends VSTLFMessage {

	public LogMessage(Level level, String sourceClass,
			String sourceMethod, String msg)
	{
		super(VSTLFMessage.Type.Log);
		level_ = level;
		sourceClass_ = sourceClass;
		sourceMethod_ = sourceMethod;
		msg_ = msg;
	}
	
	public Level getLevel() { return level_; }
	public String getSrcClass() { return sourceClass_; }
	public String getSrcMethod() { return sourceMethod_; }
	public String getMsg() { return msg_; }
	
	private Level level_;
	private String sourceClass_;
	private String sourceMethod_;
	private String msg_;
}
