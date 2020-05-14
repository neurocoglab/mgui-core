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

package mgui.interfaces.shapes.util;

import java.util.Iterator;

import mgui.interfaces.shapes.SectionSet3DInt;


public class SectionSetIterator implements Iterator {

	private int currentSection;
	private SectionSet3DInt sourceSet;
	
	public SectionSetIterator(){
		
	}
	
	public SectionSetIterator(SectionSet3DInt thisSet){
		setSectionSet(thisSet);
	}
	
	public boolean hasNext() {
		return (currentSection <= getLastSection());
	}

	public Object next() {
		if (!hasNext())
			return null;
		Object retObj = sourceSet.getShapeSet(currentSection++);
		//currentSection++;
		return retObj;
	}
	
	public int getNextSection(){
		return currentSection;
	}
	
	public int getFirstSection(){
		return sourceSet.getFirstSection();
	}
	
	public int getLastSection(){
		return sourceSet.getLastSection();
	}

	public void remove() {
		// TODO Auto-generated method stub

	}
	
	public void setSectionSet(SectionSet3DInt thisSet){
		sourceSet = thisSet;
		currentSection = sourceSet.getFirstSection();
	}

}