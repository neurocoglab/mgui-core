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
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.ArrayList;

import mgui.datasources.DataField;
import mgui.datasources.DataRecordSet;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataSourceItem;
import mgui.datasources.DataTypes;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.io.InterfaceIOOptions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.NumberFunctions;

/*****************************************************************
 * Writes data from a data source {@link DataItem} to a delimited text file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ExportDataTableTextWriter extends ExportDataTableWriter {

	boolean fail_on_error = false;
	
	@Override
	public boolean writeDataItem(DataSourceItem item, ProgressUpdater progress_bar) throws IOException{
		
		if (dataFile == null) return false;
		
		try{
			ExportDataTableTextOptions options = (ExportDataTableTextOptions)this.options;
			DataSource data_source = options.data_source;
			DataRecordSet record_set = data_source.getRecordSet(item);
			record_set.moveFirst();
			
			BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
			ArrayList<DataField> fields = record_set.getFields();
			
			if (options.has_header){
				for (int i = 0; i < fields.size(); i++){
					if (i > 0)
						writer.write(options.delimiter);
					writer.write(fields.get(i).getName());
					}
				writer.write("\n");
				}
			
			while (!record_set.EOF()){
				for (int i = 0; i < fields.size(); i++){
					if (i > 0)
						writer.write(options.delimiter);
					writer.write(getStringForValue(fields.get(i).getDataType(), fields.get(i).getValue()));
					}
				writer.write("\n");
				record_set.moveNext();
				}
			
			writer.close();
			return true;
			
		}catch (DataSourceException e){
			
			}
		
		return false;
	}
	
}