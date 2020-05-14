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

package mgui.interfaces.plots.sgt;

import gov.noaa.pmel.sgt.LineAttribute;
import gov.noaa.pmel.sgt.dm.SGTData;
import gov.noaa.pmel.sgt.dm.SGTMetaData;
import gov.noaa.pmel.sgt.dm.SimpleLine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;

/**********************************************
 * A plotted line in a Scientific Graphics Toolkit (SGT) plot. Holds attributes for the
 * line such as colour and stroke, etc. TODO: curve function, etc.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SgtLine extends SgtPlotObject<SimpleLine> {

	//protected SimpleLine line;
	
	public SgtLine(){
		super();
		init();
	}
	
	private void init(){
		
		sgt_data = new SimpleLine(new double[]{0}, new double[]{0}, "");
		sgt_data.setXMetaData(new SGTMetaData());
		sgt_data.setYMetaData(new SGTMetaData());
		
		setUnitX(InterfaceEnvironment.getDefaultSpatialUnit());
		setUnitY(InterfaceEnvironment.getDefaultSpatialUnit());
		
		attributes.add(new Attribute<Color>("LineColour", Color.black));
		attributes.add(new Attribute<Stroke>("LineStroke", new BasicStroke(1.0f)));
	
		sgt_attribute = new LineAttribute();
		updateSgtAttribute();
	}
	
	public void setColour(Color colour){
		attributes.setValue("LineColour", colour);
	}
	
	public void setStroke(Stroke stroke){
		attributes.setValue("LineStroke", stroke);
	}

	@Override
	public void attributeUpdated(AttributeEvent e) {
		
		if (e.getAttribute().getName().contains("Colour")){
			updateSgtAttribute();
			return;
			}
		
	}
	
	@Override
	protected void updateSgtAttribute(){
		
		LineAttribute attribute = (LineAttribute)sgt_attribute;
		attribute.setBatch(true);
		attribute.setColor((Color)attributes.getValue("LineColour"));
		
		//TODO: set up line style from stroke
		
		//notify property listeners
		attribute.setBatch(false);
		
	}
	
	
}