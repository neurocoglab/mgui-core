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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import javax.swing.ImageIcon;
import javax.vecmath.Point2f;

import mgui.geometry.Polygon2D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.graphics.Tool2D;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;

/***************************************************************
 * Allows the user to creates a Polygon2D shape.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolCreatePolygon2D extends Tool2D {

	protected Polygon2DInt thisPoly; //, tempShape;
	protected Point2f startPt, nextPt;
	protected Point lastPt, screenStartPt;
	public boolean close = true;
	
	public ToolCreatePolygon2D(){
		this(true);
	}
	
	public ToolCreatePolygon2D(boolean close){
		super();
		this.close = close;
		init();
	}
	
	private void init(){
		name = "Create 2D Polygon";
		setIcon();
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		Map2D map = (Map2D)targetPanel.getMap();
		switch(e.getEventType()){
		
		case ToolConstants.TOOL_MOUSE_CLICKED:
			if (toolPhase == 0){
				if (targetPanel.getToolLock())
					break;
				targetPanel.setToolLock(true);
				thisPoly = new Polygon2DInt();
				thisPoly.setAttribute("2D.ShowVertices", new MguiBoolean(true));
				thisPoly.setAttribute("2D.VertexScale", new MguiFloat(6f));
				thisPoly.setAttribute("2D.VertexColour", Color.red);
				thisPoly.setAttribute("2D.LineStyle", new BasicStroke(2f));
				startPt = map.getMapPoint(e.getPoint());
				nextPt = new Point2f(startPt);
				screenStartPt = e.getPoint();
				toolPhase = 1;
				name = "New Polygon2D to " + MguiDouble.getString(startPt.x, "###,##0.0") 
									  + ", " + MguiDouble.getString(startPt.y, "###,##0.0");
				thisPoly.getPolygon().vertices.add(new Point2f(startPt));
				targetPanel.appendTempShape(thisPoly);
				lastPt = null;
				//tempShape = null;
				break;
				}
				
			if (toolPhase == 1)
				toolPhase = 2;
			
			if (toolPhase > 0){
				//add a node
				nextPt = map.getMapPoint(e.getPoint());
				name = "New Polygon2D to " + MguiDouble.getString(nextPt.x, "###,##0.0") 
									+ ", " + MguiDouble.getString(nextPt.y, "###,##0.0");
				
				thisPoly.getPolygon().vertices.add(new Point2f(nextPt));
//				if (tempShape != null){
//					targetPanel.removeTempShape(tempShape);
//					}
				
				//tempShape = thisPoly;
				//targetPanel.drawShape2D(thisPoly);
				targetPanel.regenerateDisplay();
				break;
			}
				
			break;
			
		case ToolConstants.TOOL_MOUSE_MOVED:
			
			if (toolPhase > 0){
				Graphics2D g = (Graphics2D)targetPanel.getGraphics();
				g.setXORMode(Color.RED);
				Point screenPt = map.getScreenPoint(nextPt);
				if (lastPt != null){
					g.drawLine(screenPt.x, screenPt.y, lastPt.x, lastPt.y);
					if (close && toolPhase == 2)
						g.drawLine(screenStartPt.x, screenStartPt.y, lastPt.x, lastPt.y);
				}
				g.drawLine(screenPt.x, screenPt.y, e.getPoint().x, e.getPoint().y);
				if (close && toolPhase == 2)
					g.drawLine(screenStartPt.x, screenStartPt.y, e.getPoint().x, e.getPoint().y);
				name = "New Polygon2D to " + MguiDouble.getString(startPt.x, "###,##0.0") 
								    + ", " + MguiDouble.getString(startPt.y, "###,##0.0");
				lastPt = e.getPoint();
				break;
			}
			
			break;
		
		case ToolConstants.TOOL_MOUSE_DCLICKED:
			
			if (toolPhase == 1){
				//not a valid polygon (error dialog?)
				targetPanel.updateDisplays();
				targetPanel.setToolLock(false);
				toolPhase = 0;
				//if (tempShape != null)
					targetPanel.removeTempShape(thisPoly);
				}
			
			if (toolPhase == 2){
				//finish polygon and add to model
				if (close)
					((Polygon2D)thisPoly.thisShape).vertices.add(new Point2f(startPt));
				
				startPt = map.getMapPoint(e.getPoint());
				name = "Create 2D Polygon";
							
				thisPoly.updateShape();
				//if (tempShape != null)
					targetPanel.removeTempShape(thisPoly);
				targetPanel.addModelShape2D(thisPoly, true, true);
				
				targetPanel.updateDisplays();
				toolPhase = 0;
				targetPanel.setToolLock(false);
				break;
				}
			
			break;
		}
	}
	
	@Override
	protected void setIcon(){
		if (close){
			java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/tools/create_polyline_closed_30.png");
			if (imgURL != null)
				icon = new ImageIcon(imgURL);
			else
				InterfaceSession.log("Cannot find resource: mgui/resources/icons/tools/create_polyline_closed_30.png");
		}else{
			java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/tools/create_polyline_open_30.png");
			if (imgURL != null)
				icon = new ImageIcon(imgURL);
			else
				InterfaceSession.log("Cannot find resource: mgui/resources/icons/tools/create_polyline_open_30.png");
			}
	}
	
	@Override
	public Object clone(){
		return new ToolCreatePolygon2D(close);
	}
	
}