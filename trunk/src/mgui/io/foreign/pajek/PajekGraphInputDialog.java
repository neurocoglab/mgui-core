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

package mgui.io.foreign.pajek;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;

import org.apache.commons.collections15.Transformer;



/***********************************************************
 * Options dialog box for {@linkplain PajekGraphLoader}.
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PajekGraphInputDialog extends InterfaceIODialogBox {

	protected LineLayout lineLayout;
	
	
	JLabel lblGraphs = new JLabel("Graphs to load:");
	JTable graphs_table;
	GraphTableModel table_model;
	JScrollPane scrGraphs;
	
	File[] current_files;
	
	public PajekGraphInputDialog(){
		
	}
	
	public PajekGraphInputDialog(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		_init();
	}
	
	private void _init(){
		super.init();
		
		this.setDialogSize(700,400);
		this.setTitle("Pajek Graph Input Options");
		
		lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.5, 1);
		mainPanel.add(lblGraphs, c);
		
		updateTable();
		
	}
	
	void updateTable(){
		
		PajekGraphInputOptions _options = (PajekGraphInputOptions)options;
		current_files = _options.getFiles();
		if (current_files == null) return;
		
		table_model = new GraphTableModel(_options);
		if (graphs_table == null){
			graphs_table = new JTable(table_model);
			scrGraphs = new JScrollPane(graphs_table);
		}else{
			graphs_table.setModel(table_model);
			}
		
		TableColumn col = graphs_table.getColumnModel().getColumn(4);
		Transformer<Object,String> renderer = new Transformer<Object,String>(){
										public String transform(Object obj) {
											if (obj == null) return "~";
											ShapeSet3DInt set = (ShapeSet3DInt)obj;
											return set.getFullName();
										}
									  };
		InterfaceComboBox combo = new InterfaceComboBox(RenderMode.LongestItem, true, 
														  500, renderer);
		for (int i = 0; i < table_model.workspace_sets.size(); i++)
			combo.addItem(table_model.workspace_sets.get(i));
			
		col.setCellEditor(new DefaultCellEditor(combo));
		MyComboBoxRenderer cb = new MyComboBoxRenderer(RenderMode.LongestItem, true, 
				  									   500, renderer);
		col.setCellRenderer(cb);
		for (int i = 0; i < table_model.workspace_sets.size(); i++)
			cb.addItem(table_model.workspace_sets.get(i));
		
		graphs_table.getColumnModel().getColumn(2).setMinWidth(40);
		graphs_table.getColumnModel().getColumn(2).setMaxWidth(50);
		
		LineLayoutConstraints c = new LineLayoutConstraints(2, 8, 0.05, 0.9, 1);
		mainPanel.add(scrGraphs, c);
		lineLayout.setFlexibleComponent(scrGraphs);
		mainPanel.updateUI();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent event){
		
		if (event.getActionCommand().equals(DLG_CMD_OK)){
			
			if (table_model == null){
				this.setVisible(false);
				return;
				}
			
			// update options
			PajekGraphInputOptions _options = (PajekGraphInputOptions)options;
			
			File[] files = new File[table_model.graph_files.size()];
			for (int i = 0; i < files.length; i++)
				files[i] = table_model.graph_files.get(i);
			_options.setFiles(files);
			
			for (int i = 0; i < files.length; i++){
				_options.graph_names.set(i, table_model.graph_names.get(i));
				_options.shape_names.set(i, table_model.shape_names.get(i));
				_options.shape_sets.set(i, table_model.shape_sets.get(i));
				_options.create_shape.set(i, table_model.create_shapes.get(i));
				}
			
			
			this.setVisible(false);
			return;
			}
		
		
		super.actionPerformed(event);
	}
	
	class MyComboBoxRenderer extends InterfaceComboBox implements TableCellRenderer {
	    public MyComboBoxRenderer(RenderMode mode, boolean show_icons, int width, Transformer<Object,String> name_transformer) {
	        super(mode, show_icons, width, name_transformer);
	    }

	    public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int row, int column) {
	        if (isSelected) {
	            setForeground(table.getSelectionForeground());
	            super.setBackground(table.getSelectionBackground());
	        } else {
	            setForeground(table.getForeground());
	            setBackground(table.getBackground());
	        }

	        // Select the current value
	        setSelectedItem(value);
	        return this;
	    }
	}
	
	class GraphTableModel extends AbstractTableModel{

		public ArrayList<File> graph_files = new ArrayList<File>();
		public ArrayList<String> graph_names = new ArrayList<String>();
		public ArrayList<String> shape_names = new ArrayList<String>();
		public ArrayList<Boolean> create_shapes = new ArrayList<Boolean>();
		public ArrayList<ShapeSet3DInt> shape_sets = new ArrayList<ShapeSet3DInt>();
		
		public ArrayList<ShapeSet3DInt> workspace_sets; 
		public ShapeSet3DInt last_set;
		
		public GraphTableModel(){
			init();
		}
		
		public GraphTableModel(PajekGraphInputOptions options){
			init();
			File[] files = options.getFiles();
			if (files == null) return;
			for (int i = 0; i < files.length; i++){
				int idx = addFile(files[i], false);
				if (options.graph_names != null)
					graph_names.set(idx, options.graph_names.get(i));
				if (options.create_shape != null)
					create_shapes.set(idx, options.create_shape.get(i));
				if (options.shape_names != null)
					shape_names.set(idx, options.shape_names.get(i));
				if (options.shape_sets != null)
					shape_sets.set(idx, options.shape_sets.get(i));
				}
			this.fireTableDataChanged();
		}
		
		private void init(){
			ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
			workspace_sets = new ArrayList<ShapeSet3DInt>();
			for (int i = 0; i < models.size(); i++){
				ArrayList<InterfaceShape> sets = models.get(i).getModelSet().getShapeType(new ShapeSet3DInt(),true).getMembers();
				workspace_sets.add(models.get(i).getModelSet());
				for (int j = 0; j < sets.size(); j++)
					workspace_sets.add((ShapeSet3DInt)sets.get(j));
				}
			
		}
		
		public int addFile(File file){
			return addFile(file, true);
		}
		
		public int addFile(File file, boolean fire){
			
			String name = file.getName();
			if (name.contains("."))
				name = name.substring(0, name.lastIndexOf("."));
			
			graph_files.add(file);
			graph_names.add(name);
			create_shapes.add(true);
			
			if (last_set != null)
				shape_sets.add(last_set);
			else if (workspace_sets.size() > 0)
				shape_sets.add(workspace_sets.get(0));
			else
				shape_sets.add(null);
			
			shape_names.add(name);
			if (fire)
				this.fireTableDataChanged();
			return graph_files.size() - 1;
		}
		
		@Override
		public int getRowCount() {
			return graph_files.size();
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public Object getValueAt(int row, int column) {
			
			switch (column){
			
				case 0:
					return graph_files.get(row).getName();
				case 1:
					return graph_names.get(row);
				case 2:
					return create_shapes.get(row);
				case 3:
					return shape_names.get(row);
				case 4:
					return shape_sets.get(row);
				default:
					return "?";
				}
			
		}

		@Override
		public String getColumnName(int column) {
			switch (column){
			
				case 0:
					return "File";
				case 1:
					return "Graph Name";
				case 2:
					return "Shape?";
				case 3:
					return "Shape name";
				case 4:
					return "Shape set";
				default:
					return "?";
				}
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column){
			
				case 0:
				case 1:
				case 3:
					return String.class;
				case 2:
					return Boolean.class;
				case 4:
					return ShapeSet3DInt.class;
				default:
					return String.class;
				}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			
			switch (column){
				case 0:
					return false;
				default:
					return true;
				}
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			switch (column){
				case 1:
					graph_names.set(row, (String)value);
					break;
				case 2:
					create_shapes.set(row, (Boolean)value);
					break;
				case 3:
					shape_names.set(row, (String)value);
					break;
				case 4:
					shape_sets.set(row, (ShapeSet3DInt)value);
					break;
				default:
					return;
				}
			this.fireTableDataChanged();
		}
		
		
	}
	
	
}