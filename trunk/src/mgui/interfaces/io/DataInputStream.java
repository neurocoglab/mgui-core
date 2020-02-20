/*
* Copyright (C) 2014 Andrew Reid and the ModelGUI Project <http://mgui.wikidot.com>
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

package mgui.interfaces.io;

import java.io.IOException;
import java.util.List;

import mgui.numbers.MguiNumber;

/******************************************************
 * Represents an input stream of a specific number type.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <T> Number type for this input stream.
 */
public interface DataInputStream<T extends MguiNumber> {

	/*************************************
	 * Returns a sample of the current state of this input stream.
	 * 
	 * @return
	 */
	public List<T> getData() throws IOException;
	
	/*************************************
	 * Returns a sample of the current state of this stream as a byte array.
	 * 
	 * @return
	 */
	public byte[] getDataAsBytes() throws IOException;
	
	/*************************************
	 * Closes this stream.
	 * 
	 */
	public void close();
	
	public void addInputStreamListener(DataInputStreamListener<T> l);
	public void removeInputStreamListener(DataInputStreamListener<T> l);
	
}