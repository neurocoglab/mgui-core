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

import gov.noaa.pmel.sgt.CartesianGraph;
import gov.noaa.pmel.sgt.Graph;
import gov.noaa.pmel.sgt.JPane;
import gov.noaa.pmel.sgt.Layer;
import gov.noaa.pmel.sgt.LinearTransform;
import gov.noaa.pmel.sgt.PlainAxis;
import gov.noaa.pmel.sgt.dm.SGTData;
import gov.noaa.pmel.util.Dimension2D;
import gov.noaa.pmel.util.Range2D;
import gov.noaa.pmel.util.SoTPoint;
import gov.noaa.pmel.util.SoTRange;
import gov.noaa.pmel.util.SoTValue;

import java.awt.Dimension;
import java.util.ArrayList;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.plots.PlotException;
import mgui.interfaces.plots.sgt.SgtPlotFunctions.AxisType;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;

/***************************************************************
 * Abstract extension of {@link SgtPlotObject} for {@link CartesianGraph} objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <T> SgtPlotObject for this layout
 */
public abstract class SgtCartesianLayout<T extends SgtPlotObject<?>> extends SgtPlotLayout<T, CartesianGraph, PlainAxis> {

	/*** Names for the base X and Y axes **/
	protected String base_x, base_y;
	
	protected static final String BASE_X = "X", BASE_Y = "Y";
	
	public SgtCartesianLayout(){
		super("", new Dimension(10,10), GraphType.Cartesian);
		_init();
	}
	
	public SgtCartesianLayout(String title, Dimension size){
		super(title, size, GraphType.Cartesian);
		_init();
	}
	
	private void _init(){
		
		/*
		attributes.add(new Attribute<Font>("Label X Font", new Font("Arial", Font.PLAIN, 5)));
		attributes.add(new Attribute<Color>("Label X Colour", Color.black));
		attributes.add(new Attribute<String>("Label X", "X"));
		attributes.add(new Attribute<MguiFloat>("Label X Size", new MguiFloat(0.05f)));
		attributes.add(new Attribute<MguiFloat>("Tick1 X Size", new MguiFloat(0.05f)));
		
		attributes.add(new Attribute<Font>("Label Y Font", new Font("Arial", Font.PLAIN, 12)));
		attributes.add(new Attribute<Color>("Label Y Colour", Color.black));
		attributes.add(new Attribute<String>("Label Y", "Y"));
		attributes.add(new Attribute<MguiFloat>("Label Y Size", new MguiFloat(0.05f)));
		attributes.add(new Attribute<MguiFloat>("Tick1 Y Size", new MguiFloat(0.05f)));
		
		attributes.add(new Attribute<String>("Range X", "0.0 10.0 1.0"));
		attributes.add(new Attribute<String>("Range Y", "0.0 10.0 1.0"));
		*/
		
		attributes.add(new Attribute<MguiDouble>("Size X", new MguiDouble(1)));
		attributes.add(new Attribute<MguiDouble>("Min X", new MguiDouble(0.2)));
		attributes.add(new Attribute<MguiDouble>("Max X", new MguiDouble(0.8)));
		attributes.add(new Attribute<MguiDouble>("Size Y", new MguiDouble(1)));
		attributes.add(new Attribute<MguiDouble>("Min Y", new MguiDouble(0.2)));
		attributes.add(new Attribute<MguiDouble>("Max Y", new MguiDouble(0.8)));
		
		/*
		attributes.add(new Attribute<MguiBoolean>("Auto Range X", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiBoolean>("Auto Range Y", new MguiBoolean(false)));
		
		attributes.add(new Attribute<String>("Format X", "0.00"));
		attributes.add(new Attribute<String>("Format Y", "0.00"));
		*/
		
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e) {
		
		Attribute<?> attribute = e.getAttribute();
		
		if (attribute.getName().equals("Position")){
			updateAxes();
			updateRanges();
			return;
			}
		
		if (attribute.getName().contains("Range")){
			updateRanges();
			return;
			}
		
		if (	attribute.getName().contains("Label") ||
				attribute.getName().contains("Ordinate") ||
				attribute.getName().contains("Tick") ||
				attribute.getName().contains("Min") ||
				attribute.getName().contains("Max")){
			updateAxes();
			updateRanges();
			return;
			}
		
	}
	
