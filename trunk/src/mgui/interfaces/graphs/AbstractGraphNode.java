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

import org.jogamp.vecmath.Point3f;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiNumber;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/******************************************
 * 
 * Abstract representation of a graph node (vertex), with a specific static 3D locations.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class AbstractGraphNode extends AbstractInterfaceObject
							   			implements Comparable<AbstractGraphNode>,
					   							   XMLObject{
	
	

	protected Point3f location = new Point3f(0, 0, 0);
	protected MguiNumber current_value = new MguiDouble(0);
	
	public AbstractGraphNode(){
		super();
		setName("");
	}
	
	public AbstractGraphNode(String label){
		super();
		setName(label);
	}
	
	public Object clone() throws CloneNotSupportedException{
		throw new CloneNotSupportedException();
	}
	
	public MguiNumber getCurrentValue(){
		return this.current_value;
	}
	
	public void setCurrentValue(double d){
		current_value.setValue(d);
	}
	
	public void setLabel(String label){
		this.setName(label);
	}
	
	public String getLabel(){
		return getName();
	}
	
	@Override
	public int compareTo(AbstractGraphNode node) {
		return getLabel().compareTo(node.getLabel());
	}

	/***********************************
	 * Returns the static location of this node.
	 * 
	 * @return
	 */
	public Point3f getLocation(){
		return new Point3f(location);
	}
	
	/***********************************
	 * Sets the static location of this node.
	 * 
	 * @return
	 */
	public void setLocation(Point3f p){
		location.set(p);
	}
	
	//*********************** XML Stuff ****************************************
	
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
		return getXML(tab,0);
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type)
			throws SAXException {
		
		// Set from attributes
		String position = attributes.getValue("position");
		String[] parts = position.split(" ");
		this.setLocation(new Point3f(Float.valueOf(parts[0]),
									 Float.valueOf(parts[1]),
									 Float.valueOf(parts[2])));
		this.setLabel(attributes.getValue("label"));
		
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
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException {
		writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, new XMLOutputOptions(), null);
	}

	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*********************************************
	 * Get XML representation for the given index.
	 * 
	 * @param tab
	 * @param index
	 * @return
	 */
	public String getXML(int tab, int index) {
		
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<" + getLocalName() + " " + 
					 "class='" + this.getClass().getCanonicalName() + "' " +
					 "index='" + index + "' " +
					 "label='" + this.getLabel() + "' " +
					 "position='" + location.getX() + " " + 
			 						location.getY() + " " + 
			 						location.getZ() + 
			 		 "' />";
					 
		return xml;
		
	}
	
	@Override
	public String getLocalName(){
		return "AbstractGraphNode";
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException {
		writer.write(getXML(tab));
	}
	
}