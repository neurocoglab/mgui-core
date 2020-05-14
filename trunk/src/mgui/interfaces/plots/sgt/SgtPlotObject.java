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

import gov.noaa.pmel.sgt.Attribute;
import gov.noaa.pmel.sgt.dm.SGTData;

import java.awt.Color;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.plots.InterfacePlotObject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.util.Unit;


/****************************************************
 * A plottable object for a Scientific Graphics Toolkit (SGT) plot.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class SgtPlotObject<T extends SGTData> extends InterfacePlotObject
													   implements AttributeListener{


	protected gov.noaa.pmel.sgt.Attribute sgt_attribute;
	protected T sgt_data;
	
	public SgtPlotObject(){
		init();
	}
	
	
	private void init(){
		
		// Set up attributes...
		attributes.add(new AttributeSelection<Unit>("Unit X", 
													InterfaceEnvironment.getAllUnits(),
													Unit.class,
													InterfaceEnvironment.getDefaultSpatialUnit()));
		attributes.add(new AttributeSelection<Unit>("Unit Y", 
													InterfaceEnvironment.getAllUnits(),
													Unit.class,
													InterfaceEnvironment.getDefaultSpatialUnit()));
		
		attributes.addAttributeListener(this);
	}
	
	public abstract void setColour(Color colour);
	
	@Override
	public void attributeUpdated(AttributeEvent e) {
		
		mgui.interfaces.attributes.Attribute<?> attribute = e.getAttribute();
		
		if (attribute.getName().startsWith("Unit")){
			
			if (attribute.getName().endsWith("X")){
				if (sgt_data.getXMetaData() == null) return;
				sgt_data.getXMetaData().setUnits(((Unit)attribute.getValue()).getShortName());
				return;
				}
			
			if (attribute.getName().endsWith("Y")){
				if (sgt_data.getYMetaData() == null) return;
				sgt_data.getYMetaData().setUnits(((Unit)attribute.getValue()).getShortName());
				return;
				}
			
			}
		
	}
	
	/*************************************
	 * Returns an SGT attribute object for this object
	 * 
	 * @return
	 */
	public Attribute getSgtAttribute(){
		updateSgtAttribute();
		return sgt_attribute;
	}
	
	/**************************************
	 * Update SGT attribute from object attributes.
	 * 
	 */
	protected abstract void updateSgtAttribute();
	
	/*************************************
	 * Returns the SGT data object for this object
	 * 
	 * @return
	 */
	public T getSgtData() {
		return sgt_data;
	}
	
	/************************************
	 * Sets the SGT data for this object
	 * 
	 * @param object
	 */
	public void setSgtData(T data){
		sgt_data = data;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode node){
		super.setTreeNode(node);
		node.addChild(attributes.issueTreeNode());
	}
	
	/*******************************************
	 * Returns the unit for the X values of this object.
	 * 
	 * @return
	 */
	public Unit getUnitX(){
		return (Unit)attributes.getValue("Unit X");
	}
	
	/*******************************************
	 * Sets the unit for the X values of this object.
	 * 
	 * @param unit
	 */
	public void setUnitX(Unit unit){
		attributes.setValue("Unit X", unit);
	}
	
	/*******************************************
	 * Returns the unit for the Y values of this object.
	 * 
	 * @return
	 */
	public Unit getUnitY(){
		return (Unit)attributes.getValue("Unit Y");
	}
	
	/*******************************************
	 * Sets the unit for the Y values of this object.
	 * 
	 * @param unit
	 */
	public void setUnitY(Unit unit){
		attributes.setValue("Unit Y", unit);
	}
	
}