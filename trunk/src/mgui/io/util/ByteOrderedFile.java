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

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;

/***************************************************
 * Extends RandomAccessFile to support byte order. Why is this not already standard?
 * 
 * @author atreid
 *
 */
public class ByteOrderedFile extends RandomAccessFile {

	public ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
	
	public ByteOrderedFile(String name, String mode) throws FileNotFoundException{
		 super(name, mode);
	}
	
	public ByteOrderedFile(File file, String mode) throws FileNotFoundException{
		 super(file, mode);
	}
	
	public void setByteOrder(ByteOrder bo){
		byteOrder = bo;
	}
	
	public char readOrderedChar() throws IOException {
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN))
			return this.readChar();
		int ch1 = this.read();
		int ch2 = this.read();
		if ((ch1 | ch2) < 0)
		    throw new EOFException();
		return (char)((ch2 << 8) + (ch1 << 0));
	}

	public final int readOrderedInt() throws IOException {
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN))
			return this.readInt();
		int ch1 = this.read();
		int ch2 = this.read();
		int ch3 = this.read();
		int ch4 = this.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
		    throw new EOFException();
		return ((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0));
	}
	
	public final long readOrderedLong() throws IOException {
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN))
			return this.readLong();
		
		long a = (readOrderedInt() & 0xFFFFFFFFL);
		long b = ((long)(readOrderedInt()) << 32);
		
		return a + b;
		
	}
	
	public final float readOrderedFloat() throws IOException {
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN))
			return this.readFloat();
		
		int x = readOrderedInt();
		
		return Float.intBitsToFloat(x);
		//return Float.intBitsToFloat(readOrderedInt());
	}
	
	public final double readOrderedDouble() throws IOException {
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN))
			return this.readDouble();
		return Double.longBitsToDouble(readOrderedLong());
	}
	
	public final void writeOrderedShort(int v) throws IOException {
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN)){
			this.writeShort(v);
			return;
			}
		this.write((v >>> 0) & 0xFF);
		this.write((v >>> 8) & 0xFF);
	}
	
	public final void writeOrderedChar(int v) throws IOException {
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN)){
			this.writeChar(v);
			return;
			}
		
		this.write((v >>> 0) & 0xFF);
		this.write((v >>> 8) & 0xFF);
	}
	
	public final void writeOrderedInt(int v) throws IOException {
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN)){
			this.writeInt(v);
			return;
			}
		this.write((v >>>  0) & 0xFF);
		this.write((v >>>  8) & 0xFF);
		this.write((v >>> 16) & 0xFF);
		this.write((v >>> 24) & 0xFF);
	}
	
	public final void writeOrderedLong(long v) throws IOException {
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN)){
			this.writeLong(v);
			return;
			}
		
		this.write((int)(v >>>  0) & 0xFF);
		this.write((int)(v >>>  8) & 0xFF);
		this.write((int)(v >>> 16) & 0xFF);
		this.write((int)(v >>> 24) & 0xFF);
		this.write((int)(v >>> 32) & 0xFF);
		this.write((int)(v >>> 40) & 0xFF);
		this.write((int)(v >>> 48) & 0xFF);
		this.write((int)(v >>> 56) & 0xFF);
		
	}
	
	public final void writeOrderedFloat(float v) throws IOException {
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN)){
			this.writeFloat(v);
			return;
			}
		writeOrderedInt(Float.floatToIntBits(v));
	}
	
	public final void writeOrderedDouble(double v) throws IOException {
		if (byteOrder.equals(ByteOrder.BIG_ENDIAN)){
			this.writeDouble(v);
			return;
			}
		writeOrderedLong(Double.doubleToLongBits(v));
	}
	
}