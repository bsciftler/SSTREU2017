import edu.fiu.reu2017.DGKPrivateKey;
import edu.fiu.reu2017.DGKPublicKey;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

/*
Methods converted from C++ NTL Library using this site as my guide
http://www.shoup.net/ntl/doc/ZZ.txt
See the very bottom of the code to find the NLT methods translated to Java
DGK Code was translated from C++ thanks to:
https://github.com/Bvinhmau/DGK-outsourced

Credits to Andrew for code conversion and Samet for helping on revising the code/debugging it.

Feel free to use this code as you like.
*/
public class DGKOperations
{
	private static int l=16, t=160, k=1024;
	private static DGKPublicKey pubKey;
	private static DGKPrivateKey privkey;
	private static Random rnd = new Random();
	private static int certainty = 40;
	//Probability of getting prime is 1-(1/2)^40
	private static long biggestPlaintext;
	public static int gammaA;
	
	public DGKOperations(int newl, int newt, int newk)
	{
		l = newl;
		t = newt;
		k = newk;
		GenerateKeys(l,t,k);
	}

	public static void GenerateKeys(int l, int t, int k)
	{
		System.out.println("Generating Keys...");
	    biggestPlaintext = (long) Math.pow((double)2,(double)l);
		// First check that all the parameters of the KeyPair are coherent throw an exception otherwise
	    if (l < 0 || l >= 32 )
	    {
	        throw new IllegalArgumentException("DGK Keygen Invalid parameters : plaintext space must be less than 32 bits");
	    }

	    if (l > t ||  t > k )
	    {
	        throw new IllegalArgumentException("DGK Keygen Invalid parameters: we must have l < k < t");
	    }
	    if ( k/2 < t + l + 1 )
	    {
	        throw new IllegalArgumentException("DGK Keygen Invalid parameters: we must have k > k/2 < t + l ");
	    }

	    if ( t%2 != 0 )
	    {
	        throw new IllegalArgumentException("DGK Keygen Invalid parameters: t must be divisible by 2 ");
	    }
	    System.out.println("No Exceptions in data found");
	    
	    BigInteger p, rp;
	    BigInteger q, rq;
	    BigInteger g, h ;
	    BigInteger n, r ;
	    long u;
	    BigInteger vp,vq, vpvq,tmp;

	    while(true)
	    {
	        // Generate some of the required prime number
	    	/*
	    	 * 	Here Line 54 it is a bit in C++ Code
	    	 * 	But I remember in DGK's original Paper they said
	    	 * 	something about finding the next prime from l+2.
	    	 * 	So I will go with that...
	    	 */
	    	//System.out.println("Stuck at Next Prime");
	    	BigInteger Lplustwo = BigInteger.valueOf((long) biggestPlaintext).add(new BigInteger("2"));
	    	System.out.println(Lplustwo.toString());
	    	BigInteger zU  = NextPrime(Lplustwo);      
	    	u = zU.longValue();
	    	System.out.println(u +" " +Lplustwo.toString());
	        vp = new BigInteger(t, certainty, rnd);//(160,40,random)
	        vq = new BigInteger(t, certainty, rnd);//(160,40,random)
	        vpvq = vp.multiply(vq);
	        tmp = BigInteger.valueOf(u).multiply(vp);

	        System.out.println("Completed generating vp, vq");
	        
	        int needed_bits = k/2 - (tmp.bitLength());
	        //if k = 1,024
	        //needed is 512 - u-bit - vp bits

	        // Generate rp until p is prime such that uvp divde p-1
	        do
	        {
	        	//rp can be a random number?
	        	rp = new BigInteger(needed_bits, rnd);//(512,40,random)
		        rp = rp.setBit(needed_bits - 1 );
		        /*
	from NTL:
	long SetBit(ZZ& x, long p);
	returns original value of p-th bit of |a|, and replaces p-th bit of
	a by 1 if it was zero; low order bit is bit 0; error if p < 0;
	the sign of x is maintained
		         */
		        
		        // p = rp * u * vp + 1
		        // u | p - 1
		        // vp | p - 1
		        p = rp.multiply(tmp).add(BigInteger.ONE);
		        System.out.println("p is not prime");
	        }
	        while(!p.isProbablePrime(certainty));
	        //while(!p.isProbablePrime(90));
	        
	        //Thus we ensure that p is a prime, with p-1 divisible by prime numbers vp and u
	        //I can implement AKS for 100% certainty if need be
	        
	        tmp = BigInteger.valueOf(u).multiply(vq);
	        needed_bits = k/2 - (tmp.bitLength());
	        do
	        {
	        	// Same method for q than for p
		        rq = new BigInteger(needed_bits, rnd);//(512,40,random)
		        rq = rq.setBit(needed_bits -1);
		        q = rq.multiply(tmp).add(BigInteger.ONE);
		        System.out.println("q is not prime");
	        }
	        while(!q.isProbablePrime(certainty));    
	       //Thus we ensure that q is a prime, with p-1 divides the prime numbers vq and u
	       
	        if(!POSMOD(rq,BigInteger.valueOf(u)).equals(BigInteger.ZERO) && !POSMOD(rp,BigInteger.valueOf(u)).equals(BigInteger.ZERO))
	        {
	            break;//done!!
	        }
	    }
	    System.out.println("While Loop 1: initiational computations completed.");
	    n = p.multiply(q);
	    tmp = rp.multiply(rq).multiply(BigInteger.valueOf(u));
	    System.out.println("n, p and q sucessfully generated!");

	    while(true)
	    {
	        do
	        {
	            r = new BigInteger(n.bitLength(), rnd);//(bit size,40,random)
	            //System.out.println("bitlength of r: "+ r.bitLength());
	            //System.out.println("bitlength of n: " + n.bitLength());
	            if (r.bitLength()==n.bitLength())
	            {
	            	System.out.println("escape the loop!");
	            }
	        } 
	        while (!( r.bitLength()==n.bitLength()) );//Ensure it is n-bit Large number
	        
	        h = r.modPow(tmp,n);
	        
	        if (h.equals(BigInteger.ONE))
	        {
	            continue;
	        }
	        
	        if (h.modPow(vp,n).equals(BigInteger.ONE))
	        {
	            continue;
	        }
	        
	        if (h.modPow(vq,n).equals(BigInteger.ONE))
	        {
	            continue;
	        }
	        
	        if (h.modPow(BigInteger.valueOf(u), n).equals(BigInteger.ONE))
	        {
	            continue;
	        }
	        
	        if (h.modPow(BigInteger.valueOf(u).multiply(vq), n).equals(BigInteger.ONE))
	        {
	            continue;
	        }
	        
	        if (h.modPow(BigInteger.valueOf(u).multiply(vp), n).equals(BigInteger.ONE))
	        {
	            continue;
	        }

	        if (h.gcd(n).equals(BigInteger.ONE))
	        {                                               
	            break;
	        }
	    }

	    BigInteger rprq = rp.multiply(rq);
	    System.out.println("h and g generated");
	    
	    while(true)
	    {
	    	do
	    	{
	    		r = new BigInteger(n.bitLength(), rnd);//(bit length,40,random)
	    	} 
	    	while (! (r.bitLength()==n.bitLength()) );
		        
	        g = r.modPow(rprq,n);

	        if (g.equals(BigInteger.ONE))
	        {
	            continue;
	        }

	        if (!g.gcd(n).equals(BigInteger.ONE))
	        {
	            continue;
	        } // Then h can still be of order u, vp, vq , or a combination of them different that uvpvq

	        if (g.modPow(BigInteger.valueOf(u),n).equals(BigInteger.ONE))
	        {
	            continue;
	        }
	        if (g.modPow(BigInteger.valueOf(u).multiply(BigInteger.valueOf(u)),n).equals(BigInteger.ONE))
	        {
	            continue;
	        }
	        if (g.modPow(BigInteger.valueOf(u).multiply(BigInteger.valueOf(u)).multiply(vp),n).equals(BigInteger.ONE))
	        {
	            continue;
	        }
	        if (g.modPow(BigInteger.valueOf(u).multiply(BigInteger.valueOf(u)).multiply(vq),n).equals(BigInteger.ONE))
	        {
	            continue;
	        }

	        if (g.modPow(vp,n).equals(BigInteger.ONE))
	        {
	            continue;
	        }

	        if (g.modPow(vq,n).equals(BigInteger.ONE))
	        {
	            continue;
	        }

	        if (g.modPow(BigInteger.valueOf(u).multiply(vq),n).equals(BigInteger.ONE))
	        {
	            continue;
	        }

	        if (g.modPow(BigInteger.valueOf(u).multiply(vp),n).equals(BigInteger.ONE))
	        {
	            continue;
	        }

	        if (g.modPow(vpvq,n).equals(BigInteger.ONE))
	        {
	            continue;
	        }
	        if (POSMOD(g,p).modPow(vp,p).equals(BigInteger.ONE))
	        {
	            continue; // Temporary fix
	        }
	        
	        if ((POSMOD(g,p).modPow(BigInteger.valueOf(u),p).equals(BigInteger.ONE)))
	        {
	            continue;// Temporary fix
	        }
	        
	        if (POSMOD(g,q).modPow(vq,q).equals(BigInteger.ONE))
	        {
	            continue;// Temporary fix
	        }
	        
	        if ((POSMOD(g,q).modPow(BigInteger.valueOf(u),q).equals(BigInteger.ONE)))
	        {
	            continue;// Temporary fix
	            //((g%q)+q)%q^{u}(mod q) = 1;
	            //Test if g^{u} = 1 (mod q)
	        }
	        
	        break;
	    }
	    
	    System.out.println("Final variable tests completed");
	    /*
	        ZZ gvp = PowerMod(POSMOD(g,n),vp*vq,n);
	        for (int i=0; i<u; ++i){
	            ZZ decipher = PowerMod(gvp,POSMOD(ZZ(i),n),n);
	            lut[decipher] = i;
	        }
	    */
	    System.out.println("Generating hashmap");
	    
	    HashMap<BigInteger, Long> lut = new HashMap <BigInteger, Long>();
	    
	    BigInteger gvp = POSMOD(g,p).modPow(vp,p);
	    
	    /*
	    I am printing Lookup Tables so it can easily be imported into 
	    the Android Phone,
	    DO NOT USE DGKGENERATON ON ANDROID PHONE, IT TAKES TOO LONG!
	     */
	    String LUTlocation = "C:\\Users\\Andrew\\Desktop\\LUT.txt";
	    String hLUTlocation = "C:\\Users\\Andrew\\Desktop\\hLUT.txt";
    	String gLUTlocation = "C:\\Users\\Andrew\\Desktop\\gLUT.txt";
    	String KeyLocation= "C:\\Users\\Andrew\\Desktop\\DGKKeys.txt";
		PrintWriter keys = null;
		PrintWriter pwOne = null;
		PrintWriter pwTwo = null;
		PrintWriter pwThree = null;
    	
	    try
	    {
	    	keys = new PrintWriter(
					 new BufferedWriter(
					 new OutputStreamWriter(
					 new FileOutputStream(KeyLocation))));
	    	
	    	pwOne = new PrintWriter(
					 new BufferedWriter(
					 new OutputStreamWriter(
					 new FileOutputStream(LUTlocation))));
        	pwTwo = new PrintWriter(
        			new BufferedWriter(
        					new OutputStreamWriter(
        							new FileOutputStream(hLUTlocation))));
        	pwThree = new PrintWriter(
        			new BufferedWriter(
        					new OutputStreamWriter(
        							new FileOutputStream(gLUTlocation))));
	    }
	    catch(IOException ioe)
	    {
	    	System.out.println("FAILURE AT GENERATING PRINT WRITER FOR DGK KEY GENERATION/LOOKUP TABLES");
	    	ioe.printStackTrace();
	    }
	    for (int i=0; i<u; ++i)
	    {
	    	
	        BigInteger decipher = gvp.modPow(POSMOD(BigInteger.valueOf((long) i),p),p);
	        //lut[decipher] = i;
	        pwOne.println(decipher + "," + i);
	        lut.put(decipher,(long)i);
	    }

	    HashMap<Long, BigInteger> hLUT = new HashMap<Long, BigInteger>(2*t);
	    for (int i=0; i<2*t; ++i)
	    {
	        BigInteger e = new BigInteger("2").modPow(BigInteger.valueOf((long)(i)),n);
	        BigInteger out = h.modPow(e,n);
	        //hLUT[i] = out;
	        pwTwo.println(i + "," + out);	   
	        hLUT.put((long)i,out);
	    }
	    
	    HashMap<Long, BigInteger> gLUT = new HashMap<Long, BigInteger>();
	    for (int i=0; i<u; ++i)
	    {
	        BigInteger out = g.modPow(BigInteger.valueOf((long)i),n);
	        //gLUT[i] = out;
	        pwThree.println((i + "," + out));
	        gLUT.put((long)i,out);
	    }
	    pwOne.flush();
	    pwTwo.flush();
	    pwThree.flush();
	    pwOne.close();
	    pwTwo.close();
	    pwThree.close();
	    pubKey =  new DGKPublicKey(n,g,h, u, gLUT,hLUT,l,t,k);
   	    privkey = new DGKPrivateKey(p,q,vp,vq,lut,u);
	    System.out.println("Printing DGK values!!");
	        
		keys.println("Public Key");
		keys.println("N: " + n);
		keys.println("G: " + g);
		keys.println("H: " + h);
		keys.println("U: " + u);	
		keys.println("Private Key");	
		keys.println("p: "+ p);
		keys.println("q: "+ q);
		keys.println("vp: " + vp);
		keys.println("vq: " + vq);
		keys.println("U: " + u);
		keys.flush();
		keys.close();
	    System.out.println("FINISHED WITH DGK KEY GENERATION!");
	}//End of Generate Key Method
	
