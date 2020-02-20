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

package mgui.models.networks.components;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.trees.TreeObject;
import mgui.models.dynamic.DynamicModelComponent;


/****************************************
 * <P>Abstract generic class for neuro model components to inherit. Contains two lists, one for
 * events and one for connections. This class forces an implementation of <code>clone()</code> by its 
 * subclasses; as a general policy clones of model components should ensure that components 
 * states are also preserved in a clone.</P>
 * 
 * <P>All subclasses of this class should add its parameters as <code>Attribute</code>s. These
 * parameters should have their own get and set methods implemented. Primitives should be wrapped
 * by instances of <code>arNumber</code> (java wrappers such as <code>Integer</code> are fine too, but do not allow their
 * values to be dynamically changed...)</P>
 * 
 * <P>Variables (e.g., clock) should be declared as individual class members.</P> 
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */

public abstract class AbstractNetworkComponent extends AbstractInterfaceObject
											 implements DynamicModelComponent,
														AttributeObject,
														AttributeListener,
														TreeObject,
														InterfaceObject{

	@Override
	public Object clone(){return null;}	//?
	
}