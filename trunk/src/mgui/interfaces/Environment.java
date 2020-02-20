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

package mgui.interfaces;

import java.util.ArrayList;

/**************************************
 * Interface to be implemented by all mgui environments. An environment holds the static
 * system parameters and interface object containers. Environments are static by nature and
 * therefore should not be instantiated. As a general policy subclasses should implement
 * a private constructor which throws an <code>AssertionError</code> to prevent attempts to
 * instantiate it.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface Environment {

	/*************************************
	 * Returns a named list of interface objects, probably belonging to a particular
	 * container and having the same type (although this is not necessarily true).
	 * 
	 * <p>Policy: All instances of <code>Environment</code> should have private
	 * constructors which prevent instantiation; for added safety, they can also
	 * throw assertion errors if called from within the class..
	 * 
	 * @param name
	 * @return
	 */
	public ArrayList<InterfaceObject> getInterfaceObjectList(String name);
	
}