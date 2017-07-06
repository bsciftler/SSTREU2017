
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created by Andrew on 6/28/2017.
 */

public class SecureTriple
{
    private ArrayList<String> MACAddress;
    private BigInteger [] S2;
    private BigInteger [] S3;

    public SecureTriple(ArrayList<String> first, BigInteger [] second, BigInteger [] third)
    {
        MACAddress = first;
        S2 = second;
        S3 = third;
    }

    public ArrayList<String> getFirst() { return MACAddress; }
    public BigInteger [] getSecond() { return S2; }
    public BigInteger [] getThird() { return S3; }
}