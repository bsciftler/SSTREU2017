import java.math.BigInteger;
import java.util.HashMap;


public class DistanceDGK extends Distance
{
	//Secure localization: Data from Phone
	BigInteger [] S2;
	BigInteger [] S3;
	
	//Store the computed distances...
	BigInteger [] encryptedDistance;
		
	//Maps DGK/Paillier Values to Primary Key
	protected HashMap<BigInteger, Integer> EncDistanceKey = new HashMap<BigInteger, Integer>();
	
	protected void SQLAccess()
	{
		
	}
	
	protected void computeDistance()
	{

	}

	protected Double[] findCoordinate()
	{
		return location;
	}
}
