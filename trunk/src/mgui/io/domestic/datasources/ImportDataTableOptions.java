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

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import mgui.datasources.DataField;
import mgui.datasources.DataSource;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.io.InterfaceIOOptions;

/**************************************************************
 * Specifies options for importing data to a data source table.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class ImportDataTableOptions extends InterfaceIOOptions {

	File[] files;
	ArrayList<ArrayList<DataField>> data_fields;
	ArrayList<ArrayList<Boolean>> include_fields;
	ArrayList<String> names;
	DataSource data_source;
	public boolean add_uid = false;
	public String uid_name = "uid";
	
	public boolean overwrite_existing = true;
	public boolean fail_on_error = true;
	
	@Override
	public File[] getFiles() {
		return files;
	}

	@Override
	public void setFiles(File[] files) {
		this.files = files;
		this.names = new ArrayList<String>(files.length);
		for (int i = 0; i < files.length; i++)
			names.add(getFileName(files[i]));
	}
	
	@Override
	public JFileChooser getFileChooser(){
		return getFileChooser(null);
	}
	
	@Override
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Select text files to import");
		return fc;
	}
	
	public ArrayList<String> getNames(){
		return names;
	}
	
	public void setNames(ArrayList<String> names){
		if (files == null || files.length == 0) return;
		this.names = new ArrayList<String>(files.length);
		for (int i = 0; i < files.length; i++){
			if (i < names.size())
				this.names.add(names.get(i));
			else
				this.names.add(getFileName(files[i]));
			}
			
	}
	
	public void setDataSource(DataSource source){
		this.data_source = source;
		
		if (names == null) return;
		
		// Make names driver-friendly
		for (int i = 0; i < names.size(); i++)
			names.set(i, data_source.getDataSourceDriver().getFriendlyName(names.get(i)));
		
	}
	
	public DataSource getDataSource(){
		return this.data_source;
	}
	
	private String getFileName(File file){
		String name = file.getName();
		if (name.contains("."))
			name = name.substring(0, name.indexOf("."));
		if (this.data_source != null)
			name = data_source.getDataSourceDriver().getFriendlyName(name);
		return name;
	}
	
	/************************************
	 * Returns the {@code DataField} objects defined for this options (i.e., corresponding to the specified input
	 * files).
	 * 
	 * @return
	 */
	public ArrayList<ArrayList<DataField>> getDataFields(){
		return this.data_fields;
	}
	
	/*************************************
	 * Sets the data fields which define this import; each list corresponds to the associated input file.
	 * 
	 * @param data_fields
	 */
	public void setDataFields(ArrayList<ArrayList<DataField>> data_fields){
		setDataFields(data_fields, null);
	}
	
	/************************************
	 * Returns a list of include values; i.e., whether to include the corresponding field in the import.
	 * 
	 * @return
	 */
	public ArrayList<ArrayList<Boolean>> getIncludeFields(){
		return this.include_fields;
	}
	
	/*************************************
	 * Sets the data fields which define this import; each list corresponds to the associated input file. The
	 * {@code inlcude_fields} parameter defines which fields to include; if this is {@code null}, all fields will
	 * be imported.
	 * 
	 * @param data_fields
	 * @param include_fields
	 */
	public void setDataFields(ArrayList<ArrayList<DataField>> data_fields, ArrayList<ArrayList<Boolean>> include_fields){
		this.data_fields = data_fields;
		this.include_fields = include_fields;
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		InterfaceSession.log(this.getClass().getCanonicalName() + ": setObject not implemented.", LoggingType.Warnings);
	}
	
	

}