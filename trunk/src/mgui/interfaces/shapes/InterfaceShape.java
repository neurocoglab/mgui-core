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

package mgui.interfaces.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3f;

import mgui.datasources.DataSourceException;
import mgui.datasources.DataType;
import mgui.datasources.LinkedDataStream;
import mgui.geometry.Shape;
import mgui.geometry.Shape2D;
import mgui.geometry.Shape3D;
import mgui.geometry.util.NodeShape;
import mgui.geometry.util.NodeShapeComboRenderer;
import mgui.geometry.util.SpatialUnit;
import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.AttributeSelectionMap;
import mgui.interfaces.attributes.AttributeSelectionMap.ComboMode;
import mgui.interfaces.attributes.tree.AttributeTreeNode;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.graphs.util.GraphFunctions;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.io.PersistentObject;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.math.VariableObject;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.queries.InterfaceQuery;
import mgui.interfaces.shapes.attributes.ShapeAttribute;
import mgui.interfaces.shapes.mesh.VertexSelection;
import mgui.interfaces.shapes.queries.InterfaceShapeQuery;
import mgui.interfaces.shapes.queries.InterfaceShapeQueryObject;
import mgui.interfaces.shapes.trees.Shape3DTreeNode;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.transfers.InterfaceTransferable;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.FileLoader;
import mgui.io.FileWriter;
import mgui.io.InterfaceIO;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.InterfaceShapeLoader;
import mgui.io.domestic.shapes.ShapeInputOptions;
import mgui.io.domestic.shapes.ShapeModel3DOutputOptions;
import mgui.io.domestic.shapes.xml.ShapeXMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.io.util.IoFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.numbers.MguiShort;
import mgui.numbers.NumberFunctions;
import mgui.resources.icons.IconObject;
import mgui.util.StringFunctions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**********************************
 * Abstract base class for all shape interfaces (ShapeInts). Implements vertex-wise data, data links, 
 * attributes, attribute overriding, colour maps, name maps, node selection, shape listeners, and regions-
 * of-interest. Also keeps track of I/O parameters used to load or write it, and extends {@code XMLObject}
 * to read/write itself in XML format.
 * 
 * <p>See <a href="http://mgui.wikidot.com/shape-ints-dev">Development Notes: Interface Shapes</a> for a 
 * more detailed description.
 *  
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfaceShape extends AbstractInterfaceObject 
									 implements InterfaceObject,
												AttributeListener, 
												AttributeObject,
												Comparable<InterfaceShape>,
												IconObject,
												XMLObject,
												PopupMenuObject,
												InterfaceTransferable,
												VariableObject,
												InterfaceShapeQueryObject,
												VertexDataColumnListener,
												PersistentObject{

	
/*********** LOCAL VARIABLES **********************/
	
	public boolean notifyListeners = true;
	
	protected ArrayList<ShapeListener> shapeListeners = new ArrayList<ShapeListener>();
	protected AttributeList attributes;
	protected ShapeSet parent_set;
	protected boolean isDrawable = true;
	protected boolean isLight = false;
	protected boolean isImageShape = false;
	protected AttributeList overrideAttr;
	protected boolean isOverridden = false;
	protected HashMap<String, VertexDataColumn> vertex_data = new HashMap<String, VertexDataColumn>();
	public ArrayList<String> data_columns = new ArrayList<String>();
	protected boolean[] constraints;
	protected VertexSelection selected_nodes;
	protected HashMap<String, ColourMap> linked_colour_maps = new HashMap<String, ColourMap>();
	protected Icon icon;
	protected long ID;
	protected boolean is_auxiliary = false; 			// If true, this shape is treated as an auxiliary shape
	
	protected VertexDataColumn last_column_added, last_column_removed, last_column_changed;
	
	protected InterfaceIOOptions loader_options, writer_options;
	
	protected boolean is_registered = false;
	
/*********** ABSTRACT METHODS **********************/
	
	public abstract void drawShape2D(Graphics2D g, DrawingEngine d);
	public abstract void updateShape();
	
	/**************************************
	 * Returns the {@linkplain Shape} associated with this object
	 * 
	 * @return
	 */
	public abstract Shape getGeometry();
	
	/**************************************
	 * Sets the {@linkplain Shape} associated with this object. If the class type is
	 * incorrect, returns {@code false}
	 * 
	 * @return
	 */
	public abstract boolean setGeometry(Shape geometry);
	
	/***************************************
	 * Returns an instance of this {@code InterfaceShape}'s geometry class.
	 * 
	 * @return
	 */
	public abstract Shape getGeometryInstance();
	
	/**********************************
	 * Returns {@code true} if a change to {@code attribute} will require a redraw of
	 * this shape.
	 * 
	 * @param attribute
	 * @return
	 */
	public abstract boolean needsRedraw(Attribute<?> attribute);		//regenerate renderer?
	public abstract Attribute<?> getModifiedAttribute();
	
	/**********************************
	 * Returns the number of vertices defining the geometry of this shape.
	 * 
	 * @return
	 */
	public abstract int getVertexCount();
	
	/**********************************
	 * Is this shape a child of another shape? Usually this refers to shapes which produce
	 * themselves in a different dimensionality; e.g., a 3D shape represented on a 2D plane.
	 * 
	 * @return
	 */
	public abstract boolean hasParentShape();
	
	
/*********** INITIALIZATION ************************/
	
	protected void _init(){
		attributes = new AttributeList();
		attributes.add(new ShapeAttribute<String>("Name", "no-name", false, false, true, false));
		attributes.add(new ShapeAttribute<BasicStroke>("3D.LineStyle", new BasicStroke()));
		attributes.add(new ShapeAttribute<BasicStroke>("2D.LineStyle", new BasicStroke()));
		attributes.add(new ShapeAttribute<Color>("3D.LineColour", Color.BLUE));	
		attributes.add(new ShapeAttribute<Color>("2D.LineColour", Color.BLUE));	
		attributes.add(new ShapeAttribute<Color>("3D.FillColour", Color.BLUE));	
		attributes.add(new ShapeAttribute<Color>("2D.FillColour", Color.BLUE));	
		attributes.add(new ShapeAttribute<MguiBoolean>("3D.HasFill", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.HasFill", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<MguiBoolean>("IsVisible", new MguiBoolean(true)));
		attributes.add(new ShapeAttribute<MguiBoolean>("3D.HasAlpha", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<MguiFloat>("3D.Alpha", new MguiFloat(1.0f)));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.Show", new MguiBoolean(true), true, false));
		attributes.add(new ShapeAttribute<MguiBoolean>("3D.Show", new MguiBoolean(true), false, true));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.HasAlpha", new MguiBoolean(false), true, false));
		attributes.add(new ShapeAttribute<MguiFloat>("2D.Alpha", new MguiFloat(1.0f), true));
		attributes.add(new ShapeAttribute<MguiBoolean>("3D.ShowBounds", new MguiBoolean(false), false, true));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.ShowBounds", new MguiBoolean(false), true, false));
		attributes.add(new ShapeAttribute<Color>("3D.BoundsColour", Color.BLUE));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.ShowVertices", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<MguiBoolean>("3D.ShowVertices", new MguiBoolean(false)));
		HashMap<String, NodeShape> node_shapes = InterfaceEnvironment.getVertexShapes();
		AttributeSelectionMap<NodeShape> shapes = 
			new AttributeSelectionMap<NodeShape>("2D.VertexShape", node_shapes, NodeShape.class);
		shapes.setComboMode(ComboMode.AsValues);
		shapes.setComboRenderer(new NodeShapeComboRenderer());
		shapes.setComboWidth(100);
		attributes.add(shapes);
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.ShowSelectedVertices", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<MguiBoolean>("3D.ShowSelectedVertices", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<Color>("3D.VertexColour", Color.BLUE));
		attributes.add(new ShapeAttribute<Color>("2D.VertexColour", Color.BLUE));
		attributes.add(new ShapeAttribute<Color>("2D.VertexOutlineColour", Color.BLACK));
		attributes.add(new ShapeAttribute<Color>("SelectedVertexColour", Color.RED));
		attributes.add(new ShapeAttribute<MguiBoolean>("ShowData", new MguiBoolean(false), true));
		attributes.add(new AttributeSelection<String>("CurrentData", data_columns, String.class));	
		AttributeSelection<ColourMap> dcm = new AttributeSelection<ColourMap>("DefaultColourMap", InterfaceEnvironment.getColourMaps(), ColourMap.class);
		ColourMap cmap = InterfaceEnvironment.getColourMap("Greyscale");
		if (cmap == null){
			cmap = ContinuousColourMap.getGreyScale();
			InterfaceEnvironment.addColourMap(cmap);
			}
		dcm.select(cmap);
		attributes.add(dcm);
		attributes.add(new ShapeAttribute<MguiBoolean>("3D.ShowConstraints", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.ShowConstraints", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<MguiFloat>("3D.VertexScale", new MguiFloat(1f)));
		attributes.add(new ShapeAttribute<MguiFloat>("3D.VertexScaleExp", new MguiFloat(1f)));
		attributes.add(new ShapeAttribute<MguiFloat>("2D.VertexScale", new MguiFloat(1f)));
		attributes.add(new ShapeAttribute<MguiFloat>("2D.VertexScaleExp", new MguiFloat(1f)));
		attributes.add(new ShapeAttribute<MguiBoolean>("IsSelectable", new MguiBoolean(true), false, false, false, false));
		attributes.add(new AttributeSelection<String>("ScaleData", data_columns, String.class));
		attributes.add(new ShapeAttribute<MguiBoolean>("ScaleVertices", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<MguiBoolean>("ScaleVerticesAbs", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<Font>("3D.LabelFont", new Font("Arial", Font.PLAIN, 12)));
		attributes.add(new ShapeAttribute<MguiFloat>("3D.LabelScale", new MguiFloat(1f)));
		attributes.add(new ShapeAttribute<MguiFloat>("3D.LabelOffset", new MguiFloat(0.5f)));
		attributes.add(new ShapeAttribute<Color>("3D.LabelColour", Color.BLACK));
		attributes.add(new ShapeAttribute<String>("3D.LabelFormat", "#0.0"));
		attributes.add(new ShapeAttribute<MguiBoolean>("3D.ShowVertexLabels", new MguiBoolean(false)));
		attributes.add(new ShapeAttribute<Font>("2D.LabelFont", new Font("Arial", Font.PLAIN, 12)));
		attributes.add(new ShapeAttribute<MguiFloat>("2D.LabelScale", new MguiFloat(1f)));
		attributes.add(new ShapeAttribute<Color>("2D.LabelColour", Color.BLACK));
		attributes.add(new ShapeAttribute<Color>("2D.LabelOutlineColour", Color.WHITE));
		attributes.add(new ShapeAttribute<MguiFloat>("2D.LabelOutlineWidth", new MguiFloat(0)));
		attributes.add(new ShapeAttribute<MguiFloat>("2D.LabelBackgroundAlpha", new MguiFloat(1f)));
		attributes.add(new ShapeAttribute<Color>("2D.LabelBackgroundColour", Color.LIGHT_GRAY));
		attributes.add(new ShapeAttribute<String>("2D.LabelFormat", "#0.0"));
		attributes.add(new ShapeAttribute<MguiFloat>("2D.LabelOffset", new MguiFloat(0.5f)));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.ShowVertexLabels", new MguiBoolean(false)));
		AttributeSelection<String> pos = 
				new AttributeSelection<String>("2D.LabelPosition", GraphFunctions.getLabelPositions(), String.class, "SE");
		attributes.add(pos);
		attributes.add(new Attribute<MguiFloat>("3D.Shininess", new MguiFloat(1f)));
		attributes.add(new AttributeSelection<String>("LabelData", data_columns, String.class));	
		attributes.add(new Attribute<MguiBoolean>("InheritFromParent", new MguiBoolean(true)));
		
		attributes.add(new AttributeSelection<SpatialUnit>("Unit", InterfaceEnvironment.getSpatialUnits(), SpatialUnit.class, true, false));
		setUnit(InterfaceEnvironment.getSpatialUnit("meter"));
		
		Attribute<MguiDouble> attr = new Attribute<MguiDouble>("DataMax", new MguiDouble(1.0), true, false);
		attributes.add(attr);
		attr = new Attribute<MguiDouble>("DataMin", new MguiDouble(0.0), true, false);
		attributes.add(attr);
		Attribute<String> a = new Attribute<String>("UrlReference", "", true, false);
		a.setEditable(false);
		attributes.add(a);
		a = new Attribute<String>("FileLoader", "", true, false);
		a.setEditable(false);
		attributes.add(a);
		a = new Attribute<String>("FileWriter", "", true, false);
		a.setEditable(false);
		attributes.add(a);
		
		attributes.addAttributeListener(this);
		ID = -1;
		
		setIcon();
	}

/********** CLIP NODE BS ********************************/
	
//	/**********************
//	 * Because Java3D is buggy, workaround it to deactivate clips every time the
//	 * shape's node changes.
//	 * 
//	 */
//	public void deactivateClips(){
//		ShapeSet3DInt parent = (ShapeSet3DInt)this.getParentSet();
//		if (parent == null) return;
//		parent.deactivateClipNodes();
//	}
//	
//	/************************
//	 * Reactivates clips once change is complete
//	 * 
//	 */
//	public void reactivateClips(){
//		ShapeSet3DInt parent = (ShapeSet3DInt)this.getParentSet();
//		if (parent == null) return;
//		parent.activateClipNodes();
//	}

/********** CORE ATTRIBUTES *****************************/	
	
	public boolean isAuxiliaryShape(){
		return is_auxiliary;
	}
	
	public void isAuxiliaryShape(boolean is_auxiliary){
		this.is_auxiliary = is_auxiliary;
	}
	
/********** REGISTRATION ********************************/	
	
	/***********************
	 * Registers this shape with the current session, if it hasn't already been done,
	 * by assigning it a unique identifier.
	 * 
	 */
	public void register(){
		if (is_registered) return;
		this.setID(InterfaceSession.getUID());
		is_registered = true;
	}
	
/********** GEOMETRY ********************************/	
	
	
	/***********************************************
	 * Returns the spatial unit for this shape set.
	 * 
	 * @return the spatial unit for this shape set
	 */
	public SpatialUnit getUnit(){
		SpatialUnit unit = (SpatialUnit)attributes.getValue("Unit");
		if (unit != null) return unit;
		
		return InterfaceEnvironment.getDefaultSpatialUnit();
	}
	
	public void setUnit(SpatialUnit unit){
		setAttribute("Unit", unit);
	}
	
/********** ATTRIBUTES STUFF ****************************/
	
	/***********************************
	 * Copies {@code attributes} to this shape. The shape should determine which attributes
	 * may be copied.
	 * 
	 * @param attributes
	 * @return
	 */
	public boolean copyAttributes(InterfaceShape source_shape) {
		
		ArrayList<Attribute<?>> to_copy = new ArrayList<Attribute<?>>();
		
		AttributeList source_attributes = source_shape.getAttributes();
		
		for (Attribute<?> attribute : source_attributes.getAsList()) {
			if (attribute.isCopiable()) {
				to_copy.add(attribute);
				}
			}
		
		boolean success = false;
		
		if (to_copy.size() > 0) {
			this.attributes.setIntersection(to_copy, true);
			
			// Copy all vertex column attributes
			// Don't fail if columns don't copy perfectly
			for (VertexDataColumn source_column : source_shape.getVertexDataColumns()) {
				VertexDataColumn target_column = this.getVertexDataColumn(source_column.getName());
				target_column.copyAttributes(source_column);
				}
			success = true;
			}
		
		return success;
	}
	
	/***********************************
	 * Is this parent attribute inherited by a child shape?
	 * 
	 * @param attribute
	 * @return
	 */
	public abstract boolean isHeritableAttribute(String name);
	
	/***********************************
	 * Does this child attribute inherit values from a parent shape?
	 * 
	 * @param attribute
	 * @return
	 */
	public abstract boolean isInheritingAttribute(Attribute<?> attribute);
	
	
	/**********************************
	 * Returns the visibility of this {@code InterfaceShape}.
	 * 
	 * @return <code>true</code> if visible
	 */
	public boolean isVisible(){
		boolean visible = ((MguiBoolean)attributes.getAttribute("IsVisible").getValue()).getTrue();
		if (!visible || this.getParentSet() == null) return visible;
		return getParentSet().isVisible();
	}
	
	/**********************************
	 * Sets the visibility of this {@code InterfaceShape} to <code>true</code>.
	 * 
	 * @param b
	 */
	public void setVisible(boolean b){
		attributes.setValue("IsVisible", new MguiBoolean(b));
	}
	
	public boolean inheritAttributesFromParent(){
		if (this.hasParentShape()) {
			if (getParentSet() instanceof InterfaceShape) {
				MguiBoolean overriding = (MguiBoolean)((InterfaceShape)getParentSet()).getAttribute("IsOverriding").getValue();
				if (!overriding.getTrue()) return false;
				}
			}
		return ((MguiBoolean)attributes.getValue("InheritFromParent")).getTrue();
	}
	
	/**********************************
	 * Returns the selectability of this {@code InterfaceShape}.
	 * 
	 * @return <code>true</code> if visible
	 */
	public boolean isSelectable(){
		boolean visible = ((MguiBoolean)attributes.getAttribute("IsSelectable").getValue()).getTrue();
		if (!visible || this.getParentSet() == null) return visible;
		return getParentSet().isSelectable();
	}
	
	/**********************************
	 * Sets the selectability of this {@code InterfaceShape} to <code>true</code>.
	 * 
	 * @param b
	 */
	public void setSelectable(boolean b){
		attributes.setValue("IsSelectable", new MguiBoolean(b));
	}
	
	@Override
	public Object getAttributeValue(String name) {
		Attribute<?> attribute = getAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
	}
	
	@Override
	public Attribute<?> getAttribute(String attrName) {	
		return attributes.getAttribute(attrName);
	}
	
	/*************************
	 * 
	 * Returns the (possibly inherited) value of the named attribute.
	 * 
	 * @param name
	 * @return
	 */
	public Object getInheritedAttributeValue(String name) {
		Attribute<?> attribute = getInheritedAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
	}
	
	/*************************
	 * 
	 * Returns the (possibly inherited) named attribute.
	 * 
	 */
	public Attribute<?> getInheritedAttribute(String attrName) {	
		if (hasParentShape() && inheritAttributesFromParent()) {
			return this.getParentAttribute(attrName);
			}
		return attributes.getAttribute(attrName);
	}
	
	protected abstract Attribute<?> getParentAttribute(String attrName);
	
	/****************************
	 * Determines whether this object has an attribute named {@code name}.
	 * 
	 * @param name
	 * @return
	 */
	public boolean hasAttribute(String name) {
		return attributes.hasAttribute(name);
	}
	
	
	@Override
	public AttributeList getAttributes() {
		
		return attributes;
	}
	
	/******************************
	 * 
	 * Returns an {@linkplain AttributeList} object reflecting the current attributes for
	 * this shape, whether local or inherited (i.e., overridden by this shape's parent).
	 * 
	 * @return
	 */
	public AttributeList getInheritedAttributes() {
		
		if (hasParentShape() && inheritAttributesFromParent()) {
			if (getParentSet() instanceof InterfaceShape ) {
				// Return intersection of parent attributes
				AttributeList parent_attributes = ((InterfaceShape)getParentSet()).getInheritedAttributes();
				if (parent_attributes.hasAttribute("IsOverriding")) {
					if (!((MguiBoolean)parent_attributes.getValue("IsOverriding")).getTrue()) {
						return attributes;
						}
					}
				AttributeList overridden_attributes = (AttributeList)attributes.clone();
				overridden_attributes.setIntersection(parent_attributes);
				return overridden_attributes;
				}
			}
		
		return getAttributes();
	}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	@Override
	public void setAttributes(AttributeList thisList) {
		if (attributes != null)
			attributes.removeAttributeListener(this);
		attributes = thisList;
		attributes.addAttributeListener(this);
	}
	
	@Override
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	@Override
	public void setName(String name){
		if (!isLegalName(name)){
			InterfaceSession.log("Warning: InterfaceShape: Name must not contain a '.'; replacing with '_'", 
								 LoggingType.Warnings);
			name = getLegalName(name);
			}
		setAttribute("Name", name);
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	/************************************************
	 * Is this a legal name for a shape. Must not contain a '.' character...
	 * 
	 * @param name
	 * @return
	 */
	public boolean isLegalName(String name){
		return !name.contains(".");
	}
	
	protected String getLegalName(String name){
		return StringFunctions.replaceAll(name, ".", "_");
	}
	
	/*************************************************
	 * Returns a full name of this shape, including its parent model and shape sets, separated by
	 * dots.
	 * 
	 * @return
	 */
	public String getFullName(){
		 ShapeSet set = getParentSet();
		 if (set == null) return getName();
		 return set.getFullName() + " | " + getName();
	}
	
	public abstract Font getLabelFont();
	
	public abstract void setLabelFont(Font font);
	
	public abstract float getLabelScale();
	
	public abstract void setLabelScale(float scale);
	
	public abstract Color getLabelColour();
	
	public abstract void setLabelColour(Color colour);
	
	public boolean isDrawable(){
		return isDrawable;
	}
	
	public boolean isLight(){
		return isLight;
	}
	
	public boolean isImageShape(){
		return isImageShape;
	}
	
	public boolean show2D(){
		boolean show = ((MguiBoolean)attributes.getValue("2D.Show")).getTrue();
		if (!show || getParentSet() == null) return show;
		return getParentSet().show2D();
	}
	
	public boolean show3D(){
		boolean show = ((MguiBoolean)attributes.getValue("3D.Show")).getTrue();
		if (!show || getParentSet() == null) return show;
		return getParentSet().show3D();
	}
	
	/*********************************************************
	 * Specifies whether to show this shape in 2D.
	 * 
	 * @param b
	 */
	public void show2D(boolean b){
		setAttribute("2D.Show", new MguiBoolean(b));
	}
	
	/*********************************************************
	 * Specifies whether to show this shape in 2D.
	 * 
	 * @param b
	 */
	public void show3D(boolean b){
		attributes.setValue("3D.Show", new MguiBoolean(b));
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu() {
		return getPopupMenu(null);
	}
	
	
	/*********************************************************
	 * Queries whether this shape has transparency.
	 * 
	 * @return
	 */
	public abstract boolean hasAlpha();
	
	/*********************************************************
	 * Specifies whether this shape has transparency.
	 * 
	 * @param b
	 */
	public abstract void hasAlpha(boolean b);
	
	/*********************************************************
	 * Queries whether the vertices of this shape are visible.
	 * 
	 * @return
	 */
	public abstract boolean showVertices();
	
	/*********************************************************
	 * Specifies whether the vertices of this shape are visible.
	 * 
	 * @param b
	 */
	public abstract void showVertices(boolean b);
	
	/*********************************************************
	 * Gets the current transparency level for this shape.
	 * 
	 * @return
	 */
	public abstract float getAlpha();
	
	/*********************************************************
	 * Sets the current transparency level for this shape.
	 * 
	 * @return
	 */
	public abstract void setAlpha(float f);
	
	public void setOverride(AttributeList attr){
		if (attr != null){
			overrideAttr = attr;
			isOverridden = true;
			return;
			}
		isOverridden = false;
	}
	
	public void unsetOverride(){
		isOverridden = false;
	}
	
	public double getDataMin(){
		VertexDataColumn column = getCurrentDataColumn();
		if (column == null)
			return ((MguiDouble)attributes.getValue("DataMin")).getValue();
		return column.getDataMin();
	}
	
	public double getDataMax(){
		VertexDataColumn column = getCurrentDataColumn();
		if (column == null)
			return ((MguiDouble)attributes.getValue("DataMax")).getValue();
		return column.getDataMax();
	}
	
	/**********************************************
	 * Returns the scale of this shape's vertices
	 * 
	 * @return
	 */
	public abstract float getVertexScale();
	
	/************************************************
	 * Sets the data minimum for the current column, and fires an event
	 * 
	 * @param d
	 */
	public void setDataMin(double d){
		setDataMin(d, true);
	}
	
	/************************************************
	 * Sets the data minimum for the current column
	 * 
	 * @param update Whether to fire an event
	 * @param d
	 */
	public void setDataMin(double d, boolean update){
		String column = getCurrentColumn();
		if (column == null) return;
		setDataMin(column, d, update);
	}
	
	/************************************************
	 * Sets the data minimum for the {@code column}
	 * 
	 * @param column Column to set minimum for
	 * @param update Whether to fire an event
	 * @param d
	 */
	public void setDataMin(String column, double d, boolean update){
		VertexDataColumn v_column = getVertexDataColumn(column);
		if (v_column == null){
			InterfaceSession.log("InterfaceShape.setDataMin: No column named '" + column + "'.", LoggingType.Errors);
			return;
			}
		v_column.setDataMin(d, update);
	}
	
	/************************************************
	 * Sets the data maximum for the current column, and fires an event
	 * 
	 * @param d
	 */
	public void setDataMax(double d){
		setDataMax(d, true);
	}
	
	/************************************************
	 * Sets the data maximum for the current column
	 * 
	 * @param update Whether to fire an event
	 * @param d
	 */
	public void setDataMax(double d, boolean update){
		String column = getCurrentColumn();
		if (column == null) return;
		setDataMax(column, d, update);
	}
	
	/************************************************
	 * Sets the data maximum for the {@code column}
	 * 
	 * @param column Column to set maximum for
	 * @param update Whether to fire an event
	 * @param d
	 */
	public void setDataMax(String column, double d, boolean update){
		VertexDataColumn v_column = getVertexDataColumn(column);
		if (v_column == null){
			InterfaceSession.log("InterfaceShape.setDataMax: No column named '" + column + "'.", LoggingType.Errors);
			return;
			}
		v_column.setDataMax(d, update);
	}
		
	public String getSourceURL(){
		return (String)attributes.getValue("SourceURL");
	}
	
	/**************************************************
	 * Returns the colour of this shape's vertices.
	 * 
	 * @return
	 */
	public abstract Color getVertexColour();
	
	/**************************************************
	 * Returns the colour of this shape's lines/edges.
	 * 
	 * @return
	 */
	public abstract Color getLineColour();
	
	/***************************************************
	 * Returns the line/edge style for this shape.
	 * 
	 * @return
	 */
	public abstract Stroke getLineStyle();
	
	/***************************************************
	 * Returns the line/edge style for this shape.
	 * 
	 * @return
	 */
	public abstract void setLineStyle(Stroke s);
	
/******************** PERSISTENCE STUFF **************************/
	
	public void setFileLoader(String loader){
		attributes.setValue("FileLoader", loader);
	}
	
	@Override
	public FileLoader getFileLoader() {
		String type = (String)attributes.getValue("FileLoader");
		if (type == null || type.length() == 0) return null;
		InterfaceIOType io_type = InterfaceEnvironment.getIOType(type);
		if (io_type == null) return null;
		InterfaceIO io = io_type.getIOInstance();
		if (io == null || !(io instanceof FileLoader)){
			InterfaceSession.log("InterfaceShape: Invalid file loader: '" + type + "'.", 
								 LoggingType.Errors);
			return null;
			}
		return (FileLoader)io;
	}
	
	@Override
	public boolean setFileLoader(InterfaceIOType io_type){
		InterfaceIO io = io_type.getIOInstance();
		if (io == null) return false;
		if (!(io instanceof FileLoader)){
			InterfaceSession.log("InterfaceShape: Type is not a file loader: '" + io_type.getName() + "'.", 
					 			 LoggingType.Errors);
			return false;
			}
		attributes.setValueForced("FileLoader", io_type.getName());
		return true;
	}
	
	
	@Override
	public FileWriter getFileWriter() {
		String type = (String)attributes.getValue("FileWriter");
		if (type == null || type.length() == 0) return null;
		InterfaceIOType io_type = InterfaceEnvironment.getIOType(type);
		InterfaceIO io = null;
		if (io_type != null)
			io = io_type.getIOInstance();
		if (io == null || !(io instanceof FileWriter)){
			InterfaceSession.log("InterfaceShape: Invalid file writer: '" + type + "'.", 
								 LoggingType.Errors);
			return null;
			}
		return (FileWriter)io;
	}
	
	@Override
	public InterfaceIOOptions getLoaderOptions(){
		return loader_options;
	}
	
	@Override
	public void setLoaderOptions(InterfaceIOOptions options){
		loader_options = options;
	}
	
	@Override
	public boolean setFileWriter(InterfaceIOType io_type){
		InterfaceIO io = io_type.getIOInstance();
		if (io == null || !(io instanceof FileWriter)){
			InterfaceSession.log("InterfaceShape: Type is not a file writer: '" + io_type.getName() + "'.", 
					 			 LoggingType.Errors);
			return false;
			}
		attributes.setValueForced("FileWriter", io_type.getName());
		return true;
	}
	
	@Override
	public InterfaceIOOptions getWriterOptions(){
		return writer_options;
	}
	
	@Override
	public void setWriterOptions(InterfaceIOOptions options){
		writer_options = options;
	}
	
	@Override
	public URL getUrlReference(){
		String s = (String)attributes.getValue("UrlReference");
		if (s == null || s.length() == 0) return null;
		try{
			return new URL(s);
		}catch (MalformedURLException ex){
			InterfaceSession.log("InterfaceShape: URL is malformed: " + s, LoggingType.Errors);
			}
		return null;
	}
	
	@Override
	public void setUrlReference(URL ref){
		attributes.setValueForced("UrlReference", ref.toExternalForm());
	}
	
	
/********** VERTEX DATA STUFF **********************/
	
	public VertexDataColumn getLastColumnAdded(){
		return this.last_column_added;
	}
	
	public VertexDataColumn getLastColumnRemoved(){
		return this.last_column_removed;
	}
	
	public VertexDataColumn getLastColumnChanged(){
		return this.last_column_changed;
	}
	
	public void vertexDataColumnChanged(VertexDataColumnEvent event){
		
		VertexDataColumn column = (VertexDataColumn)event.getSource();
		
		switch (event.type){
			case ColumnIsCurrent:
				this.setCurrentColumn(column.getName());
				if (!showData())
					showData(true);
				break;
				
			case ColumnChanged:
				updateDataColumns();
				last_column_changed = (VertexDataColumn)event.getSource();
				fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.VertexColumnChanged));
				break;
				
			case NameChanged:
				last_column_changed = (VertexDataColumn)event.getSource();
				ArrayList<String> names = new ArrayList<String>(vertex_data.keySet());
				for (int i = 0; i < names.size(); i++){
					VertexDataColumn column_i = vertex_data.get(names.get(i));
					if (column_i == column){
						vertex_data.remove(names.get(i));
						vertex_data.put(column.getName(), column);
						if (getCurrentColumn() != null && getCurrentColumn().equals(names.get(i))){
							this.setCurrentColumn(column.getName());
							}
						updateDataColumns();
						return;
						}
					}
				
				break;
				
			case ColumnRemoved:
				// If the column has not already been removed, remove it
				if (this.hasColumn(column.getName())){
					// Listeners are fired here
					removeVertexData(column.getName());
					return;
					}
				
				break;
			}
	}
	
	public void vertexDataColumnColourMapChanged(VertexDataColumnEvent event){
		//colour map changed, must regenerate
		if (!isVisible()) return;
		if (!this.showData()) return;
		if (event.getSource() != getCurrentDataColumn()) return;
		
		// Only regenerate if this is the current column and data is shown
		setVisible(true);
	
	}
	
	/********************************
	 * Returns a list of the names of all vertex data columns.
	 * 
	 * @return
	 */
	public ArrayList<String> getVertexDataColumnNames(){
		updateDataColumns();
		return data_columns;
	}
	
	/********************************
	 * Returns the number of vertex data columns associated with this shape.
	 * 
	 * @return
	 */
	public int getVertexDataColumnCount(){
		updateDataColumns();
		return data_columns.size(); 
	}
	
	/*******************************
	 * Returns a list of all vertex data columns.
	 * 
	 * @return
	 */
	public ArrayList<VertexDataColumn> getVertexDataColumns(){
		ArrayList<VertexDataColumn> data_columns = new ArrayList<VertexDataColumn>(vertex_data.values());
		Collections.sort(data_columns);
		return data_columns;
	}
	
	/********************************
	 * Returns the vertex data column associated with <code>name</code>.
	 * 
	 * @param name Name of the vertex data column
	 * @return
	 */
	public VertexDataColumn getVertexDataColumn(String name){
		if (name.contains(".{")) name = name.substring(0, name.indexOf(".{"));
		return vertex_data.get(name);
	}
	
	/*******************
	 * Adds a new vertex data column and populates it with {@code data}.
	 * 
	 * @param key
	 * @param data
	 * @return
	 */
	public boolean addVertexData(String key, ArrayList<MguiNumber> data){
		return addVertexData(key, data, null, null);
	}
	
	/***************
	 * Add data as a double array
	 * 
	 * @param column
	 * @param data
	 * @return
	 */
	public boolean addVertexData(String column, double[] data){
		if (data.length != this.getVertexCount()){
			InterfaceSession.log("Value count " + data.length + " not equal to vertex count " + getVertexCount() +
					" in shape '" + this.getName() + "'. Vertex column not set.",
					LoggingType.Errors);
			return false;
			}
		ArrayList<MguiNumber> ndata = new ArrayList<MguiNumber>(this.getVertexCount());
		for (int i = 0; i < data.length; i++)
			ndata.add(new MguiDouble(data[i]));
		return addVertexData(column, ndata, null, null);
	}
	
	/***********************************************************
	 * Adds vertex-wise data to this shape.
	 * 
	 * @param key 			The key by which this column is to be referred
	 * @param data 			The values for this column
	 * @param cmap 			Colour map [optionally null] associating values to colours
	 * @return
	 */
	public boolean addVertexData(String key, ArrayList<MguiNumber> data, ColourMap cmap){
		return addVertexData(key, data, null, cmap);
	}
	
	/***********************************************************
	 * Adds vertex-wise data to this shape.
	 * 
	 * @param key 			The key by which this column is to be referred
	 * @param data 			The values for this column
	 * @param nmap 			Name map [optionally null] associating integer keys to names
	 * @return
	 */
	public boolean addVertexData(String key, ArrayList<MguiNumber> data, NameMap nmap){
		return addVertexData(key, data, nmap, null);
	}

	/***********************************************************
	 * Renames the current column.
	 * 
	 * @param old_name
	 * @param new_name
	 * @return {@code false} if a column named {@code new_name} already exists, or no column named {@code old_name}
	 * 		   exists; {@code true} otherwise
	 */
	public boolean renameVertexDataColumn(String old_name, String new_name){
		if (!hasColumn(old_name)) return false;
		if (hasColumn(new_name)) return false;
		VertexDataColumn column = vertex_data.get(old_name);
		vertex_data.remove(old_name);
		vertex_data.put(new_name, column);
		column.setName(new_name);
		updateDataColumns();
		if (getCurrentColumn() != null && getCurrentColumn().equals(old_name))
			this.setCurrentColumn(new_name, false);
		
		last_column_changed = column;
		fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.VertexColumnRenamed));
		return true;
	}
	
	/***********************************************************
	 * Adds vertex-wise data to this shape.
	 * 
	 * @param key 			The key by which this column is to be referred
	 * @param data 			The values for this column
	 * @param nmap 			Name map [optionally null] associating integer keys to names
	 * @param cmap 			Colour map [optionally null] associating values to colours
	 * @return
	 */
	public boolean addVertexData(String key, ArrayList<MguiNumber> data, NameMap nmap, ColourMap cmap){
		if (data.size() != this.getVertexCount()){
			InterfaceSession.log("Value count " + data.size() + " not equal to vertex count " + getVertexCount() +
					" in shape '" + this.getName() + "'. Vertex column not set.",
					LoggingType.Errors);
			return false;
			}
		
		if (vertex_data.containsKey(key)){
			VertexDataColumn column = vertex_data.get(key);
			column.removeListener(this);
			}
		
		VertexDataColumn column = new VertexDataColumn(key, data);
		vertex_data.put(key, column);
		if (nmap != null) column.setNameMap(nmap);
		if (cmap != null) column.setColourMap(cmap);
		column.addListener(this);
		updateDataColumns();
		
		notifyListeners = false;
		AttributeSelection<String> a = (AttributeSelection<String>)attributes.getAttribute("CurrentData");
		String currentData = getCurrentColumn();
		a.setValue(currentData, false);
		notifyListeners = true;
		
		last_column_added = column;
		fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.VertexColumnAdded));
		return true;
	}
	
	/**********************************
	 * Add a vertex-wise data column of type <code>DataBuffer.TYPE_DOUBLE</code>.
	 * 
	 * @param key		The key associated with the data column
	 */
	public boolean addVertexData(String key){
		return addVertexData(key, DataBuffer.TYPE_DOUBLE);
	}
	
	/**********************************
	 * Add a vertex-wise data column of type <code>DataBuffer.TYPE_DOUBLE</code>.
	 * 
	 * @param key		The key associated with the data column
	 */
	public boolean addVertexData(VertexDataColumn column){
		int count = getVertexCount();
		if (count != column.getData().size()){
			InterfaceSession.log("Value count " + column.getData().size() + " not equal to vertex count " + getVertexCount() +
					" in shape '" + this.getName() + "'. Vertex column not set.",
					LoggingType.Errors);
			return false;
			}
		
		if (this.hasColumn(column.getName())){
			VertexDataColumn old = vertex_data.get(column.getName());
			old.removeListener(this);
			old.destroy();
			}
			
		vertex_data.put(column.getName(), column);
		column.addListener(this);
		this.updateDataColumns();
		last_column_added = column;
		fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.VertexColumnAdded));
		return true;
	}
	
	/*********************************
	 * Add a vertex-wise data column of type <code>dataType</code>, which must be one of 
	 * <code>DataBuffer.TYPE_DOUBLE</code>, <code>DataBuffer.TYPE_FLOAT</code>, or
	 * <code>DataBuffer.TYPE_INT</code>. If an incorrect type is specified, this method
	 * creates a column of type <code>DataBuffer.TYPE_DOUBLE</code>.
	 * 
	 * @param key			The key associated with the data column
	 * @param dataType		The data type with which to store the data
	 */
	public boolean addVertexData(String key, int dataType){
		return addVertexData(key, dataType, getDefaultColourMap());
	}
	
	/*********************************
	 * Add a vertex-wise data column of type <code>dataType</code>, which must be one of 
	 * <code>DataBuffer.TYPE_DOUBLE</code>, <code>DataBuffer.TYPE_FLOAT</code>, or
	 * <code>DataBuffer.TYPE_INT</code>. If an incorrect type is specified, this method
	 * creates a column of type <code>DataBuffer.TYPE_DOUBLE</code>.
	 * 
	 * @param key			The key associated with the data column
	 * @param dataType		The data type with which to store the data
	 * @param cmap 			The colour map to associate with this column
	 */
	public boolean addVertexData(String key, int dataType, ColourMap cmap){
		int n = getVertexCount();
		ArrayList<MguiNumber> vals = new ArrayList<MguiNumber>(n);
		for (int i = 0; i < n; i++)
			switch (dataType){
				case DataBuffer.TYPE_BYTE:
				case DataBuffer.TYPE_SHORT:
				case DataBuffer.TYPE_USHORT:
					vals.add(new MguiShort(0));
					break;
				case DataBuffer.TYPE_INT:
					vals.add(new MguiInteger(0));
					break;
				case DataBuffer.TYPE_FLOAT:
					vals.add(new MguiFloat(0));
					break;
				case DataBuffer.TYPE_DOUBLE:
				default:
					vals.add(new MguiDouble(0));
				}
		return addVertexData(key, vals, cmap);
	}
	
	/***************************************
	 * Remove a vertex data column from this shape. This also removes all associated data, and
	 * calls listeners to update, for instance, tree nodes.
	 * 
	 * @param key
	 */
	public void removeVertexData(String key){
		VertexDataColumn column = getVertexDataColumn(key);
		if (column == null) return;
		column.removeListener(this);
		vertex_data.remove(key);
		
		updateDataColumns();
		AttributeSelection<String> a = (AttributeSelection<String>)attributes.getAttribute("CurrentData");
		String currentData = getCurrentColumn();
		a.setValue(currentData);
		
		last_column_removed = column;
		fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.VertexColumnRemoved));
	}
	
	/*******************************************
	 * Returns the name of the current data column
	 * 
	 * @return
	 */
	public String getCurrentColumn(){
		AttributeSelection<String> a = (AttributeSelection<String>)attributes.getAttribute("CurrentData");
		return (String)a.getValue();
	}
	
	/*******************************************
	 * Returns the current {@linkplain VertexDataColumn}.
	 * 
	 * @return
	 */
	public VertexDataColumn getCurrentDataColumn(){
		String name = getCurrentColumn();
		if (name == null) return null;
		return getVertexDataColumn(name);
	}
	
	/*******************************************
	 * Returns a new array of the same size as this shape, with the specified data type
	 * 
	 * @param transfer_type
	 * @return
	 */
	public ArrayList<MguiNumber> newVertexData(DataType type){
		
		int n = this.getVertexCount();
		
		ArrayList<MguiNumber> values = new ArrayList<MguiNumber>(n);
		
		for (int i = 0; i < n; i++){
			values.add(NumberFunctions.getInstance(type, 0));
			}
			
		return values;
		
	}
	
	/********************************
	 * Retrieves a hash map containing the vertex-wise data associated with this shape.
	 * 
	 * @return
	 */
	public HashMap<String, ArrayList<MguiNumber>> getVertexDataMap(){
		
		HashMap<String, ArrayList<MguiNumber>> map = new HashMap<String, ArrayList<MguiNumber>>();
		ArrayList<String> keys = new ArrayList<String>(vertex_data.keySet());
		for (int i = 0; i < keys.size(); i++)
			map.put(keys.get(i), vertex_data.get(keys.get(i)).getData());
		
		return map;
	}
	
	/********************************
	 * Sets the hash map specified the vertex-wise data associated with this shape.
	 * 
	 * @param data
	 */
	public void setVertexDataMap(HashMap<String, ArrayList<MguiNumber>> data){
		vertex_data.clear();
		ArrayList<String> keys = new ArrayList<String>(data.keySet());
		for (int i = 0; i < keys.size(); i++)
			vertex_data.put(keys.get(i), new VertexDataColumn(keys.get(i), data.get(keys.get(i))));
		
	}
	
	/*****************************************
	 * Sets the data in the current column.
	 * 
	 * @param column
	 * @param data
	 */
	public boolean setVertexData(ArrayList<MguiNumber> data){
		String current = this.getCurrentColumn();
		if (current == null) return false;
		return setVertexData(current, data);
		
	}
	
	/*****************************************
	 * Sets the data in {@code column}.
	 * 
	 * @param column
	 * @param data
	 */
	public boolean setVertexData(String column, ArrayList<MguiNumber> data){
		return setVertexData(column, data, true);
	}
	
	/*****************************************
	 * Sets the data in {@code column}.
	 * 
	 * @param column
	 * @param data
	 * @param Whether to inform listeners of this update
	 */
	public boolean setVertexData(String column, ArrayList<MguiNumber> data, boolean update){
		
		VertexDataColumn v_column = this.getVertexDataColumn(column);
		return v_column.setValues(data, update);
	}
	
	/********************************
	 * Retrieves the currently selected vertex-wise data column.
	 * 
	 */
	public ArrayList<MguiNumber> getCurrentVertexData(){
		String column = getCurrentColumn();
		if (column == null) return null;
		return this.getVertexData(column);
	}
	
	/******************************************
	 * Returns the value at the given vertex, for the given linked column.
	 * 
	 * @param linked_column
	 * @param index
	 * @return
	 */
	public MguiNumber getLinkedVertexDatum(String linked_column, int index){
		String[] column_field = parseLinkColumn(linked_column);
		LinkedDataStream stream = getDataLink(column_field[0], column_field[1]);
		if (stream == null) return null;
		
		VertexDataColumn column = vertex_data.get(column_field[0]);
		NameMap name_map = getNameMap(column_field[0]);
		
		try{
			if (name_map == null)
				return new MguiDouble(stream.getNumericValue(column.getValueAtVertex(index).toString(), column_field[1]));
			else{
				
				String key = name_map.get((int)column.getValueAtVertex(index).getValue());
				if (key == null)
					return new MguiDouble(stream.getUnknownValue());
				else
					return new MguiDouble(stream.getNumericValue(key, column_field[2]));
				
				}
		}catch (DataSourceException e){
			
			}
		return null;
	}
	
	/********************************
	 * Retrieves the vertex-wise data associated with a data-linked column.  
	 * 
	 * @param linked_column
	 * @return
	 */
	public ArrayList<MguiNumber> getLinkedVertexData(String linked_column){
		VertexDataColumn v_column = getVertexDataColumn(linked_column);
		if (v_column == null) return null;
		String[] column_field = parseLinkColumn(linked_column);
		LinkedDataStream<?> stream = getDataLink(column_field[0], column_field[1]);
		if (stream == null) return null;
		ArrayList<MguiNumber> column_data = vertex_data.get(column_field[0]).getData();
		ArrayList<MguiNumber> linked_data = new ArrayList<MguiNumber>();
		NameMap name_map = getNameMap(column_field[0]);
		
		for (int i = 0; i < column_data.size(); i++){
			try{
				
				if (!v_column.isNameMapped(column_field[1])){
					LinkedDataStream<Integer> stream2 = (LinkedDataStream<Integer>)getDataLink(column_field[0], column_field[1]);
					linked_data.add(new MguiDouble(stream2.getNumericValue((int)column_data.get(i).getValue(), column_field[2])));
				}else{
					LinkedDataStream<String> stream2 = (LinkedDataStream<String>)getDataLink(column_field[0], column_field[1]);
					String key = name_map.get((int)column_data.get(i).getValue());
					if (key == null)
						linked_data.add(new MguiDouble(stream2.getUnknownValue()));
					else
						linked_data.add(new MguiDouble(stream2.getNumericValue(key, column_field[2])));
					}
			}catch (DataSourceException e){
				//this will occur usually if key is not in index; set to zero
				linked_data.add(new MguiDouble(stream.getUnknownValue()));
				}
			}
		return linked_data;
	}
	
	String[] parseLinkColumn(String s){
		String[] result = new String[3];
		result[0] = s.substring(0, s.indexOf(".{"));
		result[1] = s.substring(s.indexOf(".{") + 2, s.lastIndexOf("}"));
		result[2] = s.substring(s.indexOf("}") + 2);
		return result;
	}
	
	public boolean hasColumn(String s){
		String key = s;
		if (s.contains(".{"))
			key = parseLinkColumn(s)[0];
		return vertex_data.containsKey(key);
	}
	
	public void setCurrentColumn(String key){
		setCurrentColumn(key, true);
	}
	
	public void setCurrentColumn(String key, boolean update){
		ArrayList<MguiNumber> data = getVertexData(key);
		if (data == null) return;
		
		AttributeSelection<String> a = (AttributeSelection<String>)attributes.getAttribute("CurrentData");
		a.setValue(key, update);
		
	}
	
	/********************************************
	 * Returns a live version of the data in the current column.
	 * 
	 * @param column
	 * @return
	 */
	public ArrayList<MguiNumber> getVertexData(){
		String current = this.getCurrentColumn();
		if (current == null) return null;
		return getVertexData(current);
	}
	
	/********************************************
	 * Returns a live version of the data in <code>column</code>.  
	 * 
	 * @param column
	 * @return
	 */
	public ArrayList<MguiNumber> getVertexData(String column){
		if (column.contains(".{")) return getLinkedVertexData(column);
		if (!vertex_data.containsKey(column)) return null;
		return vertex_data.get(column).getData();
	}
	
	/********************************************
	 * Returns all vertex-wide data associated with this shape.  
	 * 
	 * @param column
	 * @return
	 */
	public ArrayList<ArrayList<MguiNumber>> getAllVertexData(){
		ArrayList<ArrayList<MguiNumber>> data = new ArrayList<ArrayList<MguiNumber>>(data_columns.size());
		
		for (int i = 0; i < data_columns.size(); i++)
			data.add(getVertexData(data_columns.get(i)));
		
		return data;
	}
	
	/**********************************************
	 * Returns the datum from the current column at <code>index</code>.
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	public MguiNumber getDatumAtVertex(int index){
		String column = this.getCurrentColumn();
		if (column == null) return null;
		return getDatumAtVertex(column, index);
	}
	
	/**********************************************
	 * Returns the datum from <code>column</code> at <code>index</code>.
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	public MguiNumber getDatumAtVertex(String column, int index){
		if (!hasColumn(column)) return null;
		if (column.contains(".{"))
			return this.getLinkedVertexDatum(column, index);
		return vertex_data.get(column).getValueAtVertex(index);
	}
	
	/**********************************************
	 * Sets the datum from the current column at <code>index</code>.
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVertex(int index, MguiNumber datum){
		String column = this.getCurrentColumn();
		if (column == null) return false;
		return setDatumAtVertex(column, index, datum);
	}
	
	/**********************************************
	 * Sets the datum from <code>column</code> at <code>index</code>.
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVertex(String column, int index, MguiNumber datum){
		if (!hasColumn(column)) return false;
		if (column.contains(".{"))
			return false;	// Can't update a linked column
		vertex_data.get(column).setValueAtVertex(index, datum);
		return true;
	}
	
	/**********************************************
	 * Sets the datum from the current column at <code>index</code>.
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVertex(int index, double datum){
		String column = this.getCurrentColumn();
		if (column == null) return false;
		return setDatumAtVertex(column, index, datum);
	}
	
	/**********************************************
	 * Sets the datum from <code>column</code> at <code>index</code>.
	 * 
	 * @param column
	 * @param index
	 * @return
	 */
	public boolean setDatumAtVertex(String column, int index, double datum){
		if (!hasColumn(column)) return false;
		if (column.contains(".{"))
			return false;	// Can't update a linked column
		vertex_data.get(column).setDoubleValueAtVertex(index, datum);
		return true;
	}
	
	public boolean hasData(){
		return vertex_data != null; 
	}
	
	public void showData(boolean b){
		attributes.setValue("ShowData", new MguiBoolean(b));
	}
	
	public boolean showData(){
		return ((MguiBoolean)attributes.getValue("ShowData")).getTrue();
	}
	
	/*********************************************
	 * Returns a list of this shape's data columns, minus the linked columns.
	 * 
	 * @return
	 */
	public ArrayList<String> getNonLinkedDataColumns(){
		ArrayList<String> cols = new ArrayList<String>();
		updateDataColumns();
		for (int i = 0; i < data_columns.size(); i++)
			if (!data_columns.get(i).contains(".{"))
				cols.add(data_columns.get(i));
		return cols;
	}
	
	protected void updateDataColumns(){
		data_columns.clear();
		if (vertex_data.size() == 0) return;
		
		ArrayList<String> cols = new ArrayList<String>(vertex_data.keySet());
		Collections.sort(cols);
		
		for (int i = 0; i < cols.size(); i++){
			String thisCol = cols.get(i);
			data_columns.add(thisCol);
			//linked data?
			VertexDataColumn column = vertex_data.get(thisCol);
			ArrayList<String> names = column.getLinkedDataNames();
			for (int j = 0; j < names.size(); j++){
				LinkedDataStream<?> stream = column.getLinkedData(names.get(j));
				ArrayList<String> fields = stream.getNumericFields();
				for (int k = 0; k < fields.size(); k++){
					data_columns.add(thisCol + ".{" + names.get(j) + "}." + fields.get(k));
					}
				}
			}
		
	}
	
