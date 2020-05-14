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

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.util.ShapeListener;

/*********************************************
 * A tree node specialized for an instance of {@linkplain Shape3DInt}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Shape3DTreeNode extends ShapeTreeNode implements ShapeListener{
	
	public Shape3DTreeNode(){
		super();
	}
	
	public Shape3DTreeNode(Shape3DInt thisShape){
		super();
		setUserObject(thisShape);
	}
	
	@Override
	public void setUserObject(InterfaceObject obj){
		super.setUserObject(obj);
		this.removeAllChildren();
		
		Shape3DInt shape = (Shape3DInt)obj;
		
		if (shape.getLocalAttributes() != null)
			add(shape.getLocalAttributes().issueTreeNode());

	}
	
	@Override
	public Shape3DInt getUserObject(){
		return (Shape3DInt)super.getUserObject();
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu() {
		
		Shape3DInt shape = (Shape3DInt)getShape();
		return shape.getPopupMenu();
		
	}
	
	@Override
	public void removeShapeNode(InterfaceShape shape){
		
		//search children for shape and remove if found
		for (int i = 0; i < children.size(); i++)
			if (children.get(i) instanceof Shape3DTreeNode &&
					((Shape3DTreeNode)children.get(i)).getUserObject().equals(shape)){
				removeChild((ShapeTreeNode)children.get(i));
				return;
				}
				
	}
	
	@Override
	public boolean hasShapeNode(InterfaceShape shape) {
		
		//search children for shape and remove if found
		for (int i = 0; i < children.size(); i++)
			if (children.get(i) instanceof Shape3DTreeNode &&
					((Shape3DTreeNode)children.get(i)).getUserObject().equals(shape)){
				return true;
				}
		
		return false;
		
	}
	
	@Override
	public Object clone(){
		return new Shape3DTreeNode(getUserObject());
	}
	
}