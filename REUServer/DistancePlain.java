import java.util.ArrayList;
import java.util.HashMap;


public class DistancePlain extends Distance
{
	private int [] distance;
	//Map Distance to Primary Key
	private HashMap<Integer, Integer> DistancetoPK = new HashMap<Integer, Integer>();
	
	
	public DistancePlain(ArrayList<String> MAC, ArrayList<Integer> RSSVal)
	{
		MACAddress = MAC;
		RSS = RSSVal;
		System.out.println("Finished Creating Euclidean Computation Object with Secure setting OFF");
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
		distance = new int [rows];
		findLocationwithPK=getLUTData.getLocationData();
	}
	
	
	protected void computeDistance()
	{
		/*
			
		 */
		if (MCA==true)
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
			//Debugging: Print Distance
			//Place into Hashmap...WATCHOUT FOR SAME DISTANCE!
			for (int i=0;i<rows;i++)
			{
				System.out.println("Distance "+ i + ": " + distance[i]);
				DistancetoPK.put(distance[i], (i+1));
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

	protected Double[] findCoordinate()
	{
		this.SQLAccess();
		this.computeDistance();			
		MyMergeSort dis = new MyMergeSort();
		dis.sort(distance);
		/*
		 * 	Did Merge Sort do its job?
		 */
		for(int i=0;i<rows;i++)
		{
		    System.out.println(distance[i]);
	    }	
		
		int PK = DistancetoPK.get(distance[0]);//Get Primary Key of leftmost value (min) after MergeSort is completed.
		//Using SQL, get the Hashtable matching <PrimaryKey, Location>
		location = findLocationwithPK.get(PK);
		return location;
	}
}