	public DGKPublicKey getPublicKey()
	{
		return pubKey;
	}
	
	public DGKPrivateKey getPrivateKey()
	{
		return privkey;
	}
	
	public static BigInteger encrypt(DGKPublicKey pubKey, long plaintext)
	{
	    int t = pubKey.t;
	    BigInteger n = pubKey.n;
	    BigInteger h = pubKey.h;
	    BigInteger u = pubKey.bigU;
	    int U=u.intValue();
	    //Through error is plain text not in Zu
	    BigInteger ciphertext,r;

	    if (plaintext < 0 || plaintext >= U)
	    {
	        throw new IllegalArgumentException("Encryption Invalid Parameter : the plaintext is not in Zu");
	    }
		do
    	{
    		r = new BigInteger(2*t, rnd);//(512,40,random);
    	} 
    	while (! (r.bitLength()==(2*t)) );
	    r = r.setBit(t*2-1);
	    
	    BigInteger firstpart = pubKey.getGLUT().get(plaintext);
	    BigInteger secondpart = BigInteger.ZERO;
	    if (h.equals(BigInteger.ZERO))
	    {
	        secondpart = BigInteger.ZERO;
	    }
	    secondpart = BigInteger.ONE;
	    for(int i = 0; i < r.bitLength(); ++i)
	    {
	    	/*
	    	 * 
long bit(const ZZ& a, long k);
long bit(long a, long k); 
// returns bit k of |a|, position 0 being the low-order bit.
// If  k < 0 or k >= NumBits(a), returns 0.
	    	 */
	        if(bit(r,i) == 1)
	        {
	            secondpart = secondpart.multiply(pubKey.getHLUT().get(i));
	        }
	    }

	    //secondpart = h.modPow(r,n);
	    
	    ciphertext = POSMOD(firstpart.multiply(secondpart), n);
	    
	    //PREVENT ERRORS AT ENCRYPTION!
	    //while ( (isGreaterthan(ciphertext,n)) )
	    //{
	    	//System.out.println("Shrink cipher!");
	    	//ciphertext = ciphertext.mod(n);
	    //}
	    return ciphertext;
	}
	
