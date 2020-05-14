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

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

import mgui.datasources.security.SecureDataSourceFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.datasources.DataSourceDialogPanel;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.io.PersistentObject;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.security.StringEncrypter;
import mgui.io.FileLoader;
import mgui.io.FileWriter;
import mgui.io.InterfaceIO;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.datasources.DataSourceDriverLoader;
import mgui.io.domestic.datasources.DataSourceDriverWriter;

/*************************************
 * Defines a JDBC driver for a {@link DataConnection}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceDriver implements PersistentObject,
										 Cloneable,
										 AttributeObject{

	protected String name;
	protected String className;
	protected String login;
	protected String password;
	protected String url;
	protected boolean ssl = false;

	protected URL file_ref;
	protected FileLoader loader = new DataSourceDriverLoader();
	protected FileWriter writer = new DataSourceDriverWriter();
	
	protected AttributeList attributes;
	
	public DataSourceDriver(){
		
	}
	
	public DataSourceDriver(String name, String class_name, String url){
		this(name, class_name, url, "root", "");
	}
	
	public DataSourceDriver(String name, String class_name, String url, String user, String password){
		this.name = name;
		className = class_name;
		this.url = url;
		this.login = user;
		this.password = password;
	}
	
	public DataSourceDriver(String name, String class_name, String url, String user, String password, boolean ssl){
		this.name = name;
		className = class_name;
		this.url = url;
		this.login = user;
		this.password = password;
		this.ssl = ssl;
	}
	
	public boolean init(){
		try{
			Class.forName(className);
			return true;
		}catch (Exception e){
			InterfaceSession.log("Could not initiate driver '" + this.getName() + 
								 "', class '" + getClassName() + "'\n" +
								 "Details: " + e.getMessage(), 
								 LoggingType.Errors);
			//e.printStackTrace();
			}
		return false;
	}
	
	/**************************************************
	 * Instructs the database server to drop the specified data source. If the data source is
	 * open, it will be closed; if this fails an Exception is thrown.
	 * 
	 * @param name
	 * @return
	 */
	public void dropDatabase(DataSource source) throws DataSourceException{
		
		if (source.isConnected() && !source.disconnect())
			throw new DataSourceException("PostgreSQLDriver: could not disconnect data source '" + source.getName() + "'.");
		
		try{
			Connection connection = getConnection();
			Statement s = connection.createStatement();
			
			s.executeUpdate("DROP DATABASE " + source.getConnection().getName());
						
		}catch (Exception ex){
			throw new DataSourceException("DataSourceDriver: Exception dropping data source: " + ex.getMessage());
		}
		
	}
	
	/**************************************************
	 * Generates a driver-specific SQL string for creating a new database.
	 * 
	 * @param name Name for the new database
	 * @return
	 */
	public String getCreateDatabaseSQL(String name){
		String q = getSQLQuote();
		String sql = "CREATE DATABASE " + q + name + q;
		return sql;
	}
	
	@Override
	public URL getUrlReference() {
		return file_ref;
	}

	@Override
	public FileLoader getFileLoader() {
		return loader;
	}
	
	@Override
	public boolean setFileLoader(InterfaceIOType io_type){
		InterfaceIO io = io_type.getIOInstance();
		if (io == null || !(io instanceof FileLoader)){
			InterfaceSession.log("InterfaceShape: Type is not a file loader: '" + io_type.getName() + "'.", 
					 			 LoggingType.Errors);
			return false;
			}
			
		loader = (FileLoader)io;
		return true;
	}

	@Override
	public FileWriter getFileWriter() {
		return writer;
	}
	
	@Override
	public boolean setFileWriter(InterfaceIOType io_type){
		InterfaceIO io = io_type.getIOInstance();
		if (io == null || !(io instanceof FileWriter)){
			InterfaceSession.log("InterfaceShape: Type is not a file writer: '" + io_type.getName() + "'.", 
					 			 LoggingType.Errors);
			return false;
			}
			
		writer = (FileWriter)io;
		return true;
	}

	@Override
	public void setUrlReference(URL url){
		file_ref = url;
	}
	
	public void setClassName(String s){
		this.className = s;
	}
	
	public String getClassName(){
		return className;
	}
	
	public boolean getSSL(){
		return ssl;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	/*******************************************************
	 * Extracts a data source name from a driver-specific URL
	 * 
	 * @param url
	 * @return
	 */
	public String getDataSourceFromUrl(String url){
		return "";
	}
	
	public String getUrlFromName(String name){
		return url + "/" + name;
	}
	
	public String getUrlPrefix(){
		if (url.lastIndexOf("/") < 0) return url;
		return url.substring(0, url.lastIndexOf("/"));
	}
	
	/************************************************************
	 * If implemented, returns a list of properties the databases currently accessible through this driver. 
	 * Otherwise, returns an empty list.
	 * 
	 * @return
	 */
	public ArrayList<String> getDatabases(){
		return getDatabases(null);
	}
	
	/************************************************************
	 * If implemented, returns a map of properties the databases currently accessible through this driver. 
	 * Otherwise, returns an empty map.
	 * 
	 * @param properties If not {@code null}, fills this map with database properties
	 * @return
	 */
	public ArrayList<String> getDatabases(HashMap<String,HashMap<String,String>> properties){
		return new ArrayList<String>();
	}
	
	/************************************************************
	 * Returns a string from <code>name</code> that is safe for this driver.
	 * 
	 * @param name
	 * @return
	 */
	public String getSQLName(String name){
		return name;
	}
	
	/*************************************************************
	 * Returns a driver-specific SQL quotation mark; subclasses should only override this if their drivers expect
	 * non-standard SQL input.
	 * 
	 * @return
	 */
	public String getSQLQuote(){
		return "`";
	}
	
	/*************************************************************
	 * Returns a driver-specific SQL quotation mark
	 * 
	 * @param sql_type An SQL type; see {@link Types}
	 * @param length The length of the field; for variable-length fields only
	 * @return
	 */
	public String getSQLType(int sql_type, int length){
		return DataTypes.getSQLTypeStr(sql_type, length);
	}
	
	public Connection getConnection() throws StringEncrypter.EncryptionException,
	   										 SQLException{
		return SecureDataSourceFunctions.getSecureConnection(url, login, password);
	}
	
	/***********************************************************
	 * Converts {@code string} to a "friendly" string; i.e., replacing all illegal characters with acceptable
	 * ones.
	 * 
	 * @param string
	 * @return
	 */
	public String getFriendlyName(String string){
		
		char[] chars = new char[]{'-'};
		char[] replace = new char[]{'_'};
		
		for (int i = 0; i < chars.length; i++){
			string = string.replace(chars[i], replace[i]);
			}
		
		return string;
	}
	
	@Override
	public Object clone(){
		return new DataSourceDriver(name,
								    className,
								    url,
								    login,
								    password);
	}
	
	/************************************************
	 * Returns a {@link DataSourceDialogPanel} to specify this data source.
	 * 
	 * @return
	 */
	public DataSourceDialogPanel getDataSourceDialogPanel(){
		return new DataSourceDialogPanel();
	}
	
	@Override
	public AttributeList getAttributes() {
		return attributes;
	}

	@Override
	public Attribute<?> getAttribute(String attrName) {
		if (attributes == null)
			return null;
		return attributes.getAttribute(attrName);
	}

	@Override
	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		if (attributes == null) return;
		attributes.setValue(attrName, newValue);
	}
	
	@Override
	public InterfaceIOOptions getLoaderOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoaderOptions(InterfaceIOOptions options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InterfaceIOOptions getWriterOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWriterOptions(InterfaceIOOptions options) {
		// TODO Auto-generated method stub
		
	}
	
}