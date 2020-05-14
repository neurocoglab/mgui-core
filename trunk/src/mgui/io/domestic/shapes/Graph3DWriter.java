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

package mgui.io.domestic.shapes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.graphs.Graph3DInt;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;
import mgui.io.standard.xml.XMLOutputOptions;

/***************************************************
 * Writer for a {@linkplain Graph3DInt} object; default is to write using the domestic
 * XML representation.
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Graph3DWriter extends FileWriter {

	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		if (!(options instanceof Graph3DOutputOptions)){
			InterfaceSession.log("GraphFileWriter: options must be instance of GraphOutputOptions..");
			return false;
			}
		
		Graph3DOutputOptions _options = (Graph3DOutputOptions)options;
		boolean success = true;
		
		for (int i = 0; i < _options.files.length; i++){
			setFile(_options.files[i]);
			try{
				success &= writeGraph(_options.getGraph(i), progress_bar);
			}catch (IOException ex){
				InterfaceSession.log("GraphFileWriter: IOException writing graph to file\nDetails: " +
									 ex.getMessage(), 
									 LoggingType.Errors);
				success = false;
				}
			}
		
		
		return success;
	}

	/*******************************************************
	 * Writes this Graph3DInt to file using its domestic XML representation.
	 * 
	 * @param graph
	 * @return
	 */
	public boolean writeGraph(Graph3DInt graph, ProgressUpdater progress_bar) throws IOException{
		if (dataFile == null)
			throw new IOException("No output file specified.");
		if (dataFile.exists() && !dataFile.delete())
			throw new IOException("Could not delete existing file '" + dataFile.getAbsolutePath() +"'.");
		
		BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
		graph.writeXML(0, writer, new XMLOutputOptions(), progress_bar);
		
		return true;
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(Graph3DInt.class);
		return objs;
	}
	
}