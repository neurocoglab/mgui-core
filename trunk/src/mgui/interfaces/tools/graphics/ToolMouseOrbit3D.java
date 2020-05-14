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
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector2d;
import org.jogamp.vecmath.Vector3d;

import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.events.CameraEvent.EventType;
import mgui.interfaces.graphics.InterfaceGraphic3D;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.maps.Map3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.numbers.MguiDouble;


/*************************
 * Tool to allow user to navigate a Graphic3D interface using the mouse. Allows
 * zoom, pan, and rotate operations which alter a Camera3D object (which in turn alters
 * the ViewingPlatform transform via Map3D in the Java3D universe). While the OrbitBehavior 
 * class performs these functions well, the present class allows interaction with an 
 * accessible Map3D object, which is desired as an interface for other aspects of this object 
 * model.
 * 
 * @author Andrew Reid
 *
 */

public class ToolMouseOrbit3D extends Tool3D {

	//need a start point, to indicate where mouse was first clicked
	Point startPt;
	//camera position at first mouse click
	Vector3d startVector;
	Vector3d upVector;
	Point3d startCamPt;
	Vector2d startXY;
	double startVal;
	
	//variables to determine rates of movement
	public double zoomRate = 1;
	public double panRate = 1;
	public double rotateRate = 1;
	public Camera3D theCamera;
	
	//for operations
	private double deltaX, deltaY;
	
	public ToolMouseOrbit3D(){
		super();
		init();
	}
	
	public ToolMouseOrbit3D(InterfaceGraphic3D target){
		super();
		init();
		setTargetPanel(target);
	}
	
	private void init(){
		name = "Mouse 3D Orbit";
	}
	
	@Override
	public void setTargetPanel(InterfacePanel panel){
		super.setTargetPanel(panel);
		InterfaceGraphic3D panel3d = (InterfaceGraphic3D)panel;
		theCamera = ((Map3D)panel3d.getMap()).getCamera();
	}
	
	public void setZoomRate(double newRate){
		if (newRate > 0)
		zoomRate = newRate;
	}
	
	public void setPanRate(double newRate){
		if (newRate > 0)
		panRate = newRate;
	}
	
	public void setRotateRate(double newRate){
		if (newRate > 0)
		rotateRate = newRate;
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		
		int type = e.getEventType();
		
		//ctrl-drag is the same behaviour as rdrag (circumvents Linux issue)
		if (type == ToolConstants.TOOL_MOUSE_DRAGGED && e.isCtrlPressed())
			type = ToolConstants.TOOL_MOUSE_RDRAGGED;
		if (type == ToolConstants.TOOL_MOUSE_DRAGGED && e.isShiftPressed())
			type = ToolConstants.TOOL_MOUSE_MDRAGGED;
		
		switch(type){
			case ToolConstants.TOOL_MOUSE_DRAGGED:
				switch (toolPhase){
					case 0:
						//initialize rotation behaviour
						startPt = e.getPoint();
						startVector = (Vector3d)theCamera.getLineOfSight().clone();
						upVector = (Vector3d)theCamera.getUpVector().clone();
						toolPhase = 1;
						name = "Rotating";
						break;
					
					case 1:
						//rotate using deltaX, deltaY as vector in viewing plane;
						//X, Y, Z angles can be derived from this vector and the
						//rotateRate
						deltaX = e.getPoint().x - startPt.x;
						deltaY = e.getPoint().y - startPt.y;
						theCamera.setListen(false);
						theCamera.setLineOfSight((Vector3d)startVector.clone());
						theCamera.setUpVector((Vector3d)upVector.clone());
						theCamera.rotateY(deltaX * rotateRate * 0.01);
						theCamera.rotateX(-deltaY * rotateRate * 0.01);
						theCamera.setListen(true);
						theCamera.fireCameraAngleChanged();
						name = "Rotating: " + MguiDouble.getString(Math.toDegrees(deltaX * rotateRate * 0.01),
								"##0.00") + "X, " +
											  MguiDouble.getString(Math.toDegrees(-deltaY * rotateRate * 0.01),
								"##0.00") + "Y";
						break;
					
				}
				
				break;
				
			case ToolConstants.TOOL_MOUSE_UP:
				toolPhase = 0;
				name = "Mouse 3D Orbit";
				break;
				
			case ToolConstants.TOOL_MOUSE_WHEEL:
				theCamera.zoomDistance(e.getVal() * zoomRate / 100 * theCamera.getDistance());
				break;
				
			case ToolConstants.TOOL_MOUSE_RDRAGGED:
				switch (toolPhase){
					case 0:
						//initialize zoom behaviour
						startPt = e.getPoint();
						/** @todo limit minimum variable by model size **/
						startVal = Math.max(theCamera.getDistance(), 0.01);
						toolPhase = 1;
						name = "Zooming";
						break;
					
					case 1:
						//zoom using distance from start point
						deltaY = e.getPoint().y - startPt.y;
						theCamera.setListen(false);
						theCamera.setDistance(startVal);
						theCamera.zoomDistance(deltaY * zoomRate / 100 * startVal);
						theCamera.setListen(true);
						theCamera.fireCameraListeners(EventType.ZoomChanged);
						name = "Zooming: " + MguiDouble.getString((startVal + (deltaY * zoomRate / 100 * startVal))
																/ startVal * 100, "##0.00") + "%";
						break;
					}
				
				break;
				
			case ToolConstants.TOOL_MOUSE_MDRAGGED:
				switch (toolPhase){
					case 0:
						//initialize panning behaviour
						startPt = e.getPoint();
						startXY = theCamera.getTranslateXY();
						toolPhase = 1;
						name = "Panning";
						break;
					
					case 1:
						//zoom using distance from start point
						deltaX = e.getPoint().x - startPt.x;
						deltaY = e.getPoint().y - startPt.y;
						theCamera.setListen(false);
						theCamera.setTranslateXY((Vector2d)startXY.clone());
						theCamera.translateX(deltaX * theCamera.getDistance() * panRate / 1000);
						theCamera.translateY(deltaY * theCamera.getDistance() * panRate / 1000);
						theCamera.setListen(true);
						theCamera.fireCameraListeners(EventType.CenterChanged);
						name = "Panning: " + 
								MguiDouble.getString(deltaX * theCamera.getDistance() * panRate / 1000,
										"##0.00") + ", " +
								MguiDouble.getString(-deltaY * theCamera.getDistance() * panRate / 1000,
										"##0.00") ;
						break;
					}
				
				break;
		
			}//switch event type
		
		}//handleToolEvent
	
	@Override
	public Object clone(){
		return new ToolMouseOrbit3D();
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/tools/zoom_dynamic_30.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/tools/zoom_dynamic_30.png");
	}

}//class