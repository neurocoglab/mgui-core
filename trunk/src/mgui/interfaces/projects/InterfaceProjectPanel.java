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

package mgui.interfaces.projects;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.pipelines.InterfacePipelinesPanel;
import mgui.interfaces.projects.util.ConsolidateDirectoriesDialogBox;
import mgui.interfaces.projects.util.ProjectOptions;
import mgui.util.StringFunctions;

/****************************************************
 * Panel which provides a user interface for {@linkplain InterfaceProject}s.  
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceProjectPanel extends InterfacePanel implements ActionListener,
																	 ItemListener{

	private static final long serialVersionUID = 8467914691198300633L;

	CategoryTitle lblProject = new CategoryTitle("GENERAL");
	JLabel lblProjectCurrent = new JLabel("Current project:");
	InterfaceComboBox cmbProjectCurrent = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem,
			 													true, 500);
	JLabel lblProjectRoot = new JLabel("Root:");
	JTextField txtProjectRoot = new JTextField("");
	JButton cmdProjectRoot = new JButton("Set..");
	JButton cmdProjectCreateEdit = new JButton("Create");
	JButton cmdProjectDelete = new JButton("Delete");
	
	CategoryTitle lblFileStructure = new CategoryTitle("FILE STRUCTURE");
	
	CategoryTitle lblDataItems = new CategoryTitle("DATA ITEMS");
	
	CategoryTitle lblDatabase = new CategoryTitle("DATABASE");
	
	CategoryTitle lblUtilities = new CategoryTitle("UTILITIES");
	JLabel lblUtilitiesProcess = new JLabel("Process:");
	InterfaceComboBox cmbUtilitiesProcess = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem,
			 													  true, 500);
	JButton cmdUtilitiesExecute = new JButton("Run..");
	
	protected InterfaceProject current_project;
	protected static final String NEW_PROJECT = "<-- New Project -->";

	public InterfaceProjectPanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	@Override
	protected void init() {
		_init();
		
		// Listeners n stuff
		cmdUtilitiesExecute.addActionListener(this);
		cmdUtilitiesExecute.setActionCommand("Utilities Execute");
		cmbProjectCurrent.addItemListener(this);
		
		cmdProjectCreateEdit.setActionCommand("Create/Edit Project");
		cmdProjectCreateEdit.addActionListener(this);
		
		// Layout
		setLayout(new CategoryLayout(20, 5, 200, 10));
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();

		add(lblProject, c);
		lblProject.setParentObj(this);
		c = new CategoryLayoutConstraints("GENERAL", 1, 1, 0.05, 0.2, 1);
		add(lblProjectCurrent, c);
		c = new CategoryLayoutConstraints("GENERAL", 1, 1, 0.25, 0.7, 1);
		add(cmbProjectCurrent, c);
		c = new CategoryLayoutConstraints("GENERAL", 2, 2, 0.05, 0.2, 1);
		add(lblProjectRoot, c);
		c = new CategoryLayoutConstraints("GENERAL", 2, 2, 0.25, 0.45, 1);
		add(txtProjectRoot, c);
		c = new CategoryLayoutConstraints("GENERAL", 2, 2, 0.7, 0.25, 1);
		add(cmdProjectRoot, c);
		c = new CategoryLayoutConstraints("GENERAL", 3, 3, 0.05, 0.43, 1);
		add(cmdProjectCreateEdit, c);
		c = new CategoryLayoutConstraints("GENERAL", 3, 3, 0.52, 0.43, 1);
		add(cmdProjectDelete, c);
		
		c = new CategoryLayoutConstraints();
		add(lblFileStructure, c);
		lblFileStructure.setParentObj(this);
		
		c = new CategoryLayoutConstraints();
		add(lblDataItems, c);
		lblDataItems.setParentObj(this);
		
		c = new CategoryLayoutConstraints();
		add(lblDatabase, c);
		lblDatabase.setParentObj(this);
		
		c = new CategoryLayoutConstraints();
		add(lblUtilities, c);
		lblUtilities.setParentObj(this);
		c = new CategoryLayoutConstraints("UTILITIES", 1, 1, 0.05, 0.3, 1);
		add(lblUtilitiesProcess, c);
		c = new CategoryLayoutConstraints("UTILITIES", 1, 1, 0.35, 0.6, 1);
		add(cmbUtilitiesProcess, c);
		c = new CategoryLayoutConstraints("UTILITIES", 2, 2, 0.15, 0.7, 1);
		add(cmdUtilitiesExecute, c);
		
		// Update stuff
		initCombos();
		
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfacePipelinesPanel.class.getResource("/mgui/resources/icons/projects/project_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/projects/project_20.png");
		return null;
	}
	
	public void showPanel(){
		
		initCombos();
		
	}
	
	private void initCombos(){
		
		// Projects
		ArrayList<InterfaceProject> projects = InterfaceSession.getWorkspace().getProjects();
		
		cmbProjectCurrent.removeAllItems();
		cmbProjectCurrent.addItem(NEW_PROJECT);
		for (int i = 0; i < projects.size(); i++)
			cmbProjectCurrent.addItem(projects.get(i));
		if (current_project != null)
			cmbProjectCurrent.setSelectedItem(current_project);
		
		updateCurrentProject();
		
		// Utilities
		String current = (String)cmbUtilitiesProcess.getSelectedItem();
		cmbUtilitiesProcess.removeAllItems();
		cmbUtilitiesProcess.addItem("Consolidate directories");
		
		if (current != null)
			cmbUtilitiesProcess.setSelectedItem(current);
		else
			cmbUtilitiesProcess.setSelectedItem(0);
		
		cmdUtilitiesExecute.setEnabled(cmbUtilitiesProcess.getSelectedItem() != null);
		
		
			
	}
	
	protected void executeUtility(String process){
		
		if (process.equals("Consolidate directories")){
			InterfaceProject project = current_project;
			if (project == null){
				try{
					project = new InterfaceProject("temp", new File("."), "", "", false);
				}catch (Exception e){
					InterfaceSession.log("InterfaceProjectPanel: Error creating temp project: " + e.getMessage(),
										 LoggingType.Errors);
					return;
					}
				}
			
			ConsolidateDirectoriesDialogBox.showDialog(new ProjectOptions(project));
			
			return;
			}
		
	}
	
	protected void updateCurrentProject(){
		
		Object sel = cmbProjectCurrent.getSelectedItem();
		if (sel == null)
			cmbProjectCurrent.setSelectedItem(NEW_PROJECT);
		if (sel == null)
			return;
		if (sel.equals(NEW_PROJECT)){
			current_project = null;
		}else{
			current_project = (InterfaceProject)sel;
			}
		
		updateControls();
	}
	
	private void updateControls(){
		
		// Update control values & enabled status with current project
		
		if (current_project == null){
			this.cmdProjectCreateEdit.setText("Create");
			
		}else{
			String root = current_project.getRootDir().getAbsolutePath();
			this.txtProjectRoot.setText(root);
			this.cmdProjectCreateEdit.setText("Edit");
		}
			
		
	}
	
	public void actionPerformed(ActionEvent event){
		
		String command = event.getActionCommand();
		
		if (command.equals("Utilities Execute")){
			
			executeUtility((String)cmbUtilitiesProcess.getSelectedItem());
			
			return;
			}
		
		if (command.equals("Create/Edit Project")){
			if (cmdProjectCreateEdit.getText().equals("Create")){
				InterfaceProject new_project = InterfaceProjectDialogBox.showDialog();
				if (new_project == null) return;
				InterfaceSession.getWorkspace().addProject(new_project);
				initCombos();
				cmbProjectCurrent.setSelectedItem(new_project);
				return;
				}
			
			
			
			return;
			}
		
		
		
		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		
		// TODO respond to project change
		if (e.getSource().equals(cmbProjectCurrent)){
			updateCurrentProject();
			return;
			}
		
	}

}