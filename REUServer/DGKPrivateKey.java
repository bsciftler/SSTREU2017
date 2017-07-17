package edu.fiu.reu2017;

import java.math.BigInteger;
import java.util.HashMap;

public class DGKPrivateKey
{
	private BigInteger p;
	private BigInteger q;
	private BigInteger vp;
	private BigInteger vq;
	private long u;
	private HashMap <BigInteger, Long> LUT = new HashMap <BigInteger, Long>();

	public DGKPrivateKey()
	{

	}

	public DGKPrivateKey (BigInteger P, BigInteger Q, BigInteger VP, 
			BigInteger VQ, HashMap <BigInteger, Long> lut, long U)
	{
		p=P;
		q=Q;
		vp=VP;
		vq=VQ;
		LUT=lut;
		u=U;
	}

	/*
    Set Methods
	 */
	 public void setP(BigInteger P) { p = P;}
	 public void setQ(BigInteger Q) { q = Q;}
	 public void setVP(BigInteger VP) { vp = VP;}
	 public void setVQ(BigInteger VQ) { vq = VQ;}
	 public void setU(long U){ u = U;}
	 public void setLUT(HashMap<BigInteger, Long> look) {LUT = look;}
	 /*
    Get Methods
	  */

	 public BigInteger GetP()
	 {
		 return p;
	 }
	 public BigInteger GetQ()
	 {
		 return q;
	 }
	 public BigInteger GetVP()
	 {
		 return vp;
	 }
	 public BigInteger GetVQ()
	 {
		 return vq;
	 }
	 public HashMap<BigInteger,Long> GetLUT()
	 {
		 return LUT;
	 }
	 public long GetU()
	 {
		 return u;
	 }

	 public void printKeys()
	 {
		 System.out.println("Private Key Values:");
		 System.out.println("P: " + p);
		 System.out.println("Q: " + q);
		 System.out.println("VP: " + vp);
		 System.out.println("VQ: " + vq);
		 System.out.println("U: " + u);
	 }

	 public void printLUT()
	 {
		 BigInteger [] cipher = LUT.keySet().toArray(new BigInteger[LUT.size()]);
		 Long [] plain = LUT.values().toArray(new Long[LUT.size()]);
		 for (int i = 0; i<LUT.size(); i++)
		 {
			 System.out.println(cipher [i] + "," + plain[i]);
		 }
		 //This was just to ensure that every cipher text was mapped to a Plaintext value
		 //    	MyMergeSort testP = new MyMergeSort();
		 //    	int [] test = new int [LUT.size()];
		 //    	for (int i = 0; i < LUT.size(); i++)
		 //    	{
		 //    		test [i] = plain[i].intValue();
		 //    	}
		 //    	testP.sort(test);
		 //    	for (int i = 0; i < LUT.size(); i++)
		 //    	{
		 //    		System.out.println(test[i]);
		 //    	}
	 }
}