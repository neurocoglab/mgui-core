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

package mgui.interfaces.attributes.table;

import javax.swing.table.AbstractTableModel;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;

/************************************************************
 * Table model designed to display updatable {@code Attribute}s.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class AttributeTableModel extends AbstractTableModel {

	public AttributeList attributes;
	public boolean isEditable;
	
	public AttributeTableModel(){
		super();
		attributes = new AttributeList();
		isEditable = true;
	}
	
	public AttributeTableModel(AttributeList thisList){
		super();
		attributes = thisList;
		isEditable = true;
	}
	
	public void setAttributes(AttributeList list){
		this.attributes = list;
		this.fireTableStructureChanged();
	}
	
	public int getColumnCount() {
		return 2;
	}

	public int getRowCount() {
		if (attributes == null) return 0;
		return attributes.getSize();
	}

	public Object getValueAt(int row, int col) {
		if (col == 0)
			return getAttribute(row).getName();
		if (col == 1)
			return getAttribute(row);
		return null;
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		return (col == 1 && isEditable);
	}
	
	@Override
	public String getColumnName(int col){
		if (col == 0) return "Name";
		if (col == 1) return "Value";
		return "";
	}
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		if (col != 1 || !isEditable || value == null) return;
		Attribute<?> attribute = getAttribute(row);
		attribute.setValue(((Attribute<?>)value).getValue());
		this.fireTableDataChanged();
	}
	
	public Attribute<?> getAttribute(int row){
		return attributes.getAttribute(row);
	}
	
}