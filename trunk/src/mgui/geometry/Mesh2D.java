package mgui.geometry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.jogamp.vecmath.Point2f;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;

/***************************************************
 * 
 * Represents a 3D mesh sliced by a section.
 * 
 * @author Andrew Reid
 *
 */
public class Mesh2D extends Shape2D {
	
	public ArrayList<Point2f[]> edges;
	protected ArrayList<Polygon2D> polygons;
	protected ArrayList<Point2f> vertices;
	
	public Mesh2D(ArrayList<Point2f[]> edges) {
		this.edges = edges;
		setPolygons();
	}

	@Override
	public Point2f getVertex(int i) {
		return vertices.get(i);
	}

	@Override
	public ArrayList<Point2f> getVertices() {
		return vertices;
	}

	@Override
	public void setVertices(ArrayList<Point2f> vertices) {
		// Does nothing; vertices are determined by edges
	}
	
	public void setEdges(ArrayList<Point2f[]> edges) {
		this.edges = edges;
	}
	
	public ArrayList<Point2f[]> getEdges() {
		return edges;
	}

//	public ArrayList<Integer[]> getIndices() {
//		return indices;
//	}
	
	public ArrayList<Polygon2D> getPolygons() {
		return polygons;
	}
	
	@Override
	public float[] getCoords() {
		
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
	
	double tolerance = 0;
	double join_tolerance = 1;
	
	Comparator<Point2f> pt_comp = new Comparator<Point2f>() {
		public int compare(Point2f p1, Point2f p2) {
			if ((p1.x - p2.x) > tolerance) return 1;
			if ((p2.x - p1.x) > tolerance) return -1;
			if ((p1.y - p2.y) > tolerance) return 1;
			if ((p2.y - p1.y) > tolerance) return -1;
			return 0;
		}
	};
	
	Comparator<Point2f> pt_comp_join = new Comparator<Point2f>() {
		public int compare(Point2f p1, Point2f p2) {
			if ((p1.x - p2.x) > join_tolerance) return 1;
			if ((p2.x - p1.x) > join_tolerance) return -1;
			if ((p1.y - p2.y) > join_tolerance) return 1;
			if ((p2.y - p1.y) > join_tolerance) return -1;
			return 0;
		}
	};
	
	protected void setPolygons() {
		
		polygons = new ArrayList<Polygon2D>();
		
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
		for (Point2f k : connected.keySet()) {
			if (connected.get(k).size() == 1) {
				unconnected.add(k);
				}
			}
			
		join_tolerance = 10;
		
		TreeSet<Point2f> orphans = new TreeSet<Point2f>(pt_comp);
		TreeSet<Point2f> endpoints = new TreeSet<Point2f>(pt_comp);
		
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
						} 
					}
				
				if (found != null) {
					setk.add(found);
					connected.get(found).add(pt);
					unprocessed.remove(found);
					}
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
			return;
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

		this.vertices = new ArrayList<Point2f>(vset);
		
	}
	
}
