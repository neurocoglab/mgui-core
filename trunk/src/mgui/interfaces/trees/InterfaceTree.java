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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import mgui.interfaces.transfers.InterfaceTransferable;

/*********************************************************
 * This class extends {@link JTree} by specializing for modelGUI. It provides Drag-and-Drop, as well as
 * Copy-Cut-Paste functionality.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 */
public class InterfaceTree extends JTree {

	public InterfaceTree(){
		super();
		this.setDragEnabled(true);
		this.setDropMode(DropMode.ON_OR_INSERT);
		this.setTransferHandler(new DragDropHandler(this));
	}
	
	 public InterfaceTree(Object[] value) {
		 super(value);
	 }
	 
	 public InterfaceTree(TreeNode root) {
	     super(root, false);
	     if (root instanceof InterfaceTreeNode)
	    	 ((InterfaceTreeNode)root).setParentTree(this);
	 }
	 
	 public InterfaceTree(TreeModel newModel) {
		 super(newModel);
	 }
	 
	 //overridden to allow only leaf edits
	 @Override
	public boolean isPathEditable(TreePath path) { 
		 if (isEditable()) { 
			 return getModel().isLeaf(path.getLastPathComponent()); 
		 	 }
		 return false;
	 }
	
	 /*************************************************
	  * Handles drop events on the tree. Events will be handled differently based upon the type of source objects in the
	  * tree path. This requires that the source objects themselves provide transfer handlers...
	  * 
	  * @author Andrew Reid
	  *
	  */
	 static class DragDropHandler extends TransferHandler{

		InterfaceTree tree;
		 
		 public DragDropHandler(InterfaceTree tree){
			 this.tree = tree;
		 }
		 
		@Override
		protected Transferable createTransferable(JComponent c) {
			//get selected node(s), determine whether source(s) is(are) transferable
	        TreePath[] paths = tree.getSelectionPaths();
	        if(paths == null) return null;
	        ArrayList<Transferable> transferables = new ArrayList<Transferable>();
	        
	        for (int i = 0; i < paths.length; i++){
	        	DefaultMutableTreeNode node = (DefaultMutableTreeNode)paths[i].getLastPathComponent();
	        	if (node instanceof InterfaceTreeNode && node.getUserObject() instanceof Transferable){
	        		Transferable t = (Transferable)node.getUserObject();
	        		//only add transferables with the same data flavours
	        		if (transferables.size() == 0)
	        			transferables.add(t);
	        		else if (t.getClass().isInstance(transferables.get(0))) 
	        			transferables.add(t);
	        		}
	        	}
	        
	        if (transferables.size() == 0) return null;
	       return new TransferableList(transferables);
		}

		@Override
		protected void exportDone(JComponent source, Transferable data, int action) {
			
			//execute code if necessary
			
			
			super.exportDone(source, data, action);
		}

		@Override
		public int getSourceActions(JComponent c) {
			//depends on the source object
			return COPY_OR_MOVE;
		}
		
		@Override
		public boolean canImport(TransferSupport support) {
			JTree.DropLocation dropLocation = (JTree.DropLocation)support.getDropLocation();
			TreePath drop_path = dropLocation.getPath();
			int childIndex = dropLocation.getChildIndex();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)drop_path.getLastPathComponent();
			
			if (childIndex < 0 || childIndex == node.getChildCount()){
				//this is a drop onto a node
			}else{
				//this is an insert into a node's children; find child and insert before it
				
				//node = (DefaultMutableTreeNode)node.getChildAt(childIndex);
				//node = (DefaultMutableTreeNode)node.getChildAt(childIndex);
				}
			
			DataFlavor[] flavors = support.getDataFlavors();
			if (!(node.getUserObject() instanceof InterfaceTransferable)) return false;
		
			//TODO: have target determine the acceptable drop actions  
			
			//boolean copySupported = (COPY & support.getSourceDropActions()) == COPY;
			//boolean moveSupported = (MOVE & support.getSourceDropActions()) == MOVE;
			Transferable transferable = (Transferable)node.getUserObject();
			
			for (int i = 0; i < flavors.length; i++)
				if (transferable.isDataFlavorSupported(flavors[i])) return true;
			
			return false;
		}

		@Override
		public boolean importData(TransferSupport support) {
			//perform the import
			JTree.DropLocation dropLocation = (JTree.DropLocation)support.getDropLocation();
			TreePath drop_path = dropLocation.getPath();
			int childIndex = dropLocation.getChildIndex();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)drop_path.getLastPathComponent();
			
			if (childIndex < 0 || childIndex == node.getChildCount()){
				//this is a drop onto a node; same as inserting above it
				//childIndex = tree.getModel().getChildCount(drop_path.getLastPathComponent());
				
			}else{
				//this is an insert into a node's children; find child and insert before it
				//node = (DefaultMutableTreeNode)node.getChildAt(childIndex);
				}
				
			if (!(node.getUserObject() instanceof InterfaceTransferable)) return false;
			
			InterfaceTransferable target = (InterfaceTransferable)node.getUserObject();
			return target.performTransfer(support);
			
		}
 
	 }
	 
	 static class TransferableList implements Transferable{

		ArrayList<Transferable> transferables = new ArrayList<Transferable>();
		
		public TransferableList(ArrayList<Transferable> transferables){
			this.transferables = transferables;
		}
		 
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			ArrayList<Object> data = new ArrayList<Object>();
			for (int i = 0; i < transferables.size(); i++)
				data.add(transferables.get(i).getTransferData(flavor));
			return data;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			if (transferables.size() == 0) return null;
			return transferables.get(0).getTransferDataFlavors();
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			if (transferables.size() == 0) return false;
			return transferables.get(0).isDataFlavorSupported(flavor);
		}
		 
	 }
	 
}