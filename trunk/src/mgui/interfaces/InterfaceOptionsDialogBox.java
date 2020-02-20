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

package mgui.interfaces;

import javax.swing.JFrame;

/**************************************
 * Extension of <code>InterfaceDialogBox</code> which provides a constructor accepting an instance
 * of <code>InterfaceOptions</code> as an argument, which should be used as a means to transfer information
 * between the dialog and its parent.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * @see mgui.interfaces.InterfaceDialogBox InterfaceDialogBox
 * @see mgui.interfaces.InterfaceOptions InterfaceOptions
 *
 */

@SuppressWarnings("serial")
public abstract class InterfaceOptionsDialogBox extends InterfaceDialogBox {

	public InterfaceOptions options;
	
	public InterfaceOptionsDialogBox(){
		super();
	}
	
	public InterfaceOptionsDialogBox(JFrame aFrame, InterfaceOptions options){
		super(aFrame);
		setLocationRelativeTo(aFrame);
		this.options = options;
	}
	
	public InterfaceOptions getOptions(){
		return options;
	}
	
}