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

package mgui.io.domestic.shapes;

import java.io.File;

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.io.InterfaceIOOptions;

/*************************************************************
 * Options for loading a {@linkplain ShapeModel3D} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeModel3DInputOptions extends InterfaceIOOptions {

	//InterfaceDisplayPanel displayPanel;
	File[] files;
	public ShapeModel3D merge_with_model;
	public boolean merge_model_set = true;
	//public boolean rename_shapes = true;
	public String existing_shapes = "Rename";
	
	public ShapeModel3DInputOptions(){
		if (InterfaceSession.isInit())
			merge_with_model = InterfaceSession.getDisplayPanel().getCurrentShapeModel();
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
		
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDialogTitle("Select input file for ShapeModel3D");
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
		merge_with_model = (ShapeModel3D)obj;
	}
	
}