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

package mgui.io.domestic.maps;

import java.io.File;

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.NameMap;
import mgui.io.InterfaceIOOptions;

/*********************************************
 * Specifies options for writing a name map (or multiple name maps) to file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class NameMapOutOptions extends InterfaceIOOptions {

	public File[] files;
	public NameMap[] maps;
	public String[] names;
	public String delim = "\\t";
	
	public InterfaceDisplayPanel displayPanel;
	
	public void setDisplayPanel(InterfaceDisplayPanel p){
		displayPanel = p;
	}
	
	public InterfaceDisplayPanel getDisplayPanel(){
		return displayPanel;
	}
	
	public File getDir(){
		if (files == null || files.length == 0) return null;
		return files[0];
	}
	
	public JFileChooser getFileChooser(){
		return getFileChooser(null);
	}
	
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Select output directory for name map(s)");
		return fc;
	}

	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		maps = new NameMap[1];
		maps[0] = (NameMap)obj;
	}

}