	@Override
	public SgtAxis<PlainAxis> addAxis(String name, PlainAxis axis, AxisType type) throws PlotException{
		
		if (type != AxisType.X && type != AxisType.Y)
			throw new PlotException("Cartesian layouts can only have X or Y axes.");
		
		SgtAxis<PlainAxis> s_axis = super.addAxis(name, axis, type);
		Layer layer = this.getBaseLayer();
		CartesianGraph graph = (CartesianGraph)layer.getGraph();
		
		if (graph == null){
			axis_map.remove(name);
			throw new PlotException("Must initialize the plot before adding an axis.");
			}
		
		switch(type){
			case X:
				graph.addXAxis(name, axis);
				if (base_x == null)
					base_x = name;
				return s_axis;
			case Y:
				graph.addYAxis(name, axis);
				if (base_y == null)
					base_y = name;
				return s_axis;
			}
		
		//won't get here
		return null;
	}
	
	@Override
	public void removeAxis(String name){
		SgtAxis<PlainAxis> s_axis = axis_map.get(name);
		if (s_axis == null) return;
		
		axis_map.remove(name);
		switch (s_axis.getType()){
			case X:
				if (base_x.equals(name))
					base_x = null;
				break;
			case Y:
				if (base_y.equals(name))
					base_y = null;
				break;
			}
		
		s_axis.attributes.removeAttributeListener(this);
	}
	
	/*******************************************
	 * Updates the axes properties with this layout's attributes.
	 * 
	 */
	protected void updateAxes(){
		Layer layer = this.getBaseLayer();
		if (layer == null) return;
		
		CartesianGraph graph = (CartesianGraph)layer.getGraph();
		
		ArrayList<String> names = new ArrayList<String>(axis_map.keySet());
		
		try{
			double ord = ((MguiFloat)attributes.getValue("Ordinates Size")).getValue();
			for (int i = 0; i < names.size(); i++){
				SgtAxis<PlainAxis> axis = this.getAxis(names.get(i));
				axis.update();
				axis.getAxis().setLabelHeightP(ord);
				axis.getAxis().setLabelHeightP(ord);
				}
		}catch (PlotException ex){
			InterfaceSession.log("SgtCartesianLayout: could not update all axes", 
								 LoggingType.Errors);
			InterfaceSession.handleException(ex);
			}
		
		SgtAxis<PlainAxis> axis_x = this.getMainAxis(AxisType.X);
		SgtAxis<PlainAxis> axis_y = this.getMainAxis(AxisType.Y);
		
		SoTRange range_x = axis_x.getRange();
        SoTRange range_y = axis_y.getRange();
		
        SoTPoint origin = new SoTPoint(axis_x.getOrigin(), axis_y.getOrigin());
        
        //Set all appropriate axes flush with main axes
        //ArrayList<String> names = new ArrayList<String>(axis_map.keySet());
        for (int i = 0; i < names.size(); i++){
        	SgtAxis<PlainAxis> axis = this.getAxis(names.get(i));
        	if (axis.isFlushToMain())
        		axis.setLocation(origin);
        	}
        
		//TODO: make axis optional
		updateTransforms();
        
        this.repaint();
		
	}
	
	@Override
	public SgtAxis<PlainAxis> getMainAxis(AxisType type){
		
		switch(type){
		case X:
			if (this.base_x == null) return null;
			return getAxis(base_x);
		case Y:
			if (this.base_y == null) return null;
			return getAxis(base_y);
		}
		return null;
	}
	
	/******************************************
	 * Returns the X axis range, from a string of the form
	 * 
	 * <p>[min] [max] [step]
	 * 
	 * @return
	 */
	public SoTRange getRangeX(){
		
		SgtAxis<PlainAxis> axis = getMainAxis(AxisType.X);
		if (axis == null) return null;
		return axis.getRange();
		
		/*
		String range = (String)attributes.getValue("Range X");
		String[] parts = range.split(" ");
		if (parts.length != 3){
			InterfaceSession.log("SgtPlotLayout: invalid range '" + range + "'", 
								 LoggingType.Errors);
			return null;
			}
		return new SoTRange.Double(Double.valueOf(parts[0]),
								   Double.valueOf(parts[1]),
								   Double.valueOf(parts[2]));					   
		*/
	}
	
