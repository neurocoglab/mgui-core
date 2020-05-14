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

package mgui.interfaces.datasources;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import mgui.datasources.DataRecordSet;
import mgui.datasources.DataSource;
import mgui.datasources.DataSourceException;
import mgui.datasources.DataSourceItem;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.tables.InterfaceTableModel;
import mgui.util.arCollections;
import foxtrot.Job;
import foxtrot.Worker;

/***************************
 * Class extending AbstractTableModel to act as a model for JTable, and in particular
 * for use by InterfaceDataTable. Thus acts as a bridge between a DataRecordSet and an
 * InterfaceDataTable object.
 * 
 * TODO: Allow table sort only if cached; otherwise use DB sort
 * TODO: Sort table (not query) columns alpha-numerically
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */

public class DataTableModel extends InterfaceTableModel {

	protected DataSourceItem ds_item;
	protected DataRecordSet recordSet;
	public boolean isFiltered;
	public ArrayList<Integer> filter;
	protected boolean is_cached = false;			//caches data in memory to facilitate table rendering
	protected volatile ArrayList<ArrayList<Object>> cache = new ArrayList<ArrayList<Object>>();
	ProgressUpdater progress_updater;
	protected volatile boolean is_thread_caching = false;
	protected boolean is_new_record = false;
	
	public DataTableModel(){
		
	}
	
	public DataTableModel(DataSourceItem item) throws DataSourceException{
		setDataSourceItem(item);
	}
	
	public DataTableModel(DataSource ds, DataSourceItem item) throws DataSourceException{
		ds_item = item;
		recordSet = new DataRecordSet(ds);
		(recordSet).set(item);
	}
	
	public DataTableModel(DataRecordSet r){
		recordSet = r;
	}
	
	public DataRecordSet getRecordSet(){
		return recordSet;
	}
	
	@Override
	public Object getSource(){
		return ds_item;
	}
	
	public void setDataSourceItem(DataSourceItem item) throws DataSourceException{
		if (item.getDataSource() != null){
			recordSet = new DataRecordSet(item.getDataSource());
			(recordSet).set(item);
			}
	}
	
	public void setProgressUpdater(ProgressUpdater updater){
		if (is_thread_caching) return;
		progress_updater = updater;
	}
	
	@SuppressWarnings("unchecked")
	public boolean applyFilter(ArrayList<? extends Comparable> list, String filter_field) throws DataSourceException{
		if (is_thread_caching) return false;
		isFiltered = false;
		if (list == null) throw new DataSourceException("Null filter list.");
		
		if (recordSet.getField(filter_field) == null) throw new DataSourceException("Filter field not in record set.");
		
		//sort
		Collections.sort(list);
		
		//fill list
		if (!recordSet.moveFirst()) return false;
		isFiltered = true;
		filter = new ArrayList<Integer>();
		
		int i = 0;
		while (!recordSet.EOF()){
			try{
				Comparable c = (Comparable)recordSet.getFieldVal(filter_field);
				int index = arCollections.binarySearch(list, c);
				if (index >= 0)
					filter.add(new Integer(i)); 
				i++;
			}catch (SQLException ex){
				throw new DataSourceException("DataIndex: SQLException:\n" + ex.getMessage());
				}
			recordSet.moveNext();
			}
		
		return isFiltered;
	}
	
	/*************************************************
	 * Adds a new record to the model and allows the user to enter input into it. The record is not actually
	 * appended to the data source until {@code updateRecordSet} is called. 
	 * 
	 */
	public boolean addNewRecord(){
		this.is_new_record = true;
		//int count = recordSet.getRecordCount();
		boolean ok = recordSet.addNew();
		//this.fireTableRowsInserted(count, count);
		InterfaceSession.log("DataTableModel: Adding new record...", LoggingType.Debug);
		return ok;
	}
	
	/***********************************
	 * Instructs the record set to update its values
	 * 
	 * @return
	 */
	public boolean updateRecordSet(){
		if (!this.is_new_record) return false;
		is_new_record = false;
		if (!recordSet.update()) return false;
		recordSet.moveLast();
		int count = recordSet.getRecordCount() - 1;
		fireTableRowsInserted(count, count);
		return true;
	}
	
	/************************************
	 * Cancels the current update
	 * 
	 * @return
	 */
	public boolean cancelUpdate(){
		if (!this.is_new_record) return false;
		is_new_record = false;
		return recordSet.cancelUpdate();
	}
	
	public void removeFilter(){
		if (is_thread_caching) return;
		isFiltered = false;
		filter = null;
	}
	
	public void setIsCached(boolean b){
		if (is_thread_caching) return;
		is_cached = b;
		resetCache();
	}
	
	public boolean getIsCached(){
		return is_cached;
	}
	
	protected void resetCache(){
		if (is_thread_caching) return;
		cache = new ArrayList<ArrayList<Object>>();
	}
	
	public int getColumnCount() {
		if (recordSet == null) return 0;
		return recordSet.getFieldCount();
	}

	public int getRowCount() {
		if (recordSet == null || recordSet.isClosed()) return -1;
		if (isFiltered) return filter.size();
		//if (is_cached) return cache.size();
		if (recordSet.getRecordCount() <= 0) 
			recordSet.moveLast();
		int count = recordSet.getRecordCount();
		if (this.is_new_record) 
			count++;
		return count;
	}
	
