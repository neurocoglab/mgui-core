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

package mgui.io.domestic.shapes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.InterfaceIOOptions;
import mgui.io.standard.xml.XMLOutputOptions;

/*********************************************
 * Specifies options for writing a {@link ShapeModel3D} object to XML.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class ShapeModel3DOutputOptions extends XMLOutputOptions {

	//public ShapeModel3D model;
	public HashMap<InterfaceShape,XMLOutputOptions> shape_xml_options = new HashMap<InterfaceShape,XMLOutputOptions>(); 		// Options used for XML
	//public HashMap<InterfaceShape,InterfaceIOOptions> shape_io_options = new HashMap<InterfaceShape,InterfaceIOOptions>();		// Options used for by-reference
	public HashMap<InterfaceShape,Boolean> include_shape = new HashMap<InterfaceShape,Boolean>();
	public boolean as_subfolders = false;
	public String shapes_folder = "";			// Root shapes directory, for by-reference writes; 
												// this is relative to the parent of the XML file
	public boolean gzip_xml = true; 			// Compress final XML file?
	public boolean overwrite_existing = true; 	// Clobber?
	
	public ShapeModel3DOutputOptions(){
		
	}
	
	public ShapeModel3DOutputOptions(ShapeModel3D model){
		setFromModel(model);
	}
	
	public ShapeModel3D getModel(){
		return (ShapeModel3D)object;
	}
	
	public void setModel(ShapeModel3D model){
		this.object = model;
	}
	
	/***********************************************
	 * Returns {@code true} if this options contains any shapes to be written by reference
	 * 
	 * @return
	 */
	public boolean containsByReferenceShapes(){
		ArrayList<InterfaceShape> shapes = new ArrayList<InterfaceShape>(include_shape.keySet());
		for (int i = 0; i < shapes.size(); i++){
			InterfaceShape shape = shapes.get(i);
			if (include_shape.get(shape)){
				XMLOutputOptions options = shape_xml_options.get(shape);
				if (options.type == XMLType.Reference) return true;
				}
			}
		return false;
	}
	
	public void setFromOptions(ShapeModel3DOutputOptions options){
		
		object = options.getModel();
		shape_xml_options = new HashMap<InterfaceShape,XMLOutputOptions>(options.shape_xml_options);
		//shape_io_options = new HashMap<InterfaceShape,InterfaceIOOptions>(options.shape_io_options);
		include_shape = new HashMap<InterfaceShape,Boolean>(options.include_shape);
		as_subfolders = options.as_subfolders;
		shapes_folder = options.shapes_folder;
		gzip_xml = options.gzip_xml;
		overwrite_existing = options.overwrite_existing;
		
	}
	
	/******************************************************
	 * Set this options from the given model, resetting all values
	 * 
	 * @param model
	 */
	public void setFromModel(ShapeModel3D model){
		setFromModel(model, true);
	}
	
	/****************************************************
	 * Set this options from the given model, resetting if necessary
	 * 
	 * @param model
	 * @param reset 		Whether to reset all values, or preserve existing values
	 */
	public void setFromModel(ShapeModel3D model, boolean reset){
		this.object = model;
		
		if (model == null){
			shape_xml_options = null;
			//shape_io_options = null;
			include_shape = null;
			return;
			}
		
		// Set shape options
		ShapeSet3DInt model_set = model.getModelSet();
		ArrayList<InterfaceShape> shapes = model_set.getMembers(true);
		
		if (reset || shape_xml_options == null)
			shape_xml_options = new HashMap<InterfaceShape,XMLOutputOptions>();
		//shape_io_options = new HashMap<InterfaceShape,InterfaceIOOptions>();
		if (reset || include_shape == null)
			include_shape = new HashMap<InterfaceShape,Boolean>();
		
		for (int i = 0; i < shapes.size(); i++){
			InterfaceShape shape = shapes.get(i);
			if (shape_xml_options.get(shape) == null){
				XMLOutputOptions xml_options = new XMLOutputOptions();
				shape_xml_options.put(shape, xml_options);
				xml_options.io_options = shape.getWriterOptions();
				}
			if (include_shape.get(shape) == null){
				include_shape.put(shape, true);
				}
			}
	}
	
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Select output file for ShapeModel3D");
		return fc;
	}

//	@Override
//	public void setObject(InterfaceObject obj) throws ClassCastException{
//		model = (ShapeModel3D)obj;
//	}
	
}