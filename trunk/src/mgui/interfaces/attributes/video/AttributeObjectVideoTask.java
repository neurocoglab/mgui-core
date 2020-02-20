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

package mgui.interfaces.attributes.video;

import java.lang.reflect.Constructor;

import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.video.VideoException;
import mgui.interfaces.graphics.video.VideoTask;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.numbers.MguiNumber;
import mgui.numbers.NumberFunctions;

import org.xml.sax.Attributes;

/******************************************************
 * Represents a video task which modifies an {@link Attribute}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class AttributeObjectVideoTask extends VideoTask {

	public AttributeObject object;
	public String attribute = "Name";
	public Object start_value, end_value;
	public boolean incremental = false;   // For numeric attributes, increments over the range across the time interval 
	public double step = 0.1; 		  // Step at which to increment value
	
	boolean ended = false;
	
	//if non-null, indicates that task must search for objects in current display panel
	public String object_name, object_class;
	
	public AttributeObjectVideoTask(){
		this(0, 0, null, null, null);
	}
	
	/****************************************************
	 * Constructs a basic set attribute task (no iteration)
	 * 
	 * @param start
	 * @param stop
	 * @param object
	 * @param attribute
	 * @param new_value
	 */
	public AttributeObjectVideoTask(long start, long stop, AttributeObject object, String attribute, Object start_value){
		setStart(start);
		setStop(stop);
		this.object = object;
		this.attribute = attribute;
		this.start_value = start_value;
		this.end_value = start_value;
		this.incremental = false;
	}
	
	/****************************************************
	 * Constructs a set attribute task with possible iteration
	 * 
	 * @param start
	 * @param stop
	 * @param object
	 * @param attribute
	 * @param new_value
	 */
	public AttributeObjectVideoTask(long start, long stop, AttributeObject object, String attribute, 
								   Object start_value, Object end_value, boolean incremental, double step){
		setStart(start);
		setStop(stop);
		this.object = object;
		this.attribute = attribute;
		this.start_value = start_value;
		this.end_value = end_value;
		this.incremental = incremental;
		this.step = step;
	}
	
	private double last_increment = 0;
	
	@Override
	protected boolean do_it(InterfaceGraphic<?> g, long time) throws VideoException {
		if (ended) return false;
		
		// Set value once, if not iterating, or attribute is not numeric
		boolean is_numeric = object.getAttribute(attribute).getValue() instanceof MguiNumber;
		if (!incremental || !is_numeric){
			started = true;
			object.setAttribute(attribute, end_value);
			ended = true;
			return true;
			}
		
		double start =  NumberFunctions.getValueForObject(start_value);
		double delta_value = NumberFunctions.getValueForObject(end_value) - start;
		double sign_step = delta_value / Math.abs(delta_value) * Math.abs(step);
		
		if (!started){
			last_increment = sign_step;
			object.setAttribute(attribute, start_value);
			started = true;
			return true;
			}
		
		// increment value
		double duration = stop_time - start_time;
		double _time = time - start_time;
		double t = _time / duration;
		
		delta_value *= t;
		if (Math.abs(delta_value) < Math.abs(last_increment)) return true;
		last_increment += step * sign_step;
		MguiNumber value = (MguiNumber)((MguiNumber)start_value).clone();
		value.add(delta_value);
		
		object.setAttribute(attribute, value);
		
		return true;
		
	}

	@Override
	public String getName() {
		return "Attribute Object Task";
	}

	@Override
	public void setFromTask(VideoTask task) {
		
		AttributeObjectVideoTask _task = (AttributeObjectVideoTask)task;
		this.start_time = task.start_time;
		this.stop_time = task.stop_time;
		object = _task.object;
		attribute = _task.attribute;
		start_value = _task.start_value;
		end_value = _task.end_value;
		incremental = _task.incremental;
		step = _task.step;
	}

	public String getXMLSchema() {
		return "";
	}
	
	@Override
	public String getXML(int tab){
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String xml = super.getXML(tab);
		
		if (object.getAttribute("Name") != null)
			xml = xml + _tab2 + "object = '" + (String)object.getAttribute("Name").getValue() + "'\n";
		else
			xml = xml + _tab2 + "object = '" + object.toString() + "'\n";									//object
		xml = xml + _tab2 + "object_class = '" + object.getClass().getCanonicalName() + "'\n";			//object class
		xml = xml + _tab2 + "attribute = '" + attribute + "'\n";										//attribute
		xml = xml + _tab2 + "start_value = '" + start_value.toString() + "'\n";								//new_value
		xml = xml + _tab2 + "start_value_class = '" + start_value.getClass().getCanonicalName() + "'\n";	//new_value class
		xml = xml + _tab2 + "end_value = '" + end_value.toString() + "'\n";								//new_value
		xml = xml + _tab2 + "end_value_class = '" + end_value.getClass().getCanonicalName() + "'\n";	//new_value class
		xml = xml + _tab2 + "iterate = '" + Boolean.toString(incremental) + "'\n";								//iterate
		xml = xml + _tab2 + "step = '" + Double.toString(step) + "'\n";								//step
		xml = xml + _tab + "/>\n";			
		
		return xml;
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type){
		//should set up object and whatnot here
		
		if (localName.equals("VideoTask")){
			super.handleXMLElementStart(localName, attributes, type);
			object_name = attributes.getValue("object");
			object_class = attributes.getValue("object_class");
			start_value = getObjectForValue(attributes.getValue("start_value"),
										  attributes.getValue("start_value_class"));
			end_value = getObjectForValue(attributes.getValue("end_value"),
					  attributes.getValue("end_value_class"));
			incremental = Boolean.valueOf(attributes.getValue("iterate"));
			attribute = attributes.getValue("attribute"); 
			step = Double.valueOf(attributes.getValue("step"));
			return;
			}
	}
	
	@Override
	public void handleXMLElementEnd(String localName){
		
	}
	
	@Override
	public void updateTask(InterfaceDisplayPanel panel){
		
		if (object_name == null) return;
		object = (AttributeObject)InterfaceSession.getWorkspace().findInterfaceObjectForName(object_name, object_class);
		object_name = null;
		object_class = null;
		
	}
	
	//tries to instantiate an object from a string value and class name
	//class must have a single-string constructor
	//silently returns null if failure
	Object getObjectForValue(String value, String value_class){
		
		try{
			Class c = Class.forName(value_class);
			Constructor constr = c.getConstructor(new Class[]{String.class});
			return constr.newInstance(new Object[]{value});
			
		}catch (Exception e){
			//no class found, or class doesn't have a single String constructor
			//e.printStackTrace();
			return null;
			}
		
	}
	
	@Override
	public Object clone(){
		AttributeObjectVideoTask task = new AttributeObjectVideoTask(start_time, 
																	 stop_time, 
																	 object, 
																	 attribute,
																	 start_value,
																	 end_value,
																	 incremental,
																	 step);
		return task;
	}
	
	public static XMLObject getXMLInstance(Attributes attributes){
		return new AttributeObjectVideoTask(Long.valueOf(attributes.getValue("start")),
								 			Long.valueOf(attributes.getValue("stop")),
								 			null,
								 			attributes.getValue("attribute"),
								 			null);
								 
	}
	
}