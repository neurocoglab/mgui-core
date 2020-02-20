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
import java.awt.event.MouseEvent;

/*****************************************
 * Interface which should be implemented for all graphical objects which require popup menu
 * functionality. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface PopupMenuObject {

	/**********************************************
	 * Produces and returns a popup menu for this object.
	 * 
	 * @return
	 */
	public InterfacePopupMenu getPopupMenu();
	
	/**********************************************
	 * Handles an event on this object's popup menu.
	 * 
	 * @param e
	 */
	public void handlePopupEvent(ActionEvent e);
	
	/**********************************************
	 * Shows a popup menu at the point of the given {@link MouseEvent}.
	 * 
	 * @param e
	 */
	public void showPopupMenu(MouseEvent e);
	
}