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

import java.awt.Graphics2D;

import mgui.geometry.LineSegment2D;
import mgui.geometry.Rect2D;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiInteger;


public class Line2DInt extends Shape2DInt {

	public Line2DInt() {
		super();
		thisShape = new LineSegment2D();
		init();
	}
	
	public Line2DInt(LineSegment2D thisLine){
		super();
		thisShape = thisLine;
		init();
	}
	
	private void init(){
		//bounds
		updateShape();
	}
	
	public void drawShape(Graphics2D g, DrawingEngine d){
		if (!((MguiBoolean)attributes.getValue("IsVisible")).getTrue()) return;
		d.drawing_attributes.setAttribute(attributes.getAttribute("LineColour"));
		d.drawing_attributes.setAttribute(attributes.getAttribute("LineStyle"));
		d.setCoordSys(((MguiInteger)attributes.getValue("CoordSys")).getInt());
		d.drawLine2D(g, (LineSegment2D)thisShape);
	}
	
	@Override
	public void updateShape(){
		if (bounds == null){
			bounds = new Rect2D(Math.min(((LineSegment2D)thisShape).pt1.x, ((LineSegment2D)thisShape).pt2.x),
								Math.min(((LineSegment2D)thisShape).pt1.y, ((LineSegment2D)thisShape).pt2.y),
								Math.max(((LineSegment2D)thisShape).pt1.x, ((LineSegment2D)thisShape).pt2.x),
								Math.max(((LineSegment2D)thisShape).pt1.y, ((LineSegment2D)thisShape).pt2.y));
			}else{
			bounds.corner1.x = Math.min(((LineSegment2D)thisShape).pt1.x, ((LineSegment2D)thisShape).pt2.x);
			bounds.corner1.y = Math.min(((LineSegment2D)thisShape).pt1.y, ((LineSegment2D)thisShape).pt2.y);
			bounds.corner1.x = Math.max(((LineSegment2D)thisShape).pt1.x, ((LineSegment2D)thisShape).pt2.x);
			bounds.corner1.y = Math.max(((LineSegment2D)thisShape).pt1.y, ((LineSegment2D)thisShape).pt2.y);
			}
	}
	
	@Override
	public Rect2D getBounds(){
		return bounds;
	}
	
}