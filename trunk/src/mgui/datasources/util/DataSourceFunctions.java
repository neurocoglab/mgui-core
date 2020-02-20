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

package mgui.datasources.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import mgui.datasources.DataConnection;
import mgui.datasources.DataField;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceDriver;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataTable;
import mgui.datasources.DataTypes;
import mgui.interfaces.InterfaceSession;

public class DataSourceFunctions {

	/******************************************
	 * Attempts to creates a new data source, with the given parameters. The
	 * password should already be encrypted using 
	 * <code>SecureDataSourceFunctions.getEncryptedPassword()<code>.
	 * 
	 * @param name
	 * @param url
	 * @param login
	 * @param encrypted_password System-encrypted version of the password
	 * @return the new datasource, if successful; <code>null</code> otherwise.
	 * @see mgui.datasources.security.SecureDataSourceFunctions#getEncryptedPassword(String)
	 */
	public static DataSource createNewDataSource(String driver,
												 String name,
												 String url,
												 String login,
												 String encrypted_password){
		
		DataConnection connection = new DataConnection();
		connection.setName(name);
		connection.setUrl(url);
		connection.setLogin(login);
		connection.setDriver(driver);
		connection.setPassword(encrypted_password);
		
		DataSource source = new DataSource(connection);
		
		if (!source.create()) return null;
		
		return source;
	}
	
	/********************************************************
	 * Drops the specified data source; i.e., deletes it entirely.
	 * 
	 * @param source
	 * @return
	 */
	public static boolean dropDataSource(DataSource source){
		
		try{
			source.getDataSourceDriver().dropDatabase(source);
			return true;
		}catch (DataSourceException ex){
			InterfaceSession.handleException(ex);
			return false;
			}
		
	}
	
	
	/********************************************************
	 * Generates a set of SQL statements which transform {@code old_table} into {@code new_table}.
	 * Statements must be run in sequence; order is critical for some operations.
	 * 
	 * @param source
	 * @param old_table
	 * @param new_table
	 * @param changed_names
	 * @return
	 */
	public static ArrayList<String> getEditStatements(DataSource source, DataTable old_table, DataTable new_table, 
													HashMap<String,String> changed_names){
		
		ArrayList<String> statements = new ArrayList<String>();
		
		// Find differences between tables
		if (old_table.getName() != new_table.getName()){
			statements.add(getRenameTableStatement(old_table.getName(), new_table.getName()));
			}
		
		// Fields
		ArrayList<DataField> old_fields = old_table.getFieldList();
		ArrayList<DataField> new_fields = new_table.getFieldList();
		
		TreeSet<DataField> add_fields = new TreeSet<DataField>();
		TreeSet<DataField> remove_fields = new TreeSet<DataField>();
		TreeSet<DataField> rename_fields = new TreeSet<DataField>();
		TreeSet<DataField> update_fields = new TreeSet<DataField>();
		TreeSet<DataField> drop_key_fields = new TreeSet<DataField>();
		TreeSet<DataField> add_key_fields = new TreeSet<DataField>();
		
		for (int i = 0; i < new_fields.size(); i++){
			DataField new_field = new_fields.get(i);
			
			// Does it have a changed name?
			String new_name = new_field.getName();
			String old_name = new_name;
			if (changed_names.containsKey(new_name)){
				rename_fields.add(new_field);
				old_name = changed_names.get(new_name);
				}
			
			DataField old_field = new_table.getField(old_name);
			if (old_field == null){
				// This is a new field
				add_fields.add(new_field);
			}else{
				// Check for changes
				if (new_field.isKeyField() != old_field.isKeyField()){
					if (new_field.isKeyField())
						add_key_fields.add(new_field);
					else
						drop_key_fields.add(new_field);
				}else if (!equalFields(new_field, old_field, true)){
					update_fields.add(new_field);
					}
				}
			}
		
		// Look for removed tables
		for (int i = 0; i < old_fields.size(); i++){
			DataField old_field = old_fields.get(i);
			// If this isn't a renamed field, it must be removed
			if (!changed_names.containsValue(old_field.getName()))
				remove_fields.add(old_field);
			}
		
		// Construct SQL statements
		DataSourceDriver driver = source.getDataSourceDriver();
		
		// Remove fields
		DataField field = remove_fields.pollFirst();
		while (field != null){
			statements.add(getRemoveFieldStatement(new_table.getName(), field.getName(), driver));
			field = remove_fields.pollFirst();
			}
		
		// Add fields
		field = add_fields.pollFirst();
		while (field != null){
			statements.add(getAddFieldStatement(new_table.getName(), field, driver));
			field = add_fields.pollFirst();
			}
		
		// Rename fields
		field = rename_fields.pollFirst();
		while (field != null){
			String new_name = field.getName();
			String old_name = changed_names.get(new_name);
			statements.add(getRenameFieldStatement(new_table.getName(), old_name, new_name, driver));
			field = rename_fields.pollFirst();
			}
		
		// Update fields
		field = update_fields.pollFirst();
		while (field != null){
			String old_name = field.getName();
			if (changed_names.containsKey(old_name)) old_name = changed_names.get(old_name);
			DataField old_field = old_table.getField(old_name);
			statements.add(getUpdateFieldStatement(new_table.getName(), old_field, field, driver));
			field = update_fields.pollFirst();
			}
		
		// Drop primary keys
		field = drop_key_fields.pollFirst();
		while (field != null){
			statements.add(getChangePrimaryKeyStatement(new_table.getName(), field, driver));
			field = drop_key_fields.pollFirst();
			}
		
		// Add primary keys
		field = add_key_fields.pollFirst();
		while (field != null){
			statements.add(getChangePrimaryKeyStatement(new_table.getName(), field, driver));
			field = add_key_fields.pollFirst();
			}
		
		return statements;
		
	}
	
