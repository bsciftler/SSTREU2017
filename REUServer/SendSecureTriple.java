package edu.fiu.reu2017;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;

/**
 * Created by Andrew on 6/28/2017.
 */

public class SendSecureTriple implements Serializable
{
    public final boolean isPaillier;
    private final ArrayList<String> MACAddress;
    private final BigInteger [] S2;
    private final BigInteger [] S3;
    private final DGKPublicKey pk;
    private final DGKPrivateKey sk;
    private static final long serialVersionUID = 201194517759072124L;

    public SendSecureTriple(ArrayList<String> first, BigInteger [] second, BigInteger [] third,
                            boolean isPail, DGKPublicKey p, DGKPrivateKey s)
    {
        MACAddress = first;
        S2 = second;
        S3 = third;
        isPaillier = isPail;
        pk = p;
        sk = s;
    }

    public SendSecureTriple(ArrayList<String> first, BigInteger [] second, BigInteger [] third, boolean isPail)
    {
        MACAddress = first;
        S2 = second;
        S3 = third;
        isPaillier = isPail;
        sk = null;
        pk = null;
    }
    
    public SendSecureTriple(ArrayList<String> first, BigInteger [] second, BigInteger [] third,
    		boolean isPail, DGKPublicKey p)
    {
    	MACAddress = first;
    	S2 = second;
    	S3 = third;
    	isPaillier = isPail;
    	pk = p;
    	sk = null;
    }

    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
    {
        //always perform the default de-serialization first
        aInputStream.defaultReadObject();
    }

    /**
     * This is the default implementation of writeObject.
     * Customise if necessary.
     */
    private void writeObject(ObjectOutputStream aOutputStream) throws IOException
    {
        //perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
    }

    public ArrayList<String> getFirst() { return MACAddress; }
    public BigInteger [] getSecond() { return S2; }
    public BigInteger [] getThird() { return S3; }
    public DGKPrivateKey getDGKPrivateKey() {return sk;}
    public DGKPublicKey getDGKPublicKey() {return pk;}
    public boolean getType() {return isPaillier;}
}