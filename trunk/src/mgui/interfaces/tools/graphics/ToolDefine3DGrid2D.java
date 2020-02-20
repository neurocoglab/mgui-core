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

import mgui.geometry.Plane3D;
import mgui.geometry.Rect2D;
import mgui.geometry.Rect3D;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.shapes.Rect2DInt;
import mgui.interfaces.shapes.Rect3DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.shapes.volume.InterfaceVolumePanel;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.shapes.ToolCreateImage3D;

/*******************************************************
 * Tool to define a 3D grid on a 2D window.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolDefine3DGrid2D extends ToolCreateImage3D {

	//public Point startPt, lastPt;
	//int deltaX, deltaY;
	InterfaceVolumePanel volPanel;
	
	public ToolDefine3DGrid2D(InterfaceVolumePanel p){
		super();
		volPanel = p;
		init();
	}
	
	private void init(){
		//name = "Define Rect 2D: First corner ";
		
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		Map2D map = (Map2D)targetPanel.getMap();
		if (e.getEventType() == ToolConstants.TOOL_MOUSE_CLICKED && toolPhase == 1){
			
			Rect2D rect = new Rect2D(map.getMapPoint(startPt),
									 map.getMapPoint(e.getPoint()));
			Rect2DInt rect2D = new Rect2DInt(rect);
			//get current plane, dist
			int sect = targetPanel.getCurrentSection();
			SectionSet3DInt sections = targetPanel.getCurrentSectionSet();
			float dist = sections.getSpacing() * sect;
			Plane3D plane = sections.getRefPlane();
			Rect3DInt rect3D = (Rect3DInt)ShapeFunctions.
						getShape3DIntFromSection(plane, dist, rect2D);
			
			targetPanel.setToolLock(false);
			volPanel.setGeometryFromRect((Rect3D)rect3D.shape3d);
			
			toolPhase = 0;
			name = "Create XY Rect: first corner";
			targetPanel.updateDisplays();
		}else{
			super.handleToolEvent(e);
			}
		}
	
	@Override
	public Object clone(){
		return new ToolDefine3DGrid2D(volPanel);
	}
	
	
	
	
	
}