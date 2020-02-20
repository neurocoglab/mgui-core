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

import javax.vecmath.Point2f;

import mgui.geometry.Polygon2D;
import mgui.geometry.Radius2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.numbers.MguiDouble;

/**
 * @author Andrew Reid
 * @version 1.0
 * @date 10.16.2006
 * @description 
 * A radial representation of a closed 2D polygon which forces that polygon to
 * a circle. The polygon is oriented by selecting the node which has the
 * greatest value of x (i.e., the rightmost node), which must be convex. This node,
 * P1, will be assigned its actual angle in the radial representation. All other 
 * nodes, Pi, will be assigned angles according to:<p>
 * 
 * 		alpha_i = l_i / L * 2 PI, where<p>
 * 
 * <ul>
 * <li>alpha_i 	is the angle of Pi in the radial representation
 * <li>l_i 		is the perimetric length from P1 to Pi
 * <li>L 		is the perimeter of the polygon
 * </ul>
 * 
 * Note that the radial representation will have a shifted array index, starting
 * with point P1. Use the function getNodeIndex(i) to obtain the polygon index
 * for the radial representation index i.
 * 
 */

public class RadialRepresentation {
	
	/****
	 * @variable L - the perimeter of this polygon
	 * @variable P1 - index of the first node (angle = 0) in this representation 
	 * @variable RadialPts - array of Radius2D objects representing nodes
	 */
	private double L;
	public int P1;
	public ArrayList<Radius2D> RadialNodes;
	public Polygon2D source;
	
	//empty constructor
	public RadialRepresentation(){
	}
	
	//constructor with polygon object
	public RadialRepresentation(Polygon2D thisPoly){
		source = thisPoly;
		SetRepresentation(thisPoly);
	}
	
	/**
	 * Set the radial representation for a given polygon 
	 * @param Polygon2D thisPoly - a polygon object with which to set this representation
	 * 
	 */
	public void SetRepresentation(Polygon2D thisPoly){
		double sign = 1;
		if(!GeometryFunctions.isClockwise(thisPoly))
			sign = -1;
		
		int nodeCount = thisPoly.vertices.size();
		RadialNodes = new ArrayList<Radius2D>(nodeCount);
		
		//find center of gravity, assign to C
		//Point2f C = Polygon2D.getCenterOfGravity(thisPoly);
		Point2f C = GeometryFunctions.getCenterOfGravity(thisPoly);
		P1 = 0;
		double thisMax = Double.MIN_VALUE;
		
		//find rightmost node (P1)
		for (int i = 0; i < nodeCount; i++){
			if (thisMax < thisPoly.vertices.get(i).x){
				P1 = i;
				thisMax = thisPoly.vertices.get(i).x;
			}
		}
		
		for (int i = 0; i < nodeCount; i++)
			RadialNodes.add(new Radius2D());
		
		double startAngle = GeometryFunctions.getAngle(C, thisPoly.vertices.get(P1));
		if (Double.isNaN(startAngle)){
			InterfaceSession.log("\nBad start angle");
			printRep();
			}
		startAngle = GeometryFunctions.getMinimalAngle(startAngle);
		
		//InterfaceSession.log("RADIAL REP:");
		//InterfaceSession.log("Start Angle (N" + P1 + "->0):" 
		//		   + arDouble.getString(startAngle, "#0.000") + "rad, "
		//		   + arDouble.getString((startAngle * 360 / (2 * Math.PI) ), "#0.000") + "deg");
		
		//RadialNodes.set(0, new Radius2D((float)startAngle, Point2D.getDistance(C, thisPoly.nodes.get(P1))));
		double debugDbl = C.distance(thisPoly.vertices.get(P1));
		if (Double.isNaN(debugDbl)){
			InterfaceSession.log("\nBad distance");
			printRep();
		}
			
		RadialNodes.set(0, new Radius2D((float)startAngle, C.distance(thisPoly.vertices.get(P1))));
		
		//calculate circumferential lengths (l_i), store in array
		//	(for each node in thisPoly, calculate distance from P1)
		//calculate total circumferential length (L)		
		ArrayList<MguiDouble> lengths = new ArrayList<MguiDouble>(nodeCount);
		//fill array
		for (int i = 0; i < nodeCount; i++)
			lengths.add(new MguiDouble());
		Point2f lastNode = thisPoly.vertices.get(P1);
		Point2f thisNode;
		//arDouble L = new arDouble(0);
		double L = 0;
		for (int i = 1; i < nodeCount; i++){
			thisNode = thisPoly.vertices.get(getNodeIndex(i));
			//L += Point2D.getDistance(lastNode, thisNode);
			L += lastNode.distance(thisNode);
			lengths.set(i, new MguiDouble(L));
			//lengths.add(i, L);
			lastNode = thisNode;
		}
		
		//L += Point2D.getDistance(lastNode, thisPoly.nodes.get(P1));
		L += lastNode.distance(thisPoly.vertices.get(P1));
		
		//for each subsequent node Pi
		double thisLen, alpha_i;
		double scaleFactor =  (2 * Math.PI) / L;
		if (L == 0)
			InterfaceSession.log("\nRadRep: L = 0 !?");
		
		for (int i = 1; i < nodeCount; i++){
			//assign angle alpha_i = l_i/L * 2Pi
			thisLen = lengths.get(i).getValue();
			alpha_i = thisLen * scaleFactor;
			//InterfaceSession.log("Angle N" + (P1 + i) + " (1): " 
			//				   + arDouble.getString((alpha_i), "#0.000") + "rad, "
			//				   + arDouble.getString((alpha_i * 360 / (2 * Math.PI) ), "#0.000") + "deg");
			
			//assign length = dist(C, Pi)
			thisNode = thisPoly.vertices.get(getNodeIndex(i));			
			//thisLen = Point2D.getDistance(C, thisNode);
			thisLen = C.distance(thisNode);
			alpha_i = GeometryFunctions.getMinimalAngle(startAngle + (sign * alpha_i));
			//InterfaceSession.log("Angle (N" + getNodeIndex(i) + "->" + i + "): " 
			//		   + arDouble.getString((alpha_i), "#0.000") + "rad, "
			//		   + arDouble.getString((alpha_i * 360 / (2 * Math.PI) ), "#0.000") + "deg");
			if (alpha_i == Double.NaN){
				InterfaceSession.log("\nBad alpha i");
				printRep();
			}
			
			RadialNodes.set(i, new Radius2D((float)alpha_i, (float)thisLen));
		}
		return;
	}
	
	public void printRep(){
		for (int i = 0; i < RadialNodes.size(); i++)
			System.out.print(RadialNodes.get(i).toString());
	}
	
	/***
	 * Gets the polygon index corresponding to the radius index i
	 * @param i the radial representation index of the node
	 * @return int value for polygon index of node represented by radius i
	 */
	public int getNodeIndex(int i){
		int i_new = P1 + i;
		if (i_new >= RadialNodes.size())
			i_new -= RadialNodes.size();
		return i_new;
	}
	
	/***
	 * 
	 * @return double - the maximum length of all radii 
	 */
	public double getMaxLength(){
		int nodeCount = RadialNodes.size();
		double maxLen = Double.MIN_VALUE;
		for (int i = 1; i < nodeCount; i++)
			maxLen  = Math.max(RadialNodes.get(i).length, maxLen);
		return maxLen;
	}
	
}