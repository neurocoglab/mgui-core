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

package mgui.io.domestic.shapes;

import java.io.File;

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.InterfaceIOOptions;

/*******************************************************
 * Options for loading a {@linkplain PolygonSet3DInt} object. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PolygonSet3DInputOptions extends ShapeInputOptions {

	//public String shapeSet;
	public File[] files;
	public String[] names;
	public InterfaceDisplayPanel display_panel;
	public boolean skip_lines;
	public int skip = 1;
	public boolean skip_min_nodes;
	public int min_nodes = 1;
	public boolean new_shape_set = false;
	public String new_shape_set_name = "";
	
	public PolygonSet3DInputOptions(){
		
	}
	
	public InterfaceDisplayPanel getDisplayPanel() {
		return display_panel;
	}

	public JFileChooser getFileChooser() {
		return getFileChooser(null);
	}

	public JFileChooser getFileChooser(File f) {
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Select polygon files to input");
		return fc;
	}


	public void setDisplayPanel(InterfaceDisplayPanel p) {
		display_panel = p;
	}

	public boolean isMultiFileEnabled() {
		return true;
	}

	public File[] getFiles() {
		return files;
	}

	public void setFiles(File[] files) {
		this.files = files;
		names = new String[files.length];
		for (int i = 0; i < files.length; i++)
			names[i] = files[i].getName();
			
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		InterfaceSession.log(this.getClass().getCanonicalName() + ": setObject not implemented.", LoggingType.Warnings);
	}

}