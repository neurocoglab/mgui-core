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

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.Text2DInt;
import mgui.morph.sections.util.MorphSectionSetIterator;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;


/*******
 * Class representing a set of intermediate sections (subsections), such that each 
 * subsection holds an iterative morphing representation from some source shape to
 * some target shape.
 * @author Andrew Reid
 *
 */

public class MorphSections3DInt extends SectionSet3DInt {

	public int iterations;
	public SectionSet3DInt parentSet;
	public boolean isNextSection = false;
	public boolean isPrevSection = false;
	public AttributeList shapeAttr;
	private int currentSection;
	
	public MorphSections3DInt(){
		//super();
		init();
	}
	
	public MorphSections3DInt(SectionSet3DInt parent, int iter){
		//super();
		parentSet = parent;
		iterations = iter;
		init();
	}
	
	@Override
	protected void init(){
		super.init();
		//attributes
		attributes.add(new Attribute("ParentSectionSet", new SectionSet3DInt()));
		attributes.add(new Attribute("NoIterations", new MguiInteger(0)));
		attributes.add(new Attribute("CurrentSubsection", new MguiInteger(-1)));
		
		if (parentSet != null)
			setSpacing(parentSet.getSpacing() / ((float)iterations + 1));
			//spacing = parentSet.spacing / ((double)iterations + 1);
		
		//shape attributes
		shapeAttr = new AttributeList();
			
		shapeAttr.add(new Attribute("LineStyle", new BasicStroke()));
		shapeAttr.add(new Attribute("LineColour", Color.RED));		
		shapeAttr.add(new Attribute("CoordSys", new MguiInteger(DrawingEngine.DRAW_MAP)));
		shapeAttr.add(new Attribute("IsVisible", new MguiBoolean(true)));
		shapeAttr.add(new Attribute("HasFill", new MguiBoolean(false)));
		shapeAttr.add(new Attribute("FillColour", Color.WHITE));
		shapeAttr.add(new Attribute("ShowNodes", new MguiBoolean(true)));
		shapeAttr.add(new Attribute("NodeColour", Color.BLUE));
		shapeAttr.add(new Attribute("LabelNodes", new MguiBoolean(true)));
		shapeAttr.add(new Attribute("LabelObj", new Text2DInt("N", 10, 7)));
		shapeAttr.add(new Attribute("LabelStrings", new ArrayList<String>()));
		shapeAttr.add(new Attribute("LabelOffsetX", new MguiDouble(5)));
		shapeAttr.add(new Attribute("LabelOffsetY", new MguiDouble(0)));
		
		//bounds
		updateShape();
	}
	
	public void setParent(SectionSet3DInt parent, int iter){
		parentSet = parent;
		iterations = iter;
		//spacing = parent.spacing / (iter);
		setSpacing(parent.getSpacing() / iter);
		init();
	}
	
	@Override
	public ShapeSet2DInt getShapeSet(int keyVal){
		//subsection -1 is the parent section
		int subSect = getCurrentSubsection();
		if (subSect == -1) 
			return parentSet.getShapeSet(keyVal);
		//if (width != spacing && width > 0)
		//	return getShapeSet(keyVal, width);
		ShapeSet2DInt thisSet = sections.get(new Integer(keyVal * iterations + subSect));
		if (thisSet == null) thisSet = new ShapeSet2DInt();
		return thisSet;
	}
	
	@Override
	public ShapeSet2DInt getShapeSet(int keyVal, double sectionWidth){
		int subSect = getCurrentSubsection();
		if (subSect == -1) return parentSet.getShapeSet(keyVal, sectionWidth);
		if (width <= 0) return getShapeSet(keyVal);
		//return a set of all shapes within sectionWidth of keyVal
		InterfaceSession.log("\n==> Width > 0 Error!?\n");
		int half = keyVal - (int)((sectionWidth / 2) / getSpacing());
		ShapeSet2DInt retSet = new ShapeSet2DInt();
		Object thisSet = null;
		for (int i = keyVal - half; i <= keyVal + half; i++){
			//thisSet = (ShapeSet2DInt)sectionSet.getValue(iterations * i + subSect);
			thisSet = sections.get(new Integer(iterations * i + subSect));
			if (thisSet != null)
				retSet.addUnionSet((ShapeSet2DInt)thisSet);
			}
		if (retSet == null) retSet = new ShapeSet2DInt();
		retSet.idStr = getName() + "." + String.valueOf(keyVal);
		return retSet;
	}
	
