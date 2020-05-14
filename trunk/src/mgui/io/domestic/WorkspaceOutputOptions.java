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

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.InterfaceWorkspace;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.standard.xml.XMLOutputOptions;

/****************************************************
 * Specifies options for writing a workspace (i.e., an instance of {@link InterfaceWorkspace}) to XML.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class WorkspaceOutputOptions extends XMLOutputOptions {

	public WorkspaceOutputOptions(){
		setWorkspace();
	}
		
	public WorkspaceOutputOptions(XMLType type){
		setWorkspace();
		this.type = type;
	}
	
	void setWorkspace(){
		if (InterfaceSession.isInit())
			this.object = InterfaceSession.getWorkspace();
	}
	
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Select output file for Workspace");
		return fc;
	}

}