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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.pipelines.InterfacePipeline;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/*******************************************************
 * Loads a pipeline (or multiple pipelines) from file. Default format is XML.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineLoader extends FileLoader {

	public PipelineLoader(){
		
	}
	
	public PipelineLoader(File file){
		setFile(file);
	}
	
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		PipelineInputOptions _options = (PipelineInputOptions)options;
		
		File[] files = _options.getFiles();
		boolean success = true;
		ArrayList<InterfacePipeline> pipelines = new ArrayList<InterfacePipeline>();
		
		for (int i = 0; i < files.length; i++){
			try{
				setFile(files[i]);
				ArrayList<InterfacePipeline> _pipelines = (ArrayList<InterfacePipeline>)loadObject(progress_bar, null);
				if (_pipelines == null)
					success = false;
				else
					pipelines.addAll(_pipelines);
				
			}catch (IOException e){
				success = false;
				}
			}
	
		for (int i = 0; i < pipelines.size(); i++)
			success &= InterfaceSession.getWorkspace().addPipeline(pipelines.get(i));
		
		return success;
	}

	@Override
	public Object loadObject(ProgressUpdater progressBar, InterfaceIOOptions options) throws IOException {
		
		try{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			PipelineXMLHandler handler = new PipelineXMLHandler();
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(dataFile)));
			return handler.getPipelines();
			
		}catch (Exception e){
			//e.printStackTrace();
			InterfaceSession.log("Exception loading pipeline. Details: " + e.getMessage(),
								 LoggingType.Errors);
			return null;
			}
		
	}

}