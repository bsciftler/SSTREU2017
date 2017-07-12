import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

import edu.fiu.reu2017.DGKPrivateKey;
import edu.fiu.reu2017.DGKPublicKey;
import edu.fiu.reu2017.SendSecureTriple;


public class DistanceDGK extends Distance
{
	DGKPublicKey DGKpk;
	DGKPrivateKey DGKsk;
	SendSecureTriple incoming;
	
	//Secure localization: Data from Phone
	BigInteger [] S2;
	BigInteger [] S3;
	
	//Store the computed distances...
	BigInteger [] encryptedDistance;
	
	//List of S1 from SQLServer
	ArrayList<BigInteger []> S1Array = new ArrayList<BigInteger []>();
		
	
	//Maps DGK/Paillier Values to Primary Key
	protected HashMap<BigInteger, Integer> EncDistanceKey = new HashMap<BigInteger, Integer>();
	
	public DistanceDGK(SendSecureTriple in)
	{
		incoming = in;
		MACAddress = incoming.getFirst();
		S2= incoming.getSecond();
		S3= incoming.getThird();
		DGKpk = incoming.getDGKPublicKey();
		DGKsk = incoming.getDGKPrivateKey();
		System.out.println("Localization with DGK sucessfully created!");
	}
	
	protected void SQLAccess()
	{
		String cmd = QueryStart + PLAINLUT + ";";
		SQLRead getLUTData = new SQLRead(cmd, false);
		getLUTData.execute();
		SQLData = getLUTData.getRSS();
		rows = SQLData.size();
		//Confirm Tuple Matches Database...
		/*
		for (int i = 0; i < SQLData.size();i++)
		{
			for (int j=0;j<VECTORSIZE;j++)
			{
				System.out.println(SQLData.get(i)[j]);
			}
		}*/
	
		System.out.println("Number of Rows: " + rows);	
		encryptedDistance = new BigInteger [rows];
		findLocationwithPK=getLUTData.getLocationData();
		
		//Build S1 Array using DGK Key...
		long temp;
		for (int i = 0; i < rows; i++)
		{
			BigInteger [] tempArr = new BigInteger[VECTORSIZE];
			for (int j = 0;j<VECTORSIZE;j++)
			{
				temp = SQLData.get(i)[j] * SQLData.get(i)[j];
				tempArr[j] = DGKOperations.encrypt(DGKpk, temp);
			}
			S1Array.add(tempArr);
		}
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
						temp = DGKOperations.DGKMultiply(DGKpk, S2[j], SQLData.get(i)[j]);//S2 = -2v'*v
						temp = DGKOperations.DGKAdd(DGKpk, temp, S1Array.get(i)[j]);//S1 +  S2
						temp = DGKOperations.DGKAdd(DGKpk, temp, S3[j]);//Add the S3
						if (temp.equals(BigInteger.ZERO))
						{
							System.out.println("What happens if I localize right on a training spot?");
						}
					}
					//Keep adding more the other values
					temp = DGKOperations.DGKAdd(DGKpk, temp, DGKOperations.DGKMultiply(DGKpk, S2[j], SQLData.get(i)[j]));
					temp = DGKOperations.DGKAdd(DGKpk, temp, S1Array.get(i)[j]);
					temp = DGKOperations.DGKAdd(DGKpk, temp, S3[j]);
				}
				encryptedDistance[i] = temp;
				//Reset temp
				temp = BigInteger.ZERO;
			}
			//Fill up Hashmap...DGK does not support Re-randomization...
			for (int i = 0; i < rows; i++)
			{
				EncDistanceKey.put(encryptedDistance[i],(i+1));
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
