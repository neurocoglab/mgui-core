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
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.NameMap;

/********************
 * Stores an open connection with a data source based upon an SQL specification.
 * TODO: Include methods for searching, editing, deleting, and appending data.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class DataRecordSet implements DataSet {

	protected ResultSet recordSet;
	protected DataSource dataSource;
	protected ArrayList<DataField> fields = new ArrayList<DataField>();
	protected boolean isUpdatable = true;
	protected boolean isAddNew;
	protected boolean isEdit;
	protected boolean isLocked;
	//public int currentRecord;
	private int rowCount;				//records accessed
	private String SQLStatement;
	private boolean sort_fields = true;
	protected NameMap field_map = new NameMap();		//Sorted indices
	protected NameMap field_indices = new NameMap();	//Actual indices
	
	public DataRecordSet(){
		
	}
	
	public DataRecordSet(DataSource ds){
		setDataSource(ds);
	}
	
	public void setDataSource(DataSource ds){
		dataSource = ds;
	}
	
	public boolean set(DataSourceItem item) throws DataSourceException{
		SQLStatement = item.getSQLStatement(dataSource.getDataSourceDriver());
		this.sort_fields = item.hasSortedFields();
		if (!setRecordSet())
			return false;
		return setFields();
	}
		
	public ArrayList<DataField> getFields(){
		return new ArrayList<DataField>(fields);
	}
	
	/***********************************************
	 * Specifies whether this record set can be updated. 
	 * 
	 * @return
	 */
	public boolean isUpdatable(){
		return isUpdatable;
	}
	
	protected boolean setRecordSet(String SQLStr) throws DataSourceException{
		SQLStatement = SQLStr;
		return setRecordSet();
	}
	
	protected boolean setRecordSet() throws DataSourceException{
		recordSet = dataSource.getResultSet(SQLStatement);
		if (recordSet == null) return false;
		try{
		    isUpdatable = (recordSet.getConcurrency() == ResultSet.CONCUR_UPDATABLE);
		    }
		catch (SQLException e){
		    //e.printStackTrace();
		    throw new DataSourceException("DataRecordSet.setRecordSet: SQLException encountered: " + e.getMessage());
		    }
		return true;
	}
	
	/*******************************************
	 * Attempts to delete the specified records from the underlying table.
	 * 
	 * @param rows
	 * @return
	 */
	public boolean deleteRecords(int[] rows) throws DataSourceException{
		if (!isUpdatable) return false;
		
		//delete last rows first
		for (int i = rows.length - 1; i > -1; i--){
			this.moveTo(rows[i]);
			try{
				recordSet.deleteRow();
			}catch (SQLException ex){
				throw new DataSourceException("DataRecordSet: Error deleting row " + rows[i] + ": " + ex.getMessage());
				}
			}
		
		return true;
	}
	
	public int getRecordCount(){
		return rowCount;
	}
	
	public int getFieldCount(){
		if (fields == null) return 0;
		return fields.size();
	}
	
	public DataField getField(int f){
		if (f >= fields.size() || f < 0) return null;
		return fields.get(f);
	}
	
	//set fields from meta data
	protected boolean setFields(){
		if (recordSet == null) return false;
		//clear existing fields
	    fields.clear();
	    DataField thisField;
	    field_map = new NameMap();
	    field_indices = new NameMap();
	    
	    try{
		     ResultSetMetaData thisMetaData = recordSet.getMetaData();
		     for (int i = 1; i <= thisMetaData.getColumnCount(); i++) {
		         thisField = new DataField();
		         thisField.setName(thisMetaData.getColumnName(i));
		         thisField.setLabel(thisMetaData.getColumnLabel(i));
		         
		         //map non-standard SQL types
		         thisField.setDataType(DataTypes.getMappedSQLTypeDBMStoJDBC(thisMetaData.getColumnType(i),
		        		 												    dataSource.conn.getDriver()));
		         
		         thisField.setLength(thisMetaData.getColumnDisplaySize(i));
		         fields.add(thisField);
		         field_map.add(i - 1, thisField.getName());
		       	 }
		     
		     if (sort_fields){
			     Collections.sort(fields, new Comparator<DataField>(){
				    	 public int compare(DataField f1, DataField f2){
				    		 return f1.getName().compareTo(f2.getName());
				    	 }
			     	});
			     }
		     
		     for (int i = 0; i < fields.size(); i++)
		    	 field_indices.add(i, fields.get(i).getName());
	     
	     }catch (SQLException e){
	    	 e.printStackTrace();
	    	 return false;
	     }
		 return true;
	}
	
	public int getCurrentRecord(){
		try{
			return recordSet.getRow() - 1;
		}catch (SQLException e){
			e.printStackTrace();
			return -1;
			}
	}
	
	/**************************************
	 * Returns the value at the given record and field indices. The field index is interpreted
	 * as the sorted index; therefore it is first mapped to the sorted name and back to the
	 * actual stored index.
	 * 
	 */
	@Override
	public Object getValueAt(int record, int field) throws SQLException{
		if (record > getRecordCount() && !isAddNew) return "";
		
		String name = field_indices.get(field);
		int f = field_map.get(name);
		if (f < 0) return "";
		
		if (isAddNew && record == getRecordCount()){
			InterfaceSession.log("DataRecordSet: Getting field value '" + name + "' for new record.", LoggingType.Debug);
			Object value = getField(f).getValue();
			return value;
		}
		
		int current = getCurrentRecord();
	    if (record == current && (isEdit)){
	    	return getFieldVal(f);
	    	}
	    try{
	    	moveTo(record);
	    	if (recordSet.isAfterLast() || recordSet.isBeforeFirst())
	    		return "";
	    	return getObject(f);
	    }catch (java.sql.SQLException e){
	    	//e.printStackTrace();
	    	return null;
	    	}
	}
	
	/**************************
	 * Sets the value of a specified field for a specified record. The field index is interpreted
	 * as the sorted index; therefore it is first mapped to the sorted name and back to the
	 * actual stored index.
	 * 
	 * @param record record to update
	 * @param field field to update
	 * @param value Object with which to set the field
	 * @return true is update is successful, false otherwise
	 */
	public boolean setValueAt(int record, int field, Object value){
		
		String name = field_map.get(field);
		int f = this.getFieldIndex(name);
		if (f < 0) return false;
		
		DataField data_field = fields.get(f);
		if (!data_field.isEditable()) return false;
		
		//move to record
		this.moveTo(record);
		
		//call edit()
		if (!edit()){
			InterfaceSession.log("Could not edit record " + record + ".", LoggingType.Errors);
			return false;
			}
		
		//update value
		if (!updateField(data_field.getName(), value)){
			InterfaceSession.log("Could not update field " + data_field.getName() + ".", LoggingType.Errors);
			return false;
			}
		
		//call update()
		//return success
		return update();
		
	}
	
	public Object getObject(int fieldIndex) throws SQLException{
	    //try{
	    	//String field = field_map.get(fieldNo);
	    	//int f = this.getFieldIndex(field);
	      	return recordSet.getObject(fieldIndex + 1);
	    //	}
	   // catch (SQLException e){
	    //	//e.printStackTrace();
	    //	}
	    //return null;
	}
	
	/****************************************
	 * Returns the current value of this field
	 * 
	 * @param fieldName
	 * @return
	 * @throws SQLException
	 */
	public Object getFieldVal(String fieldName) throws SQLException{
		if (EOF()) return null;
		int i = getFieldIndex(fieldName);
		if (i < 0) return null;
		return getObject(i);
	}

	public Object getFieldVal(int field_index) throws SQLException{
	    return getObject(field_index);
	}
	
	public ObjectInputStream getBinaryStream(String fieldName){
	    return getBinaryStream(getFieldIndex(fieldName));
	  	}

    /***************
     * Fetches the serialized binary data stream from a specified field
     * in the data set.
     *
     * @todo add appropriate throw clauses
     * @param fieldNo index of field from which to read binary stream
     * @return ObjectInputStream containing serializated object
     *****/
	public ObjectInputStream getBinaryStream(int fieldNo){
	    try{
	    	return new ObjectInputStream(recordSet.getBinaryStream(fieldNo + 1));
	    	}
	    catch (SQLException e){
	      	e.printStackTrace();
	      	return null;
	    	}
	    catch (IOException e){
	    	e.printStackTrace();
	    	return null;
	    	}
	    //debug
	    catch (ClassCastException e){
	    	e.printStackTrace();
	    	return null;
	    	}
	  }
	
	public int getFieldIndex(String name) {
		Integer i = field_map.get(name);
		if (i == null) return -1;
		return i;
//		for (int i = 0; i < fields.size(); i++)
//			if (fields.get(i).getName().compareTo(fieldName) == 0)
//		        return i;
//		return -1;
	}
	
	/********************************************************************
	 * Searches for and returns the field named {@code name}; case insensitive.
	 * 
	 * @param s
	 * @return
	 */
	public DataField getField(String name){
		Integer i = field_indices.get(name);
		if (i == null) return null;
		return fields.get(i);
		
//		name = name.toLowerCase();
//		for (int i = 0; i < fields.size(); i++)
//			if (fields.get(i).getName().toLowerCase().compareTo(name) == 0)
//				return fields.get(i);
//		return null;
	}
	
	/***************************************************
	 * Updates the current value associated with {@code field}.
	 * 
	 * @param field
	 * @param newVal
	 * @return
	 */
	public boolean updateField(String field, Object newVal){
	    if (recordSet == null || !isUpdatable) return false;
	    if (!isEdit && !isAddNew) return false;
	    DataField thisField = getField(field);
	    if (thisField == null) return false;
	    try{
	    	thisField.setValue(newVal);
	    }catch (DataSourceException ex){
	    	return false;
	    	}
	    return true;
	}
	
	//move to before first position
	public boolean resetCursor(){
	    if (recordSet == null) return false;
	    try {
	    	recordSet.beforeFirst();
	    	isAddNew = false;
	    	return true;
	    	}
	    catch (SQLException e) {
	    	e.printStackTrace();
	    	return false;
	    	}
	}
	
	public boolean moveFirst(){
		if (recordSet == null) return false;
	    try {
	    	if (recordSet.first()){
	    		updateFields();
	    		rowCount = Math.max(1, rowCount);
	    		isAddNew = false;
	    		return true;
	    		
	    		}
	    	return false;
	    	}
	    catch (SQLException e) {
	    	e.printStackTrace();
	      	return false;
	    	}
	}
	
	public boolean moveNext(){
		if (getCurrentRecord() + 1 > rowCount)
			rowCount = getCurrentRecord() + 1;
		isAddNew = false;
		return moveRelative(1);
	}
	
	public boolean moveLast(){
		 if (recordSet == null) return false;
		    try {
		    	if (recordSet.last()){
		    		updateFields();
		    		rowCount = recordSet.getRow();
		    		isAddNew = false;
		    		return true;
		    		}
		    	return false;
		    	}
		    catch (SQLException e) {
		    	e.printStackTrace();
		      	return false;
		    	}
	}
	
	public boolean isClosed(){
		try{
			return (recordSet == null ||
					recordSet.isClosed());
		}catch (UnsupportedOperationException e){
			//operation not supported; test by attempting an operation
			try{
				recordSet.moveToCurrentRow();
				return false;
			}catch (Exception e2){
				return true;
				}
		}catch (AbstractMethodError err){
			//will happen if a driver is not compliant with the latest JDBC interface (e.g., tinySQL)
			//use same test as above
			try{
				recordSet.moveToCurrentRow();
				return false;
			}catch (Exception e2){
				return true;
				}
		}catch (Exception e){
			e.printStackTrace();
			return true;
			}
	}
	
	//move to absolute row
	public boolean moveTo(int row) {
	    if (recordSet == null) return false;
	    try {
	    	if (recordSet.absolute(row + 1)){
	    		updateFields();
	    		rowCount = Math.max(row, rowCount);
	    		return true;
	    		}
	    	return false;
	    	}
	    catch (SQLException e) {
	    	e.printStackTrace();
	      	return false;
	    	}
	}

	//move to relative row
	public boolean moveRelative(int offset) {
	    if (recordSet == null)return false;
	    try {
	    	if (recordSet.relative(offset)){
	    	  	updateFields();
	    	  	return true;
	      		}
	      	return false;
	    	}
	    catch (SQLException e) {
	    	e.printStackTrace();
	    	return false;
	    	}
	  }
	
	 public boolean EOF(){
	    try {
	    	return recordSet.isAfterLast();
	    	}
	    catch (SQLException e){
	    	e.printStackTrace();
	    	return true;
	    	}
	 }

	 public boolean BOF(){
	    try {
	    	return recordSet.isBeforeFirst();
	    	}
	    catch (SQLException e){
	    	e.printStackTrace();
	    	return true;
	    	}
	 }
	 
	 public Class getFieldClass(String field) {
		 return getFieldClass(getFieldIndex(field));
	 }
	 
	 public Class getFieldClass(int field) {
		    return fields.get(field).getObjectClass();
		  }
	 
	 public String getFieldName(int field){
		 return fields.get(field).getName();
	 }
	 
	 public void close(){
	    try{
	    	recordSet.close();
	    	}
	    catch (SQLException e){
	    	e.printStackTrace();
	    	}
	 }
	 
	 //data editing
	 
	 /*************************
	  * Initiate the addition of a new record to this record set.
	  * 
	  * @return
	  */
	 public boolean addNew(){
		 if (recordSet == null || !isUpdatable) return false;
		    try{
		    	recordSet.moveToInsertRow();
		      	isAddNew = true;
		      	
		      	//clear fields for new values
		      	clearFields();
		    	}
		    catch (SQLException e){
		    	//e.printStackTrace();
		    	InterfaceSession.log("DataRecordSet: Error adding new record: " + e.getMessage());
		    	isAddNew = false;
		    	return false;
		    	}
		 return true;
	 }
	 
	 /*************************
	  * Initiate editing of current record.
	  * @return true if successful, false otherwise
	  */
	 public boolean edit(){
		 if (recordSet == null || !isUpdatable) return false;
		 try{
	    	if (recordSet.isBeforeFirst() || recordSet.isAfterLast()) return false;
	    	isEdit = true;
	    	
	      	//set fields to present values
	    	if (!updateFields()) return false;
	    	}
		 catch (SQLException e){
			InterfaceSession.log("DataRecordSet: Error editing record [" + getCurrentRecord() + "]: " + e.getMessage());
	        //e.printStackTrace();
	        return false;
	      	}
		 return true;
	 }
	 
	 /*****************************
	  * Cancels an update initiated by an AddNew or Edit process.
	  * 
	  */
	 public boolean cancelUpdate(){
		 
		 try{
			 if (isAddNew)
				 recordSet.moveToCurrentRow();
			 this.isAddNew = false;
			 this.isEdit = false;
		 }catch (SQLException e){
			 InterfaceSession.log("DataRecordSet: Could not cancel update. Reason:" + e.getMessage(), LoggingType.Errors);
			 return false;
		 	}
		 
		 return true;
		 
	 }
	 
	 /*************************
	  * Apply the changes from an AddNew or Edit process. Ensure that the record is valid;
	  * i.e., required values are set, unique fields are not violated, etc. 
	  * Update the record set with these changes and return true if successful, false otherwise.
	  * 
	  * @return
	  */
	 public boolean update(){
		 if (recordSet == null || !isUpdatable) 
			 return false;
	 	 if (!isAddNew && !isEdit) 
	 		 return false;
		 try {
	    	  /**@todo: implement data check as below */
	    				 
			  //update current record from fields
			  if (!updateRecord()) 
				  return false;
			
		      if (isAddNew){
		    	  recordSet.insertRow();
		      }else{
		    	  recordSet.updateRow();
		      	  }
		      isAddNew = false;
		      isEdit = false;
		      
		      return true;
		      }
		  catch (SQLException e) {
			  
			  // For the moment this is desired behaviour: this method can be used to test whether a record can be validly updated
			  String record = "" + getCurrentRecord();
			  if (isAddNew) record = " New ";
		      InterfaceSession.log("DataRecordSet: Error updating record [" + record + "]\n" +
		    		  			   "From SQL: " + this.SQLStatement + "\nMessage: " + e.getMessage(),
		    		  			   LoggingType.Warnings);
		  	  }
		  return false;
	 }
	 
	 //	set all fields to current record
	 private boolean updateFields(){
		 if (recordSet == null || !isUpdatable) return false;
		 if (EOF() || BOF()) return false;
		 
		 try{	 
			 //set field values from record set
			 //if java object, set as binary stream
			 for (int i = 0; i < fields.size(); i++ )
	        	 if (fields.get(i).getDataType() != java.sql.Types.JAVA_OBJECT)
	        		 fields.get(i).setValue(recordSet.getObject(fields.get(i).getName()));
	        	 else
	        		 fields.get(i).setValue(recordSet.getBinaryStream(fields.get(i).getName()));
			return true;
		 	}
	    catch (SQLException e){
	    	 InterfaceSession.log("DataRecordSet: Error updating fields [" + getCurrentRecord() + "]: " + e.getMessage());
	    	}
	    catch (ClassCastException e){
	    	InterfaceSession.log("DataRecordSet: Error updating fields [" + getCurrentRecord() + "]: " + e.getMessage());
	    	}
	    catch (DataSourceException e){
	    	InterfaceSession.log("DataRecordSet: Error updating fields [" + getCurrentRecord() + "]: " + e.getMessage());
	    	}
	    
	    return false;
	 }
	 
	 //update the current record with field values
	 private boolean updateRecord(){
		 if (recordSet == null || !isUpdatable) 
			 return false;
		 
		 //for each field, set value in recordSet
		 for (int i = 0; i < fields.size(); i++)
			if (!fields.get(i).updateRecordSet(recordSet)) 
				return false; 
		 	
		 return true;
	 }
	 
	  //set all fields to null
	  private void clearFields(){
		  for (int i = 0; i < fields.size(); i++){
			  try{
			  fields.get(i).setValue((String)null);
			  }catch (Exception ex){}
		  	  }
	  }
	 
	
}