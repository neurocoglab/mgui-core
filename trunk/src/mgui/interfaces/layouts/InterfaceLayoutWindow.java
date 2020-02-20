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

package mgui.interfaces.layouts;

import java.io.IOException;
import java.io.Writer;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.tools.layouts.ToolLayout2D;
import mgui.io.standard.xml.XMLOutputOptions;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**********************************************************************
 * Provides a window panel which specifies (via {@linkplain InterfaceLayoutObject}) and renders a layout 
 * containing graphical objects from existing windows or objects. Layouts provide a flexible interface 
 * for a highly specified graphical output; these are suitable for image rendering, print output, or custom 
 * visualization of ModelGUI data. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceLayoutWindow extends InterfaceGraphic<ToolLayout2D> {

	
	public InterfaceLayoutWindow(String name){
		super();
		init();
		this.setName(name);
	}
	
	public InterfaceLayoutWindow(){	
		this("No-name");
	}
	
	// Set up the layout
	protected void init(){
		if (!this.init_once) super.init();
		
		
		
	}
	
	
	
	//********************* XML Stuff ********************************
	
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
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
