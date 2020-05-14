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

import mgui.interfaces.shapes.Shape3DInt;

/***********************************************************
 * Used as a user object for a Java3D Node representing a single vertex. Stores
 * a pointer to the shape and its vertex index.
 * 
 * @author Andrew Reid
 * @since 1.0
 * @version 1.0
 *
 */
public class ShapeVertexObject {

	protected Shape3DInt shape;
	protected int index;
	
	public ShapeVertexObject(Shape3DInt shape, int index){
		this.shape = shape;
		this.index = index;
	}
	
	public Shape3DInt getShape(){
		return shape;
	}
	
	public int getIndex(){
		return index;
	}
	
}