	public int getCurrentSubsection(){
		return ((MguiInteger)attributes.getValue("CurrentSubsection")).getInt();
	}
	
	@Override
	public boolean hasSection(int i){
		int subSect = getCurrentSubsection(); 
		if (subSect == -1) return parentSet.hasSection(i);
		return sections.containsKey(new Integer(iterations * i + subSect));
		//Object thisObj = sectionSet.getValue(iterations * i + subSect);
		//if (thisObj == null) return false;
		//return true;
	}
	
	@Override
	public double getSectionDist(int i){
		int subSect = getCurrentSubsection(); 
		if (subSect == -1) return parentSet.getSectionDist(i);
		return ((i * parentSet.getSpacing()) + ((subSect + 1) * getSpacing()));
		//return spacing * i * iterations + spacing * subSect;
	}
	
	//subSect is an absolute reference...
	public float getSubSectionDist(int subSect){
		//get section number
		int thisSect = (int)(Math.floor(subSect / (iterations + 1)));
		//get remainder (subsection number)
		subSect -= thisSect * (iterations + 1);
		return ((thisSect * parentSet.getSpacing()) + ((subSect) * getSpacing()));
	}
	
	public void setCurrentSubsection(int i){
		isNextSection = false;
		isPrevSection = false;
		if (i < -1){
			i = iterations - 1;
			isPrevSection = true;
			}
		if (i >= iterations){
			i = -1;
			isNextSection = true;
			}
		((MguiInteger)attributes.getValue("CurrentSubsection")).setValue(i);
	}
	
	public void iterateSubsection(){
		setCurrentSubsection(((MguiInteger)attributes.getValue("CurrentSubsection")).getInt() + 1);
	}
	
	public void addShape2D(Shape2DInt thisShape, int section, int subsection){
		addShape2D(thisShape, section, subsection, true);
	}
	
	public void addShape2D(Shape2DInt thisShape, int section, int subsection, boolean update){
		//if plane exists in sectionNums, add it to appropriate set
		//ShapeSet2DInt thisSect = (ShapeSet2DInt)sectionSet.getValue(section * iterations + subsection);
		ShapeSet2DInt thisSect = sections.get(new Integer(section * iterations + subsection));
		
		//if section doesn't exist, add new section & set
		if(thisSect == null){
			int newSect = section;
			while (subsection >= iterations){
				subsection -= iterations;
				newSect++;
			}
			thisSect = new ShapeSet2DInt();
			thisSect.idStr = getName() + "." + newSect + "." + subsection;
			thisSect.addShape(thisShape, true, true);
			thisSect.addShapeListener(this);
			if (thisShape.isLight()) thisShape.setAttributes(shapeAttr);
			//sectionSet.addItem(newSect * iterations + subsection, thisSect);
			sections.put(new Integer(newSect * iterations + subsection), thisSect);
			//shapeUpdated(new ShapeEvent(this));
			if (update)
				updateShape();
			fireShapeModified();
			return;
		}
		
		//otherwise add to existing set
		thisSect.addShape(thisShape, true, true);
		if (thisShape.isLight()) thisShape.setAttributes(shapeAttr);
		//thisShape.addShapeListener(thisSect);
		//shapeUpdated(new ShapeEvent(this));
		if (update)
			updateShape();
		fireShapeModified();
	}
	
	/*
	public void updateLightShapes(){
		ShapeSet2DInt thisSet;
		for (int i = 0; i < sectionSet.items.size(); i++){
			thisSet = (ShapeSet2DInt)sectionSet.items.get(i).objValue;
			for (int j = 0; j < thisSet.members.size(); j++){
				if (thisSet.members.get(j).isLight())
					thisSet.members.get(j).attributes = shapeAttr;
				}
			}
	}
	*/
	
