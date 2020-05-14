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
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.io.InterfaceIOOptions;

/*************************************************************
 * Output options for an {@linkplain InterfaceShapeWriter}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeOutputOptions extends InterfaceIOOptions {

	public ShapeModel3D shape_model;
	
	protected File[] files;
	public ArrayList<String> filenames;
	public ArrayList<InterfaceShape> shapes;
	
	public ArrayList<XMLType> types;
	public ArrayList<XMLEncoding> encodings;
	
	public InterfaceShape getShape(int i){
		return shapes.get(i);
	}
	
	@Override
	public File[] getFiles() {
		return files;
	}

	@Override
	public void setFiles(File[] files) {
		this.files = files;
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
		fc.setMultiSelectionEnabled(false);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fc.setDialogTitle("Select output folder for shape(s)");
		return fc;
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		shapes = new ArrayList<InterfaceShape>();
		shapes.add((InterfaceShape)obj);
		types = new ArrayList<XMLType>();
		types.add(XMLType.Full);
		encodings = new ArrayList<XMLEncoding>();
		encodings.add(XMLEncoding.Ascii);
	}
	
}