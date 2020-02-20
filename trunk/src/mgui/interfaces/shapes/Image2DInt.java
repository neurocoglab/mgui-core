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
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import mgui.geometry.Plane3D;
import mgui.geometry.Rect2D;
import mgui.geometry.Rect3D;
import mgui.geometry.Shape;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.morph.sections.RadialRep2D;
import mgui.numbers.MguiBoolean;


/*************************************
 * Displays an image inside a Rect2D shape.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */


public class Image2DInt extends Shape2DInt {

	public BufferedImage image;
	
	public Image2DInt(){
		super();
		init();
	}
	
	public Image2DInt(Rect2D rect, BufferedImage i, boolean hasAlpha){
		super();
		init();
		thisShape = rect;
		setImage(i);
		hasAlpha(hasAlpha);
		updateShape();
	}
	
	@Override
	public Shape getGeometryInstance(){
		return null;
	}
	
	public BufferedImage getImage(){
		return image;
	}
	
	@Override
	public boolean isImageShape(){
		return true;
	}
	
	public void setImage(BufferedImage i){
		image = i;
	}
	
	private void init(){
		//attributes.add(new Attribute("HasBorder", new MguiBoolean(false)));
		//Attribute alphaAtt = new Attribute("2D.HasAlpha", new MguiBoolean(false));
		//alphaAtt.setEditable(false);
		//attributes.add(alphaAtt);
		//attributes.setValue("Show3D", new MguiBoolean(false));
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine d){
		d.drawImage2D(g, (Rect2D)thisShape, image);
	}
	
	@Override
	public String toString(){
		return "Image2D [" + String.valueOf(ID) + "]"; 
	}
	
	@Override
	protected Shape3DInt getShape3D(Plane3D plane){
		ArrayList<Point2f> nodes = thisShape.getVertices();
		ArrayList<Point3f> newNodes = new ArrayList<Point3f>(4);
		
		for (int i = 0; i < 4; i++)
			newNodes.add(GeometryFunctions.getPointFromPlane(nodes.get(i), plane));
		
		Image3DInt image3D = new Image3DInt(new Rect3D(newNodes), image, hasAlpha());
		
		//set attributes from 2D image
		image3D.getAttributes().setIntersection(attributes, false);
		return image3D;
	}
	
	@Override
	public Object clone(){
		Image2DInt retObj = new Image2DInt((Rect2D)thisShape, image, hasAlpha());
		retObj.attributes = (AttributeList)attributes.clone();
		retObj.updateShape();
		return retObj;
	}
	
}