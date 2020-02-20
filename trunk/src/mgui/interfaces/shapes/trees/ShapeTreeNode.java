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

package mgui.interfaces.shapes.trees;

import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.ShapeSet;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.trees.util.TreeFunctions;

/******************************************************
 * Provides a tree node for an instance of {@link InterfaceShape}. Provides an implementation of {@link ShapeListener}
 * in order to allow shape sets to respond to additions, removals, or changes to their members.
 * 
 * <p>TODO: eliminate the subclasses {{Shape3DTreeNode}} and {{Shape2DTreeNode}} and make this non-abstract
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class ShapeTreeNode extends InterfaceTreeNode implements ShapeListener {
	
	/***************************************
	 * Respond to a shape event on this node's ShapeInt. The current implementation only responds to updates on a
	 * {@link ShapeSet}, but adding, removing, or moving child nodes depending on the nature of the event. 
	 * 
	 * @param e
	 */
	public void shapeUpdated(ShapeEvent e){
		ShapeSet set = null;
		InterfaceShape shape = null;
		VertexDataColumn column = null;
		InterfaceTreeNode v_node = null;
		
		switch (e.eventType){
			case ShapeAdded:
				if (!(e.getShape() instanceof ShapeSet)) return;
				set = (ShapeSet)e.getShape();
				if (set.getLastAdded() == null) return;
				//add shape's tree node to this tree 
				shape = set.getLastAdded();
				addChild(shape.issueTreeNode());
				return;
					
			case ShapeRemoved:
				if (!(e.getShape() instanceof ShapeSet)) return;
				set = (ShapeSet)e.getShape();
				if (set.getLastRemoved() == null) return;
				//remove shape's tree node from this tree 
				shape = set.getLastRemoved();
				removeShapeNode(shape);
				return;
		
			case ShapeInserted:
				if (!(e.getShape() instanceof ShapeSet)) return;
				set = (ShapeSet)e.getShape();
				if (set.getLastInserted() == null) return;
				shape = set.getLastInserted();
				insertChild(shape.issueTreeNode(), set.getLastInsert());
				break;
				
			case ShapeMoved:
				if (!(e.getShape() instanceof ShapeSet)) return;
				
				set = (ShapeSet)e.getShape();
				if (set.getLastInserted() == null) return;
				shape = set.getLastInserted();
				ShapeTreeNode child_node = getChildForShape(shape);
				if (child_node == null) return;
				moveChild(child_node, set.getLastInsert());
				return;
				
			case VertexColumnAdded:
				column = e.getShape().getLastColumnAdded();
				if (column == null) return;
				v_node = getVertexDataNode();
				if (v_node == null) return;
				v_node.addChild(column.issueTreeNode());
				return;
				
			case VertexColumnRemoved:
				column = e.getShape().getLastColumnRemoved();
				if (column == null) return;
				v_node = getVertexDataNode();
				if (v_node == null) return;
				column.destroy(); 				// Will detach from parent
				return;
				
			case VertexColumnChanged:
			case VertexColumnRenamed:
				column = e.getShape().getLastColumnChanged();
				if (column == null) return;
				v_node = getVertexDataColumnNode(column);
				if (v_node == null) return;
				v_node.objectChanged();
				return;
			
			case ShapeSetModified:
				this.objectChanged();
				return;
				
			}
	}
	
	protected InterfaceTreeNode getVertexDataNode(){
		for (int i = 0; i < children.size(); i++)
			if (((InterfaceTreeNode)children.get(i)).getUserObject() instanceof InterfaceShape.VertexDataSet){
				return (InterfaceTreeNode)children.get(i);
				}
		return null;
	}
	
	protected InterfaceTreeNode getVertexDataColumnNode(VertexDataColumn column){
		DefaultMutableTreeNode v_node = getVertexDataNode();
		ArrayList<TreeNode> children = TreeFunctions.getChildren(v_node);
		for (int i = 0; i < children.size(); i++)
			if (((InterfaceTreeNode)children.get(i)).getUserObject().equals(column)){
				return (InterfaceTreeNode)children.get(i);
				}
		return null;
	}
	
	public ShapeTreeNode getChildForShape(InterfaceShape shape){
		for (int i = 0; i < children.size(); i++)
			if (children.get(i) instanceof ShapeTreeNode &&
					((ShapeTreeNode)children.get(i)).getUserObject().equals(shape)){
				return (ShapeTreeNode)children.get(i);
				}
		return null;
	}
	
	public abstract void removeShapeNode(InterfaceShape shape);
	
	public InterfaceShape getShape(){
		return (InterfaceShape)getUserObject();
	}
	
}