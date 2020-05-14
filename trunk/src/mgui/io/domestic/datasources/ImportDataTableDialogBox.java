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

package mgui.io.domestic.datasources;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.DefaultCellEditor;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import mgui.datasources.DataField;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceDriver;
import mgui.datasources.DataTable;
import mgui.datasources.DataType;
import mgui.datasources.DataTypes;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.logs.LoggingType;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;
import mgui.util.QuotedStringTokenizer;

/*********************************************************
 * Interface which allows the user to define and import tabular data (e.g., from a text file, spreadsheet, or 
 * matrix) into a new table in a given data source.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ImportDataTableDialogBox extends InterfaceIODialogBox {

	// General
	JLabel lblSource = new JLabel("Data source:");
	InterfaceComboBox cmbDataSource = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 200);
	
	// Text file options
	JCheckBox chkHasHeader = new JCheckBox(" Has header row @");
	JTextField txtHeaderStart = new JTextField("1");
	JLabel lblDelimiter = new JLabel("Delimited by: ");
	JTextField txtDelimiter = new JTextField(",");
	JComboBox cmbDelimiter = new JComboBox();
	
	JCheckBox chkOverwriteExisting = new JCheckBox(" Overwrite existing tables");
	JCheckBox chkFailOnError = new JCheckBox(" Fail on any error");
	JCheckBox chkFirstIsKey = new JCheckBox(" First field is key");
	JCheckBox chkLowerCase = new JCheckBox(" Force lower case");
	JLabel lblTextLength = new JLabel("Text length:");
	JTextField txtTextLength = new JTextField("255");
	JCheckBox chkAddUID = new JCheckBox(" Add UID key field:");
	JTextField txtAddUID = new JTextField("uid");
	
	JButton cmdScan = new JButton("Choose files..");
	JLabel lblTables = new JLabel("Current table:");
	InterfaceComboBox cmbTables = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 200, DataTable.getIcon());
	JButton cmdRenameTable = new JButton("Rename..");
	
	// Fields
	JTable fields_table;
	JScrollPane scrFieldsTable;
	ImportFieldsTableModel fields_table_model;
	
	HashMap<String,File> file_map = new HashMap<String,File>();
	HashMap<String,ImportFieldsTableModel> model_map = new HashMap<String,ImportFieldsTableModel>();
	boolean do_update = true;
	
	DataSource current_source;
	
	public boolean is_ok = false;
	
	public ImportDataTableDialogBox(){
		super();
	}
	
	public ImportDataTableDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		_init();
	}
	
	void _init(){
		super.init();
		
		this.setDialogSize(600,645);
		this.setTitle("Import Data to Table - Options");
		
		chkAddUID.setToolTipText("Adds a unique identifier key field, where a table has no unique key field specified.");
		cmbDelimiter.setToolTipText("Specifies the character(s) which separate columns in the text file.");
		chkOverwriteExisting.setToolTipText("Whether to overwrite existing tables.");
		chkFailOnError.setToolTipText("Whether to terminate import if a failure is encountered.");
		chkHasHeader.setToolTipText("Does your text file include a header line?");
		chkFirstIsKey.setToolTipText("Assume the first field is the key field, while scanning files.");
		txtTextLength.setToolTipText("Specifies the default length of a field."); 
		chkLowerCase.setToolTipText("Whether to convert table/field names to lower case.");
		
		chkHasHeader.addActionListener(this);
		chkHasHeader.setActionCommand("Has Header");
		chkAddUID.addActionListener(this);
		chkAddUID.setActionCommand("UID Changed");
		cmbTables.addActionListener(this);
		cmbTables.setActionCommand("Table Updated");
		cmdScan.addActionListener(this);
		cmdScan.setActionCommand("Scan");
		cmbDelimiter.addActionListener(this);
		cmbDelimiter.setActionCommand("Delimiter Changed");
		cmbDataSource.addActionListener(this);
		cmbDataSource.setActionCommand("Source Changed");
		cmdRenameTable.addActionListener(this);
		cmdRenameTable.setActionCommand("Rename Table");
		
		
		LineLayout lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		
		initFieldsTable();
		initDelimiters();
		initDataSources();
		
		cmbTables.setEnabled(false);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1,1,0.05,0.2,1);
		mainPanel.add(lblSource, c);
		c = new LineLayoutConstraints(1,1,0.25,0.45,1);
		mainPanel.add(cmbDataSource, c);
		c = new LineLayoutConstraints(2,2,0.05,0.3,1);
		mainPanel.add(chkHasHeader, c);
		c = new LineLayoutConstraints(2,2,0.35,0.13,1);
		mainPanel.add(txtHeaderStart, c);
		c = new LineLayoutConstraints(2,2,0.5,0.2,1);
		mainPanel.add(lblDelimiter, c);
		c = new LineLayoutConstraints(2,2,0.7,0.1,1);
		mainPanel.add(txtDelimiter, c);
		c = new LineLayoutConstraints(2,2,0.8,0.1,1);
		mainPanel.add(cmbDelimiter, c);
		c = new LineLayoutConstraints(3,3,0.05,0.4,1);
		mainPanel.add(chkOverwriteExisting, c);
		c = new LineLayoutConstraints(3,3,0.5,0.4,1);
		mainPanel.add(chkFailOnError, c);
		c = new LineLayoutConstraints(4,4,0.05,0.4,1);
		mainPanel.add(chkFirstIsKey, c);
		c = new LineLayoutConstraints(4,4,0.5,0.25,1);
		mainPanel.add(lblTextLength, c);
		c = new LineLayoutConstraints(4,4,0.75,0.2,1);
		mainPanel.add(txtTextLength, c);
		c = new LineLayoutConstraints(5,5,0.05,0.4,1);
		mainPanel.add(chkLowerCase, c);
		chkLowerCase.setSelected(true);
		c = new LineLayoutConstraints(5,5,0.5,0.25,1);
		mainPanel.add(chkAddUID, c);
		c = new LineLayoutConstraints(5,5,0.75,0.2,1);
		mainPanel.add(txtAddUID, c);
		
		c = new LineLayoutConstraints(6,6,0.05,0.5,1);
		mainPanel.add(cmdScan, c);
		c = new LineLayoutConstraints(7,7,0.05,0.2,1);
		mainPanel.add(lblTables, c);
		c = new LineLayoutConstraints(7,7,0.25,0.4,1);
		mainPanel.add(cmbTables, c);
		c = new LineLayoutConstraints(7,7,0.65,0.3,1);
		mainPanel.add(cmdRenameTable, c);
		
		c = new LineLayoutConstraints(8,14,0.05,0.9,1);
		mainPanel.add(scrFieldsTable, c);
		
		lineLayout.setFlexibleComponent(scrFieldsTable);
		
		showDialog();
		updateControls();
		
	}
	
	private void updateControls(){
		
		boolean source_set = (current_source != null);
		
		chkHasHeader.setEnabled(source_set);
		txtHeaderStart.setEnabled(chkHasHeader.isSelected());
		txtDelimiter.setEnabled(source_set);
		cmbDelimiter.setEnabled(source_set);
		
		cmdScan.setEnabled(source_set);
		cmbTables.setEnabled(source_set);
		
		scrFieldsTable.setEnabled(source_set);
		chkAddUID.setEnabled(source_set);
		txtAddUID.setEnabled(source_set);
		
		
	}
	
	private void initFieldsTable(){
		
		fields_table_model = new ImportFieldsTableModel();
		fields_table = new JTable(fields_table_model);
		scrFieldsTable = new JScrollPane(fields_table);
		
		TableColumn col = fields_table.getColumnModel().getColumn(3);
		JComboBox formats = new JComboBox();
		ArrayList<DataType> types = DataTypes.getDataTypeList();
		
		for (int i = 0; i < types.size(); i++){
			formats.addItem(types.get(i).name);
			}
		col.setCellEditor(new DefaultCellEditor(formats));
		
	}
	
	private void initDelimiters(){
		
		cmbDelimiter.addItem("tab");
		cmbDelimiter.addItem("newline");
		cmbDelimiter.addItem("backslash");
		cmbDelimiter.addItem("quote");
		
	}
	
	private void initDataSources(){
		
		cmbDataSource.removeAllItems();
		ArrayList<DataSource> sources =  InterfaceSession.getWorkspace().getDataSources();
		
		for (int i = 0; i < sources.size(); i++){
			cmbDataSource.addItem(sources.get(i));
			}
		
	}
	
	protected void scanTables(){
		
		do_update = false;
		
		cmbTables.removeAllItems();
		file_map.clear();
		
		if (current_source == null) return;
		
		File[] files = options.getFiles();
		if (files == null || files.length == 0){
			cmbTables.setEnabled(false);
			do_update = true;
			return;
			}
		
		DataSourceDriver driver = current_source.getDataSourceDriver();
		
		for (int i = 0; i < files.length; i++){
			
			// See if it can be scanned
			ArrayList<DataField> fields = scanFile(files[i]);
			
			if (fields != null){
				String name = files[i].getName();
				if (chkLowerCase.isSelected())
					name = name.toLowerCase();
				if (name.contains("."))
					name = name.substring(0, name.indexOf("."));
				name = name.replace(" ", "_");
				name = driver.getFriendlyName(name);
				if (chkLowerCase.isSelected())
					name = name.toLowerCase();
				cmbTables.addItem(name);
				file_map.put(name, files[i]);
				ImportFieldsTableModel model = new ImportFieldsTableModel(fields);
				model_map.put(name, model);
				}
			
			}
		
		do_update = true;
		
		cmbTables.setEnabled(true);
		if (cmbTables.getItemCount() > 0)
			cmbTables.setSelectedIndex(0);
	}
	
	/***********************************************
	 * Rename a table to be imported
	 * 
	 * @param old_name
	 * @param new_name
	 */
	protected void renameTable(String old_name, String new_name){
		
		ImportFieldsTableModel model = model_map.get(old_name);
		if (model == null) return;
		
		File file = file_map.get(old_name);
		
		model_map.put(new_name, model);
		file_map.put(new_name, file);
		
		// Update combo box
		do_update = false;
		
		ArrayList<String> items = new ArrayList<String>();
		int count = cmbTables.getItemCount();
		for (int i = 0; i < count; i++){
			String item = (String)cmbTables.getItemAt(i);
			if (item.equals(old_name))
				items.add(new_name);
			else
				items.add(item);
			}
		
		cmbTables.removeAllItems();
		for (int i = 0; i < count; i++){
			cmbTables.addItem(items.get(i));
			}
		
		do_update = true;
		
		cmbTables.setSelectedItem(new_name);
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
	
	// Scan a file with the current delimiter and return a set of fields
	protected ArrayList<DataField> scanFile(File file){

		if (current_source == null) return null;
		
		boolean header = chkHasHeader.isSelected();
		int start = Integer.valueOf(txtHeaderStart.getText()) - 1;
		String delim = getDelimiter();
		if (delim.length() == 0) delim = ",";
		
		DataSourceDriver driver = current_source.getDataSourceDriver();
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			
			// Field names?
			ArrayList<String> names = new ArrayList<String>();
			
			if (header){
				int itr = 0;
				// Find header line
				while (header && line != null && itr++ < start) line = reader.readLine();
				// Can use StringTokenizer here because header shouldn't have blanks
				QuotedStringTokenizer tokens = new QuotedStringTokenizer(line, delim);
				while (tokens.hasMoreTokens()){
					String token = tokens.nextToken();
					if (chkLowerCase.isSelected())
						token = token.toLowerCase();
					names.add(driver.getFriendlyName(token));
					}
				line = reader.readLine();
			}else{
				String[] tokens = line.split(delim + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)",-1);
				//String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
				for (int i = 0; i < tokens.length; i++)
					names.add("column_" + (i + 1));
				}
			
			ArrayList<DataField> fields = new ArrayList<DataField>();
			String name = "field_";
			
			//String[] tokens = line.split(delim, -1);
			String[] tokens = line.split(delim + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
			int n = Math.max(names.size(), tokens.length);
			for (int i = 0; i < n; i++){
				if (i == names.size()){
					InterfaceSession.log("ImportDataTableDialog: Too many fields for table '" + file.getName() + "':\n" +
											i + " > " + (names.size() - 1), 
											LoggingType.Errors);
					reader.close();
					return null;
					}
				String token = "";
				if (i < tokens.length)
					token = tokens[i];
				
				//Remove all quotes
				token = token.replaceAll("\"", "");
				token = token.replaceAll("'", "");
				//String token = tokens.nextToken();
				int type = DataTypes.guessDataType(token);
				String this_name = name + (i + 1);
				if (names.size() > 0)
					this_name = names.get(i);
				DataField field = null;
				if (type != Types.VARCHAR)
					field = new DataField(this_name, type);
				else
					field = new DataField(this_name, type, Integer.valueOf(txtTextLength.getText()));
				if (chkFirstIsKey.isSelected() && i == 0)
					field.setIsKeyField(true);
				fields.add(field);
				//i++;
				}
		
			reader.close();
			
			return fields;
		
		}catch (IOException ex){
			InterfaceSession.log("ImportDataTableDialog: Error scanning file '" + file.getAbsolutePath() + "': " + ex.getMessage(),
								 LoggingType.Errors);
			}
		
		return null;
	}
	
	
	
	@Override
	public void showDialog(){
		
		// Set up files and fields
		ImportDataTableTextOptions options = (ImportDataTableTextOptions)this.options;
				
		cmbDataSource.setSelectedItem(options.getDataSource());
		chkHasHeader.setSelected(options.has_header);
		txtHeaderStart.setEnabled(options.has_header);
		txtDelimiter.setText(getDelimText(options.delimiter));
		chkFailOnError.setSelected(options.fail_on_error);
		chkOverwriteExisting.setSelected(options.overwrite_existing);
		chkAddUID.setSelected(options.add_uid);
		txtAddUID.setText(options.uid_name);
		
		do_update = false;
		cmbTables.removeAllItems();
		file_map.clear();
		
		ArrayList<ArrayList<DataField>> data_fields = options.getDataFields();
		ArrayList<ArrayList<Boolean>> include_fields = options.getIncludeFields();
		ArrayList<String> names = options.getNames();
		
		File[] files = options.getFiles();
		if (files == null || files.length == 0){
			cmbTables.setEnabled(false);
			do_update = true;
			super.showDialog();
			return;
			}
		
		if (data_fields == null){
			// Files were specified but not data fields; do a scan instead
			scanTables();
			super.showDialog();
			return;
			}
		
		for (int i = 0; i < files.length; i++){
			
			// See if it can be scanned
			ArrayList<DataField> fields = null;
			ArrayList<Boolean> includes = null;
			
			if (data_fields != null)
				fields = data_fields.get(i);
			else
				fields = scanFile(files[i]);
			
			if (include_fields != null)
				includes = include_fields.get(i);
			
			if (fields != null){
				String name = null;
				
				if (names != null)
					name = names.get(i);
				else {
					name = files[i].getName();
					if (name.contains("."))
						name = name.substring(0, name.lastIndexOf("."));
					name = name.replace(" ", "_");
					}
				cmbTables.addItem(name);
				file_map.put(name, files[i]);
				ImportFieldsTableModel model = new ImportFieldsTableModel(fields, includes);
				model_map.put(name, model);
				}
			
			}
		
		do_update = true;
		
		cmbTables.setEnabled(true);
		cmbTables.setSelectedIndex(0);
		
		super.showDialog();
	}
	
	public static boolean showDialog(ImportDataTableTextOptions options){
		
		ImportDataTableDialogBox dialog = new ImportDataTableDialogBox(InterfaceSession.getSessionFrame(),
																	   null, options);
		dialog.setVisible(false);
		return dialog.is_ok;
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		String command = e.getActionCommand();
		
		if (command.equals("Has Header")){
			txtHeaderStart.setEnabled(chkHasHeader.isSelected());
			return;
			}
		
		if (command.equals("Table Updated")){
			if (!do_update) return;
			
			String table_name = (String)cmbTables.getSelectedItem();
			ImportFieldsTableModel model = model_map.get(table_name);
			this.fields_table_model.setFromModel(model);
			
			return;
			}
		
		if (command.equals("UID Changed")){
			txtAddUID.setEnabled(chkAddUID.isEnabled());
			return;
			}
		
		if (command.equals("Source Changed")){
			
			current_source = (DataSource)cmbDataSource.getSelectedItem();
			updateControls();
			
			return;
			}
		
		if (command.equals("Scan")){
			
			JFileChooser fc = options.getFileChooser();
			fc.showDialog(InterfaceSession.getSessionFrame(), "OK");
			File[] files = fc.getSelectedFiles();
			if (files == null || files.length == 0) return;
			
			options.setFiles(files);
			
			if (options.getFiles() == null) return;
			scanTables();
			
			return;
			}
		
		if (command.equals("Delimiter Changed")){
			
			String delim = (String)cmbDelimiter.getSelectedItem();
			if (delim.equals("tab"))
				txtDelimiter.setText("{tab}");
			if (delim.equals("newline"))
				txtDelimiter.setText("{newline}");
			if (delim.equals("backslash"))
				txtDelimiter.setText("{backslash}");
			if (delim.equals("quote"))
				txtDelimiter.setText("{quote}");
			
			return;
			}
		
		if (command.equals("Rename Table")){
			
			String old_name = (String)cmbTables.getSelectedItem();
			String new_name = JOptionPane.showInputDialog("Rename table '" + old_name + "' to:");
			if (new_name == null || new_name.equals(old_name)) return;
			
			renameTable(old_name, new_name);
			
			return;
			}
		
		if (command.equals(DLG_CMD_OK)){
			
			// Set options
			ImportDataTableTextOptions options = (ImportDataTableTextOptions)this.options;
			
			options.delimiter = getDelimiter();
			options.has_header = chkHasHeader.isSelected();
			if (options.has_header)
				options.start_at = Integer.valueOf(txtHeaderStart.getText());
			options.fail_on_error = chkFailOnError.isSelected();
			options.overwrite_existing = chkOverwriteExisting.isSelected();
			options.add_uid = chkAddUID.isSelected();
			if (options.add_uid)
				options.uid_name = txtAddUID.getText();
			
			ArrayList<String> tables = new ArrayList<String>();
			
			int n = cmbTables.getItemCount();
			for (int i = 0; i < n; i++)
				tables.add((String)cmbTables.getItemAt(i));
			
			File[] files = new File[n];
			for (int i = 0; i < n; i++)
				files[i] = file_map.get(tables.get(i));
			
			ArrayList<ArrayList<DataField>> data_fields = new ArrayList<ArrayList<DataField>>();
			ArrayList<ArrayList<Boolean>> include_fields = new ArrayList<ArrayList<Boolean>>();
			
			for (int i = 0; i < n; i++){
				ImportFieldsTableModel model = model_map.get(tables.get(i));
				data_fields.add(model.fields);
				include_fields.add(model.include);
				}
			
			options.setDataSource((DataSource)cmbDataSource.getSelectedItem());
			options.setFiles(files);
			options.setNames(tables);
			options.setDataFields(data_fields, include_fields);
			
			is_ok = true;
			
			this.setVisible(false);
			
			return;
			}
	
		super.actionPerformed(e);
		
	}
	
	
	class ImportFieldsTableModel extends DefaultTableModel{
		
		ArrayList<DataField> fields = new ArrayList<DataField>();
		ArrayList<Boolean> include = new ArrayList<Boolean>();
		
		public ImportFieldsTableModel(){
			
		}
		
		public ImportFieldsTableModel(ArrayList<DataField> fields){
			this.fields = fields;
			include = new ArrayList<Boolean>();
			for (int i = 0; i < fields.size(); i++)
				include.add(true);
		}
		
		public ImportFieldsTableModel(ArrayList<DataField> fields, ArrayList<Boolean> include){
			this.fields = fields;
			this.include = include;
		}
		
		public void setFromModel(ImportFieldsTableModel model){
			this.fields = new ArrayList<DataField>(model.fields);
			this.include = new ArrayList<Boolean>(model.include);
			this.fireTableDataChanged();
		}
		
		public void setModel(ArrayList<DataField> fields){
			ArrayList<Boolean> include = new ArrayList<Boolean>();
			for (int i = 0; i < fields.size(); i++)
				include.add(true);
			setModel(fields, include);
		}
		
		public void setModel(ArrayList<DataField> fields, ArrayList<Boolean> include){
			this.fields = fields;
			this.include = include;
			this.fireTableDataChanged();
		}
		
		@Override
		public int getRowCount() {
			if (fields == null) return 0;
			return fields.size();
		}
		
		@Override
		public int getColumnCount() {
			return 8;
		}

		@Override
		public String getColumnName(int column) {
			switch (column){
				case 0:
					return "Column";
				case 1:
					return "Incl?";
				case 2:
					return "Name";
				case 3:
					return "Data Type";
				case 4:
					return "Length";
				case 5:
					return "Key?";
				case 6:
					return "Uniq?";
				case 7:
					return "Req?";
				}
			return "?";
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column != 0;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row >= include.size()) return null;
			switch (column){
				case 0:
					return row + 1;
				case 1:
					return include.get(row);
				case 2:
					return fields.get(row).getName();
				case 3:
					return fields.get(row).getDataTypeStr();
				case 4:
					return fields.get(row).getLength();
				case 5:
					return fields.get(row).isKeyField();
				case 6:
					return fields.get(row).isUnique();
				case 7:
					return fields.get(row).isRequired();
				}
			return null;
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (row >= include.size()) return;
			  switch (column){
				  case 1:
					  include.set(row, (Boolean)value);
					  break;
					  
				  case 2:
					  fields.get(row).setName((String)value);
					  break;
					  
				  case 3:
					  String s_type = (String)value;
					  int i_type = DataTypes.getSQLStrValue(s_type);
					  fields.get(row).setDataType(i_type);
					  break;
					  
				  case 4:
					  fields.get(row).setLength((Integer)value);
					  break;
					  
				  case 5:
					  fields.get(row).setIsKeyField((Boolean)value);
					  break;
					  
				  case 6:
					  fields.get(row).setIsUnique((Boolean)value);
					  break;
					  
				  case 7:
					  fields.get(row).setIsRequired((Boolean)value);
					  break;
					  
			  	  }
			  
			  this.fireTableDataChanged();
			  
		  }

		  @Override
		  public Class<?> getColumnClass(int column) {
			  switch (column){
			  case 0:
				  return Integer.class;
			  case 1:
				  return Boolean.class;
			  case 2:
				  return String.class;
			  case 3:
				  return String.class;
			  case 4:
				  return Integer.class;
			  case 5:
				  return Boolean.class;
			  case 6:
				  return Boolean.class;
			  case 7:
				  return Boolean.class;
			  }
		  return Object.class;
		  }
			
	}
	
}