/**
 * 
 */
package edu.uconn.vstlf.test;

import org.junit.*;

import edu.uconn.vstlf.data.doubleprecision.Series;

/**
 * @author ldm
 *
 */
public class SeriesTest {
	@Test public void testAppend() {
		double[] a0 = {1,2,3,4,5};
		double[] a1 = {6,7,8,9,10};
		double[] a2 = {11,12,13,14,15};
		double[] a3 = {16,17,18,19,20};
		Series s1;
		try {
			s1 = new Series(a0);
			Series s2 = s1.append(new Series(a1)).append(new Series(a2)).append(new Series(a3));
			System.out.format("s2: %s\n",s2.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	@Test public void testAppend2() {
		double[] a0 = {1,2,3,4,5};
		double[] a1 = {6,7,8,9,10};
		double[] a2 = {11,12,13,14,15};
		double[] a3 = {16,17,18,19,20};
		Series s1;
		try {
			s1 = new Series(a0);
			Series s2 = s1.append(a1,a2,a3);
			System.out.format("s2: %s\n",s2.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}
}
