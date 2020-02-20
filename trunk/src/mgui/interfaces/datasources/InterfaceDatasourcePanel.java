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

package mgui.interfaces.datasources;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.DefaultCaret;

import mgui.datasources.DataField;
import mgui.datasources.DataQuery;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataTable;
import mgui.datasources.util.DataSourceFunctions;
import mgui.interfaces.DisplayPanelEvent;
import mgui.interfaces.DisplayPanelListener;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBoxRenderer;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.layouts.CategoryLayout;
import mgui.interfaces.layouts.CategoryLayoutConstraints;
import mgui.interfaces.layouts.CategoryTitle;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.tables.InterfaceDataTable;
import mgui.io.DefaultIOOptions;
import mgui.io.domestic.datasources.DataSourceLoader;
import mgui.io.domestic.datasources.DataSourceWriter;
import mgui.io.domestic.datasources.ExportDataTableDialogBox;
import mgui.io.domestic.datasources.ExportDataTableOptions;
import mgui.io.domestic.datasources.ExportDataTableTextOptions;
import mgui.io.domestic.datasources.ExportDataTableWriter;
import mgui.io.domestic.datasources.ImportDataTableDialogBox;
import mgui.io.domestic.datasources.ImportDataTableTextLoader;
import mgui.io.domestic.datasources.ImportDataTableTextOptions;


/*********************************
 * Interface panel for displaying, querying, and managing tables and queries in a 
 * connected data source. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */

