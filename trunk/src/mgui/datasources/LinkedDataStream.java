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

import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.numbers.NumberFunctions;
import mgui.resources.icons.IconObject;

/***************************************************
 * Provides a link to a data source, using a unique field as the link field.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class LinkedDataStream<T extends Comparable<T>> extends AbstractInterfaceObject implements IconObject {

	protected String link_field;
	protected String data_table;
	protected DataSource data_source;
	protected boolean cached = false;
	protected HashMap<T, HashMap<String, Object>> cache = new HashMap<T, HashMap<String, Object>>();
	protected DataRecordSet record_set;
	protected DataIndex<T> index;
	protected double unknown_value = 0;
	//protected boolean is_name_mapped = false;
	
	protected LinkedDataStream(){
		
	}
	
	public LinkedDataStream(DataSource data_source, String data_table, String link_field) throws DataSourceException{
		this(data_source, data_table, link_field, false, "No-name");
	}
	
	public LinkedDataStream(DataSource data_source, String data_table, String link_field, boolean cached) throws DataSourceException{
		this(data_source, data_table, link_field, cached, "No-name");
	}
	
	public LinkedDataStream(DataSource data_source, String data_table, String link_field, String name) throws DataSourceException{
		this(data_source, data_table, link_field, false, name);
	}
	
	public LinkedDataStream(DataSource data_source, String data_table, String link_field, boolean cached, String name) throws DataSourceException{
		this.data_source = data_source;
		this.data_table = data_table;
		DataTable table = data_source.getTableSet().getTable(data_table);
		if (table == null)
			throw new DataSourceException("LinkedDataStream: DataSource '" + data_source.getName() + "' has no table" +
										  " named '" + data_table + "..");
		this.link_field = link_field;
		DataField field = table.getField(link_field);
		if (field == null)
			throw new DataSourceException("LinkedDataStream: DataTable '" + table.getName() + "' has no field" +
										  " named '" + link_field + "..");
		
		//make an index if one doesn't already exist
		refresh();
		this.setName(name);
		this.cached = cached;
	}
	
	/***********************************************************
	 * Returns an instance of {@code LinkedDataStream} of the correct generic type, based on the data type
	 * of the linked field.
	 * 
	 * @param data_source
	 * @param data_table
	 * @param link_field
	 * @param cached
	 * @param name
	 * @param has_name_map
	 * @return A new instance of the correct generic type
	 * @throws DataSourceException
	 */
	public static LinkedDataStream<?> getInstance(DataSource data_source, 
												  String data_table, 
												  String link_field, 
												  boolean cached, 
												  String name,
												  boolean is_name_map) throws DataSourceException{

		DataTable table = data_source.getTableSet().getTable(data_table);
		if (table == null)
			throw new DataSourceException("LinkedDataStream: DataSource '" + data_source.getName() + "' has no table" +
										  " named '" + data_table + "..");
		
		DataField field = table.getField(link_field);
		if (field == null)
			throw new DataSourceException("LinkedDataStream: DataTable '" + table.getName() + "' has no field" +
										  " named '" + link_field + "..");
		
		if (is_name_map) 
			return new LinkedDataStream<String>(data_source, data_table, link_field, cached, name);
		
		int sql_type = field.getDataType();
		// Note: all numeric links
		switch (sql_type){
			case Types.INTEGER:
				return new LinkedDataStream<Integer>(data_source, data_table, link_field, cached, name);
			case Types.DOUBLE:
				return new LinkedDataStream<Double>(data_source, data_table, link_field, cached, name);
			case Types.FLOAT:
				return new LinkedDataStream<Integer>(data_source, data_table, link_field, cached, name);
			case Types.VARCHAR:
				return new LinkedDataStream<String>(data_source, data_table, link_field, cached, name);
			default:
				return null;
			}
		
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/linked_data_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/linked_data_20.png");
		return null;
	}
	
	public static Icon getIcon(){
		return (new LinkedDataStream()).getObjectIcon();
	}
	
	public void refresh() throws DataSourceException{
		DataTable table = data_source.getTableSet().getTable(data_table);
		try{
			index = (DataIndex<T>)table.getIndex(link_field);
		}catch (ClassCastException ex){
			throw new DataSourceException("LinkedDataStream: Index for field '" + link_field + "' of wrong type: " 
										  + ex.getMessage());
			}
		
		if (index == null){
			index = new DataIndex<T>(link_field);
			table.addIndex(link_field, index);
			}
		
		record_set = new DataRecordSet(table.getDataSource());
		record_set.set(table);
		index.populate(record_set);		//NB: this takes care of row count
	}
	
	public String getLinkTable(){
		return data_table;
	}
	
	public String getLinkField(){
		return link_field;
	}
	
	public void setUnknownValue(double value){
		unknown_value = value;
	}
	
	/************************************
	 * Value to use when a key is not found in the linked source
	 * 
	 * @return
	 */
	public double getUnknownValue(){
		return unknown_value;
	}
	
	public DataSource getDataSource(){
		return data_source;
	}
	
	/********************************
	 * Returns all fields which are numeric.
	 * 
	 * @return
	 */
	public ArrayList<String> getNumericFields(){
		try{
			DataTable table = data_source.getTableSet().getTable(data_table);
			Iterator<String> itr = table.getFields().keySet().iterator();
			ArrayList<String> fields = new ArrayList<String>();
			
			while (itr.hasNext()){
				String field = itr.next();
				if (table.getField(field).isNumeric() && !field.equals(link_field))
					fields.add(field);
				}
			
			return fields;
		}catch (DataSourceException ex){
			ex.printStackTrace();
			return null;
			}
	}
	
	/**********************************
	 * Returns the value for <code>field</code> at record where link field equals
	 * <code>key</code>.
	 * 
	 * @param key
	 * @param field
	 * @throws DataSourceException if no record exists for <code>key</code>
	 * @return
	 */
	public double getNumericValue(T key, String field) throws DataSourceException{
		Object obj = getValue(key, field);
		if (!NumberFunctions.isNumeric(obj))
			throw new DataSourceException("LinkedDataStream: returned value is not numeric!");
		
		return NumberFunctions.getValueForObject(obj);
	}
	
	public Object getValue(T key, String field) throws DataSourceException{
		if (cached){
			if (cache.get(key) == null)
				cache.put(key, new HashMap<String, Object>());
			Object obj = cache.get(key).get(field);
			if (obj != null) return obj;
			}
		
		if (index == null) refresh();
		Integer record = index.seek(key);
		if (record < 0)
			throw new DataSourceException("LinkedDataStream: No record for field '" + link_field + "' = '" + key +"'..");
		int idx = record_set.getFieldIndex(field);
		try{
			Object obj = record_set.getValueAt(record, idx);
			if (obj == null)
				throw new DataSourceException("LinkedDataStream: Null record for field '" + link_field + "' = '" + key +"'..");
			if (cached) cache.get(key).put(field, obj);
			return obj;
		}catch (SQLException ex){
			throw new DataSourceException("LinkedDataStream: Error getting data for field '" + link_field + "' = '" + key +"':\n" + 
										  ex.getMessage());
			}
		
	}
	
	public void clearCache(){
		cache.clear(); // = new HashMap<Comparable<?>, HashMap<String, Object>>();
	}
	
	protected String getSQLForKey(String key){
		try{
			DataTable table = data_source.getTableSet().getTable(data_table);
			String sql = "SELECT * FROM " + table.getName() + " WHERE (";
			sql = sql + link_field + " = '" + key + "')";
			return sql;
		}catch (DataSourceException ex){
			ex.printStackTrace();
			return null;
			}
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		
		super.setTreeNode(treeNode);
		treeNode.add(new DefaultMutableTreeNode("Data Source: " + data_source.getName()));
		treeNode.add(new DefaultMutableTreeNode("Data Table: " + data_table));
		treeNode.add(new DefaultMutableTreeNode("Link Field: " + link_field));
		treeNode.add(new DefaultMutableTreeNode("Cached: " + cached));
		
	}
	
	@Override
	public String getTreeLabel(){
		return getName();
	}
	

}