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

/*****************************************************
 * Represents an XY output stream whose Y channels are named.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface DataOutputStreamNamedXY<T extends MguiNumber> extends DataOutputStreamXY<T> {

	/*****************************************
	 * Adds a Y channel named <code>channel</code>.
	 * 
	 * @param i
	 * @param name
	 */
	public void addChannel(int i, String channel);
	
	/*****************************************
	 * Removes the channel named <code>channel</code>.
	 * 
	 * @param name
	 */
	public void removeChannel(String channel);
	
	/*****************************************
	 * Sets the Y data for the channel named <code>channel</code>.
	 * 
	 * @param name
	 * @param data
	 */
	public void setYData(String channel, List<T> data) throws IOException;
	
	/******************************************
	 * Returns a list of the channels in this stream. The list's indices correspond
	 * to the channel index.
	 * 
	 * @return
	 */
	public List<String> getVariableNames();
	
}