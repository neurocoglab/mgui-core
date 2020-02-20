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

package mgui.interfaces.shapes.queries;

import mgui.interfaces.queries.InterfaceQuery;
import mgui.interfaces.queries.InterfaceQueryObject;
import mgui.interfaces.shapes.InterfaceShape;

/******************************************
 * Represents a specific vertex in a shape, for vertex-wise queries.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class QueryShapeVertex implements InterfaceQueryObject, Comparable<QueryShapeVertex> {

	protected InterfaceShape shape;
	protected int index;
	
	public QueryShapeVertex(InterfaceShape shape, int index){
		this.shape = shape;
		this.index = index;
	}
	
	@Override
	public boolean queryObject(InterfaceQuery query) {
		// TODO Auto-generated method stub
		return false;
	}

	public InterfaceShape getShape(){
		return shape;
	}
	
	public int getIndex(){
		return index;
	}
	
	@Override
	public int compareTo(QueryShapeVertex v) {
		if (!v.getShape().equals(shape)) return shape.compareTo(v.getShape());
		return ((Integer)index).compareTo(v.getIndex());
	}
	
}