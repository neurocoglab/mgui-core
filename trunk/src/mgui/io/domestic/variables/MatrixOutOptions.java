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

package mgui.io.domestic.variables;

import java.io.File;

import javax.swing.JFileChooser;

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.io.InterfaceIOOptions;
import Jama.Matrix;

public class MatrixOutOptions extends InterfaceIOOptions {

	public static final int FORMAT_ASCII_FULL = 0;
	public static final int FORMAT_BINARY_FULL = 1;
	public static final int FORMAT_ASCII_SPARSE = 2;
	public static final int FORMAT_BINARY_SPARSE = 3;
	
	public int format = FORMAT_ASCII_FULL;
	public File[] files;
	public Matrix[] matrices;
	public InterfaceDisplayPanel displayPanel;
	public int precision = 6;
	public String string_format = "#0.0000###";
	public boolean has_header = true;
	public String delimiter = "\t";
	
	public MatrixOutOptions(){
		
	}
	
	public MatrixOutOptions(int format){
		this.format = format;
	}
	
	public InterfaceDisplayPanel getDisplayPanel() {
		return displayPanel;
	}

	public JFileChooser getFileChooser() {
		// TODO Auto-generated method stub
		return null;
	}

	public JFileChooser getFileChooser(File f) {
		// TODO Auto-generated method stub
		return null;
	}

	public File[] getFiles() {
		return files;
	}
	
	public Matrix[] getMatrices(){
		return matrices;
	}

	public void setDisplayPanel(InterfaceDisplayPanel p) {
		displayPanel = p;
	}

	public void setFiles(File[] files) {
		this.files = files;
	}

	public void setMatrices(Matrix[] matrices){
		this.matrices = matrices;
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		matrices = new Matrix[1];
		matrices[0] = (Matrix)obj;
	}
	
}