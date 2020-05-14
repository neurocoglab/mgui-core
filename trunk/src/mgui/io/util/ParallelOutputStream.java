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

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/***********************************************
 * Simple class which writes to multiple output streams
 * 
 * @author Andrew Reid
 *
 */
public class ParallelOutputStream extends OutputStream {

	ArrayList<OutputStream> streams = new ArrayList<OutputStream>();

	public ParallelOutputStream(){
		
	}
	
	public ParallelOutputStream(ArrayList<OutputStream> streams){
		this.streams = streams;
	}
	
	public void addStream(OutputStream stream){
		for (int i = 0; i < streams.size(); i++)
			if (streams.get(i) == stream)
				return;
		streams.add(0, stream);
	}
	
	public void removeStream(OutputStream stream){
		streams.remove(stream);
	}
	
	public ArrayList<OutputStream> getStreams(){
		return streams;
	}
	
	@Override
	public void write(int b) throws IOException {
		for (int i = 0; i < streams.size(); i++)
			streams.get(i).write(b);
	}
	
	@Override
	public void flush() throws IOException {
		for (int i = 0; i < streams.size(); i++)
			streams.get(i).flush();
    }

	@Override
	public void close() throws IOException {
		for (int i = 0; i < streams.size(); i++)
			streams.get(i).close();
	}
	
	
}