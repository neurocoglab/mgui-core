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

import mgui.geometry.Rect2D;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;

public class ToolZoomExt2D extends Tool2D {

	public static final int MODEL = 0;
	public static final int SECTION = 1;
	
	public int zoomType;
	
	public ToolZoomExt2D(){
		init();
	}
	
	public ToolZoomExt2D(int type){
		zoomType = type;
		init();
	}
	
	private void init(){
		//isImmediate = true;
		switch (zoomType){
		case MODEL:
			name = "Zoom Model Extents - Click window";
			break;
		case SECTION:
			name = "Zoom Section Extents - Click window";
			break;
		}
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		
		if (e.getEventType() == ToolConstants.TOOL_MOUSE_CLICKED){
			switch (zoomType){
			case MODEL:
				//ShapeSet3DInt thisModel = ((InterfaceDisplayPanel)targetPanel.getParent()).shapeSet;
				/**@TODO find XY extents in section set reference plane of model extent cube **/
				/**@TODO implement methods to get model more easily **/
				targetPanel = (InterfaceGraphic2D)e.getSource();
				InterfaceDisplayPanel displayPanel = InterfaceSession.getDisplayPanel();
				SectionSet3DInt thisSet = displayPanel.getCurrentSectionSet(); // (SectionSet3DInt)(displayPanel.attributes.getValue("CurrentSectionSet"));
				Map2D thisMap = targetPanel.getDrawingEngine().getMap();
				Rect2D bounds = thisSet.getBoundBox2D();
				if (bounds == null || bounds.getDiagonalLength() == 0 || 
									  bounds.getDiagonalLength() == Double.NaN ||
									  bounds.getDiagonalLength() == Double.POSITIVE_INFINITY)
					return;
				thisMap.zoomToMapWindow(bounds);
				targetPanel.updateDisplay();
				break;
			case SECTION:
				
				
				break;
			}
		}
		
	}
	
	@Override
	public Object clone(){
		return new ToolZoomExt2D(zoomType);
	}
	
	
}