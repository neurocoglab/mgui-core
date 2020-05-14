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

package mgui.interfaces.datasources;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import mgui.datasources.DataField;
import mgui.datasources.DataType;
import mgui.datasources.DataTypes;
import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

/**************************************************
 * Dialog box to define a data field.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class EditDataFieldDialogBox extends InterfaceOptionsDialogBox {

	JLabel lblName = new JLabel("Name:");
	JTextField txtName = new JTextField();
	JLabel lblDataType = new JLabel("Data type:");
	InterfaceComboBox cmbDataType = new InterfaceComboBox(RenderMode.LongestItem, false, 200);
	JLabel lblLength = new JLabel("Length:");
	JTextField txtLength = new JTextField("0");
	JCheckBox chkIsKey = new JCheckBox(" Is key");
	JCheckBox chkIsUnique = new JCheckBox(" Is unique");
	JCheckBox chkIsRequired = new JCheckBox(" Value is required");
	
	DataField data_field;
	
	public EditDataFieldDialogBox(JFrame aFrame, DataFieldOptions options){
		super(aFrame, options);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		
		_init();
		setLocationRelativeTo(aFrame);
		this.setLocation(300, 370);
	}
	
	protected void _init(){
		super.init();
		
		DataFieldOptions _options = (DataFieldOptions)options;
		this.setTitle("Define Data Field for Table '" + _options.parent_table + "'");
		
		LineLayout lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(600,300);
		
		chkIsKey.addActionListener(this);
		chkIsKey.setActionCommand("Options changed");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.25, 1);
		mainPanel.add(lblName, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.6, 1);
		mainPanel.add(txtName, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.25, 1);
		mainPanel.add(lblDataType, c);
		c = new LineLayoutConstraints(2, 2, 0.3, 0.6, 1);
		mainPanel.add(cmbDataType, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.25, 1);
		mainPanel.add(lblLength, c);
		c = new LineLayoutConstraints(3, 3, 0.3, 0.6, 1);
		mainPanel.add(txtLength, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.45, 1);
		mainPanel.add(chkIsKey, c);
		c = new LineLayoutConstraints(4, 4, 0.5, 0.45, 1);
		mainPanel.add(chkIsRequired, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.25, 1);
		mainPanel.add(chkIsUnique, c);
		
		initDataTypes();
		updateDialog();
		
	}
	
	@Override
	public boolean updateDialog(){
		
		DataFieldOptions _options = (DataFieldOptions)options;
		if (_options.data_field == null){
			txtName.setText("?");
			cmbDataType.setSelectedIndex(0);
			txtLength.setText("0");
			chkIsKey.setSelected(false);
			chkIsUnique.setSelected(false);
			chkIsRequired.setSelected(false);
			return true;
			}
		
		txtName.setText(_options.data_field.getName());
		cmbDataType.setSelectedItem(DataTypes.getTypeForSQL(_options.data_field.getDataType()));
		txtLength.setText("" + _options.data_field.getLength());
		chkIsKey.setSelected(_options.data_field.isKeyField());
		chkIsUnique.setSelected(_options.data_field.isUnique());
		chkIsRequired.setSelected(_options.data_field.isRequired());
		return true;
		
	}
	
	@Override
	public void actionPerformed(ActionEvent event){
		
		if (event.getActionCommand().equals("Options changed")){
			
			if (chkIsKey.isSelected()){
				chkIsRequired.setSelected(true);
				chkIsUnique.setSelected(true);
				chkIsRequired.setEnabled(false);
				chkIsUnique.setEnabled(false);
				return;
				}
			
			chkIsRequired.setEnabled(true);
			chkIsUnique.setEnabled(true);
				
			return;
			}
		
		if (event.getActionCommand().equals(DLG_CMD_OK)){
			DataFieldOptions _options = (DataFieldOptions)options;
			
			String name = txtName.getText();
			if (name == null || name.length() == 0 || name.equals("?")){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Field must have a valid name!", 
											  "Create/Edit Data Field", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			DataType type = (DataType)cmbDataType.getSelectedItem();
			if (type == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
						  "No data type specified!", 
						  "Create/Edit Data Field", 
						  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			int length = 0;
			try{
				length = Integer.valueOf(txtLength.getText());
			}catch (NumberFormatException ex){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
						  "Invalid length: " + txtLength.getText(), 
						  "Create/Edit Data Field", 
						  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			_options.data_field = new DataField(name, type.sqlVal, length);
			_options.data_field.setIsKeyField(chkIsKey.isSelected());
			_options.data_field.setIsUnique(chkIsUnique.isSelected());
			_options.data_field.setIsRequired(chkIsRequired.isSelected());
			
			this.setVisible(false);
			return;
			}
		
		super.actionPerformed(event);
	}
	
	public static DataField showDialog(JFrame frame, DataFieldOptions options){
		
		EditDataFieldDialogBox dialog = new EditDataFieldDialogBox(InterfaceSession.getSessionFrame(), 
															 	   options);
	
		dialog.setVisible(true);
		return options.data_field;
		
	}
	
	public DataField getDataField(){
		return this.data_field;
	}
	
	private void initDataTypes(){
		
		ArrayList<DataType> types = DataTypes.getDataTypeList();
		cmbDataType.removeAllItems();
		for (int i = 0; i < types.size(); i++){
			cmbDataType.addItem(types.get(i));
			}
		
	}
	
}