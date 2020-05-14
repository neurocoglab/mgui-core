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

package mgui.interfaces.plots.osp;

import java.awt.BorderLayout;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.io.DataInputStream;
import mgui.interfaces.plots.InterfacePlot;
import mgui.numbers.MguiDouble;

import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.PlottingPanel;


/***********************************
 * General class for displaying Open Source Physics (OSP) plots.
 * For more information see www.opensourcephysics.org
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public abstract class InterfacePlotOsp extends InterfacePlot<MguiDouble> {

	PlottingPanel plottingPanel;
	
	@Override
	protected void init(){
		super.init();
		
		attributes.add(new Attribute<String>("X-Axis", "X"));
		attributes.add(new Attribute<String>("Y-Axis", "Y"));
		
		plottingPanel = new PlottingPanel(getTitle(), getLabelX(), getLabelY());
		
		add(plottingPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void dataInputEvent(DataInputStream<MguiDouble> s) {
		super.dataInputEvent(s);
		//plottingPanel.updateUI();
	}
	
	public String getLabelX(){
		return (String)attributes.getValue("X-Axis");
	}
	
	@Override
	public void setTitle(String title){
		super.setTitle(title);
		//updatePlottingPanel();
	}
	
	public void setLabelX(String t){
		attributes.setValue("X-Axis", t);
		//updatePlottingPanel();
	}
	
	public String getLabelY(){
		return (String)attributes.getValue("Y-Axis");
	}
	
	public void setLabelY(String t){
		attributes.setValue("Y-Axis", t);
		//updatePlottingPanel();
	}
	
	protected void updatePlottingPanel(){
		plottingPanel.setTitle(getTitle());
		plottingPanel.setXLabel(getLabelX());
		plottingPanel.setYLabel(getLabelY());
	}
	
	public void addPlot(Drawable d){
		plottingPanel.addDrawable(d);
	}
	
	public void removePlot(Drawable d){
		plottingPanel.removeDrawable(d);
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e) {
		super.attributeUpdated(e);
		updatePlottingPanel();
	}
	
}