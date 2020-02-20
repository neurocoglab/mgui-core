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

import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.io.domestic.attributes.AttributeXMLHandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/****************************************************
 * XML handler for a {@linkplain ShapeSet2DInt} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeSet2DXMLHandler extends DefaultHandler {

	protected ShapeSet2DInt shape_set;
	protected AttributeXMLHandler attribute_handler;
	
	
	
	public ShapeSet2DInt getShapeSet(){
		return shape_set;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
	}



	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
	}



	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
	}
	
	
}