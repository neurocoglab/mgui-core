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

package mgui.interfaces;

import java.util.ArrayList;

import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.trees.ShapeTreeNode;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.util.InterfaceFunctions;

/******************************************
 * Abstract class which acts as the base class for all "interface" objects; i.e., objects which provide
 * an interface between user and the underlying model. Provides generic implementations for tree nodes
 * and object destruction.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0.0
 *
 */

public abstract class AbstractInterfaceObject implements InterfaceObject{

	protected boolean isDestroyed = false;
	protected ArrayList<InterfaceTreeNode> tree_nodes = new ArrayList<InterfaceTreeNode>();
	private String name = "No name";
	
	@Override
	public void clean(){
		InterfaceFunctions.cleanInterfaceObject(this);
	}
	
	/*********************************************************
	 * Issues a new tree node and sets it using {@link setTreeNode}. The tree node is stored in this object, which 
	 * facilitates their destruction when necessary (e.g., when this object is destroyed). All issued tree nodes can
	 * informed of changes to their user object using the method {@link updateTreeNodes}.
	 * 
	 * @return a new <code>InterfaceTreeNode</code>
	 */
	@Override
	public InterfaceTreeNode issueTreeNode(){
		InterfaceTreeNode treeNode = new InterfaceTreeNode(this);
		setTreeNode(treeNode);
		tree_nodes.add(treeNode);
		return treeNode;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode tree_node){
		
		tree_node.destroyAllChildren();
		tree_node.removeAllChildren();
		tree_node.setUserObject(this);
	}
	
	public String getTreeLabel(){
		return getName();
	}
	
	public void updateTreeNodes(){
		ArrayList<InterfaceTreeNode> nodes = new ArrayList<InterfaceTreeNode>(tree_nodes);
		for (int i = 0; i < nodes.size(); i++){
			nodes.get(i).objectChanged();
			}
		
		for (InterfaceTreeNode node : nodes) {
			if (node.isDestroyed()) {
				tree_nodes.remove(node);
				}
			}
		
	}

	@Override
	public void destroy(){
		isDestroyed = true;
		ArrayList<InterfaceTreeNode> nodes = new ArrayList<InterfaceTreeNode>(tree_nodes);
		for (int i = 0; i < nodes.size(); i++){
			nodes.get(i).destroy();
			if (nodes.get(i).getParent() != null)
				nodes.get(i).removeFromParent();
			tree_nodes.remove(nodes.get(i));
			}
		tree_nodes = null;
	}
	
	@Override
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	@Override
	public String getName(){
		if (name == null) return toString();
		return name;
	}
	
	@Override
	public void setName(String name){
		this.name = name;
	}

	
}