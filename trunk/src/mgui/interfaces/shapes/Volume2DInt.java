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
import java.awt.image.BufferedImage;

import javax.media.j3d.BranchGroup;
import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

import mgui.geometry.Plane3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Polygon3D;
import mgui.geometry.Rect2D;
import mgui.geometry.Rect3D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.shapes.attributes.ShapeAttribute;
import mgui.numbers.MguiBoolean;


/**********************************
 * 2D sectional representation of a Volume3DInt. Extends Image2DInt to draw the 
 * polygon which intersects the plane on which this image is drawn.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Volume2DInt extends Image2DInt {

	public Polygon2D outline;
	public Rect3DInt planeRect;
	public Polygon3DInt planePoly;
	public BranchGroup scanNodes;
	
	public Volume2DInt(){
		super();
		init();
	}
	
	public Volume2DInt(Rect2D rect, BufferedImage image, boolean hasAlpha){
		super(rect, image, hasAlpha);
		init();
	}
	
	private void init(){
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.ShowPolygon", new MguiBoolean(true)));
		attributes.add(new ShapeAttribute<Color>("2D.PolygonColour", Color.GREEN));
		//attributes.add(new ShapeAttribute<Color>("BackColour", Color.BLACK));
		
		isImageShape = true;
	}
	
	//TODO make this a draw routine; i.e., don't override drawShape2D directly
	@Override
	public void drawShape2D(Graphics2D g, DrawingEngine d){
		if (!((MguiBoolean)attributes.getValue("IsVisible")).getTrue()) return;
						
		//draw image
		super.drawShape2D(g, d);
		
		
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine d){
		d.drawImage2D(g, (Rect2D)thisShape, image);
		
		//draw polygon outline if required
		if (outline != null && ((MguiBoolean)attributes.getValue("2D.ShowPolygon")).getTrue()){
			d.setAttribute("2D.LineColour", attributes.getValue("2D.PolygonColour"));
			d.drawPolygon2D(g, outline);
			}
	}
	
	@Override
	public int getClosestVertex(Point2f point){
		
		//if not contained, return closest from bounds
		if (!thisShape.contains(point)) return super.getClosestVertex(point);
		
		//otherwise find enclosing pixel
		Rect2D bounds = this.getBounds();
		Point2f corner = bounds.getCorner(Rect2D.CNR_TL);
		Vector2f v = new Vector2f(point);
		v.sub(corner);
		int x_pixel = Math.round((v.x / bounds.getWidth()) * image.getWidth()); 
		int y_pixel = Math.round((v.y / bounds.getHeight()) * image.getHeight()); 
		
		return y_pixel * image.getWidth() + x_pixel;
	}
	
	
	public void setOutline(Polygon2D poly){
		outline = poly;
	}
	
	public void setPlaneRect(Rect3D rect){
		planeRect = new Rect3DInt(rect);
		planeRect.attributes.setValue("LineColour", attributes.getValue("2D.LineColour"));
	}
	
	public void setPlanePoly(Polygon3D poly){
		planePoly = new Polygon3DInt(poly);
		planePoly.attributes.setValue("LineColour", attributes.getValue("2D.LineColour"));
	}
	
	public void setScanNodes(BranchGroup g){
		scanNodes = g;
	}
	
	@Override
	protected Shape3DInt getShape3D(Plane3D plane){
		return super.getShape3D(plane);
	}
	
}