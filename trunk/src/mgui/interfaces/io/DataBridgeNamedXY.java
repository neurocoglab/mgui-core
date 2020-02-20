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

package mgui.interfaces.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mgui.interfaces.maps.NameMap;
import mgui.numbers.MguiNumber;

public class DataBridgeNamedXY<T extends MguiNumber> extends DataBridgeXY<T>
											  implements DataInputStreamNamedXY<T>,
											  			 DataOutputStreamNamedXY<T>{

	protected NameMap variables = new NameMap();
	
	public DataBridgeNamedXY(int size, List<String> variables){
		this(size, null, variables);
	}
	
	public DataBridgeNamedXY(int size, T initial_value, List<String> variables){
		super(size, initial_value);
		for (int i = 0; i < Math.min(variables.size(), size); i++)
			this.variables.add(i, variables.get(i));
	}
	
	public void setVariables(NameMap vars){
		variables = vars;
	}
	
	public void addChannel(int i, String name){
		int test = variables.get(name);
		if (i < 0) return;
		if (test >= 0) return;
		
		variables.add(i, name);
	}
	
	public void removeChannel(String name){
		variables.remove(name);
	}
	
	public NameMap getVariables(){
		return variables;
	}
	
	@Override
	public void setYData(String name, List<T> data) throws IOException{
		Integer i = variables.get(name);
		if (i == null)
			throw new IOException("Invalid name index.");
		super.setYData(i, data);
	}
	
	@Override
	public List<T> getYData(String name) throws IOException{
		Integer i = variables.get(name);
		if (i == null)
			throw new IOException("Invalid name index.");
		return getYData(i);
	}
	
	public List<String> getVariableNames() {
		return new ArrayList<String>(variables.getNames());
	}

	
	
}