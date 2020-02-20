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

package mgui.geometry.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.vecmath.Point3f;

import mgui.interfaces.shapes.Shape3DInt;

/*************************************************************************
 * Convenience class for sorting a shape's vertices in order of their x, y, and z coordinates,
 * respectively.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SortedShape3D {

	protected ArrayList<Integer> sorted_x, sorted_y, sorted_z;
	
	public SortedShape3D(Shape3DInt shape){
		
		ArrayList<Point3f> vertices = shape.getVertices();
		ArrayList<Vertex> x = new ArrayList<Vertex>();
		ArrayList<Vertex> y = new ArrayList<Vertex>();
		ArrayList<Vertex> z = new ArrayList<Vertex>();
		for (int i = 0; i < vertices.size(); i++){
			x.add(new Vertex(i, vertices.get(i)));
			y.add(new Vertex(i, vertices.get(i)));
			z.add(new Vertex(i, vertices.get(i)));
			}
		
		Collections.sort(x, new VertexComparatorX());
		Collections.sort(y, new VertexComparatorY());
		Collections.sort(z, new VertexComparatorZ());
		
		sorted_x = new ArrayList<Integer>();
		for (int i = 0; i < x.size(); i++)
			sorted_x.add(x.get(i).index);
		
		sorted_y = new ArrayList<Integer>();
		for (int i = 0; i < y.size(); i++)
			sorted_y.add(y.get(i).index);
		
		sorted_z = new ArrayList<Integer>();
		for (int i = 0; i < z.size(); i++)
			sorted_z.add(z.get(i).index);
		
	}
	
	public ArrayList<Integer> getSortedX(){
		return sorted_x;
	}
	
	public ArrayList<Integer> getSortedY(){
		return sorted_y;
	}
	
	public ArrayList<Integer> getSortedZ(){
		return sorted_z;
	}
	
	public static class VertexComparatorX implements Comparator<Vertex>{
		public int compare(Vertex n1, Vertex n2) {
			return GeometryFunctions.compareFloat(n1.pt.x, n2.pt.x);
		}
	}
	
	public static class VertexComparatorY implements Comparator<Vertex>{
		public int compare(Vertex n1, Vertex n2) {
			return GeometryFunctions.compareFloat(n1.pt.y, n2.pt.y);
		}
	}
	
	public static class VertexComparatorZ implements Comparator<Vertex>{
		public int compare(Vertex n1, Vertex n2) {
			return GeometryFunctions.compareFloat(n1.pt.z, n2.pt.z);
		}
	}
	
	public static class Vertex{
		
		public Point3f pt;
		public int index;
		
		public Vertex(int index, Point3f pt){
			this.index = index;
			this.pt = pt;
		}
		
	}
	
}