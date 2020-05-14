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

package mgui.classes;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import mgui.interfaces.Utility;

/*************************************************
 * Set of functions on classes
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ClassFunctions extends Utility {

	public static boolean isMember(Class<?> subClass, Class<?> superClass){
		Class<?>[] classes = superClass.getClasses();
		for (int i = 0; i < classes.length; i++)
			if (classes[i].equals(subClass))
				return true;
		return false;
	}
	
	public static boolean isGenericAssignable(Class<?> c1, Class<?> c2){
		
		Type t1 = c1.getGenericSuperclass();
		if (t1 instanceof ParameterizedType){
			Type p1 = ((ParameterizedType)t1).getActualTypeArguments()[0];
			
			Type t2 = c2.getGenericSuperclass();
			if (t2 instanceof ParameterizedType){
				Type p2 = ((ParameterizedType)t2).getActualTypeArguments()[0];
				return p1.equals(p2);
				}
			}
		
		return false;
	}
	
	/*************************************************
	 * Retrieves all public, protected, and private fields of this class and its superclasses.
	 * 
	 * @param clazz
	 * @return
	 */
	public static ArrayList<Field> getAllFields(Class<?> clazz){
		return (getFieldsRecursive(clazz, new ArrayList<Field>()));
	}
	
	static ArrayList<Field> getFieldsRecursive(Class<?> clazz, ArrayList<Field> fields){
		Class<?> super_clazz = clazz.getSuperclass();
	    if(super_clazz != null)
	    	getFieldsRecursive(super_clazz, fields);
	    fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
	    return fields;
	}
		
}