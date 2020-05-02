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

package mgui.interfaces.io;

import java.util.EventObject;

/*************************************************
 * Event on an {@link InterfaceDataSource} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DataSourceEvent extends EventObject {

	protected int size;
	
	public DataSourceEvent(InterfaceDataSource<?> source){
		super(source);
		size = 1;
	}
	
	public DataSourceEvent(InterfaceDataSource<?> source, int size){
		super(source);
		this.size = size;
	}
	
	public InterfaceDataSource<?> getDataSource(){
		return (InterfaceDataSource<?>)getSource();
	}
	
	public int getSize(){
		return size;
	}
	
}