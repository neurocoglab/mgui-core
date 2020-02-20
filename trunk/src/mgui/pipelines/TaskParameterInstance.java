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

package mgui.pipelines;

import java.io.Serializable;
import java.util.ArrayList;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.projects.InterfaceProject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.util.InterfaceFunctions;
import mgui.interfaces.xml.XMLFunctions;

/*****************************************
 * An instance of a task parameter.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * @see TaskParameter
 *
 */
public class TaskParameterInstance implements InterfaceObject,
											  Serializable{

	TaskParameter parameter;
	public String name;
	public boolean apply = true;
	public String value;
	
	public TaskParameterInstance(TaskParameter p){
		parameter = p;
		this.name = p.name;
		this.value = p.default_value;
		if (p.optional)
			this.apply = false;
	}
	
	public void setFromParamaterInstance(TaskParameterInstance instance){
		
		this.name = instance.name;
		this.parameter = instance.parameter;
		this.apply = instance.apply;
		this.value = instance.value;
		
	}
	
	@Override
	public void clean(){
		InterfaceFunctions.cleanInterfaceObject(this);
	}
	
	public String getXML(int tab){
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<TaskParameterInstance";
		xml = xml + " name = '" + name + "'";
		xml = xml + " value = '" + value + "'";
		xml = xml + " apply = '" + apply + "'";
		xml = xml + " />\n";
		
		return xml;
		
	}
	
	public ArrayList<String> asArgs(){
		return asArgs("", "");
	}
	
	public ArrayList<String> asArgs(String instance, InterfaceProject project){
		ArrayList<String> v = new ArrayList<String>();
		if (parameter.use_name)
			v.add("-" + name);
		if (parameter.has_value && value != null)
			v.addAll(PipelineFunctions.parseArg(value, instance, project));
		return v;
	}
		
		
	public ArrayList<String> asArgs(String instance, String root_dir){
		
		ArrayList<String> v = new ArrayList<String>();
		if (parameter.use_name)
			v.add("-" + name);
		if (parameter.has_value && value != null)
			v.addAll(PipelineFunctions.parseArg(value, instance, root_dir));
		return v;
	}
	
	@Override
	public String toString(){
		return name + ": " + value;
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isDestroyed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getTreeLabel() {
		return name + ": " + value;
	}
	
	public String getPipelineTreeLabel(){
		return getTreeLabel();
		/*
		String label = name + ": "; // getNameLabel(25);
		if (value.contains("{"))
			return label + getHtmlText(value);
		return label + value;
		*/
	}

	String getHtmlText(String text){
		
		text = "<html>" + text + "</html>";
		text = text.replaceAll("\\{", "<strong><font color=blue>{");
		text = text.replaceAll("\\}", "}</font></strong>");
		return text;
		
	}
	
	@Override
	public InterfaceTreeNode issueTreeNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTreeNode(InterfaceTreeNode node) {
		// TODO Auto-generated method stub
		
	}
	
}