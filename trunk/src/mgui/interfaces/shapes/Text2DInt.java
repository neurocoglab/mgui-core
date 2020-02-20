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
import java.awt.Font;
import java.awt.Graphics2D;

import javax.vecmath.Point2f;

import mgui.geometry.Rect2D;
import mgui.geometry.Shape2D;
import mgui.geometry.Text2D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.numbers.MguiInteger;

public class Text2DInt extends Shape2DInt {

	public String FontName;
	public int FontStyle;
	public int AlignmentX;
	public int AlignmentY;
	
	public Text2DInt(){
		super();
		thisShape = new Text2D();
		init();
	}
	
	public Text2DInt(String text, Rect2D bound){
		super();
		thisShape = new Text2D(text, bound);
		init();
	}
	
	public Text2DInt(String text, Point2f pt1, Point2f pt2){
		super();
		thisShape = new Text2D(text, pt1, pt2);
		init();
	}
	
	public Text2DInt(Text2D thisText2D){
		super();
		setShape(thisText2D);
		init();
	}
	
	public Text2DInt(String text, float width, float height){
		super();
		setShape(new Text2D(text, new Rect2D(0, 0, width, height)));
		init();
	}

	@Override
	public void setShape(Shape2D newShape){
		if (newShape instanceof Text2D)
			thisShape = newShape;
	}
	
	private void init(){
		//set attributes
		//attributes = new AttributeList();
//		attributes.add(new Attribute("Font", new Font("Arial", Font.PLAIN, 10)));
//		attributes.add(new Attribute("FontColour", Color.black));	
		//attributes.add(new Attribute("FontName", "Arial"));
		//attributes.add(new Attribute("FontStyle", new arInteger(Font.PLAIN)));
		attributes.add(new Attribute("AlignHoriz", new MguiInteger(Text2D.ALIGN_LEFT)));
	}
	
	@Override
	public boolean isLabelShape(){
		return true;
	}
	
	public void setFont(Font font){
		attributes.setValue("2D.LabelFont", font);
	}
	
	public Font getFont(){
		return (Font)attributes.getValue("2D.LabelFont");
	}
	
	public void setText(String newText){
		((Text2D)thisShape).setText(newText);
	}
	
	public String getText(){
		return ((Text2D)thisShape).getText();
	}
	
	public void setBounds(Rect2D newBounds){
		((Text2D)thisShape).setBounds(newBounds);
	}
	
	@Override
	public Rect2D getBounds(){
		return ((Text2D)thisShape).getBounds();
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine d){
		d.drawText2D(g, (Text2D)thisShape);
		
	}	
	
}