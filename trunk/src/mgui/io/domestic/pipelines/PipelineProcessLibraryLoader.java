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

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.pipelines.libraries.PipelineProcessLibrary;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/****************************************************
 * Loads a {@link PipelineProcessLibrary} from file. Default format is XML.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineProcessLibraryLoader extends FileLoader {

	public PipelineProcessLibraryLoader(){
		
	}
	
	public PipelineProcessLibraryLoader(File file){
		setFile(file);
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progressBar) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public PipelineProcessLibrary loadLibrary() {
		return loadLibrary(null,null);
	}

	public PipelineProcessLibrary loadLibrary(ProgressUpdater progress, InterfaceIOOptions options) {
		try{
			return (PipelineProcessLibrary)loadObject(progress, options);
		}catch (IOException ex){
			InterfaceSession.handleException(ex);
			return null;
			}
		
	}
	
	@Override
	public Object loadObject(ProgressUpdater progressBar, InterfaceIOOptions options) throws IOException {
		
		try{
			XMLReader reader = XMLReaderFactory.createXMLReader();
			PipelineProcessLibraryXMLHandler handler = new PipelineProcessLibraryXMLHandler();
			reader.setContentHandler(handler);
			reader.setErrorHandler(handler);
			reader.parse(new InputSource(new FileReader(dataFile)));
			
			PipelineProcessLibrary library = new PipelineProcessLibrary(handler.library_name);
			
			for (int i = 0; i < handler.processes.size(); i++)
				library.addProcess(handler.processes.get(i));
			
			library.setEnvironment(handler.environment);
			
			return library;
		}catch (SAXException e){
			throw new IOException (e.getMessage());
			}
	}

}