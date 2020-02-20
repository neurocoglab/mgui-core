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

import java.awt.Point;

import javax.vecmath.Point2f;

import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.numbers.MguiDouble;


public class ToolPan2D extends Tool2D {

	Point startPt;
	Point2f startOrigin;
	
	public ToolPan2D(){
		super();
		init();
	}
	
	private void init(){
		name = "Pan 2D";
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		switch(e.getEventType()){
	
		//case ToolInputAdapter.TOOL_MOUSE_MOVED:
		//	if (toolPhase != 0){
		//		startPt = null;
		//		toolPhase = 0;
		//		name = "Pan 2D";
		//		}
		//	break;
		
		case ToolConstants.TOOL_MOUSE_DRAGGED:
			Map2D map = (Map2D)targetPanel.getMap();
			switch(toolPhase){
			case 0:
				targetPanel.getDrawingEngine().setDrawMode(DrawingEngine.DRAW_FAST);
				startPt = e.getPoint();
				startOrigin = (Point2f)map.origin.clone();
				toolPhase = 1;
				name = "Panning (x=0.0, y=0.0)";
				break;
			
			case 1:
				double deltaX = map.getMapDist(e.getPoint().x - startPt.x);
				double deltaY = map.getMapDist(startPt.y - e.getPoint().y);
				map.origin.x = (float)(startOrigin.x - deltaX);
				map.origin.y = (float)(startOrigin.y - deltaY);
				name = "Panning (x=" + MguiDouble.getString(deltaX, "###,##0.0") + ", " +
								"y=" + MguiDouble.getString(deltaY, "###,##0.0") + ")";
				targetPanel.updateDisplay();
			}
			
			break;
			
		case ToolConstants.TOOL_MOUSE_UP:
			toolPhase = 0;
			startPt = null;
			targetPanel.getDrawingEngine().setDrawMode(DrawingEngine.DRAW_NORMAL);
			targetPanel.updateDisplay();
			name = "Pan 2D";
			break;
		}
	}
	
	@Override
	public Object clone(){
		return new ToolPan2D();
	}
	
}