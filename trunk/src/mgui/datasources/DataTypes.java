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

import java.awt.image.DataBuffer;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiLong;


/****************************
 * Class to provide functions for testing and comparing data types.
 * @author Andrew Reid
 *
 */


public class DataTypes {

	public static final int INTEGER = 0;
	public static final int DOUBLE = 1;
	public static final int SINGLE = 2;
	public static final int CHAR = 3;
	public static final int STRING = 4;
	public static final int LONGSTRING = -1;
	public static final int BINARY = 5;
	public static final int BOOLEAN = 6;
	public static final int SHORT = 7;
	public static final int FLOAT = 8;
	public static final int LONG = 9;
	public static final int RGB = 10;
	public static final int RGBA = 11;
	public static final int BYTE = 12;
	public static final int USHORT = 13;
	

	public static final int CLOB = 14;
	public static final int BLOB = 15;
	public static final int JAVA_OBJECT = 16;
	public static final int TIMESTAMP = 17;
	public static final int DATE = 18;
	
	public static final int DECIMAL = 19;
	
	public static ArrayList<DataType> dataTypes; 
	
	public static void setSQLTypes(HashMap<Integer, String> map){
		if (!typesSet()) setDataTypes();
		for (int i = 0; i < dataTypes.size(); i++)
			map.put(dataTypes.get(i).sqlVal, dataTypes.get(i).sqlStr);
	}
	
	public static DataType getType(int val){
		if (dataTypes == null) setDataTypes();
		for (int i = 0; i < dataTypes.size(); i++)
			if (dataTypes.get(i).val == val) return dataTypes.get(i);
		return null;
	}
	
	public static DataType getType(String type){
		if (dataTypes == null) setDataTypes();
		for (int i = 0; i < dataTypes.size(); i++)
			if (dataTypes.get(i).name.equals(type)) return dataTypes.get(i);
		return null;
	}
	
	public static DataType getTypeForSQLStr(String sqlType){
		for (int i = 0; i < dataTypes.size(); i++)
			if (dataTypes.get(i).sqlStr.equals(sqlType))
				return dataTypes.get(i);
		return null;
	}
	
	/*********************************************
	 * Returns a <code>DataType</code> for the given SQL type (from {@link java.sql.Types}).
	 * 
	 * @param SQLType
	 * @return
	 */
	public static DataType getTypeForSQL(int SQLType){
		switch (SQLType){
	      case java.sql.Types.BOOLEAN:
	        return getType(BOOLEAN);
	      case java.sql.Types.BIT:
	    	  return getType(BOOLEAN);
	      case java.sql.Types.LONGVARCHAR:
	    	  return getType(STRING);
	      case java.sql.Types.CLOB:
	    	  return getType(CLOB);
	      case java.sql.Types.VARCHAR:
	    	  return getType(STRING);
	      case java.sql.Types.CHAR:
	    	  return getType(CHAR);
		  case java.sql.Types.DOUBLE:
			  return getType(DOUBLE);
		  case java.sql.Types.DECIMAL:
			  return getType(DECIMAL);
	      case java.sql.Types.INTEGER:
	    	  return getType(INTEGER);
	      case java.sql.Types.FLOAT:
	    	  return getType(FLOAT);
	      case java.sql.Types.BLOB:
	    	  return getType(BLOB);
	      case java.sql.Types.LONGVARBINARY:
	    	  return getType(BLOB);
	      case java.sql.Types.JAVA_OBJECT:
	    	  return getType(JAVA_OBJECT);
	      case java.sql.Types.TIMESTAMP:
	    	  return getType(TIMESTAMP);
	      case java.sql.Types.DATE:
	    	  return getType(DATE);
	      case java.sql.Types.SMALLINT:
	    	  return getType(SHORT);
	      case java.sql.Types.BIGINT:
	    	  return getType(LONG);
	      case java.sql.Types.TINYINT:
	    	  return getType(SHORT);
	    }
		return null;
	}
	
	/********************************************
	 * Returns the number of bytes required to store a value of DataType {@code type}.
	 * 
	 * @param type
	 * @return
	 */
	public static int getSize(DataType type){
		return getSizeForType(type.val);
	}
	
