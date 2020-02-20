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

package mgui.interfaces.graphics.util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.event.MouseInputAdapter;

import mgui.interfaces.graphics.InterfaceGraphic;

/****************************************************************
 * Relays a mouse event from a child panel to the parent {@link InterfaceGraphic}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class MouseRelayListener extends MouseInputAdapter implements MouseWheelListener {
	
	InterfaceGraphic<?> parent;
	
	public MouseRelayListener(InterfaceGraphic<?> parent){
		this.parent = parent;
	}
	
	MouseEvent getEventClone(MouseEvent e){
		return new MouseEvent(parent, e.getID(), e.getWhen(), 
							  e.getModifiers(),	e.getX(), e.getY(), 
							  e.getClickCount(), e.isPopupTrigger(), 
							  e.getButton());
	}
	
	MouseWheelEvent getWheelEventClone(MouseWheelEvent e){
		return new MouseWheelEvent(parent, e.getID(), e.getWhen(), 
								   e.getModifiers(), e.getX(), e.getY(), 
								   e.getClickCount(), e.isPopupTrigger(), 
								   e.getScrollType(), e.getScrollAmount(),
								   e.getWheelRotation());
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		MouseEvent me = getEventClone(e);
		parent.dispatchEvent(me);
		}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		MouseEvent me = getEventClone(e);
		parent.dispatchEvent(me);
		}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		MouseWheelEvent me = getWheelEventClone(e);
		parent.dispatchEvent(me);
		}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		MouseEvent me = getEventClone(e);
		parent.dispatchEvent(me);
	} 
	
	@Override
	public void mousePressed(MouseEvent e){
		MouseEvent me = getEventClone(e);
		parent.dispatchEvent(me);
	}
	
	@Override
	public void mouseClicked(MouseEvent e){
		MouseEvent me = getEventClone(e);
		parent.dispatchEvent(me);
	}
} 						