package mgui.io.domestic.datasources;

import java.io.IOException;
import java.util.ArrayList;

import mgui.datasources.DataField;
import mgui.datasources.DataSource;
import mgui.interfaces.ProgressUpdater;
import mgui.io.FileLoader;

/******************************************************************
 * Imports data from an external format into a new {@code DataSource} table. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class ImportDataTableLoader extends FileLoader {

	protected DataSource data_source;

	/************************************************
	 * Sets the current {@linkplain DataSource} for this loader.
	 * 
	 * @param source
	 */
	public void setDataSource(DataSource source){
		this.data_source = source;
	}
	
	
	/*********************************************************************
	 * Loads data from the current data file into a new data table, which will be created in {@code data_source}.
	 * Input file and data source must already have been set.
	 * 
	 * @param data_source		Data source to contain the new data table
	 * @param fields			Fields defining the import of data and the new table
	 * @param include			Specifies which fields to include in the new table
	 * @param progress_bar		Optional progress updater 
	 * @return Success of the import
	 */
	public abstract boolean loadDataTable(final String name, 
										  final ArrayList<DataField> fields, 
										  final ArrayList<Boolean> include,
										  final ProgressUpdater progress_bar) throws IOException;
	
}
