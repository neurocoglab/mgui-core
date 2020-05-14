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

import java.io.IOException;
import java.io.Writer;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/****************************
 * Object stores a list of DataTable objects, all representing tables in a single DataSource
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class DataTableSet extends AbstractInterfaceObject implements IconObject,
																	 XMLObject{

	protected DataSource data_source;
	//public ArrayList<DataTable> tables = new ArrayList<DataTable>();
	protected HashMap<String,DataTable> table_map = new HashMap<String,DataTable>();
	
	public DataTableSet(){
		
	}
	
	public DataTableSet(DataSource ds){
		setDataSource(ds);
	}
	
	public void setDataSource(DataSource ds){
		data_source = ds;
	}
	
	public ArrayList<DataTable> getTables() {
		ArrayList<DataTable> tables = new ArrayList<DataTable>(table_map.values());
		Collections.sort(tables, new Comparator<DataTable>(){
				public int compare(DataTable t1, DataTable t2){
					return t1.getName().compareTo(t2.getName());
				}
			});
		return new ArrayList<DataTable>(tables);
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/table_set_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/table_set_20.png");
		return null;
	}
	
	/**********************************************************
	 * Sets this table set from the information contained in its data source's metadata.
	 * 
	 * @return
	 * @throws DataSourceException
	 */
	public boolean setFromMetaData() throws DataSourceException{
		return setFromMetaData(true);
	}
	
	/**********************************************************
	 * Sets this table set from the information contained in its data source's metadata.
	 * 
	 * @param overwrite_existing 	If {@code true}, all existing tables will be removed; otherwise, pre-existing
	 * 								tables will not be altered.
	 * @return
	 * @throws DataSourceException
	 */
	public boolean setFromMetaData(boolean overwrite_existing) throws DataSourceException{
		ResultSet tableSet = data_source.getTablesFromMetaData();
		if (tableSet == null) return false;
		if (table_map == null)
			table_map = new HashMap<String,DataTable>();
		if (overwrite_existing)
			table_map.clear();
		DataTable thisTable;
	    DataRecordSet thisSet;
	    DataSourceDriver driver = data_source.getDataSourceDriver();
	    
	    boolean is_done = false;
	    while (!is_done){
		    try{
			    while (tableSet.next()){
			    	String name = tableSet.getString("TABLE_NAME");
			    	if (table_map.get(name) == null){
				    	thisTable = new DataTable(name);
				    	thisTable.setDataSource(data_source);
				    	//thisSet =  new DataRecordSet(data_source);
				    	//thisSet.setRecordSet(thisTable.getSQLStatement(driver));
				    	//thisTable.setFields(thisSet.fields);
				    	
				    	// Set the fields
				    	ResultSet column_set = data_source.getColumnsFromMetaData(name);
				    	if (column_set != null){
				    		while (column_set.next()){
				    			String field_name = column_set.getString("COLUMN_NAME");
				    			int field_type = column_set.getInt("DATA_TYPE");
				    			
				    			DataField field = new DataField(field_name, field_type);
				    			int nullable = column_set.getInt("NULLABLE");
				    			field.setIsRequired(nullable == DatabaseMetaData.columnNoNulls);
				    			String default_value = column_set.getString("COLUMN_DEF");
				    			field.setDefaultValue(default_value);
				    			thisTable.addField(field);
				    			}
				    		}
				    	
				    	// Set the key fields, if any
				    	ResultSet keySet = data_source.getKeysFromMetaData(thisTable.getName());
				    	if (keySet != null)
					    	while (keySet.next())
					    		thisTable.setKeyField(keySet.getString("COLUMN_NAME"));
				    	table_map.put(name, thisTable);
				    	}
			    	}
			    //assign primary fields
			    
			    //sortTables();
			    is_done = true;
		    	}
		    catch (Exception e){
		    	InterfaceSession.log(e.getMessage(), LoggingType.Errors);
		    	}
		    }
	    
	    return true;
	}
	
	public DataSource getDataSource(){
		return data_source;
	}
	
	public void addTable(DataTable t){
		table_map.put(t.getName(), t);
		//sortTables();
		//fireDataSourceListeners()
	}
	
	public DataTable getTable(String name){
		/*
		for (int i = 0; i < tables.size(); i++)
			if (tables.get(i).getName().equals(name))
				return tables.get(i);
		return null;
		*/
		return table_map.get(name);
	}
	
	public int findTable(DataTable t){
		//table list is limited; no need for fancy search
		ArrayList<DataTable> tables = getTables();
		for (int i = 0; i < tables.size(); i++)
			if (tables.get(i).equals(t))
				return i;
		return -1;
	}
	
	public void removeTable(DataTable t){
		table_map.remove(t.getName());
		//tables.remove(t);
	}
	
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		
		ArrayList<DataTable> tables = getTables();
		
		//add all tables in set
		for (int i = 0; i < tables.size(); i++)
			treeNode.add(tables.get(i).issueTreeNode());
		
	}
	
	@Override
	public String getTreeLabel(){
		return "Tables";
	}
	
	@Override
	public String toString(){
		return "TableSet";
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
	
	DataTable current_table;

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException{
		
		if (localName.equals("DataTable")){
			if (current_table != null)
				throw new SAXException("DataTableSet: DataTable tag encountered, but table already initiated.");
			current_table = new DataTable(attributes.getValue("name"));
			return;
			}
		
		if (current_table != null){
			current_table.handleXMLElementStart(localName, attributes, null);
			return;
			}
		
	}

	@Override
	public void handleXMLElementEnd(String localName) throws SAXException{
		
		if (localName.equals("DataTable")){
			if (current_table == null)
				throw new SAXException("DataTableSet: DataTable ended without being started.");
			this.addTable(current_table);
			current_table = null;
			return;
			}
		
		if (current_table != null){
			current_table.handleXMLElementEnd(localName);
			return;
			}
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocalName() {
		return "DataTableSet";
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		String _tab = XMLFunctions.getTab(tab);
		
		writer.write(_tab + "<DataTableSet>\n");
		
		ArrayList<DataTable> tables = getTables();
		
		for (int i = 0; i < tables.size(); i++){
			tables.get(i).writeXML(tab + 1, writer, options, progress_bar);
			writer.write("\n");
			}
		
		writer.write(_tab + "</DataTableSet>\n");
		
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

	
}