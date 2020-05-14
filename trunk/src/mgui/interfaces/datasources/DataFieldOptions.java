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

package mgui.interfaces.datasources;

import mgui.datasources.DataField;
import mgui.datasources.DataTable;
import mgui.interfaces.InterfaceOptions;

/**************************************************
 * Parameters defining a data field.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class DataFieldOptions extends InterfaceOptions{

	public DataTable parent_table;
	public DataField data_field;
	
	public DataFieldOptions(DataTable parent_table){
		this.parent_table = parent_table;
	}
	
}