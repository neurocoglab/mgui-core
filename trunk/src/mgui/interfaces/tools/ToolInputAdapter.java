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

package mgui.interfaces.tools;

import java.util.ArrayList;

/**************************************
 * Abstract class providing a listener interface for all Tool input events. 
 * 
 * @author Andrew Reid
 *
 */
public abstract class ToolInputAdapter {

	//	Types of tool events
	/**@TODO replace with enum **/
	
	
	protected ArrayList<ToolInputListener> listeners = new ArrayList<ToolInputListener>();
	
	public void fireToolEvent(ToolInputEvent e){
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).handleToolEvent(e);
	}
	
	public void addListener(ToolInputListener thisListener){
		listeners.add(thisListener);
	}
	
	public void removeListener(ToolInputListener thisListener){
		listeners.remove(thisListener);
	}
	
	public abstract ToolMouseAdapter getMouseAdapter();
	
}