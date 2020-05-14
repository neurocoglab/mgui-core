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

package mgui.interfaces.tools.graphics;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.ToolEvent;
import mgui.interfaces.tools.ToolEvent.EventType;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.ToolListener;

/**********************************************************
 * Abstract class for a {@linkplain Tool} which operates on an {@code InterfaceGraphic2D} window.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class Tool2D implements Tool {

	//pointer to shape object
	public Shape2DInt targetShape;
	public InterfaceGraphic2D targetPanel;
	public int toolPhase = 0;
	public String name;
	public boolean isImmediate = false;
	protected ArrayList<ToolListener> listeners = new ArrayList<ToolListener>();
	
	protected Icon icon;
	protected Tool2D last_tool;
	
	protected boolean is_active = false;
	
	public Tool2D(){
		name = "Unnamed 2D Tool";
		setIcon();
	}
	
	public void activate(){
		//InterfaceSession.getDisplayPanel().setCurrentTool(this);
		is_active = true;
	}
	
	public void deactivate(){
		if (!is_active) return;
		is_active = false;
		fireListeners(new ToolEvent(this, EventType.ToolDeactivated));
	}
	
	@Override
	public Tool getPreviousTool(){
		return last_tool;
	}
	
	public boolean isExclusive(){
		return true;
	}
	
	public void addListener(ToolListener tl){
		for (ToolListener l : listeners)
			if (l.equals(tl)) return;
		listeners.add(tl);
	}
	
	public void removeListener(ToolListener tl){
		listeners.remove(tl);
	}
	
	public void setTargetShape(Shape2DInt thisShape){
		targetShape = thisShape;
	}
	
	@Override
	public void setTargetPanel(InterfacePanel panel){
		targetPanel = (InterfaceGraphic2D)panel;
	}
	
	public abstract void handleToolEvent(ToolInputEvent e);
	
	public boolean isImmediate(){
		return isImmediate;
	}
	
	public String getName(){
		return name;
	}
	
	@Override
	public abstract Object clone();
	
	protected void fireListeners(){
		fireListeners(new ToolEvent(this));
	}
	
	protected void fireListeners(ToolEvent event){
		for (int i = 0; i < listeners.size(); i++)
			listeners.get(i).toolStateChanged(event);
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
	
	public InterfacePopupMenu getPopupMenu() {
		return null;
	}

	public void handlePopupEvent(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void showPopupMenu(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}