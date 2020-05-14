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

package mgui.interfaces.shapes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import mgui.geometry.Shape;

public class Shape2DSelectionSet extends Shape2DInt {

	public ArrayList<Shape2DInt> shapes = new ArrayList<Shape2DInt>();
	public ArrayList<ShapeSet2DInt> parents = new ArrayList<ShapeSet2DInt>();
	//public ArrayList<SelIDRef> selectionIndex = new ArrayList<SelIDRef>();
	private ShapeIDComp selComp = new ShapeIDComp();
	//DefaultMutableTreeNode parentsNode;
	public String name;
	public Shape2DInt shapeAdded;
	
	public Shape2DSelectionSet(){
		isDrawable = false;
	}
	
	public Shape2DSelectionSet(String thisName){
		name = thisName;
		isDrawable = false;
	}
	
	@Override
	public Shape getGeometryInstance(){
		return null;
	}
	
	public void addShape(Shape2DInt thisShape, ShapeSet2DInt shapeSet){
		int index = Collections.binarySearch(shapes, thisShape, selComp);
		if (index >= 0) return;
		index = -index - 1;
		shapes.add(index, thisShape);
		parents.add(index, shapeSet);
		shapeAdded = thisShape;
		updateShape();
		//fireShapeListeners();
	}
	
	public boolean hasMember(Shape2DInt thisShape){
		int index = Collections.binarySearch(shapes, thisShape, selComp);
		return (index >= 0);
	}
	
	/*
	public void setTreeNode(){
		//build tree node by adding parent nodes and adding shapes as children
		super.setTreeNode();
		int j, n;
		boolean blnFound;
		//InterfaceShape2DNode thisNode = super.getTreeNode();
		DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode();
		//if (parentsNode == null)
		//	parentsNode = new DefaultMutableTreeNode();
		//parentsNode.removeAllChildren();
		//n = parents.size();
		//ArrayList<DefaultMutableTreeNode> parentNodes = new ArrayList<DefaultMutableTreeNode>();
		
		//add parent nodes
		for (int i = 0; i < parents.size(); i++){
			blnFound = false;
			j = 0;
			n = treeNode.getChildCount();
			while (!blnFound){
				if (j == n){
					parentNode = new DefaultMutableTreeNode(parents.get(i).toString());
					//parentNodes.add(parentNode);
					treeNode.add(parentNode);
					blnFound = true;
				}
				else {
					if (treeNode.getChildAt(j).toString().compareTo(parents.get(i).toString()) == 0){
						parentNode = (DefaultMutableTreeNode)treeNode.getChildAt(j);
					//if (parentNodes.get(j).toString().compareTo(parents.get(i).toString())){
					//	parentNode = (DefaultMutableTreeNode)
						blnFound = true;
					}
				}
				j++;
			}
			parentNode.add((DefaultMutableTreeNode)shapes.get(i).getTreeNode().clone());
		}
		/**@TODO add listener to remove shapes if they are deleted? **
		//return thisNode;
	}
	*/
	
	public ShapeSet2DInt getShapeSet2D(){
		ShapeSet2DInt retSet = new ShapeSet2DInt();
		for (int i = 0; i < shapes.size(); i++)
			retSet.addShape(shapes.get(i), false, false);
		return retSet;
	}
	
	public SectionSet3DInt getFilteredSectionSet(SectionSet3DInt sectSet){
		SectionSet3DInt retSet = new SectionSet3DInt();
		retSet.setRefPlane(sectSet.getRefPlane());
		retSet.setSpacing(sectSet.getSpacing());
		ShapeSet2DInt thisSet;
		
		Iterator<Integer> itr = sectSet.sections.keySet().iterator();
		//for (int i = 0; i < sectSet.sectionSet.items.size(); i++){
			//thisSet = (ShapeSet2DInt)sectSet.sectionSet.items.get(i).objValue;
		while (itr.hasNext()){
			int s = itr.next().intValue();
			thisSet = sectSet.getShapeSet(s);
			for (int j = 0; j < thisSet.getSize(); j++)
				if (hasMember(thisSet.getShape(j)))
					retSet.addShape2D(thisSet.getShape(j), 
									  s,
									  false);
			}
		return retSet;
	}
	
	/*
	public InterfaceShape2DNode getTreeNode(){
		if (treeNode == null) setTreeNode();
		return treeNode;
	}*/
	
	@Override
	public void updateShape(){
		//fireShapeListeners();
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	class ShapeIDComp implements java.util.Comparator<Shape2DInt>{
		public int compare(Shape2DInt s1, Shape2DInt s2){
			if (s1.getID() > s2.getID()) return 1;
			if (s1.getID() == s2.getID()) return 0;
			return -1;
		}
	}
	
	
}