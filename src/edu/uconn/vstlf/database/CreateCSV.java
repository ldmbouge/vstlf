package edu.uconn.vstlf.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;

import edu.uconn.vstlf.data.Calendar;
import edu.uconn.vstlf.data.doubleprecision.Series;
import edu.uconn.vstlf.data.message.LogMessage;
import edu.uconn.vstlf.data.message.MessageCenter;
import edu.uconn.vstlf.database.perst.PerstPowerDB;

public class CreateCSV implements Command {

	@Override
	public void execute(String[] args) {
		if (args.length!=5) {
			MessageCenter.getInstance().put(
					new LogMessage(Level.WARNING, "CreateCSV",
							"execute", "invalid # of arguments. Check usage\n"));
			return;
		}
		String fname = args[1];
		String sname = args[2]; 
		int inc = Integer.parseInt(args[3]);
		String csvFile = args[4];
		
		Date st,ed;
		Calendar cal = new Calendar();
		PowerDB pdb = new PerstPowerDB(fname,inc);
		pdb.open();

		try {
			if (sname.startsWith("pred")) {
				st = pdb.first(sname);
				ed = pdb.last(sname);
				int minLength = 0x7FFFFFFF; 		// figure out shortest one (they should all have the same length though...)
				Series[] all12_pred = new Series[12];	
		
				for(int i=0;i<12;i++) {
					all12_pred[i] = pdb.getForecast(sname, st, ed,i);
					minLength = minLength < all12_pred[i].length() ? minLength : all12_pred[i].length();
						MessageCenter.getInstance().put(
								new LogMessage(Level.INFO, "DumpSerie",
										"execute","Vector has " + all12_pred[i].length() + " entries\n"));
				}
	
				/* Prepare the header of the CSV file */
				File file = new File(csvFile);
				Writer output = new BufferedWriter(new FileWriter(file));
				int s,j;
				int COLUMNS=12;
				for(j=1;j<COLUMNS;++j) { 
					output.write("err"+j*5+",\t\t\t");
				}
				output.write("err00\n");
	
				/*Vector of percentage errors - one for each 5 minute interval*/
				Vector<Double> v00 = new Vector<Double>();
				Vector<Double> v05 = new Vector<Double>();
				Vector<Double> v10 = new Vector<Double>();
				Vector<Double> v15 = new Vector<Double>();
				Vector<Double> v20 = new Vector<Double>();
				Vector<Double> v25 = new Vector<Double>();
				Vector<Double> v30 = new Vector<Double>();
				Vector<Double> v35 = new Vector<Double>();
				Vector<Double> v40 = new Vector<Double>();
				Vector<Double> v45 = new Vector<Double>();
				Vector<Double> v50 = new Vector<Double>();
				Vector<Double> v55 = new Vector<Double>();
			
				DateFormat df = new SimpleDateFormat ("yyyy-MM-dd");
				//Date start_date = df.parse("2010-02-01");
				Date end_date   = df.parse("2010-01-02"); 

				j=1;
				
				Date cur = (Date)st.clone();
				for(int i = 1; i <= minLength; i++){
					
					if(cur.equals(end_date)){
								System.out.println("End of date range.\n");
								break;}
					else {
						//System.out.format("At: %s:",cur);
						double loadAtb = all12_pred[0].element(i);//This returns column zero in the big matrix
						/*
						 * Select the 0th row from the big matrix and throw that into a vector
						 * repeat this 
						 */
						double actual_load = pdb.getLoad("raw", cal.addSecondsTo(cur, inc)); //actual load that was prediction 5 fives ago
				    	double err = Math.abs((loadAtb-actual_load))/actual_load;

				    	System.out.format("  %5.9f   %5.9f   %5.9f", loadAtb, actual_load, err);

				    	/*Spread the values across 12 vectors, one for each offset 00,05,10,15...*/
						//System.out.print("\t iterator mod 12: "+s);

				    	v00.add(Double.valueOf(err));

					}
					cur = cal.addSecondsTo(cur, inc);
					System.out.format("\n");
				}
				/*Write out vectors to CSV file*/
				for(int z=0;z<v00.size();z++) {
					output.write(v05.get(z)+",\t");
					output.write(v10.get(z)+",\t");
					output.write(v15.get(z)+",\t");
					output.write(v20.get(z)+",\t");
					output.write(v25.get(z)+",\t");
					output.write(v30.get(z)+",\t");
					output.write(v35.get(z)+",\t");
					output.write(v40.get(z)+",\t");
					output.write(v45.get(z)+",\t");
					output.write(v50.get(z)+",\t");
					output.write(v55.get(z)+",\t");
					output.write(v00.get(z)+"");
					output.write("\n");
				}	
				//Prints a single vector
//				for(int z=0;z<v35.size();z++){
//					System.out.format("v35[%d]: \t%f\n",z,v35.get(z));
//				}
				
				if(pdb!=null)		pdb.close();
				if(output!=null)	output.close();	
	
			} else {}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
