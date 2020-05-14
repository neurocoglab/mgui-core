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

package mgui.interfaces.tables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;

import mgui.datasources.DataField;
import mgui.datasources.DataQuery;
import mgui.datasources.DataRecordSet;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataSourceItem;
import mgui.datasources.DataTable;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.datasources.DataTableModel;
import mgui.interfaces.graphics.GraphicMouseListener;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphicTextBox;
import mgui.interfaces.gui.RowNumberTable;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.datasources.ToolDataSource;
import mgui.interfaces.variables.VariableInt;
import mgui.interfaces.variables.tables.VariableTableModel;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiBoolean;

import org.xml.sax.Attributes;

/***************************
 * Interface panel for displaying a data table or query.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */
public class InterfaceDataTable extends InterfaceGraphic<ToolDataSource> implements MouseMotionListener {

	protected JTable table;
	protected InterfaceTableModel table_model;
	protected TableRowSorter sorter;
	protected String source_name = "";
	private JScrollPane scrollPane;
	
	private boolean adding_row = false;
	
	public InterfaceDataTable(){
		init();
	}
	
	@Override
	protected void init(){
		super.init();
		_init();
		type = "Data Table";
		
		attributes.add(new Attribute<MguiBoolean>("IsCached", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiBoolean>("PreCache", new MguiBoolean(false)));
		
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
	}
	
	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/data_table_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/data_table_20.png");
		return null;
	}

	public InterfaceTableModel getTableModel(){
		return this.table_model;
	}
	
	public void setDataRecordSet(DataRecordSet r){
		setModel(new DataTableModel(r));
	}
	
	public boolean getIsCached(){
		return ((MguiBoolean)attributes.getValue("IsCached")).getTrue();
	}
	
	public boolean getPreCache(){
		return ((MguiBoolean)attributes.getValue("PreCache")).getTrue();
	}
	
	public void setTable(JTable t){
		table = t;
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.addMouseMotionListener(this);
		table.addMouseListener(this);
		
		table.setDefaultRenderer(MguiBoolean.class, table.getDefaultRenderer(Boolean.class));
		table.setDefaultEditor(MguiBoolean.class, table.getDefaultEditor(Boolean.class));
		
		this.removeAll();
		scrollPane = new JScrollPane(table);
		scrollPane.addMouseMotionListener(this);
		scrollPane.addMouseListener(this);
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	public void setModel(InterfaceTableModel m){
		table_model = m;
		if (table == null){
			setTable(new JTable(table_model));
			JTable rowTable = new RowNumberTable(table);
			scrollPane.setRowHeaderView(rowTable);
			scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,
								 rowTable.getTableHeader());
		}else{
			table.setModel(table_model);
			}
		
		sorter = new TableRowSorter(table_model);
		table.setRowSorter(sorter);
		updateUI();
	}
	
	@Override
	public DefaultMutableTreeNode getDisplayObjectsNode(){
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Data Table Sources");
		DefaultMutableTreeNode root2 = new DefaultMutableTreeNode("Data Sources");
		DefaultMutableTreeNode thisNode;
		//return all DataTable and DataQuery objects
		ArrayList<DataSource> sources = InterfaceSession.getWorkspace().getDataSources();
		for (int i = 0; i < sources.size(); i++){
			//add datasource node
			thisNode = new DefaultMutableTreeNode(sources.get(i).getConnection().getName());
			//add all tables and queries
			if (sources.get(i).isConnected()){
				try{
					ArrayList<DataTable> tables = sources.get(i).getTableSet().getTables();
					for (int j = 0; j < tables.size(); j++)
						thisNode.add(new DefaultMutableTreeNode(tables.get(j)));
				}catch (DataSourceException ex){
					InterfaceSession.log("InterfaceDataTable: Could not retrieve table set from data source '" + 
										 sources.get(i).getName(), 
										 LoggingType.Errors);
					}
				for (int j = 0; j < sources.get(i).getDataQueries().size(); j++)
					thisNode.add(new DefaultMutableTreeNode(sources.get(i).getDataQueries().get(j)));
				root2.add(thisNode);
				}
			}
		root.add(root2);
		
		root2 = new DefaultMutableTreeNode("Variables");
		ArrayList<VariableInt<?>> variables = InterfaceSession.getWorkspace().getVariables();
		for (int i = 0; i < variables.size(); i++)
			root2.add(new DefaultMutableTreeNode(variables.get(i)));
		
		root.add(root2);
		return root;
	}
	
