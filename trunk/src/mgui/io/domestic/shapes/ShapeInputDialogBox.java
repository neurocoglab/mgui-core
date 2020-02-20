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
import java.io.File;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;

/*********************************************
 * Dialog box for loading a ModelGUI shape. The default implementation is for XML encoding. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeInputDialogBox extends InterfaceIODialogBox {

	protected JLabel lblShapeSet = new JLabel("Add to shape set:");
	protected InterfaceComboBox cmbShapeSet = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			  true, 500);
	
	
protected JLabel lblSurfaces = new JLabel("Surfaces:");
	
	protected JTable table;
	protected JScrollPane scrColumns;
	
	protected LineLayout lineLayout;
	
	protected int table_offset = 0;
	
	public ShapeInputDialogBox(){
		super();
		init();
	}
	
	public ShapeInputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		ShapeInputOptions _options = (ShapeInputOptions)options;
		if (_options.shape_set == null)
			_options.shape_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		init();
	}
	
	@Override
	protected void init(){
		super.init();
		
		this.setDialogSize(570,400);
		this.setTitle("Surface Input Options");
		
		lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		
		updateShapeSets();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.3, 1);
		mainPanel.add(lblShapeSet, c);
		c = new LineLayoutConstraints(1, 1, 0.35, 0.6, 1);
		mainPanel.add(cmbShapeSet, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.9, 1);
		mainPanel.add(lblSurfaces, c);
		
		updateTable();
		
	}
	
	void updateShapeSets(){
		
		if (!InterfaceSession.isInit()) return;
		ShapeSet3DInt current_set = (ShapeSet3DInt)cmbShapeSet.getSelectedItem();
		ShapeSet3DInt shape_set = new ShapeSet3DInt();
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbShapeSet, shape_set);
		
		if (current_set != null)
			cmbShapeSet.setSelectedItem(current_set);
	}
	
	@Override
	public void showDialog(){
		ShapeInputOptions _options = (ShapeInputOptions)options;
		if (_options == null) _options = new ShapeInputOptions();
		if (_options.shape_set == null)
			_options.shape_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		
		updateShapeSets();
		if (_options.shape_set != null)
			cmbShapeSet.setSelectedItem(_options.shape_set);
		
		updateTable();
		setVisible(true);
	}
	
	protected void updateTable(){
		
		if (options == null) return;
		ShapeInputOptions _options = (ShapeInputOptions)options;
		
		//header and new table model
		if (scrColumns != null) mainPanel.remove(scrColumns);
		
		if (_options.getFiles() == null){
			lblSurfaces.setVisible(false);
			return;
		}
		
		lblSurfaces.setVisible(true);
		
		Vector<String> v_files = new Vector<String>();
		Vector<String> v_names = new Vector<String>();
		
		File[] files = options.getFiles();
		String[] names = _options.names;
		
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
		
		LineLayoutConstraints c = new LineLayoutConstraints(4 + table_offset, 8 + table_offset, 0.05, 0.9, 1);
		
		mainPanel.add(scrColumns, c);
		lineLayout.setFlexibleComponent(scrColumns);
		mainPanel.updateUI();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			ShapeInputOptions _options = (ShapeInputOptions)options;
		
			if (table == null){
				setVisible(false);
				return;
				}
			
			for (int i = 0; i < table.getModel().getRowCount(); i++)
				_options.names[i] = (String)table.getValueAt(i, 0);
			
			_options.shape_set = (ShapeSet3DInt)cmbShapeSet.getSelectedItem();
				
			setVisible(false);
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
