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

package mgui.interfaces.trees;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceObjectListener;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;

public class InterfaceTreeNode extends DefaultMutableTreeNode implements Cloneable,
																		 InterfaceObjectListener,
																		 PopupMenuObject {
	
	protected ArrayList<TreeListener> listeners = new ArrayList<TreeListener>();
	public boolean isDestroyed = false;
	public boolean isSelectable = true;
	public boolean isMouseOverSelect = false;
	
	protected InterfacePopupMenu popup_menu;
	protected PopupMenuObject popup_handler;
	
	protected JTree parent_tree;
	
	public InterfaceTreeNode(){
		super();
	}
	
	public InterfaceTreeNode(InterfaceObject thisObj){
		super();
		setUserObject(thisObj);
	}
	
	public InterfaceTreeNode(InterfaceObject thisObj, JTree parent){
		super();
		setUserObject(thisObj);
		this.parent_tree = parent;
	}
	
	public InterfaceTreeNode(String nodeStr){
		super(nodeStr);
	}
	
	public ArrayList<TreeNode> getChildren(){
		
		Enumeration children = children();
		ArrayList<TreeNode> list = new ArrayList<TreeNode>();
		
		while (children.hasMoreElements())
			list.add((TreeNode)children.nextElement());
		
		return list;
	}
	
	public void setParentTree(JTree tree){
		this.parent_tree = tree;
	}
	
	public JTree getParentTree(){
		if (parent_tree != null)
			return this.parent_tree;
		if (this.getParent() == null) return null;
		InterfaceTreeNode node = getNextInterfaceParent(getParent());
		if (node == null) return null;
		return node.getParentTree();
	}
	
	InterfaceTreeNode getNextInterfaceParent(TreeNode node){
		if (node == null || node instanceof InterfaceTreeNode) return (InterfaceTreeNode)node;
		return getNextInterfaceParent(node.getParent());
	}
	
	public void insertChild(InterfaceTreeNode node, int pos){
		if (parent_tree != null) node.setParentTree(this.parent_tree);
		if (listeners.size() == 0)
			insert(node, pos + 1);
		else
			fireTreeListeners(new TreeEvent(this, node, TreeEvent.EventType.NodeInserted, pos + 1));
		node.listeners = listeners;
		node.init();
	}
	
	public void moveChild(InterfaceTreeNode node, int pos){
		
		//decrement pos if node is currently before it in list
		int index = getIndex(node);
		if (index < pos) pos--;
		
		if (listeners.size() == 0){
			remove(node);
			insert(node, pos + 1);
		}else{
			fireTreeListeners(new TreeEvent(this, node, TreeEvent.EventType.NodeRemoved));
			fireTreeListeners(new TreeEvent(this, node, TreeEvent.EventType.NodeInserted, pos + 1));
			}
		node.listeners = listeners;
		node.init();
	}
	
	/****************************************************
	 * Adds a child to an <code>InterfaceTreeNode</code> and sets its listeners. Use instead
	 * of <code>add</code> to ensure proper tree behaviour for {@link InterfaceObject}s.
	 * 
	 * @param node
	 */
	public void addChild(InterfaceTreeNode node){
		if (parent_tree != null) node.setParentTree(this.parent_tree);
		if (listeners.size() == 0)
			add(node);
		else
			fireTreeListeners(new TreeEvent(this, node, TreeEvent.EventType.NodeAdded));
		node.listeners = listeners;
		node.init();
	}
	
	public void removeChild(InterfaceTreeNode node){
		node.destroy();
		fireTreeListeners(new TreeEvent(this, node, TreeEvent.EventType.NodeRemoved));
	}
	
	public void destroyAllChildren(){
		//Enumeration children = children();   WTF is up with this class?
		ArrayList<TreeNode> children = getChildren();
		
		for (int i = 0; i < children.size(); i++){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)children.get(i);
			if (node instanceof InterfaceTreeNode)
				removeChild((InterfaceTreeNode)node);
			}
	}
	
	public boolean isSelectable(){
		return isSelectable;
	}
	
	public void setSelectable(boolean s){
		isSelectable = s;
	}
	
	public boolean containsObject(Object o){
		if (getUserObject().equals(o)) return true;
		Enumeration children = children();
		while (children.hasMoreElements()){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)children.nextElement();
			if (node instanceof InterfaceTreeNode && 
				((InterfaceTreeNode)node).containsObject(o))
					return true;
			}
		return false;
	}
	
	public void setUserObject(InterfaceObject thisObj){
		super.setUserObject(thisObj);
		fireTreeListeners(new TreeEvent(this, TreeEvent.EventType.NodeModified));
	}
	
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	public void destroy(){
		destroy(true);
	}
	
	public void destroy(boolean fire){
		isDestroyed = true;
		//destroy all children
		if (children != null)
			for (int i = 0; i < children.size(); i++)
				if (children.get(i) instanceof InterfaceTreeNode)
					((InterfaceTreeNode)children.get(i)).destroy(false);
		if (this.userObject instanceof InterfaceTreeNode){
			InterfaceTreeNode node = (InterfaceTreeNode)userObject;
			node.destroy();
			}
		if (fire)
			fireTreeListeners(new TreeEvent(this, TreeEvent.EventType.NodeDestroyed));
	}
	
	public void init(){
		//recursively set all child node listeners to this node's listeners
		if (children != null)
			for (int i = 0; i < children.size(); i++)
				if (children.get(i) instanceof InterfaceTreeNode){
					((InterfaceTreeNode)children.get(i)).listeners = listeners;
					((InterfaceTreeNode)children.get(i)).init();
					}
	}
	
	public void objectChanged(){
		if (getUserObject() instanceof InterfaceObject)
			((InterfaceObject)getUserObject()).setTreeNode(this);
		fireTreeListeners(new TreeEvent(this, TreeEvent.EventType.NodeModified));
	}
	
	public void objectChanged(InterfaceObject object){
		if (object != getUserObject()) return;
		object.setTreeNode(this);
		fireTreeListeners(new TreeEvent(this, TreeEvent.EventType.NodeModified));
	}
	
	@Override
	public Object clone(){
		InterfaceTreeNode node = (InterfaceTreeNode)super.clone();
		return node;
	}
	
	public void addListener(TreeListener l){
		if (l != null && !listeners.contains(l))
			listeners.add(l);
	}
	
	public void removeListener(TreeListener l){
		listeners.remove(l);
	}
	
	protected void fireTreeListeners(TreeEvent e){
		ArrayList<TreeListener> temp = new ArrayList<TreeListener>(listeners);
		for (int i = 0; i < temp.size(); i++)
			temp.get(i).treeUpdated(e);
		
	}
	
	public void setPopupMenu(InterfacePopupMenu menu){
		this.popup_menu = menu;
	}
	
	public InterfacePopupMenu getPopupMenu() {
		if (popup_menu != null)
			return popup_menu;
		
		if (getUserObject() instanceof PopupMenuObject)
			return ((PopupMenuObject)getUserObject()).getPopupMenu();
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Tree Node Menu Item 1"));
		menu.addMenuItem(new JMenuItem("Tree Node Menu Item 2"));
		
		return menu;
	}

	public void handlePopupEvent(ActionEvent e) {
		
		if (getUserObject() instanceof PopupMenuObject)
			((PopupMenuObject)getUserObject()).handlePopupEvent(e);
			
	}
	
	public void handleMouseEvent(MouseEvent e, InterfaceTree tree){
		
		if (isMouseOverSelect){
			TreePath path = new TreePath(this.getPath());
			tree.setSelectionPath(path);
			}
		
	}

	public void showPopupMenu(MouseEvent e) {
		InterfacePopupMenu menu = getPopupMenu();
		if (menu == null) return;
		menu.show(e);
	}

	
}