package edu.fiu.reu2017;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Andrew on 7/6/2017.
 */

public class SendTrainingArray implements Serializable
{
    private final Double [] Xcoordinate;
    private final Double [] Ycooridnate;
    private final String [] MACAddress;
    private final Integer [] RSS;

    private static final long serialVersionUID = 3907495506938576258L;

    public SendTrainingArray(Double [] x, Double [] y, String [] m, Integer [] in)
    {
        Xcoordinate = x;
        Ycooridnate = y;
        MACAddress = m;
        RSS = in;
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

    public Double [] getX () {return Xcoordinate;}
    public Double [] getY () {return Ycooridnate;}
    public String [] getMACAddress () {return MACAddress;}
    public Integer [] getRSS () {return RSS;}
}

