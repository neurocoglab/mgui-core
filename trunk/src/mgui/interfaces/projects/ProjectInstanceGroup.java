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

import mgui.interfaces.AbstractInterfaceObject;

/*****************************************
 * Represents a group partition in a project, which refers to a set of subdirectories within the 
 * instances directory, each representing a different level of the group. As an example, if a clinical
 * trial is examining differences between patients and controls, this can be organized by specifying an
 * instance group named "Experimental Group", having two levels: "Patient" and "Control", mapping to two
 * directories in the instances directory called "patient" and "control".
 * 
 * <p>An arbitrary number of groups can be specified, sorted by their depth in the directory tree. For
 * instance, to subdivide each experimental group by gender, a second group called "Gender" can be created,
 * with the levels "Male" and "Female". This will create two subdirectories "male" and "female" within
 * each "Experimental Group" directory. Thus:
 * 
 * <p>-> instance_dir
 * <br>----> patient
 * <br>---------> male
 * <br>---------> female
 * <br>----> control
 * <br>---------> male
 * <br>---------> female
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ProjectInstanceGroup extends AbstractInterfaceObject 
								  implements Comparable<ProjectInstanceGroup> {

	protected ArrayList<String> levels = new ArrayList<String>();
	
	/******************************************
	 * Constructs a new instance group, and a list of levels.
	 * 
	 * @param name
	 * @param levels
	 */
	public ProjectInstanceGroup(String name, ArrayList<String> levels){
		setName(name);
		this.levels = levels;
	}
	
	public ArrayList<String> getLevels(){
		return new ArrayList<String>(levels);
	}
	
	public void setLevels(ArrayList<String> levels){
		this.levels = levels;
	}
	
	@Override
	public int compareTo(ProjectInstanceGroup group) {
		return getName().compareTo(group.getName());
	}

	
}