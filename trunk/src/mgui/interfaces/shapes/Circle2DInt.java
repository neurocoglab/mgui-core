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

import java.awt.Color;
import java.awt.Graphics2D;

import mgui.geometry.Circle2D;
import mgui.geometry.Rect2D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.numbers.MguiBoolean;

public class Circle2DInt extends Shape2DInt {

	public Circle2DInt() {
		super();
		thisShape = new Circle2D();
		init();
	}
	
	public Circle2DInt(Circle2D thisCircle){
		super();
		thisShape = thisCircle;
		init();
	}
	
	private void init(){
		//set attributes
		//attributes = new AttributeList();
		attributes.add(new Attribute<MguiBoolean>("HasFill", new MguiBoolean(false)));
		attributes.add(new Attribute<Color>("FillColour", Color.WHITE));
		
		//bounds
		updateShape();
	}
	
	public Circle2D getCircle(){
		return (Circle2D)thisShape;
	}
	
	@Override
	public void drawShape2D(Graphics2D g, DrawingEngine d){
		if (!((MguiBoolean)attributes.getValue("IsVisible")).getTrue()) return;
		d.drawing_attributes.setAttribute(attributes.getAttribute("FillColour"));
		d.drawing_attributes.setAttribute(attributes.getAttribute("LineColour"));
		d.drawing_attributes.setAttribute(attributes.getAttribute("LineStyle"));
		d.drawing_attributes.setAttribute(attributes.getAttribute("HasFill"));
		//d.setCoordSys(((arInteger)attributes.getValue("CoordSys")).value);
		d.drawCircle2D(g, (Circle2D)thisShape);
	}
	
	
	
	@Override
	public void updateShape(){
		if (bounds == null){
			bounds = new Rect2D(((Circle2D)thisShape).centerPt.x - ((Circle2D)thisShape).radius,
								((Circle2D)thisShape).centerPt.y - ((Circle2D)thisShape).radius,
								((Circle2D)thisShape).centerPt.x + ((Circle2D)thisShape).radius,
								((Circle2D)thisShape).centerPt.y + ((Circle2D)thisShape).radius);
			}else{
			bounds.corner1.x = ((Circle2D)thisShape).centerPt.x - ((Circle2D)thisShape).radius;
			bounds.corner1.y = ((Circle2D)thisShape).centerPt.y - ((Circle2D)thisShape).radius;
			bounds.corner2.x = ((Circle2D)thisShape).centerPt.x + ((Circle2D)thisShape).radius;
			bounds.corner2.y = ((Circle2D)thisShape).centerPt.y + ((Circle2D)thisShape).radius;
			}
	}

}