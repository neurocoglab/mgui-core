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

package mgui.geometry.mesh;

import java.util.ArrayList;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.Mesh3D.MeshFace3D;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.numbers.MguiBoolean;


/************************
 * Contains static methods for all mesh subdivision algorithms.
 * @author Andrew Reid
 * @version 1.0
 */

public class MeshSubdivision {

	public static void subdivideButterflyScheme(Mesh3DInt mesh, double w){
		boolean[] constraints = subdivideButterflyScheme(mesh.getMesh(), w, mesh.getConstraints());
		for (int i = 0; i < constraints.length; i++)
			mesh.setConstraint(i, constraints[i]);
	}
	
	
	/**************************
	 * Subdivide mesh once, using the "Butterfly Scheme" discussed by Dyn et al (1990),
	 * which will interpolate between existing nodes without altering these nodes. <tension>
	 * is used to determine the influence of neighbouring nodes upon the position of the
	 * newly inserted node. A tension of zero results in a purely linear interpolation, i.e.,
	 * no influence outside the two edge nodes.
	 * 
	 * @param mesh Mesh3D object to be subdivided
	 * @param w degree to which interpolation is influenced by neighbouring nodes
	 */
	
	public static boolean[] subdivideButterflyScheme(Mesh3D mesh, double w, boolean[] constraints){
		/**@TODO it must be possible to implement this in a less memory-intensive way? **/
		//Just 33 simple steps:
		//1. create a MeshEdgeSet
		//2. create a Point3f array edgeNodes, same size as MeshEdgeSet.edges
		//3. create a boolean triSubdivided, same size as MeshEdgeSet.triangles
		//4. create an int edgeNodeIndexes array, same size as MeshEdgeSet.edges, init all to -1
		//5. create an int meshNodeIndexes array, same size as mesh.nodes, init all to -1
		//6. create a new Mesh3D object newMesh
		//7. for each edge,
		//8.	get first term (n1 + n2) / 2
		//9.	for n3, n4 being opposite triangle nodes, get second term 2 * w * (n3 + n4),
		//			if this is an boundary edge, term2 = 0
		//10.	for n5 - n8 being opposite nodes from the four opposing edges in the
		//			two triangles, get third term w * (n5 + n6 + n7 + n8)
		//			if this is a boundary edge, term3 = 0
		//11. 	create new node at q = term1 + term2 - term3
		//12. 	add q to edgeNodes array
		//13. for each triangle i
		//14.	if !triSubdivided,
		//15.		nodes[] = new int[3]
		//16.		for each node j in triangle face,
		//17.			if meshNodeIndexes[nodej.index] < 0
		//18.				add mesh.nodes[nodej.index] to newMesh.nodes
		//19.				set meshNodeIndexes[nodej.index] to new mesh index
		//20.				nodes[j] = new mesh index
		//21.			else
		//22.				nodes[j] = meshNodeIndexes[nodej.index]
		//23.		edges[] = new int[3]
		//24.		for each edge j in triangle:
		//25.			k = MeshEdgeSet.searchEdge(ej)
		//26.			if edgeNodeIndexes[k] < 0
		//27.				add edgeNodes[k] to newMesh.nodes
		//28.				set edgeNodeIndexes[k] = new mesh index
		//29.				edges[j] = new mesh index
		//30.			else
		//31.				edges[j] = edgeNodeIndexes[k]
		//32.		add four new faces to newMesh (where n are original and q are new 
		//				(edge) nodes) (ensure these are clockwise)
		//				a. q2, q3, n4
		//				b. n1, q1, q2
		//				c. q2, q1, q3
		//				d. q1, n2, q3
		//33.		set triSubdivided[i] = true
		//34. set mesh to newMesh and return
		
		//Mesh3D mesh = meshInt.getMesh();
		Mesh3D newMesh = new Mesh3D();
		
		//boolean[] constraints = meshInt.constraints;
		ArrayList<MguiBoolean> newConstraints = new ArrayList<MguiBoolean>(); 
		
		//big objects
		MeshEdgeSet edgeMesh = new MeshEdgeSet(mesh);
		Point3f[] edgeNodes = new Point3f[edgeMesh.edges.size()];
		int[] edgeNodeIndexes = new int[edgeMesh.edges.size()];
		for (int i = 0; i < edgeNodeIndexes.length; i++)
			edgeNodeIndexes[i] = -1;
		int[] meshNodeIndexes = new int[mesh.n];
		for (int i = 0; i < meshNodeIndexes.length; i++)
			meshNodeIndexes[i] = -1;
		//Mesh3D newMesh = new Mesh3D();
		
		//flags
		boolean isBoundary = false;
		
		//working objects
		Point3f term1 = new Point3f(), term2 = new Point3f(), term3 = new Point3f();
		Point3f n3 = new Point3f(), n4 = new Point3f();
		//Point3f[] t3n = new Point3f[4];
		Point3f[] t3n = {new Point3f(), new Point3f(), new Point3f(), new Point3f()};
		Point3f q1 = new Point3f();
		MeshEdge thisEdge;
		MeshTriangle thisTri;
		
		//for each edge in edge list
		for (int i = 0; i < edgeMesh.edges.size(); i++){
			//term1
			term1.set(mesh.getVertex(edgeMesh.edges.get(i).node1));
			term1.add(mesh.getVertex(edgeMesh.edges.get(i).node2));
			term1.scale(0.5f);
			q1.set(term1);
			
			//term2
			isBoundary = false;
			n3.set(mesh.getVertex(edgeMesh.getEdgeTri(0, i).getOppositeNode(edgeMesh.edges.get(i))));
			n3.sub(q1);
			if (edgeMesh.getEdgeTri(1, i) != null){
				n4.set(mesh.getVertex(edgeMesh.getEdgeTri(1, i).getOppositeNode(edgeMesh.edges.get(i))));
				n4.sub(q1);
				}else{
				isBoundary = true;
				}
			
			if (!isBoundary){
				term2.set(n3);
				term2.add(n4);
				term2.scale((float)(2.0 * w));
				}else{
				//neighbours have no influence on boundary nodes
				term2 = new Point3f(0, 0, 0);
				}
			
			//term3
			if (!isBoundary){
				//get four opposite nodes of opposite edges
				for (int k = 0; k < 2; k++)
					for (int l = 0; l < 2; l++){
						thisEdge = edgeMesh.getEdgeTri(k, i).getOppositeEdge(l, edgeMesh.edges.get(i));
						if (thisEdge.hasOppositeTri()){
								thisTri = edgeMesh.getOppositeTri(thisEdge, edgeMesh.getEdgeTri(k, i));
							if (thisTri != null){
								//t3n[k + l].set(mesh.nodes.get(thisTri.getOppositeNode(edgeMesh.edges.get(i))));
								t3n[k + l].set(mesh.getVertex(thisTri.getOppositeNode(thisEdge)));
								t3n[k + l].sub(q1);
								t3n[k + l].scale((float)w);
								}else{
								t3n[k + l] = new Point3f(0, 0 ,0);
								}
							}else{
								t3n[k + l] = new Point3f(0, 0 ,0);
							}
						}
				term3.set(t3n[0]);
				for (int k = 1; k < 3; k++)
					term3.add(t3n[k]);
				}else{
				term3 = new Point3f(0, 0, 0);
				}
			
			//calculate q1
			q1.add(term2);
			q1.sub(term3);
			
			//add to edgeNodes array
			edgeNodes[i] = (Point3f)q1.clone();
			
			}//each edge
		
		int[] nodes = new int[3];
		int[] edges = new int[3];
		
		//for each triangle in triangle list
		for (int i = 0; i < edgeMesh.triangles.size(); i++){
			//let's subdivide this triangle
			//get this triangle's nodes
			MeshFace3D face = mesh.getFace(edgeMesh.triangles.get(i).faceIndex);
			nodes[0] = face.A;
			nodes[1] = face.B;
			nodes[2] = face.C;
			
			//for each of this triangle's nodes
			for (int j = 0; j < 3; j++){
				if (meshNodeIndexes[nodes[j]] < 0){
					//add original node to new mesh
					newMesh.addVertex(mesh.getVertex(nodes[j]));
					meshNodeIndexes[nodes[j]] = newMesh.getSize() - 1;
					if (constraints != null)
						newConstraints.add(new MguiBoolean(constraints[nodes[j]]));
					}
					
				//set this node to new mesh index
				nodes[j] = meshNodeIndexes[nodes[j]];
				}
			
			//for each of this triangle's edges
			for (int j = 0; j < 3; j++){
				int k = edgeMesh.searchEdge(edgeMesh.triangles.get(i).edges[j]);
				if (edgeNodeIndexes[k] < 0){
					//add new node to new mesh
					newMesh.addVertex(edgeNodes[k]);
					edgeNodeIndexes[k] = newMesh.getSize() - 1;
					if (constraints != null)
						newConstraints.add(new MguiBoolean(false));
					}
				//set this edge to new mesh index
				edges[j] = edgeNodeIndexes[k];
				}
			
			//we now have six nodes, let's make four triangles
			//note that CW direction of all subtriangles must be the same as the original
			//note that edges are AB, BC, CA and nodes are A, B, C
			//so:
			//A, AB, CA:
			newMesh.addFace(nodes[0], edges[0], edges[2]);
			//AB, B, BC:
			newMesh.addFace(edges[0], nodes[1], edges[1]);
			//AB, BC, CA:
			newMesh.addFace(edges[0], edges[1], edges[2]);
			//CA, BC, C:
			newMesh.addFace(edges[2], edges[1], nodes[2]);
			
			}
		
		mesh.setFromMesh(newMesh);
		//meshInt.setMesh(newMesh);
		//for (int i = 0; i < newConstraints.size(); i++)
		//	meshInt.setConstraint(i, newConstraints.get(i).value);
		boolean[] new_constraints = new boolean[newConstraints.size()];
		for (int i = 0; i < newConstraints.size(); i++)
			new_constraints[i] = newConstraints.get(i).getTrue();
			
		return new_constraints;
		//set mesh to new mesh
		//newmesh = newMesh;
		//newMesh = MeshFunctions.condenseMesh(newMesh);
	}
	
	
}