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

package mgui.morph.sections;

import java.util.ArrayList;

import mgui.interfaces.attributes.AttributeList;
import mgui.numbers.MguiDouble;


/*************
 * Provides a mapping from a node to a set of other nodes. Can be used as an element
 * in a link list to provide a mapping of any number of nodes.
 * @author Andrew Reid
 *
 */

public class NodeMap2D implements Cloneable {

	public AttributeNode2D thisNode = new AttributeNode2D();
	public ArrayList<AttributeNode2D> nodes = new ArrayList<AttributeNode2D>();
	public AttributeList attributes = new AttributeList();
	public double sortValue;
	
	public NodeMap2D(){
		
	}
	
	public NodeMap2D(AttributeNode2D node){
		thisNode = node;
	}
	
	public void addNode(AttributeNode2D newNode){
		nodes.add(newNode);
	}
	
	public void setSortAttribute(String attrStr){
		if (attributes.getValue(attrStr) instanceof Double)
			sortValue = ((MguiDouble)attributes.getValue(attrStr)).getValue();
	}
	
	public void setNodeSortAttribute(String thisAttr){
		sortValue = 0;
		for (int i = 0; i < nodes.size(); i++){
			nodes.get(i).setSortAttribute(thisAttr);
			sortValue += nodes.get(i).sortValue;
			}
	}
	
	@Override
	public Object clone(){
		ArrayList<AttributeNode2D> newList = new ArrayList<AttributeNode2D>();
		for (int i = 0; i < nodes.size(); i++)
			newList.add((AttributeNode2D)nodes.get(i).clone());
		NodeMap2D retObj = new NodeMap2D();
		retObj.attributes = (AttributeList)attributes.clone();
		return retObj;
	}
	
	
}