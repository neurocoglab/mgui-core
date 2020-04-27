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

import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Vector2f;

import mgui.geometry.util.GeometryFunctions;

/********************************************
 * Represents an ellipse shape.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Ellipse2D extends Shape2D {

	Point2f center;
	Vector2f axis_a, axis_b;

	/*****************************
	 * Creates an ellipse centered on (0,0) with uniform radius of 1.0.
	 * 
	 */
	public Ellipse2D(){
		this(new Point2f(), new Vector2f(1,0), 1);
	}
	
	/******************************
	 * Creates an ellipsed centered on <code>center</code>, with given "a" axis and perpendicular
	 * "b" axis of magnitude <code>b</code>. 
	 * 
	 * @param center		Center point of ellipse
	 * @param axis_a		Primary "a" axis
	 * @param b				Magnitude of secondary "b" axis (perpendicular to a)
	 */
	public Ellipse2D(Point2f center, Vector2f axis_a, float b){
		this.center = center;
		this.axis_a = axis_a;
		setAxisB(b);
		
	}
	
	public Vector2f getAxisA(){
		return new Vector2f(axis_a);
	}
	
	public Vector2f getAxisB(){
		return new Vector2f(axis_b);
	}
	
	public Point2f getCenter(){
		return new Point2f(center);
	}
	
	public void setAxisB(float b){
		Vector2D v = new Vector2D(center, axis_a);
		GeometryFunctions.rotate(v, center, Math.PI / 2.0);
		axis_b = v.getVector();
		axis_b.normalize();
		axis_b.scale(b);
	}
	
	@Override
	public Point2f getVertex(int i) {
		if (i < 0 || i > 2) return null;
		Point2f p;
		switch (i){
			case 0: return new Point2f(center);
			case 1: 
				p = new Point2f(center);
				p.add(axis_a);
				return p;
			case 2:
				p = new Point2f(center);
				p.add(axis_b);
				return p;
			}
		return null;
	}

	@Override
	public ArrayList<Point2f> getVertices() {
		ArrayList<Point2f> vertices = new ArrayList<Point2f>();
		vertices.add(new Point2f(center));
		Point2f p = new Point2f(center);
		p.add(axis_a);
		vertices.add(p);
		p = new Point2f(center);
		p.add(axis_b);
		vertices.add(p);
		p = new Point2f(center);
		p.sub(axis_a);
		vertices.add(p);
		p = new Point2f(center);
		p.sub(axis_b);
		vertices.add(p);
		return vertices;
	}

	public void setVertices(ArrayList<Point2f> vertices){
		
		this.center = new Point2f(vertices.get(0));
		axis_a = new Vector2f(vertices.get(1));
		axis_a.sub(center);
		axis_b = new Vector2f(vertices.get(2));
		axis_b.sub(center);
		
	}
	
}