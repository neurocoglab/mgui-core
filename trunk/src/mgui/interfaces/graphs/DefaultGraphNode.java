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

package mgui.interfaces.graphs;

import org.jogamp.vecmath.Point3f;

import mgui.interfaces.xml.XMLObject.XMLType;

import org.apache.commons.collections15.Factory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/*************************************************
 * Default implementation of a Graph in modelGUI.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DefaultGraphNode extends AbstractGraphNode {

	public DefaultGraphNode(){
		this("No-name");
	}
	
	public DefaultGraphNode(String label){
		super(label);
	}
	
	public Factory<AbstractGraphNode> getFactory(){
		return new Factory<AbstractGraphNode>(){
			
			public AbstractGraphNode create(){
				return new DefaultGraphNode();
			}
		};
	}
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type)
			throws SAXException {
		
		super.handleXMLElementStart(localName, attributes, type);
		
	}

	public Object clone() throws CloneNotSupportedException{
		DefaultGraphNode node = new DefaultGraphNode(this.getName());
		node.setCurrentValue(current_value.getValue());
		node.setLocation(location);
		return node;
	}
	
}