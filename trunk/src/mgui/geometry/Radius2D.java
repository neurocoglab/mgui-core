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
import javax.vecmath.Vector2d;

import mgui.geometry.util.GeometryFunctions;
import mgui.numbers.MguiFloat;

/**
 * DEV NOTE: deprecate or change; does not have fixed geometry, thus doesn't fit definition of a
 * <code>Shape</code>.
 *  
 *  
 * @author Andrew Reid
 * @version 1.0
 * @date 08.30.2006
 * @description
 * A 2D radius object, with variables for radial angle and length
 *
 */


public class Radius2D extends Shape2D {

	/***
	 * @variable angle the angle of this radius
	 * @variable length the length of this radius
	 */
	
	public float angle;
	public float length;
	
	public Radius2D(){
		
	}
	
	/**
	 * Set this radius with angle and length double values
	 * @param thisAngle
	 * @param thisLength
	 */
	public Radius2D(float thisAngle, float thisLength){
		angle = thisAngle;
		length = thisLength;
	}

	/**
	 * Set this radius with angle and length arDouble objects
	 * @param thisAngle
	 * @param thisLength
	 */
	public Radius2D(MguiFloat thisAngle, MguiFloat thisLength){
		angle = thisAngle.getFloat();
		length = thisLength.getFloat();
	}
	
	@Override
	public Point2f getVertex(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Point2f> getVertices() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setVertices(ArrayList<Point2f> vertices){
		
	}
	
	public Vector2d getVector(){
		return new Vector2d(Math.cos(angle) * length, Math.sin(angle) * length);
	}
	
	@Override
	public Object clone(){
		return new Radius2D(angle, length);
	}
	
	public Point2f getEndpoint(Point2f startPt){
		return new Point2f(startPt.x + (float)(Math.cos(angle) * length), startPt.y + (float)(Math.sin(angle) * length));
	}
	
	public double getAngleDiff(Radius2D r2){
		return GeometryFunctions.getAngleDiff(angle, r2.angle);
	}
	
	@Override
	public String toString(){
		//return "A: " + arDouble.getString(angle, "#0.00") + ", L:" + arDouble.getString(length, "#0.00");
		return "A: " + angle + ", L:" + length;
	}
}