	/**************************************************
	 * Compare fields for equality
	 * 
	 * @param f1
	 * @param f2
	 * @return
	 */
	public static boolean equalFields(DataField f1, DataField f2){
		
		return equalFields(f1, f2, false);
		
	}
	
	/**************************************************
	 * Compare fields for equality
	 * 
	 * @param f1
	 * @param f2
	 * @param ignore_name   If true, doesn't include the names in the comparison
	 * @return
	 */
	public static boolean equalFields(DataField f1, DataField f2, boolean ignore_name){
		
		boolean equal = true;
		
		equal &= f1.isKeyField() && f2.isKeyField();
		equal &= f1.isUnique() && f2.isUnique();
		equal &= f1.getLength() == f2.getLength();
		equal &= f1.getDataType() == f2.getDataType();
		equal &= f1.isRequired() && f2.isRequired();
		equal &= ignore_name || f1.getName().equals(f2.getName());
		
		return equal;
		
	}
	
	 /************************************************
	   * Returns an SQL statement for adding {@code field} to {@code table}.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return
	   */
	  public static String getAddFieldStatement(String table, DataField field){
		  
		  return getAddFieldStatement(table, field, new DataSourceDriver());
	  }
	
	
	 /************************************************
	   * Returns an SQL statement for adding {@code field} to {@code table}.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @param driver
	   * @return
	   */
	  public static String getAddFieldStatement(String table, DataField field, DataSourceDriver driver){
		  
		  String q = driver.getSQLQuote();
		  
		  String SQLStr = "ALTER TABLE " + q + table + q + " ADD COLUMN " +
				  q + field.getName() + q + " " + DataTypes.getSQLTypeStr(field.getDataType(),
						  field.getLength());
  
		  if (field.isKeyField()){
			  SQLStr = SQLStr + " PRIMARY KEY";
		  }else{
			  if (field.isUnique()){
				  SQLStr = SQLStr + " UNIQUE";
				  }
			  if (field.isRequired()){
				  SQLStr = SQLStr + " NOT NULL";
				  }
		  	  }
		  
		  return SQLStr;
		  
	  }
	  
	  /************************************************
	   * Returns an SQL statement for removing {@code field} from {@code table}.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return
	   */
	  public static String getRemoveFieldStatement(String table, String field){
		  return getRemoveFieldStatement(table, field, new DataSourceDriver());
	  }
	  
	  /************************************************
	   * Returns an SQL statement for removing {@code field} from {@code table}.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @param driver
	   * @return
	   */
	  public static String getRemoveFieldStatement(String table, String field, DataSourceDriver driver){
		  
		  String q = driver.getSQLQuote();
		  
		  String SQLStr = "ALTER TABLE " + q + table + q + " DROP COLUMN " + q + field + q;
		
		  return SQLStr;
	  }
	  
