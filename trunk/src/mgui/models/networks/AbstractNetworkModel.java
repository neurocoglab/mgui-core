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

package mgui.models.networks;

import java.util.ArrayList;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.graphs.GraphableObject;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.models.InterfaceAbstractModel;
import mgui.models.dynamic.DynamicModelComponent;
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelEnvironmentSensor;
import mgui.models.dynamic.DynamicModelException;
import mgui.models.dynamic.DynamicModelListener;


/***************************************
 * Abstract class for all neuronal models to inherit.
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */
public abstract class AbstractNetworkModel extends InterfaceAbstractModel 
										 implements InterfaceObject,
													GraphableObject,
													AttributeObject{

	ArrayList<DynamicModelListener> modelListeners = new ArrayList<DynamicModelListener>();
	ArrayList<DynamicModelEnvironmentSensor> sensors = new ArrayList<DynamicModelEnvironmentSensor>();
	public DynamicModelEnvironment environment;
	public AttributeList attributes = new AttributeList();
	
	public void setEnvironment(DynamicModelEnvironment e) throws DynamicModelException{
		if (environment != null)
			for (int i = 0; i < sensors.size(); i++)
				environment.removeSensor(sensors.get(i));
		environment = e;
		for (int i = 0; i < sensors.size(); i++)
			e.addSensor(sensors.get(i));
	}
	
	public ArrayList<DynamicModelEnvironmentSensor> getSensors(){
		return sensors;
	}
	
	public void addSensor(DynamicModelEnvironmentSensor sensor){
		sensors.add(sensor);
		sensor.setIndex(sensors.size() - 1);
		if (environment != null)
			environment.addSensor(sensor);
	}
	
	public void removeSensor(DynamicModelEnvironmentSensor sensor){
		sensors.remove(sensor);
		//update indices
		for (int i = 0; i < sensors.size(); i++)
			sensors.get(i).setIndex(i);
		if (environment != null)
			environment.removeSensor(sensor);
	}
	
	public DynamicModelEnvironment getEnvironment(){
		return environment;
	}
	
	public void addListener(DynamicModelListener l){
		modelListeners.add(l);
	}
	
	public void removeListener(DynamicModelListener l){
		modelListeners.remove(l);
	}
	
	protected void fireComponentAdded(DynamicModelComponent c){
		for (int i = 0; i < modelListeners.size(); i++)
			modelListeners.get(i).componentAdded(c);
	}
	
	protected void fireComponentRemoved(DynamicModelComponent c){
		for (int i = 0; i < modelListeners.size(); i++)
			modelListeners.get(i).componentRemoved(c);
	}
	
	public Attribute<?> getAttribute(String attrName) {
		return attributes.getAttribute(attrName);
	}

	public AttributeList getAttributes() {
		return attributes;
	}

	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);	
	}

	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}
	
	@Override
	public Object getAttributeValue(String name) {
		Attribute<?> attribute = getAttribute(name);
		if (attribute == null) return null;
		return attribute.getValue();
	}

	
	//public abstract InterfaceTreeNode getTreeNode(); 
	public abstract InterfaceAbstractGraph getGraph();
	@Override
	public abstract Object clone();
}