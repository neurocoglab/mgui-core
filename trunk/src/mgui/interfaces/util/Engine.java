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

import java.util.ArrayList;

import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;

/****************************************************
 * Interface for all "engine" classes; i.e., instantiable classes which hold attributes and do work.
 * An engine is specified by a set of "operations". Each operation can be implemented by a set of "methods".
 * The combination of operation & method specifies what computation will be performed.
 * 
 * <p>Each operation/method is fully specified by a corresponding {@linkplain AttributeList}, which must
 * be set prior to calling that method.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 **/
public interface Engine extends AttributeObject {

	/************************************************
	 * Returns a list of the operations available for this {@code Engine}. Operations signify a general
	 * operation, which can be implemented through any number of "methods". 
	 * 
	 * @return
	 */
	public ArrayList<String> getOperations();
	
	/************************************************
	 * Returns a list of methods implementing a particular {@code operation}. 
	 * 
	 * @param operation
	 * @return
	 */
	public ArrayList<String> getMethods(String operation);
	
	/************************************************
	 * Calls the specified operation/method pair. Returns {@code true} if the operation was successful.
	 * 
	 * @param operation Operation to perform.
	 * @param method 	Method with which to perform operation. Can be {@code null} if this operation has no sub-methods
	 * @param progress 	Optional progress updater (can be {@code null})
	 * @return
	 */
	public boolean callMethod(String operation, String method, ProgressUpdater progress);
	
	/************************************************
	 * Calls the specified operation/method pair. Returns {@code true} if the operation was successful.
	 * 
	 * @param operation Operation to perform.
	 * @param method 	Method with which to perform operation. Can be {@code null} if this operation has no sub-methods
	 * @param params 	A list of additional parameters (not in the attributes) for this method
	 * @param progress 	Optional progress updater (can be {@code null})
	 * @return
	 */
	public boolean callMethod(String operation, String method, ArrayList<?> params, ProgressUpdater progress);
	
	
	/*****************************************************
	 * Returns the attributes list corresponding to the {@code operation} and {@code method}.
	 * 
	 * @param key
	 * @return The corresponding attributes, or {@code null} if no such combination of {@code operation}/
	 * 			{@code method} exists. 
	 */
	public AttributeList getAttributes(String operation, String method);
	
}