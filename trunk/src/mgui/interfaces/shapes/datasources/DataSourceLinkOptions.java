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

package mgui.interfaces.shapes.datasources;

import mgui.datasources.LinkedDataStream;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.shapes.VertexDataColumn;

/**************************************************
 * Options for specifying a vertex data column link to a data source.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceLinkOptions extends InterfaceOptions {

	public VertexDataColumn column;
	public String name;
	public LinkedDataStream<?> linked_stream;
	
	public DataSourceLinkOptions(VertexDataColumn column){
		this(column, null);
	}
	
	public DataSourceLinkOptions(VertexDataColumn column, String name){
		this.column = column;
		this.name = name;
	}
	
}