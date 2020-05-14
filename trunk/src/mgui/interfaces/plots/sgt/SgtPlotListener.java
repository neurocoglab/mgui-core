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

package mgui.interfaces.plots.sgt;

/********************************************************
 * Defines an object which responds to events on an {@link InterfacePlotSgt}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface SgtPlotListener {

	/***************************************************
	 * Updates this plot in response to an additional data object.
	 * 
	 * @param data
	 */
	public void plotObjectAdded(SgtPlotEvent e);
	
	/***************************************************
	 * Updates this plot in response to the removal of a data object.
	 * 
	 * @param data
	 */
	public void plotObjectRemoved(SgtPlotEvent e);
	
	/***************************************************
	 * Updates this plot in response to the modification of a data object.
	 * 
	 * @param data
	 */
	public void plotObjectModified(SgtPlotEvent e);
	
}