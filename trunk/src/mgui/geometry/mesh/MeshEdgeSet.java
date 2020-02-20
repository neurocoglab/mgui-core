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
import java.util.Collections;
import java.util.Comparator;

import mgui.geometry.Mesh3D;
import mgui.geometry.Mesh3D.MeshFace3D;
import mgui.interfaces.InterfaceSession;


/**************************
 * Represents a mesh in terms of triangles and edges. For purposes of subdivision it
 * is desirable to have an edge with reference to its adjacent triangles, and a triangle
 * with reference to its edges. {@code MeshEdgeSet} has two arrays: of {@linkplain MeshEdge} 
 * objects and {@linkplain MeshTriangle} objects.
 *  
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */


public class MeshEdgeSet {

	public ArrayList<MeshEdge> edges = new ArrayList<MeshEdge>();
	public ArrayList<MeshTriangle> triangles = new ArrayList<MeshTriangle>();
	public Mesh3D mesh;
	
	public MeshEdgeSet(Mesh3D mesh3d){
		setMesh(mesh3d);
	}
	
	public void setMesh(Mesh3D mesh3d){
		mesh = mesh3d;
		MeshEdge[] theseEdges = new MeshEdge[3];
		MeshTriangle thisTri;
		
		//for each face in mesh,
		for (int i = 0; i < mesh.f; i++){
		//	for each edge in triangle,
			thisTri = new MeshTriangle(i);
			theseEdges = getEdges(i, i);
			for (int j = 0; j < 3; j++){
				//search for existing edge
				int k = searchEdge(theseEdges[j]);
				if (k >= 0){
					//edge exists so add triangle to edge and edge to triangle
					edges.get(k).addTri(i);
					thisTri.addEdge(edges.get(k));
				}else{
					//edge doesn't exist so add it to array & triangle
					thisTri.addEdge(theseEdges[j]);
					theseEdges[j].addTri(i);
					addEdge(theseEdges[j]);
					}
				}
			//	add triangle to triangle array
			triangles.add(thisTri);
			}
		
	}
	
	public ArrayList<MeshEdge> getEdges(){
		return edges;
	}
	
	//returns three edges for face at index
	public MeshEdge[] getEdges(int faceIndex, int triIndex){
		MeshEdge[] edges = new MeshEdge[3];
		MeshFace3D face = mesh.getFace(faceIndex);
		edges[0] = new MeshEdge(triIndex, -1, face.A, face.B);
		edges[1] = new MeshEdge(triIndex, -1, face.B, face.C);
		edges[2] = new MeshEdge(triIndex, -1, face.C, face.A);
		return edges;
	}
	
	/**************************
	 * Finds the specified edge in this set
	 * 
	 * @param e
	 * @return
	 */
	public int searchEdge(MeshEdge e){
		//use edgeComparator to binary search edges and return result
		return Collections.binarySearch(edges, e, new EdgeComparator());
	}
	
	/***************************
	 * Returns the index of the edge connecting {@code n1} and {@code n2}.
	 * 
	 * @param n1
	 * @param n2
	 * @return
	 */
	public int getEdgeIndex(int n1, int n2){
		MeshEdge edge = new MeshEdge(0,0,n1,n2);
		return searchEdge(edge);
	}
	
	public void addEdge(MeshEdge e){
		//do binary search
		int k = searchEdge(e);
		//if edge doesn't exist, add it in order
		if (k < 0){
			k = -k - 1;
			edges.add(k, e);
			}
	}
	
	public MeshEdge getEdge(int i){
		return edges.get(i);
	}
	
	/*********************
	 * Returns the {@code i}th triangle index associated with this edge.
	 * 
	 * @param i
	 * @param edge
	 * @return
	 */
	public MeshTriangle getEdgeTri(int i, int edge){
		if (i == 0 && edges.get(edge).tri1 >= 0)
			return triangles.get(edges.get(edge).tri1);
		if (i == 1 && edges.get(edge).tri2 >= 0) 
			return triangles.get(edges.get(edge).tri2);
		return null;
	}
	
