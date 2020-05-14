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

package mgui.interfaces.graphs;

import java.io.IOException;
import java.io.Writer;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.domestic.graphs.xml.GraphXMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.uci.ics.jung.graph.util.EdgeType;

/****************************************
 * Abstract representation of a graph edge, connecting two nodes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class AbstractGraphEdge extends AbstractInterfaceObject
										implements XMLObject{

	protected AbstractGraphNode from, to;
	protected EdgeType type = EdgeType.DIRECTED;
	
	public AbstractGraphEdge(AbstractGraphNode from, AbstractGraphNode to){
		//super(from, to);
		this.from = from;
		this.to = to;
	}
	
	public void setFromEdge(AbstractGraphEdge edge){
		
		this.from = edge.from;
		this.to = edge.to;
		this.type = edge.getType();
		
	}
	
	public void setFromNodes(AbstractGraphNode from, AbstractGraphNode to){
		this.from = from;
		this.to = to;
	}
	
	public EdgeType getType(){
		return type;
	}
	
	public void setType(EdgeType type){
		this.type = type;
	}
	
	public AbstractGraphNode getFrom(){
		return from;
	}
	
	public AbstractGraphNode getTo(){
		return to;
	}
	
	public void setVertices(AbstractGraphNode from, AbstractGraphNode to){
		this.from = from;
		this.to = to;
	}
	
	public void setLabel(String label){
		this.setName(label);
	}
	
	public String getLabel(){
		return getName();
	}
	
	public Object clone() throws CloneNotSupportedException{
		throw new CloneNotSupportedException();
	}

	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXMLSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML() {
		return getXML(0);
	}

	@Override
	public String getXML(int tab) {
		return getXML(tab, 0, 1);
	}
	
	/************************************************
	 * Returns an XML representation for this edge given from and to indices.
	 * 
	 * @param tab
	 * @param v_from
	 * @param v_to
	 * @return
	 */
	public String getXML(int tab, int v_from, int v_to) {
		
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<" + getLocalName() + 
							 " class='" + getClass().getCanonicalName() + "'" +
							 " type='" + getType() + "'" +
							 " from='" + v_from + "'" +
							 " to='" + v_to + "' />";
					 
		return xml;
	}

	@Override
	public String getLocalName() {
		return "AbstractGraphEdge";
	}
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type)
			throws SAXException {
		
		String edge_type = attributes.getValue("type");
		this.setType(GraphXMLFunctions.getEdgeTypeForString(edge_type));
		
		
	}

	@Override
	public void handleXMLElementEnd(String localName) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		
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
		return null;
	}
	
	
	
}