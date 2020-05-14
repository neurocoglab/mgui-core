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

package mgui.datasources.odbc;

import mgui.datasources.DataSourceDriver;

/********************************************************************************
 * Specifies the driver for the JDBC-ODBC bridge
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class JdbcOdbcDriver extends DataSourceDriver {

	public JdbcOdbcDriver(){
		this("jdbc:odbc:Driver={Microsoft Text Driver (*.txt; *.csv)}", "", "");
	}
	
	public JdbcOdbcDriver(String url, String user, String password){
		super("JDBC-ODBC", 
			  "sun.jdbc.odbc.JdbcOdbcDriver", 
			  url, 
			  user, 
			  password);
				
	}
	
	@Override
	public String getUrlFromName(String name){
		return name;
	}
	
	@Override
	public String getUrlPrefix(){
		return url;
		//if (url.indexOf(";") < 0) return url;
		//return url.substring(0, url.indexOf(";"));
	}
	
	@Override
	public Object clone(){
		return new JdbcOdbcDriver(url, login, password);
	}
	
}