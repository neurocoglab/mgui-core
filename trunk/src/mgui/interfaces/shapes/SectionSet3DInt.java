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

/*
 * Copyright (C) 2011 Andrew Reid and the modelGUI Project <http://mgui.wikidot.com>
 * 
 * This file is part of modelGUI[core] (mgui-core).
 * 
 * modelGUI[core] is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * modelGUI[core] is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with modelGUI[core]. If not, see <http://www.gnu.org/licenses/>.
 */

package mgui.interfaces.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.ModelClip;
import javax.swing.ImageIcon;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Box3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Rect2D;
import mgui.geometry.Shape;
import mgui.geometry.Sphere3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.geometry.util.SpatialUnit;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphics.InterfaceView3DObject;
import mgui.interfaces.graphics.View3D;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.maps.ValueMap;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.trees.Shape3DTreeNode;
import mgui.interfaces.shapes.util.SectionSetIterator;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeEvent.EventType;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.domestic.attributes.AttributeXMLHandler;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.morph.sections.RadialRep2D;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/************************************************************
 * Represents a set of parallel sections, based upon a reference plane
 * (thisShape), and a spacing value. Stores 2D shape sets in a value map, such
 * that the value is an integer i specifying the section at i * spacing units
 * from the reference plane.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */

public class SectionSet3DInt extends Shape3DInt implements ShapeListener,
		ShapeSet, InterfaceView3DObject {

	/** @TODO sort list of sections by keyValue **/
	public HashMap<Integer, ShapeSet2DInt> sections = new HashMap<Integer, ShapeSet2DInt>();
	public ValueMap scene3DNodes = new ValueMap();
	public double width;
	public Rect2D boundBox2D;
	public boolean blnUpdate = true;
	public AttributeList shapeAttr;

	public ModelClip clip_planes;

	ShapeSet2DInt last_added = null;
	ShapeSet2DInt last_removed = null;
	ShapeSet2DInt last_modified = null;
	ShapeSet2DInt last_inserted = null;
	ShapeSet2DInt last_moved = null;

	public SectionSet3DInt() {
		shape3d = new Plane3D();
		init();
		setSpacing(1);
	}

	public SectionSet3DInt(String sName, Plane3D refPlane, float spacing) {
		this(sName, refPlane, spacing, spacing);
	}

	public SectionSet3DInt(String sName, Plane3D refPlane, float spacing,
			double sectWidth) {
		width = sectWidth;
		shape3d = refPlane;
		init();
		setName(sName);
		setSpacing(spacing);
	}

	@Override
	public Shape getGeometryInstance(){
		return new Plane3D();
	}
	
	/****************************************
	 * Returns the index of <code>section_set</code> in this set.
	 * 
	 * @return index of shape, or -1 if it is not in this set
	 */
	@Override
	public int getIndexOf(InterfaceShape section_set) {
		ArrayList<ShapeSet2DInt> sets = new ArrayList<ShapeSet2DInt>(
				sections.values());
		for (int i = 0; i < sets.size(); i++)
			if (sets.get(i).equals(section_set))
				return i;
		return -1;
	}

	/******************************************
	 * Returns the index of the section in this set which is closest to
	 * {@code point}.
	 * 
	 * @param point
	 * @return
	 */
	public int getClosestSection(Point3f point){
		
		// Distance to reference plane along normal
		double d = GeometryFunctions.getSignedDistance(point, getRefPlane());
		
		// Number of sections to d
		double sep = this.getSpacing();
		double c = d / sep;
		int i = (int)Math.floor(Math.abs(c));
		if (d > c + 0.5 * sep)
			i++;
		
		if (d < 0) i = -i;
		return i;
	}

	/***
	 * Does nothing for <code>SectionSet3DInt</code>; the unit is set by the
	 * parent set (or the the default from <code>InterfaceEnvironment</code>, if
	 * no parent is set.
	 */
	public void setUnit(SpatialUnit unit){	}
	
	@Override
	public boolean isAncestorSet(ShapeSet set) {
		if (getParentSet() == null)
			return false;
		if (getParentSet().equals(set))
			return true;
		return getParentSet().isAncestorSet(set);
	}

	@Override
	protected void setIcon() {
		java.net.URL imgURL = ShapeSet3DInt.class
				.getResource("/mgui/resources/icons/section_set_3d_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/section_set_3d_20.png");
	}

	@Override
	protected void init() {
		super.init();
		attributes.add(new Attribute<MguiFloat>("Spacing", new MguiFloat(1)));
		attributes.add(new Attribute<MguiBoolean>("3D.LabelNodes", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiBoolean>("3D.ApplyClip", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiBoolean>("3D.InvertClip", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiFloat>("3D.ClipDistUp", new MguiFloat(0.5f)));
		attributes.add(new Attribute<MguiFloat>("3D.ClipDistDown", new MguiFloat(0.5f)));
		attributes.add(new Attribute<MguiBoolean>("3D.ShowSection", new MguiBoolean(true)));
	
		// shape attributes
		shapeAttr = ShapeFunctions.getDefaultShapeAttributes2D();

	}

	/*******************************************
	 * Returns a copy of this set's member list.
	 * 
	 * @return a list of members
	 */
	@Override
	public ArrayList<InterfaceShape> getMembers() {
		return new ArrayList<InterfaceShape>(sections.values());
	}

	public int getSectionForShape(Shape2DInt shape) {
		return getSectionOf((ShapeSet2DInt) shape.getParentSet());
	}

	public boolean getShowSection3D() {
		return ((MguiBoolean) attributes.getValue("3D.ShowSection")).getTrue();
	}

	/*****************************
	 * 
	 * Returns the index of {@code set} in this section set object, if it exists.
	 * Otherwise, returns {@code Integer.MAX_VALUE}.
	 * 
	 * @param set 		The set to check
	 * @return			The index of {@code set}, or {@code Integer.MAX_VALUE} if it is not in this 
	 * 					section set.
	 */
	public int getSectionOf(ShapeSet2DInt set) {
		Iterator<Integer> itr = sections.keySet().iterator();
		while (itr.hasNext()) {
			Integer i = itr.next();
			if (sections.get(i).equals(set))
				return i;
		}
		return Integer.MAX_VALUE;
	}

	@Override
	public void attributeUpdated(AttributeEvent e) {

		if (e.getAttribute().getName().equals("3D.LineColour")) {
			getModel().getModelSet().updateSectionNodes();
			fireShapeModified();
			return;
			}
		
		if (e.getAttribute().getName().equals("3D.ApplyClip")) {
			fireShapeListeners(new ShapeEvent(this,
							   EventType.ClipModified));
			return;
			}

		if (e.getAttribute().getName().contains("3D.Clip")) {
			setScene3DObject();
			fireShapeListeners(new ShapeEvent(this,
					 		   EventType.AttributeModified));
			return;
			}
		
		if (e.getAttribute().getName().equals("3D.ShowSection")) {
			// Use parent set to notify listeners of an attribute change
			ShapeSet3DInt parent_set = (ShapeSet3DInt)getParentSet();
			if (parent_set == null) return;
			parent_set.fireShapeListeners(new ShapeEvent(this, EventType.AttributeModified));
			return;
			}

		super.attributeUpdated(e);
	}

	@Override
	public boolean needsRedraw(Attribute<?> a) {
		if (a.getName().equals("Name") || 
			a.getName().equals("3D.ShowBounds")	|| 
			a.getName().equals("3D.BoundsColour") ||
			a.getName().equals("3D.ApplyClip"))
			return false;
		return true;
	}

	public int getSize() {
		return getLastSection() - getFirstSection();
	}

	/******************
	 * TODO: update coordinates to according to model's coordinate system
	 * 
	 * public void setModel(ShapeModel3D m){ if (model == m) return; if (model
	 * != null) removeShapeListener(model); model = m;
	 * setID(model.idFactory.getID()); addShapeListener(model);
	 * Iterator<ShapeSet2DInt> itr = sections.values().iterator();
	 * 
	 * while (itr.hasNext()) itr.next().setModel(m);
	 * 
	 * }
	 */

	public View3D getView3D() {
		return getView3D(1.0);
	}

	public View3D getView3D(double distance) {

		Camera3D camera = new Camera3D();
		Plane3D plane = this.getRefPlane();
		Vector3d v = new Vector3d(plane.getNormal());
		v.scale(-1.0);
		camera.lineOfSight = v;
		camera.distance = distance;
		camera.centerOfRotation = new Point3d(plane.origin);
		camera.upVector = new Vector3d(plane.yAxis);

		return new View3D("sections_" + getName(), camera, false);

	}

	public void setUpdateable(boolean b) {
		blnUpdate = b;
	}

	public boolean getUpdateable() {
		return blnUpdate;
	}

	public Rect2D getBoundBox2D() {
		return boundBox2D;
	}

	public void setBoundBox2D(Rect2D thisBox) {
		boundBox2D = thisBox;
	}

	/************************
	 * 
	 * Gets the reference plane (i.e., at index 0) for this section set.
	 * 
	 */
	public Plane3D getRefPlane() {
		return (Plane3D) shape3d;
	}

	/************************
	 * 
	 * Sets the reference plane (i.e., at index 0) for this section set.
	 * 
	 * @param thisPlane
	 */
	public void setRefPlane(Plane3D thisPlane) {
		shape3d = thisPlane;
		this.fireShapeModified();
	}

	public float getSpacing() {
		// return spacing;
		return ((MguiFloat) attributes.getValue("Spacing")).getFloat();
	}

	/***************************
	 * 
	 * Sets the spacing distance for this section set.
	 * 
	 * @param s
	 */
	public void setSpacing(float s) {
		// spacing = s;
		attributes.getAttribute("3D.ClipDistUp").setValue(new MguiFloat(s), false);
		attributes.getAttribute("3D.ClipDistDown").setValue(
				new MguiFloat((float) s), false);
		attributes.setValue("Spacing", new MguiFloat(s));

	}

	public boolean getApplyClip() {
		return ((MguiBoolean) attributes.getValue("3D.ApplyClip")).getTrue();
	}

	public void setApplyClip(boolean apply) {
		attributes.setValue("ApplyClip", new MguiBoolean(apply));
	}

	/***************************
	 * 
	 * Gets the upwards clipping distance for this section set
	 * 
	 */
	public float getClipDistUp() {
		return ((MguiFloat) attributes.getValue("3D.ClipDistUp")).getFloat();
	}

	/***************************
	 * 
	 * Sets the upwards clipping distance for this section set
	 * 
	 * @param dist
	 */
	public void setClipDistUp(float dist) {
		attributes.setValue("ClipDistUp", new MguiFloat(dist));
	}

	/***************************
	 * 
	 * Gets the downwards clipping distance for this section set
	 * 
	 */
	public float getClipDistDown() {
		return ((MguiFloat) attributes.getValue("3D.ClipDistDown")).getFloat();
	}

	/***************************
	 * 
	 * Sets the downwards clipping distance for this section set
	 * 
	 * @param dist
	 */
	public void setClipDistDown(float dist) {
		attributes.setValue("3D.ClipDistDown", new MguiFloat(dist));
	}

	public boolean getInvertClip() {
		return ((MguiBoolean) attributes.getValue("3D.InvertClip")).getTrue();
	}

	public void setInvertClip(boolean apply) {
		attributes.setValue("3D.InvertClip", new MguiBoolean(apply));
	}

	/*****************************************
	 * Get a set of two <code>ModelClip</code> nodes parallel to this section's
	 * plane, at distances defined by <code>getClipDistUp()</code> and
	 * <code>getClipDistDown()</code>. 
	 * <p>If the clipping nodes already exist, they will be updated by this method.
	 * 
	 * @param section
	 * @param parent
	 *            test whether parent is already the current parent; otherwise
	 *            must create new node
	 * @return
	 */
	public ModelClip getClipPlanes(int section) {

		if (!getApplyClip())
			return null;

		Plane3D plane = getPlaneAt(section);
		if (clip_planes == null){ // || clip_planes.getParent() != parent) {
			clip_planes = ShapeFunctions.getModelClip(plane, getClipDistUp(),
					getClipDistDown(), getInvertClip());
		} else {
			ShapeFunctions.setModelClip(clip_planes, plane, getClipDistUp(),
					getClipDistDown(), getInvertClip());
			}

		return clip_planes;
	}

	public boolean hasClipPlanes(){
		return clip_planes == null;
	}
	
	public void addShape2D(Shape2DInt thisShape, int section, boolean update) {
		addShape2D(thisShape, section, update, true);
	}

	/*******************************
	 * 
	 * Adds a 2D shape to the section shape set at index {@code section}.
	 * 
	 * @param shape
	 * @param section
	 * @param updateShape
	 * @param updateListeners
	 */
	public void addShape2D(Shape2DInt shape, int section, boolean updateShape,
			boolean updateListeners) {

		ShapeSet2DInt thisSect = addSection(section, updateShape);
		thisSect.addShape(shape, updateShape, updateListeners);
		if (updateShape) {
			if (shape instanceof LightweightShape)
				((LightweightShape) shape).setAttributes(shapeAttr);
			updateShape(thisSect, section);
			updateShape();

		}

	}

	/**********************************
	 * 
	 * Add a section shape set to this section set.
	 * 
	 * @param section
	 * @param updateShape
	 * @return
	 */
	public ShapeSet2DInt addSection(int section, boolean updateShape) {
		if (hasSection(section))
			return sections.get(new Integer(section));
		ShapeSet2DInt thisSect = new ShapeSet2DInt();
		thisSect.setName(getName() + "." + String.valueOf(section));
		sections.put(new Integer(section), thisSect);

		if (updateShape) {
			// thisSect.setModel(model);
			thisSect.parent_section_set = this;
			thisSect.addShapeListener(this);
			setScene3DObject();
			last_added = thisSect;
			fireShapeListeners(new ShapeEvent(this,
					ShapeEvent.EventType.ShapeAdded));
			last_added = null;
		}

		return thisSect;
	}

	/*****************************
	 * 
	 * Remove the section at index {@code section}.
	 * 
	 * @param section
	 * @param updateShape
	 * @param updateListeners
	 * @return
	 */
	public boolean removeSection(int section, boolean updateShape, boolean updateListeners) {

		if (!hasSection(section))
			return false;

		Integer s = new Integer(section);
		ShapeSet2DInt set = sections.get(s);
		if (set.parent_section_set == this)
			set.parent_section_set = null;
		sections.remove(s);

		set.removeShapeListener(this);

		if (updateShape)
			updateShape();

		if (updateListeners) {
			last_removed = set;
			fireShapeListeners(new ShapeEvent(this,
					ShapeEvent.EventType.ShapeRemoved));
			last_removed = null;
		}

		return true;

	}

	public ShapeSet2DInt getShapeSet(int section) {
		return getShapeSet(section, 0);
	}

	/**********************************
	 * 
	 * Return a {@linkplain ShapeSet2DInt} of all objects intersected by the current section
	 * plane and its upper and lower bounds. 
	 * 
	 * @param section
	 * @param sectionWidth
	 * @return
	 */
	public ShapeSet2DInt getShapeSet(int section, double sectionWidth) {

		Integer s = new Integer(section);

		if (sectionWidth <= 0) {
			ShapeSet2DInt retSet = sections.get(s);
			if (retSet != null)
				return retSet;
			retSet = new ShapeSet2DInt();
			retSet.setName(getName() + ":" + section);
			return retSet;
		}

		// return a set of all shapes within sectionWidth of keyVal
		int half = section - (int) ((sectionWidth / 2) / getSpacing());
		ShapeSet2DInt retSet = new ShapeSet2DInt();
		
		Object thisSet = null;
		for (int i = section - half; i <= section + half; i++) {
			thisSet = sections.get(i);
			if (thisSet != null)
				retSet.addUnionSet((ShapeSet2DInt) thisSet);
		}

		
		retSet.setName(getName() + "." + section);
		return retSet;
	}

	@Override
	public InterfaceTreeNode issueTreeNode() {
		Shape3DTreeNode treeNode = new Shape3DTreeNode(this);
		setTreeNode(treeNode);
		tree_nodes.add(treeNode);
		return treeNode;
	}

	@Override
	public void setTreeNode(InterfaceTreeNode treeNode) {
		super.setTreeNode(treeNode);

		Iterator<ShapeSet2DInt> itr = sections.values().iterator();
		while (itr.hasNext())
			treeNode.add(itr.next().issueTreeNode());
	}

	/****************************
	 * 
	 * Checks whether a section exists at index {@code i}.
	 * 
	 * @param i
	 * @return
	 */
	public boolean hasSection(int i) {
		return sections.containsKey(i);
	}

	/***************************
	 * 
	 * Checks whether any sections have yet been set for this section set.
	 * 
	 * @return
	 */
	public boolean hasSections() {
		return sections.size() > 0;
	}

	/****************************
	 * 
	 * Gets the distance of the section plane at index {@code i} from the reference plance
	 * (i.e., at index 0).
	 * 
	 * @param i
	 * @return
	 */
	public double getSectionDist(int i) {
		return getSpacing() * (double) i;
	}

	@Override
	public String toString() {
		return getName() + " [" + getID() + "]";
	}

	@Override
	public void shapeUpdated(ShapeEvent e) {

		if (e.alreadyResponded(this))
			return;
		e.responded(this);

		switch (e.eventType) {

		case AttributeModified:
			fireShapeListeners(e);
			return;

		case ShapeModified:
			fireShapeModified();
			return;

		case ShapeAdded:
		case ShapeRemoved:
			updateShape();
			fireShapeModified();
			return;

		}

	}

	public ShapeSet2DInt getShape2DType(Shape2DInt shape) {
		return getShape2DType(shape, true);
	}

	public ShapeSet2DInt getShape2DType(Shape2DInt shape, boolean recurse) {
		ShapeSet2DInt thisSet = new ShapeSet2DInt();

		Iterator<ShapeSet2DInt> itr = sections.values().iterator();
		while (itr.hasNext())
			thisSet.addUnionSet(itr.next().getShapeType(shape, recurse), false,
					false);

		return thisSet;
	}

	public void addUnionSet(ShapeSet2DInt thisSet, int section) {
		addUnionSet(thisSet, section, false, false);
	}

	public void addUnionSet(ShapeSet2DInt thisSet, int section,
			boolean updateShape, boolean updateListeners) {
		if (hasSection(section))
			sections.get(new Integer(section)).addUnionSet(thisSet,
					updateShape, updateListeners);
		else
			addSection(section, updateShape).addUnionSet(thisSet, updateShape,
					updateListeners);
		updateShape(thisSet, section);
		// if (blnUpdate)
		updateShape();

		if (updateListeners)
			fireShapeListeners(new ShapeEvent(this,
					ShapeEvent.EventType.ShapeModified));
	}

	public void addUnionSet(SectionSet3DInt uSet, boolean init) {
		sections.putAll(uSet.sections);

		if (init)
			setScene3DObject();
		updateShape();
		fireShapeModified();
	}

	public void updateShape(ShapeSet2DInt thisSet, int section) {
		if (this.getBoundBox() == null)
			this.setBoundBox2D(thisSet.getBounds());
		else
			this.setBoundBox2D(GeometryFunctions.getUnionBounds(
					this.getBoundBox2D(), thisSet.getBounds()));

	}

	public void removeShape(InterfaceShape shape) {
		removeShape((Shape2DInt) shape, true, true);
	}

	// todo: update this
	public void removeShape(Shape2DInt shape, boolean updateShape,
			boolean updateListeners) {

		Iterator<ShapeSet2DInt> itr = sections.values().iterator();
		// for (int i = 0; i < sectionSet.items.size(); i++){
		while (itr.hasNext()) {

			ShapeSet2DInt set = itr.next();
			for (int j = 0; j < set.members.size(); j++)
				if (set.hasShape(shape)) {
					set.removeShape(shape, updateShape, updateListeners);
					if (updateListeners)
						fireShapeListeners(new ShapeEvent(this,
								ShapeEvent.EventType.ShapeModified));

					/** @TODO update bounding box efficiently? **/
					return;
				}
		}
	}

	@Override
	public Shape2DInt getShape2DInt(Plane3D plane, float above_dist,float below_dist, boolean listen) {
		if (!isVisible() || !show2D() || !crossesPlane(plane))
			return null;
		Shape2DInt shape = getShape2D(plane, above_dist, below_dist);
		if (shape == null)
			return null;
		shape.setParentShape(this);
		return shape;
	}

	@Override
	public Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist) {
		if (!getShowSection3D())
			return null;
		SectionSet2DInt set = new SectionSet2DInt(this, plane);
		set.attributes.setIntersection(attributes);
		set.attributes.setValue("2D.LineColour", getLineColour());
		set.attributes.setValue("2D.LineStyle", getLineStyle());
		
		return set;
	}

	public boolean crossesPlane(Plane3D plane) {
		return !(this.getRefPlane().equals(plane) || GeometryFunctions
				.isParallel(this.getRefPlane(), plane));
	}

	public void removeSelectionSet(ShapeSelectionSet selSet) {
		Iterator itr = selSet.getIterator();
		while (itr.hasNext()) {
			Object o = itr.next();
			if (o instanceof Shape2DInt)
				removeShape((Shape2DInt) o);
		}
	}

	@Override
	public BranchGroup getScene3DObject(ShapeSelectionSet selSet) {
		if (selSet == null)
			return getScene3DObject();
		// TODO implement a ShapeSceneNode method to set visibility from
		// a selection set, rather than instantiate new nodes as here..
		SectionSet3DInt s = selSet.getFilteredSectionSet(this);
		return s.getScene3DObject();
	}

	@Override
	public void setScene3DObject() {
		setScene3DObject(null, true);
	}

	@Override
	public void setScene3DObject(boolean make_live) {
		setScene3DObject(null, make_live);
	}

	public void setScene3DObject(ShapeSelectionSet filter, boolean make_live) {

		super.setScene3DObject(false);
		if (scene3DObject == null) return;
		if (!this.isVisible() || !this.show3D() || shape3d == null) {
			if (make_live)
				setShapeSceneNode();
			return;
		}

		Iterator<Integer> itr = sections.keySet().iterator();
		while (itr.hasNext()) {
			Integer s = itr.next();
			ShapeSet2DInt set = sections.get(s);
			Shape2DSectionNode node = set.getShapeSectionNode(this, s);
			if (node.getParent() != scene3DObject) {
				node.detach();
				scene3DObject.addChild(node);
			}
		}

		setShapeSceneNode();
	}

	public ArrayList<Point3f> getVertices() {
		ArrayList<Point3f> nodes = new ArrayList<Point3f>();
		ArrayList<Point3f> temp;
		// for (int i = 0; i < sectionSet.items.size(); i++){
		Iterator<Integer> itr = sections.keySet().iterator();
		while (itr.hasNext()) {
			Integer s = itr.next();
			ShapeSet2DInt set = sections.get(s);

			ShapeSet3DInt set3D = ShapeFunctions.getShapeSet3DFromSection(
					getRefPlane(), (float) this.getSectionDist(s.intValue()),
					set, null);
			for (int j = 0; j < set3D.members.size(); j++) {
				temp = set3D.members.get(j).getVertices();
				if (temp != null)
					nodes.addAll(temp);
			}
		}
		return nodes;
	}

	/*
	 * public Shape2DSectionNode getShape2DSceneNode(int section, Shape2DInt
	 * thisShape){
	 * 
	 * Shape2DSectionNode newNode = new Shape2DSectionNode(this, section,
	 * thisShape); thisShape.addShapeListener(newNode); return newNode; }
	 */

	public ShapeSet3DInt getShapeSet3DInt() {
		return getShapeSet3DInt(null);
	}

	/*************************
	 * Return the plane corresponding to the specified section
	 * 
	 * @param section
	 * @return Plane at this section
	 */
	public Plane3D getPlaneAt(int section) {
		Plane3D p = (Plane3D) this.getRefPlane().clone();
		Vector3f offset = p.getNormal();
		float o = (float) (getSpacing() * (double) section);
		offset.scale(o);
		p.origin.add(offset);
		return p;
	}

	public ShapeSet3DInt getShapeSet3DInt(ShapeSelectionSet selSet) {
		// for each Shape2DInt in section, create Shape3DInt
		// determine coordinates from refPlane origin, normal vector, and
		// section spacing
		ShapeSet3DInt retSet = new ShapeSet3DInt();

		Iterator<Integer> itr = sections.keySet().iterator();
		while (itr.hasNext()) {
			Integer s = itr.next();
			ShapeSet2DInt set = sections.get(s);
			retSet.addUnionSet(
					ShapeFunctions.getShapeSet3DFromSection(this.getRefPlane(),
							(float) ((double) s.intValue() * getSpacing()),
							set, selSet), false);
		}

		return retSet;
	}

	public void updateShape() {

		boundBox = null;
		boundBox2D = null;
		boundSphere = null;
		if (centerPt == null)
			centerPt = (Point3f) ((Plane3D) shape3d).origin.clone();
		if (sections.size() == 0)
			return;

		float minZ = Float.MAX_VALUE;
		float maxZ = Float.MIN_VALUE;

		// bound box
		Iterator<Integer> itr = sections.keySet().iterator();
		while (itr.hasNext()) {
			Integer s = itr.next();
			ShapeSet2DInt set = sections.get(s);

			minZ = Math.min(minZ, s.intValue());
			maxZ = Math.max(maxZ, s.intValue());

			Rect2D bounds = set.getBounds();
			Rect3DInt bounds3d = (Rect3DInt) ShapeFunctions.getShape3DIntFromSection(getRefPlane(),
								 													(float)((double) s.intValue() * getSpacing()), 
								 													new Rect2DInt(bounds));
			Box3D bounds3D = bounds3d.getBoundBox();

			boundBox = GeometryFunctions.getUnionBounds(bounds3D, boundBox);
			boundBox2D = GeometryFunctions.getUnionBounds(bounds, boundBox2D);
		}

		// center point
		centerPt = GeometryFunctions.getCenterOfGravity(boundBox.getVertices());

		Plane3D thisPlane = (Plane3D) shape3d;
		Vector3f vX = new Vector3f();
		Vector3f vY = new Vector3f();
		Vector3f vZ = new Vector3f();
		Vector3f vR = new Vector3f();
		vX.set(thisPlane.xAxis);
		vY.set(thisPlane.yAxis);
		vZ.cross(vX, vY);

		vX.normalize();
		vY.normalize();
		vZ.normalize();

		vX.scale((boundBox2D.getWidth()) / 2.0f);
		vY.scale((boundBox2D.getHeight()) / 2.0f);
		vZ.scale((maxZ - minZ) / 2.0f);

		vR = new Vector3f(1, 1, 1);
		vR.scale(Math.max(Math.max(vX.length(), vY.length()), vZ.length()));

		boundSphere = new Sphere3D();
		boundSphere.center = new Point3f(centerPt);
		// boundSphere.center.add(vR);
		boundSphere.radius = vR.length() / 2.0f;

	}

	public int getFirstSection() {
		TreeSet<Integer> sorted = new TreeSet<Integer>(sections.keySet());
		return sorted.first();
	}

	public int getLastSection() {
		TreeSet<Integer> sorted = new TreeSet<Integer>(sections.keySet());
		return sorted.last();
	}

	public void setFromSectionSet(SectionSet3DInt thisSet) {
		setSpacing(thisSet.getSpacing());
		setRefPlane(thisSet.getRefPlane());
	}

	public SectionSet3DInt getInitSectionSet() {
		SectionSet3DInt retSet = new SectionSet3DInt();
		retSet.setFromSectionSet(this);
		return retSet;
	}

	public boolean validateNodes() {
		Iterator itr = getIterator();
		while (itr.hasNext()) {
			if (!((ShapeSet2DInt) itr.next()).validateNodes())
				return false;
		}
		return true;
	}

	public Iterator getIterator() {
		return new SectionSetIterator(this);
	}

	// return the number of shapes in this set
	public int getShapeCount() {
		int count = 0;
		Iterator<ShapeSet2DInt> itr = sections.values().iterator();
		while (itr.hasNext())
			count += itr.next().getShapeCount();
		return count;
	}

	public void updateLightShapes() {

		Iterator<ShapeSet2DInt> itr = sections.values().iterator();
		while (itr.hasNext()) {
			ShapeSet2DInt set = itr.next();
			for (int j = 0; j < set.members.size(); j++) {
				if (set.members.get(j) instanceof LightweightShape)
					((LightweightShape) set.members.get(j))
							.setAttributes(shapeAttr);
			}
		}
	}

	public boolean addShape(InterfaceShape shape) {
		return false;
	}

	public boolean moveShapeBefore(InterfaceShape shape, InterfaceShape target) {
		return false;
	}

	public Set<InterfaceShape> getShapeSet() {
		TreeSet<InterfaceShape> set = new TreeSet<InterfaceShape>(
				sections.values());
		return set;
	}

	/***************************************************
	 * Returns a list of all 2D shapes in this section set
	 * 
	 * @return
	 */
	public ArrayList<Shape2DInt> get2DShapes() {
		return get2DShapes(true);
	}

	/***************************************************
	 * Returns a list of all 2D shapes in this section set
	 * 
	 * @return
	 */
	public ArrayList<Shape2DInt> get2DShapes(boolean recurse) {

		ArrayList<ShapeSet2DInt> section_sets = new ArrayList<ShapeSet2DInt>(sections.values());
		ArrayList<Shape2DInt> list = new ArrayList<Shape2DInt>();

		for (int i = 0; i < section_sets.size(); i++) {
			list.add(section_sets.get(i));
			list.addAll(section_sets.get(i).get2DShapes(recurse));
		}

		return list;
	}

	public Set<ShapeSet> getSubSets() {
		return new TreeSet<ShapeSet>();
	}

	/****************************
	 * Not implemented.
	 */
	@Override
	public boolean hasShape(InterfaceShape s) {

		return false;
	}

	/****************************
	 * Not implemented.
	 */
	@Override
	public boolean hasShape(InterfaceShape s, boolean recurse) {
		return false;
	}

	public String getLocalName() {
		return "SectionSet3DInt";
	}

	public String getXML(int tab, XMLType type) {

		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);

		String _type = "full";
		if (type.equals(XMLType.Reference))
			_type = "reference";

		String xml = _tab + "<SectionSet3DInt type = '" + _type + "'";
		xml = xml + " spacing = '" + getSpacing() + "'";

		xml = xml + this.getRefPlane().getXML(tab + 1);

		xml = xml + attributes.getXML(tab + 1);

		xml = xml + _tab2 + "<ShapeAttributes>\n";
		xml = xml + shapeAttr.getXML(tab + 2);
		xml = xml + _tab2 + "</ShapeAttributes>\n";

		Iterator<Integer> itr = sections.keySet().iterator();
		while (itr.hasNext()) {
			ShapeSet2DInt set = sections.get(itr.next());
			xml = xml + set.getXML(tab + 1, type);
		}

		xml = xml + _tab + "</SectionSet3DInt>\n";
		return xml;

	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{

		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		String _tab3 = XMLFunctions.getTab(tab + 2);
		
		XMLType type = options.type;
		String _type = "full";		// Section sets are always full
		//if (type.equals(XMLType.Reference))
		//	_type = "reference";

		writer.write(_tab + "<InterfaceShape \n" + 
				_tab2 + "class = '" + getClass().getCanonicalName() + "'\n" +
				_tab2 + "name = '" + getName() + "'\n" +
				_tab2 + "type = '" + _type + "'\n" + 
				_tab + ">\n");

		getGeometry().writeXML(tab + 1, writer, options, progress_bar);
//		writer.write(_tab2 + "<RefPlane>\n");
//		getRefPlane().writeXML(tab + 2, writer, progress_bar);
//		writer.write(_tab2 + "</RefPlane>\n");
		writer.write("\n");

		switch (type){
			case Full:
			case Reference:
			case Short:
				ArrayList<Integer> _sections = new ArrayList<Integer>(sections.keySet());
				Collections.sort(_sections);
				writer.write(_tab2 + "<Sections>\n");
				for (int i = 0; i < _sections.size(); i++){
					writer.write(_tab3 + "<Section section = '" + _sections.get(i) + "'>\n");
					sections.get(_sections.get(i)).writeXML(tab + 3, writer, options, progress_bar);
					writer.write(_tab3 + "</Section>\n");
					}
				writer.write(_tab2 + "</Sections>\n");
				break;
			default:
				// Placeholder
			}
		
		if (attributes != null) {
			attributes.writeXML(tab + 1, writer, progress_bar);
			}
		
		//close up
		writer.write("\n" + _tab + "</InterfaceShape>");
	}

	private AttributeXMLHandler attribute_handler;
	private Plane3D current_plane;

	private XMLType xml_type;
	
//	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
//		
//		if (localName.equals("SectionSet3DInt")){
//			setName(attributes.getValue("name"));
//			xml_type = XMLType.Full;
//			if (attributes.getValue("type").equals("reference"))
//				xml_type = XMLType.Reference;
//			if (attributes.getValue("type").equals("short"))
//				xml_type = XMLType.Short;
//			return;
//		}
//
//		if (localName.equals("AttributeList")) {
//			attribute_handler = new AttributeXMLHandler();
//			try {
//				attribute_handler.startElement(null, "AttributeList", null,
//						attributes);
//			} catch (SAXException e) {
//				e.printStackTrace();
//			}
//			return;
//		}
//
//		if (attribute_handler != null) {
//			try {
//				attribute_handler.startElement(null, localName, null,
//						attributes);
//			} catch (SAXException e) {
//				e.printStackTrace();
//			}
//			return;
//		}
//
//		if (localName.equals("Plane3D") && current_plane != null){
//			current_plane.handleXMLElementStart(localName, attributes, null);
//		}
//
//
//	}
//
//	public void handleXMLElementEnd(String localName) {
//
//		if (localName.equals("AttributeList")) {
//			if (attribute_handler == null)
//				return;
//			try {
//				attribute_handler.endElement(null, "AttributeList", null);
//			} catch (SAXException e) {
//				e.printStackTrace();
//				}
//			shapeAttr = attribute_handler.getAttributeList();
//
//			attribute_handler = null;
//			return;
//		}
//
//		if (localName.equals("Attribute")) {
//			if (attribute_handler == null)
//				return;
//			try {
//				attribute_handler.endElement(null, localName, null);
//			} catch (SAXException e) {
//				e.printStackTrace();
//			}
//			return;
//		}
//
//		if (localName.equals("RefPlane")) {
//			if (current_plane == null)
//				return;
//			setRefPlane(current_plane);
//			return;
//		}
//
//		if (attribute_handler != null)
//			try {
//				attribute_handler.endElement(null, localName, null);
//				attribute_handler = null;
//			}catch (SAXException e){
//				e.printStackTrace();
//			}
//
//		if (current_plane != null)
//			current_plane.handleXMLElementEnd(localName);
//
//	}
//
//	public void handleXMLString(String s) {
//		if (attribute_handler != null) {
//			try {
//				attribute_handler.characters(s.toCharArray(), 0, s.length());
//			} catch (SAXException e) {
//				e.printStackTrace();
//			}
//			return;
//		}
//
//	}

	public InterfaceShape getLastAdded() {
		return last_added;
	}

	public InterfaceShape getLastRemoved() {
		return last_removed;
	}

	public InterfaceShape getLastModified() {
		return last_modified;
	}

	public InterfaceShape getLastInserted() {
		return last_inserted;
	}
	
	public InterfaceShape getLastMoved() {
		return last_moved;
	}

	@Override
	public int getLastInsert() {
		return -1;
	}

}