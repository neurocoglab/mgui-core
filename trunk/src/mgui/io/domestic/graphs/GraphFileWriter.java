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

package mgui.io.domestic.graphs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;
import mgui.io.standard.xml.XMLOutputOptions;

/*********************************************************
 * Abstract class for all graph writers.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class GraphFileWriter extends FileWriter {

	protected XMLType xml_type = XMLType.Normal;
	
	/*****************************************
	 * Constructor setting the output file.
	 * 
	 * @param file
	 */
	public GraphFileWriter(File file){
		setFile(file);
	}
	
	/******************************************
	 * Sets the XML type for this writer.
	 * 
	 * @param type
	 */
	public void setXMLType(XMLType type){
		xml_type = type;
	}
	
	/******************************************
	 * Gets the current XML type for this writer.
	 * 
	 * @param type
	 */
	public XMLType getXMLType(){
		return xml_type;
	}
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		if (!(options instanceof GraphOutputOptions)){
			InterfaceSession.log("GraphFileWriter: options must be instance of GraphOutputOptions..");
			return false;
			}
		
		GraphOutputOptions _options = (GraphOutputOptions)options;
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
	
	@Override
	public String getTitle() {
		return "Write graph(s) to file";
	}

	/*******************************************************
	 * Writes this graph to file using its domestic XML representation.
	 * 
	 * @param graph
	 * @return
	 */
	public boolean writeGraph(InterfaceAbstractGraph graph, ProgressUpdater progress_bar) throws IOException{
		if (dataFile == null)
			throw new IOException("No output file specified.");
		if (dataFile.exists() && !dataFile.delete())
			throw new IOException("Could not delete existing file '" + dataFile.getAbsolutePath() +"'.");
		
		BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
		graph.writeXML(0, writer, new XMLOutputOptions(), progress_bar);
		
		return true;
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = GraphFileWriter.class.getResource("/mgui/resources/icons/graph_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/volume_3d_20.png");
		return null;
	}
}