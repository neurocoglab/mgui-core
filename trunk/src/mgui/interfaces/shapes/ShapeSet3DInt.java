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

import java.awt.Graphics2D;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.ModelClip;
import javax.media.j3d.Node;
import javax.media.j3d.SharedGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.TransferHandler.TransferSupport;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import mgui.geometry.Box3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Polygon3D;
import mgui.geometry.Sphere3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.geometry.util.SpatialUnit;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.tree.AttributeTreeNode;
import mgui.interfaces.graphics.GraphicEvent;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.graphics.InterfaceGraphicListener;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.maps.Camera3DListener;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeEvent.EventType;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.domestic.shapes.ShapeModel3DOutputOptions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;

/**********************************************************
 * Represents a set of <code>Shape3DInt</code> objects. This class is itself a descendant of <code>Shape3DInt</code>,
 * which means it inherits much of its behaviour and attributes, and can be added to another <code>ShapeSet3DInt</code>.
 * Shapes added to a shape set inherit its spatial unit; thus, all shapes in a set must be defined in the same unit.
 * 
 * <p>See <a href="http://mgui.wikidot.com/shape-sets-dev">Development Notes: Shape Sets</a> for a more detailed
 * description.
 * 
 * <p>TODO: Also implement and enforce set-wide coordinate systems
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeSet3DInt extends Shape3DInt implements ShapeSet,
														 InterfaceGraphicListener{

	public ArrayList<Shape3DInt> members; 
	public SharedGroup sharedNode;		
	ArrayList<InterfaceGraphic<?>> windows = new ArrayList<InterfaceGraphic<?>>();
	protected ArrayList<Camera3D> registered_cameras = new ArrayList<Camera3D>();
	
	public ShapeModel3D model;
	
	TreeSet<InterfaceGraphic2D> windows_2d = new TreeSet<InterfaceGraphic2D>();
	protected BranchGroup sections_node;
	protected HashMap<InterfaceGraphic2D, Polygon3DInt> section_polygons = new HashMap<InterfaceGraphic2D, Polygon3DInt>();
	protected InterfaceGraphic2D last_section_added, last_section_removed;
	HashMap<BranchGroup, BranchGroup> clipNodes = new HashMap<BranchGroup, BranchGroup>();
	HashMap<SectionSet3DInt, ModelClip> clipModels = new HashMap<SectionSet3DInt, ModelClip>();
	
	
	Shape3DInt last_added = null;
	Shape3DInt last_removed = null;
	Shape3DInt last_modified = null;
	Shape3DInt last_inserted = null;
	int last_insert = -1;
	
	public ShapeSet3DInt(){
		init();
	}
	
	public ShapeSet3DInt(String name){
		init();
		setName(name);
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/shape_set_3d_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/shape_set_3d_20.png");
	}
	
	@Override
	protected void init(){
		super.init();
		
		members = new ArrayList<Shape3DInt>();
		updateShape();
		
		//todo section parameters probably don't belong here
		attributes.add(new Attribute<MguiBoolean>("IsOverriding", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiBoolean>("3D.ShowSections", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("3D.FillSections", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiFloat>("3D.SectionAlpha", new MguiFloat(0.9f)));
		attributes.add(new Attribute<String>("RootURL", ""));
		attributes.add(new AttributeSelection<SpatialUnit>("Unit", InterfaceEnvironment.getSpatialUnits(), SpatialUnit.class));
		setUnit(InterfaceEnvironment.getSpatialUnit("meter"));
		
	}
	
	public InterfaceGraphic2D getLastSectionAdded(){
		return this.last_section_added;
	}
	
	public InterfaceGraphic2D getLastSectionRemoved(){
		return this.last_section_removed;
	}
	
	/*******************************************
	 * Returns a copy of this set's member list.
	 * 
	 * @return a list of members
	 */
	@Override
	public ArrayList<InterfaceShape> getMembers(){
		return new ArrayList<InterfaceShape>(members);
	}
	
	/*******************************************
	 * Returns a copy of this set's member list.
	 * 
	 * @param recursive  	If true, returns all subset members as well
	 * @return a list of members
	 */
	public ArrayList<InterfaceShape> getMembers(boolean recursive){
		if (!recursive) return getMembers();
		
		ArrayList<InterfaceShape> copy = new ArrayList<InterfaceShape>();
		
		// First add not-set members
		for (int i = 0; i < members.size(); i++){
			InterfaceShape shape = members.get(i);
			if (!(shape instanceof ShapeSet3DInt)){
				copy.add(members.get(i));
				}
			}
		
		// Now add set members recursively
		for (int i = 0; i < members.size(); i++){
			InterfaceShape shape = members.get(i);
			if (shape instanceof ShapeSet3DInt){
				copy.addAll(((ShapeSet3DInt)shape).getMembers(true));
				}
			}
		
		return copy;
	}
	
	@Override
	public InterfaceShape getLastAdded(){
		return last_added;
	}
	
	@Override
	public InterfaceShape getLastRemoved(){
		return last_removed;
	}
	
	@Override
	public InterfaceShape getLastInserted(){
		return last_inserted;
	}
	
	@Override
	public int getLastInsert(){
		return last_insert;
	}
	
	@Override
	public InterfaceShape getLastModified(){
		return last_modified;
	}
	
	/****************************************
	 * Returns the index of <code>shape</code> in this set.
	 * 
	 * @return index of shape, or -1 if it is not in this set
	 */
	@Override
	public int getIndexOf(InterfaceShape shape){
		for (int i = 0; i < members.size(); i++)
			if (members.get(i).equals(shape)) return i;
		return -1;
	}
	
	/***********************************************
	 * Returns the spatial unit for this shape set.
	 * 
	 * @return the spatial unit for this shape set
	 */
	@Override
	public SpatialUnit getUnit(){
		return (SpatialUnit)attributes.getValue("Unit");
	}
	
	public void setUnit(SpatialUnit unit){
		attributes.setValue("Unit", unit);
	}
	
	/**************************
	 * If attr is non-null, sets override attributes for all members of this set, 
	 * and sets isOverridden to true.
	 * @param attr AttributeList with which to override the members of this set
	 */
	@Override
	public void setOverride(AttributeList attr){
		if (attr != null){
			overrideAttr = attr;
			isOverridden = true;
			//set for all members
			for (int i = 0; i < members.size(); i++)
				members.get(i).setOverride(attr);
			return;
			}
		for (int i = 0; i < members.size(); i++)
			members.get(i).setOverride(null);
		isOverridden = false;
	}
	
	/**************************
	 * Unsets the override on this set. If this set is overriding, itself, this override
	 * will be reapplied to its members.
	 */
	@Override
	public void unsetOverride(){
		isOverridden = false;
		if (isOverriding()){
			applyOverride();
			return;
			}
		//otherwise unset override for all members
		for (int i = 0; i < members.size(); i++)
			members.get(i).unsetOverride();
		
	}
	
	/***********************************************
	 * Sets this shape set's current model. This is only necessary for a top-level shape set (i.e., the model's 
	 * base set). Any members, including other instances of <code>ShapeSet3DInt</code>, will access this model
	 * through the {@link getModel} method.
	 * 
	 * <p>TODO: reconsider the access modifier for this method; setting models should possibly be restricted to
	 * internal processes.
	 * 
	 * @param model_node The model to set as this set's parent
	 */
	public void setModel(ShapeModel3D m){
		setModel(m, true);
	}
	
	/***********************************************
	 * Sets this shape set's current model. This is only necessary for a top-level shape set (i.e., the model's 
	 * base set). Any members, including other instances of <code>ShapeSet3DInt</code>, will access this model
	 * through the {@link getModel} method.
	 * 
	 * <p>TODO: reconsider the access modifier for this method; setting models should possibly be restricted to
	 * internal processes.
	 * 
	 * @param model The model to set as this set's parent
	 * @param setID Whether to set a shape ID for this shape
	 */
	public void setModel(ShapeModel3D model, boolean setID){
		this.model = model;
	}
	
	@Override
	public void register(){
		super.register();
		for (int i = 0; i < members.size(); i++)
			members.get(i).register();
	}
	
	/**************************
	 * Applies this set's attributes to all members.
	 *
	 */
	public void applyOverride(){
		//((MguiBoolean)attributes.getValue("IsOverriding")).value = true;
		attributes.setValue("IsOverriding", new MguiBoolean(true));
		
		//apply to each member
		for (int i = 0; i < members.size(); i++)
			members.get(i).setOverride(attributes);
	}
	
	/**************************
	 * Removes this set's override; all members are rendered according to their own attributes.
	 *
	 */
	public void removeOverride(){
		//((MguiBoolean)attributes.getValue("IsOverriding")).value = false;
		attributes.setValue("IsOverriding", new MguiBoolean(false));
		
		//apply to each member
		for (int i = 0; i < members.size(); i++)
			members.get(i).unsetOverride();
	}
	
	/**************************
	 * Query whether this set is currently overriding its members' attributes.
	 *
	 * @return <code>true</code> if this set is overriding
	 */
	public boolean isOverriding(){
		return ((MguiBoolean)attributes.getValue("IsOverriding")).getTrue();
	}
	
	@Override
	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist, Matrix4d transform){
		if (!((MguiBoolean)attributes.getValue("2D.ShowBounds")).getTrue()) return null;
		if (boundBox == null) return null;
		Box3D bounds = boundBox;
		if (transform != null){
			GeometryFunctions.transform((Box3D)bounds.clone(), transform);
			}
		Polygon2DInt poly = new Polygon2DInt(ShapeFunctions.getIntersectionPolygon(bounds, plane));
		poly.attributes.setValue("LineColour", attributes.getValue("2D.BoundsColour"));
		return poly;
	}
	
	@Override
	public Shape2DInt getShape2DInt(Plane3D plane, float above_dist, float below_dist, boolean listen){
		if (members.size() == 0) return null;
		//if (members.size() == 0 || boundBox == null) return null;
		if (!isVisible() || !show2D()) return null;
		ShapeSet2DInt set = new ShapeSet2DInt();
		set.setParentShape(this);
		Shape2DInt bounds = getShape2D(plane, above_dist, below_dist);
		if (bounds != null) set.addShape(bounds, true, false);
		for (int i = 0; i < members.size(); i++){
			Shape2DInt shape = members.get(i).getShape2DInt(plane, above_dist, below_dist, listen);
			if (shape != null){
				set.addShape(shape, true, false);
				}
			}
		return set;
	}
	
	public void registerCamera(Camera3D c){
		Camera3DListener cl;
		registered_cameras.add(c);
		for (int i = 0; i < members.size(); i++)
			if (ShapeSet3DInt.class.isInstance(members.get(i))){
				((ShapeSet3DInt)members.get(i)).registerCamera(c);
			}else if (members.get(i).hasCameraListener){
				cl = members.get(i).getCameraListener();
				if (cl != null)
					c.removeListener(cl);
				members.get(i).registerCameraListener(c);
				}
	}
	
	public void registerCameras(ArrayList<Camera3D> cameras){
		for (int i = 0; i < cameras.size(); i++)
			registerCamera(cameras.get(i));
	}
	
	public void deregisterCameras(ArrayList<Camera3D> cameras){
		for (int i = 0; i < cameras.size(); i++)
			deregisterCamera(cameras.get(i));
	}
	
	public void deregisterCamera(Camera3D c){
		registered_cameras.remove(c);
		for (int i = 0; i < members.size(); i++)
			if (ShapeSet3DInt.class.isInstance(members.get(i)))
				((ShapeSet3DInt)members.get(i)).deregisterCamera(c);
			else if (members.get(i).hasCameraListener){
				members.get(i).deregisterCameraListener(c);
				}
	}
	
	/***************************************************
	 * Determines whether the {@code i}th member of this set is visible
	 * 
	 * @param i
	 * @return
	 */
	public boolean isVisible(int i){
		if (i >= members.size()) return false;
		return members.get(i).isVisible();
	}
	
	@Override
	public int getSize(){
		return members.size();
	}
	
	/******************************************************
	 * Returns the {@code i}th member of this set
	 * 
	 * @param i
	 * @return
	 */
	public Shape3DInt getMember(int i){
		return getShape(i);
	}
	
	/******************************************************
	 * Returns the {@code i}th member of this set
	 * 
	 * @param i
	 * @return
	 */
	public Shape3DInt getShape(int i){
		if (i >= members.size()) return null;
		return members.get(i);
	}

	@Override
	public Set<InterfaceShape> getShapeSet() {
		return new TreeSet<InterfaceShape>(members);
	}
	
	public Set<ShapeSet> getSubSets(){
		TreeSet<ShapeSet> subsets = new TreeSet<ShapeSet>();
		for (int i = 0; i < members.size(); i++)
			if (members.get(i) instanceof ShapeSet)
				subsets.add((ShapeSet)members.get(i));
		return subsets;
	}
	
	/*****************************************************
	 * Determines whether a shape with the name {@code name} is in this set. 
	 * Does a recursive search of all subsets.
	 * 
	 * @param name
	 * @return
	 */
	public boolean hasShape(String name){
		return hasShape(name, true);
	}

	/*****************************************************
	 * Determines whether a shape with the name {@code name} is in this set. 
	 * 
	 * @param name
	 * @param recurse 		Whether to do a recursive search of all subsets.
	 * @return
	 */
	public boolean hasShape(String name, boolean recurse){
		for (int i = 0; i < members.size(); i++){
			if (members.get(i).getName().equals(name))
				return true;
			if (recurse && 
				members.get(i) instanceof ShapeSet3DInt &&
				((ShapeSet3DInt)members.get(i)).hasShape(name))
				return true;
			}
		return false;
	}
	
	/***********************************
	 * Searches this set for a shape named <code>name</code> and returns the first
	 * instance found. Does not search subsets; see {code getShape(String name, boolean recurse)}.
	 * 
	 * @param name
	 * @return
	 */
	public Shape3DInt getShape(String name){
		return getShape(name, false);
	}
	
	/***********************************
	 * Searches this set for a shape named <code>name</code> and returns the first
	 * instance found. Recurse indicates that all subsets should also be searched.
	 * 
	 * @param name
	 * @param recurse
	 * @return
	 */
	public Shape3DInt getShape(String name, boolean recurse){
		for (int i = 0; i < members.size(); i++){
			if (members.get(i).getName().equals(name))
				return members.get(i);
			if (recurse && 
				members.get(i) instanceof ShapeSet3DInt){
				Shape3DInt shape = ((ShapeSet3DInt)members.get(i)).getShape(name, recurse);
				if (shape != null) return shape;
				}
			}
		return null;
	}
	
	/************************************************
	 * Merge {@code set} with this set. Merge of subordinate sets is done recursively. Shapes are renamed
	 * if shapes already exist by the same name.
	 * 
	 * @param set
	 */
	public void mergeWithSet(ShapeSet3DInt set){
		mergeWithSet(set, "rename", true);
	}
	

	/************************************************
	 * Merge {@code set} with this set. Merge of subordinate sets is done recursively.
	 * 
	 * @param set 				Set to merge with this one
	 * @param existing_shapes 	If a shape in this set has the same name as one being merged,
	 * 							do we rename it ({@code "rename"}), skip it ({@code "skip"}),
	 * 							or replace it ({@code "overwrite"})?
	 */
	public void mergeWithSet(ShapeSet3DInt set, String existing_shapes){
		mergeWithSet(set, existing_shapes, true);
	}
	
	/************************************************
	 * Merge {@code set} with this set. Merge of subordinate sets is done recursively.
	 * 
	 * @param set 				Set to merge with this one
	 * @param existing_shapes 	If a shape in this set has the same name as one being merged,
	 * 							do we rename it ({@code "rename"}), skip it ({@code "skip"}),
	 * 							or replace it ({@code "overwrite"})?
	 */
	public void mergeWithSet(ShapeSet3DInt set, String existing_shapes, boolean update){
		
		ArrayList<InterfaceShape> members = set.getMembers();
		for (int i = 0; i < members.size(); i++){
			Shape3DInt shape = (Shape3DInt)members.get(i);
			if (shape instanceof ShapeSet3DInt){
				Shape3DInt target = this.getShape(shape.getName(), false);
				if (target != null && target instanceof ShapeSet3DInt){
					((ShapeSet3DInt)target).mergeWithSet((ShapeSet3DInt)shape, existing_shapes);
				}else{
					if (target != null){
						// Shape exists with this name but it is not a shape set;
						// must find unique name
						if (existing_shapes.toLowerCase().equals("overwrite")){
							this.removeShape(target, true, false);
							target.destroy();
							addShape(shape, true, false);
							InterfaceSession.log("ShapeSet3DInt.mergeSet: Shape with name '" + target.getFullName() + 
									 "' already exists; overwriting..", 
									 LoggingType.Warnings);
							
						}else if (existing_shapes.toLowerCase().equals("skip")){
							InterfaceSession.log("ShapeSet3DInt.mergeSet: Shape with name '" + target.getFullName() + 
												 "' already exists; skipping..", 
												 LoggingType.Warnings);
						}else{
							String new_name = this.getUniqueName(shape.getName());
							shape.setName(new_name);
							addShape(shape, true, false);
							InterfaceSession.log("ShapeSet3DInt.mergeSet: Shape with name '" + target.getFullName() + 
									 			 "' already exists; renaming to '" + new_name + "'..", 
									 			 LoggingType.Warnings);
							}
						
					}else{
						// Target set not found, add a new one
						addShape(shape, true, false);
						}
					}
			}else{
				// Add this shape
				if (hasShape(shape.getName())){
					if (existing_shapes.toLowerCase().equals("overwrite")){
						Shape3DInt target = getShape(shape.getName());
						removeShape(target, true, false);
						target.destroy();
						addShape(shape, true, false);
						InterfaceSession.log("ShapeSet3DInt.mergeSet: Shape with name '" + target.getFullName() + 
								 "' already exists; overwriting..", 
								 LoggingType.Warnings);
					}else if (existing_shapes.toLowerCase().equals("rename")){
						String new_name = this.getUniqueName(shape.getName());
						InterfaceSession.log("ShapeSet3DInt.mergeSet: Shape with name '" + shape.getName() + 
					 			 "' already exists; renaming to '" + new_name + "'..", 
					 			 LoggingType.Warnings);
						shape.setName(new_name);
						addShape(shape);
					}else{
						InterfaceSession.log("ShapeSet3DInt.mergeSet: Shape with name '" + shape.getName() + 
					 			 "' already exists; skipping..", 
					 			 LoggingType.Warnings);
						}
				}else{
					addShape(shape, true, false);
					}
				}
			}
		
		if (update){
			ShapeEvent event = new ShapeEvent(this, EventType.ShapeSetModified);
			this.fireShapeListeners(event);
			}
	}
	
	/************************************************
	 * Finds a unique name by iteratively adding numbers to the end of
	 * {@code name}.
	 * 
	 * @param name
	 * @return
	 */
	protected String getUniqueName(String name){
		if (!this.hasShape(name)) return name;
		int i = 1;
		String new_name = name + "_" + i;
		while (this.hasShape(new_name)){
			new_name = name + "_" + ++i;
			}
		return new_name;
	}
	
	
	/*****************************************************
	 * Determines whether {@code shape} is in this set. Does a recursive search of all
	 * subsets.
	 * 
	 * @param shape3d
	 * @return
	 */
	public boolean hasShape(InterfaceShape s){
		return hasShape(s, true);
	}
	
	/*****************************************************
	 * Determines whether {@code shape} is in this set.
	 * 
	 * @param shape
	 * @param recurse 		Whether to also search all subsets recursively
	 * @return
	 */
	public boolean hasShape(InterfaceShape shape, boolean recurse){
		if (!(shape instanceof Shape3DInt)) return false;
		for (int i = 0; i < members.size(); i++){
			if (members.get(i).equals(shape))
				return true;
			if (recurse &&
				members.get(i) instanceof ShapeSet &&
				((ShapeSet)members.get(i)).hasShape(shape))
				return true;
			}
		return false;
	}
	
	/*****************************************************
	 * Adds {@code shape} to this set, updates it, and fires this set's listeners.
	 * 
	 * @param shape
	 * @return
	 */
	public boolean addShape(InterfaceShape shape) {
		if (shape instanceof Shape3DInt)
			return addShape((Shape3DInt)shape, -1, true, true);
		return false;
	}
	
	/*****************************************************
	 * Adds {@code shape} to this set.
	 * 
	 * @param shape3d
	 * @param update			Whether to update this shape and fire its listeners
	 * @return
	 */
	public boolean addShape(Shape3DInt thisShape, boolean update){
		return addShape(thisShape, -1, update, update);
	}
	
	/*****************************************************
	 * Adds {@code shape} to this set at {@code index}.
	 * 
	 * @param shape3d
	 * @param update			Whether to update this shape and fire its listeners
	 * @return
	 */
	public boolean addShape(Shape3DInt thisShape, int index, boolean update){
		return addShape(thisShape, index, update, update);
	}
	
	/*****************************************************
	 * Adds {@code shape} to this set.
	 * 
	 * @param shape
	 * @param updateShape			Whether to update this shape
	 * @param updateListeners	 	Whether to fire this set's listeners
	 * @return
	 */
	public boolean addShape(Shape3DInt shape, boolean updateShape, boolean updateListeners){
		return addShape(shape, -1, updateShape, updateListeners);
	}
	
	/****************************************************
	 * Adds <code>shape</code> to this shape set. If <code>updateShape</code> is <code>true</code>, performs updates on the
	 * shape, sets this set as its parent set and registers itself as a shape listener on <code>shape</code>, registers 
	 * camera listeners, and generates a Java3D node. If <code>updateListeners</code> is <code>true</code>, fires this shape 
	 * set's listeners with a <code>ShapeAdded ShapeEvent</code>.
	 * 
	 * <p>It is not recommended to set these arguments to <code>false</code> unless you are using this set to perform
	 * non-GUI-related tasks.
	 * 
	 * @param shape The shape to add
	 * @param index The index at which to insert the shape
	 * @param updateShape Specifies whether to perform shape updates
	 * @param updateListeners Specifies whether to fire shape listeners
	 * @return <code>true</code> if successful 
	 */
	public boolean addShape(Shape3DInt shape, int index, boolean updateShape, boolean updateListeners){
		
		if (shape instanceof ShapeSet3DInt && isAncestorSet((ShapeSet3DInt)shape)) return false;
		if (shape.equals(this)) return false;
	
		if (index < 0)
			members.add(shape);
		else
			members.add(index, shape);
		
		shape.register();
		
		if (updateShape){
			//shape bounds update
			shape.updateShape();
			updateShape();
			
			//set this as parent (will remove it from an existing parent, and add this as a shape listener)
			shape.setParentSet(this);
			
			//register camera listeners
			if (shape instanceof ShapeSet3DInt){
				((ShapeSet3DInt)shape).registerCameras(registered_cameras);
			} else if (shape.hasCameraListener) {
				for (int i = 0; i < registered_cameras.size(); i++)
					shape.registerCameraListener(registered_cameras.get(i));
				}
			
			//set model
			if (getModel() != null)
				shape.setID(getModel().getNextID());
			
			//set shape's scene node
			shape.setScene3DObject();
			
			//set this set's scene node
			if (scene3DObject == null){
				setScene3DObject();
			}else{
				ShapeSceneNode node = shape.getShapeSceneNode();
				if (node == null){
					InterfaceSession.log("ShapeSet3DInt: Error adding shape '" + shape.getName() + "' (null shape node).", 
										 LoggingType.Errors);
					return false;
					}
				if (node.getParent() != null)
					node.detach();
				try{
					scene3DObject.addChild(node);
				}catch (Exception e){
					node.detach();
					scene3DObject.addChild(node);
					}
				}
			}

		//alert listeners; this includes any tree nodes
		if (updateListeners){
			if (index < 0){
				last_added = shape;
				ShapeEvent e = new ShapeEvent(this, ShapeEvent.EventType.ShapeAdded);
				fireShapeListeners(e);
				last_added = null;
			}else{
				last_inserted = shape;
				last_insert = index;
				ShapeEvent e = new ShapeEvent(this, ShapeEvent.EventType.ShapeInserted);
				fireShapeListeners(e);
				last_inserted = null;
				}
			}
		
		return true;
		
	}
	
	public boolean moveShapeBefore(InterfaceShape inserted_shape, InterfaceShape target_shape){
		return moveShapeBefore((Shape3DInt)inserted_shape, (Shape3DInt)target_shape, true);
	}
	
	/*************************************************
	 * Inserts <code>inserted_shape</code> at a position before <code>target_shape</code> in this list. 
	 * Both shapes must already be in this set. To add a new shape at a specific position, use 
	 * {@link addShape}.
	 * 
	 * 
	 * @param inserted_shape
	 * @param target_shape
	 * @param update
	 * @return
	 */
	public boolean moveShapeBefore(Shape3DInt moved_shape, Shape3DInt target_shape, boolean update){
		if (!this.hasShape(target_shape) || !this.hasShape(moved_shape)) return false;
		
		int insert = members.indexOf(target_shape);
		members.remove(moved_shape);
		int ins = members.indexOf(target_shape);
		members.add(ins, moved_shape);
		
		if (update){
			updateShape();
			last_inserted = moved_shape;
			last_insert = insert;
			ShapeEvent e = new ShapeEvent(this, ShapeEvent.EventType.ShapeMoved);
			fireShapeListeners(e);
			last_inserted = null;
			}
		
		return true;
	}
	
	/************************************************
	 * Determines whether <code>set</code> is an ancestor of this set.
	 * @param set
	 * @return
	 */
	public boolean isAncestorSet(ShapeSet set){
		if (getParentSet() == null) return false;
		if (getParentSet().equals(set)) return true;
		return getParentSet().isAncestorSet(set);
	}
	
	public void addShapes(ArrayList<Shape3DInt> shapes){
		addShapes(shapes, true, true);
	}
	
	/**************************************
	 * Add multiple shapes to this set. Use this instead of multiple calls to addShape,
	 * as it will only update itself once all shapes are added, rather once for every shape.
	 * 
	 * @param shapes
	 * @param update
	 * @param listeners
	 */
	public void addShapes(ArrayList<Shape3DInt> shapes, boolean update, boolean listeners){
		if (shapes.size() == 0) return;
		if (shapes.size() == 1){
			addShape(shapes.get(0), update, listeners);
			return;
			}
		for (int i = 0; i < shapes.size(); i++){
			Shape3DInt shape = shapes.get(i);
			
			addShape(shape, update, listeners);
			}
			
	}
	
	@Override
	public ShapeModel3D getModel(){
		if (this.parent_set != null){
			//System.out.println("Parent set is " + parent_set.getName() + " for shape '" + this.getName() + "'.");
			return parent_set.getModel();
			}
		//System.out.println("Model is " + model.getName() + " for shape '" + this.getName() + "'.");
		return model;
	}
	
	@Override
	protected void fireShapeListeners(ShapeEvent e){
	
		super.fireShapeListeners(e);
		
		if (e.getShape() instanceof SectionSet3DInt &&
				e.eventType == EventType.AttributeModified){
			this.updateSectionNodes();
			}
		
		//fire model as a listener
		ShapeModel3D model = getModel();
		if (model != null)
			model.shapeUpdated(e);
	}
	
	/******************************************************
	 * Removes <code>shape</code> from this shape set and removes it as a parent set and a shape listener. Updates 
	 * this set's bounds and fires this set's shape listeners with a <code>ShapeRemoved ShapeEvent</code>.
	 * 
	 * @param shape The shape to remove
	 */
	@Override
	public void removeShape(InterfaceShape shape){
		removeShape((Shape3DInt)shape, true, true);
	}
	
	/******************************************************
	 * Removes <code>shape</code> from this shape set and removes it as a parent set and a shape listener. If 
	 * <Code>updateShape</code> is <code>true</code>, updates this set's bounds. If <code>updateListeners</code>
	 * is <code>true</code>, fires this set's shape listeners with a <code>ShapeRemoved ShapeEvent</code>.
	 * 
	 * <p>It is not recommended to set these arguments to <code>false</code> unless you are using this set to perform
	 * non-GUI-related tasks.
	 * 
	 * @param shape The shape to remove
	 * @param updateShape Specifies whether to update this set's bounds
	 * @param updateListeners Specifies whether to fire this set's shape listeners
	 */
	public void removeShape(Shape3DInt shape, boolean updateShape, boolean updateListeners){
		members.remove(shape);
		shape.removeShapeListener(this);
		shape.parent_set = null;
		shape.destroy();
		if (this.scene3DObject != null)
			shape.getShapeSceneNode().detach();
		
		if (updateShape){
			updateShape();
			}
		
		if (updateListeners){
			last_removed = shape;
			fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.ShapeRemoved));
			last_removed = null;
			}
	}
	
	public void removeShape2D(Shape2DInt shape, boolean updateShape, boolean updateListeners){
		
	}
	
	@Override
	public void destroy(){
		isDestroyed = true;
		// Destroy members first
		for (int i = 0; i < members.size(); i++)
			members.get(i).destroy();
		
		if (sections_node != null)
			sections_node.detach();
		
		clipNodes.clear();
		
		if (sceneNode != null)
			sceneNode.destroy();
		
		for (int i = 0; i < tree_nodes.size(); i++)
			tree_nodes.get(i).destroy();
		
		fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.ShapeDestroyed));
	}
	
	public void drawMember(int index, Graphics2D g, DrawingEngine d){
		members.get(index).drawShape2D(g, d);
	}
	
	public void drawShape(Graphics2D g, DrawingEngine d){
		if (!((MguiBoolean)attributes.getValue("IsVisible")).getTrue()) return;
		for (int i = 0; i < members.size(); i++){
			members.get(i).drawShape2D(g, d);
		}
	}
	
	//probably a better way for subclasses to avoid invoking this class's method..
	//..but apparently not.
	protected void setSuperTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
	}
	
	/****************************************
	 * Constructs a tree node from this shape. Adds an {@link AttributeTreeNode} via the super method, and 
	 * also adds a node to display the vertex-wise data columns associated with this ShapeInt. 
	 * 
	 * @param treeNode node to construct
	 */
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		//Does not call super because we don't want to add a data node here, and super.super is illegal.
		treeNode.removeAllChildren();
		treeNode.setUserObject(this);
		
		for (int i = 0; i < members.size(); i++)
			if (!treeNode.containsObject(members.get(i)))
				treeNode.add(members.get(i).issueTreeNode());
		
	}
	
	
	public boolean needsRedraw3D(Attribute a){
		//shouldn't redraw entire set unless all members need redrawing..
		if (a.getName().equals("IsOverriding") ||
			a.getName().equals("IsVisible") ||
			a.getName().equals("3D.Show")) return true;
		
		return false;
	}
	
	@Override
	public String toString(){
		return getName() + " [" + getID() + "]";
	}
	
	/************************************************
	 * Responds to a change in a member shape. Typically this involves calling {@link updateShape} to update
	 * this set's bounds and center point.
	 * 
	 * @param e a <code>ShapeEvent</code> characterizing the change
	 */
	@Override
	public void shapeUpdated(ShapeEvent e){
	
		if (e.alreadyResponded(this)) return;
		e.responded(this);
		
		switch (e.eventType){
			case ShapeAdded:
				updateShape();
				fireShapeModified();
				return;
			case ShapeModified:
				updateShape();
				if (e.modifiesShapeSet())
					fireShapeModified();
				else
					fireShapeListeners(e);
				return;
			case AttributeModified:
			case ClipModified:
			case ShapeDestroyed:
				updateShape();
				return;
			case VertexColumnChanged:
				fireShapeModified();
				return;
			}
		
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		
		//section node attributes
		if (e.getAttribute().getName().equals("3D.ShowSections") ||
			e.getAttribute().getName().equals("3D.SectionAlpha") ||
			e.getAttribute().getName().equals("3D.FillSections") ){
			updateSectionNodes();
			setSectionNodes();
			return;
			}
		
		//reflect change in visibility
		if (e.getAttribute().getName().equals("IsVisible") ||
			e.getAttribute().getName().equals("2D.Show")){
			
			}
		
		if (e.getAttribute().getName().equals("IsVisible") ||
			e.getAttribute().getName().equals("3D.Show")){
			if (isVisible() && show3D()){
				setBoundBoxNode();
				}
			}
		
		if (e.getAttribute().getName().equals("Unit")){
			//TODO: update all shapes to reflect new unit;
			if (members.size() == 0) return;
			ShapeEvent ev = new ShapeEvent(members.get(0), ShapeEvent.EventType.AttributeModified);
			ev.responded(this);
			for (int i = 0; i < members.size(); i++){
				if (members.get(i) instanceof SectionSet3DInt){
					ev.setShape(members.get(i));
					members.get(i).fireShapeListeners(ev);
					}
				}
			
			return;
			}
		
//		if (((MguiBoolean)this.getAttribute("IsOverriding").getValue()).getTrue()) {
//			if (needsRedraw3D(e.getAttribute())) {
//				this.
//				}
//			}
		
		if (needsRedraw3D(e.getAttribute())) { 
			if (scene3DObject != null)
				setScene3DObject();
			fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.AttributeModified));
			return;
			}
		
		super.attributeUpdated(e);
	}
	
	public int addUnionSet(ShapeSet3DInt thisSet, boolean update){
		return addUnionSet(thisSet, update, true);
	}
	
	public int addUnionSet(ShapeSet3DInt thisSet, boolean updateShapes, boolean updateListeners){
		
		for (int i = 0; i < thisSet.members.size(); i++){
			Shape3DInt shape = thisSet.members.get(i);
			members.add(shape);
			
			if (updateShapes){
				shape.updateShape();
				//shape.setModel(model);
				shape.parent_set = this;
				//add scene node
				if (scene3DObject == null)
					setScene3DObject();
				else
					scene3DObject.addChild(shape.getShapeSceneNode());
				shape.addShapeListener(this);
				}
			
		if (updateListeners){
			ShapeEvent e = new ShapeEvent(shape, ShapeEvent.EventType.ShapeAdded);
			fireShapeListeners(e);
			}
		}
		
		updateShape();
		return members.size();
		
	}
	
	@Override
	public BranchGroup getScene3DObject(ShapeSelectionSet selSet){
		//TODO provide function to set visibility based upon selSet
		ShapeSet3DInt filterSet = selSet.getFilteredShapeSet3D(this);
		return filterSet.getScene3DObject();
	}
	
	@Override
	public void setBoundBoxNode(){
		super.setBoundBoxNode();
		
	}
	
	public boolean hasWindow(InterfaceGraphic2D window){
		return windows_2d.contains(window);
	}
	
	public boolean addWindow(InterfaceGraphic2D window){
		if (hasWindow(window)) return false;
		windows_2d.add(window);
		window.addGraphicListener(this);
		setSectionNode(window);
		return true;
	}
	
	public void removeWindow(InterfaceGraphic2D window){
		if (!windows_2d.contains(window)) return;
		windows_2d.remove(window);
		window.removeGraphicListener(this);
		this.destroySectionNode(window);
	}
	
	@Override
	public void graphicSourceChanged(GraphicEvent e) {
		
		InterfaceGraphic<?> graphic = e.getGraphic();
		if (!(graphic instanceof InterfaceGraphic2D) ||
			!(windows_2d.contains(graphic)))
			return;
		
		setSectionNode((InterfaceGraphic2D)graphic);
		
	}

	@Override
	public void graphicUpdated(GraphicEvent e) {
		
		InterfaceGraphic<?> graphic = e.getGraphic();
		if (!(graphic instanceof InterfaceGraphic2D) ||
			!(windows_2d.contains(graphic)))
			return;
		
		switch(e.getType()){
		
			case Destroyed:
				removeWindow((InterfaceGraphic2D)graphic);
				break;
		
		}
		
	}
	
	public void clearWindows(){
		ArrayList<InterfaceGraphic2D> windows = getSectionWindows();
		for (int i = 0; i < windows.size(); i++){
			windows.get(i).removeGraphicListener(this);
			destroySectionNode(windows.get(i));
			}
		windows_2d.clear();
	}
	
	public ArrayList<InterfaceGraphic2D> getSectionWindows(){
		return new ArrayList<InterfaceGraphic2D>(windows_2d);
		//return new ArrayList<InterfaceGraphic2D>(section_polygons.keySet());
	}
	
	/****************************
	 * Updates all existing section nodes.
	 *
	 ****/
	public void updateSectionNodes(){
		ArrayList<InterfaceGraphic2D> keys = getSectionWindows();
		//ArrayList<InterfaceGraphic2D> keys = new ArrayList<InterfaceGraphic2D>(section_polygons.keySet());

		// Polygons can be added but not removed, so this method has no concurrency issues
		for (int i = 0; i < keys.size(); i++)
			setSectionNode(keys.get(i));
		
//		Iterator<InterfaceGraphic2D> itr = section_polygons.keySet().iterator();
//		while (itr.hasNext())
//			setSectionNode(itr.next());
	}
	
	/**********************
	 * Updates or creates the section node for this window. If it doesn't already exist, it is created; 
	 * otherwise it is modified to match the current state of the window.
	 * 
	 * @param window
	 */
	protected void setSectionNode(InterfaceGraphic2D window){
		
		//Here we need to:
		//1. Check if this shape set has a legitimate bound box (i.e., contains shapes)
		//1.1. If not, 
		//1.1.1. Destroy node (if it exists)
		//1.1.2. Return
		//2. Check if window has a current section set, and that it is a member of this set
		//2.1. If not, 
		//2.1.1. Destroy node (if it exists)
		//2.1.2. Return
		//3. Get section polygon, if it exists
		//4. Check if a node already exists for this window
		//4.1. If not,
		//4.1.1. Check if polygon exists (current section crosses bounds)
		//4.1.1.1. If not, return
		//4.1.1.2. If so,
		//4.1.1.2.1. Create node
		//4.1.1.2.2. Create section node if it hasn't been done yet
		//4.1.1.2.3. Update clip planes, if necessary
		//4.1.1.2.4. Add node to sections node
		//4.1.1.2.5. Add polygon to hash map
		//4.1.1.2.6. Return
		//4.2. If so, 
		//4.2.1. Check if window is destroyed 
		//4.2.1.1. If so, 
		//4.2.1.1.1. Destroy node 
		//4.2.1.1.2. Return
		//4.2.1.2. If not,
		//4.2.1.2.1. Check if polygon exists
		//4.2.1.2.1.1. If so,
		//4.2.1.2.1.1.1. Modify node's geometry & attributes
		//4.2.1.2.1.1.2. Update clip planes, if necessary
		//4.2.1.2.1.1.3. Return
		//4.2.1.2.1.2. If not,
		//4.2.1.2.1.2.1. Remove node from sections node
		//4.2.1.2.1.2.2. Return
		
		if (scene3DObject == null) return;
		
		//1. Check if this shape set has a legitimate bound box (i.e., contains shapes)
		Box3D bounds = this.getBoundBox();
		//1.1. If not, 
		if (bounds == null || window.isDestroyed() || window.getParent() == null){
			//1.1.1. Destroy node (if it exists)
			destroySectionNode(window);
			//1.1.2. Return
			return;
			}
		
		if (boundBoxNode == null) setBoundBoxNode();
		
		//2. Check if window has a current section set, and that it is a member of this set
		SectionSet3DInt section_set = window.getCurrentSectionSet();
		
		//2.1. If not,
		if (section_set == null || !this.hasShape(section_set, true) || 
				!((MguiBoolean)section_set.getAttribute("IsVisible").getValue()).getTrue() ||
				!((MguiBoolean)section_set.getAttribute("3D.Show").getValue()).getTrue()){
			//2.1.1. Destroy node (if it exists)
			destroySectionNode(window);
			//2.1.2. Return
			return;
			}
		
		//3. Get section polygon, if it exists
		Plane3D plane = section_set.getPlaneAt(window.getCurrentSection());
		Polygon2D poly = ShapeFunctions.getIntersectionPolygon(bounds, 
															   plane);
		Polygon3D poly3D = null;
		
		if (poly != null && poly.vertices.size() > 2){
			ArrayList<Point2f> nodes2D = poly.getVertices();
			ArrayList<Point3f> nodes3D = GeometryFunctions.getVerticesFromSection(nodes2D, plane);
			poly3D = new Polygon3D(nodes3D);
			}
		
		//4. Check if a polygon already exists for this window
		//BranchGroup section_node = sectionNodes.get(window);
		Polygon3DInt poly3dint = section_polygons.get(window);
		
		//4.1. If not,
		if (poly3dint == null){
			//4.1.1. Check if polygon exists (current section crosses bounds)
			
			//4.1.1.1. If not, return
			if (poly3D == null){
				return;
			//4.1.1.2. If so,
			}else{
				//4.1.1.2.1. Create node
				poly3dint = new Polygon3DInt(poly3D);
				poly3dint.isAuxiliaryShape(true); 		// Creates a 3D node without requiring a model
				poly3dint.getAttributes().setIntersection(attributes);
				poly3dint.setAttribute("3D.LineColour", section_set.getAttribute("3D.LineColour").getValue());
				poly3dint.setAttribute("3D.HasFill", new MguiBoolean(((MguiBoolean)attributes.getValue("3D.FillSections")).getTrue()));
				poly3dint.setAttribute("3D.FillColour", section_set.getAttribute("3D.LineColour").getValue());
				poly3dint.setAttribute("3D.FillAlpha", attributes.getValue("3D.SectionAlpha"));
				poly3dint.setAttribute("3D.AlphaMode", attributes.getValue("3D.AlphaMode"));
				poly3dint.setClosed(true);
					
				//4.1.1.2.2. Create section node if it hasn't been done yet
				if (sections_node == null){
					sections_node = new BranchGroup();
					sections_node.setCapability(BranchGroup.ALLOW_DETACH);
					sections_node.setCapability(Group.ALLOW_CHILDREN_WRITE);
					sections_node.setCapability(Group.ALLOW_CHILDREN_EXTEND);
					sections_node.setCapability(Group.ALLOW_CHILDREN_READ);
					boundBoxNode.addChild(sections_node);
					}
				
				//4.1.1.2.3. Update clip planes, if necessary
				updateClipPlanes(section_set, window, poly3dint.getShapeSceneNode());
				
				//4.1.1.2.4. Add node to sections node
				sections_node.addChild(poly3dint.getShapeSceneNode());
				
				//4.1.1.2.5. Add node to hash map
				section_polygons.put(window, poly3dint);
				
				//4.1.1.2.6. Return
				return;
				}
			
		//4.2. If so,
		}else{
			//4.2.1. Check if window is destroyed
			//4.2.1.1. If so, 
			if (window.isDestroyed()){
				//4.2.1.1.1. Destroy node
				this.destroySectionNode(window);
				
				//4.2.1.1.2. Return
				return;
				}
			
			//4.2.1.2. If not,
			//4.2.1.2.1. Check if polygon exists
			//4.2.1.2.1.1. If so,
			if (poly3D != null){
				//4.2.1.2.1.1.1. Modify node's geometry & attributes
				poly3dint.setShape(poly3D);
				poly3dint.setAttribute("3D.LineColour", section_set.getAttribute("3D.LineColour").getValue());
				poly3dint.setAttribute("3D.HasFill", new MguiBoolean(((MguiBoolean)attributes.getValue("3D.FillSections")).getTrue()));
				poly3dint.setAttribute("3D.FillColour", section_set.getAttribute("3D.LineColour").getValue());
				poly3dint.setAttribute("3D.FillAlpha", attributes.getValue("3D.SectionAlpha"));
				poly3dint.setClosed(true);
				
				poly3dint.setShapeSceneNode();
				
				//4.2.1.2.1.1.2. Update clip planes, if necessary
				updateClipPlanes(section_set, window, poly3dint.getShapeSceneNode());
				
				//4.2.1.2.1.1.3. Return
				return;
				
			//4.2.1.2.1.2. If not,
			}else{
				//4.2.1.2.1.2.1. Remove node from sections node
				this.destroySectionNode(window);
				
				//4.2.1.2.1.2.2. Return
				return;
				}
			
			}
		
	}
	
	void updateClipPlanes(SectionSet3DInt section_set,InterfaceGraphic2D window, BranchGroup bg){
		
		boolean activate = false;
		
		// If clip is to be applied,
		if (section_set.getApplyClip()){
			BranchGroup clipNode = clipNodes.get(bg);
			
			// If clipNode does not already exist for this branch group, create it
			if (clipNode == null){
				activate = true;
				clipNode = new BranchGroup();
				clipNode.setCapability(BranchGroup.ALLOW_DETACH);
				clipNode.setCapability(Group.ALLOW_CHILDREN_WRITE);
				clipNode.setCapability(Group.ALLOW_CHILDREN_EXTEND);
				clipNodes.put(bg, clipNode);
				}
			
			// Get clipping planes; if they do not exist they will be created, otherwise
			// the existing planes will be modified to reflect the current section
			ModelClip clip_planes = section_set.getClipPlanes(window.getCurrentSection());
			if (clip_planes == null){
				// TODO: throw Exception?
				return;
				}
			
			// If clip planes already have a parent node, but is not clipNode, set clipNode to current parent
			Node parent = clip_planes.getParent();
			if (parent != null && !parent.equals(clipNode)){
				clipNode = (BranchGroup)parent;
				clipNodes.put(bg, clipNode);
				}
			
			// If these planes have no parent, add them to clipNode
			if (parent == null){
				clipNode.addChild(clip_planes);
				}
			
			clipModels.put(section_set, clip_planes);

			// If clipNode is not currently a child of bg, add it
			if (clipNode.getParent() != bg){
				clipNode.detach();
				bg.addChild(clipNode);
				}
			
			// Activate only if this is a new node
			if (activate)
				activateClip(clip_planes);
			
		// If clip is not to be applied, but clip nodes already exist for this branch group, detach:
		}else if (clipNodes.containsKey(bg)){
			clipNodes.get(bg).detach();
			clipModels.remove(section_set);
			}
		
	
	}
	
