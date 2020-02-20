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

package mgui.interfaces.shapes.graphs.util;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import mgui.geometry.Plane3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.graphs.AbstractGraphEdge;
import mgui.interfaces.graphs.AbstractGraphNode;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;

/**********************************************************
 * Layout for use with {@code Graph2DInt}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <V>
 * @param <E>
 */
public class Graph2DLayout<V extends AbstractGraphNode, E extends AbstractGraphEdge> extends AbstractLayout<V,E> {

	protected Plane3D projection_plane;
	protected Point2D.Float min_pt, max_pt;
	HashMap<V,Point2D> map = new HashMap<V,Point2D>();
	
	/**********************************
	 * Creates an instance for the specified graph, with a default horizontal projection plane.
	 * 
	 * 
	 */
	public Graph2DLayout(Graph<V, E> g) {
		this(g, new Plane3D(new Point3f(0,0,0), 
							new Vector3f(1,0,0), 
							new Vector3f(0,-1,0)));
	}
	
	public Graph2DLayout(Graph<V, E> graph, Plane3D projection_plane) {
		super(graph);
		this.projection_plane = projection_plane;
		setCoordMap(graph);
		initialized = true;
	}
	
	public Plane3D getProjectionPlane(){
		return this.projection_plane;
	}
	
	public void setProjectionPlane(Plane3D plane){
		this.projection_plane = plane;
		initialized = false;
		initialize();
	}
	
	protected void setCoordMap(Graph<V,E> graph){
		ArrayList<V> vertex_list = new ArrayList<V>(graph.getVertices());
		
		min_pt = new Point2D.Float(Float.MAX_VALUE, Float.MAX_VALUE);
		max_pt = new Point2D.Float(Float.MIN_VALUE, Float.MIN_VALUE);
		
		for (V node : vertex_list){
			Point2D p = new Point2D.Float();
			map.put(node, p);
			Point3f p3d = node.getLocation();
			p = getProjectedPoint(p3d, projection_plane);
			map.put(node, p);
			min_pt.setLocation(Math.min(min_pt.getX(), p.getX()),
							   Math.min(min_pt.getY(), p.getY()));
			max_pt.setLocation(Math.max(max_pt.getX(), p.getX()),
					   		   Math.max(max_pt.getY(), p.getY()));
			}
		
	}
	
	@Override
	public void initialize() {

		if (!initialized) setCoordMap(graph);
		
		Dimension d = getSize();
		if (d == null) return;
		
//		float delta_x = (float)max_pt.getX() - (float)min_pt.getX();
//		float delta_y = (float)max_pt.getY() - (float)min_pt.getY();
//		if (delta_x < 1) delta_x = 1;
//		if (delta_y < 1) delta_y = 1;
//		Point2D.Float center = new Point2D.Float(delta_x, delta_y);
//		
//		float x_scale = (float)d.getWidth() / delta_x;
//		float y_scale = (float)d.getHeight() / delta_y;
//				
//		float max_scale = Math.max(x_scale, y_scale) * 0.5f;
		
		ArrayList<V> vertex_list = new ArrayList<V>(graph.getVertices());
		for (int i = 0; i < vertex_list.size(); i++){
			V node = vertex_list.get(i);
			Point2D screen_coord = transform(node);
			Point2D real_coord = map.get(node);
			
//			float x = (float)(real_coord.getX() + (delta_x * 0.5f)) * max_scale;
//			float y = (float)(real_coord.getY() + (delta_y * 0.5f)) * max_scale;
			float x = (float)real_coord.getX();
			float y = (float)real_coord.getY();
			
			screen_coord.setLocation(x, y);
			}
		
		
	}

	protected Point2D getProjectedPoint(Point3f p, Plane3D projection_plane){
		Point2f p2d = GeometryFunctions.getProjectedPoint(p, projection_plane);
		return new Point2D.Double(p2d.x, p2d.y);
	}
	
	@Override
	public void reset() {
		initialize();
	}

}