	/********************************************
	 * Returns the number of bytes required to store a value of DataType value {@code type}.
	 * 
	 * @param type
	 * @return
	 */
	public static int getSizeForType(int type){
		switch (type){
		 	case BINARY:
		        return 1;
			  case BOOLEAN:
			    return 1;
			  case CHAR:
			    return 1;
			  case INTEGER:
			    return 4;
			  case SHORT:
				return 2;
			  case DOUBLE:
			    return 8;
			  case FLOAT:
				return 4;
			  case LONG:
				return 8; 
			}
		return -1;
	}
	
	/***************************************
	 * Returns the number of bytes required to store a value of the SQL type {@code type}.
	 * 
	 * @param type
	 * @return
	 */
	public static int getSizeForSQLType(int type){
		switch (type){
		  case Types.BINARY:
	        return 1;
		  case Types.BOOLEAN:
	        return 1;
	      case Types.CHAR:
		    return 1;
	      case Types.INTEGER:
	        return 4;
	      case Types.SMALLINT:
	    	return 2;
	      case Types.DOUBLE:
	        return 8;
	      case Types.FLOAT:
	    	return 4;
	      case Types.BIGINT:
	    	return 8;
			}
		return -1;
	}
	
	public static DataType getTypeForSize(int size){
		for (int i = 0; i < dataTypes.size(); i++)
			if (getSize(dataTypes.get(i)) == size)
				return dataTypes.get(i);
		return null;
	}
	
	public static Object getSQLTypeObject(int dataType){
	    switch (dataType){
	      case Types.BOOLEAN:
	        return new MguiBoolean(false);
	      case Types.VARCHAR:
	        return new String();
	      case Types.CHAR:
		    return new String();
	      case Types.INTEGER:
	        return new MguiInteger(0);
	      case Types.DOUBLE:
	        return new MguiDouble(0.0);
	      case Types.FLOAT:
	    	return new MguiFloat(0.0f);
	    }
	    return new Object();
	  }
	
	private static boolean typesSet(){
		return (dataTypes != null);
	}
	
	/******************
	 * Sets an internal sorted list of data types for use by this class's functions
	 */
	private static void setDataTypes(){
		dataTypes = new ArrayList<DataType>();
		dataTypes.add(new DataType("Boolean", BOOLEAN, "BOOLEAN", java.sql.Types.BOOLEAN));
		dataTypes.add(new DataType("Binary", BINARY, "BINARY", java.sql.Types.BINARY));
		dataTypes.add(new DataType("Byte", BYTE, "BINARY", java.sql.Types.BINARY));
		dataTypes.add(new DataType("String", STRING, "VARCHAR", java.sql.Types.VARCHAR));
		dataTypes.add(new DataType("Long String", LONGSTRING, "LONGVARCHAR", java.sql.Types.LONGVARCHAR));
		dataTypes.add(new DataType("Integer", INTEGER, "INTEGER", java.sql.Types.INTEGER));
		dataTypes.add(new DataType("Long Integer", LONG, "BIGINT", java.sql.Types.BIGINT));
		dataTypes.add(new DataType("Short", SHORT, "TINYINT", java.sql.Types.TINYINT));
		dataTypes.add(new DataType("Char", CHAR, "CHAR", java.sql.Types.CHAR));
		dataTypes.add(new DataType("Binary Large Object", BLOB, "BLOB", java.sql.Types.BLOB));
		dataTypes.add(new DataType("Timestamp", TIMESTAMP, "TIMESTAMP", java.sql.Types.TIMESTAMP));
		dataTypes.add(new DataType("Unsigned Small Integer", USHORT, "SMALLINT", java.sql.Types.SMALLINT));
		dataTypes.add(new DataType("Small Integer", SHORT, "SMALLINT", java.sql.Types.SMALLINT));
		dataTypes.add(new DataType("Double", DOUBLE, "DOUBLE", java.sql.Types.DOUBLE));
		dataTypes.add(new DataType("Float", FLOAT, "FLOAT", java.sql.Types.FLOAT));
		dataTypes.add(new DataType("Decimal", DECIMAL, "DECIMAL", java.sql.Types.DECIMAL));
		dataTypes.add(new DataType("Date", DATE, "DATE", java.sql.Types.DATE));
		Collections.sort(dataTypes, new Comparator<DataType>(){
			public int compare(DataType o1, DataType o2){
				return o1.name.compareTo(o2.name);
			}});
	}

