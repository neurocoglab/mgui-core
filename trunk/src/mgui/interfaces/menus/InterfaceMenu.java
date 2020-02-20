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

package mgui.interfaces.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class InterfaceMenu extends JMenu implements ActionListener {

	public PopupMenuObject object;
	String action_command;
	
	public InterfaceMenu(PopupMenuObject object){
		this(null, object);
	}
	
	public InterfaceMenu(String label, PopupMenuObject object){
		super(label);
		this.object = object;
	}
	
	public void addMenuItem(JMenuItem item){
		super.add(item);
		item.addActionListener(this);
	}
	
	public void removeMenuItem(JMenuItem item){
		super.remove(item);
		item.removeActionListener(this);
	}
	
	public void addMenuItem(JMenuItem parent, JMenu child){
		parent.add(child);
		child.addActionListener(this);
	}
	
	public void removeMenuItem(JMenuItem parent, JMenu child){
		parent.remove(child);
		child.removeActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e){
		if (object == null) return;
		object.handlePopupEvent(new ActionEvent(e.getSource(), e.getID(), action_command));
	}
	
}