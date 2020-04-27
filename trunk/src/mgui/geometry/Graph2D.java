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

package mgui.geometry;

import java.util.ArrayList;
import java.util.HashMap;

import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3f;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.InterfaceAbstractGraph;

/*************************************
 * Represents a graph as a 2D geometric shape.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Graph2D extends PointSet2D {

	protected InterfaceAbstractGraph graph;
	HashMap<AbstractGraphNode,Integer> index_map;
	HashMap<Integer,AbstractGraphNode> vertex_map;
	
	public Graph2D(){
		
	}
	
	public Graph2D(InterfaceAbstractGraph graph){
		setGraph(graph);
	}
	
	public Graph2D(InterfaceAbstractGraph graph, HashMap<AbstractGraphNode,Point2f> vertices){
		setGraph(graph);
		ArrayList<AbstractGraphNode> g_nodes = new ArrayList<AbstractGraphNode>(graph.getVertices());
		index_map = new HashMap<AbstractGraphNode,Integer>();
		vertex_map = new HashMap<Integer,AbstractGraphNode>();
		for (int i = 0; i < g_nodes.size(); i++){
			this.addNode(vertices.get(g_nodes.get(i)));
			index_map.put(g_nodes.get(i), i);
			vertex_map.put(i, g_nodes.get(i));
			}
	}
	
	public ArrayList<AbstractGraphNode> getJungVertices(){
		return new ArrayList<AbstractGraphNode>(graph.getVertices());
	}
	
	public InterfaceAbstractGraph getGraph(){
		return graph;
	}
	
	public void setGraph(InterfaceAbstractGraph graph){
		this.graph = graph;
	}
	
}