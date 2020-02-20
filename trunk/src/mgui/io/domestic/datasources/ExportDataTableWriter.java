package mgui.io.domestic.datasources;

import java.io.File;
import java.io.IOException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.ArrayList;

import mgui.datasources.DataSourceItem;
import mgui.datasources.DataTypes;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.NumberFunctions;

/********************************************************************
 * Abstract class for extension by writers which export data from a {@linkplain DataSourceItem} to an external
 * format.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class ExportDataTableWriter extends FileWriter {

	/*****************************************************************
	 * Writes a {@code DataSourceItem} to an external format, using the current settings of this writer.
	 * 
	 * @param item
	 * @param progress_bar
	 * @return
	 */
	public abstract boolean writeDataItem(DataSourceItem item, ProgressUpdater progress_bar) throws IOException;
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		this.options = options;
		ExportDataTableOptions _options = (ExportDataTableOptions)options;
		
		ArrayList<DataSourceItem> items = _options.data_items;
		
		File[] files = _options.getFiles();
		
		boolean success = true;
		
		if (progress_bar != null){
			progress_bar.setMinimum(0);
			progress_bar.setMaximum(files.length);
			progress_bar.update(0);
			}
		
		for (int i = 0; i < files.length; i++){
			this.dataFile = files[i];
			
			try{
				success &= writeDataItem(items.get(i), progress_bar);
				if (progress_bar != null){
					progress_bar.update(i);
					}
			}catch (IOException ex){
				InterfaceSession.log(getClass().getName() + ": Error reading file '" + dataFile.getAbsolutePath() + "':" +
									 ex.getMessage(),
									 LoggingType.Errors);
				success = false;
				}
			
			}
		
		return success;
		
	}
	
	/*************************************************
	 * Returns a String corresponding to {@code value}. If {@code value} is numeric, converts it to a
	 * formatted number based on {@code options.precision}. Otherwise just returns its String representation
	 * as returned from the {@code toString()} method.
	 * 
	 * @param datatype
	 * @param value
	 * @return
	 */
	protected String getStringForValue(int datatype, Object value){
		
		if (value == null) return "";
		if (!DataTypes.isNumeric(datatype)) return value.toString();
		
		ExportDataTableTextOptions options = (ExportDataTableTextOptions)this.options;
		DecimalFormat df = NumberFunctions.getDecimalFormat(options.precision);
		
		switch (datatype){
			case Types.INTEGER: 
				return ((MguiInteger)value).toString("0");
			case Types.FLOAT: 
				return ((MguiFloat)value).toString(df.toPattern());
			case Types.DOUBLE: 
				return ((MguiDouble)value).toString(df.toPattern());
			}
		
		return value.toString();
		
	}

}
