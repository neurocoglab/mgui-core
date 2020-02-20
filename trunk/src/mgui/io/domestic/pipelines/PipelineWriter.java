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

package mgui.io.domestic.pipelines;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.pipelines.InterfacePipeline;

/**********************************************************
 * Writes an {@link InterfacePipeline} instance to file. Default format is XML.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineWriter extends FileWriter {

	public PipelineWriter(){
		
	}
	
	public PipelineWriter(File file){
		setFile(file);
	}
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progressBar) {
		
		
		File[] files = options.getFiles();
		ArrayList<InterfacePipeline> pipelines = ((PipelineOutputOptions)options).pipelines;
		boolean success = true;
		setFile(files[0]);
		
		for (int i = 0; i < pipelines.size(); i++){
			try{
				success = writePipelines(pipelines);
			}catch (Exception e){
				if (pipelines.get(i) != null)
					InterfaceSession.log("PipelineWriter: Exception writing pipeline '" + 
							pipelines.get(i).getName() + "' to file.");
				else
					InterfaceSession.log("PipelineWriter: null pipeline encountered...");
				success = false;
				}
			}
		return success;
		
	}

	/**********************************************
	 * Writes a pipeline to the current file.
	 * 
	 * @param pipeline
	 * @return
	 * @throws IOException
	 */
	public boolean writePipeline(InterfacePipeline pipeline) throws IOException{
		
		BufferedWriter writer = new BufferedWriter(new java.io.FileWriter (dataFile));
		pipeline.writeXML(0, writer, new XMLOutputOptions(), null);
		writer.close();
		return true;
		
	}
	
	/**********************************************
	 * Writes multiple pipelines to the current file.
	 * 
	 * @param pipelines
	 * @return
	 * @throws IOException
	 */
	public boolean writePipelines(ArrayList<InterfacePipeline> pipelines) throws IOException{
		
		BufferedWriter writer = new BufferedWriter(new java.io.FileWriter (dataFile));
		for (int i = 0; i < pipelines.size(); i++){
			pipelines.get(i).writeXML(0, writer, new XMLOutputOptions(), null);
			}
		writer.close();
		return true;
		
	}
	
}