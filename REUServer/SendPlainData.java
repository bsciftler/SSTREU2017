package edu.fiu.reu2017;

/**
 * Created by Andrew on 7/6/2017.
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class SendPlainData implements Serializable
{
    private static final long serialVersionUID = 8710640030657899020L;
    private ArrayList<String> MACAddress;
    private ArrayList<Integer> RSS;

    public SendPlainData(ArrayList<String> first, ArrayList<Integer> rs)
    {
        MACAddress = first;
        RSS = rs;
    }

    public ArrayList<String> getMAC() {return MACAddress;}
    public ArrayList<Integer> getRSS() {return RSS;}

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
}
