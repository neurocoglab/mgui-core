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

package mgui.morph.sections.util;

import mgui.interfaces.shapes.util.SectionSetIterator;
import mgui.morph.sections.MorphSections3DInt;


public class MorphSectionSetIterator extends SectionSetIterator {

	private int currentSubsection;
	private int currentSection;
	private MorphSections3DInt sourceSet;
	private boolean isFirst;
	
	public MorphSectionSetIterator(MorphSections3DInt thisSet){
		setMorphSectionSet(thisSet);
	}
	
	@Override
	public boolean hasNext() {
		return getNextSection() <= getLastSection(); // || currentSubsection == -1;
	}

	@Override
	public Object next() {
		if (!hasNext()) return null;
		if (isFirst){
			isFirst = false;
			return sourceSet.parentSet.getShapeSet(currentSection);
		}
		Object retObj = sourceSet.getNextSubSection();
		currentSection = sourceSet.getCurrentSection();
		currentSubsection = sourceSet.getCurrentSubsection();
		return retObj;
	}

	@Override
	public int getNextSection(){
		return (currentSection * (sourceSet.iterations + 1)) + currentSubsection + 2;
	}
	
	@Override
	public int getFirstSection(){
		return sourceSet.getFirstSection() * (sourceSet.iterations + 1);
	}
	
	@Override
	public int getLastSection(){
		return sourceSet.getLastSection() * (sourceSet.iterations + 1);
	}
	
	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}
	
	public void setMorphSectionSet(MorphSections3DInt thisSet){
		sourceSet = thisSet;
		sourceSet.resetSections();
		currentSubsection = -1;
		currentSection = sourceSet.getFirstSection();
		isFirst = true;
	}

}