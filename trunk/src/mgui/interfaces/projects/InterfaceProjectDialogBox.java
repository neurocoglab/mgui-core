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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsTabbedDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBoxRenderer;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.resources.icons.NamedIcon;

/***********************************************************
 * Dialog for setting up or editing an {@linkplain InterfaceProject} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceProjectDialogBox extends InterfaceOptionsTabbedDialogBox {

	//Main tab
	JLabel lblMainName = new JLabel("Name:");
	JTextField txtMainName = new JTextField("Project 1");
	JLabel lblMainRootFolder = new JLabel("Root folder:");
	JTextField txtMainRootFolder = new JTextField();
	JButton cmdMainRootFolder = new JButton("Browse..");
	JLabel lblSubjectPrefix = new JLabel("Prefix:");
	JTextField txtSubjectPrefix = new JTextField("");
	JLabel lblProjectFolder = new JLabel("Project folder:");
	JTextField txtProjectFolder = new JTextField("project");
	JLabel lblInstanceFolder = new JLabel("Instance folder:");
	JTextField txtInstanceFolder = new JTextField("instances");
	JCheckBox chkAllowFSUpdate = new JCheckBox(" Allow file system updates");
	
	// How to obtain subjects?
//	JLabel lblSubjects = new JLabel("Determine subjects from:");
//	JCheckBox chkSubjectsFromDirs = new JCheckBox("Folders");
//	JCheckBox chkSubjectsFromFiles = new JCheckBox("Files; filter:");
//	JTextField txtSubjectsFromFiles = new JTextField("*.*");
	
	JButton cmdMainApply = new JButton("Create");
	JButton cmdMainRevert = new JButton("Revert");
	
	//Instances tab
	JLabel lblSubInstances = new JLabel("Instances in instance folder:");
	JLabel lblIconType = new JLabel("Icon type:");
	InterfaceComboBox cmbIconType;
	
	JList lstSubInstances;
	JScrollPane scrSubInstances;
	JLabel lblSubSubfolders = new JLabel("Subfolders:");
	JList lstSubSubfolders;
	JScrollPane scrSubSubfolders;
	JButton cmdSubAddSubfolder = new JButton("Add");
	JButton cmdSubRemSubfolder = new JButton("Remove");
	JButton cmdSubDefineSubfolder = new JButton("Define..");
	JButton cmdSubAddInstance = new JButton("Add instance");
	JButton cmdSubAddInstanceList = new JButton("Add list");
	JCheckBox chkSubRemFileSystem = new JCheckBox(" Apply removals to file system");
	JButton cmdSubRemInstance = new JButton("Remove selected");
	JButton cmdSubApply = new JButton("Apply");
	JButton cmdSubRevert = new JButton("Revert");
	
	//Processes tab
	
	DefaultListModel<ProjectInstance> instance_model;
	DefaultListModel<ProjectDirectory> subfolder_model;
	
	InterfaceProject current_project;
	
	public InterfaceProjectDialogBox(){
		super();
	}

	public InterfaceProjectDialogBox(JFrame aFrame, InterfaceOptions options){
		super(aFrame, options);
		init();
	}
	
	@Override
	protected void init(){
		buttonType = BT_OK_CANCEL;
		super.init();
		
		InterfaceProjectOptions _options = (InterfaceProjectOptions)options;
		current_project = _options.project;
		
		if (current_project == null)
			this.setTitle("New Project settings");
		else
			this.setTitle("Project '" + _options.project.getName() + "' settings");
		
		cmdMainRootFolder.addActionListener(this);
		cmdMainRootFolder.setActionCommand("Main Set Root");
		cmdMainApply.addActionListener(this);
		cmdMainApply.setActionCommand("Main Apply");
		cmdSubAddInstance.addActionListener(this);
		cmdSubAddInstance.setActionCommand("Instances Add Instance");
		cmdSubRemInstance.addActionListener(this);
		cmdSubRemInstance.setActionCommand("Instances Remove Instance");
		cmdSubAddInstanceList.addActionListener(this);
		cmdSubAddInstanceList.setActionCommand("Instances Add List");
		cmdSubAddSubfolder.addActionListener(this);
		cmdSubAddSubfolder.setActionCommand("Instances Add Subfolder");
		cmdSubRemSubfolder.addActionListener(this);
		cmdSubRemSubfolder.setActionCommand("Instances Remove Subfolder");
		cmdSubDefineSubfolder.addActionListener(this);
		cmdSubDefineSubfolder.setActionCommand("Instances Define Subfolder");
		
		cmdSubRevert.addActionListener(this);
		cmdSubRevert.setActionCommand("Instances Revert");
		cmdSubApply.addActionListener(this);
		cmdSubApply.setActionCommand("Instances Apply");
		
		chkAllowFSUpdate.setSelected(false);
		
		//set up lists
		updateDialog();
		updateIconTypes();
		
		LineLayout layout = new LineLayout(20, 5, 0);
		setDialogSize(700, 600);
		
		JPanel panel = new JPanel();
		panel.setLayout(layout);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.15, 1);
		panel.add(lblMainName, c);
		c = new LineLayoutConstraints(1, 1, 0.2, 0.55, 1);
		panel.add(txtMainName, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.15, 1);
		panel.add(lblMainRootFolder, c);
		c = new LineLayoutConstraints(2, 2, 0.2, 0.55, 1);
		panel.add(txtMainRootFolder, c);
		c = new LineLayoutConstraints(2, 2, 0.75, 0.2, 1);
		panel.add(cmdMainRootFolder, c);
		
		c = new LineLayoutConstraints(4, 4, 0.05, 0.15, 1);
		panel.add(lblProjectFolder, c);
		c = new LineLayoutConstraints(4, 4, 0.2, 0.55, 1);
		panel.add(txtProjectFolder, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.15, 1);
		panel.add(lblInstanceFolder, c);
		c = new LineLayoutConstraints(5, 5, 0.2, 0.55, 1);
		panel.add(txtInstanceFolder, c);
		c = new LineLayoutConstraints(6, 6, 0.05, 0.15, 1);
		panel.add(lblSubjectPrefix, c);
		c = new LineLayoutConstraints(6, 6, 0.2, 0.55, 1);
		panel.add(txtSubjectPrefix, c);
		c = new LineLayoutConstraints(7, 7, 0.05, 0.9, 1);
		panel.add(chkAllowFSUpdate, c);
		c = new LineLayoutConstraints(8, 9, 0.45, 0.24, 1);
		panel.add(cmdMainApply, c);
		c = new LineLayoutConstraints(8, 9, 0.71, 0.24, 1);
		panel.add(cmdMainRevert, c);
		
		addTab("Main", panel);
		
		panel = new JPanel();
		panel.setLayout(layout);
		
		c = new LineLayoutConstraints(1, 1, 0.05, 0.9, 1);
		panel.add(lblSubInstances, c);
		c = new LineLayoutConstraints(2, 6, 0.05, 0.7, 1);
		panel.add(scrSubInstances, c);
		c = new LineLayoutConstraints(7, 7, 0.05, 0.7, 1);
		panel.add(lblSubSubfolders, c);
		c = new LineLayoutConstraints(8, 12, 0.05, 0.7, 1);
		panel.add(scrSubSubfolders, c);
		c = new LineLayoutConstraints(8, 8, 0.75, 0.2, 1);
		panel.add(cmdSubAddSubfolder, c);
		c = new LineLayoutConstraints(9, 9, 0.75, 0.2, 1);
		panel.add(cmdSubRemSubfolder, c);
		c = new LineLayoutConstraints(10, 10, 0.75, 0.2, 1);
		panel.add(cmdSubDefineSubfolder, c);
		c = new LineLayoutConstraints(2, 2, 0.75, 0.2, 1);
		panel.add(cmdSubAddInstance, c);
		c = new LineLayoutConstraints(3, 3, 0.75, 0.2, 1);
		panel.add(cmdSubAddInstanceList, c);
		c = new LineLayoutConstraints(4, 4, 0.75, 0.2, 1);
		panel.add(cmdSubRemInstance, c);
		c = new LineLayoutConstraints(13, 13, 0.05, 0.9, 1);
		panel.add(chkSubRemFileSystem, c);
		c = new LineLayoutConstraints(15, 16, 0.45, 0.24, 1);
		panel.add(cmdSubApply, c);
		c = new LineLayoutConstraints(15, 16, 0.71, 0.24, 1);
		panel.add(cmdSubRevert, c);
		
		addTab("Instance Data", panel);
		
		//TODO: add "Project Data" tab
		
	}
	
	protected void updateIconTypes(){
		cmbIconType = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem,
		 								    true, 500);
		ArrayList<NamedIcon> icons = new ArrayList<NamedIcon>(ProjectInstance.getIconTypes().values());
		NamedIcon selected = null;
		InterfaceProjectOptions _options = (InterfaceProjectOptions)options;
		for (int i = 0; i < icons.size(); i++){
			cmbIconType.addItem(icons.get(i));
			if (_options.icon_type.equals(icons.get(i).getName()))
				selected = icons.get(i);
			}
		
		if (selected != null)
			cmbIconType.setSelectedItem(selected);
	}
	
	public static InterfaceProject showDialog(){
		InterfaceProjectOptions options = new InterfaceProjectOptions();
		InterfaceProjectDialogBox dialog = new InterfaceProjectDialogBox(InterfaceSession.getSessionFrame(), options);
		dialog.setVisible(true);
		return options.project;
	}
	
	public static InterfaceProject showDialog(InterfaceProject project){
		InterfaceProjectOptions options = new InterfaceProjectOptions((InterfaceProject)project.clone());
		InterfaceProjectDialogBox dialog = new InterfaceProjectDialogBox(InterfaceSession.getSessionFrame(), options);
		dialog.setVisible(true);
		return options.project;
	}
	
	@Override
	public boolean updateDialog(){
		
		InterfaceProjectOptions _options = (InterfaceProjectOptions)options;
		
		if (current_project != null){
			txtMainName.setText(current_project.getName());
			txtMainRootFolder.setText(current_project.getRootDir().getAbsolutePath());
			txtSubjectPrefix.setText(current_project.getInstancePrefix());
			txtProjectFolder.setText(current_project.getProjectDir());
			txtInstanceFolder.setText(current_project.getInstanceDir());
			cmdMainApply.setText("Apply");
		}else{
				cmdMainApply.setText("Create");
			}
		
		updateInstances();
		updateSubfolders();
		
		return true;
	}
	
	void updateInstances(){
		
		if (instance_model == null){
			instance_model = new DefaultListModel();
			lstSubInstances = new JList(instance_model);
			scrSubInstances = new JScrollPane(lstSubInstances);
			lstSubInstances.setCellRenderer(new InterfaceComboBoxRenderer());
		}else{	
			instance_model.removeAllElements();
			}
		
		if (current_project != null && current_project.hasInstances()){
			for (int i = 0; i < current_project.getInstances().size(); i++)
				instance_model.addElement(current_project.getInstances().get(i));
			}
		
		lstSubInstances.updateUI();
		
	}
	
	void updateSubfolders(){
		
		if (subfolder_model == null){
			subfolder_model = new DefaultListModel();
			lstSubSubfolders = new JList(subfolder_model);
			scrSubSubfolders = new JScrollPane(lstSubSubfolders);
			lstSubSubfolders.setCellRenderer(new InterfaceComboBoxRenderer(){
				@Override
				public Component getListCellRendererComponent(JList list, 
															  Object value,
															  int index, 
															  boolean isSelected, 
															  boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					setText(((ProjectDirectory)value).getPath());
					return this;
				}
			});
		}else{	
			subfolder_model.removeAllElements();
			}
		
		if (current_project != null){
			for (int i = 0; i < current_project.getSubdirs().size(); i++)
				addSubfolder(current_project.getSubdirs().get(i));
				
			}
		
		lstSubSubfolders.updateUI();
		
	}
	
	private void addSubfolder(ProjectDirectory dir){
		subfolder_model.addElement(dir);
		ArrayList<ProjectDirectory> subdirs = dir.getSubdirectories();
		for (int i = 0; i < subdirs.size(); i++)
			addSubfolder(subdirs.get(i));
	}
	
	public ProjectDirectory getSelectedDirectory(){
		return (ProjectDirectory)lstSubSubfolders.getSelectedValue();
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
	
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			InterfaceProjectOptions _options = (InterfaceProjectOptions)options;
			_options.project = current_project;
			
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().startsWith("Main")){
			
			if (e.getActionCommand().endsWith("Set Root")){
			
				JFileChooser fc = null;
				if (txtMainRootFolder.getText().length() > 0)
					fc = new JFileChooser(txtMainRootFolder.getText());
				else
					fc = new JFileChooser();
				
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setFileFilter(getDirFilter2());
				fc.setMultiSelectionEnabled(false);
				if (fc.showDialog(InterfaceSession.getSessionFrame(), "Accept") != JFileChooser.APPROVE_OPTION) return;
				
				txtMainRootFolder.setText(fc.getSelectedFile().getAbsolutePath());
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Apply")){
				
				if (txtMainName.getText().length() == 0){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No name set!", 
												  "Project dialog error", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				if (txtMainRootFolder.getText().length() == 0){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "No root directory set!", 
												  "Project dialog error", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				File root = new File(txtMainRootFolder.getText());
				String instance_dir = txtInstanceFolder.getText();
				String project_dir = txtProjectFolder.getText();
				
				if (!root.exists() && (!chkAllowFSUpdate.isSelected() || !root.mkdir())){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Invalid root directory!", 
												  "Create New Project", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				if (current_project == null){
					try{
						current_project = new InterfaceProject(txtMainName.getText(), 
															   root, 
															   project_dir, instance_dir,
															   chkAllowFSUpdate.isSelected());
					}catch (ProjectIOException ex){
						//ex.printStackTrace();
						InterfaceSession.log(ex.getMessage());
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Could not create project!", 
													  "Create New Project", 
													  JOptionPane.ERROR_MESSAGE);
						return;
						}
				}else{
					current_project.setRootDir(root);
					current_project.setName(txtMainName.getText());
					current_project.setInstances();
					current_project.setSubdirs();
					}
				
				current_project.setInstancePrefix(txtSubjectPrefix.getText());
				
				updateDialog();
				
				return;
				}
				
			}
		
		if (e.getActionCommand().startsWith("Instances")){
			
			if (e.getActionCommand().endsWith("Add Instance")){
				String subject = JOptionPane.showInputDialog("Enter name for new instance");
				if (subject == null) return;
				
				instance_model.addElement(new ProjectInstance(subject));
				scrSubInstances.updateUI();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove Instance")){
				
				int[] selected = lstSubInstances.getSelectedIndices();
				
				for (int i = selected.length - 1; i >= 0; i--)
					//because removeElement with non-generic Vector is useless
					instance_model.remove(selected[i]);
					
				lstSubInstances.updateUI();
				return;
				}
			
			if (e.getActionCommand().endsWith("Add Subfolder")){
				
				String name = JOptionPane.showInputDialog("Enter name for new subfolder");
				if (name == null) return;
				
				subfolder_model.addElement(new ProjectDirectory(name));
				lstSubSubfolders.updateUI();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove Subfolder")){
				
				int[] selected = lstSubSubfolders.getSelectedIndices();
				
				for (int i = selected.length - 1; i >= 0; i--)
					subfolder_model.remove(selected[i]);
					
				lstSubSubfolders.updateUI();
				return;
				}
			
			if (e.getActionCommand().endsWith("Define Subfolder")){
				//TODO: show define subfolder dialog here
				ProjectDirectory selected = getSelectedDirectory();
				if (selected == null) return;
				ProjectDirectory directory = ProjectDirectoryDialogBox.showDialog(InterfaceSession.getSessionFrame(), 
																		 		  selected);
				if (directory != null)
					selected.setFromDirectory(directory);
				return;
				}
			
			if (e.getActionCommand().endsWith("Add List")){
				JFileChooser fc = new JFileChooser(current_project.getRootDir());
				fc.setDialogTitle("Choose instances list as text file");
				if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
					return;
				File list_file = fc.getSelectedFile();
				if (list_file == null) return;
				ArrayList<String> instances = readInstanceList(list_file);
				if (instances == null) return;
				
				for (int i = 0; i < instances.size(); i++)
					instance_model.addElement(new ProjectInstance(instances.get(i)));
				
				scrSubInstances.updateUI();
				return;
				}
			
			if (e.getActionCommand().endsWith("Revert")){
				updateDialog();
				return;
				}
			
			if (e.getActionCommand().endsWith("Apply")){
				//apply changes to file system
				InterfaceProjectOptions _options = (InterfaceProjectOptions)options;
				_options.instances = new ArrayList<ProjectInstance>();
				_options.subdirs = new ArrayList<ProjectDirectory>();
				
				for (int i = 0; i < instance_model.size(); i++)
					_options.instances.add((ProjectInstance)instance_model.get(i));
				for (int i = 0; i < subfolder_model.size(); i++)
					_options.subdirs.add((ProjectDirectory)subfolder_model.get(i));
				
				current_project.setInstances(_options.instances);
				current_project.setSubdirectories(_options.subdirs);
				
				if (current_project.updateFileSystem(chkSubRemFileSystem.isSelected()))
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "File system updated", 
												  "Update Project File System", 
												  JOptionPane.INFORMATION_MESSAGE);
				else
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Problems encountered in file system update..", 
												  "Update Project File System", 
												  JOptionPane.ERROR_MESSAGE);
				
				//updateDialog();
				
				return;
				}
			
			
			}
		
		
		super.actionPerformed(e);
	}
	
	protected ArrayList<String> readInstanceList(File file){
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			ArrayList<String> instances = new ArrayList<String>();
			
			while (line != null){
				instances.add(line);
				line = reader.readLine();
				}
			
			reader.close();
			return instances;
			
		}catch (IOException ex){
			JOptionPane.showMessageDialog(this, "Error reading file.", "Read instances list", JOptionPane.ERROR_MESSAGE);
			return null;
			}
		
	}
	
	static javax.swing.filechooser.FileFilter getDirFilter2(){
		return new javax.swing.filechooser.FileFilter() {
	        @Override
			public boolean accept(File dir) {
	            return dir.isDirectory();
	        }
	        @Override
			public String getDescription(){
	        	return "Directory";
	        }
		};
	}
	
	static FilenameFilter getDirFilter(){
		return new FilenameFilter() {
	        public boolean accept(File dir, String name) {
	            return new File(dir, name).isDirectory();
	        }
		};
	}
	
}