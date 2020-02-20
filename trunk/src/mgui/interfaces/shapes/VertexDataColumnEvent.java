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

package mgui.interfaces.shapes;

import java.util.EventObject;

/***********************************************
 * Specifies an event on a vertex data column.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VertexDataColumnEvent extends EventObject {

	public enum EventType{
		ColumnAdded,
		ColumnRemoved,
		ColumnChanged,
		ColumnIsCurrent,
		ColourMapChanged,
		NameChanged;
	}
	
	public EventType type = EventType.ColumnChanged;
	
	public VertexDataColumnEvent(VertexDataColumn column, EventType type){
		super(column);
		this.type = type;
	}
	
}