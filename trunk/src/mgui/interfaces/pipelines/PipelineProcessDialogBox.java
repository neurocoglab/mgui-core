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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.pipelines.JavaProcess;
import mgui.pipelines.NativeProcess;
import mgui.pipelines.PipelineProcess;
import mgui.pipelines.TaskParameter;

/*******************************************************
 * A dialog box for creating or modifying a {@link PipelineProcess}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineProcessDialogBox extends InterfaceOptionsDialogBox {

	JLabel lblName = new JLabel("Name:");
	JTextField txtName = new JTextField();
	JLabel lblType = new JLabel("Type:");
	JComboBox cmbType = new JComboBox();
	JLabel lblMainClass = new JLabel("Main class:");
	JTextField txtMainClass = new JTextField();
	JLabel lblPath = new JLabel("Path:");
	JTextField txtPath = new JTextField();
	JLabel lblSetChar = new JLabel("Set character:");
	JTextField txtSetChar = new JTextField();
	JLabel lblInputParam = new JLabel("Input parameter:");
	JTextField txtInputParam = new JTextField();
	JLabel lblOutputParam = new JLabel("Output parameter:");
	JTextField txtOutputParam = new JTextField();
	JLabel lblParameters = new JLabel("Parameters:");
	//JButton cmdEnvironment = new JButton("Environment..");
	JLabel lblHelpParam = new JLabel("Help parameter:");
	JTextField txtHelpParam = new JTextField("-help");
	
	JScrollPane scrParameters;
	JTable tblParameters;
	ParameterTableModel parameter_model;
	
	JButton cmdAddParameter = new JButton("Add");
	JButton cmdRemoveParameter = new JButton("Remove");
	JButton cmdParameterUp = new JButton("Move Up");
	JButton cmdParameterDown = new JButton("Move Down");
	
	PipelineProcess current_process;
	
	private boolean doUpdate = true;
	
	public PipelineProcessDialogBox(){
		super();
	}

	public PipelineProcessDialogBox(InterfaceOptions options){
		super(InterfaceSession.getSessionFrame(), options);
		init();
	}
	
	@Override
	protected void init(){
		buttonType = BT_OK_CANCEL;
		super.init();
		
		this.setTitle("Pipeline Process Settings");
		
		PipelineProcessOptions _options = (PipelineProcessOptions)options;
		if (_options.process != null)
			current_process = (PipelineProcess)_options.process.clone();
		
		if (current_process == null)
			current_process = new JavaProcess("", "");
		
		//set up lists
		updateDialog();
		
		LineLayout layout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		mainPanel.setLayout(layout);
		setDialogSize(600, 670);
		
		cmbType.addActionListener(this);
		cmbType.setActionCommand("Type Changed");
		cmdAddParameter.addActionListener(this);
		cmdAddParameter.setActionCommand("Add Parameter");
		cmdRemoveParameter.addActionListener(this);
		cmdRemoveParameter.setActionCommand("Remove Parameter");
		cmdParameterUp.addActionListener(this);
		cmdParameterUp.setActionCommand("Parameter Up");
		cmdParameterDown.addActionListener(this);
		cmdParameterDown.setActionCommand("Parameter Down");
		//cmdEnvironment.setActionCommand("Environment");
		//cmdEnvironment.addActionListener(this);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.3, 1);
		mainPanel.add(lblType, c);
		c = new LineLayoutConstraints(1, 1, 0.35, 0.6, 1);
		mainPanel.add(cmbType, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.3, 1);
		mainPanel.add(lblName, c);
		c = new LineLayoutConstraints(2, 2, 0.35, 0.6, 1);
		mainPanel.add(txtName, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.3, 1);
		mainPanel.add(lblMainClass, c);
		c = new LineLayoutConstraints(3, 3, 0.35, 0.6, 1);
		mainPanel.add(txtMainClass, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.3, 1);
		mainPanel.add(lblInputParam, c);
		c = new LineLayoutConstraints(4, 4, 0.35, 0.6, 1);
		mainPanel.add(txtInputParam, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.3, 1);
		mainPanel.add(lblOutputParam, c);
		c = new LineLayoutConstraints(5, 5, 0.35, 0.6, 1);
		mainPanel.add(txtOutputParam, c);
		c = new LineLayoutConstraints(6, 6, 0.05, 0.3, 1);
		mainPanel.add(lblHelpParam, c);
		c = new LineLayoutConstraints(6, 6, 0.35, 0.6, 1);
		mainPanel.add(txtHelpParam, c);
		c = new LineLayoutConstraints(7, 7, 0.05, 0.3, 1);
		mainPanel.add(lblPath, c);
		c = new LineLayoutConstraints(7, 7, 0.35, 0.6, 1);
		mainPanel.add(txtPath, c);
		c = new LineLayoutConstraints(8, 8, 0.05, 0.3, 1);
		mainPanel.add(lblSetChar, c);
		c = new LineLayoutConstraints(8, 8, 0.35, 0.6, 1);
		mainPanel.add(txtSetChar, c);
		//c = new LineLayoutConstraints(9, 9, 0.05, 0.45, 1);
		//mainPanel.add(cmdEnvironment, c);
		c = new LineLayoutConstraints(9, 9, 0.05, 0.9, 1);
		mainPanel.add(lblParameters, c);
		c = new LineLayoutConstraints(10, 16, 0.05, 0.9, 1);
		mainPanel.add(scrParameters, c);
		layout.setFlexibleComponent(scrParameters);
		c = new LineLayoutConstraints(17, 17, 0.05, 0.44, 1);
		mainPanel.add(cmdParameterUp, c);
		c = new LineLayoutConstraints(17, 17, 0.51, 0.44, 1);
		mainPanel.add(cmdParameterDown, c);
		c = new LineLayoutConstraints(18, 18, 0.05, 0.44, 1);
		mainPanel.add(cmdAddParameter, c);
		c = new LineLayoutConstraints(18, 18, 0.51, 0.44, 1);
		mainPanel.add(cmdRemoveParameter, c);
		
	}
	
	public static PipelineProcess showDialog(PipelineProcessOptions options){
		PipelineProcessDialogBox dialog = new PipelineProcessDialogBox(options);
		
		dialog.setVisible(true);
		return options.process;
	}
	
	public static PipelineProcess showDialog(){
		return showDialog(new PipelineProcessOptions());
	}
	
	@Override
	public boolean updateDialog(){
		
		if (current_process == null) return false;
		
		cmbType.removeAllItems();
		cmbType.addItem("Java Process");
		cmbType.addItem("Native Process");
		
		doUpdate = false;
		String type = "Native Process";
		if (current_process instanceof JavaProcess)
			type = "Java Process";
		
		cmbType.setSelectedItem(type);
		
		if (type.equals("Java Process")){
			//cmdEnvironment.setVisible(false);
			lblMainClass.setText("Main class:");
			lblPath.setVisible(false);
			txtPath.setVisible(false);
			lblSetChar.setVisible(false);
			txtSetChar.setVisible(false);
		}else{
			//cmdEnvironment.setVisible(true);
			lblMainClass.setText("Command:");
			lblPath.setVisible(true);
			txtPath.setVisible(true);
			lblSetChar.setVisible(true);
			txtSetChar.setVisible(true);
			}
		
		doUpdate = true;
		
		this.setTitle(type + " Settings: " + current_process.getName());
		this.setIconImage(((ImageIcon)current_process.getObjectIcon()).getImage());
		
		txtName.setText(current_process.getName());
		txtInputParam.setText(current_process.getInputParameter());
		txtOutputParam.setText(current_process.getOutputParameter());
		txtHelpParam.setText(current_process.getHelpParameter());
		
		if (current_process instanceof JavaProcess){
			txtMainClass.setText(((JavaProcess)current_process).getMainClass());
		}else{
			txtMainClass.setText(((NativeProcess)current_process).getCommand());
			txtPath.setText(((NativeProcess)current_process).getPath());
			txtSetChar.setText(((NativeProcess)current_process).getSetOperator());
			}
			
		if (tblParameters == null){
			parameter_model = new ParameterTableModel(current_process);
			tblParameters = new JTable(parameter_model);
			scrParameters = new JScrollPane(tblParameters);
		}else{
			parameter_model.setCurrentProcess(current_process);
			}
		
		return true;
	}
	
	protected void setProcess(){
		if (current_process == null) return;
		current_process.setName(txtName.getText());
		current_process.setInputParameter(txtInputParam.getText());
		current_process.setOutputParameter(txtOutputParam.getText());
		current_process.setHelpParameter(txtHelpParam.getText());
		if (current_process instanceof JavaProcess){
			((JavaProcess)current_process).setMainClass(txtMainClass.getText());
		}else{
			((NativeProcess)current_process).setCommand(txtMainClass.getText());
			((NativeProcess)current_process).setPath(txtPath.getText());
			((NativeProcess)current_process).setSetOperator(txtSetChar.getText());
			}
		
		
	}
	
	TaskParameter getSelectedParameter(){
		int row = tblParameters.getSelectedRow();
		if (row < 0) return null;
		return current_process.getParameters().get(row);
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().startsWith("Parameter")){
			if (current_process == null || current_process.getParameters().size() < 2) return;
			int size = current_process.getParameters().size();
			TaskParameter parameter = getSelectedParameter();
			int row = tblParameters.getSelectedRow();
			if (row < 0) return;
			if (parameter == null) return;
			int new_row = row;
			
			
			if (e.getActionCommand().endsWith("Up")){
				new_row = row - 1;
				if (new_row < 0) return;
				current_process.removeParameter(parameter.name);
				current_process.insertParameter(parameter, new_row);
				}
			
			if (e.getActionCommand().endsWith("Down")){
				new_row = row + 1;
				if (new_row >= size) return;
				current_process.removeParameter(parameter.name);
				current_process.insertParameter(parameter, new_row);
				}
			
			this.parameter_model.fireTableDataChanged();
			tblParameters.getSelectionModel().setSelectionInterval(new_row, new_row);
			return;
			}
		
		if (e.getActionCommand().equals("Type Changed")){
			if (!doUpdate) return;
			
			setProcess();
			String type = (String)cmbType.getSelectedItem();
			
			if (type.equals("Java Process")){
				if (current_process == null){
					current_process = new JavaProcess("", "");
					}
				if (current_process instanceof NativeProcess){
					current_process = new JavaProcess(current_process);
					}
				lblMainClass.setText("Main class:");
			}else{
				if (current_process == null){
					current_process = new NativeProcess("", "");
					}
				if (current_process instanceof JavaProcess){
					current_process = new NativeProcess(current_process);
					}
				lblMainClass.setText("Command:");
				}
			
			updateDialog();
			return;
			}
		
		
		
		if (e.getActionCommand().equals("Add Parameter")){
			
			parameter_model.process.addParameter(new TaskParameter());
			parameter_model.fireTableDataChanged();
			
			return;
			}
		
		if (e.getActionCommand().equals("Remove Parameter")){
			
			int row = tblParameters.getSelectedRow();
			if (row < 0) return;
			
			String name = (String)parameter_model.getValueAt(row, 0);
			parameter_model.process.removeParameter(name);
			parameter_model.fireTableDataChanged();
			
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			if (current_process == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
						"No process specified!", 
						"Create/Edit Pipeline Process",
						JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			setProcess();
			PipelineProcessOptions _options = (PipelineProcessOptions)options;
			_options.process = current_process;
			
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_CANCEL)){
			PipelineProcessOptions _options = (PipelineProcessOptions)options;
			_options.process = null;
			this.setVisible(false);
			}

	}
	
	
	static class ParameterTableModel extends DefaultTableModel {
		
		PipelineProcess process;
		
		public ParameterTableModel(PipelineProcess process){
			this.process = process;
		}
		
		public void setCurrentProcess(PipelineProcess instance){
			process = instance;
			this.fireTableDataChanged();
		}
		
		@Override
		public int getRowCount() {
			if (process == null) return 0;
			return process.getParameters().size();
		}
		
		@Override
		public int getColumnCount() {
			return 5;
		}
		 
		@Override
		public Object getValueAt(int row, int column) {
			if (process == null) return 0;
			ArrayList<TaskParameter> parameters = process.getParameters();
			
			switch (column){
				case 0:
					return parameters.get(row).name;
				case 1:
					return parameters.get(row).optional;
				case 2:
					return parameters.get(row).use_name;
				case 3:
					return parameters.get(row).has_value;
				case 4:
					return parameters.get(row).default_value;
				}
			 
			return null;
		}
		
		 @Override
		public boolean isCellEditable(int row, int column) {
			 return true;
		 }
		
		@Override
		public void setValueAt(Object value, int row, int column) {
			if (process == null) return;
			ArrayList<TaskParameter> parameters = process.getParameters();
			switch (column){
				case 0:
					parameters.get(row).name = (String)value;
					break;
				case 1:
					parameters.get(row).optional = (Boolean)value;
					break;
				case 2:
					parameters.get(row).use_name = (Boolean)value;
					break;
				case 3:
					parameters.get(row).has_value = (Boolean)value;
					break;
				case 4:
					parameters.get(row).default_value = (String)value;
					break;
				}
			
		}
		
		@Override
		public Class<?> getColumnClass(int column){
			
			switch (column){
				case 0:
					return String.class;
				case 1:
					return Boolean.class;
				case 2:
					return Boolean.class;
				case 3:
					return Boolean.class;
				case 4:
					return String.class;
				}
			
			return Object.class;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column){
				case 0:
					return "Name";
				case 1:
					return "Optional";
				case 2:
					return "Use name";
				case 3:
					return "Has value";
				case 4:
					return "Default value";
				}
			return "?";
		}
	}
	
	
	
}