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

/**********************
 * Represents an edge in a mesh as a reference to its one or two adjacent triangular faces.
 * An edge with one triangle is a boundary edge.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */


public class MeshEdge {

	//public MeshTriangle t1, t2;
	public int tri1 = -1, tri2 = -1;
	public int node1 = -1, node2 = -1;
	
	public MeshEdge(int t1, int t2, int n1, int n2){
		tri1 = t1;
		tri2 = t2;
		//smallest first for searching purposes
		if (n1 < n2){
			node1 = n1;
			node2 = n2;
		}else{
			node1 = n2;
			node2 = n1;
			}
	}
	
	public boolean hasOppositeTri(){
		return tri2 >= 0;
	}
	
	public void addTri(int i){
		if (tri1 < 0) 
			tri1 = i;
		else
			if (i != tri1)
				tri2 = i;
	}
	
	public void addNode(int n){
		if (node1 < 0){
			node1 = n;
			return;
			}
		if (n > node1){
			node2 = n;
			}else{
			node2 = node1;
			node1 = n;
			}
	}

}