	/**********************
	 * Get the triangle opposite to {@code face}, which is joined at
	 * {@code edge}.
	 * 
	 * @param e
	 * @param t
	 * @return
	 */
	public MeshTriangle getOppositeTri(MeshEdge edge, MeshTriangle face){
		
		if (triangles.get(edge.tri1) == face) return triangles.get(edge.tri2);
		if (triangles.get(edge.tri2) == face) return triangles.get(edge.tri1);
		return null;
	}
	
	/**********************
	 * Get the triangle opposite to triangle at {@code face_idx}, which is joined at
	 * {@code edge}.
	 * 
	 * @param e
	 * @param t
	 * @return
	 */
	public MeshTriangle getOppositeTri(int edge_idx, int face_idx){
		MeshEdge edge = edges.get(edge_idx);
		if (triangles.get(edge.tri1).faceIndex == face_idx) return triangles.get(edge.tri2);
		if (triangles.get(edge.tri2).faceIndex == face_idx) return triangles.get(edge.tri1);
		return null;
	}
	
	/**********************
	 * Get the face index (i.e., in the source mesh) of the triangle opposite to triangle at {@code face_idx}, 
	 * which is joined at {@code edge}.
	 * 
	 * @param e
	 * @param t
	 * @return
	 */
	public int getOppositeFaceIndex(int edge_idx, int face_idx){
		MeshEdge edge = edges.get(edge_idx);
		if (triangles.get(edge.tri1).faceIndex == face_idx) return triangles.get(edge.tri2).faceIndex;
		if (triangles.get(edge.tri2).faceIndex == face_idx) return triangles.get(edge.tri1).faceIndex;
		return -1;
	}
	
	/**********************
	 * Get the triangle index (i.e., in this set) of the triangle opposite to triangle at {@code face_idx}, 
	 * which is joined at {@code edge}.
	 * 
	 * @param edge_idx
	 * @param face_idx
	 * @return
	 */
	public int getOppositeTriIndex(int edge_idx, int face_idx){
		MeshEdge edge = edges.get(edge_idx);
		if (triangles.get(edge.tri1).faceIndex == face_idx) return edge.tri2;
		if (triangles.get(edge.tri2).faceIndex == face_idx) return edge.tri1;
		return -1;
	}
	
	/*********************
	 * Returns the face index (i.e., in the source mesh) of the triangle at
	 * {@code tri_idx}.
	 * 
	 * @param tri_idx
	 * @return
	 */
	public int getFaceIndex(int tri_idx){
		return triangles.get(tri_idx).faceIndex;
	}
	
	/**********************
	 * Get the vertex index (i.e., in the source mesh) of the vertex opposite to {@code triangle}, 
	 * which is joined at {@code edge}.
	 * 
	 * @param edge
	 * @param triangle
	 * @return
	 */
	public int getOppositeNode(MeshEdge edge, MeshTriangle triangle){
		MeshTriangle tri = getOppositeTri(edge, triangle);
		return tri.getOppositeNode(edge);
		
	}
	
	/**********************
	 * Get the vertex index (i.e., in the source mesh) of the vertex opposite to triangle at {@code tri_index}, 
	 * which is joined at {@code edge}.
	 * 
	 * @param edge
	 * @param triangle
	 * @return
	 */
	public int getOppositeNode(int edge_index, int tri_index){
		
		MeshEdge edge = edges.get(edge_index);
		MeshTriangle tri = triangles.get(tri_index);
		return getOppositeNode(edge, tri);
		
	}
	
	/**@TODO create edge comparator **/
	class EdgeComparator implements Comparator<MeshEdge> {
		
		public int compare(MeshEdge e1, MeshEdge e2){
			if (e1.node1 > e2.node1) return 1;
			if (e1.node1 == e2.node1){
				if (e1.node2 > e2.node2) return 1;
				if (e1.node2 == e2.node2) return 0;
				return -1;
				}
			return -1;
			}
	}
	
}