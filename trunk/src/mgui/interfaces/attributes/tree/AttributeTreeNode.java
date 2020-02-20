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

package mgui.interfaces.attributes.tree;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.trees.InterfaceTreeNode;

/***************************************************
 * Provides a tree node to display and update an {@link Attribute}. Should be used in conjunction with
 * {@link AttributeTreeCellRenderer} and {@link AttributeTreeCellEditor}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class AttributeTreeNode extends InterfaceTreeNode{
	
	public AttributeTreeNode(){
		super();
		isMouseOverSelect = true;
		//isSelectable = false;
	}
	
	public AttributeTreeNode(Attribute<?> thisAttr){
		super();
		setUserObject(thisAttr);
		isMouseOverSelect = true;
	}
	
	public void setUserObject(Attribute<?> thisAttr){
		//TODO: Use dot notation to indicate branches (i.e., categories)
		super.setUserObject(thisAttr);
		this.removeAllChildren();
		if (thisAttr.getValue() instanceof AttributeObject)
			this.add(((AttributeObject)thisAttr.getValue()).getAttributes().issueTreeNode());
	}
	
	@Override
	public Attribute getUserObject(){
		return (Attribute)super.getUserObject();
	}
	
	@Override
	public Object clone(){
		return new AttributeTreeNode(getUserObject());
	}
	
}