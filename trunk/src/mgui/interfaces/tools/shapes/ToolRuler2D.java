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

package mgui.interfaces.tools.shapes;

import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import org.jogamp.vecmath.Point2f;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.graphics.Tool2D;
import mgui.interfaces.tools.shapes.util.RulerDialog;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;

/******************************************************
 * Allows the user to measure distance on a 2D window, along a path with N nodes. Opens a non-modal
 * dialog box containing a table which displays the distance of each segment of the path, along with
 * the cumulative distance at each vertex. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolRuler2D extends ToolCreatePolygon2D {

	static RulerDialog dialog; // = new RulerDialog();
	Point2f lastRulerPt;
	
	public ToolRuler2D(){
		super(false);
		init();
	}
	
	public ToolRuler2D(Tool2D last_tool){
		super(false);
		init();
		this.last_tool = last_tool;
	}
	
	private void init(){
		name = "Ruler 2D: Next point:";
		
	}
	
	@Override
	public void activate(){
		super.activate();
		if (dialog == null){
			dialog = new RulerDialog(InterfaceSession.getSessionFrame());
			}
		dialog.registerTool(this);
	}
	
	@Override
	public void deactivate(){
		if (dialog != null){
			dialog.deregisterTool(this);
			}
		if (targetPanel != null){
			targetPanel.setToolLock(false);
			targetPanel.clearTempShapes();
			targetPanel.regenerateDisplay();
			}
		super.deactivate();
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		
		switch(e.getEventType()){
		
			case ToolConstants.TOOL_MOUSE_MOVED:
				if (toolPhase > 0){
					super.handleToolEvent(e);
					name = "Ruler to " + MguiDouble.getString(nextPt.x, "###,##0.0") 
							+ ", " + MguiDouble.getString(nextPt.y, "###,##0.0");
				}else{
					name = "2D Ruler Tool";
					}
				
				break;
			
			case ToolConstants.TOOL_KEY_PRESSED:
				if (toolPhase == 0) break;
				
				if (e.getKeyInputVal() == KeyEvent.VK_ESCAPE){
					// Stop the ruler here (start over)
					targetPanel.setToolLock(false);
					//if (tempShape != null)
						targetPanel.removeTempShape(thisPoly);
					targetPanel.regenerateDisplay();
					toolPhase = 0;
					dialog.clearCurrentTool();
					name = "2D Ruler Tool";
					}
					
				break;
				
			case ToolConstants.TOOL_MOUSE_DCLICKED:
				
				if (toolPhase == 0) break;
				
				// Same behaviour as ESCAPE
				// Stop the ruler here (start over)
				targetPanel.setToolLock(false);
				//if (tempShape != null)
					targetPanel.removeTempShape(thisPoly);
				targetPanel.regenerateDisplay();
				dialog.clearCurrentTool();
				toolPhase = 0;
				name = "2D Ruler Tool";
				
				break;
				
			case ToolConstants.TOOL_MOUSE_CLICKED:
				
				if (toolPhase == 0 && targetPanel.getToolLock())
					break;
				
				if (!dialog.setCurrentTool(this)){
					InterfaceSession.log("ToolRuler2D: dialog is in use by another tool..", LoggingType.Warnings);
					return;
					}
				
				int _toolPhase = toolPhase;
				
				super.handleToolEvent(e);
				name = "Ruler to " + MguiDouble.getString(nextPt.x, "###,##0.0") 
						+ ", " + MguiDouble.getString(nextPt.y, "###,##0.0");
				
				switch (_toolPhase){
					case 0:
						// Starting, reset dialog
						dialog.reset();
						lastRulerPt = startPt;
						thisPoly.setAttribute("IsClosed", new MguiBoolean(false));
						break;
					
					default:
						// New vertex has been added, get distance of latest line segment
						// and update dialog
						float dist = lastRulerPt.distance(nextPt);
						lastRulerPt.set(nextPt);
						dialog.addSegment(dist);
						
						break;

					}
				break;
			}
	}
	@Override
	public Object clone() {
		ToolRuler2D ruler = new ToolRuler2D();
		//dialog.registerTool(ruler);
		return ruler;
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/tools/ruler_2d_30.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/tools/ruler_2d_30.png");
	}

}