/********** COLOUR MAP STUFF *********************/
	
	public void setDefaultColourMap(ColourMap cm){
		attributes.setValue("DefaultColourMap", cm, true);
	}
	
	public void setDefaultColourMap(ColourMap cm, boolean update){
		attributes.setValue("DefaultColourMap", cm, update);
	}
	
	/*************************************************
	 * Returns the {@linkplain ColourMap} associated with the current data column. If there is no
	 * associated colour map, returns the default colour map for this shape.
	 * 
	 * @return
	 */
	public ColourMap getColourMap(){
		ColourMap cmap = getColourMap(this.getCurrentColumn());
		if (cmap != null) return cmap;
		return getDefaultColourMap();
	}
	
	public ColourMap getDefaultColourMap(){
		return (ColourMap)attributes.getValue("DefaultColourMap");
	}
	
	/**************************************
	 * Sets the colour map for the current column.
	 * 
	 * @param column
	 * @param cm
	 */
	public void setColourMap(ColourMap cm){
		String column = this.getCurrentColumn();
		if (column == null) return;
		setColourMap(column, cm);
	}
	
	/**************************************
	 * Sets the colour map for {@code column}.
	 * 
	 * @param column
	 * @param cm
	 */
	public void setColourMap(String column, ColourMap cm){
		if (!hasColumn(column)) return;
		VertexDataColumn v_column = this.getVertexDataColumn(column);
		v_column.setColourMap(cm);
	}
	
	/**************************************
	 * Gets the colour map for {@code column}.
	 * 
	 * @param column
	 * @param cm
	 */
	public ColourMap getColourMap(String key){
		if (key == null || !hasColumn(key)) return getDefaultColourMap();
		VertexDataColumn column = getVertexDataColumn(key);
		if (column == null) return null;
		ColourMap cmap = column.getColourMap();
		if (cmap == null) return getDefaultColourMap();
		return cmap;
	}
	
	public void removeColourMap(String key){
		if (!hasColumn(key)) return;
		VertexDataColumn column = getVertexDataColumn(key);
		column.setColourMap(getDefaultColourMap());
	}
	
		
