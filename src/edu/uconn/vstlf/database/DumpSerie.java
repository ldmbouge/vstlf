package edu.uconn.vstlf.database;

import java.util.Date;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.database.perst.PerstPowerDB;

public class DumpSerie implements Command {

	@Override
	public void execute(String[] args) {
		if (args.length!=4) {
			System.err.println("invalid # of arguments. Check usage\n");
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
			st = pdb.first(sname);
			ed = pdb.last(sname);
			Series load = pdb.getLoad(sname,st, ed);
			System.out.format("Vector has %d entries\n",load.length());
			pdb.close();
			Date cur = (Date)st.clone();
			for(int i = 1;i<=load.length();i++){				
				double cl = load.element(i);
				System.out.format("%s - load = %f\n", cur,cl);
				cur = cal.addSecondsTo(cur, inc);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
