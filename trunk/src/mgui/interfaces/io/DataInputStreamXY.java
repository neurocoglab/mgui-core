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

package mgui.interfaces.io;

import java.io.IOException;
import java.util.List;

import mgui.numbers.MguiNumber;

/********************************************************
 * Represents an XY input stream with one X and multiple Y channels.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <T>
 */
public interface DataInputStreamXY<T extends MguiNumber> extends DataInputStream<T> {

	/**************************************
	 * Returns the X data as a value of type <code>T</code>.
	 * 
	 * @return
	 */
	public List<T> getXData() throws IOException;
	
	/**************************************
	 * Returns the Y data for all channels, as a list of type <code>T</code>.
	 * 
	 * @param i
	 * @return
	 */
	public List<List<T>> getYData() throws IOException;
	
	/**************************************
	 * Returns the Y data for the i'th channel, as type <code>T</code>.
	 * 
	 * @param i
	 * @return
	 */
	public List<T> getYData(int i) throws IOException;
	
	/**************************************
	 * Returns the number of Y channels in this stream.
	 * 
	 * @return
	 */
	public int getChannelCount() ;
	
}