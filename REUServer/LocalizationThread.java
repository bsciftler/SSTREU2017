import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;



public class LocalizationThread implements Runnable
{
	int compute;
	Scanner input;
	int counter = 1;
	
	private boolean secure;
	Double[] location;
	EuclideanComputation comp;
	/*
	 * 	REU Variables
	 */
	PublicKey pk = new PublicKey();//Public Key
    SecureTriple transmission; //For Encrypted Paillier Transmission
    ArrayList<String> MACAddress; //For unencrypted transmission Part 1
    ArrayList<Integer> RSS;//For unencrpypted transmission Part 2

    /*
     *  Time
     */
    static long Billion = 1000000000;
    
    protected Socket incomingClient = null;
    protected String serverText   = null;

    public LocalizationThread(Socket clientSocket, String serverText)
    {
        this.incomingClient = clientSocket;
        this.serverText   = serverText;
    }

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

			//	Get the Paillier Key
			//	try
			//	{
			//		pk = (Paillier.PublicKey)fromClient.readObject();
			//	}
			//	catch(ClassNotFoundException cnf)
			//				{
			//	System.out.println("FAILURE GETTING PAILLIER KEY");
			//	cnf.printStackTrace();
			//}

			//If I dont have a MAC Address I probably have secure transmission!
			//Is it encrypted/unencrypted
			try
			{
				MACAddress = (ArrayList<String>)fromClient.readObject();
				RSS = (ArrayList<Integer>)fromClient.readObject();
				System.out.println("UNSECURE DATA RECEIVED");
				secure=false;
			}
			catch(ClassNotFoundException cnf)
			{
				//If I fail here, then I have big trouble!
				try 
				{
					transmission = (SecureTriple) fromClient.readObject();
					System.out.println("SECURE DATA RECEIVED");
					secure = true;					
				}
				catch (ClassNotFoundException cnfTwo)
				{
					cnfTwo.printStackTrace();
				}
			}
			/*
				 	After getting the data, send it to the Euclidean Computation Class
				  	Do Required Computations...see Euclidean Computation Class
			 */

			if (secure==true)
			{
				comp = new EuclideanComputation(transmission, pk);
			}
			else
			{
				comp = new EuclideanComputation(MACAddress,RSS);
			}
			location = comp.findCoordinate();

			//compute = compute*=2;

			/*
			 * 	Return Data to Client
			 */

			ObjectOutputStream responseToClient = new ObjectOutputStream(incomingClient.getOutputStream());
			responseToClient.writeObject(location);
			//PrintStream responseToClient = new PrintStream(incomingClient.getOutputStream());
			//responseToClient.println(compute); //Give back the X, Y Coordinate

			this.closeClientConnection();//Close Connection of Socket
			long estimatedTime = System.nanoTime() - startTime;
			
			System.out.println(counter + ": Computation completed from Client at: " + incomingClient.getInetAddress() 
			+ " and it took " + (estimatedTime/Billion) + "seconds");
			++counter;
		}
		catch(IOException IOE)
		{
			IOE.printStackTrace();
		}
    }
    
	public void closeClientConnection()
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