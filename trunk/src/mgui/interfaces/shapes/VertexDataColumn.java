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

package mgui.interfaces.shapes;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import mgui.datasources.DataType;
import mgui.datasources.DataTypes;
import mgui.datasources.LinkedDataStream;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.shapes.VertexDataColumnEvent.EventType;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.domestic.attributes.AttributeXMLHandler;
import mgui.io.domestic.maps.ColourMapXMLHandler;
import mgui.io.domestic.maps.NameMapXMLHandler;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.io.util.IoFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiLong;
import mgui.numbers.MguiNumber;
import mgui.numbers.MguiShort;
import mgui.numbers.NumberFunctions;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Base64;

/********************************************
 * Encapsulates a column of vertex-wise numerical data. Allows this data column to be associated with
 * a {@link NameMap}, a {@link ColourMap}, and a list of {@link LinkedDataStream}s.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VertexDataColumn extends AbstractInterfaceObject implements Comparable<VertexDataColumn>,
																		 AttributeListener,
																		 IconObject,
																		 XMLObject,
																		 PopupMenuObject{

	protected ArrayList<MguiNumber> data = new ArrayList<MguiNumber>();
	protected AttributeList attributes = new AttributeList();
	protected HashMap<String, LinkedDataStream<?>> linked_data = new HashMap<String, LinkedDataStream<?>>();
	protected HashMap<String, Boolean> link_name_mapped = new HashMap<String, Boolean>();
	protected TreeSet<VertexDataColumnListener> listeners = new TreeSet<VertexDataColumnListener>();
	
	public VertexDataColumn(String name){
		init();
		setName(name);
	}
	
	public VertexDataColumn(String name, ArrayList<MguiNumber> data){
		this.data = data;
		this.updateDataLimits(false);
		init();
		setName(name);
	}
	
	private void init(){
		attributes.add(new Attribute<String>("Name", "no-name", true, false));
		attributes.add(new Attribute<MguiBoolean>("ShowLabels2D", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiBoolean>("ShowLabels3D", new MguiBoolean(false)));
		attributes.add(new Attribute<Font>("LabelFont", new Font("Courier", Font.PLAIN, 10)));
		attributes.add(new Attribute<Color>("LabelBackColour", Color.white));
		attributes.add(new Attribute<Color>("LabelColour", Color.blue));
		attributes.add(new Attribute<MguiDouble>("DataMin", new MguiDouble(0), true, false));
		attributes.add(new Attribute<MguiDouble>("DataMax", new MguiDouble(1), true, false));
		attributes.add(new Attribute<MguiDouble>("ColourMin", new MguiDouble(0)));
		attributes.add(new Attribute<MguiDouble>("ColourMax", new MguiDouble(1)));
		ArrayList<String> style = new ArrayList<String> ();
		style.add("All vertices");
		style.add("ROIs");
		AttributeSelection<?> list = new AttributeSelection<String>("Label2DStyle", style, String.class);
		attributes.add(list);
		style = new ArrayList<String>();
		style.add("All billboard");
		style.add("All flat");
		style.add("ROIs billboard");
		style.add("ROIs flat");
		list = new AttributeSelection<String>("Label3DStyle", style, String.class);
		attributes.add(list);
		list = new AttributeSelection<NameMap>("NameMap", InterfaceEnvironment.getNameMaps(), NameMap.class);
		list.allowUnlisted(true);
		attributes.add(list);
		
		list = new AttributeSelection<ColourMap>("ColourMap", InterfaceEnvironment.getColourMaps(), ColourMap.class);
		attributes.add(list);
		
		attributes.addAttributeListener(this);
	}
	
	/************************************
	 * Copies the attributes of {@code source_column} to this column.
	 * 
	 * @param source_column
	 * @return
	 */
	public boolean copyAttributes( VertexDataColumn source_column ) {
		
		AttributeList source_attributes = source_column.getAttributes();
		ArrayList<Attribute<?>> to_copy = new ArrayList<Attribute<?>>();
		
		for (Attribute<?> attribute : source_attributes.getAsList()) {
			if (attribute.isCopiable()) {
				to_copy.add(attribute);
				}
			}
		
		if (to_copy.size() > 0) {
			this.attributes.setIntersection(to_copy, true);
			}
		
		return true;
	}
	
	public AttributeList getAttributes() {
		return this.attributes;
	}
	
	/**********************************************************
	 * Updates this column's data limits based upon its current data
	 * 
	 * @param update
	 */
	public void updateDataLimits(){
		updateDataLimits(false);
	}
	
	/**********************************************************
	 * Updates this column's data limits based upon its current data
	 * 
	 * @param update If true, fires an AttributeEvent after limits are updated
	 */
	public void updateDataLimits(boolean update){
		if (data == null) return;
		double min = Double.MAX_VALUE;
		double max = -Double.MAX_VALUE;
		double v;
		for (int i = 0; i < data.size(); i++){
			v = data.get(i).getValue();
			min = Math.min(min, v);
			max = Math.max(max, v);
			}
		this.setDataLimits(min, max, update);	
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/vector_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/vector_20.png");
		return null;
	}
	
	public static Icon getIcon(){
		return (new VertexDataColumn("")).getObjectIcon();
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		
		Attribute<?> attribute = e.getAttribute();
		
		if (attribute.getName().equals("ColourMap") ||
				attribute.getName().startsWith("ColourM")){
			this.fireDataColumnColourMapChanged(new VertexDataColumnEvent(this, EventType.ColumnChanged));
			return;
			}
		
		if (attribute.getName().equals("Name")){
			this.fireDataColumnChanged(new VertexDataColumnEvent(this, EventType.NameChanged));
			return;
		}
		
	}
	
	/************************************************
	 * Set this data column to match the values, limits, and colour map of {@code column}. 
	 * 
	 * @param column
	 */
	public void setFromVertexDataColumn(VertexDataColumn column){
		this.setValues(column.getData(), true);
		this.setColourMap(column.getColourMap(), false);
		this.setDataLimits(column.getDataMin(), column.getDataMax(), true);
		this.setColourLimits(column.getColourMin(), column.getColourMax(), true);
	}
	
	/************************************************
	 * Returns the data minimum for this column
	 * 
	 * @return
	 */
	public double getDataMin(){
		return ((MguiDouble)attributes.getValue("DataMin")).getValue();
	}
	
	/************************************************
	 * Sets the data minimum for this column, and fires an event.
	 * 
	 * @param min
	 */
	public void setDataMin(double min){
		setDataMin(min, true);
	}
	
	/************************************************
	 * Sets the data minimum for this column; fires an event if {@code update} is {@code true}.
	 * 
	 * @param max
	 * @param update
	 */
	public void setDataMin(double min, boolean update){
		if (min > getDataMax()) return;
		attributes.setValue("DataMin", new MguiDouble(min), update);
	}
	
	/************************************************
	 * Sets the data limits for this column; fires an event if {@code update} is {@code true}.
	 * 
	 * @param max
	 * @param update
	 */
	public void setDataLimits(double min, double max){
		setDataLimits(min, max, true);
	}
	
	/************************************************
	 * Sets the data limits for this column; fires an event if {@code update} is {@code true}.
	 * 
	 * @param max
	 * @param update
	 */
	public void setDataLimits(double min, double max, boolean update){
		if (min > max) return;
		attributes.setValue("DataMin", new MguiDouble(min), false);
		attributes.setValue("DataMax", new MguiDouble(max), update);
	}
	
	/************************************************
	 * Sets the data limits for this column; fires an event if {@code update} is {@code true}.
	 * 
	 * @param max
	 * @param update
	 */
	public void setColourLimits(double min, double max){
		setColourLimits(min, max, true);
	}
	
	/************************************************
	 * Sets the colour limits for this column; fires an event if {@code update} is {@code true}.
	 * 
	 * @param max
	 * @param update
	 */
	public void setColourLimits(double min, double max, boolean update){
		if (min > max) return;
//		getColourMap().mapMin = min;
//		getColourMap().mapMax = max;
		attributes.setValue("ColourMin", new MguiDouble(min), false);
		attributes.setValue("ColourMax", new MguiDouble(max), update);
		
	}
	
	/************************************************
	 * Returns the data maximum for this column
	 * 
	 * @return
	 */
	public double getDataMax(){
		return ((MguiDouble)attributes.getValue("DataMax")).getValue();
	}
	
	/************************************************
	 * Sets the data maximum for this column, and fires an event.
	 * 
	 * @param max
	 */
	public void setDataMax(double max){
		setDataMax(max, true);
	}
	
	/************************************************
	 * Sets the data maximum for this column; fires an event if {@code update} is {@code true}.
	 * 
	 * @param max
	 * @param update
	 */
	public void setDataMax(double max, boolean update){
		if (max < getDataMin()) return;
		attributes.setValue("DataMax", new MguiDouble(max), update);
	}
	
	/************************************************
	 * Returns the data maximum for this column
	 * 
	 * @return
	 */
	public double getColourMax(){
		return ((MguiDouble)attributes.getValue("ColourMax")).getValue();
	}
	
	/************************************************
	 * Sets the data maximum for this column, and fires an event.
	 * 
	 * @param max
	 */
	public void setColourMax(double max){
		setColourMax(max, true);
	}
	
	/************************************************
	 * Sets the data maximum for this column; fires an event if {@code update} is {@code true}.
	 * 
	 * @param max
	 * @param update
	 */
	public void setColourMax(double max, boolean update){
		if (max < getDataMin()){
			InterfaceSession.log("VertexDataColumn.setColourMin: min >  max", LoggingType.Errors);
			return;
			}
		attributes.setValue("ColourMax", new MguiDouble(max), update);
	}
	
	/************************************************
	 * Returns the data maximum for this column
	 * 
	 * @return
	 */
	public double getColourMin(){
		return ((MguiDouble)attributes.getValue("ColourMin")).getValue();
	}
	
	/************************************************
	 * Sets the data maximum for this column, and fires an event.
	 * 
	 * @param max
	 */
	public void setColourMin(double min){
		setColourMin(min, true);
	}
	
	/************************************************
	 * Sets the data maximum for this column; fires an event if {@code update} is {@code true}.
	 * 
	 * @param max
	 * @param update
	 */
	public void setColourMin(double min, boolean update){
		if (min > getDataMax()){
			InterfaceSession.log("VertexDataColumn.setColourMin: min >  max", LoggingType.Errors);
			return; // Should throw exception
			}
		attributes.setValue("ColourMin", new MguiDouble(min), update);
	}
	
	public void addListener(VertexDataColumnListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(VertexDataColumnListener listener){
		listeners.remove(listener);
	}
	
	@Override
	public int compareTo(VertexDataColumn column) {
		return getName().compareTo(column.getName());
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	/**************************************************
	 * Returns {@code true} if this column has an associated name map
	 * 
	 * @return
	 */
	public boolean hasNameMap(){
		return getNameMap() != null;
	}
	
	/**************************************************
	 * Returns the associated name map, or {@code null} if none exists.
	 * 
	 * @return
	 */
	public NameMap getNameMap(){
		AttributeSelection<NameMap> selection = (AttributeSelection<NameMap>)attributes.getAttribute("NameMap");
		return selection.getValue();
	}
	
	/**************************************************
	 * Associates {@code map} with this column. 
	 * 
	 * @param map
	 */
	public void setNameMap(NameMap name_map){
		AttributeSelection<NameMap> selection = (AttributeSelection<NameMap>)attributes.getAttribute("NameMap");
		selection.setValue(name_map);
	}
	
	/******************************************************
	 * Returns the colour map associated with this column, or {@code null} if there
	 * is no associated colour map
	 * 
	 * @return
	 */
	public ColourMap getColourMap(){
		AttributeSelection<ColourMap> selection = (AttributeSelection<ColourMap>)attributes.getAttribute("ColourMap");
		ColourMap cmap = selection.getValue();
		return cmap;
	}
	
	/*******************************************************
	 * Sets the colour map associated with this column. Sets data min and data max to
	 * the limits of the colour map
	 * 
	 * @param map
	 */
	public void setColourMap(ColourMap map){
		setColourMap(map, true);
	}
	
	/*******************************************************
	 * Sets the colour map associated with this column. Sets data min and data max to
	 * the limits of the colour map
	 * 
	 * @param map
	 * @param colour_min Sets the colour minimum
	 * @param colour_min Sets the colour maximum
	 * @param update Whether to fire an event to this column's listeners
	 */
	public void setColourMap(ColourMap map, double colour_min, double colour_max, boolean update){
		setColourMin(colour_min, false);
		setColourMax(colour_max, false);
		setColourMap(map, update);
	}
	
	/*******************************************************
	 * Sets the colour map associated with this column. Sets data min and data max to
	 * the limits of the colour map
	 * 
	 * @param map
	 * @param update Whether to fire an event to this column's listeners
	 */
	public void setColourMap(ColourMap map, boolean update){
		AttributeSelection<ColourMap> selection = (AttributeSelection<ColourMap>)attributes.getAttribute("ColourMap");
		selection.setValue(map, false);
		if (update)
			fireDataColumnColourMapChanged(new VertexDataColumnEvent(this, VertexDataColumnEvent.EventType.ColourMapChanged));
	}
	
	/*******************************************************
	 * Returns the value in this column for the vertex at {@code index}, as a {@code double}
	 * 
	 * @param index
	 * @return
	 */
	public double getDoubleValueAtVertex(int index){
		return data.get(index).getValue();
	}
	
	/******************************************************
	 * Returns the value in this column for the vertex at {@code index}, as an {link MguiDouble}
	 * 
	 * @param index
	 * @return The value, or {@code null} if the index is out of bounds
	 */
	public MguiNumber getValueAtVertex(int index){
		if (index < 0 || index >= data.size()) 
			return null;
		return data.get(index);
	}
	
	/*****************************************************
	 * Returns the vertex-wise data as type {@code double}
	 * 
	 * @return
	 */
	public double[] getDataAsDouble(){
		if (data == null) return null;
		double[] values = new double[data.size()];
		for (int i = 0; i < data.size(); i++)
			values[i] = data.get(i).getValue();
		return values;
	}
	
	/******************************************************
	 * Sets the values for this data column.
	 * 
	 * @param values
	 * @return
	 */
	public boolean setValues(ArrayList<MguiNumber> values){
		return setValues(values, true);
	}
	
	/******************************************************
	 * Sets the values for this data column.
	 * 
	 * @param values 	New values for this column
	 * @param update 	Whether to fire this column's listeners
	 * @return
	 */
	public boolean setValues(ArrayList<MguiNumber> values, boolean update){
		return setValues(values, update, true);
	}
	
	/******************************************************
	 * Sets the values for this data column.
	 * 
	 * @param values 		New values for this column
	 * @param update 		Whether to fire this column's listeners
	 * @param check_size	Whether to check the data size first; should be true unless the caller
	 * 						wants to perform its own check
	 * @return
	 */
	public boolean setValues(ArrayList<MguiNumber> values, boolean update, boolean check_size){
		
		if (data != null && check_size && values.size() != data.size()){
			InterfaceSession.log("VertexDataColumn: Wrong number of elements (" + values.size() + ", expected " + data.size() + ").", 
								 LoggingType.Errors);
			return false;
			}
		if (data == null) data = new ArrayList<MguiNumber>();
		data.clear();
		data.addAll(values);
		
		resetDataLimits(update);
		
		if (update)
			fireDataColumnChanged(new VertexDataColumnEvent(this,EventType.ColumnChanged));
		return true;
	}
	
	protected void resetDataLimits(boolean update){
		if (data == null) return;
		double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
		for (int i = 0; i < data.size(); i++){
			min = Math.min(min, data.get(i).getValue());
			max = Math.max(max, data.get(i).getValue());
			}
		this.setDataLimits(min, max, update);
	}
	
	/******************************************************
	 * Sets the value in this column for the vertex at {@code index}
	 * 
	 * @param index
	 * @param value
	 */
	public void setDoubleValueAtVertex(int index, double value){
		data.get(index).setValue(value);
	}
	
	/******************************************************
	 * Sets the value in this column for the vertex at {@code index}
	 * 
	 * @param index
	 * @param value
	 */
	public void setValueAtVertex(int index, MguiNumber value){
		data.set(index, value);
	}
	
	/******************************************************
	 * Returns the data array underlying this column. This is not a copy, so changes to this array
	 * will affect the column.
	 * 
	 * @return
	 */
	public ArrayList<MguiNumber> getData(){
		return data;
	}
	
	/******************************************************
	 * Determines the data transfer type (as defined by {@linkplain DataBuffer}) for 
	 * this data column.
	 * 
	 * @return
	 */
	public int getDataTransferType(){
		MguiNumber n = this.getValueAtVertex(0);
		if (n == null) return -1;
		if (n instanceof MguiShort)
			return DataBuffer.TYPE_SHORT;
		if (n instanceof MguiInteger)
			return DataBuffer.TYPE_INT;
		if (n instanceof MguiFloat)
			return DataBuffer.TYPE_FLOAT;
		if (n instanceof MguiDouble)
			return DataBuffer.TYPE_DOUBLE;
		return -1;
	}
	
	/*****************************************************
	 * Adds a data link to this column.
	 * 
	 * @param name
	 * @param stream
	 */
	public void addDataLink(String name, LinkedDataStream<?> stream){
		addDataLink(name, stream, false);
	}
	
	/*******************************************************
	 * Adds a data link to this column.
	 * 
	 * @param name
	 * @param stream
	 * @param name_mapped 		If {@code true}, and this column has a name map,
	 *  						name map values will be used as keys instead of the
	 *  						numeric ones.
	 */
	public void addDataLink(String name, LinkedDataStream<?> stream, boolean name_mapped){
		linked_data.put(name, stream);
		name_mapped &=  hasNameMap();
		link_name_mapped.put(name, name_mapped);
		
		fireDataColumnChanged(new VertexDataColumnEvent(this, EventType.ColumnChanged));
	}
	
	/*************************************************
	 * Removes the data link specified by {@code name} from this column
	 * 
	 * @param name
	 */
	public void removeDataLink(String name){
		linked_data.remove(name);
		link_name_mapped.remove(name);
		fireDataColumnChanged(new VertexDataColumnEvent(this, EventType.ColumnChanged));
	}
	
	protected void fireDataColumnChanged(VertexDataColumnEvent event){
		ArrayList<VertexDataColumnListener> list = new ArrayList<VertexDataColumnListener>(listeners);
		for (int i = 0; i < list.size(); i++)
			list.get(i).vertexDataColumnChanged(event);
	}
	
	protected void fireDataColumnColourMapChanged(VertexDataColumnEvent event){
		ArrayList<VertexDataColumnListener> list = new ArrayList<VertexDataColumnListener>(listeners);
		for (int i = 0; i < list.size(); i++)
			list.get(i).vertexDataColumnColourMapChanged(event);
	}
	
	/************************************************
	 * Returns a list of names for all data linked to this column
	 * 
	 * @return
	 */
	public ArrayList<String> getLinkedDataNames(){
		ArrayList<String> names = new ArrayList<String>(linked_data.keySet());
		Collections.sort(names);
		return names;
	}
	
	/************************************************
	 * Returns a {@link LinkedDataStream} specified by {@code name}
	 * 
	 * @param name
	 * @return
	 */
	public LinkedDataStream<?> getLinkedData(String name){
		return linked_data.get(name);
	}
	
	/***********************************************
	 * Returns {@code true} if the data link specified by {@code name} has an
	 * associated name map.
	 * 
	 * @param name
	 * @return
	 */
	public boolean isNameMapped(String name){
		return link_name_mapped.get(name);
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		
		super.setTreeNode(treeNode);
		treeNode.add(attributes.issueTreeNode());
		
		//linked data
		ArrayList<String> link_names = this.getLinkedDataNames();
		for (int i = 0; i < link_names.size(); i++){
			treeNode.add(this.getLinkedData(link_names.get(i)).issueTreeNode());
			}
		
	}
	
	@Override
	public String getTreeLabel(){
		return getName();
	}
	
	// ********************** XML Stuff ***************************************
	
	
	@Override
	public String getDTD() {
		return null;
	}

	@Override
	public String getXMLSchema() {
		return null;
	}

	@Override
	public String getXML() {
		return getXML(0);
	}

	@Override
	public String getXML(int tab) {
		InterfaceSession.log("VertexDataColumn: getXML not implemented. Use writeXML instead.", 
				 			  LoggingType.Errors);
		return getShortXML(tab);
	}
	
	protected String xml_data_type;
	protected XMLEncoding xml_data_encoding;
	protected int xml_data_size = -1;
	protected String xml_data;
	protected String xml_current_block;
	protected String xml_current_cmap;
	protected ColourMapXMLHandler xml_cmap_handler;
	protected AttributeXMLHandler xml_attribute_handler;
	protected NameMapXMLHandler xml_nmap_handler;

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type)throws SAXException {
		
		if (xml_cmap_handler != null){
			// Redirect
			xml_cmap_handler.startElement(null, localName, "", attributes);
			return;
			}
		
		if (xml_nmap_handler != null){
			// Redirect
			xml_nmap_handler.startElement(null, localName, "", attributes);
			return;
			}
		
		if (xml_attribute_handler != null){
			// Redirect
			xml_attribute_handler.startElement(null, localName, "", attributes);
			return;
			}
		
		if (localName.equals("AttributeList")){
			xml_attribute_handler = new AttributeXMLHandler();
			xml_attribute_handler.startElement(null, localName, "", attributes);
			return;
			}
		
		if (localName.equals(this.getLocalName())){
			setName(attributes.getValue("name"));
			return;
			}
		
		// Start data loading here
		if (localName.equals("Data")){
			
			xml_data_type = attributes.getValue("type");
			xml_data_encoding = XMLFunctions.getEncodingForStr(attributes.getValue("encoding"));
			xml_data_size = Integer.valueOf(attributes.getValue("size"));
			
			data = new ArrayList<MguiNumber>(xml_data_size);
			
			xml_current_block = localName;
			return;
			}
		
		if (localName.equals("ColourMap")){
			// Check if colour map by this name exists
			// If not, load it
			String name = attributes.getValue("name");
			if (InterfaceEnvironment.getColourMap(name) == null){
				xml_cmap_handler = new ColourMapXMLHandler();
				xml_cmap_handler.startElement(null, localName, "", attributes);
			}else{
				ColourMap cmap = InterfaceEnvironment.getColourMap(name);
//				cmap.mapMin = getDataMin();
//				cmap.mapMax = getDataMax();
				this.setColourMap(cmap, true);
				return;
				}
			return;
			}
		
		if (localName.equals("NameMap")){
			// Check if name map by this name exists
			// If not, load it
			String name = attributes.getValue("name");
			if (InterfaceEnvironment.getNameMap(name) == null){
				xml_nmap_handler = new NameMapXMLHandler();
				xml_nmap_handler.startElement(null, localName, name, attributes);
			}else{
				NameMap nmap = InterfaceEnvironment.getNameMap(name);
				this.setNameMap(nmap);
				}
			return;
			}
		
	}

	@Override
	public void handleXMLElementEnd(String localName) throws SAXException {
		
		
		if (xml_cmap_handler != null){
			if (localName.equals("ColourMap")){
				// All done
				ColourMap cmap = xml_cmap_handler.getMap();
				InterfaceEnvironment.addColourMap(cmap);
//				cmap.mapMin = getDataMin();
//				cmap.mapMax = getDataMax();
				setColourMap(cmap, true);
				xml_cmap_handler = null;
				return;
				}
			xml_cmap_handler.endElement(null, localName, "");
			return;
			}
		
		if (xml_nmap_handler != null){
			if (localName.equals("NameMap")){
				// All done
				NameMap nmap = xml_nmap_handler.getMap();
				InterfaceEnvironment.addNameMap(nmap);
				setNameMap(nmap);
				xml_nmap_handler = null;
				return;
				}
			xml_nmap_handler.endElement(null, localName, "");
			return;
			}
		
		// If attribute handler is active, redirect this element to it.
		if (xml_attribute_handler != null){
			
			if (localName.equals("AttributeList")){
				attributes.setIntersection(xml_attribute_handler.getAttributeList(), false);
				double min = ((MguiDouble)xml_attribute_handler.getAttributeList().getValue("DataMin")).getValue();
				double max = ((MguiDouble)xml_attribute_handler.getAttributeList().getValue("DataMax")).getValue();
				setDataLimits(min,max);
				min = ((MguiDouble)xml_attribute_handler.getAttributeList().getValue("ColourMin")).getValue();
				max = ((MguiDouble)xml_attribute_handler.getAttributeList().getValue("ColourMax")).getValue();
				setColourLimits(min,max,false);
				xml_attribute_handler = null;
			}else{
				xml_attribute_handler.endElement(null, localName, "");
				}
			return;
			}
				
		
		// Finalize data loading here
		if (localName.equals("Data")){
			xml_data_size = -1;
			xml_data_type = null;
			xml_data_encoding = null;
			xml_current_block = null;
			return;
			}
	}

	@Override
	public void handleXMLString(String s) throws SAXException {
		
		if (xml_attribute_handler != null){
			xml_attribute_handler.characters(s.toCharArray(), 0, s.length());
			return;
		}
		
		// String is the data to set
		if (xml_current_block != null && xml_current_block.equals("Data")){
			if (data == null)
				throw new SAXException("VertexDataColumn.handleXMLString: Vertex data received but no data array set.");
			
			if (xml_data_encoding == null)
				throw new SAXException("VertexDataColumn.handleXMLString: Vertex data received but no encoding set.");
			
			switch(xml_data_encoding){
				case Ascii:
					loadAsciiData(s);
					break;
				case Base64Binary:
					loadBinaryData(s, 0);
					break;
				case Base64BinaryZipped:
				case Base64BinaryGZipped:
					loadBinaryData(s, 1);
					break;
//				case Base64BinaryGZipped:
//					loadBinaryData(s, 2);
//					break;
				}
			
			return;
		}
		
	}

	@Override
	public String getLocalName() {
		return "VertexDataColumn";
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String data_type = "?";
		if (data.size() > 0){
			data_type = data.get(0).getLocalName();
			}
		
		writer.write(_tab + "<" + getLocalName() + " name='" + getName() + "' >\n");
		
		// Attributes
		attributes.writeXML(tab + 1, writer, options, progress_bar);
		
		// Data
		writer.write("\n" + _tab2 + "<Data " + 
					 "type='" + data_type + "' " + 
					 "size='" + data.size() + "' " +
					 "encoding='" + XMLFunctions.getEncodingStr(options.encoding) + "' >\n");
		
		if (data.size() > 0){
		
			switch (options.encoding){
				case Base64Binary:
					writeBinaryData(writer, tab+2, 0);
					break;
				case Base64BinaryZipped:
				case Base64BinaryGZipped:
					writeBinaryData(writer, tab+2, 1);
					break;
//				case Base64BinaryGZipped:
//					writeBinaryData(writer, tab+2, 2);
//					break;
				case Ascii:
					writeAsciiData(writer, tab+2, options.max_line_size, options.sig_digits);
					break;
				}
			}
		
		writer.write("\n" + _tab2 + "</Data>\n");
		
		// Colour map
		ColourMap cmap = this.getColourMap();
		if (cmap != null){
			cmap.writeXML(tab + 1, writer, options, progress_bar);
			writer.write("\n");
			}
		
		// Name map
		NameMap nmap = this.getNameMap();
		if (nmap != null){
			nmap.writeXML(tab + 1, writer, options, progress_bar);
			writer.write("\n");
			}
		
		// TODO: Linked data
		
		writer.write(_tab + "</" + getLocalName() + ">\n");
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException {
		writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, new XMLOutputOptions(), null);
	}

	@Override
	public String getShortXML(int tab) {
		return XMLFunctions.getTab(tab) + "<VertexDataColumn size='" + this.data.size() + "' />\n";
	}
	
	/*****************************************
	 * Writes this vertex column's data as Base64 binary.
	 * 
	 * @param tab
	 * @param compress; compression: 0 = none, 1 = zipped; 2 = gzipped
	 */
	protected void writeBinaryData(Writer writer, int tab, int compress) throws IOException{
		
		MguiNumber specimen = data.get(0);
		int type = DataTypes.FLOAT;
		if (specimen instanceof MguiInteger)
			type = DataTypes.INTEGER;
		else if (specimen instanceof MguiLong)
			type = DataTypes.LONG;
		else if (specimen instanceof MguiDouble)
			type = DataTypes.DOUBLE;
		int data_size = DataTypes.getSizeForType(type);
		ByteBuffer data_out = ByteBuffer.allocate(data.size() * data_size);
		
		// First encode as raw bytes
		for (int i = 0; i < data.size(); i++){
			switch (type){
				case DataTypes.INTEGER:
					data_out.putInt((int)data.get(i).getValue());
					break;
				case DataTypes.LONG:
					data_out.putLong((long)data.get(i).getValue());
					break;
				case DataTypes.FLOAT:
					data_out.putFloat((float)data.get(i).getValue());
					break;
				case DataTypes.DOUBLE:
					data_out.putDouble(data.get(i).getValue());
					break;
				}
			}
		
		// Now compress if necessary
		if (compress == 1){
			byte[] b = IoFunctions.compressZipped(data_out.array());
			data_out = ByteBuffer.wrap(b);
		}else if (compress == 2){
			byte[] b = IoFunctions.compressGZipped(data_out.array());
			data_out = ByteBuffer.wrap(b);
			}
		
		// Now encode as Base64
		// Finally, write string to file
		writer.write(Base64.getMimeEncoder().encodeToString(data_out.array()));
		
	}
	
	/*****************************************
	 * Writes this vertex column's data as Ascii
	 * 
	 * @param tab
	 * @param compress; compression: 0 = none, 1 = zipped; 2 = gzipped
	 */
	protected void writeAsciiData(Writer writer, int tab, int line_size, int sig_digits) throws IOException{
		String _tab = XMLFunctions.getTab(tab);
		
		writer.write(_tab);
		int pos = 0;
		for (int i = 0; i < data.size(); i++){
			writer.write(MguiDouble.getString(data.get(i).getValue(), sig_digits));
			pos++;
			if (pos > line_size){
				pos = 0;
				writer.write("\n" + _tab);
			}else{
				writer.write(" ");
				}
			}
		
	}
	
	/********************************************
	 * Load vertices from Base64 binary encoded data.
	 * 
	 * @param string_data
	 * @param compression Compression; 0 for none, 1 for zip, 2 for gzip
	 */
	protected void loadBinaryData(String string_data, int compression){
		
		try{
			// Decode
			Charset charset = Charset.forName("UTF-8");
			byte[] utf8_bytes = string_data.getBytes(charset);
			byte[] b_data = Base64.getMimeDecoder().decode(utf8_bytes);
			
			DataType type = DataTypes.getType(DataTypes.DOUBLE);
			if (xml_data_type.equals("MguiInteger"))
				type = DataTypes.getType(DataTypes.INTEGER);
			if (xml_data_type.equals("MguiLong"))
				type = DataTypes.getType(DataTypes.LONG);
			if (xml_data_type.equals("MguiFloat"))
				type = DataTypes.getType(DataTypes.FLOAT);
			
			// Decompress
			switch (compression){
				case 1: 
					b_data = IoFunctions.decompressZipped(b_data);
					break;
				case 2: 
					b_data = IoFunctions.decompressGZipped(b_data);
					break;
				}
			
			// Convert to data
			ByteBuffer buffer = ByteBuffer.wrap(b_data);
			while (buffer.hasRemaining()){
				double value = Double.NaN;
				switch (type.val){
					case DataTypes.INTEGER:
						value = buffer.getInt();
						break;
					case DataTypes.FLOAT:
						value = buffer.getFloat();
						break;
					case DataTypes.LONG:
						value = buffer.getLong();
						break;
					case DataTypes.DOUBLE:
						value = buffer.getDouble();
						break;
					}
				data.add(NumberFunctions.getInstance(type, value));
				}
			
		}catch (Exception ex){
			InterfaceSession.handleException(ex);
			}
		
	}
	
	/******************************************************
	 * Load vertices from Ascii encoded data
	 * 
	 * @param string_data
	 */
	protected void loadAsciiData(String string_data){
		
		StringTokenizer tokens = new StringTokenizer(string_data);
		
		DataType type = DataTypes.getType(DataTypes.DOUBLE);
		if (xml_data_type.equals("MguiInteger"))
			type = DataTypes.getType(DataTypes.INTEGER);
		if (xml_data_type.equals("MguiLong"))
			type = DataTypes.getType(DataTypes.LONG);
		if (xml_data_type.equals("MguiFloat"))
			type = DataTypes.getType(DataTypes.FLOAT);
		
		while (tokens.hasMoreTokens()){
			double value = Double.NaN;
			switch (type.val){
				case DataTypes.INTEGER:
					value = Double.valueOf(tokens.nextToken()).intValue();
					break;
				case DataTypes.FLOAT:
					value = Float.valueOf(tokens.nextToken());
					break;
				case DataTypes.LONG:
					value = Long.valueOf(tokens.nextToken());
					break;
				case DataTypes.DOUBLE:
					value = Double.valueOf(tokens.nextToken());
					break;
				}
			data.add(NumberFunctions.getInstance(type, value));
			}
		
	}
	
	
	@Override
	public InterfacePopupMenu getPopupMenu() {
		return getPopupMenu(null);
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu(List<Object> selection) {
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		
		menu.addMenuItem(new JMenuItem("Set current"));
		menu.addMenuItem(new JMenuItem("Rename"));
		menu.addMenuItem(new JMenuItem("Delete"));
		
		return menu;
	}

	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		if (e.getActionCommand().equals("Set current")){
			
			fireDataColumnChanged(new VertexDataColumnEvent(this, EventType.ColumnIsCurrent));
			return;
			}
		
		if (e.getActionCommand().equals("Rename")){
			
			String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), "New name for column:", this.getName());
			if (name == null || name.equals(this.getName())) return;
			
			this.setName(name);
			return;
			}
		
		if (e.getActionCommand().equals("Delete")){
			
			this.fireDataColumnChanged(new VertexDataColumnEvent(this, EventType.ColumnRemoved));
			return;
			}
		
	}

	@Override
	public void showPopupMenu(MouseEvent e) {
		InterfacePopupMenu menu = getPopupMenu();
		menu.show(e);
	}
	
	
}