	public static long decrypt(DGKPublicKey pubKey, DGKPrivateKey privKey, BigInteger ciphertext)
	{
	    BigInteger vp = privKey.GetVP();
	    BigInteger p = privKey.GetP();
	    BigInteger n = pubKey.n;
	    //long u = pubKey.u;
	    //System.out.println("Value of N: ");
	    //System.out.println(n);
	    if (ciphertext.signum()==-1)
	    {
	    	throw new IllegalArgumentException("Decryption Invalid Parameter : the ciphertext is not in Zn");
	    }
	    if(ciphertext.compareTo(n)==1)
	    {
	    	throw new IllegalArgumentException("Decryption Invalid Parameter : the ciphertext is not in Zn");
	    }
	    BigInteger decipher = POSMOD(ciphertext,p);
	    decipher = ciphertext.modPow(vp,p);
	    long plain = privKey.GetLUT().get(decipher).longValue();
	    return plain;
	}
	
	//Cipher a * Cipher b
	public static BigInteger DGKAdd(DGKPublicKey pubKey, BigInteger a, BigInteger b)
	{
	    BigInteger n= pubKey.n;
	    if (a.signum()==-1 || a.compareTo(n)== 1 || b.signum()==-1 || b.compareTo(n)==1)
	    {
	        throw new IllegalArgumentException("DGKAdd Invalid Parameter : at least one of the ciphertext is not in Zn");
	    }
	    BigInteger result = a.multiply(b).mod(n);
	    //Originally called MulMod...Method not found...
	    //Assume a*b(mod n)
	    return result;
	}
	
	public static BigInteger DGKSubtract(DGKPublicKey pubKey, BigInteger a, BigInteger b)
	{
		BigInteger minusB = DGKMultiply(pubKey, b, -1);
		return DGKOperations.DGKAdd(pubKey, a, minusB);
	}
	//cipher a * Plain text
	public static BigInteger DGKMultiply(DGKPublicKey pubKey, BigInteger cipher, long plaintext)
	{
	    BigInteger n = pubKey.getN();
	    long u = pubKey.getU();
	    if (cipher.signum()==-1 || cipher.compareTo(n)==1)
	    {
	    	 throw new IllegalArgumentException("DGKMultiply Invalid Parameter :  the ciphertext is not in Zn");
	    }
	    if (plaintext <= -2 || u <= plaintext )
	    {
	    	 throw new IllegalArgumentException("DGKMultiply Invalid Parameter :  the plaintext is not in Zu");
	    }
	    BigInteger result = cipher.modPow(BigInteger.valueOf(plaintext),n);
	    return result;
	}
	
 
	public static HashMap<Long, BigInteger> generateLUT(DGKPublicKey pubKey, DGKPrivateKey privKey)
	{
    	//BigInteger n = pubKey.getN();
    	BigInteger g = pubKey.g;
    	//BigInteger h = pubKey.h;
    	BigInteger u = pubKey.bigU;
    	BigInteger p = privKey.GetP();
    	BigInteger vp = privKey.GetVP() ;
    	BigInteger gvp = POSMOD(g,p).modPow(vp,p);
    	HashMap <Long, BigInteger>LUT = new HashMap <Long,BigInteger>(u.intValue());
    	for (int i=0; i<u.intValue(); ++i)
    	{
        	BigInteger decipher = gvp.modPow(POSMOD(BigInteger.valueOf((long)i),p),p);
        	//LUT[decipher] = i;
        	LUT.put((long)i, decipher);
    	}
    	return LUT;
	}
	
