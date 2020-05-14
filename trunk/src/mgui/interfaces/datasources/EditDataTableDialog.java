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
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import mgui.datasources.DataField;
import mgui.datasources.DataTable;
import mgui.datasources.DataType;
import mgui.datasources.DataTypes;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

/**************************************************************
 * Interface allowing the user to create a new table, or edit an existing one, with defined fields.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class EditDataTableDialog extends InterfaceOptionsDialogBox {

	JLabel lblName = new JLabel("Table Name:");
	JTextField txtName = new JTextField();
	JLabel lblFields = new JLabel("Fields:");
	JTable fields_table;
	FieldsTableModel fields_model;
	JScrollPane scrFields;
	
	JButton cmdAddField = new JButton("Add Field");
	JButton cmdRemField = new JButton("Remove Field");
	
	DataTable data_table;
	boolean exception = false;
	boolean is_edit = false;
	
	public HashMap<String,String> changed_names;
	
	public EditDataTableDialog(JFrame frame, DataTableOptions options){
		super(frame, options);
		_init();
	}
	
	
	private void _init(){
		this.setButtonType(BT_OK_CANCEL);
		init();
		
		cmdAddField.setActionCommand("Add Field");
		cmdAddField.addActionListener(this);
		cmdRemField.setActionCommand("Remove Field");
		cmdRemField.addActionListener(this);
		
		DataTableOptions _options = (DataTableOptions)this.options;
		
		initTable();
		
		String source_name = "?";
		if (_options.source != null)
			source_name = _options.source.getName();
		if (_options.table == null){
			setTitle("Create New Data Table for '" + source_name + "'.");
			is_edit = false;
		}else{
			setTitle("Edit Data Table '" + _options.table.getName() + "'.");
			txtName.setText(_options.table.getName());
			is_edit = true;
			}
		
		LineLayout layout = new LineLayout(20, 5, 0);
		setMainLayout(layout);
		setDialogSize(600, 400);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.25, 1);
		mainPanel.add(lblName, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.6, 1);
		mainPanel.add(txtName, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.5, 1);
		mainPanel.add(lblFields, c);
		c = new LineLayoutConstraints(3, 9, 0.05, 0.9, 1);
		mainPanel.add(scrFields, c);
		c = new LineLayoutConstraints(10, 10, 0.05, 0.43, 1);
		mainPanel.add(cmdAddField, c);
		c = new LineLayoutConstraints(10, 10, 0.52, 0.43, 1);
		mainPanel.add(cmdRemField, c);
		
	}
	
	void initTable(){
		
		DataTableOptions _options = (DataTableOptions)this.options;
		if (_options.table == null){
			fields_model = new FieldsTableModel();
		}else{
			fields_model = new FieldsTableModel(_options.table);
			}
		fields_table = new JTable(fields_model);
		fields_table.getColumnModel().getColumn(2).setMaxWidth(70);
		fields_table.getColumnModel().getColumn(3).setMaxWidth(40);
		fields_table.getColumnModel().getColumn(4).setMaxWidth(40);
		fields_table.getColumnModel().getColumn(5).setMaxWidth(40);
		
		//Combo box
		TableColumn column = fields_table.getColumnModel().getColumn(1);
		
		ArrayList<DataType> types = DataTypes.getDataTypeList();
		JComboBox box = new JComboBox();
		for (int i = 0; i < types.size(); i++){
			box.addItem(types.get(i));
			}
		
		column.setCellEditor(new DefaultCellEditor(box));
		
		scrFields = new JScrollPane(fields_table);
		
	}
	
	public boolean hasException(){
		return exception;
	}
	
	public DataTable getDataTable(){
		return data_table;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Add Field")){
			String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), "Name for field:");
			if (name == null) return;
			DataField field = new DataField(name, Types.CHAR, 50);
			fields_model.addField(field);
			return;
			}
		
		if (e.getActionCommand().equals("Remove Field")){
			int row = fields_table.getSelectedRow();
			fields_model.removeField(row);
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			// TODO: If this is an edit, generate SQL commands for field changes,
			// rather than creating a new table, which destroys data
			
			// Create table (update table if this is an edit)
			this.data_table = new DataTable(txtName.getText());
			if (changed_names != null)
				changed_names.clear();
			else
				changed_names = new HashMap<String,String>();
			for (int i = 0; i < fields_model.fields.size(); i++){
				DataField field = fields_model.fields.get(i);
				data_table.addField(field);
				if (!field.getName().equals(fields_model.original_names.get(i)))
					changed_names.put(field.getName(), fields_model.original_names.get(i));
				}
			
			this.setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
	}
	
	public static DataTable showDialog(JFrame frame, DataTableOptions options){
		
		EditDataTableDialog dialog = new EditDataTableDialog(InterfaceSession.getSessionFrame(), 
															 options);
	
		dialog.setVisible(true);
		return dialog.getDataTable();
		
	}
	
	public static DataTable showDialog(JFrame frame, DataTableOptions options, HashMap<String,String> changed_names){
		
		EditDataTableDialog dialog = new EditDataTableDialog(InterfaceSession.getSessionFrame(), 
				 											 options);
		dialog.changed_names = changed_names;

		dialog.setVisible(true);
		return dialog.getDataTable();
		
	}
	
	class FieldsTableModel extends AbstractTableModel{

		
		private ArrayList<DataField> fields = new ArrayList<DataField>();
		public ArrayList<String> original_names = new ArrayList<String>();
		
		public FieldsTableModel(){
			
		}
		
		public FieldsTableModel(DataTable table){
			
			ArrayList<DataField> _fields = table.getFieldList();
			
			for (int i = 0; i < _fields.size(); i++){
				DataField field = _fields.get(i);
				fields.add((DataField)field.clone());
				}
			
			this.fireTableDataChanged();
		}
		
		public void addField(DataField field){
			fields.add(field);
			original_names.add(field.getName());
			this.fireTableDataChanged();
		}
		
		public void removeField(int i){
			fields.remove(i);
			original_names.remove(i);
			this.fireTableDataChanged();
		}
		
		@Override
		public boolean isCellEditable(int row, int col) {
			
			if (col > 3 && fields.get(row).isKeyField()) return false;
			
			return true;
		}
		
		@Override
		public int getRowCount() {
			return fields.size();
		}

		@Override
		public int getColumnCount() {
			return 6;
		}

		@Override
		public Object getValueAt(int row, int col) {
			switch(col){
				case 0:
					return fields.get(row).getName();
				case 1:
					return DataTypes.getTypeForSQL(fields.get(row).getDataType());
				case 2:
					return fields.get(row).getLength();
				case 3:
					return fields.get(row).isKeyField();
				case 4:
					return fields.get(row).isRequired();
				case 5:
					return fields.get(row).isUnique();
				}
			return null;
		}
		
		@Override
		public void setValueAt(Object value, int row, int col) {
			
			switch(col){
				case 0:
					fields.get(row).setName((String)value);
					break;
				case 1:
					fields.get(row).setDataType(((DataType)value).sqlVal);
					break;
				case 2:
					fields.get(row).setLength(Integer.valueOf((String)value));
					break;
				case 3:
					fields.get(row).setIsKeyField((Boolean)value);
					break;
				case 4:
					fields.get(row).setIsRequired((Boolean)value);
					break;
				case 5:
					fields.get(row).setIsUnique((Boolean)value);
					break;
				}
			this.fireTableDataChanged();
		}

		@Override
		public String getColumnName(int column) {
			switch(column){
				case 0:
					return "Name";
				case 1:
					return "Data Type";
				case 2:
					return "Length";
				case 3:
					return "IsKey";
				case 4:
					return "IsReq";
				case 5:
					return "IsUniq";
				}
			return "?";
		}

		@Override
		public Class<?> getColumnClass(int column) {
			if (column == 1) return DataType.class;
			if (column < 3)
				return String.class;
			return Boolean.class;
		}
		
	}
	
}