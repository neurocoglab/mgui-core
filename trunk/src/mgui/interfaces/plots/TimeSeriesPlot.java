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

package mgui.interfaces.plots;

import mgui.interfaces.io.DataSourceListener;
import mgui.numbers.MguiNumber;

/*********************************************
 * Interface for an X-Y plot with multiple Y variables.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface TimeSeriesPlot<T extends MguiNumber> extends DataSourceListener {

	/******************************************
	 * Set a time series data source for this plot.
	 * 
	 * @param source
	 */
	public void setDataSource(PlotTimeSeriesDataSource<T> source);
	
}