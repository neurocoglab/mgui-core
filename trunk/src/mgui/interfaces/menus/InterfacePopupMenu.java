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
import java.awt.event.MouseEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class InterfacePopupMenu extends JPopupMenu implements ActionListener{

	public PopupMenuObject object;
	
	public InterfacePopupMenu(PopupMenuObject object){
		this(null, object);
	}
	
	public InterfacePopupMenu(String label, PopupMenuObject object){
		super(label);
		this.object = object;
	}
	
	public void addMenuItem(JMenuItem item){
		super.add(item);
		item.setActionCommand(item.getName());
		item.addActionListener(this);
	}
	
	public void addSubmenu(JMenu menu){
		add(menu);
		registerSubmenus(menu);
	}
	
	public void addSubmenu(JMenu menu, String command){
		add(menu);
		registerSubmenus(menu, command);
	}
	
	//recursively register submenu items by addint his popup menu
	//as an action listener
	protected void registerSubmenus(JMenu menu){
		registerSubmenus(menu, menu.getText());
	}
	
	protected void registerSubmenus(JMenu menu, String command){
		for (int i = 0; i < menu.getItemCount(); i++){
			JMenuItem item = menu.getItem(i);
			String new_command = command + "." + item.getText();
			if (item instanceof JMenu)
				registerSubmenus((JMenu) item, new_command);
			else{
				item.setActionCommand(new_command); 
				item.addActionListener(this);
				}
			}
	}
	
	public void removeMenuItem(JMenuItem item){
		super.remove(item);
		item.removeActionListener(this);
		if (item instanceof JMenu)
			deregisterSubmenus((JMenu)item);
	}
	
	protected void deregisterSubmenus(JMenu menu){
		for (int i = 0; i < menu.getItemCount(); i++){
			JMenuItem item = menu.getItem(i);
			if (item instanceof JMenu)
				deregisterSubmenus((JMenu) item);
			else
				item.removeActionListener(this);
			}
	}
	
	public void addMenuItem(JMenuItem parent, JMenu child){
		parent.add(child);
		child.addActionListener(this);
		add(parent);
	}
	
	public void removeMenuItem(JMenuItem parent, JMenu child){
		parent.remove(child);
		child.removeActionListener(this);
	}
	
	public void actionPerformed(ActionEvent e){
		if (object == null) return;
		object.handlePopupEvent(e);
	}
	
	public void show(MouseEvent e){
		show(e.getComponent(), e.getX(), e.getY());
	}
	
	
}