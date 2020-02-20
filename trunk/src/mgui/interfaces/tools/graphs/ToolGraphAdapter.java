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

package mgui.interfaces.tools.graphs;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;

import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputAdapter;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.ToolMouseAdapter;
import mgui.interfaces.tools.ToolMouseEvent;


public class ToolGraphAdapter extends ToolInputAdapter {

	protected MyAdapter mouseAdapter = new MyAdapter();
	
	public ToolGraphAdapter(){
		
	}
	
	@Override
	public ToolMouseAdapter getMouseAdapter(){
		return mouseAdapter;
	}
	
	class MyAdapter extends ToolMouseAdapter{
		@Override
		public void mouseMoved(MouseEvent e) {
			//handleEvent(TOOL_MOUSE_MOVED, (InterfaceGraphic2D)e.getSource(), e.getPoint());
			fireToolEvent(new ToolMouseEvent(e, ToolConstants.TOOL_MOUSE_MOVED));
		}
		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e))
				//handleEvent(TOOL_MOUSE_RCLICKED, (InterfaceGraphic2D)e.getSource(), e.getPoint());
				fireToolEvent(new ToolMouseEvent(e, ToolConstants.TOOL_MOUSE_RCLICKED));
			else if (SwingUtilities.isMiddleMouseButton(e))
				fireToolEvent(new ToolMouseEvent(e, ToolConstants.TOOL_MOUSE_MCLICKED));
			else if (e.getClickCount() == 1)
				//handleEvent(TOOL_MOUSE_CLICKED, (InterfaceGraphic2D)e.getSource(), e.getPoint());
				fireToolEvent(new ToolMouseEvent(e, ToolConstants.TOOL_MOUSE_CLICKED));
			else if (e.getClickCount() > 1)
				//handleEvent(TOOL_MOUSE_DCLICKED, (InterfaceGraphic2D)e.getSource(), e.getPoint());
				fireToolEvent(new ToolMouseEvent(e, ToolConstants.TOOL_MOUSE_DCLICKED));
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			//handleEvent(TOOL_MOUSE_DRAGGED, (InterfaceGraphic2D)e.getSource(), e.getPoint());
			if (SwingUtilities.isMiddleMouseButton(e))
				//this is a middle button drag
				fireToolEvent(new ToolMouseEvent(e, ToolConstants.TOOL_MOUSE_MDRAGGED));
			if (SwingUtilities.isRightMouseButton(e))
				//this is a right button drag
				fireToolEvent(new ToolMouseEvent(e, ToolConstants.TOOL_MOUSE_RDRAGGED));
			if (SwingUtilities.isLeftMouseButton(e))
				//this is a left button drag
				fireToolEvent(new ToolMouseEvent(e, ToolConstants.TOOL_MOUSE_DRAGGED));
		}
		@Override
		public void mouseWheelMoved(MouseWheelEvent e){
			ToolInputEvent thisEvent;
			if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL){
				thisEvent = new ToolMouseEvent(e, ToolConstants.TOOL_MOUSE_WHEEL);
				thisEvent.setVal(-1 * e.getWheelRotation() * (e.getScrollAmount()));
				fireToolEvent(thisEvent);
				}
		}
		@Override
		public void mousePressed(MouseEvent e){
			fireToolEvent(new ToolMouseEvent(e, ToolConstants.TOOL_MOUSE_DOWN));
		}
		@Override
		public void mouseReleased(MouseEvent e){
			fireToolEvent(new ToolMouseEvent(e, ToolConstants.TOOL_MOUSE_UP));
		}
		
	}
	
}