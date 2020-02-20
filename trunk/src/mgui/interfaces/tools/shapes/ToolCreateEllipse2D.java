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

import java.awt.Point;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

import mgui.geometry.Ellipse2D;
import mgui.geometry.Vector2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.shapes.Ellipse2DInt;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.graphics.Tool2D;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;

public class ToolCreateEllipse2D extends Tool2D {

	protected Ellipse2DInt ellipse;
	protected Point2f startPt, nextPt;
	protected Point lastPt, screenStartPt;
	
	public ToolCreateEllipse2D(){
		super();
		init();
	}
	
	private void init(){
		name = "Create 2D Ellipse";
		setIcon();
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		Map2D map = (Map2D)targetPanel.getMap();
		switch(e.getEventType()){
		
		case ToolConstants.TOOL_MOUSE_CLICKED:
			
			switch(toolPhase){
				case 0:
					if (targetPanel.getToolLock())
						break;
					targetPanel.setToolLock(true);
					
					startPt = map.getMapPoint(e.getPoint());
					ellipse = new Ellipse2DInt(new Ellipse2D(startPt, new Vector2f(0,0), 0));
					ellipse.setAttribute("ShowNodes", new MguiBoolean(true));
					nextPt = startPt;
					screenStartPt = e.getPoint();
					toolPhase = 1;
					
					lastPt = null;
					break;
					
				case 1:
					nextPt = map.getMapPoint(e.getPoint());
					Vector2f v = new Vector2f(nextPt);
					v.sub(startPt);
					
					ellipse = new Ellipse2DInt(new Ellipse2D(startPt, v, 0));
					
					name = "B-axis: " + MguiDouble.getString(0, "###,##0.000") 
										+ ", " + MguiDouble.getString(0, "###,##0.000");
					toolPhase = 2;
					break;
				
				case 2:
					//create
					nextPt = map.getMapPoint(e.getPoint());
					name = "New Polygon2D to " + MguiDouble.getString(nextPt.x, "###,##0.0") 
										+ ", " + MguiDouble.getString(nextPt.y, "###,##0.0");
					
					
					break;
				}
			break;
			
		case ToolConstants.TOOL_MOUSE_MOVED:
				Vector2f v;
			switch (toolPhase){
				
				case 0:
					startPt = map.getMapPoint(e.getPoint());
					name = "Ellipse center: " + MguiDouble.getString(startPt.x, "###,##0.000") 
										+ ", " + MguiDouble.getString(startPt.y, "###,##0.000");
					break;
					
				case 1:
					nextPt = map.getMapPoint(e.getPoint());
					v = new Vector2f(nextPt);
					v.sub(startPt);
					name = "A-axis: " + MguiDouble.getString(v.x, "###,##0.000") 
										+ ", " + MguiDouble.getString(v.y, "###,##0.000");
					break;
					
				case 2:
					nextPt = map.getMapPoint(e.getPoint());
					Vector2D v2d = new Vector2D(startPt, ellipse.getEllipse().getAxisA());
					GeometryFunctions.rotate(v2d, startPt, Math.PI/2.0);
					v = v2d.getVector();
					v.normalize();
					
					name = "A-axis: " + MguiDouble.getString(v.x, "###,##0.000") 
										+ ", " + MguiDouble.getString(v.y, "###,##0.000");
					break;
				}
		
		
		}
	}
	
	@Override
	protected void setIcon(){
		
	}
	
	@Override
	public Object clone(){
		return new ToolCreateEllipse2D();
	}
}