//	void updateClipNodes(){
//	
//		ArrayList<ModelClip> model_clips = new ArrayList<ModelClip>(clipModels.values());
//		for (int i = 0; i < model_clips.size(); i++){
//			model_clips.get(i).addScope(this.getModel().getModelSet().getShapeSceneNode());
//			}	
//	}
	
//	/********************************************
//	 * Removes this model from the scopes of the current clip nodes
//	 * 
//	 */
//	public void deactivateClipNodes(){
//		ArrayList<ModelClip> model_clips = new ArrayList<ModelClip>(clipModels.values());
//		for (int i = 0; i < model_clips.size(); i++){
//			model_clips.get(i).removeScope(getModel().getModelSet().getShapeSceneNode());
//			}	
//	}
	
	/********************************************
	 * Adds this model to the scopes of the current clip nodes
	 * 
	 */
//	public void activateClipNodes(){
//		ArrayList<ModelClip> model_clips = new ArrayList<ModelClip>(clipModels.values());
//		for (int i = 0; i < model_clips.size(); i++){
//			//model_clips.get(i).addScope(getModel().getModelSet().getShapeSceneNode());
//			}	
//	}
	
	public void activateClip(ModelClip clip){
//		clip.removeAllScopes();
//		clip.addScope(getModel().getModelSet().getShapeSceneNode());
		//clip.setInfluencingBounds(new BoundingSphere(new Point3d(0,0,0), Double.MAX_VALUE));
	}
	
	
	public boolean showSections(){
		return ((MguiBoolean)attributes.getValue("3D.ShowSections")).getTrue();
	}
	
	protected void destroySectionNode(InterfaceGraphic2D window){
		
		Polygon3DInt poly = section_polygons.get(window);
		if (poly == null) return;
		
		clipNodes.remove(poly.getShapeSceneNode());
		poly.getShapeSceneNode().destroy();
		section_polygons.remove(window);
		this.fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.SectionRemoved));
		
	}
	
	public void setSectionNodes(){
		
		if (sections_node == null) return;
	
		if (!showSections()){
			sections_node.detach();
			return;
			}
		
		if (sections_node.getParent() == null){
			boundBoxNode.addChild(sections_node);
		}else if (sections_node.getParent() != boundBoxNode){
			sections_node.detach();
			boundBoxNode.addChild(sections_node);
			}
		
	}
	
	

	@Override
	public void setScene3DObject(boolean make_live){
		setScene3DObject(make_live, null);
	}
	
	public void setScene3DObject(boolean make_live, ShapeSelectionSet filter){
		this.setScene3DObject(make_live, filter, false);
	}
	
	//creates a new scene3D object
	public void setScene3DObject(boolean make_live, ShapeSelectionSet filter, boolean super_only){
		
		if (getModel() == null || !getModel().isLive3D()) return;
		
		//add nodes for each member object
		Shape3DInt thisShape;
		super.setScene3DObject(false);
		
		//skip for subclasses
		if (super_only)	return;
		
		if (!this.isVisible() || !this.show3D()){
			if (make_live) setShapeSceneNode();
			return;
		}
		
		for (int i = 0; i < members.size(); i++){
			thisShape = members.get(i);
			if (filter == null || 
				thisShape instanceof SectionSet3DInt || 
				filter.hasShape(thisShape)){
				ShapeSceneNode n = members.get(i).getShapeSceneNode();
				//if (n == null || ((MguiBoolean)getAttribute("IsOverriding").getValue()).getTrue()){
					if (filter != null && thisShape instanceof SectionSet3DInt)
						((SectionSet3DInt)thisShape).setScene3DObject(filter, false);
					else
						thisShape.setScene3DObject();
					n = thisShape.getShapeSceneNode();
				//	}
				//if (n.getParent() != null && n.getParent() != scene3DObject)
				if (n.getParent() != scene3DObject){
					n.detach();
					scene3DObject.addChild(n);
					}
				}
			}
		
		if (make_live) setShapeSceneNode();
	}
	
	/*****************************************************
	 * Returns all 3D shapes in this shape set; if <code>recurse</code> is <code>false</code>, limits this list
	 * to the set's members; otherwise, also adds all members of all subsets (along with the sets themselves).
	 * 
	 * @param recurse
	 * @return
	 */
	public ArrayList<Shape3DInt> get3DShapes(boolean recurse){
		
		ArrayList<Shape3DInt> shapes = new ArrayList<Shape3DInt>();
		
		for (int i = 0; i < members.size(); i++){
			shapes.add(members.get(i));
			if (members.get(i) instanceof ShapeSet3DInt)
				if (recurse) 
					shapes.addAll(((ShapeSet3DInt)members.get(i)).get3DShapes(true));
			}
		
		return shapes;
	}
	
	public ShapeSet3DInt getShapeType(Shape3DInt thisShape){
		return getShapeType(thisShape, false);
	}
	
	public ShapeSet3DInt getShapeType(Shape3DInt thisShape, boolean recurse){
		ShapeSet3DInt thisSet = new ShapeSet3DInt();
		return getShapeType(thisShape, thisSet, recurse);
		
	}
	
	public ShapeSet3DInt getShapeType(Shape3DInt thisShape, ShapeSet3DInt thisSet, boolean recurse){
		//if thisShape is a shapeset, special case
		if (ShapeSet3DInt.class.isInstance(thisShape)){
			for (int i = 0; i < members.size(); i++)
				if (recurse && 
					ShapeSet3DInt.class.isInstance(members.get(i))){
					thisSet.addShape(members.get(i), false, false);
					((ShapeSet3DInt)members.get(i)).getShapeType(thisShape, thisSet, true);
					}
			return thisSet;
			}
		
		for (int i = 0; i < members.size(); i++){
			if (recurse && ShapeSet3DInt.class.isInstance(members.get(i)))
				thisSet.addUnionSet(((ShapeSet3DInt)members.get(i)).getShapeType(thisShape), false, false);
			else if (thisShape.getClass().isInstance(members.get(i)))
				thisSet.addShape(members.get(i), false, false);
		}
		return thisSet;
	}
	
	public Point3f getCenterPoint(){
		return centerPt;
	}
	
	public void updateMembers(){
		for (int i = 0; i < members.size(); i++)
			members.get(i).updateShape();
	}
	
	@Override
	public void updateShape(){
		//all members should have bounding spheres
		//need to get a union of each sphere
		//System.out.print("Update shape set..");
		
		if (members.size() == 0){
			boundSphere = new Sphere3D(new Point3f(0,0,0), 0.1f);
			boundBox = null;
			centerPt = new Point3f(0,0,0);
			updateSectionNodes();
			setBoundBoxNode();
			return;
		}
		//updateMembers();
		int j = 0;
		boundSphere = members.get(j).boundSphere;
		while (j < members.size() && !GeometryFunctions.isValidSphere(boundSphere)){
			boundSphere = members.get(j).boundSphere;
			j++;
			}
			
		if (!GeometryFunctions.isValidSphere(boundSphere))
			boundSphere = new Sphere3D(new Point3f(0, 0, 0), 100);
		
		if (centerPt == null) centerPt = new Point3f();
		centerPt.x = 0;
		centerPt.y = 0;
		centerPt.z = 0;
		
		int count = 0;
		boundBox = null;
		int valid = 0;
		for (int i = 0; i < members.size(); i++){
			if (members.get(i).getBoundBox() != null) valid++;
			boundBox = GeometryFunctions.getUnionBounds(boundBox, members.get(i).getBoundBox());
			if (members.get(i) instanceof SectionSet3DInt){
				SectionSet3DInt set = (SectionSet3DInt)members.get(i);
				Point3f cp = new Point3f();
				cp.set(set.centerPt);
				int c = set.getShapeCount();
				cp.scale(c);
				centerPt.add(cp);
				count += c;
			}else if (members.get(i) instanceof ShapeSet3DInt){
				ShapeSet3DInt set = (ShapeSet3DInt)members.get(i);
				Point3f cp = new Point3f();
				cp.set(set.centerPt);
				int c = set.getShapeCount();
				cp.scale(c);
				centerPt.add(cp);
				count += c;
			}else{
				if (GeometryFunctions.isValidPoint(members.get(i).centerPt)){
					centerPt.add(members.get(i).centerPt);
					count++;
					}
				}
			if (GeometryFunctions.isValidSphere(members.get(i).getBoundSphere()))
				boundSphere = GeometryFunctions.getUnionSphere(boundSphere, members.get(i).getBoundSphere());
			}
		
		if (count == 0) return;
		
		centerPt.x /= count;
		centerPt.y /= count;
		centerPt.z /= count;
		
		updateSectionNodes();
		setBoundBoxNode();
		
		updateCameras();
		
		//InterfaceSession.log("done.");
		//InterfaceSession.log("Bounds updated: [" + getID() + "] members: " + members.size() + " valid: " + valid);
	}
	
	protected void updateCameras() {
		
		if (this.registered_cameras == null || this.registered_cameras.size() == 0) return;
		
		// Force cameras to inform listeners their angle has changed
		for (Camera3D camera : registered_cameras) {
			camera.fireCameraAngleChanged();
			}
		
		
	}
	
	//return number of shapes in this set and all of its subsets
	public int getShapeCount(){
		int count = 0;
		for (int i = 0; i < members.size(); i++)
			if (members.get(i) instanceof ShapeSet3DInt)
				count += ((ShapeSet3DInt)members.get(i)).getShapeCount();
			else
				count++;
		return count;
	}
	
	/*********************************************
	 * Returns a list of all nodes of all members in this set, including all shapes in all subsets. To get a list
	 * of only the shapes in this immediate set, use <code>getNodes(false)</code>.
	 * 
	 * @return a list of nodes
	 */
	@Override
	public ArrayList<Point3f> getVertices(){
		return getNodes(true);
	}
	
	/*********************************************
	 * Returns a list of all nodes of all members in this set. If <code>recurse</code> is <code>true</code>, also
	 * includes all subsets.
	 * 
	 * @param recurse Specifies whether to include all shapes in all subsets
	 * @return a list of nodes
	 */
	public ArrayList<Point3f> getNodes(boolean recurse){
		ArrayList<Point3f> nodes = new ArrayList<Point3f>();
		for (int i = 0; i < members.size(); i++)
			if (recurse || !(members.get(i) instanceof ShapeSet3DInt))
				nodes.addAll(members.get(i).getVertices());
		return nodes;
	}
	
	public boolean isShape() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setParentWindow(InterfaceGraphic<?> thisParent) {
		// TODO Auto-generated method stub
	}
	
