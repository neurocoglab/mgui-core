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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.jogamp.vecmath.Point3f;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;

/*************************
 * Geometry class for a polygon in R3. Nodes are stored in a Point3f[] array,
 * such that a reference to this array can be passed to a Java3D node. The number 
 * of nodes is indicated by the field n. Note that the nodes array actually contains
 * n + 1 nodes, since the final node is the same as the first for a closed polygon.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */
public class Polygon3D extends Shape3D {

	public float[] nodes;
	public int n = 0;
	
	public Polygon3D(){
		nodes = new float[30];
		n = 0;
	}
	
	public Polygon3D(ArrayList<Point3f> nodeList){
		n = nodeList.size();
		nodes = new float[(n * 3)]; // + 3];
		for (int i = 0; i < n; i++){
			nodes[(i * 3)] = nodeList.get(i).x;
			nodes[(i * 3) + 1] = nodeList.get(i).y;
			nodes[(i * 3) + 2] = nodeList.get(i).z;
			}
		//close polygon
		//for (int i = 0; i < 3; i++)
		//	nodes[(n * 3) + i] = nodes[i];
		
		//nodes[n] = nodes[0];
	}
	
	public Polygon3D(Point3f[] nodeList){
		n = nodeList.length;
		nodes = new float[(n * 3)]; // + 3];
		for (int i = 0; i < n; i++){
			nodes[(i * 3)] = nodeList[i].x;
			nodes[(i * 3) + 1] = nodeList[i].y;
			nodes[(i * 3) + 2] = nodeList[i].z;
			}
		//close polygon
		//for (int i = 0; i < 3; i++)
		//	nodes[(n * 3) + i] = nodes[i];
	}
	
	public void addNode(Point3f node){
		resizeArray();
		for (int i = 0; i < 3; i++)
			nodes[((n + 1) * 3) + i] = nodes[i];
		nodes[(n * 3)] = node.x;
		nodes[(n * 3) + 1] = node.y;
		nodes[(n * 3) + 2] = node.z;
		
		n++;
	}
	
	private void resizeArray(){
		if (n >= (nodes.length / 3) - 1){
			//new array needed
			//Point3f[] buffer = new Point3f[n * 2];
			float[] buffer = new float[n * 6];
			System.arraycopy(nodes, 0, buffer, 0, (n * 3)); // + 1);
			nodes = buffer;
			}
	}
	
	/*********************
	 * Returns an array of n coordinates {x1,y1,z1,x2,...,zn}
	 * @return
	 */
	@Override
	public float[] getCoords(){
		return nodes;
	}
	
	/*********************
	 * Sets the nodes array from an array in the form {x1,y1,z1,x2,...,zn}
	 * The last coordinate must equal the first.
	 * TODO: check for this and add if absent
	 */
	@Override
	public void setCoords(float[] f){
		// TODO check for validity of f
		nodes = f;
		n = nodes.length / 3;
	}
	
	@Override
	public ArrayList<Point3f> getVertices(){
		ArrayList<Point3f> array = new ArrayList<Point3f>(nodes.length);
		for (int i = 0; i < n; i++){
			array.add(new Point3f(nodes[i * 3], nodes[(i * 3) + 1], nodes[(i * 3) + 2]));
			}
		return array;
	}
	
	public float getPerimeter(){
		ArrayList<Point3f> pts = getVertices();
		float distance = 0;
		for (int i = 0; i < n - 1; i++)
			distance += pts.get(i).distance(pts.get(i + 1));
		return distance;
	}
	
	@Override
	public void finalize(){
		//trim array
		//Point3f[] buffer = new Point3f[n + 1];
		
		float[] buffer = new float[n * 3];
		System.arraycopy(nodes, 0, buffer, 0, n * 3);
		//buffer[(n * 3)] = buffer[0];
		//buffer[(n * 3) + 1] = buffer[1];
		//buffer[(n * 3) + 2] = buffer[2];
		
		nodes = buffer;
	}
	
	@Override
	public void setVertices(Point3f[] nodes){
		//this.nodes = nodes;
		n = nodes.length;
		this.nodes = new float[nodes.length * 3];
		for (int i = 0; i < nodes.length; i++){
			this.nodes[i * 3] = nodes[i].x;
			this.nodes[(i * 3) + 1] = nodes[i].y;
			this.nodes[(i * 3) + 2] = nodes[i].z;
			}
		
	}
	
	@Override
	public void setVertices(ArrayList<Point3f> nodes){
		//nodes.toArray(this.nodes);
		n = nodes.size();
		this.nodes = new float[nodes.size() * 3];
		for (int i = 0; i < nodes.size(); i++){
			this.nodes[i * 3] = nodes.get(i).x;
			this.nodes[(i * 3) + 1] = nodes.get(i).y;
			this.nodes[(i * 3) + 2] = nodes.get(i).z;
			}
	}
	
	@Override
	public Point3f getVertex(int i){
		return new Point3f(nodes[i * 3], nodes[(i * 3) + 1], nodes[(i * 3) + 2]);
	}
	
	
	
}