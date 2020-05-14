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

import java.awt.Point;
import java.awt.event.MouseEvent;

import mgui.interfaces.InterfacePanel;


public class ToolMouseEvent extends ToolInputEvent {

	public MouseEvent mouseEvent;
	
	public ToolMouseEvent(InterfacePanel panel) {
		super(panel);
	}

	public ToolMouseEvent(InterfacePanel panel, int type, Point thisPt) {
		super(panel, type, thisPt);
	}
	
	public ToolMouseEvent(MouseEvent e, int type){
		super(e.getSource());
		this.eventType = type;
		mouseEvent = e;
	}
	
}