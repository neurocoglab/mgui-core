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

package mgui.interfaces.shapes;

import java.awt.BasicStroke;
import java.awt.Color;

import mgui.geometry.Polygon3D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;


public class LPolygon3DInt extends Polygon3DInt implements LightweightShape {

	public LPolygon3DInt(){
		init();
	}
	
	public LPolygon3DInt(Polygon3D poly){
		setShape(poly);
		init();
	}
	
	@Override
	protected void init(){
		super.init();
		isLight = true;
		//releaseAttributes();
	}
	
	@Override
	public void setAttributes(AttributeList a){
		//if (attributes != null) attributes.removeAttributeListener(this);
		attributes = a;
		//attributes.addAttributeListener(this);
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		//parent handles this
		
	}
	
	public void releaseAttributes(){
		attributes.removeAttributeListener(this);
		attributes = null;
	}
	
	@Override
	public void setScene3DObject(){
		if (attributes == null) setDefaultAttributes();
		super.setScene3DObject();
		//releaseAttributes();
	}
	
	protected void setDefaultAttributes(){
		if (attributes != null) attributes.removeAttributeListener(this);
		attributes = new AttributeList();
		attributes.add(new Attribute("LineStyle", new BasicStroke()));
		attributes.add(new Attribute("LineColour", Color.BLUE));
		//attributes.add(new Attribute("CoordSys", coordSys));
		attributes.add(new Attribute("IsVisible", new MguiBoolean(true)));
		attributes.add(new Attribute("HasTransparency", new MguiBoolean(false)));
		attributes.add(new Attribute("Alpha", new MguiFloat(1.0f)));
		attributes.add(new Attribute("Show2D", new MguiBoolean(false)));
		attributes.add(new Attribute("SetAlpha2D", new MguiBoolean(false)));
		attributes.add(new Attribute("HasFill", new MguiBoolean(false)));
		attributes.add(new Attribute("FillColour", Color.WHITE));
		attributes.add(new Attribute("ShowNodes", new MguiBoolean(true)));
		attributes.add(new Attribute("NodeColour", Color.GRAY));
		attributes.add(new Attribute("IsClosed", new MguiBoolean(true)));
		attributes.add(new Attribute("AsCylinder", new MguiBoolean(false)));
		attributes.add(new Attribute("CylRadius", new MguiFloat(1.0f)));
		attributes.add(new Attribute("CylEdges", new MguiInteger(8)));
		attributes.addAttributeListener(this);
	}
	
	
	/*
	public void setTreeNode(InterfaceTreeNode treeNode) {
		//if (treeNode == null)
		//	treeNode = new InterfaceShape3DNode(this);
		
		//treeNode.removeAllChildren();
		
		//if (nodeListener == null)
		//	addShapeListener((InterfaceShape3DNode)treeNode);
		//nodeListener = (InterfaceShape3DNode)treeNode;
	}
	*/
	
}