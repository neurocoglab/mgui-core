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

package mgui.interfaces.maps;

import java.util.Comparator;

public class ValueItemComparator implements Comparator {

	public int compare(Object obj1, Object obj2){
		if (obj1 instanceof ValueMapItem && obj2 instanceof ValueMapItem){
			if (((ValueMapItem)obj1).keyValue < ((ValueMapItem)obj2).keyValue)
				return -1;
			if (((ValueMapItem)obj1).keyValue == ((ValueMapItem)obj2).keyValue)
				return 0;
			return 1;
		}
		
		throw new ClassCastException();
	}
	
	
}