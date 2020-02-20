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

package mgui.interfaces.layouts;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;

/**********************************************************************
 * Interface panel allowing interaction with a 2D Layout object.
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfaceLayoutPanel extends InterfacePanel 
							 			   implements AttributeObject {

	
	
	
	
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showPanel() {
		// TODO Auto-generated method stub
		super.showPanel();
	}

	@Override
	public void updateDisplay() {
		// TODO Auto-generated method stub
		super.updateDisplay();
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