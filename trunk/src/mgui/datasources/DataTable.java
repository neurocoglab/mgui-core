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

package mgui.datasources;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.tree.DefaultMutableTreeNode;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.datasources.DataSetTreeNode;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.menus.InterfaceMenu;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tables.InterfaceDataTable;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**********************************************
 * General data table object specifying the organization of data into fields.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class DataTable extends AbstractInterfaceObject implements Cloneable, 
								  								  IconObject,
								  								  PopupMenuObject,
								  								  DataSourceItem,
								  								  DataFieldListener{

	//public String name;
	public ArrayList<String> sortFields = new ArrayList<String>();
	protected HashMap<String, DataField> fields = new HashMap<String, DataField>();
	protected HashMap<String, DataIndex<?>> indexes = new HashMap<String, DataIndex<?>>();
	public DataSetTreeNode treeNode;
	public DataSource dataSource;
	protected boolean sort_fields = true;
	
	public DataTable(){
		init();
	}
	
	public DataTable(String name){
		setName(name);
		init();
	}
	
	public static Icon getIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/data_table_17.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/data_table_17.png");
		return null;
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/data_table_17.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/data_table_17.png");
		return null;
	}
	
	protected void init(){
		
	}
	
	public void setDataSource(DataSource ds){
		dataSource = ds;
	}
	
	public DataSource getDataSource(){
		return dataSource;
	}
	
	public boolean hasSortedFields(){
		return this.sort_fields;
	}
	
	public void setSortedFields(boolean b){
		this.sort_fields = b;
	}
	
	public void setFields(ArrayList<DataField> f){
		fields = new HashMap<String, DataField>();
		for (int i = 0; i < f.size(); i++)
			addField(f.get(i));
	}
	
	/*********************************************
	 * Returns a list of this table's key fields.
	 * 
	 * @return
	 */
	public ArrayList<String> getKeyFields(){
		ArrayList<String> key_fields = new ArrayList<String>();
		for (int i = 0; i < fields.size(); i++)
			if (fields.get(i).isKeyField())
			key_fields.add(fields.get(i).getName());
		return key_fields;
	}
	
	public String getSQLStatement(String[] sort_fields){
		return getSQLStatement(sort_fields, null);
	}
	
	public String getSQLStatement(){
		 return getSQLStatement((DataSourceDriver)null);
	}
	
	public String getSQLStatement(String[] sort_fields, DataSourceDriver driver){
		String q = "`";
		if (driver != null)
			q = driver.getSQLQuote();
		return "SELECT * FROM " + q + getName() + q + getSQLSortStr(sort_fields); // + " WHERE TRUE" + getSQLSortStr(sort_fields);
	}
	
	public String getSQLStatement(DataSourceDriver driver){
		String q = "`";
		if (driver != null)
			q = driver.getSQLQuote();
		return "SELECT * FROM " + q + getName() + q + getSQLSortStr(); //+ " WHERE TRUE" + getSQLSortStr();
		//return "SELECT * FROM " + q + getName() + q + " WHERE TRUE" + getSQLSortStr();
	}
	
	public String getSQLSortStr(String[] sort_fields){
		return getSQLSortStr(sort_fields, null);
	}
	
	public String getSQLSortStr(String[] sort_fields, DataSourceDriver driver){
		String q = "`";
		if (driver != null)
			q = driver.getSQLQuote();
		String retStr = " ORDER BY " + q;
		for (int i = 0; i < sort_fields.length; i++)
			if (i == 0)
				retStr.concat(sort_fields[i] + q);
			else
				retStr.concat(q + ", " + q + sort_fields[i] + q);
		retStr.concat(";");
		return retStr;
	}
	
	public String getSQLSortStr(){
		return getSQLSortStr((DataSourceDriver)null);
	}
	
	public String getSQLSortStr(DataSourceDriver driver){
		String q = "`";
		if (driver != null)
			q = driver.getSQLQuote();
		 if (sortFields.size() == 0) return "";
		 String retStr = " ORDER BY " + q;
		 retStr.concat(sortFields.get(0) + q);
		 for (int i = 1; i < sortFields.size(); i++)
			 retStr.concat(", " + q + sortFields.get(i) + q);
		 return retStr;
	}
	
	/**************************************************
	 * Adds a new field to this table. Note: this does not update the data source. To add a field to a
	 * live table, use {@link DataSource.addField}.
	 * 
	 * @param field
	 * @return
	 */
	public boolean addField(DataField field){
		if (fields.containsKey(field.getName())) return false;
		fields.put(field.getName(), field);
		if (field.isKeyField())
			indexes.put("PrimaryKey", new DataIndex(field.getName()));
		return true;
	}
	
	public boolean hasField(String name){
		return (fields.containsKey(name));
	}
	
	public boolean removeField(DataField field){
		if (!fields.containsKey(field.getName())) return false;
		fields.remove(field.getName());
		return true;
	}
	 
	public boolean removeField(String field){
		if (!fields.containsKey(field)) return false;
		return removeField(fields.get(field));
	}
	
	public HashMap<String, DataField> getFields(){
		return fields;
	}
	
	public ArrayList<DataField> getFieldList(){
		return new ArrayList<DataField>(fields.values());
	}
	
	public DataField getField(String name){
		return fields.get(name);
	}
	
	public boolean setKeyField(String name){
		DataField field = fields.get(name);
		if (field == null) return false;
		field.setIsUnique(true);
		field.setIsKeyField(true);
		int sql_type = field.getDataType();
		switch (sql_type){
			case Types.INTEGER:
				indexes.put("PrimaryKey", new DataIndex<Integer>(name));
				break;
			case Types.DOUBLE:
				indexes.put("PrimaryKey", new DataIndex<Double>(name));
				break;
			case Types.FLOAT:
				indexes.put("PrimaryKey", new DataIndex<Float>(name));
				break;
			case Types.VARCHAR:
				indexes.put("PrimaryKey", new DataIndex<String>(name));
				break;
			default:
				return false;
			}
		//indexes.put("PrimaryKey", new DataIndex(name));
		return true;
	}
	
	public boolean unsetKeyField(String name){
		DataField field = fields.get(name);
		if (field == null) return false;
		field.setIsKeyField(false);
		indexes.remove("PrimaryKey");
		return true;
	}
	
	public Class<? extends Comparable<?>> getFieldClass(String name){
		DataField field = fields.get(name);
		if (field == null) return null;
		int sql_type = field.getDataType();
		switch (sql_type){
			case Types.INTEGER:
				return Integer.class;
			case Types.DOUBLE:
				return Double.class;
			case Types.FLOAT:
				return Float.class;
			case Types.VARCHAR:
				default:
				return String.class;
			}
	}
	
	public void addIndex(String name, DataIndex<?> index){
		indexes.put(name, index);
	}
	
	public HashMap<String, DataIndex<?>> getIndexes(){
		return indexes;
	}
	
	public DataIndex<?> getIndex(String name){
		return indexes.get(name);
	}
	
	/*******************************************
	 * Enumerates the indices of records in this table where <code>filter_field</code> matches an element
	 * in <code>list</code>. Creates a temporary table in this table's <code>DataSource</code> and returns that table.
	 * 
	 * <p>If an error is encountered in the table creation process, a <code>DataSourceException</code> is thrown.
	 * 
	 * @param list
	 * @param join_field
	 * @return
	 */
	public DataTable getFilteredByList(ArrayList<Comparable> list, String filter_field) throws DataSourceException{
		DataTable table = (DataTable)clone();
		table.setName(getTempTableName());
		
		if (!dataSource.addTempTable(table)) return null;
		
		//add records which match elements of list
		Collections.sort(list);
		DataRecordSet inSet = new DataRecordSet(dataSource);
		DataQuery q = new DataQuery(dataSource, this.getSQLStatement(new String[]{filter_field}));
		inSet.set(q);
		DataRecordSet outSet = new DataRecordSet(dataSource);
		outSet.set(table);
		
		if (!inSet.moveFirst()) return table;
		int i = 0;
		
		Iterator<String> itr;
		while (!inSet.EOF()){
			try{
				while (i < list.size() && list.get(i).compareTo(inSet.getFieldVal(filter_field)) < 0)
					i++;
				
				if (i < list.size()){
					Comparable f = list.get(i);
					while (!inSet.EOF() && f.compareTo(inSet.getFieldVal(filter_field)) > 0)
						inSet.moveNext();
					
					//copy records
					while (!inSet.EOF() && f.compareTo(inSet.getFieldVal(filter_field)) == 0){
						outSet.addNew();
						itr = fields.keySet().iterator();
						while (itr.hasNext()){
							String f_name = itr.next();
							outSet.getField(f_name).setValue(inSet.getFieldVal(f_name));
							}
						outSet.update();
						inSet.moveNext();
						}
					}
				}catch(SQLException ex){
					throw new DataSourceException("DataTable: SQLException:\n" + ex.getMessage());
					}
			inSet.moveNext();
			}
		
		inSet.close();
		outSet.close();
		
		return table;
	}
	
	String getTempTableName(){
		try{
			int n = dataSource.temp_tables.size();
			String name = "temp_table_" + n;
			while (dataSource.getTableSet().getTable(name) != null)
				name = "temp_table_" + ++n;
			return name;
		}catch (DataSourceException ex){
			ex.printStackTrace();
			return null;
			}
	}
	
	@Override
	public Object clone(){
		DataTable table = new DataTable(getName());
		table.setDataSource(dataSource);
		
		//add fields
		Iterator<String> itr = fields.keySet().iterator();
		while (itr.hasNext())
			table.addField((DataField)fields.get(itr.next()).clone());
		
		//add indexes
		itr = indexes.keySet().iterator();
		while (itr.hasNext()){
			String key = itr.next();
			DataIndex index = indexes.get(key);
			table.addIndex(key, (DataIndex)index.clone());
			}
		
		return table;
	}
	
	  @Override
	public void setTreeNode(InterfaceTreeNode treeNode){

		  super.setTreeNode(treeNode);
		
		  DefaultMutableTreeNode fieldNode = new DefaultMutableTreeNode("Fields");
			//add all fields
			Iterator<DataField> itr = fields.values().iterator();
			while (itr.hasNext())
				fieldNode.add(itr.next().issueTreeNode());
			
			treeNode.add(fieldNode);
			
	  }
	  
	@Override
	public String getTreeLabel(){
	return getName();
	}

	@Override
	public InterfacePopupMenu getPopupMenu() {
		return getPopupMenu(null);
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu(List<Object> selected) {
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		
		menu.addMenuItem(new JMenuItem("DataTable: " + getName(), getObjectIcon()));
		
		menu.add(new JSeparator(), 1);
		menu.add(new JSeparator(), 1);
		
		if (this.getDataSource().isConnected()){
		
			InterfaceMenu menu2 = new InterfaceMenu("Show in window", this);
			ArrayList<InterfaceGraphicWindow> windows = InterfaceSession.getDisplayPanel().getPanels();
			
			boolean has_window = false;
			for (int i = 0; i < windows.size(); i++)
				if (windows.get(i).getPanel() instanceof InterfaceDataTable){
					JMenuItem item = new JMenuItem(windows.get(i).getPanel().getName());
					item.setActionCommand("Show in window");
					menu2.addMenuItem(item);
					has_window = true;
					}
			
			if (has_window) menu.addMenuItem(menu2);
			
			}
		
		return menu;
	}

	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getActionCommand() != null && item.getActionCommand().equals("Show in window")){
			String name = item.getText();
			ArrayList<InterfaceGraphicWindow> windows = InterfaceSession.getDisplayPanel().getPanels();
			for (int i = 0; i < windows.size(); i++)
				if (windows.get(i).getPanel() instanceof InterfaceDataTable &&
					windows.get(i).getPanel().getName().equals(name)){
					InterfaceDataTable table = (InterfaceDataTable)windows.get(i).getPanel();
					table.setSource(this);
					}
			}
		
	}
	
	@Override
	public void showPopupMenu(MouseEvent e) {
		InterfacePopupMenu menu = getPopupMenu();
		if (menu == null) return;
		menu.show(e);
	}
	
	//return a list of attributes for this table
	public AttributeList getAttributeList(){
		AttributeList list = new AttributeList();
		list.add(new Attribute("Name", new String(getName())));
		//other attributes?
		return list;
	}
	
	@Override
	public String toString(){
		return "Data Table: '" + getName() + "'";
	}

	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXMLSchema() {
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
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException {
		
		if (localName.equals("DataField")){
			DataField new_field = new DataField(attributes.getValue("name"),
												DataTypes.getSQLStrValue(attributes.getValue("datatype")));
			
			new_field.setLabel(attributes.getValue("label"));
			new_field.setLength(Integer.valueOf(attributes.getValue("length")));
			new_field.setIsUnique(Boolean.valueOf(attributes.getValue("isunique")));
			new_field.setIsRequired(Boolean.valueOf(attributes.getValue("isrequired")));
			new_field.setIsKeyField(Boolean.valueOf(attributes.getValue("iskeyfield")));
			new_field.default_value = attributes.getValue("default");
			
			this.addField(new_field);
			return;
			}
		
	}

	@Override
	public void handleXMLElementEnd(String localName) throws SAXException {
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException {
		
	}

	@Override
	public String getLocalName() {
		return "DataTable";
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		String _tab = XMLFunctions.getTab(tab);
		
		writer.write(_tab + "<DataTable name='" + XMLFunctions.getXMLFriendlyString(this.getName()) + "'>\n");
		ArrayList<String> names = new ArrayList<String>(fields.keySet());
		
		for (int i = 0; i < names.size(); i++){
			fields.get(names.get(i)).writeXML(tab + 1, writer, options, progress_bar);
			}
		
		writer.write(_tab + "</DataTable>\n");
		
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException {
		writeXML(tab, writer, new XMLOutputOptions(), progress_bar);		
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, new XMLOutputOptions(), null);
	}

	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	/*****************************************************
	 * Returns a list of the primary keys in this table 
	 * 
	 * @return
	 */
	public ArrayList<String> getPrimaryKeys(){
		ArrayList<DataField> fields = this.getFieldList();
		ArrayList<String> keys = new ArrayList<String>();
		for (int i = 0; i < fields.size(); i++)
			if (fields.get(i).isKeyField())
				keys.add(fields.get(i).getName());
		return keys;
	}
	
	@Override
	public void dataFieldChanged(DataFieldEvent e) {
		
		DataField field = (DataField)e.getSource();
		DataSourceDriver driver = dataSource.getDataSourceDriver();
		String q = driver.getSQLQuote();
		
		switch (e.event_code){
			
			case PrimaryKeyChanged:
				
				// TODO: This is still experimental
				
				ArrayList<String> keys = getPrimaryKeys();
				if (keys.size() == 0) return;
				
				String sql = "ALTER TABLE " + q + getName() + q +
							 "ADD CONSTRAINT PRIMARY KEY (";
				for (int i = 0; i < keys.size(); i++){
					if (i > 0) sql = sql + ",";
					sql = sql + keys.get(i);
					}
				sql = sql + ");";
				DataQuery query = new DataQuery(dataSource, sql);
				
				try{
					dataSource.executeUpdate(query);
				}catch (DataSourceException ex){
					
					}
				
				return;
		
		
			}
		
	}
	
}