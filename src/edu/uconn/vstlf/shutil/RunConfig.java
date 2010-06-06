package edu.uconn.vstlf.shutil;

import edu.uconn.vstlf.config.Items;

public class RunConfig{

	public static void main(String[] args){
		if(args.length == 0){
			System.out.println("USAGE:\n\tjava -jar uconn-vstlf.jar config [key=value] [key=value] [...]");
			System.out.println("\nYou can set values for any of the following keys:\n");
			for(Items item : Items.values())
				System.out.format("\t%s\t\t\t(currently %s=%s)\n\n",
								  item.key(),
								  item.key(),
								  Items.get(item));
		}
		else{
			for(int i = 0; i < args.length; i++){
			boolean used = false;
			for(Items item : Items.values()){
				String key = item.key()+'=';
				if(args[i].startsWith(key)){
					String val = args[i].substring(key.length());
					Items.put(item,val);
					used = true;
					break;
				}
			}
			if(!used)
				System.out.format("Skipped invalid argument: %s",args[i]);
			}
		}
		try{
			Items.save(Items.file());
		}
		catch(Exception ex){
			System.out.println("WARNING! Could not overwrite '"+Items.file()+"'. \n" + ex.getMessage());
		}
	}
}