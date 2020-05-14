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

package mgui.io.domestic.variables;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.variables.MatrixInOptions.Format;
import mgui.io.domestic.variables.MatrixInOptions.Variable;

/***************************************************
 * Dialog for specifying input options for loading a matrix or vector from file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MatrixInDialogBox extends InterfaceIODialogBox {

	JLabel lblFormat = new JLabel("File format:");
	JComboBox cmbFormat = new JComboBox();
	JLabel lblLoadAs = new JLabel("Load as:");
	JComboBox cmbLoadAs = new JComboBox();
	JLabel lblFiles = new JLabel("Matrices:");
	JCheckBox chkHeader = new JCheckBox(" Has header line");
	
	JTable file_table;
	JScrollPane scrFiles;
	
	public MatrixInDialogBox(){
		super();
	}
	
	public MatrixInDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		
		_init();
		
	}
	
	private void _init(){
		super.init();
		
		this.setDialogSize(450,450);
		this.setTitle("Input Matrix - Options");
		
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.35, 1);
		mainPanel.add(lblFormat, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.55, 1);
		mainPanel.add(cmbFormat, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.35, 1);
		mainPanel.add(lblLoadAs, c);
		c = new LineLayoutConstraints(2, 2, 0.4, 0.55, 1);
		mainPanel.add(cmbLoadAs, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.9, 1);
		mainPanel.add(lblFiles, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.9, 1);
		mainPanel.add(chkHeader, c);
		
		initCombos();
		updateTable();
		
	}
	
	@Override
	public void showDialog(){
		updateDialog();
		super.showDialog();
	}
	
	@Override
	public boolean updateDialog(){
		
		MatrixInOptions _options = (MatrixInOptions)options;
		
		chkHeader.setSelected(_options.has_header);
		
		initCombos();
		
		switch (_options.format){
			case AsciiFull:
				cmbFormat.addItem("Ascii Full");
				break;
			case AsciiSparse:
				cmbFormat.addItem("Ascii Sparse");
				break;
			case BinaryFull:
				cmbFormat.addItem("Binary Full");
				break;
			case BinarySparse:
				cmbFormat.addItem("Binary Sparse");
				break;
			}
		
		switch (_options.as_type){
			case Matrix:
				cmbLoadAs.setSelectedItem("Matrix");
				break;
			case RowsAsVectors:
				cmbLoadAs.setSelectedItem("Rows as vectors");
				break;
			case ColumnsAsVectors:
				cmbLoadAs.setSelectedItem("Columns as vectors");
				break;
			case VectorPackedRow:
				cmbLoadAs.setSelectedItem("Row-packed vector");
				break;
			case VectorPackedColumn:
				cmbLoadAs.setSelectedItem("Column-packed vector");
				break;
		}
		
		
		updateTable();
		return true;
	}
	
	protected void initCombos(){
		cmbFormat.removeAllItems();
		cmbFormat.addItem("Ascii Full");
		cmbFormat.addItem("Ascii Sparse");
		cmbFormat.addItem("Binary Full");
		cmbFormat.addItem("Binary Sparse");
		
		cmbLoadAs.removeAllItems();
		cmbLoadAs.addItem("Matrix");
		cmbLoadAs.addItem("Rows as vectors");
		cmbLoadAs.addItem("Columns as vectors");
		cmbLoadAs.addItem("Row-packed vector");
		cmbLoadAs.addItem("Column-packed vector");
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			MatrixInOptions _options = (MatrixInOptions)options;
			String format = (String)cmbFormat.getSelectedItem();
			if (format.equals("Ascii Full"))
				_options.format = Format.AsciiFull;
			if (format.equals("Ascii Sparse"))
				_options.format = Format.AsciiSparse;
			if (format.equals("Binary Full"))
				_options.format = Format.BinaryFull;
			if (format.equals("Binary Sparse"))
				_options.format = Format.BinarySparse;
			
			String load_as = (String)cmbLoadAs.getSelectedItem();
			if (load_as.equals("Matrix"))
				_options.as_type = Variable.Matrix;
			if (load_as.equals("Rows as vectors"))
				_options.as_type = Variable.RowsAsVectors;
			if (load_as.equals("Columns as vectors"))
				_options.as_type = Variable.ColumnsAsVectors;
			if (load_as.equals("Row-packed vector"))
				_options.as_type = Variable.VectorPackedRow;
			if (load_as.equals("Column-packed vector"))
				_options.as_type = Variable.VectorPackedColumn;
			
			if (file_table != null){
				for (int i = 0; i < file_table.getModel().getRowCount(); i++)
					_options.names[i] = (String)file_table.getValueAt(i, 0);
				}
			
			_options.has_header = chkHeader.isSelected();
			
			setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
	}
	
	protected void updateTable(){
		
		if (options == null) return;
		MatrixInOptions opts = (MatrixInOptions)options;
		
		//header and new table model
		if (scrFiles != null) mainPanel.remove(scrFiles);
		
		if (opts.getFiles() == null){
			lblFiles.setVisible(false);
			return;
		}
		
		lblFiles.setVisible(true);
		
		ArrayList<String> v_files = new ArrayList<String>();
		ArrayList<String> v_names = new ArrayList<String>();
		
		File[] files = options.getFiles();
		String[] names = opts.names;
		
		for (int i = 0; i < files.length; i++){
			v_files.add(files[i].getName());
			v_names.add(names[i]);
			}
		
		ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>(files.length);
		for (int i = 0; i < files.length; i++){
			ArrayList<String> v = new ArrayList<String>(4);
			v.add(v_names.get(i));
			v.add(v_files.get(i));
			values.add(v);
			}
		
		ArrayList<String> header = new ArrayList<String>(4);
		header.add("Name");
		header.add("Filename");
		
		TableModel model = new TableModel(values, header);
		file_table = new JTable(model);
		scrFiles = new JScrollPane(file_table);
		file_table.getColumnModel().getColumn(0).setPreferredWidth(15);
		
		LineLayoutConstraints c = new LineLayoutConstraints(5, 12, 0.05, 0.9, 1);
		mainPanel.add(scrFiles, c);
		mainPanel.updateUI();
		
	}
	
	
	protected class TableModel extends AbstractTableModel {
	      
		ArrayList<ArrayList<String>> data;
		ArrayList<String> columns;
		
		public TableModel(ArrayList<ArrayList<String>> data, ArrayList<String> columns){
			this.data = data;
			this.columns = columns;
		}

        public int getColumnCount() {
            return columns.size();
        }

        public int getRowCount() {
            return data.size();
        }

        @Override
		public String getColumnName(int col) {
            return columns.get(col);
        }

        public Object getValueAt(int row, int col) {
            return data.get(row).get(col);
        }

        @Override
		public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Column names not editable
         */
        @Override
		public boolean isCellEditable(int row, int col) {
        	if (col == 1) return false;
        	return true;
        }

        @Override
		public void setValueAt(Object value, int row, int col) {
            data.get(row).set(col, (String)value);
            fireTableCellUpdated(row, col);
        }

    }
	
}