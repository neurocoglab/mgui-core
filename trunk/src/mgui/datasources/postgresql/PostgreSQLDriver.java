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

package mgui.datasources.postgresql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import mgui.datasources.DataSource;
import mgui.datasources.DataSourceDriver;
import mgui.datasources.DataSourceException;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.datasources.DataSourceDialogPanel;
import mgui.interfaces.datasources.DataSourceListDialogPanel;
import mgui.interfaces.logs.LoggingType;

/****************************************************************
 * Specifies the driver for PostgreSQL connectivity.
 * 
 * <p>See the <a href="http://jdbc.postgresql.org">PostgreSQL JDBC Site</a> for details.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */
public class PostgreSQLDriver extends DataSourceDriver {
	
	public PostgreSQLDriver(){
		this("jdbc:postgresql://localhost", "", "");
	}
	
	public PostgreSQLDriver(String url, String user, String password){
		super("PostgreSQL", 
			  "org.postgresql.Driver", 
			  url, 
			  user, 
			  password);
		this.ssl = false;
		attributes = new AttributeList();
		attributes.add(new Attribute<String>("encoding", "UTF8"));
		attributes.add(new Attribute<String>("template", "template0"));
	}
		
	/**********************************************
	 * Sets the character encoding for this database; 
	 * see <a href='http://www.postgresql.org/docs/8.1/static/multibyte.html'>
	 * http://www.postgresql.org/docs/8.1/static/multibyte.html</a>
	 * as this applies to PostgreSQL.
	 * 
	 * @param encoding
	 */
	public void setEncoding(String encoding){
		attributes.setValue("encoding", encoding);
	}
	
	/*********************************************
	 * Returns the character encoding for this database; 
	 * see <a href='http://www.postgresql.org/docs/8.1/static/multibyte.html'>
	 * http://www.postgresql.org/docs/8.1/static/multibyte.html</a>
	 * as this applies to PostgreSQL.
	 * 
	 * @return
	 */
	public String getEncoding(){
		return (String)attributes.getValue("encoding");
	}
	
	/*********************************************
	 * Sets the name of the template database to use for creating new databases
	 * 
	 * @param template
	 */
	public void setTemplate(String template){
		attributes.setValue("template", template);
	}
	
	/*********************************************
	 * Returns the name of the current template database
	 * 
	 * @return
	 */
	public String getTemplate(){
		return (String)attributes.getValue("template");
	}
	
	@Override
	public String getCreateDatabaseSQL(String name){
		String q = getSQLQuote();
		String sql = "CREATE DATABASE " + q + name + q;
		String encoding = getEncoding();
		if (encoding != null)
			sql = sql + " ENCODING='" + encoding + "'";
		String template = getTemplate();
		if (template != null)
			sql = sql + " TEMPLATE=" + template;
		return sql + ";";
	}
	
	@Override
	public ArrayList<String> getDatabases(HashMap<String,HashMap<String,String>> properties){
		
		try{
			ArrayList<String> dbs = new ArrayList<String>();
			Connection connection = getConnection();
			
			//DatabaseMetaData md = connection.getMetaData();
			//ResultSet rs = md.getCatalogs();
			
			Statement s = connection.createStatement();
            String sql = "SELECT pg_database.datname, " +  
            					"pg_encoding_to_char(pg_database.encoding) AS encoding, " +
            					"pg_user.usename AS owner " +
            			 "FROM pg_catalog.pg_database, pg_catalog.pg_user " +
            			 "WHERE pg_database.datdba = pg_user.usesysid;";
            ResultSet rs = s.executeQuery(sql);
            while (rs.next()) {
            	String name = rs.getString("datname");
                dbs.add(name);
                if (properties != null){
                	HashMap<String,String> map = new HashMap<String,String>();
                	map.put("encoding", rs.getString("encoding"));
                	map.put("owner", rs.getString("owner"));
                	properties.put(name, map);
                	}
            }
            rs.close();
           // s.close();
            connection.close();
            
            return dbs;
		
		 }catch (Exception e){
			//e.printStackTrace();
			InterfaceSession.log("PostgreSQLDriver: Error reading database names; check the server.", LoggingType.Errors);
		 	}
		
		return null;
	}
	
	@Override
	public String getDataSourceFromUrl(String url){
		
		int p = url.lastIndexOf("/");
		if (p <= 0 || p == url.length()) return "";
		
		String sub = url.substring(p + 1);
		if (sub.startsWith("/")) return "";
		return sub;
		
	}

	@Override
	public String getSQLQuote(){
		return "\"";
	}
	
	@Override
	public Object clone(){
		return new PostgreSQLDriver(url, login, password);
	}
	
	@Override
	public DataSourceDialogPanel getDataSourceDialogPanel(){
		return new DataSourceListDialogPanel(this);
	}
	
	@Override
	public String getUrlFromName(String name){
		// Does url contain reference to a database?
		// If so, it should be replaced.
		String[] parse = url.split("//");
		if (parse.length < 2)
			return url + "/" + name;
		
		int c = parse[1].indexOf("/");
		if (c < 0)
			return url + "/" + name;
		
		return parse[0] + "//" + parse[1].substring(0,c) + "/" + name;
		
	}
	
}