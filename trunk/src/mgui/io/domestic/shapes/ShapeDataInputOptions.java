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

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.io.InterfaceIOOptions;

/*****************************************************************
 * Options for loading vertex-wise data into a shape. Allows multiple files with multiple columns.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeDataInputOptions extends InterfaceIOOptions {

	public enum Format{
		Double,
		Float,
		Integer,
		Short,
		Boolean;
	}
	
	public InterfaceShape shape;
	public String[] names;
	public File[] files;
	public String[][] columns;
	public boolean[][] load_column;
	public Format[][] formats;
	public boolean skip_header;
	public boolean as_one_column;
	
	public ShapeDataInputOptions(){
		
	}
	
	public File[] getFiles() {
		return files;
	}

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
	
	public String[] getNames(){
		return names;
	}
	
	public void setNames(String[] names){
		this.names = names;
	}
	
	public InterfaceShape getShape(){
		return shape;
	}
	
	public void setShape(Shape3DInt shape){
		this.shape = shape;
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
		fc.setMultiSelectionEnabled(true);
		fc.setDialogTitle("Select shape data files to input");
		return fc;
	}
	
	@Override
	public void setObject(InterfaceObject obj) throws ClassCastException{
		InterfaceSession.log(this.getClass().getCanonicalName() + ": setObject not implemented.", LoggingType.Warnings);
	}

}