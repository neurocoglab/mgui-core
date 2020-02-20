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

import java.util.List;

import mgui.interfaces.plots.XYData;
import mgui.numbers.MguiNumber;

/********************************************************************
 * XY data source with unique X-Y pairs. Use if X data are not constant across Y data (i.e., non-time-series,
 * scatterplots, etc.)
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <T>
 */
public interface InterfaceDataSourceXY<T extends MguiNumber> extends InterfaceDataSource<T>  {
	
	/*************************************
	 * Returns the X-Y data corresponding to this signal's i'th channel.
	 * 
	 * @param i
	 * @return
	 */
	public List<XYData<T>> getSourceSignalXY(int i);
	
	/*************************************
	 * Returns the X data corresponding to this signal's i'th channel.
	 * 
	 * @param i
	 * @return
	 */
	public List<T> getSourceSignalX(int i);
	
	/*************************************
	 * Returns the Y data corresponding to this signal's i'th channel.
	 * 
	 * @param i
	 * @return
	 */
	public List<T> getSourceSignalY(int i);
	
	/**************************************
	 * Returns the number of Y channels in this source.
	 * 
	 * @return
	 */
	public int getChannelCount(); 
	
	
}