import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

/*
Code for implementing Multi-THread Server is from:
http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html
 */
public class Server implements Runnable
{	
	protected int          serverPort = 9254;
	protected ServerSocket serverSocket = null;
	protected boolean      isStopped    = false;
	protected Thread       runningThread= null;

	public Server()
	{
		synchronized(this)
		{
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		while(! isStopped())
		{
			Socket clientSocket = null;
			try
			{
				clientSocket = this.serverSocket.accept();
			}
			catch (IOException e)
			{
				if(isStopped())
				{
					System.out.println("Server Stopped.") ;
					return;
				}
				throw new RuntimeException("Error accepting client connection", e);
			}
			new Thread(
					new LocalizationThread(
							clientSocket, "Multithreaded Server")
					).start();
		}//end While
		System.out.println("Server Stopped.") ;
	}

	public void run()
	{
		synchronized(this)
		{
			this.runningThread = Thread.currentThread();
		}
		openServerSocket();
		while(! isStopped())
		{
			Socket clientSocket = null;
			try
			{
				clientSocket = this.serverSocket.accept();
			}
			catch (IOException e)
			{
				if(isStopped())
				{
					System.out.println("Server Stopped.") ;
					return;
				}
				throw new RuntimeException(
						"Error accepting client connection", e);
			}
			new Thread(
					new WorkerRunnable(
							clientSocket, "Multithreaded Server")
					).start();
		}
		System.out.println("Server Stopped.") ;
	}

	private synchronized boolean isStopped()
	{
		return this.isStopped;
	}

	public synchronized void stop()
	{
		this.isStopped = true;
		try
		{
			this.serverSocket.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Error closing server", e);
		}
	}

	private void openServerSocket()
	{
		try
		{
			this.serverSocket = new ServerSocket(this.serverPort);
		}
		catch (IOException e)
		{
			throw new RuntimeException("Cannot open port " + this.serverPort, e);
		}
	}


	public static void main(String args [])
	{
		Server Localizationserver = new Server();
		new Thread(Localizationserver).start();
		try 
		{
		    Thread.sleep(20 * 1000);
		}
		catch (InterruptedException e)
		{
		    e.printStackTrace();
		}
		System.out.println("Stopping Server");
		Localizationserver.stop();


//		MultiThreadedServer server = new MultiThreadedServer(9254);
//		new Thread(server).start();
//	
//		try
//		{
//		    Thread.sleep(20 * 1000);
//		}
//		catch (InterruptedException e)
//		{
//		    e.printStackTrace();
//		}
//		
//		System.out.println("Stopping Server");
//		server.stop();
	}
}