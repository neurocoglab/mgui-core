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

package mgui.interfaces.plots.osp;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.io.DataInputStream;
import mgui.interfaces.io.DataInputStreamNamedXY;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.interfaces.plots.InterfacePlotDialog;
import mgui.interfaces.plots.PlotInputException;
import mgui.numbers.MguiDouble;
import mgui.util.Colour;
import mgui.util.Colours;

import org.opensourcephysics.display.Drawable;
import org.opensourcephysics.display.Stripchart;


/*******************************
 * Displays an OSP StripChart; i.e., time vs. data
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class InterfaceStripChart extends InterfacePlotOsp {

	public ArrayList<Stripchart> plots = new ArrayList<Stripchart>();
	public ArrayList<Variable> variables = new ArrayList<Variable>();
	
	public InterfaceStripChart(){
		init();
	}
	
	public InterfaceStripChart(String title){
		init();
		setTitle(title);
	}
	
	@Override
	protected void init(){
		super.init();
		
		attributes.add(new Attribute<MguiDouble>("X-range", new MguiDouble(100)));
		attributes.add(new Attribute<MguiDouble>("Y-range", new MguiDouble(100)));
		attributes.add(new Attribute<MguiDouble>("AxisX-min", new MguiDouble(0)));
		attributes.add(new Attribute<MguiDouble>("AxisX-max", new MguiDouble(100)));
		attributes.add(new Attribute<MguiDouble>("AxisY-min", new MguiDouble(0)));
		attributes.add(new Attribute<MguiDouble>("AxisY-max", new MguiDouble(100)));
		attributes.add(new Attribute<DiscreteColourMap>("ColourMap", new DiscreteColourMap()));
		
	}
	
	@Override
	public InterfacePlotDialog<?> getPlotDialog(){
		return null;
	}
	
	public DiscreteColourMap getColourMap(){
		return (DiscreteColourMap)attributes.getValue("ColourMap");
	}
	
	public void setColourMap(DiscreteColourMap map){
		attributes.setValue("ColourMap", map);
		updateColours();
	}
	
	public double getXRange(){
		return ((MguiDouble)attributes.getValue("X-range")).getValue();
	}
	
	public double getYRange(){
		return ((MguiDouble)attributes.getValue("Y-range")).getValue();
	}
	
	public double getAxisXMin(){
		return ((MguiDouble)attributes.getValue("AxisX-min")).getValue();
	}
	
	public double getAxisXMax(){
		return ((MguiDouble)attributes.getValue("AxisX-max")).getValue();
	}
	
	public double getAxisYMin(){
		return ((MguiDouble)attributes.getValue("AxisY-min")).getValue();
	}
	
	public double getAxisYMax(){
		return ((MguiDouble)attributes.getValue("AxisY-max")).getValue();
	}
	
	// TODO: add setters
	
	@Override
	protected void setInputStream(DataInputStream<MguiDouble> s) throws PlotInputException{
		if (!(s instanceof DataInputStreamNamedXY)) throw new PlotInputException(
				"Input to InterfaceStripChart must be instance of DataInputStreamNamedXY.");
		
		super.setInputStream(s); 
		
		DataInputStreamNamedXY<MguiDouble> stream = (DataInputStreamNamedXY<MguiDouble>)s;
		
		Set<String> names = new TreeSet<String> (stream.getVariableNames());
		variables = new ArrayList<Variable>(names.size());
		
		Iterator<String> itr = names.iterator();
		boolean cmap = hasValidColourMap();
		
		while (itr.hasNext()){
			String name = itr.next();
			Color c = null;
			if (cmap){
				Colour clr = getColourMap().getColour(name);
				if (clr != null) c = clr.getColor();
				}
			
			if (c == null) c = Colours.getRandom().getColor();
		
			variables.add(new Variable(name, c));
			}
		
		resetCharts();
	}
	
	protected void resetCharts(){
		//remove existing
		for (int i = 0; i < plots.size(); i++)
			removePlot(plots.get(i));
		
		//add all variables
		for (int i = 0; i < variables.size(); i++){
			Variable v = variables.get(i);
			Stripchart plot = new Stripchart(getXRange(), getYRange());
			plot.setConnected(true);
			plot.setLineColor(v.getColour());
			plot.setName(v.getName());
			plot.setMarkerSize(0);
			addPlot(plot);
			}
	}
	
	public void updateCharts(){
		for (int i = 0; i < plots.size(); i++)
			plots.get(0).setRange(getXRange(), getYRange());
	}
	
	@Override
	public void addPlot(Drawable d){
		if (!(d instanceof Stripchart)) return;
		super.addPlot(d);
		plots.add((Stripchart)d);
	}
	
	protected void updateColours(){
		boolean cmap = hasValidColourMap();
		
		for (int i = 0; i < variables.size(); i++){
			Variable v = variables.get(i);
			Color c = null;
			if (cmap){
				Colour clr = getColourMap().getColour(v.getName());
				if (clr != null) c = clr.getColor();
				}
			if (c == null) c = Colours.getRandom().getColor();
			v.setColour(c);
			}
		
	}
	
	public boolean hasValidColourMap(){
		if (getColourMap() == null) return false;
		if (!getColourMap().hasNameMap()) return false;
		return true;
	}
	
	@Override
	public void dataInputEvent(DataInputStream<MguiDouble> s) {
		super.dataInputEvent(s);
		DataInputStreamNamedXY<MguiDouble> stream = (DataInputStreamNamedXY<MguiDouble>)s;
		try{
			List<MguiDouble> t = stream.getXData();
			
			for (int i = 0; i < plots.size(); i++){
				List<MguiDouble> y = stream.getYData(plots.get(i).getName());
				for (int j = 0; j < t.size(); j++){
					plots.get(i).append(t.get(j).getValue(), y.get(j).getValue());
					}
				}
		}catch (IOException e){
			InterfaceSession.log("InterfaceStripChart: IOException encountered.", 
					LoggingType.Errors);
			}
		
	}
	
	@Override
	public String toString(){
		return "StripChart: " + getTitle();
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e) {
		super.attributeUpdated(e);
		updateCharts();
	}
	
	public class Variable{
		public AttributeList attributes = new AttributeList();
		
		public Variable(String name, Color colour){
			attributes.add(new Attribute("Name", name));
			attributes.add(new Attribute("Colour", colour));
		}
		
		public String getName(){
			return (String)attributes.getValue("Name");
		}
		
		public Color getColour(){
			return (Color)attributes.getValue("Colour");
		}
		
		public void setColour(Color c){
			attributes.setValue("Colour", c);
		}
	}
	
}