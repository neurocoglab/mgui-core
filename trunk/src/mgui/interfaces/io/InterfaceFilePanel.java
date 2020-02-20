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

package mgui.interfaces.io;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceDialogUpdater;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.logs.LoggingType;
import mgui.io.FileLoader;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;

/***************************************************************
 * {@link InterfacePanel} providing a standard interface for input & output operations.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceFilePanel extends InterfacePanel implements ActionListener,
																  InterfaceDialogUpdater,
																  InterfaceIOPanel{
	
	//constants
	public HashMap<String, InterfaceIOType> ioTypes = InterfaceEnvironment.getIOTypes();
	InterfaceIOType currentInputType, currentOutputType;
	InterfaceIOOptions currentInputOptions, currentOutputOptions;
	InterfaceIODialogBox currentInputDialog, currentOutputDialog;
	
	//controls
	
	CategoryTitle lblInputHeader = new CategoryTitle("INPUT"); 
	JLabel lblInputFile = new JLabel("Input file(s):"); 
	JTextField txtInputFile = new JTextField("");
	JButton cmdInputFile = new JButton("Browse..");
	JLabel lblInputFileType = new JLabel("File type:"); 
	InterfaceComboBox cmbInputFileType = new InterfaceComboBox(RenderMode.LongestItem, true, 500);
	JButton cmdInputOptions = new JButton("Options..");
	JButton cmdExecuteInput = new JButton("Read Data");
	
	CategoryTitle lblOutputHeader = new CategoryTitle("OUTPUT"); 
	JLabel lblOutputFile = new JLabel("Output file(s):"); 
	JTextField txtOutputFile = new JTextField("");
	JButton cmdOutputFile = new JButton("Browse..");
	JLabel lblOutputFileType = new JLabel("File type:"); 
	InterfaceComboBox cmbOutputFileType = new InterfaceComboBox(RenderMode.LongestItem, true, 500);
	JButton cmdOutputOptions = new JButton("Options..");
	JButton cmdExecuteOutput = new JButton("Write Data");
	
	//Files
	File[] inputFiles;
	File[] outputFiles;
	
	//constants
	public enum Command {
		Input ("Input Data"),
		Output ("Output Data"),
		Input_opt ("Input Options"),
		Output_opt ("Output Options"),
		Input_file ("Input File"),
		Input_file_type ("Input File Type"),
		Output_file ("Output File"),
		Output_file_type ("Output File Type"),
		Apply_Input_Opt ("Apply Input Options"),
		Apply_Output_Opt ("Apply Output Options"),
		Cancel_Input_Opt ("Cancel Input Options"),
		Cancel_Output_Opt ("Cancel Output Options");
		
		public String type;
		
		Command (String ctype){
			type = ctype;
		}
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceFilePanel.class.getResource("/mgui/resources/icons/disk_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/disk_20.png");
		return null;
	}
	
	public InterfaceFilePanel(){
		if (InterfaceSession.isInit())
			init();
	}
	
	public void updateFromDialog(InterfaceDialogBox box) {
		// TODO Auto-generated method stub
		
	}
	
	protected void init(){
		_init();
		
		setLayout(new CategoryLayout(InterfaceEnvironment.getLineHeight(), 5, 200, 10));
		
		lblInputHeader.setHorizontalAlignment(JLabel.CENTER);
		lblOutputHeader.setHorizontalAlignment(JLabel.CENTER);
		
		cmdExecuteInput.setActionCommand(Command.Input.type);
		cmdExecuteInput.addActionListener(this);
		cmdExecuteOutput.setActionCommand(Command.Output.type);
		cmdExecuteOutput.addActionListener(this);
		cmdInputOptions.setActionCommand(Command.Input_opt.type);
		cmdInputOptions.addActionListener(this);
		cmdOutputOptions.setActionCommand(Command.Output_opt.type);
		cmdOutputOptions.addActionListener(this);
		cmdInputFile.setActionCommand(Command.Input_file.type);
		cmdInputFile.addActionListener(this);
		cmdOutputFile.setActionCommand(Command.Output_file.type);
		cmdOutputFile.addActionListener(this);
		cmbInputFileType.setActionCommand(Command.Input_file_type.type);
		cmbInputFileType.addActionListener(this);
		cmbOutputFileType.setActionCommand(Command.Output_file_type.type);
		cmbOutputFileType.addActionListener(this);
		txtInputFile.setEditable(false);
		txtOutputFile.setEditable(false);
		
		fillIOTypeCombos();
		
		currentInputType = (InterfaceIOType)cmbInputFileType.getSelectedItem();
		currentOutputType = (InterfaceIOType)cmbOutputFileType.getSelectedItem();
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		lblInputHeader.isExpanded = true;
		lblInputHeader.setParentObj(this);
		
		add(lblInputHeader, c);
		c = new CategoryLayoutConstraints("INPUT", 1, 1, 0.05, 0.28, 1);
		add(lblInputFileType, c);
		c = new CategoryLayoutConstraints("INPUT", 2, 2, 0.05, 0.9, 1);
		add(cmbInputFileType, c);
		c = new CategoryLayoutConstraints("INPUT", 3, 3, 0.05, 0.9, 1);
		add(lblInputFile, c);
		c = new CategoryLayoutConstraints("INPUT", 4, 4, 0.05, 0.9, 1);
		add(txtInputFile, c);
		c = new CategoryLayoutConstraints("INPUT", 5, 5, 0.15, 0.7, 1);
		add(cmdInputFile, c);
		c = new CategoryLayoutConstraints("INPUT", 6, 6, 0.15, 0.7, 1);
		add(cmdInputOptions, c);
		c = new CategoryLayoutConstraints("INPUT", 8, 9, 0.15, 0.7, 1);
		add(cmdExecuteInput, c);
	
		c = new CategoryLayoutConstraints();
		lblOutputHeader.isExpanded = true;
		lblOutputHeader.setParentObj(this);
		add(lblOutputHeader, c);
		c = new CategoryLayoutConstraints("OUTPUT", 1, 1, 0.05, 0.28, 1);
		add(lblOutputFileType, c);
		c = new CategoryLayoutConstraints("OUTPUT", 2, 2, 0.05, 0.9, 1);
		add(cmbOutputFileType, c);
		c = new CategoryLayoutConstraints("OUTPUT", 3, 3, 0.05, 0.9, 1);
		add(lblOutputFile, c);
		c = new CategoryLayoutConstraints("OUTPUT", 4, 4, 0.05, 0.9, 1);
		add(txtOutputFile, c);
		c = new CategoryLayoutConstraints("OUTPUT", 5, 5, 0.15, 0.7, 1);
		add(cmdOutputFile, c);
		c = new CategoryLayoutConstraints("OUTPUT", 6, 6, 0.15, 0.7, 1);
		add(cmdOutputOptions, c);
		c = new CategoryLayoutConstraints("OUTPUT", 8, 9, 0.15, 0.7, 1);
		add(cmdExecuteOutput, c);
		
		updateDisplay();
	}
	
	void fillIOTypeCombos(){
		
		ArrayList<String> keys = new ArrayList<String>(ioTypes.keySet());
		Collections.sort(keys);
		
		ArrayList<InterfaceIOType> types = new ArrayList<InterfaceIOType>(ioTypes.values());
		Collections.sort(types, new Comparator<InterfaceIOType>(){
			public int compare(InterfaceIOType t1, InterfaceIOType t2){
				return t1.label.compareTo(t2.label);
			}
		});
		
		for (int i = 0; i < types.size(); i++){
			if (types.get(i).type == InterfaceIOType.TYPE_INPUT)
				cmbInputFileType.addItem(types.get(i));
			if (types.get(i).type == InterfaceIOType.TYPE_OUTPUT)
				cmbOutputFileType.addItem(types.get(i));
			}
		
		if (cmbInputFileType.getItemCount() > 0)
			cmbInputFileType.setSelectedIndex(0);
		if (cmbOutputFileType.getItemCount() > 0)
			cmbOutputFileType.setSelectedIndex(0);
		
	}
	
	public boolean setParameters(InterfaceOptions p, int code) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (Command.Input_file.type.equals(e.getActionCommand())){
			if (currentInputOptions == null) return;
			
			JFileChooser fc;
			if (inputFiles != null)
				fc = currentInputOptions.getFileChooser(inputFiles[0]);
			else
				fc = currentInputOptions.getFileChooser();
			
			if (currentInputType.filter != null)
				fc.setFileFilter(currentInputType.filter);
			
			if (fc.isMultiSelectionEnabled()){
				if (fc.showOpenDialog(InterfaceSession.getSessionFrame()) == JFileChooser.APPROVE_OPTION){
					inputFiles = fc.getSelectedFiles();
					updateInputFiles();
					}
			}else{
				if (fc.showOpenDialog(InterfaceSession.getSessionFrame()) == JFileChooser.APPROVE_OPTION){
					inputFiles = new File[1];
					inputFiles[0] = fc.getSelectedFile();
					updateInputFiles();
					}
				}
				
			}
		
		if (Command.Output_file.type.equals(e.getActionCommand())){
			if (currentOutputOptions == null) return;
			JFileChooser fc;
			
			if (outputFiles != null && outputFiles.length > 0)
				fc = currentOutputOptions.getFileChooser(outputFiles[0]);
			else
				fc = currentOutputOptions.getFileChooser();
			
			if (currentOutputType.filter != null)
				fc.setFileFilter(currentOutputType.filter);
			
			if (fc.isDirectorySelectionEnabled()){
				if (fc.showSaveDialog(InterfaceSession.getSessionFrame()) == JFileChooser.APPROVE_OPTION){
					outputFiles = new File[1];
					outputFiles[0] = fc.getSelectedFile();
					}
				updateOutputFiles();
				return;
				}
			
			
			if (fc.isMultiSelectionEnabled()){
				if (fc.showSaveDialog(InterfaceSession.getSessionFrame()) == JFileChooser.APPROVE_OPTION){
					outputFiles = fc.getSelectedFiles();
					if (outputFiles == null || outputFiles.length == 0){
						outputFiles = new File[1];
						outputFiles[0] = fc.getSelectedFile();
						}
					updateOutputFiles();
					}
			}else{
				if (fc.showSaveDialog(InterfaceSession.getSessionFrame()) == JFileChooser.APPROVE_OPTION){
					outputFiles = new File[1];
					outputFiles[0] = fc.getSelectedFile();
					updateOutputFiles();
					}
				}
			return;
			}
		
		if (Command.Input_file_type.type.equals(e.getActionCommand())){
			currentInputType = (InterfaceIOType)cmbInputFileType.getSelectedItem();
			if (currentInputType == null) return;
			txtInputFile.setText("");
			currentInputOptions = currentInputType.getOptionsInstance();
			currentInputDialog = currentInputType.getDialogInstance(InterfaceSession.getSessionFrame(), this, currentInputOptions);
		}
		
		if (Command.Output_file_type.type.equals(e.getActionCommand())){
			currentOutputType = (InterfaceIOType)cmbOutputFileType.getSelectedItem();
			if (currentOutputType == null) return;
			txtOutputFile.setText("");
			currentOutputOptions = currentOutputType.getOptionsInstance();
			currentOutputDialog = currentOutputType.getDialogInstance(InterfaceSession.getSessionFrame(), this, currentOutputOptions);
		}
		
		if (Command.Input.type.equals(e.getActionCommand())){
			//input data using these settings
			if (currentInputType == null){
				InterfaceSession.log("Load error: Input type = null");
				return;
			}
			FileLoader loader = (FileLoader)currentInputType.getIOInstance();
			if (loader == null){
				InterfaceSession.log("InterfaceFilePanel: Load error: Loader = null",
									 LoggingType.Errors);
				return;
			}
			
			InterfaceProgressBar progress_bar = new InterfaceProgressBar(loader.getProgressMessage());
			
			progress_bar.register();
			if (loader.load(currentInputOptions, progress_bar)){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  loader.getSuccessMessage(), 
											  loader.getTitle(), 
											  JOptionPane.INFORMATION_MESSAGE);
			}else{
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  loader.getFailureMessage(), 
											  loader.getTitle(), 
											  JOptionPane.INFORMATION_MESSAGE);
				}
			
			//ensure progress bar has been deregistered
			if (progress_bar.isRegistered()) progress_bar.deregister();
		}
		
		if (Command.Output.type.equals(e.getActionCommand())){
			//output data using these settings
			if (currentOutputType == null) return;
			FileWriter writer = (FileWriter)currentOutputType.getIOInstance();
			if (writer == null) return;
			
			//TODO: implement progress bar for writers
			InterfaceProgressBar progress_bar = new InterfaceProgressBar(writer.getProgressMessage());
			
			progress_bar.register();
			
			if (writer.write(currentOutputOptions, progress_bar))
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  writer.getSuccessMessage(), 
											  writer.getTitle(), 
											  JOptionPane.INFORMATION_MESSAGE);
			else
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  writer.getFailureMessage(), 
											  writer.getTitle(), 
											  JOptionPane.ERROR_MESSAGE);
			
			//ensure progress bar has been deregistered
			if (progress_bar.isRegistered()) progress_bar.deregister();
			}
		
		if (Command.Input_opt.type.equals(e.getActionCommand())){
			
			currentInputDialog = currentInputType.getDialogInstance(InterfaceSession.getSessionFrame(), this, currentInputOptions);
			
			if (currentInputDialog != null)
				currentInputDialog.showDialog();
			
			}
		
		if (Command.Output_opt.type.equals(e.getActionCommand())){
		
			currentOutputDialog = currentOutputType.getDialogInstance(InterfaceSession.getSessionFrame(), this, currentOutputOptions);

			if (currentOutputDialog != null)
				currentOutputDialog.showDialog();
			
			}
		
		if (Command.Cancel_Input_Opt.type.equals(e.getActionCommand())){
			
			}
		
		if (Command.Cancel_Output_Opt.type.equals(e.getActionCommand())){
			
			}
		
		if (Command.Apply_Input_Opt.type.equals(e.getActionCommand())){
			
			}
		
		if (Command.Apply_Output_Opt.type.equals(e.getActionCommand())){
		
			
			}
		
	}
	
	public void setInputFiles(File[] files){
		inputFiles = files;
		updateInputFiles();
	}
	
	public void setOutputFiles(File[] files){
		outputFiles = files;
		updateOutputFiles();
	}

	private void updateInputFiles(){
		if (inputFiles != null){
			String fileStr = "";
			for (int i = 0; i < inputFiles.length; i++)
				if (inputFiles[i] != null && inputFiles[i].exists()){
					if (i != 0) fileStr += ";";
					fileStr += inputFiles[i].getName();
					if (currentInputOptions != null)
						currentInputOptions.setFiles(inputFiles);
					}
			txtInputFile.setText(fileStr);
			}
		else
			txtInputFile.setText("");
	}
	
	private void updateOutputFiles(){
		if (outputFiles != null){
			String fileStr = "";
			for (int i = 0; i < outputFiles.length; i++)
				if (outputFiles[i] != null){
					if (i != 0) fileStr += ";";
					fileStr += outputFiles[i].getName();
					if (currentOutputOptions != null)
						currentOutputOptions.setFiles(outputFiles);
					}
			txtOutputFile.setText(fileStr);
			}
		else
			txtOutputFile.setText("");
	}
	
	@Override
	public void showPanel(){
		
	}
	
	@Override
	public void cleanUpPanel(){
		if (currentInputOptions != null)
			currentInputOptions.clean();
		if (currentOutputOptions != null)
			currentOutputOptions.clean();
		clean();
	}
	
	public String toString(){
		return "File Data Panel";
	}
	
}