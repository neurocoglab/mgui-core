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

package mgui.interfaces.tools.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;

import org.jogamp.vecmath.Point2f;

import mgui.interfaces.maps.Map2D;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;

/************************************************
 * Interactive tool which allows the user to zoom on a window within the current view.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolWindowZoom2D extends Tool2D {

	public Point startPt, lastPt;
	int deltaX, deltaY;
	
	
	public ToolWindowZoom2D(){
		super();
		init();
	}
	
	public ToolWindowZoom2D(Tool2D last_tool){
		super();
		init();
		this.last_tool = last_tool;
	}
	
	private void init(){
		name = "Zoom Window 2D: First corner";
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		Map2D map = (Map2D)targetPanel.getMap();
		switch(e.getEventType()){
		
			case ToolConstants.TOOL_MOUSE_MOVED:
				if (toolPhase > 0){
					deltaX = Math.abs(e.getPoint().x - startPt.x);
					deltaY = Math.abs(e.getPoint().y - startPt.y);
					
					Point2f p = new Point2f(map.getMapDist(deltaX), map.getMapDist(deltaY));			
					name = "Zoom Window 2D: " + mgui.numbers.NumberFunctions.getPoint2fStr(p, "##0.00");
					Graphics2D g = (Graphics2D)targetPanel.getGraphics();
					g.setXORMode(Color.RED);
					if (lastPt != null)
						g.drawRect(Math.min(startPt.x, lastPt.x), Math.min(startPt.y, lastPt.y),
								   Math.abs(lastPt.x - startPt.x), Math.abs(lastPt.y - startPt.y));
					g.drawRect(Math.min(startPt.x, e.getPoint().x), Math.min(startPt.y, e.getPoint().y),
							   deltaX, deltaY);
				
					lastPt = e.getPoint();
					}
				
				break;
			
			case ToolConstants.TOOL_KEY_PRESSED:
				
				if (e.getKeyInputVal() == KeyEvent.VK_ESCAPE)
					deactivate();
				
				return;
				
			case ToolConstants.TOOL_MOUSE_CLICKED:
				
				switch (toolPhase){
					case 0:
						if (targetPanel.getToolLock())
							break;
						targetPanel.setToolLock(true);
						//set start pt
						startPt = e.getPoint();
						lastPt = null;
						name = "Zoom Window 2D: Second corner";
						toolPhase = 1;
						break;
					
					case 1:
						//zoom to window
						
						Point2f center = map.getMapPoint(new Point((e.getPoint().x + startPt.x) / 2,
												 								  (e.getPoint().y + startPt.y) / 2));
						map.setZoom(map.getMapDist(Math.abs(e.getPoint().x - startPt.x)));
						map.centerOnPoint(center);
						toolPhase = 0;
						name = "Zoom Window 2D: first corner";
						targetPanel.updateDisplay();
						
						deactivate();
						
						break;

					}
				break;
			}
	}
	
//	public void finishTool(){
//		targetPanel.setToolLock(false);
//		targetPanel.finishTool();
//		
//	}
	
	@Override
	public Object clone(){
		return new ToolWindowZoom2D();
	}
	
}