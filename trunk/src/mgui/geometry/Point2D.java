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

package mgui.geometry;

import java.util.ArrayList;

import javax.vecmath.Point2f;

/******************************************
 * Represents a 2D point.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Point2D extends Shape2D {
	
	public Point2f point;
	
	public Point2D(float x, float y){
		point = new Point2f(x, y);
	}
	
	public Point2D(Point2f pt){
		point = pt;
	}
	
	@Override
	public ArrayList<Point2f> getVertices(){
		ArrayList<Point2f> nodes = new ArrayList<Point2f>(1);
		nodes.add(point);
		return nodes;
	}
	
	public void setVertices(ArrayList<Point2f> vertices){
		point = new Point2f(vertices.get(0));
	}
	
	@Override
	public Point2f getVertex(int i) {
		if (i != 0) return null;
		return new Point2f(point);
	}
	
	/**
	 * @param pt1 first point
	 * @param pt2 second point
	 * @return distance between pt1 and pt2
	 */
	public static float getDistance(Point2f pt1, Point2f pt2){
		return (float)Math.sqrt(Math.pow(pt1.x - pt2.x, 2) + 
						 Math.pow(pt1.y - pt2.y, 2));
		
	}

}