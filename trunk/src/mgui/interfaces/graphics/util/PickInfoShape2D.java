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

package mgui.interfaces.graphics.util;

import mgui.interfaces.shapes.Shape2DInt;

/*********************************************
 * Provides information about a {@link Shape2DInt} object which has been intersected with a pick 
 * operation.
 * 
 * @author Andrew Reid
 *
 */
public class PickInfoShape2D implements Comparable<PickInfoShape2D> {

	public float distance_to_closest;
	public Shape2DInt shape;
	public int closest_vertex;
	
	public PickInfoShape2D(Shape2DInt shape, int closest_vertex, float distance_to_closest){
		this.shape = shape;
		this.closest_vertex = closest_vertex;
		this.distance_to_closest = distance_to_closest;
	}
	
	public int compareTo(PickInfoShape2D shape){
		if (distance_to_closest < shape.distance_to_closest) return -1;
		if (distance_to_closest > shape.distance_to_closest) return 1;
		return 0;
	}
}