	@Override
	public String toString(){
		return getName();
	}
	
	/**********
	 * Adds shapes from morph section set newSections to corresponding sections
	 * in this set.
	 * @param newSections MorphSections3DInt object to perform union with
	 *
	
	public void addUnionSet(MorphSections3DInt newSections){
		int section;
		/**@TODO test for equality of reference plane and spacing? 
		 * or.. adjust section values as a function of reference plane & spacing  
		 ***
		for (int i = 0; i < newSections.sectionSet.items.size(); i++){
				section = newSections.sectionSet.items.get(i).keyValue;
				if (super.hasSection(section))
					((ShapeSet2DInt)sectionSet.getValue(section)).
						addUnionSet((ShapeSet2DInt)newSections.sectionSet.items.get(i).objValue);
					else {
				ShapeSet2DInt thisSect = new ShapeSet2DInt();
				thisSect.idStr = name + "." + String.valueOf(section);
				sectionSet.addItem(section, thisSect);
				thisSect.addUnionSet((ShapeSet2DInt)newSections.sectionSet.items.get(i).objValue);
				thisSect.addShapeListener(this);
					}
			fireShapeListeners();
		}
	}
	 
	
	public BranchGroup getScene3DObject(){
		return getScene3DObject(null);
	}
	
	public BranchGroup getScene3DObject(ShapeSelectionSet selSet){
		//get ShapeSet3DInt scene groups for each section
		BranchGroup retGroup = new BranchGroup();
		//add subsections
		float dist;
		ShapeSet2DInt thisSet2D;
		ShapeSet3DInt thisSet3D;
		BranchGroup thisGroup;
				
		for (int i = 0; i < sectionSet.items.size(); i++){
			dist = getSubSectionDist(getSubSection(sectionSet.items.get(i).keyValue));
			thisSet2D = (ShapeSet2DInt)sectionSet.items.get(i).objValue;
			thisSet3D = ShapeFunctions.getShapeSet3DFromSection((Plane3D)thisShape,
																dist,
																thisSet2D,
																selSet);
			thisGroup = thisSet3D.getScene3DObject();
			if (thisGroup != null)
				retGroup.addChild(thisGroup);
		}
		
		//scene3DObject = retGroup;
		return retGroup;
	}
	*/
	
	//return index of subsection of the section in this set at i (i.e., including parent
	//sections
	public int getSubSection(int i){
		int thisSect = (int)Math.floor(i / iterations);
		return i + thisSect + 1;
	}
	
	public void resetSections(){
		setCurrentSubsection(-1);
		currentSection = getFirstSection();
	}
	
	@Override
	public int getFirstSection(){
		return parentSet.getFirstSection();
	}
	
	@Override
	public int getLastSection(){
		return parentSet.getLastSection();
	}
	
	public ShapeSet2DInt getNextSubSection(){
		iterateSubsection();
		int i = getCurrentSubsection();
		//if a parent section
		if (i < 0 && isNextSection)
			return parentSet.getShapeSet(++currentSection);
		return getShapeSet(currentSection);
	}
	
	public int getCurrentSection(){
		return currentSection;
	}
	
	@Override
	public void setFromSectionSet(SectionSet3DInt thisSet){
		if (thisSet instanceof MorphSections3DInt)
			setParent(((MorphSections3DInt)thisSet).parentSet, 1);
		iterations = ((MorphSections3DInt)thisSet).iterations;
		super.setFromSectionSet(thisSet);
	}
	
	@Override
	public SectionSet3DInt getInitSectionSet(){
		MorphSections3DInt retSet = new MorphSections3DInt();
		retSet.setFromSectionSet(this);
		return retSet;
	}
	
	@Override
	public Iterator getIterator(){
		resetSections();
		return new MorphSectionSetIterator(this);
	}
	
}