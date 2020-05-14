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

package mgui.io.domestic.views;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.View3D;
import mgui.interfaces.logs.LoggingType;
import mgui.io.InterfaceIOOptions;

/***********************************************
 * Options for writing a set of View3D objects to file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class View3DOutputOptions extends InterfaceIOOptions {

	public ArrayList<View3D> views = new ArrayList<View3D>();
	File[] files;
	
	public View3DOutputOptions(){
		
	}
	
	public View3DOutputOptions(ArrayList<View3D> views){
		this.views = views;
	}
	
	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}
	
	@Override
	public JFileChooser getFileChooser(){
		if (files == null || files.length == 0)
			return getFileChooser(null);
		return getFileChooser(files[0]);
	}
	
	@Override
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Select output file for View3D object(s)");
		return fc;
	}

	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		views = new ArrayList<View3D>();
		views.add((View3D)obj);
	}
	
}