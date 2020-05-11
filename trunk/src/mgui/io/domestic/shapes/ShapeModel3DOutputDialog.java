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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject.XMLEncoding;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.io.util.IoFunctions;

/*****************************************************************
 * Dialog box for specifying the XML output of a {@code ShapeModel3D}. Allows per-shape specification of
 * the {@code XMLType} (either Full or Reference), the encoding (for Full writes), and the writer 
 * and file name (for Reference writes). Also specifies the folder organization for Reference writes 
 * (flat or with subset folders).
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeModel3DOutputDialog extends InterfaceIODialogBox {

	protected LineLayout lineLayout;
	
	JLabel lblShapeModel = new JLabel("Shape model:");
	InterfaceComboBox cmbShapeModel = new InterfaceComboBox(RenderMode.LongestItem,
															true,
															100);
	JCheckBox chkShapesInFolder = new JCheckBox(" All shapes in folder:");
	JTextField txtShapesInFolder = new JTextField("");
	JCheckBox chkSetSubfolders = new JCheckBox(" Sets in subfolders");
	JCheckBox chkOverwrite = new JCheckBox(" Overwrite existing files");
	JCheckBox chkCompress = new JCheckBox(" Compress XML file");
	
	TableModel table_model;
	JScrollPane scrShapesTable;
	JTable table;
	
	JLabel lblShapes = new JLabel("Shapes:");
	ShapeModel3DOutputOptions temp_options;
	
	boolean update = true;
	
	public ShapeModel3DOutputDialog(){
		super();
	}
	
	public ShapeModel3DOutputDialog(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		temp_options = (ShapeModel3DOutputOptions)options;
		init();
		setLocationRelativeTo(frame);
	}

	@Override
	protected void init(){
		this.setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		
		super.init();
		
		cmbShapeModel.addActionListener(this);
		cmbShapeModel.setActionCommand("Shape Model Changed");
		chkShapesInFolder.addActionListener(this);
		chkShapesInFolder.setActionCommand("Shapes in Folder");
		chkSetSubfolders.addActionListener(this);
		chkSetSubfolders.setActionCommand("Shapes in Subfolders");
		
		lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		
		this.setMainLayout(lineLayout);
		this.setDialogSize(1200,600);
		this.setTitle("Shape Model 3D Output Options");
		
		initCombos();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.07, 0.2, 1);
		mainPanel.add(lblShapeModel, c);
		c = new LineLayoutConstraints(1, 1, 0.25, 0.3, 1);
		mainPanel.add(cmbShapeModel, c);
		c = new LineLayoutConstraints(2, 2, 0.07, 0.18, 1);
		mainPanel.add(chkShapesInFolder, c);
		c = new LineLayoutConstraints(2, 2, 0.25, 0.3, 1);
		mainPanel.add(txtShapesInFolder, c);
		c = new LineLayoutConstraints(2, 2, 0.65, 0.3, 1);
		mainPanel.add(chkSetSubfolders, c);
		c = new LineLayoutConstraints(3, 3, 0.07, 0.3, 1);
		mainPanel.add(chkOverwrite, c);
		c = new LineLayoutConstraints(3, 3, 0.37, 0.3, 1);
		mainPanel.add(chkCompress, c);
		
		
		updateDialog();
	}
	
	@Override
	public boolean updateDialog(){
		
		temp_options = new ShapeModel3DOutputOptions();
		if (options != null)
			temp_options.setFromOptions((ShapeModel3DOutputOptions)options);
		
		ShapeModel3D model = temp_options.getModel();
		
		if (model != null){
			// Set current model
			update = false;
			cmbShapeModel.setSelectedItem(model);
			update = true;
		}else{
			ShapeModel3D new_model = (ShapeModel3D)cmbShapeModel.getSelectedItem();
			if (new_model != null)
				temp_options.setFromModel(new_model);
			}
		
		updateTableModel();
		chkSetSubfolders.setSelected(temp_options.as_subfolders);
		chkShapesInFolder.setSelected(!temp_options.as_subfolders);
		chkCompress.setSelected(temp_options.gzip_xml);
		chkOverwrite.setSelected(temp_options.overwrite_existing);
		txtShapesInFolder.setText(temp_options.shapes_folder);
		updateControls();
		updateFilenames();
		
		return true;
	}
	
	private void initCombos(){
		fillModelCombo();
		
	}
	
	private void fillModelCombo(){
		update = false;
		
		cmbShapeModel.removeAllItems();
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		
		for (int i = 0; i < models.size(); i++)
			cmbShapeModel.addItem(models.get(i));	
		
		update = true;
		
	}
	
	private void initTable(){
		if (temp_options == null || temp_options.getModel() == null) return;
		table_model = new TableModel(temp_options);
		table = new JTable(table_model);
		table.setRowHeight(InterfaceEnvironment.getLineHeight());
		scrShapesTable = new JScrollPane(table);
		LineLayoutConstraints c = new LineLayoutConstraints(5, 10, 0.05, 0.9, 1);
		mainPanel.add(scrShapesTable, c);
		lineLayout.setFlexibleComponent(scrShapesTable);
		
		// Column widths and combo box editors
		TableColumn column = table.getColumnModel().getColumn(0);
		column.setMaxWidth(50);
		column = table.getColumnModel().getColumn(1);
		column.setCellRenderer(new CustomCellRenderer());
		column.setMinWidth(150);
		
		column = table.getColumnModel().getColumn(2);
		JComboBox combo = new InterfaceComboBox(RenderMode.LongestItem, false, 100);
		combo.addItem(XMLFunctions.getXMLStrForType(XMLType.Full));
		combo.addItem(XMLFunctions.getXMLStrForType(XMLType.Reference));
		column.setCellRenderer(new CustomCellRenderer());
		column.setCellEditor(new DefaultCellEditor(combo));
		column.setMaxWidth(80);
		column = table.getColumnModel().getColumn(3);
		combo = new InterfaceComboBox(RenderMode.LongestItem, false, 300);
		combo.addItem(XMLFunctions.getEncodingStr(XMLEncoding.Ascii));
		combo.addItem(XMLFunctions.getEncodingStr(XMLEncoding.Base64Binary));
		combo.addItem(XMLFunctions.getEncodingStr(XMLEncoding.Base64BinaryZipped));
		combo.addItem(XMLFunctions.getEncodingStr(XMLEncoding.Base64BinaryGZipped));
		combo.addItem(XMLFunctions.getEncodingStr(XMLEncoding.XML));
		column.setCellRenderer(new CustomCellRenderer());
		column.setCellEditor(new DefaultCellEditor(combo));
		column.setMaxWidth(110);
		
		column = table.getColumnModel().getColumn(4);
		column.setCellRenderer(new CustomCellRenderer());
		column.setCellEditor(new CustomCellEditor());
		
		column = table.getColumnModel().getColumn(5);
		column.setCellRenderer(new CustomCellRenderer());
		
		column = table.getColumnModel().getColumn(6);
		column.setCellRenderer(new OptionsCellButton());
		column.setCellEditor(new OptionsCellButton());
		column.setMinWidth(70);
		column.setMaxWidth(100);
		
		column = table.getColumnModel().getColumn(7);
		column.setMaxWidth(50);
		
	}
	
	private void updateTableModel(){
		
		if (temp_options == null)
			temp_options = new ShapeModel3DOutputOptions();
		
		ShapeModel3D model = (ShapeModel3D)cmbShapeModel.getSelectedItem();
		//ShapeModel3DOutputOptions _options = (ShapeModel3DOutputOptions)options;
		if (temp_options.getModel() == null || temp_options.getModel() != model){
			temp_options.setFromModel(model, true);
		}else{
			temp_options.setFromModel(model, false);
			}
		
		if (table_model == null){
			initTable();
		}else{
			table_model.setFromOptions(temp_options);
			}
		
		this.updateFilenames();
		
	}
	
	protected void updateFilenames(){
		if (table_model == null) return;
		ArrayList<InterfaceShape> shapes = table_model.shapes;
		if (shapes == null || shapes.size() == 0) return;
		boolean is_flat = true; //chkShapesInFolder.isSelected();
		
		for (int i = 0; i < shapes.size(); i++){
			InterfaceShape shape = shapes.get(i);
			String filename = shape.getFullName();
			if (table_model.options.get(shape).filename != null)
				filename = table_model.options.get(shape).filename;
			filename = filename.toLowerCase();
			filename = filename.replace(" ", "_");
			if (is_flat)
				filename = filename.replace(File.separator, ".");
			else
				filename = filename.replace(".", File.separator);
			
//			if (table_model.options.get(shape).writer != null) {
//				List<String> exts = table_model.options.get(shape).writer.getIOType().getExtensions();
//				if (exts.size() > 0) {
//					boolean has_ext = false;
//					for (String ext : exts) {
//						if (filename.endsWith("." + ext)) {
//							has_ext = true;
//							break;
//							}
//						}
//					if (!has_ext) {
//						filename = filename + "." + exts.get(0);
//						}
//					}
//				}
			
			table_model.options.get(shape).filename = filename;
			}
		
		table_model.fireTableDataChanged();
	}
	
	protected void updateControls(){
		
		txtShapesInFolder.setEnabled(chkShapesInFolder.isSelected());
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Shape Model Changed")){
			if (!update) return;
			ShapeModel3D model = (ShapeModel3D)cmbShapeModel.getSelectedItem();
			if (temp_options.getModel() == model) return; 
			temp_options.setModel(model);
			updateTableModel();
			return;
			}
		
		if (e.getActionCommand().startsWith("Shapes in")){
			if (!update) return;
			if (e.getActionCommand().endsWith(" Folder")){
				chkSetSubfolders.setSelected(!chkShapesInFolder.isSelected());
			}else{
				chkShapesInFolder.setSelected(!chkSetSubfolders.isSelected());
				}
			updateControls();
			updateFilenames();
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			if (temp_options == null){
				setVisible(false);
				return;
				}
			
			temp_options.setModel((ShapeModel3D)cmbShapeModel.getSelectedItem());
			temp_options.as_subfolders = chkSetSubfolders.isSelected();
			temp_options.gzip_xml = chkCompress.isSelected();
			
			table_model.setOptions(temp_options);
			ShapeModel3DOutputOptions _options = (ShapeModel3DOutputOptions)options;
			_options.setFromOptions(temp_options);
			
			if (chkShapesInFolder.isSelected()){
				_options.shapes_folder = txtShapesInFolder.getText();
			}else{
				_options.shapes_folder = "";
				}
			
			setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
	@Override
	public void showDialog(){
		setVisible(true);
		
	}
	
	
	protected class TableModel extends AbstractTableModel {

		ArrayList<InterfaceShape> shapes;
		ArrayList<Boolean> include, selected;
		HashMap<InterfaceShape,XMLOutputOptions> options;
		
		public TableModel(){
			
		}
		
		public TableModel(ShapeModel3D shape_model){
			setFromModel(shape_model);
		}
		
		public TableModel(ShapeModel3DOutputOptions options){
			setFromOptions(options);
		}
		
		public void setOptions(ShapeModel3DOutputOptions options){
			
			options.include_shape = new HashMap<InterfaceShape,Boolean>();
			
			for (int i = 0; i < shapes.size(); i++){
				InterfaceShape shape = shapes.get(i);
				options.include_shape.put(shape, include.get(i));
				options.shape_xml_options.put(shape, this.options.get(shape));
				}
			
		}
		
		/*************************************************
		 * Set defaults from an existing model
		 * 
		 * @param shape_model
		 */
		public void setFromModel(ShapeModel3D shape_model){
			
			if (shape_model == null){
				shapes = null;
				include = null;
				options = null;
				selected = null;
				fireTableDataChanged();
				return;
				}
			
			// Set shapes and values from this model
			ShapeSet3DInt model_set = shape_model.getModelSet();
			shapes = model_set.getMembers(true);
			include = new ArrayList<Boolean>(shapes.size());
			selected = new ArrayList<Boolean>(shapes.size());
			options = new HashMap<InterfaceShape,XMLOutputOptions>(shapes.size());
			
			// Set options from these shapes
			for (int i = 0; i < shapes.size(); i++){
				include.add(true);
				selected.add(false);
				InterfaceShape shape = shapes.get(i);
				XMLOutputOptions _options = new XMLOutputOptions();
				options.put(shape, _options);
				FileWriter writer = shape.getFileWriter();
				//String filename = shape.getFullName();
				if (writer != null){
					_options.writer = writer;
					ArrayList<String> filters = writer.getIOType().getFilter().getPatterns();
					String ext = "";
					if (filters != null && filters.size() > 0){
						ext = filters.get(0);
						if (ext.contains(".") && ext.lastIndexOf(".") < ext.length())
							ext = ext.substring(ext.lastIndexOf(".") + 1);
						}
					InterfaceIOOptions shape_io_options = shape.getWriterOptions();
					if (shape_io_options != null && writer.getIOType().isCompatibleOptions(shape_io_options)){
						_options.io_options = shape_io_options;
					}else{
						_options.io_options = writer.getIOType().getOptionsInstance();
						}
						
				}else{
					// Get a default writer
					InterfaceIOType type = IoFunctions.getDefaultIOType(shape, InterfaceIOType.TYPE_OUTPUT);
					if (type != null){
						_options.writer = (FileWriter)type.getIOInstance();
						_options.io_options = type.getOptionsInstance();
						}
					_options.filename = null;
					}
				}
			
			fireTableDataChanged();
			
		}
		
		/**************************************************
		 * Set values from an existing instance of {@code ShapeModel3DOutputOptions}
		 * 
		 * @param options
		 */
		public void setFromOptions(ShapeModel3DOutputOptions options){
			
			if (options == null){
				shapes = null;
				include = null;
				selected = null;
				this.options = null;
				fireTableDataChanged();
				return;
				}
			
			ShapeModel3D shape_model = options.getModel();
			ShapeSet3DInt model_set = shape_model.getModelSet();
			shapes = model_set.getMembers(true);
			include = new ArrayList<Boolean>(shapes.size());
			selected = new ArrayList<Boolean>(shapes.size());
			this.options = new HashMap<InterfaceShape,XMLOutputOptions>(shapes.size());
			
			for (int i = 0; i < shapes.size(); i++){
				selected.add(false);
				InterfaceShape shape = shapes.get(i);
				include.add(options.include_shape.get(shape));
				XMLOutputOptions opts = options.shape_xml_options.get(shape);
				this.options.put(shape, opts);
				
				// If writer and file name are null, try using defaults
				if (opts.writer == null){
					// Get a default writer
					InterfaceIOType type = IoFunctions.getDefaultIOType(shape, InterfaceIOType.TYPE_OUTPUT);
					if (type != null && type.getIOInstance().getComplementIOType() != null){
						opts.writer = (FileWriter)type.getIOInstance();
						opts.io_options = type.getOptionsInstance();
						}
					}
				
				}
			
			fireTableDataChanged();
		}
		
        public int getColumnCount() {
            return 8;
        }

        public int getRowCount() {
        	if (shapes == null) return 0;
            return shapes.size();
        }

        @Override
		public String getColumnName(int col) {
            switch (col){
	            case 0: return "Include";
	            case 1: return "Full name";
	            case 2: return "XML type";
	            case 3: return "Encoding";
	            case 4: return "Writer";
	            case 5: return "File name";
	            case 6: return "Options";
	            case 7: return "Select";
	            default: return "?";
            	}
        }

        public Object getValueAt(int row, int col) {
        	InterfaceShape shape = shapes.get(row);
        	XMLOutputOptions options = this.options.get(shape);
        	switch (col){
	            case 0: 
	            	return include.get(row);
	            case 1: 
	            	return shape.getFullName();
	            case 2:
	            	return XMLFunctions.getXMLStrForType(options.type);
	            case 3: 
	            	return XMLFunctions.getEncodingStr(options.encoding);
	            case 4: 
	            	return options.writer;
	            case 5: 
	            	return options.filename;
	            case 6:
	            	return options.io_options;
	            case 7:
	            	return selected.get(row);
	            default: return "?";
        		}
        }

        @Override
		public Class getColumnClass(int col) {
        	switch (col){
	            case 0: return Boolean.class;
	            case 1: 
	            case 2:
	            case 3: return String.class;
	            case 4: return FileWriter.class;
	            case 5: return String.class;
	            case 6: return InterfaceIOOptions.class;
	            case 7: return Boolean.class;
	            default: return Object.class;
	        	}
        }

        /*
         * Column names not editable
         */
        @Override
		public boolean isCellEditable(int row, int col) {
        	switch (col){
	            case 1: 
	            	return false; 
	            default:
	            	return isEnabled(row,col);
	        	}
        }

        @Override
		public void setValueAt(Object value, int row, int col) {
        	if (value == null) return;
        	InterfaceShape shape = shapes.get(row);
        	XMLOutputOptions options = this.options.get(shape);
	        switch (col){
	        	case 0:
	        		include.set(row, (Boolean)value);
	        		fireTableRowsUpdated(row, row);
	        		return;
	            case 2: 
	            	XMLType type = XMLFunctions.getXMLTypeForStr((String)value);
	            	options.type = type;
	            	fireTableRowsUpdated(row, row);
	            	return;
	            case 3:
	            	XMLEncoding encoding = XMLFunctions.getEncodingForStr((String)value);
	            	options.encoding = encoding;
	            	return;
	            case 4: 
	            	FileWriter writer = (FileWriter)value;
	            	if (options.writer == null || options.writer.getClass() != writer.getClass()){
	            		// Update options if writer changed
	            		options.writer = writer;
	            		options.io_options = writer.getIOType().getOptionsInstance();
	            		}
	            	return;
	            case 5: 
	            	options.filename = (String)value;
	            	return;
	            case 6:
	            	options.io_options = (InterfaceIOOptions)value;
	            	return;
	            case 7:
	            	selected.set(row, (Boolean)value);
	            	return;
	            default: 
	            	return;
	        	}
        }
        
        /****************************
         * Return the enabled state of a component, based on the XML type
         * 
         * @param row
         * @param col
         * @return
         */
        public boolean isEnabled(int row, int col){
        	if (col > 0 && !include.get(row)) return false;
        	InterfaceShape shape = shapes.get(row);
        	XMLOutputOptions options = this.options.get(shape);
        	if (options == null) return col > 2;
        	
        	switch (options.type){
	        	case Full:
	        		return !(col > 3 && col < 7);
	        	case Reference:
	        		return (col != 3);
        		default:
        			return true;
        		}
        }
		
		
	}
	
	class CustomCellRenderer extends DefaultTableCellRenderer{

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
																	  row, column);
			
			TableModel table_model = (TableModel)table.getModel();
			component.setEnabled(table_model.isEnabled(row, column));
			return component;
		}
		
		
		
	}
	
	class CustomCellEditor extends DefaultCellEditor{

		public CustomCellEditor() {
			super(new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem,
										true,
										400));
		}
		
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			
			TableModel table_model = (TableModel)table.getModel();
			InterfaceShape shape = table_model.shapes.get(row);
			InterfaceComboBox combo = (InterfaceComboBox)editorComponent;
			combo.removeAllItems();
			boolean has_items = false; 
			
			// File loader list
			ArrayList<String> supported = IoFunctions.getSupportingTypes(shape);
			for (int j = 0; j < supported.size(); j++){
				InterfaceIOType type = InterfaceEnvironment.getIOType(supported.get(j));
				if (type != null && type.getType() == InterfaceIOType.TYPE_OUTPUT){
					if (type.getIOInstance().getComplementIOType() != null){
						combo.addItem(type.getIOInstance());
						has_items = true;
						}
					}
				}
			
			FileWriter writer = table_model.options.get(shape).writer;
			if (writer != null){
				InterfaceIOType type = writer.getIOType();
				combo.setSelectedItem(type.getName());
			}else if (has_items){
				combo.setSelectedIndex(0);
				}
			
			combo.setEnabled(table_model.isEnabled(row,column));
			
			return combo;
		}

	}
	
	class OptionsCellButton extends JButton implements TableCellEditor,
													   TableCellRenderer,
													   ActionListener{
		
		ArrayList<CellEditorListener> listeners = new ArrayList<CellEditorListener>();
		
		InterfaceIOOptions options;
		int row;
		
		public OptionsCellButton(){
			super();
			this.setText("Edit..");
			this.addActionListener(this);
		}
		
		@Override
		public Object getCellEditorValue() {
			return options;
		}

		@Override
		public boolean isCellEditable(EventObject anEvent) {
			return true;
		}

		@Override
		public boolean shouldSelectCell(EventObject anEvent) {
			return false;
		}

		@Override
		public boolean stopCellEditing() {
			for (int i = 0; i < listeners.size(); i++)
				listeners.get(i).editingStopped(new ChangeEvent(this));
			return true;
		}

		@Override
		public void cancelCellEditing() {
			for (int i = 0; i < listeners.size(); i++)
				listeners.get(i).editingCanceled(new ChangeEvent(this));
		}

		@Override
		public void addCellEditorListener(CellEditorListener l) {
			listeners.add(l);
		}

		@Override
		public void removeCellEditorListener(CellEditorListener l) {
			listeners.remove(l);
		}

		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {

			this.options = (InterfaceIOOptions)value;
			this.row = row;
			
			return this;
		}
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			this.setEnabled(table_model.isEnabled(row,column));
			
			return this;
		}

		@Override
		public void actionPerformed(ActionEvent e){
			
			InterfaceShape shape = table_model.shapes.get(row);
			XMLOutputOptions xml_options = table_model.options.get(shape);
			if (xml_options == null || xml_options.writer == null){
				this.cancelCellEditing();
				return;
				}
			if (options != null && !xml_options.writer.getIOType().isCompatibleOptions(options))
				options = null;
			if (options == null)
				options = xml_options.writer.getIOType().getOptionsInstance();
			if (options == null){
				this.cancelCellEditing();
				return;
				}
				
			InterfaceIODialogBox dialog = 
					xml_options.writer.getIOType().getDialogInstance(InterfaceSession.getSessionFrame(), 
															 		 io_panel, options);
			
			if (dialog == null){
				this.cancelCellEditing();
				return;
				}
			
			ArrayList<InterfaceObject> objs = new ArrayList<InterfaceObject>();
			objs.add(shape);
			dialog.setIOObjects(objs);
			
			dialog.showDialog();
			this.stopCellEditing();
			
			return;
		}
		
		
		
	}
	
}