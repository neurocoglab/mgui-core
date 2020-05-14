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

package mgui.interfaces.io;

import java.util.ArrayList;

import javax.swing.JFrame;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceObject;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;

/********************************************************************
 * Base class for all dialog boxes for specifying I/O options.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class InterfaceIODialogBox extends InterfaceDialogBox {

	protected InterfaceIOPanel io_panel;
	protected InterfaceIOOptions options;
	// List of objects to be considered for I/O operations; can be null, in which case
	// all objects in a specified container are viable.
	protected ArrayList<InterfaceObject> io_objects;
	
	public InterfaceIODialogBox(){
		super();
		buttonType = BT_OK_CANCEL;
	}
	
	public InterfaceIODialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame);
		setIOPanel(panel);
		setOptions(opts);
		buttonType = BT_OK_CANCEL;
		setLocationRelativeTo(frame);
	}
	
	/******************************************************
	 * Explicitly set the objects available for I/O. If this value is {@code null}, the dialog
	 * must have a default source (e.g., ShapeModel3D) from which to select objects. Subclasses
	 * should override this method to customize their behaviour in response to explicit versus
	 * open source objects specification.
	 * 
	 * @param objects
	 */
	public void setIOObjects(ArrayList<InterfaceObject> objects){
		io_objects = objects;
	}
	
	public void setIOPanel(InterfaceIOPanel panel){
		io_panel = panel;
	}
	
	public void setOptions(InterfaceIOOptions options){
		this.options = options;
	}
	
	/******
	 * Override to update dialog from options before showing
	 */
	public void showDialog(){
		setVisible(true);
	}
	
	
}