	/******************************************
	 * Returns the Y axis range, from a string of the form
	 * 
	 * <p>[min] [max] [step]
	 * 
	 * @return
	 */
	public SoTRange getRangeY(){
		
		SgtAxis<PlainAxis> axis = getMainAxis(AxisType.Y);
		if (axis == null) return null;
		return axis.getRange();
		
		/*
		String range = (String)attributes.getValue("Range Y");
		String[] parts = range.split(" ");
		if (parts.length != 3){
			InterfaceSession.log("SgtPlotLayout: invalid range '" + range + "'", 
								 LoggingType.Errors);
			return null;
			}
		return new SoTRange.Double(Double.valueOf(parts[0]),
								   Double.valueOf(parts[1]),
								   Double.valueOf(parts[2]));
		*/
	}
	
	/*************************************************
	 * The X dimension of the graph; axes are defined relative to this.
	 * 
	 * @return
	 */
	public double getSizeX(){
		return ((MguiDouble)attributes.getValue("Size X")).getValue();
	}
	
	/*************************************************
	 * Returns the start of the X axis, in physical units relative to its size 
	 * (see {@link JPane} for details).
	 * 
	 * @return
	 */
	public double getMinX(){
		return ((MguiDouble)attributes.getValue("Min X")).getValue();
	}
	
	/*************************************************
	 * Returns the end of the X axis, in physical units (see {@link JPane} for details).
	 * 
	 * @return
	 */
	public double getMaxX(){
		return ((MguiDouble)attributes.getValue("Max X")).getValue();
	}
	
	/*************************************************
	 * The Y dimension of the graph; axes are defined relative to this.
	 * 
	 * @return
	 */
	public double getSizeY(){
		return ((MguiDouble)attributes.getValue("Size Y")).getValue();
	}
	
	/*************************************************
	 * Returns the start of the Y axis, in physical units (see {@link JPane} for details).
	 * 
	 * @return
	 */
	public double getMinY(){
		return ((MguiDouble)attributes.getValue("Min Y")).getValue();
	}
	
	/*************************************************
	 * Returns the end of the Y axis, in physical units (see {@link JPane} for details).
	 * 
	 * @return
	 */
	public double getMaxY(){
		return ((MguiDouble)attributes.getValue("Max Y")).getValue();
	}
	
	/*************************************************
	 * Indicates whether this layout automatically adjusts its X range to its X data.
	 * 
	 * @return
	 */
	public boolean isAutoRangeX(){
		SgtAxis<PlainAxis> axis = getMainAxis(AxisType.X);
		if (axis == null) return false;
		return axis.isAutoRange();
		//return ((MguiBoolean)attributes.getValue("Auto Range X")).getTrue();
	}
	
	/*************************************************
	 * Indicates whether this layout automatically adjusts its Y range to its Y data.
	 * 
	 * @return
	 */
	public boolean isAutoRangeY(){
		SgtAxis<PlainAxis> axis = getMainAxis(AxisType.Y);
		if (axis == null) return false;
		return axis.isAutoRange();
		//return ((MguiBoolean)attributes.getValue("Auto Range Y")).getTrue();
	}
	
	/**************************************************
	 * Sets this layout's X range from <code>range</code>.
	 * 
	 * @param range
	 */
	public void setRangeX(SoTRange range){
		SgtAxis<PlainAxis> axis = getMainAxis(AxisType.X);
		if (axis == null) return;
		axis.setRange(range);
		/*
		String s = "" + range.getStart() + " " + range.getEnd() + " " + range.getDelta();
		try{
			if (getBaseLayer() != null && getBaseLayer().getGraph() != null){
				CartesianGraph graph = (CartesianGraph)getBaseLayer().getGraph();
				Axis axis = graph.getXAxis(BOTTOM_AXIS);
				range = Graph.computeRange(range, auto_interval_x);
				axis.setRangeU(range);
				s = "" + range.getStart() + " " + range.getEnd() + " " + range.getDelta();
				}
			attributes.setValue("Range X", s);
		}catch (Exception ex){
			InterfaceSession.log("SgtCartesianLayout: Could not set X axis.", 
								 LoggingType.Errors);
			InterfaceSession.handleException(ex);
			}
		*/
	}
	
