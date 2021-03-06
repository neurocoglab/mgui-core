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

package mgui.interfaces.tools.dialogs;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceOptions;


public abstract class DialogToolDialogBox extends InterfaceDialogBox {

	protected InterfaceOptions options;
	//protected InterfaceDisplayPanel display_panel;
	
	public InterfaceOptions getOptions(){
		return options;
	}
	
	
	protected DialogToolDialogBox(JFrame frame, InterfaceOptions options){
		super(frame);
		this.options = options;
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
			
		if (e.getActionCommand().equals(DLG_CMD_CANCEL)){
			options = null;
			this.setVisible(false);
			}
	
	}
	
	
}