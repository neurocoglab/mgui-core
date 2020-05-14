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

package mgui.interfaces.graphics;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.event.MouseInputAdapter;
import org.jogamp.vecmath.Point2f;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceTextBox;
import mgui.interfaces.maps.Map;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.maps.Map3D;
import mgui.interfaces.tables.InterfaceDataTable;
import mgui.interfaces.tables.InterfaceTableModel;
import mgui.interfaces.tools.Tool;
import mgui.numbers.MguiDouble;

/****************************************************************
 * Represents a labelled text box which updates from mouse movement events on its registered
 * {@link InterfaceGraphic} components. Useful for implementing an updating status bar. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceGraphicTextBox extends InterfaceTextBox 
									 implements GraphicMouseListener, 
									 			GraphicPropertyListener {

	private InterfaceGraphicTextBox thisRef = this;
	public Map theMap;
	public Map2D map2D;
	public Map3D map3D;
	public TextMouseListener mouseAdapter;
	public TextPropertyListener propertyAdapter;
	//public int interfaceType; 
	
	//constants
//	public static final int INT_MOUSE_EVENT = 1;
//	public static final int INT_TOOL_TYPE = 2;
//	public static final int INT_SOURCE_NAME = 3;
//	public static final int INT_CURRENT_ZOOM = 4;
	
	public InterfaceGraphicTextBox(){
		super();
		//setInterfaceType(INT_MOUSE_EVENT);
		init();
	}
	
	public InterfaceGraphicTextBox(String textStr){
		super(textStr);
		//setInterfaceType(INT_MOUSE_EVENT);
		init();
	}
	
//	public InterfaceGraphicTextBox(String textStr, int type){
//		super(textStr);
//		//setInterfaceType(type);
//		init();
//	}
	
	private void init(){
		//mouseAdapter = new TextMouseListener();
		mouseAdapter = new TextMouseListener();
		propertyAdapter = new TextPropertyListener();
	}
	
//	public void setInterfaceType(int thisType){
//		interfaceType = thisType;
//	}
	
	public void setMap(Map m){
		theMap = m;
	}
	
	//TODO Umm.. this needs to be changed
	class TextMouseListener extends MouseInputAdapter implements MouseWheelListener {
		@Override
		public void mouseMoved(MouseEvent e) {
			if (!(e.getSource() instanceof InterfaceGraphic)) return;
			updateText(e.getPoint(), (InterfaceGraphic<?>)e.getSource(), e);
			}
		@Override
		public void mouseDragged(MouseEvent e) {
			if (!(e.getSource() instanceof InterfaceGraphic)) return;
			updateText(e.getPoint(), (InterfaceGraphic<?>)e.getSource(), e);
			}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (!(e.getSource() instanceof InterfaceGraphic)) return;
			updateText(e.getPoint(), (InterfaceGraphic<?>)e.getSource(), e);
			}
		
		private void updateText(Point p, InterfaceGraphic<?> window, MouseEvent e){
			//theMap = window.getMap();
			
			window.updateStatusBox(thisRef, e);
			
			
			/*
			switch(thisRef.interfaceType){
				case INT_MOUSE_EVENT:
					if (theMap == null){
						if (g instanceof InterfaceDataTable){
							//mouse click handle
							thisRef.setText("");
							return;
							}
						/*
						if (g instanceof InterfaceGraphDisplay){
							//graph name
							InterfaceGraphDisplay gd = (InterfaceGraphDisplay)g;
							if (gd.viewer != null)
								thisRef.setText("Graph coords: " + 
										arDouble.getString(gd.viewer.inverseTransform(p).getX(), "##0.00")
										+ ", " +
										arDouble.getString(gd.viewer.inverseTransform(p).getY(), "##0.00"));
							else
								thisRef.setText("Graph coords: ");
							return;
							}
						*
						Point2f coords = g.getMouseCoords(p);
						if (coords == null){
							thisRef.setText("Mouse at: ?");
							return;
							}
						thisRef.setText("Mouse at: " + MguiDouble.getString(coords.x, 2) + ", "
													 + MguiDouble.getString(coords.y, 2));
						return;
					}
					if (theMap.getType() == Map.MAP_2D){
						map2D = (Map2D)g.getMap();
						if (map2D == null) return;
						Point2f theseCoords = map2D.getMapPoint(p);
						thisRef.setText("Mouse coords: " + new MguiDouble(theseCoords.x).toString("###.0") + ", "
														 + new MguiDouble(theseCoords.y).toString("###.0"));
						}
					if (theMap.getType() == Map.MAP_3D){
						map3D = (Map3D)g.getMap();
						thisRef.setText("Center of rotation: " + mgui.numbers.NumberFunctions.getPoint3dStr(
								map3D.getCamera().getCenterOfRotation(), "##0.0"));
					}
					break;
				case INT_SOURCE_NAME:
					thisRef.setText("Current window: " + g.getName());
					break;
				case INT_TOOL_TYPE:
					if (theMap == null){
						if (g instanceof InterfaceDataTable){
							//data table source
							String source = ((InterfaceDataTable)g).getSourceName();
							if (source == null) source = "None";
							if (source.compareTo("") == 0) source = "None";
							thisRef.setText("Current source: '" + source + "'");
							}
						/*
						if (g instanceof InterfaceGraphDisplay){
							//show tool
							InterfaceGraphDisplay gd = (InterfaceGraphDisplay)g;
							if (gd.currentTool != null)
								thisRef.setText("Current tool: " + gd.currentTool.getName());
							else
								thisRef.setText("Current tool: ");
							return;
							}
						*
						Tool tool = g.getCurrentTool();
						if (tool == null)
							thisRef.setText("Current tool: None");
						else
							thisRef.setText("Current tool:" + tool.getName());
						
						return;
					}
					if (theMap.getType() == Map.MAP_2D){
						if (((InterfaceGraphic2D)g).currentTool != null)
							thisRef.setText("Current tool: " + ((InterfaceGraphic2D)g).currentTool.getName());
						else
							thisRef.setText("Current tool: ");
						}
					if (theMap.getType() == Map.MAP_3D){
						if (((InterfaceGraphic3D)g).currentTool != null)
							thisRef.setText("Current tool: " + ((InterfaceGraphic3D)g).currentTool.getName());
						else
							thisRef.setText("Current tool: ");
						}
					break;
				case INT_CURRENT_ZOOM:
					if (theMap == null){
						if (g instanceof InterfaceDataTable){
							//record count
							InterfaceTableModel table_model = ((InterfaceDataTable)g).getTableModel();
							if (table_model != null){
								int records = (table_model.getRowCount());
								thisRef.setText("Records: " + records);
								}
							else{
								thisRef.setText("");
								}
							return;
							}
						/*
						if (g instanceof InterfaceGraphDisplay){
							//show scale
							InterfaceGraphDisplay gd = (InterfaceGraphDisplay)g;
							if (gd.viewer != null)
								thisRef.setText("Scale: " + arDouble.getString(
										gd.viewer.getLayoutTransformer().getScale(),
										"###,##0.00"));
							else
								thisRef.setText("Scale: ");
							return;
							}
						*
						ArrayList<String> messages = g.getStatusMessages(p);
						String message = "";
						if (messages.size() > 1)
							message = messages.get(1);
						thisRef.setText(message);
						
						return;
					}
					thisRef.setText("Zoom: " + MguiDouble.getString(g.getMap().getZoom(), "###,##0.00"));
					break;
				}
			*/
			}
		}
	
	class TextPropertyListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent e){
			if (!(e.getSource() instanceof InterfaceGraphic)) return;
			InterfaceGraphic<?> graphic = (InterfaceGraphic<?>)e.getSource();
			graphic.updateStatusBox(thisRef, null);
			
//			switch(thisRef.interfaceType){
//				case INT_TOOL_TYPE:
//					thisRef.setText("Current tool: " + ((InterfaceGraphic2D)e.getSource()).currentTool.getName());
//					break;
//				}
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