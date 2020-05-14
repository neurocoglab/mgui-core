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
import java.util.Collections;
import java.util.Comparator;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.util.GeometryFunctions;


/**********************************
 * Sorts a mesh by x, y, z and stores a list of sorted indices
 * 
 * @author AndrewR
 *
 */

public class SortedMesh {

	public ArrayList<Integer> sorted_nodes;
	public ArrayList<Integer> sorted_faces;
	
	public SortedMesh(Mesh3D mesh){
		
		ArrayList<Node> nodes = new ArrayList<Node>(mesh.n);
		ArrayList<Point3f> pts = mesh.getVertices();
		
		for (int i = 0; i < pts.size(); i++)
			nodes.add(new Node(i, pts.get(i)));
		
		ArrayList<Face> faces = sortFaces(mesh.getFaces());
	
		Collections.sort(nodes, new NodeComparator());
		//Collections.sort(faces, new FaceComparator());
		
		sorted_nodes = new ArrayList<Integer>(pts.size());
		for (int i = 0; i < nodes.size(); i++)
			sorted_nodes.add(new Integer(nodes.get(i).index));
		
		sorted_faces = new ArrayList<Integer>(pts.size());
		for (int i = 0; i < faces.size(); i++)
			sorted_faces.add(new Integer(faces.get(i).index));
	}
	
	public static Mesh3D.MeshFace3D getSortedFace(Mesh3D.MeshFace3D face){
		Mesh3D.MeshFace3D newFace = new Mesh3D.MeshFace3D(face);
		
		newFace.A = face.A;
		newFace.B = face.B;
		
		if (face.A > face.B){
			newFace.A = face.B;
			newFace.B = face.A;
			}
		
		if (face.C < newFace.A){
			newFace.C = newFace.B;
			newFace.B = newFace.A;
			newFace.A = face.C;
		}else if (face.C < newFace.B){
			newFace.C = newFace.B;
			newFace.B = face.C;
		}else{
			newFace.C = face.C;
			}
		return newFace;
	}
	
	ArrayList<Face> sortFaces(ArrayList<Mesh3D.MeshFace3D> faces){
		ArrayList<Face> sorted = new ArrayList<Face>(faces.size());
		for (int i = 0; i < faces.size(); i++){
			Mesh3D.MeshFace3D face = faces.get(i);
			Mesh3D.MeshFace3D newFace = new Mesh3D.MeshFace3D(face);
			
			newFace.A = face.A;
			newFace.B = face.B;
			
			if (face.A > face.B){
				newFace.A = face.B;
				newFace.B = face.A;
				}
			
			if (face.C < newFace.A){
				newFace.C = newFace.B;
				newFace.B = newFace.A;
				newFace.A = face.C;
			}else if (face.C < newFace.B){
				newFace.C = newFace.B;
				newFace.B = face.C;
			}else{
				newFace.C = face.C;
				}
			
			sorted.add(new Face(i, newFace));
			}
		return sorted;
	}
	
	class FaceComparator implements Comparator<Face>{
		public int compare(Face f1, Face f2) {
			if (f1.f.A > f2.f.A) return 1;
			if (f1.f.A < f2.f.A) return -1;
			if (f1.f.B > f2.f.B) return 1;
			if (f1.f.B < f2.f.B) return -1;
			if (f1.f.C > f2.f.C) return 1;
			if (f1.f.C < f2.f.C) return -1;
			return 0;
		}
	}
	
	class NodeComparator implements Comparator<Node>{
		public int compare(Node n1, Node n2) {
			int t = GeometryFunctions.compareFloat(n1.pt.x, n2.pt.x);
			if (t != 0) return t;
			t = GeometryFunctions.compareFloat(n1.pt.y, n2.pt.y);
			if (t != 0) return t;
			return GeometryFunctions.compareFloat(n1.pt.z, n2.pt.z);
		}
	}
	
	class Node{
		
		public Point3f pt;
		public int index;
		
		public Node(int index, Point3f pt){
			this.index = index;
			this.pt = pt;
		}
		
	}
	
	class Face{
		
		public Mesh3D.MeshFace3D f;
		public int index;
		
		public Face(int index, Mesh3D.MeshFace3D f){
			this.index = index;
			this.f = f;
		}
		
	}
	
}