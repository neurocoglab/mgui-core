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
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

import mgui.interfaces.graphics.GraphicMouseListener;
import mgui.interfaces.gui.InterfaceProgressBar;

/****************************************
 * Interface panel which acts as a status bar for a given instance of <code>InterfaceFrame</code>.
 * Uses instances of <code>InterfaceTextBox</code> to update from graphic mouse events. Displays an 
 * instance of <code>InterfaceProgressBar</code> instead of its other components when one is registered. 
 * 
 * <p>TODO: revamp this class. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class InterfaceStatusBarPanel extends InterfacePanel {

	public ArrayList<InterfaceTextBox> boxes = new ArrayList<InterfaceTextBox>();
	
	public InterfaceStatusBarPanel(){
		super();
		init();
	}
	
	@Override
	protected void init(){
		//set up stuff
		this.setLayout(new GridLayout(1,1));
	}
	
	public void addTextBox(InterfaceTextBox thisText){
		boxes.add(thisText);
		this.setLayout(new GridLayout(1, this.getComponentCount() + 1));
		this.add(thisText);
	}
	
	//TODO: implement progress bar stack for tasks-within-tasks
	public void registerProgressBar(InterfaceProgressBar bar){
		this.removeAll();
		this.setLayout(new BorderLayout());
		bar.setSize(this.getWidth(), this.getHeight());
		bar.setPreferredSize(new Dimension(this.getWidth(), this.getHeight()));
		this.add(bar, BorderLayout.CENTER);
		this.validate();
		
		updateUI();
		
	}
	
	public void deregisterProgressBar(){
		this.removeAll();
		this.setLayout(new GridLayout(1, boxes.size()));
		for (int i = 0; i < boxes.size(); i++)
			add(boxes.get(i));
		//InterfaceSession.log("Progress bar deregistered");
		//this.paintImmediately(0, 0, this.getWidth(), this.getHeight());
		updateUI();
	}
	
	//remove any of this status bar's components that may exist in list
	public void removeListeners(ArrayList<GraphicMouseListener> list){
		Component[] clist = this.getComponents().clone();
		for (int i = 0; i < clist.length; i++)
			for (int j = 0; j < list.size(); j++)
				if (clist[i].equals(list.get(j)))
					this.remove(clist[i]);
	}
	
	//add this status bar's components to listeners list
	public void addListeners(ArrayList<GraphicMouseListener> list){
		//first remove any existing listeners
		removeListeners(list);
		
		//then add the components
		Component[] clist = this.getComponents().clone();
		for (int i = 0; i < clist.length; i++)
			if (clist[i] instanceof GraphicMouseListener)
				list.add((GraphicMouseListener)clist[i]);
	}
	
	
	
}