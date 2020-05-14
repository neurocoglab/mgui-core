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

package mgui.interfaces.plots;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeObject;

/********************************************
 * Abstract representation of a plottable object; allows objects to be represented generically
 * as interface objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfacePlotObject extends AbstractInterfaceObject
										  implements AttributeObject,
										  			 AttributeListener{

	
	protected AttributeList attributes = new AttributeList();
	
	
	@Override
	public Attribute<?> getAttribute(String name) {
		return attributes.getAttribute(name);
	}

	@Override
	public AttributeList getAttributes() {
		return attributes;
	}

	@Override
	public void setAttribute(String name, Object value) {
		attributes.setValue(name, value);
	}

	@Override
	public void setAttributes(AttributeList attributes) {
		this.attributes = attributes;
	}
	
	@Override
	public Object getAttributeValue(String name) {
		Attribute<?> attribute = getAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
	}

}