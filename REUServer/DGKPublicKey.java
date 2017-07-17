package edu.fiu.reu2017;

import java.math.BigInteger;
import java.util.HashMap;

public class DGKPublicKey
{
	public BigInteger n;
	public BigInteger g;
	public BigInteger h;
	public long u;
	public BigInteger bigU;
	public int l;
	public int t;
	public static int k;
	//public BigInteger u;
	public HashMap <Long, BigInteger> gLUT = new HashMap <Long, BigInteger> ();
	public HashMap <Long, BigInteger> hLUT = new HashMap <Long, BigInteger> ();

	public DGKPublicKey()
	{

	}

	public DGKPublicKey(BigInteger N, BigInteger G, BigInteger H, long U, 
			HashMap <Long,BigInteger> GLUT, HashMap<Long,BigInteger> HLUT, int L, int T, int K)
	{
		n=N;
		g=G;
		h=H;
		u=U;
		bigU=BigInteger.valueOf(u);
		gLUT=GLUT;
		hLUT=HLUT;
		l=L;
		t=T;
		k=K;
	}
	public HashMap<Long,BigInteger> getGLUT()
	{
		return gLUT;
	}
	public HashMap<Long,BigInteger> getHLUT()
	{
		return hLUT;
	}
	public BigInteger getN()
	{
		return n;
	}
	public Long getU()
	{
		return u;
	}

	public void printKeys()
	{
		System.out.println("Printing Public Key");
		System.out.println("Parameters: "  + " t: " + t + " l: " + l + " k: " + k);
		System.out.println("N: " + n);
		System.out.println("G: " + g);
		System.out.println("H: " + h);
		System.out.println("U: " + u);
	}

	public void printhLUT()
	{
		BigInteger [] encH = hLUT.values().toArray(new BigInteger[hLUT.size()]);
		Long [] plain = hLUT.keySet().toArray(new Long[hLUT.size()]);
		for (int i = 0; i<hLUT.size(); i++)
		{
			System.out.println(plain[i] + " " + encH[i]);
		}
	}

	public void printgLUT()
	{
		BigInteger [] encH = gLUT.values().toArray(new BigInteger[gLUT.size()]);
		Long [] plain = gLUT.keySet().toArray(new Long[gLUT.size()]);
		for (int i = 0; i<gLUT.size(); i++)
		{
			System.out.println(plain[i] + " " + encH[i]);
		}
	}
}