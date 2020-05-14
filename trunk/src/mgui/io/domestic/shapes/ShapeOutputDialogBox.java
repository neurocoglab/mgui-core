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

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.xml.XMLObject.XMLEncoding;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.util.StringFunctions;

/********************************************************
 * Dialog box for output of {@linkplain InterfaceShape} objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeOutputDialogBox extends InterfaceIODialogBox {

	JLabel lblShapeModel = new JLabel("Shape model:");
	InterfaceComboBox cmbShapeModel = new InterfaceComboBox(RenderMode.LongestItem,true,200);
	JLabel lblShapes = new JLabel("Shapes to write:");
	JTable table;
	ShapesTableModel table_model;
	JScrollPane scrShapes;
	
	LineLayout lineLayout;
	
	public ShapeOutputDialogBox(){
		
	}
	
	public ShapeOutputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		init();
	}
	
	@Override
	protected void init(){
		this.setButtonType(BT_OK_CANCEL);
		super.init();
		
		
		lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,525);
		this.setTitle("Write Mgui Shapes to XML");
		
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		mainPanel.add(lblShapeModel, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(cmbShapeModel, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.24, 1);
		mainPanel.add(lblShapes, c);
		
		initModels();
		updateTable();
	}
	
	private void initModels(){
		cmbShapeModel.removeAllItems();
		if (this.io_objects != null) return;
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		for (int i = 0; i < models.size(); i++)
			cmbShapeModel.addItem(models.get(i));
		if (options != null){
			ShapeOutputOptions _options = (ShapeOutputOptions)options;
			if (_options.shape_model == null) return;
			cmbShapeModel.setSelectedItem(_options.shape_model);
			}
	}
	
	@Override
	public void setIOObjects(ArrayList<InterfaceObject> objects){
		super.setIOObjects(objects);
		if (objects == null){
			// Open specification of objects
			cmbShapeModel.setEnabled(true);
		}else{
			// Explicit specification of objects
			cmbShapeModel.setEnabled(false);
			}
		initModels();
		updateTable();
	}
	
	private void updateTable(){
		
		ShapeOutputOptions _options = (ShapeOutputOptions)options;
		
		if (options != null && _options.shape_model != null){
			if (table_model == null)
				table_model = new ShapesTableModel(_options.shape_model.getModelSet());
			else
				table_model.setFromShapes(_options.shape_model.getModelSet().getMembers(true));
			table_model.setFromOptions(_options);
		}else if (this.io_objects != null){
			ArrayList<InterfaceShape> shapes = new ArrayList<InterfaceShape>();
			for (int i = 0; i < io_objects.size(); i++)
				shapes.add((InterfaceShape)io_objects.get(i));
			if (table_model == null)
				table_model = new ShapesTableModel(shapes);
			else
				table_model.setFromShapes(shapes);
		}else if (cmbShapeModel.getSelectedItem() != null){
			ShapeModel3D model = (ShapeModel3D)cmbShapeModel.getSelectedItem();
			if (table_model == null)
				table_model = new ShapesTableModel(model.getModelSet());
			else
				table_model.setFromShapes(model.getModelSet().getMembers(true));
		}else{
			if (table_model == null)
				table_model = new ShapesTableModel();
			else
				table_model.setFromShapes(new ArrayList<InterfaceShape>());
			}
		if (table == null){
			table = new JTable(table_model);
			scrShapes = new JScrollPane(table);
			}
		
		JComboBox<XMLType> cbox = new JComboBox<XMLType>();
		cbox.addItem(XMLType.Normal);
		cbox.addItem(XMLType.Full);
		cbox.addItem(XMLType.Short);
		cbox.addItem(XMLType.Reference);
		TableColumn col = table.getColumnModel().getColumn(3);
		col.setCellEditor(new DefaultCellEditor(cbox));
		
		JComboBox<XMLEncoding> cbox2 = new JComboBox<XMLEncoding>();
		cbox2.addItem(XMLEncoding.Ascii);
		cbox2.addItem(XMLEncoding.Base64Binary);
		cbox2.addItem(XMLEncoding.Base64BinaryGZipped);
		cbox2.addItem(XMLEncoding.Base64BinaryZipped);
		col = table.getColumnModel().getColumn(4);
		col.setCellEditor(new DefaultCellEditor(cbox2));
		
		LineLayoutConstraints c = new LineLayoutConstraints(3, 9, 0.05, 0.9, 1);
		mainPanel.add(scrShapes, c);
		lineLayout.setFlexibleComponent(scrShapes);
		
	}
	
	@Override
	public boolean updateDialog(){
		
		if (options == null) return false;
		
		
		
		return true;
		
	}
	
	@Override
	public void actionPerformed(ActionEvent event){
		
		if (event.getActionCommand().equals(DLG_CMD_OK)){
			
			ShapeOutputOptions _options = (ShapeOutputOptions)options;
			int n_selected = 0;
			for (int i = 0; i < table_model.select.size(); i++)
				if (table_model.select.get(i)) n_selected++;
			
			if (n_selected == 0){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											"No shapes selected!", 
											"Output shape(s) to XML", 
											JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			_options.shape_model = (ShapeModel3D)cmbShapeModel.getSelectedItem();
			
			//_options.files = new File[n_selected];
			_options.filenames = new ArrayList<String>(n_selected);
			int j = 0;
			_options.shapes = new ArrayList<InterfaceShape>(n_selected);
			_options.types = new ArrayList<XMLType>(n_selected);
			_options.encodings = new ArrayList<XMLEncoding>(n_selected);
			
			for (int i = 0; i < table_model.filenames.size(); i++){
				if (table_model.select.get(i)){
					_options.filenames.add(table_model.filenames.get(i));
					_options.shapes.add(table_model.shapes.get(i));
					_options.types.add(table_model.types.get(i));
					_options.encodings.add(table_model.encodings.get(i));
					}
				}
			
			
			this.setVisible(false);
			return;
			}
		
		
		super.actionPerformed(event);
		
	}
	
	
	class ShapesTableModel extends AbstractTableModel{

		ArrayList<InterfaceShape> shapes = new ArrayList<InterfaceShape>();
		ArrayList<XMLType> types = new ArrayList<XMLType>();
		ArrayList<String> filenames = new ArrayList<String>();
		ArrayList<Boolean> select = new ArrayList<Boolean>();
		ArrayList<XMLEncoding> encodings = new ArrayList<XMLEncoding>();
		
		public ShapesTableModel(){
		
			
		}
		
		public ShapesTableModel(ShapeSet3DInt set){
			setFromShapes(set.getMembers(true));
		}
		
		public ShapesTableModel(ArrayList<InterfaceShape> shapes){
			setFromShapes(shapes);
		}
		
		public void setFromShapes(ArrayList<InterfaceShape> shapes){
			_init();
			for (int i = 0; i < shapes.size(); i++)
				addShape(shapes.get(i), false);
			this.fireTableDataChanged();
		}
		
		public void setFromOptions(ShapeOutputOptions options){
			for (int i = 0; i < options.shapes.size(); i++){
				int idx = getIndexFor(options.shapes.get(i));
				if (idx > -1){
					select.set(idx, true);
					types.set(idx, options.types.get(i));
					encodings.set(idx, options.encodings.get(i));
					filenames.set(idx, options.filenames.get(i));
					}
				}
			this.fireTableDataChanged();
		}
		
		private int getIndexFor(InterfaceShape shape){
			for (int i = 0; i < shapes.size(); i++)
				if (shapes.get(i).equals(shape)) return i;
			return -1;
		}
		
		private void _init(){
			shapes.clear();
			types.clear();
			filenames.clear();
			select.clear();
			encodings.clear();
			this.fireTableDataChanged();
		}
		
		public void addShape(InterfaceShape shape, boolean fire){
			int row = shapes.size();
			shapes.add(shape);
			select.add(false);
			types.add(XMLType.Full);
			encodings.add(XMLEncoding.Ascii);
			filenames.add(getFullName(shape) + ".shape");
			
			if (fire)
				fireTableRowsInserted(row, row);
		}
		
		private String getFullName(InterfaceShape shape){
			String full_name = shape.getFullName();
			int a = full_name.indexOf('.');
			if (a > 0)
				full_name = full_name.substring(a+1);
			a = full_name.indexOf('.');
			if (a > 0)
				full_name = full_name.substring(a+1);
				
			full_name = StringFunctions.replaceAll(full_name, " ", "_");
			return full_name;
		}
		
		@Override
		public String getColumnName(int column) {
			switch (column){
			
				case 0: return "Shape Set";
				case 1: return "Shape";
				case 2: return "Select";
				case 3: return "XML Type";
				case 4: return "Encoding";
				case 5: return "Writer";
				case 6: return "Filename";
			
				}
			
			return "?";
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column){
			
				case 0: return String.class;
				case 1: return String.class;
				case 2: return Boolean.class;
				case 3: return XMLType.class;
				case 4: return XMLEncoding.class;
				case 5: return String.class;
				case 6: return String.class;
			
				}
		
			return String.class;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column){
				case 0: 
				case 1:
				case 5:  	// TODO implement this
					return false;
				}
	
			return true;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			
			switch (column){
				case 2: 
					select.set(row, (Boolean)value);
					InterfaceShape shape = shapes.get(row);
					if (shape instanceof ShapeSet3DInt)
						updateShapeSet(row);
					break;
				case 3:
					XMLType type = (XMLType)value;
					types.set(row, type);
					break;
				case 4:
					XMLEncoding encoding = (XMLEncoding)value;
					encodings.set(row, encoding);
					break;
				case 6: 
					filenames.set(row, (String)value);
				}
			
		}

		private void updateShapeSet(int row){
			// De/select all shapes in a de/selected shape set
			boolean selected = select.get(row);
			ShapeSet3DInt set = (ShapeSet3DInt)shapes.get(row);
			
			for (int i = 0; i < shapes.size(); i++){
				InterfaceShape shape = shapes.get(i);
				ShapeSet this_set = shape.getParentSet();
				if (this_set.equals(set))
					select.set(row, selected);
				}
			this.fireTableDataChanged();
		}
		
		@Override
		public int getRowCount() {
			return shapes.size();
		}

		@Override
		public int getColumnCount() {
			return 7;
		}

		@Override
		public Object getValueAt(int row, int column) {
			
			InterfaceShape shape = shapes.get(row);
			
			switch (column){
				case 0: 
					ShapeSet set = shape.getParentSet();
					if (set == null) 
						return "None";
					return set.getName();
				case 1: return shape.getName();
				case 2: return select.get(row);
				case 3: 
					return types.get(row);
//					switch (types.get(row)){
//						case Normal: return "Normal";
//						case Full: return "Full";
//						case Reference: return "Reference";
//						case Short: return "Short";
//						}
//					return "?";
				case 4:
					return encodings.get(row);
				case 5: return "XML";
				case 6: return filenames.get(row);
				}
			
			return null;
		}
		
	}
	
}