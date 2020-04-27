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
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

/***********************************
 * Represents a 2D triangle.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Triangle2D extends Shape2D {

	Point2f A;
	Point2f B;
	Point2f C;
	
	public Triangle2D(){
		
	}
	
	public Triangle2D(Point2f p1, Point2f p2, Point2f p3){
		A = p1;
		B = p2;
		C = p3;
	}
	
	public Triangle2D(ArrayList<Point2f> nodes){
		A = nodes.get(0);
		A = nodes.get(1);
		A = nodes.get(2);
	}
	
	@Override
	public ArrayList<Point2f> getVertices(){
		ArrayList<Point2f> nodes = new ArrayList<Point2f>(3);
		nodes.add(A);
		nodes.add(B);
		nodes.add(C);
		return nodes;
	}
	
	public void setVertices(ArrayList<Point2f> vertices){
		A = new Point2f(vertices.get(0));
		B = new Point2f(vertices.get(1));
		B = new Point2f(vertices.get(2));
	}
	
	@Override
	public Point2f getVertex(int i){
		switch(i){
			case 0: return A;
			case 1: return B;
			case 2: return C;
			}
		return null;
	}
	
	/*******************************
	 * From http://www.blackpawn.com/texts/pointinpoly/default.html:
	 * 
	 * function SameSide(p1,p2, a,b)
	 * cp1 = CrossProduct(b-a, p1-a)
	 * cp2 = CrossProduct(b-a, p2-a)
	 * if DotProduct(cp1, cp2) >= 0 then return true
	 * else return false
	 *
	 * function PointInTriangle(p, a,b,c)
	 * if SameSide(p,a, b,c) and SameSide(p,b, a,c)
	 *    and SameSide(p,c, a,b) then return true
	 * else return false
	 */
	@Override
	public boolean contains(Point2f p){
		return same_side(p, A, B, C) && same_side(p, B, A, C) && same_side(p, C, A, B);
	}
	
	private boolean same_side(Point2f p1, Point2f p2, Point2f a, Point2f b){
		
		Vector3f v1 = new Vector3f(b.x, b.y, 0);
		Point3f a3 = new Point3f(a.x, a.y, 0);
		v1.sub(a3);
		Vector3f v2 = new Vector3f(p1.x, p1.y, 0);
		v2.sub(a3);
		Vector3f v3 = new Vector3f(p2.x, p2.y, 0);
		v3.sub(a3);
		Vector3f cp1 = new Vector3f();
		cp1.cross(v1, v2);
		Vector3f cp2 = new Vector3f();
		cp2.cross(v1, v3);
		
		return cp1.dot(cp2) >= 0;
	}
	
}