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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/****************************************
 * Interface panel displaying a list of tool-related buttons.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public class InterfaceButtonPanel extends InterfacePanel implements ActionListener {

	public InterfaceDisplayPanel targetPanel;
	
	public InterfaceButtonPanel(){
		super();
		init();
	}
	
	@Override
	protected void init(){
		//set up stuff
		this.setLayout(new GridLayout(1,1));
	}
	
	public void setTargetPanel(InterfaceDisplayPanel thisPanel){
		targetPanel = thisPanel;
	}
	
	public void addButton(InterfaceButton thisButton){
		thisButton.addActionListener(this);
		this.setLayout(new GridLayout(this.getComponentCount() + 1, 1));
		this.add(thisButton);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		if (targetPanel == null) return;
		InterfaceButton thisButton = (InterfaceButton)e.getSource();
		targetPanel.setCurrentTool(thisButton.thisTool);
	}
	
	@Override
	public String toString(){
		return "Button Panel";
	}
}