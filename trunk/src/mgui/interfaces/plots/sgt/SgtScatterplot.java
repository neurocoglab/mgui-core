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

import gov.noaa.pmel.sgt.dm.PointCollection;
import gov.noaa.pmel.sgt.dm.SimplePoint;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.io.DataSourceEvent;
import mgui.interfaces.io.InterfaceDataSource;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.plots.InterfacePlotDialog;
import mgui.interfaces.plots.InterfacePlotOptions;
import mgui.interfaces.plots.PlotException;
import mgui.interfaces.plots.PlotXYDataSource;
import mgui.interfaces.plots.XYData;
import mgui.numbers.MguiDouble;
import mgui.util.Colours;

/****************************************************************
 * Displays an XY scatter plot using the Scientific Graphics Toolkit (SGT) library.
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SgtScatterplot extends InterfacePlotSgt<SgtPointSet> {

	protected HashMap<String,List<XYData<MguiDouble>>> xy;
	protected PlotXYDataSource<MguiDouble> data_source;
	
	public SgtScatterplot(){
		this("XY Scatter Plot");
	}
	
	public SgtScatterplot(String title){
		init2();
		this.setTitle(title);
	}
	
	private void init2(){
		_init();
		
		attributes.add(new Attribute<Color>("BorderColour", Color.black));
		
		//setPlotLayout();
	}
	
	@Override
	public void setDataSource(InterfaceDataSource<?> source) throws PlotException{
		
		if (!(source instanceof PlotXYDataSource<?>))
			throw new PlotException("SgtScatterPlot: Data source must be of type PlotXYDataSource.");
		
		if (this.data_source != null){
			this.data_source.removeDataSourceListener(this);
			}
		
		this.data_source = (PlotXYDataSource<MguiDouble>)source;
		data_source.addDataSourceListener(this);
		try{
			data_source.reset();
		}catch (IOException e){
			InterfaceSession.log("SgtMultilinePlot: Could not set data source; with exception:\n" + e.getMessage(),
								 LoggingType.Errors);
			}
		
	}

	@Override
	public void dataSourceEmission(DataSourceEvent event) {
		PlotXYDataSource<MguiDouble> source = (PlotXYDataSource<MguiDouble>)event.getDataSource();
		ArrayList<String> channels = new ArrayList<String>(source.getChannelNames());
		
		for (int i = 0; i < channels.size(); i++){
			xy.put(channels.get(i), source.getSourceSignalXY(channels.get(i)));
			}
		
		this.updatePlotData();
	}

	@Override
	public void dataSourceReset(DataSourceEvent event) {
		
		PlotXYDataSource<MguiDouble> source = (PlotXYDataSource<MguiDouble>)event.getDataSource();
		//int size = source.getSize();
		ArrayList<String> channels = new ArrayList<String>(source.getChannelNames());
		updatePoints(channels);
		
		// Reset X and Y data
		xy = new HashMap<String,List<XYData<MguiDouble>>>();
		
		for (int i = 0; i < channels.size(); i++){
			ArrayList<XYData<MguiDouble>> this_xy = new ArrayList<XYData<MguiDouble>>();
			xy.put(channels.get(i), this_xy);
			// Fill this array
			for (int j = 0; j < source.getChannelSize(channels.get(i)); j++)
				this_xy.add(new XYData<MguiDouble>(new MguiDouble(), new MguiDouble()));
			}
		
		
	}

	/************************************
	 * Updates this plot's point sets with a list of variables; will preserve existing sets
	 * if they are in <code>list</code>, and remove them otherwise. Will add new variables
	 * if not already in list, with default attributes - but using a novel colour.
	 * 
	 * @param list
	 */
	protected void updatePoints(ArrayList<String> list){
		
		ArrayList<Color> colours = new ArrayList<Color>();
		TreeSet<String> existing = new TreeSet<String>(objects.keySet());
		ArrayList<String> retain = new ArrayList<String>();
		ArrayList<String> add = new ArrayList<String>();
		
		for (int i = 0; i < list.size(); i++){
			String key = list.get(i);
			if (existing.contains(key)){
				//retain
				retain.add(key);
				colours.add((Color)objects.get(key).getAttribute("PointColour").getValue());
			}else{
				//add
				add.add(key);
				}
			}
				
		HashMap<String, SgtPointSet> new_map = new HashMap<String, SgtPointSet>();
		for (int i = 0; i < retain.size(); i++)
			new_map.put(retain.get(i), (SgtPointSet)objects.get(retain.get(i)));
		
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
			
			SgtPointSet points = new SgtPointSet();
			points.setColour(colour);
			objects.put(add.get(i), points);
			}
		
		resetPlot();
		
	}
	
	@Override
	public void updatePlotData(){
		if (xy.size() == 0) return;
		
		ArrayList<String> keys = new ArrayList<String>(objects.keySet());
		for (int i = 0; i < objects.size(); i++){
			String channel = keys.get(i);
			if (objects.get(channel) instanceof SgtPointSet){
				PointCollection pts = ((SgtPointSet)objects.get(keys.get(i))).getSgtData();
				pts.clear();
				List<XYData<MguiDouble>> list = xy.get(channel);
				for (int j = 0; j < list.size(); j++){
					SimplePoint p = new SimplePoint(list.get(j).getX().getValue(), 
													list.get(j).getY().getValue(),
													"" + j);
					pts.add(p);
					}
				}
			}
		
	}

	/*******************************************
	 * Adds a point set to this plot, with the given label. If this label currently
	 * exists, previous point set will be overwritten.
	 * 
	 * @param label
	 * @param line
	 */
	protected void addDataSeries(SgtPointSet points){
		
		try{
			if (plot_layout != null)
				plot_layout.addData(points);
			objects.put(points.getName(), points);
		}catch (PlotException ex){
			InterfaceSession.log("SgtScatterplot.addDataSeries failed with exception: "
								 + ex.getMessage(), 
								 LoggingType.Errors);
			}
	}
	
	@Override
	protected void setPlotLayout(){
		
		this.plot_layout = new SgtPointSetLayout();
		setLayout(this.plot_layout);
		
	}
	

	public String getPlotType(){
		return "XY Scatter Plot";
	}
	
	@Override
	public InterfacePlotOptions<?> getOptionsInstance(){
		return new InterfacePlotOptions<SgtScatterplot>(this);
	}
	
	@Override
	public void setFromOptions(InterfacePlotOptions<?> options){
		
	}

	@Override
	public InterfacePlotDialog<?> getPlotDialog(){
		return new SgtScatterPlotDialog(InterfaceSession.getSessionFrame(),
				new InterfacePlotOptions<SgtScatterplot>());
	}
	
}