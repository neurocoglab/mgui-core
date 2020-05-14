/*
* Copyright (C) 2020 Andrew Reid and the ModelGUI Project <http://www.modelgui.org>
* 
* This file is part of ModelGUI[core] (mgui-core).
* 
* ModelGUI[core] is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* ModelGUI[core] is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with ModelGUI[core]. If not, see <http://www.gnu.org/licenses/>.
*/

package mgui.io.util;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 *  EndianCorrectOutputStream extends DataOutputStream; it will optionally flip
 *  bytes to write little endian data correctly. 
 */

public class EndianCorrectOutputStream extends DataOutputStream {

    private boolean bigendian = true;

    /**
     * Constructor for a disk file.
     * @param filename filename for datafile
     * @param be -- endian flag: if be (big endian) is false bytes will be flipped on write
     * @exception FileNotFoundException
     */
    public EndianCorrectOutputStream(String filename, boolean be) throws FileNotFoundException, SecurityException {
	super(new FileOutputStream(filename));
	bigendian = be;
    }

    /**
     * Constructor for an OutputStream.
     * @param is OutputStream to write data to
     * @param be -- endian flag: if be (big endian) is false bytes will be flipped on write
     * @exception FileNotFoundException
     */
    public EndianCorrectOutputStream(OutputStream os, boolean be)  {
	super(os);
	bigendian = be;
    }



    /**
     *  writeShortCorrect will write a short to the stream
     */
    public void writeShortCorrect(short val) throws IOException {

	if (bigendian) {
		writeShort(val);
	}
	else {	
           int byte0 = val & 0xff;
           int byte1 = (val>>8) & 0xff;
           // swap the byte order
           writeShort((short) ((byte0<<8) | (byte1))) ;
	}
	return;
    }
    /**
     *  flipShort will byte flip a short
     */
    public short flipShort(short val) {

           int byte0 = val & 0xff;
           int byte1 = (val>>8) & 0xff;
           // swap the byte order
           return (short) ((byte0<<8) | (byte1)) ;
    }


    /**
     *  writeIntCorrect will write an int to the stream
     */
    public void writeIntCorrect(int val) throws IOException {

	if (bigendian){
        	writeInt(val);
	}

	else {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(val);
		this.write(buffer.array());
//           int byte0 = val & 0xff;
//           int byte1 = (val>>8) & 0xff;
//           int byte2 = (val>>16) & 0xff;
//           int byte3 = (val>>24) & 0xff;
//           // swap the byte order
//           writeInt((byte0<<24) | (byte1<<16) | (byte2<<8) | byte3);
	}
	return;
    }

    /**
     *  flipInt will flip the byte order of an int
     */
    public int flipInt(int val) {

           int byte0 = val & 0xff;
           int byte1 = (val>>8) & 0xff;
           int byte2 = (val>>16) & 0xff;
           int byte3 = (val>>24) & 0xff;
           // swap the byte order
           return (byte0<<24) | (byte1<<16) | (byte2<<8) | byte3;
    }

    /**
     *  writeLongCorrect will write a long to the stream
     */
    public void writeLongCorrect(long val) throws IOException {

	if (bigendian){
        	writeLong(val);
	}

	else {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putLong(val);
		this.write(buffer.array());
		//writeLong(flipLong(val));
	}
	return;
    }

    /**
     *  flipLong will flip the byte order of a long
     */
    public long flipLong(long val) {

           long byte0 = val & 0xff;
           long byte1 = (val>>8) & 0xff;
           long byte2 = (val>>16) & 0xff;
           long byte3 = (val>>24) & 0xff;
           long byte4 = (val>>32) & 0xff;
           long byte5 = (val>>40) & 0xff;
           long byte6 = (val>>48) & 0xff;
           long byte7 = (val>>56) & 0xff;
           // swap the byte order
           return ((byte0<<56) | (byte1<<48) | (byte2<<40) | (byte3<<32) | (byte4<<24) | (byte5<<16)| (byte6<<8) | byte7);
    }


    /**
     *  writeFloatCorrect will write a float to the stream
     */ 
    public void writeFloatCorrect(float val) throws IOException {

        if (bigendian){
		writeFloat(val);
	}

	else {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putFloat(val);
		this.write(buffer.array());
		
		//writeFloat(flipFloat(val));
	}
	return;
    }

    /**
     *  flipFloat will flip the byte order of a float
     */ 
    public float flipFloat(float val) throws IOException {

		int x = Float.floatToIntBits(val);
		int y = flipInt(x);
	    	return Float.intBitsToFloat(y);
    }

    /**
     *  writeDoubleCorrect will write a double to the stream
     */
    public void writeDoubleCorrect(double val) throws IOException {

	if (bigendian){
	    writeDouble(val);
	}
	else {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putDouble(val);
		this.write(buffer.array());
		//writeDouble(flipDouble(val));
	}
	return;
    }
    
    /**
     *  flipDouble will flip the byte order of a double
     */ 
    public double flipDouble(double val) {

		long x = Double.doubleToLongBits(val);
		long y = flipLong(x);
	    	return Double.longBitsToDouble(y);
    }

	//////////////////////////////////////////////////////////////////
	/**
	* main routine is used only for testing
	* @param args 4 args: in_dir true/false out_dir true/false 
	*/


	/*************************************************************
	public static void main(String[] args) {
		
		double d;
		float f;
		int i;
		short s;
		byte b;
		long l;



		// double 64
		try {
		EndianCorrectInputStream ecs_in = new EndianCorrectInputStream(args[0]+File.separator+"double.dat", Boolean.valueOf(args[1]).booleanValue());
		d = ecs_in.readDoubleCorrect();
		InterfaceSession.log("read in "+d);
		ecs_in.close();

		EndianCorrectOutputStream ecs_out = new EndianCorrectOutputStream(args[2]+File.separator+"double.dat", Boolean.valueOf(args[3]).booleanValue());
		ecs_out.writeDoubleCorrect(d);
		ecs_out.close();

		}
		catch (IOException ex) {
			InterfaceSession.log("Oops ! bad double: "+ex.getMessage());
		}

		// short 16
		try {
		EndianCorrectInputStream ecs_in = new EndianCorrectInputStream(args[0]+File.separator+"short.dat", Boolean.getBoolean(args[1]));
		s = ecs_in.readShortCorrect();
		InterfaceSession.log("read in "+s);
		ecs_in.close();

		EndianCorrectOutputStream ecs_out = new EndianCorrectOutputStream(args[2]+File.separator+"short.dat", Boolean.getBoolean(args[3]));
		ecs_out.writeShortCorrect(s);
		ecs_out.close();

		}
		catch (IOException ex) {
			InterfaceSession.log("Oops ! bad short: "+ex.getMessage());
		}

		// int32
		try {
		EndianCorrectInputStream ecs_in = new EndianCorrectInputStream(args[0]+File.separator+"int.dat", Boolean.getBoolean(args[1]));
		i = ecs_in.readIntCorrect();
		InterfaceSession.log("read in "+i);
		ecs_in.close();

		EndianCorrectOutputStream ecs_out = new EndianCorrectOutputStream(args[2]+File.separator+"int.dat", Boolean.getBoolean(args[3]));
		ecs_out.writeIntCorrect(i);
		ecs_out.close();

		}
		catch (IOException ex) {
			InterfaceSession.log("Oops ! bad int: "+ex.getMessage());
		}

	return;
	}
	*************************************************************/
		

}