	  //return data type mapped to particular driver (for non-standard types)
	  //from DBMS type to JDBC type
	/********************************************************
	 * Return data type mapped to particular driver (for non-standard types) from DBMS type to JDBC type
	 * 
	 * @param dataType DBMS type
	 * @param driverStr The driver URL as a <code>String</code>
	 * @return <code>dataType</code> mapped to the JDBC type
	 */
	 public static int getMappedSQLTypeDBMStoJDBC(int dataType, String driverStr){
	    if (driverStr.compareTo("com.borland.datastore.jdbc.DataStoreDriver") == 0){
	      switch (dataType) {
	        case 7:
	          return Types.FLOAT;
	      }
	      return dataType;
	    }
	    if (driverStr.compareTo("com.mysql.jdbc.Driver") == 0){
	    	switch (dataType) {
	        case 7:
	          return Types.FLOAT;
	      }
	    }
	    return dataType;
	  }

	  //return data type mapped to particular driver (for non-standard types)
	  //from DBMS type to JDBC type
	  public static int getMappedSQLTypeJDBCtoDBMS(int dataType, String driverStr){
	    if (driverStr.compareTo("com.borland.datastore.jdbc.DataStoreDriver") == 0){
	      switch (dataType) {
	      }
	      return dataType;
	    }
	   
	    return dataType;
	  }
	
	 //return string label of data type
	 public static String getSQLTypeLabel(int SQLType){
	    switch (SQLType){
	      case java.sql.Types.BOOLEAN:
	        return "Boolean";
	      case java.sql.Types.VARCHAR:
	        return "String";
	      case java.sql.Types.CHAR:
		    return "Char"; 
	      case java.sql.Types.DOUBLE:
	        return "Double";
	      case java.sql.Types.INTEGER:
	        return "Integer";
	      case java.sql.Types.BLOB:
	        return "Blob";
	      case java.sql.Types.JAVA_OBJECT:
	        return "Object";
	      case java.sql.Types.TIMESTAMP:
	    	return "Time Stamp";
	      case java.sql.Types.DATE:
		    	return "Date";
	      case java.sql.Types.DECIMAL:
		    	return "Decimal";
	      case java.sql.Types.SMALLINT:
		    return "Small Integer";
	      case java.sql.Types.TINYINT:
			    return "Tiny Integer";
	    }
	    return "None";
	  }

    //return SQL string equivalent of data type
    public static String getSQLTypeStr(int SQLType){
	    return getSQLTypeStr(SQLType, 0);
	  }

    //return SQL string equivalent of data type
    public static String getSQLTypeStr(int SQLType, int length){
	    switch (SQLType){
	      case java.sql.Types.BOOLEAN:
	        return "BOOLEAN";
	      case java.sql.Types.BIT:
	        return "BOOLEAN";
	      case java.sql.Types.LONGVARCHAR:
	    	return "LONGVARCHAR(" + length + ")";
	      case java.sql.Types.CLOB:
	    	return "CLOB";
	      case java.sql.Types.VARCHAR:
	        return "VARCHAR(" + length + ")";
	      case java.sql.Types.CHAR:
		    return "CHAR(" + length + ")" ;
		  case java.sql.Types.DOUBLE:
	        return "DOUBLE";
		  case java.sql.Types.DECIMAL:
		        return "DECIMAL";
	      case java.sql.Types.INTEGER:
	        return "INTEGER";
	      case java.sql.Types.FLOAT:
	        return "FLOAT";
	      case java.sql.Types.BLOB:
	        return "BLOB";
	      case java.sql.Types.LONGVARBINARY:
	    	return "LONGVARBINARY";
	      case java.sql.Types.JAVA_OBJECT:
	        return "OBJECT";
	      case java.sql.Types.DATE:
		    	return "DATE";
	      case java.sql.Types.TIMESTAMP:
	    	return "TIMESTAMP";
	      case java.sql.Types.SMALLINT:
	    	return "SMALLINT";
	      case java.sql.Types.BIGINT:
		    return "BIGINT";
	      case java.sql.Types.TINYINT:
	    	return "TINYINT";
	      case java.sql.Types.BINARY:
	    	return "BINARY";
	    }
	    return "UNKNOWN";
	  }

