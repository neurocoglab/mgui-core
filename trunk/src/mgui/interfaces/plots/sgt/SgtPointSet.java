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

package mgui.interfaces.plots.sgt;

import gov.noaa.pmel.sgt.PointAttribute;
import gov.noaa.pmel.sgt.dm.PointCollection;
import gov.noaa.pmel.sgt.dm.SGTMetaData;

import java.awt.Color;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;

/**************************************************************
 * Represents a set of points for an Scientific Graphics Toolkit (SGT) plot.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SgtPointSet extends SgtPlotObject<PointCollection> {

	public SgtPointSet(){
		super();
		init();
	}
	
	private void init(){
		
		sgt_data = new PointCollection();
		sgt_data.setXMetaData(new SGTMetaData());
		sgt_data.setYMetaData(new SGTMetaData());
		
		setUnitX(InterfaceEnvironment.getDefaultSpatialUnit());
		setUnitY(InterfaceEnvironment.getDefaultSpatialUnit());
		
		attributes.add(new Attribute<Color>("PointColour", Color.black));
		attributes.add(new Attribute<Integer>("PlotMark", 1));
		//TODO: render shape
	
		sgt_attribute = new PointAttribute();
		
		updateSgtAttribute();
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e) {
		
		if (e.getAttribute().getName().contains("Colour") ||
			e.getAttribute().getName().contains("Mark")){
			updateSgtAttribute();
			return;
			}
		
	}
	
	@Override
	protected void updateSgtAttribute() {
		PointAttribute attribute = (PointAttribute)sgt_attribute;
		attribute.setDrawLabel(false);
		attribute.setColor((Color)attributes.getValue("PointColour"));
		int mark = (Integer)attributes.getValue("PlotMark");
		if (mark < 1) mark = 1; 
		attribute.setMark(mark);
	}

	@Override
	public void setColour(Color colour) {
		attributes.setValue("PointColour", colour);
		
	}

	
	
	
}