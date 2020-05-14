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

package mgui.interfaces.plots.mgui;

import mgui.interfaces.io.DataSourceListener;
import mgui.interfaces.io.InterfaceDataSource;
import mgui.interfaces.plots.InterfacePlot;
import mgui.interfaces.plots.PlotException;
import mgui.numbers.MguiDouble;

/************************************************************
 * General class for displaying ModelGUI plots.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfacePlotMgui extends InterfacePlot<MguiDouble>
										implements DataSourceListener{

	
	/************************************
	 * Sets the data source for this plot
	 * 
	 * @param source
	 * @throws PlotException
	 */
	public abstract void setDataSource(InterfaceDataSource<?> source) throws PlotException;
	
	/************************************
	 * Responds to a change in this plot's data
	 * 
	 * @param event
	 */
	public abstract void dataChanged(MguiPlotEvent event);
	
	
	
}