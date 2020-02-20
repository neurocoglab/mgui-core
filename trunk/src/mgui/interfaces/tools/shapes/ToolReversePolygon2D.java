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

import mgui.geometry.Polygon2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.graphics.Tool2D;

public class ToolReversePolygon2D extends Tool2D {

	public ToolReversePolygon2D(){
		super();
		init();
	}
	
	private void init(){
		name = "Change 2D Polygon Direction";
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		switch(e.getEventType()){
		
		case ToolConstants.TOOL_MOUSE_CLICKED:
			Shape2DInt thisShape = targetPanel.pickShape(e.getPoint(), 4);
			if (!(thisShape instanceof Polygon2DInt)) return;
			
			Polygon2DInt thisPoly = (Polygon2DInt)thisShape;
			thisPoly.thisShape = GeometryFunctions.getReversePolygon((Polygon2D)thisPoly.thisShape);
			//targetPanel.updateDisplay();
			thisPoly.updateShape();
			break;
		}
		
	}
	
	@Override
	public Object clone(){
		return new ToolReversePolygon2D();
	}
	
}