/********** NAME MAP STUFF ***********************/
	
	/**********************************
	 * Returns the name map associated with the current column, or 
	 * {@code null} if there is no column, or no name map associated with it.
	 * 
	 * @return
	 */
	public NameMap getNameMap(){
		if (getCurrentColumn() == null) return null;
		return getNameMap(getCurrentColumn());
	}
	
	/**********************************
	 * Returns the name map associated with {@code column}, or 
	 * {@code null} if there is no name map associated with it.
	 * 
	 * @return
	 */
	public NameMap getNameMap(String key){
		if (!hasColumn(key)) return null;
		if (key.contains(".{")) return null;		//TODO: name maps for linked columns?
		VertexDataColumn column = getVertexDataColumn(key);
		if (column == null) return null;
		return column.getNameMap();
	}
	
	/**********************************
	 * Sets the name map associated with column {@code key}.
	 * 
	 * @param key
	 * @param map
	 */
	public void setNameMap(String key, NameMap map){
		if (!hasColumn(key)) return;
		VertexDataColumn column = getVertexDataColumn(key);
		column.setNameMap(map);
		fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.General));
	}
	
	/**********************************
	 * Removes the name map associated with column {@code key}.
	 * 
	 * @param key
	 * @param map
	 */
	public void removeNameMap(String key){
		if (!hasColumn(key)) return;
		VertexDataColumn column = getVertexDataColumn(key);
		column.setNameMap(null);
		fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.General));
	}
	
	
