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

package mgui.interfaces.util;

import java.lang.reflect.Field;
import java.util.ArrayList;

import mgui.classes.ClassFunctions;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.Utility;

/***************************************************************
 * Utility class for interface objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceFunctions extends Utility {

	/************************************************************
	 * Cleans up the members of this object; i.e., searches for destroyed objects and
	 * releases them.
	 * 
	 * @param object
	 */
	public static void cleanInterfaceObject(InterfaceObject object){
		
		// Find all members and call their clean functions, if they are
		// InterfaceObjects
		ArrayList<Field> fields = ClassFunctions.getAllFields(object.getClass());
		
		for (int i = 0; i < fields.size(); i++){
			Field field = fields.get(i);
			if (ClassFunctions.isMember(field.getType(), InterfaceObject.class)){
				try{
					InterfaceObject obj = (InterfaceObject)field.get(object);
					if (obj.isDestroyed())
						field.set(object, null);		// If destroyed, release it
				}catch (Exception e){} // Do nothing
				}
			}
		
	}
	
	
}