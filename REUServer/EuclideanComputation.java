import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class EuclideanComputation
{
	private final static int VECTORSIZE = 10;

	//Unsecure localization: Data from Phone
	ArrayList<String> MACAddress;
	ArrayList<Integer> RSS;
	
	//Insecure localization: Data from SQLServer: LookupTable
	ArrayList<Integer []> SQLData;
	//Secure localization: Data from SQLServer: LookupTable
	ArrayList<BigInteger []> S1Array;
	
	//Secure localization: Data from Phone
	BigInteger [] S2;
	BigInteger [] S3;
	
	//Paillier Public Key
	PublicKey pk;
	
	//Store the computed distances...
	BigInteger [] encryptedDistance;
	int [] distance;
	static int rows;
	
	private boolean secureCompute;
	private boolean DMA=true;//Use Dynamic Matching Algorithm

	HashMap<BigInteger, Integer> MaptoPK = new HashMap<BigInteger, Integer>();
	Double location [] = new Double [2];
	
	//Maps Paillier Value to Primary Key
	HashMap<BigInteger, Integer> EncDistanceKey = new HashMap<BigInteger, Integer>();
	//Maps Primary Key to Location (from SQL Table)
	HashMap<Integer, Double []> findLocationwithPK;
	
	public EuclideanComputation(ArrayList<String> MAC, ArrayList<Integer> RSSVal)
	{
		MACAddress = MAC;
		RSS = RSSVal;
		secureCompute = false;
		System.out.println("Finished Creating Euclidean Computation Object with Secure setting OFF");
	}
	
	public EuclideanComputation(SecureTriple input, PublicKey n)
	{
		pk = n;
		//Each index will have:
		//1- MAC Address
		//2- S2
		//3- S3
		MACAddress = input.getFirst();
		S2 = input.getSecond();
		S3 = input.getThird();
		secureCompute = true;
		System.out.println("Finished Creating Euclidean Computation Object with Secure setting ON");
	}
	
//================================PlainText==============================================	
	//Extract all the Values SQL Database LookupTable

	private void SQLAccess()
	{
		/*
		 * 	 I need both Vector and the First Primary Key...
		 * 	The Index will trace me back to the Correct XY Coordinate
		 */
		final String QueryStart = "select * from ";
		if (secureCompute == true)
		{
			String cmd = QueryStart + "reuplainlut;";
			SQLRead getLUTData = new SQLRead(cmd,secureCompute);
			getLUTData.execute();
			S1Array = getLUTData.getS1();
			SQLData = getLUTData.getRSS();
			rows = S1Array.size();
			distance = new int [rows];
			findLocationwithPK=getLUTData.getLocationData();
		}
		else
		{
			String cmd = QueryStart + "reuencryptedlut;";
			SQLRead getLUTData = new SQLRead(cmd, secureCompute);
			getLUTData.execute();
			SQLData = getLUTData.getRSS();
			rows = SQLData.size();
			distance = new int [rows];
			findLocationwithPK=getLUTData.getLocationData();
		}
	}
	

	//Find the smallest distance using merge sort, since it has the X, Y
	public Double [] findCoordinate()
	{
		if (secureCompute==true)
		{
			this.SQLAccess();
			this.EncryptedcomputeDistance();
			//Find the min
			for (int i = 0; i < rows; i++)
			{
				//I am re-randomizing to minimize risk of a repeated key
				MaptoPK.put(Paillier.reRandomize(encryptedDistance[i], pk),(i+1));
			}
			//PlaceHolder...Paillier Comparison
			
			//Gettting min...
			int PK = MaptoPK.get(BigInteger.ZERO);//FIX IT...
			location = findLocationwithPK.get(PK);
		}
		else
		{
			this.SQLAccess();
			this.computeDistance();			//Find the Min
			int [][] sort = new int [distance.length][2];//Distance and Primary Key Pair...
			for (int i=0;i<distance.length;i++)
			{
				sort [i][0] = distance[i];
				sort [i][1] = (i+1);
			}
			MergeSort mms = new MergeSort();
			mms.sort(sort);
			int PK = sort[0][1];//Get Primary Key of leftmost value (min) after MergeSort is completed.
			//Using SQL, get the Hashtable matching <PrimaryKey, Location>
			location = findLocationwithPK.get(PK);
		}
		
		//Return that to Client Thread
		return location;
	}
//========================================Plain Text Computation===============================	
	//DMA Algorithm (Compute Distance Part 1)
	private void computeDistance()
	{
		/*
			
		 */
		if (DMA==true)
		{
			for (int i=0;i<rows;i++)
			{
				for (int j = 0; j < VECTORSIZE;j++)
				{
					//(x_1 - x_0)^2
					distance[i] += (SQLData.get(i)[j]-RSS.get(j))*(SQLData.get(i)[j]-RSS.get(j));
				}
				//Use MultiMap as I can MAYBE have repeated distances?
				//Could happen if this location has 
				//-120, ..., -120?
			}
		}
		/*
		 	Otherwise, use the Dynamic Matching Algorithm...
		 	Instead of computing all 10 columns and setting misses as RSS = -120
		 	I will skip computing the column if the MAC Address does NOT exist.
		 */
		else
		{
			
		}
	}
	
//=====================Encrypted Paillier================================================
	//DMA Algorithm (Compute Distance Part 1)
	private void EncryptedcomputeDistance()
	{
		if (DMA==true)//If null use RSS = -120 encrypted in Paillier
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
		}
		else
		{
			/*
			 * 	To know which parts to "Skip" USE THE MAC ADDRESS TABLE SENT! 
			 * 	It either has a MAC or is NULL! If NULL, SKIP!
			 */
		}
	}
}