/********** DATA LINK STUFF **********************/
	
	public LinkedDataStream<?> getDataLink(String column, String key){
		if (!hasColumn(column)) return null;
		VertexDataColumn v_column = getVertexDataColumn(column);
		return v_column.getLinkedData(key);
		//return linked_streams.get(column);
	}
	
	public void addDataLink(String column, String key, LinkedDataStream<?> link_stream){
		if (!hasColumn(column)) return;
		VertexDataColumn v_column = getVertexDataColumn(column);
		v_column.addDataLink(key, link_stream);
		updateDataColumns();
	
	}
	
	public void removeDataLink(String column, String key){
		//remove existing columns
		if (!hasColumn(column)) return;
		VertexDataColumn v_column = getVertexDataColumn(column);
		v_column.removeDataLink(key);
		
	}
	
	
/********** QUERY STUFF **************************/
	
	@Override
	public boolean queryObject(InterfaceQuery query){
		
		return false;
	}
	
	@Override
	public boolean queryShapeAtVertex(int vertex, InterfaceShapeQuery query){
		
		return false;
	}
	
	
/********** XML STUFF ****************************/
	
	//protected XMLEncoding 			xml_current_encoding;
	protected XMLType 				xml_current_type;
	protected ShapeInputOptions 	xml_current_shape_options;
	protected String 				xml_current_url;
	protected InterfaceShapeLoader 	xml_current_loader;
	protected Shape 				xml_current_shape;
	protected VertexDataColumn		xml_current_column;
	protected String 				xml_root_dir;
	protected boolean 				xml_is_vertex_data = false;
	
	public String getXML() {
		return getXML(0);
	}

	public String getXML(int tab) {
		return getXML(tab, XMLType.Full);
	}
	
	public String getXML(int tab, XMLType type){
		return "";
	}
	
	public String getXMLSchema() {
		
		return null;
	}

	
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException{
		
		if (localName.equals("VertexData")){
			if (xml_is_vertex_data)
				throw new SAXException("InterfaceShape.handleXMLElementStart: VertexData has already been started..");
			xml_is_vertex_data = true;
			return;
			}
		
		if (xml_current_shape != null){
			xml_current_shape.handleXMLElementStart(localName, attributes, type);
			return;
			}
		
		if (xml_current_column != null){
			xml_current_column.handleXMLElementStart(localName, attributes, type);
			return;
			}
		
		xml_current_type = type;
		
		// Handle a reference type element
		if (localName.equals("InterfaceShape")){
			this.setName(attributes.getValue("name"));
			switch (xml_current_type){
			
				case Full:
					// Shape is fully encoded in the XML source, prepare for accepting the data, which
					// must be handled by this object's shape's handleXMLString function.
					//xml_current_encoding = XMLFunctions.getEncodingForStr(attributes.getValue("encoding"));
					
					return;
					
				case Reference:
					// Shape is encoded at a referenced URL. The loader must be defined.
					xml_current_url = attributes.getValue("url");
					if (xml_root_dir != null)
						xml_current_url = xml_current_url.replace("{root}", xml_root_dir);
					
					String _loader = attributes.getValue("loader");
					
					// Get loader instance
					xml_current_loader = ShapeXMLFunctions.getLoaderInstance(_loader);
					if (xml_current_loader == null){
						xml_current_url = null;
						throw new SAXException("InterfaceShape.handleXMLElementStart: Could not instantiate " +
								 "a loader for the reference '" + _loader + "'.");
						}
					xml_current_loader.setFile(new File(xml_current_url));
					return;
				}
			return;
			}
		
		if (localName.equals("InterfaceIOOptions")){
			// Set I/O options here
			if (xml_current_loader == null)
				throw new SAXException("InterfaceShape.handleXMLElementStart: IOOptions started, but " + 
									   "no loader has been set..");
			
			InterfaceIOType complement = xml_current_loader.getWriterComplement();
			if (complement == null){
				InterfaceSession.log("InterfaceShape.handleXMLElementStart: No writer complement for loader" +
									 xml_current_loader.getClass().getCanonicalName() +"'; using defaults.", 
									 LoggingType.Warnings);
				return;
				}
			
			InterfaceIOOptions c_options = complement.getOptionsInstance();
			InterfaceIOOptions options = xml_current_loader.getIOType().getOptionsInstance();
			if (c_options == null || options == null || 
					!(options instanceof ShapeInputOptions)){
				xml_current_shape_options = null;
				xml_current_url = null;
				String _loader = xml_current_loader.getClass().getCanonicalName();
				xml_current_loader = null;
				throw new SAXException("InterfaceShape.handleXMLElementStart: Could not instantiate " +
						 			   "options for the loader '" + _loader + "'.");
				}
			
			xml_current_shape_options = (ShapeInputOptions)options;
			c_options.handleXMLElementStart(localName, 
											attributes, 
											null);
			xml_current_shape_options.setFromComplementaryOptions(c_options);
			return;
			}
		
		if (localName.equals("VertexDataColumn")){
			if (!xml_is_vertex_data)
				throw new SAXException("InterfaceShape.handleXMLElementStart: Vertex data column must occur " +
									   "within a VertexData block..");
			
			xml_current_column = new VertexDataColumn(attributes.getValue("name"));
			xml_current_column.handleXMLElementStart(localName, attributes, type);
			return;
			}
		
		Shape shape = getGeometryInstance();
		if (shape == null)
			throw new SAXException("InterfaceShape.handleXMLElementStart: ShapeInt " + this.getLocalName() +
								   " does not produce a geometry instance.");
		
		if (localName == shape.getLocalName()){
			// Starting to load this shape's XML
			xml_current_shape = shape;
			xml_current_shape.handleXMLElementStart(localName, attributes, type);
			return;
			}
		
	}
	
	@Override
	public void handleXMLElementEnd(String localName) throws SAXException{
		
		if (localName.equals("VertexData")){
			if (xml_current_column != null)
				throw new SAXException("InterfaceShape.handleXMLElementEnd: Vertex data ended but is still being loaded..");
			xml_is_vertex_data = false;
			return;
			}
		
		// If we're loading a shape, redirect unless this shape is ending
		if (xml_current_shape != null){
			if (localName.equals(xml_current_shape.getLocalName())){
				// Geometry should have already been loaded
				setGeometry(xml_current_shape);
				this.finalizeAfterXML();
				xml_current_shape = null;
				xml_current_type = null;
				return;
				}
			xml_current_shape.handleXMLElementEnd(localName);
			return;
			}
		
		// If we're loading a data column, redirect unless this column is ending
		if (xml_current_column != null){
			if (localName.equals("VertexDataColumn")){
				addVertexData(xml_current_column);
				xml_current_column = null;
				return;
				}
			xml_current_column.handleXMLElementEnd(localName);
			return;
			}
		
		if (localName.equals("InterfaceShape")){

			xml_current_loader = null;
			xml_current_url = null;
			xml_current_shape_options = null;
			
			return;
			}
		
		if (localName.equals("InterfaceIOOptions")){
			// Options are defined, so the load can be executed.
			if (xml_current_shape_options == null)
				throw new SAXException("InterfaceShape.handleXMLElementEnd: IOOptions ended, but " + 
						   			   "no options have been set..");
			try{
				InterfaceShape shape = xml_current_loader.loadShape(xml_current_shape_options, null);
				setGeometry(shape.getGeometry());
				this.finalizeAfterXML();
				xml_current_shape_options = null;
				xml_current_loader = null;
			}catch (IOException ex){
				throw new SAXException("InterfaceShape.handleXMLElementEnd: IOException loading referenced " + 
									   "shape..\nDetails: " + ex.getMessage());
				}
			
			return;
			}
		
		if (localName.equals("VertexData")){
			throw new SAXException("InterfaceShape.handleXMLElementEnd: Vertex data column ended without" +
								   " being started.");
			}
		
		
	}
	
	/*****************************************
	 * Allows subclasses to do some finalization after an XML load operation. Does nothing by
	 * default.
	 * 
	 */
	public void finalizeAfterXML(){
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException{
		
		if (xml_current_shape != null){
			xml_current_shape.handleXMLString(s);
			return;
			}
		
		if (xml_current_column != null){
			xml_current_column.handleXMLString(s);
			return;
			}
		
	}
	
	public void setXMLRoot(String root_dir){
		xml_root_dir = root_dir;
	}
	
	@Override
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, new XMLOutputOptions(), null);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		writeXML(tab, writer, new XMLOutputOptions(), null);
	}
	
	List<String> by_reference_urls = null;
	
	/****************************
	 * 
	 * Returns a URL string for the latest call to {@linkplain writeXML}. Is {@code null} if no call has yet
	 * been made, or the latest write was not by reference.
	 * 
	 * @return
	 */
	public List<String> getByReferenceUrls() {
		return by_reference_urls;
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
	
		by_reference_urls = null;
		
		XMLOutputOptions shape_options = null;
		ShapeModel3DOutputOptions model_options = null;
		if (options instanceof ShapeModel3DOutputOptions){
			model_options = (ShapeModel3DOutputOptions)options;
			shape_options = model_options.shape_xml_options.get(this);
			if (shape_options == null)
				throw new IOException("InterfaceShape: no XML options defined for shape " + this.getFullName() + ".");
		}else{
			shape_options = options;
			}
		
		XMLType type = shape_options.type;
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		//if "normal" type, check whether this shape has a source URL
		//if so, write only the reference to XML
		//otherwise, write fully
		if (type.equals(XMLType.Normal)){
			String url = getSourceURL();
			if (url != null && url.length() > 0 && !url.equals("n/a"))
				type = XMLType.Reference;
			else
				type = XMLType.Full;
			}
		
		String _type = XMLFunctions.getXMLStrForType(type);
				
		switch (type){
			case Full:
			case Short:
				writer.write(_tab + "<InterfaceShape \n" + 
						_tab2 + "class = '" + getClass().getCanonicalName() + "'\n" +
						_tab2 + "name = '" + getName() + "'\n" +
						_tab2 + "type = '" + _type + "'\n" + 
						_tab + ">\n");
				break;
			case Reference:
				//Shape shape = getGeometry();
				String url_ref = null, url_ref2 = null;
				InterfaceIOOptions io_options = null;
				FileWriter shape_writer = null;
				FileLoader shape_loader = null;
				if (model_options != null){
					
					shape_writer = shape_options.writer;
					if (shape_writer == null)
						throw new IOException("No shape writer assigned for shape '" + this.getFullName() + "'.");
					
					// Set up from model
					String filename = shape_options.filename;
					
					// Add extension if not defined
					List<String> exts = shape_writer.getIOType().getExtensions();
					if (exts.size() > 0) {
						boolean has_ext = false;
						for (String ext : exts) {
							if (filename.endsWith("." + ext)) {
								has_ext = true;
								break;
								}
							}
						if (!has_ext) {
							filename = filename + "." + exts.get(0);
							}
						}
					
					File root_dir = options.getFiles()[0].getParentFile();
					File shapes_dir = IoFunctions.fullFile(root_dir, model_options.shapes_folder);
					File file = IoFunctions.fullFile(shapes_dir, filename);
					if (file.exists() && !model_options.overwrite_existing){
						InterfaceSession.log("InterfaceShape.writeXML: Skipping existing file '" + file.getAbsolutePath() +"'.", 
											 LoggingType.Warnings);
						return;
						}
					//File parent_dir = file.getParentFile();
					if (!shapes_dir.exists() && !IoFunctions.createDirs(shapes_dir))
						throw new IOException("Could not create the path to '" + file.getAbsolutePath() + "'.");
					url_ref = "{root}" + File.separator + model_options.shapes_folder + File.separator + filename;
					by_reference_urls = new ArrayList<String>();
					by_reference_urls.add(url_ref);
					url_ref2 = file.getAbsolutePath();
					io_options = shape_options.io_options;
					//io_options = model_options.shape_io_options.get(this);
					if (io_options == null){
						// Get default options
						io_options = shape_writer.getIOType().getOptionsInstance();
						}
					if (shape_writer.getLoaderComplement() == null)
						throw new IOException("Shape writer '" + shape_writer.getClass().getCanonicalName() + 
											  "' has no complementary loader.");
					shape_loader = (FileLoader)shape_writer.getLoaderComplement().getIOInstance();
				}else{
					// Get from shape itself
					url_ref = (String)attributes.getValue("UrlReference");
					if (url_ref == null || url_ref == ""){
						throw new IOException("Shape [" + this.getClass().getCanonicalName() + ": " + 
											  this.getName() + "]: URL reference not set for XML write of type 'reference'.");
						}
					shape_writer = this.getFileWriter();
					if (shape_writer == null){
						throw new IOException("Shape [" + this.getClass().getCanonicalName() + ": " + 
											  this.getName() + "]: No file writer set for XML write of type 'reference'.");
						}
					shape_loader = (FileLoader)shape_writer.getLoaderComplement().getIOInstance();
					if (shape_loader == null)
						throw new IOException("Shape writer '" + shape_writer.getClass().getCanonicalName() + 
											  "' has no complementary loader.");
					io_options = shape_writer.getIOType().getOptionsInstance();
					
					// Set up the URL, replace the full path with the root tag, if applicable
					if (this.getModel() != null){
						String root_dir = (String)getModel().getModelSet().getAttribute("RootURL").getValue();
						if (root_dir != null && root_dir.length() > 0){
							if (!root_dir.endsWith(File.separator))
								root_dir = root_dir + File.separator;
							url_ref2 = url_ref.replace(root_dir, "{root}" + File.separator);
							}
						}
					}
				
				writer.write(_tab + "<InterfaceShape \n" + 
							 _tab2 + "class = '" + getClass().getCanonicalName() + "'\n" +
							 _tab2 + "name = '" + getName() + "'\n" +
							 _tab2 + "type = '" + _type + "'\n" + 
							 _tab2 + "url='" + url_ref + "'\n" +
							 _tab2 + "loader='" + shape_loader.getClass().getCanonicalName() + "'\n" +
							 _tab2 + "writer='" + shape_writer.getClass().getCanonicalName() + "'\n" +
							 _tab + ">\n");
			
				// Write the options
				if (io_options != null){
					io_options.writeXML(tab+1, writer, progress_bar);
					writer.write("\n");
					}
				
				// Now write the data
				try{
					io_options.setObject(this);
				}catch (ClassCastException ex){
					throw new IOException("Shape [" + this.getClass().getCanonicalName() + ": " +  
							  			  "FileWriter's setObject class must be implemented.");
					}
				if (url_ref.startsWith("file:"))
					url_ref = url_ref.substring(6);
				File output_file = new File(url_ref2);
				io_options.setFiles(new File[]{output_file});
				if (progress_bar != null){
					progress_bar.setMessage("Writing '" + getName() + "':");
					}
				
				// Does parent directory exist?
				File parent_dir = output_file.getParentFile();
				if (!parent_dir.exists()){
					if (!IoFunctions.createDirs(parent_dir)){
						throw new IOException("Shape [" + this.getClass().getCanonicalName() + "]: " +  
		  			  			  "parent directory '" + parent_dir.getAbsolutePath() + " cannot be created.");
						}
					}
				
				if (!shape_writer.write(io_options, progress_bar)){
					throw new IOException("Shape [" + this.getClass().getCanonicalName() + "]: " +  
				  			  			  "FileWriter failed to write shape.");
					}
				break;
			default:
				break;
			}
		
		//Shape writes its XML here if necessary
		switch (type){
			case Full:
				//writer.write("\n");
				getGeometry().writeXML(tab + 1, writer, shape_options, progress_bar);
				break;
			case Short:
				//writer.write("\n");
				writer.write(getGeometry().getShortXML(tab + 1));
				break;
			default:
				break;
			}
		
		// Vertex data here
		if (type == XMLType.Full && this.vertex_data.size() > 0){
			
			writer.write("\n" + _tab2 + "<VertexData>\n");
			
			ArrayList<String> names = getVertexDataColumnNames();
			for (int i = 0; i < names.size(); i++){
				VertexDataColumn column = getVertexDataColumn(names.get(i));
				column.writeXML(tab + 2, writer, shape_options, progress_bar);
				}
			
			writer.write(_tab2 + "</VertexData>\n");
			}
		
		// Attributes are last because they may need to load last
		if (attributes != null){
			attributes.writeXML(tab + 1, writer, progress_bar);
			}
		
		//close up
		writer.write("\n" + _tab + "</InterfaceShape>");
		
	}
	
	
