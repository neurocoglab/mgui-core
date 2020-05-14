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

import java.util.List;

import mgui.numbers.MguiNumber;

/**********************************************************
 * Represents an XY signal source with multiple Y channels.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <T>
 */
public interface InterfaceDataSourceTimeSeries<T extends MguiNumber> extends InterfaceDataSourceXY<T> {

	/*************************************
	 * Returns the X data for this source.
	 * 
	 * @return
	 */
	public List<T> getSourceSignalX();
	
	/*************************************
	 * Returns the Y data corresponding to this signal's i'th channel.
	 * 
	 * @param i
	 * @return
	 */
	public List<T> getSourceSignalY(int i);
	
	/*************************************
	 * Returns the Y data corresponding to all channels.
	 * 
	 * @param i
	 * @return
	 */
	public List<List<T>> getSourceSignalY();
	
	/**************************************
	 * Returns the number of Y channels in this source.
	 * 
	 * @return
	 */
	public int getChannelCount();
	
}