	@Override
	public String toString(){
		return "Data Table Panel: " + getName();
	}
	
	@Override
	public String getTitle(){
		if (source_name == null || source_name.length() == 0)
			return getName();
		return getName() + " [" + source_name + "]";
	}
	
	public String getSourceName(){
		return this.source_name;
	}
	
	@Override
	public boolean setSource(Object o){
		if (!isDisplayable(o)) return false;
		
		try{
			if (DataSourceItem.class.isInstance(o)){
				DataSourceItem item = (DataSourceItem)o;
				if (!item.getDataSource().isConnected()) return false;
				DataTableModel model = new DataTableModel(item);
//				if (getIsCached()){
//					model.setProgressUpdater(new InterfaceProgressBar("Caching table: ", InterfaceSession.getDisplayPanel()));
//					if (getPreCache())
//						model.prefillCache();
//					}
				setModel(model);
				source_name = item.getName();
				if (source_name == null) source_name = "";
				updateDisplay();
				if (title_panel != null)
					title_panel.updateTitle();
				return true;
				}
			
			
		}catch (DataSourceException ex){
			//ex.printStackTrace();
			InterfaceSession.log("InterfaceDataTable: Error setting data source: " + ex.getMessage(), 
								 LoggingType.Errors);
			return false;
			}
		
		if (VariableInt.class.isInstance(o)){
			VariableInt<?> variable = (VariableInt<?>)o;
			setModel(new VariableTableModel(variable));
			source_name = variable.getName();
			if (source_name == null) source_name = "";
			updateDisplay();
			if (title_panel != null)
				title_panel.updateTitle();
			return true;
			}
		
		return false;
	}
	
	public InterfaceTableModel getModel(){
		return table_model;
	}
	
	@Override
	public boolean isDisplayable(Object o){
		return DataTable.class.isInstance(o) ||
			   DataQuery.class.isInstance(o) ||
			   VariableInt.class.isInstance(o);
	}
	
	public void addMouseListener(GraphicMouseListener thisObj){
		super.addMouseListener(thisObj);
		if (scrollPane != null){
			scrollPane.addMouseMotionListener(thisObj.getMouseListener());
			scrollPane.addMouseListener(thisObj.getMouseListener());
			}
		if (table != null){
			table.addMouseMotionListener(thisObj.getMouseListener());
			scrollPane.addMouseListener(thisObj.getMouseListener());
			}
	}
	
	@Override
	public void mouseClicked(MouseEvent e){
		updateAddedRow();
	}
	
	@Override
	public void mouseExited(MouseEvent e){
		//updateAddedRow();
	}
	
	@Override
	public int updateStatusBox(InterfaceGraphicTextBox box, MouseEvent e){
		
		int index = super.updateStatusBox(box, e);
		if (index <= 0) return index;
		
		switch (index){
			case 1:
				String source = getSourceName();
				if (source == null || source.length() == 0) 
					box.setText("Current table: None");
				else
					box.setText("Current table: '" + source + "'");
				break;
				
			case 2:
				InterfaceTableModel table_model = getTableModel();
				if (table_model != null){
					int records = (table_model.getRowCount());
					box.setText("Records: " + records);
					}
				else{
					box.setText("");
					}
				break;
				
			default:
				box.setText("");
			}
		
		return index;
		
	}
	
