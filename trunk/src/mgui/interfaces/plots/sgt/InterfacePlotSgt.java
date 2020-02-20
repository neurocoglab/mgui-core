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

import gov.noaa.pmel.sgt.swing.JPlotLayout;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.graphics.util.MouseRelayListener;
import mgui.interfaces.io.DataSourceListener;
import mgui.interfaces.io.InterfaceDataSource;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.plots.InterfacePlot;
import mgui.interfaces.plots.PlotException;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.numbers.MguiDouble;

/****************************************************
 * Abstract class for displaying Scientific Graphics Toolkit (SGT) plots.
 * 
 * <p>The generic type <code>T</code> specifies the type of {@link JPlotLayout} to be displayed.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfacePlotSgt<T extends SgtPlotObject<?>> extends InterfacePlot<MguiDouble>
																   implements DataSourceListener{

	protected SgtPlotLayout<T,?,?> plot_layout;
	protected HashMap<String, T> objects = new HashMap<String, T>();
	
	protected SgtPlotObject<?> last_added, last_removed, last_modified;
	
	/*********************************
	 * Initialize plot.
	 * 
	 */
	protected void _init(){
		super.init();
		
		attributes.add(new Attribute<String>("Title2", ""));
		attributes.add(new Attribute<String>("Title3", ""));
		attributes.add(new Attribute<Color>("Background", Color.white));
		
	}
	
	public abstract void setDataSource(InterfaceDataSource<?> source) throws PlotException;
	
	public void attributeUpdated(AttributeEvent e) {
		
		if (e.getAttribute().getName().startsWith("Title")){
			setTitles();
			return;
			}
		
		if (e.getAttribute().getName().equals("Background")){
			if (plot_layout == null) return;
			plot_layout.setBackground((Color)attributes.getValue("Background"));
			
			plot_layout.paintComponent(plot_layout.getGraphics());
			return;
			}
		
	}
	
	/*********************************************************
	 * Updates this plot's data from its current state.
	 * 
	 */
	public abstract void updatePlotData();
	
	public SgtPlotObject<?> getLastAdded(){
		return last_added;
	}
	
	public SgtPlotObject<?> getLastRemoved(){
		return last_removed;
	}
	
	public SgtPlotObject<?> getLastModified(){
		return last_modified;
	}
	
	protected void setTitles(){
		
		if (plot_layout == null) return;
		
		//Layer layer = plot_layout.getFirstLayer();
		plot_layout.repaint();
	}
	
	protected void setLayout(SgtPlotLayout<T,?,?> layout){
		if (this.plot_layout != null){
			this.remove(this.plot_layout);
			for (int i = 0; i < relay_listeners.size(); i++){
				plot_layout.removeMouseListener(relay_listeners.get(i));
				plot_layout.removeMouseWheelListener(relay_listeners.get(i));
				}
			}
		this.plot_layout = layout;
		plot_layout.setBackground((Color)attributes.getValue("Background"));
		this.add(plot_layout);
		for (int i = 0; i < relay_listeners.size(); i++){
			plot_layout.addMouseListener(relay_listeners.get(i));
			plot_layout.addMouseWheelListener(relay_listeners.get(i));
			}
		
		//set data
		ArrayList<String> keys = new ArrayList<String>(objects.keySet());
		try{
			plot_layout.clearData();
			for (int i = 0; i < keys.size(); i++){
				plot_layout.addData(objects.get(keys.get(i)));
				}	
		}catch (Exception ex){
			InterfaceSession.log("InterfacePlotSge: failed to set plot layout data " +
								 " (encountered exception): " + ex.getMessage(),
								 LoggingType.Errors);
			}
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		
		super.setTreeNode(treeNode);
		treeNode.addChild(plot_layout.issueTreeNode());
		
	}
	
	@Override
	public void addMouseRelayListener(MouseRelayListener listener){
		relay_listeners.add(listener);
		if (plot_layout != null){
			plot_layout.addMouseListener(listener);
			plot_layout.addMouseWheelListener(listener);
			}
	}
	
	@Override
	public void removeMouseRelayListener(MouseRelayListener listener){
		relay_listeners.remove(listener);
		if (plot_layout != null){
			plot_layout.removeMouseListener(listener);
			plot_layout.removeMouseWheelListener(listener);
			}
	}
	
	/****************************************************
	 * Resets the plot's data and redraws it if necessary.
	 * 
	 */
	protected void resetPlot(){
		
		if (plot_layout == null){
		//	setPlotLayout();
			return;
			}
		
		//plot_layout.clear();
		ArrayList<String> labels = new ArrayList<String>(objects.keySet());
		
		for (int i = 0; i < labels.size(); i++){
			String label = labels.get(i);
			SgtPlotObject<?> object = objects.get(label);
			//plot_layout.addData(object.getSgtData(), object.getSgtAttribute(), label);
			}
		
	}
	
	/***********************************************
	 * Sets a plot layout appropriate for this plot type of the sub class.
	 * 
	 */
	protected abstract void setPlotLayout();
	
}