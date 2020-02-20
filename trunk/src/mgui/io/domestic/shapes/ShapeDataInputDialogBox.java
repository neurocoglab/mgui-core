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

package mgui.io.domestic.shapes;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;

/**********************************************************
 * Dialog box for ShapeDataLoader.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * @see mgui.io.domestic.shapes.ShapeDataLoader
 *
 */
public class ShapeDataInputDialogBox extends InterfaceIODialogBox {

	JLabel lblShape = new JLabel("Load into shape:");
	InterfaceComboBox cmbShape = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
				  									   true, 500, true);
	JCheckBox chkAsOneColumn = new JCheckBox(" Load as one column");
	JCheckBox chkHasHeader = new JCheckBox(" Has header");
	JLabel lblColumns = new JLabel("Columns:");
	
	JLabel lblSingleColumn = new JLabel("Column name:");
	JTextField txtSingleColumn = new JTextField("no-name");
	JLabel lblSingleColumnType = new JLabel("Data type:");
	JComboBox cmbSingleColumnType = new JComboBox();
	
	JScrollPane lstColumns;
	JTable column_table;
	ColumnTableModel table_model;
	
	File[] current_files;
	int[] columns_per_file;
	
	public ShapeDataInputDialogBox(){
		
	}
	
	public ShapeDataInputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super (frame, panel, options);
		_init();
	}
	
	private void _init(){
		init();
		
		chkHasHeader.addActionListener(this);
		chkHasHeader.setActionCommand("Has Header");
		chkAsOneColumn.addActionListener(this);
		chkAsOneColumn.setActionCommand("As One Column");
		
		cmbSingleColumnType.addItem("Double");
		cmbSingleColumnType.addItem("Float");
		cmbSingleColumnType.addItem("Integer");
		cmbSingleColumnType.addItem("Short");
		cmbSingleColumnType.addItem("Boolean");
		
		setDialogSize(400, 450);
		setTitle("Input Shape Node Data Options");
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		
		//add components if necessary
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		mainPanel.add(lblShape, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(cmbShape, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.9, 1);
		mainPanel.add(chkAsOneColumn, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.9, 1);
		mainPanel.add(chkHasHeader, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.9, 1);
		mainPanel.add(lblColumns, c);
		
		c = new LineLayoutConstraints(3, 3, 0.05, 0.4, 1);
		mainPanel.add(lblSingleColumn, c);
		c = new LineLayoutConstraints(3, 3, 0.45, 0.5, 1);
		mainPanel.add(txtSingleColumn, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.4, 1);
		mainPanel.add(lblSingleColumnType, c);
		c = new LineLayoutConstraints(4, 4, 0.45, 0.5, 1);
		mainPanel.add(cmbSingleColumnType, c);
		
		fillShapeCombo();
		updateTable(options.getFiles());
		updateControls();
		
	}
	
	@Override
	public void showDialog(){
		fillShapeCombo();
		File[] files = null;
		if (options.getFiles() != null)
			files = options.getFiles();
		if (files != null) // || file != current_file)
			updateTable(files);
		this.setVisible(true);
	}
	
	void fillShapeCombo(){
		
		InterfaceSession.getWorkspace().populateShapeCombo(cmbShape);
		
	}
	
	void updateTable(File[] files){
		
		if (files == null) return;
		current_files = files;
		
		ArrayList<String> columns = getColumns(files);
		table_model = new ColumnTableModel(columns);
		if (column_table == null){
			column_table = new JTable(table_model);
			lstColumns = new JScrollPane(column_table);
		}else{
			column_table.setModel(table_model);
			}
		TableColumn col = column_table.getColumnModel().getColumn(3);
		JComboBox formats = new JComboBox();
		formats.addItem("Double");
		formats.addItem("Float");
		formats.addItem("Integer");
		formats.addItem("Short");
		formats.addItem("Boolean");
		col.setCellEditor(new DefaultCellEditor(formats));
		
		column_table.getColumnModel().getColumn(0).setMinWidth(25);
		column_table.getColumnModel().getColumn(0).setMaxWidth(30);
		column_table.getColumnModel().getColumn(1).setMinWidth(25);
		column_table.getColumnModel().getColumn(1).setMaxWidth(30);
		
		LineLayoutConstraints c = new LineLayoutConstraints(5, 11, 0.05, 0.9, 1);
		mainPanel.add(lstColumns, c);
		mainPanel.updateUI();
		
	}
	
	ArrayList<String> getColumns(File[] files){
		
		ArrayList<String> columns = new ArrayList<String>();
		this.columns_per_file = new int[files.length];
		
		for (int i = 0; i < files.length; i++){
			ArrayList<String> header = getHeader(files[i]);
			columns.addAll(header);
			columns_per_file[i] = header.size();
			}
		
		return columns;
	}
	
	ArrayList<String> getHeader(File file){
		
		ArrayList<String> header = new ArrayList<String>();
		String filename = file.getName(); 
		if (filename.contains("."))
			filename = filename.substring(0, filename.lastIndexOf("."));
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			
				StringTokenizer tokens = new StringTokenizer(line);
				int t = tokens.countTokens();
				int i = 1;
				while (tokens.hasMoreTokens()){
					String token = tokens.nextToken();
					if (chkHasHeader.isSelected()){
						header.add(filename + "." + token);
					}else if (t < 2){
						header.add(filename);
					}else{
						header.add(filename + ".column" + i++);
						}
					}
				
			reader.close();
			return header;
		}catch (IOException e){
			InterfaceSession.handleException(e, LoggingType.Errors);
			//e.printStackTrace();
			return header;
			}
		
	}
	
	void refreshHeader(){
		//File[] file = null;
		if (options.getFiles() == null) return;
			//file = options.getFiles()[0];
		
		ArrayList<String> columns = getColumns(options.getFiles());
		table_model.updateNames(columns);
		
	}
	
	void updateControls(){
		
		boolean as_one = chkAsOneColumn.isSelected(); 
		
		chkHasHeader.setVisible(!as_one);
		lblColumns.setVisible(!as_one);
		
		if (lstColumns != null){
			lstColumns.setVisible(!as_one);
			//column_table.setVisible(!as_one);
			}
		
		lblSingleColumn.setVisible(as_one);
		txtSingleColumn.setVisible(as_one);
		lblSingleColumnType.setVisible(as_one);
		cmbSingleColumnType.setVisible(as_one);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Has Header")){
			refreshHeader();
			return;
			}
		
		if (e.getActionCommand().equals("As One Column")){
			updateControls();
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			ShapeDataInputOptions _options = (ShapeDataInputOptions)options;
			
			_options.shape = (InterfaceShape)cmbShape.getSelectedItem();
			
			if (chkAsOneColumn.isSelected()){
				_options.as_one_column = true;
				_options.columns = new String[][]{{txtSingleColumn.getText()}};
				_options.formats = new ShapeDataInputOptions.Format[][]{{
						getFormatForString((String)cmbSingleColumnType.getSelectedItem())}};
				_options.skip_header = false;
				_options.load_column = new boolean[][]{{true}};
				this.setVisible(false);
				return;
				}
			
			_options.as_one_column = false;
			ArrayList<String> names = table_model.names;
			_options.columns = new String[current_files.length][];
			_options.formats = new ShapeDataInputOptions.Format[current_files.length][];
			_options.load_column = new boolean[current_files.length][];
			int pos = 0;
			for (int f = 0; f < current_files.length; f++){
				int c = columns_per_file[f];
				_options.columns[f] = new String[c];
				_options.formats[f] = new ShapeDataInputOptions.Format[c];
				_options.load_column[f] = new boolean[c];
				for (int i = 0; i < c; i++){
					_options.columns[f][i] = names.get(pos + i);
					_options.formats[f][i] = table_model.formats.get(pos + i);
					_options.load_column[f][i] = table_model.include.get(pos + i);
					}
				pos += c;
				}
			_options.skip_header = chkHasHeader.isSelected();
			
			this.setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
	}
	
	static String getFormat(ShapeDataInputOptions.Format format){
		switch (format){
			case Double: return "Double";
			case Float: return "Float";
			case Integer: return "Integer";
			case Short: return "Short";
			case Boolean: return "Boolean";
			}
		return null;
	}
	
	static ShapeDataInputOptions.Format getFormatForString(String s){
		if (s.equals("Double")) return ShapeDataInputOptions.Format.Double;
		if (s.equals("Float")) return ShapeDataInputOptions.Format.Float;
		if (s.equals("Integer")) return ShapeDataInputOptions.Format.Integer;
		if (s.equals("Short")) return ShapeDataInputOptions.Format.Short;
		if (s.equals("Boolean")) return ShapeDataInputOptions.Format.Boolean;
		return null;
	}
	
	static class ColumnTableModel extends AbstractTableModel{

		public ArrayList<String> names = new ArrayList<String>();
		public ArrayList<ShapeDataInputOptions.Format> formats = new ArrayList<ShapeDataInputOptions.Format>();
		public ArrayList<Boolean> include = new ArrayList<Boolean>();
		
		public ColumnTableModel(ArrayList<String> names){
			this.names = names;
			for (int i = 0; i < names.size(); i++){
				formats.add(ShapeDataInputOptions.Format.Double);
				include.add(true);
				}
		}
		
		public void updateNames(ArrayList<String> list){
			for (int i = 0; i < names.size(); i++)
				names.set(i, list.get(i));
			this.fireTableDataChanged();
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex){
				case 0: return Boolean.class;
				case 1: return Integer.class;
				case 2: return String.class;
				case 3: return String.class;
				}
			return Object.class;
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex){
				case 0: return "Load";
				case 1: return "Col";
				case 2: return "Name";
				case 3: return "Format";
				}
		return "?";
		}

		@Override
		public int getRowCount() {
			return names.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex){
				case 0: return include.get(rowIndex);
				case 1: return rowIndex + 1;
				case 2: return names.get(rowIndex);
				case 3: return getFormat(formats.get(rowIndex));
				}
			return null;
		}
		
		

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch (columnIndex){
				case 0: return true;
				case 1: return false;
				case 2: return true;
				case 3: return true;
				}
			return false;
		}

		@Override
		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			switch (columnIndex){
				case 0: 
					include.set(rowIndex, (Boolean)value);
					return;
				case 2: 
					names.set(rowIndex, (String)value);
					return;
				case 3: 
					formats.set(rowIndex, getFormatForString((String)value));
					return;
				}
		}
		
		
		
	}
	
	
	
}