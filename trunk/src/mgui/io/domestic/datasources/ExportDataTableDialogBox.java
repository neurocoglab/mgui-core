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

package mgui.io.domestic.datasources;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import mgui.datasources.DataQuery;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataSourceItem;
import mgui.datasources.DataTable;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.io.domestic.datasources.ExportDataTableExcelOptions.Format;

/***************************************************************
 * Dialog box to specify options for exporting data from a data source to an external file format
 * (e.g., delimited text).
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ExportDataTableDialogBox extends InterfaceIODialogBox {

	JLabel lblDataSource = new JLabel("Data Source:");
	InterfaceComboBox cmbDataSource = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 200);
	JCheckBox chkHasHeader = new JCheckBox(" Write header");
	
	JLabel lblOutputFormat = new JLabel("Output format: ");
	InterfaceComboBox cmbOutputFormat = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 200);
	//JComboBox cmbDelimiter = new JComboBox();
	
	// Text specific
	// TODO: Writers provide their own panels for specific parameters
	JLabel lblDelimiter = new JLabel("Delimiter: ");
	JTextField txtDelimiter = new JTextField(",");
	JLabel lblPrecision = new JLabel("Decimals:");
	JTextField txtPrecision = new JTextField("9");
	
	// Excel specific
	JLabel lblExcelFormat = new JLabel("Excel format: ");
	JComboBox cmbExcelFormat = new JComboBox();
	
	JLabel lblDirectory = new JLabel("Save in folder:");
	JTextField txtDirectory = new JTextField();
	JButton cmdDirectory = new JButton("Browse..");
	
	JLabel lblTables = new JLabel("Tables & Queries:");
	JTable data_export_table;
	ExportTableModel data_export_table_model;
	JScrollPane scrDataItems;
	
	private boolean doUpdate = true;
	DataSource current_source;
	
	File current_folder;
	
	public boolean is_ok = false;
	
	static final String EXCEL_TYPE_XLS = "Classic (xls)";
	static final String EXCEL_TYPE_XLSX = "Open Office XML (xlsx)";
	
	public ExportDataTableDialogBox(){
		super();
	}
	
	public ExportDataTableDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		_init();
	}
	
	void _init(){
		super.init();
		
		this.setDialogSize(600,600);
		this.setTitle("Export Data Source Item - Options");
	
		cmbDataSource.addActionListener(this);
		cmbDataSource.setActionCommand("Source Changed");
		//cmbDelimiter.addActionListener(this);
		//cmbDelimiter.setActionCommand("Delimiter Changed");
		cmbOutputFormat.addActionListener(this);
		cmbOutputFormat.setActionCommand("Format Changed");
		cmbExcelFormat.addActionListener(this);
		cmbExcelFormat.setActionCommand("Excel Format Changed");
		cmdDirectory.addActionListener(this);
		cmdDirectory.setActionCommand("Browse Folder");
		txtDirectory.setEditable(false);
		
		LineLayout lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		
		//initDelimiters();
		initOutputTypes();
		initDataItems();
		initDataSources();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1,1,0.05,0.2,1);
		mainPanel.add(lblDataSource, c);
		c = new LineLayoutConstraints(1,1,0.25,0.45,1);
		mainPanel.add(cmbDataSource, c);
		c = new LineLayoutConstraints(2,2,0.05,0.2,1);
		mainPanel.add(lblOutputFormat, c);
		c = new LineLayoutConstraints(2,2,0.25,0.45,1);
		mainPanel.add(cmbOutputFormat, c);
		c = new LineLayoutConstraints(3,3,0.05,0.25,1);
		mainPanel.add(chkHasHeader, c);
		c = new LineLayoutConstraints(3,3,0.3,0.15,1);
		mainPanel.add(lblDelimiter, c);
		c = new LineLayoutConstraints(3,3,0.45,0.1,1);
		mainPanel.add(txtDelimiter, c);
		//c = new LineLayoutConstraints(2,2,0.6,0.1,1);
		//mainPanel.add(cmbDelimiter, c);
		c = new LineLayoutConstraints(3,3,0.6,0.18,1);
		mainPanel.add(lblPrecision, c);
		c = new LineLayoutConstraints(3,3,0.8,0.1,1);
		mainPanel.add(txtPrecision, c);
		c = new LineLayoutConstraints(4,4,0.05,0.2,1);
		mainPanel.add(lblDirectory, c);
		c = new LineLayoutConstraints(4,4,0.25,0.4,1);
		mainPanel.add(txtDirectory, c);
		c = new LineLayoutConstraints(4,4,0.65,0.3,1);
		mainPanel.add(cmdDirectory, c);
		
		// Excel-specific
		c = new LineLayoutConstraints(3,3,0.3,0.15,1);
		mainPanel.add(lblExcelFormat, c);
		c = new LineLayoutConstraints(3,3,0.45,0.3,1);
		mainPanel.add(cmbExcelFormat, c);
		
		c = new LineLayoutConstraints(5,5,0.05,0.2,1);
		mainPanel.add(lblTables, c);
		c = new LineLayoutConstraints(6,11,0.05,0.9,1);
		mainPanel.add(scrDataItems, c);
		
		lineLayout.setFlexibleComponent(scrDataItems);
		
		current_folder = InterfaceEnvironment.getCurrentDir();
		txtDirectory.setText(current_folder.getAbsolutePath());
		
	}
	
	/**********************************************
	 * Returns the current output type.
	 * 
	 * @return
	 */
	public InterfaceIOType getOutputType(){
		if (options == null) return null;
		
		if (options instanceof ExportDataTableExcelOptions) 
			return InterfaceEnvironment.getIOTypeForInstance(new ExportDataTableExcelWriter());
		
		return InterfaceEnvironment.getIOTypeForInstance(new ExportDataTableTextWriter());
	}
	
	/***********************************************
	 * Returns the current options.
	 * 
	 * @return
	 */
	public ExportDataTableOptions getCurrentOptions(){
		return (ExportDataTableOptions)options;
	}
	
	protected void initOutputTypes(){
		doUpdate = false;
		cmbOutputFormat.removeAllItems();
		
		// Get writers, look for instances of ExportDataTableWriter
		HashMap<String,InterfaceIOType> io_types = InterfaceEnvironment.getIOTypes();
		ArrayList<String> names = new ArrayList<String>(io_types.keySet());
		Collections.sort(names);
		
		for (int i = 0; i < names.size(); i++){
			InterfaceIOType type = io_types.get(names.get(i));
			if (ExportDataTableWriter.class.isAssignableFrom(type.getIO())){
				cmbOutputFormat.addItem(type);
				}
			}
		
		// TODO: remove after making this more generic
		cmbExcelFormat.removeAllItems();
		cmbExcelFormat.addItem(EXCEL_TYPE_XLS);
		cmbExcelFormat.addItem(EXCEL_TYPE_XLSX);
		cmbExcelFormat.setSelectedItem(EXCEL_TYPE_XLSX);
		
		doUpdate = true;
		
		// Ensure the correct initial output type is selected
		InterfaceIOType io_type = getOutputType();
		if (io_type != null){
			cmbOutputFormat.setSelectedItem(io_type);
			}
			
	}
	
	protected void initDataSources(){
		
		doUpdate = false;
		
		cmbDataSource.removeAllItems();
		
		ArrayList<DataSource> sources = InterfaceSession.getWorkspace().getDataSources();
		for (int i = 0; i < sources.size(); i++){
			if (sources.get(i).isConnected())
				cmbDataSource.addItem(sources.get(i));
			}
		
		doUpdate = true;
		
		if (sources.size() > 0)
			cmbDataSource.setSelectedIndex(0);
	}
	
	protected void initDataItems(){
		
		data_export_table_model = new ExportTableModel();
		data_export_table = new JTable(data_export_table_model);
		
		data_export_table.getColumnModel().getColumn(0).setMaxWidth(45);
		
		data_export_table.getColumnModel().getColumn(1).setCellRenderer(new TableCellRenderer(){

			JLabel label = new JLabel();
			
			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				
				DataSourceItem item = (DataSourceItem)value;
				label.setIcon(item.getObjectIcon());
				label.setText(item.getName());
				return label;
			}
			
		});
		
		scrDataItems = new JScrollPane(data_export_table);
		
	}
	
	public void showDialog(){
		
		if (ExportDataTableTextOptions.class == options.getClass()){
			
			cmbOutputFormat.setSelectedItem((new ExportDataTableTextWriter()).getIOType());
		
			// Set up files and fields
			ExportDataTableTextOptions options = (ExportDataTableTextOptions)this.options;
					
			
			chkHasHeader.setSelected(options.has_header);
			txtDelimiter.setText(getDelimText(options.delimiter));
			txtPrecision.setText("" + options.precision);
			
			File[] files = options.getFiles();
			if (files != null && files.length > 0){
				this.current_folder = files[0].getParentFile();
				if (current_folder != null) txtDirectory.setText(current_folder.getAbsolutePath());
				}
			
			if (options.data_source != null)
				cmbDataSource.setSelectedItem(options.data_source);
			
			// TODO: update file names + selections
			
			
			
			return;
			}
		
		
		if (ExportDataTableExcelOptions.class == options.getClass()){
			
			cmbOutputFormat.setSelectedItem((new ExportDataTableExcelWriter()).getIOType());
			
			ExportDataTableExcelOptions options = (ExportDataTableExcelOptions)this.options;
			
			switch(options.excel_format){
				case Xls:
					cmbExcelFormat.setSelectedItem(EXCEL_TYPE_XLS);
				case Xlsx:
					cmbExcelFormat.setSelectedItem(EXCEL_TYPE_XLSX);
				}
			
			}
		
		this.updateFormat();
		
	}
	
	public static boolean showDialog(ExportDataTableTextOptions options){
		
		ExportDataTableDialogBox dialog = new ExportDataTableDialogBox(InterfaceSession.getSessionFrame(),
																	   null, options);
		dialog.setVisible(true);
		return dialog.is_ok;
		
	}
	
	protected void updateDataTables(){
	
		if (current_source == null) return;
		
		try{
			ArrayList<DataTable> tables = current_source.getTableSet().getTables();
			ArrayList<DataQuery> queries = current_source.getDataQueries();
		
			ArrayList<DataSourceItem> items = new ArrayList<DataSourceItem>();
			items.addAll(tables);
			items.addAll(queries);
			
			data_export_table_model.setItems(items);
			
		}catch (DataSourceException ex){
			InterfaceSession.log("ExportDataTableDialogBox: Error retrieving tables for '" + current_source.getName() + "'.", 
								 LoggingType.Errors);
			}
		
	}
	
	private String getDelimiter(){
		
		String delim = txtDelimiter.getText();
		if (delim.equals("{tab}")) return "\t";
		if (delim.equals("{newline}")) return "\n";
		if (delim.equals("{backslash}")) return "\\";
		if (delim.equals("{quote}")) return "\"";
		return delim;
		
	}
	
	private String getDelimText(String delim){
		if (delim.equals("\t")) return "{tab}";
		if (delim.equals("\n")) return "{newline}";
		if (delim.equals("\\")) return "{backslash}";
		if (delim.equals("\"")) return "{quote}";
		return delim;
	}
	
	private void updateFormat(){
		
		// TODO: make more generic...
		
		InterfaceIOType format = (InterfaceIOType)cmbOutputFormat.getSelectedItem();
		if (format == null) return;
		boolean is_text = format.getIO().equals(ExportDataTableTextWriter.class);
		
		if (is_text){
			if (!(options instanceof ExportDataTableTextOptions)){
				ExportDataTableTextOptions new_options = new ExportDataTableTextOptions();
				new_options.setFrom((ExportDataTableOptions)options);
				options = new_options;
				}
		}else{
			if (!(options instanceof ExportDataTableExcelOptions)){
				ExportDataTableExcelOptions new_options = new ExportDataTableExcelOptions();
				new_options.setFrom((ExportDataTableOptions)options);
				options = new_options;
				}
			}
		
		if (data_export_table_model != null){
			if (is_text){
				if (this.txtDelimiter.getText().equals(","))
					data_export_table_model.updateExtensions("csv");
				else
					data_export_table_model.updateExtensions("txt");
			}else{
				if (cmbExcelFormat.getSelectedItem() == EXCEL_TYPE_XLS)
					data_export_table_model.updateExtensions("xls");
				else
					data_export_table_model.updateExtensions("xlsx");
				}
			}
		
		lblDelimiter.setVisible(is_text);
		txtDelimiter.setVisible(is_text);
		lblPrecision.setVisible(is_text);
		txtPrecision.setVisible(is_text);
		
		lblExcelFormat.setVisible(!is_text);
		cmbExcelFormat.setVisible(!is_text);
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Source Changed")){
			if (!doUpdate) return;
			
			current_source = (DataSource)cmbDataSource.getSelectedItem();
			updateDataTables();
			
			return;
			}
		
		if (e.getActionCommand().endsWith("Format Changed")){
			if (!doUpdate) return;
			
			updateFormat();
			
			return;
			}
		
		
