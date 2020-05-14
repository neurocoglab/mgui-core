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

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.resources.icons.IconObject;

/******************************************************************
 * Interface for any class which performs an operation or set of operations on ModelGUI objects, which 
 * involves user interaction.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface Tool extends Cloneable, 
							  ToolInputListener,
							  IconObject,
							  PopupMenuObject {

	public String getName();
	public Object clone() throws CloneNotSupportedException;
	
	/**********************************
	 * Specifies whether this tool is immediate; this indicates that it should be run immediately upon
	 * calling and stop, rather than wait for AWT events.
	 * 
	 * @return
	 */
	public boolean isImmediate();
	public void addListener(ToolListener listener);
	public void removeListener(ToolListener listener);
	
	/**********************************
	 * Returns the tool that was set previous to this one, if one exists
	 * 
	 * @return
	 */
	public Tool getPreviousTool();
	
	/**********************************
	 * Should be called once a tool is activated within a particular InterfaceDisplayPanel.
	 * 
	 */
	public void activate();
	
	/**********************************
	 * Should be called once a tool is deactivated; instances of Tool should perform clean-up activities
	 * here.
	 * 
	 */
	public void deactivate();
	
	/**********************************
	 * Specifies whether this tool requires exclusively; otherwise it will run in parallel with the
	 * default tool, if one exists. 
	 * 
	 * @return
	 */
	public boolean isExclusive();

	public void setTargetPanel(InterfacePanel panel);
	
}