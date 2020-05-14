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

package mgui.interfaces.shapes;

import java.util.EventObject;

/****************************************************
 * Represents an event on a {@linkplain ShapeModel3D} object. Events are defined for the
 * following changes:
 * 
 * <ul>
 * <li>ModelSetChanged: generic (unspecified) change occurred
 * <li>ModelDestroyed: the model's {@code destroy} method was called
 * <li>NameChanged: the model's name was changed
 * </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeModelEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7726940386266127068L;

	public enum EventType{
		ModelSetChanged,
		ModelDestroyed,
		NameChanged;
		
	}
	
	public EventType type = EventType.ModelSetChanged;
	
	public ShapeModelEvent(ShapeModel3D model, EventType type){
		super(model);
		this.type = type;
	}
	
}