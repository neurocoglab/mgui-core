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

package mgui.geometry.util;

import java.awt.Shape;
import java.util.ArrayList;

import mgui.interfaces.NamedObject;

/**********************************************
 * Wraps a {@link java.awt.geom.Shape} used to render graph or 2D shape nodes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NodeShape implements NamedObject {

	protected ArrayList<Shape> shapes;
	protected String name;
	protected boolean is_fillable = true;
	
	public NodeShape(){
		
	}
	
	public NodeShape(String name, Shape shape, boolean is_fillable){
		this.name = name;
		this.shapes = new ArrayList<Shape>(1);
		this.shapes.add(shape);
		this.is_fillable = is_fillable;
	}
	
	public NodeShape(String name, ArrayList<Shape> shapes, boolean is_fillable){
		this.name = name;
		this.shapes = new ArrayList<Shape>(shapes);
		this.is_fillable = is_fillable;
	}
	
	/***************
	 * Gets the shape for this node shape, or the first if there are multiple shapes.
	 * 
	 * @return
	 */
	public Shape getShape() {
		if (shapes == null || shapes.size() == 0) return null;
		return shapes.get(0);
	}

	/****************
	 * Sets this node shape to a single shape
	 * 
	 * @param shape
	 */
	public void setShape(Shape shape) {
		this.shapes = new ArrayList<Shape>(1);
		this.shapes.add(shape);
	}
	
	public void addShape(Shape shape){
		if (shapes == null) 
			setShape(shape);
		else
			shapes.add(shape);
	}
	
	/*****************
	 * Returns a list of all shapes to be rendered
	 * 
	 * @return
	 */
	public ArrayList<Shape> getShapes(){
		return shapes;
	}
	
	/*****************
	 * Sets the list of shapes
	 * 
	 * @param shapes
	 */
	public void setShapes(ArrayList<Shape> shapes){
		this.shapes = new ArrayList<Shape>(shapes);
	}

	/*****************
	 * Determines whether this shape should be filled
	 * 
	 * @return
	 */
	public boolean isFillable(){
		return is_fillable;
	}
	
	/******************
	 * Sets whether this shape should be filled
	 * 
	 * @param fillable
	 */
	public void setFillable(boolean fillable){
		this.is_fillable = fillable;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString(){
		return name;
	}
	
}