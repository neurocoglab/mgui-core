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

package mgui.interfaces.trees;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.graphs.tree.GraphTreeCellRenderer;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.VertexDataColumn;
import mgui.interfaces.shapes.trees.ShapeTreeCellRenderer;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.trees.util.TreeKeyHandler;


/**
 * Provides a tree interface for model objects 
 * @author Andrew Reid
 *
 */

public class InterfaceTreePanel extends InterfacePanel implements TreeListener,
																  KeyListener,
																  MouseMotionListener{

	//tree showing object model in memory
	InterfaceTree tree = new InterfaceTree();
	
	DefaultTreeModel model; 
	
	public InterfaceTreePanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	@Override
	protected void init(){
		_init();
		tree.setFont(new Font("Courier", Font.PLAIN, 13));
		//set up stuff
		tree.setRowHeight(20);
		this.setLayout(new GridLayout(1, 1));
		InterfaceTreeCellRenderer renderer = new InterfaceTreeCellRenderer();
		
		tree.setCellRenderer(renderer);
		tree.setCellEditor(new InterfaceTreeCellEditor());
		tree.setEditable(true);
		this.add(tree);
		addKeyListener(this);
		tree.addKeyListener(this);
		tree.addMouseMotionListener(this);
		tree.addMouseListener(this);
		
		renderer.addRenderer(InterfaceShape.class, new ShapeTreeCellRenderer());
		renderer.addRenderer(InterfaceAbstractGraph.class, new GraphTreeCellRenderer());
		
		
	}
	
	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceTreePanel.class.getResource("/mgui/resources/icons/tree_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/tree_20.png",
								 LoggingType.Errors);
		return null;
	}
	
	@Override
	public void showPopupMenu(MouseEvent e){
		InterfacePopupMenu menu = getPopupMenu(e);
		if (menu == null) return;
		menu.show(e);
	}
	
	public InterfacePopupMenu getPopupMenu(MouseEvent e){
		
		//find item and display its menu
		TreePath path = tree.getClosestPathForLocation(e.getPoint().x, e.getPoint().y);
		if (path == null) return null;
		
		if (!(path.getLastPathComponent() instanceof InterfaceTreeNode)) return super.getPopupMenu();
		
		return ((InterfaceTreeNode)path.getLastPathComponent()).getPopupMenu();
		
	}
	
	public DefaultTreeModel getModel(){
		return model;
	}
	
	public JTree getTree(){
		return tree;
	}
	
	public void setRootNode(InterfaceTreeNode node){
		//remove this tree from existing root note
		if (model != null)
			((InterfaceTreeNode)model.getRoot()).removeListener(this);
		
		model = new DefaultTreeModel(node);
		tree.setModel(model);
		node.setParentTree(tree);
		node.addListener(this);
		//sets all child node listeners
		node.init();
	}
	
	@Override
	public String toString(){
		return "Tree Panel";
	}
	
	@Override
	public void updateDisplay(){
		tree.updateUI();
	}
	
	public void treeUpdated(TreeEvent e){
		
		try{
			TreeNode parent = null;
			switch (e.eventType){
			
				case NodeAdded:
					//append node
					model.insertNodeInto(e.getChildNode(), e.getParentNode(), e.getParentNode().getChildCount());
					//tree.scrollPathToVisible(new TreePath(e.getChildNode().getPath()));
					break;
					
				case NodeInserted:
					//insert node
					model.insertNodeInto(e.getChildNode(), e.getParentNode(), e.insert_at);
					//tree.scrollPathToVisible(new TreePath(e.getChildNode().getPath()));
					break;
			
				case NodeRemoved:
					//append node
					if (e.getChildNode() == null) return;
					parent = e.getChildNode().getParent();
					if (parent != null)
						model.removeNodeFromParent(e.getChildNode());
					break;
					
				case NodeDestroyed:
					//remove node
					parent = e.getNode().getParent();
					if (parent != null)
						model.removeNodeFromParent(e.getNode());
					break;
					
				case NodeModified:
					//notify that changes were made
					//model.nodeChanged(e.getNode());
					if (e.getNode() != null){
						model.nodeStructureChanged(e.getNode());
						model.nodesWereInserted(e.getNode(), null);
						tree.scrollPathToVisible(new TreePath(e.getNode().getPath()));
						}
					break;
					
				}
		}catch (Exception ex){
			//ex.printStackTrace();
			InterfaceSession.log("Tree update exception..");
			}
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		TreePath path = tree.getClosestPathForLocation(e.getPoint().x, e.getPoint().y);
		if (path.getLastPathComponent() instanceof InterfaceTreeNode){
			((InterfaceTreeNode)path.getLastPathComponent()).handleMouseEvent(e, tree);
			}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		//InterfaceSession.log("Tree has detected keyPressed event..");
		
		
		
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
		// TODO: Make this generic; objects should handle their own delete requests... 
		
		if (e.getKeyCode() == KeyEvent.VK_DELETE){
			//delete key was pressed, delete selected objects after confirmation
			
			TreePath[] paths = tree.getSelectionPaths();
			
			if (paths == null || paths.length == 0){
				return;
				}
			
			ArrayList<InterfaceShape> shapes = new ArrayList<InterfaceShape>();
			ArrayList<InterfaceAbstractGraph> graphs = new ArrayList<InterfaceAbstractGraph>();
			ArrayList<InterfaceTreeNode> column_nodes = new ArrayList<InterfaceTreeNode>();
 			
			for (int i = 0; i < paths.length; i++){
				InterfaceTreeNode node = (InterfaceTreeNode)paths[i].getLastPathComponent();
				if (node.getUserObject() instanceof InterfaceShape)
					shapes.add((InterfaceShape)node.getUserObject());
				if (node.getUserObject() instanceof InterfaceAbstractGraph)
					graphs.add((InterfaceAbstractGraph)node.getUserObject());
				if (node.getUserObject() instanceof VertexDataColumn){
					column_nodes.add(node);
					}
				}
			
			int count = shapes.size(); 
			boolean something_was_deleted = false; 
			
			if (count != 0){
				something_was_deleted = true;
				if (JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), "Really delete " + count + " shapes?", 
											  	  "Delete Shapes", 
											      JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
					
					boolean updateModel = false;
					count = 0;
					
					for (int i = 0; i < shapes.size(); i++){
						if (shapes.get(i) instanceof ShapeSet3DInt && 
						   ((Shape3DInt)shapes.get(i)).getModel().getModelSet().equals(shapes.get(i))){
								JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Can't delete a model set!");
						}else{
							if (shapes.get(i) instanceof Shape3DInt){
								Shape3DInt shape = (Shape3DInt)shapes.get(i);
								ShapeModel3D model = shape.getModel();
								if (model != null && model.getModelSet() != null)
									model.getModelSet().removeShape(shape, true, true);
								shape.destroy();
								updateModel = true;
								count++;
								}
							
							else if (shapes.get(i) instanceof Shape2DInt){
								Shape2DInt shape = (Shape2DInt)shapes.get(i);
								ShapeModel3D model = shape.getModel();
								if (model != null && model.getModelSet() != null)
									model.getModelSet().removeShape2D(shape, true, true);
								updateModel = true;
								count++;
								}
							}
						
						}
					
					if (updateModel){
						ShapeSet3DInt shape_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
						shape_set.updateShape();
						shape_set.fireShapeModified();
						shape_set.fireChildren2D(new ShapeEvent(shape_set, ShapeEvent.EventType.ShapeRemoved));
						}
					
					InterfaceSession.getDisplayPanel().updateDisplays();
					
					System.gc();
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), count + " shapes deleted.");
					}
				}
				
			count = graphs.size(); 
			
			if (count != 0){
				something_was_deleted = true;
				
				if (JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), "Really delete " + count + " graphs?", 
					  	  "Delete Shapes", 
					      JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
				
					for (int i = 0; i < graphs.size(); i++){
						InterfaceAbstractGraph graph = graphs.get(i);
						InterfaceSession.getWorkspace().removeGraph(graph);
						graph.destroy();
						InterfaceSession.getWorkspace().updateObjectTree();
						}
					}
				
				}
			
			count = column_nodes.size();
			if (count != 0){
				something_was_deleted = true;
				
				if (JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), "Really delete " + count + " data columns?", 
					  	  "Delete Vertex Data Columns", 
					      JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
				
					for (int i = 0; i < column_nodes.size(); i++){
						InterfaceTreeNode node = column_nodes.get(i);
						VertexDataColumn column = (VertexDataColumn)node.getUserObject();
						InterfaceShape parent = getParentShape(node);
						if (parent == null){
							InterfaceSession.log("InterfaceTreePanel.keyReleased: Vertex column" + column.getName() + "' has no parent..", LoggingType.Errors);
						}else{
							parent.removeVertexData(column.getName());
							}
						}
					InterfaceSession.getWorkspace().updateObjectTree();
					}
				
				}
			
			if (!something_was_deleted){
				for (int i = 0; i < paths.length; i++){
					InterfaceTreeNode node = (InterfaceTreeNode)paths[i].getLastPathComponent();
					if (node.getUserObject() instanceof TreeKeyHandler)
						((TreeKeyHandler)node.getUserObject()).handleTreeKeyEvent(e);
					}
				return;
				}
			
			return;
			}
		
	}
	
	protected InterfaceShape getParentShape(InterfaceTreeNode node){
		Object[] path = node.getUserObjectPath();
		for (int i = 0; i < path.length; i++){
			if (path[i] != null && path[i] instanceof InterfaceShape)
				return (InterfaceShape)path[i];
			}
		return null;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
		
	}
	
	class DeleteAction extends AbstractAction{

		public void actionPerformed(ActionEvent e) {
			
			//InterfaceSession.log("Delete action detected on object: " + e.getSource().toString());
			
		}
		
	}
}