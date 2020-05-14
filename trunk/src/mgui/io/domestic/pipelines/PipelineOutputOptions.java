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

import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.io.InterfaceIOOptions;
import mgui.pipelines.InterfacePipeline;

public class PipelineOutputOptions extends InterfaceIOOptions {

	File[] files;
	public ArrayList<InterfacePipeline> pipelines;
	
	public PipelineOutputOptions(){
		
	}
	
	public PipelineOutputOptions(ArrayList<InterfacePipeline> pipelines){
		this.pipelines = pipelines;
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
		fc.setMultiSelectionEnabled(false);
		fc.setDialogTitle("Write colour map file");
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
		pipelines = new ArrayList<InterfacePipeline>();
		pipelines.add((InterfacePipeline)obj);
	}
	
}