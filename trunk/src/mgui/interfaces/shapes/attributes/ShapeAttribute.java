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

package mgui.interfaces.shapes.attributes;

import mgui.interfaces.attributes.Attribute;

/*************************************************
 * Extends {@linkplain Attribute} to indicate shape-specific parameters (e.g., whether
 * a change to the value requires a redraw.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <V>
 */
public class ShapeAttribute<V> extends Attribute<V> {

	protected boolean needsRedraw2D = false;
	protected boolean needsRedraw3D = false;
	
	/**************************
	 * Instantiates a {@code ShapeAttribute}
	 * 
	 * @param name			Name of the attribute
	 * @param clazz 		Class of the attribute value
	 * 
	 */
	public ShapeAttribute(String name, Class<V> clazz){
		super(name, clazz);
	}
	
	/**************************
	 * Instantiates a {@code ShapeAttribute}
	 * 
	 * @param name			Name of the attribute
	 * @param value 		Value of the attribute
	 * 
	 */
	public ShapeAttribute (String name, V value){
		this(name, value, true, true, true, true);
	}
	
	/**************************
	 * Instantiates a {@code ShapeAttribute}
	 * 
	 * @param name			Name of the attribute
	 * @param value 		Value of the attribute
	 * @param needsRedraw2D	Does changing this attribute require a 2D/3D redraw?
	 * 
	 */
	public ShapeAttribute (String name, V value, boolean needsRedraw){
		this(name, value, needsRedraw, needsRedraw, true, true);
	} 
	
	/**************************
	 * Instantiates a {@code ShapeAttribute}
	 * 
	 * @param name			Name of the attribute
	 * @param value 		Value of the attribute
	 * @param needsRedraw2D	Does changing this attribute require a 2D redraw?
	 * @param needsRedraw3D	Does changing this attribute require a 3D redraw?
	 * 
	 */
	public ShapeAttribute (String name, V value, boolean needsRedraw2D, boolean needsRedraw3D){
		this(name, value, needsRedraw2D, needsRedraw3D, true, true);
	} 
	
	/**************************
	 * Instantiates a {@code ShapeAttribute}
	 * 
	 * @param name			Name of the attribute
	 * @param value 		Value of the attribute
	 * @param needsRedraw2D	Does changing this attribute require a 2D redraw?
	 * @param needsRedraw3D	Does changing this attribute require a 3D redraw?
	 * @param isEditable 	Is this attribute editable?
	 * 
	 */
	public ShapeAttribute (String name, V value, boolean needsRedraw2D, boolean needsRedraw3D, boolean isEditable){
		this(name, value, needsRedraw2D, needsRedraw3D, isEditable, true);
	}
	
	/**************************
	 * Instantiates a {@code ShapeAttribute}
	 * 
	 * @param name			Name of the attribute
	 * @param value 		Value of the attribute
	 * @param needsRedraw2D	Does changing this attribute require a 2D redraw?
	 * @param needsRedraw3D	Does changing this attribute require a 3D redraw?
	 * @param isEditable 	Is this attribute editable?
	 * @param isCopiable 	Is this attribute copiable?
	 * 
	 */
	public ShapeAttribute (String name, V value, boolean needsRedraw2D, 
						   boolean needsRedraw3D, boolean isEditable, 
						   boolean isCopiable){
		
		super(name, value);
		this.needsRedraw2D = needsRedraw2D;
		this.needsRedraw3D = needsRedraw3D;
		this.isEditable = isEditable;
		this.isCopiable = isCopiable;
		
	} 
	
	/********************************
	 * Indicates whether a change to this attribute's value requires a 2D redraw of the shape.
	 * 
	 * @return
	 */
	public boolean needsRedraw2D(){
		return needsRedraw2D;
	}
	
	/********************************
	 * Sets whether a change to this attribute's value requires a 2D redraw of the shape.
	 * 
	 * @return
	 */
	public void needsRedraw2D(boolean b){
		this.needsRedraw2D = b;
	}
	
	/********************************
	 * Indicates whether a change to this attribute's value requires a 3D redraw of the shape.
	 * 
	 * @return
	 */
	public boolean needsRedraw3D(){
		return needsRedraw3D;
	}
	
	/********************************
	 * Sets whether a change to this attribute's value requires a 3D redraw of the shape.
	 * 
	 * @return
	 */
	public void needsRedraw3D(boolean b){
		this.needsRedraw3D = b;
	}
	
}