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

package mgui.interfaces.shapes;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.jogamp.vecmath.Point2f;

import mgui.geometry.PointSet2D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Rect2D;
import mgui.geometry.util.NodeShape;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.logs.LoggingType;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.util.Colour;

/*******************************************************
 * A 2D representation of a {@link Mesh3DInt} object. Allows references to vertices of the parent mesh. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Mesh2DInt extends Shape2DInt {

	//TODO: replace geometry with a Shape2D -> Mesh2D object
	
	public ArrayList<Point2f[]> edges;
	public ArrayList<Integer[]> indices;
	public ArrayList<Colour> colours;		//TODO: replace with reference to parent shape
	
	public Mesh2DInt(){
		super();
		init();
	}
	
	/****************************************
	 * Constructor for this <code>Mesh2DInt</code>.
	 * 
	 * @param edges		list of edges obtained from a parent mesh
	 */
	public Mesh2DInt(ArrayList<Point2f[]> edges){
		this(edges, null);
	}
	
	/****************************************
	 * Constructor for this <code>Mesh2DInt</code>.
	 * 
	 * @param edges		list of edges obtained from a parent mesh
	 * @param indices	list of indices corresponding to the vertices of the parent mesh (can be <code>null</code>)
	 */
	public Mesh2DInt(ArrayList<Point2f[]> edges, ArrayList<Integer[]> indices){
		super();
		
		this.edges = edges;
		this.indices = indices;
		init();
	}
	
	private void init(){
		//add attributes here
		
	}
	
	public ArrayList<Point2f> getVertices(){
		TreeSet<Point2f> vertices = new TreeSet<Point2f>(pt_comp);
		for (Point2f[] pts : edges) {
			vertices.add(pts[0]);
			vertices.add(pts[1]);
			}
		return new ArrayList<Point2f>(vertices);
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine d){
		if (!isVisible()) return;
		
		if (edges == null){
			InterfaceSession.log("No edges to draw!", LoggingType.Debug);
			return;
			}
		
		float alpha = -1;
		if (((MguiBoolean)attributes.getValue("2D.HasAlpha")).getTrue())
			alpha = ((MguiFloat)attributes.getValue("2D.Alpha")).getFloat();
		
		if (((MguiBoolean)attributes.getValue("2D.HasFill")).getTrue()) {
			ArrayList<Polygon2D> polygons = getAsPolygons();
			
			if (polygons.size() > 0) {
				for (Polygon2D polygon : polygons) {
					d.drawPolygon2D(g, polygon);
					}
				//return;
				}
			}
		
		if (colours == null)
			d.drawMesh2D(g, edges, alpha);
		else
			d.drawMesh2D(g, edges, colours, alpha);
		
		if (((MguiBoolean)attributes.getValue("2D.ShowVertices")).getTrue()){
			PointSet2D points = new PointSet2D(getCoords());
			float scale = (((MguiFloat)attributes.getValue("2D.VertexScale")).getFloat());
			NodeShape shape = this.getVertexShape();
			d.drawPointSet2D(g, points, scale, shape, null, alpha);
		}
	
	}
	
	protected float[] getCoords() {
		TreeSet<Point2f> vertices = new TreeSet<Point2f>(pt_comp);
		for (Point2f[] pts : edges) {
			vertices.add(pts[0]);
			vertices.add(pts[1]);
			}
		float[] coords = new float[vertices.size()*2];
		int i = 0;
		for (Point2f p : vertices) {
			coords[i] = p.x;
			coords[i+1] = p.y;
			i+=2;
			}
		return coords;
	}
	
	class PolyNode {
		
		public PolyNode(Point2f node) {
			this.node = node;
		}
		
		public Point2f node; 
		PolyNode next = null, prev = null;
		
		public PolyNode addNeighbour(Point2f nbr) {
			if (next == null) {
				next = new PolyNode(nbr);
				next.prev = this;
				return next;
			} else if (prev == null) {
				prev = new PolyNode(nbr);
				prev.next = this;
				return prev;
				}
			return null;
		}
		
		public LinkedList<Point2f> getVertices(){
			LinkedList<Point2f> vertices = new LinkedList<Point2f>();
			vertices.add(node);
			if (next != null) {
				next.getVertices(vertices, false);
				}
			if (prev != null) {
				prev.getVertices(vertices, true);
				}
			
			return vertices;
		}
		
		public void getVertices(LinkedList<Point2f> vertices, boolean before){
			
			if (vertices.contains(node)) return;
			
			if (before) {
				vertices.addFirst(node);
			} else {
				vertices.addLast(node);
				}
			
			if (next != null) {
				next.getVertices(vertices, false);
				}
			if (prev != null) {
				prev.getVertices(vertices, true);
				}
		}
		
	}
	
	double tolerance = 0; //.0000001;
	double join_tolerance = 1;
	
	Comparator<Point2f> pt_comp = new Comparator<Point2f>() {
		public int compare(Point2f p1, Point2f p2) {
			if (p1.x - p2.x > tolerance) return 1;
			if (p2.x - p1.x > tolerance) return -1;
			if (p1.y - p2.y > tolerance) return 1;
			if (p2.y - p1.y > tolerance) return -1;
			if (!p1.equals(p2)) {
				tolerance = tolerance * 1;
				}
			return 0;
		}
	};
	
	Comparator<Point2f> pt_comp_join = new Comparator<Point2f>() {
		public int compare(Point2f p1, Point2f p2) {
			if (p1.x - p2.x > join_tolerance) return 1;
			if (p2.x - p1.x > join_tolerance) return -1;
			if (p1.y - p2.y > join_tolerance) return 1;
			if (p2.y - p1.y > join_tolerance) return -1;
			return 0;
		}
	};
	
	private ArrayList<Polygon2D> getAsPolygons() {
		
		ArrayList<Polygon2D> polygons = new ArrayList<Polygon2D>();
		
		HashMap<Point2f,Set<Point2f>> connected = new HashMap<Point2f,Set<Point2f>>();
		Set<Point2f> vset = new TreeSet<Point2f>(pt_comp);
		
		// For each edge, sort points, add p1 -> p2
		for (Point2f[] edge : edges) {
			
			if (pt_comp.compare(edge[0], edge[1]) == 0) {
				// Edge is a single point; discard it

			} else {
				
				if (connected.get(edge[0]) == null) {
					connected.put(edge[0], new TreeSet<Point2f>(pt_comp));
					}
				
				if (connected.get(edge[1]) == null) {
					connected.put(edge[1], new TreeSet<Point2f>(pt_comp));
					}
				
				connected.get(edge[0]).add(edge[1]);
				connected.get(edge[1]).add(edge[0]);
				
				vset.add(edge[0]);
				vset.add(edge[1]);
				}
			
			}
		
		// Find vertices with only one connected vertex 
		// If unconnected points are within a search distance, connect them
		TreeSet<Point2f> unconnected = new TreeSet<Point2f>(pt_comp);
		//TreeSet<Point2f> overconnected = new TreeSet<Point2f>(pt_comp);
		for (Point2f k : connected.keySet()) {
			if (connected.get(k).size() == 1) {
				unconnected.add(k);
				}
			}
		TreeSet<Point2f> orphans = new TreeSet<Point2f>(pt_comp);
		TreeSet<Point2f> endpoints = new TreeSet<Point2f>(pt_comp);
		
		join_tolerance = 10;
		
		while (unconnected.size() > 1) {
			Stack<Point2f> unprocessed = new Stack<Point2f>();
			unprocessed.addAll(unconnected);
			//Point2f pt = unprocessed.pop();
			while (!unprocessed.isEmpty()) {
				Point2f pt = unprocessed.pop();
				Point2f found = null;
				float delta = (float)join_tolerance;
				Set<Point2f> setk = connected.get(pt);
				for (Point2f k : vset) {
					if (pt_comp.compare(pt, k) != 0 &&
							pt_comp_join.compare(pt, k) == 0) {
						// Find nearest
						if (pt.distance(k) < delta && !setk.contains(k)) {
							found = k;
							delta = pt.distance(k);
							}
						//continue;
						} 
					}
				
				if (found != null) {
					
					setk.add(found);
					
					if (setk.size() == 1) {
						endpoints.add(pt);
					} else {
						connected.get(found).add(pt);
						}
					unprocessed.remove(found);
				} else {
					orphans.add(pt);
					}
//				if (!unprocessed.isEmpty()) pt = unprocessed.pop();
				}
			
			unconnected.clear();
			for (Point2f k : connected.keySet()) {
				Set<Point2f> setk = connected.get(k);
				if (!endpoints.contains(k) && !orphans.contains(k) && setk.size() == 1) {
					unconnected.add(k);
					}
				}
			
			}
		
		Stack<Point2f> unprocessed = new Stack<Point2f>();
		unprocessed.addAll(vset);
		Point2f pt = unprocessed.pop();
		
		if (pt == null) {
			InterfaceSession.log("Mesh2DInt.draw: No polygons found.", LoggingType.Debug);
			return polygons;
			}
		
		Stack<PolyNode> nodes_to_process = null;
		
		while (!unprocessed.isEmpty()) {
			
			PolyNode start_node = new PolyNode(pt);
			nodes_to_process = new Stack<PolyNode>();
			nodes_to_process.add(start_node);
			
			while (!nodes_to_process.isEmpty()) {
				
				PolyNode node = nodes_to_process.pop();
				Set<Point2f> conns = connected.get(node.node);
				for (Point2f pti : conns) {
					if (unprocessed.contains(pti)) {
						PolyNode nbr = node.addNeighbour(pti);
						if (nbr != null) {
							unprocessed.remove(pti);
							nodes_to_process.push(nbr);
							}
						}
					}
				
				if (nodes_to_process.isEmpty()) {
					tolerance = tolerance + 0;
					}
				
				}
			
			// No more nodes, make new polygon
			ArrayList<Point2f> vertices = new ArrayList<Point2f>(start_node.getVertices());
			vertices.add(vertices.get(0));
			
			if (vertices.size() > 2) {
				polygons.add(new Polygon2D(vertices));
				}
			
			if (!unprocessed.isEmpty()) {
				pt = unprocessed.pop();
				}
			
			}
		
		return polygons;
		
	}
	
	@Override
	public boolean needsRedraw(Attribute<?> a){
		//is it visible?
		
		if (a.getName().equals("2D.LineColour") ||
			a.getName().equals("2D.LineStyle") ||
			a.getName().equals("2D.HasAlpha") ||
			a.getName().equals("2D.Alpha")) 
			return true;
		
		return super.needsRedraw(a);
	}
	
	public void setColours(ArrayList<Colour> c){
		colours = c;
	}
	
	public void merge(Mesh2DInt mesh){
		if (mesh == null) return;
		ArrayList<Point2f[]> new_edges = mesh.edges;
		if (new_edges == null) return;
		if (edges == null) 
			edges = new_edges;
		else
			edges.addAll(new_edges);
	}
	
	public void addEdge(Point2f[] edge){
		edges.add(edge);
	}
	
	@Override
	public void updateShape(){
		float maxX = Float.MIN_VALUE, minX = Float.MAX_VALUE;
		float maxY = Float.MIN_VALUE, minY = Float.MAX_VALUE;
		float xSum = 0, ySum = 0;
		
		for (int i = 0; i < edges.size(); i++)
			for (int j = 0; j < 2; j++){
				maxX = Math.max(edges.get(i)[j].x, maxX);
				maxY = Math.max(edges.get(i)[j].y, maxY);
				minX = Math.min(edges.get(i)[j].x, minX);
				minY = Math.min(edges.get(i)[j].y, minY);
				xSum += edges.get(i)[j].x;
				ySum += edges.get(i)[j].y;
				}
		
		if (centerPt == null) centerPt = new Point2f();
		centerPt.x = xSum / (edges.size() * 2);
		centerPt.y = ySum / (edges.size() * 2);
		
		if (bounds == null )
			bounds = new Rect2D(minX, minY, maxX, maxY);
		else{
			bounds.corner1.x = minX;
			bounds.corner1.y = minY;
			bounds.corner2.x = maxX;
			bounds.corner2.y = maxY;
		}
	}
}