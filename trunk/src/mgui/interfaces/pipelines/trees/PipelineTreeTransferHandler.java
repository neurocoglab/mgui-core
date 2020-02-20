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

package mgui.interfaces.pipelines.trees;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import mgui.pipelines.InterfacePipeline;
import mgui.pipelines.PipelineProcessInstance;
import mgui.pipelines.PipelineTask;


public class PipelineTreeTransferHandler extends TransferHandler implements Serializable {
	
	protected static DataFlavor pipeline_flavor = new DataFlavor(PipelinesTransferable.class, "Pipelines");
	protected static DataFlavor task_flavor = new DataFlavor(TasksTransferable.class, "Tasks");
	
	Transferable transferable;
	InterfacePipeline target_pipeline;
	PipelineTask target_task, source_task;
	
	public enum TransferType{
		Pipeline,
		Task;
	}
	
	@Override
	public boolean canImport(TransferHandler.TransferSupport info) {
        if (!info.isDataFlavorSupported(pipeline_flavor) &&
        	!info.isDataFlavorSupported(task_flavor)) {
            return false;
        }
        return true;
	}
	
	@Override
	protected Transferable createTransferable(JComponent c) {
		 
		JTree tree = (JTree)c;
		DefaultMutableTreeNode obj = (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
		if (obj.getUserObject() == null) return null;
		
		transferable = null;
		
		if (obj.getUserObject() instanceof InterfacePipeline)
			transferable = new PipelinesTransferable((PipelineTree)c);
		
		else if (obj.getUserObject() instanceof PipelineProcessInstance)
			transferable = new TasksTransferable((PipelineTree)c);
		 
		return transferable;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY_OR_MOVE;
	}
	
	@Override
	public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop())
            return false;
	
        JTree.DropLocation dl = (JTree.DropLocation)info.getDropLocation();
        
        try{
        	//ArrayList<ProcessInstance> tasks = (ArrayList<ProcessInstance>)t.getTransferData(task_flavor);
        	if (transferable instanceof TasksTransferable){
        		ArrayList<PipelineProcessInstance> tasks = 
        								(ArrayList<PipelineProcessInstance>)transferable.getTransferData(task_flavor);
        	
	        	if (tasks == null) return false;
	        	
	        	TreePath path = dl.getPath();
	        	target_pipeline = null;
	        	
	        	for (int i = 1; i < path.getPathCount(); i++){
	    			Object o = path.getPathComponent(i);
	    			if (o instanceof TaskTreeNode){
	    				PipelineTask t = ((TaskTreeNode)o).getTask();
	    				if (t instanceof InterfacePipeline)
	    					target_pipeline = (InterfacePipeline)t;
		    			}
		        	}
	        	
	        	PipelineTask task = (PipelineTask)((DefaultMutableTreeNode)(path.getLastPathComponent())).getUserObject();
	        	int insert_at = 0;
	        	if (task instanceof PipelineProcessInstance)
	        		insert_at = target_pipeline.getTaskIndex(task);
	        	
	        	for (int j = 0; j < tasks.size(); j++){
	        		PipelineTask clone = (PipelineTask)tasks.get(j).clone();
	        		target_pipeline.insert(insert_at, clone);
					if (j == 0)
						target_task = clone;
	        		}
	        
	        	return true;
	        	}
        	
        	return false;
        	
        }catch (Exception e){
        	e.printStackTrace();
        	return false;
        	}
	
	}
	
	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
		
		PipelineTree tree = (PipelineTree)c;
		InterfacePipeline pipeline = null;
		if (data instanceof TasksTransferable)
			 pipeline = ((TasksTransferable)data).parent;
		
		try{
			if (action == TransferHandler.MOVE) {
				if (data instanceof TasksTransferable){
					ArrayList<PipelineProcessInstance> tasks = ((TasksTransferable)data).tasks;
					
					for (int i = 0; i < tasks.size(); i++)
						pipeline.remove(tasks.get(i));
					
					}
			}
		
		}catch (Exception e){
			e.printStackTrace();
			return;
		}
		
		if (target_task == null && target_pipeline != null)
			target_task = target_pipeline.getTasks().get(0);
		
		//tree.panel.setPipelineTree();
	
		
		tree.expandPipelineNode(pipeline);
		//if (target_pipeline != null && target_pipeline.getTasks().size() > 0)
		//	tree.panel.setSelectedProcess(target_pipeline, target_task);
		
		
	}
	
	static class PipelinesTransferable implements Transferable{

		ArrayList<InterfacePipeline> pipelines;
		
		public PipelinesTransferable(PipelineTree tree){
			pipelines = tree.getSelectedPipelines();
		}
		
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
			
			if (flavor.equals(pipeline_flavor))
				return pipelines;
			
			return null;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{pipeline_flavor};
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.equals(pipeline_flavor);
		}
		
	}
	
	static class TasksTransferable implements Transferable{

		public ArrayList<PipelineProcessInstance> tasks;
		public InterfacePipeline parent;
		
		public TasksTransferable(PipelineTree tree){
			tasks = tree.getSelectedTasks();
			parent = tree.getSelectedPipeline();
		}
		
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!isDataFlavorSupported(flavor)) throw new UnsupportedFlavorException(flavor);
			
			if (flavor.equals(task_flavor))
				return tasks;
			
			return null;
		}

		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{task_flavor};
		}

		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return flavor.equals(task_flavor);
		}
		
	}

	
}