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

package mgui.interfaces.graphics.video;

import org.jogamp.java3d.Transform3D;
import org.jogamp.vecmath.AxisAngle4d;
import org.jogamp.vecmath.Quat4d;
import org.jogamp.vecmath.Vector3d;

import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic3D;
import mgui.interfaces.graphics.View3D;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.maps.Map3D;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;

import org.xml.sax.Attributes;


public class SetView3DTask extends VideoTask3D {

	public View3D view_target;
	protected Camera3D camera_start;
	protected Quat4d q_LOS, q_Up;
	//protected AxisAngle4d a_LOS, a_Up;
	protected AxisAngle4d axis_LOS = new AxisAngle4d(), axis_Up = new AxisAngle4d();
	public boolean set_zoom = true, set_target = true;
	
	public SetView3DTask(){
		this(0, 1000, new View3D());
	}
	
	public SetView3DTask(long start, long stop, View3D view){
		this(start, stop, view, true, true);
	}
	
	public SetView3DTask(long start, long stop, View3D view, boolean set_zoom, boolean set_target){
		setStart(start);
		setStop(stop);
		setView(view);
		this.set_zoom = set_zoom;
		this.set_target = set_target;
	}
	
	@Override
	protected boolean do_it(InterfaceGraphic g, long time) throws VideoException{
		
		if (view_target == null)
			throw new VideoException("SetView3DTask: view not set");
		
		if (!(g instanceof InterfaceGraphic3D))
			throw new VideoException("SetView3DTask: window must be an instance of InterfaceGraphic3D");
		
		double duration = stop_time - start_time;
		InterfaceGraphic3D g3d = (InterfaceGraphic3D)g;
		double _time = time - start_time;
		
		if (duration == 0 || duration <= _time){
			//instantaneous
			g3d.getCamera().setFromCamera(view_target.camera, set_target, set_zoom);
			started = true;
		}else{
			double t = _time / duration; 
			
			if (!started) {
				camera_start = new Camera3D();
				camera_start.setFromCamera(g3d.getCamera(), true, true);
				started = true;
				}
			
			//angle interpolation
			double angle = GeometryFunctions.getSignedAngle(view_target.camera.lineOfSight,
													  		camera_start.lineOfSight) * t;
			Vector3d axis = new Vector3d();
			axis.cross(camera_start.lineOfSight, view_target.camera.lineOfSight);
			boolean t_LOS = GeometryFunctions.isNonZeroVector(axis);
			
			if (t_LOS){
				axis.normalize();
				axis_LOS.set(axis.x,
							 axis.y,
							 axis.z,
							 angle);
				}
			
			angle = GeometryFunctions.getSignedAngle(view_target.camera.upVector,
					  						   	   	 camera_start.upVector) * t;
			axis.cross(camera_start.upVector, view_target.camera.upVector);
			boolean t_Up = GeometryFunctions.isNonZeroVector(axis);
			
			if (t_Up){
				axis.normalize();
				axis_Up.set(axis.x,
							axis.y,
							axis.z,
							angle);
				}
				
			Camera3D camera_now = new Camera3D();
			camera_now.setFromCamera(camera_start, true, true);
			Camera3D camera_target = view_target.camera;
			if (set_zoom)
				camera_now.distance = camera_start.distance + t * (camera_target.distance - camera_start.distance);
			if (set_target){
				camera_now.centerOfRotation.x += t * (camera_target.centerOfRotation.x - camera_start.centerOfRotation.x);
				camera_now.centerOfRotation.y += t * (camera_target.centerOfRotation.y - camera_start.centerOfRotation.y);
				camera_now.centerOfRotation.z += t * (camera_target.centerOfRotation.z - camera_start.centerOfRotation.z);
				}
			Transform3D T = new Transform3D();
			if (t_LOS){
				T.set(axis_LOS);
				T.transform(camera_now.lineOfSight);
				camera_now.lineOfSight.normalize();
				}
			if (t_Up){
				T.set(axis_Up);
				T.transform(camera_now.upVector);
				camera_now.upVector.normalize();
				}
					
			g3d.getCamera().setFromCamera(camera_now, true, true);
			}
			
		((Map3D)g3d.getMap()).updateTargetTransform();
		
		return true;
	}
	
	public void setView(View3D view){
		this.view_target = view;
	}

	@Override
	public String getName(){
		return "Set View 3D";
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	@Override
	public String getDTD(){
		//TODO: retrieve from file
		return "";
	}
	
	public String getXMLSchema(){
		//TODO: retrieve from file
		return "";
	}
	
	@Override
	public String getXML(int tab){
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String xml = super.getXML(tab);
		
		xml = xml + _tab2 + "set_zoom = '" + set_zoom + "'\n";			//set zoom
		xml = xml + _tab2 + "set_target = '" + set_target + "'\n";		//set target
		xml = xml + _tab2 + ">\n";			
		
		xml = xml + _tab2 + "<TargetView>";
		xml = xml + view_target.getXML(tab + 2);
		xml = xml + "\n" + _tab2 + "</TargetView>";
		
		xml = xml + "\n" + _tab + "</VideoTask>\n";
		
		return xml;
	}
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type){
		
		if (localName.equals("VideoTask")){
			super.handleXMLElementStart(localName, attributes, type);
			set_target = Boolean.valueOf(attributes.getValue("set_target"));
			set_zoom = Boolean.valueOf(attributes.getValue("set_zoom"));
			return;
			}
		
		if (localName.equals("TargetView")){
			view_target = new View3D();
			return;
			}
		
		view_target.handleXMLElementStart(localName, attributes, null);
		
	}
	
	@Override
	public void setFromTask(VideoTask task){
		
		SetView3DTask t = (SetView3DTask)task;
		this.start_time = t.start_time;
		this.stop_time = t.stop_time;
		this.set_target = t.set_target;
		this.set_zoom = t.set_zoom;
		this.view_target = t.view_target;
		
	}
	
	@Override
	public Object clone(){
		SetView3DTask task = new SetView3DTask(start_time, stop_time, view_target, set_zoom, set_target);
		return task;
	}
	
	public static XMLObject getXMLInstance(Attributes attributes){
		return new SetView3DTask(Long.valueOf(attributes.getValue("start")),
								 Long.valueOf(attributes.getValue("stop")),
								 null,
								 Boolean.valueOf(attributes.getValue("set_zoom")),
								 Boolean.valueOf(attributes.getValue("set_target")));
	}
	
}