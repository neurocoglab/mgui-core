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


import java.awt.Color;
import java.awt.Graphics2D;

import mgui.geometry.Point2D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.numbers.MguiInteger;


public class Point2DInt extends Shape2DInt {

	public Point2DInt(Point2D pt){
		super();
		setShape(pt);
		init();
	}
	
	public Point2DInt(Point2D pt, Color colour, int size){
		super();
		setShape(pt);
		init();
		attributes.setValue("2D.FillColour", colour);
		attributes.setValue("2D.VertexSize", new MguiInteger(size));
	}
	
	private void init(){
//		attributes.add(new Attribute("Size", new MguiInteger(2)));
//		attributes.add(new Attribute("FillColour", Color.BLUE));
		updateShape();
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine d){
		d.drawPoint2D(g, getPoint(), getSize());
	}
	
	//public void setShape(Shape2D shape){
	//	thisShape = shape;
	//}
	
	public Point2D getPoint(){
		return (Point2D)thisShape;
	}
	
	public int getSize(){
		return ((MguiInteger)attributes.getValue("Size")).getInt();
	}
	
}