import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Scanner;

public class Generation
{
	public static void main(String[] args)
	{
		PrivateKey sk = new PrivateKey(1024);
		PublicKey pk = new PublicKey();
		Paillier.keyGen(sk,pk);


		/*
		 * 	Note: I already did RSSSquared in PlainText as well.
		 * 	But I did it through using Excel Function...
		 * 	This is just to encrypt the RSS Squared in Paillier
		 */
		//Print Keys to this Location...
		String KeyPrint = "C:\\Users\\Andrew\\Desktop\\REU\\PaillierKeys.txt";

		//Read CSV full of Training data from here...
		String FileIn="C:\\Users\\Andrew\\Desktop\\REU\\REUSQLDatabase2.csv";
		Scanner encryptDatabase = null;
		try 
		{
			encryptDatabase = new Scanner(new File(FileIn));
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}

		try 
		{
			PrintWriter pw = new PrintWriter(
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(KeyPrint))));
			pw.println("Value of k1: " + sk.k1);
			pw.println("Value of N: (PUBLIC) " + pk.n);
			pw.println("Value of Mod: (PUBLIC) " + pk.modulous);
			pw.println("Value of LAMDA: " + sk.lambda);
			pw.println("Value of Mu:" + sk.mu);
			pw.flush();
			pw.close();
			/*
			 * 	Encrypt the Value with Paillier...
			 * 	In this case the very last row has the RSS Squared values which I need to print!
			 */
			String newCSV = "C:\\Users\\Andrew\\Desktop\\REU\\REUSQLDatabase3.csv";
			PrintWriter crypto = new PrintWriter(
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(newCSV))));

			String header = encryptDatabase.next();//Get Rid of header...
			
			int RSSSQ;//Get RSSSQuared Value
			String input;//Prints new Row to CSV File (with Encrypted Value);
			String [] fullRow;
			BigInteger temp;
			crypto.println(header+ "," + "EncryptedRSSSquared");
			while(encryptDatabase.hasNext())
			{
				//Isolate the RSS SQuared Value
				input = encryptDatabase.next();
				fullRow = input.split(",");
				RSSSQ = Integer.parseInt(fullRow[4]);
				System.out.println(RSSSQ);
				
				//Encrypt it
				temp = Paillier.encrypt(new BigInteger(fullRow[4]), pk);
				System.out.println(temp);
				
				//Print new String to a new CSV File! REUSQLDatabase3
				crypto.println(input + "," + temp);
			}

			crypto.flush();
			crypto.close();
			encryptDatabase.close();
			

		}
		catch (IOException ioe)
		{
			System.out.println("IO EXCEPTION!");
		}

	}

	public static int Random() //Generate numbers 1 to 100
	{
		return (int)Math.ceil((Math.random()*100));
	}

	public static String tenAPGenerate()
	{
		String gen="";
		for (int i=0;i<10;i++)
		{
			gen+=Integer.toString(Random())+",";
		}
		return gen;
	}
}//End of Class
//===================Code Graveyard===============================
//	 //Step 2: According to Shamet
//    public static BigInteger computeZ(BigInteger CipherTextA, BigInteger CipherTextB, PrivateKey sk)
//    {
//    	BigInteger Two = new BigInteger("2");
//    	BigInteger Z = Two.pow(sk.k1).multiply(CipherTextA).multiply(CipherTextB);
//    	return Z;
//    }
//    
//    //Step 3: According to Shamet
//    public static BigInteger computeD(BigInteger Z, PublicKey pk)
//    {
//    	//Generate Random and encrypt it
//    	BigInteger random = new BigInteger((pk.k1+kappa+1), k2, rnd);
//    	BigInteger encryptedRandom = Paillier.encrypt(random, pk);
//    	//D = [Z + Random]
//    	BigInteger D=Paillier.add(Z, encryptedRandom, pk);
//    	//Rehash D
//    	D=Paillier.add(D, Paillier.encrypt(BigInteger.ZERO, pk), pk);
//    	return D;
//    }
//    
//    //Step 5: According to Shamet
//    public static BigInteger computeZPrime(BigInteger D,PublicKey pk)
//    {
//    	//Generate Random and encrypt it
//    	BigInteger random = new BigInteger((pk.k1+kappa+1), k2, rnd);
//    	BigInteger encryptedRandom = Paillier.encrypt(random, pk);
//    	
//    	BigInteger modulus = new BigInteger("2");
//    	modulus.pow(pk.k1);
//    	//z=d(mod 2^l)
//    	BigInteger Zprime=D.mod(modulus);
//    	//( rrandom(mod 2^L) )^-1
//    	
//    	Zprime=Zprime.multiply(encryptedRandom.modInverse(modulus));
//    	return Zprime;
//    	 /*
//    	 Bob must obtain an encryption [Lambda] by a binary value
//    	 containing the result by the comparison of two private inputs:
//    	 dprime = d (mod 2^l) held by Alice (user)
//    	 rprime = r (mod 2^l) held by Bob (Database) 
//    	 */
//    }
//    
//    
//    
//    //Step 9: According to Shamet
//    public static int chooseS()
//    {
//    	//select a random number from 1 to 100.
//    	int draw = (int)Math.ceil((Math.random()*100));
//    	int S;
//    	if (draw%2==0)
//    	{
//    		S=1;
//    	}
//    	else
//    	{
//    		S=-1;
//    	}
//    	return S;
//    }
//    //Step 10
//    //public static BigInteger DGKCompare()
//    //{
//    	//BigInteger compareCipher;
//    	//for (int i=0;i<10;i++)
//    	//{
//    		//compareCipher.multiply(val);
//    	//}
//    	//return compareCipher;
//    //}
//    //Step 12 and Step 13
//    public static BigInteger computeEi(BigInteger CipherText, PublicKey pk)
//    {
//    	//r_i belongs to z_u.  In this case u = 7
//    	BigInteger random = new BigInteger(7, k2, rnd);
//    	CipherText.pow(random.intValue());
//    	
//    	//Step 13: Re-randomize and permates the encryptions [[e_i]]
//    	//and send them to Alice (user)
//    	
//    	return CipherText;
//    }
//    
//    //Step 15
//    public boolean LambdaSign(int S)
//    {
//    	if (S==1)
//    	{
//    		return true;
//    		/*
//    		 * 	[Lambda] = [-Lambda Prime]
//    		 */
//    	}
//    	else
//    	{
//    		return false;
//    		/*
//    		 * 	[Lambda] = [Lambda Prime]
//    		 */
//    	}
//    }
//    
//    //Step 16
//    //is z(mod 2^L) = Z prime * [Lambda]^(2l)
