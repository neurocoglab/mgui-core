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

package mgui.io.domestic.datasources;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import mgui.datasources.DataSourceDriver;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.util.QuotedStringTokenizer;

/******************************************************
 * Loader for a data source (JDBC) driver specification file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceDriverLoader extends FileLoader {

	public DataSourceDriver loadDriver(){
		if (dataFile == null){
			InterfaceSession.log("DataSourceDriverLoader: No file specified.");
			return null;
			}
		
		if (!dataFile.exists()){
			InterfaceSession.log("DataSourceDriverLoader: Input file '" + dataFile.getAbsolutePath() + "' not found.");
			return null;
			}
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			String line = reader.readLine();
			reader.close();
			
			QuotedStringTokenizer tokens = new QuotedStringTokenizer(line, " ");
			String token = tokens.nextToken();
			String className = "";
			if (token == null) return null;
			try{
				
				className = token;
				DataSourceDriverClassLoader c_loader = new DataSourceDriverClassLoader();
				Class c = c_loader.loadClass(className);
				DataSourceDriver driver = (DataSourceDriver)c.newInstance();
				token = tokens.nextToken();
				if (token == null) return null;
				driver.setUrl(token);
				token = tokens.nextToken();
				if (token == null) return null;
				driver.setName(token);
				driver.setUrlReference(dataFile.toURI().toURL());
				
				driver.init();
				
				if (tokens.hasMoreTokens()){
					token = tokens.nextToken();
					driver.setLogin(token);
					
					if (tokens.hasMoreTokens()) 
						driver.setPassword(tokens.nextToken());
					}
				return driver;
				
			}catch (Exception e){
				InterfaceSession.log("DataSourceDriverLoader: Error loading driver. Details: " + e.getMessage(), 
									 LoggingType.Errors);
				return null;
			}
			
			
		}catch (IOException ex){
			InterfaceSession.log("DataSourceDriverLoader: Problem reading driver file '" + dataFile.getAbsolutePath() + "':" + ex.getMessage());
			}
		
		return null;
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException {
		return loadDriver();
	}
	
	class DataSourceDriverClassLoader extends ClassLoader{
		public DataSourceDriverClassLoader(){
			
		}
		public synchronized Class loadClass(String className){
			try{
				Class c = loadClass(className, false);
				if (c == null) 
					return null;
				if (DataSourceDriver.class.isInstance(c.newInstance())){
					c = loadClass(className, true);
					return c;
					}
			}catch (Exception e){
				e.printStackTrace();
				}
			return null;
		}
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(DataSourceDriver.class);
		return objs;
	}

}