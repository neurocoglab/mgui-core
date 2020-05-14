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

package mgui.geometry;

import java.util.ArrayList;

import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Tuple2d;
import org.jogamp.vecmath.Vector2f;

import mgui.geometry.util.GeometryFunctions;

/**********************************************
 * Represents a line segment in R2.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class LineSegment2D extends Shape2D {

	public Point2f pt1;
	public Point2f pt2;
	
	public LineSegment2D(){
		pt1 = new Point2f();
		pt2 = new Point2f();
	}
	
	public LineSegment2D(Point2f thisPt1, Point2f thisPt2){
		pt1 = thisPt1;
		pt2 = thisPt2;
	}
	
	@Override
	public Object clone(){
		return new LineSegment2D(new Point2f((Tuple2d)(pt1.clone())), new Point2f((Tuple2d)(pt2.clone())));
	}
	
	@Override
	public ArrayList<Point2f> getVertices(){
		ArrayList<Point2f> nodes = new ArrayList<Point2f>();
		nodes.add(pt1);
		nodes.add(pt2);
		return nodes;
	}
	
	public void setVertices(ArrayList<Point2f> vertices){
		this.pt1 = new Point2f(vertices.get(0));
		this.pt2 = new Point2f(vertices.get(1));
	}
	
	@Override
	public Point2f getVertex(int i) {
		if (i < 0 || i > 1) return null;
		if (i == 0) return new Point2f(pt1);
		return new Point2f(pt2);
	}
	
	public float getLength(){
		return pt1.distance(pt2);
	}
	
	@Override
	public Point2f getProximityPoint(Point2f thisPt){
		//check for oblique angles
		if (GeometryFunctions.isOblique(GeometryFunctions.getAngle(thisPt, pt1, pt2)) ||
			GeometryFunctions.isOblique(GeometryFunctions.getAngle(thisPt, pt2, pt1)))
			return null;
		float u = ((thisPt.x - pt2.x) * (pt1.x - pt2.x) + 
					(thisPt.y - pt2.y) * (pt1.y - pt2.y)) /
					(float)Math.pow(GeometryFunctions.getDistance(pt1, pt2), 2);;
		
		return new Point2f(pt2.x + u * (pt1.x - pt2.x), 
						   pt2.y + u * (pt1.y - pt2.y));
	}
	
	@Override
	public float getProximity(Point2f thisPt){
		//determine perpendicular distance from each edge to this point
		Point2f proxPt = getProximityPoint(thisPt);
		if (proxPt == null)
			return -1;
		return thisPt.distance(getProximityPoint(thisPt));
		//return GeometryFunctions.getDistance(thisPt, getProximityPoint(thisPt)); 
	}
	
	public Vector2D asVector(){
		Vector2f v = new Vector2f(pt2);
		v.sub(pt1);
		Vector2D vector = new Vector2D(new Point2f(pt1), v);
		return vector;
	}
	
}