	public static BigInteger isSuperiorTo(DGKPublicKey pubKey,DGKPrivateKey privKey, BigInteger x, BigInteger y)
	{
	    // A & B
	    int l = pubKey.l - 2 ; // see constraints in the veugen paper
	    BigInteger N = pubKey.n;
	    long u = pubKey.u;
	    BigInteger powL = new BigInteger("2");//Was originally long...
	    long longpowL = 2;

	    // A
	    long r = RandomBnd(N.bitLength()).longValue();//

	    //r = 5; //TODO remove this heresy
	    BigInteger encR = encrypt(pubKey,r);
	    long alpha = POSMOD(BigInteger.valueOf(r),powL).longValue();
	    BigInteger alphaZZ = BigInteger.valueOf(alpha);
	    BigInteger z = 
	    			DGKAdd(pubKey,
	    			encrypt(pubKey,powL.longValue()),
	    			DGKAdd(pubKey, encR,
	    			DGKAdd(pubKey, x, DGKMultiply(pubKey,y,u-1))));
	    // B
	    long plainZ = decrypt(pubKey,privKey,z);

	    long beta = POSMOD(BigInteger.valueOf(plainZ), powL).longValue();
	    BigInteger encBetaMayOverflow = 
	    	encrypt
	    	(
	    		pubKey,
	    		(
	    			overflow(beta,(powL.subtract(POSMOD(N,powL)).longValue()))
	    		)
	    	);
	    BigInteger d ;
	    if ( plainZ < N.subtract(BigInteger.ONE).divide(new BigInteger("2")).longValue())
	    {
	        d = encrypt(pubKey,1);
	    }
	    else
	    {
	        d = encrypt(pubKey,0);
	    }
	    long betaTab [] = new long [l];
	    BigInteger encBetaTab [] = new BigInteger [l];

	    for(int i = 0 ; i < l ; i++)
	    {
	        betaTab[i] = bit(beta,i);
	        encBetaTab[i] = encrypt(pubKey, betaTab[i]);
	    }
	    //A
	    if (r < N.subtract(BigInteger.ONE).divide(new BigInteger("2")).longValue())
	    {
	        d = encrypt(pubKey,0);
	    }
	    BigInteger encAlphaXORBetaTab [] = new BigInteger[l];
	    BigInteger W [] = new BigInteger [l] ;
	    BigInteger c [] = new BigInteger [l+1];


	    long alphaHat = POSMOD(BigInteger.valueOf(r).subtract(N), powL).longValue();
	    BigInteger xorBitsSum = encrypt(pubKey,0);
	    BigInteger alphaHatZZ = BigInteger.valueOf(alphaHat);
	    for(int i = 0 ; i < l ; i++)
	    {
	        if(bit(alpha,i) == 0)
	        {
	            encAlphaXORBetaTab[i] = encBetaTab[i];
	        }
	        else
	        {
	            encAlphaXORBetaTab[i] = DGKAdd(pubKey,  encrypt(pubKey,1), DGKMultiply(pubKey,encBetaTab[i],u-1));
	        }
	        if(bit(alphaZZ,i) == bit(alphaHatZZ,i))
	        {
	            W[i] = encAlphaXORBetaTab[i];
	            //  xorBitsSum = DGKAdd(pubKey,xorBitsSum,encAlphaXORBetaTab[i] );
	        }
	        else
	        {
	            W[i] = DGKAdd(pubKey,  encAlphaXORBetaTab[i], DGKMultiply(pubKey,d,u-1));
	            // W[i] = encAlphaXORBetaTab[i];
	            // xorBitsSum = DGKAdd(pubKey,xorBitsSum,DGKAdd(pubKey,encrypt(pubKey,1),DGKMultiply(pubKey,encAlphaXORBetaTab[i],u-1)) );
	        }

	        W[i] = DGKMultiply(pubKey, W[i],(long)Math.pow(2,i));
	        xorBitsSum = DGKAdd(pubKey,xorBitsSum,DGKMultiply(pubKey,W[i],2 ));
	    }
	    long da = RandomBnd(2).longValue();
	    long s = 1 -2*da;
	    BigInteger wProduct = encrypt(pubKey,0);
	    for(int i = 0 ; i < l ; i++)
	    {
	        long alphaexp = POSMOD( bit(alphaHatZZ,l-1-i) -  bit(alphaZZ,l-1-i), u);
	        c[l-1-i] =  DGKAdd(pubKey,
	                           wProduct,
	                           DGKAdd(pubKey, DGKAdd(pubKey,DGKMultiply(pubKey,encBetaTab[l-1-i],u-1),encrypt(pubKey, POSMOD(BigInteger.valueOf(s + bit(alphaZZ, l-1-i)), N).longValue() )), DGKMultiply(pubKey,d,alphaexp)));

	        BigInteger rBlind = RandomBits_ZZ(pubKey.t * 2);
	        rBlind = rBlind.setBit(pubKey.t * 2 -1);
	        //TODO BLIND
	        wProduct = DGKAdd(pubKey,wProduct,DGKMultiply(pubKey, W[l-1-i],3));

	    }
	    c[l] = DGKAdd(pubKey, encrypt(pubKey,da),xorBitsSum);
	    // A shuffle C

	    // B
	    BigInteger db =  encrypt(pubKey,0);
	    for(int i = 0 ; i < l+1 ; i++)
	    {
	        if(decrypt(pubKey, privKey,c[i]) == 0)
	        {
	            db= encrypt(pubKey,1);
	            break;
	        }
	    }
	    BigInteger divZ = encrypt(pubKey,plainZ/powL.longValue());

	    //A
	    BigInteger betaInfAlpha;
	    if (da == 1)
	    {
	        betaInfAlpha = db;
	    }
	    else
	    {
	        betaInfAlpha = DGKAdd(pubKey,encrypt(pubKey,1),DGKMultiply(pubKey, db, u -1));
	    }

	    BigInteger overflow = DGKMultiply(pubKey,d,N.divide(powL.add(BigInteger.ONE)).longValue());
	    BigInteger doubleBucketGap ;
	    if (beta >= longpowL - POSMOD(N,powL).longValue())
	    {
	        doubleBucketGap = encrypt(pubKey,0);
	    }
	    else
	    {
	        doubleBucketGap = DGKMultiply(pubKey, d, u -1);
	    }
	    overflow = DGKAdd(pubKey,overflow,doubleBucketGap);
//	    BigInteger result =
//	    		DGKAdd(pubKey,
//	    		DGKAdd(pubKey,divZ,
//	    		DGKMultiply(pubKey,DGKAdd(pubKey, encrypt(pubKey,r/longpowL),betaInfAlpha), u-1))
//	             , overflow);
//	    long lastpush = decrypt(pubKey,privKey,d) *
//	    (
//	    	(1 -decrypt(pubKey,privKey,betaInfAlpha))*(1 - (overflowTwo(alpha,POSMOD(N,powL).longValue())))* (overflow(beta,(longpowL - POSMOD(N,powL).longValue())))
//	    	+(decrypt(pubKey,privKey,betaInfAlpha))*(0 - (overflowTwo(alpha,POSMOD(N,powL).longValue()))* (1-(overflow(beta,(longpowL - POSMOD(N,powL).longValue())))
//	    )));
//
//	    BigInteger effectOfAlphaBetaOverflow = CipherMultiplication(pubKey,privKey,betaInfAlpha,encBetaMayOverflow).get(0);
//	    //effectOfAlphaBetaOverflow = DGKMultiply(pubKey,effectOfAlphaBetaOverflow, 
//	    		POSMOD(BigInteger.valueOf
//	    				(
//	    						2 * overflowTwo(alpha,POSMOD(N,powL).longValue())
//	    				).subtract(BigInteger.ONE),N));
//	    //effectOfAlphaBetaOverflow = DGKAdd(pubKey,effectOfAlphaBetaOverflow,DGKMultiply(pubKey,DGKAdd(pubKey, encBetaMayOverflow, betaInfAlpha),
//	    		POSMOD(
//	    				N.subtract
//	    				(
//	    				BigInteger.valueOf
//	    					(
//	    						overflowTwo(alpha,POSMOD(N,powL).longValue())
//	    					)
//	    				)
//	    				,N)));
//	    effectOfAlphaBetaOverflow = DGKAdd(pubKey, effectOfAlphaBetaOverflow,encBetaMayOverflow );
//	    effectOfAlphaBetaOverflow = CipherMultiplication(pubKey,privKey,effectOfAlphaBetaOverflow,d).get(0);	    // encBetaMayOverflow
//	    result = DGKAdd(pubKey,result,DGKMultiply(pubKey,effectOfAlphaBetaOverflow,u-1));
return null;
	    //return result;
	}
	
