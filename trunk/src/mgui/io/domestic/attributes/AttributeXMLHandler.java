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

package mgui.io.domestic.attributes;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.xml.XMLFunctions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**************************************************
 * Handles XML events for {@link Attribute} objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class AttributeXMLHandler extends DefaultHandler {

	protected AttributeList attribute_list;
	protected Attribute<?> current_attribute;
	protected String current_attribute_string;
	protected Class<?> current_class;
	
	public AttributeXMLHandler(){
		super();
	}
	
	public AttributeList getAttributeList(){
		return attribute_list;
	}
	
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		
		if (localName.equals("AttributeList")){
			attribute_list = new AttributeList();
			return;
			}
		
		if (localName.equals("Attribute")){
			try{
				String class_name = attributes.getValue("object_class");
				if (class_name.equals("null"))
					class_name = "java.lang.Object";
				current_class = Class.forName(class_name);
				Object value = XMLFunctions.getObjectForXML(localName, attributes, class_name);
				current_attribute = XMLFunctions.createAttribute(current_class, attributes.getValue("name"), value);
				
			}catch (ClassNotFoundException e){
				throw new SAXException("AttributeXMLHandler: Bad class for attribute: '" + 
														attributes.getValue("object_class") + "'");
			}catch (Exception e){
				//e.printStackTrace();
				InterfaceSession.handleException(e, LoggingType.Debug);
				throw new SAXException("AttributeXMLHandler: Error handling attribute: '" + 
						attributes.getValue("name") + "', with class '" +
						attributes.getValue("object_class") + "'\nDetails: " +
						e.getMessage());
				}
			current_attribute_string = "";
			}
		
		//if (current_attribute != null)
		//	current_attribute.handleXMLElementStart(localName, attributes, null);
		
	}
	
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		
		if (localName.equals("Attribute")){
			
			if (current_attribute == null)
				throw new SAXException("Attribute element ended without being started..");
			if (attribute_list == null)
				throw new SAXException("Attribute element ended without AttributeList being set..");
			if (current_attribute_string != null){
				if (current_attribute_string.trim().toLowerCase().equals("null")){
					current_attribute.setValue(null,false,true);
					return;
					}
				current_attribute.handleXMLString(current_attribute_string.trim());
				}
			attribute_list.add(current_attribute);
			current_attribute = null;
			return;
			}
		
		if (current_attribute != null)
			current_attribute.handleXMLElementEnd(localName);
		
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		if (current_attribute == null) return;
		
		String s = new String(ch, start, length);
		current_attribute_string = current_attribute_string + s; 
		
	}
	
	
	
	
}