	/**************************************************
	 * Sets this layout's Y range from <code>range</code>.
	 * 
	 * @param range
	 */
	public void setRangeY(SoTRange range){
		SgtAxis<PlainAxis> axis = getMainAxis(AxisType.Y);
		if (axis == null) return;
		axis.setRange(range);
		/*
		String s = "" + range.getStart() + " " + range.getEnd() + " " + range.getDelta();
		try{
			if (getBaseLayer() != null && getBaseLayer().getGraph() != null){
				CartesianGraph graph = (CartesianGraph)getBaseLayer().getGraph();
				Axis axis = graph.getYAxis(LEFT_AXIS);
				range = Graph.computeRange(range, auto_interval_y);
				axis.setRangeU(range);
				}
			attributes.setValue("Range Y", s);
		}catch (Exception ex){
			InterfaceSession.log("SgtCartesianLayout: Could not set Y axis.", 
								 LoggingType.Errors);
			InterfaceSession.handleException(ex);
			}
		*/
	}
	
	/*****************************************
	 * Returns the label for the X axis.
	 * 
	 * @return
	 */
	public String getLabelX(){
		SgtAxis<PlainAxis> axis = getMainAxis(AxisType.X);
		if (axis == null) return null;
		return axis.getLabel();
		//return (String)attributes.getValue("Label X");
	}
	
	/*****************************************
	 * Returns the label for the Y axis.
	 * 
	 * @return
	 */
	public String getLabelY(){
		SgtAxis<PlainAxis> axis = getMainAxis(AxisType.Y);
		if (axis == null) return null;
		return axis.getLabel();
		//return (String)attributes.getValue("Label Y");
	}
	
	/**************************************************
	 * Set the label for the X axis.
	 * 
	 * @param label
	 */
	public void setLabelX(String label){
		SgtAxis<PlainAxis> axis = getMainAxis(AxisType.X);
		if (axis == null) return;
		axis.setLabel(label);
		//attributes.setValue("Label X", label);
	}
	
	/**************************************************
	 * Set the label for the Y axis.
	 * 
	 * @param label
	 */
	public void setLabelY(String label){
		SgtAxis<PlainAxis> axis = getMainAxis(AxisType.Y);
		if (axis == null) return;
		axis.setLabel(label);
		//attributes.setValue("Label Y", label);
	}
	
	/***************************************************
	 * Initiates this plot's range based upon its first data object. 
	 * 
	 * @param object
	 */
	protected void initRange(T object) {
		SGTData data = object.getSgtData();
		SoTRange x_range = null;
		SoTRange y_range = null;
		try{
			x_range = data.getXRange();
			y_range = data.getYRange();
		}catch (Exception ex){
			// Range for some objects, e.g., PointCollection, can cause NullPointerExceptions 
			x_range = new SoTRange.Double(0,1);
			y_range = new SoTRange.Double(0,1);
			}
		SoTRange xn_range = null;
		SoTRange yn_range = null;
		
		if (data.getXMetaData() != null)
			rev_axis_x = data.getXMetaData().isReversed();
		if (data.getYMetaData() != null)
			rev_axis_y = data.getYMetaData().isReversed();
 
		boolean data_good = !(x_range.isStartOrEndMissing() ||
                			  y_range.isStartOrEndMissing());

		if(data_good) {
		    //
			// flip range if data_good and flipped
			//
			if(rev_axis_x) {
			  	x_range.flipStartAndEnd();
				}
			if(rev_axis_x) {
				y_range.flipStartAndEnd();
				}
			
			if(isAutoRangeX()) {
	            xn_range = Graph.computeRange(x_range, auto_interval_x);
	        }else{
	            xn_range = getRangeX();
	            }
			
			if(isAutoRangeY()) {
	            yn_range = Graph.computeRange(y_range, auto_interval_y);
	        }else{
	            yn_range = getRangeY();
	            }
			
			this.setRangeX(xn_range);
			this.setRangeY(yn_range);
			
			}
		
	}
	
