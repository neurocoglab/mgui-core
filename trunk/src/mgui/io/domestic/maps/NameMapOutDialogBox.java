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

package mgui.io.domestic.maps;

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
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.maps.NameMap;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;


public class NameMapOutDialogBox extends InterfaceIODialogBox {

	protected LineLayout lineLayout;
	
	JLabel lblDelim = new JLabel("Delimiter:");
	JTextField txtDelim = new JTextField("\\t");
	JLabel lblNameMaps = new JLabel("Name Maps:");
	protected JTable table;
	protected JScrollPane scrColumns;
	
	public NameMapOutDialogBox() {
		super();
	}
	
	public NameMapOutDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		init();
		setLocationRelativeTo(InterfaceSession.getSessionFrame());
	}
	
	@Override
	protected void init(){
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		
		this.setDialogSize(550,380);
		this.setTitle("Name Map Output Options");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.25, 1);
		mainPanel.add(lblDelim, c);
		c = new LineLayoutConstraints(1, 1, 0.25, 0.7, 1);
		mainPanel.add(txtDelim, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.9, 1);
		mainPanel.add(lblNameMaps, c);
		
		initTable();
	}
	
	@Override
	public boolean updateDialog(){
		updateTable();
		return true;
	}
	
	protected void updateTable(){
		initTable();
		if (options == null) options = new NameMapOutOptions();
		NameMapOutOptions opts = (NameMapOutOptions)options;
		
		if (opts.names == null) return;
		
		for (int i = 0; i < opts.maps.length; i++)
			for (int j = 0; j < table.getModel().getRowCount(); j++)
				if (table.getValueAt(j, 1).equals(opts.maps[i])){
					table.setValueAt(opts.names[i], j, 2);
					table.setValueAt(new Boolean(true), j, 0);
					}
		mainPanel.updateUI();
	}
	
	protected void initTable(){
		//ArrayList<NameMap> maps = ioPanel.getDisplayPanel().nameMaps;
		ArrayList<NameMap> maps = InterfaceEnvironment.getNameMaps();
		if (maps == null || maps.size() == 0){
			if (scrColumns != null) mainPanel.remove(scrColumns);
			return;
			}
		if (scrColumns != null) mainPanel.remove(scrColumns);
		
		Vector<Boolean> v_out = new Vector<Boolean>(maps.size());
		Vector<NameMap> v_maps = new Vector<NameMap>(maps.size());
		Vector<String> v_files = new Vector<String>(maps.size());
		
		for (int i = 0; i < maps.size(); i++){
			v_out.add(new Boolean(false));
			v_maps.add(maps.get(i));
			v_files.add(maps.get(i).getName() + ".nmap");
			}
		
		Vector<Vector> values = new Vector<Vector>(maps.size());
		for (int i = 0; i < maps.size(); i++){
			Vector v = new Vector(3);
			v.add(v_out.get(i));
			v.add(v_maps.get(i));
			v.add(v_files.get(i));
			values.add(v);
			}
		
		Vector<String> header = new Vector<String>(4);
		header.add("Write");
		header.add("Map");
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
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			if (options == null || table == null){
				this.setVisible(false);
				return;
				}
			NameMapOutOptions opts = (NameMapOutOptions)options;
			
			int count = 0;
			for (int i = 0; i < table.getModel().getRowCount(); i++)
				if (((Boolean)table.getValueAt(i, 0)).booleanValue())
					count++;
			
			if (count > 0){
				opts.names = new String[count];
				opts.maps = new NameMap[count];
				int j = 0;
				for (int i = 0; i < table.getModel().getRowCount(); i++)
					if (((Boolean)table.getValueAt(i, 0)).booleanValue()){
						opts.names[j] = (String)table.getModel().getValueAt(i, 2);
						opts.maps[j] = (NameMap)table.getModel().getValueAt(i, 1);
						j++;
						}
			}else{
				opts.names = null;
				opts.maps = null;
				}
			
			this.setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
		
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