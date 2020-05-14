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

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;

import mgui.interfaces.InterfacePanel;


/*********************************************
 * Extends <code>ToolInputAdapter</code> to respond specifically to mouse events.
 * 
 * @author Andrew Reid
 * @since 1.0
 * @version 1.0
 *
 */
public class ToolInputMouseAdapter extends ToolInputAdapter {

	public MyAdapter mouseAdapter = new MyAdapter();
	
	public ToolInputMouseAdapter(){
		
	}
	
	@Override
	public void fireToolEvent(ToolInputEvent e){
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).handleToolEvent(e);
	}
	
	@Override
	public ToolMouseAdapter getMouseAdapter(){
		return mouseAdapter;
	}
	
	//	need to declare a MouseInputAdapter class
	class MyAdapter extends ToolMouseAdapter{
		@Override
		public void mouseMoved(MouseEvent e) {
			if (!(e.getSource() instanceof InterfacePanel)) return;
			fireToolEvent(new ToolInputEvent((InterfacePanel)e.getSource(), ToolConstants.TOOL_MOUSE_MOVED, e.getPoint()));
		}
		@Override
		public void mouseClicked(MouseEvent e) {
			if (!(e.getSource() instanceof InterfacePanel)) return;
			if (SwingUtilities.isRightMouseButton(e))
				fireToolEvent(new ToolInputEvent((InterfacePanel)e.getSource(), ToolConstants.TOOL_MOUSE_RCLICKED, e.getPoint(),
						e.isControlDown(), e.isShiftDown()));
			else if (SwingUtilities.isMiddleMouseButton(e))
				fireToolEvent(new ToolInputEvent((InterfacePanel)e.getSource(), ToolConstants.TOOL_MOUSE_MCLICKED, e.getPoint(),
						e.isControlDown(), e.isShiftDown()));
			else if (e.getClickCount() == 1)
				fireToolEvent(new ToolInputEvent((InterfacePanel)e.getSource(), ToolConstants.TOOL_MOUSE_CLICKED, e.getPoint(),
						e.isControlDown(), e.isShiftDown()));
			else if (e.getClickCount() > 1)
				fireToolEvent(new ToolInputEvent((InterfacePanel)e.getSource(), ToolConstants.TOOL_MOUSE_DCLICKED, e.getPoint(),
						e.isControlDown(), e.isShiftDown()));
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			if (!(e.getSource() instanceof InterfacePanel)) return;
			if (SwingUtilities.isMiddleMouseButton(e))
				//this is a middle button drag
				fireToolEvent(new ToolInputEvent((InterfacePanel)e.getSource(), ToolConstants.TOOL_MOUSE_MDRAGGED, e.getPoint(),
						e.isControlDown(), e.isShiftDown()));
			if (SwingUtilities.isRightMouseButton(e))
				//this is a right button drag
				fireToolEvent(new ToolInputEvent((InterfacePanel)e.getSource(), ToolConstants.TOOL_MOUSE_RDRAGGED, e.getPoint(),
						e.isControlDown(), e.isShiftDown()));
			if (SwingUtilities.isLeftMouseButton(e))
				//this is a left button drag
				fireToolEvent(new ToolInputEvent((InterfacePanel)e.getSource(), ToolConstants.TOOL_MOUSE_DRAGGED, e.getPoint(),
						e.isControlDown(), e.isShiftDown()));
		}
		@Override
		public void mouseWheelMoved(MouseWheelEvent e){
			if (!(e.getSource() instanceof InterfacePanel)) return;
			ToolInputEvent thisEvent;
			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL){
				thisEvent = new ToolInputEvent((InterfacePanel)e.getSource(), ToolConstants.TOOL_MOUSE_WHEEL, e.getPoint());
				thisEvent.setVal(-1 * e.getWheelRotation() * (e.getScrollAmount()));
				fireToolEvent(thisEvent);
				}
		}
		@Override
		public void mousePressed(MouseEvent e){
			if (!(e.getSource() instanceof InterfacePanel)) return;
			fireToolEvent(new ToolInputEvent((InterfacePanel)e.getSource(), ToolConstants.TOOL_MOUSE_DOWN, e.getPoint()));
		}
		@Override
		public void mouseReleased(MouseEvent e){
			if (!(e.getSource() instanceof InterfacePanel)) return;
			fireToolEvent(new ToolInputEvent((InterfacePanel)e.getSource(), ToolConstants.TOOL_MOUSE_UP, e.getPoint()));
		}
		
	}
	
}