	/***************************************************
	 * Updates this plot's range based upon an added data object. 
	 * 
	 * @param object
	 */
	protected void updateRange(T object) {
		
		SGTData data = object.getSgtData();
		
		if (this.isAutoRangeX()){
			SoTRange obj_range = data.getXRange();
			SoTRange x_range = getRangeX();
			x_range.add(obj_range);
			setRangeX(x_range);
			}
		
		if (this.isAutoRangeY()){
			SoTRange obj_range = data.getYRange();
			SoTRange y_range = getRangeY();
			y_range.add(obj_range);
			setRangeY(y_range);
			}
		
	}
	
	/*******************************************************
	 * Updates this plot's range if autorange is set, and repaints the layout. 
	 * 
	 */
	protected void updateRanges(){
		
		if (this.getBaseLayer() == null) return;
		CartesianGraph graph = (CartesianGraph)getBaseLayer().getGraph();
		if (graph == null) return;
		
		/*
		Axis axis_x = null;
		Axis axis_y = null;
		
		try{
			axis_x = graph.getXAxis(BOTTOM_AXIS);
			axis_y = graph.getYAxis(LEFT_AXIS);
		}catch (AxisNotFoundException e){
			InterfaceSession.log("SgtCartesianLayout: Axes not set.", 
								 LoggingType.Errors);
			}
			*/
		
		SgtAxis<PlainAxis> axis_x = this.getMainAxis(AxisType.X);
		 
		//is auto X/Y?
		if (axis_x != null && isAutoRangeX()){
			//get all ranges
			SoTRange range = null;
			ArrayList<String> objects = new ArrayList<String>(object_map.keySet());
			for (int i = 0; i < objects.size(); i++){
				if (i == 0) range = object_map.get(objects.get(i)).getSgtData().getXRange().copy();
				range.add(object_map.get(objects.get(i)).getSgtData().getXRange());
				}
			if (range != null){
				try{
					range = Graph.computeRange(range, auto_interval_x);
					axis_x.setRange(range, false);
					//String s = "" + range.getStart() + " " + range.getEnd() + " " + range.getDelta();
					//attributes.setValue("Range X", s, false);
				}catch (Exception ex){
					InterfaceSession.log("SgtCartesianLayout: Could not set X axis.", 
										 LoggingType.Errors);
					InterfaceSession.handleException(ex);
					}
				}
			}
		
		SgtAxis<PlainAxis> axis_y = getMainAxis(AxisType.Y);
		
		if (axis_y != null && isAutoRangeY()){
			//get all ranges
			SoTRange range = null;
			ArrayList<String> objects = new ArrayList<String>(object_map.keySet());
			for (int i = 0; i < objects.size(); i++){
				if (i == 0) range = object_map.get(objects.get(i)).getSgtData().getYRange().copy();
				range.add(object_map.get(objects.get(i)).getSgtData().getYRange());
				}
			if (range != null){
				try{
					range = Graph.computeRange(range, auto_interval_x);
					axis_y.setRange(range, false);
					//String s = "" + range.getStart() + " " + range.getEnd() + " " + range.getDelta();
					//attributes.setValue("Range Y", s, false);
				}catch (Exception ex){
					InterfaceSession.log("SgtCartesianLayout: Could not set Y axis.", 
										 LoggingType.Errors);
					InterfaceSession.handleException(ex);
					}
				}
			}
		
		//Set transforms and locations
        updateTransforms();
        
        //Set axes only for base graph
        SoTRange range_x = getRangeX();
        SoTRange range_y = getRangeY();
        
        //SoTPoint origin = new SoTPoint(range_x.getStart(), range_y.getStart());
        
        //SoTPoint origin = new SoTPoint(); //axis_y.getOrigin(), axis_x.getOrigin());
        
        String pos_x = axis_x.getPosition();
        String pos_y = axis_y.getPosition();
        
        SoTValue origin_y = null;
        if (pos_x.equals("Bottom"))
        	origin_y = range_y.getStart();
        else
        	origin_y = range_y.getEnd();
        
        SoTValue origin_x = null;
        if (pos_y.equals("Left"))
        	origin_x = range_x.getStart();
        else
        	origin_x = range_x.getEnd();
        
        SoTPoint origin = new SoTPoint(origin_x, origin_y);
        
        axis_x.setLocation(origin);
        axis_y.setLocation(origin);
		
		this.repaint();
	}
	
