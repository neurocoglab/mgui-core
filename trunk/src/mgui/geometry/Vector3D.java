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

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

/***********************************
 * Represents a 3D vector, defined by an end-point and a vector.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Vector3D extends Shape3D {

	

	public Point3f start;
	public Vector3f vector;
	
	public Vector3D(Point3f start, Vector3f vector){
		this.start = new Point3f(start);
		this.vector = new Vector3f(vector);
	}
	
	public Vector3D(float x_start,  float y_start,  float z_start, 
					float x_vector, float y_vector, float z_vector){
		start = new Point3f(x_start, y_start, z_start);
		vector = new Vector3f(x_vector, y_vector, z_vector);
	}
	
	public Vector3D(Vector3D vector){
		this.start = new Point3f(vector.start);
		this.vector = new Vector3f(vector.vector);
	}
	
	public Point3f getStart(){
		return new Point3f(start);
	}
	
	public void setStart(Point3f p){
		this.start = p;
	}
	
	public Point3f getEndPoint(){
		Point3f p = new Point3f(start);
		p.add(vector);
		return p;
	}
	
	public Vector3f getVector(){
		return new Vector3f(vector);
	}
	
	public void setVector(Vector3f v){
		this.vector = v;
	}
	
	@Override
	public ArrayList<Point3f> getVertices(){
		ArrayList<Point3f> nodes = new ArrayList<Point3f>();
		Point3f p = new Point3f(start);
		nodes.add(p);
		p = new Point3f(start);
		p.add(vector);
		nodes.add(p);
		return nodes;
	}
	
	public float getLength(){
		return vector.length();
	}
	
	@Override
	public float[] getCoords(){
		
		float[] coords = new float[6];
		coords[0] = start.x;
		coords[1] = start.y;
		coords[2] = start.z;
		Point3f p = new Point3f(start);
		p.add(vector);
		coords[3] = p.x;
		coords[4] = p.y;
		coords[5] = p.z;
		return coords;
		
	}
	
	@Override
	public Point3f getVertex(int index) {
		return getVertices().get(index);
	}

	@Override
	public void setCoords(float[] coords) {
		if (coords.length != 6) return;
		start = new Point3f(coords[0], coords[1], coords[2]);
		vector = new Vector3f(coords[3], coords[4], coords[5]);
		vector.sub(start);
	}

	@Override
	public void setVertices(ArrayList<Point3f> nodes) {
		if (nodes.size() != 2) return;
		start = new Point3f(nodes.get(0));
		vector = new Vector3f(nodes.get(1));
		vector.sub(start);
	}
	
	@Override
	public Object clone(){
		return new Vector3D(new Point3f(start), new Vector3f(vector));
	}
	
	
}