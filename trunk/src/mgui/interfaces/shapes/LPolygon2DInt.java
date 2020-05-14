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

package mgui.interfaces.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import mgui.geometry.Polygon2D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;


/***********
 * Light 2D polygon with no attributes (attributes are assignable)
 * @author Andrew Reid
 *
 */


public class LPolygon2DInt extends Polygon2DInt implements LightweightShape {

	public LPolygon2DInt(){
		super();
		init();
	}
	
	public LPolygon2DInt(Polygon2D thisPoly){
		super(thisPoly);
		init();
	}
	
	private void init(){
		isLight = true;
		releaseAttributes();
		updateShape();
	}
	
	@Override
	public boolean isVisible(){
		if (attributes == null) return false;
		return super.isVisible();
	}
	
	@Override
	public boolean show3D(){
		if (attributes == null) return false;
		return super.show3D();
	}
	
	@Override
	public void setAttributes(AttributeList a){
		attributes = a;
	}
	
	public void releaseAttributes(){
		attributes = null;
	}
	
	@Override
	public void drawShape2D(Graphics2D g, DrawingEngine d){
		if (attributes != null){
			super.drawShape2D(g, d);
			return;
		}
		//set default and then release
		attributes = new AttributeList();
		attributes.add(new Attribute("LineStyle", new BasicStroke()));
		attributes.add(new Attribute("LineColour", Color.BLACK));		
		attributes.add(new Attribute("CoordSys", new MguiInteger(DrawingEngine.DRAW_MAP)));
		attributes.add(new Attribute("IsVisible", new MguiBoolean(true)));
		attributes.add(new Attribute("HasFill", new MguiBoolean(false)));
		attributes.add(new Attribute("FillColour", Color.WHITE));
		attributes.add(new Attribute("ShowNodes", new MguiBoolean(true)));
		attributes.add(new Attribute("NodeColour", Color.BLUE));
		attributes.add(new Attribute("LabelNodes", new MguiBoolean(true)));
		attributes.add(new Attribute("LabelObj", new Text2DInt("N", 10, 7)));
		attributes.add(new Attribute("LabelStrings", new ArrayList<String>()));
		attributes.add(new Attribute("LabelOffsetX", new MguiDouble(5)));
		attributes.add(new Attribute("LabelOffsetY", new MguiDouble(0)));
		super.drawShape2D(g, d);
		releaseAttributes();
	}
	
	@Override
	public String toString(){
		return "LPolygon2D [" + String.valueOf(ID) + "]"; 
	}
	
	@Override
	public Object clone(){
		LPolygon2DInt retObj = new LPolygon2DInt(getPolygon());
		//retObj.attributes = (AttributeList)attributes.clone();
		retObj.updateShape();
		return retObj;
	}
	
	
}