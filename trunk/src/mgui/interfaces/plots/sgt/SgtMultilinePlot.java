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

import gov.noaa.pmel.sgt.dm.SimpleLine;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.io.DataSourceEvent;
import mgui.interfaces.io.InterfaceDataSource;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.plots.InterfacePlotDialog;
import mgui.interfaces.plots.InterfacePlotOptions;
import mgui.interfaces.plots.PlotException;
import mgui.interfaces.plots.PlotTimeSeriesDataSource;
import mgui.numbers.MguiDouble;
import mgui.util.Colours;

/*******************************************************
 * Displays a line plot using the Scientific Graphics Toolkit (SGT) library. Allows
 * multiple lines to be plotted, with arbitrary X and Y coordinates. For time series
 * plots, see {@link SgtTimeSeriesPlot}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SgtMultilinePlot extends InterfacePlotSgt<SgtLine> { // implements TimeSeriesPlot<MguiDouble> {

	//protected HashMap<String, SgtLine> lines = new HashMap<String, SgtLine>();
	protected ArrayList<MguiDouble> x;
	protected ArrayList<ArrayList<MguiDouble>> y;
	protected PlotTimeSeriesDataSource<MguiDouble> data_source;
	
	public SgtMultilinePlot(){
		this("Multiline Plot");
	}
	
	public SgtMultilinePlot(String title){
		init2();
		this.setTitle(title);
	}
	
	/*******************************************
	 * Adds a line series to this plot, with the given label. If this label currently
	 * exists, previous line will be overwritten.
	 * 
	 * @param label
	 * @param line
	 */
	protected void addDataSeries(SgtLine line){
		
		try{
			if (plot_layout != null)
				plot_layout.addData(line);
			objects.put(line.getName(), line);
		}catch (PlotException ex){
			InterfaceSession.log("SgtMultilinePlot.addDataSeries failed with exception: "
								 + ex.getMessage(), 
								 LoggingType.Errors);
			}
	}
	
	@Override
	public void setDataSource(InterfaceDataSource<?> source) throws PlotException{
		
		if (!(source instanceof PlotTimeSeriesDataSource<?>))
			throw new PlotException("SgtMultilinePlot: Data source must be of type PlotTimeSeriesDataSource.");
		
		if (this.data_source != null){
			this.data_source.removeDataSourceListener(this);
			}
		
		this.data_source = (PlotTimeSeriesDataSource<MguiDouble>)source;
		data_source.addDataSourceListener(this);
		try{
			data_source.reset();
		}catch (IOException e){
			InterfaceSession.log("SgtMultilinePlot: Could not set data source; with exception:\n" + e.getMessage(),
								 LoggingType.Errors);
			}
		
	}
	
	private void init2(){
		_init();
		
		attributes.add(new Attribute<Color>("BorderColour", Color.black));
		
		setPlotLayout();
	}
	
	@Override
	protected void setPlotLayout(){
		
		this.plot_layout = new SgtLineLayout();
		setLayout(this.plot_layout);
		
	}
	
	@Override
	public void dataSourceEmission(DataSourceEvent event) {
		// Append XY data from source emission
		
		PlotTimeSeriesDataSource<MguiDouble> source = (PlotTimeSeriesDataSource<MguiDouble>)event.getDataSource();
		ArrayList<String> y_variables = new ArrayList<String>(source.getChannelNames());
		
		x = new ArrayList<MguiDouble>(source.getSourceSignalX());
			
		for (int i = 0; i < y_variables.size(); i++)
			y.set(i, new ArrayList<MguiDouble>(source.getSourceSignalY(y_variables.get(i))));
			
		this.updatePlotData();
	}
	
	@Override
	public void updatePlotData(){
		
		if (x.size() == 0) return;
		
		ArrayList<String> keys = new ArrayList<String>(objects.keySet());
		for (int i = 0; i < objects.size(); i++){
			if (objects.get(keys.get(i)) instanceof SgtLine){
				SimpleLine line = ((SgtLine)objects.get(keys.get(i))).getSgtData();
				double[] x_array = new double[x.size()];
				for (int j = 0; j < x.size(); j++)
					x_array[j] = x.get(j).getValue();
				ArrayList<MguiDouble> y2 = y.get(i);
				double[] y_array = new double[y2.size()];
				for (int j = 0; j < y2.size(); j++)
					y_array[j] = y2.get(j).getValue();
				line.setXArray(x_array);
				line.setYArray(y_array);
				}
			}
		//this.plot_layout.resetZoom();
	}
	
	public void dataSourceReset(DataSourceEvent event){
		
		PlotTimeSeriesDataSource<MguiDouble> source = (PlotTimeSeriesDataSource<MguiDouble>)event.getDataSource();
		//int size = source.getSize();
		ArrayList<String> y_variables = new ArrayList<String>(source.getChannelNames());
		updateLines(y_variables);
		
		// Reset X and Y data
		x = new ArrayList<MguiDouble>();
		y = new ArrayList<ArrayList<MguiDouble>>(y_variables.size());
	
		for (int i = 0; i < y_variables.size(); i++){
			y.add(new ArrayList<MguiDouble>());
			}
		
		
	}
	
	/************************************
	 * Updates this plot's lines with a list of variables; will preserve existing lines
	 * if they are in <code>list</code>, and remove them otherwise. Will add new variables
	 * if not already in list, with default attributes - but using a novel colour.
	 * 
	 * @param list
	 */
	protected void updateLines(ArrayList<String> list){
		
		ArrayList<Color> colours = new ArrayList<Color>();
		TreeSet<String> existing = new TreeSet<String>(objects.keySet());
		ArrayList<String> retain = new ArrayList<String>();
		ArrayList<String> add = new ArrayList<String>();
		
		for (int i = 0; i < list.size(); i++){
			String key = list.get(i);
			if (existing.contains(key)){
				//retain
				retain.add(key);
				colours.add((Color)objects.get(key).getAttribute("LineColour").getValue());
			}else{
				//add
				add.add(key);
				}
			}
		
		HashMap<String, SgtLine> new_map = new HashMap<String, SgtLine>();
		for (int i = 0; i < retain.size(); i++)
			new_map.put(retain.get(i), (SgtLine)objects.get(retain.get(i)));
		
		objects.clear();
		objects.putAll(new_map);
		
		for (int i = 0; i < add.size(); i++){
			Color colour = Colours.getRandom().getColor();
			boolean found = false;
			do {
				colour = Colours.getRandom().getColor();
				for (int c = 0; c < colours.size(); c++)
					if (colour.equals(colours.get(c))){
						found = true;
						break;
						}
			}while (found);
			SgtLine line = new SgtLine();
			line.setColour(colour);
			objects.put(add.get(i), line);
			}
		
		resetPlot();
		
	}
	
	public String getPlotType(){
		return "Multiline Plot";
	}
	
	@Override
	public InterfacePlotOptions<?> getOptionsInstance(){
		return new InterfacePlotOptions<SgtMultilinePlot>(this);
	}
	
	@Override
	public void setFromOptions(InterfacePlotOptions<?> options){
		
	}

	@Override
	public InterfacePlotDialog<?> getPlotDialog(){
		return new SgtMultilinePlotDialog(InterfaceSession.getSessionFrame(),
				new InterfacePlotOptions<SgtMultilinePlot>());
	}
}