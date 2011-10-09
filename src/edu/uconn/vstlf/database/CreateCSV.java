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
					output.write("err"+j*5+",");
				}
				output.write("err60\n");
				
				/*Arrays to hold the values in the prediction matrix - one for each column*/
				double[] v05 = new double[minLength];
				double[] v10 = new double[minLength];
				double[] v15 = new double[minLength];
				double[] v20 = new double[minLength];
				double[] v25 = new double[minLength];
				double[] v30 = new double[minLength];
				double[] v35 = new double[minLength];
				double[] v40 = new double[minLength];
				double[] v45 = new double[minLength];
				double[] v50 = new double[minLength];
				double[] v55 = new double[minLength];
				double[] v00 = new double[minLength];
				
				/*Vecotrs to store the computed error values*/
				Vector<Double> e05 = new Vector<Double>();
				Vector<Double> e10 = new Vector<Double>();
				Vector<Double> e15 = new Vector<Double>();
				Vector<Double> e20 = new Vector<Double>();
				Vector<Double> e25 = new Vector<Double>();
				Vector<Double> e30 = new Vector<Double>();
				Vector<Double> e35 = new Vector<Double>();
				Vector<Double> e40 = new Vector<Double>();
				Vector<Double> e45 = new Vector<Double>();
				Vector<Double> e50 = new Vector<Double>();
				Vector<Double> e55 = new Vector<Double>();
				Vector<Double> e60 = new Vector<Double>();
				
				DateFormat df = new SimpleDateFormat ("yyyy-MM-dd");
				//Date start_date = df.parse("2010-02-01");
				Date end_date   = df.parse("2010-02-01"); 

				j=1;				
				Date cur = (Date)st.clone();
				for(int i = 1; i <= minLength; i++){
					
					if(cur.equals(end_date)){
								System.out.println("End of date range.\n");
								break;}
					else {
						System.out.format("At: %s:",cur);
						
						/* Perform row selection */
						double loadAtb_0 = all12_pred[0].element(i);//This returns column zero in the big matrix
						v05[i] = loadAtb_0;
						v10[i] = all12_pred[1].element(i);
						v15[i] = all12_pred[2].element(i);
						v20[i] = all12_pred[3].element(i);
						v25[i] = all12_pred[4].element(i);
						v30[i] = all12_pred[5].element(i);
						v35[i] = all12_pred[6].element(i);
						v40[i] = all12_pred[7].element(i);
						v45[i] = all12_pred[8].element(i);
						v50[i] = all12_pred[9].element(i);
						v55[i] = all12_pred[10].element(i);
						v00[i] = all12_pred[11].element(i);
						
						/* Get actual load at time cur and compute error */
						double actual_read_5 = pdb.getLoad("raw", cal.addMinutesTo(cur, 5)); //actual load that was prediction 5 fives ago
				    	double err_5 = Math.abs((v05[i]-actual_read_5))/actual_read_5;    	
				    	double actual_read_10 = pdb.getLoad("raw", cal.addMinutesTo(cur, 10));
				    	double err_10 = Math.abs((v10[i]-actual_read_10))/actual_read_10;
				    	double actual_read_15 = pdb.getLoad("raw", cal.addMinutesTo(cur, 15)); 
				    	double err_15 = Math.abs((v15[i]-actual_read_15))/actual_read_15;
				    	double actual_read_20 = pdb.getLoad("raw", cal.addMinutesTo(cur, 20));
				    	double err_20 = Math.abs((v20[i]-actual_read_20))/actual_read_20;
				    	double actual_read_25 = pdb.getLoad("raw", cal.addMinutesTo(cur, 25)); 
				    	double err_25 = Math.abs((v25[i]-actual_read_25))/actual_read_25;
				    	double actual_read_30 = pdb.getLoad("raw", cal.addMinutesTo(cur, 30)); 
				    	double err_30 = Math.abs((v30[i]-actual_read_30))/actual_read_30;
				    	double actual_read_35 = pdb.getLoad("raw", cal.addMinutesTo(cur, 35));
				    	double err_35 = Math.abs((v35[i]-actual_read_35))/actual_read_35;
				    	double actual_read_40 = pdb.getLoad("raw", cal.addMinutesTo(cur, 40)); 
				    	double err_40 = Math.abs((v40[i]-actual_read_40))/actual_read_40;
				    	double actual_read_45 = pdb.getLoad("raw", cal.addMinutesTo(cur, 45));
				    	double err_45 = Math.abs((v45[i]-actual_read_45))/actual_read_45;
				    	double actual_read_50 = pdb.getLoad("raw", cal.addMinutesTo(cur, 50)); 
				    	double err_50 = Math.abs((v50[i]-actual_read_50))/actual_read_50;
				    	double actual_read_55 = pdb.getLoad("raw", cal.addMinutesTo(cur, 55)); 
				    	double err_55 = Math.abs((v55[i]-actual_read_55))/actual_read_55;
				    	double actual_read_60 = pdb.getLoad("raw", cal.addMinutesTo(cur, 60)); 
				    	double err_60 = Math.abs((v00[i]-actual_read_60))/actual_read_60;

				    	/* Add error to vector */
				    	e05.add(Double.valueOf(err_5));
				    	e10.add(Double.valueOf(err_10));
				    	e15.add(Double.valueOf(err_15));
				    	e20.add(Double.valueOf(err_20));
				    	e25.add(Double.valueOf(err_25));
				    	e30.add(Double.valueOf(err_30));
				    	e35.add(Double.valueOf(err_35));
				    	e40.add(Double.valueOf(err_40));
				    	e45.add(Double.valueOf(err_45));
				    	e50.add(Double.valueOf(err_50));
				    	e55.add(Double.valueOf(err_55));
				    	e60.add(Double.valueOf(err_60));
				    	
				    			
//				    	System.out.format("  %5.9f   %5.9f    %5.9f   %5.9f   %5.9f   %5.9f    %5.9f   %5.9f   %5.9f   %5.9f    %5.9f   %5.9f  "
//				    			, v05[i], v10[i], v15[i],v20[i],v25[i],v30[i],v35[i],v40[i],v45[i],v50[i],v55[i],v00[i]);
//				    	System.out.format("  %5.9f   %5.9f   %5.9f", loadAtb_0, actual_load, err);
					}
					cur = cal.addSecondsTo(cur, inc);
					System.out.format("\n");
				}
				/*Write out vectors to CSV file*/
				for(int z=0;z<e05.size();z++) {
					output.write(e05.elementAt(z)+",");
					output.write(e10.elementAt(z)+",");
					output.write(e15.elementAt(z)+",");
					output.write(e25.elementAt(z)+",");
					output.write(e25.elementAt(z)+",");
					output.write(e35.elementAt(z)+",");
					output.write(e35.elementAt(z)+",");
					output.write(e45.elementAt(z)+",");
					output.write(e45.elementAt(z)+",");
					output.write(e55.elementAt(z)+",");
					output.write(e55.elementAt(z)+",");
					output.write(e60.elementAt(z)+"");
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
