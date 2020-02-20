/*
* Copyright (C) 2011 Andrew Reid and the modelGUI Project <http://mgui.wikidot.com>
* 
* This file is part of modelGUI[core] (mgui-core).
* 
* modelGUI[core] is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* modelGUI[core] is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with modelGUI[core]. If not, see <http://www.gnu.org/licenses/>.
*/

package mgui.interfaces.layouts;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

/********************************************************
 * Frame to display a print layout window
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceLayoutFrame extends JFrame {

	public InterfaceLayoutPanel panel;
	public JScrollPane scroll_pane;
	
	public InterfaceLayoutFrame(InterfaceLayoutPanel panel, String title, Dimension size){
		super();
		this.panel = panel;
		scroll_pane = new JScrollPane(panel);
		this.setContentPane(scroll_pane);
		this.setTitle("Layout: " + title);
		this.setPreferredSize(size);
	}
	
	
}