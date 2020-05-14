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

package mgui.models.dynamic;

import mgui.interfaces.InterfaceObject;

/********************************************************
 * Acts as a sensor for a model environment, which responds to state change events via the
 * <code>stimulate</code> method.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface DynamicModelEnvironmentSensor extends InterfaceObject{

	/**************************************************
	 * Notifies this sensor that the environment has changed.
	 * 
	 * @param e
	 * @return
	 */
	public boolean stimulate(DynamicModelEnvironmentEvent e);
	public void setIndex(int i);
	public int getIndex();
	public String getName();
	public void reset();
	
}