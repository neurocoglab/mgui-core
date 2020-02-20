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

/**********************************************************
 * Represents an general output stream in modelGUI, of a specific data type.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <T> Number type of this stream
 */
public interface DataOutputStream<T extends MguiNumber> extends DataSourceListener {

	/************************************
	 * Sets the data for this output stream.
	 * 
	 * @param data
	 */
	public void setData(List<T> data) throws IOException;
	
	/************************************
	 * Sets the data for this output stream as a </code>byte</code> array.
	 * 
	 * @param d
	 */
	public void setDataAsBytes(byte[] d) throws IOException;
	
	/*************************************
	 * Closes this output stream.
	 * 
	 */
	public void close();
	
}