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

package mgui.datasources.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import mgui.datasources.DataSourceDriver;
import mgui.datasources.odbc.JdbcOdbcDriver;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.datasources.DataSourceDialogPanel;
import mgui.interfaces.datasources.DataSourceListDialogPanel;
import mgui.interfaces.logs.LoggingType;

/********************************************************************************
 * Specifies the driver for MySQL connectivity.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MySQLDriver extends DataSourceDriver {

	public MySQLDriver(){
		this("jdbc:mysql://localhost:3306", "root", "");
	}
	
	public MySQLDriver(String url, String user, String password){
		super("MySQL", 
			  "com.mysql.jdbc.Driver", 
			  url, 
			  user, 
			  password);
	}
	
	@Override
	public ArrayList<String> getDatabases(HashMap<String,HashMap<String,String>> properties){
		
		try{
			ArrayList<String> dbs = new ArrayList<String>();
			Connection connection = getConnection();
			
			Statement s = connection.createStatement();
            String sql = "select SCHEMA_NAME as 'Database'" +
                         "from INFORMATION_SCHEMA.SCHEMATA;";
            ResultSet rs = s.executeQuery(sql);
            // TODO: fill with properties
            while (rs.next()) {
                dbs.add(rs.getString(1));
            	}
            rs.close();
            s.close();
            connection.close();
            
            return dbs;
		
		 }catch (Exception e){
			InterfaceSession.log("MySQLDriver: Error reading database names; check the server.", LoggingType.Errors);
		 	}
		
		return null;
	}
	
	@Override
	public String getDataSourceFromUrl(String url){
		int p = url.lastIndexOf("/");
		if (p < 0) return "";
		String part = url.substring(p + 1);
		p = part.indexOf("?");
		if (p > 0)
			return part.substring(0, p);
		return part;
	}
	
	@Override
	public Object clone(){
		return new MySQLDriver(url, login, password);
	}
	
	@Override
	public DataSourceDialogPanel getDataSourceDialogPanel(){
		return new DataSourceListDialogPanel(this);
	}
	
}