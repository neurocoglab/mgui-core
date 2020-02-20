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

package mgui.datasources;

import java.sql.SQLException;
import java.util.HashMap;

/***************************
 * Class to index specified fields in a table. Provides method to populate an index from a record
 * set, and subsequently perform quick searching of the indexed field. Fields must be unique in
 * order to be indexed.
 * 
 * <p>NOTE: Record indices provided by <code>DataIndex</code> and handled by <code>DataRecordSet</code> 
 * start at zero, whereas JDBC's <code>ResultSet</code> starts at 1. This should be taken into consideration
 * when working directly with <code>ResultSet</code>s.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class DataIndex<T extends Comparable<T>> implements Cloneable{

	String field_name;
	HashMap<T, Integer> index_map;
	
	public DataIndex(String field_name){
		this.field_name = field_name;
	}
	
	/*******************************
	 * Test whether this index is currently populated.
	 * 
	 * @return
	 */
	public boolean isPopulated(){
		return index_map != null;
	}
	
	/*******************************
	 * Unpopulate this index.
	 */
	public void unpopulate(){
		index_map = null;
	}
	
	/*******************************
	 * Populates this index with data from <code>recordset</code>.
	 * 
	 * @param recordset
	 * @throws DataSourceException
	 */
	public void populate(DataRecordSet record_set) throws DataSourceException{
		
		record_set.moveFirst();
		index_map = new HashMap<T, Integer>();
		int field_index = record_set.getFieldIndex(field_name);
		
		try{
			while (!record_set.EOF()){
				index_map.put((T)record_set.getFieldVal(field_index), record_set.getCurrentRecord());
				record_set.moveNext();
				}
		}catch (ClassCastException ex){
			throw new DataSourceException("DataIndex: Class returned by field '" + field_name + "' not compatible with index type.");
		}catch (SQLException ex){
			throw new DataSourceException("DataIndex: SQLException:\n" + ex.getMessage());
			}
		
		return;
	}
	
	/*******************************
	 * Seeks for <code>seek_value</code> in this index. 
	 * 
	 * @param seek_value
	 * @return the index of the record containing <code>seek_value</code>, if found; <code>null</code>
	 * otherwise.
	 * @throws DataSourceException if this index is not currently populated
	 */
	public int seek(T seek_value) throws DataSourceException{
		if (!isPopulated()) throw new DataSourceException("Attempt to access unpopulated data index..");
		Integer index = index_map.get(seek_value);
		if (index == null) return -1;
		return index;
	}
	
	@Override
	public Object clone(){
		return new DataIndex<T>(field_name);
	}
	
}