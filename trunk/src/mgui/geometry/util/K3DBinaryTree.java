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
import java.util.List;

import javax.vecmath.Point3f;

import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;

/*******************************************************************
 * Implements a kd-tree for 3 dimensions; specifically Euclidian points in R3. Useful, e.g., for a binary
 * nearest neighbour search. See <a href="http://en.wikipedia.org/wiki/kd-tree">http://en.wikipedia.org/wiki/kd-tree</a>.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class K3DBinaryTree {

	public K3DNode root;
	public int size;
	
	private Point3fComparatorX comparator_x ;
	private Point3fComparatorY comparator_y ;
	private Point3fComparatorZ comparator_z ;
	
	/********************************************
	 * Constructor builds a k3-tree from {@code points}
	 * 
	 * @param points
	 */
	public K3DBinaryTree(ArrayList<Point3f> points){
		
		size = points.size();
		
		comparator_x = new Point3fComparatorX();
		comparator_y = new Point3fComparatorY();
		comparator_z = new Point3fComparatorZ();
		
		ArrayList<Vertex> vertices = new ArrayList<Vertex>(points.size());
		for (int i = 0; i < points.size(); i++)
			vertices.add(new Vertex(points.get(i), i));
		
		root = addNode(vertices, 0);
		
	}

	/*********************************************************
	 * Adds nodes recursively until the entire set is assigned.
	 * 
	 * @param vertices
	 * @param depth
	 * @return
	 */
	protected K3DNode addNode(List<Vertex> vertices, int depth){
		
		if (vertices.size() == 0) return null;
		
		int axis = depth % 3;
		
		Point3fComparator comparator = null;
		
		switch(axis){
			case 0: 
				comparator = comparator_x; 
				break;
			case 1: 
				comparator = comparator_y;
				break;
			case 2: 
				comparator = comparator_z;
				break;
			}
		
		Collections.sort(vertices, comparator);
		int n = (int)((float)vertices.size() / 2f);
		
		K3DNode node = new K3DNode(vertices.get(n), axis);
		if (n > 0){
			node.child_left = addNode(new ArrayList<Vertex>(vertices.subList(0, n)), depth + 1);
			}
		
		if (n < vertices.size()){
			node.child_right = addNode(new ArrayList<Vertex>(vertices.subList(n + 1, vertices.size())), depth + 1);
			}
			
		return node;
	}
	
	public static class K3DNode{
		
		public int axis;
		public Vertex vertex;
		public K3DNode child_left, child_right;
		
		public K3DNode(Vertex vertex, int axis){
			this.vertex = vertex;
			this.axis = axis;
		}
		
		public Point3f getPoint(){
			return vertex.point;
		}
		
	}
	
	/*****************************************************
	 * Returns a list of values representing the position of each vertex in this tree.
	 * Values are determined as:
	 * 
	 * <p>All values left of the root are negative; right are positive
	 * <p>The magnitude is determined as how left a node is, times its depth; all values under 
	 * {@code max_depth} are assigned the value of their parents.
	 * 
	 * @return
	 */
	public ArrayList<Integer> getTreeValues(int max_depth){
		
		ArrayList<Integer> values = new ArrayList<Integer>(size);
		for (int i = 0; i < size; i++)
			values.add(-1);
		
		setTreeValues(root, values, 0, new MguiInteger(0), max_depth);
		
		return values;
	}
	
	private void setTreeValues(K3DNode node, ArrayList<Integer> values, int depth, MguiInteger current, int max_depth){
		
		if (node == null) return;
		
		values.set(node.vertex.index, current.getInt());
		
		if (depth <= max_depth)
			current.add(1);
		setTreeValues(node.child_left, values, depth + 1, current, max_depth);
		if (depth <= max_depth)
			current.add(1);
		setTreeValues(node.child_right, values, depth + 1, current, max_depth);
		
	}
	
	/******************************************************
	 * Searches this binary tree for the nearest neighbour
	 * 
	 * @param point
	 * @return
	 */
	public int getNearestNeighbour(Point3f point){
		
		K3DNode nearest = searchNN(root, point, null, new MguiFloat(Float.MAX_VALUE));
		
		return nearest.vertex.index;
	}
	
	/******************************************************
	 * Does a recursive search of this tree to find the nearest neighbour of {@code point}.
	 * 
	 * @param here
	 * @param point
	 * @param best
	 * @param current_best
	 * @return
	 */
	protected K3DNode searchNN(K3DNode here, Point3f point, K3DNode best, MguiFloat current_best){
		
		if (here == null) 
			return best;
		
		if (point.x > 0)
			best = best;
		
		// Consider the current node
		float current_distance = here.getPoint().distance(point);
		if (current_distance < current_best.getFloat()){
			current_best.setValue(current_distance);
			best = here;
			}
		
		K3DNode child_near = getClosestChild(here, point);
		best = searchNN(child_near, point, best, current_best);
		
		float axis_distance = getAxisDistance(here.vertex, point, here.axis);
		if (axis_distance < current_best.getFloat()){
			// Have to search other side
			K3DNode child_far = getFurthestChild(here, point);
			best = searchNN(child_far, point, best, current_best);
			}
		
		return best;
	}
	
	protected K3DNode getClosestChild(K3DNode node, Point3f point){
		
		switch (node.axis){
			case 0:
				if (point.x <= node.getPoint().x)
					return node.child_left;
				else
					return node.child_right;
			case 1:
				if (point.y <= node.getPoint().y)
					return node.child_left;
				else
					return node.child_right;
			case 2:
				if (point.z <= node.getPoint().z)
					return node.child_left;
				else
					return node.child_right;
			}
		
		//shouldn't get here
		return null;
	}
	
	protected K3DNode getFurthestChild(K3DNode node, Point3f point){
		switch (node.axis){
		case 0:
			if (point.x > node.getPoint().x)
				return node.child_left;
			else
				return node.child_right;
		case 1:
			if (point.y > node.getPoint().y)
				return node.child_left;
			else
				return node.child_right;
		case 2:
			if (point.z > node.getPoint().z)
				return node.child_left;
			else
				return node.child_right;
		}
	
	//shouldn't get here
	return null;
	}
	
	protected float getAxisDistance(Vertex vertex, Point3f point, int axis){
		
		switch (axis){
			case 0:
				return Math.abs(vertex.point.x - point.x);
			case 1:
				return Math.abs(vertex.point.y - point.y);
			case 2:
				return Math.abs(vertex.point.z - point.z);
			}
		
		return Float.NaN;
	}
	
	interface Point3fComparator extends Comparator<Vertex>{
		
	}
	
	class Point3fComparatorX implements Point3fComparator{
		public int compare(Vertex n1, Vertex n2) {
			return GeometryFunctions.compareFloat(n1.point.x, n2.point.x);
		}
	}
	
	class Point3fComparatorY implements Point3fComparator{
		public int compare(Vertex n1, Vertex n2) {
			return GeometryFunctions.compareFloat(n1.point.y, n2.point.y);
		}
	}
	
	class Point3fComparatorZ implements Point3fComparator{
		public int compare(Vertex n1, Vertex n2) {
			return GeometryFunctions.compareFloat(n1.point.z, n2.point.z);
		}
	}
	
	
	class Vertex {
		
		public Point3f point;
		public int index;
		
		public Vertex(Point3f point, int index){
			this.point = point;
			this.index = index;
		}
		
	}
	
}