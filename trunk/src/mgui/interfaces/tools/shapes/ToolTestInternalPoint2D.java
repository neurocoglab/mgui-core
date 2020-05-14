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

package mgui.interfaces.tools.shapes;
import java.awt.Color;
import java.util.ArrayList;

import org.jogamp.vecmath.Point2f;

import mgui.geometry.Point2D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Rect2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.shapes.Point2DInt;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.Rect2DInt;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;


/***********************************************************
 * 
 * 
 * @author typically
 *
 */
public class ToolTestInternalPoint2D extends ToolCreatePolygon2D {

	public ToolTestInternalPoint2D(){
		super();
		init();
	}
	
	private void init(){
		name = "Test Internal Point 2D";
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		Map2D map = (Map2D)targetPanel.getMap();
		switch(e.getEventType()){
		
			case ToolConstants.TOOL_MOUSE_RCLICKED:
				InterfaceSession.log("Test internal point rclick -- OK");
				if (toolPhase == 1){
					//not a valid polygon (error dialog?)
					targetPanel.updateDisplays();
					targetPanel.setToolLock(false);
					toolPhase = 0;
					}
				
				if (toolPhase == 2){
					//finish polygon and add to model	
					startPt = map.getMapPoint(e.getPoint());
					name = "Test Internal Point 2D";
					
					thisPoly.getPolygon().vertices.add(startPt);
					thisPoly.updateShape();
					targetPanel.clearTempShapes();
					targetPanel.addModelShape2D(thisPoly, true, true);
					Rect2D bounds = thisPoly.getPolygon().getBounds();
					Rect2DInt bound2D = new Rect2DInt(bounds);
					bound2D.setAttribute("LineColour", Color.GRAY);
					targetPanel.addModelShape2D(bound2D, true, true);
					
					bounds = thisPoly.getPolygon().getBounds();
					float width = bounds.getWidth();
					float height = bounds.getHeight();
					
					//add test points and colour by internality
					Point2f cnr1 = bounds.getCorner(Rect2D.CNR_BL);
					Point2f cnr2 = bounds.getCorner(Rect2D.CNR_TR);
					
					cnr1.x -= width / 4f;
					cnr1.y -= height / 4f;
					cnr2.x += width / 4f;
					cnr2.y += height / 4f;
					
					bounds = new Rect2D(cnr1, cnr2);
					ArrayList<Polygon2D> searchPolys = new ArrayList<Polygon2D>();
					
					//create 25 pts
					Color colour;
					Point2DInt pInt;
					Point2D pt;
					Polygon2D searchPoly, lastPoly = null;
					for (int i = 0; i < 5; i++)
						for (int j = 0; j < 5; j++){
							pt = new Point2D(bounds.getCorner(Rect2D.CNR_BL));
							pt.point.x += (i / 4f) * bounds.getWidth();
							pt.point.y += (j / 4f) * bounds.getHeight();
							
							colour = Color.BLUE;
							if (thisPoly.getPolygon().getBounds().contains(pt.point))
								colour = Color.GREEN;
							searchPoly = GeometryFunctions.getIsInternalConcavePoly(thisPoly.getPolygon(), pt.point);
							if (searchPoly != null){
								if (searchPoly != lastPoly)
									searchPolys.add(searchPoly);
								//if (thisPoly.getPolygon().contains(pt.point))
									colour = Color.RED;
									lastPoly = searchPoly;
								}
							pInt = new Point2DInt(pt, colour, 2);
							targetPanel.addModelShape2D(pInt, true, true);
							}
					
					//targetPanel.addModelShape2D(new Rect2DInt(thisPoly.getPolygon().getBounds()), false);
					
					for (int i = 0; i < searchPolys.size(); i++){
						Polygon2DInt polyInt = new Polygon2DInt(searchPolys.get(i));
						polyInt.setAttribute("FillColour", Color.DARK_GRAY);
						polyInt.setAttribute("HasTransparency", new MguiBoolean(true));
						polyInt.setAttribute("HasFill", new MguiBoolean(true));
						polyInt.setAttribute("Alpha", new MguiFloat(0.5f));
						targetPanel.addModelShape2D(polyInt, true, true);
						//targetPanel.drawShape2D(polyInt);
						InterfaceSession.log("Drawing containing poly..");
						}
					
					//targetPanel.setShapeSet2D();
					targetPanel.updateDisplays();
					
					toolPhase = 0;
					targetPanel.setToolLock(false);
					//break;
					}
				
				return;
			}
	
		super.handleToolEvent(e);
	}
	
	@Override
	public Object clone(){
		return new ToolTestInternalPoint2D();
	}
	
}