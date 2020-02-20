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

import java.awt.Point;
import java.util.EventObject;

import javax.media.j3d.Canvas3D;

import mgui.interfaces.InterfacePanel;

/********************************************
 * Event signifying that input has been detected and must be handled by a 
 * {@code Tool} object. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class ToolInputEvent extends EventObject {

	protected int eventType;
	protected Point eventPoint;
	protected double eventVal;
	protected int keyInputVal;
	protected boolean ctrl_pressed, shift_pressed;
	
	//constants
	public ToolInputEvent(Object object){
		super(object);
	}
	
	public ToolInputEvent(InterfacePanel panel) {
		super(panel);
	}

	public ToolInputEvent(InterfacePanel panel, int type, Point thisPt) {
		super(panel);
		eventType = type;
		eventPoint = thisPt;
	}
	
	public ToolInputEvent(InterfacePanel panel, int type, Point thisPt, boolean ctrl_pressed, boolean shift_pressed) {
		super(panel);
		eventType = type;
		eventPoint = thisPt;
		this.ctrl_pressed = ctrl_pressed;
		this.shift_pressed = shift_pressed;
	}
	
	public ToolInputEvent(Canvas3D panel, int type, Point thisPt) {
		super(panel);
		eventType = type;
		eventPoint = thisPt;
	}
	
	public ToolInputEvent(InterfacePanel panel, int type, int keyInputVal) {
		super(panel);
		eventType = type;
		this.keyInputVal = keyInputVal;
	}
	
	public int getEventType(){
		return eventType;
	}
	
	public Point getPoint(){
		return eventPoint;
	}
	
	public double getVal(){
		return eventVal;
	}
	
	public void setVal(double thisVal){
		eventVal = thisVal;
	}
	
	public int getKeyInputVal(){
		return keyInputVal;
	}
	
	public void setKeyInputVal(int thisVal){
		keyInputVal = thisVal;
	}
	
	public boolean isShiftPressed(){
		return shift_pressed;
	}
	
	public boolean isCtrlPressed(){
		return ctrl_pressed;
	}
	
}