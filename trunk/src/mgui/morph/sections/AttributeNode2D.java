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

import org.jogamp.vecmath.Point2f;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;


public class AttributeNode2D implements Cloneable {

	public double sortValue;
	public Point2f point;
	public AttributeList attributes = new AttributeList();
	public String idStr;
	
	public AttributeNode2D(){
		init();
		}
	
	public AttributeNode2D(Point2f thisPt){
		point = thisPt;
		init();
		}
	
	private void init(){
		attributes.add(new Attribute("SourceIndex", new MguiInteger(0)));
	}
	
	public void setSortAttribute(String attrStr){
		if (attributes.getValue(attrStr) instanceof Double)
			sortValue = ((MguiDouble)attributes.getValue(attrStr)).getValue();
	}
	
	public int getIndex(){
		return (((MguiInteger)attributes.getValue("SourceIndex")).getInt());
	}
	
	@Override
	public Object clone(){
		AttributeNode2D retObj = new AttributeNode2D();
		retObj.point = (Point2f)point.clone();
		retObj.attributes = (AttributeList)attributes.clone();
		return retObj;
	}
	
	public int getSourceIndex(){
		return ((MguiInteger)attributes.getValue("SourceIndex")).getInt();
	}
	
}