	/************************************************
	 * Updates all graph transforms to fit the axes.
	 * 
	 */
	protected void updateTransforms(){
		
		SoTRange range_x = getRangeX();
        SoTRange range_y = getRangeY();
        
        LinearTransform xt = new LinearTransform(new Range2D(getMinX(), getMaxX()), range_x);
        LinearTransform yt = new LinearTransform(new Range2D(getMinY(), getMaxY()), range_y);
        
        //Set transforms for all graphs
        ArrayList<String> names = new ArrayList<String>(graph_map.keySet());
        for (int i = 0; i < names.size(); i++){
        	CartesianGraph this_graph = (CartesianGraph)graph_map.get(names.get(i));
        	this_graph.setXTransform(xt);
        	this_graph.setYTransform(yt);
	        }
        
        //origin?
       
	}
	
	@Override
	protected CartesianGraph initPlot(T object){
		
		SGTData data = object.getSgtData();
		
		String name_x = "X", name_y = "Y";
		if (data.getXMetaData() != null)
			name_x = data.getXMetaData().getName();
		if (data.getYMetaData() != null)
			name_y = data.getYMetaData().getName();
		
		try{
			CartesianGraph c_graph = new CartesianGraph(object.getName());
			base_x = BASE_X; 
			PlainAxis axis_x = new PlainAxis(base_x);
			SgtAxis<PlainAxis> s_axis_x = new SgtAxis<PlainAxis>(base_x, axis_x, AxisType.X);
			s_axis_x.setLabel(name_x + " (" + object.getUnitX().getShortName() + ")");
			//bottom.setRangeU(getRangeX());
			c_graph.addXAxis(axis_x);
			this.axis_map.put(base_x, s_axis_x);
			s_axis_x.attributes.addAttributeListener(this);
			
			base_y = BASE_Y; 
			PlainAxis axis_y = new PlainAxis(base_y);
			SgtAxis<PlainAxis> s_axis_y = new SgtAxis<PlainAxis>(base_y, axis_y, AxisType.Y);
			s_axis_y.setLabel(name_y + " (" + object.getUnitY().getShortName() + ")");
			c_graph.addYAxis(axis_y);
			this.axis_map.put(base_y, s_axis_y);
			s_axis_y.attributes.addAttributeListener(this);
			
			double ord = ((MguiFloat)attributes.getValue("Ordinates Size")).getValue();
			s_axis_x.getAxis().setLabelHeightP(ord);
			s_axis_y.getAxis().setLabelHeightP(ord);
			
			c_graph.setData(object.getSgtData(), object.getSgtAttribute());
		       
		    return c_graph;
			
	     } catch (Exception e) {
	    	InterfaceSession.log("SgtLineLayout: Error initiating graph: " + e.getLocalizedMessage(), 
	    						 LoggingType.Errors);
	    	InterfaceSession.handleException(e);
	    	return null;
	     	}
	     
	}
	
	@Override
	protected CartesianGraph getGraph(String layer_name) {
		Layer layer = this.getLayer(layer_name);
		return (CartesianGraph)layer.getGraph();
	}
	
	@Override
	protected void addGraph(T object) throws PlotException{
		
		if (graph_map.containsKey(object.getName()))
			throw new PlotException("Object with key '" + object + "' already exists.");
		
		CartesianGraph graph = null;
		
		if (graph_map.size() == 0){
			initRange(object);
			graph = initPlot(object);
			if (graph == null)
				throw new PlotException("Could not create graph for object '" + object.getName() + ".");
			base_layer = object.getName();
			addGraph(base_layer, graph);
			
			updateAxes();
		}else{
			updateRange(object);
			graph = new CartesianGraph(object.getName());
			graph.setData(object.getSgtData(), object.getSgtAttribute());
			CartesianGraph base_graph = (CartesianGraph)getBaseLayer().getGraph();
			graph.setXTransform(base_graph.getXTransform());
			graph.setYTransform(base_graph.getYTransform());
			addGraph(object.getName(), graph);
			updateAxes();
			
			}
		
	}
	
	protected void addGraph(String label, CartesianGraph graph) throws PlotException{
		
		Layer layer = new Layer(label, new Dimension2D(getSizeX(), getSizeY()));
		layer.setGraph(graph);
        addLayer(label, layer);
		
        graph_map.put(label, graph);
	}
	
}