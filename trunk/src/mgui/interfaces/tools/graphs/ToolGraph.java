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

package mgui.interfaces.tools.graphs;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphs.InterfaceGraphDisplay;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.ToolListener;
import mgui.interfaces.tools.ToolMouseEvent;
import edu.uci.ics.jung.visualization.control.GraphMousePlugin;

/*****************************************************
 * Abstract class to be inherited by tools which operate on graphs; i.e, through
 * {@link InterfaceGraphDisplay}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class ToolGraph implements Tool {

	protected String name = "Unknown Graph Tool";
	protected ArrayList<GraphMousePlugin> plugins = new ArrayList<GraphMousePlugin>();
	protected ArrayList<ToolListener> listeners = new ArrayList<ToolListener>();
	protected boolean isImmediate;
	protected InterfaceGraphDisplay target_panel;
	protected Icon icon;
	
	public ArrayList<GraphMousePlugin> getPlugins(){
		return plugins;
	}
	
	public void setTargetPanel(InterfacePanel p){
		target_panel = (InterfaceGraphDisplay)p;
	}
	
	@Override
	public Tool getPreviousTool(){
		return null;
	}
	
	public void handleToolEvent(ToolInputEvent e){
		if (!(e instanceof ToolMouseEvent) || ((ToolMouseEvent)e).mouseEvent == null) return;
		MouseEvent me = ((ToolMouseEvent)e).mouseEvent;
		
		for (int i = 0; i < plugins.size(); i++){
			if (plugins.get(i) instanceof MouseListener){
				MouseListener ml = (MouseListener)plugins.get(i);
				switch (e.getEventType()){
					case ToolConstants.TOOL_MOUSE_CLICKED:
					case ToolConstants.TOOL_MOUSE_MCLICKED:
					case ToolConstants.TOOL_MOUSE_RCLICKED:
					case ToolConstants.TOOL_MOUSE_DCLICKED:
						ml.mouseClicked(me);
						break;
					case ToolConstants.TOOL_MOUSE_DOWN:
						ml.mousePressed(me);
						break;
					case ToolConstants.TOOL_MOUSE_UP:
						ml.mouseReleased(me);
						break;
					}
				//return;
				}
			if (plugins.get(i) instanceof MouseMotionListener){
				MouseMotionListener ml = (MouseMotionListener)plugins.get(i);
				switch (e.getEventType()){
					case ToolConstants.TOOL_MOUSE_DRAGGED:
					case ToolConstants.TOOL_MOUSE_MDRAGGED:
					case ToolConstants.TOOL_MOUSE_RDRAGGED:
						ml.mouseDragged(me);
						break;
					case ToolConstants.TOOL_MOUSE_MOVED:
						ml.mouseMoved(me);
						break;
					}
				//return;
				}
				
			}
	}
	
	public Icon getObjectIcon(){
		return icon;
	}
	
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/tools/tool_3d_30.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/tools/tool_3d_30.png");
	}
	
	public void addListener(ToolListener tl){
		for (ToolListener l : listeners)
			if (l.equals(tl)) return;
		listeners.add(tl);
	}
	
	public void removeListener(ToolListener tl){
		listeners.remove(tl);
	}

	public String getName() {
		return name;
	}
	
	@Override
	public abstract Object clone();
	
	public boolean isImmediate(){
		return isImmediate;
	}
	
}