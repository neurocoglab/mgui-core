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

package mgui.interfaces;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

/**************************************
 * Extension of <code>InterfaceOptionsDialogBox</code> which allows for tabs.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * @see mgui.interfaces.InterfaceOptionsDialogBox InterfaceOptionsDialogBox
 * @see mgui.interfaces.InterfaceDialogBox InterfaceDialogBox
 * @see mgui.interfaces.InterfaceOptions InterfaceOptions
 *
 */

@SuppressWarnings("serial")
public abstract class InterfaceOptionsTabbedDialogBox extends InterfaceOptionsDialogBox {

public JTabbedPane tabs = new JTabbedPane();
	
	public InterfaceOptionsTabbedDialogBox(){
		super();
	}

	public InterfaceOptionsTabbedDialogBox(JFrame aFrame, InterfaceOptions options){
		super(aFrame, options);
	}
	
	@Override
	protected void init(){
		super.init();
		
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(tabs, BorderLayout.CENTER);
		
	}
	
	public void addTab(String title, Component c){
		tabs.addTab(title, c);
	}
	
	
	
	
}