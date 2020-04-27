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

package mgui.interfaces.tools;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import org.jogamp.java3d.Canvas3D;
import javax.swing.SwingUtilities;

import mgui.interfaces.graphics.InterfaceGraphic3D;
import mgui.interfaces.maps.Map3D;

import org.jogamp.java3d.utils.behaviors.vp.ViewPlatformAWTBehavior;


/**************
 * Adapter to act as bridge between AWT Behaviors and the Tools that respond to
 * them.
 * 
 * @author Andrew Reid
 *
 */
public class ToolBehavior3DAdapter extends ViewPlatformAWTBehavior {

	private ArrayList<ToolInputListener> listeners = new ArrayList<ToolInputListener>();
	public Map3D theMap;
 
	//public ToolMouseAdapter mouseAdapter = new ToolMouseAdapter();
	
	//	Types of tool events
	/**@TODO replace with enum **/
	public static final int TOOL_MOUSE_CLICKED = 1;
	public static final int TOOL_MOUSE_MOVED = 2;
	public static final int TOOL_MOUSE_DRAGGED = 3;
	public static final int TOOL_MOUSE_MDRAGGED = 10;
	public static final int TOOL_MOUSE_RDRAGGED = 11;
	public static final int TOOL_MOUSE_DCLICKED = 4;
	public static final int TOOL_MOUSE_RCLICKED = 5;
	public static final int TOOL_MOUSE_WHEEL = 6;
	public static final int TOOL_MOUSE_UP = 7;
	public static final int TOOL_MOUSE_DOWN = 8;
	public static final int TOOL_IMMEDIATE = 9;
	
	public ToolBehavior3DAdapter(Canvas3D c, Map3D map){
		super(c, MOUSE_MOTION_LISTENER | MOUSE_LISTENER | MOUSE_WHEEL_LISTENER);
		theMap = map;
	}
	
	public void fireToolEvent(ToolInputEvent e){
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).handleToolEvent(e);
	}
	
	public void addListener(ToolInputListener thisListener){
		listeners.add(thisListener);
	}
	
	public void removeListener(ToolInputListener thisListener){
		listeners.remove(thisListener);
	}

	public void setMap3D(Map3D m){
		theMap = m;
	}
	
	@Override
	protected void integrateTransforms() {
		if (theMap == null) return;
		theMap.updateTargetTransform();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!(e.getSource() instanceof InterfaceGraphic3D)) return;
			//this is a left button drag
		if (SwingUtilities.isLeftMouseButton(e))
			fireToolEvent(new ToolInputEvent((InterfaceGraphic3D)e.getSource(), TOOL_MOUSE_DRAGGED, e.getPoint(), 
											  e.isControlDown(), e.isShiftDown()));
			
			//this is a middle button drag
		if (SwingUtilities.isMiddleMouseButton(e))
			fireToolEvent(new ToolInputEvent((InterfaceGraphic3D)e.getSource(), TOOL_MOUSE_MDRAGGED, e.getPoint(), 
					  						  e.isControlDown(), e.isShiftDown()));
			
			//this is a right button drag
		if (SwingUtilities.isRightMouseButton(e))
			fireToolEvent(new ToolInputEvent((InterfaceGraphic3D)e.getSource(), TOOL_MOUSE_RDRAGGED, e.getPoint(), 
					  						  e.isControlDown(), e.isShiftDown()));
			
			//this is a left button drag
			//fireToolEvent(new ToolEvent((Canvas3D)e.getSource(), TOOL_MOUSE_DRAGGED, e.getPoint()));
		integrateTransforms();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)){
			if (e.getClickCount() > 1)
				fireToolEvent(new ToolInputEvent((InterfaceGraphic3D)e.getSource(), TOOL_MOUSE_DCLICKED, e.getPoint()));
			else
				fireToolEvent(new ToolInputEvent((InterfaceGraphic3D)e.getSource(), TOOL_MOUSE_CLICKED, e.getPoint()));
			}
		
		if (SwingUtilities.isRightMouseButton(e))
			fireToolEvent(new ToolInputEvent((InterfaceGraphic3D)e.getSource(), TOOL_MOUSE_RCLICKED, e.getPoint()));
		integrateTransforms();
	}
	
	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		//super.mouseMoved(arg0);
		return;
	}
	
	@Override
	public void mouseReleased(MouseEvent e){
		if (!(e.getSource() instanceof InterfaceGraphic3D)) return;
		fireToolEvent(new ToolInputEvent((InterfaceGraphic3D)e.getSource(), TOOL_MOUSE_UP, e.getPoint()));
		//fireToolEvent(new ToolEvent((Canvas3D)e.getSource(), TOOL_MOUSE_UP, e.getPoint()));
		integrateTransforms();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!(e.getSource() instanceof InterfaceGraphic3D)) return;
		ToolInputEvent thisEvent;
		if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL){
			thisEvent = new ToolInputEvent((InterfaceGraphic3D)e.getSource(), TOOL_MOUSE_WHEEL, e.getPoint());
			//thisEvent = new ToolEvent((Canvas3D)e.getSource(), TOOL_MOUSE_WHEEL, e.getPoint());
			thisEvent.setVal(-1 * e.getWheelRotation() * (e.getScrollAmount()));
			fireToolEvent(thisEvent);
			}
		integrateTransforms();
	}

	@Override
	protected void processAWTEvents(AWTEvent[] arg0) {
		// TODO Auto-generated method stub
		motion = true;
	}
	
}