    /****************************************************
     * Returns the equivalent integer value (from {@link java.sql.Types} for the given type string.
     * 
     * @param typeStr
     * @return
     */
    public static int getSQLStrValue(String typeStr){
	    typeStr = typeStr.toLowerCase();
	    /** @todo change this back to BOOLEAN */
	    if (typeStr.compareTo("boolean") == 0)
	      return java.sql.Types.BIT;
	    //string length?
	    if (typeStr.compareTo("string") == 0)
	      return java.sql.Types.VARCHAR;
	    if (typeStr.compareTo("char") == 0)
		  return java.sql.Types.CHAR;
	    if (typeStr.compareTo("double") == 0)
	      return java.sql.Types.DOUBLE;
	    if (typeStr.compareTo("float") == 0)
		  return java.sql.Types.FLOAT;
	    if (typeStr.compareTo("integer") == 0)
	      return java.sql.Types.INTEGER;
	    /** @todo change this after working around driver issue */
	    if (typeStr.compareTo("object") == 0)
	      return java.sql.Types.JAVA_OBJECT;
	    if (typeStr.equals("date"))
	    	return java.sql.Types.DATE;
	    if (typeStr.equals("timestamp"))
	    	return java.sql.Types.TIMESTAMP;
	    if (typeStr.equals("decimal"))
	    	return java.sql.Types.DECIMAL;
	    if (typeStr.equals("short"))
	    	return java.sql.Types.TINYINT;
	    if (typeStr.equals("tinyint"))
	    	return java.sql.Types.TINYINT;
	    return -1;
	  }
    
    //returns true if the specified datatype is comparable
    public static boolean isComparable(int dataType){
    	switch (dataType){
	      case Types.BOOLEAN:
	        return false;
	      case Types.VARCHAR:
	        return true;
	      case Types.CHAR:
	    	return true;
	      case Types.INTEGER:
	        return true;
	      case Types.DOUBLE:
	        return true;
	      case Types.FLOAT:
	    	return true;
	    }
    	return false;
    }

    //test whether obj is of type type
    public static boolean isType(Object obj, int type){
    	return true;
    }
    
    /**************************************
     * Converts a value from a {@code String} to an instance of the specified type.
     * 
     * @param value
     * @param data_type
     * @throws DataSourceException - If {@code value} cannot be converted to the specified type
     * @return
     */
    public static Object getInstanceForValue(Object value, int data_type) throws DataSourceException {
    	
    	try{
	    	switch (data_type){
		    	case Types.BOOLEAN:
			        return new MguiBoolean(value.toString());
			      case Types.VARCHAR:
			      case Types.CHAR:
			        return value;
			      case Types.INTEGER:
			      case Types.TINYINT:
			      case Types.SMALLINT:
			        return new MguiInteger(value.toString());
			      case Types.BIGINT:
				        return new MguiLong(value.toString());
			      case Types.DOUBLE:
			      case Types.DECIMAL:
			        return new MguiDouble(value.toString());
			      case Types.FLOAT:
			    	  return new MguiFloat(value.toString());
			      case Types.DATE:
			    	  return value;
			      case Types.TIMESTAMP:
			    	  return value;
			    	  
			    	  
		    	}
    	
    	}catch (Exception e){
    		throw new DataSourceException("Cannot convert '" + value + "' to type " + getSQLTypeStr(data_type));
    		}
    	
    	return value;
    }
    
    /***********************************************
     * Returns a best guess for the data type of the given string.
     * 
     * @param token
     * @return
     */
    public static int guessDataType(String token){
    	
    	//Integer?
    	try{
    		Integer.parseInt(token);
    		return Types.INTEGER;
    	}catch (NumberFormatException e){};
    	
    	//Float?
    	try{
    		Float.parseFloat(token);
    		return Types.FLOAT;
    	}catch (NumberFormatException e){};
    	
    	//Double?
    	try{
    		Double.parseDouble(token);
    		return Types.DOUBLE;
    	}catch (NumberFormatException e){};
    	
    	//Date? Timestamp?
    	
    	return Types.VARCHAR;
    	
    }
    
    /**********************************************
     * Return the class of given data type
     * 
     * @param dataType
     * @return
     */
    public static Class getSQLTypeClass(int dataType){
    	switch (dataType){
	      case Types.BOOLEAN:
	        return MguiBoolean.class;
	      case Types.VARCHAR:
	        return String.class;
	      case Types.CHAR:
	    	return String.class;
	      case Types.INTEGER:
	        return MguiInteger.class;
	      case Types.DOUBLE:
	        return MguiDouble.class;
	      case Types.FLOAT:
	    	  return MguiFloat.class;
	    }
    	return Object.class;
    }
    