	public static ArrayList<BigInteger> CipherMultiplication(DGKPublicKey pubKey,DGKPrivateKey privKey, BigInteger x, BigInteger y)
	{
	    long u = pubKey.u;
	    // Generate all the blinding/challenge values
	    long ca = RandomBnd(u).longValue();
	    long cm = (RandomBnd(u-1).add(BigInteger.ONE)).longValue();
	    long bx = RandomBnd(u).longValue();
	    long by = RandomBnd(u).longValue();
	    long p = (RandomBnd(u-1).add(BigInteger.ONE)).longValue();

	    //long secrets[]= {ca, cm, bx, by,p};
	    BigInteger caEncrypted = encrypt(pubKey, ca);
	    BigInteger bxEncrypted = encrypt(pubKey, bx);
	    BigInteger byEncrypted = encrypt(pubKey, by);
	    //BigInteger pEncrypted  = encrypt(pubKey, p);

	    BigInteger xBlinded  = DGKAdd(pubKey, x, bxEncrypted);
	    BigInteger yBlinded  = DGKAdd(pubKey, y, byEncrypted);
	    BigInteger challenge = DGKAdd(pubKey, xBlinded, caEncrypted);
	    challenge = challenge.multiply(DGKMultiply(pubKey, challenge,cm));

	    // Send blinded operands and challenge to the key owner
	    // Below Owner part
	    BigInteger product = 
	    	encrypt
	    	(
	    		pubKey,
	    		POSMOD
	    		(
	    			decrypt(pubKey,privKey,xBlinded)*(decrypt(pubKey,privKey,yBlinded))
	    			,u
	    		)
	    	);

	    BigInteger response = encrypt
	    (
	    	pubKey,
	    	POSMOD
	    	(
	    		decrypt(pubKey,privKey,challenge)*
	    		decrypt(pubKey,privKey,yBlinded)
	    	,u)
	    );
	    // The owner send product + response flow

	    //Unblind the challenge
	    BigInteger associated = 
	    	DGKMultiply
	    	(pubKey,
	    		DGKAdd
	    		(pubKey, response,
	    				DGKMultiply
	    				(pubKey,
	    					DGKAdd
	    						(pubKey,
	    							DGKMultiply(pubKey, product,
	    								POSMOD(cm,u)),
	    								DGKMultiply(pubKey, yBlinded,POSMOD(ca*cm,u))
	    						),u-1
	    				)
	    		), p
	    	);

	    // Un-blind the Result
	    BigInteger result = DGKAdd
	    (pubKey,product,
	    	DGKMultiply
	    		(pubKey,
	    			DGKAdd
	    			(pubKey,
	    				DGKAdd
	    				(pubKey,
	    						DGKMultiply(pubKey, x, by),
	    						DGKMultiply(pubKey, y, bx)),
	    						encrypt(pubKey, POSMOD(bx*by,u)
	    				)
	    			),
	    u-1		)
	    );
	    
	    ArrayList<BigInteger> answer= new ArrayList<BigInteger>();
	    answer.add(result);
	    answer.add(associated);
	    return answer;
	};


/*
====================================COMPUTATIONAL METHODS USED=====================================
 */
	public static BigInteger POSMOD(BigInteger x, BigInteger n)
	{
		//while (!(x.signum()==-1) )
		//{
			//x = x.add(n);
			//x = x.mod(n);
		//}
		BigInteger answer = x.mod(n);
		answer = answer.add(n);
		answer = answer.mod(n);
		return answer;
	}
	
	public static long POSMOD(long x, long n)
	{
		return ((x%n)+n)%n;
	}
	
	public static BigInteger NextPrime (BigInteger x)
	{
		//Find next Prime number after x
		//Example if x =18, return 19 (closest prime)

		if(x.mod(new BigInteger("2")).equals(BigInteger.ZERO))
		{
			x=x.add(BigInteger.ONE);
		}

		while (true)
		{
			if (isPrime(x))
			{
				return x;
			}
			//System.out.print(x.toString());
			x = x.add(new BigInteger("2"));
		}
	}
	
