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

package mgui.interfaces.tools;

import java.util.EventObject;

/******************************************************
 * An event on a {@linkplain Tool} object
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolEvent extends EventObject {

	public enum EventType{
		ToolActivated,
		ToolDeactivated,
		ToolDestroyed,
		Unspecified;
	}
	
	protected EventType type = EventType.Unspecified;
	
	public ToolEvent(Tool tool){
		super(tool);
	}
	
	public ToolEvent(Tool tool, EventType type){
		super(tool);
		this.type = type;
	}
	
	public Tool getTool(){
		return (Tool)getSource();
	}
	
	public EventType getType(){
		return type;
	}
	
}