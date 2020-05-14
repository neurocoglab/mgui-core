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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;

/**********************************************
 * Buffered version of RandomAccessFile.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class BufferedRandomAccessFile {

	public int buffer_size = 1000000;
	RandomAccessFile raf;
	ByteBuffer buffer;
	ReadableByteChannel channel;
	int last_read;
	long already_read;
	long length;
	
	public BufferedRandomAccessFile(RandomAccessFile raf){
		this(raf, 1000000, ByteOrder.nativeOrder(), 0);
	}
	
	public BufferedRandomAccessFile(RandomAccessFile raf, int buffer_size, ByteOrder byte_order, long start){
		this.raf = raf;
		this.buffer_size = buffer_size;
		buffer = ByteBuffer.allocate(buffer_size).order(byte_order);
		
		try{
			length = raf.length();
			position(start);
		}catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public int readInt() throws IOException, EOFException{
		if (isEOF()) throw new EOFException("Attempt to read past end of file! Pos: " + position() + " Length: " + length);
		if (buffer.remaining() < 4)
			rebuffer();
		return buffer.getInt();
	}
	
	public long readLong() throws IOException, EOFException{
		if (isEOF()) throw new EOFException("Attempt to read past end of file! Pos: " + position() + " Length: " + length);
		if (buffer.remaining() < 8)
			rebuffer();
		return buffer.getLong();
	}
	
	public int read() throws IOException, EOFException{
		if (isEOF()) throw new EOFException("Attempt to read past end of file! Pos: " + position() + " Length: " + length);
		if (buffer.remaining() < 1)
			rebuffer();
		return buffer.get();
	}
	
	public float readFloat() throws IOException, EOFException{
		if (isEOF()) throw new EOFException("Attempt to read past end of file! Pos: " + position() + " Length: " + length);
		if (buffer.remaining() < 4)
			rebuffer();
		return buffer.getFloat();
	}
	
	public double readDouble() throws IOException, EOFException{
		if (isEOF()) throw new EOFException("Attempt to read past end of file! Pos: " + position() + " Length: " + length);
		if (buffer.remaining() < 8)
			rebuffer();
		return buffer.getDouble();
	}
	
	public float readShort() throws IOException, EOFException{
		if (isEOF()) throw new EOFException("Attempt to read past end of file! Pos: " + position() + " Length: " + length);
		if (buffer.remaining() < 2)
			rebuffer();
		return buffer.getShort();
	}
	
	public char readChar() throws IOException, EOFException{
		if (isEOF()) throw new EOFException("Attempt to read past end of file! Pos: " + position() + " Length: " + length);
		if (buffer.remaining() < 2)
			rebuffer();
		return buffer.getChar();
	}
	
	public boolean isEOF(){
		return (last_read < 0);
	}
	
	private void rebuffer() throws IOException{
		if (buffer.remaining() > 0){
			byte[] remaining = new byte[buffer.remaining()];
			buffer.get(remaining);
			buffer.clear();
			buffer.put(remaining);
		}else{
			buffer.clear();	
			}
		last_read = channel.read(buffer);
		buffer.position(0);
		already_read += last_read;
	}
	
	public void position(long p) throws IOException{
		already_read = p;
		raf.seek(p);
		channel = raf.getChannel();
		buffer.position(buffer.capacity());
		rebuffer();
	}
	
	public void skip(int skip) throws IOException{
		int pos = buffer.position();
		if (pos + skip > buffer.capacity()){
			position(position() + skip);
			return;
			}
		buffer.position(pos + skip);
	}
	
	public long position(){
		return buffer.position() + already_read - last_read;
	}
	
	public void close() throws IOException{
		raf.close();
		buffer.clear();
	}
	
	
}