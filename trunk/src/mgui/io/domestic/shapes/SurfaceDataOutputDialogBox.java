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

package mgui.io.domestic.shapes;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;


public class SurfaceDataOutputDialogBox extends MeshOptionsDialogBox {

	//protected LineLayout lineLayout;
	
	JLabel lblPrefix = new JLabel("Prefix:");
	protected JTextField txtPrefix = new JTextField();
	JLabel lblColumns = new JLabel("Columns:");
	protected JTable table;
	protected JScrollPane scrColumns;
	
	public SurfaceDataOutputDialogBox() {
		super();
	}
	
	public SurfaceDataOutputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		init();
		setLocationRelativeTo(frame);
	}
	
	@Override
	protected void init(){
		super.init();
		updateDialog();
		
		this.setDialogSize(550,380);
		this.setTitle("Surface Data Output Options");
		
		LineLayoutConstraints c = new LineLayoutConstraints(2, 2, 0.05, 0.24, 1);
		mainPanel.add(lblPrefix, c);
		c = new LineLayoutConstraints(2, 2, 0.3, 0.65, 1);
		mainPanel.add(txtPrefix, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.5, 1);
		mainPanel.add(lblColumns, c);
		
		updateTable();
	}
	
	@Override
	public boolean updateDialog(){
		if (options != null){
			SurfaceDataOutputOptions _options = (SurfaceDataOutputOptions)options;
			currentMesh = _options.mesh;
			}
		fillMeshCombo();
		return true;
	}
	
	@Override
	protected void meshChanged(){
		super.meshChanged();
		updateTable();
	}
	
	
	protected void updateTable(){
		if (currentMesh == null) return;
		
		//header and new table model
		if (scrColumns != null) mainPanel.remove(scrColumns);
		
		//table for all data into mesh
		ArrayList<String> cols = currentMesh.getVertexDataColumnNames();
		if (cols == null) return;
		
		Vector<String> v_cols = new Vector<String>(cols);
		Vector<String> v_files = new Vector<String>();
		Vector<String> v_formats = new Vector<String>();
		Vector<Boolean> v_out = new Vector<Boolean>();
		
		SurfaceDataOutputOptions _options = (SurfaceDataOutputOptions)options;
		
		for (int i = 0; i < v_cols.size(); i++){
			String column = v_cols.get(i);
			if (_options.columns != null && _options.columns.contains(column)){
				int idx = _options.columns.indexOf(column);
				v_files.add(_options.filenames.get(idx));
				v_formats.add(_options.formats.get(idx));
				v_out.add(true);
			}else{
				v_files.add(txtPrefix.getText() + (v_cols.get(i)) + _options.extension);
				v_out.add(false);
				v_formats.add("0.000#####");
				}
			}
		
		Vector<Vector<Object>> values = new Vector<Vector<Object>>(cols.size());
		for (int i = 0; i < cols.size(); i++){
			Vector<Object> v = new Vector<Object>(4);
			v.add(v_out.get(i));
			v.add(v_cols.get(i));
			v.add(v_files.get(i));
			v.add(v_formats.get(i));
			values.add(v);
			}
		
		Vector<String> header = new Vector<String>(4);
		header.add("Write");
		header.add("Column");
		header.add("Filename");
		header.add("Number");
		
		TableModel model = new TableModel(values, header);
		table = new JTable(model);
		scrColumns = new JScrollPane(table);
		table.getColumnModel().getColumn(0).setPreferredWidth(15);
		
		LineLayoutConstraints c = new LineLayoutConstraints(4, 9, 0.05, 0.9, 1);
		mainPanel.add(scrColumns, c);
		mainPanel.updateUI();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		super.actionPerformed(e);
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			if (currentMesh == null){
				setVisible(false);
				return;
				}
			
			//update options
			SurfaceDataOutputOptions ops = (SurfaceDataOutputOptions)options;
			TableModel model = (TableModel)table.getModel();
			
			ops.columns = new Vector<String>();
			ops.filenames = new Vector<String>();
			ops.formats = new Vector<String>();
			for (int i = 0; i < model.getRowCount(); i++)
				if (model.getValueAt(i, 0).equals(true)){
					ops.columns.add((String)model.getValueAt(i, 1));
					ops.filenames.add((String)model.getValueAt(i, 2));
					ops.formats.add((String)model.getValueAt(i, 3));
					}
				
			ops.mesh = currentMesh;
			io_panel.updateFromDialog(this);
			setVisible(false);
			}
		
	}
	
	protected class TableModel extends AbstractTableModel {
      
		Vector<Vector<Object>> data;
		Vector<String> columns;
		
		public TableModel(Vector<Vector<Object>> data, Vector<String> columns){
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
            data.get(row).set(col, value);
            fireTableCellUpdated(row, col);
        }

    }

}