/******************* TREE NODE STUFF ***********************/
	
	public void cleanTreeNodes(){
		for (int i = 0; i < tree_nodes.size(); i++)
			if (tree_nodes.get(i).isDestroyed()){
				tree_nodes.remove(i--);
				}
	}
	
	/**************************************************
	 * Constructs a tree node from this shape. Adds an {@link AttributeTreeNode} via the <code>super</code>
	 * method, and also adds a node to display the vertex-wise data columns associated with this ShapeInt.
	 * 
	 * <p>If overriding this method, subclass implementations should first call this super method to initialize
	 * the node and provide a basic construction.
	 * 
	 * @param treeNode the tree node to construct
	 */
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		if (!hasData()) return;
		
		ArrayList<String> list = new ArrayList<String>(vertex_data.keySet());
		//if (list == null) return;
		
		InterfaceTreeNode node = new InterfaceTreeNode(new VertexDataSet("Vertex Data"));

		for (int i = 0; i < list.size(); i++)
			node.add(vertex_data.get(list.get(i)).issueTreeNode());
		
		treeNode.add(node);
	}
	
	public class VertexDataSet extends AbstractInterfaceObject implements IconObject{
		
		public VertexDataSet(String name){
			setName(name);
		}
		
		@Override
		public Icon getObjectIcon(){
			java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/vertex_data_set_20.png");
			if (imgURL != null)
				return new ImageIcon(imgURL);
			else
				InterfaceSession.log("Cannot find resource: /mgui/resources/icons/vertex_data_set_20.png");
			return null;
		}
		
		@Override
		public String getTreeLabel(){
			return getName();
		}
		
	}
	
	