	public static boolean isPrime(BigInteger x)
	{
		BigInteger factor = new BigInteger("3");
		
		while (!factor.equals(x))
		{
			if (x.mod(factor).equals(BigInteger.ZERO))
			{
				return false;
			}
			factor=factor.add(new BigInteger("2"));
		}
		return true;
	}
	//long bit(const ZZ& a, long k);
	//long bit(long a, long k); 
	// returns bit k of |a|, position 0 being the low-order bit.
	// If  k < 0 or k >= NumBits(a), returns 0.
	public static long bit(BigInteger a, long k)
	{
		if (a.compareTo(BigInteger.valueOf(k))==1|| (a.subtract(BigInteger.valueOf(k)).equals(BigInteger.ZERO)) )
		{
			return 0;
		}
		if (k <0)
		{
			return 0;
		}
		String bit= a.toString(2);//get it in Binary
		return bit.charAt((int)k);
	}
	
	public static long bit(long a, long k)
	{
		if (a >= k)
		{
			return 0;
		}
		if (k < 0)
		{
			return 0;
		}
		BigInteger Bit = BigInteger.valueOf(k);
		String bit= Bit.toString(2);//get it in Binary
		return bit.charAt((int)k);
	}
	/*
void RandomBnd(ZZ& x, const ZZ& n);
ZZ RandomBnd(const ZZ& n);
void RandomBnd(long& x, long n);
long RandomBnd(long n);
x = pseudo-random number in the range 0..n-1, or 0 if n <= 0
	 */
	public static BigInteger RandomBnd(int n)
	{
		if (n == 0)
		{
			return BigInteger.ZERO;
		}
		BigInteger r;
		do 
		{
		    r = new BigInteger(n, rnd);
		}
		//while (r >= n), stay stuck!
		while (r.compareTo(new BigInteger(Integer.toString(n))) >= 0);
		return r;
	}
	
	public static BigInteger RandomBnd(long n)
	{
		if (n <= 0)
		{
			return BigInteger.ZERO;
		}
		BigInteger r;
		do 
		{
		    r = new BigInteger((int)n, rnd);
		}
		// 0 <= r <= n - 1
		//if r is negative or r >= n, keep generating random numbers
		while (r.compareTo(BigInteger.valueOf(n)) >= 0 || r.signum()==-1);
		return r;
	}
	/*
	 *
void RandomBits(ZZ& x, long l);
ZZ RandomBits_ZZ(long l);
void RandomBits(long& x, long l);
long RandomBits_long(long l);
// x = pseudo-random number in the range 0..2^L-1.
// EXCEPTIONS: strong ES
	 */
	
	public static BigInteger RandomBits_ZZ(int x)
	{
		BigInteger max = new BigInteger("2").pow(x);
		BigInteger r;
		do 
		{
		    r = new BigInteger(x, rnd);
		}
		//New number must be 0 <=  r <= 2^l - 1
		//If r >= 2^l or r <= 0 keep generating
		while (r.compareTo(max) >= 0 || r.signum()==-1);
		return r;
	}
	
