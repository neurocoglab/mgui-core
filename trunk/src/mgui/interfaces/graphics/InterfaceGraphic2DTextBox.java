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

package mgui.interfaces.graphics;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.MouseInputAdapter;

import mgui.interfaces.InterfaceTextBox;
import mgui.interfaces.maps.Map;
import mgui.interfaces.maps.Map2D;

/*******************************************************************
 * Status text box for a Graphic2D window.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceGraphic2DTextBox extends InterfaceTextBox implements GraphicMouseListener, 
																		   GraphicPropertyListener {

	
	private InterfaceGraphic2DTextBox thisRef = this;
	public Map2D theMap;
	public TextMouseListener mouseAdapter;
	public TextPropertyListener propertyAdapter;
	public int interfaceType; 
	
	//constants
	public static final int INT_MOUSE_EVENT = 1;
	public static final int INT_TOOL_TYPE = 2;
	public static final int INT_SOURCE_NAME = 3;
	public static final int INT_CURRENT_ZOOM = 4;
	
	public InterfaceGraphic2DTextBox(){
		super();
		setInterfaceType(INT_MOUSE_EVENT);
		init();
	}
	
	public InterfaceGraphic2DTextBox(String textStr){
		super(textStr);
		setInterfaceType(INT_MOUSE_EVENT);
		init();
	}
	
	public InterfaceGraphic2DTextBox(String textStr, int type){
		super(textStr);
		setInterfaceType(type);
		init();
	}
	
	private void init(){
		//mouseAdapter = new TextMouseListener();
		mouseAdapter = new TextMouseListener();
		propertyAdapter = new TextPropertyListener();
	}
	
	public void setInterfaceType(int thisType){
		interfaceType = thisType;
	}
	
	public void setMap(Map mt){
		
	}
	
	class TextMouseListener extends MouseInputAdapter implements MouseWheelListener {
		
		@Override
		public void mouseMoved(MouseEvent e) {
			/**
			switch(thisRef.interfaceType){
				case INT_MOUSE_EVENT:
					theMap = ((InterfaceGraphic2D)e.getSource()).theMap;
					if (theMap == null) return;
					Point2f theseCoords = theMap.getMapPoint(e.getPoint());
					thisRef.setText("Mouse coords: " + new arDouble(theseCoords.x).toString("###.#") + ", "
													 + new arDouble(theseCoords.y).toString("###.#"));
					break;
				case INT_SOURCE_NAME:
					thisRef.setText("Current window: " + ((InterfaceGraphic2D)e.getSource()).getName());
					break;
				case INT_TOOL_TYPE:
					if (((InterfaceGraphic2D)e.getSource()).currentTool != null)
						thisRef.setText("Current tool: " + ((InterfaceGraphic2D)e.getSource()).currentTool.getName());
					break;
				case INT_CURRENT_ZOOM:
					thisRef.setText("Zoom: " + ((InterfaceGraphic2D)e.getSource()).theMap.zoomWidth);
					break;
				} **/
			}
		@Override
		public void mouseDragged(MouseEvent e) {
			/**
			switch(thisRef.interfaceType){
				case INT_MOUSE_EVENT:
					theMap = ((InterfaceGraphic2D)e.getSource()).theMap;
					if (theMap == null) return;
					Point2f theseCoords = theMap.getMapPoint(e.getPoint());
					thisRef.setText("Mouse coords: " + new arDouble(theseCoords.x).toString("###.#") + ", "
													 + new arDouble(theseCoords.y).toString("###.#"));
					break;
				case INT_SOURCE_NAME:
					thisRef.setText("Current window: " + ((InterfaceGraphic2D)e.getSource()).getName());
					break;
				case INT_TOOL_TYPE:
					if (((InterfaceGraphic2D)e.getSource()).currentTool != null)
						thisRef.setText("Current tool: " + ((InterfaceGraphic2D)e.getSource()).currentTool.getName());
					break;
				case INT_CURRENT_ZOOM:
					thisRef.setText("Zoom: " + ((InterfaceGraphic2D)e.getSource()).theMap.zoomWidth);
					break;
				} **/
			}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			/**
			switch(thisRef.interfaceType){
				case INT_MOUSE_EVENT:
					theMap = ((InterfaceGraphic2D)e.getSource()).theMap;
					if (theMap == null) return;
					Point2f theseCoords = theMap.getMapPoint(e.getPoint());
					thisRef.setText("Mouse coords: " + new arDouble(theseCoords.x).toString("###.#") + ", "
													 + new arDouble(theseCoords.y).toString("###.#"));
					break;
				case INT_SOURCE_NAME:
					thisRef.setText("Current window: " + ((InterfaceGraphic2D)e.getSource()).getName());
					break;
				case INT_TOOL_TYPE:
					if (((InterfaceGraphic2D)e.getSource()).currentTool != null)
						thisRef.setText("Current tool: " + ((InterfaceGraphic2D)e.getSource()).currentTool.getName());
					break;
				case INT_CURRENT_ZOOM:
					thisRef.setText("Zoom: " + ((InterfaceGraphic2D)e.getSource()).theMap.zoomWidth);

					break;
				} **/
			} 
		
		}
	
	class TextPropertyListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent e){
			switch(thisRef.interfaceType){
				case INT_TOOL_TYPE:
					thisRef.setText("Current tool: " + ((InterfaceGraphic2D)e.getSource()).currentTool.getName());
					break;
				}
			}
		}
	
	public MouseInputAdapter getMouseListener(){
		return mouseAdapter;
	}
	
	public MouseWheelListener getMouseWheelListener(){
		return mouseAdapter;
	}
	
	public PropertyChangeListener getPropertyListener(){
		return propertyAdapter;
	}
	
	public void setParentWindow(InterfaceGraphic thisParent){
		//parent is current interface panel
	}
	
	public boolean isShape(){
		return false;
	}
	
	public void windowUpdated(InterfaceGraphic g) {
		// TODO Auto-generated method stub
		
	}

	
}