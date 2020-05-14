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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import mgui.geometry.Circle2D;
import mgui.geometry.PointSet3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Rect2D;
import mgui.geometry.Shape;
import mgui.geometry.Shape2D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.Text2DInt;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;

public class RadialRep2DInt extends Shape2DInt {

	public static final int DRAW_TRUE_EXT = 0;
	public static final int DRAW_TRUE_RADII = 1;
	public static final int DRAW_CIRCLE_EXT = 2;
	public static final int DRAW_CIRCLE_RADII = 3;
	public static final int DRAW_CIRCLE_ONLY = 4;
	public static final int DRAW_RADII_ONLY = 5;
	
	public RadialRep2DInt(){
		thisShape = new RadialRep2D();
		init();
	}
	
	public RadialRep2DInt(RadialRep2D thisRR){
		thisShape = thisRR;
		init();
	}
	
	public RadialRep2DInt(Polygon2D thisPoly){
		thisShape = new RadialRep2D(thisPoly);
		init();
	}
	
	public RadialRep2DInt(RadialRepresentation thisRR){
		thisShape = new RadialRep2D(thisRR);
		init();
	}
	
	@Override
	public Shape getGeometryInstance(){
		return new RadialRep2D();
	}
	
	private void init(){
		//set attributes
		attributes.add(new Attribute<MguiBoolean>("HasFill", new MguiBoolean(false)));
		attributes.add(new Attribute<Color>("FillColour", Color.WHITE));
		attributes.add(new Attribute<MguiBoolean>("ShowNodes", new MguiBoolean(true)));
		attributes.add(new Attribute<Color>("NodeColour", Color.BLUE));
		attributes.add(new Attribute<Color>("RadiiTrueLineColour", Color.BLACK));
		attributes.add(new Attribute<Color>("RadiiExtLineColour", Color.GRAY));
		attributes.add(new Attribute<BasicStroke>("RadiiTrueLineStyle", new BasicStroke(2.0f)));
		attributes.add(new Attribute<BasicStroke>("RadiiExtLineStyle", new BasicStroke(2.0f)));
		attributes.add(new Attribute<MguiInteger>("DrawStyle", new MguiInteger(DRAW_CIRCLE_EXT)));
		attributes.add(new Attribute<MguiBoolean>("LabelNodes", new MguiBoolean(true)));
		attributes.add(new Attribute<Text2DInt>("LabelObj", new Text2DInt("N", 10, 5)));
		attributes.add(new Attribute<ArrayList<String>>("LabelStrings", new ArrayList<String>()));
		attributes.add(new Attribute<MguiDouble>("LabelOffsetX", new MguiDouble(0)));
		attributes.add(new Attribute<MguiDouble>("LabelOffsetY", new MguiDouble(0)));
		
		//bounds
		updateShape();
	}
	
	@Override
	public void drawShape2D(Graphics2D g, DrawingEngine d){
		if (!((MguiBoolean)attributes.getValue("IsVisible")).getTrue()) return;
		MorphDrawEngine d2 = new MorphDrawEngine(d.getMap());
		d2.drawing_attributes.setAttribute(attributes.getAttribute("FillColour"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("LineColour"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("LineStyle"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("HasFill"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("ShowNodes"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("NodeColour"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("RadiiTrueLineColour"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("RadiiExtLineColour"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("RadiiTrueLineStyle"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("RadiiExtLineStyle"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("LabelNodes"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("LabelObj"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("LabelStrings"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("LabelOffsetX"));
		d2.drawing_attributes.setAttribute(attributes.getAttribute("LabelOffsetY"));
		
		d2.setCoordSys(((MguiInteger)attributes.getValue("CoordSys")).getInt());
		d2.DrawRadialRep2D(g, (RadialRep2D)thisShape);
	}
	
	@Override
	public void updateShape(){
		if (bounds == null){
			bounds = new Rect2D(((RadialRep2D)thisShape).circle.centerPt.x - ((RadialRep2D)thisShape).circle.radius,
								((RadialRep2D)thisShape).circle.centerPt.y - ((RadialRep2D)thisShape).circle.radius,
								((RadialRep2D)thisShape).circle.centerPt.x + ((RadialRep2D)thisShape).circle.radius,
								((RadialRep2D)thisShape).circle.centerPt.y + ((RadialRep2D)thisShape).circle.radius);
			}else{
			bounds.corner1.x = ((RadialRep2D)thisShape).circle.centerPt.x - ((RadialRep2D)thisShape).circle.radius;
			bounds.corner1.y = ((RadialRep2D)thisShape).circle.centerPt.y - ((RadialRep2D)thisShape).circle.radius;
			bounds.corner2.x = ((RadialRep2D)thisShape).circle.centerPt.x + ((RadialRep2D)thisShape).circle.radius;
			bounds.corner2.y = ((RadialRep2D)thisShape).circle.centerPt.y + ((RadialRep2D)thisShape).circle.radius;
			}
		
		centerPt = ((RadialRep2D)thisShape).circle.centerPt;
		attributes.setValue("LabelStrings", getPolygonNodeLabels());
		this.fireShapeModified();
	}
	
	public void setCircle(Circle2D thisCircle){
		((RadialRep2D)thisShape).setCircle(thisCircle);
	}
	
	public int getPolygonNode(int i){
		return ((RadialRep2D)thisShape).getPolygonNode(i);
	}

	public ArrayList<String> getPolygonNodeLabels(){
		int listSize = ((RadialRep2D)thisShape).radii.size();
		ArrayList<String> retList = new ArrayList<String>(listSize);
		for (int i = 0; i < listSize; i++){
			retList.add(String.valueOf(getPolygonNode(i)));
		}
		return retList;
	}
	
	@Override
	public void shapeUpdated(ShapeEvent e){
		if (!(e.getSource() instanceof Polygon2DInt))
			return;
		setRepresentation(((Polygon2DInt)e.getSource()).getPolygon());
		//this.fireShapeListeners();
		//parent.updateDisplays();
	}
	
	public void setRepresentation(Polygon2D thisPoly){
		RadialRepresentation thisRep = new RadialRepresentation();
		thisRep.SetRepresentation(thisPoly);
		((RadialRep2D)thisShape).setFromRadialRepresentation(thisRep);
		//attributes.setValue("LabelStrings", getPolygonNodeLabels());
		updateShape();
	}
	
	public Polygon2D getSource(){
		return ((RadialRep2D)thisShape).source;
	}
	
	@Override
	public Shape2D getShape(){
		return thisShape;
	}
	
	@Override
	public String toString(){
		return "RadialRep2D [" + idStr + "]";
	}
	
}