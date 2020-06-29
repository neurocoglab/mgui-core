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

package mgui.interfaces;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryObject;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.util.InterfaceFunctions;
import mgui.resources.icons.IconObject;

/**************************************************
 * Base class for all Swing panels (i.e., extensions of <code>JPanel</code>) to be show in an instance of
 * <code>InterfaceDisplayPanel</code>. Provides empty or skeletal implementations of a number of interfaces, 
 * which can be overridden by subclasses when necessary. 
 * 
 * <p>Listens to:
 * 
 * <ul>
 * <li>Component events
 * <li>Mouse events
 * <li>Keyboard events
 * </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
@SuppressWarnings("serial")
public abstract class InterfacePanel extends JPanel implements InterfaceObject,
															   DisplayListener,
															   AttributeObject,
															   AttributeListener,
															   ComponentListener,
															   ShapeListener,
															   IconObject, 
															   PopupMenuObject,
															   MouseListener,
															   MouseMotionListener,
															   CategoryObject,
															   KeyListener
															   {

	//TODO: make most of these protected
	public AttributeList attributes = new AttributeList();
	public transient InterfaceStatusBarPanel statusBar;
	public transient ArrayList<DisplayListener> displayListeners = new ArrayList<DisplayListener>();
	public transient InterfaceTreeNode treeNode;
	public String type = "Unspecified Panel";
	
	protected transient boolean isDestroyed = false;
	protected transient ArrayList<InterfaceTreeNode> treeNodes = new ArrayList<InterfaceTreeNode>();
	protected Point last_click_point;
	
	protected void _init(){
		this.addComponentListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
	}
	
	@Override
	public void clean(){
		InterfaceFunctions.cleanInterfaceObject(this);
	}
	
	public void attributeUpdated(AttributeEvent e) {
		
	}
	
	public InterfacePopupMenu getPopupMenu(){
		return getPopupMenu(null);
	}
	
	//subclass should override this to provide a popup menu
	public InterfacePopupMenu getPopupMenu(List<Object> selection){
		//temp
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Item1"));
		menu.addMenuItem(new JMenuItem("Item2"));
		
		return menu;
	}
	
	@Override
	public void handlePopupEvent(ActionEvent e){
		// Empty; should be overridden by objects which wish to respond to popup requests
	}
	
	public void showPopupMenu(MouseEvent e){
		last_click_point = e.getPoint();
		InterfacePopupMenu menu = getPopupMenu();
		if (menu == null) return;
		menu.show(e);
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String t){
		type = t;
	}
	
	public String getTreeLabel(){
		return getName();
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfacePanel.class.getResource("/mgui/resources/icons/panel_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/panel_20.png");
		return null;
	}
	
	public void setStatusBar(InterfaceStatusBarPanel p){
		statusBar = p;
	}
	
	
	public Attribute<?> getAttribute(String attrName) {	
		return attributes.getAttribute(attrName);
	}
	
	@Override
	public Object getAttributeValue(String name) {
		Attribute<?> attribute = getAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
	}
	
	public AttributeList getAttributes() {
		return attributes;
	}
	
	public AttributeList getLocalAttributes() {
		return attributes;
	}

	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
		
	}

	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}
	
	public String getTitle(){
		return "Panel: " + getName();
	}
	
	public void fireDisplayListeners(){
		for (int i = 0; i < displayListeners.size(); i++)
			displayListeners.get(i).updateDisplay();
	}
	
	/**************************
	 * Requests this panel to update its display.
	 * 
	 */
	public void updateDisplay(){
		
	}
	public void updateDisplays(){
		fireDisplayListeners();  
	}

	public void addDisplayListener(DisplayListener thisListener){
		displayListeners.add(thisListener);
	}
	public void removeDisplayListener(DisplayListener thisListener){
		displayListeners.remove(thisListener);
	}
	
	public InterfaceTreeNode issueTreeNode(){
		InterfaceTreeNode treeNode = new InterfaceTreeNode(this);
		setTreeNode(treeNode);
		treeNodes.add(treeNode);
		return treeNode;
	}
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		
		//destroy and remove existing children
		Enumeration c = treeNode.children();
		while (c.hasMoreElements()){
			InterfaceTreeNode node = (InterfaceTreeNode)c.nextElement();
			node.destroy(!c.hasMoreElements());
			}
		
		treeNode.removeAllChildren();
		treeNode.setUserObject(this);
	}
	
	protected void updateTreeNodes(){
		ArrayList<InterfaceTreeNode> nodes = new ArrayList<InterfaceTreeNode>(treeNodes);
		for (int i = 0; i < nodes.size(); i++){
			InterfaceTreeNode node = nodes.get(i);
			node.objectChanged();
			}
	}
	
	/*****************************************************
	 * This method is called when a panel is displayed. Subclasses should override to implement
	 * specific behaviour.
	 * 
	 */
	public void showPanel(){	
	}
	

	/*****************************************************
	 * Sets the source object for this panel.
	 * 
	 * @param obj
	 * @return
	 */
	public boolean setSource(Object obj){
		return false;
	}
	
	/*****************************************************
	 * Returns the source object for this panel; returns {@code null} if no source object is
	 * set.
	 * 
	 * @return
	 */
	public Object getSource(){
		return null;
	}
	
	protected abstract void init();
	
	public void destroy(){
		isDestroyed = true;
	}
	
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	/*********************************************************
	 * Cleans up the panel's data when it loses focus. Does nothing by default; override this method to 
	 * perform custom housekeeping.
	 * 
	 */
	public void cleanUpPanel(){
		
	}
	
	public void componentHidden(ComponentEvent e) {
		cleanUpPanel();
	}

	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void shapeUpdated(ShapeEvent e){
		
	}
	
	public void updateFromDialog(InterfaceDialogBox box){
		
	}
	
	public void mouseClicked(MouseEvent e) {
		
	}

	//ensures that this panel gets focus when the mouse enters it; this is necessary to handle key events
	public void mouseEntered(MouseEvent e) {
		if (!this.hasFocus())
			this.requestFocusInWindow();
	}

	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) showPopupMenu(e);
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) showPopupMenu(e);
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public void collapseAllCategories() {
		if (!(this.getLayout() instanceof CategoryLayout)) return;
		CategoryLayout layout = (CategoryLayout)getLayout();
		layout.collapseAllCategories();
		updateUI();
	}

	public void collapseCategory(String cat) {
		
	}

	public void collapseOtherCategories(String cat) {
		if (!(this.getLayout() instanceof CategoryLayout)) return;
		CategoryLayout layout = (CategoryLayout)getLayout();
		layout.collapseOtherCategories(cat);
		updateUI();
		
	}

	public void expandAllCategories() {
		if (!(this.getLayout() instanceof CategoryLayout)) return;
		CategoryLayout layout = (CategoryLayout)getLayout();
		layout.expandAllCategories();
		updateUI();
	}

	public void expandCategory(String cat) {
		
		if (!(this.getLayout() instanceof CategoryLayout)) return;
		
		CategoryLayout layout = (CategoryLayout)getLayout();
		layout.expandCategory(cat);
		updateUI();
		
	}
	
	//Key Listener handlers; should be overridden to provide keyboard functionality
	
	@Override
	public void keyPressed(KeyEvent e) {
		
	}


	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}