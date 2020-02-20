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
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.vecmath.Point2f;

import mgui.geometry.Polygon2D;
import mgui.geometry.Rect2D;
import mgui.geometry.Shape2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.xml.XMLFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;

import org.xml.sax.Attributes;

/***************************************************
 * Interface for a 2D polygon.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Polygon2DInt extends Shape2DInt {

	public Polygon2DInt(){
		super();
		thisShape = new Polygon2D();
		init();
	}
	
	public Polygon2DInt(Polygon2D thisPoly){
		super();
		setShape(thisPoly);
		init();
	}
	
	private void init(){
		//set attributes
		attributes.add(new Attribute<MguiBoolean>("IsClosed", new MguiBoolean(true)));
		attributes.add(new Attribute<Text2DInt>("2D.LabelObj", new Text2DInt("N", 10, 7)));
		attributes.add(new Attribute<ArrayList<String>>("LabelStrings", new ArrayList<String>()));
	
		updateShape();
	}
	
	@Override
	public void setShape(Shape2D newShape){
		if (newShape instanceof Polygon2D){
			thisShape = newShape;
			}
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/polygon_closed_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/polygon_closed_20.png");
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine d){
		
		d.drawPolygon2D(g, (Polygon2D)thisShape);
	}
	
	public void isClosed(boolean b){
		attributes.setValue("IsClosed", new MguiBoolean(b));
	}
	
	public boolean isClosed(){
		return ((MguiBoolean)attributes.getValue("IsClosed")).getTrue();
	}
	
	@Override
	public Rect2D getExtBounds(){
		Rect2D thisBounds = (Rect2D)bounds.clone();
		//if (((arBoolean)attributes.getValue("ShowNodes")).value){
			thisBounds = GeometryFunctions.getScaledShape(thisBounds.getCenterPt(), 
														  thisBounds, 
														  1.25);
		return thisBounds;
	}
	
	public Polygon2D getPolygon(){
		return (Polygon2D)thisShape;
	}
	
	@Override
	public String toString(){
		return "Polygon2D [" + String.valueOf(ID) + "]"; 
	}
	
	@Override
	public Object clone(){
		Polygon2DInt retObj = new Polygon2DInt(getPolygon());
		retObj.attributes = (AttributeList)attributes.clone();
		retObj.updateShape();
		return retObj;
	}
	
	private boolean is_nodes = false;
	
	@Override
	public void handleXMLElementEnd(String localName) {
		if (localName.equals("Vertices")){
			is_nodes = false;
			}
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		if (localName.equals("Vertices")){
			getPolygon().vertices = new ArrayList<Point2f>(Integer.valueOf(attributes.getValue("n")));
			is_nodes = true;
			}
	}

	@Override
	public void handleXMLString(String s) {
		if (!is_nodes) return;
		ArrayList<Point2f> pts = XMLFunctions.getPoint2fList(s);
		for (Point2f p : pts)
			getPolygon().addVertex(p);
	}
	
}