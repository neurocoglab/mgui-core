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
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import mgui.datasources.security.SecureDataSourceFunctions;
import mgui.datasources.util.DataSourceFunctions;
import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.InterfaceWorkspace;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.datasources.DataSourceListener;
import mgui.interfaces.events.DataSourceEvent;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;

/*************************
 * Acts as a port into the JDBC interface. Specific data source objects should extend this
 * class's basic implementations. In general, communicates to database drivers (e.g. LDBC?)
 * to retrieve, modify, and store data using SQL statements. 
 * 
 * <p>Can also: 
 * 
 * <ul>
 * <li>Build a DataTableSet from meta data
 * <li>Create/delete databases
 * </ul>
 *   
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class DataSource extends AbstractInterfaceObject implements Cloneable,
																   IconObject,
																   PopupMenuObject,
																   XMLObject{

	protected DataTableSet tableSet;
	protected DataConnection conn;
	protected boolean isConnected;
	
	protected Connection connection;
	protected ArrayList<DataQuery> queries = new ArrayList<DataQuery>();
	protected ArrayList<DataSourceListener> listeners = new ArrayList<DataSourceListener>();
	protected ArrayList<DataTable> temp_tables = new ArrayList<DataTable>();
	protected InterfaceWorkspace workspace;
	
	public DataSource(){
		setConnection(new DataConnection());
	}
	
	public DataSource(String name){
		setConnection(new DataConnection(name));
	}
	
	public DataSource(DataConnection dc){
		//setName(dc.getName());
		setConnection(dc);
	}
	
	public DataConnection getConnection(){
		return conn;
	}
	
	
	
	public void setWorkspace(InterfaceWorkspace workspace){
		this.workspace = workspace;
	}
	
	public InterfaceWorkspace getWorkspace(){
		return workspace;
	}
	
	/************************************************
	 * Returns a list of the queries associated with this data source.
	 * 
	 * @return
	 */
	public ArrayList<DataQuery> getDataQueries(){
		return new ArrayList<DataQuery>(queries);
	}
	
	public DataQuery getDataQuery(String query){
		for (int i = 0 ;i < queries.size(); i++)
			if (queries.get(i).getName().equals(query))
				return queries.get(i);
		return null;
	}
	
	public boolean isConnected(){
		return isConnected;
	}
	
	public static Icon getIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/data_source_17.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/data_source_17.png");
		return null;
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/data_source_17.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/data_source_17.png");
		return null;
	}
	
	/***************************************
	 * Sets this data source's connection parameters.
	 * 
	 * @param dc The <code>DataConnection</code> specifying the connection parameters
	 * @see mgui.datasources.DataConnection 
	 * 
	 ****/
	public void setConnection(DataConnection dc){
		conn = dc;
	}
	
	//open driver class if it exists
	protected boolean openDriver(String driver){
		try{
			Class.forName(driver);
			return true;
		}
		catch (ClassNotFoundException e){
			//e.printStackTrace();
			InterfaceSession.log("Driver '" + driver + "' not found.", LoggingType.Errors);
		}
		return false;
	}
	
	@Override
	public void destroy(){
		disconnect();
		isDestroyed = true;
	}
	
	/*********************************************
	 * Create a new data source with the given connection parameters. This method will only work
	 * if the specified driver/engine supports the creation of new databases.
	 * 
	 * @return <code>true</code> if successful, <code>false</code> otherwise.
	 */
	public boolean create(){
		if (conn == null){
			InterfaceSession.log("DataSource.create: no connection specified..");
			return false;
			}
		if (!conn.isValid()){
			InterfaceSession.log("DataSource.create: connection is invalid: " + conn.toString());
			return false;
			}
			
		try{
			DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriverByClass(conn.getDriver());
			if (connection == null){
				if (driver == null){
					InterfaceSession.log("DataSource: create: No driver found for class: " + conn.getDriver());
					return false;
					}
				connection = driver.getConnection();
				}
			Statement s = connection.createStatement();
			String sql = driver.getCreateDatabaseSQL(conn.getName());
			
			s.executeUpdate(sql);
			connection = null;
			return true;
			}
		catch (SQLException e){
			InterfaceSession.log("SQL Exception encountered while creating database '" +
					conn.getName() + "'");
			e.printStackTrace();
			connection = null;
			return false;
		}catch (Exception e){
			InterfaceSession.log("Exception encountered while creating database '" +
					conn.getName() + "'");
			e.printStackTrace();
			connection = null;
			return false;
			}
			  
	 }
	
	/*********************************************
	 * Deletes an existing data source with the given connection parameters. This method will only work
	 * if the specified driver/engine supports the deletion of databases.
	 * 
	 * TODO: transfer driver-specific code to their <code>DataSourceDriver</code> class. 
	 * 
	 * @return <code>true</code> if successful, <code>false</code> otherwise.
	 */
	 public boolean delete(){
	    if (conn == null) return false;
	    if (conn.getName() == null) return false;
	    boolean success = false;
	    try {
	    	success = DataSourceFunctions.dropDataSource(this);
//    		if (!isConnected || connection == null || connection.isClosed()) return false;
//    		Statement s = connection.createStatement();
//    		s.executeUpdate("DROP DATABASE " + conn.getName());
//    		success = true;
	    	}
	    catch (Exception e) {
	    	InterfaceSession.log("Error deleting database '" + conn.getName() + "'");
	    	InterfaceSession.handleException(e);
	    	return false;
	    	}
	    return success;
	  }
	
	/*******************************************
	 * Attempts to connect to a data source with the given parameters.
	 * 
	 * @return <code>true</code> if successful; <code>false</code> otherwise.
	 * @throws DataSourceException
	 */
	public boolean connect() throws DataSourceException{
		
		if (conn == null){
			InterfaceSession.log("DataSource.connect: no connection specified..", 
								 LoggingType.Errors);
			return false;
			}
		if (!conn.isValid()){
			InterfaceSession.log("DataSource.connect: connection is invalid: " + conn.toString(), 
								 LoggingType.Errors);
			return false;
			}
		
		if (connection == null){
		      try {
		        if (!openDriver(conn.getDriver())) return false;
		        
		        String login = conn.getLogin();
		        String pass = conn.getPassword();
		        if (login == null || login.length() == 0 || pass == null || pass.length() == 0){
		        	connection = DriverManager.getConnection(conn.getUrl());
		        	isConnected = true;
			        return setTableSet();
		        	}
		        
		        //connects using encrypted password
		        connection = SecureDataSourceFunctions.getSecureConnection(conn.getUrl(),
		        										 				   conn.getLogin(), 
		        										 				   conn.getPassword());
		        isConnected = true;
		        return setTableSet();
		      }
		      catch (Exception e) {
		    	  InterfaceSession.log("Exception encountered while attempting to connect to " +
			    					   "'" + conn.getUrl() + "': " +
			    					   e.getMessage(), 
			    					   LoggingType.Errors);
		    	  //e.printStackTrace();
		    	 
		      	}
		      return false;
		    }

	    //shouldn't get here...?
	    try{
	    	InterfaceSession.log("DataSource.connect: connection already defined?: " + conn.toString(), 
					 			 LoggingType.Errors);
	      if (!connection.isClosed()){
	    	isConnected = false;
	        return false;
	      	}
	      connection = DriverManager.getConnection(conn.getUrl(),
					 							   conn.getLogin(), 
					 							   conn.getPassword());
	      isConnected = true;
	      return setTableSet();
	    }
	    catch (java.sql.SQLException e){
	    	InterfaceSession.log("Exception encountered while attempting to connect to" +
	    						 "'" + conn.getUrl() + "'", 
		    					 LoggingType.Errors);
	    	e.printStackTrace();
	    	}
	   isConnected = false;
	   return false;
	}
	
	/***********************************
	 * Attempts to disconnect from the connected data source. If successful this will also
	 * remove all temporary tables from the data source.
	 * 
	 * @return <code>true</code> if successful; <code>false</code> otherwise.
	 */
	public boolean disconnect(){
		if (!isConnected) return false;
		if (connection != null){
		      try {
		        if (!connection.isClosed()){
			        connection.close();
			        connection = null;
			        isConnected = false;
			        removeTempTables();
			        fireDataSourceListeners();
			        return true;
		        	}
		      	}
		      catch (java.sql.SQLException e) {
		        e.printStackTrace();
		      	}
			}
		return false;
	}
	
	/****************************
	 * Attempts to add a temporary table to data source
	 * Returns true if successful. Temp tables can be cleared
	 * using removeTempTables() or disconnect()
	 * 
	 * @param thisTable
	 * @return
	 */
	public boolean addTempTable(DataTable thisTable){
		if (!addDataTable(thisTable)) return false;
		this.temp_tables.add(thisTable);
		return true;
	}
	
	public boolean removeTempTables(){
		boolean success = true;
		ArrayList<DataTable> temp_list = new ArrayList<DataTable>(temp_tables);
		int j = 0;
		for (int i = 0; i < temp_list.size(); i++){
			if (this.removeDataTable(temp_list.get(i))){
				temp_tables.remove(j);
			}else{
				success = false;
				j++;
				}
			}
		return success;
	}
	
	/*****************************************************************
	 * Returns the {@link DataSourceDriver} associated with this data source.
	 * 
	 * @return
	 */
	public DataSourceDriver getDataSourceDriver(){
		return InterfaceEnvironment.getDataSourceDriverByClass(conn.getDriver());
	}
	
	/*****************************
	 * Attempt to add a table to data source. Also adds this table to the table set.
	 * 
	 * @param DataTable thisTable
	 * @return <code>true</code> if successful; <code>false</code> otherwise.
	 */ 
	 public boolean addDataTable(DataTable thisTable){
	    if (!isConnected || connection == null)
	      return false;
	    //TODO: attempt to add table to connection
	    
	    
	    boolean blnBracket = false;

	    DataSourceDriver ds_driver = getDataSourceDriver();
	    String q = ds_driver.getSQLQuote();
	    String table_name = ds_driver.getSQLName(thisTable.getName());
	    //String SQLStr = "CREATE TABLE `" + thisTable.getName() + "`";
	    String SQLStr = "CREATE TABLE " + q + table_name + q;
	   
	    Iterator<DataField> fields = thisTable.getFields().values().iterator();
	    
	    while (fields.hasNext()){
		      if (!blnBracket){
		        blnBracket = true;
		        SQLStr = SQLStr + " (";
		      }
		      DataField field = fields.next();
		      String sql = null;
		      
		      sql = field.getSQLString(ds_driver);
		      
		      if (sql == null){
			    	InterfaceSession.log("DataSource: Could not create valid SQL statement for field: '" +
			    						 field.getName() + "'.", 
			    						 LoggingType.Errors);
			    	return false;
			    	}
		      SQLStr = SQLStr + sql;
		      
		      if (fields.hasNext())
		        SQLStr = SQLStr + ", ";
	    	  }
	    
	    // Primary keys
	    ArrayList<String> keys = thisTable.getPrimaryKeys();
	    if (keys.size() > 0){
	    	SQLStr = SQLStr + ", PRIMARY KEY (";
	    	for (int i = 0; i < keys.size(); i++){
	    		if (i > 0) SQLStr = SQLStr + ", ";
	    		SQLStr = SQLStr + q + keys.get(i) + q;
	    		}
	    	SQLStr = SQLStr + ")";
	    	}

	    //at least one field is necessary
	    if (blnBracket)
	      SQLStr = SQLStr + ")";
	    else
	    	return false;

	    try{
	      Statement sqlStmt = connection.createStatement();
	      sqlStmt.executeUpdate(SQLStr);
	    }
	    catch (Exception e){
	      //e.printStackTrace();
	    	InterfaceSession.log("DataSource: Error adding data table: " + e.getMessage(), LoggingType.Errors);
	      return false;
	    }
	    thisTable.setDataSource(this);
	    tableSet.addTable(thisTable);
	    fireDataSourceListeners();
	    return true;
	  }
	 
	 /***********************************
	  * Adds an SQL query to this data source.
	  * 
	  * @param query
	  * @return {@code false} if a query by this name already exists
	  */
	 public boolean addDataQuery(DataQuery query){
		 for (int i = 0; i < this.queries.size(); i++)
			 if (queries.get(i).getName().equals(query.getName())) return false;
		 queries.add(query);
		 fireDataSourceListeners();
		 return true;
	 }
	 
	 /************************************
	  * Remove an SQL query from this data source.
	  * 
	  * @param query
	  */
	 public void removeDataQuery(DataQuery query){
		 queries.remove(query);
		 fireDataSourceListeners();
	 }

	  /**********************************
	   * Removes the specified table from this data source.
	   * 
	   * @param thisTable
	   * @return
	   */
	  public boolean removeDataTable(DataTable thisTable){
		  
		  if (tableSet.findTable(thisTable) < 0)
			  return false;
		  
	      //build SQL statement
		  String q = this.getDataSourceDriver().getSQLQuote();
	      String SQLStr = "DROP TABLE " + q + thisTable.getName() + q;
	
	      //execute statement
	      try {
	         Statement sqlStmt = connection.createStatement();
	         sqlStmt.executeUpdate(SQLStr);
	         tableSet.removeTable(thisTable);
	      	 }
	      catch (SQLException e) {
	         //e.printStackTrace();
	    	 InterfaceSession.log("DataSource.removeDataTable: Could not remove table '" + thisTable.getName() + "': " +
				    			  e.getMessage(), 
				    			  LoggingType.Errors);
	    	 
	         return false;
	      	 }
	      return true;
	  }
	
	  /************************************************
	   * Attempts to adds a field to the specified table in this data source.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return <code>true</code> if successful; <code>false</code> otherwise.
	   */
	  public boolean addDataField(DataTable table, DataField field){
		  if (!isConnected || connection == null)
			  return false;
		
		  if (table == null)
			  return false;
		  
		  if (tableSet.findTable(table) < 0)
			  return false;
		
		  String SQLStr = DataSourceFunctions.getAddFieldStatement(table.getName(), field, getDataSourceDriver());
		
		  try{
			  Statement sqlStmt = connection.createStatement();
			  sqlStmt.executeUpdate(SQLStr);
			  table.addField(field);
		  }catch (SQLException e){
			  InterfaceSession.handleException(e);
			  return false;
		  	}
		  
		  return true;
	  }
	  
	 
	  
	  /************************************************
	   * Attempts to remove a field from the specified table in this data source.
	   * 
	   * @param thisTable
	   * @param thisField
	   * @return <code>true</code> if successful; <code>false</code> otherwise.
	   */
	  public boolean removeDataField(DataTable table, DataField field){
		  if (!isConnected || connection == null)
			  return false;
		
		  if (table == null)
			  return false;
		  
		  if (tableSet.findTable(table) < 0)
			  return false;
		  
		  String SQLStr = DataSourceFunctions.getRemoveFieldStatement(table.getName(), field.getName());
		  
		  try{
			  Statement sqlStmt = connection.createStatement();
			  sqlStmt.executeUpdate(SQLStr);
			  table.removeField(field);
		  }
		  catch (SQLException e){
	          e.printStackTrace();
	          return false;
	      	  } 
		  return true;
	  }
	  
	  /***************************************
	   * Returns a result set from the given SQL statement.
	   * 
	   * @param SQLStr
	   * @return the result set
	   * @throws DataSourceException
	   */
	  public ResultSet getResultSet(String SQLStr) throws DataSourceException{
		  if (!isConnected || connection == null)
		      return null;
		  try{
		      Statement sqlStmt = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
		                                                     ResultSet.CONCUR_UPDATABLE);
		      return sqlStmt.executeQuery(SQLStr);
		      }
	      catch (SQLException e){
	    	  //try without concurrency?
	    	  try{
	    		  Statement sqlStmt = connection.createStatement();
	    		  
	    		  return sqlStmt.executeQuery(SQLStr);
	    	  }catch (Exception e2){
	    		  throw new DataSourceException("DataSource.getResultSet: Exception encountered: " + e2.getMessage());
	    	  	}
	      }catch (Exception e){
	          throw new DataSourceException("DataSource.getResultSet: Exception encountered: " + e.getMessage());
	      	  }
	  }
	  
	  /**********************************************************
	   * Attempts to execute the given update query. This command should be used, e.g., for create table,
	   * insert, or delete queries.
	   * 
	   * @param query
	   * @return Success of execution
	   * @throws DataSourceException If something goes wrong...
	   */
	  public boolean executeUpdate(DataQuery query) throws DataSourceException{
		  
		  if (!isConnected || connection == null)
		      return false;
		  try{
		      executeStatement(query.getSQLStatement(getDataSourceDriver()));
		  }catch (SQLException e){
	    	  throw new DataSourceException("DataSource.executeUpdate: Exception encountered: " + e.getMessage());
	      	  }
		  
	      return true;
		  
	  }
	  
	  /*****************************************************
	   * Attempts to execute {@code SQL_statement} on this data source
	   * 
	   * @param SQL_statement
	   * @throws SQLException
	   */
	  public void executeStatement(String SQL_statement) throws SQLException{
		  
	      Statement statement = connection.createStatement();
	      statement.executeUpdate(SQL_statement);
		   
	  }
	  
	  /*******************************************************
	   * Returns a {@link DataRecordSet} object accessing the specified table in this data source.
	   * 
	   * @param table
	   * @return
	   * @throws DataSourceException
	   */
	  public DataRecordSet getRecordSet(DataSourceItem item) throws DataSourceException{
		  DataRecordSet record_set = new DataRecordSet(this);
		  record_set.set(item);
		  return record_set;
	  }
	  
	  /*
	  public DataRecordSet getRecordSet(DataQuery query) throws DataSourceException{
		  DataRecordSet set = new DataRecordSet(this);
		  set.set(query);
		  return set;
	  }
	  */
	  
	  /*******************************************
	   * Returns the set of tables for this data source. If none have been set, attempts to retrieve
	   * them from the source. Use <code>setTableSet()</code> to refresh this list from the source.
	   * 
	   * @return the set of tables, or <code>null</code> if none can be retrieved
	   */
	  public DataTableSet getTableSet() throws DataSourceException{
		  if (tableSet == null){
			  setTableSet();
		  	  }
		  return tableSet;
	  }
	  
	  /*******************************************
	   * Refreshes the list of tables from the data source.
	   * 
	   * @return <code>true</code> if successful; <code>false</code> otherwise.
	   * @throws DataSourceException if this source is not connected or an exception is encountered while connecting
	   */
	  public boolean setTableSet() throws DataSourceException{
		  return setTableSet(true);
	  }
	  
	  /*******************************************
	   * Refreshes the list of tables from the data source.
	   * 
	   * @param keep_existing If {@code true}, only tables which do not already exist in the table set
	   * 					  will be added; otherwise, these tables will be overwritten with the metadata
	   * 					  versions.
	   * @return <code>true</code> if successful; <code>false</code> otherwise.
	   * @throws DataSourceException if this source is not connected or an exception is encountered while connecting
	   */
	  public boolean setTableSet(boolean overwrite_existing) throws DataSourceException{
		  if (!isConnected) throw new DataSourceException("DataSource.setTableSet: Data source is not connected!");
		  if (tableSet == null) tableSet = new DataTableSet(this);
		  if (tableSet.setFromMetaData(overwrite_existing)){
			  fireDataSourceListeners();
			  return true;
		  	}
		  return false;
	  }
	  
	  /********************************************
	   * Sets the tables for this data source from a predefined set.
	   * 
	   * @param table_set
	   */
	  public void setTableSet(DataTableSet table_set){
		  this.tableSet = table_set;
		  tableSet.setDataSource(this);
		  fireDataSourceListeners();
	  }
	  
	  /***********************
	   * Creates and returns a temporary table, and adds it to this data source's list
	   * of temp tables. Temporary tables will be removed from the data source whenever
	   * disconnect() or clearTempTables() is called.
	   * @param name
	   * @return
	   */
	  public DataTable createTempTable(String name) throws DataSourceException{
		
		  
		  
		  
		  return null;
	  }
	  
	  //return a table list from meta data
	  /****************************
	   * Returns a list of tables retrieved from this data source's
	   * meta data.
	   * 
	   * @return ResultSet as described in DatabaseMetaData.getTables()
	   * @see java.sql.DatabaseMetaData
	   * **/
	  public ResultSet getTablesFromMetaData(){
		  
		  try{
			 DatabaseMetaData md = connection.getMetaData();
			 if (md == null) return null;
			 return md.getTables(null, null, "%", new String[]{"TABLE"});
		  	 }
		  catch (SQLException e){
			 e.printStackTrace();
		 	 }
		  return null;
	  }
	  
	  /***************************************************
	   * Returns a list of columns from this DataSource's metadata, for the specified table.
	   * 
	   * @see {@link DatabaseMetaData.getColumns}
	   * 
	   * @param table_name
	   * @return
	   */
	  public ResultSet getColumnsFromMetaData(String table_name){
		  
		  try{
				 DatabaseMetaData md = connection.getMetaData();
				 if (md == null) return null;
				 return md.getColumns(null, null, table_name, null);
			  	 }
			  catch (SQLException e){
				 e.printStackTrace();
			 	 }
			  return null;
		  
	  }
	  
	  /************************
	   * Returns a set of primary keys for the specified table
	   * 
	   * @return ResultSet as described in DatabaseMetaData.getPrimaryKeys()
	   * @see java.sql.DatabaseMetaData
	   */
	  public ResultSet getKeysFromMetaData(String table){
		  
		  try{
			 DatabaseMetaData md = connection.getMetaData();
			 if (md == null) return null;
			 return md.getPrimaryKeys(null, null, table);
		  	 }
		  catch (SQLException e){
			  //Likely doesn't support primary keys if we get here...
			  //Unfortunately there doesn't seem to be a supportsPrimaryKeys function
			  //so just suppressing the exception output here..
			  //e.printStackTrace();
		  	 }
		  return null;
	  }
	  
	  @Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		  super.setTreeNode(treeNode);
	
		  //add parameters
		  treeNode.add(getConnectedTreeNode());
		  
		  //add connection settings
		  if (conn != null){
			  //conn.setTreeNode();
			  treeNode.add(conn.issueTreeNode());
			  }
		  
		  //add table set
		  if (tableSet != null)
			  treeNode.add(tableSet.issueTreeNode());
		  
		  //add queries
		  QueriesTreeNode queryNode = new QueriesTreeNode(this, "Queries");
		  for (int i = 0; i < queries.size(); i++)
			  queryNode.add(queries.get(i).issueTreeNode());
		  
		  treeNode.add(queryNode);
		  
	  }
	  
	  protected InterfaceTreeNode getConnectedTreeNode(){
		  return new InterfaceTreeNode(new ConnectionObject()) ;
	  }
	  
	  class ConnectionObject extends AbstractInterfaceObject implements IconObject{

		@Override
		public Icon getObjectIcon() {
			
			return getConnectionIcon();
		}
		
		@Override
		public String getTreeLabel(){
			if (isConnected())
				return "Connected";
			else
				return "Disconnected";
		}
		  
	  }
	  
	  public ImageIcon getConnectionIcon(){
		  java.net.URL imgURL = null;
		  
		  if (isConnected)
			  imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/socket_connected_17.png");
		  else
			  imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/socket_disconnected_18.png");
		  if (imgURL != null)
			  return new ImageIcon(imgURL);
		  else
			  InterfaceSession.log("Cannot find resource: /mgui/resources/icons/socket_(dis)connected_18.png");
		  return null;
		  
	  }
	  
	  @Override
	  public String getTreeLabel(){
		  return getName();
	  }
	
	  @Override
	  public String getName(){
		  return conn.getName();
	  }
	  
	  @Override
	  public void setName(String name){
		  conn.setName(name);
	  }
	  
	  /****************************************
	   * Returns the name of the underlying database (as contained in its URL).
	   * 
	   * @return the source name
	   */
	  public String getSourceName(){
		  String url = conn.getUrl();
		  if (url == null) return "null";
		  if (!url.contains("/"))
			  if (url.contains(":"))
				  return url.substring(url.indexOf(":") + 1);
			  else
				  return url;
		  return conn.getUrl().substring(url.lastIndexOf("/") + 1);
	  }
	  
	  public void addListener(DataSourceListener l){
		  listeners.add(l);
	  }
	  
	  public void removeListener(DataSourceListener l){
		  listeners.remove(l);
	  }
	  
	  private void fireDataSourceListeners(){
		  //updateTreeNodes();
		  DataSourceEvent e = new DataSourceEvent(this);
		  for (int i = 0; i < listeners.size(); i++)
			  listeners.get(i).dataSourceUpdated(e);
		  updateTreeNodes();
	  }
	  
	  @Override
	public String toString(){
		  String retStr = "Data Source";
		  if (conn != null)
			  if (conn.getName() != null)
				  return retStr + ": '" + conn.getName() + "'";
		  return retStr + ": not defined";
	  }
	  
	  @Override
	public Object clone(){
		  
		  DataSource ds = new DataSource();
		  ds.conn = (DataConnection)conn.clone();
		  
		  return ds;
		  
	  }
	  
	  @Override
	  public InterfacePopupMenu getPopupMenu() {
			return getPopupMenu(null);
	  }
		
	  @Override
	  public InterfacePopupMenu getPopupMenu(List<Object> selected) {
		  InterfacePopupMenu menu = new InterfacePopupMenu(this);
			
		  menu.addMenuItem(new JMenuItem(getName(), getObjectIcon()));
			
		  menu.add(new JSeparator(), 1);
		  menu.add(new JSeparator(), 1);
			
		  JMenuItem item = null;
		  if (isConnected()){
			  item = new JMenuItem("Disconnect");
			  item.setActionCommand("Disconnect");
		  }else{
			  item = new JMenuItem("Connect");
			  item.setActionCommand("Connect");
		  	  }
		  
		  menu.addMenuItem(item);
		  
		  return menu;
	  }

	  @Override
	  public void handlePopupEvent(ActionEvent e) {
		 
		  	if (e.getActionCommand().equals("Connect")){
		  		if (isConnected()) return;
		  		try{
		  			if (this.connect()){
		  				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  						  "Connected to '" + getName() + "'.", 
							  						  "Connect to Data Source", 
							  						  JOptionPane.INFORMATION_MESSAGE);
		  				return;
		  				}
		  				
		  		}catch (DataSourceException ex){
		  			return;
		  			}
		  		JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Could not connect to '" + getName() + "'!", 
											  "Connect to Data Source", 
											  JOptionPane.ERROR_MESSAGE);
		  		return;
		  		}
		  	
		  	if (e.getActionCommand().equals("Disconnect")){
		  		if (!isConnected()) return;
		  			if (this.disconnect()){
		  				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
							  						  "Disconnected from '" + getName() + "'.", 
							  						  "Disconnect Data Source", 
							  						  JOptionPane.INFORMATION_MESSAGE);
		  				return;
		  				}
		  		JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Could not disconnect from '" + getName() + "'!", 
											  "Disconnect Data Source", 
											  JOptionPane.ERROR_MESSAGE);
		  		return;
		  		}
		  	
	  }

	  @Override
	  public void showPopupMenu(MouseEvent e) {
		  InterfacePopupMenu menu = getPopupMenu();
		  if (menu == null) return;
		  menu.show(e);
	  }
	  
	  /****************************************
	   * Sets this data source from an existing source. Attempts to disconnect if
	   * currently connected.
	   * 
	   * @param ds
	   * @return <code>true</code> if successful, <code>false</code> otherwise.
	   */
	  public boolean setFromDataSource(DataSource ds){
		  if (isConnected() && !disconnect())
			  return false;
		  this.setConnection((DataConnection)ds.conn.clone());
		  this.setName(ds.getName());
		  //if (ds.isConnected())
		  //  return connect();
		  return true;
	  }
	  
	  static class QueriesTreeNode extends InterfaceTreeNode{
		  
		  DataSource source;
		  
		  public QueriesTreeNode(DataSource source, String label){
			  super(label);
			  this.source = source;
		  }

		  @Override
		public InterfacePopupMenu getPopupMenu() {
			  InterfacePopupMenu menu = new InterfacePopupMenu(this);
			  menu.addMenuItem(new JMenuItem("Add new query"));
				
			  return menu;
		  }

		  @Override
		public void handlePopupEvent(ActionEvent e) {
				
			  if (!(e.getSource() instanceof JMenuItem)) return;
			  JMenuItem item = (JMenuItem)e.getSource();
				
			  if (item.getText().equals("Add new query")){
				  //TODO: implement specialized query builder dialog
				  String name = JOptionPane.showInputDialog(InterfaceSession.getDisplayPanel(), 
															 "Query name:", 
															 "Add New Query", 
															 JOptionPane.QUESTION_MESSAGE);
				  
				  if (name == null) return;
				  
				  String sql = JOptionPane.showInputDialog(InterfaceSession.getDisplayPanel(), 
														 "SQL text:", 
														 "Add New Query", 
														 JOptionPane.QUESTION_MESSAGE);
				  if (sql == null) return;
				 
				  DataQuery query = new DataQuery(name, source, sql);
				  source.addDataQuery(query);
				  return;
			  	  }
				
			}

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
		return null;
	}

	@Override
	public String getXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementEnd(String localName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocalName() {
		return "DataSource";
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		XMLType type = options.type;
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		writer.write(_tab + "<DataSource name='" + getName() + "'>\n");
		
		// Connection
		this.conn.writeXML(tab + 1, writer, options, progress_bar);
		//writer.write("\n");
		
		// Tables
		if (tableSet != null){
			this.tableSet.writeXML(tab + 1, writer, options, progress_bar);
		}else{
			// Happens when a data source has not connected
			writer.write("\n" + _tab2 + "<DataTableSet>\n" + 
								_tab2 + "</DataTableSet>\n");
			}
		
		// Queries
		writer.write(_tab2 + "<DataQueries>\n");
		
		for (int i = 0; i < queries.size(); i++){
			queries.get(i).writeXML(tab + 2, writer, options, progress_bar);
			}
		
		writer.write(_tab2 + "</DataQueries>\n" + 
					 _tab + "</DataSource>\n");
		
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
		return null;
	}
	  
}