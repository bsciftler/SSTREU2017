import java.math.BigInteger;
import java.util.HashMap;

public class DGKPublicKey
{
	// k1 is the security parameter. It is the number of bits in n.
    public int k1;
    public BigInteger n;
	public BigInteger g;
	public BigInteger h;
	public long u;
	public BigInteger bigU;
	public int l;
	public int t;
	public static int k;
	//public BigInteger u;
	public HashMap <Long, BigInteger> gLUT;
	public HashMap <Long, BigInteger> hLUT;
	
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
}