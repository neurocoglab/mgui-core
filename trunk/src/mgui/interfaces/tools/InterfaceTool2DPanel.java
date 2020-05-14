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

package mgui.interfaces.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JLabel;

import mgui.interfaces.InterfaceButton;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;


/*********
 * Panel to display option of the various 2D tools, organized by:
 * <p>
 * 1. Viewing tools
 * <ul>
 * <li> a. Pan
 * <li>	b. Zoom real-time
 * <li>	c. Zoom window
 * <li>	d. Zoom model extents
 * <li>	e. Zoom section extents
 * <li>	f. Zoom selection
 * <li>	g. Insert/remove Graphic2D window
 * </ul>
 * 2. Drawing tools
 * <ul>
 * <li>	a. Draw polygon
 * <li> b. Draw image
 * <li>	c. Draw [other shapes]...
 * </ul>
 * 3. Shape editing tools
 * <ul>
 * <li>	a. Select shape(s)
 * <li>	b. Delete shape(s)
 * <li>	c. Copy shape(s)
 * <li>	d. Cut shape(s)
 * <li>	e. Paste shape(s)
 * </ul>
 * 4. Node editing tools
 * <ul>
 * <li>	a. Select node(s)
 * <li>	b. Move node(s)
 * <li>	c. Insert node 
 * <li>	d. Delete node(s)
 * </ul>
 * 5. Transform tools
 * <ul>
 * <li>	a. Translate shape
 * <li>	b. Rotate shape
 * <li>	c. Scale shape
 * </ul>
 * 
 * @author Andrew Reid
 * @version 1.0
 * @date 09/26/2006
 * @deprecated
 */


@Deprecated
public class InterfaceTool2DPanel extends InterfacePanel implements ActionListener {

	//categories
	public ArrayList<String> categories = new ArrayList<String>();
	
	//controls
	public ArrayList<Control> buttons = new ArrayList<Control>();
	public InterfaceDisplayPanel displayPanel;
	public InterfaceTool2DPanel(){
		
	}
	
	@Override
	protected void init(){
		
	}
	
	public void addCategory(String thisCat){
		for (int i = 0; i < categories.size(); i++)
			if (categories.get(i).compareTo(thisCat) == 0) return;
		categories.add(thisCat);
	}
	
	public void addButton(String thisCat, InterfaceButton thisButton){
		int c = -1;
		for (int i = 0; i < categories.size(); i++)
			if (categories.get(i).compareTo(thisCat) == 0) c = i;
		if (c >= 0){
			buttons.add(new Control(thisCat, thisButton));
			thisButton.addActionListener(this);
			}
	}
	
	public void setDisplayPanel(InterfaceDisplayPanel panel){
		displayPanel = panel;
	}
	
	public void updatePanel(){
		removeAll();
		setLayout(new LineLayout(20, 5, 200));
		int thisLine = 1;
		LineLayoutConstraints c;
		double thisCol;
		//for each category
		for (int i = 0; i < categories.size(); i++){
		//add JLabel
		c = new LineLayoutConstraints(thisLine, thisLine, 0.1, 0.9, 1);
		add(new JLabel(categories.get(i)), c);
		//for each button of type category
		thisCol = 0.1;
		for (int j = 0; j < buttons.size(); j++){
			//add button
			if (buttons.get(j).category.compareTo(categories.get(i)) == 0){
				thisLine ++;
				//c = new LineLayoutConstraints(thisLine, thisLine + 1, thisCol, 0.35, 1);
				c = new LineLayoutConstraints(thisLine, thisLine, 0.1, 0.8, 1);
				add(buttons.get(j).button, c);
				//if (thisCol < 0.2) 
				//	thisCol = 0.55;
				//else
				//	thisCol = 0.1;
				}
			}
		thisLine += 2;
		}
		//this.setPreferredSize(new Dimension(500,500));
		//this.updateUI();
		//updateDisplay();
	}
	
	public void actionPerformed(ActionEvent e) {
		if (displayPanel == null) return;
		InterfaceButton thisButton = (InterfaceButton)e.getSource();
		displayPanel.setCurrentTool(thisButton.thisTool);
	}
		
	@Override
	public String toString(){
		return "Tool 2D Panel";
	}
	
	@Override
	public void updateDisplay(){
		super.repaint();
		repaint();
	}
	
	
	class Control{
		public String category;
		public InterfaceButton button;
		
		public Control(String thisCat, InterfaceButton thisButton){
			category = thisCat;
			button = thisButton;
		}
	}
	
	
}