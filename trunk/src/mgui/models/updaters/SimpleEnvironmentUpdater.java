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

package mgui.models.updaters;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelUpdater;
import mgui.models.dynamic.Updateable;
import mgui.models.exceptions.DynamicModelException;

/**************************
 * Default super class for environment updaters. 
 * 
 * @author Andrew Reid
 * @version 1.0
 */
public abstract class SimpleEnvironmentUpdater extends AbstractInterfaceObject implements DynamicModelUpdater,
																						  AttributeObject{

	public AttributeList attributes = new AttributeList();
	
	public boolean update(Updateable c, double timeStep) {
		try{
			return test(c, timeStep);
		}catch (DynamicModelException e){
			e.printStackTrace();
			return false;
			}
	}
	
	protected void init(){
		attributes.add(new Attribute<String>("Name", "no-name"));
	}
	
	//test for instances of DynamicModelEnvironment
	protected boolean test(Updateable c, double timeStep) throws DynamicModelException{
		if (!(c instanceof DynamicModelEnvironment)) throw new DynamicModelException(
				"EnvironmentUpdater can only update instances of DynamicModelEnvironment");
		return doUpdate((DynamicModelEnvironment<?>)c, timeStep);
	}
	
	//to be overridden
	protected abstract boolean doUpdate(DynamicModelEnvironment<?> c, double timeStep);

	
	public Attribute<?> getAttribute(String attrName) {	
		return attributes.getAttribute(attrName);
	}
	
	@Override
	public Object getAttributeValue(String name) {
		Attribute<?> attribute = getAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
	}

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
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		treeNode.add(attributes.issueTreeNode());
	}
	
	@Override
	public String toString(){
		return "SimpleEnvironmentUpdater";
	}
	
}