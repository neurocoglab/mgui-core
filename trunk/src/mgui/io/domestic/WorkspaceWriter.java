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

package mgui.io.domestic;

import java.io.File;

import mgui.interfaces.InterfaceWorkspace;
import mgui.interfaces.ProgressUpdater;
import mgui.io.InterfaceIOOptions;
import mgui.io.standard.xml.XMLWriter;

/****************************************************
 * Writes a workspace (i.e., an instance of {@link InterfaceWorkspace}) to XML.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class WorkspaceWriter extends XMLWriter {

	public WorkspaceWriter(){
		
	}
	
	public WorkspaceWriter(File file){
		this.setFile(file);
	}
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		if (!(options instanceof WorkspaceOutputOptions)) return false;
		WorkspaceOutputOptions _options = (WorkspaceOutputOptions)options;
		File file = _options.file;
		if (file == null) return false;
		setFile(file);
		
		try{
			return writeObject(_options, progress_bar);
		}catch (Exception ex){
			ex.printStackTrace();
			}
		
		return false;
	}

}