public class InterfaceDatasourcePanel extends InterfacePanel implements ActionListener,
																		ListSelectionListener,
																		DisplayPanelListener{

	/******** SOURCES *****************************************/
	
	CategoryTitle lblSources = new CategoryTitle("SOURCES");
	JLabel lblSourcesCurrent = new JLabel("Current source:");
	InterfaceComboBox cmbSourcesCurrent = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
	JButton cmdSourcesDefineEdit = new JButton("Define");
	JButton cmdSourcesCreateUpdate = new JButton("Create");
	JButton cmdSourcesRemove = new JButton("Remove");
	JButton cmdSourcesConnect = new JButton("Connect");
	JButton cmdSourcesLoad = new JButton("Load");
	JButton cmdSourcesSave = new JButton("Save");
	JButton cmdSourcesRefresh = new JButton("Refresh");
	JButton cmdSourcesDrop = new JButton("Drop");
	
	/******** TABLES ******************************************/
	
	CategoryTitle lblTables = new CategoryTitle("TABLES");
	JScrollPane scrTables = new JScrollPane();
	JTable tblTables;
	TableListModel data_tables_model;
	JButton cmdTablesAddNew = new JButton("Add New");
	JButton cmdTablesRemove = new JButton("Remove");
	JButton cmdTablesEdit = new JButton("Edit");
	JButton cmdTablesShow = new JButton("Show");
	JLabel lblTablesShow = new JLabel("Show in window:");
	InterfaceComboBox cmbTablesShow = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 200);
	
	/******** FIELDS ******************************************/
	
	CategoryTitle lblFields = new CategoryTitle("FIELDS");
	JScrollPane scrFields = new JScrollPane();
	JTable tblFields;
	FieldListModel data_fields_model;
	JButton cmdFieldsAddNew = new JButton("Add New");
	JButton cmdFieldsRemove = new JButton("Remove");
	JButton cmdFieldsEdit = new JButton("Edit");
	
	/******** QUERIES *****************************************/
	
	CategoryTitle lblQueries = new CategoryTitle("QUERIES");
	JScrollPane scrQueries = new JScrollPane();
	JList queries_list;
	DefaultListModel queries_list_model;
	JTextArea txtQuerySQL = new JTextArea();
	JButton cmdQueriesAddNew = new JButton("Add New");
	JButton cmdQueriesRemove = new JButton("Remove");
	JButton cmdQueriesEdit = new JButton("Edit");
	JButton cmdQueriesShow = new JButton("Show");
	JButton cmdQueriesExecute = new JButton("Execute");
	JLabel lblQueriesShow = new JLabel("Show in window:");
	InterfaceComboBox cmbQueriesShow = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 200);
	
	/******** I/O *********************************************/
	
	CategoryTitle lblIO = new CategoryTitle("I/O");
	
	JButton cmdIOImportData = new JButton("Import data..");
	JButton cmdIOExportData = new JButton("Export data..");
	
	/******** ADMIN *****************************************/
	
	CategoryTitle lblAdmin = new CategoryTitle("ADMIN");
	JButton cmdAdminDefineDrivers = new JButton("Define drivers..");
	
	/******** LOCAL *******************************************/
	
	DataSource current_source;
	DataSourceOptions current_source_options;
	DataTable current_table;
	DataField current_field;
	DataQuery current_query;
	
	boolean doUpdate = true;
	
	/******** CONSTANTS ***************************************/
	
	public static final String NEW_SOURCE = "<-NEW SOURCE->";
	
	public InterfaceDatasourcePanel(){
		
		init();
		
	}
	
	@Override
	protected void init() {
		_init();
		
		setLayout(new CategoryLayout(InterfaceEnvironment.getLineHeight(), 5, 200, 10));
		
		cmdAdminDefineDrivers.addActionListener(this);
		cmdAdminDefineDrivers.setActionCommand("Admin Define Drivers");
		
		cmbSourcesCurrent.addActionListener(this);
		cmbSourcesCurrent.setActionCommand("Source Changed");
		cmdSourcesDefineEdit.addActionListener(this);
		cmdSourcesDefineEdit.setActionCommand("Source Define Edit");
		cmdSourcesCreateUpdate.addActionListener(this);
		cmdSourcesCreateUpdate.setActionCommand("Source Create Update");
		cmdSourcesConnect.addActionListener(this);
		cmdSourcesConnect.setActionCommand("Source Connect Disconnect");
		cmdSourcesLoad.addActionListener(this);
		cmdSourcesLoad.setActionCommand("Source Load");
		cmdSourcesSave.addActionListener(this);
		cmdSourcesSave.setActionCommand("Source Save");
		cmdSourcesRefresh.addActionListener(this);
		cmdSourcesRefresh.setActionCommand("Source Refresh");
		cmdSourcesRemove.addActionListener(this);
		cmdSourcesRemove.setActionCommand("Source Remove");
		cmdSourcesDrop.addActionListener(this);
		cmdSourcesDrop.setActionCommand("Source Drop");
		
		cmdTablesAddNew.addActionListener(this);
		cmdTablesAddNew.setActionCommand("Tables Add New");
		cmdTablesEdit.addActionListener(this);
		cmdTablesEdit.setActionCommand("Tables Edit");
		cmdTablesRemove.addActionListener(this);
		cmdTablesRemove.setActionCommand("Tables Remove");
		cmdTablesShow.addActionListener(this);
		cmdTablesShow.setActionCommand("Tables Show");
		
		cmdFieldsAddNew.addActionListener(this);
		cmdFieldsAddNew.setActionCommand("Fields Add New");
		cmdFieldsRemove.addActionListener(this);
		cmdFieldsRemove.setActionCommand("Fields Remove");
		
		cmdQueriesAddNew.addActionListener(this);
		cmdQueriesAddNew.setActionCommand("Queries Add New");
		cmdQueriesEdit.addActionListener(this);
		cmdQueriesEdit.setActionCommand("Queries Edit");
		cmdQueriesShow.addActionListener(this);
		cmdQueriesShow.setActionCommand("Queries Show");
		cmdQueriesExecute.addActionListener(this);
		cmdQueriesExecute.setActionCommand("Queries Execute");
		
		cmdIOImportData.addActionListener(this);
		cmdIOImportData.setActionCommand("I/O Import Data");
		cmdIOExportData.addActionListener(this);
		cmdIOExportData.setActionCommand("I/O Export Data");
		
		initDataTables();
		initDataFields();
		initQueries();
		
		CategoryLayoutConstraints c = new CategoryLayoutConstraints();
		
		add(lblSources, c);
		lblSources.setParentObj(this);
		c = new CategoryLayoutConstraints("SOURCES", 1, 1, 0.05, 0.3, 1);
		add(lblSourcesCurrent, c);
		c = new CategoryLayoutConstraints("SOURCES", 1, 1, 0.35, 0.6, 1);
		add(cmbSourcesCurrent, c);
		c = new CategoryLayoutConstraints("SOURCES", 2, 2, 0.05, 0.43, 1);
		add(cmdSourcesDefineEdit, c);
		c = new CategoryLayoutConstraints("SOURCES", 2, 2, 0.52, 0.43, 1);
		add(cmdSourcesCreateUpdate, c);
		c = new CategoryLayoutConstraints("SOURCES", 3, 3, 0.05, 0.43, 1);
		add(cmdSourcesRemove, c);
		c = new CategoryLayoutConstraints("SOURCES", 3, 3, 0.52, 0.43, 1);
		add(cmdSourcesConnect, c);
		c = new CategoryLayoutConstraints("SOURCES", 4, 4, 0.05, 0.43, 1);
		add(cmdSourcesLoad, c);
		c = new CategoryLayoutConstraints("SOURCES", 4, 4, 0.52, 0.43, 1);
		add(cmdSourcesSave, c);
		c = new CategoryLayoutConstraints("SOURCES", 5, 5, 0.05, 0.43, 1);
		add(cmdSourcesRefresh, c);
		c = new CategoryLayoutConstraints("SOURCES", 5, 5, 0.52, 0.43, 1);
		add(cmdSourcesDrop, c);
		
		
		c = new CategoryLayoutConstraints();
		add(lblTables, c);
		lblTables.setParentObj(this);
		c = new CategoryLayoutConstraints("TABLES", 1, 8, 0.05, 0.9, 1);
		add(scrTables, c);
		c = new CategoryLayoutConstraints("TABLES", 9, 9, 0.05, 0.3, 1);
		add(lblTablesShow, c);
		c = new CategoryLayoutConstraints("TABLES", 9, 9, 0.35, 0.6, 1);
		add(cmbTablesShow, c);
		c = new CategoryLayoutConstraints("TABLES", 10, 10, 0.05, 0.43, 1);
		add(cmdTablesAddNew, c);
		c = new CategoryLayoutConstraints("TABLES", 10, 10, 0.52, 0.43, 1);
		add(cmdTablesRemove, c);
		c = new CategoryLayoutConstraints("TABLES", 11, 11, 0.05, 0.43, 1);
		add(cmdTablesEdit, c);
		c = new CategoryLayoutConstraints("TABLES", 11, 11, 0.52, 0.43, 1);
		add(cmdTablesShow, c);
		
		c = new CategoryLayoutConstraints();
		add(lblFields, c);
		lblFields.setParentObj(this);
		c = new CategoryLayoutConstraints("FIELDS", 1, 8, 0.05, 0.9, 1);
		add(scrFields, c);
		c = new CategoryLayoutConstraints("FIELDS", 9, 9, 0.05, 0.43, 1);
		add(cmdFieldsAddNew, c);
		c = new CategoryLayoutConstraints("FIELDS", 9, 9, 0.52, 0.43, 1);
		add(cmdFieldsRemove, c);
		c = new CategoryLayoutConstraints("FIELDS", 10, 10, 0.05, 0.43, 1);
		add(cmdFieldsEdit, c);
		
		c = new CategoryLayoutConstraints();
		add(lblQueries, c);
		lblQueries.setParentObj(this);
		c = new CategoryLayoutConstraints("QUERIES", 1, 7, 0.05, 0.9, 1);
		add(scrQueries, c);
		c = new CategoryLayoutConstraints("QUERIES", 8, 9, 0.05, 0.9, 1);
		add(txtQuerySQL, c);
		txtQuerySQL.setEditable(false);
		
		// Prevents auto-scrolling
		DefaultCaret caret = (DefaultCaret) txtQuerySQL.getCaret();
	    caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		c = new CategoryLayoutConstraints("QUERIES", 10, 10, 0.05, 0.3, 1);
		add(lblQueriesShow, c);
		c = new CategoryLayoutConstraints("QUERIES", 10, 10, 0.35, 0.6, 1);
		add(cmbQueriesShow, c);
		c = new CategoryLayoutConstraints("QUERIES", 11, 11, 0.05, 0.43, 1);
		add(cmdQueriesAddNew, c);
		c = new CategoryLayoutConstraints("QUERIES", 11, 11, 0.52, 0.43, 1);
		add(cmdQueriesRemove, c);
		c = new CategoryLayoutConstraints("QUERIES", 12, 12, 0.05, 0.43, 1);
		add(cmdQueriesEdit, c);
		c = new CategoryLayoutConstraints("QUERIES", 12, 12, 0.52, 0.43, 1);
		add(cmdQueriesShow, c);
		c = new CategoryLayoutConstraints("QUERIES", 13, 13, 0.52, 0.43, 1);
		add(cmdQueriesExecute, c);
		
		c = new CategoryLayoutConstraints();
		add(lblIO, c);
		lblIO.setParentObj(this);
		c = new CategoryLayoutConstraints("I/O", 1, 1, 0.1, 0.8, 1);
		add(cmdIOImportData, c);
		c = new CategoryLayoutConstraints("I/O", 2, 2, 0.1, 0.8, 1);
		add(cmdIOExportData, c);
		
		c = new CategoryLayoutConstraints();
		add(lblAdmin, c);
		lblAdmin.setParentObj(this);
		c = new CategoryLayoutConstraints("ADMIN", 1, 1, 0.15, 0.7, 1);
		add(cmdAdminDefineDrivers, c);
	}
	
	@Override
	public void displayPanelChanged(DisplayPanelEvent e) {
		this.showPanel();
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceDatasourcePanel.class.getResource("/mgui/resources/icons/data_source_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/data_source_20.png");
		return null;
	}
	
	void setCurrentDataSource(DataSource source){
		current_source = source;
		current_source_options = null;
		updateDataTables();
		updateQueries();
	}
	
	void updateControls(){
		boolean source_is_new = cmbSourcesCurrent.getSelectedItem() != null &&
								cmbSourcesCurrent.getSelectedItem().equals(NEW_SOURCE);
		boolean source_is_set = current_source != null;
		boolean source_is_connected = source_is_set && current_source.isConnected();
		boolean source_is_creatable = current_source_options != null &&
									  current_source_options.source != null;
		boolean table_is_set = current_table != null;
		boolean field_is_set = current_field != null;
		boolean query_is_set = current_query != null;
		boolean table_window_is_set = cmbTablesShow.getSelectedItem() != null;
		boolean query_window_is_set = cmbQueriesShow.getSelectedItem() != null;
		
		cmdSourcesCreateUpdate.setEnabled(source_is_creatable);
		if (source_is_new)
			cmdSourcesCreateUpdate.setText("Create");
		else
			cmdSourcesCreateUpdate.setText("Update");
		cmdSourcesConnect.setEnabled(source_is_set);
		if (source_is_connected)
			cmdSourcesConnect.setText("Disconnect");
		else
			cmdSourcesConnect.setText("Connect");
		cmdSourcesDefineEdit.setEnabled(source_is_set || source_is_new);
		if (source_is_new)
			cmdSourcesDefineEdit.setText("Define");
		else
			cmdSourcesDefineEdit.setText("Edit");
		cmdSourcesRemove.setEnabled(!source_is_new);
		cmdSourcesRefresh.setEnabled(source_is_set);
		
		cmdTablesAddNew.setEnabled(source_is_connected);
		cmdTablesEdit.setEnabled(table_is_set);
		cmdTablesRemove.setEnabled(table_is_set);
		cmdTablesShow.setEnabled(table_is_set && table_window_is_set);
		
		cmdFieldsAddNew.setEnabled(table_is_set);
		cmdFieldsEdit.setEnabled(field_is_set);
		cmdFieldsRemove.setEnabled(field_is_set);
				
		if (query_is_set)
			txtQuerySQL.setText(current_query.getSQLStatement());
		else
			txtQuerySQL.setText("");
		
		cmdQueriesAddNew.setEnabled(source_is_connected);
		cmdQueriesEdit.setEnabled(query_is_set);
		cmdQueriesRemove.setEnabled(query_is_set);
		cmdQueriesShow.setEnabled(query_is_set && query_window_is_set);
		
	}
	
	void updateDataSources(){
		doUpdate = false;
		
		cmbSourcesCurrent.removeAllItems();
		cmbSourcesCurrent.addItem(NEW_SOURCE);
		ArrayList<DataSource> sources = InterfaceSession.getWorkspace().getDataSources();
		
		for (int i = 0; i < sources.size(); i++)
			cmbSourcesCurrent.addItem(sources.get(i));
		
		if (current_source != null){
			cmbSourcesCurrent.setSelectedItem(current_source);
		}else{
			current_table = null;
			current_field = null;
			doUpdate = true;
			if (cmbSourcesCurrent.getSelectedItem().equals(NEW_SOURCE))
				setCurrentDataSource(null);
			else
				setCurrentDataSource((DataSource)cmbSourcesCurrent.getSelectedItem());
			return;
			}
		
		updateControls();
		doUpdate = true;
	}
	
	void updateDataTables(){
		doUpdate = false;
		
		try{
		if (current_source != null && current_source.isConnected()){
			data_tables_model.setTables(current_source.getTableSet().getTables());
		}else{
			data_tables_model.setTables(new ArrayList<DataTable>());
			}
		}catch (DataSourceException ex){
			JOptionPane.showMessageDialog(getParent(), 
					  "Problem reading from '" + current_source.getName() + "'!", 
					  "Data Source Error", 
					  JOptionPane.ERROR_MESSAGE);
			}
		
		if (current_table != null){
			int pos = data_tables_model.getTablePos(current_table);
			if (pos > -1)
				tblTables.getSelectionModel().setSelectionInterval(pos, pos);
			else{
				pos = tblTables.getSelectedRow();
				if (pos > -1)
					current_table = data_tables_model.getTableAt(pos);
				}
		}else{
			current_field = null;
			int pos = tblTables.getSelectedRow();
			if (pos > -1)
				current_table = data_tables_model.getTableAt(pos);
			}
		
		doUpdate = true;
		
		updateDataFields();
	}
	
	void updateDataFields(){
		doUpdate = false;
		
		if (current_table != null){
			data_fields_model.setFields(current_table.getFieldList());
		}else{
			data_fields_model.setFields(new ArrayList<DataField>());
			}
		
		updateControls();
		doUpdate = true;
	}
	
	void updateQueries(){
		doUpdate = false;
		
		if (this.current_source == null){
			this.queries_list_model.removeAllElements();
			doUpdate = true;
			return;
		}else{
			this.queries_list_model.removeAllElements();
			ArrayList<DataQuery> queries = current_source.getDataQueries();
			Collections.sort(queries, new Comparator<DataQuery>(){
					public int compare(DataQuery q1, DataQuery q2){
						return q1.getName().compareTo(q2.getName());
					}
				});
			for (int i = 0; i < queries.size(); i++)
				queries_list_model.addElement(queries.get(i));
			}
		
		if (current_query != null)
			queries_list.setSelectedValue(current_query, false);
		
		doUpdate = true;
	}
	
	void initDataTables(){
		try{
			if (current_source != null){
				data_tables_model = new TableListModel(current_source.getTableSet().getTables());
			}else{
				data_tables_model = new TableListModel();
				}
		}catch (DataSourceException ex){
			JOptionPane.showMessageDialog(getParent(), 
					  "Problem reading from '" + current_source.getName() + "'!", 
					  "Data Source Error", 
					  JOptionPane.ERROR_MESSAGE);
			return;
			}
		
		tblTables = new JTable(data_tables_model);
		tblTables.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrTables = new JScrollPane(tblTables);
		
		tblTables.getSelectionModel().addListSelectionListener(this);
	}
	
	void initDataFields(){
		if (current_table != null){
			data_fields_model = new FieldListModel(current_table.getFieldList());
		}else{
			data_fields_model = new FieldListModel();
			}
		
		tblFields = new JTable(data_fields_model);
		tblFields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrFields = new JScrollPane(tblFields);
		
		tblFields.getSelectionModel().addListSelectionListener(this);
	}
	
	void initQueries(){
		
		txtQuerySQL.setWrapStyleWord(true);
		txtQuerySQL.setLineWrap(true);
		
		this.queries_list_model = new DefaultListModel();
		this.queries_list = new JList(queries_list_model);
		queries_list.setCellRenderer(new InterfaceComboBoxRenderer(true));
		queries_list.addListSelectionListener(this);
		
		scrQueries = new JScrollPane(queries_list);
		
	}
	
	public void showPanel(){
		this.updateDisplay();
	}
	
	public void updateDisplay(){
		updateWindowList();
		updateDataSources();
		updateQueries();
	}
	
	void updateWindowList(){
		cmbTablesShow.removeAllItems();
		cmbQueriesShow.removeAllItems();
		ArrayList<InterfaceGraphicWindow> panels = InterfaceSession.getDisplayPanel().getWindows();
		for (int i = 0; i < panels.size(); i++)
			if (panels.get(i).getPanel() instanceof InterfaceDataTable){
				cmbTablesShow.addItem(panels.get(i));
				cmbQueriesShow.addItem(panels.get(i));
				}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		if (!doUpdate) return;
		
		if (e.getSource().equals(tblTables.getSelectionModel())){
			//respond to table selection
			current_table = data_tables_model.getTableAt(tblTables.getSelectedRow());
			updateDataFields();
			return;
			}
		
		if (e.getSource().equals(tblFields.getSelectionModel())){
			current_field = data_fields_model.getFieldAt(tblFields.getSelectedRow());
			updateControls();
			return;
			}
		
		if (e.getSource().equals(queries_list)){
			current_query = (DataQuery)queries_list_model.getElementAt(queries_list.getSelectedIndex());
			updateControls();
			return;
			}
		
	}
	
	protected boolean refreshDataSource(){
		if (current_source == null) return false;
		
		try{
			current_source.setTableSet(false);
			updateDataTables();
			return true;
		}catch (DataSourceException ex){
			
			return false;
			}
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().startsWith("Admin")){
			
			if (e.getActionCommand().endsWith("Define Drivers")){
				
				DataSourceDriverDialogBox.showDialog(new DataSourceDriverOptions());
				
				return;
				}
			
			return;
			}
		
		if (e.getActionCommand().startsWith("Source")){
			
			if (e.getActionCommand().endsWith("Drop")){
				if ( JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
												  "This will completely remove the data source from memory; are you sure?", 
												  "Drop Data Source", 
												  JOptionPane.YES_NO_OPTION, 
												  JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
					return;
				
				if (current_source == null) return;
				
				if (current_source.delete()){
					InterfaceSession.getWorkspace().removeDataSource(current_source);
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  "Successfully dropped data source '" + current_source.getName() + "'.", 
							  "Drop Data Source", 
							  JOptionPane.INFORMATION_MESSAGE);
					
					current_source = null;
					updateDisplay();
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not drop data source '" + current_source.getName() + "'.", 
												  "Drop Data Source", 
												  JOptionPane.ERROR_MESSAGE);
					}
				
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove")){
				
				if (current_source == null) return;
				
				if ( JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
						  "Close and remove data source '" + current_source.getName() + "'?", 
						  "Remove Data Source", 
						  JOptionPane.YES_NO_OPTION, 
						  JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
					return;
				
				// If connected, try to disconnect
				if (current_source.isConnected() && !current_source.disconnect()){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not disconnect '" + current_source.getName() + "'!", 
												  "Remove Data Source", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				InterfaceSession.getWorkspace().removeDataSource(current_source);
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Removed data source '" + current_source.getName() + "'.", 
											  "Remove Data Source", 
											  JOptionPane.INFORMATION_MESSAGE);
				current_source = null;
				updateDataSources();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Changed")){
				if (!doUpdate){
					return;
					}
				if (cmbSourcesCurrent.getSelectedItem().equals(NEW_SOURCE)){
					setCurrentDataSource(null);
					return;
					}
				setCurrentDataSource((DataSource)cmbSourcesCurrent.getSelectedItem());
				return;
				}
			
			if (e.getActionCommand().endsWith("Define Edit")){
				if (cmbSourcesCurrent.getSelectedItem() == null) return;
				if (current_source_options == null){
					if (cmbSourcesCurrent.getSelectedItem().equals(NEW_SOURCE)){
						current_source_options = new DataSourceOptions(new DataSource());
					}else{
						current_source_options = new DataSourceOptions(current_source);
						}
					}
				
				DataSourceDialogBox.showPanel(current_source_options);
				updateControls();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Create Update")){
				if (cmdSourcesCreateUpdate.getText().equals("Create")){
					//Create new
					if (current_source_options == null || current_source_options.source == null)	//shouldn't happen
						return;
					
					while (!InterfaceSession.getWorkspace().addDataSource(current_source_options.source)){
						String new_name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
																	"A data source by that name already exists. Revised name?", 
																	current_source_options.source.getName() + "1");
						if (new_name == null) return;
						current_source_options.source.setName(new_name);
						}
					DataSource source = current_source_options.source;
					showPanel();
					cmbSourcesCurrent.setSelectedItem(source);
					
					// Now connect
					try{
						if (current_source.connect()){
							updateControls();
							updateDataTables();
							return;
							}
					}catch (DataSourceException ex){}
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Could not connect '" + current_source.getName() + "'", 
													  "Connect Data Source", 
													  JOptionPane.ERROR_MESSAGE);
						
					return;
					
				}else{
					//Update existing
					if (current_source_options == null || current_source_options.source == null)	//shouldn't happen
						return;
					
					current_source.setFromDataSource(current_source_options.source);
					showPanel();
					}
				
				}
			
			if (e.getActionCommand().endsWith("Load")){
				
				DefaultIOOptions options = new DefaultIOOptions();
				JFileChooser jc = new JFileChooser();
				jc.setDialogTitle("Load Data Source(s)");
				jc.setFileFilter(new FileFilter(){
					public boolean accept(File f) {
						return f.isDirectory() || f.getName().endsWith(".src");
					}

					public String getDescription() {
						return "Mgui Data Sources (*.src)";
					}
				});
				
				jc.setMultiSelectionEnabled(true);
				if (jc.showOpenDialog(InterfaceSession.getSessionFrame()) == JFileChooser.CANCEL_OPTION)
					return;
				
				File[] files = jc.getSelectedFiles();
				if (files == null || files.length == 0) return;
				boolean success = true;
				DataSourceLoader loader = new DataSourceLoader();
				for (int i = 0; i < files.length; i++){
					try{
						loader.setFile(files[i]);
						DataSource new_source = (DataSource)loader.loadObject(options);
					
						if (new_source == null){
							success = false;
							InterfaceSession.log("DataSourceLoader: could not load '" + files[i].getAbsolutePath() + "'.");
						}else{
							boolean ok = true;
							while (!InterfaceSession.getWorkspace().addDataSource(new_source)){
								String new_name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
																			"A data source by that name already exists. Revised name?", 
																			new_source.getName() + "1");
								if (new_name == null){
									ok = false;
									break;
									}
								new_source.setName(new_name);
								}
							if (ok){
								success &= new_source.connect();
								if (i == 0) current_source = new_source;
							}else{
								success = false;
								}
							}
					}catch (IOException ex){
						InterfaceSession.log("DataSourceLoader: could not load '" + files[i].getAbsolutePath() + "'.",
											 LoggingType.Errors);
						success = false;
					}catch (DataSourceException ex){
						InterfaceSession.log("DataSourceLoader: could not connect '" + files[i].getName() + "'.",
								 			 LoggingType.Errors);
						success = false;
						}
					}
				if (!success){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Problem loading one or more data sources", 
												  "Load Data Source(s)",
												  JOptionPane.ERROR_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Data source(s) loaded", 
												  "Load Data Source(s)",
												  JOptionPane.INFORMATION_MESSAGE);
					}
				
				this.refreshDataSource();
				this.showPanel();
				return;
				}
			
			if (e.getActionCommand().endsWith("Save")){
				
				if (this.current_source == null) return;
				JFileChooser jc = new JFileChooser();
				jc.setDialogTitle("Write Data Source");
				jc.setFileFilter(new FileFilter(){
					public boolean accept(File f) {
						return f.isDirectory() || f.getName().endsWith(".src");
					}

					public String getDescription() {
						return "Mgui Data Sources (*.src)";
					}
				});
				
				jc.setMultiSelectionEnabled(true);
				if (jc.showSaveDialog(InterfaceSession.getSessionFrame()) == JFileChooser.CANCEL_OPTION)
					return;
				
				DataSourceWriter writer = new DataSourceWriter(jc.getSelectedFile());
				
				try{
					writer.writeDataSource(current_source);
				}catch (IOException ex){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Problem writing data source", 
												  "Write Data Source",
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Data source saved", 
											  "Write Data Source",
											  JOptionPane.INFORMATION_MESSAGE);
				return;
				}
			
			if (e.getActionCommand().endsWith("Connect Disconnect")){
				
				if (current_source == null) return;
				
				if (cmdSourcesConnect.getText().equals("Connect")){
					//Connect source
					try{
						if (current_source.connect()){
							updateControls();
							updateDataTables();
							JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
														  "Connected '" + current_source.getName() + "'", 
														  "Connect Data Source", 
														  JOptionPane.INFORMATION_MESSAGE);
							return;
							}
					}catch (DataSourceException ex){}
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Could not connect '" + current_source.getName() + "'", 
													  "Connect Data Source", 
													  JOptionPane.ERROR_MESSAGE);
					return;
				}else{
					//Disconnect source
					if (current_source.disconnect()){
						updateControls();
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Disconnected '" + current_source.getName() + "'", 
													  "Disconnect Data Source", 
													  JOptionPane.INFORMATION_MESSAGE);
						return;
						}
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not disconnect '" + current_source.getName() + "'", 
												  "Disconnect Data Source", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				}
			
			// Refresh tables from metadata
			if (e.getActionCommand().endsWith("Refresh")){
				
				if (current_source == null) return;
				
				if (!refreshDataSource()){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not refresh data source '" + current_source.getName() + "'. Check log.", 
												  "Refresh Data Source", 
												  JOptionPane.ERROR_MESSAGE);
					}
				
				this.showPanel();
				
				return;
				}
			
			}
		
		
		if (e.getActionCommand().startsWith("Tables")){
			
			if (e.getActionCommand().endsWith("Add New")){
				DataTable new_table = EditDataTableDialog.showDialog(InterfaceSession.getSessionFrame(), 
																	 new DataTableOptions(this.current_source));
				
				if (new_table == null) return;
				
				if (current_source.addDataTable(new_table)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Created table '" + new_table.getName() + "'", 
												  "Create Data Table", 
												  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not create new table.", 
												  "Create Data Table", 
												  JOptionPane.ERROR_MESSAGE);
					}
				
				refreshDataSource();
				return;
				}
			
			if (e.getActionCommand().endsWith("Edit")){
				
				// Get selected table
				if (current_source == null) return;
				
				int row = tblTables.getSelectedRow();
				if (row < 0) return;
				
				DataTable table = data_tables_model.getTableAt(row);
				if (table == null) return;
				
				HashMap<String,String> changed_names = new HashMap<String,String>();
				DataTable edited_table = EditDataTableDialog.showDialog(InterfaceSession.getSessionFrame(), 
						 												new DataTableOptions(current_source, table),
						 												changed_names);

				if (edited_table == null) return;
				
				int ok = JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
						  "This operation may alter existing data in table '" + table.getName() + "'. Proceed anyway?", 
						  "Edit Table", 
						  JOptionPane.YES_NO_OPTION, 
						  JOptionPane.WARNING_MESSAGE);

				if (ok != JOptionPane.YES_OPTION) return;
				
				// Get list of SQL commands
				ArrayList<String> statements = DataSourceFunctions.getEditStatements(current_source, table, edited_table, changed_names);
				
				try{
					for (int i = 0; i < statements.size(); i++){
						current_source.executeStatement(statements.get(i));
						}
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Changes successful.", 
												  "Edit Data Table", 
												  JOptionPane.INFORMATION_MESSAGE);
				}catch (SQLException ex){
					InterfaceSession.log("InterfaceDatasourcePanel: Exception updating table: " + ex.getMessage(), 
										 LoggingType.Errors);
					InterfaceSession.handleException(ex);
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not update table; partial updates may have been made. See log for details.", 
												  "Edit Data Table", 
												  JOptionPane.ERROR_MESSAGE);
					}
