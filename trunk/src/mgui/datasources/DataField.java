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
import java.io.InputStream;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.datasources.DataFieldTreeNode;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.TypeMap;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.NumberFunctions;

import org.xml.sax.Attributes;


/************************
 * Object to specify a field data type and length.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class DataField extends AbstractInterfaceObject implements Cloneable,
																  AttributeListener,
																  XMLObject,
																  Comparable<DataField>{

	//public static ValueMap typelist;
	public static HashMap<Integer, String> typelist;
	
	protected AttributeList attributes;
	protected Object value;       			//value for updating dataset records
	public boolean isModified; 				//has this field been modified?
	public DataFieldTreeNode treeNode;
	
	Object default_value;
	
	public DataField(){ 
		init();
	}
	
	public DataField(String thisName, int thisType){
		this(thisName, thisType, 255);
	}
	
	public DataField(String thisName, int thisType, int length){
		init();
	    setName(thisName);
	    setDataType(thisType);
	    setLength(length);
	    setLabel(thisName);
	    value = DataTypes.getSQLTypeObject(getDataType());
	    attributes.setValue("IsComparable", DataTypes.isComparable(getDataType()));
	
	}
	
	private void init(){
		
		attributes = new AttributeList();
		attributes.add(new Attribute<TypeMap>("DataType", new TypeMap()));
		attributes.add(new Attribute<String>("Name", "no-name"));
		attributes.add(new Attribute<MguiInteger>("Length", new MguiInteger(255)));
		attributes.add(new Attribute<String>("Label", "No name"));
		Attribute<MguiBoolean> a = new Attribute<MguiBoolean>("IsComparable", new MguiBoolean(true));
		a.setEditable(false);
		attributes.add(a);
		attributes.add(new Attribute<MguiBoolean>("IsKeyField", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiBoolean>("IsUnique", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiBoolean>("IsEditable", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("IsRequired", new MguiBoolean(false)));
		
		//set up static data type list for datatype
		//should only have to do this on the first instance of DataField
		if (typelist == null){
			typelist = new HashMap<Integer, String>();
			DataTypes.setSQLTypes(typelist);
			}
		TypeMap datatype = new TypeMap();
		datatype.setTypes(typelist);
		setDataTypeMap(datatype);
		
		attributes.addAttributeListener(this);
		
	}
	
	public Object getDefaultValue(){
		return default_value;
	}
	
	public void setDefaultValue(Object value){
		default_value = value;
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public void setLength(int length){
		attributes.setValue("Length", new MguiInteger(length));
	}
	
	public int getLength(){
		return ((MguiInteger)attributes.getValue("Length")).getInt();
	}
	
	public void setDataTypeMap(TypeMap map){
		attributes.setValue("DataType", map);
	}
	
	public TypeMap getDataTypeMap(){
		return (TypeMap)attributes.getValue("DataType");
	}
	
	public void setDataType(int datatype){
		TypeMap map = (TypeMap)attributes.getValue("DataType");
		map.setType(DataTypes.getSQLTypeStr(datatype, getLength()));
		if (map.getType() == 0)
			map = map;
		
		if (isNumeric()){
	    	this.default_value = 0;
	    }else if (isText()){
	    	this.default_value = "?";
	    }else if (isDate()){
	    	Calendar cal = Calendar.getInstance();
	    	this.default_value = cal.getTime();
	    }else if (isTimestamp()){
	    	Calendar cal = Calendar.getInstance();
	    	this.default_value = cal.getTime();
	    	}
		
	}
	
	/*******************************
	 * Returns the data type for this field; see {@link java.sql.Types}. 
	 * 
	 * @return
	 */
	public int getDataType(){
		TypeMap map = (TypeMap)attributes.getValue("DataType");
		return ((TypeMap)attributes.getValue("DataType")).getType();
	}
	
	public String getDataTypeStr(){
		DataType type = DataTypes.getTypeForSQL(getDataType());
		if (type == null){
			type = DataTypes.getTypeForSQL(getDataType());
			return "?";
			}
		return type.name;
	}
	
	public String getLabel(){
		return (String)attributes.getValue("Label");
	}
	
	public void setLabel(String label){
		attributes.setValue("Label", label);
	}
	
	public void setIsUnique(boolean b){
		if (!b && isKeyField()) return;
		attributes.setValue("IsUnique", new MguiBoolean(b));
	}
	
	public boolean isUnique(){
		return ((MguiBoolean)attributes.getValue("IsUnique")).getTrue();
	}
	
	public void setIsEditable(boolean b){
		attributes.setValue("IsEditable", new MguiBoolean(b));
	}
	
	public boolean isEditable(){
		return ((MguiBoolean)attributes.getValue("IsEditable")).getTrue();
	}
	
	public boolean isComparable(){
		return ((MguiBoolean)attributes.getValue("IsComparable")).getTrue();
	}
	
	public void setIsKeyField(boolean b){
		attributes.setValue("IsKeyField", new MguiBoolean(b));
		
	}
	
	public boolean isKeyField(){
		return ((MguiBoolean)attributes.getValue("IsKeyField")).getTrue();
	}
	
	public void setIsRequired(boolean b){
		if (!b && isKeyField()) return;
		attributes.setValue("IsRequired", new MguiBoolean(b));
	}
	
	public boolean isRequired(){
		return ((MguiBoolean)attributes.getValue("IsRequired")).getTrue();
	}
	
	@Override
	public Object clone(){
		DataField field = new DataField(getName(), getDataType());
		
		field.setLabel(getLabel());
		field.setLength(getLength());
		field.setIsKeyField(isKeyField());
		field.setIsRequired(isRequired());
		field.setIsUnique(isUnique());
		
		return field;
	}
	
	public void attributeUpdated(AttributeEvent e){
		
		Attribute<?> attr = e.getAttribute();
		
		if (attr.getName().equals("DataType")){
			Attribute<MguiBoolean> a = (Attribute<MguiBoolean>)attributes.getAttribute("IsComparable");
			a.setValue(new MguiBoolean(DataTypes.isComparable(getDataType())), false);
			
			}
		
		if (attr.getName().equals("IsKeyField")){
			if (isKeyField()){
				Attribute<?> attribute = attributes.getAttribute("IsUnique");
				attribute.setValue(new MguiBoolean(true), false);
				attribute.setEditable(false);
				
				
				attribute = attributes.getAttribute("IsRequired");
				attribute.setValue(new MguiBoolean(true), false);
				attribute.setEditable(false);
				
				
				
			}else{
				Attribute<?> attribute = attributes.getAttribute("IsUnique");
				attribute.setEditable(true);
				attribute = attributes.getAttribute("IsRequired");
				attribute.setEditable(true);
				}
			}
		
	}	
	
	public String getTypeStr(){
		return mgui.datasources.DataTypes.getSQLTypeStr(getDataType(), getLength());
	}
	
	public void setValue (Object value) throws DataSourceException{
		if (value == null){
			this.value = value;
			return;
			}
		this.value = DataTypes.getInstanceForValue(value, getDataType());
	}

	public Object getValue (){
	    return value;
	}
	
	public boolean isNumeric(){
		return DataTypes.isNumeric(getDataType());
	}
	
	public boolean isText(){
		return DataTypes.isText(getDataType());
	}
	
	public boolean isBoolean(){
		return (getDataType() == DataTypes.BOOLEAN);
	}
	
	public boolean isDate(){
		return (getDataType() == DataTypes.DATE);
	}
	
	public boolean isTimestamp(){
		return (getDataType() == DataTypes.TIMESTAMP);
	}
	
	public double getNumericValue(){
		if (!isNumeric()) return Double.NaN;
		return NumberFunctions.getValueForObject(getValue());
	}
	
	public String getTextValue(){
		return getTextValue("#0.00000");
	}
	
	public String getTextValue(String format){
		if (isNumeric()) return MguiDouble.getString(getNumericValue(), format);
		return getValue().toString();
	}
	
	/************************************************************
	 * Returns an SQL string representing this field
	 * 
	 * @return
	 */
	public String getSQLString(){
	    return getSQLString(null);
	}
	
	/************************************************************
	 * Returns an SQL string representing this field, formatted to suit a specific driver (since the JDBC
	 * standard is not always used in a standard way....)
	 * 
	 * @return
	 */
	public String getSQLString(DataSourceDriver driver){
		String q = "`";
		if (driver != null)
			q = driver.getSQLQuote();
		String type = DataTypes.getSQLTypeStr(getDataType(), getLength());
		if (driver != null)
			type = driver.getSQLType(getDataType(), getLength());
		String SQLStr = q + getName() + q + " " + type;
//		 if (isKeyField()){
//			  SQLStr = SQLStr + " PRIMARY KEY";
//		  }else{
			  if (isUnique()){
				  SQLStr = SQLStr + " UNIQUE";
				  }
			  if (isRequired()){
				  SQLStr = SQLStr + " NOT NULL";
				  }
//		  	  }
		return SQLStr;
	}
	
	public Class getObjectClass(){
		return DataTypes.getSQLTypeClass(getDataType());
	}
	
	
	
	public void setTreeNode(){
		if (treeNode == null) treeNode = new DataFieldTreeNode(getName());
		//add settings
		treeNode.setValues(this);
	}
	
	@Override
	public DataFieldTreeNode issueTreeNode(){
		if (treeNode == null) setTreeNode();
		return treeNode;
	}
	
		/**************************************************
		* Update a result set with this field, mapped to its data type
	   /*
	    * @todo: include more data types here
	    *
	    * @param recordSet ResultSet to update
	    * @return true is update is successful, otherwise false
	    */
	   public boolean updateRecordSet(ResultSet recordSet){
	     if (recordSet == null) return false;
	     if (value == null) return true;  //leave as null...?
	     try{
	    	 int dt = getDataType();
	       switch (dt){
	         case java.sql.Types.BOOLEAN:
	           recordSet.updateBoolean(getName(), ((MguiBoolean)value).getTrue());
	           return true;
	         case java.sql.Types.DOUBLE:
	           recordSet.updateDouble(getName(), ((MguiDouble)value).getValue());
	           return true;
	         case java.sql.Types.FLOAT:
	           recordSet.updateDouble(getName(), ((MguiFloat)value).getValue());
	           return true;
	         case java.sql.Types.VARCHAR:
	           recordSet.updateString(getName(), (String)value);
	           return true;
	         case java.sql.Types.INTEGER:
	           recordSet.updateInt(getName(), ((MguiInteger)value).getInt());
	           return true;
	         case java.sql.Types.JAVA_OBJECT:
	           recordSet.updateBinaryStream(getName(), (InputStream)value, getLength());
	           return true;
	         case java.sql.Types.TIMESTAMP:
			    return true;
	 	     case java.sql.Types.SMALLINT:
	 	    	recordSet.updateInt(getName(), ((MguiInteger)value).getInt());
		        return true;
	 	     case java.sql.Types.TINYINT:
	 	    	recordSet.updateInt(getName(), ((MguiInteger)value).getInt());
		        return true;
	 	     case java.sql.Types.CHAR:
	 	    	recordSet.updateString(getName(), (String)value);
		        return true;
	 	     case java.sql.Types.LONGVARCHAR:
	 	    	recordSet.updateString(getName(), (String)value);
		        return true;
	 	     case java.sql.Types.LONGVARBINARY:
	 	    	recordSet.updateBinaryStream(getName(), (InputStream)value, getLength());
		        return true;
		     default:
		    	 InterfaceSession.log("DataField (SQLException): Unknown data type for field '" + getName() + "': " + dt, 
	   				LoggingType.Errors);
		    	return false;
	 	    	 
	       }
	     }
	     catch (SQLException e){
	       InterfaceSession.log("DataField (SQLException): Could not update field '" + getName() + "' with value " + value.toString() +
	    		   				" : " + e.getMessage(), 
	    		   				LoggingType.Errors);
	       return false;
	     }
	     catch (ClassCastException e){
	    	 InterfaceSession.log("DataField (ClassCastException): Could not update field '" + getName() + "' with value " + value.toString() +
					   				" : " + e.getMessage(), 
					   				LoggingType.Errors);
	       return false;
	     }
	     
	   }
	
	   //return a list of attributes for this table
	   public AttributeList getAttributeList(){
		   return attributes;
	   }
	   
	   @Override
	public String toString(){
		   return "Data Field: '" + getName() + "'";
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
		return "DataField";
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		if (default_value == null)
			default_value = "";
		
		String xml = (_tab + "<DataField name='" + XMLFunctions.getXMLFriendlyString(getName()) + "'" +
					 "\n" + _tab2 + "label='" + XMLFunctions.getXMLFriendlyString(getLabel()) + "'" +
					 "\n" + _tab2 + "datatype='" + getDataTypeStr() + "'" +
					 "\n" + _tab2 + "length='" + getLength() + "'" +
					 "\n" + _tab2 + "iskeyfield='" + isKeyField() + "'" +
					 "\n" + _tab2 + "isrequired='" + isRequired() + "'" +
					 "\n" + _tab2 + "isunique='" + isUnique() + "'" +
					 "\n" + _tab2 + "default='" + default_value.toString() + "'" +
					 "\n" + _tab + "/>\n");
		
		writer.write(xml);
		
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
		return "";
	}
	
	@Override
	public int compareTo(DataField field) {
		return this.getName().compareTo(field.getName());
	}
	   
}