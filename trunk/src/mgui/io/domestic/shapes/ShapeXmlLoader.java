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
import java.io.FileReader;
import java.io.IOException;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.io.domestic.shapes.xml.InterfaceShapeXMLHandler;
import mgui.io.util.IoFunctions;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import foxtrot.Job;
import foxtrot.Worker;

/*********************************************************
 * Loader for an {@linkplain InterfaceShape} stored in domestic XML format.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeXmlLoader extends InterfaceShapeLoader {

	public ShapeXmlLoader(){
		
	}
	
	public ShapeXmlLoader(File dataFile){
		this.setFile(dataFile);
	}
	
	@Override
	public InterfaceShape loadShape(final ShapeInputOptions options, final ProgressUpdater progress_bar) throws IOException {
		
		if (progress_bar == null)
			return loadShapeBlocking(options, null);
		
		return (InterfaceShape)Worker.post(new Job(){
			
			public InterfaceShape run(){
				return loadShapeBlocking(options, progress_bar);
			}
			
		});
		
	}
	
	protected InterfaceShape loadShapeBlocking(ShapeInputOptions options,
 			 								   ProgressUpdater progress_bar) {
		
		if (options == null){
			InterfaceSession.log("ShapeXmlLoader: Options not set..");
			return null;
			}
		
		if (dataFile == null){
			InterfaceSession.log("ShapeXmlLoader: Input file not set..");
			return null;
			}
		
		if (!dataFile.exists()){
			InterfaceSession.log("ShapeXmlLoader: Input file '" + dataFile.getAbsolutePath() + "' not found.");
			return null;
			}
		
		try{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			InterfaceShapeXMLHandler handler = new InterfaceShapeXMLHandler();
			
			File write_file = dataFile;
			
			// Is it compressed? If so, decompress to a temporary file
			if (dataFile.getAbsolutePath().endsWith(".gz")){
				String temp = dataFile.getAbsolutePath();
				temp = temp + ".~";
				write_file = IoFunctions.unzipFile(dataFile, new File(temp));
				}
			
			handler.setRootDir(dataFile.getParent());
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(write_file)));
			
			// Clean up if necessary
			if (dataFile.getAbsolutePath().endsWith(".gz")){
				write_file.delete();
				}
			
			return handler.getShape();
			
		}catch (Exception e){
			InterfaceSession.handleException(e);
			return null;
			}
	}
	
	

}
