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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryUpdater;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.TransferHandler.TransferSupport;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import mgui.geometry.Box3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Shape;
import mgui.geometry.Shape3D;
import mgui.geometry.Sphere3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.maps.Camera3DListener;
import mgui.interfaces.menus.InterfaceMenu;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.attributes.ShapeAttribute;
import mgui.interfaces.shapes.mesh.VertexSelection;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.trees.Shape3DTreeNode;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.shapes.util.ShapeVertexObject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;

import org.apache.commons.collections15.Transformer;

import com.sun.j3d.utils.geometry.Sphere;

/***********************************************************
 * Base class for all <code>Shape3D</code> interface objects. Provides default implementations of a number of
 * methods, which should typically be overridden or utilized by subclasses, including:
 * 
 * <ul>
 * <li>obtaining Java3D scene nodes representing the shape
 * <li>obtaining <code>Shape2DInt</code> objects representing the projection or intersection of this 3D shape,
 * and maintaining a link with this object to update and/or query it when necessary 
 * with a particular plane, and particular projection limits
 * <li>obtaining a <code>Shape3DTreeNode</code> for insertion into a <code>JTree</code>
 * <li>setting and accessing vertex-wise data
 * <li>linking to a <code>DataSource</code> with vertex-wise index data
 * <li>obtaining an XML encoding of this shape and its attributes
 * <li>obtaining an <code>InterfacePopupMenu</code> and handling its events
 * </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class Shape3DInt extends InterfaceShape 
								 implements GeometryUpdater {

	public Shape3D shape3d;
	public DrawingEngine drawEngine2D;
	public String idStr = "";
	
	protected ArrayList<Shape2DInt> children2D = new ArrayList<Shape2DInt>();
	protected Shape2DInt parent2D;
	protected ShapeListener nodeListener;
	protected boolean hasCameraListener = false;
	public Box3D boundBox;
	public Sphere3D boundSphere;
	public Point3f centerPt;
	protected BranchGroup scene3DObject;
	protected ShapeSceneNode sceneNode;
	public BranchGroup boundBoxNode;
	protected javax.media.j3d.Shape3D bounds_shape;
	protected BranchGroup group_node;
	protected BranchGroup bounds_group;
	protected BranchGroup shape_group;
	protected BranchGroup vertices_group;
	protected BranchGroup selected_vertices_group;
	
	protected boolean has_3d_node = false;
	
	Attribute<?> modified_attribute;
	protected VertexShapeTransformer vertex_shape_transformer;
	
	protected boolean creatable_scene_node = true;
	
	@Override
	public Shape getGeometryInstance(){
		return null;
	}
	
	protected void init(){
		_init();
		
		ArrayList<String> render_modes = new ArrayList<String>();
		render_modes.add("Nicest");
		render_modes.add("Fastest");
		render_modes.add("Screen Door");
		
		attributes.add(new AttributeSelection<String>("3D.AlphaMode", render_modes, String.class, "Nicest"));
		
				
	}
	
	@Override
	protected Attribute<?> getParentAttribute(String attrName){
		if (!hasParentShape() || !isHeritableAttribute(attributes.getAttribute(attrName))) {
			return attributes.getAttribute(attrName);
			}
		ShapeSet3DInt parent = (ShapeSet3DInt)parent_set;
		if (!parent.isOverriding()) return attributes.getAttribute(attrName);
		return (parent.getAttribute(attrName));
	}
	
	@Override
	public boolean isHeritableAttribute(Attribute<?> attribute){
		if (!attribute.getName().startsWith("3D.")) return false;
		if (attribute.getName().equals("3D.Show")) return false;
		String name = attribute.getName();
		name = name.replaceFirst("3D.", "2D.");
		return attributes.hasAttribute(name);
	}
	
	@Override
	public boolean isInheritingAttribute(Attribute<?> attribute){
		if (!attribute.getName().startsWith("2D.")) return false;
		if (attribute.getName().equals("2D.Show")) return false;
		String name = attribute.getName();
		name = name.replaceFirst("2D.", "3D.");
		return attributes.hasAttribute(name);
	}
	
	@Override
	public Font getLabelFont(){
		return (Font)attributes.getValue("3D.LabelFont");
	}
	
	@Override
	public void setLabelFont(Font font){
		attributes.setValue("3D.LabelFont", font);
		attributes.setValue("2D.LabelFont", font);
	}
	
	@Override
	public float getLabelScale(){
		return ((MguiFloat)attributes.getValue("3D.LabelScale")).getFloat();
	}
	
	@Override
	public void setLabelScale(float scale){
		attributes.setValue("3D.LabelScale", new MguiFloat(scale));
		attributes.setValue("2D.LabelScale", new MguiFloat(scale));
	}
	
	@Override
	public Color getLabelColour(){
		return (Color)attributes.getValue("3D.LabelColour");
	}
	
	@Override
	public void setLabelColour(Color colour){
		attributes.setValue("3D.LabelColour", colour);
	}
	
	@Override
	public boolean hasAlpha(){
		return ((MguiBoolean)attributes.getValue("3D.HasAlpha")).getTrue();
	}
	
	@Override
	public void hasAlpha(boolean b){
		attributes.setValue("3D.HasAlpha", new MguiBoolean(b));
		attributes.setValue("2D.HasAlpha", new MguiBoolean(b));
	}
	
	@Override
	public float getAlpha(){
		return ((MguiFloat)attributes.getValue("3D.Alpha")).getFloat();
	}
	
	@Override
	public void setAlpha(float f){
		attributes.setValue("3D.Alpha", new MguiFloat(f));
		if (!this.inheritAttributesFromParent())
			attributes.setValue("2D.Alpha", new MguiFloat(f));
	}
	
	@Override
	public boolean showVertices(){
		return ((MguiBoolean)attributes.getValue("3D.ShowVertices")).getTrue();
	}

	@Override
	public void showVertices(boolean b){
		attributes.setValue("3D.ShowVertices", new MguiBoolean(b));
		if (!this.inheritAttributesFromParent())
			attributes.setValue("2D.ShowVertices", new MguiBoolean(b));
	}
	
	@Override
	public float getVertexScale(){
		return ((MguiFloat)attributes.getValue("3D.VertexScale")).getFloat();
	}
	
	/**************************************************
	 * Returns the scale for vertex i; this depends on whether <code>ScaleVertexValues</code>
	 * is set.
	 * 
	 * @param i
	 * @return
	 */
	public float getVertexScale(int i){
		
		boolean scale_vertices = ((MguiBoolean)attributes.getValue("ScaleVertices")).getTrue();
		float general_scale = ((MguiFloat)attributes.getValue("3D.VertexScale")).getFloat();
		float exp_scale = ((MguiFloat)attributes.getValue("3D.VertexScaleExp")).getFloat();
		
		if (!scale_vertices)
			return general_scale;
		
		String column = (String)attributes.getValue("ScaleData");
		if (column == null) return general_scale;
		
		float value = (float)getDatumAtVertex(column, i).getValue();
		return (float)Math.pow(general_scale * value, exp_scale);
	}
	
	@Override
	public Color getVertexColour(){
		return (Color)attributes.getValue("3D.VertexColour");
	}
	
	/************************************
	 * Returns the colour of vertex i; depends on whether <code>ColourVertexValues</code>
	 * is set.
	 * 
	 * @param i
	 * @return
	 */
	public Color getVertexColour(int i){
		return getVertexColour();
	}
	
	@Override
	public Color getLineColour(){
		return (Color)attributes.getValue("3D.LineColour");
	}
	
	@Override
	public Stroke getLineStyle(){
		return (Stroke)attributes.getValue("3D.LineStyle");
	}
	
	@Override
	public void setLineStyle(Stroke s){
		attributes.setValue("3D.LineStyle", s);
		if (!this.inheritAttributesFromParent())
			attributes.setValue("2D.LineStyle", s);
	}
	
	@Override
	public Shape getGeometry(){
		return this.getShape();
	}
	
	
	
	@Override
	public boolean setGeometry(Shape geometry){
		if (!(geometry instanceof Shape3D)) return false;
		setShape((Shape3D)geometry);
		return true;
	}
	
	@Override
	public String getFullName(){
		
		ShapeSet3DInt parent = (ShapeSet3DInt)getParentSet();
		if (parent == null){
			ShapeModel3D model = this.getModel();
			if (model == null)
				return getName();
			return model.getName() + "." + getName();
			}
		return parent.getFullName() + "." + getName();
	}
	
	public boolean hasCameraListener(){
		return hasCameraListener;
	}
	
	//Attempt to avoid memory leaks!?
	protected void releaseScene3DChildren(){
		if (scene3DObject == null) return;
		Enumeration children = this.scene3DObject.getAllChildren();
		
		while (children.hasMoreElements()){
			Node node = (Group)children.nextElement();
			if (node instanceof BranchGroup)
				((BranchGroup)node).detach();
			}
	}
	
	@Override
	public int getVertexCount(){
		if (shape3d == null) return -1;
		return shape3d.getSize();
	}
	
	public void setSelectedVertices(ArrayList<Integer> indices){
		VertexSelection selection = getVertexSelection();
		selection.clear();
		selection.select(indices);
	}
	
	@Override
	public String getTreeLabel(){
		return getName();
	}
	
	//override to set a specific icon
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/shape_3d_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/shape_3d_20.png");
	}
	
	protected void setCreatableSceneNode(boolean b){
		creatable_scene_node = b;
		if (!b && this.scene3DObject != null){
			this.sceneNode.destroy();
			}
	}
	
	/*****************************************************
	 * Returns the index of the vertex of this shape which is closest to <code>point</code>.
	 * 
	 * @param point
	 * @return
	 */
	public int getClosestVertex(Point3f point){
		return GeometryFunctions.getClosestVertex(shape3d, point);
	}
	
	//for super calls
	public final Icon getIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/shape_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/shape_3d_20.png");
		return null;
	}
	
	public void setParent2D(Shape2DInt parent){
		if (parent2D != null) parent2D.setChild3D(null);
		parent2D = parent;
	}
	
	public Shape2DInt getParent2D(){
		return parent2D;
	}
	
	@Override
	public void destroy(){
		isDestroyed = true;
		//destroy tree nodes
		for (int i = 0; i < tree_nodes.size(); i++)
			tree_nodes.get(i).destroy();
		ShapeEvent event = new ShapeEvent(this, ShapeEvent.EventType.ShapeDestroyed);
		//destroy children
		for (int i = 0; i < children2D.size(); i++)
			children2D.get(i).destroy(event);
		//destroy scene node 
		if (sceneNode != null)
			sceneNode.destroy();
		fireShapeListeners(event);
	}
	
	public void registerCameraListener(Camera3D c){
		
	}
	
	public void deregisterCameraListener(Camera3D c){
		
	}
	
	public Camera3DListener getCameraListener(){
		return null;
	}
	
	
	
	public boolean contains(Point3d thisPoint) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drawShape2D(Graphics2D g, DrawingEngine d){
		
	}
	
	/*****************************
	 * Draw this shape as it is intersected by plane p, as well as its projection from
	 * distances <= spacing / 2, if required. Method calls the protected method draw2D(),
	 * which should be overridden (rather than this one) by all subclasses wishing to 
	 * draw their shapes in 2D. 
	 * @param g Graphics2D where this shape will be drawn
	 * @param d DrawEngine that will do the drawing
	 * @param p Plane in R3 that (possibly) intersects the shape
	 * @param spacing Thickness of the section from which to project this shape to the
	 *                plane p.
	 */
	public void drawShape2D(Graphics2D g, DrawingEngine d, Plane3D p, float above_dist, float below_dist){
		drawShape2D(g,d,p,above_dist,below_dist,false);
	}
	
	/*****************************
	 * Draw this shape as it is intersected by plane p, as well as its projection from
	 * distances <= spacing / 2, if required. Method calls the protected method draw2D(),
	 * which should be overridden (rather than this one) by all subclasses wishing to 
	 * draw their shapes in 2D. 
	 * @param g Graphics2D where this shape will be drawn
	 * @param d DrawEngine that will do the drawing
	 * @param p Plane in R3 that (possibly) intersects the shape
	 * @param above_dist Thickness of the above part of the section from which to project this shape to the plane p.
	 * @param below_dist Thickness of the above part of the section from which to project this shape to the plane p.
	 * @param transform Transform to apply to the shape before rendering; can be {@code null}
	 */
	public void drawShape2D(Graphics2D g, DrawingEngine d, Plane3D p, float above_dist, float below_dist, boolean listen) {
		d.setAttributes(attributes);
		if (isOverridden)
			d.setAttributes(overrideAttr);
		
		Shape2DInt shape2d = getShape2D(p, above_dist, below_dist);
		
		draw2D(shape2d, g, d, p, above_dist, below_dist);
		String prefix = "2D.";
		if (inheritAttributesFromParent())
			prefix = "3D.";
		
		if (((MguiBoolean)attributes.getValue(prefix + "ShowBounds")).getTrue()){
			Box3D bounds = getBoundBox();
			
			Polygon2DInt poly = new Polygon2DInt(ShapeFunctions.getIntersectionPolygon(bounds, p));
			if (poly != null){
				poly.setAttribute(prefix + "LineColour", attributes.getValue(prefix + "BoundsColour"));
				draw2D(poly, g, d, p, above_dist, below_dist);
				}
			}
		
		if (listen){
			this.addShape2DChild(shape2d);
			}
	}
	
	/*****************
	 * Clears the currently set 2D children for this shape
	 * 
	 */
	public void clear2DChildren(){
		for (int i = 0; i < children2D.size(); i++)
			children2D.get(i).destroy();
		this.children2D.clear();
	}
	

	protected void draw2D(Shape2DInt shape2d, Graphics2D g, DrawingEngine d, Plane3D p, float above_dist, float below_dist)  {
		shape2d.drawShape2D(g, d);
	}
	
	public Shape2DInt getShape2DInt(Plane3D plane, float above_dist, float below_dist){
		return getShape2DInt(plane, above_dist, below_dist, true);
	}
	
	/******************************************************
	 * Determines a 2D shape which is the projection of this 3D shape onto <code>plane</code>, within the projection
	 * limits defined by <code>above_dist</code> and <code>below_dist</code>.
	 * 
	 * @param plane The plane on which to project this shape
	 * @param above_dist The projection limit above the plane
	 * @param above_dist The projection limit below the plane
	 * @param listen 
	 */
	public Shape2DInt getShape2DInt(Plane3D p, float above_dist, float below_dist, boolean listen){
//		if (boundBox == null) 
//			return null;
		if (!isVisible() || !show2D()) 
			return null;
		Shape2DInt shape = getShape2D(p, above_dist, below_dist);
		if (shape == null) return null;
		
		shape.setParentShape(this);
		
		return shape;
	}
	
	/*********************************************************
	 * Convenience method which determines whether this shape cross <code>plane</code>.
	 * 
	 * @param plane
	 * @return
	 * @see {@link mgui.geometry.util.GeometryFunctions.crossesPlane}
	 */
	public boolean crossesPlane(Plane3D plane){
		return GeometryFunctions.crossesPlane(getBoundBox(), plane);
	}
	
	//protected boolean getting_shape_2d = false;
	
	/**********************************
	 * To be overridden by subclasses implementing a 2D representation
	 * 
	 * @param plane				Plane on which to render
	 * @param above_dist		Distance above plane within which to project a shape
	 * @param below_dist		Distance below plane within which to project a shape
	 * @return
	 */
	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist) {
		
		return getShape2D(plane,above_dist,below_dist,null);
	}
	
	/**********************************
	 * To be overridden by subclasses implementing a 2D representation
	 * 
	 * @param plane				Plane on which to render
	 * @param above_dist		Distance above plane within which to project a shape
	 * @param below_dist		Distance below plane within which to project a shape
	 * @param matrix			Transformation matrix to apply; can be {@code null}
	 * @return
	 */
	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist, Matrix4d transform) {
		
		return null;
	}
	
	public void addShape2DChild(Shape2DInt shape){
		//remove all destroyed shapes
		for (int i = 0; i < children2D.size(); i++){
			if (children2D.get(i).equals(shape)) return;
			if (children2D.get(i).isDestroyed()){
				children2D.remove(i);
				i--;
				}
			}
		children2D.add(shape);
		
		// Set attribute lists which depend on parent's data columns
		AttributeSelection<String> attr = (AttributeSelection<String>)shape.getAttribute("CurrentData");
		attr.setList(data_columns);
		attr = (AttributeSelection<String>)shape.getAttribute("LabelData");
		attr.setList(data_columns);
	}
	
	public void removeShape2DChild(Shape2DInt shape){
		children2D.remove(shape);
	}
	
	public Sphere3D getBoundSphere() {
		return boundSphere;
	}
	
	public Box3D getBoundBox() {
		//if (boundBox == null) updateShape();
		return boundBox;
	}
	
	public Point3f getCenterOfGravity(){
		return boundSphere.center;
	}
	
	public Point3f getGeometricCenter(){
		return this.getBoundBox().getCenter();
	}

	public double getProximity(Point3d thisPoint) {
		
		return 0;
	}

	public Shape3D getShape() {
		return shape3d;
	}
	
	@Override
	public VertexSelection getVertexSelection(){
		//TODO: Implement vertex selection listener
		if (selected_nodes == null){
			selected_nodes = new VertexSelection(getVertexCount());
			}
		return selected_nodes;
	}
	
	@Override
	public void setVertexSelection(VertexSelection selection){
		if (selected_nodes == null){
			selected_nodes = new VertexSelection(getVertexCount());
			}
		this.selected_nodes.set(selection);
		this.updateSelectedVertices();
	}
	
	public void setShowSelectedVertices(boolean b){
		attributes.setValue("3D.ShowSelectedVertices", new MguiBoolean(b));
	}
	
	/**********************************************
	 * Updates the vertices
	 * 
	 */
	public void updateVertices(){
		if (group_node == null) return;
		if (((MguiBoolean)attributes.getValue("3D.ShowVertices")).getTrue() 
				&& this.getVertexCount() < InterfaceEnvironment.getMaxDisplayVertices()){
			if (vertices_group == null){
				vertices_group = new BranchGroup();
				vertices_group.setCapability(BranchGroup.ALLOW_DETACH);
				vertices_group.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
				vertices_group.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
				vertices_group.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
				group_node.addChild(vertices_group);
			}else{
				vertices_group.removeAllChildren();
				}
			Sphere sphere;
			Transform3D transform;
			TransformGroup tg;
			Shape3D shape = this.getShape();
			
			for (int i = 0; i < shape.getSize(); i++){
				float scale = getVertexScale(i);
				Appearance app = getVertexAppearance(i);
				sphere = new Sphere(scale, app);
				sphere.getShape().setUserData(new ShapeVertexObject(this, i));
				transform = new Transform3D();
				transform.set(new Vector3f(shape.getVertex(i)));
				tg = new TransformGroup(transform);
				tg.addChild(sphere);
				BranchGroup bg = new BranchGroup();
				bg.setCapability(BranchGroup.ALLOW_DETACH);
				bg.addChild(tg);
				vertices_group.addChild(bg);
				}
		}else{
			if (vertices_group != null){
				vertices_group.removeAllChildren();
				}
			if (((MguiBoolean)attributes.getValue("3D.ShowVertices")).getTrue()){
				InterfaceSession.log("Shape3DInt: Prevented rendering of vertices > max_display_vertices" +
									 " (this value is set in InterfaceEnvironment)", 
									 LoggingType.Warnings);
				}
			}
		
		
		
	}
	
	/**********************************************
	 * Updates the scene graph with the current vertex selection options
	 * 
	 */
	public void updateSelectedVertices(){
		if (group_node == null) return;
		if (selected_nodes != null && ((MguiBoolean)attributes.getValue("3D.ShowSelectedVertices")).getTrue()
				&& this.getVertexSelection().getSelectedCount() < InterfaceEnvironment.getMaxDisplayVertices()){
			if (selected_vertices_group == null){
				selected_vertices_group = new BranchGroup();
				selected_vertices_group.setCapability(BranchGroup.ALLOW_DETACH);
				selected_vertices_group.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
				selected_vertices_group.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
				selected_vertices_group.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
				group_node.addChild(selected_vertices_group);
				//System.out.println("UpdateSelectedVertices: new node created.");
			}else{
				selected_vertices_group.removeAllChildren();
				//System.out.println("UpdateSelectedVertices: children removed.");
				}
			Sphere sphere;
			Transform3D transform;
			TransformGroup tg;
			Material m = new Material();
			m.setDiffuseColor(new Color3f((Color)attributes.getValue("SelectedVertexColour")));
			Appearance app = new Appearance();
			app.setMaterial(m);
			
			Shape3D shape = this.getShape();
			float scale = this.getVertexScale();
			
			//System.out.print("UpdateSelectedVertices: adding children...");
			int count = 0;
			for (int i = 0; i < shape.getSize(); i++)
				if (selected_nodes.isSelected(i)){
					count++;
					sphere = new Sphere(scale, app);
					sphere.getShape().setUserData(new ShapeVertexObject(this, i));
					transform = new Transform3D();
					transform.set(new Vector3f(shape.getVertex(i)));
					tg = new TransformGroup(transform);
					tg.addChild(sphere);
					BranchGroup bg = new BranchGroup();
					bg.setCapability(BranchGroup.ALLOW_DETACH);
					bg.addChild(tg);
					selected_vertices_group.addChild(bg);
					}
			//System.out.println(" " + count + " added.");
		}else{
			if (selected_vertices_group != null){
				selected_vertices_group.removeAllChildren();
				//System.out.println("UpdateSelectedVertices: not visible.. removing children...");
				}
			if (((MguiBoolean)attributes.getValue("3D.ShowSelectedVertices")).getTrue()){
				InterfaceSession.log("Shape3DInt: Prevented rendering of selected vertices > max_display_vertices" +
									 " (this value is set in InterfaceEnvironment)", 
									 LoggingType.Warnings);
				}
			}
	}
	
	public int[] getDimensions() {
		return new int[]{this.getVertexCount()};
	}
	
	/***************************************
	 * Issues a new tree node for this ShapeInt. Creates the node and then calls {@link setTreeNode}
	 * to construct it. <code>setTreeNode</code> should be overridden by subclasses.
	 * 
	 * @return a new <code>InterfaceTreeNode</code>, which is an instance of <code>Shape3DTreeNode</code>. 
	 */
	@Override
	public InterfaceTreeNode issueTreeNode(){
		Shape3DTreeNode treeNode = new Shape3DTreeNode(this);
		setTreeNode(treeNode);
		tree_nodes.add(treeNode);
		return treeNode;
	}
	
	public void setShape(Shape3D newShape) {
		shape3d = newShape;
		this.selected_nodes = null;
		getVertexSelection();
	}

	/***************************************
	 * Updates the geometric bounds of this ShapeInt. Does not call {@link setScene3DObject}.
	 * 
	 */
	@Override
	public void updateShape() {
		if (shape3d == null) return;
		ArrayList<Point3f> nodes = shape3d.getVertices();
		if (nodes == null) return;
		if (nodes.size() == 0){
			boundSphere = null;
			boundBox = null;
			return;
			}
		
		//six values for max/min
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float minZ = Float.MAX_VALUE;
		float maxX = -Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE;
		float maxZ = -Float.MAX_VALUE;
		float xSum = 0, ySum = 0, zSum = 0;
		Tuple3f thisNode;
		
		for (int i = 0; i < nodes.size(); i++){
			thisNode = nodes.get(i);
			if (thisNode.x < minX) minX = thisNode.x;
			if (thisNode.y < minY) minY = thisNode.y;
			if (thisNode.z < minZ) minZ = thisNode.z;
			if (thisNode.x > maxX) maxX = thisNode.x;
			if (thisNode.y > maxY) maxY = thisNode.y;
			if (thisNode.z > maxZ) maxZ = thisNode.z;
			xSum += thisNode.x;
			ySum += thisNode.y;
			zSum += thisNode.z;
		}
		
		float deltaX = maxX - minX;
		float deltaY = maxY - minY;
		float deltaZ = maxZ - minZ;
		
		boundBox = shape3d.getBoundBox();
		
		Point3f center = new Point3f(minX + (deltaX / 2.0f), minY + (deltaY / 2.0f), minZ + (deltaZ / 2.0f));
		//centerPt = new Point3f(xSum / nodes.size(), ySum / nodes.size(), zSum / nodes.size());
		if (boundBox != null)
			centerPt = boundBox.getCenter();
		else
			centerPt = center;
		
		float radius = (float)Math.sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ));
		radius /= 2.0f;
		boundSphere = new Sphere3D(center, radius);
		
		if (boundBoxNode != null)
			setBoundBoxNode();
	}

	@Override
	public String toString(){
		return "Unspecified Shape 3D: " + getName();
	}
	
	public BranchGroup getScene3DObject(ShapeSelectionSet selSet){
		if (selSet.hasShape(this)) return getScene3DObject();
		return null;
	}
	
	/********************************************
	 * Returns a <code>BranchGroup</code> representing this ShapeInt. This node is subject to change, and therefore
	 * is not the Java3D node that should be inserted into a Java3D graph. This should instead be retrieved using
	 * <code>getShapeSceneNode</code>, which returns a stable node.
	 * 
	 * <p>TODO: reconsider the access modifier of this method. Should probably be protected to avoid confusion and
	 * undesired external access.
	 * 
	 * @return the scene node representing this ShapeInt
	 */
	public BranchGroup getScene3DObject(){
		
		if (scene3DObject == null){
			setScene3DObject();
			if (scene3DObject == null) return null;
			}
		
		return scene3DObject;
	}
	
	/*******************************************
	 * Sets a bounding box node for this ShapeInt, that can be retrieved using {@link getBoundBoxNode}.
	 * 
	 * <p>TODO: reconsider the access modifier of this method; probably doesn't need to be public
	 * 
	 */
	public void setBoundBoxNode(){
		if (scene3DObject == null){
			setScene3DObject();
			return;
			}
		if (boundBoxNode == null){
			boundBoxNode = new BranchGroup();
			boundBoxNode.setPickable(false);
			boundBoxNode.setCapability(BranchGroup.ALLOW_DETACH);
			boundBoxNode.setCapability(Group.ALLOW_CHILDREN_WRITE);
			boundBoxNode.setCapability(Group.ALLOW_CHILDREN_READ);
			boundBoxNode.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			boundBoxNode.setCapability(Node.ALLOW_PARENT_READ);
			scene3DObject.addChild(boundBoxNode);
		}else{
			//boundBoxNode.removeAllChildren();
			
			}
		
		try{
			if (boundBoxNode.getParent() == null)
				scene3DObject.addChild(boundBoxNode);
		}catch(Exception ex){
			// Do nothing
			
			}
		
		if (!((MguiBoolean)attributes.getValue("3D.ShowBounds")).getTrue() ||
			!show3D() ||
			!isVisible()){
			if (bounds_group != null)
			bounds_group.detach();
			return;
			}
		
		if (boundBox == null) return;
		
		//points
//		ArrayList<Point3f> points = boundBox.getVertices();
//		Point3f[] coords = new Point3f[4];
		
		QuadArray quads = (new Box3DInt(boundBox)).getFaces();
		
		//faces (6 of them)
//		QuadArray quads = new QuadArray(24, GeometryArray.COORDINATES);
//		//F1
//		coords[0] = points.get(0);
//		coords[1] = points.get(1);
//		coords[2] = points.get(2);
//		coords[3] = points.get(3);
//		quads.setCoordinates(0 * 4, coords);
//		//F2
//		coords[0] = points.get(0);
//		coords[1] = points.get(4);
//		coords[2] = points.get(5);
//		coords[3] = points.get(1);
//		quads.setCoordinates(1 * 4, coords);
//		//F3
//		coords[0] = points.get(5);
//		coords[1] = points.get(4);
//		coords[2] = points.get(7);
//		coords[3] = points.get(6);
//		quads.setCoordinates(2 * 4, coords);
//		//F4
//		coords[0] = points.get(2);
//		coords[1] = points.get(6);
//		coords[2] = points.get(7);
//		coords[3] = points.get(3);
//		quads.setCoordinates(3 * 4, coords);
//		//F5
//		coords[0] = points.get(0);
//		coords[1] = points.get(3);
//		coords[2] = points.get(7);
//		coords[3] = points.get(4);
//		quads.setCoordinates(4 * 4, coords);
//		//F6
//		coords[0] = points.get(1);
//		coords[1] = points.get(5);
//		coords[2] = points.get(6);
//		coords[3] = points.get(2);
//		quads.setCoordinates(5 * 4, coords);
		
		//set up nodes
		if (bounds_shape == null){
			bounds_shape = new javax.media.j3d.Shape3D(quads);
			bounds_shape.setCapability(javax.media.j3d.Shape3D.ALLOW_GEOMETRY_WRITE);
			bounds_shape.setCapability(javax.media.j3d.Shape3D.ALLOW_APPEARANCE_WRITE);
		}else{
			bounds_shape.setGeometry(quads);
			}
		
		//set appearance
		Appearance thisAppNode = new Appearance();
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		pAtt.setBackFaceNormalFlip(true);
		pAtt.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		Color edgeColour = (Color)attributes.getValue("3D.BoundsColour");
		ColoringAttributes cAtt = new ColoringAttributes();
		cAtt.setColor(new Color3f(edgeColour));
		thisAppNode.setPolygonAttributes(pAtt);
		thisAppNode.setColoringAttributes(cAtt);
		bounds_shape.setAppearance(thisAppNode);
		
		if (bounds_group == null){
			bounds_group = new BranchGroup();
			bounds_group.addChild(bounds_shape);
			bounds_group.setCapability(BranchGroup.ALLOW_DETACH);
			bounds_group.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			bounds_group.setCapability(Group.ALLOW_CHILDREN_WRITE);
			bounds_group.setCapability(Node.ALLOW_PARENT_READ);
			boundBoxNode.addChild(bounds_group);
		}else if (bounds_group.getParent() == null){
			boundBoxNode.addChild(bounds_group);
			}
		
	}
	
	@Override
	public Attribute<?> getModifiedAttribute(){
		return modified_attribute;
	}
	
	public BranchGroup getBoundBoxNode(){
		
		if (boundBoxNode == null) setBoundBoxNode();
		return boundBoxNode;
		
	}
	
	/**************************************************
	 * Sets this ShapeInt's Java3D scene node from its current geometry and rendering attributes. This node should
	 * be retrieved using the {@link getShapeSceneNode} method.
	 * 
	 * A scene node will only created if one of these conditions is met:
	 * 
	 * <ul>
	 * <li> The shape is auxiliary (i.e., not a model shape, but a helper shape such as a section polygon)
	 * <li> The shape is associated with a {@linkplain ShapeModel3D}, and this model is live (is associated with at least
	 * one Java3D scene graph).
	 * </ul>
	 * 
	 * Subclasses which call this super method should always check that a scene node has indeed been created.
	 * 
	 * @param make_live
	 */
	public void setScene3DObject(){
		setScene3DObject(true);
	}
	
	/**********************************************
	 * Sets this ShapeInt's Java3D scene node from its current geometry and rendering attributes. This node should
	 * be retrieved using the {@link getShapeSceneNode} method.
	 * 
	 * A scene node will only created if one of these conditions is met:
	 * 
	 * <ul>
	 * <li> The shape is auxiliary (i.e., not a model shape, but a helper shape such as a section polygon)
	 * <li> The shape is associated with a {@linkplain ShapeModel3D}, and this model is live (is associated with at least
	 * one Java3D scene graph).
	 * </ul>
	 * 
	 * Subclasses which call this super method should always check that a scene node has indeed been created.
	 * 
	 * @param make_live
	 */
	public void setScene3DObject(boolean make_live){
		
		if (!is_auxiliary && (getModel() == null || !getModel().isLive3D()))
			return;
		
		if (!this.creatable_scene_node) return;
		
		if (scene3DObject == null){
			scene3DObject = new BranchGroup();
			//set capabilities
			scene3DObject.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			scene3DObject.setCapability(Group.ALLOW_CHILDREN_WRITE);
			scene3DObject.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
			scene3DObject.setCapability(BranchGroup.ALLOW_DETACH);
			
			scene3DObject.setUserData(this);
		}else{
			
			}
		
		if (!this.isVisible() || !this.show3D()){
			//deactivateClips();
			scene3DObject.removeAllChildren();
			//reactivateClips();
			return;
			}
		
		//deactivateClips();
		setBoundBoxNode();
			
		if (make_live) setShapeSceneNode();
		//reactivateClips();
	}
	
	public void setShapeSceneNode(){
		//if (getModel() == null || !getModel().isLive3D())
		//deactivateClips();
		if (sceneNode == null)
			sceneNode = new Shape3DSceneNode(this, null); //, parent2D);
		else{
			sceneNode.setNode(this);
			}
		//reactivateClips();
	}
	
	public ArrayList<Point3f> getVertices(){
		if (shape3d != null)
			return shape3d.getVertices();
		return null;
	}
	
	public Point3f getVertex(int index){
		if (shape3d != null)
			return shape3d.getVertex(index);
		return null;
	}
	
	/******************************
	 * Responds to an update in one of this shape's attributes.
	 * 
	 * <p>Subclasses should override and call this super method AFTER handling the
	 * attribute change appropriately. Subclasses should also override needsRedraw() 
	 * to indicate whether the scene node should be regenerated.
	 * 
	 * @param e an <code>AttributeEvent</code> specifying which attribute has been changed
	 */
	public void attributeUpdated(AttributeEvent e){
		if (!notifyListeners) return;
		
		Attribute<?> attribute = e.getAttribute();
				
		if (attribute.getName().equals("DataMin") ||
				attribute.getName().equals("DataMax"))
			return;
		
		if (attribute.getName() == "3D.ShowBounds" ||
				attribute.getName() == "3D.BoundsColour")
			setBoundBoxNode();
		
		if (needsRedraw(attribute) && scene3DObject != null)
			setScene3DObject();
		
		if (needsRedraw(attribute))
			fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.AttributeModified));
		
		updateChildren2D(attribute);
	
	}
	
	// Flag to allow 2D attributes to change when they are inheriting values
	private boolean inheriting_attribute_can_change = false;
	
	/*******************************************
	 * Called when this shape's geometry has changed
	 * 
	 */
	public void geometryChanged(){
		
	}
	
	@Override
	public void fireShapeModified(){
		super.fireShapeModified();
		fireChildren2DModified();
	}
	
	@Override
	protected void fireShapeListeners(ShapeEvent e){
		super.fireShapeListeners(e);
		fireChildren2D(e);
	}
	
	/*********************************************
	 * Fires a {@linkplain ShapeEvent} for this shape's 2D children
	 * 
	 */
	public void fireChildren2D(ShapeEvent e){
		for (int i = 0; i < children2D.size(); i++)
			children2D.get(i).fireShapeListeners(e);
	}
	
	/*********************************************
	 * Fires a shape modified event for this shape's 2D children
	 * 
	 */
	public void fireChildren2DModified(){
		for (int i = 0; i < children2D.size(); i++)
			children2D.get(i).fireShapeModified();
	}
	
	/***************************************
	 * Updates an attribute of this shape's 2D children.
	 * 
	 * @param a
	 */
	protected void updateChildren2D(Attribute<?> a){

		
		if (inheritAttributesFromParent()){
			// Don't update child if it is inheriting this attribute from its parent
//			if (isInheritingAttribute(a)) 
//				return;
			// Inherit 3D value if this attribute is heritable
			if (isHeritableAttribute(a)){
				Attribute<?> attr2 = this.getInheritingAttribute(a);
				String name = attr2.getName();
				for (int i = 0; i < children2D.size(); i++){
					if (!children2D.get(i).attributes.hasAttribute(name)) return;
					children2D.get(i).attributes.setValue(name, a.getValue());
					}
				return;
				}
			}
		
		// Set child's attribute normally
		for (int i = 0; i < children2D.size(); i++)
			children2D.get(i).attributes.setValue(a);
	}
	
	/****************************************
	 * Returns the 2D attribute inheriting from a 3D attribute. Use when InheritFromParent=true.
	 * Subclasses should override this method if certain 3D attributes are not intended
	 * to be inherited (e.g., vertex scales).
	 * 
	 * @param name
	 * @return The inheriting attribute, or {@code null} if attribute is not heritable
	 */
	public Attribute<?> getInheritingAttribute(Attribute<?> attribute){
		if (!isHeritableAttribute(attribute)) return null;
		String name = attribute.getName();
		String name2 = name.replace("3D.", "2D.");
		return attributes.getAttribute(name2);
		
	}
	
	@Override
	public boolean needsRedraw(Attribute<?> a){
		
		if (a.getName().startsWith("2D") ||
			a.getName().equals("2D.Show") ||
			a.getName().equals("Name") ||
			a.getName().equals("3D.ShowBounds") ||
			a.getName().equals("3D.BoundsColour") ||
			a.getName().equals("UrlReference")) return false;
		if (a.getName().equals("InheritFromParent") &&
				!this.hasParentShape())
			return false;
		if (a instanceof ShapeAttribute){
			return ((ShapeAttribute<?>)a).needsRedraw3D();
			}
		return true;
	}
	
	@Override
	public boolean hasParentShape(){
		return this.parent_set != null;
	}
	
	/*****************************************************
	 * Returns this ShapeInt's Java3D node. This node is stable, meaning that only its children are subject to change;
	 * the node is regenerated only when this method is called for the first time, or if it has been destroyed.
	 * Thus it is suitable for insertion into a Java3D scene graph.
	 * 
	 * @return a
	 */
	public ShapeSceneNode getShapeSceneNode(){
		if (!this.creatable_scene_node) return null;
		if (sceneNode == null){
			setScene3DObject(true);
			}
		return sceneNode;
	}
	
	
	public ShapeSceneNode getShapeSceneNode(ShapeSelectionSet s){
		if (!this.creatable_scene_node) return null;
		return new Shape3DSceneNode(this, s);
	}
	
	public void updateData(Geometry geometry) {
		
	}
	
	public String getDTD() {
		
		return null;
	}

	public String getLocalName() {
		return "Shape3DInt";
	}
	
	
	
	public String getShortXML(int tab){
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<Shape3DInt class = '" + getClass().getCanonicalName() + "' name = '" + getName() + "' />\n";
		
		return xml;
		
	}
	

	/****************************
	 * Subclasses should override this if necessary and call super.getPopupMenu() to get
	 * this top-level menu and add items to it. 
	 * 
	 */
	public InterfacePopupMenu getPopupMenu() {
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		
		menu.addMenuItem(new JMenuItem("Shape3DInt", getIcon()));
		
		menu.add(new JSeparator(), 1);
		menu.add(new JSeparator(), 1);
		
		menu.addMenuItem(new JMenuItem("Edit attributes.."));
		menu.add(new JSeparator(), 4);
		
		menu.addMenuItem(new JMenuItem("Copy"));
		menu.addMenuItem(new JMenuItem("Cut"));
		menu.addMenuItem(new JMenuItem("Delete"));
		if (this.isVisible())
			menu.addMenuItem(new JMenuItem("Hide"));
		else
			menu.addMenuItem(new JMenuItem("Show"));
		
		menu.add(new JSeparator());
		
		InterfaceMenu menu2 = new InterfaceMenu("Order", null);
		
		menu2.addMenuItem(new JMenuItem("To top"));
		menu2.addMenuItem(new JMenuItem("Up one"));
		menu2.addMenuItem(new JMenuItem("Down one"));
		menu2.addMenuItem(new JMenuItem("To bottom"));
		
		menu.addMenuItem(menu2);
		menu.addMenuItem(new JMenuItem("Rename"));
		
		return menu;
	}
	
	protected int getPopupMenuLength(){
		return 12;
	}

	/****************************
	 * Subclasses should override this if necessary and call super.getGraphic3DPopupMenu() to get
	 * this top-level menu and add items to it. 
	 * 
	 */
	public void setGraphic3DPopupMenu(InterfacePopupMenu menu){
		
		JMenuItem item = new JMenuItem("Edit attributes..");
		
		menu.addMenuItem(item);
		item.setActionCommand("Edit Shape Attributes");
		menu.addMenuItem(new JMenuItem("Hide"));
		menu.addMenuItem(new JMenuItem("Clear selection"));
		
	}
	
	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getText().equals("Delete")){
			//self destruct!
			if (JOptionPane.showConfirmDialog(getModel().getDisplayPanel(), "Really delete shape '" + getName() + "'?", 
				  	  "Delete Shape", 
				      JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
				
				if (this instanceof ShapeSet3DInt && 
					getModel().getModelSet().equals(this)){
								JOptionPane.showMessageDialog(getModel().getDisplayPanel(), "Can't delete a model set!");
				}else{
					if (getParentSet() == null) return;	//shouldn't happen
					getParentSet().removeShape(this);
					return;
					}
				}
			}
		
		if (item.getText().equals("Edit attributes..")){
			InterfaceSession.getWorkspace().showAttributeDialog(this);
			return;
			}
		
		if (item.getText().equals("Hide")){
			setVisible(false);
			return;
			}
		
		if (item.getText().equals("Show")){
			setVisible(true);
			return;
			}
		
		if (item.getText().equals("Rename")){
			String new_name = JOptionPane.showInputDialog(getModel().getDisplayPanel(), 
														  "New name:",									  
														  "Rename shape 3D", 
														  JOptionPane.QUESTION_MESSAGE);
			if (new_name == null) return;
			setName(new_name);
			return;
			}
		
		if (item.getText().equals("Clear selection")){
			//TODO: Implement vertex selection listener
			getVertexSelection().clear();
			if (((MguiBoolean)attributes.getValue("3D.ShowSelectedVertices")).getTrue())
				updateSelectedVertices();
			}
		
	}
	
	
	
	/**************************************
	 * Returns the appearance of vertex i.
	 * 
	 * @param i
	 * @return
	 */
	protected Appearance getVertexAppearance(int i){
		//Material m = new Material();
		//m.setDiffuseColor(new Color3f((Color)attributes.getValue("VertexColour")));
		//Appearance app = new Appearance();
		//app.setMaterial(m);
		Color3f colour = new Color3f(getVertexColour(i));
		Material material = new Material(); //colour, new Color3f(Color.black), colour, colour, 100f);
		material.setDiffuseColor(colour);
		Appearance app = new Appearance();
		app.setMaterial(material);
		return app;
	}
	
	public void setCurrentColumn(String key, boolean update){
		super.setCurrentColumn(key, update);
		
//		if (showData())
//			setScene3DObject(update);
	}

	public void showPopupMenu(MouseEvent e) {
		InterfacePopupMenu menu = getPopupMenu();
		if (menu == null) return;
		menu.show(e);
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.getRepresentationClass().isAssignableFrom(Shape3DInt.class)) 
			return this;
		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavors = new DataFlavor[1];
		String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + Shape3DInt.class.getName();
		try{
			flavors[0] = new DataFlavor(mimeType);
		}catch (ClassNotFoundException cnfe){
			//if this happens then hell freezes over :-/
			cnfe.printStackTrace();
			return new DataFlavor[0];
			}
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		//flavor class must be an instance of Shape3DInt
		//in this case, an insert will be performed, moving the transferred shape(s) above this
		//one in the parent set.
		Class<?> this_class = flavor.getRepresentationClass();
		return Shape3DInt.class.isAssignableFrom(this_class);
	}
	
	/**************************************************
	 * Moves a list of shapes to a position above this shape in its parent set. Does nothing if this shape does
	 * not have a parent set.
	 * 
	 * @return <code>true</code> if successful.
	 */
	@Override
	public boolean performTransfer(TransferSupport support){
		
		if (getParentSet() == null) return false;
		Transferable transferable = support.getTransferable();
		
		//TODO: support user actions (e.g., ctrl-drop => copy) 
		ArrayList<Transferable> data = null;
		
		try{
			data = (ArrayList<Transferable>)transferable.getTransferData(this.getTransferDataFlavors()[0]);
		}catch (Exception e){
			e.printStackTrace();
			return false;
			}
		
		boolean success = data.size() > 0;
		
		//for all transferables (shapes)
		for (int i = 0; i < data.size(); i++){
			Transferable t = data.get(i);
			//if t is not a Shape3DInt or t is equal to this shape, do nothing 
			if (!(t instanceof Shape3DInt) || this.equals(t)){
				success &= false;
			}else{
				Shape3DInt shape = (Shape3DInt)t;
				//if moved shape is not a member of this shape's parent set, make it one
				boolean ok = true;
				if (!getParentSet().hasShape(shape, false))
					ok = getParentSet().addShape(shape);
				
				success &= ok;
				
				//move shape to position before this shape
				if (ok)	success &= getParentSet().moveShapeBefore(shape, this);
				}
			}
		
		return success;
	}
	
	/*
	public class ShapeNodeRef {
		public int node;
		public Shape3DInt shape;
		
		public ShapeNodeRef(Shape3DInt shape, int node){
			this.shape = shape;
			this.node = node;
		}
	}
	*/
	
	/***************************************************************
	 * Transformer determines the rendering of vertices
	 * 
	 * @author Andrew Reid
	 * @version 1.0
	 * @since 1.0
	 *
	 */
	protected class VertexShapeTransformer implements Transformer<Integer, Node>{

		public VertexShapeTransformer(){
			
		}
		
		@Override
		public Node transform(Integer i) {
			//Uniform scale or scale with values?
			float scale = ((MguiFloat)attributes.getValue("VertexScale")).getFloat();
			
			//Uniform colour or colour with values?
			Color3f colour = new Color3f((Color)attributes.getValue("VertexColour"));
			Material material = new Material(colour, new Color3f(Color.black), colour, colour, 100f);
			Appearance appearance = new Appearance();
			appearance.setMaterial(material);
			
			Sphere sphere = new Sphere(scale, 
									   Sphere.GENERATE_NORMALS | 
									   Sphere.ENABLE_GEOMETRY_PICKING |
									   Sphere.ENABLE_APPEARANCE_MODIFY, 
									   appearance);
			
			return sphere;
		}
		
		
	}
	
}