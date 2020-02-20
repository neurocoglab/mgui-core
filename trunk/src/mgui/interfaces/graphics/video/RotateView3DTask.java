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

package mgui.interfaces.graphics.video;

import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic3D;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.maps.Map3D;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;

import org.xml.sax.Attributes;

/****************************************************
 * Video task which rotates a {@link Camera3D} over a specific time interval, 
 * with the rotation defined by an X and a Y angle.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class RotateView3DTask extends VideoTask3D {

	public double x_angle, y_angle;
	protected Camera3D camera_start;
	//double x_start, y_start;
	
	double step;
	
	public RotateView3DTask(){
		this(0,0,0,0);
	}
	
	public RotateView3DTask(long start, long stop, double rot_x, double rot_y){
		setStart(start);
		setStop(stop);
		setXAngle(rot_x);
		setYAngle(rot_y);
	}
	
	@Override
	protected boolean do_it(InterfaceGraphic<?> g, long time) throws VideoException {
		
		if (!(g instanceof InterfaceGraphic3D))
			throw new VideoException("RotateView3DTask: window must be an instance of InterfaceGraphic3D");
		
		double duration = stop_time - start_time;
		
		InterfaceGraphic3D g3d = (InterfaceGraphic3D)g;
		Camera3D c = g3d.getCamera();
		
		if (duration == 0){
			started = true;
			c.rotateX(x_angle);
			c.rotateY(y_angle);
			return true;
			}
		
		if (!started){
			camera_start = new Camera3D();
			camera_start.setFromCamera(c, false, false);
			started = true;
			}
		
		double _time = time - start_time;
		double t = _time / duration;
		
		Camera3D camera_now = new Camera3D();
		camera_now.setFromCamera(camera_start);
		
		double angle_x = t * x_angle; 
		double angle_y = t * y_angle; 
		
		camera_now.rotateX(angle_x);
		camera_now.rotateY(angle_y);
		
		c.setFromCamera(camera_now, false, false);
		
		((Map3D)g3d.getMap()).updateTargetTransform();
		
		return false;
	}
	
	void init(){
		
	}
	
	

	public void setXAngle(double a){
		x_angle = a;
		init();
	}
	
	public void setYAngle(double a){
		y_angle = a;
		init();
	}
	
	@Override
	public String getName(){
		return "Rotate View 3D";
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
		
		xml = xml + _tab2 + "x_angle = '" + x_angle + "'\n";		//x rotation
		xml = xml + _tab2 + "y_angle = '" + y_angle + "'\n";		//y rotation
		xml = xml + _tab + "/>\n";			
		
		return xml;
	}
	
	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type){
		
		if (localName.equals("VideoTask")){
			super.handleXMLElementStart(localName, attributes, type);
			x_angle = Double.valueOf(attributes.getValue("x_angle"));
			y_angle = Double.valueOf(attributes.getValue("y_angle"));
			return;
			}
		
	}
	
	@Override
	public void setFromTask(VideoTask task){
		
		RotateView3DTask t = (RotateView3DTask)task;
		this.start_time = t.start_time;
		this.stop_time = t.stop_time;
		this.x_angle = t.x_angle;
		this.y_angle = t.y_angle;
		
	}
	
	@Override
	public Object clone(){
		RotateView3DTask task = new RotateView3DTask(start_time, stop_time, x_angle, y_angle);
		return task;
	}
	
	public static XMLObject getXMLInstance(Attributes attributes){
		return new RotateView3DTask(Long.valueOf(attributes.getValue("start")),
									Long.valueOf(attributes.getValue("stop")),
									Double.valueOf(attributes.getValue("x_angle")),
									Double.valueOf(attributes.getValue("y_angle")));
	}
	

}