	public static long overflow(long x, long y)
	{
		if (x >= y)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	public static long overflowTwo (long x, long y)
	{
		if (x < y)
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	/*
	 * 	Polynomial running time 100% fool proof prime test.
	 * 	Paper is from 2002.
	 */
	public static boolean AKSTest(BigInteger p)
	{
		//(x-1)^p - (x^p - 1)
		//Test if p divides all the coefficients
		//excluding the first and last term of (x-1)^p
		//If it can divide all of them then p is a prime
	
		//Using Binomial Theorem, I obtain the coefficients of all
		//terms from the expansion (x-1)^p
		ArrayList<BigInteger> coeff = BinomialTheorem(p);
		
		coeff.remove(0); //Remove first term
		coeff.remove(coeff.remove(coeff.size()-1)); //Remove last term
		
		for (int i=0;i<coeff.size();i++)
		{
			//System.out.println(coeff.get(i));
			//System.out.println(coeff.get(i).mod(p));
			if (!coeff.get(i).mod(p).equals(BigInteger.ZERO))
			{
				return false;
			}
		}
		return true;
	}
	//AKS-Test, I can use binomial theorem
	public static ArrayList<BigInteger> BinomialTheorem (BigInteger x)
	{
		ArrayList<BigInteger> coeff = new ArrayList<BigInteger>();
		/*
		 * 	Binomial Theorem: Choose
		 * 	n	n	n	...	n
		 * 	0	1	2	...	n
		 */
		BigInteger start = BigInteger.ZERO;
		while (! (start.equals(x.add(BigInteger.ONE))) )
		{
			coeff.add(nCr(x,start));
			start = start.add(BigInteger.ONE);
		}
		return coeff;
	}
	public static BigInteger nCr (BigInteger n, BigInteger r)
	{
		BigInteger nCr=factorial(n);
		nCr=nCr.divide(factorial(r));
		nCr=nCr.divide(factorial(n.subtract(r)));
		//nCr = n!/r!(n-r)!
		//or (n * n-1 * ... r+1)/(n-r)!
		return nCr;
	}
	
	public static BigInteger factorial(BigInteger x)
	{
		BigInteger result = BigInteger.ONE;
		BigInteger n = x;
		while (!n.equals(BigInteger.ZERO))
		{
			result = result.multiply(n);
			n= n.subtract(BigInteger.ONE);
		}
		return result;
	}
	
	//Protocol 2:
	public static void Pailliercompare(BigInteger Px, BigInteger Py, PublicKey pk)
	{
		//A(Server) chooses a random number r of l + 1 + sigma bits and computes z
		int sigma = 80;//As Suggested by Paper
		int l = 20;
		BigInteger z = Paillier.subtract(Px, Py, pk);
		BigInteger TwopowL = new BigInteger(String.valueOf(exponent(2,l)));
		//Get a random balue of EXACTLY l + 1 + sigma bits
		BigInteger r;
		do 
		{
			r = new BigInteger(l+1+sigma, rnd);
		}
		while (r.bitLength() != l + 1 + sigma);
		
		z = Paillier.add(z, Paillier.encrypt(r.add(TwopowL), pk), pk);
		
		//Step 2: Send to B (Android Phone, and decrypt z...
		//Compute z (mod 2^l)
		
		//Step 3:
		BigInteger alpha = r.mod(TwopowL);
	
		//A and B (Server and Android Phone run the comparison protocol with Private input
		//See Protocol 3...I will get gammaA and gamma B
		//such that gammaA XOR gammaB = (alpha <= beta)
		
	}
	
	private static int exponent(int base, int exponent)
	{
		if (exponent == 1)
		{
			return base;
		}
		return base * exponent(base, exponent-1);
	}
	//Protocol 3: x and y are PRIVATE PLAIN TEXT INPUTS!
	//Read Section 5.1 in Original 2007 DGK Paper
	//Also follows code from Thijis paper
	public static ArrayList<BigInteger> Protocol3(long x, ArrayList<BigInteger> y, DGKPublicKey pubKey)
	{
		BigInteger bigX = BigInteger.valueOf(x);
		
		int l = Math.max(bigX.bitLength(), y.size());
		if (bigX.bitLength() != y.size())
		{
			//if x is bigger, append 0 to y
			int xCounter = bigX.bitLength();
			int yCounter = y.size();
			String xBits = bigX.toString(2);
			
			while (xCounter != l)
			{
				xBits = "0" + xBits;
				++xCounter;
				
				//Update value of X
			}
			//if y is bigger, append 0 to x
			while(yCounter != l)
			{
				//Place more encrypted 0...
				//Sy = "0" + Sy;
				++yCounter;
			}
		}
		String bigXbits = bigX.toString(2);
		ArrayList<BigInteger> encryptedXORY = new ArrayList<BigInteger>();
		
		final BigInteger EncryptedOne = DGKOperations.encrypt(pubKey, (long)1);
		//Step 2:
		for (int i = 0; i < bigX.bitLength();i++)
		{
			if (bigXbits.charAt(i)=='0')
			{
				encryptedXORY.add(y.get(i));
			}
			else
			{
				encryptedXORY.add(DGKOperations.DGKSubtract(pubKey, EncryptedOne, y.get(i)));
			}
		}
		/*
		Step 3:
		Choose a uniformly random-bit gammaA.  Let L
		be the set {i| 0 <= i < l and x_i = gammaA}
		 */
		gammaA = new BigInteger(100,rnd).mod(new BigInteger("2")).intValue();
		//L is the set of all bits that is equal to gammaA.
		//Find all Indexes where x_i = gammaA
		ArrayList <Integer> indexList = new ArrayList<Integer>(); 
		for(int i = 0; i < bigX.bitLength(); i++)
		{
			if (bigXbits.charAt(i)==(char)gammaA)
			{
				indexList.add(i);
			}
		}
		
		//Step 4A: Compute C_i
		ArrayList<BigInteger> c_i = new ArrayList<BigInteger>();
			
		for (int i = 0; i <= l-1;i++)
		{
			c_i.add(XORsummation(i+1,l,bigXbits,y,pubKey));
		}
		//Step 4B:
		for (int i=0;i<c_i.size();i++)
		{
			if (gammaA==0)
			{
				c_i.set(i, DGKOperations.DGKSubtract
						(pubKey, DGKOperations.DGKAdd(pubKey, EncryptedOne, c_i.get(i)), y.get(i)));
			}
			else
			{
				c_i.set(i, DGKOperations.DGKAdd(pubKey, y.get(i), c_i.get(i)));
			}
		}
		
		//Step 5: Blinding Phase
		BigInteger temp;
		for (int i=0;i<c_i.size();i++)
		{
			if(indexList.contains(i))
			{
				c_i.set(i, DGKOperations.DGKMultiply(pubKey, c_i.get(i), randomNumbernbits(2*160).longValue()));
			}
			else
			{
				c_i.set(i, DGKOperations.encrypt(pubKey, randomNumbernbits(2*160).longValue()));
			}
		}
		//Step 6:
		return c_i;
	}
	
	private static BigInteger randomNumbernbits(int n)
	{
		BigInteger random;
		do
		{
			random = new BigInteger(n, rnd);
		}
		while (random.bitLength() != n);
		return random;
	}
	
	private static BigInteger XORsummation(int lowerBound, int upperBound, String plainBits, ArrayList<BigInteger> encbits, DGKPublicKey pubKey)
	{
		BigInteger sum = DGKOperations.encrypt(pubKey, 0);
		for(int i = lowerBound; i <= upperBound; i++)
		{
			
		}
		return sum;
	}
	public static int XOR (int x, int y)
	{
		if (x == 1 && y == 1 || x == 0 && y == 0)
		{
			return 0;
		}
		return 1;	
	}
	/*
	These methods are to build DGK Keys.
	This assumes the values were already pre-computed.
	This is just to export the data to a less computationally advanced device e. g. 
	an Android Phone.
	 */
	public static void readKeys (DGKPublicKey pk, DGKPrivateKey sk, String keyLocation)
	{
		Scanner read = null;
/*
NOTE THIS METHOD ASSUMES THE TEXT FILE HAS THIS STRUCTURE!!
AND ASSUMES CASE SENSATIVE (SEE DGKOPERATIONS.GENERATEKEY METHOD
Public Key
N: BLAH
G: BLAH
H: BLAH
U: BLAH
Private Key
p: BLAH
q: BLAH
vp: BLAH
vq: BLAH
U: BLAH
 */
		String reader;
		String [] cut;
		try
		{
			read= new Scanner(new File(keyLocation));
			reader=read.nextLine();
			if (reader.equals("Public Key"))
			{
				System.out.println("Term: Public Key was read...");
				reader = read.nextLine();
				//System.out.println(reader);
				cut = reader.split(":");
				System.out.println("Value of N: " + cut [1].trim());
				pk.n = new BigInteger(cut[1].trim());
				
				reader = read.nextLine();
				//System.out.println(reader);
				cut = reader.split(":");
				System.out.println("Value of G: " +cut [1].trim());
				pk.g = new BigInteger(cut [1].trim());
				
				reader = read.nextLine();
				//System.out.println(reader);
				cut = reader.split(":");
				System.out.println("Value of H: " +cut [1].trim());
				pk.h = new BigInteger(cut [1].trim());
				
				reader = read.nextLine();
				//System.out.println(reader);
				cut = reader.split(":");
				System.out.println("Value of U: " +cut [1].trim());
				pk.u = Long.parseLong(cut [1].trim());
				pk.bigU = BigInteger.valueOf(pk.u);
			}
			pk.l=l;
			pk.t=t;
			pk.k=k;
			
			reader = read.nextLine();
			if (reader.equals("Private Key"))
			{
				System.out.println("Private Key being read...");
				//Constructor to use: this (P, Q, VP, VQ, null, U);
				reader = read.nextLine();
				//System.out.println(reader);
				cut = reader.split(":");
				System.out.println("Value of P: " + cut [1].trim());
				BigInteger P = new BigInteger(cut [1].trim());
				
				reader = read.nextLine();
				//System.out.println(reader);
				cut = reader.split(":");
				System.out.println("Value of Q: " +cut [1].trim());
				BigInteger Q = new BigInteger(cut [1].trim());
				
				reader = read.nextLine();
				//System.out.println(reader);
				cut = reader.split(":");
				System.out.println("Value of VP: " +cut [1].trim());
				BigInteger VP = new BigInteger(cut [1].trim());
				
				reader = read.nextLine();
				//System.out.println(reader);
				cut = reader.split(":");
				System.out.println("Value of VQ: " +cut [1].trim());
				BigInteger VQ = new BigInteger(cut [1].trim());
				
				reader = read.nextLine();
				//System.out.println(reader);
				cut = reader.split(":");
				System.out.println("Value of U: " + cut [1].trim());
				long U = Long.parseLong(cut [1].trim());
				
				sk.setP(P);
				sk.setQ(Q);
				sk.setVP(VP);
				sk.setVQ(VQ);
			    sk.setU(U);
			}
			else
			{
				System.out.println("Something went wrong with reading Keys...");
			}
			
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		read.close();
	}
	public static void readLUT(DGKPrivateKey sk, String filelocation)
	{
		Scanner read = null;
		String in;
		String [] parts;
		HashMap <BigInteger, Long> lookup = new HashMap<BigInteger, Long>();
		try
		{
			read= new Scanner(new File(filelocation));
			//LUT is <BigInteger, Long>
			while (read.hasNext())
			{
				in = read.next();
				//System.out.println(in);
				parts = in.split(",");
				System.out.println(parts[0] + " " + parts[1]);
				lookup.put(new BigInteger(parts[0]), Long.parseLong(parts[1]));
			}
			sk.setLUT(lookup);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		read.close();
	}
	
	public static void readgLUT(DGKPublicKey pk, String filelocation)
	{
		Scanner read = null;
		String in;
		String [] parts;
		try
		{
			read= new Scanner(new File(filelocation));
			//gLUT is <Long, BigInteger>
			while (read.hasNext())
			{
				in = read.next();
				//System.out.println(in);
				parts = in.split(",");
				//System.out.println(parts[0] + " " + parts[1]);
				pk.gLUT.put(Long.parseLong(parts[0]), new BigInteger(parts[1]));
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		read.close();
	}
	
	public static void readhLUT(DGKPublicKey pk, String filelocation)
	{
		Scanner read = null;
		String in;
		String [] parts;
		try
		{
			read= new Scanner(new File(filelocation));
			while (read.hasNext())
			{
				//hLUT is <Long, BigInteger>
				in = read.next();
				parts = in.split(",");
				pk.hLUT.put(Long.parseLong(parts[0]), new BigInteger(parts[1]));
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		read.close();
	}
	
	public static void main (String[] args)
	{
		
		//DGKOperations FIU = new DGKOperations(l, t, k);
		
		//Get keys
		//DGKPublicKey pubKey = FIU.getPublicKey();
		//DGKPrivateKey privKey = FIU.getPrivateKey();
		String KeyLocation= "C:\\Users\\Andrew\\Desktop\\DGKKeys.txt";
		String LUTlocation = "C:\\Users\\Andrew\\Desktop\\LUT.txt";
	    String hLUTlocation = "C:\\Users\\Andrew\\Desktop\\hLUT.txt";
    	String gLUTlocation = "C:\\Users\\Andrew\\Desktop\\gLUT.txt";
    	
    	
		DGKPublicKey pubKey = new DGKPublicKey();
		DGKPrivateKey privKey = new DGKPrivateKey();
		DGKOperations.readKeys(pubKey, privKey, KeyLocation);
		
		DGKOperations.readLUT(privKey, LUTlocation);
		DGKOperations.readgLUT(pubKey, gLUTlocation);
		DGKOperations.readhLUT(pubKey, hLUTlocation);
	
		//pubKey.printKeys();
		//privKey.printKeys();
		privKey.printLUT();
		//pubKey.printgLUT();
		//pubKey.printhLUT();
		
		long escape;
		
		
		BigInteger test = DGKOperations.encrypt(pubKey, (long)70);
		
		BigInteger testSub = DGKOperations.DGKSubtract(pubKey, test, DGKOperations.encrypt(pubKey, 100));
		System.out.println("70 - 100: "+ DGKOperations.decrypt(pubKey, privKey, testSub));
		//Test 1: Encrypt/Decrypt good
		for (int i=0;i<19;i++)
		{
			test = DGKOperations.encrypt(pubKey, (long)i);
			escape = DGKOperations.decrypt(pubKey, privKey, test);
			if (i==escape)
			{
				System.out.println("SUCESS AT ENCRYPT/DECRYPT: " + i);
			}
			else
			{
				System.out.println("FAILURE AT ENCRYPT/DECRYPT: " + i);
			}
		}		
		
		BigInteger add = DGKOperations.encrypt(pubKey, (long)5);
		for (int j=10000; j < 10005; j++)
		{
			BigInteger partOne = DGKOperations.encrypt(pubKey, (long)j);
			partOne = DGKOperations.DGKAdd(pubKey, partOne, add);
			System.out.println(DGKOperations.decrypt(pubKey, privKey, partOne));
		}
		
		for (int j=500; j < 505; j++)
		{
			BigInteger partOne = DGKOperations.encrypt(pubKey, (long)j);
			partOne = DGKOperations.DGKMultiply(pubKey, partOne, (long)2);
			System.out.println(DGKOperations.decrypt(pubKey, privKey, partOne));
			//Should see 500*2, 501*2, 502*2, 503*2, 504*2
		}
		
//		ArrayList<BigInteger> compare = DGKOperations.Protocol3(90, test, pubKey);
//		long de;
//		int gammaB;
//		for (int i=0;i<compare.size();i++)
//		{
//			de = DGKOperations.decrypt(pubKey, privKey, compare.get(i));
//			if (de == 0)
//			{
//				gammaB=1;
//			}
//		}
//		gammaB = 0;
//		if (gammaA == gammaB)
//		{
//			System.out.println("XOR Result: 0, x > y");
//		}
//		else
//		{
//			System.out.println("XOR Result: 0, x <= y");
//		}
	}
}//END OF CLASS