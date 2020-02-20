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

package mgui.models.dynamic;

import java.util.ArrayList;

import mgui.interfaces.InterfaceObject;


/**************************************
 * Interface which must be implemented by all components of a dynamic model. It must also
 * be able to update itself from an enviroment state, via its updateFromEnviroment method.
 * This interface extends TimeStepListener, so a dynamic component must also be able to
 * update itself as a function of a given interval of time elapsed from its current state.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public interface DynamicModelComponent extends TimeStepListener,
											   Updateable,
											   Comparable<DynamicModelComponent>,
											   Cloneable,
											   InterfaceObject {

	public void addToEngine(DynamicModelEngine e);
	public void addEvent(DynamicModelEvent e);
	public void addConnection(DynamicModelComponent c);
	public void removeConnection(DynamicModelComponent c);
	//public void setUpdater(DynamicModelUpdater u);
	//public boolean updateFromEnvironment(DynamicModelEnvironment environment);
	/**************************
	 * Executes any events in this component's event cue and clears the cue.
	 * @return true if successful, false if errors encountered
	 */
	public boolean executeEvents(double step);
	
	//does this component have subcomponents?
	public boolean hasSubComponents();
	
	//get ArrayList of complete subcomponent tree
	public ArrayList<DynamicModelComponent> getSubComponents();
	
	//get/set unique identifier (used for sorting/searching)
	public long getID();
	public void setID(long id);
	public void setID(long id, boolean update);
	public void reset();
	public Object clone();
	
}