//	public void windowUpdated(InterfaceGraphic<?> g) {
//		
//		setSectionNodes();
//		
//	}
	
	@Override
	public String getLocalName() {
		return "ShapeSet3DInt";
	}
	
	@Override
	public String getXML(int tab, XMLType type) {
		
		String _tab = XMLFunctions.getTab(tab);
		
		String _type = "full";
		if (type.equals(XMLType.Reference)) _type = "reference";
		
		String xml = _tab + "<ShapeSet3DInt type = '" + _type + "'>\n";
		xml = xml + attributes.getXML(tab + 1);
			
		for (int i = 0; i < members.size(); i++)
			xml = xml + members.get(i).getXML(tab + 1, type);
		
		xml = xml + _tab + "</ShapeSet3DInt>\n";
		return xml;
		
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String _type = "full";
		XMLType type = options.type;
		if (type.equals(XMLType.Reference)) _type = "reference";
		
		writer.write(_tab + "<ShapeSet3DInt \n" +
					 _tab2 + "name = '" + getName() + "'\n" +
					 _tab2 + "type = '" + _type + "'\n" + 
					 _tab + ">\n");
		
		//ShapeModel3DOutputOptions s_options = (ShapeModel3DOutputOptions)options;
		
		//Members write their XML here
		writer.write(_tab2 + "<Members>\n");
		for (int i = 0; i < members.size(); i++){
			// If this is a ShapeModel3D write, ensure this member is included
			if (members.get(i) instanceof ShapeSet3DInt){
				// Write this subset and its members, if any
				ShapeSet3DInt set = (ShapeSet3DInt)members.get(i);
				ArrayList<InterfaceShape> sub_members = set.getMembers(true);
				
				if (sub_members.size() > 0){
					int j = 0;
					boolean add_set = true;
					if (options instanceof ShapeModel3DOutputOptions){
						// Make sure at least one sub-member is included in write
						while (j < sub_members.size() && 
								!((ShapeModel3DOutputOptions)options).include_shape.get(sub_members.get(j)));
						add_set = j < sub_members.size();
						}
					if (add_set){
						members.get(i).writeXML(tab + 2, writer, options, progress_bar);
						writer.write("\n");
						}
					}
			}else{
				if (!(options instanceof ShapeModel3DOutputOptions) ||
						((ShapeModel3DOutputOptions)options).include_shape.get(members.get(i))){
					members.get(i).writeXML(tab + 2, writer, options, progress_bar);
					writer.write("\n");
					}
				}
			}
		writer.write(_tab2 + "</Members>\n");
		
		// Attributes go last because they may need to be loaded last
		if (attributes != null){
			attributes.writeXML(tab + 1, writer, progress_bar);
			}
		
		//close up
		writer.write("\n" + _tab + "</ShapeSet3DInt>\n");
		
	}
	
	public InterfacePopupMenu getPopupMenu() {
		InterfacePopupMenu menu = super.getPopupMenu();
		
		int start = super.getPopupMenuLength();
		
		menu.add(new JSeparator(), start);
		menu.add(new JSeparator(), start);
		menu.addMenuItem(new JMenuItem("ShapeSet3DInt", getObjectIcon()));
		menu.add(new JSeparator(), start + 3);
		menu.add(new JSeparator(), start + 3);
		
		menu.addMenuItem(new JMenuItem("New Shape Set"));
		menu.addMenuItem(new JMenuItem("Paste"));
		
		return menu;
	}
	
	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getText().equals("New Shape Set")){
			String name = JOptionPane.showInputDialog(getModel().getDisplayPanel(), 
													  "Name for new shape set:",
													  "Create 3D Shape Set",
													  JOptionPane.QUESTION_MESSAGE);
			if (name == null) return;
			ShapeSet3DInt set = new ShapeSet3DInt(name);
			this.addShape(set);
			return;
			}
		
		super.handlePopupEvent(e);
		
	}
	
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		//flavor class must be an instance of Shape3DInt
		//in this case, adds shape to the bottom of this shape set
		Class<?> this_class = flavor.getRepresentationClass();
		return Shape3DInt.class.isAssignableFrom(this_class);
	}
	
	public DataFlavor[] getTransferDataFlavors(){
		DataFlavor[] flavors = new DataFlavor[1];
		String mimeType2 = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + Shape3DInt.class.getName();
		try{
			flavors[0] = new DataFlavor(mimeType2);
		}catch (ClassNotFoundException cnfe){
			//if this happens then hell freezes over :-/
			cnfe.printStackTrace();
			return new DataFlavor[0];
			}
		return flavors;
	}
	
	@Override
	public boolean performTransfer(TransferSupport support){
		
		Transferable transferable = support.getTransferable();
		
		try{
			//get list of transferables (i.e., shapes to be moved into this set)
			Object obj = transferable.getTransferData(this.getTransferDataFlavors()[0]);
			ArrayList<Transferable> transferables = (ArrayList<Transferable>)obj;
			
			boolean success = transferables.size() > 0;
			
			for (int i = 0; i < transferables.size(); i++){
				if (!(transferables.get(i) instanceof Shape3DInt) || 
						this.equals(obj)){
					success = false;
				}else{
					Shape3DInt shape = (Shape3DInt)transferables.get(i);
					//if this is already the parent shape, do nothing
					if (shape.getParentSet() != null && shape.getParentSet() == this)
						success = false;
					// TODO: avoid adding containing set to child set...
					else
						//otherwise add shape to this set
						success &= addShape(shape, true);
					}
				}
			
			return success;
		}catch (Exception e){
			return false;
			}
		
	}

	
	
}