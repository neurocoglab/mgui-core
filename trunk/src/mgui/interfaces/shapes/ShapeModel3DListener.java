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

/********************************************
 * Object which listens to events on a {@linkplain ShapeModel3D} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface ShapeModel3DListener {

	/****************************************************
	 * Fired when a {@code ShapeModel3D} is changed in some way. The specific change
	 * is indicated by the {@code EventType} of the {@linkplain ShapeModelEvent}.
	 * 
	 * @param event
	 */
	public void shapeModelChanged(ShapeModelEvent event);
	
}