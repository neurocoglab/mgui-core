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

package mgui.interfaces.shapes.mesh;

import java.util.ArrayList;
import java.util.Collections;

import mgui.interfaces.AbstractInterfaceObject;


/************************************************
 * Defines a selection of nodes for a shape object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VertexSelection extends AbstractInterfaceObject {

	protected ArrayList<Boolean> selected_nodes = new ArrayList<Boolean>();
	
	public VertexSelection(int n){
		selected_nodes = new ArrayList<Boolean>(n);
		for (int i = 0; i < n; i++)
			selected_nodes.add(new Boolean(false));
	}
	
	public VertexSelection(ArrayList<Boolean> selected){
		this.selected_nodes = new ArrayList<Boolean>(selected);
	}
	
	public VertexSelection(boolean[] selected){
		selected_nodes = new ArrayList<Boolean>(selected.length);
		for (int i = 0; i < selected.length; i++)
			selected_nodes.add(selected[i]);
	}
	
	public boolean isSelected(int i){
		return selected_nodes.get(i);
	}
	
	public void select(int i){
		selected_nodes.set(i, true);
	}
	
	public void select(ArrayList<Integer> indices){
		for (Integer i : indices)
			selected_nodes.set(i, true);
	}
	
	public void deselect(int i){
		selected_nodes.set(i, false);
	}
	
	public void deselect(ArrayList<Integer> indices){
		for (Integer i : indices)
			selected_nodes.set(i, false);
	}
	
	public void clear(){
		Collections.fill(selected_nodes, false);
	}
	
	public void set(int index, boolean selected){
		if (index < 0 || index >= selected_nodes.size()) return;
		selected_nodes.set(index, selected);
	}
	
	/*****************************************************
	 * Returns a new selection which is the intersection of this object and {@code selection}.
	 * 
	 * @param selection
	 * @return
	 */
	public VertexSelection getIntersection(VertexSelection selection){
		VertexSelection intersection = new VertexSelection(selected_nodes.size());
		for (int i = 0; i < selected_nodes.size(); i++)
			intersection.set(i, selection.isSelected(i) && this.isSelected(i));
		return intersection;
	}
	
	/*****************************************************
	 * Returns a new selection which is the union of this object and {@code selection}.
	 * 
	 * @param selection
	 * @return
	 */
	public VertexSelection getUnion(VertexSelection selection){
		VertexSelection intersection = new VertexSelection(selected_nodes.size());
		for (int i = 0; i < selected_nodes.size(); i++)
			intersection.set(i, selection.isSelected(i) || this.isSelected(i));
		return intersection;
	}
	
	public void set(VertexSelection selection){
		this.selected_nodes = new ArrayList<Boolean>(selection.selected_nodes);
		// Fire event...
	}
	
	/*****************************************************
	 * Returns the count of selected vertices
	 * 
	 * @return
	 */
	public int getSelectedCount(){
		int count = 0;
		for (Boolean b : selected_nodes)
			if (b) count++;
		return count;
	}
	
}