	protected void updateAddedRow(){
		if (!adding_row) return;
		int e_row = table.getEditingRow();
		int count = getTableModel().getRowCount();
		if (e_row > 0 && e_row != count){
			InterfaceTableModel model = getTableModel();
			if (!DataTableModel.class.isInstance(model)) return;
			DataRecordSet record_set = ((DataTableModel)model).getRecordSet();
			if (!record_set.update()){
				record_set.cancelUpdate();
				}
			adding_row = false;
			}
		updateUI();
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu(){
		InterfacePopupMenu menu = super.getPopupMenu();
		int start = super.getPopupLength();	//can we get this from the menu itself?
		
		menu.add(new JSeparator(), start);
		menu.add(new JSeparator(), start);
		menu.addMenuItem(new JMenuItem("Data Table Window", getObjectIcon()));
		menu.add(new JSeparator(), start + 3);
		menu.add(new JSeparator(), start + 3);
		
		ArrayList<VariableInt<?>> variables = InterfaceSession.getWorkspace().getVariables();
		
		int add = 0;
		if (variables.size() > 0){
			JMenu submenu = new JMenu("Set Variable as source");
		
			for (int i = 0; i < variables.size(); i++){
				JMenuItem item = new JMenuItem(variables.get(i).getName(), variables.get(i).getObjectIcon());
				submenu.add(item);
				}
			
			add++;
			menu.addSubmenu(submenu);
			}
		
		ArrayList<DataSource> sources = InterfaceSession.getWorkspace().getDataSources();
		
		if (sources.size() > 0){
			JMenu tables_menu = new JMenu("Set Data Table as source");
			JMenu queries_menu = new JMenu("Set Data Query as source");
			boolean ok_to_add_q = false, ok_to_add_t = false; 
			
			try{
				for (int i = 0; i < sources.size(); i++){
					DataSource source = sources.get(i);
					if (source.isConnected()){
						JMenu t_menu = new JMenu(source.getName());
						
						ArrayList<DataTable> tables = source.getTableSet().getTables();
						for (int j = 0; j < tables.size(); j++){
							JMenuItem item = new JMenuItem(tables.get(j).getName(), tables.get(j).getObjectIcon());
							t_menu.add(item);
							}
						
						JMenu q_menu = new JMenu(source.getName());
						ArrayList<DataQuery> queries = source.getDataQueries();
						for (int j = 0; j < queries.size(); j++){
							JMenuItem item = new JMenuItem(queries.get(j).getName(), queries.get(j).getObjectIcon());
							q_menu.add(item);
							}
						
						if (tables.size() > 0){
							ok_to_add_t = true;
							tables_menu.add(t_menu);
							}
						
						if (queries.size() > 0){
							ok_to_add_q = true;
							queries_menu.add(q_menu);
							}
						
						}
					}
			}catch (DataSourceException ex){
				InterfaceSession.log("InterfaceDataTable: Error accessing data sources\nDetails: " + ex.getMessage(), 
									 LoggingType.Errors);
				}
			
			if (ok_to_add_t){
				menu.addSubmenu(tables_menu, "Set table");
				}
			
			if (ok_to_add_q){
				menu.addSubmenu(queries_menu, "Set query");
				}
			
			}
		
		if (table == null) return menu;
			
		menu.addMenuItem(new JMenuItem("Add new row"));
		
		if (table != null && table.getSelectedRowCount() == 1){
			menu.addMenuItem(new JMenuItem("Delete row"));
		} else if (table.getSelectedRowCount() > 1){
			menu.addMenuItem(new JMenuItem("Delete rows"));
			}
		
		return menu;
	}
	
	@Override
	protected int getPopupLength(){
		return super.getPopupLength() + 7; 
	}
	
	
	@Override
	public void handlePopupEvent(ActionEvent e){
		
		String command = e.getActionCommand();
		
		if (command.startsWith("Set table") || command.startsWith("Set query")){
			String source_str = command.substring(command.indexOf(".") + 1);
			int a = source_str.indexOf(".");
			
			String source = source_str.substring(0, a);
			String ds_item = source_str.substring(a + 1);
			
			DataSource data_source = InterfaceSession.getWorkspace().getDataSource(source);
			if (data_source == null || !data_source.isConnected()){
				InterfaceSession.log("InterfaceDataTable: Error accessing data source '" + source + "'.", 
						 			 LoggingType.Errors);
				return;
				}
			
			
			try{
				if (command.startsWith("Set table")){
					DataTable data_table = data_source.getTableSet().getTable(ds_item);
					if (data_table == null){
						InterfaceSession.log("InterfaceDataTable: Data table '" + source_str + "' not found.", 
					 			 			 LoggingType.Errors);
						return;
						}
					
					this.setSource(data_table);
					return;
					}
				
				DataQuery query = data_source.getDataQuery(ds_item);
				if (query == null){
					InterfaceSession.log("InterfaceDataTable: Data query '" + source_str + "' not found.", 
	 			 			 LoggingType.Errors);
					return;
					}
				this.setSource(query);
			
			}catch (DataSourceException ex){
				InterfaceSession.log("InterfaceDataTable: Error accessing data source '" + source + 
									 "\nDetails: " + ex.getMessage(), 
									 LoggingType.Errors);
				}
			
			return;
			}
		
		if (command.equals("Add new row")){
			//TODO: make more generic
			if (table_model instanceof DataTableModel){
				DataTableModel _model = (DataTableModel)table_model;
				DataRecordSet set = _model.getRecordSet();
				if (set == null) return;
				if (!_model.addNewRecord()){
					return;
					}
				//set.addNew();
				for (int i = 0; i < set.getFieldCount(); i++){
					DataField field = set.getField(i);
					Object value = field.getDefaultValue();
					if (value == null && field.isRequired()){
						if (field.isNumeric())
							value = "0";
						if (field.isText())
							value = "?";
						if (field.isDate() || field.isTimestamp())
							value = InterfaceEnvironment.getNow("dd/mm/yy");
						}
					if (set.updateField(field.getName(), value)){
						InterfaceSession.log("InterfaceDataTable: Set default values for new record.", LoggingType.Debug);
					}else{
						InterfaceSession.log("InterfaceDataTable: Failed to set default values for new record.", LoggingType.Debug);
						}
					
					}
				
				//TODO: Don't update until user has stopped editing the row
				if (!_model.updateRecordSet()){
					_model.cancelUpdate();
					InterfaceSession.log("InterfaceDataTable: Record could not be added.", LoggingType.Debug);
				}else{
					InterfaceSession.log("InterfaceDataTable: Record added.", LoggingType.Debug);
					}
					
				}
			
			return;
			}
		
		if (command.startsWith("Delete row")){
			
			if (table_model instanceof DataTableModel){
				DataTableModel _model = (DataTableModel)table_model;
				if (!_model.getRecordSet().isUpdatable()) return;
				
				int[] rows = table.getSelectedRows();
				
				if (_model.deleteRows(rows)){
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Rows deleted.", 
												  "Delete Rows from Table", 
												  JOptionPane.INFORMATION_MESSAGE);
				}else{
					JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
												  "Error deleting rows. Check log.", 
												  "Delete Rows from Table", 
												  JOptionPane.ERROR_MESSAGE);
					}
				
				}
			
			return;
			}
		
