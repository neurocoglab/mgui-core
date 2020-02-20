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

package mgui.io.domestic.views;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphics.View3D;
import mgui.interfaces.maps.Camera3D;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.ShapeDataLoader;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/******************************************
 * Loads a set of View3D objects from an XML file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class View3DLoader extends FileLoader {

	public View3DLoader(){
		
	}
	
	public View3DLoader(File file){
		setFile(file);
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		View3DInputOptions _options = (View3DInputOptions)options;
		
		File[] files = _options.getFiles();
		boolean success = true;
		ArrayList<View3D> views = new ArrayList<View3D>();
		
		for (int i = 0; i < files.length; i++){
			try{
				setFile(files[i]);
				ArrayList<View3D> _views = (ArrayList<View3D>)loadObject(progress_bar, null);
				if (_views == null)
					success = false;
				else
					views.addAll(_views);
				
			}catch (IOException e){
				success = false;
				}
			}
		
		for (int i = 0; i < views.size(); i++)
			InterfaceSession.getWorkspace().addView3D(views.get(i));
		
		return success;
	}

	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException {
		
		try{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			View3DXMLHandler handler = new View3DXMLHandler();
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(dataFile)));
			return handler.getViews();
			
		}catch (Exception e){
			e.printStackTrace();
			return null;
			}

	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = View3DLoader.class.getResource("/mgui/resources/icons/view_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/view_3d_20.png");
		return null;
	}

	public class View3DXMLHandler extends DefaultHandler{

		public ArrayList<View3D> views = new ArrayList<View3D>();
		View3D current_view;
		boolean is_camera = false;
		
		public ArrayList<View3D> getViews(){
			return views;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			
			if (is_camera){
				if (current_view == null || current_view.camera == null)
					throw new SAXException("View3DXMLHandler: is_camera=true, but no camera set...");
				
				current_view.camera.handleXMLElementStart(localName, attributes, null);
				}
			
			if (localName.equals("View3D")){
				current_view = new View3D(attributes.getValue("name"));
				}
			
			if (localName.equals("Camera3D")){
				if (current_view == null)
					throw new SAXException("View3DXMLHandler: Camera3D element started with no View3D...");
				Camera3D camera = new Camera3D();
				camera.handleXMLElementStart(localName, attributes, null);
				current_view.camera = camera;
				is_camera = true;
				}
			
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			
			if (localName.equals("View3D")){
				if (current_view == null)
					throw new SAXException("View3DXMLHandler: View3D element ended without being started...");
				views.add(current_view);
				current_view = null;
				}
			
			if (localName.equals("Camera3D")){
				if (current_view == null || current_view.camera == null)
					throw new SAXException("View3DXMLHandler: Camera3D element ended without being started...");
				is_camera = false;
				}
			
			
		}
		
	}
	
}