/******************* LISTENER STUFF ***********************/
	
	/***********************************************
	 * Notifies this shape that it has been modified and should inform its listeners 
	 * 
	 */
	public void fireShapeModified(){
		ShapeEvent e = new ShapeEvent(this, ShapeEvent.EventType.ShapeModified);
		fireShapeListeners(e);
	}
	
	protected void fireShapeListeners(ShapeEvent e){
		ArrayList<ShapeListener> currentListeners = new ArrayList<ShapeListener>(shapeListeners);
		int j = 0;
		for (int i = 0; i < currentListeners.size(); i++)
			if (currentListeners.get(i).isDestroyed())
				shapeListeners.remove(j);
			else
				j++;
		
		currentListeners = new ArrayList<ShapeListener>(shapeListeners);
		for (int i = 0; i < currentListeners.size(); i++){
			currentListeners.get(i).shapeUpdated(e);
			}
		
		//fire tree node listeners; remove any which are destroyed
		
		ArrayList<InterfaceTreeNode> temp = new ArrayList<InterfaceTreeNode>(tree_nodes);
		for (int i = 0; i < temp.size(); i++)
			if (temp.get(i).isDestroyed())
				tree_nodes.remove(temp.get(i));
			else
				((Shape3DTreeNode)temp.get(i)).shapeUpdated(e);
		
		last_column_added = null;
		last_column_removed = null;
		last_column_changed = null;
	}
	
	public void addShapeListener(ShapeListener thisListener){
		for (int i = 0; i < shapeListeners.size(); i++)
			if (shapeListeners.get(i).equals(thisListener)) return;
		shapeListeners.add(thisListener);
	}
	
	public void removeShapeListener(ShapeListener thisListener){
		shapeListeners.remove(thisListener);
	}
	
