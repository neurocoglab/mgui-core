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

package mgui.models.dynamic.functions;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.trees.InterfaceTreeNode;


/********************************
 * Abstract class for all model functions.
 * 
 * @author Andrew Reid
 *
 */
public abstract class Function extends AbstractInterfaceObject 
							   implements AttributeObject{

	public AttributeList attributes;
	//protected ArrayList<InterfaceTreeNode> treeNodes = new ArrayList<InterfaceTreeNode>();
	public boolean isDestroyed = false;
	
	protected void init(){
		attributes = new AttributeList();
	}
	
	/********************************
	 * Evaluate function with a single parameter d
	 * @param d
	 * @return result of the evaluation
	 */
	public abstract double evaluate(double d);
	
	/********************************
	 * Evaluate function with multiple parameters d
	 * @param d
	 * @return result of the evaluation
	 */
	public abstract double[] evaluate(double[] d);
		
	public Attribute<?> getAttribute(String attrName) {	
		return attributes.getAttribute(attrName);
	}

	@Override
	public AttributeList getAttributes() {
		return attributes;
	}

	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}
	
	@Override
	public Object getAttributeValue(String name) {
		Attribute<?> attribute = getAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
	}

	public InterfaceTreeNode getTreeNodeCopy() {
		return null;
	}

	@Override
	public void setTreeNode(InterfaceTreeNode treeNode) {
		super.setTreeNode(treeNode);
		treeNode.add(attributes.issueTreeNode());
	}
	
	@Override
	public Object clone(){
		return null;
	}
	
	@Override
	public String toString(){
		return "Function";
	}
	
	@Override
	public void destroy(){
		isDestroyed = true;
	}
	
	@Override
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
}