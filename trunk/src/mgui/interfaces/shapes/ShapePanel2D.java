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

import java.awt.Point;
import java.util.ArrayList;

import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.graphics.util.PickInfoShape2D;

/****************************************
 * Interface for panels hosting 2D shapes
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface ShapePanel2D {

	/*****************************************
	 * Searches the currently displayed shapes for an intersection with <code>point</code>.
	 * Returns a list of {@PickInfoShape2D} objects, sorted by the distance to a shape's nearest vertex.
	 * 
	 * @param point
	 * @return a list of {@PickInfoShape2D} objects, sorted by the distance to a shape's nearest vertex.
	 */
	public ArrayList<PickInfoShape2D> getPickShapes(Point point);
	
	/*****************************************
	 * Returns the drawing engine for this panel
	 * 
	 * @return
	 */
	public DrawingEngine getDrawingEngine();
	
	
}
