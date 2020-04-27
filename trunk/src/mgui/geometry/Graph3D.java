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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import org.jogamp.vecmath.Point3f;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.DefaultGraph;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject.XMLEncoding;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/*************************************
 * Represents a graph as a 3D geometric shape.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Graph3D extends Shape3D {

	protected InterfaceAbstractGraph graph;
	
	public Graph3D(){
		this(new DefaultGraph());
	}
	
	public Graph3D(InterfaceAbstractGraph graph){
		setGraph(graph);
	}
	
	public Graph3D(InterfaceAbstractGraph graph, HashMap<AbstractGraphNode,Point3f> nodes){
		setGraph(graph);
	}
	
	public InterfaceAbstractGraph getGraph(){
		return graph;
	}
	
	public void setGraph(InterfaceAbstractGraph graph){
		this.graph = graph;
	}
	
	@Override
	public ArrayList<Point3f> getVertices(){
		ArrayList<AbstractGraphNode> vertices = new ArrayList<AbstractGraphNode>(graph.getVertices());
		ArrayList<Point3f> coords = new ArrayList<Point3f>();
		for (int i = 0; i < vertices.size(); i++){
			coords.add(vertices.get(i).getLocation());
			}
		return coords;
	}
	
	public ArrayList<AbstractGraphNode> getJungVertices(){
		return new ArrayList<AbstractGraphNode>(graph.getVertices());
	}
	
	@Override
	public void setVertices(Point3f[] nodes){
		ArrayList<AbstractGraphNode> vertices = new ArrayList<AbstractGraphNode>(graph.getVertices());
		for (int i = 0; i < vertices.size(); i++){
			if (nodes.length > i)
			vertices.get(i).setLocation(nodes[i]);
			}
	}
	
	@Override
	public void setVertices(ArrayList<Point3f> nodes){
		ArrayList<AbstractGraphNode> vertices = new ArrayList<AbstractGraphNode>(graph.getVertices());
		for (int i = 0; i < vertices.size(); i++){
			if (nodes.size() > i)
			vertices.get(i).setLocation(nodes.get(i));
			}
		
	}
	
	public int getSize(){
		return graph.getVertexCount();
	}
	
	@Override
	public float[] getCoords(){
		ArrayList<Point3f> vertices = getVertices();
		float[] coords = new float[vertices.size() * 3];
		for (int i = 0; i < vertices.size(); i++){
			Point3f v = vertices.get(i);
			coords[i*3] = v.getX();
			coords[i*3+1] = v.getY();
			coords[i*3+2] = v.getZ();
			}
		return coords;
	}
	
	//TODO
	@Override
	public void setCoords(float[] coords){
		
		int n = coords.length / 3;
		ArrayList<Point3f> vertices = new ArrayList<Point3f> (coords.length / 3);
		for (int i = 0; i < n; i++){
			Point3f v = new Point3f(coords[i*3],
									coords[i*3+1],
									coords[i*3+2]);
			vertices.add(v);
			}
		this.setVertices(vertices);
	}
	
	@Override
	public Point3f getVertex(int index) {
		ArrayList<Point3f> vertices = getVertices();
		return vertices.get(index);
	}

	// ******************** XML Stuff *********************************
	
	@Override
	public String getLocalName() {
		return "Graph3D";
	}

	@Override
	public void handleXMLElementEnd(String localName) throws SAXException {
		// Forward to graph
		if (graph == null)
			throw new SAXException("Graph3D.handleXMLElementEnd: Attempted to load graph but no graph set yet.");
		
		graph.handleXMLElementEnd(localName);
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) throws SAXException {
		
		if (localName.equals(this.getLocalName())){
			xml_encoding = XMLFunctions.getEncodingForStr(attributes.getValue("encoding"));
			return;
			}
		
		// Forward to graph
		if (graph == null)
			throw new SAXException("Graph3D.handleXMLElementStart: Attempted to load graph but no graph set yet.");
		
		graph.handleXMLElementStart(localName, attributes, type);
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException {
		
		if (graph == null)
			throw new SAXException("Graph3D.handleXMLString: Attempted to load graph but no graph set yet.");
		
		graph.handleXMLString(s);
		
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
		// Graph coordinates from source
		String _tab = XMLFunctions.getTab(tab);
		
		writer.write(_tab + "<" + getLocalName() + " encoding='" + XMLFunctions.getEncodingStr(options.encoding) + "' >\n");
		graph.writeXML(tab + 1, writer, options, progress_bar);
		writer.write("\n" + _tab + "</" + getLocalName() + ">");
		
	}
	
}