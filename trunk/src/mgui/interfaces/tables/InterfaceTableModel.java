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

package mgui.interfaces.tables;

import javax.swing.table.AbstractTableModel;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.util.InterfaceFunctions;

/*********************************************
 * Abstract Table Model interface class. Can be subclassed to provide an implementation that assigns
 * values from the underlying data model to an instance of <code>InterfaceDataTable
 * 
 * @author Andrew Reid
 *
 */
public abstract class InterfaceTableModel extends AbstractTableModel implements InterfaceObject,
																				AttributeObject {

	
	AttributeList attributes;
	
	protected void _init(){
		attributes.add(new Attribute<String>("Name", "no-name"));
	}
	
	/***********************************************
	 * Returns the source of data for this table.
	 * 
	 * @return
	 */
	public abstract Object getSource();
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return (String)attributes.getValue("Name");
	}

	@Override
	public void clean(){
		InterfaceFunctions.cleanInterfaceObject(this);
	}
	
	@Override
	public boolean isDestroyed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setName(String name) {
		attributes.setValue("Name", name);
	}

	@Override
	public String getTreeLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InterfaceTreeNode issueTreeNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTreeNode(InterfaceTreeNode node) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public Attribute getAttribute(String attrName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AttributeList getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAttributes(AttributeList thisList) {
		// TODO Auto-generated method stub
		
	}

}