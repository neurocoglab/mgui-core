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

package mgui.interfaces.datasources;

import java.util.ArrayList;
import java.util.HashMap;

import mgui.datasources.DataSourceDriver;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptions;

/***********************************************************
 * Options which specify a JDBC data source driver.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceDriverOptions extends InterfaceOptions {

	public DataSourceDriverOptions(){
		
	}
	
	/************************************************
	 * Returns a hash map of the currently loaded data source JDBC drivers. 
	 * 
	 * @return
	 */
	public HashMap<String, DataSourceDriver> getDrivers(){
		ArrayList<String> names = InterfaceEnvironment.getDataSourceDriverNames();
		HashMap<String, DataSourceDriver> drivers = new HashMap<String, DataSourceDriver>();
		
		for (int i = 0; i < names.size(); i++){
			DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriver(names.get(i));
			drivers.put(names.get(i), driver);
			}
		
		return drivers;
	}
	
	
	
	
}