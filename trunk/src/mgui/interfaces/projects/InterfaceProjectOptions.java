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

package mgui.interfaces.projects;

import java.util.ArrayList;

import mgui.interfaces.InterfaceOptions;


public class InterfaceProjectOptions extends InterfaceOptions {

	public InterfaceProject project;
	
	public ArrayList<ProjectInstance> instances;
	public ArrayList<ProjectDirectory> subdirs;
	public String instance_prefix = "";
	public String instance_suffix = "";
	
	public String icon_type = "Instances";
	
	//public ArrayList<String> subfolders;
	
	public InterfaceProjectOptions(){
		
	}
	
	public InterfaceProjectOptions(InterfaceProject project){
		this.project = project;
	}
	
}