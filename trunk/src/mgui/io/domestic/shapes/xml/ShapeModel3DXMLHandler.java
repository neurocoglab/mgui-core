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

import mgui.geometry.util.SpatialUnit;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet3DInt;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/****************************************************
 * XML handler for a {@linkplain ShapeModel3D} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeModel3DXMLHandler extends DefaultHandler {

	protected ShapeModel3D shape_model;
	protected ShapeSet3DXMLHandler shape_set_xml_handler;
	protected boolean model_is_loading;
	protected String model_name, model_unit;
	protected String root_dir;
	
	/**************************************************
	 * Instantiates a handler with no pre-specified shape model. A new model will be
	 * created.
	 * 
	 * @param model
	 */
	public ShapeModel3DXMLHandler(){
		super();
	}
	
	/**************************************************
	 * Instantiates a handler with a specified {@linkplain ShapeModel3D}. This handler
	 * will expect a shape model as input.
	 * 
	 * @param model
	 */
	public ShapeModel3DXMLHandler(ShapeModel3D model){
		super();
		shape_model = model;
	}
	
	public ShapeModel3D getShapeModel(){
		return shape_model;
	}
	
	public void setRootDir(String dir){
		root_dir = dir;
	}
	
	public String getRootDir(){
		return root_dir;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		// If shape set handler is active, redirect
		if (shape_set_xml_handler != null){
			shape_set_xml_handler.startElement(uri, localName, qName, attributes);
			return;
			}
		
		if (localName.equals("ShapeModel3D")){
			if (model_is_loading)
				throw new SAXException("ShapeModel3DXMLHandler: Shape model encountered but one is already started.");
			
			if (shape_model != null){
				// Merging with existing model
				//model_set = shape_model.getModelSet();
			}else{
				// Create new model
				model_name = attributes.getValue("name");
				model_unit = attributes.getValue("unit");
				}
			
			model_is_loading = true;
			return;
			}
		
		if (localName.equals("ShapeSet3DInt")){
			if (!model_is_loading)
				throw new SAXException("ShapeModel3DXMLHandler: Model set encountered but no model has been started.");
			
			ShapeSet3DInt model_set = null; 
			if (shape_model != null)
				model_set = shape_model.getModelSet();
			if (model_set == null)
				model_set = new ShapeSet3DInt(attributes.getValue("name"));
			
			shape_set_xml_handler = new ShapeSet3DXMLHandler(model_set);
			shape_set_xml_handler.setRootDir(root_dir);

			return;
			}
		
		if (localName.equals("AttributeList")){
			// Shouldn't get attributes here
			throw new SAXException("ShapeModel3DXMLHandler: AttributeList encountered but no current set exists...");
			}
		
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		// If shape set handler is active, redirect
		if (shape_set_xml_handler != null){
			shape_set_xml_handler.endElement(uri, localName, qName);
			// Is it finished?
			if (!shape_set_xml_handler.isFinished())
				return;
			}
		
		if (localName.equals("ShapeModel3D")){
			// Done reading shape model
			// Model should be loading
			if (!model_is_loading)
				throw new SAXException("ShapeModel3DXMLHandler: Shape model ended but not started.");
			// Should not have an active handler
			if (shape_set_xml_handler != null)
				throw new SAXException("ShapeModel3DXMLHandler: Model set ended but shape set handler still active...");
			
			model_is_loading = false;
			
			return;
			}
		
		if (localName.equals("ShapeSet3DInt")){
			if (shape_set_xml_handler == null)
				throw new SAXException("ShapeModel3DXMLHandler: Shape set ended but not started.");
			
			// This is the model set; instantiate the model now
			SpatialUnit sunit = InterfaceEnvironment.getSpatialUnit(model_unit);
			ShapeSet3DInt shape_set = shape_set_xml_handler.getShapeSet();
			
			if (shape_model != null){
				shape_model.setModelSet(shape_set);
			}else{
				shape_model = new ShapeModel3D(model_name, 
											   shape_set, 
											   sunit);
				}
			
			shape_set_xml_handler = null;
			return;
			}
		
	}



	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		
		// If shape set handler is active, redirect
		if (shape_set_xml_handler != null){
			shape_set_xml_handler.characters(ch, start, length);
			return;
			}
		
	}
	
	
	
}