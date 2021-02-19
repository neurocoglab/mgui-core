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

package mgui.interfaces.projects.util;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;
import mgui.util.StringFunctions;
import foxtrot.Task;
import foxtrot.Worker;

/*********************************************************
 * Defines parameters for a consolidation of directories into one directory. See
 * {@linkplain ProjectFunctions.consolidateDirectories}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ConsolidateDirectoriesDialogBox extends InterfaceOptionsDialogBox {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4525437733636496559L;

	JLabel lblDirectories = new JLabel("Directories to consolidate:");
	JScrollPane scrDirectories;
	JList lstDirectories;
	ListModel list_model;
	JButton cmdAddSource = new JButton("Add..");
	JButton cmdRemoveSource = new JButton("Remove");
	JLabel lblTarget = new JLabel("Target directory:");
	JTextField txtTarget = new JTextField("");
	JButton cmdSetTarget = new JButton("Set..");
	JCheckBox chkRetainOrig = new JCheckBox(" Retain original data");
	JCheckBox chkPrefix = new JCheckBox(" Prefix for top folders:");
	JTextField txtPrefix = new JTextField("{dir}_");
	JCheckBox chkSuffix = new JCheckBox(" Suffix for top folders:");
	JTextField txtSuffix = new JTextField("_{dir}");
	JCheckBox chkOverwrite = new JCheckBox(" Overwrite existing data");
	
	File current_target = null;
	InterfaceProgressBar current_progress = null;
	
	public ConsolidateDirectoriesDialogBox(JFrame frame, InterfaceOptions options){
		super(frame, options);
		_init();
	}
	
	private void _init(){
		super.init();
		
		setTitle("Project Utilities: Consolidate Directories");
		setDialogSize(550, 525);
		LineLayout layout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		setMainLayout(layout);
		
		cmdAddSource.addActionListener(this);
		cmdAddSource.setActionCommand("Add Source");
		cmdRemoveSource.addActionListener(this);
		cmdRemoveSource.setActionCommand("Remove Source");
		cmdSetTarget.addActionListener(this);
		cmdSetTarget.setActionCommand("Set Target");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.9, 1);
		ProjectOptions _options = (ProjectOptions)options;
		this.current_target = _options.project.getRootDir();
		
				
		initList();
		
		mainPanel.add(lblDirectories, c);
		c = new LineLayoutConstraints(2, 8, 0.05, 0.7, 1);
		mainPanel.add(scrDirectories, c);
		c = new LineLayoutConstraints(2, 2, 0.77, 0.18, 1);
		mainPanel.add(cmdAddSource, c);
		c = new LineLayoutConstraints(3, 3, 0.77, 0.18, 1);
		mainPanel.add(cmdRemoveSource, c);
		c = new LineLayoutConstraints(9, 9, 0.05, 0.3, 1);
		mainPanel.add(lblTarget, c);
		c = new LineLayoutConstraints(9, 9, 0.35, 0.4, 1);
		mainPanel.add(txtTarget, c);
		c = new LineLayoutConstraints(9, 9, 0.77, 0.18, 1);
		mainPanel.add(cmdSetTarget, c);
		c = new LineLayoutConstraints(10, 10, 0.05, 1, 1);
		mainPanel.add(chkRetainOrig, c);
		c = new LineLayoutConstraints(11, 11, 0.05, 0.35, 1);
		mainPanel.add(chkPrefix, c);
		c = new LineLayoutConstraints(11, 11, 0.4, 0.35, 1);
		mainPanel.add(txtPrefix, c);
		c = new LineLayoutConstraints(12, 12, 0.05, 0.35, 1);
		mainPanel.add(chkSuffix, c);
		c = new LineLayoutConstraints(12, 12, 0.4, 0.35, 1);
		mainPanel.add(txtSuffix, c);
		layout.setFlexibleComponent(scrDirectories);
		
	}
	
	public static void showDialog(ProjectOptions options){
		
		ConsolidateDirectoriesDialogBox box = new ConsolidateDirectoriesDialogBox(InterfaceSession.getSessionFrame(), options);
		box.setVisible(true);
		
	}
	
	
	private void initList(){
		ProjectOptions _options = (ProjectOptions)options;
		list_model = new ListModel(_options.project.getRootDir());
		lstDirectories = new JList(list_model);
		scrDirectories = new JScrollPane(lstDirectories);
	}
	
	private void updateTarget(){
	
		if (current_target == null)
			txtTarget.setText("~");
		
		ProjectOptions _options = (ProjectOptions)options;
		String full = current_target.getAbsolutePath();
		full = StringFunctions.replaceAll(full, _options.project.getRootDir().getAbsolutePath(), "{root}");
		
		txtTarget.setText(full);
	}
	
	private boolean execute_process(){
		
		final HashMap<String,Object> parameters = getParameters();
		if (parameters == null) return false;
		
		final InterfaceProgressBar progress = new InterfaceProgressBar("Consolidating directories: ");
		current_progress = progress;
		progress.register();
		
		try{

			boolean success = (Boolean)Worker.post(new Task(){
				
				public Boolean run(){
					return ProjectFunctions.consolidateDirectories(parameters, progress);
				}
				
			});
			
			progress.deregister();
			current_progress = null;
			
			return success;
		}catch (Exception ex){
			InterfaceSession.log("Error consolidating directories: " + ex.getMessage(), LoggingType.Errors);
			progress.deregister();
			current_progress = null;
			return false;
			}
		
	}
	
	protected HashMap<String,Object> getParameters(){
		if (current_target == null){
			InterfaceSession.log("Consolidate directories: Target directory not set.", LoggingType.Errors);
			return null;
			}
			
		HashMap<String,Object> parameters = new HashMap<String,Object>();
		parameters.put("directories", list_model.directories);
		parameters.put("target_dir", current_target.getAbsolutePath());
		if (chkRetainOrig.isSelected())
			parameters.put("retain_original", null);
		if (chkPrefix.isSelected())
			parameters.put("top_prefix", txtPrefix.getText());
		if (chkSuffix.isSelected())
			parameters.put("top_suffix", txtSuffix.getText());
		if (chkOverwrite.isSelected())
			parameters.put("clobber", null);
		
		return parameters;
	}
	
	public void actionPerformed(ActionEvent event){
		
		String command = event.getActionCommand();
		
		if (command.equals(DLG_CMD_CANCEL)){
			
			if (current_progress == null){
				// If no process is running, simply close the dialog
				super.actionPerformed(event);
				return;
				}
			
			// If process is running, treat this as a cancel
			current_progress.cancel();
			return;
			}
		
		if (current_progress != null) return;
		
		
		  
		if (command.equals("Add Source")){
			
			JFileChooser fc = null;
			ProjectOptions _options = (ProjectOptions)options;
			
			fc = new JFileChooser(_options.project.getRootDir());
			
			fc.setDialogTitle("Select Source Director(ies)");
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setMultiSelectionEnabled(true);
			if (fc.showDialog(InterfaceSession.getSessionFrame(), "Select") != JFileChooser.APPROVE_OPTION)
				return;
			
			File[] selected = fc.getSelectedFiles();
			if (selected == null || selected.length == 0) return;
				
			for (int i = 0; i < selected.length; i++){
				list_model.addDirectory(selected[i].getAbsolutePath());
				}
			
			return;
			}
		  
		if (command.equals("Remove Source")){
			ProjectOptions _options = (ProjectOptions)options;
			Object[] selected = lstDirectories.getSelectedValues();
			
			for (int i = 0; i < selected.length; i++){
				String name = (String)selected[i];
				name = StringFunctions.replaceAll(name, "{root}", _options.project.getRootDir().getAbsolutePath());
				list_model.removeDirectory(name);
				}
			
			return;
			}
		  
		if (command.equals("Set Target")){
			
			JFileChooser fc = null;
			ProjectOptions _options = (ProjectOptions)options;
			
			if (this.current_target != null)
				fc = new JFileChooser(current_target);
			else
				fc = new JFileChooser(_options.project.getRootDir());
			
			fc.setDialogTitle("Select Target Directory");
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setMultiSelectionEnabled(false);
			if (fc.showDialog(InterfaceSession.getSessionFrame(), "Select") != JFileChooser.APPROVE_OPTION)
				return;
			
			File selected = fc.getSelectedFile();
			if (!selected.isDirectory()) return;
			
			this.current_target = selected;
			updateTarget();
			
			return;
			}
		
		if (command.equals(DLG_CMD_OK)){
			
			// Execute process
			if (execute_process()){
				// Success message
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
						"Success.", 
						"Consolidate Directories", 
						JOptionPane.INFORMATION_MESSAGE);
			}else{
				// Failure message
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
						"Process failed. Check your values and the error log.", 
						"Consolidate Directories", 
						JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			// Done; hide this
			this.setVisible(false);
			return;
			}
		
		
		
		super.actionPerformed(event);
		  
	}
	
	
	class ListModel extends AbstractListModel{
		
		public ArrayList<String> directories = new ArrayList<String>();
		public File root;
		
		public ListModel(File root){
			this.root = root;
		}
		
		public boolean addDirectory(String dir){
			
			File file = new File(dir);
			if (!file.exists() || !file.isDirectory())
				return false;
			
			directories.add(file.getAbsolutePath());
			fireContentsChanged(this, 0, directories.size() - 1);
			
			return true;
		}
		
		public void removeDirectory(String dir){
			for (int i = 0; i < directories.size(); i++)
				if (directories.get(i).equals(dir)){
					directories.remove(i);
					fireContentsChanged(this, 0, directories.size() - 1);
					}
			
		}

		@Override
		public Object getElementAt(int index) {
			String dir = directories.get(index);
			return StringFunctions.replaceAll(dir, root.getAbsolutePath(), "{root}");
		}

		@Override
		public int getSize() {
			return directories.size();
		}

	}
	
}