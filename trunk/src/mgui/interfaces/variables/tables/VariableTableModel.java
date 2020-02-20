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

package mgui.interfaces.variables.tables;

import java.util.ArrayList;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.tables.InterfaceTableModel;
import mgui.interfaces.variables.VariableException;
import mgui.interfaces.variables.VariableInt;

public class VariableTableModel extends InterfaceTableModel {

	VariableInt<?> variable;
	
	public VariableTableModel(){
		
	}
	
	public VariableTableModel(VariableInt<?> variable){
		setVariable(variable);
	}
	
	public void setVariable(VariableInt<?> variable){
		this.variable = variable;
		//fire listeners?
		
	}
	
	@Override
	public Object getSource(){
		return variable;
	}
	
	public VariableInt<?> getVariable(){
		return variable;
	}
	@Override
	public boolean isCellEditable(int rowIndex, int col) {
		return col != 0 &&variable.isEditable();
	}
	
	@Override
	public int getColumnCount() {
		ArrayList<Integer> dims = variable.getDimensions();
		if (dims.size() == 1) return 1;
		return dims.get(1);
	}
	
	@Override
	public String getColumnName(int c){
		return "" + c;
	}

	@Override
	public int getRowCount() {
		ArrayList<Integer> dims = variable.getDimensions();
		//if (dims.size() < 2) return 1;
		return dims.get(0);
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (variable == null) return "{Err}";
		ArrayList<Integer> indices = new ArrayList<Integer>(2);
		indices.add(row);
		indices.add(col);
		try{
			return variable.getValueAt(indices);
		}catch (VariableException e){
			return "{Err}";
			}
	}
	
	//TODO: implement
	@Override
	public void setValueAt(Object value, int row, int column) {
				
		ArrayList<Integer> indices = new ArrayList<Integer>(2);
		indices.add(column);
		indices.add(row);
		
		try{
			if (!variable.setStringValue(indices, (String)value)){
				InterfaceSession.log("VariableTableModel: Could not set value for '" + variable.getName() + "'.", 
						 			 LoggingType.Errors);
				return;
				}
			
			this.fireTableDataChanged();
			
		}catch(VariableException e){
			InterfaceSession.log("VariableTableModel: Exception while setting value: " + e.getMessage(), 
								 LoggingType.Errors);
			}
		
	}

}