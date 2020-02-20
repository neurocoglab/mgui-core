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

import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import mgui.geometry.Plane3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Rect2D;
import mgui.geometry.Rect3D;
import mgui.geometry.Shape2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.numbers.MguiBoolean;

/*****************************************************************
 * Represents a 2D rectangle.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Rect2DInt extends Shape2DInt {
	
	public Rect2DInt(){
		super();
		thisShape = new Polygon2D();
		init();
	}
	
	public Rect2DInt(Rect2D thisRect){
		super();
		setShape(thisRect);
		init();
	}
	
	private void init(){
		//set attributes
//		attributes.add(new Attribute("HasFill", new MguiBoolean(false)));
//		attributes.add(new Attribute("FillColour", Color.WHITE));
//		attributes.add(new Attribute("ShowNodes", new MguiBoolean(true)));
//		attributes.add(new Attribute("NodeColour", Color.BLUE));
//		attributes.add(new Attribute("LabelNodes", new MguiBoolean(false)));
//		attributes.add(new Attribute("LabelObj", new Text2DInt("N", 10, 7)));
//		attributes.add(new Attribute("LabelStrings", new ArrayList<String>()));
//		attributes.add(new Attribute("LabelOffsetX", new MguiDouble(5)));
//		attributes.add(new Attribute("LabelOffsetY", new MguiDouble(0)));
//		//temp
//		attributes.setValue("LineColour", Color.CYAN);
		
		//bounds
		updateShape();
	}
	
	@Override
	public void setShape(Shape2D newShape){
		if (newShape instanceof Rect2D)
			thisShape = newShape;
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine d){
		d.drawRect2D(g, (Rect2D)thisShape);
	}
	
	@Override
	protected Shape3DInt getShape3D(Plane3D plane){
		ArrayList<Point2f> vertices_2d = thisShape.getVertices();
		ArrayList<Point3f> vertices_3d = GeometryFunctions.getPointsFromPlane(vertices_2d, plane);
		Rect3D rect = new Rect3D();
		rect.setVertices(vertices_3d);
		return new Rect3DInt(rect);
	}
	
	@Override
	public Rect2D getExtBounds(){
		Rect2D thisBounds = (Rect2D)bounds.clone();
		//if (((arBoolean)attributes.getValue("ShowNodes")).value){
			thisBounds = GeometryFunctions.getScaledShape(thisBounds.getCenterPt(), 
														  thisBounds, 
														  1.25);
		//}
		//adjust to include nodes
		if (((MguiBoolean)attributes.getValue("LabelNodes")).getTrue()){
			//Text2DInt thisText = ((Text2DInt)attributes.getValue("LabelObj"));
			Rect2D labelBox = ((Text2DInt)attributes.getValue("LabelObj")).getBounds();
			double labelSize = labelBox.getDiagonalLength() * 3;
			labelSize = labelSize / thisBounds.getDiagonalLength();
			thisBounds = GeometryFunctions.getScaledShape(thisBounds.getCenterPt(), 
														  thisBounds, 
														  1 + labelSize);
			}
		return thisBounds;
	}
	
	@Override
	public String toString(){
		return "Rect2D [" + String.valueOf(ID) + "]"; 
	}
	
	@Override
	public Object clone(){
		Rect2DInt retObj = new Rect2DInt((Rect2D)((Rect2D)thisShape).clone());
		retObj.attributes = (AttributeList)attributes.clone();
		retObj.updateShape();
		return retObj;
	}
}