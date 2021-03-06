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

package mgui.interfaces.shapes.util;

import java.util.ArrayList;
import java.util.EventObject;

import mgui.interfaces.shapes.InterfaceShape;

/**********************************************************
 * Event on an {@linkplain InterfaceShape} object.
 * 
 * The following events are defined by the {@code EventType} enum:
 * 
 * <p>
 * <ul>
 * <li>General
 * <li>ShapeAdded
 * <li>ShapeRemoved
 * <li>ShapeModified
 * <li>ShapeSetModified
 * <li>AttributeModified
 * <li>ShapeDestroyed
 * <li>ShapeInserted
 * <li>ShapeMoved
 * <li>TextureModified
 * <li>SectionAdded
 * <li>SectionRemoved
 * <li>VertexColumnAdded
 * <li>VertexColumnRemoved
 * <li>VertexColumnChanged
 * <li>VertexColumnRenamed
 * <li>ClipModified
 * </ul>
 * 
 * @version 1.0
 * @since 1.0
 * @author Andrew Reid
 *
 */
public class ShapeEvent extends EventObject {


	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 7383217668410306626L;
	
	boolean is_consumed;		//indicates that some process has already consumed this event
	boolean modifies_shape_set = false;
	
	public ArrayList<ShapeListener> already_responded = new ArrayList<ShapeListener>();
	
	public static enum EventType{
		General,
		ShapeAdded,
		ShapeRemoved,
		ShapeModified,
		ShapeSetModified,
		AttributeModified,
		ShapeDestroyed,
		ShapeInserted,
		ShapeMoved,
		TextureModified,
		SectionAdded,
		SectionRemoved,
		VertexColumnAdded,
		VertexColumnRemoved,
		VertexColumnChanged,
		VertexColumnRenamed,
		ClipModified;
	}
	
	public EventType eventType = EventType.General; 
	
	public ShapeEvent(InterfaceShape thisShape, EventType type){
		super(thisShape);
		eventType = type;
	}
	
	public ShapeEvent(InterfaceShape thisShape, EventType type, boolean modifies_shape_set){
		super(thisShape);
		eventType = type;
		this.modifies_shape_set = modifies_shape_set;
	}
	
	public void setShape(InterfaceShape shape){
		this.source = shape;
	}
	
	public InterfaceShape getShape(){
		return (InterfaceShape)this.getSource();
	}
	
	public void setModifiesShapeSet(boolean b){
		modifies_shape_set = b;
	}
	
	public boolean modifiesShapeSet(){
		return modifies_shape_set;
	}
	
	public void responded(ShapeListener listener){
		already_responded.add(listener);
	}
	
	public boolean alreadyResponded(ShapeListener listener){
		for (int i = 0; i < already_responded.size(); i++)
			if (already_responded.get(i).equals(listener)) return true;
		return false;
	}
	
	public void consume(){
		is_consumed = true;
	}
	
	public boolean isConsumed(){
		return is_consumed;
	}
	
}