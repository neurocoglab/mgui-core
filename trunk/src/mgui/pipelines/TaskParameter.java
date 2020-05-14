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

package mgui.pipelines;

import java.io.Serializable;

import mgui.interfaces.xml.XMLFunctions;

/*******************************************************
 * Specifies a parameter for a given {@linkplain PipelineProcess}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class TaskParameter implements Serializable,
									  Cloneable{

	public boolean optional = false;
	public String name = "";
	public String default_value = "";
	public boolean has_value = true;
	public boolean use_name = true;
	
	public TaskParameter(){
		
	}
	
	public TaskParameter(String name){
		this.name = name;
	}
	
	public TaskParameter(String name, String default_value, boolean optional){
		this(name, default_value, optional, true, true);
	}
	
	public TaskParameter(String name, String default_value, boolean optional, boolean has_value, boolean use_name){
		this.name = name;
		this.default_value = default_value;
		this.optional = optional;
		this.has_value = has_value;
		this.use_name = use_name;
	}
	
	public TaskParameter(TaskParameter param){
		this.name = param.name;
		this.default_value = param.default_value;
		this.optional = param.optional;
		this.has_value = param.has_value;
		this.use_name = param.use_name;
	}
	
	public String getXML(int tab){
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String xml = _tab + "<TaskParameter\n";
		xml = xml + _tab2 + "name = '" + name + "'\n";
		xml = xml + _tab2 + "optional = '" + optional + "'\n";
		xml = xml + _tab2 + "default_value = '" + default_value + "'\n";
		xml = xml + _tab2 + "has_value = '" + has_value + "'\n";
		xml = xml + _tab2 + "use_name = '" + use_name + "'\n";
		xml = xml + _tab + "/>\n";
		
		return xml;
	}
	
	public TaskParameterInstance getInstance(){
		return new TaskParameterInstance(this);
	}
	
	@Override
	public String toString(){
		return "TaskParameter: " + name;
	}
	
	@Override
	public Object clone(){
		return new TaskParameter(this);
	}
	
}