		if (command.startsWith("Set Variable as source")){
			String name = command.substring(command.indexOf(".") + 1);
			VariableInt<?> var = InterfaceSession.getWorkspace().getVariableByName(name);
			if (var != null){
				this.setSource(var);
				}
			return;
			}
		
	}
	
	
	//************************ XML Stuff *****************************************
	
	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXMLSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleXMLElementEnd(String localName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab);
		
		String source = null, source_type = null;
		
		Object table_source = this.table_model.getSource();
		if (table_source instanceof DataSourceItem){
			// Create composite name
			DataSourceItem item = ((DataSourceItem) table_source);
			String ds = item.getDataSource().getName();
			String name = item.getName();
			source = ds + "." + name;
			source_type = item.getLocalName();
		}else if (table_source instanceof VariableInt){
			VariableInt<?> v_int = (VariableInt<?>)table_source;
			source = v_int.getName();
			source_type = v_int.getLocalName();
			}
		
		writer.write(_tab + "<InterfaceDataTable\n" +  
					 _tab2 + "source='" + source + "'\n" + 
					 _tab2 + "source_type='" + source_type + "'\n" + 
					 _tab + ">\n");
			
		//TODO: write stuff
		attributes.writeXML(tab + 1, writer, options, progress_bar);
		
		writer.write(_tab + "</InterfaceDataTable>\n");
		
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, null);
	}
	
	@Override
	public boolean isToolable(Tool tool){
		return tool instanceof ToolDataSource;
	}
	
}