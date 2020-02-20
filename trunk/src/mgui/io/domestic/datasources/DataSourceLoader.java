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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import mgui.datasources.DataConnection;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceDriver;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/********************************************
 * Loads a Data Source object. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceLoader extends FileLoader {

	public enum FileType{
		Plain,
		Xml,
		Detect;
	}
	
	public FileType type = FileType.Detect;
	
	public DataSourceLoader(){
		
	}
	
	public DataSourceLoader(FileType type){
		this.type = type;
	}
	
	public DataSourceLoader(File file){
		setFile(file);
	}
	
	public DataSourceLoader(File file, FileType type){
		this.type = type;
		setFile(file);
	}
	
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		File[] files = options.getFiles();
		if (files == null || files.length == 0) return false;
		boolean success = true;
		
		for (int i = 0; i < files.length; i++){
			setFile(files[i]);
			try{
				DataSource source = (DataSource)loadObject(progress_bar, null);
				if (source == null){
					InterfaceSession.log("DataSourceLoader: could not load '" + files[i].getAbsolutePath() + "'.");
					success = false;
				}else{
					success &= InterfaceSession.getWorkspace().addDataSource(source);
					}
			}catch (IOException e){
				InterfaceSession.log("DataSourceLoader: could not load '" + files[i].getAbsolutePath() + "'.");
				success = false;
				}
			}
		
		return success;
	}

	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException {
	
		if (dataFile == null) return null;
		
		if (type == FileType.Xml) return loadObjectXml(progress_bar);
		
		if (type == FileType.Detect){
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			String line = reader.readLine();
			reader.close();
			if (line == null){
				return null;
				}
			if (line.equals(XMLFunctions.getXMLHeader()))
				return loadObjectXml(progress_bar);
			}
		
		DataConnection dc = new DataConnection();
		dc.setFromFile(dataFile);
		DataSourceDriver driver = InterfaceEnvironment.getDataSourceDriverByClass(dc.getDriver());
		if (driver == null){
			String params = dc.getDriver() + " " + 
							dc.getDriver() + " " +
							dc.getUrl();
			if (dc.getLogin().length() > 0)
				params = params + " " + dc.getLogin() + " " + dc.getPassword();
			if (!InterfaceEnvironment.addDataSourceDriver(params)){
				InterfaceSession.log("InterfaceEnvironment: error loading data source" + 
								   " init file '" + dataFile.getAbsolutePath() + "'");
				dc = null;
				}
			}
		if (dc == null) return null;
		
		return new DataSource(dc);
		
	}

	/*******************************************************
	 * Loads this data source from an XML format file
	 * 
	 * @param progress_bar
	 * @return
	 * @throws IOException
	 */
	public Object loadObjectXml(ProgressUpdater progress_bar) throws IOException {
		
		if (dataFile == null) return null;
		
		
		try{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			DataSourceXMLHandler handler = new DataSourceXMLHandler();
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(dataFile)));
			return handler.getDataSource();
			
		}catch (SAXParseException e){
			InterfaceSession.log("DataSourceLoader (" + dataFile.getAbsolutePath() + "): Exception at line " + e.getLineNumber() + " col " + e.getColumnNumber() + ": " +
					e.getMessage(),
					LoggingType.Errors);
			//e.printStackTrace();	
			InterfaceSession.handleException(e);
			}
		catch (Exception e){
			e.printStackTrace();	
			}
		
		return null;
		
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(DataSource.class);
		return objs;
	}
	
}