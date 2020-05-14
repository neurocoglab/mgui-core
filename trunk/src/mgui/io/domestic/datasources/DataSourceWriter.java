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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import mgui.datasources.DataConnection;
import mgui.datasources.DataSource;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;

/**************************************************
 * Writes a Data Source object to file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceWriter extends FileWriter {

	public enum FileType{
		Plain,
		Xml;
	}
	
	public FileType type = FileType.Xml;
	
	public DataSourceWriter(){
		
	}
	
	public DataSourceWriter(FileType type){
		this.type = type;
	}
	
	public DataSourceWriter(File file){
		setFile(file);
	}
	
	public DataSourceWriter(File file, FileType type){
		this.type = type;
		setFile(file);
	}
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progressBar) {
		
		File[] files = options.getFiles();
		if (files == null || files.length == 0) return false;
		setFile(files[0]);
		DataSource source = ((DataSourceOutOptions)options).data_source;
		
		try{
			return writeDataSource(source);
		}catch (IOException e){
			InterfaceSession.log("DataSourceWriter: failed to write '" + dataFile.getAbsolutePath() + "'.");
			}
		
		return false;
	}
	
	/**********************************************************
	 * Writes this data source to a text file.
	 * 
	 * @param source
	 * @return
	 * @throws IOException
	 */
	public boolean writeDataSource(DataSource source) throws IOException{
		
		if (type == FileType.Xml) return writeDataSourceAsXml(source);
		
		BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
		DataConnection dc = source.getConnection();
		
		// Names with spaces must be wrapped in double quotes
		String name = dc.getName();
		if (name.contains(" "))
			name = "\"" + name + "\"";
		
		writer.write("name=" + name);
		writer.write("\nlogin=" + dc.getLogin());
		writer.write("\npassword=" + dc.getPassword());
		writer.write("\ndriver=" + dc.getDriver());
		writer.write("\nurl=" + dc.getUrl());
		writer.write("\nfile=" + dc.getFile().getAbsolutePath());
		
		writer.close();
		
		return true;
		
	}

	
	public boolean writeDataSourceAsXml(DataSource source) throws IOException{
		
		if (dataFile == null) return false;
		
		BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
		writer.write(XMLFunctions.getXMLHeader() + "\n\n");
		source.writeXML(0, writer);
		writer.close();
		
		return true;
	}
	
}