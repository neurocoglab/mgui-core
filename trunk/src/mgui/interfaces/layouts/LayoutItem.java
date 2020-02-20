/*
* Copyright (C) 2011 Andrew Reid and the modelGUI Project <http://mgui.wikidot.com>
* 
* This file is part of modelGUI[core] (mgui-core).
* 
* modelGUI[core] is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* modelGUI[core] is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with modelGUI[core]. If not, see <http://www.gnu.org/licenses/>.
*/

package mgui.interfaces.layouts;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;

/*********************************************************
 * Specifies an element to be rendered in a {@linkplain InterfaceLayoutDocument}. Specifies its own
 * location, dimension, and attributes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class LayoutItem extends AbstractInterfaceObject implements XMLObject {

	protected Point2D.Float location = new Point2D.Float();
	protected Point2D.Float size = new Point2D.Float(1,1);
	
	protected AttributeList attributes;
	
	/*****************************************
	 * Initiates this item's attributes.
	 * 
	 */
	protected void init(){
		
		attributes = new AttributeList();
		
		attributes.add(new Attribute<String>("Name","no-name"));
		attributes.add(new Attribute<Color>("Background",Color.white));
		
		// Border, title
		
	}
	
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	/****************************************
	 * Returns the upper-left corner of this item, in document space.
	 * 
	 * @return
	 */
	public Point2D getLocation(){
		return location;
	}
	
	/****************************************
	 * Sets the upper-left corner of this item, in document space.
	 * 
	 * @param location
	 */
	public void setLocation(Point2D.Float location){
		if (location == null) return;
		this.location = location;
	}
	
	/****************************************
	 * Returns the width of this item, in document units.
	 * 
	 * @return
	 */
	public double getWidth(){
		return size.getX();
	}
	
	/****************************************
	 * Sets the width of this item, in document units.
	 * 
	 * @param width
	 */
	public void setWidth(float width){
		size.setLocation(width, size.getY());
	}
	
	/****************************************
	 * Returns the height of this item, in document units.
	 * 
	 * @return
	 */
	public double getHeight(){
		return size.getY();
	}
	
	
	/****************************************
	 * Sets the width of this item, in document units.
	 * 
	 * @param width
	 */
	public void setHeight(float height){
		size.setLocation(size.getX(), height);
	}
	
	/****************************************
	 * Returns the size of this item, in document units.
	 * 
	 * @return
	 */
	public Point2D.Float getSize(){
		return new Point2D.Float((float)size.getX(),(float)size.getY());
	}
	
	
	/****************************************
	 * Sets the size of this item, in document units.
	 * 
	 * @param width
	 */
	public void setSize(Point2D.Float size){
		this.size.setLocation(size);
	}

	
	// *************************** XML Stuff *******************************
	

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
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void handleXMLElementStart(String localName, Attributes attributes,
			XMLType type) throws SAXException {
		// TODO Auto-generated method stub
		
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
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options,
			ProgressUpdater progress_bar) throws IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar)
			throws IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}
	
}