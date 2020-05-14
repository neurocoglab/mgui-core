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

package mgui.geometry.polygon;

import java.util.Iterator;

import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.ShapeSet2DInt;


	/****************************
	 * 
	 * 25 July, 2009
	 * 
	 * This file is part of modelGUI[core] (mgui-core).
	 * 
	 * modelGUI[core] is free software: you can redistribute it and/or modify
	 * it under the terms of the GNU General Public License as published by
	 * the Free Software Foundation, either version 3 of the License, or
	 * (at your option) any later version.
	 *  
	 * modelGUI[core] is distributed in the hope that it will be useful,
	 * but WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	 * GNU General Public License for more details.
	 * 
	 * You should have received a copy of the GNU General Public License
	 * along with modelGUI[core].  If not, see <http://www.gnu.org/licenses/>.
	 * 
	 ****************************
	 * 
	 * Static function class to perform operations on 2D and 3D polygons.
	 * @author Andrew Reid
	 *
	 */


public class PolygonFunctions {

	public enum PolyDirection{
		Clockwise,
		CounterClockwise;
	}
	
	/****************************
	 * 
	 * Sets the polygon direction of each polygon in <code>polygons</code> to the specified
	 * direction.
	 * 
	 * @param polygons
	 * @param direction
	 */
	public static void setPolygonDirections(SectionSet3DInt polygons, PolyDirection direction){
		
		Iterator<ShapeSet2DInt> sections = polygons.sections.values().iterator();
		
		while (sections.hasNext()){
			ShapeSet2DInt polys = sections.next().getShapeType(new Polygon2DInt());
			for (Shape2DInt poly : polys.members){
				Polygon2DInt poly2D = (Polygon2DInt)poly;
				boolean clockwise = GeometryFunctions.isClockwise(poly2D.getPolygon());
				switch (direction){
					case Clockwise:
						if (!clockwise) poly2D.setShape(GeometryFunctions.getReversePolygon(poly2D.getPolygon()));
						break;
					case CounterClockwise:
						if (clockwise) poly2D.setShape(GeometryFunctions.getReversePolygon(poly2D.getPolygon()));
						break;
					}
				}
			}
		
	}
	
	
	
}