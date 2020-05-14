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
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;


public class PolygonSet3DInputDialogBox extends InterfaceIODialogBox {

	protected JCheckBox chkNewShapeSet = new JCheckBox("As new shape set: ");
	protected JTextField txtNewShapeSet = new JTextField("");
	protected JLabel lblParentShapeSet = new JLabel("Parent shape set: ");
	InterfaceComboBox cmbParentShapeSet = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  true, 500);
	JCheckBox chkMinNodes = new JCheckBox("Min node count:");
	JTextField txtMinNodes = new JTextField("1");
	protected JLabel lblPolysets = new JLabel("Polygon sets:");
	
	JTable table;
	JScrollPane scrColumns;
	
	LineLayout lineLayout;
	
	public PolygonSet3DInputDialogBox(){
		super();
		init();
	}
	
	public PolygonSet3DInputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		//((PolygonSet3DInputOptions)opts).shape_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		init();
	}
	
	@Override
	protected void init(){
		super.init();
		
		this.setDialogSize(550,400);
		this.setTitle("Polygon 3D Input Options");
		
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
				
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.35, 1);
		mainPanel.add(chkNewShapeSet, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.55, 1);
		mainPanel.add(txtNewShapeSet, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.35, 1);
		mainPanel.add(lblParentShapeSet, c);
		c = new LineLayoutConstraints(2, 2, 0.4, 0.55, 1);
		mainPanel.add(cmbParentShapeSet, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.35, 1);
		mainPanel.add(chkMinNodes, c);
		c = new LineLayoutConstraints(3, 3, 0.4, 0.55, 1);
		mainPanel.add(txtMinNodes, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.9, 1);
		mainPanel.add(lblPolysets, c);
		
		updateTable();
		
	}
	
	@Override
	public void showDialog(){
		PolygonSet3DInputOptions opts = (PolygonSet3DInputOptions)options;
		if (opts == null) opts = new PolygonSet3DInputOptions();
		if (opts.shape_set == null)
			opts.shape_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		chkNewShapeSet.setSelected(opts.new_shape_set);
		txtNewShapeSet.setText(opts.new_shape_set_name);
		
		chkMinNodes.setSelected(opts.skip_min_nodes);
		txtMinNodes.setText("" + opts.min_nodes);
		
		cmbParentShapeSet.removeAllItems();
		ShapeSet3DInt current_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		cmbParentShapeSet.addItem(current_set);
		ShapeSet3DInt all_sets = current_set.getShapeType(current_set, true);
		for (int i = 0; i < all_sets.members.size(); i++){
			cmbParentShapeSet.addItem(all_sets.members.get(i));
			}
		cmbParentShapeSet.setSelectedItem(opts.shape_set);
		
		updateTable();
		setVisible(true);
	}
	
	protected void updateTable(){
		
		if (options == null) return;
		PolygonSet3DInputOptions opts = (PolygonSet3DInputOptions)options;
		
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
		
		LineLayoutConstraints c = new LineLayoutConstraints(5, 10, 0.05, 0.9, 1);
		mainPanel.add(scrColumns, c);
		mainPanel.updateUI();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		super.actionPerformed(e);
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			if (chkNewShapeSet.isSelected() && txtNewShapeSet.getText().length() == 0) {
				JOptionPane.showMessageDialog(this, "You must specify a name for the new shape set!", 
											  "Load polygon set", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			PolygonSet3DInputOptions opts = (PolygonSet3DInputOptions)options;
			
			if (table == null){
				setVisible(false);
				return;
				}
			
			for (int i = 0; i < table.getModel().getRowCount(); i++)
				opts.names[i] = (String)table.getValueAt(i, 0);
			
			opts.new_shape_set = chkNewShapeSet.isSelected();
			opts.new_shape_set_name = txtNewShapeSet.getText();
			
			opts.shape_set = (ShapeSet3DInt)cmbParentShapeSet.getSelectedItem();
			
			opts.skip_min_nodes = chkMinNodes.isSelected();
			if (opts.skip_min_nodes)
				opts.min_nodes = Integer.valueOf(txtMinNodes.getText());
			
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