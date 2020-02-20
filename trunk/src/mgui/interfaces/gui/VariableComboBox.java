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

package mgui.interfaces.gui;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboPopup;

public class VariableComboBox extends JComboBox {

	protected int listWidth;
	PopupListener listener;
	
	public VariableComboBox(int width){
		super();
		listener = new PopupListener(width);
		this.addPopupMenuListener(listener);
	}
	
	public void setListWidth(int width){
		listener.width = width;
	}
	
	public int getListWidth(){
		return listener.width;
	}
	
}

class PopupListener implements PopupMenuListener{

	public int width;
	
	public PopupListener(int w){
		width = w;
	}
	
	public void popupMenuCanceled(PopupMenuEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		JComboBox box = (JComboBox)e.getSource();
		BasicComboPopup popup = (BasicComboPopup)box.getUI().getAccessibleChild(box, 0);
		if (width < 1) return;
		
		Dimension d = popup.getSize();
		d.width = width;
		popup.setSize(d);
		
	}
	
}