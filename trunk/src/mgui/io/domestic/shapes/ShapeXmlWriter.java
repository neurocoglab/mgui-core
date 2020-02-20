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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject.XMLEncoding;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.InterfaceIOOptions;
import mgui.io.standard.xml.XMLOutputOptions;

/************************************************
 * Writes an {@link InterfaceShape} object to XML. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeXmlWriter extends InterfaceShapeWriter {

	public ShapeXmlWriter(){
		
	}
	
	public ShapeXmlWriter(File file){
		dataFile = file;
	}
	
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		if (!(options instanceof ShapeOutputOptions)){
			InterfaceSession.log("InterfaceShapeWriter: options must be instance of InterfaceShapeOutputOptions..");
			return false;
			}
		
		ShapeOutputOptions _options = (ShapeOutputOptions)options;
		if (_options.getFiles() == null){
			InterfaceSession.log("InterfaceShapeWriter: No output directory specified.");
			return false;
			}
		
		File output_dir = _options.getFiles()[0];
		boolean success = true;
		
		for (int i = 0; i < _options.filenames.size(); i++){
			setFile(new File(output_dir.getAbsolutePath() + File.separator + _options.filenames.get(i)));
			xml_options = new XMLOutputOptions();
			xml_options.type = _options.types.get(i);
			xml_options.encoding = _options.encodings.get(i);
			try{
				success &= writeShape(_options.getShape(i), progress_bar);
			}catch (IOException ex){
				InterfaceSession.log("InterfaceShapeWriter: IOException writing shape to file\nDetails: " +
									 ex.getMessage(), 
									 LoggingType.Errors);
				success = false;
				}
			}
		
		return success;
	}
	
	XMLOutputOptions xml_options;
	
	@Override
	public boolean writeShape(InterfaceShape shape, ProgressUpdater progress_bar) throws IOException{
		if (dataFile == null)
			throw new IOException("No output file specified.");
		if (dataFile.exists() && !dataFile.delete())
			throw new IOException("Could not delete existing file '" + dataFile.getAbsolutePath() +"'.");
		
		BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
		
		if (xml_options == null){
			// Use default options
			xml_options = new XMLOutputOptions();
			}
		
		writer.write(XMLFunctions.getXMLHeader());
		shape.writeXML(0, writer, xml_options, progress_bar);
		
		writer.close();
		return true;
	}
	
	
	
}
