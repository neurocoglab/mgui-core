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

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import mgui.datasources.DataSource;
import mgui.datasources.DataSourceItem;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.io.InterfaceIOOptions;

/**********************************************************************
 * Options for exporting data from a data source to an external file format.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0 
 *
 */
public abstract class ExportDataTableOptions extends InterfaceIOOptions {

	File[] files;
	public ArrayList<DataSourceItem> data_items;
	public DataSource data_source;
	public ArrayList<String> names;
	
	public boolean has_header = true;
		
	@Override
	public File[] getFiles() {
		return files;
	}

	@Override
	public void setFiles(File[] files) {
		this.files = files;
	}
	
	@Override
	public JFileChooser getFileChooser(){
		return getFileChooser(null);
	}
	
	public void setNames(ArrayList<String> names){
		this.names = names;
	}
	
	@Override
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Select file for export");
		return fc;
	}
	
	public void setFrom(ExportDataTableOptions options){
		this.data_items = options.data_items;
		this.data_source = options.data_source;
		this.files = options.files;
		this.names = options.names;
		this.has_header = options.has_header;
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		InterfaceSession.log(this.getClass().getCanonicalName() + ": setObject not implemented.", LoggingType.Warnings);
	}
	

}