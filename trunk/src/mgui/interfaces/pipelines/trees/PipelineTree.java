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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.pipelines.PipelineProcessInstanceDialogBox;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.pipelines.InterfacePipeline;
import mgui.pipelines.StaticPipelineEvent;
import mgui.pipelines.PipelineFork;
import mgui.pipelines.StaticPipelineListener;
import mgui.pipelines.PipelineProcessInstance;
import mgui.pipelines.PipelineTask;
import mgui.pipelines.TaskParameterInstance;

/****************************************************************
 * Implements a tree interface for ModelGUI pipelines.
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineTree extends JTree implements PopupMenuObject, 
												   MouseListener,
												   StaticPipelineListener {

	DefaultTreeModel model;
	Font parameter_font = new Font("Courier", Font.PLAIN, 11);
	Font root_font = new Font("Courier", Font.BOLD, 13);
	
	public enum PopupObject{
		Pipeline,
		Process,
		Fork,
		Task;
	}
	
	protected PopupObject current_popup;
	protected Point current_location;
	
	public PipelineTree(){
		this(null);
	}
	
	public PipelineTree(InterfacePipeline pipeline){
		super();
		this.setCellRenderer(new PipelineRenderer());
		if (pipeline != null){
			TaskTreeNode pipeline_node = (TaskTreeNode)pipeline.issueTreeNode();
			this.setModel(getTreeModel(pipeline_node));
			pipeline_node.setTree(this);
			}
		this.setTransferHandler(new PipelineTreeTransferHandler());
		addMouseListener(this);
		this.setFont(new Font("Courier", Font.PLAIN, 13));
	}
	
	public void setParameterFont(Font font){
		this.parameter_font = font;
	}
	
	public Font getParameterFont(){
		return parameter_font;
	}
	
	public void setRootFont(Font font){
		this.root_font = font;
	}
	
	public Font getRootFont(){
		return root_font;
	}
	
	public void setPipeline(InterfacePipeline pipeline){
		TaskTreeNode new_node = null;
		if (model == null){
			new_node = (TaskTreeNode)pipeline.issueTreeNode();
			this.setModel(getTreeModel(new_node));
			//pipeline_node.setTree(this);
		}else{
			TaskTreeNode old_node = (TaskTreeNode)model.getRoot();
			if (old_node.getTask().equals(pipeline)) return;
			old_node.destroy();
			((InterfacePipeline)old_node.getTask()).removeStaticListener(this);
			new_node = (TaskTreeNode)pipeline.issueTreeNode();
			
			}
		
		model.setRoot(new_node);
		new_node.setTree(this);
		pipeline.addStaticListener(this);
		
	}
	
	public DefaultTreeModel getTreeModel(InterfaceTreeNode node){
		if (model == null){
			model = new DefaultTreeModel(node);
		}else{
			model.setRoot(node);
			}
		return model;
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu() {
		return getPopupMenu((List<Object>)null);
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu(List<Object> selected) {
		return null;
	}
	
	public InterfacePopupMenu getPopupMenu(MouseEvent e) {
		//if (current_event == null) return null;
		TreePath path = this.getPathForLocation(e.getX(), e.getY());
		if (path == null || path.getPathCount() == 0) return null;
		
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
		
		if (node.getUserObject() instanceof InterfacePipeline){
			return getPipelineMenu();
			}
		
		if (node.getUserObject() instanceof PipelineProcessInstance){
			return getProcessMenu();
			}
		
		if (node.getUserObject() instanceof PipelineFork){
			return getForkMenu();
			}
		
		if (node.getUserObject() instanceof PipelineTask){
			return getTaskMenu();
			}
		
		return null;
		
	}
	
	@Override
	public void pipelineUpdated(StaticPipelineEvent e) {
		
		TaskTreeNode parent = (TaskTreeNode)model.getRoot();
		TaskTreeNode node;
		PipelineTask task = e.getTask();
		InterfacePipeline pipeline;
		PipelineTask new_task;
		
		switch (e.getType()){
		
			case PipelineModified:
				model.nodeStructureChanged((InterfaceTreeNode)model.getRoot());
				return;
		
			case TaskAppended:
				pipeline = (InterfacePipeline)task;
				parent = getTaskNode(pipeline);
				if (parent == null){
					InterfaceSession.log("PipelineTree: No node for pipeline '" + pipeline.getName() + "'...", 
							LoggingType.Errors);
					return;
					}
				new_task = pipeline.getTaskAtIndex(e.getTaskIndex());
				if (new_task == null){
					InterfaceSession.log("PipelineTree: No task at index " + e.getTaskIndex() + ".", 
							LoggingType.Errors);
					return;
					}
				node = (TaskTreeNode)new_task.issueTreeNode();
				model.insertNodeInto(node, parent, parent.getChildCount());
				node.setTree(this);
				return;
		
			case TaskRemoved:
				node = getTaskNode(e.getTask());
				if (node != null){
					model.removeNodeFromParent(node);
					node.destroy();
					}
				return;
				
			case TaskInserted:
				pipeline = (InterfacePipeline)task;
				parent = getTaskNode(pipeline);
				if (parent == null){
					InterfaceSession.log("PipelineTree: No node for pipeline '" + pipeline.getName() + "'...", 
							LoggingType.Errors);
					return;
					}
				new_task = pipeline.getTaskAtIndex(e.getTaskIndex());
				if (new_task == null){
					InterfaceSession.log("PipelineTree: No task at index " + e.getTaskIndex() + ".", 
							LoggingType.Errors);
					return;
					}
				node = (TaskTreeNode)new_task.issueTreeNode();
				model.insertNodeInto(node, parent, e.getTaskIndex());
				node.setTree(this);
				return;
		
			case TaskModified:
				node = getTaskNode(e.getTask());
				if (node != null)
					model.nodeStructureChanged(node);
				return;
				
			}
		
	}
	
	/*******************************
	 * Searches for a child node having <code>task</code> as its user object.
	 * 
	 * @param task
	 * @param root
	 * @return
	 */
	protected TaskTreeNode getTaskNode(PipelineTask task){
		TaskTreeNode root = (TaskTreeNode)model.getRoot();
		if (root.getTask().equals(task)) return root;
		return getTaskNode(task, root);
	}
	
	/*******************************
	 * Searches for a child node having <code>task</code> as its user object.
	 * 
	 * @param task
	 * @param root
	 * @return
	 */
	protected TaskTreeNode getTaskNode(PipelineTask task, InterfaceTreeNode root){
		
		ArrayList<TreeNode> children = ((InterfaceTreeNode)model.getRoot()).getChildren();
		
		for (int i = 0; i < children.size(); i++){
			if (children.get(i) instanceof TaskTreeNode){
				TaskTreeNode task_node = (TaskTreeNode)children.get(i);
				if (task_node.getTask().equals(task))
					return (TaskTreeNode)children.get(i);
				if (task_node.getTask() instanceof PipelineFork){
					TaskTreeNode node = getTaskNode(task, task_node);
					if (node != null) return node;
					}
				}
			if (children.get(i) instanceof InterfaceTreeNode){
				InterfaceTreeNode tree_node = (InterfaceTreeNode)children.get(i);
				if (tree_node.getUserObject() instanceof InterfacePipeline && tree_node != root){
					TaskTreeNode node = getTaskNode(task, tree_node);
					if (node != null) return node;
					}
				}
			}
		
		return null;
		
	}
	
	protected InterfacePopupMenu getPipelineMenu(){
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Pipeline", 0));
		menu.add(new JSeparator(),1);
		menu.addMenuItem(new JMenuItem("Rename"));
		menu.addMenuItem(new JMenuItem("Copy"));
		menu.addMenuItem(new JMenuItem("Cut"));
		menu.addMenuItem(new JMenuItem("Delete"));
		
		current_popup = PopupObject.Pipeline;
		this.clearSelection();
		this.addSelectionPath(this.getClosestPathForLocation(current_location.x, current_location.y));
		
		return menu;
		
	}
	
	protected InterfacePopupMenu getProcessMenu(){
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Process Instance", 0));
		menu.add(new JSeparator(),1);
		JMenuItem item = new JMenuItem("Edit");
		item.setActionCommand("Process Edit");
		menu.addMenuItem(item);
		item = new JMenuItem("Append");
		item.setActionCommand("Process Append");
		menu.addMenuItem(item);
		item = new JMenuItem("Insert");
		item.setActionCommand("Process Insert");
		menu.addMenuItem(item);
		item = new JMenuItem("Delete");
		item.setActionCommand("Process Delete");
		menu.addMenuItem(item);
		
		current_popup = PopupObject.Process;
		this.clearSelection();
		this.addSelectionPath(this.getClosestPathForLocation(current_location.x, current_location.y));
		
		return menu;
		
	}
	
	protected InterfacePopupMenu getForkMenu(){
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Pipeline Fork", 0));
		menu.add(new JSeparator(),1);
		menu.addMenuItem(new JMenuItem("Edit"));
		menu.addMenuItem(new JMenuItem("Copy"));
		menu.addMenuItem(new JMenuItem("Cut"));
		menu.addMenuItem(new JMenuItem("Delete"));
		
		current_popup = PopupObject.Fork;
		this.clearSelection();
		this.addSelectionPath(this.getClosestPathForLocation(current_location.x, current_location.y));
		
		return menu;
		
	}
	
	protected InterfacePopupMenu getTaskMenu(){
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Task", 0));
		menu.add(new JSeparator(),1);
		menu.addMenuItem(new JMenuItem("Edit"));
		menu.addMenuItem(new JMenuItem("Copy"));
		menu.addMenuItem(new JMenuItem("Cut"));
		menu.addMenuItem(new JMenuItem("Delete"));
		
		current_popup = PopupObject.Task;
		this.clearSelection();
		this.addSelectionPath(this.getClosestPathForLocation(current_location.x, current_location.y));
		
		return menu;
		
	}
	
	public void handlePopupEvent(ActionEvent e) {
		
		if (current_popup == null || current_location == null) return;
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		TreePath path = this.getSelectionPath();
		if (path.getPathCount() == 0) return;
		
		switch (current_popup){
		
			case Pipeline:
				if (!(((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject() instanceof InterfacePipeline)) return;
				
				if (item.getText().equals("Rename")){
					InterfacePipeline pipeline = (InterfacePipeline)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
					
					String new_name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
																  "Rename pipeline:", 
																  pipeline.getName());
					if (new_name == null) return;
					
					pipeline.setName(new_name);
					this.updateUI();
					return;
					}
				
				if (item.getText().equals("Delete")){
					//panel.actionPerformed(new ActionEvent(this, 0, "Pipeline Remove"));
					return;
					}
				
				break;
				
			case Process:
				if (!(((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject() instanceof PipelineTask)) return;
				
				if (item.getActionCommand().equals("Edit")){
					//panel.actionPerformed(new ActionEvent(this, 0, "Pipeline Process Edit"));
					PipelineProcessInstance instance = getSelectedProcessInstance();
					if (instance == null) return;
					PipelineProcessInstance new_instance = PipelineProcessInstanceDialogBox.showDialog(new PipelineProcessInstance(instance));
					if (new_instance == null) return;
					
					instance.setFromProcessInstance(new_instance);
					getSelectedPipeline().fireTaskModified(instance);
					return;
					}
				
				if (item.getActionCommand().endsWith("Append")){
					
					if (getSelectedPipeline() == null) return;
					
					PipelineProcessInstance new_instance = PipelineProcessInstanceDialogBox.showDialog();
					if (new_instance == null) return;
					
					getSelectedPipeline().append(new_instance);
					
					return;
					}
				
				if (item.getActionCommand().endsWith("Insert")){
					if (getSelectedPipeline() == null) return;
					
					PipelineProcessInstance instance = this.getSelectedProcessInstance();
					PipelineProcessInstance new_instance = PipelineProcessInstanceDialogBox.showDialog();
					if (new_instance == null) return;
					
					if (instance == null)
						getSelectedPipeline().append(new_instance);
					else
						getSelectedPipeline().insertBefore(instance, new_instance);
					
					return;
					}
				
				if (item.getActionCommand().endsWith("Delete")){
					
					if (getSelectedPipeline() == null) return;
					
					InterfacePipeline pipeline = getSelectedPipeline();
					if (pipeline == null) return;
					PipelineProcessInstance instance = this.getSelectedProcessInstance();
					if (instance == null) return;
					
					if (JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
							"Really delete process '" + instance.getTreeLabel() + "'?", 
							"Delete Process Instance", 
							JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION){
						return;
						}
					
					pipeline.remove(instance);
					return;
					}
				
				break;
		
			}
		
	}
	
	public void showPopupMenu(MouseEvent e) {
		current_location = e.getPoint();
		InterfacePopupMenu menu = getPopupMenu(e);
		if (menu == null) return;
		
		menu.show(e);
	}

	public void mouseClicked(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) showPopupMenu(e);
	}

	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) showPopupMenu(e);
	}
	
	public ArrayList<InterfacePipeline> getSelectedPipelines(){
		
		ArrayList<InterfacePipeline> pipes = new ArrayList<InterfacePipeline>();
		TreePath[] paths = getSelectionPaths();
		if (paths == null) return pipes;
		
		for (int i = 0; i < paths.length; i++){
			TreePath path = paths[i];
			if (path != null)
				for (int j = 0; j < path.getPathCount(); j++){
					Object obj = path.getPathComponent(j);
					if (obj instanceof TaskTreeNode &&
						((TaskTreeNode)obj).getTask() instanceof InterfacePipeline){
						pipes.add((InterfacePipeline)((TaskTreeNode)obj).getTask());
						}
					}
			}
		
		return pipes;
	}
	
	public void expandPipelineNode(InterfacePipeline pipeline){
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
		Enumeration children = root.children();
		
		while (children.hasMoreElements()){
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
			if (child.getUserObject().equals(pipeline)){
				this.expandPath(new TreePath(child.getPath()));
				break;
				}
			}
		
		updateUI();
	}
	
	public ArrayList<PipelineProcessInstance> getSelectedTasks(){
		
		TreePath[] paths = getSelectionPaths();
		ArrayList<PipelineProcessInstance> tasks = new ArrayList<PipelineProcessInstance>();
		
		for (int i = 0; i < paths.length; i++){
			TreePath path = paths[i];
			if (path != null)
				for (int j = 1; j < path.getPathCount(); j++){
					Object obj = path.getPathComponent(j);
					if (obj instanceof TaskTreeNode &&
						((TaskTreeNode)obj).getTask() instanceof PipelineProcessInstance){
						tasks.add((PipelineProcessInstance)((TaskTreeNode)obj).getTask());
						}
					}
			}
		
		return tasks;
	}
	
	public InterfacePipeline getSelectedPipeline(){
		TreePath path = getSelectionPath();
		if (path == null) return null;
		
		for (int i = 0; i < path.getPathCount(); i++){
			Object obj = path.getPathComponent(i);
			if (obj instanceof TaskTreeNode &&
				((TaskTreeNode)obj).getTask() instanceof InterfacePipeline){
				return (InterfacePipeline)((TaskTreeNode)obj).getTask();
				}
			}
		return null;
	}
	
	public PipelineProcessInstance getSelectedProcessInstance(){
		
		TreePath path = getSelectionPath();
		if (path == null) return null;
		
		for (int i = 1; i < path.getPathCount(); i++){
			Object obj = path.getPathComponent(i);
			if (obj instanceof TaskTreeNode &&
					!(((TaskTreeNode)obj).getTask() instanceof InterfacePipeline)){
				return (PipelineProcessInstance)((TaskTreeNode)obj).getTask();
				}
			}
		return null;
		
	}
	
	class PipelineRenderer extends DefaultTreeCellRenderer{
		
		Color pfc = new Color(0, 102, 102);
		Color iofc = new Color(102, 0, 0);
		
		public PipelineRenderer(){
			super();
			this.setBackgroundSelectionColor(new Color(200, 200, 255));
			this.setOpaque(true);
		}
		
		Color getParameterForeground(){
			return pfc;
		}
		
		Color getIOForeground(){
			return iofc;
		}
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree,
												      Object value,
												      boolean sel,
												      boolean expanded,
												      boolean leaf,
												      int row,
												      boolean hasFocus){
		
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			JPanel panel = new JPanel();
			panel.setLayout(new BorderLayout());
			if (sel)
				panel.setBackground(this.getBackgroundSelectionColor());
			else
				panel.setBackground(this.getBackgroundNonSelectionColor());
			setComponentOrientation(tree.getComponentOrientation());
			
			if (value instanceof TaskTreeNode){
				PipelineTask task = ((TaskTreeNode)value).getTask();
				panel.setForeground(getTextColour(task.getStatus()));
				ImageIcon icon = (ImageIcon)task.getObjectIcon();
				icon.setImageObserver((TaskTreeNode)value);
				JLabel label = getLabel(task.getPipelineTreeLabel(), sel);
				if (icon != null){
					label.setIcon(icon);
					}
				if (tree.getModel().getRoot().equals(value))
					label.setFont(((PipelineTree)tree).getRootFont());	
				else
					label.setFont(tree.getFont());
					
				label.setComponentOrientation(tree.getComponentOrientation());
				label.setForeground(getTextColour(task.getStatus()));
				panel.add(label, BorderLayout.CENTER);
				
			}else{
				Object obj = ((DefaultMutableTreeNode)value).getUserObject();
				Font font = ((PipelineTree)tree).getParameterFont();
				String label_text = obj.toString();
				
				if (obj instanceof TaskParameterInstance || label_text.contains(": ")){
					String[] text = null;
					if (obj instanceof TaskParameterInstance){
						TaskParameterInstance param = (TaskParameterInstance)obj;
						text = param.getPipelineTreeLabel().split(": ", 2);
					}else{
						text = label_text.split(": ", 2);
						}
					JLabel name_label = getLabel(text[0] + ":", sel);
					name_label.setFont(font);
					if (obj instanceof TaskParameterInstance)
						name_label.setForeground(getParameterForeground());
					else
						name_label.setForeground(getIOForeground());
					name_label.setPreferredSize(new Dimension(150, 20));
					name_label.setComponentOrientation(tree.getComponentOrientation());
					panel.add(name_label, BorderLayout.WEST);
					
					if (text[1].contains("{"))
						text[1] = getHtmlText(text[1]);
					JLabel value_label = getLabel(text[1], sel);
					value_label.setFont(font);
					value_label.setForeground(getForeground());
					value_label.setComponentOrientation(tree.getComponentOrientation());
					
					panel.add(value_label, BorderLayout.CENTER);
				}else{
					panel.setForeground(tree.getForeground());
					JLabel label = getLabel(obj.toString(), sel);
					label.setFont(font);
					panel.add(label, BorderLayout.CENTER);
					}
				
				}
			
			return panel;
		}
		
		String getHtmlText(String text){
			
			text = "<html>" + text + "</html>";
			text = text.replaceAll("\\{", "<strong><font color=blue>{");
			text = text.replaceAll("\\}", "}</font></strong>");
			return text;
			
		}
		
		JLabel getLabel(String text, boolean sel){
			JLabel label = new JLabel(text);
			label.setOpaque(true);
			if (sel)
				label.setBackground(this.getBackgroundSelectionColor());
			else
				label.setBackground(this.getBackgroundNonSelectionColor());
			
			return label;
		}
		
		
		
		Color getTextColour(PipelineTask.Status status){
			switch (status){
				case NotStarted:
					return Color.black;
				case Processing:
					return Color.blue;
				case Failure:
				case Interrupted:
					return Color.red;
				case Success:
					return new Color(0, 102, 0);
				}
			return Color.black;
		}
		
	}
	
	
}