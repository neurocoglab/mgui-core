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
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.jogamp.java3d.BranchGroup;
import javax.swing.ImageIcon;
import javax.swing.TransferHandler.TransferSupport;
import org.jogamp.vecmath.Point2f;

import mgui.geometry.Rect2D;
import mgui.geometry.util.SpatialUnit;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.trees.Shape2DTreeNode;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.numbers.MguiBoolean;
import mgui.util.IDFactory;

/**********************************************************
 * Represents a set of <code>Shape2DInt</code> objects. This class is itself a descendant of <code>Shape2DInt</code>,
 * which means it inherits much of its behaviour and attributes, and can be added to another <code>ShapeSet2DInt</code>.
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
public class ShapeSet2DInt extends Shape2DInt
						   implements ShapeSet {

	private boolean isOverriding = false;
	protected IDFactory idFactory = new IDFactory();
	
	/**
	 * TODO: replace this with a hashmap, with ID as key.
	 * Also provide a name map to ensure 1. no duplicate names and 2. shapes can be
	 * referenced by name as well as ID.
	 */
	
	public ArrayList<Shape2DInt> members = new ArrayList<Shape2DInt>();
	protected SectionSet3DInt parent_section_set;
	
	Shape2DInt last_removed;
	Shape2DInt last_added;
	Shape2DInt last_modified;
	Shape2DInt last_inserted;
	int last_insert = -1;
	
	protected Shape2DSectionNode section_node;
	
	public ShapeSet2DInt(){
		super();
		init();
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
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/shape_set_2d_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/shape_set_2d_20.png");
	}
	
	private void init(){
		attributes.add(new Attribute<MguiBoolean>("IsOverriding", new MguiBoolean(false)));
		
		attributes.add(new AttributeSelection<SpatialUnit>("Unit", InterfaceEnvironment.getSpatialUnits(), SpatialUnit.class));
		setUnit(InterfaceEnvironment.getSpatialUnit("meter"));
		
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
	
	@Override
	public SpatialUnit getUnit(){
		return (SpatialUnit)attributes.getValue("Unit");
	}
	
	public void setUnit(SpatialUnit unit){
		attributes.setValue("Unit", unit);
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
	
	public InterfaceShape getLastAdded(){
		return last_added;
	}
	
	public InterfaceShape getLastInserted(){
		return last_inserted;
	}
	
	public int getLastInsert(){
		return last_insert;
	}
	
	@Override
	public ShapeModel3D getModel(){
		if (parent_section_set == null) return null;
		return parent_section_set.getModel();
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
	
	/**************************
	 * Applies this set's attributes to all members.
	 *
	 */
	public void applyOverride(){
		//((MguiBoolean)attributes.getValue("IsOverriding")).value = true;
		attributes.setValue("IsOverriding", new MguiBoolean(true));
		isOverriding = true;
		//apply to each member
		for (int i = 0; i < members.size(); i++)
			members.get(i).setOverride(attributes);
	}
	
	public void removeOverride(){
		//((MguiBoolean)attributes.getValue("IsOverriding")).value = false;
		attributes.setValue("IsOverriding", new MguiBoolean(false));
		isOverriding = false;
		//apply to each member
		for (int i = 0; i < members.size(); i++)
			members.get(i).unsetOverride();
	}
	
	public boolean isOverriding(){
		return ((MguiBoolean)attributes.getValue("IsOverriding")).getTrue();
	}
	
	public boolean isVisible(int i){
		if (i >= members.size()) return false;
		return members.get(i).isVisible();
	}
	
	/*******************************************
	 * Returns the number of members in this set
	 * 
	 */
	public int getSize(){
		return members.size();
	}
	
	public ArrayList<Shape2DInt> get2DShapes(){
		return get2DShapes(true);
	}
	
	/*****************************************************
	 * Returns all shapes in this shape set; if <code>recurse</code> is <code>false</code>, limits this list
	 * to the set's members; otherwise, also adds all members of all subsets (along with the sets themselves).
	 * 
	 * @param recurse
	 * @return
	 */
	public ArrayList<Shape2DInt> get2DShapes(boolean recurse){
		
		ArrayList<Shape2DInt> shapes = new ArrayList<Shape2DInt>();
		
		for (int i = 0; i < members.size(); i++){
			shapes.add(members.get(i));
			if (members.get(i) instanceof ShapeSet2DInt && recurse)
				shapes.addAll(((ShapeSet2DInt)members.get(i)).get2DShapes(true));
			}
		
		return shapes;
	}
	
	public Shape2DInt getShape(int i){
		if (i >= members.size()) return null;
		return members.get(i);
	}
	
	@Override
	public void destroy(){
		isDestroyed = true;
		//remove pointer in parent shape if one exists
		if (parentShape != null)
			parentShape.removeShape2DChild(this);
		for (int i = 0; i < members.size(); i++)
			members.get(i).destroy();
	}
	
	public boolean hasShape(InterfaceShape s){
		return hasShape(s, true);
	}
	
	public boolean hasShape(InterfaceShape s, boolean recurse){
		if (!(s instanceof Shape2DInt)) return false;
		for (int i = 0; i < members.size(); i++){
			if (members.get(i).equals(s))
				return true;
			if (recurse &&
				members.get(i) instanceof ShapeSet &&
					((ShapeSet)members.get(i)).hasShape(s))
				return true;
			}
		return false;
	}
	
	public boolean addShape(InterfaceShape shape) {
		if (shape instanceof Shape2DInt){
			addShape((Shape2DInt)shape, true, true);
			return true;
			}
		return false;
	}

	public Set<ShapeSet> getSubSets(){
		TreeSet<ShapeSet> subsets = new TreeSet<ShapeSet>();
		for (int i = 0; i < members.size(); i++)
			if (members.get(i) instanceof ShapeSet)
				subsets.add((ShapeSet)members.get(i));
		return subsets;
	}
	
	public Set<InterfaceShape> getShapeSet() {
		return new TreeSet<InterfaceShape>(members);
	}
		
	public int addShape(Shape2DInt thisShape, boolean updateShape, boolean updateListeners){
		return addShape(thisShape, -1, updateShape, updateListeners);
	}
	
	public int addShape(Shape2DInt shape, int index, boolean updateShape, boolean updateListeners){
		if (shape == null) return -1;
		
		if (index < 0)
			members.add(shape);
		else
			members.add(index, shape);
		
		if (updateShape){
			shape.setParentSet(this);
			shape.updateShape();
			shape.addShapeListener(this);
			updateShape();
			}
		
		if (updateListeners){
			last_added = shape;
			fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.ShapeAdded));
			last_added = null;
			}
		
		return members.size();
	}
	
	public int addShape(Shape2DInt thisShape, int index){
		return addShape(thisShape, index, true, true);
	}
	
	public int addShape(Shape2DInt thisShape, int index, boolean update){
		return addShape(thisShape, index, update, update);
	}
	
	public void removeShape(InterfaceShape shape){
		removeShape((Shape2DInt)shape, true, true);
	}
	
	public void removeShape(Shape2DInt shape, boolean updateShape, boolean updateListeners){
		members.remove(shape);
		shape.removeShapeListener(this);
		shape.parent_set = null;
		
		if (updateShape)
			updateShape();
		
		if (updateListeners){
			last_removed = shape;
			fireShapeListeners(new ShapeEvent(shape, ShapeEvent.EventType.ShapeRemoved));
			last_removed = null;
			}
	}
	
	public boolean moveShapeBefore(InterfaceShape shape, InterfaceShape target){
		return false;
	}
	
	public InterfaceShape getLastRemoved(){
		return last_removed;
	}
	
	public void removeShape(int index){
		members.remove(index);
	}
	
	public InterfaceShape getLastModified(){
		return last_modified;
	}
	
	public void drawMember2D(int index, Graphics2D g, DrawingEngine d){
		if (!isVisible() || !show2D()) return;
		if (isOverriding() && !isOverriding)
			applyOverride();
		if (!isOverriding() && isOverriding)
			removeOverride();
		if (members.get(index).isVisible() && members.get(index).show2D())
			members.get(index).drawShape2D(g, d);
	}
	
	@Override
	public void drawShape2D(Graphics2D g, DrawingEngine d){
		if (!isVisible() || !show2D()) return;
		if (isOverriding() && !isOverriding)
			applyOverride();
		if (!isOverriding() && isOverriding)
			removeOverride();
		for (int i = 0; i < members.size(); i++)
			if (members.get(i).isVisible() && members.get(i).show2D())
				members.get(i).drawShape2D(g, d);
	}
	
	public Shape2DInt getMember(int i){
		return members.get(i);
	}
	
	@Override
	public InterfaceTreeNode issueTreeNode(){
		Shape2DTreeNode treeNode = new Shape2DTreeNode(this);
		setTreeNode(treeNode);
		tree_nodes.add(treeNode);
		return treeNode;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		for (int i = 0; i < members.size(); i++)
			treeNode.add(members.get(i).issueTreeNode());
	}

	
	@Override
	public String toString(){
		return "ShapeSet2DInt: " + getName() + "[" + getID() + "]" ;
	}
	
	public int addUnionSet(ShapeSet2DInt thisSet){
		return addUnionSet(thisSet, true, true);
	}
	
	
	public int addUnionSet(ShapeSet2DInt thisSet, boolean updateShape, boolean updateListeners){
		Shape2DInt shape;
		for (int i = 0; i < thisSet.members.size(); i++){
			shape = thisSet.members.get(i);
			if (updateShape){
				shape.updateShape();
				//shape.setModel(model);
				shape.setParentSet(this);
				shape.addShapeListener(this);
				}
			members.add(shape);
			}
		
		if (updateShape)
			updateShape();
		
		if (updateListeners)
			fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.ShapeModified));
		return members.size();
	}
	
	@Override
	public void updateShape(){
		if (bounds == null)
			bounds = new Rect2D();
		
		if (members.size() == 0){
			centerPt = new Point2f();
			return;
			}
		
		bounds.corner1.x = Float.MAX_VALUE;
		bounds.corner1.y = Float.MAX_VALUE;
		bounds.corner2.x = -Float.MAX_VALUE;
		bounds.corner2.y = -Float.MAX_VALUE;
		float xSum = 0, ySum = 0;
		
		for (int i = 0; i < members.size(); i++){
			Rect2D member_bounds = members.get(i).getBounds();
			if (member_bounds != null){
				bounds.corner1.x = Math.min(bounds.corner1.x, 
											member_bounds.getCorner(Rect2D.CNR_BL).x);
				bounds.corner1.y = Math.min(bounds.corner1.y, 
											member_bounds.getCorner(Rect2D.CNR_BL).y);
				bounds.corner2.x = Math.max(bounds.corner2.x, 
											member_bounds.getCorner(Rect2D.CNR_TR).x);
				bounds.corner2.y = Math.max(bounds.corner2.y, 
											member_bounds.getCorner(Rect2D.CNR_TR).y);
				xSum += members.get(i).getCenterPoint().x;
				ySum += members.get(i).getCenterPoint().y;
				}
		}
		
		if (centerPt == null) centerPt = new Point2f();
		
		centerPt.x = bounds.corner1.x + bounds.getWidth() / 2f;
		centerPt.y = bounds.corner1.y + bounds.getHeight() / 2f;
		
		return;
	}

	@Override
	public void shapeUpdated(ShapeEvent e){
		
		switch (e.eventType){
			case AttributeModified:
			case ShapeModified:
			case VertexColumnChanged:
				fireShapeListeners(e);
				return;
			}
		
	}
	
	public ShapeSet2DInt getShapeType(Shape2DInt thisShape){
		return getShapeType(thisShape, false);
	}
	
	public ShapeSet2DInt getShapeType(Shape2DInt thisShape, boolean recurse){
		ShapeSet2DInt thisSet = new ShapeSet2DInt();
		return getShapeType(thisShape, thisSet, recurse);
		
	}
	
	public ShapeSet2DInt getShapeType(Shape2DInt thisShape, ShapeSet2DInt thisSet, boolean recurse){
		//if thisShape is a shapeset, special case
		if (ShapeSet2DInt.class.isInstance(thisShape)){
			for (int i = 0; i < members.size(); i++)
				if (recurse && 
					ShapeSet2DInt.class.isInstance(members.get(i))){
					thisSet.addShape(members.get(i), false, false);
					((ShapeSet2DInt)members.get(i)).getShapeType(thisShape, thisSet, true);
					}
			return thisSet;
			}
		
		for (int i = 0; i < members.size(); i++){
			if (recurse && ShapeSet2DInt.class.isInstance(members.get(i)))
				thisSet.addUnionSet(((ShapeSet2DInt)members.get(i)).getShapeType(thisShape), false, false);
			else if (thisShape.getClass().isInstance(members.get(i)))
				thisSet.addShape(members.get(i), false, false);
		}
		return thisSet;
	}
	
	@Override
	public ArrayList<Point2f> getVertices(){
		ArrayList<Point2f> retObj = new ArrayList<Point2f>();
		for (int i = 0; i < members.size(); i++)
			retObj.addAll(members.get(i).thisShape.getVertices());
		return retObj;
	}
	
	@Override
	public boolean validateNodes(){
		ArrayList<Point2f> nodeList = getVertices();
		for (int i = 0; i < nodeList.size(); i++)
			if (Float.isNaN(nodeList.get(i).x) || Float.isNaN(nodeList.get(i).y))
				return false;
		return true;
	}
	
	//return number of shapes in this set and all of its subsets
	public int getShapeCount(){
		int count = 0;
		for (int i = 0; i < members.size(); i++)
			if (members.get(i) instanceof ShapeSet2DInt)
				count += ((ShapeSet2DInt)members.get(i)).getShapeCount();
			else
				count++;
		return count;
	}
	
	public void setShapeSceneNode(SectionSet3DInt s, int section){
		setShapeSceneNode(s, section, false, null);
	}
	
	public void setShapeSceneNode(SectionSet3DInt s, int section, boolean update){
		setShapeSceneNode(s, section, update, null);
	}
	
	public void setShapeSceneNode(SectionSet3DInt s, int section, boolean update, ShapeSelectionSet filter){
		BranchGroup scene3DObject = new BranchGroup();
		boolean add;
		
		/*
		//get ShapeSceneNode for each member
		for (int i = 0; i < members.size(); i++){
			add = (filter == null || filter.hasShape(members.get(i)));
			
			if (add){
				//force update (i.e., if some global change such as to spacing is made)
				if (update)
					members.get(i).setShapeSceneNode(s, section);
				ShapeSceneNode n = members.get(i).getShapeSceneNode();
				if (n == null){
					members.get(i).setShapeSceneNode(s, section);
					n = members.get(i).getShapeSceneNode();
					}
				n.detach();
				scene3DObject.addChild(n);
				}
			}
		
		
		scene3DObject.setCapability(BranchGroup.ALLOW_DETACH);
		scene3DObject.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		scene3DObject.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		sceneNode = new Shape3DSceneNode(null);
		
		sceneNode.addChild(scene3DObject);
		*/
		
	}
	
	public Shape2DSectionNode getShapeSectionNode(SectionSet3DInt set, int section){
		
		if (section_node == null)
			setShapeSectionNode(set, section);
		
		return section_node;
		
	}
	
	public void setShapeSectionNode(SectionSet3DInt set, int section){
		
		section_node = new Shape2DSectionNode(set, section, this);
	}
	
	public void writeXML(int tab, Writer writer, XMLType type, InterfaceProgressBar progress_bar) throws IOException{
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String _type = "full";
		if (type.equals(XMLType.Reference)) _type = "reference";
		
		writer.write(_tab + "<ShapeSet2DInt \n" +
						_tab2 + "name = '" + getName() + "'\n" +
						_tab2 + "type = '" + _type + "'\n" + 
						_tab + ">\n");
		
		if (attributes != null){
			writer.write(_tab2 + "<AttributeList>\n");
			attributes.writeXML(tab + 2, writer, progress_bar);
			writer.write(_tab2 + "</AttributeList>\n");
			}
		
		//subclasses write their XML here
		switch (type){
			case Full:
				writer.write(_tab2 + "<Members>\n");
				for (int i = 0; i < members.size(); i++)
					members.get(i).writeXML(tab + 2, writer, progress_bar);
				writer.write(_tab2 + "</Members>\n");
				break;
			
			}
		
		//close up
		writer.write(_tab + "</ShapeSet2DInt>\n");
		
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors(){
		DataFlavor[] flavors = new DataFlavor[2];
		String mimeType1 = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + ShapeSet2DInt.class.getName();
		String mimeType2 = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + Shape2DInt.class.getName();
		try{
			flavors[0] = new DataFlavor(mimeType1);
			flavors[1] = new DataFlavor(mimeType2);
		}catch (ClassNotFoundException cnfe){
			//if this happens then hell freezes over :-/
			cnfe.printStackTrace();
			return new DataFlavor[0];
			}
		return flavors;
	}
	
	@Override
	public boolean performTransfer(TransferSupport support){
		
		return false;
	}
	
}