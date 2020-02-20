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
import java.sql.Types;
import java.util.ArrayList;

import mgui.datasources.DataField;
import mgui.datasources.DataRecordSet;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataTable;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.io.InterfaceIOOptions;
import foxtrot.Job;
import foxtrot.Worker;

/******************************************************************
 * Imports data from a delimited text file into a new {@code DataSource} table. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ImportDataTableTextLoader extends ImportDataTableLoader {

	String delimiter = "\t";
	boolean has_header = true;
	boolean fail_on_error = false;
	boolean overwrite_existing = false;
	boolean add_uid = false;
	String uid_name = "uid";
	int start_at = 1;
	
	@Override
	public boolean load(final InterfaceIOOptions options, final ProgressUpdater progress_bar) {
		
		ImportDataTableTextOptions _options = (ImportDataTableTextOptions)options;
		this.delimiter = _options.delimiter;
		this.has_header = _options.has_header;
		this.data_source = _options.getDataSource();
		this.overwrite_existing = _options.overwrite_existing;
		this.add_uid = _options.add_uid;
		this.uid_name = _options.uid_name;
		this.start_at = _options.start_at;
		if (has_header) start_at = Math.max(start_at, 1);
		
		final File[] files = _options.getFiles();
		final ArrayList<ArrayList<DataField>> data_fields = _options.getDataFields();
		final ArrayList<ArrayList<Boolean>> include_fields = _options.getIncludeFields();
		final ArrayList<String> names = _options.getNames();
		
		boolean success = true;
		
		if (progress_bar != null){
			progress_bar.setMinimum(0);
			progress_bar.setMaximum(files.length);
			progress_bar.update(0);
			}
		
		for (int i = 0; i < files.length && (success || !_options.fail_on_error); i++){
			this.dataFile = files[i];
			
				final String name = names.get(i);
				final ArrayList<DataField> fields = data_fields.get(i);
				final ArrayList<Boolean> include = include_fields.get(i);
				success &= (Boolean)Worker.post(new Job(){
					public Boolean run(){
						try{
							return loadDataTable(name, fields, include,  progress_bar);
						}catch (IOException ex){
							InterfaceSession.log("ImportDataTableTextLoader: Error reading file '" + dataFile.getAbsolutePath() + "':" +
												 ex.getMessage(),
												 LoggingType.Errors);
							return false;
							}
						}
					});
				
				if (progress_bar != null){
					if (progress_bar.isCancelled()){
						break;
						}
					progress_bar.update(i);
					}
				
			}
		
		return success;
	}

	
	
	public void setDelimiter(String delimiter){
		
		this.delimiter = delimiter;
	}
	
	@Override
	public boolean loadDataTable(final String name, 
								 final ArrayList<DataField> fields, 
								 final ArrayList<Boolean> include,
								 final ProgressUpdater progress_bar) throws IOException {
		
			if (data_source == null || dataFile == null){
				InterfaceSession.log("ImportDataTableTextLoader: Data source or input file not set.", 
									 LoggingType.Errors);
				return false;
				}
			
			if (progress_bar != null){
				progress_bar.setMessage("Importing table '" + name + "' ");
				progress_bar.setMaximum(fields.size());
				progress_bar.reset();
				}
			
			// Construct the table and create in data source
			DataTable new_table = new DataTable(name);
			int key_count = 0;
			for (int i = 0; i < fields.size(); i++){
				if (include.get(i)){
					new_table.addField(fields.get(i));
					if (fields.get(i).isKeyField())
						key_count++;
					}
				}
			
			String uid_field = null;
			if (add_uid && key_count != 1){
				// Add a unique key field
				uid_field = uid_name;
				int k = 1;
				while (new_table.hasField(uid_field))
					uid_field = uid_name + "_" + k++;
				DataField field = new DataField(uid_field, Types.INTEGER);
				field.setIsKeyField(true);
				new_table.addField(field);
				}
			
			try{
				if (this.overwrite_existing && data_source.getTableSet().getTable(name) != null){
					// Delete existing
					DataTable existing = data_source.getTableSet().getTable(name);
					if (!data_source.removeDataTable(existing)){
						InterfaceSession.log("ImportDataTableTextLoader: Could not delete existing table '" + 
								name + "' in data source '" + data_source, 
								LoggingType.Errors);
						return false;
						}
					}
			}catch (DataSourceException ex){
				InterfaceSession.log("ImportDataTableTextLoader: Could not delete existing table '" + 
						name + "' in data source '" + data_source, 
						LoggingType.Errors);
				return false;
				}
			
			if (!data_source.addDataTable(new_table)){
				InterfaceSession.log("ImportDataTableTextLoader: Could not create table '" + new_table.getName() +
										"' in data source '" + data_source, 
										LoggingType.Errors);
				return false;
				}
			
			// Import data into the new table
			try{
				BufferedReader reader = new BufferedReader(new FileReader(dataFile));
				
				String line = reader.readLine();
				
				if (progress_bar != null){
					int count = 0;
					// Get line count
					while (line != null){
						line = reader.readLine();
						count++;
						}
					reader.close();
					progress_bar.setMaximum(count);
					reader = new BufferedReader(new FileReader(dataFile));
					
					line = reader.readLine();
					}
				
				if (has_header){
					int itr = 0;
					while (line != null && itr++ < start_at)
						line = reader.readLine();
					}
				
				DataRecordSet record_set = data_source.getRecordSet(new_table);
				
				int line_no = start_at;
				while (line != null){
					record_set.addNew();
					String[] tokens = line.split(delimiter + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
					
					//String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
					
					int n = tokens.length;
					
					if (n > fields.size()){
						InterfaceSession.log("ImportDataTableTextLoader: Warning: line " + line_no + " has incorrect field count.", 
								 			 LoggingType.Warnings);
						if (fail_on_error){
							reader.close();
							if (!data_source.removeDataTable(new_table)){
								InterfaceSession.log("ImportDataTableTextLoader: Could not remove new table '" + new_table.getName() +
													 "' from data source '" + data_source + "' after failed import attempt.", 
													 LoggingType.Errors);
								}
								return false;
							}
							
						}
					
					// Add uid field if set
					if (uid_field != null){
						record_set.updateField(uid_field, "" + line_no);
						}
					
					//while (tokens.hasMoreTokens() && i < fields.size()){
					for (int i = 0; i < n && i < fields.size(); i++){
						DataField field = fields.get(i);
						String token = null;
						token = tokens[i];
						
						//Remove all quotes
						token = token.replaceAll("\"", "");
						token = token.replaceAll("'", "");
						token = token.replaceAll("\u0000", "");	// Needed for PostgreSQL??
							
						record_set.updateField(field.getName(), token);
						}
					
					if (!record_set.update()){
						if (!fail_on_error){
							InterfaceSession.log("ImportDataTableTextLoader: Warning: Could not update line " + line_no + ".", 
						 			 			 LoggingType.Warnings);
						}else{
							InterfaceSession.log("ImportDataTableTextLoader: Warning: Could not update line " + line_no + "... failing.", 
			 			 			 			  LoggingType.Errors);
							reader.close();
							if (!data_source.removeDataTable(new_table)){
								InterfaceSession.log("ImportDataTableTextLoader: Could not remove new table '" + new_table.getName() +
													 "' from data source '" + data_source + "' after failed import attempt.", 
													 LoggingType.Errors);
								}
								return false;
							}
						}
					
					if (progress_bar != null){
						if (progress_bar.isCancelled()){
							InterfaceSession.log("ImportDataTableTextLoader: Operation cancelled by user..",
									 			 LoggingType.Warnings);
							reader.close();
							return false;
							}
						progress_bar.update(line_no);
						}
					
					line = reader.readLine();
					line_no++;
					}
				
				reader.close();
				
			}catch (IOException ex){
				// If import fails, destroy new table and rethrow
				if (!data_source.removeDataTable(new_table)){
					InterfaceSession.log("ImportDataTableTextLoader: Could not remove new table '" + new_table.getName() +
										 "' from data source '" + data_source + "' after failed import attempt.", 
										 LoggingType.Errors);
					}
				
				throw (ex);
			}catch (DataSourceException ex){
				
				InterfaceSession.log("ImportDataTableTextLoader: Exception accessing table '" + new_table.getName() +
						 "' from data source '." + data_source + "'.", 
						 LoggingType.Errors);
				if (!data_source.removeDataTable(new_table)){
					InterfaceSession.log("ImportDataTableTextLoader: Could not remove new table '" + new_table.getName() +
										 "' from data source '" + data_source + "' after failed import attempt.", 
										 LoggingType.Errors);
					}
				return false;
				}
			
			return true;
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException {
		
		return null;
	}

}