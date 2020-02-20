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

package mgui.models.dynamic;


/********************************
 * Represents a discrete causal event in a dynamic model. Events are passed between components,
 * and should be passed only by the component's <code>addEvent</code> method. Components should be able
 * to handle specific events based upon the <code>eventCode</code>, and events between components are
 * exclusive to these components.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @see DynamicModelComponent
 *
 */

public interface DynamicModelEvent extends TimeStepListener {

	public int getCode();
	public double getDelay();
	
	/*
	int eventCode;
	
	/*
	public DynamicModelEvent(DynamicModelComponent source, int code){
		super(source);
		eventCode = code;
	}
	
	
	public DynamicModelEvent(int code){
		//super();
		eventCode = code;
	}
	
	/*
	public DynamicModelComponent getSource(){
		return (DynamicModelComponent)source;
	}
	
	
	public int getCode(){
		return eventCode;
	}
	*/
}