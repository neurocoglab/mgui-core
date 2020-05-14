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

package mgui.morph.sections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import mgui.geometry.Polygon2D;
import mgui.geometry.Radius2D;
import mgui.geometry.util.GeometryFunctions;


public class ConstrainedRadialRepresentation extends RadialRepresentation {

	public ArrayList<ControlNode> controlNodes = new ArrayList<ControlNode>();
	public ArrayList<Radius2D> controlRadialNodes;
	
	private enum Validity{
		EXISTS,
		VALID,
		INVALID;
		//private Validity valid;
		//public Validity get(){return valid;}
		//public void set(Validity validity){
		//	valid = validity;}
	}
	
	Comparator c = new Comparator(){
		public int compare(Object o1, Object o2){
			if (((ControlNode)o1).node > ((ControlNode)o2).node) return 1;
			if (((ControlNode)o1).node == ((ControlNode)o2).node) return 0;
			return -1;
		} };
	
	//empty constructor
	public ConstrainedRadialRepresentation(){
		super();
	}
	
	//constructor with polygon object
	public ConstrainedRadialRepresentation(Polygon2D thisPoly){
		super(thisPoly);
	}
	
	/***
	 * Sets a control node, such that the node is assigned the given angle,
	 * and any nodes between this point and the next control point clockwise
	 * are adjusted so that their radii fit between them, but maintain their
	 * proportionality. If only one control point is specified, all radii are simply
	 * rotated to fit control node's radius. 
	 * 
	 * This function is useful in cases where it is desirable to force a mapping
	 * between particular nodes (e.g., known corner points), which will be certain 
	 * to map since their angles are identical.
	 *  
	 * @param node Node to assign as a control node
	 * @param angle Angle to assign to this node
	 */
	
	public void addControlNode(int node, double angle){
		//create new control node
		//test its validity (given other control nodes)
		//add if valid
		ControlNode thisNode = new ControlNode(node, angle);
		
		Validity valid = isValidControlNode(thisNode);
		
		if (valid == Validity.VALID)
			addControlNode(thisNode, false);
		if (valid == Validity.EXISTS)
			addControlNode(thisNode, true);
	}
	
	private void addControlNode(ControlNode newNode, boolean overwrite){
		//assumes validity
		if (controlNodes.size() == 0){
			controlNodes.add(newNode);
			return;
			}
		int index = Collections.binarySearch(controlNodes, newNode, c);
		if (index >= 0)
			controlNodes.add(index, newNode);
	}
	
	private Validity isValidControlNode(ControlNode thisNode){
		//is node in list?
		//if so return EXISTS 
		int index = Collections.binarySearch(controlNodes, thisNode, c);
		
		if (index >= 0){
			if (controlNodes.get(index).node == thisNode.node) return Validity.EXISTS;
			if (controlNodes.size() < 2) return Validity.VALID;
			if (index == controlNodes.size()) index = 0;
			int j = index - 1;
			if (j < 0) j = controlNodes.size() - 1;
			int k = index + 1;
			if (k == controlNodes.size()) k = 0;
			//node must fall between surrounding nodes
			if (GeometryFunctions.isIntermediateAngleCW(thisNode.angle,
													    controlNodes.get(j).angle,
													    controlNodes.get(k).angle))
				return Validity.VALID;
		}
		return Validity.INVALID;
	}
	
	public void updateRepresentation(){
		//if control nodes exist:
		controlRadialNodes = getRadialNodesCopy();
		if (controlNodes.size() == 0)
			return;
		
		if (controlNodes.size() > 1){
			//for each control node:
			//	find the next CW control node, if one exists
			ControlNode thisNode, nextNode;
			for (int i = 0; i < controlNodes.size(); i++){
				//	set arcRatio1 = new arc (next CW control pt angle - new control pt angle)
				//  	            /original arc (next CW control pt angle - original node angle)
				thisNode = controlNodes.get(i);
				int j = i + 1;
				if (j == controlNodes.size()) j = 0;
				nextNode = controlNodes.get(j);
				double newArc = GeometryFunctions.getAngleDiffCW(nextNode.angle, thisNode.angle);
				double oldArc = GeometryFunctions.getAngleDiffCW(
						RadialNodes.get(nextNode.node).angle,
						RadialNodes.get(thisNode.node).angle);
				double arcRatio = newArc / oldArc;
				//set these control points
				controlRadialNodes.get(thisNode.node).angle = (float)thisNode.angle;
				controlRadialNodes.get(nextNode.node).angle = (float)nextNode.angle;
				
				//	set intervening nodes CW to controlNode_new_angle + 
				//								((thisNode_orig_angle - 
				//								  controlNode_orig_angle) *
				//								  arcRatio1)
				
				int k = nextNode.node;
				double thisArc;
				int m;
				if (k < thisNode.node) k += controlNodes.size();
				if (k - thisNode.node != 1){
					for (int l = thisNode.node + 1; l < k; l++){
						m = l;
						if (m >= controlNodes.size()) m -= controlNodes.size();
							thisArc = RadialNodes.get(m).angle - 
									  RadialNodes.get(thisNode.node).angle;
							controlRadialNodes.get(m).angle = (float)(thisNode.angle + thisArc * arcRatio);
						}
					}
				}
			}else{
			//if only one control node exists:
			//	rotate all radii by (controlNode_new_angle - controlNode_orig_angle)
				double angleDiff = controlNodes.get(0).angle - 
								   RadialNodes.get(controlNodes.get(0).node).angle;
				for (int i = 0; i < RadialNodes.size(); i++)
					controlRadialNodes.get(i).angle += angleDiff;
			}
	}
	
	public ArrayList<Radius2D> getRadialNodesCopy(){
		ArrayList<Radius2D> retObj = new ArrayList<Radius2D>(RadialNodes.size());
		for (int i = 0; i < RadialNodes.size(); i++)
			retObj.add((Radius2D)RadialNodes.get(i).clone());
		return retObj;
	}
	
	class ControlNode {

		public int node;
		public double angle;
		
		public ControlNode(){
			
		}
		
		public ControlNode(int thisNode, double thisAngle){
			node = thisNode;
			angle = thisAngle;
		}
		
	}
	
}