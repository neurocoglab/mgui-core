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

package mgui.interfaces.queries;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.util.InterfaceFunctions;
import mgui.resources.icons.IconObject;

/*****************************
 * Interface for a class which allows it to query a given {@link InterfaceQueryObject}. Queries are
 * classes which obtain information about an object and store it as a set of {@link QueryResult}s.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public abstract class InterfaceQuery implements InterfaceObject,
												IconObject{

	protected HashMap<InterfaceQueryObject, QueryResult> results = new HashMap<InterfaceQueryObject, QueryResult>();
	protected ArrayList<InterfaceQueryObject> sorted_objects = new ArrayList<InterfaceQueryObject>();
	protected ArrayList<QueryListener> listeners = new ArrayList<QueryListener>();
	protected String name;
	
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/query_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/query_20.png");
		return null;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void clean(){
		InterfaceFunctions.cleanInterfaceObject(this);
	}
	
	/****************************
	 * Returns the result associated with <code>object</code>, if it exists; returns <code>null</code>
	 * otherwise.
	 * 
	 * @param object
	 * @return
	 */
	public Object getResult(String object){
		return results.get(object);
		
	}
	
	/****************************
	 * Returns a list of the objects for which this query has results, in the order they
	 * were added.
	 * 
	 * @return
	 */
	public ArrayList<InterfaceQueryObject> getObjects(){
		return sorted_objects;
	}
	
	/****************************
	 * Returns the results stored in this query, in the order they were added.
	 * 
	 * @return
	 */
	public ArrayList<QueryResult> getResults(){
		ArrayList<QueryResult> sorted_results = new ArrayList<QueryResult>();
		for (int i = 0; i < sorted_objects.size(); i++)
			sorted_results.add(results.get(sorted_objects.get(i)));
		return sorted_results;
	}
	
	/****************************
	 * Clears the results currently stored in this query.
	 */
	public void clearResults(){
		results.clear();
		sorted_objects.clear();
	}
	
	/****************************
	 * Determines whether this query instance can query the given {@link InterfaceQueryObject}.
	 * 
	 * @param object
	 * @return
	 */
	public abstract boolean canQuery(InterfaceQueryObject object);
	
	/****************************
	 * Performs a query on the specified query object. Adds a new result to the result list.
	 * 
	 * @param object
	 * @throws ObjectNotQueriableException
	 */
	public abstract void query(InterfaceQueryObject object) throws ObjectNotQueriableException;
	
	/****************************
	 * Returns a list of the property names for this query, based on the last call to {@link query()}.
	 * 
	 * @return
	 */
	
	/****************************
	 * Adds a query listener to this query.
	 * 
	 * @param listener
	 */
	public void addListener(QueryListener listener){
		for (int i = 0; i < listeners.size(); i++)
			if (listener.equals(listeners.get(i))) return;
		listeners.add(listener);
	}
	
	/****************************
	 * Removes a query listener from this query.
	 * 
	 * @param listener
	 */
	public void removeListener(QueryListener listener){
		listeners.remove(listener);
	}
	
	/****************************
	 * Returns a new instance of this class of query.
	 * 
	 * @param name
	 * @return
	 */
	public abstract InterfaceQuery getNewInstance(String name);
	
	/******************************
	 * Adds a result to this query; if a result from this object already exists, it is replaced,
	 * and moved to the end of the list. 
	 * 
	 * @param result
	 */
	protected void addResult(QueryResult result){
		
		InterfaceQueryObject object = result.getObject();
		QueryResult old_result = results.remove(object);
		results.put(object, result);
		if (old_result != null)
			sorted_objects.remove(object);
		sorted_objects.add(0, object);
		
	}
	
	/*******************************
	 * Returns a set of tree nodes containing the results of this query.
	 * 
	 * @return
	 */
	public InterfaceTreeNode getResultTreeNode(){
		
		InterfaceTreeNode node = new InterfaceTreeNode(this);
		ArrayList<QueryResult> sorted_results = getResults();
		
		for (int i = 0; i < sorted_results.size(); i++){
			node.add(sorted_results.get(i).getTreeNode());
			}
		
		return node;
	}
	
	/****************************
	 * Returns the last query result.
	 * 
	 * @return
	 */
	public QueryResult getLastResult(){
		if (sorted_objects.size() == 0) return null;
		return results.get(sorted_objects.get(sorted_objects.size() - 1));
	}
	
	protected void fireListeners(){
		QueryEvent e = new QueryEvent(this);
		for (QueryListener l : listeners)
			l.objectQueried(e);
	}
	
	public String getTreeLabel() {
		return getName();
	}

	public InterfaceTreeNode issueTreeNode() {
		InterfaceTreeNode node = new InterfaceTreeNode(this);
		setTreeNode(node);
		return node;
	}

	public void setTreeNode(InterfaceTreeNode node) {
		node.removeAllChildren();
		
		
	}
	
	public void destroy() {
		
	}

	public boolean isDestroyed() {
		return false;
	}
	
}