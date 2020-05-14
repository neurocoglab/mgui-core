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
import org.jogamp.vecmath.Vector2f;

/***********************************
 * Represents a 2D vector, defined by an end-point and a vector.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Vector2D extends Shape2D {

	public Point2f start;
	public Vector2f vector;
	
	public Vector2D(Point2f start, Vector2f vector){
		this.start = start;
		this.vector = vector;
	}
	
	public Vector2D(Vector2D vector){
		this.start = vector.start;
		this.vector = vector.vector;
	}
	
	public Vector2D(float x_start,  float y_start, 
					float x_vector, float y_vector){
		vector = new Vector2f(x_vector, y_vector);
		start = new Point2f(x_start, y_start);
	}
	
	public Point2f getStart(){
		return new Point2f(start);
	}
	
	public Vector2f getVector(){
		return new Vector2f(vector);
	}
	
	public Point2f getEndPoint(){
		Point2f p = new Point2f(start);
		p.add(vector);
		return p;
	}
	
	@Override
	public ArrayList<Point2f> getVertices(){
		ArrayList<Point2f> nodes = new ArrayList<Point2f>();
		Point2f p = new Point2f(start);
		nodes.add(p);
		p = new Point2f(start);
		p.add(vector);
		nodes.add(p);
		return nodes;
	}
	
	public void setVertices(ArrayList<Point2f> vertices){
		start = new Point2f(vertices.get(0));
		vector = new Vector2f(vertices.get(1));
		vector.sub(start);
	}
	
	@Override
	public Point2f getVertex(int i) {
		if (i < 0 || i > 1) return null;
		if (i == 0) return getStart();
		return getEndPoint();
	}
	
	public LineSegment2D asLineSegment(){
		return new LineSegment2D(start, getEndPoint()); 
	}
	
}