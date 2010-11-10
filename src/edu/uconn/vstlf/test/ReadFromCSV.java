package edu.uconn.vstlf.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

public class ReadFromCSV {
	public double[][] Read(String filename, int row, int col) throws Exception
	{
		File file = new File(filename);
		BufferedReader bfrRdr = new BufferedReader(new FileReader(file));
		
		double[][] data = new double[row][col];
		for (int r = 0; r < row; ++r) {
			String line = bfrRdr.readLine();
			StringTokenizer st = new StringTokenizer(line, ",");
			for (int c = 0; c < col; ++c)
				data[r][c] = Double.parseDouble(st.nextToken());
		}
			
		return data;
	}
	
	public static void main(String[] args) throws Exception
	{
		String filename = "doc/source/MatlabCodes/output.csv";
		int row = 12, col = 1;
		double d[][] = new ReadFromCSV().Read(filename, row, col);
		System.out.println(d[0][0]);
	}
}
