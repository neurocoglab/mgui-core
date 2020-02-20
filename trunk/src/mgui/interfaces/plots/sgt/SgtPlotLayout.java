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

import gov.noaa.pmel.sgt.Axis;
import gov.noaa.pmel.sgt.DataKey;
import gov.noaa.pmel.sgt.DataNotFoundException;
import gov.noaa.pmel.sgt.Graph;
import gov.noaa.pmel.sgt.JPane;
import gov.noaa.pmel.sgt.Layer;
import gov.noaa.pmel.sgt.PlainAxis;
import gov.noaa.pmel.sgt.StackedLayout;
import gov.noaa.pmel.sgt.dm.Collection;
import gov.noaa.pmel.sgt.dm.SGTData;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.plots.PlotException;
import mgui.interfaces.plots.sgt.SgtPlotFunctions.AxisType;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.util.InterfaceFunctions;
import mgui.numbers.MguiFloat;

/***********************************************************
 * The base layout panel for an SGT plot.
 * 
 * <p>The initiation process will typically call (in order):
 * 
 * <ol>
 * <li><code>initRange</code> to initialize this layout's range from an initial plot object
 * <li><code>initGraph</code> to initialize this layout's graph object wrt. ranges, labels, titles, etc.
 * </ol>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class SgtPlotLayout<T extends SgtPlotObject<?>, G extends Graph, A extends Axis> 
																				 extends JPane
																			  	 implements PropertyChangeListener,
																			  			 	SgtPlotListener,
																			  			 	InterfaceObject,
																			  			 	AttributeListener{
	
	protected boolean is_destroyed = false;
	protected ArrayList<InterfaceTreeNode> treeNodes = new ArrayList<InterfaceTreeNode>();
	protected DataKey object_key;
	
	//protected Graph graph;
	protected GraphType graph_type = GraphType.Cartesian;
	//protected PlotType plot_type = PlotType.Line;
	protected HashMap<String,Layer> layers = new HashMap<String,Layer>();
	protected HashMap<SGTData,gov.noaa.pmel.sgt.Attribute> data_attributes = 
												new HashMap<SGTData,gov.noaa.pmel.sgt.Attribute>();
	protected HashMap<String,Graph> graph_map = new HashMap<String,Graph>();
	protected HashMap<String,SgtPlotObject<?>> object_map = new HashMap<String,SgtPlotObject<?>>();
	protected HashMap<String,SgtAxis<A>> axis_map = new HashMap<String,SgtAxis<A>>();
	protected Collection data_collection = new Collection();
	
	protected boolean auto_range_x = true, auto_range_y = true;
	protected int auto_interval_x = 10, auto_interval_y = 10;
	protected boolean rev_axis_x = false, rev_axis_y = false;
	
	protected AttributeList attributes = new AttributeList();
	
	protected String base_layer;
	
	public enum GraphType{
		Cartesian,
		Polar,
		Map;
	}
	
	@Override
	public void clean(){
		InterfaceFunctions.cleanInterfaceObject(this);
	}
	
	public SgtPlotLayout(GraphType graph_type){
		this("", new Dimension(10,10), GraphType.Cartesian);
	}
	
	public SgtPlotLayout(String title, Dimension size, GraphType type){
		super(title, size);
		this.graph_type = type;
		this.setOpaque(true);
		//this.plot_type = plot_type;
		_init();
	}
	
	public Graph getPlot(){
		return getGraph("Graph");
	}
	
	public ArrayList<String> getLayerNames(){
		return new ArrayList<String>(layers.keySet());
	}
	
	/*******************************************
	 * Returns the layer associated with the specified name.
	 * 
	 * @param name
	 * @return
	 */
	public Layer getLayer(String name){
		return layers.get(name);
	}
	
	/*********************************************
	 * Returns the base layer; i.e., containing axes + labels.
	 * 
	 * @return
	 */
	public Layer getBaseLayer(){
		if (base_layer != null)
			return layers.get(base_layer);
		return null;
	}
	
	/**********************************************
	 * Adds a layer with the specified name to this layout.
	 * 
	 * @param name
	 * @param layer
	 * @throws PlotException
	 */
	public void addLayer(String name, Layer layer) throws PlotException{
		if (layers.containsKey(name))
			throw new PlotException("Layer '" + name + "' already exists!");
		layers.put(name, layer);
		layer.setPane(this);
		this.add(layer, -1);
	}
	
	/************************************************
	 * Removes the layer corresponding the the specified name from this layout.
	 * 
	 * @param name
	 */
	public void removeLayer(String name){
		Layer layer = layers.get(name);
		if (layer == null) return;
		
		this.remove(layer);
		layers.remove(name);
	}
	
	/*************************************************
	 * Adds an axis to this plot.
	 * 
	 * @param name
	 * @param axis
	 * @return the created {@link SgtAxis}
	 * @throws PlotException
	 */
	public SgtAxis<A> addAxis(String name, A axis, AxisType type) throws PlotException{
		
		if (axis_map.containsKey(name))
			throw new PlotException("Layout already has axis with name '" + name + "'.");
		
		if (getBaseLayer() == null)
			throw new PlotException("Must initialize the plot before adding an axis.");
		
		SgtAxis<A> s_axis = new SgtAxis<A>(name, axis, type);
		
		axis_map.put(name, s_axis);
		s_axis.attributes.addAttributeListener(this);
		
		return s_axis;
		
	}
	
	/**********************************************
	 * Removes the axis with the specified name from this layout.
	 * 
	 * @param name
	 */
	public void removeAxis(String name){
		
		SgtAxis<A> s_axis = axis_map.get(name);
		if (s_axis == null) return;
		
		axis_map.remove(name);
		
		s_axis.attributes.removeAttributeListener(this);
	}
	
	/***********************************************
	 * Returns the axis with the specified name; <code>null</code> if no such
	 * axis exists.
	 * 
	 * @param name
	 * @return
	 */
	public SgtAxis<A> getAxis(String name){
		return axis_map.get(name);
	}
	
	/***********************************************
	 * Returns the main axis of the given {@link AxisType}. 
	 * 
	 * @param type
	 * @return
	 */
	public abstract SgtAxis<PlainAxis> getMainAxis(AxisType type);
	
	private void _init(){
		MouseListener[] listeners = this.getMouseListeners();
		for (int i = 0; i < listeners.length; i++)
			this.removeMouseListener(listeners[i]);
		
		attributes.add(new Attribute<Font>("Title1 Font", new Font("Arial", Font.PLAIN, 16)));
		attributes.add(new Attribute<Color>("Title1 Colour", Color.black));
		attributes.add(new Attribute<String>("Title1", "SGT Plot"));
		attributes.add(new Attribute<MguiFloat>("Title1 Size", new MguiFloat(0.05f)));
		attributes.add(new Attribute<Font>("Title2 Font", new Font("Arial", Font.PLAIN, 13)));
		attributes.add(new Attribute<Color>("Title2 Colour", Color.black));
		attributes.add(new Attribute<String>("Title2", ""));
		attributes.add(new Attribute<MguiFloat>("Title2 Size", new MguiFloat(0.05f)));
		
		attributes.add(new Attribute<Font>("Ordinates Font", new Font("Arial", Font.PLAIN, 12)));
		attributes.add(new Attribute<Color>("Ordinates Colour", Color.black));
		attributes.add(new Attribute<MguiFloat>("Ordinates Size", new MguiFloat(0.05f)));
		
		attributes.addAttributeListener(this);
		
		this.setBatch(true);
		this.setLayout(new StackedLayout());
		
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e) {
		
		
		
		
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	public ArrayList<String> getMapKeys(){
		return new ArrayList<String>(graph_map.keySet());
	}
	
	
	
	/***************************************************
	 * Add a data object to this plot, and if autoranges are set, update ranges accordingly.
	 * 
	 * <p>If this is the first data object, X and Y axes and transforms are created and 
	 * added to the graph; for all additional objects, the transforms will be linked from
	 * the first object layer, and no axes will be created.   
	 * 
	 * @param xy_data
	 * @throws PlotException If data's ID already exists. Existing data object must be removed first.
	 */
	public void addData(T object) throws PlotException {
		
		SGTData data = object.getSgtData();
		
		if (graph_map.containsKey(data.getId()))
			throw new PlotException("Data object '" + data.getId() + "' already exists!");
		
		addGraph(object);
		object_map.put(object.getName(), object);
		
		this.setModified(true, "Sgt object added");
	}
	
	/*******************************************************
	 * Removes all graph data from this layout.
	 * 
	 */
	public void clearData(){
		
		ArrayList<String> keys = new ArrayList<String>(graph_map.keySet());
		for (int i = 0; i < keys.size(); i++){
			this.removeLayer(keys.get(i));
			}
		
		keys.clear();
	}
	
	@Override
	public void plotObjectAdded(SgtPlotEvent e) {
		try{
			addData((T)e.getPlot().getLastAdded());
		}catch (PlotException ex){
			InterfaceSession.log(ex.getMessage(), LoggingType.Errors);
			}
	}

	@Override
	public void plotObjectModified(SgtPlotEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void plotObjectRemoved(SgtPlotEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	/***************************************************
	 * Initiates this plot's range based upon its first data object. 
	 * 
	 * @param object
	 */
	protected abstract void initRange(T object);
	
	/***************************************************
	 * Updates this plot's range based upon an added data object. 
	 * 
	 * @param object
	 */
	protected abstract void updateRange(T object);
	
	/***************************************************
	 * Returns the graph for the given layer.
	 * 
	 * @param layer
	 * @return
	 */
	protected abstract G getGraph(String layer);
	
	/***************************************************
	 * Creates a graph for <code>object</code> and adds it to its own layer.
	 * 
	 * @param object
	 */
	protected abstract void addGraph(T object) throws PlotException;
	
	/***************************************************
	 * Initiates the plot using the given initial data object. This function should set up axes,
	 * transforms, and ranges for the graph object.
	 * 
	 * @param layer
	 * @param attributes attributes for the plot
	 * @return the graph created for this object
	 */
	protected abstract G initPlot(T object) throws PlotException;
	
	/***********************************************
	 * Sets the base graph for this plot.
	 * 
	 * @param g
	 */
	//protected void setGraph(Graph g){
	//	this.getLayer(GRAPH_LAYER).setGraph(g);
	//}
	
	/***********************************************
	 * Set attributes for the given data object.
	 * 
	 * @param data
	 * @param attr
	 */
	public void addAttribute(SGTData data, gov.noaa.pmel.sgt.Attribute attr) {
		data_attributes.put(data, attr);
	}
	
	/*************************************************
	 * Returns the attributes for the given data object.
	 * 
	 * @param data
	 * @return
	 * @throws DataNotFoundException
	 */
	public gov.noaa.pmel.sgt.Attribute getAttribute(SGTData data) throws DataNotFoundException {
		gov.noaa.pmel.sgt.Attribute attr = (gov.noaa.pmel.sgt.Attribute)data_attributes.get(data);
	    if(attr == null) {
	    	throw new DataNotFoundException();
	    	}
	    return attr;
	}
	
	/*************************************************
	 * Returns the attributes for the data object with the given name.
	 * 
	 * @param xy_data
	 * @return
	 * @throws DataNotFoundException
	 */
	public gov.noaa.pmel.sgt.Attribute getAttribute(String name) throws DataNotFoundException {
		ArrayList<SGTData> list = new ArrayList<SGTData>(data_attributes.keySet());
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).getId().equals(name))
				return getAttribute(list.get(i));
		throw new DataNotFoundException();
	}
	
	@Override
	public void addMouseListener(MouseListener l){
		super.addMouseListener(l);
	}
	
	@Override
	public String getTreeLabel() {
		return "Layout";
	}

	@Override
	public InterfaceTreeNode issueTreeNode() {
		InterfaceTreeNode treeNode = new InterfaceTreeNode(this);
		setTreeNode(treeNode);
		treeNodes.add(treeNode);
		return treeNode;
	}

	@Override
	public void setTreeNode(InterfaceTreeNode node) {
		
		node.addChild(attributes.issueTreeNode());
		
		ArrayList<String> names = new ArrayList<String>(axis_map.keySet());
		
		for (int i = 0; i < names.size(); i++)
			node.addChild(getAxis(names.get(i)).issueTreeNode());
		
	}
	
	@Override
	public void destroy() {
		is_destroyed = true;
		ArrayList<InterfaceTreeNode> nodes = new ArrayList<InterfaceTreeNode>(treeNodes);
		for (int i = 0; i < nodes.size(); i++){
			nodes.get(i).destroy();
			if (nodes.get(i).getParent() != null)
				nodes.get(i).removeFromParent();
			treeNodes.remove(nodes.get(i));
			}
		treeNodes = null;
	}

	@Override
	public boolean isDestroyed() {
		return is_destroyed;
	}

	
}