/******************* VARIABLES STUFF **********************/
	
	/******************
	 * Returns this shape's columns as a list of variables. Note that the names
	 * will have been altered to remove special characters, including spaces, 
	 * operators, and brackets
	 * 
	 */
	public ArrayList<String> getVariables() {
		ArrayList<String> vars = new ArrayList<String>();
		vars.add(toVariable("Coords.x"));
		vars.add(toVariable("Coords.y"));
		vars.add(toVariable("Coords.z"));
		Iterator<String> itr = vertex_data.keySet().iterator();
		while (itr.hasNext())
			vars.add(toVariable(itr.next()));
		Collections.sort(vars);
		return vars;
	}
	
	/********************
	 * Converts a column name to a legitimate variable name
	 * 
	 * @param string
	 * @return
	 */
	public static String toVariable(String string){
		string = string.replace("__", "__du__");
		string = string.replace(" ", "__s__");
		string = string.replace("-", "__m__");
		string = string.replace("+", "__p__");
		string = string.replace("*", "__t__");
		string = string.replace("/", "__d__");
		string = string.replace("(", "__l__");
		string = string.replace(")", "__r__");
		string = string.replace("[", "__ls__");
		string = string.replace("]", "__rs__");
		string = string.replace("{", "__lc__");
		string = string.replace("}", "__rc__");
		string = string.replace(",", "__c__");
		string = string.replace(".", "__dt__");
		string = string.replace(";", "__sc__");
		string = string.replace(":", "__co__");
		return string;
	}
	
	/********************
	 * Converts a variable name back to its original column name
	 * 
	 * @param string
	 * @return
	 */
	public static String fromVariable(String string){
		string = string.replace("__s__", " ");
		string = string.replace("__m__", "-");
		string = string.replace("__p__", "+");
		string = string.replace("__d__", "/");
		string = string.replace("__t__", "*");
		string = string.replace("__l__", "(");
		string = string.replace("__r__", ")");
		string = string.replace("__ls__", "[");
		string = string.replace("__rs__", "]");
		string = string.replace("__lc__", "{");
		string = string.replace("__rc__", "}");
		string = string.replace("__c__", ",");
		string = string.replace("__sc__", ";");
		string = string.replace("__co__", ":");
		string = string.replace("__dt__", ".");
		string = string.replace("__du__", "__");
		return string;
	}

	@Override
	public double getVariableValue(String variable, int[] element) {
		variable = fromVariable(variable);
		if (variable.startsWith("Coords.")){
			Shape shape = getGeometry();
			if (shape instanceof Shape2D){
				Point2f vertex = ((Shape2D)shape).getVertex(element[0]);
				if (variable.endsWith("x"))
					return vertex.getX();
				if (variable.endsWith("y"))
					return vertex.getY();
			}else if (shape instanceof Shape3D){
				Point3f vertex = ((Shape3D)shape).getVertex(element[0]);
				if (variable.endsWith("x"))
					return vertex.getX();
				if (variable.endsWith("y"))
					return vertex.getY();
				if (variable.endsWith("z"))
					return vertex.getZ();
				}
			return Double.NaN;
			}
		if (!hasColumn(variable)) return Double.NaN;
		ArrayList<MguiNumber> data = getVertexData(variable);
		return data.get(element[0]).getValue();
	}

	public Object getVariableValues(String variable){
		variable = fromVariable(variable);
		if (!hasColumn(variable)) return null;
		return new ArrayList<MguiNumber>(getVertexData(variable));
	}
	
	public boolean setVariableValues(String variable, Object values){
		return setVariableValues(variable, values, null);
	}
	
	public boolean setVariableValues(String variable, Object values, VertexSelection selection){
		
		variable = fromVariable(variable);
		
		try{
			double[] vals = (double[])values;
			ArrayList<MguiNumber> column = getVertexData(variable);
			if (column == null){
				InterfaceSession.log("InterfaceShape.setVariableValues: No variable called '" + variable + "'.");
				return false;
			}
			int m = Math.min(vals.length, column.size());
			for (int i = 0; i < m; i++)
				if (selection == null || selection.isSelected(i))
					column.get(i).setValue(vals[i]);
			//attributeUpdated(new AttributeEvent(attributes.getAttribute("ShowData")));
			VertexDataColumn v_column = this.getVertexDataColumn(variable);
			v_column.resetDataLimits(true);
			//v_column.fireDataColumnChanged(new VertexDataColumnEvent(v_column, EventType.ColumnChanged));
			
			return true;
		}catch (ClassCastException ex){
			InterfaceSession.log("Invalid value array passed to InterfaceShape.setVariableValues()");
			//ex.printStackTrace();
			return false;
			}
		
	}
	
	public Class<?> getVariableType(){
		ArrayList<MguiNumber> list = new ArrayList<MguiNumber>();
		return list.getClass();
	}
	
	//TODO: implement a means of type-checking lists against one another
	//		this is an erasure issue (see http://bugs.sun.com/view_bug.do?bug_id=5098163)
	public boolean supportsVariableType(Class<?> type){
		if (getVariableType().isAssignableFrom(type)) return true;
		return false;
	}
	
	
