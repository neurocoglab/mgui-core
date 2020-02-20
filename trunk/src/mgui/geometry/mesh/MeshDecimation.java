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

package mgui.geometry.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.Plane3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.Utility;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;

/********************************************************************
 * Utility class for specialized functions dealing with mesh decimation.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MeshDecimation extends Utility {

	/***************************
	 * Decimates a mesh as decribed in:
	 * <p>Knapp, <i>Mesh decimation using VTK</i>, and..</p>
	 * <p>Schroeder WJ, Zarge JA, Lorensen WE. (1992) <i>Decimation of Triangle Meshes.</i>
	 * Conference Proceedings of SIGGRAPH. pp. 65-70</p>
	 * 
	 * <p>As follows:</p>
	 * <p>1. Represent mesh as an edge set</p>
	 * <p>2. Classify edges as feature edges if their opposing faces have an angle > threshold</p>
	 * <p>3. Classify vertices as:</p>
	 * <ul>
	 * <li>Simple: surrounded by a complete cycle of triangles</li>
	 * <li>Complex: like simple, but with one or more edges having multiple triangles</li>
	 * <li>Boundary: surrounded by semi-cycle of triangles</li>
	 * <li>Interior: Having exactly two feature edges</li>
	 * <li>Corner: Having more than two feature edges</li>
	 * </ul>
	 * <p>4. Calculate and sort by error (mesh without this vertex compared to mesh with this vertex)</p>
	 * <p>5. Eliminate vertices starting with those having the lowest error, until either
	 * the specified target number (of remaining vertices) is met, or until no more vertices
	 * are below the error threshold (unless ensureTarget == true, in which case vertices will
	 * continue to be removed until the target size is met)</p>
	 * 
	 * @param mesh Mesh to decimate
	 * @param featureAngle minimum angle above which edges are considered feature edges
	 * @param errorThreshold maximum error beyond which vertices will not be removed (unless ensureTarget == true)
	 * @param targetSize target number of vertices
	 * @param endureTarget ensure that the the mesh is decimated to targetSize
	 */
	
	public static void decimateVTK(Mesh3D mesh, double featureAngle, double errorThreshold, int targetSize, boolean ensureTarget){
		//NOTE: at the moment this does not handle complex vertices
		
		//1. represent as edge set
		MeshEdgeSet edgeSet = new MeshEdgeSet(mesh);
		boolean[] isFeatureEdge = new boolean[edgeSet.edges.size()];
		int[] features = new int[mesh.n];
		ArrayList<IndexedEdge> featureEdges = new ArrayList<IndexedEdge>();
		//int[][] featureEdges = new int[mesh.n][2];
		boolean[] isBoundary = new boolean[mesh.n];
		ArrayList<IndexedEdge> boundaryEdges = new ArrayList<IndexedEdge>();
		
		//2. classify each edge
		//3. classify each vertex
		for (int i = 0; i < isFeatureEdge.length; i++){
			MeshEdge edge = edgeSet.edges.get(i);
			if (edge.hasOppositeTri()){
				MeshTriangle tri1 = edgeSet.getEdgeTri(0, i);
				MeshTriangle tri2 = edgeSet.getEdgeTri(1, i);
				
				int A = edge.node1;
				int B = edge.node2;
				int C = tri1.getOppositeNode(edge);
				int D = tri2.getOppositeNode(edge);
				
				if (GeometryFunctions.getAngle(mesh.getVertex(A),
											   mesh.getVertex(B),
											   mesh.getVertex(C),
											   mesh.getVertex(D)) 
											   > featureAngle){
					featureEdges.add(new IndexedEdge(A, i));
					featureEdges.add(new IndexedEdge(B, i));
					features[A]++;
					features[B]++;
					}
				}else{
					isBoundary[edge.node1] = true;
					isBoundary[edge.node2] = true;
					boundaryEdges.add(new IndexedEdge(edge.node1, i));
					boundaryEdges.add(new IndexedEdge(edge.node2, i));
				}
			}
		
		Collections.sort(featureEdges);
		Collections.sort(boundaryEdges);
		
		//4.a. Calculate error for each vertex, based upon its classification
		//     Complex and corner vertices should be preserved, thus are given
		//	   errors of Double.MAX_VALUE
		//get neighbourhood relationships
		NeighbourhoodMesh neighbourhoods = new NeighbourhoodMesh(mesh);
		float[][] error = new float[mesh.n][2];
		for (int i = 0; i < mesh.n; i++){
			error[i][1] = i;
			if (features[i] > 2){
				//corner
				error[i][0] = Float.MAX_VALUE;
			}else if(isBoundary[i]){
				//boundary
				Integer v = new Integer(i);
				int j = Collections.binarySearch(boundaryEdges, v);
				if (j > 0 && j + 1 < boundaryEdges.size() && boundaryEdges.get(j+1).index == i){
					MeshEdge edge1 = edgeSet.edges.get(boundaryEdges.get(j).edge);
					MeshEdge edge2 = edgeSet.edges.get(boundaryEdges.get(j+1).edge);
					Point3f p1 = null, p2 = mesh.getVertex(i), p3 = null;
					if (edge1.node1 == j)
						p1 = mesh.getVertex(edge1.node2);
					else
						p1 = mesh.getVertex(edge1.node1);
					if (edge2.node1 == j)
						p2 = mesh.getVertex(edge2.node2);
					else
						p2 = mesh.getVertex(edge2.node1);
					error[i][0] = getDistance(p1, p2, p3);
				}else{
					//shouldn't get here..?
					InterfaceSession.log("Error, only one boundary node for vertex " + i + "...");
					}
			}else if(features[i] > 0){
				//interior
				Integer v = new Integer(i);
				int j = Collections.binarySearch(featureEdges, v);
				
				if (j > 0 && j + 1 < featureEdges.size() && featureEdges.get(j + 1).index == i){
					MeshEdge edge1 = edgeSet.edges.get(featureEdges.get(j).edge);
					MeshEdge edge2 = edgeSet.edges.get(featureEdges.get(j + 1).edge);
					Point3f p1 = null, p2 = mesh.getVertex(i), p3 = null;
					if (edge1.node1 == j)
						p1 = mesh.getVertex(edge1.node2);
					else
						p1 = mesh.getVertex(edge1.node1);
					if (edge2.node1 == j)
						p2 = mesh.getVertex(edge2.node2);
					else
						p2 = mesh.getVertex(edge2.node1);
					error[i][0] = getDistance(p1, p2, p3);
					}
			}else{
				//simple (or not-so-simple)
				//a. get immediate neighbourhood points of i-th node
				ArrayList<Point3f> points = MeshFunctions.getNeighbourhoodPoints(mesh, neighbourhoods, i);
				
				//b. get the average plane of these points
				Plane3D plane = GeometryFunctions.getOrthogonalRegressionPlane(points);
				
				//c. error is the distance of this vertex from the plane
				error[i][0] = GeometryFunctions.getDistance(mesh.getVertex(i), plane);
				}
			}
		
		//4.b. sort list by error (ascending)
		Arrays.sort(error, new Comparator<float[]>(){
			public int compare(float[] i1, float[] i2){
				if (i1[0] < i2[0]) return -1;
				if (i1[0] > i2[0]) return 1;
				return 0;
				}
			});
		
		//5. decimate sorted list until a.) error is below threshold and target is not met, or 
		//   b.) ensureTarget = true and target is not met
		int i = 0;
		//ArrayList<arInteger> toRemove = new ArrayList<arInteger>();
		boolean[] removed = new boolean[mesh.n];
		while ((error[i][0] < errorThreshold && i < targetSize) ||
			   (ensureTarget && i < targetSize)){
			//toRemove.add(new arInteger((int)error[i][1]));
			removed[(int)error[i][1]] = true;
			i++;
			}
			
		MeshFunctions.removeNodes(mesh, removed);
	}
	
	//return the triangle height of triangle p1p2p3, with p1p3 as base
	static float getDistance(Point3f p1, Point3f p2, Point3f p3){
		Vector3f p1p2 = new Vector3f(p2);
		p1p2.sub(p1);
		Vector3f p1p3 = new Vector3f(p3);
		p1p2.sub(p1);
		float theta = p1p3.angle(p1p2);
		float b = p1p2.length();
		return b * (float)Math.sin(theta);
	}
	
	static class IndexedEdge implements Comparable<Object>{
		public int index;
		public int edge;
		
		public IndexedEdge(int index, int edge){
			this.index = index;
			this.edge = edge;
		}
		
		public int compareTo(Object o){
			if (o instanceof IndexedEdge){
				IndexedEdge e = (IndexedEdge)o;
				if (index < e.index) return -1;
				if (index > e.index) return 1;
				return 0;
				}
			if (o instanceof Integer){
				Integer i = (Integer)o;
				if (index < i.intValue()) return -1;
				if (index > i.intValue()) return 1;
				}
			return 0;
		}
	}
	
	/*************************************
	 * Decimates by removing nodes one-by-one, skipping boundary nodes:
	 * 
	 * 1. random list of nodes & tags
	 * 2. get neighbourhood mesh
	 * 3. for each node
	 * 4. if not tagged and not boundary node
	 * 5. remove from mesh
	 * 6. tag
	 * 7. tag all neighbours
	 * 
	 * @param mesh Mesh to decimate
	 * TODO: keep feature edges
	 * TODO: refine by comparing to original and iterating
	 */
	public static void decimateNeighbours(Mesh3D mesh){
	
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		
		//random list
		ArrayList<Integer> list = new ArrayList<Integer>(mesh.n);
		for (int i = 0; i < mesh.n; i++)
			list.add(new Integer(i));
		
		//Collections.shuffle(list);
		
		ArrayList<MguiBoolean> tags = new ArrayList<MguiBoolean>(mesh.n);
		ArrayList<MguiBoolean> remove = new ArrayList<MguiBoolean>(mesh.n);
		for (int i = 0; i < mesh.n; i++){
			tags.add(new MguiBoolean(false));
			remove.add(new MguiBoolean(false));
			}
		
		//tag edge nodes and nodes whose neighbours are edge nodes
		tagBoundaryNodes(tags, n_mesh);
		
		//int index, r_count = 0;
		
		ArrayBlockingQueue<MguiInteger> Q = new ArrayBlockingQueue<MguiInteger>(mesh.n);
		
		InterfaceSession.log("Decimating mesh (no-neighbour method) [" + mesh.n + " nodes, " + mesh.f + " faces].");
		//breadth-first decimation starting from random points
		for (int i = 0; i < list.size(); i++){
			int index = list.get(i);
			if (!remove.get(index).getTrue() && !tags.get(index).getTrue()){
				removeNeighbourNodes(n_mesh, index, Q, remove, tags);
				while (!Q.isEmpty()){
					//System.out.print(".");
					int[] nbrs = n_mesh.getNeighbourhood(Q.poll().getInt()).getNeighbourList();
					for (int j = 0; j < nbrs.length; j++)
						if (!remove.get(nbrs[j]).getTrue() && !tags.get(nbrs[j]).getTrue())
							removeNeighbourNodes(n_mesh, nbrs[j], Q, remove, tags);
					}
				}
			}
		
		//remove nodes and retriangulate
		//for each node in mesh, add to new_mesh and map
		list = new ArrayList<Integer>(mesh.n);
		//int j = 0;
		Mesh3D new_mesh = new Mesh3D();
		for (int i = 0; i < mesh.n; i++)
			if (!remove.get(i).getTrue()){
				new_mesh.addVertex(mesh.getVertex(i));
				int j = new_mesh.getSize() - 1;
				list.add(new Integer(j));
			}else{
				list.add(new Integer(-1));
				}
			
		InterfaceSession.log("Adding preserved faces.");
		//for each face in mesh, if all three nodes remain, add to new_mesh
		for (int i = 0; i < mesh.f; i++){
			Mesh3D.MeshFace3D face = mesh.getFace(i);
			if (!remove.get(face.A).getTrue() && 
				!remove.get(face.B).getTrue() &&
				!remove.get(face.C).getTrue()){
				//System.out.print(".");
				new_mesh.addFace(list.get(face.A).intValue(),
								 list.get(face.B).intValue(),
								 list.get(face.C).intValue());
				}
			}
		
		
		//for each removed node, if neighbourhood is a cycle, retriangulate
		//add add triangles to new_mesh
		InterfaceSession.log("Retriangulating.");
		for (int i = 0; i < remove.size(); i++)
			if (remove.get(i).getTrue()){
				ArrayList<MguiInteger> n = n_mesh.getNeighbourhoodRing(i);
				
				if (n != null){
					Mesh3D patch = MeshFunctions.getSubmesh(mesh, n);
					
					//add faces in patch
					for (int k = 0; k < patch.f; k++){
						Mesh3D.MeshFace3D face = patch.getFace(k);
						new_mesh.addFace(list.get(n.get(face.A).getInt()),
										 list.get(n.get(face.B).getInt()),
										 list.get(n.get(face.C).getInt()));
						}
					}
				}
		
		new_mesh.finalize();
		InterfaceSession.log("Correcting orientation.");
		MeshFunctions.correctOrientation(new_mesh);
		//MeshFunctions.validateSurface(new_mesh);
		//MeshFunctions.condenseMesh(new_mesh);
		InterfaceSession.log("Done.");
		String n_pct = MguiDouble.getString((double)new_mesh.n / (double)mesh.n * 100f, "0.00") + "%";
		String f_pct = MguiDouble.getString((double)new_mesh.f / (double)mesh.f * 100f, "0.00") + "%";
		mesh.setFromMesh(new_mesh);
		InterfaceSession.log("Resulting mesh: [" + new_mesh.n + " nodes (" + n_pct + "), " + 
												 new_mesh.f + " faces (" + f_pct + ")].");
	}
	
	/***************************
	 * Tag all boundary nodes.
	 */
	public static void tagBoundaryNodes(ArrayList<MguiBoolean> tagged, NeighbourhoodMesh n_mesh){
		//for each node, if is boundary or has boundary neighbours, tag
		for (int i = 0; i < tagged.size(); i++)
			if (n_mesh.isBoundaryNode(i))
				tagged.get(i).setTrue(true);
		
	}
	
	static void removeNeighbourNodes(NeighbourhoodMesh n_mesh, 
									 int index, 
									 Queue<MguiInteger> Q, 
									 ArrayList<MguiBoolean> remove,
									 ArrayList<MguiBoolean> tagged){
		
		remove.get(index).setValue(true);
		int[] nbrs = n_mesh.getNeighbourhood(index).getNeighbourList();
		
		//add neighbours if not already tagged
		for (int i = 0; i < nbrs.length; i++)
			if (!remove.get(nbrs[i]).getTrue() && !tagged.get(nbrs[i]).getTrue()){
				tagged.get(nbrs[i]).setValue(true);
				Q.add(new MguiInteger(nbrs[i]));
				}
		
	}
	
}