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

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/*************************************************
 * Represents a 1-ring neighbourhood in a mesh.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Neighbourhood{
	public int node;
	public TreeSet<Integer> neighbours = new TreeSet<Integer>();
	
	public Neighbourhood(int n){
		node = n;
	}

	public Neighbourhood(int n, List<Integer> list){
		neighbours = new TreeSet<Integer>(list);
		node = n;
	}
	
	public void addNeighbour(int n){
		neighbours.add(n);
	}
	
	public int[] getNeighbourList(){
		Iterator<Integer> itr = neighbours.iterator();
		int[] list = new int[neighbours.size()];
		
		int i = 0;
		while (itr.hasNext())
			list[i++] = itr.next();
		
		return list;
	}
	
	public int getSize(){
		return neighbours.size();
	}
	
	public boolean hasNeighbour(int n){
		return neighbours.contains(n);
	}
	
	/*
	public void removeNode(int node){
		Iterator<arInteger> itr = neighbours.iterator();
		while (itr.hasNext()){
			arInteger n = itr.next();
			if (n.value > node) 
				n.value--;
			else if (n.value == node)
				neighbours.remove(n);
			}
	}
	*/
	
}