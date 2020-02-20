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

package mgui.models.environments;

import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.models.dynamic.DynamicModelEnvironmentEvent;
import mgui.models.events.SimpleEvent;

public class SimpleEnvironmentEvent extends SimpleEvent implements DynamicModelEnvironmentEvent {

	//public double[] values;
	public SimpleEnvironment environment;
	
	public SimpleEnvironmentEvent(SimpleEnvironment environment){
		this.environment = environment;
		delay = 0;
	}
	
	public DynamicModelEnvironment getEnvironment(){
		return environment;
	}
	
	public double[] getInputState(){
		return environment.getInputState();
	}
	
}