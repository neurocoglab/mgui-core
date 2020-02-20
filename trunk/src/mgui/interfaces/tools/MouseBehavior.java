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
import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.WakeupOnAWTEvent;

public abstract class MouseBehavior extends Behavior {

	@Override
	public void initialize() {
		this.wakeupOn(new WakeupOnAWTEvent(AWTEvent.MOUSE_EVENT_MASK | 
										   AWTEvent.MOUSE_MOTION_EVENT_MASK |
										   AWTEvent.MOUSE_WHEEL_EVENT_MASK));
	}
	
	public void processMouseEvent(java.awt.event.MouseEvent e){
		
	}
	
	public void processMouseWheelEvent(java.awt.event.MouseWheelEvent e){
		
	}
	
	@Override
	public void processStimulus(Enumeration arg0) {
		// TODO Auto-generated method stub
		
	}

}