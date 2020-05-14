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

package mgui.interfaces.plots;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.variables.VariableEvent;
import mgui.interfaces.variables.VariableInt;
import mgui.interfaces.variables.VariableListener;
import mgui.numbers.MguiNumber;

/******************************************************************
 * XY data source which uses two {@link VariableInt} object as its X and Y sources.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VariablePlotXYDataSource<T extends MguiNumber> extends PlotXYDataSource<T> implements VariableListener {

	HashMap<String,VariableInt<T>> x_variables = new HashMap<String,VariableInt<T>>();
	HashMap<String,VariableInt<T>> y_variables = new HashMap<String,VariableInt<T>>();
	HashMap<String,String> x_parts = new HashMap<String,String>();
	HashMap<String,String> y_parts = new HashMap<String,String>();
	
	
	public VariablePlotXYDataSource(){
	}
	
	
	/**************************************************
	 * Adds an X-Y variable pair, as strings of the form: 
	 * 
	 * <p><code>{variable='[var_name]' part='[part_string]'},
	 * 
	 * <p>where [part_string] has the syntax 
	 * "<code>x_start,y_start,..,n_start:x_start,y_start,..,n_start</code>".
	 * E.g., "<code>1,2:20,2</code>". Use asterisk, "*" to indicate last element in a dimension.
	 * E.g,, "<code>1,2:*,*</code>".
	 * 
	 * @see VariableInt
	 * @param var_string
	 */
	public void addXYPair(String var_string_x, String var_string_y, String key) throws IOException{
		
		String part = var_string_x.substring(var_string_x.indexOf("='") + 2);
		String var = part.substring(0, part.indexOf("' "));
		part = part.substring(part.indexOf("='") + 2);
		part = part.substring(0, part.indexOf("'"));
		
		VariableInt<T> variable = getWorkspaceVariable(var, part);
		//if (var_count < 1 && size < 0) size = variable.getSize();
		if (variable == null)
			throw new IOException("VariablePloyXYDataSource: Variable '" + var_string_y + " returns null.");
		if (x_variables.containsKey(key)){
			x_variables.get(key).removeListener(this);
			}
	
		x_variables.put(key, variable);
		variable.addListener(this);
		
		setXPart(key, part);
		
		part = var_string_y.substring(var_string_y.indexOf("='") + 2);
		var = part.substring(0, part.indexOf("' "));
		part = part.substring(part.indexOf("='") + 2);
		part = part.substring(0, part.indexOf("'"));
		
		variable = getWorkspaceVariable(var, part);
		
		if (variable == null)
			throw new IOException("VariablePloyXYDataSource: Variable '" + var_string_y + " returns null.");
		if (y_variables.containsKey(key)){
			y_variables.get(key).removeListener(this);
			}
	
		y_variables.put(key, variable);
		variable.addListener(this);
		
		setYPart(key, part);
		var_count++;
	}
	
	protected void setXPart(String name, String part) throws IOException{
		VariableInt<T> variable = x_variables.get(name);
		if (variable == null) return;
		if (!xy_data.containsKey(name))
			xy_data.put(name, null);
		if (part == null || part.length() > 0){
			VariableInt<T> var_part = variable.getPart(part);
			if (var_part != null){
				x_parts.put(name, part);
				setX(name, var_part.getAsList());
				}
		}else{
			x_parts.put(name, null);
			setX(name, variable.getAsList());
			}
	}
	
	protected void setYPart(String name, String part) throws IOException{
		VariableInt<T> variable = y_variables.get(name);
		if (variable == null) return;
		if (!xy_data.containsKey(name))
			xy_data.put(name, null);
		if (part == null || part.length() > 0){
			VariableInt<T> var_part = variable.getPart(part);
			if (var_part != null){
				y_parts.put(name, part);
				setY(name, var_part.getAsList());
				}
		}else{
			y_parts.put(name, null);
			setY(name, variable.getAsList());
			}
	}
	
	public ArrayList<String> getChannelNames(){
		ArrayList<String> names = new ArrayList<String>(x_variables.keySet());
		Collections.sort(names);
		return names;
	}
	
	private VariableInt<T> getWorkspaceVariable(String name, String part) throws IOException{
		VariableInt<T> variable = null;
		
		try{
			variable = (VariableInt<T>)InterfaceSession.getWorkspace().getVariableByName(name);
		}catch (ClassCastException e){
			throw new IOException("Invalid type for variable '" + name + "'.");
			}
		
		if (variable == null)
			throw new IOException("No such variable: " + name);
		
		return variable;
		
	}
	
	/*********************************
	 * Resets this data source.
	 * 
	 */
	public void reset() throws IOException{
		
		// update values from variables
		
		if (xy_data != null)
			xy_data.clear();
		else
			xy_data = new HashMap<String, List<XYData<T>>>();
		ArrayList<String> channels = this.getChannelNames();
		ArrayList<XYData<T>> values;
		for (int i = 0; i < channels.size(); i++){
			String channel = channels.get(i);
			values = null;
			VariableInt<T> x_variable = x_variables.get(channel);
			VariableInt<T> y_variable = y_variables.get(channel);
			String x_part = x_parts.get(channel);
			String y_part = y_parts.get(channel);
			if (x_part != null && y_part != null){
				VariableInt<T> v_x = x_variable.getPart(x_part);
				if (v_x == null)
					throw new IOException("Invalid variable part: '" + x_part + "'.");
				VariableInt<T> v_y = y_variable.getPart(y_part);
				if (v_y == null)
					throw new IOException("Invalid variable part: '" + y_part + "'.");
				ArrayList<T> x_values = v_x.getAsList();
				ArrayList<T> y_values = v_y.getAsList();
				values = new ArrayList<XYData<T>>(x_values.size());
				for (int j = 0; j < x_values.size(); j++){
					values.add(new XYData<T>(x_values.get(j), y_values.get(j)));
					}
			}else{
				ArrayList<T> x_values = x_variable.getAsList();
				ArrayList<T> y_values = y_variable.getAsList();
				values = new ArrayList<XYData<T>>(x_values.size());
				for (int j = 0; j < x_values.size(); j++){
					values.add(new XYData<T>(x_values.get(j), y_values.get(j)));
					}
				}
			xy_data.put(channel, values);
			}
		
		super.reset();
	}
	
	@Override
	public void variableValuesUpdated(VariableEvent e) {

		VariableInt<T> variable = (VariableInt<T>)e.getSource();
		
		try{
			if (x_variables.containsKey(variable.getName())){
				setXPart(variable.getName(), this.x_parts.get(variable.getName()));
				return;
				}
			
			if (y_variables.containsKey(variable.getName())){
				setYPart(variable.getName(), this.y_parts.get(variable.getName()));
				return;
				}
		}catch (IOException ex){
			InterfaceSession.log("VariableXYSeriesDataSource: IOException encountered: " + ex.getMessage(), 
								 LoggingType.Errors);
			}
		
	}

}