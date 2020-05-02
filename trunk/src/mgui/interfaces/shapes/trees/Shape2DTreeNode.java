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

import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.util.ShapeListener;


public class Shape2DTreeNode extends ShapeTreeNode implements ShapeListener {

	//public InterfaceTreePanel treePanel;
	private boolean isDestroyed = false;
	
	public Shape2DTreeNode(){
		super();
	}
	
	public Shape2DTreeNode(Shape2DInt thisShape){
		super();
		setUserObject(thisShape);
	}
	
	public void setUserObject(Shape2DInt thisShape){
		super.setUserObject(thisShape);
		//set attributes nodes
		if (thisShape.getAttributes() != null)
			add(thisShape.getAttributes().issueTreeNode());
	}
	
	@Override
	public Shape2DInt getUserObject(){
		return (Shape2DInt)super.getUserObject();
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu() {
		
		Shape2DInt shape = (Shape2DInt)getShape();
		return shape.getPopupMenu();
		
	}
	
	@Override
	public void removeShapeNode(InterfaceShape shape){
		
		//search children for shape and remove if found
		for (int i = 0; i < children.size(); i++)
			if (children.get(i) instanceof Shape2DTreeNode &&
				((Shape2DTreeNode)children.get(i)).getUserObject().equals(shape)){
				removeChild((ShapeTreeNode)children.get(i));
				return;
				}
				
	}
	
	@Override
	public boolean hasShapeNode(InterfaceShape shape) {
		
		//search children for shape and remove if found
		for (int i = 0; i < children.size(); i++)
			if (children.get(i) instanceof Shape2DTreeNode &&
				((Shape2DTreeNode)children.get(i)).getUserObject().equals(shape)){
				return true;
				}
		
		return false;
		
	}
	
	@Override
	public Object clone(){
		return new Shape2DTreeNode(getUserObject());
	}
	
}