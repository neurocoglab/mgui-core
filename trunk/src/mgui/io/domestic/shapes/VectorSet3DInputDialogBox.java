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

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;


public class VectorSet3DInputDialogBox extends InterfaceIODialogBox {

	protected JCheckBox chkAddToCurrent = new JCheckBox("Add to current shape set");
	
	protected JLabel lblPolysets = new JLabel("Vector sets:");
	
	JTable table;
	JScrollPane scrColumns;
	
	LineLayout lineLayout;
	
	public VectorSet3DInputDialogBox(){
		super();
		init();
	}
	
	public VectorSet3DInputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		((VectorSet3DInputOptions)opts).shapeSet = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		init();
	}
	
	@Override
	protected void init(){
		super.init();
		
		this.setDialogSize(550,400);
		this.setTitle("Vector 3D Input Options");
		
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.9, 1);
		mainPanel.add(chkAddToCurrent, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.9, 1);
		mainPanel.add(lblPolysets, c);
		
		updateTable();
		
	}
	
	@Override
	public void showDialog(){
		VectorSet3DInputOptions opts = (VectorSet3DInputOptions)options;
		if (opts == null) opts = new VectorSet3DInputOptions();
		if (opts.shapeSet == null)
			opts.shapeSet = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		chkAddToCurrent.setSelected(opts.shapeSet == InterfaceSession.getDisplayPanel().getCurrentShapeSet());
		
		updateTable();
		setVisible(true);
	}
	
	protected void updateTable(){
		
		if (options == null) return;
		VectorSet3DInputOptions opts = (VectorSet3DInputOptions)options;
		
		//header and new table model
		if (scrColumns != null) mainPanel.remove(scrColumns);
		
		if (opts.getFiles() == null){
			lblPolysets.setVisible(false);
			return;
		}
		
		lblPolysets.setVisible(true);
		
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
			v.add(v_names.get(i));
			v.add(v_files.get(i));
			values.add(v);
			}
		
		Vector<String> header = new Vector<String>(4);
		header.add("Name");
		header.add("Filename");
		
		TableModel model = new TableModel(values, header);
		table = new JTable(model);
		scrColumns = new JScrollPane(table);
		table.getColumnModel().getColumn(0).setPreferredWidth(15);
		
		LineLayoutConstraints c = new LineLayoutConstraints(3, 8, 0.05, 0.9, 1);
		mainPanel.add(scrColumns, c);
		mainPanel.updateUI();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		super.actionPerformed(e);
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			VectorSet3DInputOptions opts = (VectorSet3DInputOptions)options;
			
			if (table == null){
				setVisible(false);
				return;
				}
			
			for (int i = 0; i < table.getModel().getRowCount(); i++)
				opts.names[i] = (String)table.getValueAt(i, 0);
			
			setVisible(false);
			return;
			}
	}
	
	protected class TableModel extends AbstractTableModel {
	      
		Vector<Vector> data;
		Vector<String> columns;
		
		public TableModel(Vector<Vector> data, Vector<String> columns){
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