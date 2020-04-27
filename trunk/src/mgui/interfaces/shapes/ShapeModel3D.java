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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Locale;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.VirtualUniverse;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.MenuElement;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import mgui.geometry.util.SpatialUnit;
import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.DisplayPanelEvent;
import mgui.interfaces.DisplayPanelListener;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphics.GraphicEvent;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.graphics.InterfaceGraphic3D;
import mgui.interfaces.graphics.InterfaceGraphicListener;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.graphics.WindowEvent;
import mgui.interfaces.graphics.WindowListener;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.shapes.ShapeModelEvent.EventType;
import mgui.interfaces.shapes.selection.ShapeSelectionEvent;
import mgui.interfaces.shapes.selection.ShapeSelectionListener;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.trees.ShapeModel3DTreeNode;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.transfers.InterfaceTransferable;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.trees.util.TreeKeyHandler;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;

/*****************************************************************
 * The top container for all shape interfaces.  
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeModel3D extends AbstractInterfaceObject implements ShapeListener, 
																	 InterfaceObject,
																	 WindowListener,
																	 InterfaceGraphicListener,
																	 XMLObject,
																	 ShapeSelectionListener,
																	 DisplayPanelListener,
																	 IconObject,
																	 PopupMenuObject,
																	 TreeKeyHandler,
																	 InterfaceTransferable{

	protected VirtualUniverse universe;
	protected Locale locale;
	protected Shape3DSceneNode model_node;
	protected BranchGroup sections_node;
	protected HashMap<InterfaceGraphic2D, Polygon3DInt> section_polygons = new HashMap<InterfaceGraphic2D, Polygon3DInt>();
	protected ShapeSet3DInt modelSet;
	protected ArrayList<ShapeListener> shape_listeners = new ArrayList<ShapeListener>();
	protected ArrayList<ShapeModel3DListener> model_listeners = new ArrayList<ShapeModel3DListener>();
	public ArrayList<ShapeSelectionSet> selections = new ArrayList<ShapeSelectionSet>();
	protected ArrayList<ShapeSelectionListener> slisteners = new ArrayList<ShapeSelectionListener>();
	protected ShapeSelectionSet exclusionFilter;
	protected boolean excludeToSelection;
	protected DefaultMutableTreeNode selectionSetNode = new DefaultMutableTreeNode("Selection Sets");
	protected InterfaceDisplayPanel displayPanel; 
	protected BranchGroup temp_shapes;
	protected boolean is_live3d = false;
	
	protected SpatialUnit default_unit;
	protected ShapeSelectionSet working_selection;
	
	
	public ShapeModel3D(){
		this("No name", new ShapeSet3DInt("Base Shape Set"));
	}
	
	public ShapeModel3D(String name){
		this(name, new ShapeSet3DInt("Base Shape Set"));
	}
	
	public ShapeModel3D(String name, ShapeSet3DInt set){
		this(name, set, InterfaceEnvironment.getSpatialUnit("meter"));
	} 
	
	public ShapeModel3D(String name, ShapeSet3DInt set, SpatialUnit default_unit){
		setName(name);
		init();
		setModelSet(set);
		
	} 
	
	private void init(){
		universe = new VirtualUniverse();
		locale = new Locale(universe);
		temp_shapes = new BranchGroup();
		temp_shapes.setCapability(BranchGroup.ALLOW_DETACH);
		temp_shapes.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		temp_shapes.setCapability(Group.ALLOW_CHILDREN_READ);
		temp_shapes.setCapability(Group.ALLOW_CHILDREN_WRITE);
		locale.addBranchGraph(temp_shapes);
		
	}
	
	/**********************************
	 * Indicates whether this model has a live 3D node
	 * 
	 * @return
	 */
	public boolean isLive3D(){
		return this.is_live3d;
	}
	
	public void setName(String name){
		super.setName(name);
		this.fireModelListeners(new ShapeModelEvent(this, EventType.NameChanged));
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/shape_model_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/shape_model_20.png");
		return null;
	}
	
	/**************************************************
	 * Returns the default spatial unit for this shape model
	 * 
	 * @return
	 */
	public SpatialUnit getDefaultUnit(){
		return default_unit;
	}
	
	/**************************************************
	 * Returns the working selection set for this model. 
	 * 
	 * @return
	 */
	public ShapeSelectionSet getWorkingSelection(){
		if (working_selection == null){
			working_selection = new ShapeSelectionSet(getName() + ".working");
			working_selection.setModel(this);
			}
		return working_selection;
	}
	
	public void clearWorkingSelection(){
		working_selection = null;
		
	}
	
	/**************************************************
	 * Sets the default spatial unit for this model.
	 * 
	 * @param name
	 * @return
	 */
	public boolean setDefaultUnit(String name){
		SpatialUnit unit = InterfaceEnvironment.getSpatialUnit(name);
		if (unit == null) return false;
		this.default_unit = unit;
		return true;
	}
	
	public void setDisplayPanel(InterfaceDisplayPanel panel){
		if (displayPanel != null)
			displayPanel.removeDisplayPanelListener(this);
		displayPanel = panel;
		if (displayPanel != null)
			displayPanel.addDisplayPanelListener(this);
	}
	
	public InterfaceDisplayPanel getDisplayPanel(){
		return displayPanel;
	}
	
	public void displayPanelChanged(DisplayPanelEvent e){
		
		InterfaceGraphic<?> panel;
		
		switch (e.type){
		
			case WindowAdded:
				panel = e.getDisplayPanel().getLastAddedPanel();
				if (panel == null || !(panel instanceof InterfaceGraphic2D)) return;
				modelSet.addWindow((InterfaceGraphic2D)panel);
				//this.modelSet.setSectionNode((InterfaceGraphic2D)panel);
				return;
			
			case WindowRemoved:
				panel = e.getDisplayPanel().getLastRemovedPanel();
				if (panel == null || !(panel instanceof InterfaceGraphic2D)) return;
				modelSet.removeWindow((InterfaceGraphic2D)panel);
				//this.modelSet.setSectionNode((InterfaceGraphic2D)panel);
				return;
				
			}
		
	}
	
	public void addTempShape(BranchGroup shape){
		if (shape.getParent() != null)
			shape.detach();
		temp_shapes.addChild(shape);
	}
	
	public void removeTempShape(BranchGroup shape){
		Iterator<Node> e =	temp_shapes.getAllChildren();
		while (e.hasNext()){
			Object o = e.next();
			if (o instanceof BranchGroup && o.equals(shape))
				shape.detach();
			}
	}
	
	public void clearTempShapes(){
		temp_shapes.removeAllChildren();
	}
	
	public void shapeSelectionChanged(ShapeSelectionEvent e){
		
	}
	
	public void setModelSet(ShapeSet3DInt set){
		if (modelSet != null){
			// Remove 2D windows from current set
			modelSet.clearWindows();
			
			// Remove camera listeners for all InterfaceGraphic3D windows
			for (int i = 0; i < shape_listeners.size(); i++)
				if (shape_listeners.get(i) instanceof InterfaceGraphic3D)
					modelSet.deregisterCamera(((InterfaceGraphic3D)shape_listeners.get(i)).getCamera());
			// Unset the model for the previous Shape Set
			modelSet.setModel(null);
			}
		
		// Set this as the Shape Set's Shape Model
		set.setModel(this);
		
		// Set the Shape Set for this Shape Model
		modelSet = set;
		
		// Register the set with the current Session; if it is not already
		set.register();
		
		// Set camera listeners for all InterfaceGraphic3D windows
		for (int i = 0; i < shape_listeners.size(); i++)
			if (shape_listeners.get(i) instanceof InterfaceGraphic3D)
				modelSet.registerCamera(((InterfaceGraphic3D)shape_listeners.get(i)).getCamera());
		
		// Add 2D windows
		if (getDisplayPanel() != null){
			ArrayList<InterfaceGraphicWindow> windows = getDisplayPanel().getWindows();
			for (int i = 0; i < windows.size(); i++){
				if (windows.get(i).getPanel() instanceof InterfaceGraphic2D){
					InterfaceGraphic2D window = (InterfaceGraphic2D)windows.get(i).getPanel();
					modelSet.addWindow(window);
					}
				}
			}
		
		refreshModel();
		updateTreeNodes();
	}
	
	@Override
	public void showPopupMenu(MouseEvent e) {
		InterfacePopupMenu menu = getPopupMenu();
		if (menu == null) return;
		menu.show(e);
	}

	@Override
	public InterfacePopupMenu getPopupMenu() {
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		
		menu.addMenuItem(new JMenuItem("ShapeModel3D", getObjectIcon()));
		
		menu.add(new JSeparator(), 1);
		menu.add(new JSeparator(), 1);
		
		menu.addMenuItem(new JMenuItem("Rename.."));
		menu.addMenuItem(new JMenuItem("Delete"));
		
		int s = 5;
		ShapeModel3D c_model = InterfaceSession.getWorkspace().getDisplayPanel().getCurrentShapeModel();
		if (this != c_model){
			menu.addMenuItem(new JMenuItem("Set current"));
			s++;
			}
		
		if (modelSet != null){
			InterfacePopupMenu menu2 = modelSet.getPopupMenu();
			
			MenuElement[] elements = menu2.getSubElements();
			for (int i = 0; i < elements.length; i++){
				if (i == 0){
					menu.add(new JSeparator(), s);
					menu.add(new JSeparator(), s);
					}
				if (i == 8){
					menu.add(new JSeparator(), s+12);
					menu.add(new JSeparator(), s+12);
					}
				if (elements[i] instanceof JMenuItem){
					JMenuItem item = (JMenuItem)elements[i];
					item.removeActionListener(menu2);
					menu.addMenuItem(item);
					}
				if (i == 0){
					menu.add(new JSeparator(), s+3);
					menu.add(new JSeparator(), s+3);
					}
				if (i == 8){
					menu.add(new JSeparator(), s+15);
					menu.add(new JSeparator(), s+15);
					}
				}
			
			}
		
		return menu;
	}
	
	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getText().equals("Rename..")){
			String name = JOptionPane.showInputDialog("Enter name for model:", getName());
			if (name == null) return;
			this.setName(name);
			this.updateTreeNodes();
			return;
			}
		
		if (item.getText().equals("Delete")){
			if (JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
											  "Really delete shape model '" + getName() + "'?", 
											  "Delete Shape Model", 
											  JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return;
			InterfaceSession.getWorkspace().removeShapeModel(this);
			this.destroy();
			return;
			}
		
		if (item.getText().equals("Set current")){
			InterfaceSession.getWorkspace().getDisplayPanel().setCurrentShapeModel(this);
			return;
			}
		
		if (modelSet != null){
			modelSet.handlePopupEvent(e);
			}
		
	}
	
	@Override
	public void handleTreeKeyEvent(KeyEvent event) {
		
		if (event.getKeyCode() == KeyEvent.VK_DELETE){
			if (JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
					  "Really delete shape model '" + getName() + "'?", 
					  "Delete Shape Model", 
					  JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
				return;
			
			InterfaceSession.getWorkspace().removeShapeModel(this);
			this.destroy();
			return;
			
			}
		
	}
	
	public long getNextID(){
		return InterfaceSession.getUID();
	}
	
	/************************
	 * Updates this model by setting its shape scene node. Filters the model shape set
	 * if <code>excludeToSelection</code> is true. 
	 *
	 */
	public void refreshModel(){
		if (modelSet == null || locale == null || !is_live3d) return;
		
		// Detach existing Shape Set node, if applicable
		if (model_node != null)
			locale.removeBranchGraph(model_node);
		
		// Detach existing Sections node, if applicable
		if (sections_node != null)
			locale.removeBranchGraph(sections_node);
		
		// Set and attach new Shape Set node
		if (excludeToSelection)
			modelSet.setScene3DObject(true, exclusionFilter);
		else
			modelSet.setScene3DObject(true);
		
		modelSet.setShapeSceneNode();
		model_node = (Shape3DSceneNode)modelSet.getShapeSceneNode();
		model_node.detach();
		locale.addBranchGraph(model_node);
		
		// Set and attach new Sections node
		setSectionsNode();
		
	}
	
	/**************************************************
	 * Sets the {@code BranchGroup} which visualizes the 2D windows which have section sets
	 * as their source which are members of this model.
	 * 
	 */
	protected void setSectionsNode(){
		
		if (!this.is_live3d) return;
		
		if (sections_node == null){
			sections_node = new BranchGroup();
			sections_node.setCapability(BranchGroup.ALLOW_DETACH);
			sections_node.setCapability(Group.ALLOW_CHILDREN_WRITE);
			sections_node.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			sections_node.setCapability(Group.ALLOW_CHILDREN_READ);
		}else{
			sections_node.removeAllChildren();
			}
		
		// 1. List of windows for all member section sets
		InterfaceDisplayPanel display_panel = InterfaceSession.getWorkspace().getDisplayPanel();
		if (display_panel == null) return;
		ArrayList<InterfaceGraphicWindow> windows = display_panel.getAllWindows();
		
		// 1.1. Get only those windows with sources which are member section sets
		for (int i = 0; i < windows.size(); i++){
			InterfaceGraphicWindow window = windows.get(i);
			if (window.getPanel() instanceof InterfaceGraphic2D){
				SectionSet3DInt section_set = (SectionSet3DInt)window.getPanel().getSource();
				if (section_set != null && section_set.getModel() == this){
					// Passes, get polygon
					InterfaceGraphic2D window2d = (InterfaceGraphic2D)window.getPanel();
					if (!modelSet.addWindow(window2d))
						modelSet.setSectionNode(window2d);
					addShapeListener(window2d);
					}
				}
			}
		
	}
	
	
	public Locale getLocale(){
		return locale;
	}
	
	public void setExcludeToSelection(boolean b){
		excludeToSelection = b;
		refreshModel();
	}
	
	public boolean getExcludeToSelection(){
		return excludeToSelection;
	}
	
	public void setExclusionFilter(String selStr){
		setExclusionFilter(getSelectionSet(selStr));
	}
	
	public void setExclusionFilter(ShapeSelectionSet selSet){
		setExclusionFilter(selSet, true);
	}
	
	public void setExclusionFilter(ShapeSelectionSet selSet, boolean update){
		if (selSet == null){
			exclusionFilter = null;
			setExcludeToSelection(false);
			return;
		}
		if (!hasSelectionSet(selSet)) return;
		exclusionFilter = selSet;
		if (update && excludeToSelection)
			refreshModel();
	}
	
	public ShapeSelectionSet getExclusionFilter(){
		return exclusionFilter;
	}
	
	public boolean hasSelectionSet(ShapeSelectionSet selSet){
		for (int i = 0; i < selections.size(); i++)
			if (selections.get(i).getName().equals(selSet.getName())) return true;
		return false;
	}
	
	/*****************************************
	 * Returns a list of this model's selection sets, including its working set.
	 * 
	 * @return
	 */
	public ArrayList<ShapeSelectionSet> getSelectionSets(){
		ArrayList<ShapeSelectionSet> sets = new ArrayList<ShapeSelectionSet>();
		sets.add(getWorkingSelection());
		sets.addAll(selections);
		return sets;
	}
	
	public ShapeSelectionSet getSelectionSet(String name){
		for (int i = 0; i < selections.size(); i++)
			if (selections.get(i).getName().equals(name)) return selections.get(i);
		return null;
	}
	
	public ArrayList<ShapeSelectionSet> getSelectionSets(InterfaceShape thisShape){
		ArrayList<ShapeSelectionSet> retSet = new ArrayList<ShapeSelectionSet>();
		//search all selection sets and return those containing thisShape
		for (int i = 0; i < selections.size(); i++)
			if (selections.get(i).hasShape(thisShape))
				retSet.add(selections.get(i));
		return retSet;
	}
	
	public ShapeSet3DInt getModelSet(){
		return modelSet;
	}
	
	public void addGraphics3D(InterfaceGraphic3D g){
		
		if (!this.isLive3D()){
			is_live3d = true;
			refreshModel();
			}
		
		locale.addBranchGraph(g.viewingPlatform);
		addShapeListener(g);
		addModelListener(g);
		if (modelSet != null)
			modelSet.registerCamera(g.getCamera());
	}
	
	public void removeGraphics3D(InterfaceGraphic3D g){	
		locale.removeBranchGraph(g.viewingPlatform);
		removeShapeListener(g);
		removeModelListener(g);
		if (modelSet != null)
			modelSet.deregisterCamera(g.getCamera());
		
	}
	
	public BranchGroup getModel(){
		return model_node;
	}
	
	public InterfaceTreeNode issueTreeNode(){
		ShapeModel3DTreeNode treeNode = new ShapeModel3DTreeNode(this);
		setTreeNode(treeNode);
		tree_nodes.add(treeNode);
		return treeNode;
	}
	
	public void setTreeNode(){
		//treeNode = new InterfaceTreeNode(this);
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		if (modelSet == null) return;
		InterfaceTreeNode set_root = modelSet.issueTreeNode();
		//treeNode.addChild(set_root);
		
		treeNode.add(getSelectionSetNode());
		ArrayList<TreeNode> children = set_root.getChildren();
		for (int i = 0; i < children.size(); i++)
			treeNode.addChild((InterfaceTreeNode)children.get(i));
		
		set_root.removeAllChildren();
		set_root.destroy(false);
	}

	public boolean isDataFlavorSupported(DataFlavor flavor) {
		if (modelSet != null)
			return modelSet.isDataFlavorSupported(flavor);
		return false;
	}
	
	public DataFlavor[] getTransferDataFlavors(){
		if (modelSet != null)
			return modelSet.getTransferDataFlavors();
		return new DataFlavor[]{};
	}
	
	@Override
	public boolean performTransfer(TransferSupport support){
		if (modelSet != null)
			return modelSet.performTransfer(support);
		return false;
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (modelSet != null)
			return modelSet.getTransferData(flavor);
		return null;
	}
	
	protected void cleanTreeNodes(){
		for (int i = 0; i < tree_nodes.size(); i++)
			if (tree_nodes.get(i).isDestroyed())
				tree_nodes.remove(tree_nodes.get(i--));
	}
	
	protected DefaultMutableTreeNode getSelectionSetNode(){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Selection Sets");
		for (int i = 0; i < selections.size(); i++){
			node.add(selections.get(i).issueTreeNode());
			}
		return node;
	}
	
	protected void fireModelListeners(ShapeModelEvent e){
		ArrayList<ShapeModel3DListener> temp = new ArrayList<ShapeModel3DListener>(model_listeners);
		for (int i = 0; i < temp.size(); i++)
			temp.get(i).shapeModelChanged(e);
	}
	
	protected void fireShapeListeners(ShapeEvent e){
		if (modelSet == null) return;
		ArrayList<ShapeListener> temp = new ArrayList<ShapeListener>(shape_listeners);
		for (int i = 0; i < temp.size(); i++)
			temp.get(i).shapeUpdated(e);
		
		
		//fire tree node listeners; remove any which are destroyed
		ArrayList<InterfaceTreeNode> temp2 = new ArrayList<InterfaceTreeNode>(tree_nodes);
		for (int i = 0; i < temp2.size(); i++)
			if (temp2.get(i).isDestroyed())
				tree_nodes.remove(temp2.get(i));
			else if (modelSet != null && e.getSource() == modelSet)
				((ShapeModel3DTreeNode)temp2.get(i)).shapeUpdated(e);
		
	}
	
	protected void fireSelectionListeners(ShapeSelectionEvent e){
		if (modelSet == null) return;
		for (int i = 0; i < slisteners.size(); i++)
			slisteners.get(i).shapeSelectionChanged(e);
	}
	
	public void addShapeListener(ShapeListener s){
		for (int i = 0; i < shape_listeners.size(); i++)
			if (shape_listeners.get(i).equals(s)) return;
		shape_listeners.add(s);
	}
	
	public void removeShapeListener(ShapeListener s){
		shape_listeners.remove(s);
	}
	
	public void addModelListener(ShapeModel3DListener s){
		for (int i = 0; i < model_listeners.size(); i++)
			if (model_listeners.get(i).equals(s)) return;
		model_listeners.add(s);
	}
	
	public void removeModelListener(ShapeModel3DListener s){
		model_listeners.remove(s);
	}
	
	public void addSelectionListener(ShapeSelectionListener s){
		slisteners.add(s);
	}
	
	public void removeSelectionListener(ShapeSelectionListener s){
		shape_listeners.remove(s);
	}
	
	public void shapeUpdated(ShapeEvent e){
		
		Shape3DInt shape;
		switch (e.eventType){
			case AttributeModified:
			case ShapeMoved:
				fireShapeListeners(e);
				return;
				
			case ShapeModified:
				fireShapeListeners(e);
				if (e.getShape() == this.modelSet){
					refreshModel();
					//updateTreeNodes();
					}
				return;
				
			case ShapeAdded:
				//add new camera listener to all cameras
				if (e.getSource() instanceof Shape3DInt){
					shape = (Shape3DInt)e.getSource();
					if (shape == null) return;
					shape.setID(InterfaceSession.getUID());
					if (shape.hasCameraListener())
						for (int i = 0; i < shape_listeners.size(); i++)
							if (shape_listeners.get(i) instanceof InterfaceGraphic3D)
								shape.registerCameraListener(((InterfaceGraphic3D)shape_listeners.get(i)).getCamera());
					fireShapeListeners(e);
					
					//workaround for Texture3D rendering issues
					if (shape instanceof Volume3DInt && InterfaceSession.isInit())
						InterfaceSession.getDisplayPanel().updatePanels();
					
					return;
					}
				
			case ShapeRemoved:
				//remove listener from all cameras
				if (e.getSource() instanceof Shape3DInt){
					shape = (Shape3DInt)e.getSource();
					if (shape == null) return;
					if (shape.hasCameraListener)
						for (int i = 0; i < shape_listeners.size(); i++)
							if (shape_listeners.get(i) instanceof InterfaceGraphic3D)
								shape.deregisterCameraListener(((InterfaceGraphic3D)shape_listeners.get(i)).getCamera());
					fireShapeListeners(e);
					return;
					}
				
			case ShapeSetModified:
				fireShapeListeners(e);
				return;
				
//			case VertexColumnChanged:
//				fireShapeListeners(e);
//				return;
			}
		
	}
	
	public void addSelectionSet(ShapeSelectionSet selSet){
		selections.add(selSet);
		selSet.setModel(this);
		selSet.addSelectionListener(this);
		selSet.sort();
		fireSelectionListeners(new ShapeSelectionEvent(selSet, ShapeSelectionEvent.EventType.ShapeAdded));
	}
	
	public void setSelectionSets(ArrayList<ShapeSelectionSet> sets){
		//remove existing selection sets and add new ones
		for (int i = 0; i < selections.size(); i++)
			selections.get(i).removeSelectionListener(this);
		
		selections = new ArrayList<ShapeSelectionSet>(sets.size());
		
		for (int i = 0; i < sets.size(); i++){
			selections.add(sets.get(i));
			sets.get(i).setModel(this);
			sets.get(i).addSelectionListener(this);
			sets.get(i).sort();
			}
		
		fireSelectionListeners(new ShapeSelectionEvent(sets.get(0)));
	}
	
	public void removeSelectionSet(ShapeSelectionSet selSet){
		selections.remove(selSet);
		selSet.removeSelectionListener(this);
		selSet.destroy();
		fireSelectionListeners(new ShapeSelectionEvent(selSet, ShapeSelectionEvent.EventType.SetDestroyed));
	}
	
	@Override
	public void graphicSourceChanged(GraphicEvent e){
		
		
		
	}
	
	@Override
	public void graphicUpdated(GraphicEvent e){
		
		InterfaceGraphic<?> panel = e.getGraphic();
		
		if (panel instanceof InterfaceGraphic2D){
			InterfaceGraphic2D g2d = (InterfaceGraphic2D)panel;
			switch (e.getType()){
				case Modified:
					if (modelSet == null) return;
					//window is updated, so reposition its section3D, if visible
					modelSet.setSectionNode(g2d);
					
					return;
					
				case NewSource:
					//if new source is not a member of this model, destroy old section node
					if (modelSet == null) return;
					
					SectionSet3DInt set = g2d.getCurrentSectionSet();
					if (!set.getModel().equals(this)){
						modelSet.removeWindow(g2d);
						return;
						}
					if (!modelSet.hasWindow(g2d)){
						modelSet.addWindow(g2d);
					}else{
						// Create/update section node if window is already known to set
						modelSet.setSectionNode(g2d);
						}
					addShapeListener(g2d);
					
					return;
			
				case Destroyed:
					//if this window has been destroyed, destroy its section node
					if (modelSet == null) return;
					this.removeShapeListener(g2d);
					g2d.removeGraphicListener(this);
					modelSet.destroySectionNode(g2d);
					return;
					
				default:
					return;
				}
			
			
			
			}
		
	}
	
	public void windowUpdated(WindowEvent e) {
		//if Window2D, update model with section
		
		
		
		
		
	}
	
	@Override
	public void destroy(){
		isDestroyed = true;
		if (modelSet != null)
			modelSet.destroy();
		super.destroy();
		
		//this.fireShapeListeners(new ShapeEvent(modelSet, ShapeEvent.EventType.ModelDestroyed));
		this.fireModelListeners(new ShapeModelEvent(this, EventType.ModelDestroyed));
		locale.removeBranchGraph(temp_shapes);
		
		VirtualUniverse vu = locale.getVirtualUniverse();
		if (vu != null)
			vu.removeLocale(locale);
	}
	
	
	@Override
	public boolean isDestroyed(){
		return this.isDestroyed;
	}
	
	@Override
	public String toString(){
		return getName();
		
	}
	
	public InterfaceTreeNode getTreeNodeCopy() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getDTD() {
		
		return null;
	}

	public String getLocalName() {
		return "ShapeModel3D";
	}

	public String getXML() {
		return getXML(0);
	}

	public String getXML(int tab) {
		return getXML(tab, XMLType.Full);
	}
	
	public String getXML(int tab, XMLType type){
		
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<ShapeModel3D>\n";
		
		if (modelSet != null)
			xml = xml + modelSet.getXML(tab + 1, type);
		
		xml = xml + _tab + "</ShapeModel3D>\n";
		
		return xml;
		
	}
	
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, new XMLOutputOptions(), null);
	}
	
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		
		writer.write(_tab + "<ShapeModel3D name = '" + getName() + "' >\n");
		
		if (modelSet != null)
			modelSet.writeXML(tab + 1, writer, options, progress_bar);
		
		writer.write(_tab + "</ShapeModel3D>\n");
		
	}
	
	/******************************************************
	 * Writes this model to an XML writer, setting the specified root directory.
	 * 
	 * @param tab
	 * @param writer
	 * @param type
	 * @param progress_bar
	 * @param root_dir
	 * @throws IOException
	 */
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, 
						 ProgressUpdater progress_bar, File root_dir) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		
		writer.write(_tab + "<ShapeModel3D name = '" + getName() + "' >\n");
		
		if (modelSet != null)
			modelSet.writeXML(tab + 1, writer, options, progress_bar);
		
		writer.write(_tab + "</ShapeModel3D>\n");
		
	}
	
	public String getShortXML(int tab) {
		return XMLFunctions.getTab(tab) + "<" + getLocalName() + " />";
	}
	
	public String getXMLSchema() {
		
		return null;
	}

	public void handleXMLElementEnd(String localName) {
		
	}

	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		
	}

	public void handleXMLString(String s) {
		
	}

	@Override
	public void windowSourceChanged(WindowEvent e) {
		// TODO Auto-generated method stub
		
		
		
	}
	
}