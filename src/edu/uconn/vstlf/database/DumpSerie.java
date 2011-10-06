package edu.uconn.vstlf.database;

import java.util.Date;
import java.util.logging.Level;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.database.perst.PerstPowerDB;

public class DumpSerie implements Command {

	@Override
	public void execute(String[] args) {
		if (args.length!=4) {
			MessageCenter.getInstance().put(
					new LogMessage(Level.WARNING, "DumpSerie",
							"execute", "invalid # of arguments. Check usage\n"));
			return;
		}
		String fname = args[1];
		String sname = args[2]; 
		int inc = Integer.parseInt(args[3]);
		Calendar cal = new Calendar();
		Date st,ed;    
		PowerDB pdb = new PerstPowerDB(fname,inc);
		pdb.open();
		
		try {
			if (sname.startsWith("pred")) {
				st = pdb.first(sname);
				ed = pdb.last(sname);
				Series[] all12 = new Series[12];
				int minLength = 0x7FFFFFFF; // figure out shortest one (they should all have the same length though...)
				for(int i=0;i<12;i++) {
					all12[i] = pdb.getForecast(sname, st, ed,i);
					minLength = minLength < all12[i].length() ? minLength : all12[i].length();
					MessageCenter.getInstance().put(
							new LogMessage(Level.INFO, "DumpSerie",
									"execute","Vector has " + all12[i].length() + " entries\n"));
				}
				Date cur = (Date)st.clone();
				for(int i = 1;i<=minLength;i++){
					System.out.format("At: %s 5m vector (5-60m):",cur);
					for(int b = 0 ; b < 12 ; b++) {
						double loadAtb = all12[b].element(i);
						System.out.format("  %5.3f ",	loadAtb);
					}
					cur = cal.addSecondsTo(cur, inc);
					System.out.format("\n");
				}
			} else {
				st = pdb.first(sname);
				ed = pdb.last(sname);
				Series load = pdb.getLoad(sname,st, ed);
				MessageCenter.getInstance().put(
						new LogMessage(Level.INFO, "DumpSerie",
								"execute","Vector has " + load.length() + " entries\n"));
				pdb.close();
				Date cur = (Date)st.clone();
				for(int i = 1;i<=load.length();i++){				
					double cl = load.element(i);
					MessageCenter.getInstance().put(
							new LogMessage(Level.INFO, "DumpSerie",
									"execute", cur +" - load = " + cl + "\n"));
					cur = cal.addSecondsTo(cur, inc);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
