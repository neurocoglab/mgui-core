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

package mgui.interfaces.graphs;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.graphs.Graph3DInt;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;


/************************************************
 * Default implementation of a Graph in ModelGUI. Nodes are associated with unique
 * labels.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DefaultGraph extends InterfaceAbstractGraph{

	
	//maintain hash map of nodes, indexed by their labels
	protected HashMap<String, AbstractGraphNode> nodes = new HashMap<String, AbstractGraphNode>();
	
	public DefaultGraph(){
		_init();
	}
	
	public DefaultGraph(String name){
		_init();
		this.setName(name);
	}
	
	/*******************************************
	 * Construct a new graph as a copy of <code>graph</code>.
	 * 
	 * @param graph
	 */
	public DefaultGraph(InterfaceAbstractGraph graph){
		_init();
		setFromGraph(graph);
	}
	
	private void _init(){
		super.init();
	}
	
	@Override
	public boolean addVertex(AbstractGraphNode vertex){
		this.addGraphNode(vertex);
		return true;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		
		return new DefaultGraph(this);
		
	}
	
	@Override
	public String getTreeLabel(){
		return "Graph: " + getName();
	}
	
	/***************************************
	 * Adds a node to this graph. Returns <code>true</code> if the node was added;
	 * returns <code>false</code> if it (or a node of the same label) already exists.
	 * 
	 * @param node
	 * @return
	 */
	public boolean addGraphNode(AbstractGraphNode node){
		if (nodes.containsKey(node) || !super.addVertex(node)) 
			return false;
		
		nodes.put(node.getLabel(), node);
		return true;
	}
	
	/*****************************************
	 * Removes the node corresponding to <code>label</code>. Returns <code>true</code>
	 * if it was removed; <code>false</code> if it was not found.
	 * 
	 * @param label
	 * @return
	 */
	public boolean removeGraphNode(String label){
		if (!nodes.containsKey(label)) return false;
		return removeGraphNode(nodes.get(label));
	}
	
	/*****************************************
	 * Removes the given node. Returns <code>true</code>
	 * if it was removed; <code>false</code> if it was not found.
	 * 
	 * @param label
	 * @return
	 */
	public boolean removeGraphNode(AbstractGraphNode node){
		return super.removeVertex(node);
	}
	
	/******************************************
	 * Adds an edge to this graph. The endpoints of the edge must exists as nodes in
	 * this graph; otherwise, this method returns <code>false</code>.
	 * 
	 * @param edge
	 * @return <code>true</code> is edge was added; <code>false</code> otherwise
	 */
	public boolean addGraphEdge(AbstractGraphEdge edge){
		return addGraphEdge(edge, EdgeType.DIRECTED);
	}
	
	/******************************************
	 * Adds an edge to this graph. The endpoints of the edge must exists as nodes in
	 * this graph; otherwise, this method returns <code>false</code>.
	 * 
	 * @param edge
	 * @return <code>true</code> is edge was added; <code>false</code> otherwise
	 */
	public boolean addGraphEdge(AbstractGraphEdge edge, EdgeType type){
		
		if (!this.containsVertex(edge.from) || !this.containsVertex(edge.to))
			return false;
		
		if (!addEdge(edge, new Pair<AbstractGraphNode>(edge.from, edge.to), type))
			return false;
		
		return true;
	}
	
	
	
	/**********************************************
	 * Removes the given graph edge. Returns <code>true</code> if it was
	 * removed successfully; <code>false</code> otherwise.
	 * 
	 * @param edge
	 * @return
	 */
	public boolean removeGraphEdge(AbstractGraphEdge edge){
		return super.removeEdge(edge);
	}
	
	
	/*****************************************
	 * Returns the node corresponding to <code>label</code>. Returns <code>null</code>
	 * if there is no corresponding label.
	 * 
	 * @param label
	 * @return The node corresponding to <code>label</code>
	 */
	public AbstractGraphNode getVertexByLabel(String label){
		return nodes.get(label);
	}
	
	protected void setFromGraph(InterfaceAbstractGraph g){
		
		ArrayList<AbstractGraphNode> vertices = new ArrayList<AbstractGraphNode>(g.getNodes());
		HashMap<AbstractGraphNode,AbstractGraphNode> v_map = new HashMap<AbstractGraphNode,AbstractGraphNode>();
		
		try{
			for (int i = 0; i < vertices.size(); i++){
				AbstractGraphNode vertex = vertices.get(i);
				AbstractGraphNode cloned = (AbstractGraphNode)vertex.clone();
				v_map.put(vertex, cloned);
				this.addVertex(cloned);
				}
			
			ArrayList<AbstractGraphEdge> edges = new ArrayList<AbstractGraphEdge>(g.getEdges());
			for (int i = 0; i < edges.size(); i++){
				AbstractGraphEdge edge = (AbstractGraphEdge)edges.get(i).clone();
				this.addEdge(edge, v_map.get(edge.from), v_map.get(edge.to));
				}
			
		}catch (CloneNotSupportedException ex){
			InterfaceSession.handleException(ex);
			return;
			}
		
	}
	
	
	@Override
	public InterfacePopupMenu getPopupMenu() {
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Shape3DInt", getObjectIcon()));
		
		menu.add(new JSeparator(), 1);
		menu.add(new JSeparator(), 1);
		
		menu.addMenuItem(new JMenuItem("Create Shape 3D"));
		
		return menu;
	}

	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getText().equals("Create Shape 3D")){
			
			String name = JOptionPane.showInputDialog(InterfaceSession.getSessionFrame(), 
													  "Enter name for shape:", 
													  this.getName());
			
			if (name == null || name.length() == 0) return;
			
			//TODO: better dialog to specify target shape set
			Graph3DInt graph_int = new Graph3DInt(this);
			graph_int.setName(name);
			
			if (InterfaceSession.getDisplayPanel().getCurrentShapeSet().addShape(graph_int, true)){
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Shape '" + name + "' created.", 
											  "Create Graph 3D Shape", 
											  JOptionPane.INFORMATION_MESSAGE);
			}else{
				JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  "Error creating shape.", 
											  "Create Graph 3D Shape", 
											  JOptionPane.ERROR_MESSAGE);
				}
			
			return;
			}
		
	}

	@Override
	public void showPopupMenu(MouseEvent e) {
		InterfacePopupMenu menu = getPopupMenu();
		if (menu == null) return;
		menu.show(e);
	}
	
//	@Override
//	public String getLocalName() {
//		return "DefaultGraph";
//	}
	
}