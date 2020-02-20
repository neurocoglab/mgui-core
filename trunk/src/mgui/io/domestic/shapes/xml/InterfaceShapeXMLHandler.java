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

package mgui.io.domestic.shapes.xml;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject.XMLEncoding;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.domestic.attributes.AttributeXMLHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/****************************************************
 * XML handler for an {@linkplain InterfaceShape} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceShapeXMLHandler extends DefaultHandler {

	protected InterfaceShape shape;
	protected AttributeXMLHandler attribute_handler;
	protected StringBuilder current_shape_string;
	//protected XMLEncoding current_encoding;
	protected XMLType current_xml_type;
	protected String root_dir;
	
	/*********************************************************
	 * Empty constructor; shape must be set 
	 */
	public InterfaceShapeXMLHandler(){
		
	}
	
	/*********************************************************
	 * Construct with a shape.
	 * 
	 * @param shape
	 */
	public InterfaceShapeXMLHandler(InterfaceShape shape){
		this.shape = shape;
	}
	
	public void setRootDir(String dir){
		root_dir = dir;
	}
	
	public String getRootDir(){
		return root_dir;
	}
	
	public InterfaceShape getShape(){
		return shape;
	}
	
	protected boolean is_vertex_data = false;
	protected XMLType current_type = XMLType.Normal;
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		// If attribute handler is active, redirect this element to it.
		if (attribute_handler != null){
			attribute_handler.startElement(uri, localName, qName, attributes);
			return;
			}
		
		if (localName.equals("VertexData")){
			is_vertex_data = true;
			shape.handleXMLElementStart(localName, attributes, current_type);
			return;
			}
		
		if (is_vertex_data){
			shape.handleXMLElementStart(localName, attributes, current_type);
			return;
			}
		
		if (localName.equals("InterfaceShape")){
			if (shape != null)
				throw new SAXException("ShapeXMLHandler: Attempt to start InterfaceShape, but one has already been started..");
			
			shape = getShape(attributes);
			current_type = XMLFunctions.getXMLTypeForStr(attributes.getValue("type"));
			shape.setXMLRoot(root_dir);
			shape.handleXMLElementStart(localName, attributes, current_type);
			current_shape_string = new StringBuilder();
			return;
			}
		
		if (localName.equals("AttributeList")){
			attribute_handler = new AttributeXMLHandler();
			attribute_handler.startElement(uri, localName, qName, attributes);
			return;
			}
		
		if (localName.equals("Attribute")){
			if (attribute_handler != null)
				attribute_handler.startElement(uri, localName, qName, attributes);
			else
				throw new SAXException("ShapeXMLHandler: Attempt to add Attribute, but no AttributeList set..");
			return;
			}
		
		if (shape != null){
			shape.handleXMLElementStart(localName, attributes, current_xml_type);
			return;
			}
		
	}


	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (is_vertex_data){
			if (current_shape_string != null){
				shape.handleXMLString(current_shape_string.toString());
				current_shape_string = new StringBuilder();
				}
			shape.handleXMLElementEnd(localName);
			if (localName.equals("VertexData")){
				is_vertex_data = false;
				}
			return;
			}
		
		// If attribute handler is active, redirect this element to it.
		if (attribute_handler != null){
			if (shape == null)
				throw new SAXException("ShapeXMLHandler: Attempt to end Attribute, but no shape is set..");
				
			attribute_handler.endElement(uri, localName, qName);
			if (localName.equals("AttributeList")){
				//shape.setAttributes(attribute_handler.getAttributeList());
				AttributeList attributes = shape.getAttributes();
				attributes.setIntersection(attribute_handler.getAttributeList(), false);
				attribute_handler = null;
				}
			return;
			}
		
		if (localName.equals("Shape3DInt") || localName.equals("Shape2DInt")){
			if (is_vertex_data){
				throw new SAXException("ShapeXMLHandler: Shape ended while loading vertex data..");
				}
			return;
			}
		
		if (shape != null){
			if (current_shape_string != null){
				shape.handleXMLString(current_shape_string.toString().trim());
				current_shape_string = new StringBuilder();
				}
			shape.handleXMLElementEnd(localName);
			return;
			}
		
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		// If attribute handler is active, redirect this element to it.
		if (attribute_handler != null){
			attribute_handler.characters(ch, start, length);
			return;
			}
		
		if (shape != null){
			current_shape_string = current_shape_string.append(ch, start, length);
			return;
			}
		
	}
	
	// Attempts to instantiate a new InterfaceShape from the given attributes
	InterfaceShape getShape(Attributes attributes) throws SAXException{
		
		try{
			Class<?> _class = Class.forName(attributes.getValue("class"));
			InterfaceShape shape = (InterfaceShape)_class.newInstance();
			shape.setName(attributes.getValue("name"));
			//current_encoding = XMLFunctions.getEncodingForStr(attributes.getValue("encoding"));
			current_xml_type = XMLFunctions.getXMLTypeForStr(attributes.getValue("type"));
			return shape;
		}catch (Exception e){
			InterfaceSession.handleException(e, LoggingType.Errors);
			//e.printStackTrace();
			throw new SAXException("Invalid class for InterfaceShape: " + attributes.getValue("class"));
			}
		
	}
	
}