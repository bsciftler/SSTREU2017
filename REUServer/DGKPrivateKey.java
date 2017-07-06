import java.math.BigInteger;
import java.util.HashMap;

public class DGKPrivateKey
{
	private BigInteger p;
	private BigInteger q;
	private BigInteger vp;
	private BigInteger vq;
	private long u;
	HashMap <BigInteger, Long>LUT;
	
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
}