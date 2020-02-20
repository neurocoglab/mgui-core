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

package mgui.geometry;

import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Tuple2f;

/**********************************************
 * Represents a 2D circle.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Circle2D extends Shape2D {

	public Point2f centerPt;
	public float radius;
	
	public Circle2D(){
		centerPt = new Point2f();
	}
	
	public Circle2D(Point2f thisCenter, float thisRadius){
		centerPt = thisCenter;
		radius = thisRadius;
	}
	
	public Circle2D(float x, float y, float thisRadius){
		centerPt = new Point2f(x, y);
		radius = thisRadius;
	}
	
	@Override
	public Point2f getVertex(int i) {
		if (i != 0)
			return null;
		return new Point2f(centerPt);
	}

	@Override
	public ArrayList<Point2f> getVertices() {
		ArrayList<Point2f> list = new ArrayList<Point2f>();
		list.add(new Point2f(centerPt));
		return list;
	}
	
	public void setVertices(ArrayList<Point2f> n){
		centerPt = new Point2f(n.get(0));
	}
	
	@Override
	public Object clone(){
		return new Circle2D(new Point2f((Tuple2f)centerPt.clone()), radius);
	}
	
}