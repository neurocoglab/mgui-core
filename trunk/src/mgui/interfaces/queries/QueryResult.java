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

package mgui.interfaces.queries;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;

/********************************************
 * Stores a set of results for a specific query.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class QueryResult{

	protected InterfaceQueryObject object;
	protected HashMap<String, Object> values = new HashMap<String, Object>();
	protected ArrayList<String> sorted_properties = new ArrayList<String>();
	
	public QueryResult(InterfaceQueryObject object){
		this.object = object;
	}
	
	/*************************************
	 * Returns the queriable object associated with this result
	 * 
	 * @return
	 */
	public InterfaceQueryObject getObject(){
		return object;
	}
	
	/*************************************
	 * Adds the specified property to this result. If it is already in the list, removes it and
	 * places it at the end of the list. 
	 * 
	 * @param property
	 * @param value
	 */
	public void addValue(String property, Object value){
		Object old_value = values.remove(property);
		values.put(property, value);
		if (old_value != null)
			sorted_properties.remove(property);
		sorted_properties.add(property);
	}
	
	/************************************
	 * Returns the value of the specified property
	 * 
	 * @param property
	 * @return
	 */
	public Object getValue(String property){
		return values.get(property);
	}
	
	/*************************************
	 * Returns a list of the properties stored in this result, in the same order as they were added 
	 * 
	 * @return
	 */
	public ArrayList<String> getProperties(){
		return sorted_properties;
	}
	
	public DefaultMutableTreeNode getTreeNode(){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(this.getObject());
		node.add(new DefaultMutableTreeNode(this));
		return node;
	}
	
}