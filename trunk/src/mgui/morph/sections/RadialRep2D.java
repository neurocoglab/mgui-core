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

package mgui.morph.sections;


import java.util.ArrayList;

import org.jogamp.vecmath.Point2f;

import mgui.geometry.Circle2D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Radius2D;
import mgui.geometry.Shape2D;

public class RadialRep2D extends Shape2D {

	//needs a circle and set of radii
	public Polygon2D source;
	public Circle2D circle;
	public ArrayList<Radius2D> radii;
	public float maxRadius = 0;
	private int P1;
	
	public RadialRep2D (){
		circle = new Circle2D();
		radii = new ArrayList<Radius2D>();
	}
	
	public RadialRep2D (Polygon2D sourcePoly){
		circle = new Circle2D();
		radii = new ArrayList<Radius2D>();
		setFromRadialRepresentation(new RadialRepresentation(sourcePoly));
	}
	
	public RadialRep2D (RadialRepresentation thisRep){
		circle = new Circle2D();
		radii = new ArrayList<Radius2D>();
		setFromRadialRepresentation(thisRep);
	}
	
	public void setCircle(Circle2D thisCircle){
		circle = thisCircle;
	}
	
	@Override
	public Point2f getVertex(int i) {
		return circle.getVertex(i);
	}

	@Override
	public ArrayList<Point2f> getVertices() {
		return circle.getVertices();
	}
	
	@Override
	public void setVertices(ArrayList<Point2f> n) {
		// TODO Auto-generated method stub
		
	}
	
	public void addRadius(Radius2D thisRadius){
		radii.add(thisRadius);
		maxRadius = Math.max(maxRadius, thisRadius.length);
	}
	
	public void setCircleRadius(float thisLen){
		circle.radius = thisLen;
	}
	
	public void setFromRadialRepresentation(RadialRepresentation thisRep){
		source = thisRep.source;
		radii = new ArrayList<Radius2D>();
		int nodeCount = thisRep.RadialNodes.size();
		for (int i = 0; i < nodeCount; i++)
			addRadius(thisRep.RadialNodes.get(i));
		P1 = thisRep.P1;
	}
	
	public int getPolygonNode(int i){
		int i_new = P1 + i;
		if (i_new >= radii.size())
			i_new -= radii.size();
		return i_new;
	}
	
	public int getRadialNode(int i){
		int i_new = i - P1;
		if (i_new < 0)
			i_new += radii.size();
		return i_new;
	}
	
	public Radius2D getRadiusAtNode(int i){
		return radii.get(getRadialNode(i));
	}
	
	public void printRep(){
		for (int i = 0; i < radii.size(); i++)
			System.out.print(radii.get(i).toString() + "  ");
	}
	
}