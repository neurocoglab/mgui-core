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

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JScrollPane;

public class InterfaceListPanel extends InterfacePanel {

	JList thisList = new JList();
	JScrollPane thisPane;
	
	public InterfaceListPanel(){
		super();
		init();
	}
	
	@Override
	protected void init(){
		//set up stuff
		this.setLayout(new GridLayout(1, 1));
		thisPane = new JScrollPane(thisList);
		this.add(thisPane);
	}
	
	public void setList(ArrayList items){
		thisList.setListData(new Vector(items));
		updateDisplay();
	}
	
	@Override
	public void updateDisplay(){
		thisList.updateUI();
		repaint();
	}
}