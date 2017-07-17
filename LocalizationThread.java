import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import edu.fiu.reu2017.SendPlainData;
import edu.fiu.reu2017.SendSecureTriple;
import edu.fiu.reu2017.SendTrainingArray;



public class LocalizationThread implements Runnable
{
	int compute;
	Scanner input;
	int counter = 1;
	private boolean isSecure;
	
	Double[] location;
	/*
	 * 	REU Variables
	 */
	PublicKey pk = new PublicKey();//Paillier Public Key (Pre-defined on Server)
	//Only for Testing purposes!
	PrivateKey sk = new PrivateKey(1024);//Paillier Pivate Key (Pre-defined on Server)
	
    SendSecureTriple transmission; //For Encrypted DGK/Paillier Transmission
    SendPlainData unsafeTransmission; //For PlainText Transmission
    SendTrainingArray trainDatabase; //For Training  Data

    /*
     *  Time
     */
    static long Billion = 1000000000;
    
    protected Socket incomingClient = null;
    protected String serverText   = null;
    
    //Possible Computations
	DistancePlain PlaintextLocalization;
	DistanceDGK DGKLocalization;
	DistancePaillier PallierLocalization;
	

    public LocalizationThread(Socket clientSocket, String serverText)
    {
        this.incomingClient = clientSocket;
        this.serverText   = serverText;
    }

    @SuppressWarnings("unchecked")
	public void run()
    {
		try
		{
			System.out.println("INCOMING REQUEST FROM " + incomingClient.getInetAddress()+ "!!");
			
			//input = new Scanner(incomingClient.getInputStream());//Read from the Client Socket
			//compute = input.nextInt();
		    long startTime = System.nanoTime();
			System.out.println("Localization Thread started running");
			ObjectInputStream fromClient= new ObjectInputStream(incomingClient.getInputStream());
			System.out.println("Object Input Stream Intialized!");
			//	Get the Paillier Key
			//	try
			//	{
			//		pk = (Paillier.PublicKey)fromClient.readObject();
			//	}
			//	catch(ClassNotFoundException cnf)
			//	{
			//	System.out.println("FAILURE GETTING PAILLIER KEY");
			//	cnf.printStackTrace();
			//	}

			Object x = fromClient.readObject();
			System.out.println("Object Read...");
			
			if (x instanceof SendSecureTriple)
			{
				transmission = (SendSecureTriple)x;
				System.out.println("SECURE DATA RECEIVED");
				isSecure=true;
			}
			else if(x instanceof SendPlainData)
			{
				unsafeTransmission = (SendPlainData) x;
				System.out.println("UNSECURE DATA RECEIVED");
				isSecure=false;
			}
			else if(x instanceof SendTrainingArray)
			{
				trainDatabase = (SendTrainingArray) x;
				System.out.println("TRAINING DATA RECEIVED");
				System.out.println("DATA BEING FORWARDED TO DATABASE...");
				//Paillier Public Key already predefined...
				BuildLookupTable train = new BuildLookupTable(new PublicKey());

				train.submitTrainingData(trainDatabase);

				System.out.println("Training Completed");
				boolean trainingSuccessful = true;
				ObjectOutputStream responseToClient = new ObjectOutputStream(incomingClient.getOutputStream());
				responseToClient.writeObject(trainingSuccessful);
				
				
				
				long estimatedTime = System.nanoTime() - startTime;

				System.out.println(counter + ": Training completed from Client at: " 
						+ incomingClient.getInetAddress() 
						+ " and it took " + estimatedTime + " nano-seconds");
				++counter;

				fromClient.close();
				responseToClient.close();
				return;//I hope this kills the thread
			}
			else if (x instanceof String)
			{
				String getFlags = (String) x;
				BuildLookupTable getMarkers = new BuildLookupTable(pk);
				System.out.println("Acquring all Trained Data Points");
				if (getFlags.equals("Acquire all current training points"))
				{
					getMarkers.getXY();//Get All X-Y Coordinates from Training Table
				}
				else
				{
					throw new IllegalArgumentException("INVALID OBJECT!");
				}
				ArrayList<Double> XCoordinates = getMarkers.Xmap;
				ArrayList<Double> YCoordinates = getMarkers.Ymap;
				
				ObjectOutputStream responseToClient = new ObjectOutputStream(incomingClient.getOutputStream());
				responseToClient.writeObject(XCoordinates);
				responseToClient.writeObject(YCoordinates);

				long estimatedTime = System.nanoTime() - startTime;
				
				System.out.println(counter + ": Flags retrived for Client at: " + incomingClient.getInetAddress() 
				+ " and it took " + estimatedTime + " nano-seconds");
				++counter;
				
				//Close I/O streams
				fromClient.close();
				responseToClient.close();
				//Close Socket
				this.closeClientConnection();
				return;
				
			}
			else
			{
				throw new IllegalArgumentException("INVALID OBJECT!");
			}
			
			/*
				 	After getting the data, send it to the Euclidean Computation Class
				  	Do Required Computations...see Euclidean Computation Class
			 */

			if (isSecure==true)
			{
				if (transmission.isPaillier==true)
				{
					PallierLocalization = new DistancePaillier(transmission, pk);
					location = PallierLocalization.findCoordinate();
				}
				else
				{
					DGKLocalization = new DistanceDGK(transmission);
					location = DGKLocalization.findCoordinate();
				}
			}
			else
			{
				PlaintextLocalization = new DistancePlain(unsafeTransmission.getMAC(),unsafeTransmission.getRSS());
				location = PlaintextLocalization.findCoordinate();
			}

			/*
			 * 	Return Data to Client
			 */

			ObjectOutputStream responseToClient = new ObjectOutputStream(incomingClient.getOutputStream());
			responseToClient.writeObject(location);
			
			long estimatedTime = System.nanoTime() - startTime;
			
			System.out.println(counter + ": Computation completed for Client at: " + incomingClient.getInetAddress() 
			+ " and it took " + estimatedTime + " nano-seconds");
			++counter;
			
			//Close I/O streams
			fromClient.close();
			responseToClient.close();
			//Close Socket
			this.closeClientConnection();//Close Connection of Socket
			return;
		}
		catch(IOException IOE)
		{
			IOE.printStackTrace();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
    }
    
	private void closeClientConnection()
	{
		if (incomingClient!=null)
		{
			if (incomingClient.isConnected())
            {
                try 
                {
					incomingClient.close();
				}
                catch (IOException ioe)
                {
                	ioe.printStackTrace();
				}
            }
		}
	}
}