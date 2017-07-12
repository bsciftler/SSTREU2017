import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import edu.fiu.reu2017.SendSecureTriple;


public class DistancePaillier extends Distance
{
	//Secure localization: Data from SQLServer: LookupTable
	protected ArrayList<BigInteger []> S1Array;
	
	
	//Secure localization: Data from Phone
	private BigInteger [] S2;
	private	BigInteger [] S3;
	
	//Paillier Keys
	private PublicKey pk;
	private PrivateKey sk = new PrivateKey(1024);
	
	//Store the computed distances...
	BigInteger [] encryptedDistance;
	
	//Maps DGK/Paillier Values to Primary Key
	protected HashMap<BigInteger, Integer> EncDistanceKey = new HashMap<BigInteger, Integer>();
	
	public DistancePaillier(SendSecureTriple input, PublicKey n)
	{
		pk = n;
		//Each index will have:
		//1- MAC Address
		//2- S2
		//3- S3
		MACAddress = input.getFirst();
		S2 = input.getSecond();
		S3 = input.getThird();
		System.out.println("Finished Creating Euclidean Computation Object with Paillier input objects");
	}
	
	protected void SQLAccess()
	{
		String cmd = QueryStart + SECRETLUT + ";";
		SQLRead getLUTData = new SQLRead(cmd, true);
		getLUTData.execute();
		S1Array = getLUTData.getS1();
		SQLData = getLUTData.getRSS();
		rows = S1Array.size();
		encryptedDistance = new BigInteger [rows];
		findLocationwithPK=getLUTData.getLocationData();
	}
	
	protected void computeDistance()
	{
		if (MCA==true)//If null use RSS = -120 encrypted in Paillier
		{
			BigInteger temp = BigInteger.ZERO;
			for (int i=0;i<rows;i++)
			{
				for (int j = 0; j < VECTORSIZE;j++)
				{
					if (temp.equals(BigInteger.ZERO))
					{
						//Create the temp
						temp = Paillier.multiply(S2[j], SQLData.get(i)[j], pk);//S2 = -2v'*v
						temp = Paillier.add(temp, S1Array.get(i)[j], pk);//S1 +  S2
						temp = Paillier.add(temp, S3[j], pk);//Add the S3
						if (temp.equals(BigInteger.ZERO))
						{
							System.out.println("What happens if I localize right on a training spot?");
						}
					}
					//Keep adding more the other values
					temp = Paillier.add(temp, Paillier.multiply(S2[j], SQLData.get(i)[j], pk), pk);
					temp = Paillier.add(temp, S1Array.get(i)[j], pk);
					temp = Paillier.add(temp, S3[j], pk);
				}
				encryptedDistance[i] = temp;
				//Reset temp
				temp = BigInteger.ZERO;
			}
			//Fill up Hashmap...The Rerandomize should secure no two same keys to occur...
			for (int i = 0; i < rows; i++)
			{
				//I am re-randomizing to minimize risk of a repeated key
				EncDistanceKey.put(Paillier.reRandomize(encryptedDistance[i], pk),(i+1));
			}
		}
		else
		{
			/*
			 * 	To know which parts to "Skip" USE THE MAC ADDRESS TABLE SENT! 
			 * 	It either has a MAC or is NULL! If NULL, SKIP!
			 */
		}
	}

	protected Double[] findCoordinate()
	{
		this.SQLAccess();
		this.computeDistance();
		//Find the min
	
		
		//Getting min...
		int PK = EncDistanceKey.get(BigInteger.ZERO);//FIX IT...
		location = findLocationwithPK.get(PK);
		return location;
	}
}
