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

package mgui.interfaces.shapes.util;

/***********************************
 * Specifies an object that listens to events on an {@linkplain InterfaceShape}.
 * 
 * 
 * @author Andrew Reid
 *
 */
public interface ShapeListener {

	/******************************
	 * A shape that this object is listening to has been updated. The specific type of event is
	 * defined by {@linkplain ShapeEvent.EventType}.
	 * 
	 * @param event
	 */
	public void shapeUpdated(ShapeEvent event);
	
	/**********************
	 * Returns {@code true} if this listener has been destroyed.
	 * 
	 * @return
	 */
	public boolean isDestroyed();
	
	/**********************
	 * Destroys this listener. Once this is called, {@linkplain isDestroyed()} must return {@code true}. 
	 * 
	 */
	public void destroy();
	
}