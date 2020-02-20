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

package mgui.interfaces;

import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.util.InterfaceFunctions;


/*********************************
 * Base interface for specifying options or parameters for some process.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfaceOptions implements InterfaceObject {
	
	
	@Override
	public void clean(){
		InterfaceFunctions.cleanInterfaceObject(this);
	}
	
	@Override
	public void destroy(){}
	
	@Override
	public boolean isDestroyed(){
		return false;
	}
	
	@Override
	public String getName(){
		return this.getClass().getName();
	}
	
	@Override
	public void setName(String name) {
		
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
	public String getTreeLabel() {
		return getName();
	}

}