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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/******************************************
 * Holds a list of {@link DataSourceDriver} objects.
 * 
 * @author AndrewR
 *
 */
public class DataSourceDrivers {

	static HashMap<String, DataSourceDriver> drivers = new HashMap<String, DataSourceDriver>();
	//static String login, password;
	
	public static ArrayList<String> getDrivers(){
		ArrayList<String> ret = new ArrayList<String>();
		Iterator<String> itr = drivers.keySet().iterator();
		while (itr.hasNext())
			ret.add(itr.next());
		return ret;
			
	}
	
	public static String getUrl(String driver){ 
		return drivers.get(driver).url;
	}
	
	
	public static DataSourceDriver getDriver(String driver){
		return drivers.get(driver);
	}
	
	public static String getClassName(String driver){
		DataSourceDriver d = drivers.get(driver);
		if (d == null) return null;
		return d.className;
	}
	
	public static ArrayList<String> getDatabaseNames(String driver){
		
		DataSourceDriver d = drivers.get(driver);
		if (d == null) return null;
		return d.getDatabases();
		
	}
	
}