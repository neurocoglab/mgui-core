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
import java.util.ArrayList;

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.shapes.graphs.Graph3DInt;
import mgui.io.InterfaceIOOptions;

/*************************************************************
 * Output options for a Graph3DWriter
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Graph3DOutputOptions extends InterfaceIOOptions {

	public File[] files;
	public String[] names;
	public ArrayList<Graph3DInt> graphs;
	
	public Graph3DInt getGraph(int i){
		return graphs.get(i);
	}
	
	@Override
	public File[] getFiles() {
		return files;
	}

	@Override
	public void setFiles(File[] files) {
		this.files = files;
		names = new String[files.length];
		for (int i = 0; i < files.length; i++){
			String name = files[i].getName();
			if (name.lastIndexOf(".") > 0)
				name = name.substring(0, name.lastIndexOf("."));
			names[i] = name;
			}
	}

	@Override
	public JFileChooser getFileChooser(){
		return getFileChooser(null);
	}
	
	@Override
	public JFileChooser getFileChooser(File f){
		JFileChooser fc = null;
		if (f != null)
			fc = new JFileChooser(f);
		else
			fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Select output folder for 3D graph shape(s)");
		return fc;
	}

	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		graphs = new ArrayList<Graph3DInt>();
		graphs.add((Graph3DInt)obj);
	}
	

}