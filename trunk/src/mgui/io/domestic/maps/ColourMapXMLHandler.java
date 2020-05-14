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

package mgui.io.domestic.maps;

import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.maps.DiscreteColourMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/****************************************************************************
 * XML (SAX) handler for reading XML-format colour map files.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ColourMapXMLHandler extends DefaultHandler {

	public ColourMap map;
	
	public ColourMapXMLHandler(){
		
	}
	
	public ColourMap getMap(){
		return map;
	}
	
	@Override
	public void endElement(String uri, String localName, String name) throws SAXException {
		
		if (localName.equals("ColourMap")){
			return;
			}
		
		if (map != null)
			map.handleXMLElementEnd(localName);
		
	}
	
	@Override
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		
		if (localName.equals("ColourMap")){
			if (attributes.getValue("type").equals("discrete")){
				map = new DiscreteColourMap(attributes.getValue("name"));
				return;
				}
			
			if (attributes.getValue("type").equals("continuous")){
				map = new ContinuousColourMap(attributes.getValue("name"));
				return;
				}
			}
		
		if (map != null)
			map.handleXMLElementStart(localName, attributes, null);
		
		
	}
	
	
	
}