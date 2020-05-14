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

package mgui.io.domestic.pipelines;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.pipelines.libraries.PipelineProcessLibrary;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;

/******************************************************
 * Writes a {@link PipelineProcessLibrary} to file. Default format is XML.
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PipelineProcessLibraryWriter extends FileWriter {

	public PipelineProcessLibraryWriter(){
		
	}
	
	public PipelineProcessLibraryWriter(File file){
		setFile(file);
	}
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progressBar) {
		
		
		
		return false;
	}
	
	public boolean writeLibrary(PipelineProcessLibrary library, ProgressUpdater progressBar) throws IOException {
		
		BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
		library.writeXML(0, writer, progressBar);
		writer.close();
		
		return true;
		
	}

}