//		if (e.getActionCommand().equals("Delimiter Changed")){
//			
//			String delim = (String)cmbDelimiter.getSelectedItem();
//			if (delim.equals("tab"))
//				txtDelimiter.setText("{tab}");
//			if (delim.equals("newline"))
//				txtDelimiter.setText("{newline}");
//			if (delim.equals("backslash"))
//				txtDelimiter.setText("{backslash}");
//			if (delim.equals("quote"))
//				txtDelimiter.setText("{quote}");
//			
//			return;
//			}
		
		if (e.getActionCommand().equals("Browse Folder")){
			
			JFileChooser fc = null;
			if (current_folder != null)
				fc = new JFileChooser(current_folder);
			else
				fc = new JFileChooser();
			
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (!(fc.showOpenDialog(InterfaceSession.getSessionFrame()) == JFileChooser.APPROVE_OPTION))
				return;
			
			current_folder = fc.getSelectedFile();
			txtDirectory.setText(current_folder.getAbsolutePath());
			
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			if (current_folder == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(),
												"No folder set!", 
												"Export Data Source Tables", 
												JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			if (current_source == null){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(),
												"No data source set!", 
												"Export Data Source Tables", 
												JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			InterfaceIOType format = (InterfaceIOType)cmbOutputFormat.getSelectedItem();
			if (format == null) return;
			boolean is_text = format.getIO().equals(ExportDataTableTextWriter.class);
			
			ExportDataTableOptions _options = (ExportDataTableOptions)options;
			_options.data_items = new ArrayList<DataSourceItem>();
			ArrayList<String> file_names = new ArrayList<String>();
			
			for (int i = 0; i < data_export_table_model.include.size(); i++){
				if (data_export_table_model.include.get(i)){
					_options.data_items.add(data_export_table_model.items.get(i));
					file_names.add(data_export_table_model.file_names.get(i));
					}
				}
			
			File[] files = new File[file_names.size()];
			for (int i = 0; i < file_names.size(); i++){
				files[i] = new File(current_folder.getAbsolutePath() + File.separator + file_names.get(i));
				}
			
			_options.setFiles(files);
			_options.data_source = current_source;
			_options.has_header = chkHasHeader.isSelected();
			
			if (is_text){
				ExportDataTableTextOptions _options2 = (ExportDataTableTextOptions)options;
				
				_options2.precision = Integer.valueOf(txtPrecision.getText());
				_options2.delimiter = getDelimiter();
				
			}else{
				ExportDataTableExcelOptions _options2 = (ExportDataTableExcelOptions)options;
				
				if (cmbExcelFormat.getSelectedItem().equals(EXCEL_TYPE_XLS)){
					_options2.excel_format = Format.Xls;
				}else{
					_options2.excel_format = Format.Xlsx;
					}
					
				_options2.sheet_name = "Sheet1";
				}
			
			is_ok = true;
			
			setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
	class ExportTableModel extends AbstractTableModel{

		ArrayList<DataSourceItem> items = new ArrayList<DataSourceItem>();
		ArrayList<Boolean> include = new ArrayList<Boolean>();
		ArrayList<String> file_names = new ArrayList<String>();
		
		public ExportTableModel(){
			
		}
		
		public ExportTableModel(ArrayList<DataSourceItem> items){
			setItems(items);
		}
		
		public void setItems(ArrayList<DataSourceItem> items){
			setItems(items, "txt");
		}
		
		public void updateExtensions(String ext){
			if (this.file_names == null) return;
			
			for (int i = 0; i < file_names.size(); i++){
				String name = file_names.get(i);
				if (name.contains(".")) name = name.substring(0, name.lastIndexOf("."));
				name = name + "." + ext;
				file_names.set(i, name);
				}
			this.fireTableDataChanged();
		}
		
		public void setItems(ArrayList<DataSourceItem> items, String ext){
			this.items = items;
			include = new ArrayList<Boolean>();
			file_names = new ArrayList<String>();
			for (int i = 0; i < items.size(); i++){
				file_names.add(items.get(i).getName() + "." + ext);
				include.add(false);
				}
			this.fireTableDataChanged();
		}
		
		public void addItem(DataSourceItem item, boolean include){
			addItem(item, include, "txt");
		}
		
		public void addItem(DataSourceItem item, boolean include, String ext){
			items.add(item);
			this.include.add(include);
			file_names.add(item.getName() + "." + ext);
			this.fireTableDataChanged();
		}
		
		public void removeItem(DataSourceItem item){
			for (int i = 0; i < items.size(); i++)
				if (items.get(i).equals(item)){
					items.remove(i);
					include.remove(i);
					file_names.remove(i);
					this.fireTableDataChanged();
					return;
					}
			
		}
		
		@Override
		public int getRowCount() {
			return items.size();
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public Object getValueAt(int row, int column) {
			
			switch (column){
			
				case 0: return include.get(row);
				case 1: return items.get(row);
				case 2: return file_names.get(row);
				
				}
			
			return null;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column){
			
				case 0: return Boolean.class;
				case 1: return DataSourceItem.class;
				case 2: return String.class;
				
				}
			return String.class;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column != 1;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			switch (column){
				case 0:
					include.set(row, (Boolean)value);
					return;
				case 2:
					file_names.set(row, (String)value);
				}
		}

		@Override
		public String getColumnName(int column) {
			switch (column){
				case 0:
					return "Export?";
				case 1:
					return "Data Item";
				case 2:
					return "File name";
				}
			return "?";
		}
		
	}
	
	
	
}