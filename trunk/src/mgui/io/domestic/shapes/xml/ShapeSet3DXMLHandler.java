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

package mgui.io.domestic.shapes.xml;

import java.util.Stack;

import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.domestic.attributes.AttributeXMLHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/****************************************************
 * XML handler for a {@linkplain ShapeSet3DInt} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeSet3DXMLHandler extends DefaultHandler {

	protected ShapeSet3DInt shape_set;
	protected ShapeSet3DXMLHandler shape_set_xml_handler; 	// Recursive handler
	protected InterfaceShapeXMLHandler shape_handler;
	protected AttributeXMLHandler attribute_handler;
	protected boolean is_finished = false;
	protected boolean handling_members = false;
	protected String root_dir;
	
	public ShapeSet3DXMLHandler(ShapeSet3DInt shape_set){
		this.shape_set = shape_set;
	}
	
	public ShapeSet3DInt getShapeSet(){
		return shape_set;
	}
	
	public boolean isFinished(){
		return is_finished;
	}
	
	public void setRootDir(String dir){
		root_dir = dir;
	}
	
	public String getRootDir(){
		return root_dir;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
	
		// If we have a recursive shape set handler, redirect
		if (shape_set_xml_handler != null){
			shape_set_xml_handler.startElement(uri, localName, qName, attributes);
			return;
			}
		
		// If we have a shape handler, redirect
		if (shape_handler != null){
			shape_handler.startElement(uri, localName, qName, attributes);
			return;
			}
		
		// If we have an attribute handler, redirect
		if (attribute_handler != null){
			attribute_handler.startElement(uri, localName, qName, attributes);
			return;
			}
		
		if (localName.equals("AttributeList")){
			attribute_handler = new AttributeXMLHandler();
			attribute_handler.startElement(uri, localName, qName, attributes);
			return;
			}
		
		if (localName.equals("Members")){
			// We should now expect to handle shapes
			handling_members = true;
			return;
			}
		
		if (localName.equals("ShapeSet3DInt")){
			if (shape_set != null){
				if (!handling_members)
					throw new SAXException("ShapeXMLHandler: Subordinate shape set encountered outside of Members block..");
				// This is a subordinate set, start recursive handler
				ShapeSet3DInt set = new ShapeSet3DInt(attributes.getValue("name"));
				shape_set_xml_handler = new ShapeSet3DXMLHandler(set);
				shape_set_xml_handler.setRootDir(root_dir);
				return;
				}
			
			shape_set = new ShapeSet3DInt(attributes.getValue("name"));
			is_finished = false;
			
			return;
			}
		
		if (!handling_members){
			throw new SAXException("ShapeXMLHandler: Unexpected start tag: " + localName);
			}
		
		if (localName.equals("InterfaceShape")){
			shape_handler = new InterfaceShapeXMLHandler();
			shape_handler.setRootDir(root_dir);
			shape_handler.startElement(uri, localName, qName, attributes);
			
			return;
			}
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		// If we have a recursive shape set handler, redirect
		if (shape_set_xml_handler != null){
			shape_set_xml_handler.endElement(uri, localName, qName);
			if (shape_set_xml_handler.isFinished()){
				if (shape_set == null)
					throw new SAXException("ShapeXMLHandler: Subordinate set ended but no parent set started..");
				shape_set.addShape(shape_set_xml_handler.getShapeSet());
				shape_set_xml_handler = null;
				return;
				}
			return;
			}
		
		// If we have a shape handler, redirect
		if (shape_handler != null){
			shape_handler.endElement(uri, localName, qName);
			if (localName.equals("InterfaceShape")){
				if (shape_set == null)
					throw new SAXException("ShapeXMLHandler: Shape ended but no parent set started..");
				shape_set.addShape(shape_handler.getShape());
				shape_handler = null;
				return;
				}
			return;
			}
		
		// If we have an attribute handler, redirect
		if (attribute_handler != null){
			attribute_handler.endElement(uri, localName, qName);
			if (localName.equals("AttributeList")){
				if (shape_set == null)
					throw new SAXException("ShapeXMLHandler: AttributeList ended but no parent set started..");
				shape_set.setAttributes(attribute_handler.getAttributeList());
				attribute_handler = null;
				return;
				}
			return;
			}
		
		if (localName.equals("Members")){
			// Done handling member shapes
			handling_members = false;
			return;
			}
		
		if (localName.equals("ShapeSet3DInt")){
			// Done this shape set
			is_finished = true;
			}
		
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		// If we have a recursive shape set handler, redirect
		if (shape_set_xml_handler != null){
			shape_set_xml_handler.characters(ch, start, length);
			return;
			}
		
		// If we have a shape handler, redirect
		if (shape_handler != null){
			shape_handler.characters(ch, start, length);
			return;
			}
		
		// If we have an attribute handler, redirect
		if (attribute_handler != null){
			attribute_handler.characters(ch, start, length);
			return;
			}
		
	}
	
}