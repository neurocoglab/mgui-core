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

package mgui.interfaces.shapes.selection;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.TransferHandler.TransferSupport;

import mgui.geometry.util.SpatialUnit;
import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSceneNode;
import mgui.interfaces.shapes.ShapeSet;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.transfers.InterfaceTransferable;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.trees.TreeListener;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;


public class ShapeSelectionSet extends AbstractInterfaceObject implements ShapeSet,
																		  XMLObject,
																		  PopupMenuObject,
																		  InterfaceTransferable,
																		  AttributeObject{

	public TreeSet<InterfaceShape> shapes = new TreeSet<InterfaceShape>();
	public TreeSet<ShapeSet> subsets = new TreeSet<ShapeSet>();
	private ShapeIDComp selComp = new ShapeIDComp();
	//public String name;
	public InterfaceShape shapeAdded;
	public InterfaceTreeNode treeNode;
	public ArrayList<ShapeSelectionListener> selection_listeners = new ArrayList<ShapeSelectionListener>();
	public InterfaceDisplayPanel displayPanel;
	private boolean isDestroyed = false;
	public ShapeModel3D model;
	protected AttributeList attributes;
	
	public ShapeSelectionSet(){
		this("No name");
	}
	
	public ShapeSelectionSet(String name){
		init();
		setName(name);
	}
	
	public ShapeSelectionSet(ShapeSelectionSet set){
		init();
		setFromSelectionSet(set);
	}
	
	private void init(){
		attributes = new AttributeList();
		attributes.add(new Attribute("Name", "No name"));
	}
	
	public boolean isSelectable(){
		return false;
	}
	
	public void setSourceURL(String url){}
	public String getSourceURL(){return null;}
	
	public Icon getObjectIcon(){
		return null;
	}
	
	/************************************************
	 * Sets this set's selection to those in <code>set</code>.
	 * 
	 * @param set
	 */
	public void setFromSelectionSet(ShapeSelectionSet set){
		setFromSelectionSet(set, true);
	}
	
	/************************************************
	 * Sets this set's selection to those in <code>set</code>.
	 * 
	 * @param set
	 */
	public void setFromSelectionSet(ShapeSelectionSet set, boolean update){
		
		addShapes(set.getMembers());
		setName(set.getName());
		setModel(set.getModel());
		
	}
	
	/****************************************
	 * Returns the index of <code>shape</code> in this set.
	 * 
	 * @return index of shape, or -1 if it is not in this set
	 */
	@Override
	public int getIndexOf(InterfaceShape shape){
		ArrayList<InterfaceShape> sets = new ArrayList<InterfaceShape>(shapes);
		for (int i = 0; i < sets.size(); i++)
			if (sets.get(i).equals(shape)) return i;
		return -1;
	}
	
	/*******************************************
	 * Returns a copy of this set's member list.
	 * 
	 * @return a list of members
	 */
	@Override
	public ArrayList<InterfaceShape> getMembers(){
		return new ArrayList<InterfaceShape>(shapes);
	}
	
	public ShapeModel3D getModel(){
		return model;
	}
	
	public boolean isVisible(){
		return false;
	}
	
	public boolean show2D(){
		return false;
	}
	
	public boolean show3D(){
		return false;
	}
	
	public void setModel(ShapeModel3D m){
		model = m;
	}
	
	public ShapeSet3DInt asShapeSet3D(){
		ShapeSet3DInt set = new ShapeSet3DInt();
		ArrayList<InterfaceShape> list = new ArrayList<InterfaceShape>(this.shapes);
		
		for (int i = 0; i < list.size(); i++)
			if (list.get(i) instanceof Shape3DInt)
				set.addShape(list.get(i));
		
		return set;
	}
	
	public void shapeUpdated(ShapeEvent e){
		
	}
	
	public ShapeSet2DInt asShapeSet2D(){
		ShapeSet2DInt set = new ShapeSet2DInt();
		ArrayList<InterfaceShape> list = new ArrayList<InterfaceShape>(this.shapes);
		
		for (int i = 0; i < list.size(); i++)
			if (list.get(i) instanceof Shape2DInt)
				set.addShape(list.get(i));
		
		return set;
	}
	
	public int getSize(){
		return shapes.size();
	}
	
	public void setOverride(AttributeList attr){
		
	}
	
	public void unsetOverride(){
		
	}
	
	public Set<InterfaceShape> getShapeSet() {
		return shapes;
	}
	
	public boolean addShape(InterfaceShape s){
		return addShape(s, true);
	}
	
	public Set<ShapeSet> getSubSets(){
		return subsets;
	}
	
	public ShapeSelectionSet getUnion(ShapeSet set, boolean recursive){
		ShapeSelectionSet union = new ShapeSelectionSet();
		union.addUnion(this, recursive);
		union.addUnion(set, recursive);
		return union;
	}
	
	public void addUnion(ShapeSet set, boolean recursive){
		shapes.addAll(set.getShapeSet());
		//add the contents of all subsets recursively if requested
		if (recursive){
			Set<ShapeSet> subsets = set.getSubSets();
			Iterator<ShapeSet> itr = subsets.iterator();
			while (itr.hasNext())
				addUnion(itr.next(), true);
			}
	}
	
	public boolean needsRedraw(Attribute a){
		return false;
	}
	
	//add shape to set
	public boolean addShape(InterfaceShape s, boolean update){
		//is it already in set?
		if (shapes.contains(s)) return false;
		
		//if not, add it sorted
		shapes.add(s);
		if (update){
			shapeAdded = s;
			setTreeNode();
			fireSelectionListeners(new ShapeSelectionEvent(this, ShapeSelectionEvent.EventType.ShapeAdded));
			}
		
		return true;
	}
	
	public void addShapes(TreeSet<InterfaceShape> shapes){
		this.shapes.addAll(shapes);
	}
	
	public void addShapes(ArrayList<InterfaceShape> shapes){
		this.shapes.addAll(shapes);
		setTreeNode();
		fireSelectionListeners(new ShapeSelectionEvent(this, ShapeSelectionEvent.EventType.ShapeAdded));
	}
	
	public void addShapes(ShapeSelectionSet set){
		addShapes(set.shapes);
	}
	
	public void addSet(ShapeSelectionSet set){
		subsets.add(set);
		setTreeNode();
		fireSelectionListeners(new ShapeSelectionEvent(this, ShapeSelectionEvent.EventType.ShapeAdded));
	}
	
	public void removeSet(ShapeSelectionSet set){
		subsets.remove(set);
		setTreeNode();
		fireSelectionListeners(new ShapeSelectionEvent(this, ShapeSelectionEvent.EventType.ShapeRemoved));
	}
	
	public void removeShape(InterfaceShape s){
		if (!shapes.remove(s)) return;
		setTreeNode();
		fireSelectionListeners(new ShapeSelectionEvent(this, ShapeSelectionEvent.EventType.ShapeRemoved));
	}

	/*************
	 * Returns a section set which is sectSet, filtered by this selection set
	 * @param sectSet
	 * @return SectionSet3DInt filtered by this selection set
	 */
	public SectionSet3DInt getFilteredSectionSet(SectionSet3DInt sectSet){
		SectionSet3DInt retSet = sectSet.getInitSectionSet();
		//retSet.setFromSectionSet(sectSet);
		//retSet.setRefPlane(sectSet.getRefPlane());
		//retSet.setSpacing(sectSet.getSpacing());
		ShapeSet2DInt thisSet;
		
		Iterator<Integer> itr = sectSet.sections.keySet().iterator();
		while (itr.hasNext()){
			Integer s = itr.next();
			thisSet = sectSet.getShapeSet(s.intValue());
			thisSet = getFilteredShapeSet2D(thisSet);
			if (thisSet.members.size() > 0)
				retSet.sections.put(s, thisSet);
			}
		
		retSet.updateShape();
		return retSet;
	}
	
	
	/**********
	 * Return a shape3D set which is thisSet, filtered by this selection set.
	 * @param thisSet
	 * @return filtered ShapeSet3DInt
	 */
	public ShapeSet3DInt getFilteredShapeSet3D(ShapeSet3DInt thisSet){
		ShapeSet3DInt retSet = new ShapeSet3DInt();
		
		for (int i = 0; i < thisSet.members.size(); i++){
			//if (SectionSet3DInt.class.isInstance(thisSet.getMember(i)))
			if (thisSet.getMember(i) instanceof SectionSet3DInt)
				retSet.addShape(getFilteredSectionSet((SectionSet3DInt)thisSet.getMember(i)), true, false);
			else if (hasShape(thisSet.getMember(i)))
				retSet.addShape(thisSet.getMember(i), true, false);
		}
		
		retSet.updateShape();
		return retSet;
	}
	
	
	/**********
	 * Return a shape2D set which is thisSet, filtered by this selection set.
	 * @param thisSet
	 * @return filtered ShapeSet2DInt
	 */
	public ShapeSet2DInt getFilteredShapeSet2D(ShapeSet2DInt thisSet){
		
		ShapeSet2DInt retSet = new ShapeSet2DInt();
		
		for (int i = 0; i < thisSet.members.size(); i++)
			if (hasShape(thisSet.getMember(i)))
				retSet.addShape(thisSet.getMember(i), true, false);
		
		retSet.updateShape();
		return retSet;
		
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	/************************************
	 * Returns <code>true</code> if <code>shape</code> is in this selection set.
	 * 
	 * TODO: implement to search member shape and selection sets
	 */
	@Override
	public boolean hasShape(InterfaceShape shape){
		if (shapes.contains(shape)) return true;
		Iterator<ShapeSet> itr = subsets.iterator();
		while (itr.hasNext())
			if (itr.next().hasShape(shape)) return true;
			
		return false;
	}
	
	/************************************
	 * Current returns the same as {@link hasShape(InterfaceShape s)}
	 * 
	 * TODO: implement to search member shape and selection sets
	 */
	@Override
	public boolean hasShape(InterfaceShape shape, boolean recurse){
		return hasShape(shape);
	}
	
	public void resetTreeNode(){
		treeNode = null;
		setTreeNode();
	}
	
	public void setTreeNode(TreeListener l){
		boolean add = (treeNode == null);
		setTreeNode();
		if (add) treeNode.addListener(l);
	}
	
	public void setTreeNode(){
		if (treeNode == null)
			treeNode = new InterfaceTreeNode(this);
		
		treeNode.removeAllChildren();
		sort();
		
		//add shapes
		//for (int i = 0; i < shapes.size(); i++)
		//	treeNode.add((InterfaceShapeNode)shapes.get(i).getTreeNodeCopy());
		Iterator itr = shapes.iterator();
		while (itr.hasNext())
			treeNode.add(new InterfaceTreeNode((InterfaceShape)itr.next()));
		
	}
	
	@Override
	public InterfaceTreeNode issueTreeNode(){
		//if (treeNode == null) setTreeNode();
		//return treeNode;
		InterfaceTreeNode treeNode = new InterfaceTreeNode(this);
		sort();
		Iterator itr = shapes.iterator();
		while (itr.hasNext())
			treeNode.add(new InterfaceTreeNode((InterfaceShape)itr.next()));
		return treeNode;
	}
	
	public void fireSelectionListeners(ShapeSelectionEvent e){
		for (int i = 0; i < selection_listeners.size(); i++)
			selection_listeners.get(i).shapeSelectionChanged(e);
	}
	
	public void addSelectionListener(ShapeSelectionListener s){
		selection_listeners.add(s);
	}
	
	public void removeSelectionListener(ShapeSelectionListener s){
		selection_listeners.remove(s);
	}
	
	class ShapeIDComp implements java.util.Comparator<InterfaceShape>{
		public int compare(InterfaceShape s1, InterfaceShape s2){
			if (s1.getID() > s2.getID()) return 1;
			if (s1.getID() == s2.getID()) return 0;
			return -1;
		}
	}

	public Iterator<InterfaceShape> getIterator(){
		return shapes.iterator();
	}
	
	public void sort(){
		//Collections.sort(shapes, selComp);
	}

	public ShapeSceneNode getShapeSceneNode(){ return null; }
	
	@Override
	public void destroy(){
		isDestroyed = true;
		fireShapeDestroyed();
	}
	@Override
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	protected void fireShapeDestroyed(){
		ShapeSelectionEvent e = new ShapeSelectionEvent(this, ShapeSelectionEvent.EventType.SetDestroyed);
		fireSelectionListeners(e);
	}
	
	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXMLSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleXMLElementEnd(String localName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		//TODO: implement this with short XML
		
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
	}
	
	/*********************
	 * Not implemented.
	 */
	@Override
	public InterfaceShape getLastAdded(){
		return null;
	}
	
	/*********************
	 * Not implemented.
	 */
	@Override
	public InterfaceShape getLastRemoved(){
		return null;
	}
	
	/*********************
	 * Not implemented.
	 */
	@Override
	public InterfaceShape getLastModified(){
		return null;
	}
	
	/*********************
	 * Not implemented.
	 */
	@Override
	public InterfaceShape getLastInserted(){
		return null;
	}
	
	/*********************
	 * Not implemented.
	 */
	@Override
	public int getLastInsert(){
		return -1;
	}
	
	public InterfacePopupMenu getPopupMenu() {
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("ShapeSelectionSet Menu Item 1"));
		menu.addMenuItem(new JMenuItem("ShapeSelectionSet Menu Item 2"));
		
		return menu;
	}

	public void handlePopupEvent(ActionEvent e) {
		
	}

	public void showPopupMenu(MouseEvent e) {
		InterfacePopupMenu menu = getPopupMenu();
		if (menu == null) return;
		menu.show(e);
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean performTransfer(TransferSupport support){
		
		return false;
	}
	
	@Override
	public SpatialUnit getUnit() {
		return model.getDefaultUnit();
	}

	@Override
	public boolean isAncestorSet(ShapeSet set) {
		return false;
	}

	@Override
	public boolean moveShapeBefore(InterfaceShape shape, InterfaceShape target) {
		
		return false;
	}

	@Override
	public void setUnit(SpatialUnit unit) {
		
		
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	@Override
	public String getFullName(){
		return getName();
	}
	
	@Override
	public Attribute<?> getAttribute(String attrName) {
		return attributes.getAttribute(attrName);
	}
	
	@Override
	public Object getAttributeValue(String name) {
		Attribute<?> attribute = getAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
	}

	@Override
	public AttributeList getAttributes() {
		return attributes;
	}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	@Override
	public void setAttributes(AttributeList thisList) {
		this.attributes = thisList;
	}

	
}