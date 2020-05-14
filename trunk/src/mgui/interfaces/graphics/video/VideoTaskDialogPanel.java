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

package mgui.interfaces.graphics.video;

import java.awt.event.ActionListener;

import mgui.interfaces.InterfacePanel;

/*********************************************************
 * A dialog defining a {@link VideoTask} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class VideoTaskDialogPanel extends InterfacePanel implements ActionListener {

	/**********************************
	 * Returns the number of lines in this panel
	 * 
	 * @return
	 */
	public abstract int getLineCount();
	
	/**********************************
	 * Updates the present task with values from this panel
	 * 
	 */
	public abstract void updateTask();
	
}