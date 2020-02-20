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

package mgui.models.dynamic.ode;

import mgui.models.dynamic.DynamicModelComponent;

import org.opensourcephysics.numerics.ODE;


/********************************************
 * Interface for components which solve ordinary differential equations (ODEs).
 * ODEs are solved using code from the OpenSourcePhysics project. 
 * See http://www.opensourcephysics.org for details.
 * 
 * @author Andrew Reid
 *
 */

public interface DynamicModelODEComponent extends DynamicModelComponent, ODE {

	
}