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

package mgui.interfaces.shapes.util;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/*******************************************
 * Represents a 2D point object, rendering a vector at a specified coordinate
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Point2DShape {

	public ArrayList<Shape> shape;
	public ArrayList<Boolean> fill;
	public float scale;
	
	public Point2DShape(ArrayList<Shape> shape, ArrayList<Boolean> fill){
		this.shape = shape;
		this.fill = fill;
	}
	
	/*********************
	 * Draw this point, centered on {@code origin}
	 * 
	 * @param g
	 * @param origin
	 * @param scale
	 * @param rotate
	 */
	public void draw(Graphics2D g, Point origin, float scale, float rotate){
		
		//AffineTransform scale_transform = new AffineTransform();
		//scale_transform.s
//		Rectangle2D bounds = null;
//		for (int i = 0; i < shape.size(); i++){
//			Shape this_shape = shape.get(i);
//			if (bounds == null)
//				bounds = this_shape.getBounds2D();
//			else
//				bounds = bounds.createUnion(this_shape.getBounds2D());
//			}
//		
//		double center_x = bounds.getWidth() / 2.0;
//		double center_y = bounds.getHeight() / 2.0;
		
		AffineTransform transform_old = g.getTransform();
		AffineTransform transform = new AffineTransform();
		transform.translate(origin.x, origin.y);
		transform.rotate(rotate);
		
		g.transform(transform);
		
		for (int i = 0; i < shape.size(); i++){
			if (fill.get(i))
				g.fill(shape.get(i));
			else
				g.draw(shape.get(i));
			}
		
		g.setTransform(transform_old);
		
	}
	
	public void setScale(float scale){
		this.scale = scale;
	}
	
	public static Point2DShape getFilledCircle(double scale){
		
		Ellipse2D.Double circle = new Ellipse2D.Double(-scale/2f,-scale/2f,scale,scale);
		ArrayList<Shape> shapes = new ArrayList<Shape>();
		shapes.add(circle);
		ArrayList<Boolean> fill = new ArrayList<Boolean>();
		fill.add(true);
		Point2DShape shape = new Point2DShape(shapes, fill);
		shape.setScale((float)scale);
		return shape;
		
	}
	
	/******************************
	 * Returns a basic filled arrow shape, oriented to the right (angle == 0). Can thus simply 
	 * be rotated to suit a particular line segment, angle, etc..
	 *  
	 * @param scale
	 * @return
	 */
	public static Point2DShape getFilledArrow(double scale){
		
		GeneralPath arrow = new GeneralPath(Path2D.WIND_EVEN_ODD, 3);
		arrow.moveTo(-scale/2f, scale/2f);
		arrow.lineTo(scale/2f, 0f);
		arrow.lineTo(-scale/2d, -scale/2f);
		arrow.lineTo(-scale/2f, scale/2f);
		ArrayList<Shape> shapes = new ArrayList<Shape>();
		shapes.add(arrow);
		ArrayList<Boolean> fill = new ArrayList<Boolean>();
		fill.add(true);
		Point2DShape shape = new Point2DShape(shapes, fill);
		shape.setScale((float)scale);
		return shape;
		
	}
	
}