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

package mgui.interfaces.math;

import java.util.ArrayList;

/***********************************
 * Interface allowing an object to represent a set of variables of a particular class type.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */
public interface VariableObject {

	/***************************
	 * Returns a list of variables for this object, which can be inserted into a mathematical
	 * expression.
	 * 
	 * @return The list of variables contained in this object
	 */
	public ArrayList<String> getVariables();
	
	/***************************
	 * Returns the dimensions of this object as an array of integers
	 * 
	 * @return the dimensions of this object
	 */
	public int[] getDimensions();
	
	/***************************
	 * Returns the value of a variable in this object, at the specified element location. If <code>element</code>
	 * is not of the correct length for the variable object (i.e., as determined by the length of
	 * {@link getDimensions()}), a value of <code>Double.NaN</code> is returned.
	 * 
	 * @param variable Name of the variable
	 * @param element The element at which to retrieve a value 
	 * @return The value at <code>element</code>, as a <code>double</code>
	 */
	public double getVariableValue(String variable, int[] element);
	
	/***************************
	 * Returns an object containing the set of values for <code>variable</code>. The returned class type is
	 * specified by {@link getVariableType()}.
	 * 
	 * @param variable
	 * @return The values corresponding to <code>variable</code>
	 */
	public Object getVariableValues(String variable);
	
	public String getName();
	
	/****************************
	 * Sets the variable's values with the <code>values</code> object, which must be a type acceptable to the
	 * variable object. If not, a value of <code>false</code> is returned.
	 * 
	 * @param variable		The variable to update
	 * @param values		An <code>Object</code> containing the new data
	 */
	public boolean setVariableValues(String variable, Object values);
	
	/****************************
	 * Specifies the class type of the object returned by {@link getVariableValues()}. 
	 * 
	 * @return The class type
	 */
	public Class<?> getVariableType();
	
	/****************************
	 * Specifies whether this variable object supports the given class type when setting variables. 
	 * 
	 * TODO: solve erasure issue which prevents type-checking of generic <code>ArrayList</code>s. See
	 * {@link http://bugs.sun.com/view_bug.do?bug_id=5098163}
	 * 
	 * @param type
	 * @return <code>true</code>, if this class type is supported
	 */
	public boolean supportsVariableType(Class<?> type);
	
}