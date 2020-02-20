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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import mgui.graphs.networks.NetworkGraphEvent;
import mgui.graphs.networks.NetworkGraphListener;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.util.InterfaceFunctions;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.domestic.graphs.xml.GraphXMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiBoolean;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**********************************************
 * Abstract representation of a Graph in modelGUI; extends Jung's SparseGraph
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfaceAbstractGraph extends SparseGraph<AbstractGraphNode,
																 AbstractGraphEdge>
												   implements AttributeObject,
												 			  AttributeListener,
											   			  	  InterfaceObject,
											   			  	  NetworkGraphListener,
											   			  	  PopupMenuObject,
											   			  	  IconObject,
											   			  	  XMLObject{

	public AttributeList attributes = new AttributeList();
	public ArrayList<InterfaceGraphListener> graphListeners = new ArrayList<InterfaceGraphListener>();
	protected ArrayList<InterfaceTreeNode> treeNodes = new ArrayList<InterfaceTreeNode>();
	
	public InterfaceTreeNode treeNode;
	public String name;
	protected boolean isDestroyed = false;
	
	protected void init(){
		attributes.addAttributeListener(this);
		attributes.add(new Attribute<String>("Name", "no-name"));
		attributes.add(new Attribute<MguiBoolean>("LabelEdges", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("LabelNodes", new MguiBoolean(true)));
	}
	
	@Override
	public void clean(){
		InterfaceFunctions.cleanInterfaceObject(this);
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = InterfaceAbstractGraph.class.getResource("/mgui/resources/icons/graph_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/graph_20.png");
		return null;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		
		throw new CloneNotSupportedException();
	}
	
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	public String getTreeLabel(){
		return getName();
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	public String getDefaultLayout(){
		return "Circle Layout";
	}

	public boolean getLabelNodes(){
		return ((MguiBoolean)attributes.getValue("LabelNodes")).getTrue();
	}
	
	public boolean getLabelEdges(){
		return ((MguiBoolean)attributes.getValue("LabelEdges")).getTrue();
	}
	
	public void addGraphListener(InterfaceGraphListener g){
		graphListeners.add(g);
	}
	
	public void removeGraphListener(InterfaceGraphListener g){
		graphListeners.remove(g);
	}
	
	protected void fireGraphListeners(){
		fireGraphListeners(InterfaceGraphEvent.GE_GENERAL);
	}
	
	protected void fireGraphListeners(int code){
		for (int i = 0; i < graphListeners.size(); i++)
			graphListeners.get(i).graphUpdated(new InterfaceGraphEvent(this, code));
	}
	
	public void attributeUpdated(AttributeEvent e){
		Attribute a = (Attribute)e.getSource();
		if (a.getName().startsWith("Label"))
			fireGraphListeners(InterfaceGraphEvent.GE_LABELS);
		else 
			fireGraphListeners(InterfaceGraphEvent.GE_GENERAL);
	}
	
	public Attribute getAttribute(String attrName) {	
		return attributes.getAttribute(attrName);
	}

	public AttributeList getAttributes() {
		return attributes;
	}

	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}
	
	public void graphUpdated(NetworkGraphEvent e) {
		//fire listeners with general event
		fireGraphListeners();
	}
	
	public InterfaceTreeNode issueTreeNode(){
		InterfaceTreeNode treeNode = new InterfaceTreeNode(this);
		setTreeNode(treeNode);
		treeNodes.add(treeNode);
		return treeNode;
	}
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		
		ArrayList<InterfaceTreeNode> nodes = new ArrayList<InterfaceTreeNode>(treeNodes);
		
		//if any children are in treeNode list, remove them (otherwise we have a memory leak)
		Enumeration children = treeNode.children();
		while (children.hasMoreElements()){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)children.nextElement();
			if (nodes.contains(node)) treeNodes.remove(node);
			}
		
		treeNode.removeAllChildren();
		treeNode.setUserObject(this);
		
		treeNode.add(attributes.issueTreeNode());
		
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu() {
		return null;
	}

	@Override
	public void handlePopupEvent(ActionEvent e) {
		
	}

	@Override
	public void showPopupMenu(MouseEvent e) {
		
	}
	
	/***********************************
	 * Overridden to set up edge properly.
	 * 
	 * @param edge
	 * @param v1
	 * @param v2
	 * @param edgeType
	 * @return
	 */
	public boolean addEdge(AbstractGraphEdge edge, 
						   AbstractGraphNode v1, 
						   AbstractGraphNode v2, 
						   EdgeType edgeType){
		
		if (edge.from != v1) edge.from = v1;
		if (edge.to != v2) edge.to = v2;
		edge.setType(edgeType);
		
		super.addEdge(edge, v1, v2, edgeType);
		
		return true;
	}
	
	public boolean addEdge(AbstractGraphEdge edge,
						   EdgeType edgeType){
		edge.setType(edgeType);
		return addEdge(edge, edge.from, edge.to, edgeType);
	}
			
	public boolean addEdge(AbstractGraphNode v1, 
						   AbstractGraphNode v2, 
						   EdgeType edgeType){
		
		return addEdge(new DefaultGraphEdge(v1, v2, edgeType), v1, v2, edgeType);
		
	}
	
	/************************************
	 * Removes all edges in this Graph.
	 * 
	 */
	public void removeAllEdges(){
		Collection<AbstractGraphEdge> edges = this.getEdges();
		Iterator<AbstractGraphEdge> itr = edges.iterator();
		while (itr.hasNext())
			this.removeEdge(itr.next());
	}
	
	/************************************
	 * Removes all nodes in this Graph.
	 * 
	 */
	public void removeAllNodes(){
		Collection<AbstractGraphNode> nodes = this.getVertices();
		Iterator<AbstractGraphNode> itr = nodes.iterator();
		while (itr.hasNext())
			this.removeVertex(itr.next());
	}
	
	public ArrayList<AbstractGraphNode> getNodes(){
		return new ArrayList<AbstractGraphNode>(getVertices());
	}
	
	public void destroy(){
		isDestroyed = true;
		ArrayList<InterfaceTreeNode> nodes = new ArrayList<InterfaceTreeNode>(treeNodes);
		for (int i = 0; i < nodes.size(); i++){
			nodes.get(i).destroy();
			if (nodes.get(i).getParent() != null)
				nodes.get(i).removeFromParent();
			treeNodes.remove(nodes.get(i));
			}
		treeNodes = null;
	}
	
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	
	//******************* XML Stuff **********************************
	
		@Override
		public String getDTD() {
			return null;
		}

		@Override
		public String getXMLSchema() {
			return null;
		}

		@Override
		public String getXML() {
			return getXML(0);
		}

		@Override
		public String getXML(int tab) {
			InterfaceSession.log("DefaultGraph: getXML not implemented. Use writeXML instead.", 
								 LoggingType.Errors);
			return getShortXML(tab);
		}

		protected HashMap<Integer,AbstractGraphNode> xml_nodes;
		protected int xml_itr = 0, xml_count = 0;
		protected String xml_block = "";
		
		@Override
		public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException {
			
			if (localName.equals("Vertices")){
				// Start new set of vertices
				xml_nodes = new HashMap<Integer,AbstractGraphNode>();
				xml_itr = 0;
				xml_block = "Vertices";
				xml_count = Integer.valueOf(attributes.getValue("count"));
				return;
				}
			
			if (localName.equals("Edges")){
				xml_block = "Edges";
				xml_itr = 0;
				xml_count = Integer.valueOf(attributes.getValue("count"));
				return;
				}
			
			// Instantiate and add node of the specified class
			if (localName.equals("AbstractGraphNode")){
				if (xml_nodes == null || !xml_block.equals("Vertices"))
					throw new SAXException("InterfaceAbstractGraph.handleXMLElementStart: AbstractGraphNode " +
										   "encountered but vertices not started.");
				if (xml_itr > xml_count)
					throw new SAXException("InterfaceAbstractGraph.handleXMLElementStart: Vertex count " +
										   "> expected (" + xml_count + ")");
				AbstractGraphNode node = GraphXMLFunctions.getGraphNodeInstance(attributes);
				if (node != null){
					node.handleXMLElementStart(localName, attributes, type);
					addVertex(node);
					xml_nodes.put(xml_itr, node);
					}
				xml_itr++;	//Iterate whether it passed or not
				return;
				}
			
			// Instantiate and add edge of the specified class
			if (localName.equals("AbstractGraphEdge")){
				if (xml_nodes == null || !xml_block.equals("Edges"))
					throw new SAXException("InterfaceAbstractGraph.handleXMLElementStart: AbstractGraphEdge " +
										   "encountered but edges not started.");
				if (xml_itr > xml_count)
					throw new SAXException("InterfaceAbstractGraph.handleXMLElementStart: Edge count " +
										   "> expected (" + xml_count + ")");
				AbstractGraphEdge edge = GraphXMLFunctions.getGraphEdgeInstance(attributes);
				if (edge != null){
					edge.handleXMLElementStart(localName, attributes, type);
					int from = Integer.valueOf(attributes.getValue("from"));
					int to = Integer.valueOf(attributes.getValue("to"));
					edge.setFromNodes(xml_nodes.get(from), xml_nodes.get(to));
					addEdge(edge, edge.getType());
					}
				xml_itr++;  //Iterate whether it passed or not
				return;
				}
			
		}

		@Override
		public void handleXMLElementEnd(String localName) throws SAXException {
			
			if (localName.equals("Vertices")){
				xml_block = "";
				xml_itr = 0;
				xml_count = 0;
				return;
				}
			
			if (localName.equals("Edges")){
				xml_block = "";
				xml_itr = 0;
				xml_count = 0;
				xml_nodes = null;
				return;
				}
			
		}

		@Override
		public void handleXMLString(String s) throws SAXException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
			
			String _tab = XMLFunctions.getTab(tab);
			String _tab2 = XMLFunctions.getTab(tab + 1);
			
			writer.write(_tab + "<" + getLocalName() + "\n" +
						 _tab2 + "class = '" + this.getClass().getCanonicalName() + "'\n" +
						 _tab2 + "name = '" + getName() + "'\n" +
						 _tab + ">");
			
			// Write attributes
			writer.write("\n");
			attributes.writeXML(tab + 1, writer, options, progress_bar);
			writer.write("\n");
			
			// Write vertices
			writer.write(_tab2 + "<Vertices count='" + this.getVertexCount() + "'>\n");
			
			HashMap<AbstractGraphNode,Integer> index_map = new HashMap<AbstractGraphNode,Integer>();
			ArrayList<AbstractGraphNode> vertices = new ArrayList<AbstractGraphNode>(getVertices());
			for (int i = 0; i < vertices.size(); i++){
				AbstractGraphNode node = vertices.get(i);
				writer.write(node.getXML(tab + 2, i) + "\n");
				index_map.put(node, i);
				}
			
			writer.write(_tab2 + "</Vertices>\n");
			
			// Write edges
			writer.write(_tab2 + "<Edges count='" + this.getEdgeCount() + "'>\n");
			
			ArrayList<AbstractGraphEdge> edges = new ArrayList<AbstractGraphEdge>(getEdges());
			for (int i = 0; i < edges.size(); i++){
				AbstractGraphEdge edge = edges.get(i);
				int from = index_map.get(edge.from);
				int to = index_map.get(edge.to);
				writer.write(edge.getXML(tab + 2, from, to) + "\n");
				}
			
			writer.write(_tab2 + "</Edges>\n");
			
			writer.write(_tab + "</" + getLocalName() +">");
			
		}

		@Override
		public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException {
			writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
		}

		@Override
		public void writeXML(int tab, Writer writer) throws IOException {
			writeXML(tab, writer, new XMLOutputOptions(), null);
		}

		@Override
		public String getShortXML(int tab) {
			String _tab = XMLFunctions.getTab(tab);
			String s = _tab + "<" + getLocalName() + " name='" + getName() + "'/>";
			return s;
		}
		
		@Override
		public String getLocalName() {
			return "InterfaceAbstractGraph";
		}
		
}