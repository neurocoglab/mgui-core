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

package mgui.datasources.tinysql;

import mgui.datasources.DataSourceDriver;
import mgui.datasources.DataTypes;
import mgui.datasources.mysql.MySQLDriver;

/********************************************************************************
 * Specifies the driver for TinySQL connectivity.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class TinySQLDriver extends DataSourceDriver {

	public TinySQLDriver(){
		this("", "");
	}
	
	public TinySQLDriver(String user, String password){
		super("TinySQL", 
			  "com.sqlmagic.tinysql.dbfFileDriver", 
			  "jdbc:dbfFile", 
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
	}
	
	@Override
	public String getSQLQuote(){
		return "";
	}
	
	@Override
	public String getSQLType(int sql_type, int length){
		String s = DataTypes.getSQLTypeStr(sql_type, length);
		if (s.startsWith("INTEGER"))
			s = s.replace("INTEGER", "INT");
		if (s.startsWith("DOUBLE"))
			s = s.replace("DOUBLE", "FLOAT");
		if (!s.contains("INT") && 
			!s.contains("CHAR") &&
			!s.contains("FLOAT") &&
			!s.contains("DATE")) return null;
		return s;
	}
	
	@Override
	public String getSQLName(String name){
		return name.toUpperCase();
	}
	
	@Override
	public Object clone(){
		return new TinySQLDriver(login, password);
	}
	
}