	  /************************************************
	   * Returns an SQL statement to rename {@code old_table} to {@code new_table}.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return
	   */
	  public static String getRenameTableStatement(String old_name, String new_name){
		  return getRenameTableStatement(old_name, new_name, new DataSourceDriver());
	  }
	  
	  /************************************************
	   * Returns an SQL statement to rename {@code old_table} to {@code new_table}.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return
	   */
	  public static String getRenameTableStatement(String old_name, String new_name, DataSourceDriver driver){
		  
		  String q = driver.getSQLQuote();
		  
		  String SQLStr = "ALTER TABLE " + q + old_name + q + " RENAME TO " +
				  		  q + new_name + q;
		
		  return SQLStr;
	  }
	  
	  
	  /************************************************
	   * Returns an SQL statement to rename {@code old_field} to {@code new_field}.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return
	   */
	  public static String getRenameFieldStatement(String table, String old_field, String new_field){
		  return getRenameFieldStatement(table, old_field, new_field, new DataSourceDriver());
	  }
	  
	  /************************************************
	   * Returns an SQL statement to rename {@code old_field} to {@code new_field}.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return
	   */
	  public static String getRenameFieldStatement(String table, String old_name, String new_name, DataSourceDriver driver){
		  
		  String q = driver.getSQLQuote();
		  
		  String SQLStr = "ALTER TABLE " + q + table + q + " RENAME COLUMN " + q + old_name + q +
				  		  " TO " + q + new_name + q;
		
		  return SQLStr;
	  }
	  
	  
	  /************************************************
	   * Returns an SQL statement to update {@code old_field} to match {@code new_field}.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return
	   */
	  public static String getUpdateFieldStatement(String table, DataField old_field, DataField new_field){
		  return getUpdateFieldStatement(table, old_field, new_field, new DataSourceDriver());
	  }
	  
	  /************************************************
	   * Returns an SQL statement to update {@code old_field} to match {@code new_field}.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return
	   */
	  public static String getUpdateFieldStatement(String table, DataField old_field, DataField new_field, DataSourceDriver driver){
		  
		  String q = driver.getSQLQuote();
		  
		  String SQLStr = "ALTER TABLE " + q + table + q;
		  String comma = "";
		  
		  if (old_field.isUnique() != new_field.isUnique()){
			  String prep = "SET";
			  if (!new_field.isUnique()) prep = "DROP";
			  SQLStr = SQLStr + " ALTER COLUMN " + q + new_field.getName() + q + " " + prep + " UNIQUE";
			  comma = ", ";
		  	  }
		  
		  if (old_field.getDataType() != new_field.getDataType()){
			  SQLStr = SQLStr + comma + " ALTER COLUMN " + q + new_field.getName() + q + " TYPE " + 
					   DataTypes.getSQLTypeStr(new_field.getDataType(), new_field.getLength());
			  comma = ", ";
		  	  }
		  
		  if (old_field.isRequired() != new_field.isRequired()){
			  String prep = "SET";
			  if (!new_field.isUnique()) prep = "DROP";
			  SQLStr = SQLStr + comma + " ALTER COLUMN " + q + new_field.getName() + q + " " + prep + " NOT NULL";
			  comma = ", ";
		  	  }
		
		  return SQLStr;
	  }
	  
	  /************************************************
	   * Returns an SQL statement to update {@code table} by adding or dropping a primary key for {@code new_field}.
	   * Dropping assumes the field is named "{field_name}_pkey"
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return
	   */
	  public static String getChangePrimaryKeyStatement(String table, DataField new_field){
		  return getChangePrimaryKeyStatement(table, new_field, new DataSourceDriver());
	  }
	  
	  /************************************************
	   * Returns an SQL statement to update {@code table} by adding or dropping a primary key for {@code new_field}.
	   * Dropping assumes the field is named "{field_name}_pkey"
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return
	   */
	  public static String getChangePrimaryKeyStatement(String table, DataField new_field, DataSourceDriver driver){
		  
		  String q = driver.getSQLQuote();
		  
		  String SQLStr = "ALTER TABLE " + q + table + q;
		  
		  if (new_field.isKeyField()){
			  SQLStr = SQLStr + " ADD PRIMARY KEY (" + q + new_field.getName() + q + ")";
		  }else{
			  SQLStr = SQLStr + " DROP CONSTRAINT " + q + new_field.getName() + q + "_pkey";
		  	  }
		  
		  return SQLStr;
	  }
	
}