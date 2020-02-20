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

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/***********************************
 * Represents a 3D triangle.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Triangle3D extends Shape3D {

	

	public Point3f A;
	public Point3f B;
	public Point3f C;
	
	public Triangle3D(){
		
	}
	
	public Triangle3D(Point3f p1, Point3f p2, Point3f p3){
		A = p1;
		B = p2;
		C = p3;
	}
	
	public Plane3D getPlane(){
		Plane3D plane = new Plane3D();
		plane.origin = new Point3f(A);
		Vector3f v = new Vector3f(B);
		v.sub(A);
		v.normalize();
		plane.xAxis = v; // new Vector3f(v);
		v = new Vector3f(C);
		v.sub(A);
		v.cross(v, plane.xAxis);
		plane.yAxis.cross(plane.xAxis, v);
		return plane;
	}
	
	@Override
	public ArrayList<Point3f> getVertices(){
		ArrayList<Point3f> nodes = new ArrayList<Point3f>(3);
		nodes.add(A);
		nodes.add(B);
		nodes.add(C);
		return nodes;
	}
	
	/********************
	 * Returns edge lengths as an array ordered by: |AB|,|BC|,|AC|.
	 * 
	 * @return
	 */
	public float[] getEdgeLengths(){
		float[] lengths = new float[3];
		Vector3f v = new Vector3f(A);
		v.sub(B);
		lengths[0] = v.length();
		v.set(B);
		v.sub(C);
		lengths[1] = v.length();
		v.set(C);
		v.sub(A);
		lengths[2] = v.length();
		return lengths;
	}
	
	/**********************
	 * Returns face angles as an array ordered by: ∠A, ∠B, ∠C
	 * 
	 * @return
	 */
	public float[] getAngles(){
		float[] angles = new float[3];
		Vector3f v1 = new Vector3f(B);
		v1.sub(A);
		Vector3f v2 = new Vector3f(C);
		v2.sub(A);
		angles[0] = v1.angle(v2);
		v1.set(A);
		v1.sub(B);
		v2.set(C);
		v2.sub(B);
		angles[1] = v1.angle(v2);
		v1.set(A);
		v1.sub(C);
		v2.set(B);
		v2.sub(C);
		angles[2] = v1.angle(v2);
		return angles;
	}
	
	/********************
	 * Returns edges as an array of vectors ordered by: AB,BC,CA.
	 * 
	 * @return
	 */
	public Vector3f[] getEdges(){
		Vector3f[] edges = new Vector3f[3];
		edges[0] = new Vector3f(B);
		edges[0].sub(A);
		edges[1] = new Vector3f(C);
		edges[1].sub(B);
		edges[2] = new Vector3f(A);
		edges[2].sub(C);
		return edges;
	}
	
	@Override
	public Point3f getVertex(int i){
		switch(i){
			case 0: return A;
			case 1: return B;
			case 2: return C;
			}
		return null;
	}
	
	/**************************************
	 * Determines whether <code>p</code> is contained by this triangle. Assumes that 
	 * <code>p</code> is conplanar with this triangle.
	 * 
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
	 * 
	 */
	@Override
	public boolean contains(Point3f p){
		return same_side(p, A, B, C) && same_side(p, B, A, C) && same_side(p, C, A, B);
	}
	
	private boolean same_side(Point3f p1, Point3f p2, Point3f a, Point3f b){
		
		Vector3f v1 = new Vector3f(b);
		v1.sub(a);
		Vector3f v2 = new Vector3f(p1);
		v2.sub(a);
		Vector3f v3 = new Vector3f(p2);
		v3.sub(a);
		Vector3f cp1 = new Vector3f();
		cp1.cross(v1, v2);
		Vector3f cp2 = new Vector3f();
		cp2.cross(v1, v3);
		
		return cp1.dot(cp2) >= 0;
	}
	
	@Override
	public String toString(){
		return   "A: (" + A.x + ", " + A.y + ", " + A.z +
			   ") B: (" + B.x + ", " + B.y + ", " + B.z + ")" +
			   ") C: (" + C.x + ", " + C.y + ", " + C.z + ")"; 
	}
	
	
	@Override
	public float[] getCoords() {
		float[] coords = new float[9];
		coords[0] = A.x;
		coords[1] = A.y;
		coords[2] = A.z;
		coords[3] = B.x;
		coords[4] = B.y;
		coords[5] = B.z;
		coords[6] = C.x;
		coords[7] = C.y;
		coords[8] = C.z;
		return coords;
	}

	@Override
	public void setCoords(float[] coords) {
		if (coords.length != 9) return;
		A = new Point3f();
		A.x = coords[0];
		A.y = coords[1];
		A.z = coords[2];
		B = new Point3f();
		B.x = coords[3];
		B.y = coords[4];
		B.z = coords[5];
		C = new Point3f();
		C.x = coords[6];
		C.y = coords[7];
		C.z = coords[8];
	}

	@Override
	public void setVertices(ArrayList<Point3f> nodes) {
		if (nodes.size() != 3) return;
		A = new Point3f(nodes.get(0));
		B = new Point3f(nodes.get(1));
		C = new Point3f(nodes.get(2));
	}
	
}