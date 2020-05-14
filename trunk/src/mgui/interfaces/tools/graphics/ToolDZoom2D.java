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

import java.awt.Point;

import javax.swing.ImageIcon;
import org.jogamp.vecmath.Point2f;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.numbers.MguiDouble;


public class ToolDZoom2D extends Tool2D {

	/**
	 * @variable zoomRate - Percentage of current zoom width to increase by
	 * 
	 */
	
	Point startPt;
	Point2f centerPt;
	double startVal;
	public double zoomRate = 1;
	
	public ToolDZoom2D(){
		super();
		init();
	}
	
	private void init(){
		name = "Dynamic Zoom/Pan 2D";
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/tools/zoom_dynamic_30.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/tools/zoom_dynamic_30.png");
	}
	
	public void setZoomRate(double newRate){
		if (newRate > 0)
		zoomRate = newRate;
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		Map2D map = (Map2D)targetPanel.getMap();
		
		int type = e.getEventType();
		
		// Alternate mouse actions
		if (type == ToolConstants.TOOL_MOUSE_DRAGGED && e.isCtrlPressed())
			type = ToolConstants.TOOL_MOUSE_MDRAGGED;
		if (type == ToolConstants.TOOL_MOUSE_DRAGGED && e.isShiftPressed())
			type = ToolConstants.TOOL_MOUSE_RDRAGGED;
		
		switch(type){
		
			case ToolConstants.TOOL_MOUSE_MOVED:
				if (toolPhase == 2){
					startPt = null;
					toolPhase = 0;
					targetPanel.getDrawingEngine().setDrawMode(DrawingEngine.DRAW_NORMAL);
					targetPanel.updateDisplay();
					name = "Dynamic Zoom 2D";
					}
				//else
					//targetPanel.drawEngine.setDrawMode(DrawEngine.DRAW_NORMAL);
				break;
			
			case ToolConstants.TOOL_MOUSE_MDRAGGED:
				if (startPt == null) toolPhase = 0;
				switch(toolPhase){
				case 0:
					startPt = e.getPoint();
					centerPt = map.getMapCenterPt();
					startVal = map.getZoom();
					toolPhase = 1;
					name = "Zooming to 100%";
					targetPanel.getDrawingEngine().setDrawMode(DrawingEngine.DRAW_FAST);
					break;
				
				case 1:
				case 2:
					map.setZoom(startVal * (1.0 + (Math.max(((startPt.y - e.getPoint().y) * zoomRate), -99.0) / 100.0)));
					map.centerOnPoint(centerPt);
					name = "Zooming to " + MguiDouble.getString(100.0 + 
							Math.max(((startPt.y - e.getPoint().y) * zoomRate), -99.0), "###.0") + "%";
					targetPanel.updateDisplay();
					toolPhase = 2;
				}
				break;
				
			case ToolConstants.TOOL_MOUSE_DRAGGED:
				switch(toolPhase){
				case 0:
					targetPanel.getDrawingEngine().setDrawMode(DrawingEngine.DRAW_FAST);
					startPt = e.getPoint();
					centerPt = (Point2f)map.origin.clone();
					toolPhase = 1;
					name = "Panning (x=0.0, y=0.0)";
					break;
				
				case 1:
				case 2:
					if (startPt == null) return;
					double deltaX = map.getMapDist(e.getPoint().x - startPt.x);
					double deltaY = map.getMapDist(startPt.y - e.getPoint().y);
					map.origin.x = (float)(centerPt.x - deltaX);
					map.origin.y = (float)(centerPt.y - deltaY);
					name = "Panning (x=" + MguiDouble.getString(deltaX, "###,##0.0") + ", " +
									"y=" + MguiDouble.getString(deltaY, "###,##0.0") + ")";
					targetPanel.updateDisplay();
					toolPhase = 2;
				}
				
				break;
			
			case ToolConstants.TOOL_MOUSE_UP:
				startPt = null;
				startVal = 0;
				targetPanel.getDrawingEngine().setDrawMode(DrawingEngine.DRAW_NORMAL);
				if (toolPhase == 2)
					targetPanel.updateDisplay();
				toolPhase = 0;
				name = "Change 2D Zoom";
				break;
			
				
			case ToolConstants.TOOL_MOUSE_WHEEL:
				toolPhase = 2;
				targetPanel.getDrawingEngine().setDrawMode(DrawingEngine.DRAW_FAST);
				startVal = map.getZoom();
				centerPt = map.getMapCenterPt();
				map.setZoom(startVal * (1.0 + 
						(Math.max(((e.getVal()) * zoomRate * 2.0), -99.0) / 100.0)));
				name = "Zooming to " + MguiDouble.getString(100.0 + 
						Math.max(((e.getVal()) * zoomRate * 2.0), -99.0), "###.0") + "%";
				map.centerOnPoint(centerPt);
				targetPanel.updateDisplay();
				//targetPanel.theMap.zoomWidth *= (1 + (e.getVal() * zoomRate / 100));
				break;
			}
	}
	
	@Override
	public Object clone(){
		return new ToolDZoom2D();
	}
	
}