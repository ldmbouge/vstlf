package edu.uconn.vstlf.database;

import edu.uconn.vstlf.database.perst.PerstPowerDB;

public class GetInfo implements Command {
	@Override
	public void execute(String[] args) {	
		if (args.length!=2) {
			System.err.println("invalid # of arguments. Check usage\n");
			return;
		}
		String name = args[1];
		PowerDB pdb = new PerstPowerDB(name,0);
		pdb.open();
		System.out.println(pdb.getInfo());
	}

}
