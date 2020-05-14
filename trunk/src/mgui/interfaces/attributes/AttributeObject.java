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

package mgui.interfaces.attributes;

import mgui.interfaces.NamedObject;


/**************************************************
 * Interface for objects which specify their attributes using an {@link AttributeList}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public interface AttributeObject extends NamedObject {

	/***************************************
	 * Returns the list of current attributes for this object.
	 * 
	 * @return
	 */
	public AttributeList getAttributes();
	
	/****************************************
	 * Returns a specific attribute for this object.
	 * 
	 * @param name
	 * @return
	 */
	public Attribute<?> getAttribute(String name);
	
	/*****************************************
	 * Sets the list of attributes for this object.
	 * 
	 * @param list
	 */
	public void setAttributes(AttributeList list);
	
	/*******************************************
	 * Sets a value for a specific attribute.
	 * 
	 * @param name
	 * @param value
	 */
	public void setAttribute(String name, Object value);
	
	/********************************************
	 * Gets the value of attribute {@code name}, or {@code null} if it does not exist.
	 * 
	 * @param name Name of the attribute
	 * @return the value of attribute {@code name}, or {@code null} if it does not exist
	 */
	public Object getAttributeValue(String name);
	
	
	
}