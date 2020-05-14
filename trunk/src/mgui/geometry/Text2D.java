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

package mgui.geometry;

import java.util.ArrayList;

import org.jogamp.vecmath.Point2f;

/*******************************************
 * Represents a character string with 2D geometry.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Text2D extends Shape2D {

	protected String textStr;
	protected Rect2D bounds;
	
	public static final int ALIGN_CENTER = 0;
	public static final int ALIGN_LEFT = 1;
	public static final int ALIGN_RIGHT = 2;
	
	public Text2D() {
		bounds = new Rect2D();
	}
	
	public Text2D(String text, Rect2D bound){
		textStr = text;
		bounds = bound;
	}
	
	public Text2D(String text, Point2f pt1, Point2f pt2){
		textStr = text;
		bounds = new Rect2D(pt1, pt2);
	}
	
	@Override
	public ArrayList<Point2f> getVertices(){
		return bounds.getVertices();
	}
	
	public void setVertices(ArrayList<Point2f> vertices){
		bounds.setVertices(vertices);
	}
	
	@Override
	public Point2f getVertex(int i) {
		return bounds.getVertex(i);
	}
	
	public String getText(){
		return textStr;
	}
	
	public void setText(String text){
		textStr = text;
	}
	
	@Override
	public Rect2D getBounds(){
		return bounds;
	}
	
	public void setBounds(Rect2D bounds){
		this.bounds = bounds;
	}
	
	@Override
	public Object clone(){
		return new Text2D(new String(textStr), (Rect2D)bounds.clone());
	}
	
}