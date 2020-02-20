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

package mgui.io.domestic.maps;

import mgui.interfaces.maps.NameMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**********************************************************
 * XML (SAX) handler for a name map XML file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NameMapXMLHandler extends DefaultHandler {

	public NameMap map;
	
	public NameMapXMLHandler(){
		
	}
	
	public NameMap getMap(){
		return map;
	}
	
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		
		if (localName.equals("NameMap")){
			return;
			}
		
		if (map != null)
			map.handleXMLElementEnd(localName);
		
	}
	
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		
		if (localName.equals("NameMap")){
			if (attributes.getValue("strict_naming") != null){
				map = new NameMap(attributes.getValue("name"), Boolean.valueOf(attributes.getValue("strict_naming")));
			}else{
				map = new NameMap(attributes.getValue("name"));
				}
			
			return;
			}
		
		if (map != null)
			map.handleXMLElementStart(localName, attributes, null);
		
	}
	
}