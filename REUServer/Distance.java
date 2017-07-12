import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;


public abstract class Distance 
{
	protected final static int VECTORSIZE = 10;
	
	//Unsecure localization: Data from Phone
	protected ArrayList<String> MACAddress;
	protected ArrayList<Integer> RSS;
	
	//Insecure localization: Data from SQLServer: LookupTable
	protected ArrayList<Integer []> SQLData;
	//Secure localization: Data from SQLServer: LookupTable
	protected ArrayList<BigInteger []> S1Array;
	
	//Rows in Lookup Table
	protected static int rows;
	
	//Other variables variables
	protected final String QueryStart = "select * from ";
	protected final static String PLAINLUT = "REUPlainLUT";
	protected final static String SECRETLUT = "REUEncryptedLUT";
	protected boolean MCA=true;//Use Missed Constant Algorithm
	
	protected Double location [] = new Double [2];
	
	//Maps Primary Key to Location (from SQL Table)
	protected HashMap<Integer, Double []> findLocationwithPK;

	
	//Methods
	protected abstract void computeDistance();
	
	/*
	 * 	 I need both Vector and the First Primary Key...
	 * 	The Index will trace me back to the Correct XY Coordinate
	 */
	
	//Extract all the Values SQL Database LookupTable
	protected abstract void SQLAccess();
	protected abstract Double [] findCoordinate();
}