//				current_source.removeDataTable(table);
//				current_source.addDataTable(edited_table);
				
				refreshDataSource();
				return;
				}
			
			if (e.getActionCommand().endsWith("Remove")){
				
				if (current_source == null) return;
				
				int row = tblTables.getSelectedRow();
				if (row < 0) return;
				
				DataTable table = data_tables_model.getTableAt(row);
				
				int ok = JOptionPane.showConfirmDialog(InterfaceSession.getSessionFrame(), 
													  "Really remove table '" + table.getName() + "'?", 
													  "Remove Table", 
													  JOptionPane.YES_NO_OPTION, 
													  JOptionPane.WARNING_MESSAGE);
				
				if (ok != JOptionPane.YES_OPTION) return;
				
				if (current_source.removeDataTable(table)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Removed table '" + table.getName() + "'", 
												  "Remove Data Table", 
												  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not remove table '" + table.getName() + "'.", 
												  "Remove Data Table", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				refreshDataSource();
				return;
				}
			
			if (e.getActionCommand().endsWith("Show")){
				if (current_table == null || cmbTablesShow.getSelectedItem() == null) return;
				InterfaceGraphicWindow window = (InterfaceGraphicWindow)cmbTablesShow.getSelectedItem();
				InterfaceDataTable panel = (InterfaceDataTable)window.getPanel();
				panel.setSource(current_table);
				return;
				}
			
			updateDataTables();
			
			return;
			}
		
		if (e.getActionCommand().startsWith("Fields")){
			
			if (current_table == null) return;
			
			if (e.getActionCommand().endsWith("Add New")){
				
				if (current_table == null) return;
				
				DataFieldOptions options = new DataFieldOptions(current_table);
				DataField field = EditDataFieldDialogBox.showDialog(InterfaceSession.getSessionFrame(), options);
				if (field == null) return;
				
				// Does field already exist by this name?
				if (current_table.hasField(field.getName())){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Field '" + field.getName() + "' already exists!", 
												  "Add New Data Field", 
												  JOptionPane.ERROR_MESSAGE);
					return;
					}
				
				if (current_source.addDataField(current_table, field)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  "Added field '" + field.getName() + "' to table '" + current_table.getName() + "'.", 
							  "Add New Data Field", 
							  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  "Could not add field '" + field.getName() + "' to table '" + current_table.getName() + "'."
							  + " Please see log.", 
							  "Add New Data Field", 
							  JOptionPane.ERROR_MESSAGE);
					}
				
				updateDataFields();
				return;
				}
			
			if (e.getActionCommand().endsWith("Edit")){
				
				if (current_field == null) return;
				
				
				
				}
			
			
			}
		
		if (e.getActionCommand().startsWith("Queries")){
			
			if (e.getActionCommand().endsWith("Add New")){
				if (current_source == null) return;
				
				DataQuery new_query = new DataQuery("New Query", current_source, "");
				new_query = EditQueryDialogBox.showDialog(InterfaceSession.getSessionFrame(), new_query);
				
				if (new_query == null) return;
				
				current_source.addDataQuery(new_query);
				
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Query '" + new_query.getName() + "' added.", 
											  "Add New Data Query", 
											  JOptionPane.INFORMATION_MESSAGE);
				
				current_query = new_query;
				updateQueries();
				updateControls();
				
				return;
				}
			
			if (e.getActionCommand().endsWith("Edit")){
				if (current_query == null) return;
				
				DataQuery new_query = EditQueryDialogBox.showDialog(InterfaceSession.getSessionFrame(), current_query);
				
				if (new_query == null) return;
				
				current_query.setName(new_query.getName());
				current_query.setSQLStatement(new_query.getSQLStatement());
				
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Query '" + new_query.getName() + "' updated.", 
											  "Edit Data Query", 
											  JOptionPane.INFORMATION_MESSAGE);
				
				updateControls();
				return;
				}
			
			if (e.getActionCommand().endsWith("Show")){
				if (current_query == null || cmbQueriesShow.getSelectedItem() == null) return;
				InterfaceGraphicWindow window = (InterfaceGraphicWindow)cmbQueriesShow.getSelectedItem();
				InterfaceDataTable panel = (InterfaceDataTable)window.getPanel();
				if (!panel.setSource(current_query)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Could not set query; check syntax (also check log for error message)", 
												  "Show Data Query", 
												  JOptionPane.ERROR_MESSAGE);
					}
				return;
				}
			
			if (e.getActionCommand().endsWith("Execute")){
				if (current_source == null || current_query == null) return;
				try{
					if (current_source.executeUpdate(current_query)){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
								  "Query '" + current_query.getName() + "' executed.", 
								  "Execute Data Query", 
								  JOptionPane.INFORMATION_MESSAGE);
						refreshDataSource();
						return;
						}
				}catch (DataSourceException ex){
					InterfaceSession.log("InterfaceDatasourcePanel: Error executing query '" + current_query.getName() + "':"
										 + ex.getMessage(), 
										 LoggingType.Errors);
					}
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Error executing query '" + current_query.getName() + "'. Check syntax and log.", 
											  "Execute Data Query", 
											  JOptionPane.ERROR_MESSAGE);
				return;
				}
			
			return;
			}
		
		if (e.getActionCommand().startsWith("I/O")){
			
			if (e.getActionCommand().endsWith("Import Data")){
				// Show dialog
				ImportDataTableTextOptions options = new ImportDataTableTextOptions();
				options.setDataSource(current_source);
				if (!ImportDataTableDialogBox.showDialog(options))
					return;
				
				// Import tables
				ImportDataTableTextLoader loader = new ImportDataTableTextLoader();
				InterfaceProgressBar progress_bar = new InterfaceProgressBar("Importing data into '" + 
																			 options.getDataSource().getName() + "'");	
				progress_bar.register();
				
				// Indicate success/failure
				if (loader.load(options, progress_bar)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Data imported successfully.", 
												  "Import Data to Data Source", 
												  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Error importing data. Check log.", 
												  "Import Data to Data Source", 
												  JOptionPane.ERROR_MESSAGE);
					}
				
				progress_bar.deregister();
				refreshDataSource();
				return;
				}
			
			if (e.getActionCommand().endsWith("Export Data")){
				// Show dialog
				ExportDataTableOptions options = new ExportDataTableTextOptions();
				ExportDataTableDialogBox dialog = new ExportDataTableDialogBox(InterfaceSession.getSessionFrame(),
						   null, options);
				dialog.setVisible(true);

				if (!dialog.is_ok) return;
				
				// Export tables
				InterfaceIOType io_type = dialog.getOutputType();
				if (io_type == null) return;
				
				options = dialog.getCurrentOptions();
				
				ExportDataTableWriter writer = (ExportDataTableWriter)io_type.getIOInstance();
				InterfaceProgressBar progress_bar = new InterfaceProgressBar("Exporting data from '" + 
																			 options.data_source.getName() + "'");	
				
				// Indicate success/failure
				if (writer.write(options, progress_bar)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Data exported successfully.", 
												  "Export Data from Data Source", 
												  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Error exporting data. Check log.", 
												  "Export Data from Data Source", 
												  JOptionPane.ERROR_MESSAGE);
					}
					
				
				return;
				}
			
			return;
			}
		
	}

	public String toString(){
		return "Data Sources Panel";
	}
	
	static class TableListModel extends AbstractTableModel{

		ArrayList<DataTable> tables = new ArrayList<DataTable>();
		ArrayList<String> columnNames = new ArrayList<String>();
		int rows;
		int cols;
		
		public TableListModel(){
			
		}
		
		public TableListModel(ArrayList<DataTable> tables){
			setTables(tables);
		}
		
		public void setTables(ArrayList<DataTable> tables){
			this.tables = tables;
			Collections.sort(this.tables, new Comparator<DataTable>(){
				public int compare(DataTable f1, DataTable f2){
					return f1.getName().compareTo(f2.getName());
					}
				});
			this.fireTableDataChanged();
		}
		
		public DataTable getTableAt(int pos){
			if (pos < 0 || pos >= tables.size()) return null;
			return tables.get(pos);
		}
		
		public int getTablePos(DataTable table){
			for (int i = 0; i < tables.size(); i++)
				if (tables.get(i).equals(table))
					return i;
			return -1;
		}
		
		public Object getValueAt(int row, int col){
			DataTable table = tables.get(row);
			switch (col){
				case 0:
					return table.getName();
				case 1:
					return table.getFields().size();
				case 2:
					return true;		//TODO: implement editability on tables
				}
			return null;
		}
		
		public int getRowCount(){
			return tables.size();
		}
		
		public int getColumnCount(){
			return 3;
		}
		
		public String getColumnName(int c){
			switch (c){
				case 0:
					return "Name";
				case 1:
					return "Fields";
				case 2:
					return "Editable";
				}
			return "?";
		}
		
		public Class getColumnClass(int c){
			switch (c){
				case 0:
					return String.class;
				case 1:
					return Integer.class;
				case 2:
					return Boolean.class;
				}
			return String.class;
		}
		
		public boolean isCellEditable(int row, int col) {
			return false;
		}
		
	}
	
	static class FieldListModel extends AbstractTableModel{

		ArrayList<DataField> fields = new ArrayList<DataField>();
	
		public FieldListModel(){
			
		}
		
		public FieldListModel(ArrayList<DataField> fields){
			setFields(fields);
		}
		
		public void setFields(ArrayList<DataField> fields){
			this.fields = fields;
			Collections.sort(this.fields, new Comparator<DataField>(){
				public int compare(DataField f1, DataField f2){
					return f1.getName().compareTo(f2.getName());
					}
				});
			this.fireTableDataChanged();
		}
		
		public DataField getFieldAt(int pos){
			if (pos < 0 || pos >= fields.size()) return null;
			return fields.get(pos);
		}
		
		public int getFieldPos(DataField field){
			for (int i = 0; i < fields.size(); i++)
				if (fields.get(i).equals(field))
					return i;
			return -1;
		}
		
		public Object getValueAt(int row, int col){
			DataField field = fields.get(row);
			switch (col){
				case 0:
					return field.getName();
				case 1:
					return field.getDataTypeStr();
				case 2:
					return field.isEditable();
				case 3:
					return field.isUnique();
				case 4:
					return field.isKeyField();
				}
			return null;
		}
		
		public int getRowCount(){
			return fields.size();
		}
		
		public int getColumnCount(){
			return 5;
		}
		
		public String getColumnName(int c){
			switch (c){
				case 0:
					return "Name";
				case 1:
					return "Data Type";
				case 2:
					return "IsEditable";
				case 3:
					return "IsUnique";
				case 4:
					return "IsKeyField";
				}
			return "?";
		}
		
		public Class getColumnClass(int c){
			switch (c){
				case 0:
					return String.class;
				case 1:
					return Integer.class;
				case 2:
					return Boolean.class;
				case 3:
					return Boolean.class;
				case 4:
					return Boolean.class;
				}
			return String.class;
		}
		
		public boolean isCellEditable(int row, int col) {
			return false;
		}
		
	}
	
	
	
}