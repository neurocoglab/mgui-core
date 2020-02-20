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

package mgui.interfaces.pipelines;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBoxRenderer;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.pipelines.libraries.PipelineProcessLibrary;
import mgui.interfaces.pipelines.libraries.PipelineProcessLibraryOptions;
import mgui.interfaces.pipelines.trees.PipelineTree;
import mgui.interfaces.projects.InterfaceProject;
import mgui.interfaces.projects.ProjectInstance;
import mgui.io.domestic.pipelines.PipelineInputOptions;
import mgui.io.domestic.pipelines.PipelineLoader;
import mgui.io.domestic.pipelines.PipelineProcessLibraryLoader;
import mgui.io.domestic.pipelines.PipelineProcessLibraryWriter;
import mgui.io.domestic.pipelines.PipelineProcessLibraryXMLHandler;
import mgui.io.domestic.pipelines.PipelineWriter;
import mgui.io.util.IoFunctions;
import mgui.pipelines.DynamicPipelineEvent;
import mgui.pipelines.DynamicPipelineListener;
import mgui.pipelines.InterfacePipeline;
import mgui.pipelines.JavaProcess;
import mgui.pipelines.PipelineException;
import mgui.pipelines.PipelineLauncher;
import mgui.pipelines.PipelineProcess;
import mgui.pipelines.PipelineProcessInstance;
import mgui.pipelines.PipelineTask;
import mgui.pipelines.PipelineTask.Status;
import mgui.pipelines.StaticPipelineEvent;
import mgui.pipelines.StaticPipelineListener;
import mgui.pipelines.TaskParameter;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/************************************************************
 * Provides a GUI for:
 * 
 * <ul>
 * <li>creating, viewing, modifying, deleting, and executing pipelines. 
 * <li>creating, modifying, and deleting pipeline processes. 
 * <li>I/O (XML) operations on these objects.
 * </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfacePipelinesPanel extends InterfacePanel implements ActionListener,
																	   ItemListener,
																	   StaticPipelineListener,
																	   DynamicPipelineListener{

	private static final long serialVersionUID = -3560533997426865226L;

	CategoryTitle lblGeneral = new CategoryTitle("GENERAL");
	JLabel lblGeneralPipeline = new JLabel("Pipeline:");
	InterfaceComboBox cmbGeneralPipeline = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem,
																 true, 500);
	JButton cmdGeneralCreateEdit = new JButton("Create");
	JButton cmdGeneralDelete = new JButton("Delete");
	JButton cmdGeneralLoad = new JButton("Load");
	JButton cmdGeneralSave = new JButton("Save");
	JLabel lblGeneralRoot = new JLabel("Root dir:");
	JButton cmdGeneralRoot = new JButton("Browse..");
	
	CategoryTitle lblProject = new CategoryTitle("PROJECT");
	
	JCheckBox chkProjectAssign = new JCheckBox(" Assign project");
	JLabel lblProjectCurrent = new JLabel("Project:");
	InterfaceComboBox cmbProjectCurrent = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem,
																true, 500);
	JLabel lblProjectInstances = new JLabel("Instances:");
	JTable instances_list;
	InstancesTableModel instances_table_model;
	JScrollPane scrProjectInstances;
	JButton cmdProjectSelectInstance = new JButton("Select");
	JButton cmdProjectDeselectInstance = new JButton("Deselect");
	JButton cmdProjectSelectAll = new JButton("Select All");
	JButton cmdProjectSelectNone = new JButton("Select None");
	JButton cmdProjectSelectFilter = new JButton("Select Filter..");
	
	CategoryTitle lblPipeline = new CategoryTitle("PIPELINE");
	JCheckBox chkPipelineSingle = new JCheckBox(" Single pipeline");
	JCheckBox chkPipelineSerialInstances = new JCheckBox(" Serial instances");
	JCheckBox chkPipelineParallelInstances = new JCheckBox(" Parallel instances");
	PipelineTree pipeline_tree;
	JScrollPane scrPipelineTree;
	JButton cmdPipelineAppend = new JButton("Append New");
	JButton cmdPipelineInsert = new JButton("Insert New");
	JButton cmdPipelineEdit = new JButton("Edit");
	JButton cmdPipelineDelete = new JButton("Delete");
	JButton cmdPipelineLoad = new JButton("Load Task");
	JButton cmdPipelineSave = new JButton("Save Task");
	JButton cmdPipelineStopReset = new JButton("Reset");
	JButton cmdPipelineLaunch = new JButton("Launch");
	
	CategoryTitle lblProcesses = new CategoryTitle("PROCESSES");
	JLabel lblProcessesLibrary = new JLabel("Library:");
	InterfaceComboBox cmbProcessesLibrary = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem,
																  true, 500);
	JButton cmdProcessesLibraryAdd = new JButton("New");
	JButton cmdProcessesLibraryRemove = new JButton("Remove");
	JButton cmdProcessesLibraryRename = new JButton("Rename");
	JButton cmdProcessesLibraryLoad = new JButton("Load");
	JButton cmdProcessesLibrarySave = new JButton("Save");
	JButton cmdProcessesLibraryEnvironment = new JButton("Environment..");
	JLabel lblProcessesProcesses = new JLabel("Processes:");
	JList processes_list;
	DefaultListModel processes_list_model;
	JScrollPane scrProcessesList;
	JButton cmdProcessesAdd = new JButton("Add");
	JButton cmdProcessesRemove = new JButton("Remove");
	JButton cmdProcessesEdit = new JButton("Edit");
	JButton cmdProcessesHelp = new JButton("Help");
	
	InterfaceProject current_project;
	InterfacePipeline current_pipeline;
	
	HashMap<InterfacePipeline, PipelineLauncher> executing_pipelines = new HashMap<InterfacePipeline, PipelineLauncher>();
	
	boolean doUpdate = true;
	
	final static String NEW_PIPELINE = "<- New Pipeline ->";
	
	File current_root = InterfaceEnvironment.getCurrentDir();
	
	public InterfacePipelinesPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	@Override
	protected void init() {
		_init();
		
		chkPipelineSingle.setSelected(true);
		chkPipelineSerialInstances.setSelected(false);
		chkPipelineParallelInstances.setSelected(false);
		chkPipelineParallelInstances.setEnabled(false);
		
		//***** Listeners ******
		cmbProcessesLibrary.addItemListener(this);
		cmdProcessesLibrarySave.addActionListener(this);
		cmdProcessesLibrarySave.setActionCommand("Processes Library Save");
		cmdProcessesLibraryAdd.addActionListener(this);
		cmdProcessesLibraryAdd.setActionCommand("Processes Library Add");
		cmdProcessesLibraryLoad.addActionListener(this);
		cmdProcessesLibraryLoad.setActionCommand("Processes Library Load");
		cmdProcessesLibraryEnvironment.addActionListener(this);
		cmdProcessesLibraryEnvironment.setActionCommand("Processes Library Environment");
		
		cmdProcessesEdit.addActionListener(this);
		cmdProcessesEdit.setActionCommand("Processes Edit");
		cmdProcessesAdd.addActionListener(this);
		cmdProcessesAdd.setActionCommand("Processes Add");
		cmdProcessesRemove.addActionListener(this);
		cmdProcessesRemove.setActionCommand("Processes Remove");
		cmdProcessesHelp.addActionListener(this);
		cmdProcessesHelp.setActionCommand("Processes Help");
		cmbProjectCurrent.addActionListener(this);
		cmbProjectCurrent.setActionCommand("Project Changed");
		cmdProjectSelectInstance.addActionListener(this);
		cmdProjectSelectInstance.setActionCommand("Project Select");
		cmdProjectDeselectInstance.addActionListener(this);
		cmdProjectDeselectInstance.setActionCommand("Project Deselect");
		cmdProjectSelectAll.addActionListener(this);
		cmdProjectSelectAll.setActionCommand("Project Select All");
		cmdProjectSelectNone.addActionListener(this);
		cmdProjectSelectNone.setActionCommand("Project Select None");
		chkProjectAssign.addActionListener(this);
		chkProjectAssign.setActionCommand("Project Assign");
		cmdPipelineAppend.addActionListener(this);
		cmdPipelineAppend.setActionCommand("Pipeline Append");
		cmdPipelineDelete.addActionListener(this);
		cmdPipelineDelete.setActionCommand("Pipeline Delete");
		cmdPipelineInsert.addActionListener(this);
		cmdPipelineInsert.setActionCommand("Pipeline Insert");
		cmdPipelineEdit.addActionListener(this);
		cmdPipelineEdit.setActionCommand("Pipeline Edit");
		cmdPipelineLaunch.addActionListener(this);
		cmdPipelineLaunch.setActionCommand("Pipeline Launch");
		cmdPipelineStopReset.addActionListener(this);
		cmdPipelineStopReset.setActionCommand("Pipeline Stop/Reset");
		
		cmdGeneralRoot.addActionListener(this);
		cmdGeneralRoot.setActionCommand("General Set Root");
		cmbGeneralPipeline.addActionListener(this);
		cmbGeneralPipeline.setActionCommand("General Pipeline Changed");
		cmdGeneralCreateEdit.addActionListener(this);
		cmdGeneralCreateEdit.setActionCommand("General Pipeline Create/Edit");
		cmdGeneralSave.addActionListener(this);
		cmdGeneralSave.setActionCommand("General Pipeline Save");
		cmdGeneralLoad.addActionListener(this);
		cmdGeneralLoad.setActionCommand("General Pipeline Load");
		cmdGeneralDelete.addActionListener(this);
		cmdGeneralDelete.setActionCommand("General Pipeline Delete");
		
		chkPipelineSingle.addActionListener(this);
		chkPipelineSingle.setActionCommand("Pipeline Instances Single");
		chkPipelineSerialInstances.addActionListener(this);
		chkPipelineSerialInstances.setActionCommand("Pipeline Instances Serial");
		chkPipelineParallelInstances.addActionListener(this);
		chkPipelineParallelInstances.setActionCommand("Pipeline Instances Parallel");
		
		setLayout(new CategoryLayout(20, 5, 200, 10));
		
		updateGeneral();
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		add(lblGeneral, c);
		lblGeneral.setParentObj(this);
		c = new CategoryLayoutConstraints("GENERAL", 1, 1, 0.05, 0.3, 1);
		add(lblGeneralPipeline, c);
		c = new CategoryLayoutConstraints("GENERAL", 1, 1, 0.35, 0.6, 1);
		add(cmbGeneralPipeline, c);
		c = new CategoryLayoutConstraints("GENERAL", 2, 2, 0.05, 0.43, 1);
		add(cmdGeneralCreateEdit, c);
		c = new CategoryLayoutConstraints("GENERAL", 2, 2, 0.52, 0.43, 1);
		add(cmdGeneralDelete, c);
		c = new CategoryLayoutConstraints("GENERAL", 3, 3, 0.05, 0.43, 1);
		add(cmdGeneralLoad, c);
		c = new CategoryLayoutConstraints("GENERAL", 3, 3, 0.52, 0.43, 1);
		add(cmdGeneralSave, c);
		c = new CategoryLayoutConstraints("GENERAL", 4, 4, 0.05, 0.3, 1);
		add(lblGeneralRoot, c);
		c = new CategoryLayoutConstraints("GENERAL", 4, 4, 0.35, 0.6, 1);
		add(cmdGeneralRoot, c);
		
		updateProjects();
		
		c = new CategoryLayoutConstraints();
		add(lblProject, c);
		lblProject.setParentObj(this);
		c = new CategoryLayoutConstraints("PROJECT", 1, 1, 0.05, 0.9, 1);
		add(chkProjectAssign, c);
		c = new CategoryLayoutConstraints("PROJECT", 2, 2, 0.05, 0.3, 1);
		add(lblProjectCurrent, c);
		c = new CategoryLayoutConstraints("PROJECT", 2, 2, 0.35, 0.6, 1);
		add(cmbProjectCurrent, c);
		c = new CategoryLayoutConstraints("PROJECT", 3, 3, 0.05, 0.9, 1);
		add(lblProjectInstances, c);
		c = new CategoryLayoutConstraints("PROJECT", 4, 11, 0.05, 0.9, 1);
		add(scrProjectInstances, c);
		c = new CategoryLayoutConstraints("PROJECT", 12, 12, 0.05, 0.43, 1);
		add(cmdProjectSelectInstance, c);
		c = new CategoryLayoutConstraints("PROJECT", 12, 12, 0.52, 0.43, 1);
		add(cmdProjectDeselectInstance, c);
		c = new CategoryLayoutConstraints("PROJECT", 13, 13, 0.05, 0.43, 1);
		add(cmdProjectSelectAll, c);
		c = new CategoryLayoutConstraints("PROJECT", 13, 13, 0.52, 0.43, 1);
		add(cmdProjectSelectNone, c);
		c = new CategoryLayoutConstraints("PROJECT", 14, 14, 0.05, 0.43, 1);
		add(cmdProjectSelectFilter, c);
		
		updatePipelines();
		
		c = new CategoryLayoutConstraints();
		add(lblPipeline, c);
		lblPipeline.setParentObj(this);
		c = new CategoryLayoutConstraints("PIPELINE", 1, 1, 0.05, 0.9, 1);
		add(chkPipelineSingle, c);
		c = new CategoryLayoutConstraints("PIPELINE", 2, 2, 0.05, 0.9, 1);
		add(chkPipelineSerialInstances, c);
		c = new CategoryLayoutConstraints("PIPELINE", 3, 3, 0.05, 0.9, 1);
		add(chkPipelineParallelInstances, c);
		c = new CategoryLayoutConstraints("PIPELINE", 4, 13, 0.05, 0.9, 1);
		add(scrPipelineTree, c);
		c = new CategoryLayoutConstraints("PIPELINE", 14, 14, 0.05, 0.43, 1);
		add(cmdPipelineAppend, c);
		c = new CategoryLayoutConstraints("PIPELINE", 14, 14, 0.52, 0.43, 1);
		add(cmdPipelineInsert, c);
		c = new CategoryLayoutConstraints("PIPELINE", 15, 15, 0.05, 0.43, 1);
		add(cmdPipelineEdit, c);
		c = new CategoryLayoutConstraints("PIPELINE", 15, 15, 0.52, 0.43, 1);
		add(cmdPipelineDelete, c);
		c = new CategoryLayoutConstraints("PIPELINE", 16, 16, 0.05, 0.43, 1);
		add(cmdPipelineLoad, c);
		c = new CategoryLayoutConstraints("PIPELINE", 16, 16, 0.52, 0.43, 1);
		add(cmdPipelineSave, c);
		c = new CategoryLayoutConstraints("PIPELINE", 17, 18, 0.05, 0.43, 1);
		add(cmdPipelineStopReset, c);
		c = new CategoryLayoutConstraints("PIPELINE", 17, 18, 0.52, 0.43, 1);
		add(cmdPipelineLaunch, c);
		
		updateProcesses();
		
		c = new CategoryLayoutConstraints();
		add(lblProcesses, c);
		lblProcesses.setParentObj(this);
		c = new CategoryLayoutConstraints("PROCESSES", 1, 1, 0.05, 0.3, 1);
		add(lblProcessesLibrary, c);
		c = new CategoryLayoutConstraints("PROCESSES", 1, 1, 0.35, 0.6, 1);
		add(cmbProcessesLibrary, c);
		c = new CategoryLayoutConstraints("PROCESSES", 2, 2, 0.05, 0.43, 1);
		add(cmdProcessesLibraryAdd, c);
		c = new CategoryLayoutConstraints("PROCESSES", 2, 2, 0.52, 0.43, 1);
		add(cmdProcessesLibraryRemove, c);
		c = new CategoryLayoutConstraints("PROCESSES", 3, 3, 0.05, 0.43, 1);
		add(cmdProcessesLibraryLoad, c);
		c = new CategoryLayoutConstraints("PROCESSES", 3, 3, 0.52, 0.43, 1);
		add(cmdProcessesLibrarySave, c);
		c = new CategoryLayoutConstraints("PROCESSES", 4, 4, 0.05, 0.43, 1);
		add(cmdProcessesLibraryRename, c);
		c = new CategoryLayoutConstraints("PROCESSES", 4, 4, 0.52, 0.43, 1);
		add(cmdProcessesLibraryEnvironment, c);
		c = new CategoryLayoutConstraints("PROCESSES", 5, 5, 0.05, 0.9, 1);
		add(lblProcessesProcesses, c);
		c = new CategoryLayoutConstraints("PROCESSES", 6, 15, 0.05, 0.9, 1);
		add(scrProcessesList, c);
		c = new CategoryLayoutConstraints("PROCESSES", 16, 16, 0.05, 0.43, 1);
		add(cmdProcessesAdd, c);
		c = new CategoryLayoutConstraints("PROCESSES", 16, 16, 0.52, 0.43, 1);
		add(cmdProcessesRemove, c);
		c = new CategoryLayoutConstraints("PROCESSES", 17, 17, 0.05, 0.43, 1);
		add(cmdProcessesEdit, c);
		c = new CategoryLayoutConstraints("PROCESSES", 17, 17, 0.52, 0.43, 1);
		add(cmdProcessesHelp, c);
		
		
		updateDisplay();
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfacePipelinesPanel.class.getResource("/mgui/resources/icons/pipelines/pipeline_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/pipelines/pipeline_20.png");
		return null;
	}
	
	public void updateDisplay(){
		updateControls();
	}
	
	public void showPanel(){
		updateDisplay();
		updateGeneral();
		updateProjects();
		updateProcesses();
		updatePipelines();
	}
	
	private void updateGeneral(){
		
		doUpdate = false;
		
		ArrayList<InterfacePipeline> pipelines = InterfaceSession.getWorkspace().getPipelines();
		Object current = cmbGeneralPipeline.getSelectedItem();
		
		cmbGeneralPipeline.removeAllItems();
		cmbGeneralPipeline.addItem(NEW_PIPELINE);
		
		for (int i = 0; i < pipelines.size(); i++)
			cmbGeneralPipeline.addItem(pipelines.get(i));
		
		if (current != null)
			cmbGeneralPipeline.setSelectedItem(current);
		
		current = cmbGeneralPipeline.getSelectedItem();
		if (current != null){
			if (current.equals(NEW_PIPELINE))
				current_pipeline = null;
			else
				current_pipeline = (InterfacePipeline)current;
			}
		
		doUpdate = true;
		
	}
	
	private void updateControls(){
		
		//general
		if (current_pipeline == null)
			cmdGeneralCreateEdit.setText("Create");
		else
			cmdGeneralCreateEdit.setText("Rename");
		
		//projects
		boolean assign = chkProjectAssign.isSelected();
		
		cmbProjectCurrent.setEnabled(assign);
		cmdProjectDeselectInstance.setEnabled(assign);
		this.cmdProjectSelectAll.setEnabled(assign);
		this.cmdProjectSelectFilter.setEnabled(assign);
		this.cmdProjectSelectInstance.setEnabled(assign);
		this.cmdProjectSelectNone.setEnabled(assign);
		scrProjectInstances.setEnabled(assign);
		this.instances_list.setEnabled(assign);
		
		//pipelines
		if (!assign || current_project == null){
			doUpdate = false;
			chkPipelineSingle.setSelected(true);
			chkPipelineSerialInstances.setSelected(false);
			chkPipelineSerialInstances.setEnabled(false);
			chkPipelineParallelInstances.setSelected(false);
			chkPipelineParallelInstances.setEnabled(false);
			doUpdate = true;
		}else{
			chkPipelineSerialInstances.setEnabled(true);
			chkPipelineParallelInstances.setEnabled(false);	//temp while not implemented
			}
		
		//if executing
		boolean is_exec = current_pipeline != null && executing_pipelines.containsKey(current_pipeline);
		this.cmdPipelineAppend.setEnabled(!is_exec);
		this.cmdPipelineDelete.setEnabled(!is_exec);
		this.cmdPipelineEdit.setEnabled(!is_exec);
		this.cmdPipelineInsert.setEnabled(!is_exec);
		this.cmdPipelineLaunch.setEnabled(!is_exec);
		
		if (is_exec){
			this.cmdPipelineStopReset.setText("Stop");
		}else{
			this.cmdPipelineStopReset.setText("Reset");
			}
		
	}
	
	private void updateProjects(){
		
		doUpdate = false;
		
		cmbProjectCurrent.removeAllItems();
		
		
		ArrayList<InterfaceProject> projects = InterfaceSession.getWorkspace().getProjects();
		for (int i = 0; i < projects.size(); i++)
			cmbProjectCurrent.addItem(projects.get(i));
		
		if (current_project != null) 
			cmbProjectCurrent.setSelectedItem(current_project);
		else
			current_project = (InterfaceProject)cmbProjectCurrent.getSelectedItem();
				
		if (instances_table_model == null){
			instances_table_model = new InstancesTableModel(current_project);
			instances_list = new JTable(instances_table_model);
			instances_table_model.setTable(instances_list);
			InstancesRenderer renderer = new InstancesRenderer(instances_table_model, new Color(0.8f, 0.8f, 1.0f));
			instances_list.getColumn("Instance").setCellRenderer(renderer);
			this.scrProjectInstances = new JScrollPane(instances_list);
			//instances_list.getColumn("Sel").setCellRenderer(renderer);
			instances_list.getColumn("Sel").setPreferredWidth(10);
			instances_list.getColumn("Sel").setMaxWidth(25);
			instances_list.getColumn("Sel").setMinWidth(25);
		}else{
			instances_table_model.setProject(current_project);
			}
		
		doUpdate = true;
		
	}
	
	private void initPipelines(){
		mgui.pipelines.PipelineProcess process = new JavaProcess("Process1", "SomeClass.java");
		process.addParameter(new TaskParameter("param1"));
		process.addParameter(new TaskParameter("param2"));
		
		current_pipeline = new InterfacePipeline("Demo");
		
		current_pipeline.append(new PipelineProcessInstance(process, 0));
		current_pipeline.append(new PipelineProcessInstance(process, 1));
		
		updatePipelines();
	}
	
	private void updatePipelines(){
		
		if (pipeline_tree == null){
			if (current_pipeline == null){
				pipeline_tree = new PipelineTree();
				scrPipelineTree = new JScrollPane(pipeline_tree);
				pipeline_tree.setVisible(false);
				return;
			}else{
				pipeline_tree = new PipelineTree(current_pipeline);
				scrPipelineTree = new JScrollPane(pipeline_tree);
				pipeline_tree.setVisible(true);
				}
		}else{
			if (current_pipeline == null){
				pipeline_tree.setVisible(false);
			}else{
				pipeline_tree.setPipeline(current_pipeline);
				pipeline_tree.setVisible(true);
				}
			}
		
	}

	private void updateProcesses(){
		
		this.cmbProcessesLibrary.removeAllItems();
		ArrayList<PipelineProcessLibrary> libraries = InterfaceEnvironment.getPipelineProcessLibraries();
		
		for (int i = 0; i < libraries.size(); i++){
			cmbProcessesLibrary.addItem(libraries.get(i));
			}
		
		updateProcessList();
	}
	
	void updateProcessList(){
		if (processes_list_model == null){
			processes_list_model = new DefaultListModel();
			InterfaceComboBoxRenderer renderer = new InterfaceComboBoxRenderer();
			processes_list = new JList(processes_list_model);
			processes_list.setCellRenderer(renderer);
			scrProcessesList = new JScrollPane(processes_list);
			
		}else{
			processes_list_model.removeAllElements();
			}
		
		PipelineProcessLibrary library = (PipelineProcessLibrary)this.cmbProcessesLibrary.getSelectedItem();
		if (library == null) return;
		
		ArrayList<PipelineProcess> processes = library.getProcesses();
		Collections.sort(processes, new Comparator<PipelineProcess>(){
			public int compare(PipelineProcess p1, PipelineProcess p2){
				return p1.getName().compareTo(p2.getName());
				}
			});
		
		for (int i = 0; i < processes.size(); i++)
			processes_list_model.addElement(processes.get(i));
		
		
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		
		if (e.getSource().equals(cmbProcessesLibrary)){
			
			updateProcessList();
			
			return;
			}
		
		
	}
	
	protected PipelineProcessInstance getSelectedProcessInstance(){
		if (pipeline_tree == null) return null;
		return pipeline_tree.getSelectedProcessInstance();
	}
	
	protected InterfacePipeline getSelectedPipeline(){
		//get pipeline from tree selection
		//if (pipeline_tree == null) return null;
		//return pipeline_tree.getSelectedPipeline();
		Object obj = cmbGeneralPipeline.getSelectedItem();
		if (obj == null || obj instanceof String) return null;
		return (InterfacePipeline)obj;
	}
	
	protected ArrayList<InterfacePipeline> getSelectedPipelines(){
		if (pipeline_tree == null) return null;
		return pipeline_tree.getSelectedPipelines();
	}
	
	ArrayList<ProjectInstance> getSelectedInstances(){
		if (!chkProjectAssign.isSelected() || current_project == null) return null;
		ArrayList<ProjectInstance> instances = this.instances_table_model.getSelectedInstances();
		return instances;
	}
		
	class SerialPipelineLaunchInstance {
		
		public InterfaceProject project;
		public ArrayList<ProjectInstance> instances = new ArrayList<ProjectInstance>();
		public ArrayList<InterfacePipeline> pipelines = new ArrayList<InterfacePipeline>();
		
		protected Stack<ProjectInstance> instances_to_execute = new Stack<ProjectInstance>();
		protected Stack<InterfacePipeline> pipelines_to_execute = new Stack<InterfacePipeline>();
		
		public ProjectInstance current_instance;
		public InterfacePipeline current_pipeline;
		public boolean is_finished = false;
		
		public SerialPipelineLaunchInstance(ArrayList<ProjectInstance> instances, 
											ArrayList<InterfacePipeline> pipelines,
											InterfaceProject project){
			this.instances = instances;
			this.pipelines = pipelines;
			this.project = project;
			
			// Init stacks
			for (int i = instances.size() - 1; i >= 0; i--)
				instances_to_execute.push(instances.get(i));
			
		}
		
		/*************************
		 * Iterates to the next pipeline for the current instance; returns true if there IS a next
		 * pipeline; otherwise returns false. In the latter case, will iterate to the next instance
		 * and reset the pipeline stack. If no instances are left, sets the {@code is_finished} flag
		 * to true. 
		 * 
		 * @return
		 */
		public void iterate(){
			if (pipelines_to_execute.size() == 0){
				if (instances_to_execute.size() == 0){
					is_finished = true;
					return;
					}
				current_instance = instances_to_execute.pop();
				for (int i = pipelines.size() - 1; i >= 0; i--)
					pipelines_to_execute.push(pipelines.get(i));
				}
			
			if (pipelines_to_execute.size() == 0) return;
			current_pipeline = pipelines_to_execute.pop();
		}
		
		
	}
	
	HashMap<InterfacePipeline,SerialPipelineLaunchInstance> serial_launch_instances = 
													new HashMap<InterfacePipeline,SerialPipelineLaunchInstance>();
	
	// Launch a pipeline as serial instances
	boolean launchPipelinesSerial(ArrayList<InterfacePipeline> pipelines){
		
		if (!chkProjectAssign.isSelected() || current_project == null) return false;
		
		ArrayList<ProjectInstance> instances = getSelectedInstances();
		if (instances.size() == 0) return false;
				
		SerialPipelineLaunchInstance launch_instance = new SerialPipelineLaunchInstance(instances,
																						pipelines,
																						current_project);
		
		launch_instance.iterate();
		
		if (!launch_instance.is_finished){
			serial_launch_instances.put(launch_instance.current_pipeline, launch_instance);
			return launchPipeline(launch_instance.current_pipeline,
								  launch_instance.current_instance.getName(), 
								  launch_instance.project);
			}
		
		return false;
	}

	boolean launchPipeline(InterfacePipeline pipeline, String instance, InterfaceProject project){
		//exec serial
		if (pipeline == null){
			//shouldn't happen
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "Null pipeline encountered..!");
			return false;
			}
		
		if (project == null){
			if (!pipeline.setRootDirectory(current_root)){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No root directory set.");
				return false;
				}
		}else{
			if (!pipeline.setRootDirectory(project.getRootDir())){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No project root directory set.");;
				return false;
				}
			}
		
		// Attempt to reset the pipeline
		pipeline.reset();
		
		
		try{
			// Launch and return; pipeline will launch in its own thread
			//pipeline.addDynamicListener(this);
			PipelineLauncher launcher = new PipelineLauncher(pipeline, instance, project);
			launcher.addDynamicListener(this);
			launcher.execute();
			executing_pipelines.put(pipeline, launcher);
			
		}catch (Exception ex){
			//shouldn't fail here because we reset it
			InterfaceSession.log("Pipeline '" + pipeline.getName() + "' failed with exception: " + 
								 ex.getMessage(), 
								 LoggingType.Errors);
			//ex.printStackTrace();
			return false;
			}
		
			
			//}
		return true;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().startsWith("General")){
			
			if (e.getActionCommand().endsWith("Set Root")){
				
				JFileChooser jc = new JFileChooser();
				jc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jc.setSelectedFile(current_root);
				jc.setDialogTitle("Select Pipeline Root Directory");
				
				if (jc.showDialog(InterfaceSession.getSessionFrame(), "Select") == 
						JFileChooser.APPROVE_OPTION){
					current_root = jc.getSelectedFile();
					}
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Pipeline Changed")){
				if (!doUpdate) return;
				Object current = cmbGeneralPipeline.getSelectedItem();
				if (current != null){
					if (current.equals(NEW_PIPELINE))
						current_pipeline = null;
					else
						current_pipeline = (InterfacePipeline)current;
					}
				updatePipelines();
				updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Pipeline Create/Edit")){
				
				Object current = cmbGeneralPipeline.getSelectedItem();
				
				if (current == null){
					doUpdate = false;
					cmbGeneralPipeline.setSelectedItem(NEW_PIPELINE);
					doUpdate = true;
					}
				
				if (current.equals(NEW_PIPELINE)){
					//Create
					String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
															  "Name of new pipeline:", 
															  "Create Pipeline", 
															  JOptionPane.QUESTION_MESSAGE);
					
					if (name == null) return;
					InterfacePipeline pipeline = new InterfacePipeline(name);
					if (!InterfaceSession.getWorkspace().addPipeline(pipeline)){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
															  "Pipeline '" + name + "' already exists!", 
															  "Create Pipeline", 
															  JOptionPane.ERROR_MESSAGE);
						return;
					}else{
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
								  "Pipeline '" + name + "' added to the workspace.", 
								  "Create Pipeline", 
								  JOptionPane.INFORMATION_MESSAGE);
						}
					
					updateGeneral();
					cmbGeneralPipeline.setSelectedItem(pipeline);
					return;
					
				}else{
					//Rename
					String name = (String)JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
																	  "Rename pipeline:", 
																	  "Rename Pipeline", 
																	  JOptionPane.QUESTION_MESSAGE,
																	  null,
																	  null,
																	  current_pipeline.getName());

					if (name == null || name.equals(current_pipeline.getName())) return;
					if (InterfaceSession.getWorkspace().getPipeline(name) != null){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  	  "Pipeline '" + name + "' already exists!", 
												  	  "Rename Pipeline", 
												  	  JOptionPane.ERROR_MESSAGE);
						return;
						}
					
					current_pipeline.setName(name);
					this.updateGeneral();
					return;
					}
				}
			
			if (e.getActionCommand().endsWith("Pipeline Save")){
				
				if (current_pipeline == null) return;
				
				JFileChooser jc = new JFileChooser();
				jc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jc.setMultiSelectionEnabled(false);
				ArrayList<String> filter = new ArrayList<String> ();
				filter.add("pipe");
				jc.setFileFilter(IoFunctions.getFileChooserFilter(filter, "Mgui Pipelines (*.pipe)"));
				jc.setDialogTitle("Save Pipeline '" + current_pipeline.getName() + "'");
				
				if (jc.showSaveDialog(InterfaceSession.getSessionFrame()) != JFileChooser.APPROVE_OPTION){
					return;
					}
				
				PipelineWriter writer = new PipelineWriter(jc.getSelectedFile());
				boolean success = false;
				try{
					success = writer.writePipeline(current_pipeline);
				}catch (IOException ex){
					ex.printStackTrace();
					success = false;
					}
			
				if (success){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  	  "Pipeline '" + current_pipeline.getName() + "' saved.", 
											  	  "Save Pipeline", 
											  	  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  	  "Pipeline '" + current_pipeline.getName() + "' could not be saved...", 
											  	  "Save Pipeline", 
											  	  JOptionPane.ERROR_MESSAGE);
					}
				}
			
			if (e.getActionCommand().endsWith("Pipeline Load")){
				
				JFileChooser jc = new JFileChooser();
				jc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				jc.setMultiSelectionEnabled(true);
				jc.setDialogTitle("Load Pipeline(s)");
				ArrayList<String> filter = new ArrayList<String> ();
				filter.add("pipe");
				jc.setFileFilter(IoFunctions.getFileChooserFilter(filter, "Mgui Pipelines (*.pipe)"));
				
				if (jc.showOpenDialog(InterfaceSession.getSessionFrame()) != JFileChooser.APPROVE_OPTION){
					return;
					}
				
				PipelineLoader loader = new PipelineLoader();
				PipelineInputOptions options = new PipelineInputOptions();
				options.setFiles(jc.getSelectedFiles());
				if (!loader.load(options, null)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  	  "Pipeline(s) could not be loaded...", 
											  	  "Load Pipeline(s)", 
											  	  JOptionPane.ERROR_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  	  "Pipeline(s) loaded.", 
											  	  "Load Pipeline(s)", 
											  	  JOptionPane.INFORMATION_MESSAGE);
					
					showPanel();
					}
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Pipeline Delete")){
				
				if (current_pipeline == null) return;
				
				if (JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
												  "Really delete pipeline '" + current_pipeline.getTreeLabel() + "'?", 
												  "Delete Pipeline", 
												  JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION){
					return;
					}
				
				InterfaceSession.getWorkspace().removePipeline(current_pipeline);
				current_pipeline.destroy();
				
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										  	  "Pipeline '" + current_pipeline.getTreeLabel() + "' deleted.", 
										  	  "Delete Pipeline", 
										  	  JOptionPane.INFORMATION_MESSAGE);
				
				this.showPanel();
				return;
				}
				
		}
		
		
		if (e.getActionCommand().startsWith("Pipeline")){
			
			if (e.getActionCommand().endsWith("Edit")){
				
				PipelineProcessInstance instance = getSelectedProcessInstance();
				if (instance == null) return;
				PipelineProcessInstance new_instance = PipelineProcessInstanceDialogBox.showDialog(new PipelineProcessInstance(instance));
				if (new_instance == null) return;
				
				instance.setFromProcessInstance(new_instance);
				current_pipeline.fireTaskModified(instance);
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Append")){
				
				if (current_pipeline == null) return;
				
				PipelineProcessInstance new_instance = PipelineProcessInstanceDialogBox.showDialog();
				if (new_instance == null) return;
				
				current_pipeline.append(new_instance);
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Insert")){
				if (current_pipeline == null) return;
				
				PipelineProcessInstance instance = this.getSelectedProcessInstance();
				PipelineProcessInstance new_instance = PipelineProcessInstanceDialogBox.showDialog();
				if (new_instance == null) return;
				
				if (instance == null)
					current_pipeline.append(new_instance);
				else
					current_pipeline.insertBefore(instance, new_instance);
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Delete")){
				
				if (current_pipeline == null) return;
				
				InterfacePipeline pipeline = this.getSelectedPipeline();
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
			
			if (e.getActionCommand().endsWith("Launch")){
				
				ArrayList<InterfacePipeline> pipes = getSelectedPipelines();
				if (pipes == null){
					
					}
				cmdPipelineStopReset.setText("Stop");
				InterfaceProject project = null;
				if (chkProjectAssign.isSelected())
					project = current_project;
				
				if (chkPipelineSingle.isSelected()){
					launchPipeline(pipes.get(0), null, project);
				}else if (chkPipelineSerialInstances.isSelected()){
					launchPipelinesSerial(pipes);
					}
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Reset")){
				
				if (cmdPipelineStopReset.getText().startsWith("Stop")){
					
					//stop process(es)
					if (chkPipelineSerialInstances.isSelected()){
						// clear remaining pipelines if serial execution
						serial_launch_instances.clear();
						}
					
					cmdPipelineStopReset.setText("Reset");
					final InterfacePipeline pipeline = current_pipeline;
					
					try{
						pipeline.interrupt();
					}catch (PipelineException ex){
						InterfaceSession.log("InterfacePipelinesPanel: Error interrupting pipeline '" + pipeline.getName() + "'." +
								"\n" + ex.getClass() + "\n" + ex.getMessage(), 
								LoggingType.Errors);
						}
					
					new Timer().schedule(new TimerTask(){
											public void run(){
												// If interrupt hasn't worked after 2 seconds, force stop
												if (pipeline != null && 
														pipeline.getStatus().equals(Status.Processing)){
													terminate_pipeline(pipeline);
													}
											}
										},
										(long)2000);
					
					return;
					}
				
				if (cmdPipelineStopReset.getText().equals("Reset")){
				
					//CaminoPipeline pipeline = getSelectedPipeline();
					ArrayList<InterfacePipeline> pipes = getSelectedPipelines();
					
					if (pipes.size() == 0){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), "No pipelines selected!");
						return;
						}
					
					for (int i = 0; i < pipes.size(); i++){
						pipes.get(i).reset();
						// Hack solution, should have been terminated properly
						if (this.executing_pipelines.containsKey(pipes.get(i)))
							terminate_pipeline(pipes.get(i));
						}
					
					return;
					}
				
				}
			
			if (e.getActionCommand().startsWith("Pipeline Instances")){
				if (!doUpdate) return;
				
				if (e.getActionCommand().endsWith("Single")){
					if (!chkProjectAssign.isSelected() || current_project == null){
						updateControls();
						return;
						}
					
					doUpdate = false;
					
					if (chkPipelineSingle.isSelected()){
						chkPipelineSerialInstances.setSelected(false);
						chkPipelineParallelInstances.setSelected(false);
					}else{
						chkPipelineSerialInstances.setSelected(true);
						chkPipelineParallelInstances.setSelected(false);
						}
					
					doUpdate = true;
					return;
					}
				
				if (e.getActionCommand().endsWith("Serial")){
					doUpdate = false;
					
					if (chkPipelineSerialInstances.isSelected()){
						chkPipelineSingle.setSelected(false);
						chkPipelineParallelInstances.setSelected(false);
					}else{
						chkPipelineSingle.setSelected(true);
						chkPipelineParallelInstances.setSelected(false);
						}
					
					doUpdate = true;
					return;
					}
				
				if (e.getActionCommand().endsWith("Parallel")){
					doUpdate = false;
					
					//temp: not implemented yet
					chkPipelineParallelInstances.setSelected(false);
					return;
					
					}
				
				}
			
			}
		
		if (e.getActionCommand().startsWith("Processes")){ 
			
			if (e.getActionCommand().endsWith("Library Add")){
				
				String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
														  "Enter name for new process library:");
				
				if (name == null) return;
				if (InterfaceEnvironment.getPipelineProcessLibrary(name) != null){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Library already exists!", 
												  "Create New Process Library", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				PipelineProcessLibrary library = new PipelineProcessLibrary(name);
				InterfaceEnvironment.addPipelineProcessLibrary(library);
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Library created.", 
											  "Create New Process Library", 
											  JOptionPane.INFORMATION_MESSAGE);
				
				this.updateProcesses();
				return;
				}
			
			if (e.getActionCommand().endsWith("Library Save")){
				
				PipelineProcessLibrary library = (PipelineProcessLibrary)cmbProcessesLibrary.getSelectedItem();
				if (library == null) return;
				
				File current_dir = InterfaceEnvironment.getCurrentDir();
				JFileChooser jc = new JFileChooser();
				jc.setDialogTitle("Save Process Library");
				jc.setSelectedFile(current_dir);
				if (jc.showSaveDialog(InterfaceSession.getSessionFrame()) !=
						JFileChooser.APPROVE_OPTION){ 
					return;
					}
				
				PipelineProcessLibraryWriter writer = new PipelineProcessLibraryWriter(jc.getSelectedFile());
				
				try{
					writer.writeLibrary(library, null);
				}catch (IOException ex){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Error writing to file...", 
												  "Save Pipeline Process Library", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Saved to '" + jc.getSelectedFile().getName() + "'.", 
											  "Save Pipeline Process Library", 
											  JOptionPane.INFORMATION_MESSAGE);
				return;
				}
			
			if (e.getActionCommand().endsWith("Library Load")){
				
				JFileChooser jc = new JFileChooser();
				jc.setFileFilter(new javax.swing.filechooser.FileFilter() {
												        public boolean accept(File file) {
												            return file.getName().endsWith("proclib") ||
												            		file.isDirectory();
												        }
												        public String getDescription() {
												            return "Pipeline process libraries (*.proclib)";
												        }
													});
				jc.setMultiSelectionEnabled(true);
				if (jc.showOpenDialog(InterfaceSession.getSessionFrame()) == JFileChooser.CANCEL_OPTION) return;
				
				File[] files = jc.getSelectedFiles();
				PipelineProcessLibraryLoader loader = new PipelineProcessLibraryLoader();
				
				for (int i = 0; i < files.length; i++){
				
					loader.setFile(files[i]);
					
					try{
//						XMLReader reader = XMLReaderFactory.createXMLReader();
//						PipelineProcessLibraryXMLHandler handler = new PipelineProcessLibraryXMLHandler();
//						reader.setContentHandler(handler);
//						reader.setErrorHandler(handler);
//						reader.parse(new InputSource(new FileReader(files[i])));
//						
//						PipelineProcessLibrary library = new PipelineProcessLibrary(handler.library_name);
//						
//						for (int j = 0; j < handler.processes.size(); j++)
//							library.addProcess(handler.processes.get(j));
//						
//						library.
						
						PipelineProcessLibrary library = loader.loadLibrary();
						if (library == null){
							JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
									  "Error loading libary from '" + files[i].getAbsolutePath() + "'", 
									  "Load pipeline process library", 
									  JOptionPane.ERROR_MESSAGE);
							}else{
								InterfaceEnvironment.registerPipelineProcessLibrary(library);
								JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
															  "Loaded " + files.length + " libaries.", 
															  "Load pipeline process library", 
															  JOptionPane.INFORMATION_MESSAGE);
							}
						
					}catch (Exception ex){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Error loading libary from '" + files[i].getAbsolutePath() + "'", 
													  "Load pipeline process library", 
													  JOptionPane.ERROR_MESSAGE);
						InterfaceSession.log(ex.getMessage(), LoggingType.Errors);
						return;
						}
					}
				
				
				
				showPanel();
				
				return;
				
				}
			
			if (e.getActionCommand().endsWith("Library Environment")){
				
				PipelineProcessLibrary library = (PipelineProcessLibrary)cmbProcessesLibrary.getSelectedItem();
				if (library == null) return;
				
				PipelineProcessLibraryOptions _options = new PipelineProcessLibraryOptions(library);
				EnvironmentDialog.showDialog(_options);
				return;
				
			}
			
			if (e.getActionCommand().endsWith("Help")){
				
				// Try to run help for this process
				PipelineProcess process = (PipelineProcess)processes_list.getSelectedValue();
				if (process == null) return;
				process.showHelp();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Edit")){
				
				int selected = this.processes_list.getSelectedIndex();
				if (selected < 0) return;
				
				PipelineProcess process = (PipelineProcess)processes_list.getSelectedValue();
				
				PipelineProcessOptions options = new PipelineProcessOptions((PipelineProcess)process);
				PipelineProcess new_process = PipelineProcessDialogBox.showDialog(options);
				
				if (new_process == null) return;
				
				if (process.getClass().equals(new_process.getClass())){
					process.setFromProcess(new_process);
				}else{
					PipelineProcessLibrary library = (PipelineProcessLibrary)cmbProcessesLibrary.getSelectedItem();
					library.removeProcess(process.getName());
					library.addProcess(new_process);
					}
				
				//updateProcessList();
				return;
				}
			
			if (e.getActionCommand().endsWith("Add")){
				
				PipelineProcess new_process = PipelineProcessDialogBox.showDialog();
				if (new_process == null) return;
				PipelineProcessLibrary library = (PipelineProcessLibrary)cmbProcessesLibrary.getSelectedItem();
				library.addProcess(new_process);
				
				updateProcessList();
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove")){
				
				PipelineProcess process = (PipelineProcess)processes_list.getSelectedValue();
				if (process == null) return;
				
				if (JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
												  "Really remove process '" + process.getName() + "'?", 
												  "Remove Process",
												  JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION){
					return;
					}
				
				PipelineProcessLibrary library = (PipelineProcessLibrary)cmbProcessesLibrary.getSelectedItem();
				library.removeProcess(process.getName());
				
				updateProcessList();
				return;
				}
			
			}
		
		if (e.getActionCommand().startsWith("Project")){
			
			if (e.getActionCommand().endsWith("Assign")){
				updateDisplay();
				return;
				}
			
			if (e.getActionCommand().endsWith("Changed")){
				if (!doUpdate) return;
				if (current_project == cmbProjectCurrent.getSelectedItem()) return;
					
				current_project = (InterfaceProject)cmbProjectCurrent.getSelectedItem();
				updateProjects();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Select")){
				int[] selected = instances_list.getSelectedRows();
				if (selected.length == 0) return;
				instances_table_model.select(selected);
				return;
				}
			
			if (e.getActionCommand().endsWith("Select All")){
				instances_table_model.selectAll();
				return;
				}
			
			if (e.getActionCommand().endsWith("Deselect")){
				int[] selected = instances_list.getSelectedRows();
				if (selected.length == 0) return;
				instances_table_model.deselect(selected);
				return;
				}
			
			if (e.getActionCommand().endsWith("Select None")){
				instances_table_model.deselectAll();
				return;
				}
			
		}
		
		
	}
	
	@Override
	public void pipelineLaunched(DynamicPipelineEvent event){
		// Update controls to reflect execution status
		updateControls();
		
	}
	
	@Override
	public void pipelineTerminated(DynamicPipelineEvent event){
		InterfacePipeline pipeline = event.getPipeline();
		
		terminate_pipeline(pipeline);
		
	}
	
	private void terminate_pipeline(InterfacePipeline pipeline){
		
		// This pipeline has terminated; is it registered?
		if (!executing_pipelines.containsKey(pipeline)){
			InterfaceSession.log("InterfacePipelinesPanel: Pipeline '" + pipeline.getName() +
								 "' terminated but is not registered...", 
								 LoggingType.Errors);
			pipeline.removeDynamicListener(this);
			return;
			}
		
		PipelineLauncher launcher = executing_pipelines.get(pipeline);
		// Cancel if necessary
		if (!launcher.isDone()){
			launcher.cancel(true);
			}
		
		// Deregister
		executing_pipelines.remove(pipeline);
		launcher.removeDynamicListener(this);
		
		// Other pipelines to execute?
		SerialPipelineLaunchInstance launch_instance = serial_launch_instances.get(pipeline);
		if (launch_instance != null){
			serial_launch_instances.remove(pipeline);
			launch_instance.iterate();
			if (!launch_instance.is_finished){
				serial_launch_instances.put(launch_instance.current_pipeline, launch_instance);
				launchPipeline(launch_instance.current_pipeline, 
							   launch_instance.current_instance.getName(), 
							   launch_instance.project);
				}
			}
			
		// Update controls to reflect termination status
		updateControls();
		
	}
	
	@Override
	public void pipelineTaskLaunched(DynamicPipelineEvent event, PipelineTask task){
		
	}
	
	@Override
	public void pipelineTaskTerminated(DynamicPipelineEvent event, PipelineTask task){
		
	}
	
	@Override
	public void pipelineTaskUpdated(DynamicPipelineEvent event, PipelineTask task){
		
	}
	
	@Override
	public void pipelineUpdated(StaticPipelineEvent event){
		
	}
	
	class InstancesRenderer extends DefaultTableCellRenderer{

		InstancesTableModel model;
		Color selected_colour;
		
		public InstancesRenderer(InstancesTableModel model, Color selected_colour){
			super();
			this.model = model;
			this.selected_colour = selected_colour;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
													   Object value, 
													   boolean isSelected, 
													   boolean hasFocus, 
													   int row,
													   int column) {
			
			super.setBackground(table.getBackground());
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if (value instanceof ProjectInstance){
				ProjectInstance instance = (ProjectInstance)value;
				this.setIcon(instance.getObjectIcon());
				this.setText(instance.getTreeLabel());
				if (!isSelected && model.isSelected(instance.getName())){
					super.setBackground(selected_colour);
					}
				}
			
			return this;
		}
		
		
		
	}
	
	class InstancesTableModel extends DefaultTableModel{
		
		InterfaceProject project;
		HashMap<String, Boolean> selected;
		JTable table;
		
		public InstancesTableModel(InterfaceProject project){
			if (project == null) return;
			setProject(project);
		}
		
		public void setTable(JTable table){
			this.table = table;
		}
		
		public void select(int[] rows){
			if (project == null) return;
			for (int i = 0; i < rows.length; i++)
				selected.put(project.getInstance(rows[i]).getName(), true);
			this.fireTableDataChanged();
		}
		
		public void selectAll(){
			if (project == null) return;
			ArrayList<String> names = project.getInstanceNames();
			for (int i = 0; i < names.size(); i++)
				selected.put(names.get(i), true);
			this.fireTableDataChanged();
		}
		
		public void deselect(int[] rows){
			if (project == null) return;
			for (int i = 0; i < rows.length; i++)
				selected.put(project.getInstance(rows[i]).getName(), false);
			this.fireTableDataChanged();
		}
		
		public void deselectAll(){
			if (project == null) return;
			ArrayList<String> names = project.getInstanceNames();
			for (int i = 0; i < names.size(); i++)
				selected.put(names.get(i), false);
			this.fireTableDataChanged();
		}
		
		public void setProject(InterfaceProject project){
			this.project = project;
			if (project == null){
				selected = null;
			}else{
				selected = new HashMap<String, Boolean>();
				ArrayList<String> names = project.getInstanceNames();
				for (int i = 0; i < names.size(); i++)
					selected.put(names.get(i), false);
				}
			this.fireTableDataChanged();
		}
		
		public ArrayList<ProjectInstance> getSelectedInstances(){
			ArrayList<ProjectInstance> list = new ArrayList<ProjectInstance>();
			ArrayList<String> names = project.getInstanceNames();
			
			for (int i = 0; i < names.size(); i++)
				if (selected.get(names.get(i)))
					list.add(project.getInstance(names.get(i)));
			
			return list;
		}
		
		public boolean isSelected(String instance){
			if (project == null) return false;
			return selected.get(instance);
		}
		
		@Override
		public int getRowCount() {
			if (project == null) return 0;
			return project.getInstances().size();
		}

		
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) return "Sel";
			return "Instance";
		}

		@Override
		public Class<?> getColumnClass(int column) {
			if (column == 0) return Boolean.class;
			return ProjectInstance.class;
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) return selected.get(project.getInstance(row).getName());
			return project.getInstance(row);
		}
		
		@Override
		public void setValueAt(Object aValue, int row, int column) {
			if (column != 0) return;
			boolean sel = (Boolean)aValue;
			int[] sel_rows = null;
			if (table == null){
				sel_rows = new int[]{row};
			}else{
				sel_rows = table.getSelectedRows();
				}
			if (sel)
				select(sel_rows);
			else
				deselect(sel_rows);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			if (column == 0) return true;
			return false;
		}
		
		
		
	}
	
	
	static class EnvironmentDialog extends InterfaceOptionsDialogBox{
		
		JLabel lblEnvironment = new JLabel("Environment values:");
		JTable table;
		DefaultTableModel table_model;
		JScrollPane scrEnvironment;
		JButton cmdAdd = new JButton("Add");
		JButton cmdRemove = new JButton("Remove");
		JButton cmdSystem = new JButton("Set from system");
		
		public EnvironmentDialog(){
			super();
		}

		public EnvironmentDialog(InterfaceOptions options){
			super(InterfaceSession.getSessionFrame(), options);
			_init();
		}
		
		public static void showDialog(PipelineProcessLibraryOptions options){
			
			EnvironmentDialog dialog = new EnvironmentDialog(options);
			dialog.setVisible(true);
			
		}
		
		private void _init(){
			buttonType = BT_OK_CANCEL;
			super.init();
			this.setTitle("Pipeline Process Environment");
			
			LineLayout layout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
			this.setMainLayout(layout);
			this.setDialogSize(400, 450);
			
			cmdAdd.setActionCommand("Add");
			cmdAdd.addActionListener(this);
			cmdRemove.setActionCommand("Remove");
			cmdRemove.addActionListener(this);
			cmdSystem.setActionCommand("System");
			cmdSystem.addActionListener(this);
			
			init_table();
			
			LineLayoutConstraints c = new LineLayoutConstraints(1,1,0.05,0.9,1);
			mainPanel.add(lblEnvironment, c);
			c = new LineLayoutConstraints(2,8,0.05,0.9,1);
			mainPanel.add(scrEnvironment, c);
			layout.setFlexibleComponent(scrEnvironment);
			c = new LineLayoutConstraints(9,9,0.05,0.43,1);
			mainPanel.add(cmdAdd, c);
			c = new LineLayoutConstraints(9,9,0.52,0.43,1);
			mainPanel.add(cmdRemove, c);
			c = new LineLayoutConstraints(10,10,0.05,0.43,1);
			mainPanel.add(cmdSystem, c);
			
		}
		
		private void init_table(){
			
			PipelineProcessLibraryOptions _options = (PipelineProcessLibraryOptions)options;
			PipelineProcessLibrary library = _options.library;
			HashMap<String,String> environment = library.getEnvironment();
			
			if (environment == null){
				table_model = new DefaultTableModel(0,2);
			}else{
				table_model = new DefaultTableModel(environment.size(),2);
				ArrayList<String> keys = new ArrayList<String>(environment.keySet());
				Collections.sort(keys);
				
				for (int i = 0; i < keys.size(); i++){
					table_model.setValueAt(keys.get(i), i, 0);
					table_model.setValueAt(environment.get(keys.get(i)), i, 1);
					}
					
				}
			
			table_model.setColumnIdentifiers(new Object[]{"Name", "Value"});
			table = new JTable(table_model);
			scrEnvironment = new JScrollPane(table);
			
		}
		
		public void actionPerformed(ActionEvent e){
			
			if (e.getActionCommand().equals("Add")){
				
				table_model.addRow(new Object[]{"~","~"});
								
				return;
				}
			
			if (e.getActionCommand().equals("Remove")){
				
				int[] rows = table.getSelectedRows();
				Arrays.sort(rows);
				if (rows.length == 0) return;
				
				for (int i = rows.length-1; i >= 0; i--){
					table_model.removeRow(rows[i]);
					}
				
				table.clearSelection();
				return;
				}
			
			if (e.getActionCommand().equals("System")){
				HashMap<String,String> environment = new HashMap<String,String>(System.getenv());
				table_model.setRowCount(0);
				
				ArrayList<String> keys = new ArrayList<String>(environment.keySet());
				Collections.sort(keys);
				table_model.setRowCount(keys.size());
				
				for (int i = 0; i < keys.size(); i++){
					table_model.setValueAt(keys.get(i), i, 0);
					table_model.setValueAt(environment.get(keys.get(i)), i, 1);
					}
				
				table_model.fireTableDataChanged();
				return;
				}

			if (e.getActionCommand().equals(DLG_CMD_OK)){
				
				PipelineProcessLibraryOptions _options = (PipelineProcessLibraryOptions)options;
				
				// Get environment from table
				int count = table_model.getRowCount();
				if (count == 0){
					_options.library.setEnvironment(null);
				}else{
					HashMap<String,String> environment = new HashMap<String,String>();
					
					for (int i = 0; i < count; i++){
						String name = (String)table_model.getValueAt(i, 0);
						String value = (String)table_model.getValueAt(i, 1);
						if (name == null || value == null || name.equals("~")){
							JOptionPane.showMessageDialog(this, 
														  "Row " + i + " is invalid..", 
														  "Set pipeline process library environment", 
														  JOptionPane.ERROR_MESSAGE);
							return;
							}
						
						environment.put(name, value);
						}
					
					_options.library.setEnvironment(environment);
					}
				
				this.setVisible(false);
				return;
				}
			
			
			super.actionPerformed(e);
		}
		
	}
	
}