    public static int getDataBufferType(DataType type){
    	
    	switch (type.val){
	    	case DataTypes.BYTE:
	    		return DataBuffer.TYPE_BYTE;
	    	case DataTypes.SHORT:
	    		return DataBuffer.TYPE_SHORT;
	    	case DataTypes.USHORT:
	    		return DataBuffer.TYPE_USHORT;
	    	case DataTypes.INTEGER:
	    		return DataBuffer.TYPE_INT;
	    	case DataTypes.FLOAT:
	    		return DataBuffer.TYPE_FLOAT;
	    	case DataTypes.DOUBLE:
	    		return DataBuffer.TYPE_DOUBLE;
    		}
    	return -1;
    	
    }
    
    public static DataType getFromDataBufferType(int db_type){
    	
    	switch (db_type){
	    	case DataBuffer.TYPE_BYTE:
	    		return getType(DataTypes.BYTE);
	    	case DataBuffer.TYPE_SHORT:
	    		return getType(DataTypes.SHORT);
	    	case DataBuffer.TYPE_USHORT:
	    		return getType(DataTypes.USHORT);
	    	case DataBuffer.TYPE_INT:
	    		return getType(DataTypes.INTEGER);
	    	case DataBuffer.TYPE_FLOAT:
	    		return getType(DataTypes.FLOAT);
	    	case DataBuffer.TYPE_DOUBLE:
	    		return getType(DataTypes.DOUBLE);
    		}
    	return null;
    	
    }
    
    public static int getDataBufferType(String type){
    	
    	if (type.equals("TYPE_BYTE"))
	    		return DataBuffer.TYPE_BYTE;
    	if (type.equals("TYPE_SHORT"))
	    		return DataBuffer.TYPE_SHORT;
    	if (type.equals("TYPE_USHORT"))
	    		return DataBuffer.TYPE_USHORT;
    	if (type.equals("TYPE_INT"))
	    		return DataBuffer.TYPE_INT;
    	if (type.equals("TYPE_FLOAT"))
	    		return DataBuffer.TYPE_FLOAT;
    	if (type.equals("TYPE_DOUBLE"))
	    		return DataBuffer.TYPE_DOUBLE;
    	return -1;
    	
    }
    
    public static String getDataBufferTypeStr(int type){
    	
    	switch (type){
    		case DataBuffer.TYPE_BYTE:
    			return "TYPE_BYTE";
	    	case DataBuffer.TYPE_SHORT:
	    		return "TYPE_SHORT";
	    	case DataBuffer.TYPE_USHORT:
	    		return "TYPE_USHORT";
	    	case DataBuffer.TYPE_INT:
	    		return "TYPE_INT";
	    	case DataBuffer.TYPE_FLOAT:
	    		return "TYPE_FLOAT";
	    	case DataBuffer.TYPE_DOUBLE:
	    		return "TYPE_DOUBLE";
    		}
    	return "Invalid Data Type";
    }
    
    /**********************************
     * Determines whether a given SQL type is numeric
     * 
     * @param sqlType
     * @return
     */
    public static boolean isNumeric(int sqlType){
    	switch (sqlType){
	    	case Types.INTEGER:
		    case Types.DOUBLE:
		    case Types.FLOAT:
		    	return true;
	    	}
    	return false;
    }
    
    /**********************************
     * Determines whether a given SQL type is text
     * 
     * @param sqlType
     * @return
     */
    public static boolean isText(int sqlType){
    	switch (sqlType){
    		case Types.LONGVARCHAR:
    		case Types.VARCHAR:
    		case Types.CHAR:
    			return true;
    	}
    	return false;
    }
    
    /**********************************
     * Determines whether a given data type is text
     * 
     * @param sqlType
     * @return
     */
    public static boolean isText(DataType dataType){
    	return isText(dataType.sqlVal);
    }
    
    /**************************
     * Returns a sorted list of strings representing data types as specified in the setDataTypes method
     * @return sorted ArrayList of strings
     */
    public static ArrayList<DataType> getDataTypeList(){
    	if (!typesSet()) setDataTypes();
    	return dataTypes;
    }
}