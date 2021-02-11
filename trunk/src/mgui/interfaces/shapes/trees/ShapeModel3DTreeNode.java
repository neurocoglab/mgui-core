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

package mgui.interfaces.shapes.trees;

import java.util.ArrayList;

import javax.swing.JTree;

import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.trees.InterfaceTreeNode;

/***********************************************************
 * Tree node specialized for a {@linkplain ShapeModel3D} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeModel3DTreeNode extends InterfaceTreeNode implements ShapeListener {

	public ShapeModel3DTreeNode(){
		super();
	}
	
	public ShapeModel3DTreeNode(ShapeModel3D thisObj){
		super(thisObj);
	}
	
	public ShapeModel3DTreeNode(ShapeModel3D thisObj, JTree parent){
		super(thisObj, parent);
	}
	
	public ShapeModel3D getShapeModel(){
		return (ShapeModel3D) this.getUserObject();
	}
	
	public void shapeUpdated(ShapeEvent e){
		ShapeSet set = null;
		InterfaceShape shape = null;
		
		ShapeModel3D model = getShapeModel();
		if (model == null) return;
		ShapeSet3DInt model_set = model.getModelSet();
		if (model_set == null) return;
		int offset = 1;
		
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
				insertChild(shape.issueTreeNode(), set.getLastInsert() + offset);
				break;
				
			case ShapeMoved:
				if (!(e.getShape() instanceof ShapeSet)) return;
				
				set = (ShapeSet)e.getShape();
				if (set.getLastMoved() == null) return;
				shape = set.getLastMoved();
				ShapeTreeNode child_node = getChildForShape(shape);
				if (child_node == null) return;
				moveChild(child_node, set.getLastInsert() + offset);
				return;
				
			case ShapeSetModified:
				
				if (!(e.getShape() instanceof ShapeSet3DInt)) return;
				ShapeSet3DInt set3d = (ShapeSet3DInt)e.getShape();
				if (!this.hasShapeNode(set3d)) {
					addChild(set3d.issueTreeNode());
					}
				
				this.objectChanged();
				return;
				
			}
	}
	
	public boolean hasShapeNode(InterfaceShape shape) {
		
		//search children for shape and remove if found
		for (int i = 0; i < children.size(); i++)
			if (children.get(i) instanceof Shape3DTreeNode &&
					((Shape3DTreeNode)children.get(i)).getUserObject().equals(shape)){
				return true;
				}
		
		return false;
		
	}
	
	public void removeShapeNode(InterfaceShape shape){
		
		//search children for shape and remove if found
		for (int i = 0; i < children.size(); i++)
			if (children.get(i) instanceof Shape3DTreeNode &&
					((Shape3DTreeNode)children.get(i)).getUserObject().equals(shape)){
				removeChild((ShapeTreeNode)children.get(i));
				return;
				}
				
	}
	
	public ShapeTreeNode getChildForShape(InterfaceShape shape){
		for (int i = 0; i < children.size(); i++)
			if (children.get(i) instanceof ShapeTreeNode &&
					((ShapeTreeNode)children.get(i)).getUserObject().equals(shape)){
				return (ShapeTreeNode)children.get(i);
				}
		return null;
	}
	
}