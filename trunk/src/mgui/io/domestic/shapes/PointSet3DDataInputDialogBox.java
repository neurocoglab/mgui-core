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
import java.io.File;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.PointSet3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;


public class PointSet3DDataInputDialogBox extends InterfaceIODialogBox {

	JLabel lblPointSet = new JLabel("Load into point set:");
	protected JComboBox cmbPointSet = new JComboBox();
	JLabel lblColumns = new JLabel("Columns:");
	
	protected JTable table;
	protected JScrollPane scrColumns;
	
	protected LineLayout lineLayout;
	
	protected int table_pos = 3;
	
	public PointSet3DDataInputDialogBox(){
		
	}
	
	public PointSet3DDataInputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		init();
	}
	
	@Override
	protected void init(){
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,400);
		this.setTitle("Input Point Set 3D Data Options");
		
		fillMeshCombo();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		mainPanel.add(lblPointSet, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(cmbPointSet, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.9, 1);
		mainPanel.add(lblColumns, c);
		
		updateTable();
	}
	
	@Override
	public void showDialog(){
		if (options == null) options = new PointSet3DDataInputOptions();
		fillMeshCombo();
		updateTable();
		setVisible(true);
	}
	
	protected void fillMeshCombo(){
		if (io_panel == null) return;
		
		cmbPointSet.removeAllItems();
		
		ShapeSet3DInt sets = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new PointSet3DInt());
		for (int i = 0; i < sets.members.size(); i++)
			cmbPointSet.addItem(sets.members.get(i));
		
		if (cmbPointSet.getItemCount() == 0) return;
		
		PointSet3DInt set = null;
		if (options != null)
			set = ((PointSet3DDataInputOptions)options).pointset;
		if (set != null)
			cmbPointSet.setSelectedItem(set);
		else
			cmbPointSet.setSelectedIndex(0);
			
	}
	
	protected void updateTable(){
		
		if (options == null) return;
		PointSet3DDataInputOptions opts = (PointSet3DDataInputOptions)options;
		
		//header and new table model
		if (scrColumns != null) mainPanel.remove(scrColumns);
		
		if (opts.getFiles() == null) return;
		
		Vector<String> v_files = new Vector<String>();
		Vector<String> v_names = new Vector<String>();
		
		File[] files = options.getFiles();
		String[] names = opts.names;
		
		for (int i = 0; i < files.length; i++){
			v_files.add(files[i].getName());
			v_names.add(names[i]);
			}
		
		Vector<Vector> values = new Vector<Vector>(files.length);
		for (int i = 0; i < files.length; i++){
			Vector v = new Vector(4);
			v.add(v_files.get(i));
			v.add(v_names.get(i));
			values.add(v);
			}
		
		Vector<String> header = new Vector<String>(4);
		header.add("Filename");
		header.add("Name");
		
		TableModel model = new TableModel(values, header, 0);
		table = new JTable(model);
		scrColumns = new JScrollPane(table);
		table.getColumnModel().getColumn(0).setPreferredWidth(15);
		
		LineLayoutConstraints c = new LineLayoutConstraints(table_pos, table_pos + 5, 0.05, 0.9, 1);
		mainPanel.add(scrColumns, c);
		mainPanel.updateUI();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		super.actionPerformed(e);
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			PointSet3DDataInputOptions opts = (PointSet3DDataInputOptions)options;
			if (cmbPointSet.getItemCount() > 0)
				opts.pointset = (PointSet3DInt)cmbPointSet.getSelectedItem();
			if (table == null){	
				setVisible(false);
				return;
				}
			for (int i = 0; i < table.getModel().getRowCount(); i++)
				opts.names[i] = (String)table.getValueAt(i, 1);
			setVisible(false);
			return;
			}
	
	}
	
	protected class TableModel extends AbstractTableModel {
	      
		Vector<Vector> data;
		Vector<String> columns;
		int file_col = 0;
		
		public TableModel(Vector<Vector> data, Vector<String> columns, int f_col){
			this.data = data;
			this.columns = columns;
			file_col = f_col;
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
        	if (col == file_col) return false;
        	return true;
        }

        @Override
		public void setValueAt(Object value, int row, int col) {
            data.get(row).set(col, value);
            fireTableCellUpdated(row, col);
        }

    }
	
	
	
}