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

package mgui.interfaces.graphics;

import java.util.EventObject;

/***********************************************************
 * Event on an {@link InterfaceGraphic} objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class GraphicEvent extends EventObject {

	EventType type = EventType.General;
	
	public enum EventType{
		General,
		Redrawn,
		Modified,
		Updated,
		NewSource,
		Destroyed;
	}
	
	public GraphicEvent(InterfaceGraphic<?> graphic){
		super(graphic);
	}
	
	public GraphicEvent(InterfaceGraphic<?> graphic, EventType type){
		super(graphic);
		this.type = type;
	}
	
	public EventType getType(){
		return type;
	}
	
	public InterfaceGraphic<?> getGraphic(){
		return (InterfaceGraphic<?>)getSource();
	}
	
}