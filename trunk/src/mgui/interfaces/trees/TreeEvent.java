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

package mgui.interfaces.trees;

import java.util.EventObject;


public class TreeEvent extends EventObject {

	public static enum EventType{
		General,
		NodeAdded,
		NodeRemoved,
		NodeModified,
		NodeDestroyed,
		NodeInserted;
		}
	
	public EventType eventType = EventType.General; 
	InterfaceTreeNode child_node;
	public int insert_at = -1;
	
	public TreeEvent(InterfaceTreeNode node, EventType type){
		super(node);
		eventType = type;
	}
	
	public TreeEvent(InterfaceTreeNode parent_node, InterfaceTreeNode child_node, EventType type){
		super(parent_node);
		this.child_node = child_node;
		eventType = type;
	}
	
	public TreeEvent(InterfaceTreeNode parent_node, InterfaceTreeNode child_node, EventType type, int insert_at){
		super(parent_node);
		this.child_node = child_node;
		eventType = type;
		this.insert_at = insert_at;
	}
	
	public InterfaceTreeNode getParentNode(){
		return getNode();
	}
	
	public InterfaceTreeNode getNode(){
		return (InterfaceTreeNode)getSource();
	}
	
	public InterfaceTreeNode getChildNode(){
		return child_node;
	}
		
}