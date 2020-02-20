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

package mgui.interfaces.plots;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.variables.VariableEvent;
import mgui.interfaces.variables.VariableInt;
import mgui.interfaces.variables.VariableListener;
import mgui.numbers.MguiNumber;

/********************************************************
 * An XY data source with a {@link VariableInt} as its source. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VariablePlotTimeSeriesDataSource<T extends MguiNumber> extends PlotTimeSeriesDataSource<T>
															implements VariableListener{

	String x_part;
	VariableInt<T> x_variable;
	HashMap<String,VariableInt<T>> y_variables = new HashMap<String,VariableInt<T>>();
	HashMap<String,String> y_parts = new HashMap<String,String>();
	//HashMap<String,ArrayList<T>> y_values = new HashMap<String,ArrayList<T>>();
	
	public VariablePlotTimeSeriesDataSource(){
	}
	
	
	/**************************************************
	 * Set the X variable, as a string of the form: 
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
	public void setXVariable(String var_string) throws IOException{
		
		if (x_variable != null){
			x_variable.removeListener(this);
			}
		
		String part = var_string.substring(var_string.indexOf("='") + 2);
		String var = part.substring(0, part.indexOf("' "));
		part = part.substring(part.indexOf("='") + 2);
		part = part.substring(0, part.indexOf("'"));
		
		x_variable = getWorkspaceVariable(var, part);
		x_variable.addListener(this);
		
		setXPart(part);
		
	}
	
	@Override
	public void setX(ArrayList<T> x){
		this.x_value = x;
		fireReset();
		fireEmission(x.size());
	}
	
	protected void setXPart(String part){
		if (x_variable == null) return;
		if (part == null || part.length() > 0){
			VariableInt<T> var_part = x_variable.getPart(part);
			if (var_part != null){
				//this.size = var_part.getSize();
				setX(var_part.getAsList());
				x_part = part;
				}
		}else{
			x_part = null;
			//size = x_variable.getSize();
			setX(x_variable.getAsList());
			}
	}
	
	/**************************************************
	 * Adds a Y variable, as a string of the form: 
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
	public void addYVariable(String var_string) throws IOException{
		
		String part = var_string.substring(var_string.indexOf("='") + 2);
		String var = part.substring(0, part.indexOf("' "));
		part = part.substring(part.indexOf("='") + 2);
		part = part.substring(0, part.indexOf("'"));
		
		VariableInt<T> variable = getWorkspaceVariable(var, part);
		if (y_variables.containsKey(var)){
			y_variables.get(var).removeListener(this);
			}
	
		y_variables.put(var, variable);
		variable.addListener(this);
		
		setYPart(var, part);
		
	}
	
	protected void setYPart(String name, String part) throws IOException{
		VariableInt<T> variable = y_variables.get(name);
		if (variable == null) return;
		if (!y_values.containsKey(name))
			y_values.put(name, null);
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
	
	public ArrayList<String> getChannelNames(){
		return new ArrayList<String>(y_variables.keySet());
	}
	
	/*********************************
	 * Resets this data source.
	 * 
	 */
	public void reset() throws IOException{
		
		// update values from variables
		
		// X
		if (x_value != null)
			x_value.clear();
		else
			x_value = new ArrayList<T>();
		
		ArrayList<T> values = null;
		if (x_part != null){
			VariableInt<T> v = x_variable.getPart(x_part);
			if (v == null)
				throw new IOException("Invalid variable part: '" + x_part + "'.");
			values = v.getAsList();
		}else{
			values = x_variable.getAsList();
			}
		
		x_value.addAll(values);
		
		// Y
		if (y_values != null)
			y_values.clear();
		else
			y_values = new HashMap<String, List<T>>();
		ArrayList<String> channels = this.getChannelNames();
		for (int i = 0; i < channels.size(); i++){
			String channel = channels.get(i);
			values = null;
			VariableInt<T> y_variable = y_variables.get(channel);
			String y_part = y_parts.get(channel);
			if (y_part != null){
				VariableInt<T> v = y_variable.getPart(y_part);
				if (v == null)
					throw new IOException("Invalid variable part: '" + y_part + "'.");
				values = v.getAsList();
			}else{
				values = y_variable.getAsList();
				}
			y_values.put(channel, values);
			}
		
		super.reset();
	}
	
	@Override
	public void variableValuesUpdated(VariableEvent e) {

		VariableInt<T> variable = (VariableInt<T>)e.getSource();
		
		if (variable.equals(x_variable)){
			setXPart(this.x_part);
			return;
			}
		
		try{
			if (y_variables.containsKey(variable.getName())){
				setYPart(variable.getName(), this.y_parts.get(variable.getName()));
				return;
				}
		}catch (IOException ex){
			InterfaceSession.log("VariablePlotTimeSeriesDataSource: IOException encountered: " + ex.getMessage(), 
								 LoggingType.Errors);
			}
		
	}
	
	
}