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

package mgui.interfaces.pipelines.trees;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.pipelines.PipelineTask;
import mgui.pipelines.PipelineTaskEvent;
import mgui.pipelines.PipelineTaskListener;

/*****************************************************************
 * Tree node for a pipeline task.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class TaskTreeNode extends InterfaceTreeNode 
						  implements PipelineTaskListener,
									 ImageObserver{

	PipelineTree tree;
	DefaultTreeModel model;
	Vector<TaskTreeNodeListener> listeners = new Vector<TaskTreeNodeListener>();
	
	public TaskTreeNode(PipelineTask task){
		this(task, null);
	}
	
	public TaskTreeNode(PipelineTask task, PipelineTree tree){
		super(task);
		task.addListener(this);
		this.addTreeNodeListener(task);
		if (tree != null){
			setTree(tree);
			}
	}
	
	public void addTaskChild(TaskTreeNode node){
		super.addChild(node);
		if (tree != null)
			node.setTree(tree);
	}
	
	public void setTree(PipelineTree tree){
		this.model = (DefaultTreeModel)tree.getModel();
		this.tree = tree;
	}
	
	public void taskStatusChanged(PipelineTaskEvent e){
		if (model == null) return;
		model.nodeStructureChanged(this);
		tree.repaint();
	}
	
	public void addTreeNodeListener(TaskTreeNodeListener l){
		listeners.add(l);
	}
	
	public void removeTreeNodeListener(TaskTreeNodeListener l){
		listeners.add(l);
	}
	
	public PipelineTask getTask(){
		return (PipelineTask)getUserObject();
	}
	
	public void detach(){
		TaskTreeNodeEvent e = new TaskTreeNodeEvent(this);
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).treeNodeDetached(e);
		listeners.clear();
		
		//detach all children
		Enumeration children = this.children();
		while (children.hasMoreElements()){
			Object obj = children.nextElement();
			if (obj instanceof TaskTreeNode)
				((TaskTreeNode)obj).detach();
			}
		
		((DefaultMutableTreeNode)getParent()).remove(this);
	}
	
	@Override
	public boolean imageUpdate(Image img, int flags, int x, int y, int w, int h) {
		if (tree == null || model == null) return false;
	      if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
	    	  TreePath path = new TreePath(model.getPathToRoot(this));
	    	  Rectangle rect = tree.getPathBounds(path);
	    	  if (rect != null) {
	    		  tree.repaint(rect);
	    	  	  }
	      	  }
	      return (flags & (ALLBITS | ABORT)) == 0;
	}

}