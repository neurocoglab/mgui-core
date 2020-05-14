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

package mgui.interfaces.pipelines;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.pipelines.libraries.PipelineProcessLibrary;
import mgui.pipelines.PipelineProcess;
import mgui.pipelines.PipelineProcessInstance;
import mgui.pipelines.TaskParameter;
import mgui.pipelines.TaskParameterInstance;

/*****************************************************
 * Dialog box for specifying a pipeline process instance.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineProcessInstanceDialogBox extends InterfaceOptionsDialogBox {

	JLabel lblLibrary = new JLabel("Library:");
	InterfaceComboBox cmbLibrary = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JLabel lblProcess = new JLabel("Process:");
	InterfaceComboBox cmbProcess = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JButton cmdDefineProcesses = new JButton("Define processes..");
	JCheckBox chkInputNone = new JCheckBox(" No input");
	JCheckBox chkInputPrevious = new JCheckBox(" Input from previous task");
	JCheckBox chkInputFile = new JCheckBox(" Input from file");
	JTextField txtInputFile = new JTextField();
	JButton cmdInputFile = new JButton("Browse..");
	JCheckBox chkInputPrepend = new JCheckBox(" Prepend prefix and subject name");
	
	JCheckBox chkOutputFile = new JCheckBox(" Output to file");
	JTextField txtOutputFile = new JTextField();
	JButton cmdOutputFile = new JButton("Browse..");
	JCheckBox chkOutputPrepend = new JCheckBox(" Prepend prefix and subject name");
	
	JLabel lblParameters = new JLabel("Parameters:");
	JTable tblParameters;
	JScrollPane scrParameters;
	ParameterTableModel parameter_model;
	
	PipelineProcessInstance current_instance;
	
	boolean update_combo = true;
	public boolean is_ok = false;;
	
	public PipelineProcessInstanceDialogBox(){
		super();
	}

	public PipelineProcessInstanceDialogBox(JFrame aFrame, PipelineProcessInstanceOptions options){
		super(aFrame, options);
		init();
	}
	
	public static PipelineProcessInstance showDialog(){
		return showDialog(null);
	}
	
	public static PipelineProcessInstance showDialog(PipelineProcessInstance instance){
		PipelineProcessInstanceOptions options = new PipelineProcessInstanceOptions(instance);
		PipelineProcessInstanceDialogBox dialog = new PipelineProcessInstanceDialogBox(InterfaceSession.getSessionFrame(), options);
		
		dialog.setVisible(true);
		
		if (!dialog.is_ok) return null;
		return options.process_instance;
	}
	
	@Override
	protected void init(){
		buttonType = BT_OK_CANCEL;
		super.init();
		
		this.setTitle("Pipeline Process Instance Settings");
		
		LineLayout layout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		mainPanel.setLayout(layout);
				
		setDialogSize(720, 630);
		
		PipelineProcessInstanceOptions _options = (PipelineProcessInstanceOptions)options;
		current_instance = _options.process_instance;
		
		//set up lists
		updateDialog();
		
		chkInputPrevious.addActionListener(this);
		chkInputPrevious.setActionCommand("Input previous check");
		chkInputNone.addActionListener(this);
		chkInputNone.setActionCommand("Input none");
		chkInputFile.addActionListener(this);
		chkInputFile.setActionCommand("Input file check");
		cmdInputFile.addActionListener(this);
		cmdInputFile.setActionCommand("Input file browse");
		cmdDefineProcesses.addActionListener(this);
		cmdDefineProcesses.setActionCommand("Define processes");
		chkOutputFile.addActionListener(this);
		chkOutputFile.setActionCommand("Output file check");
		cmdOutputFile.addActionListener(this);
		cmdOutputFile.setActionCommand("Output file browse");
		cmbProcess.addActionListener(this);
		cmbProcess.setActionCommand("Process changed");
		cmbLibrary.addActionListener(this);
		cmbLibrary.setActionCommand("Library Changed");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.15, 1);
		mainPanel.add(lblLibrary, c);
		c = new LineLayoutConstraints(1, 1, 0.2, 0.55, 1);
		mainPanel.add(cmbLibrary, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.15, 1);
		mainPanel.add(lblProcess, c);
		c = new LineLayoutConstraints(2, 2, 0.2, 0.55, 1);
		mainPanel.add(cmbProcess, c);
		c = new LineLayoutConstraints(2, 2, 0.75, 0.2, 1);
		mainPanel.add(cmdDefineProcesses, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.9, 1);
		mainPanel.add(chkInputNone, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.9, 1);
		mainPanel.add(chkInputPrevious, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.9, 1);
		mainPanel.add(chkInputFile, c);
		c = new LineLayoutConstraints(6, 6, 0.1, 0.65, 1);
		mainPanel.add(txtInputFile, c);
		c = new LineLayoutConstraints(6, 6, 0.75, 0.2, 1);
		mainPanel.add(cmdInputFile, c);
		c = new LineLayoutConstraints(7, 7, 0.1, 0.8, 1);
		mainPanel.add(chkInputPrepend, c);
		c = new LineLayoutConstraints(8, 8, 0.05, 0.9, 1);
		mainPanel.add(chkOutputFile, c);
		c = new LineLayoutConstraints(9, 9, 0.1, 0.65, 1);
		mainPanel.add(txtOutputFile, c);
		c = new LineLayoutConstraints(9, 9, 0.75, 0.2, 1);
		mainPanel.add(cmdOutputFile, c);
		c = new LineLayoutConstraints(10, 10, 0.1, 0.8, 1);
		mainPanel.add(chkOutputPrepend, c);
		c = new LineLayoutConstraints(11, 11, 0.05, 0.9, 1);
		mainPanel.add(lblParameters, c);
		c = new LineLayoutConstraints(12, 17, 0.05, 0.9, 1);
		mainPanel.add(scrParameters, c);
		layout.setFlexibleComponent(scrParameters);
		
	}
	
	protected void updateLibrariesCombo(){
		update_combo = false;
		
		PipelineProcessLibrary current = (PipelineProcessLibrary)cmbLibrary.getSelectedItem();
		if (current == null && current_instance != null)
			current = current_instance.getProcess().getLibrary();
		
		cmbLibrary.removeAllItems();
		ArrayList<PipelineProcessLibrary> libraries = InterfaceEnvironment.getPipelineProcessLibraries();
		for (int i = 0; i < libraries.size(); i++){
			if (libraries.get(i).getProcesses().size() > 0)
				cmbLibrary.addItem(libraries.get(i));
			}
		
		if (current != null)
			cmbLibrary.setSelectedItem(current);
		else if (libraries.size() > 0)
			cmbLibrary.setSelectedIndex(0);
		
		updateProcessCombo();
			
		update_combo = true;
	}
	
	protected void updateProcessCombo(){
		update_combo = false;
	
		cmbProcess.removeAllItems();
		
		PipelineProcessLibrary library = (PipelineProcessLibrary)cmbLibrary.getSelectedItem();
		if (library == null) return;
		
		ArrayList<PipelineProcess> processes = library.getProcesses();
		
		Collections.sort(processes, new Comparator<PipelineProcess>(){
				public int compare(PipelineProcess p1, PipelineProcess p2){
					return p1.getName().compareTo(p2.getName());
				}
			});
		
		for (int i = 0; i < processes.size(); i++)
			cmbProcess.addItem(processes.get(i));
		
		if (current_instance != null)
			cmbProcess.setSelectedItem(current_instance.getProcess());
		
		if (cmbProcess.getSelectedItem() == null && cmbProcess.getItemCount() > 0)
			cmbProcess.setSelectedIndex(0);
		
		//if (current_instance != null)
			updateCurrentInstance();
		
		update_combo = true;
	}
	
	@Override
	public boolean updateDialog(){
		
		updateLibrariesCombo();
		updateProcessCombo();
		
		//if (current_instance == null) return false;
		
		if (tblParameters == null){
			parameter_model = new ParameterTableModel(current_instance);
			tblParameters = new JTable(parameter_model);
			tblParameters.getColumnModel().getColumn(0).setMaxWidth(50);
			tblParameters.getColumnModel().getColumn(1).setPreferredWidth(100);
			tblParameters.getColumnModel().getColumn(2).setPreferredWidth(400);
			scrParameters = new JScrollPane(tblParameters);
		}else{
			parameter_model.setCurrentInstance(current_instance);
			}
		
		updateControls();
		
		return true;
	}
	
	void updateControls(){
		
		txtInputFile.setEnabled(chkInputFile.isSelected());
		cmdInputFile.setEnabled(chkInputFile.isSelected());
		txtOutputFile.setEnabled(chkOutputFile.isSelected());
		cmdOutputFile.setEnabled(chkOutputFile.isSelected());
		
		
	}
	
	void updateCurrentInstance(){
		
		if (current_instance == null){
		
			PipelineProcess process = getProcess();
			if (process == null) return;
			
			current_instance = process.getInstance(0);
			}
		
		if (!current_instance.hasInput()){
			chkInputNone.setSelected(true);
			chkInputPrevious.setSelected(false);
			chkInputFile.setSelected(false);
		}else{
			chkInputNone.setSelected(false);
			chkInputPrevious.setSelected(current_instance.getInputFile() == null);
			chkInputFile.setSelected(!chkInputPrevious.isSelected());
			}
		if (chkInputFile.isSelected())
			txtInputFile.setText(current_instance.getInputFile());
		chkOutputFile.setSelected(current_instance.getOutputFile() != null);
		if (chkOutputFile.isSelected())
			txtOutputFile.setText(current_instance.getOutputFile());
		chkInputPrepend.setSelected(current_instance.isPrependSubjectInput());
		chkOutputPrepend.setSelected(current_instance.isPrependSubjectOutput());
		
	}
	
	protected PipelineProcess getProcess(){
		
		return (PipelineProcess)cmbProcess.getSelectedItem();
		
	}
	
	protected void setProcessInstance(){
		
		if (chkInputNone.isSelected()){
			current_instance.setHasInput(false);
			current_instance.setInputFile(null);
		}else{
			current_instance.setHasInput(true);
			if (chkInputFile.isSelected())
				current_instance.setInputFile(txtInputFile.getText());
			else
				current_instance.setInputFile(null);
			}
		if (chkOutputFile.isSelected())
			current_instance.setOutputFile(txtOutputFile.getText());
		else
			current_instance.setOutputFile(null);
		current_instance.setPrependSubjectInput(chkInputPrepend.isSelected());
		current_instance.setPrependSubjectOutput(chkOutputPrepend.isSelected());
		
		//parameters
		current_instance.setParameters(parameter_model.process_instance.getParameters());
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals(InterfaceDialogBox.DLG_CMD_OK)){
			
			if (current_instance == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
						"No instance specified!", 
						"Create/Edit Pipeline Process Instance",
						JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			setProcessInstance();
			PipelineProcessInstanceOptions _options = (PipelineProcessInstanceOptions)options;
			_options.process_instance = current_instance;
		
			is_ok = true;
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals("Input previous check")){
			chkInputFile.setSelected(!chkInputPrevious.isSelected());
			chkInputNone.setSelected(false);
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Input file check")){
			chkInputPrevious.setSelected(!chkInputFile.isSelected());
			chkInputNone.setSelected(false);
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Input none")){
			chkInputPrevious.setSelected(!chkInputNone.isSelected());
			chkInputFile.setSelected(false);
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Output file check")){
			
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals("Input file browse")){
			
			JFileChooser jc = null;
			
			if (current_instance.getInputFile() != null)
				jc = new JFileChooser(current_instance.getInputFile());
			else
				jc = new JFileChooser();
			
			jc.setMultiSelectionEnabled(false);
			//PipelineProcessInstanceOptions _options = (PipelineProcessInstanceOptions)options;
			jc.showDialog(InterfaceSession.getSessionFrame(), "Select");
			
			if (jc.getSelectedFile() == null) return;
			
			//current_instance.input_file = jc.getSelectedFile().getAbsolutePath();
			String path = jc.getSelectedFile().getAbsolutePath();
			//if (path.contains(" ")) path = "\"" + path + "\"";
			txtInputFile.setText(path);
			
			return;
			}
		
		if (e.getActionCommand().equals("Output file browse")){
			
			JFileChooser jc = null;
			
			if (current_instance.getOutputFile() != null)
				jc = new JFileChooser(current_instance.getOutputFile());
			else
				jc = new JFileChooser();
			
			jc.setMultiSelectionEnabled(false);
			//PipelineProcessInstanceOptions _options = (PipelineProcessInstanceOptions)options;
			jc.showSaveDialog(InterfaceSession.getSessionFrame());
			
			if (jc.getSelectedFile() == null) return;
			
			String path = jc.getSelectedFile().getAbsolutePath();
			//if (path.contains(" ")) path = "\"" + path + "\"";
			txtOutputFile.setText(path);
			
			return;
			}
		
		if (e.getActionCommand().equals("Process changed")){
			if (!update_combo) return;
			
			current_instance = new PipelineProcessInstance(getProcess(), 0);
			
			updateCurrentInstance();
			updateDialog();
			return;
			}
		
		if (e.getActionCommand().equals("Library Changed")){
			if (!update_combo) return;
			
			updateLibrariesCombo();
			
			
			return;
			}
		
		super.actionPerformed(e);
	}
	
	static class ParameterTableModel extends DefaultTableModel {
		
		PipelineProcessInstance process_instance;
		ArrayList<TaskParameterInstance> parameters;
		
		public ParameterTableModel(PipelineProcessInstance process){
			setCurrentInstance(process);
		}
		
		public void setCurrentInstance(PipelineProcessInstance instance){
			process_instance = instance;
			PipelineProcess p = instance.getProcess();
			ArrayList<TaskParameter> process_params = p.getParameters();
			
			parameters = new ArrayList<TaskParameterInstance>();
			for (int i = 0; i < process_params.size(); i++)
				parameters.add(instance.getParameter(process_params.get(i).name));
			
			this.fireTableDataChanged();
		}
		
		@Override
		public int getRowCount() {
			if (process_instance == null) return 0;
			return process_instance.getParameters().size();
		}
		
		@Override
		public int getColumnCount() {
			return 3;
		}
		 
		@Override
		public Object getValueAt(int row, int column) {
			if (process_instance == null) return 0;
			//ArrayList<TaskParameterInstance> parameters = new ArrayList<TaskParameterInstance>(process_instance.getParameters().values());
			switch (column){
				case 0:
					return parameters.get(row).apply;
				case 1:
					return parameters.get(row).name;
				case 2:
					return parameters.get(row).value;
				}
			 
			return null;
		}
		
		 @Override
		public boolean isCellEditable(int row, int column) {
			 if (column == 1) return false;
			 if (column == 0){
				TaskParameter p = process_instance.getProcess().getParameter((String)getValueAt(row, 1));
				if (p == null) return false;
				if (!p.optional) return false;
			 	}
			 if (column == 2){
				TaskParameter p = process_instance.getProcess().getParameter((String)getValueAt(row, 1));
				return p.has_value; 
			 	}
			 return true;
		 }
		
		@Override
		public void setValueAt(Object value, int row, int column) {
			if (process_instance == null || row >= parameters.size()) return;
			//ArrayList<TaskParameterInstance> parameters = new ArrayList<TaskParameterInstance>(process_instance.getParameters().values());
			switch (column){
				case 0:
					parameters.get(row).apply = (Boolean)value;
					break;
				case 2:
					parameters.get(row).value = (String)value;
					break;
				}
			
		}
		
		@Override
		public Class<?> getColumnClass(int column){
			
			switch (column){
				case 0:
					return Boolean.class;
				case 1:
					return String.class;
				case 2:
					return String.class;
				}
			
			return Object.class;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column){
				case 0:
					return "Apply";
				case 1:
					return "Name";
				case 2:
					return "Value";
				}
			return "?";
		}
		
		
	}
	
	
	
	
	
	
}