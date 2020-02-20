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

package mgui.interfaces.datasources;

import javax.swing.tree.DefaultMutableTreeNode;

import mgui.datasources.DataField;
import mgui.interfaces.trees.InterfaceTreeNode;

/**********************************************
 * A tree node specialized for a {@link DataField} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataFieldTreeNode extends InterfaceTreeNode {

	public DataFieldTreeNode(){
		super("NoName");
	}
	
	public DataFieldTreeNode(String name){
		super(name);
	}
	
	public void setValues(DataField f){
		this.removeAllChildren();
		add(f.getAttributeList().issueTreeNode());
		
	}
	
	public void addNode(String name, Object value){
		//if exists, set value
		
		//otherwise add new
		add(new DefaultMutableTreeNode(name + ": " + value));
		
	}
	
}