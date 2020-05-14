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

/***************************
 * Represents a triangular mesh face, defined by three edges and an index referencing a
 * Mesh3D object
 * 
 * @author Andrew Reid
 *
 */


public class MeshTriangle {
	
	public int faceIndex;
	public MeshEdge[] edges = new MeshEdge[3];
	
	public MeshTriangle(int index){
		faceIndex = index;
	}
	
	public MeshTriangle(int index, MeshEdge e1, MeshEdge e2, MeshEdge e3){
		edges[0] = e1;
		edges[1] = e2;
		edges[2] = e3;
	}
	
	public void setEdges(MeshEdge[] e){
		edges[0] = e[0];
		edges[1] = e[1];
		edges[2] = e[2];
	}
	
	public void addEdge(MeshEdge e){
		if (edges[0] == null){
			edges[0] = e;
			return;
			}
		if (edges[1] == null){
			edges[1] = e;
			return;
			}
		edges[2] = e;
	}
	
	public int getOppositeNode(MeshEdge e){
		if (e == edges[0])
			if (edges[1].node1 == e.node1 || edges[1].node1 == e.node2)
				return edges[1].node2;
			else
				return edges[1].node1;
		if (e == edges[1])
			if (edges[2].node1 == e.node1 || edges[2].node1 == e.node2)
				return edges[2].node2;
			else
				return edges[2].node1;
		if (e == edges[2])
			if (edges[0].node1 == e.node1 || edges[0].node1 == e.node2)
				return edges[0].node2;
			else
				return edges[0].node1;
		return -1;
	}
	
	public MeshEdge getOppositeEdge(int i, MeshEdge e){
		if (i < 0 || i > 1) return null;
		if (e == edges[0])
			return edges[1 + i];
		if (e == edges[1]){
			if (i == 1) return edges[0];
			return edges[2];
			}
		if (e == edges[2])
			return edges[i];
		return null;
	}

}