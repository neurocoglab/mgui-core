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

import mgui.geometry.Mesh3D;
import mgui.numbers.MguiInteger;


/********************************
 * Constructs a list of node neighbourhoods, i.e., for each node lists its neighbours. This format
 * is useful for quickly searching a vertex's neighbouring vertices.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NeighbourhoodMesh {

	public ArrayList<Neighbourhood> neighbourhoods = new ArrayList<Neighbourhood>();
	
	public NeighbourhoodMesh(Mesh3D mesh){
		
		for (int i = 0; i < mesh.n; i++)
			neighbourhoods.add(new Neighbourhood(i));
		
		for (int i = 0; i < mesh.f; i++){
			Mesh3D.MeshFace3D face = mesh.getFace(i);
			neighbourhoods.get(face.A).addNeighbour(face.B);
			neighbourhoods.get(face.A).addNeighbour(face.C);
			neighbourhoods.get(face.B).addNeighbour(face.A);
			neighbourhoods.get(face.B).addNeighbour(face.C);
			neighbourhoods.get(face.C).addNeighbour(face.A);
			neighbourhoods.get(face.C).addNeighbour(face.B);
			}
	}
	
	public Neighbourhood getNeighbourhood(int i){
		return neighbourhoods.get(i);
	}
	
	/*******************************
	 * Return neighbourhood as connected cycle, or null if neighbourhood does
	 * not form a cycle
	 * 
	 * @param i node for which to find a neighbourhood
	 * @return Neighbourhood whose neighbour list forms a connected ring
	 */
	public ArrayList<MguiInteger> getNeighbourhoodRing(int i){
		int[] nbrs = getNeighbourhood(i).getNeighbourList();
		
		if (nbrs.length < 3) return null;
		ArrayList<MguiInteger> c = new ArrayList<MguiInteger>(nbrs.length);
		c.add(new MguiInteger(nbrs[0]));
		
		int current = 0, next = 1;
		Neighbourhood neighbours;
		boolean[] passed = new boolean[nbrs.length];
		passed[0] = true;
		neighbours = getNeighbourhood(nbrs[0]);
		
		while (next < nbrs.length){
			//neighbours = getNeighbourhood(nbrs[current]);
			if (!passed[next] && neighbours.hasNeighbour(nbrs[next])){
				c.add(new MguiInteger(nbrs[next]));
				passed[next] = true;
				current = next;
				neighbours = getNeighbourhood(nbrs[current]);
				next = 0;
				}
			next++;
			}
			
		if (c.size() == nbrs.length){
			//is there a cycle?
			neighbours = getNeighbourhood(c.get(0).getInt());
			if (neighbours.hasNeighbour(c.get(c.size() - 1).getInt()))
				return c;
			}
		
		return null;
	}
	
	/*************************
	 * Returns true if this node lies on a mesh boundary; false otherwise.
	 * 
	 * @param i
	 * @return
	 */
	public boolean isBoundaryNode(int i){
		return getNeighbourhoodRing(i) == null;
	}
	
}