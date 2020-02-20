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

package mgui.interfaces.maps;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JTable;


public class NameMapTable extends JTable {

	TableModel model;
	
	public NameMapTable(){
		
	}
	
	public NameMapTable(NameMap map){
		setModel(new TableModel(map));
	}
	
	public void setNameMap(NameMap map){
		if (model == null)
			setModel(new TableModel(map));
		else
			model.setMap(map);
	}
	
	class TableModel extends javax.swing.table.AbstractTableModel {
	
		NameMap map;
		ArrayList<Integer> indices;
		
		public TableModel(NameMap map){
			setMap(map);
		}
		
		public void setMap(NameMap map){
			this.map = map;
			indices = map.getIndices();
			Collections.sort(indices);
		}
		
		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			return map.getSize();
		}
		
		@Override
		public boolean isCellEditable(int row, int col){
			return true;
		}

		public Object getValueAt(int row, int col) {
			
			if (row >= map.getSize()) return null;
			
			switch(col){
				case 0:
					return indices.get(row);
				case 1:
					return map.get(indices.get(row));
				}
			
			return null;
		}
		
		@Override
		public void setValueAt(Object o, int row, int col) {
			
			Integer i,n;
			String name;
			
			switch(col){
				
				case 0:
					//index changed
					i = indices.get(row);
					n = Integer.valueOf((String)o);
					//if value hasn't changed or new value already in map, do nothing
					if (i.equals(n) || map.contains(n)) return;
					name = map.get(i);
					map.remove(i);
					map.add(n, name);
					setMap(map);
					this.fireTableDataChanged();
					return;
					
				case 1:
					//name changed
					i = indices.get(row);
					name = (String)o;
					//no zero-length names or duplicates
					if (map.contains(name) || name.length() == 0) return;
					map.set(i, name);
					setMap(map);
					this.fireTableDataChanged();
					return;
					
				}
		}
		
		@Override
		public String getColumnName(int col){
			switch (col){
				case 0:
					return "Index";
				case 1:
					return "Name";
				}
			return "?";
		}
		
		
	}
	
}