/******************* MISC STUFF ***************************/
	
	/*************************************************************
	 * Returns a list of the currently selected vertices for this shape. 
	 * 
	 */
	public abstract VertexSelection getVertexSelection();
	
	public abstract void setVertexSelection(VertexSelection selection);
	
	/**********************************
	 * Returns an icon for this shape, if one has been defined. Returns null otherwise.
	 */
	@Override
	public Icon getObjectIcon(){
		if (icon == null) setIcon();
		return icon;
	}
	
	//override to set a specific icon
	protected void setIcon(){
		java.net.URL imgURL = InterfaceShape.class.getResource("/mgui/resources/icons/shape_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/shape_20.png");
	}
	
	public ShapeModel3D getModel(){
		if (parent_set == null){
			//System.out.println("Parent set is null for shape '" + this.getName() + "'.");
			return null;
		}
		//System.out.println("Parent set is '" + parent_set.getName() + " for shape '" + this.getName() + "'.");
		return parent_set.getModel();
	}
	
	/****************************************************
	 * Sets this shape's parent set to <code>set</code>. If this shape already has a parent, removes itself from
	 * the existing parent, including the shape listener.
	 * 
	 * @param set
	 */
	public void setParentSet(ShapeSet set){
		if (parent_set != null)
			parent_set.removeShape(this);
		parent_set = set;
		
		if (set != null)
			this.addShapeListener(parent_set);
	}
	
	public ShapeSet getParentSet(){
		return parent_set;
	}
	
	public long getID(){
		return ID;
	}
	
	public void setID(long id){
		ID = id;
	}
	
	public int compareTo(InterfaceShape s){
		if (getID() > getID()) return 1;
		if (getID() < getID()) return -1;
		return 0;
	}
	
	public void setConstraint(int i, boolean c){
		constraints[i] = c;
	}
	
	public boolean getConstraint(int i){
		return constraints[i];
	}
	
	public boolean[] getConstraints(){
		return this.constraints;
	}
	
//	/*****************************************
//	 * Returns the spatial unit for this section set. This is the unit of the parent set, if one is set;
//	 * otherwise it is the default from <code>InterfaceEnvironment</code>. 
//	 * 
//	 */
//	public SpatialUnit getUnit(){
//		if (getParentSet() != null){
//			SpatialUnit unit = getParentSet().getUnit();
//			if (unit != null) return unit;
//			}
//		return InterfaceEnvironment.getDefaultSpatialUnit();
//	}
	
	
	
	
}