	protected void fillCache(final int row) throws DataSourceException{
		if (is_thread_caching) return;
		if (recordSet == null || recordSet.getRecordCount() <= 0) return;
		final int start = cache.size();
		
		if (progress_updater != null){
			is_thread_caching = true;
			if (progress_updater.isCancelled()){
				is_thread_caching = false;
				return;
				}
			if (progress_updater instanceof InterfaceProgressBar){
				((InterfaceProgressBar) progress_updater).setMessage("Caching recordset: ");
				((InterfaceProgressBar) progress_updater).register();
				}
			progress_updater.setMinimum(start);
			progress_updater.setMaximum(row);
			progress_updater.update(start);
			
			Worker.post(new Job(){
				@Override
				public Object run(){
					try{
						fillCacheBlocking(row, start);
					}catch (DataSourceException ex){
						InterfaceSession.log("DataTableModel: Exception filling cache: " + ex.getMessage(), LoggingType.Errors);
						}
					return null;
					}
				});
			
			if (progress_updater instanceof InterfaceProgressBar)
				((InterfaceProgressBar) progress_updater).deregister();
			is_thread_caching = false;
			return;
			}
		
		fillCacheBlocking(row, start);
	}
	
	synchronized protected void fillCacheBlocking(int row, int start) throws DataSourceException{
		int cols = getColumnCount();
		//add data to cache
		for (int i = start; i <= row; i++){
			ArrayList<Object> record = new ArrayList<Object>(cols);
			if (progress_updater != null){
				if (progress_updater.isCancelled()) return;
				progress_updater.update(i);
				}
			try{
				for (int j = 0; j < cols; j++)
					record.add(recordSet.getValueAt(i, j));
				cache.add(record);
			}catch (SQLException ex){
				throw new DataSourceException("DataIndex: SQLException:\n" + ex.getMessage());
				}
			}
	}
	
	protected Object getCached(int row, int col) throws SQLException, DataSourceException{
		if (row >= cache.size())
			fillCache(row);
		
		//if a separate thread is currently caching the record set, get data directly from source
		if (is_thread_caching)
			return recordSet.getValueAt(row, col);
			
		if (row >= cache.size())
			return recordSet.getValueAt(row, col);
		
		return cache.get(row).get(col);
	}
	
	public void prefillCache() throws DataSourceException{
		fillCache(getRowCount());
	}
	
	public Object getValueAt(int row, int col) {
		if (recordSet == null) return null;
		
		if (this.is_new_record && row == getRowCount() - 1){
			//InterfaceSession.log("DataTableModel: getting value for new record at " + row + ", " + col, LoggingType.Debug);
			return getValueFromRecordSetAt(row, col);
			}
		
		if (isFiltered)
			return getValueFromRecordSetAt(filter.get(row).intValue(), col);
		
		if (is_cached && !is_thread_caching)
			try{
				return getCached(row, col);
			}catch (Exception ex){
				InterfaceSession.log("DataTableModel: Error getting data from [" + row + ", " + col + "]:\n" + ex.getMessage());
				}
			
		//return getValueFromRecordSetAt(filter.get(row).intValue(), col);
		Object obj = getValueFromRecordSetAt(row, col);
		return obj;
		
	}
	
	protected Object getValueFromRecordSetAt(int row, int col){
		try{
			return recordSet.getValueAt(row, col);
		}catch (SQLException ex){
			InterfaceSession.log("DataTableModel: Error getting data from [" + row + ", " + col + "]:\n" + ex.getMessage());
			}
		return null;
	}
	
	@Override
	public void setValueAt(Object value, int row, int column) {
		if (recordSet == null) return;

		if (this.is_new_record){
			if (row == getRowCount() - 1){
				recordSet.updateField(recordSet.getField(column).getName(), value);
				if (updateRecordSet()){
					this.is_new_record = false;
					InterfaceSession.log("DataTableModel: Updated new record.", LoggingType.Debug);
					fireTableDataChanged();
					}
				return;
			}else{
				if (!updateRecordSet())
					recordSet.cancelUpdate();
				this.is_new_record = false;
				fireTableDataChanged();
				return;
				}
		}
		
		boolean success = true;
		if (!isFiltered)
			success = recordSet.setValueAt(row, column, value);
		else
			success = recordSet.setValueAt(filter.get(row).intValue(), column, value);
		if (!success){
			InterfaceSession.log("Could not update data table at " + row + ", " + column, LoggingType.Errors);
			return;
			}
		if (is_cached)
			setCacheValue(value, row, column);
	}
	
	//update cache after record set update
	protected void setCacheValue(Object value, int row, int column){
		if (cache.size() <= row) return;
		cache.get(row).set(column, value);
	}

	//return the label for this column
	@Override
	public String getColumnName(int c){
		return recordSet.getField(c).getName();
	}
	
	@Override
	public Class getColumnClass(int col){
		return recordSet.getField(col).getObjectClass();
	}
	
	@Override
	public boolean isCellEditable(int row, int col){
		if (!recordSet.isUpdatable()) return false;
		return recordSet.getField(col).isEditable();
	}
	
	/***********************************************
	 * Attempts to delete the specified rows from the underlying data source item
	 * 
	 * @param rows
	 * @return
	 */
	public boolean deleteRows(int[] rows){
		
		if (!recordSet.isUpdatable()) return false;
		
		try{
			if (!recordSet.deleteRecords(rows)) return false;
			recordSet.moveLast();
			this.fireTableDataChanged();
			return true;
		}catch (DataSourceException ex){
			InterfaceSession.log(ex.getMessage(), LoggingType.Errors);
			}
		
		return false;
	}
	
}