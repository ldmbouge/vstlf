package edu.uconn.vstlf.prediction;

public class DaubSpec {
	double _rootThree = Math.sqrt(3);
    double _fourRootTwo = 4*Math.sqrt(2);   
    double[] _db4LD = {(1 + _rootThree)/_fourRootTwo, (3 + _rootThree)/_fourRootTwo,
                      (3 - _rootThree)/_fourRootTwo, (1 - _rootThree)/_fourRootTwo};
    double[] _db4HD = {_db4LD[3], -_db4LD[2], _db4LD[1], -_db4LD[0]};
    double[] _db4LR = {_db4LD[3], _db4LD[2], _db4LD[1], _db4LD[0]};
    double[] _db4HR = {_db4HD[3], _db4HD[2], _db4HD[1], _db4HD[0]};

    private int nlvls_;
    
    public DaubSpec(int nLvls)
    {
    	nlvls_ = nLvls;
    }
    
    public int getNumLevels() { return nlvls_; }
    
	public double[] getDB4LD() { return _db4LD; }
	public double[] getDB4HD() { return _db4HD; }
	public double[] getDB4LR() { return _db4LR; }
	public double[] getDB4HR() { return _db4HR; }
}
