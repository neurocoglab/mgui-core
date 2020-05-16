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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;

/**********************************************************
 * Dialog box for loading vertex-wise data into a surface.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SurfaceDataInputDialogBox extends InterfaceIODialogBox {
	
	protected JLabel lblMesh = new JLabel("Load into mesh:");
	//protected JComboBox cmbMesh = new JComboBox();
	protected InterfaceComboBox cmbMesh = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, 
			   										  true, 500, true);
	protected JLabel lblColumns = new JLabel("Columns:");
	
	protected JTable table;
	protected JScrollPane scrColumns;
	protected LineLayout lineLayout;
	protected int table_pos = 3;
	
	public SurfaceDataInputDialogBox(){
		
	}
	
	public SurfaceDataInputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		_init();
	}
	
	private void _init(){
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,400);
		this.setTitle("Input Surface Data Options");
		
		fillMeshCombo();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		mainPanel.add(lblMesh, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(cmbMesh, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.9, 1);
		mainPanel.add(lblColumns, c);
		
		updateTable();
	}
	
	@Override
	public void showDialog(){
		if (options == null) options = new SurfaceDataInputOptions();
		fillMeshCombo();
		updateTable();
		setVisible(true);
	}
	
	protected void fillMeshCombo(){
		if (io_panel == null) return;
		
		cmbMesh.removeAllItems();
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		
		for (int j = 0; j < models.size(); j++){
			List<Shape3DInt> meshes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
			for (Shape3DInt mesh : meshes) {
				cmbMesh.addItem(mesh);
				}
			}
		
		if (cmbMesh.getItemCount() == 0) return;
		
		Mesh3DInt mesh = null;
		if (options != null)
			mesh = ((SurfaceDataInputOptions)options).mesh;
		if (mesh != null)
			cmbMesh.setSelectedItem(mesh);
		else
			cmbMesh.setSelectedIndex(0);
			
	}
	
	protected void updateTable(){
		
		if (options == null) return;
		SurfaceDataInputOptions opts = (SurfaceDataInputOptions)options;
		
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
			SurfaceDataInputOptions opts = (SurfaceDataInputOptions)options;
			if (cmbMesh.getItemCount() > 0)
				opts.mesh = (Mesh3DInt)cmbMesh.getSelectedItem();
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