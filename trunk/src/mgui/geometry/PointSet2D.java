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

/******************************************
 * Represents a set of 2D points.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PointSet2D extends Shape2D {

	public int n = 0;
	public float[] nodes;
	
	public PointSet2D(){
		nodes = new float[30];
	}
	
	public PointSet2D(float[] nodes){
		n = nodes.length / 2;
		this.nodes = nodes;
	}
	
	@Override
	public Point2f getVertex(int i){
		if (i < 0 || i > n) return null;
		return new Point2f(nodes[i * 2], nodes[(i * 2) + 1]);
	}
	
	public void addNode(Point2f p){
		n++;
		resizeArray();
		
		nodes[(n - 1) * 2] = p.x;
		nodes[((n - 1) * 2) + 1] = p.y;
	}
	
	protected void resizeArray(){
		if (n < (nodes.length / 2) - 2) return;
		
		float[] copy = new float[nodes.length * 2];
		System.arraycopy(nodes, 0, copy, 0, nodes.length);
		nodes = copy;
	}
	
	@Override
	public void finalize(){
		if (nodes.length == n * 2) return;
		
		float[] copy = new float[n * 2];
		System.arraycopy(nodes, 0, copy, 0, n * 2);
		nodes = copy;
		
	}
	
	@Override
	public ArrayList<Point2f> getVertices(){
		
		ArrayList<Point2f> pts = new ArrayList<Point2f>(n);
		
		for (int i = 0; i < n; i++)
			pts.add(new Point2f(nodes[i * 2],
								nodes[(i * 2) + 1]));
		
		return pts;
	}
	
	public void setVertices(ArrayList<Point2f> vertices){
		n = vertices.size();
		nodes = new float[n * 2];
		for (int i = 0; i < vertices.size(); i++){
			Point2f p = vertices.get(i);
			nodes[i * 2] = p.x;
			nodes[(i * 2) + 1] = p.y;
			}
		
	}
	
}