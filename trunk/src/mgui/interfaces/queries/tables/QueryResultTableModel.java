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

package mgui.interfaces.queries.tables;

import javax.swing.table.AbstractTableModel;

import mgui.interfaces.queries.QueryEvent;
import mgui.interfaces.queries.QueryListener;
import mgui.interfaces.queries.QueryResult;

/************************************************
 * Table model for {@link QueryResultTable}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class QueryResultTableModel extends AbstractTableModel implements QueryListener {

	QueryResult result;
	
	public QueryResultTableModel(QueryResult result){
		setResult(result);
	}
	
	public void setResult(QueryResult result){
		this.result = result;
		this.fireTableDataChanged();
	}
	
	@Override
	public void objectQueried(QueryEvent e) {
		this.fireTableDataChanged();
	}
	
	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public int getRowCount() {
		if (result == null) return 0;
		return result.getProperties().size();
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		switch (column){
			case 0:
				return result.getProperties().get(row);
			case 1:
				return result.getValue(result.getProperties().get(row));
			}
		return "?";
	}
	
	@Override
	public String getColumnName(int column) {
		switch (column){
			case 0: return "Item";
			case 1: return "Value";
			}
		
		return "?";
	}
		
	
}