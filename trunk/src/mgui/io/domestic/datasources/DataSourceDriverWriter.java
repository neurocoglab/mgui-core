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

package mgui.io.domestic.datasources;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import mgui.datasources.DataSourceDriver;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;

/*********************************************************
 * Writer for a data source (JDBC) driver file. Simple one-line, space-delimited.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceDriverWriter extends FileWriter{

	private DataSourceDriver driver;

	public boolean write(DataSourceDriver driver){
		
		if (dataFile == null){
			InterfaceSession.log("DataSourceDriverWriter: No file specified.");
			return false;
			}
		
		try{
			if (dataFile.exists() && !dataFile.delete()){
				InterfaceSession.log("DataSourceDriverWriter: Could not delete existing driver file '" + dataFile.getAbsolutePath() + "'.");
				return false;
				}
			
			if (!dataFile.createNewFile()){
				InterfaceSession.log("DataSourceDriverWriter: Problem creating driver file '" + dataFile.getAbsolutePath() + "'.");
				return false;
				}
			
			BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
			String class_name = driver.getClass().getCanonicalName();
			
			//Names with spaces must be wrapped in double quotes
			String name = driver.getName();
			if (name.contains(" "))
				name = "\"" + name + "\"";
			
			writer.write(class_name + " " + 
						 driver.getUrl() + " " + 
						 name + " " + 
						 driver.getLogin() + " " + 
						 driver.getPassword());
			
			writer.close();
			return true;
			
		}catch (IOException ex){
			InterfaceSession.log("DataSourceDriverWriter: Problem writing driver file '" + dataFile.getAbsolutePath() + "':" + ex.getMessage());
			return false;
			}
		
	}
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		
		
		
		return false;
	}

	@Override
	public String getSuccessMessage() {
		return super.getSuccessMessage();
	}

	@Override
	public String getFailureMessage() {
		return super.getFailureMessage();
	}

	@Override
	public String getTitle() {
		return "Write Data Source Driver";
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(DataSourceDriver.class);
		return objs;
	}
	
	
}