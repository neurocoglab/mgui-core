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

package mgui.util;

import java.util.List;

public class arCollections {

	/*********************************
	 * Modified binary search works around the generics crap.
	 * @param <T>
	 * @param list
	 * @param key
	 * @return
	 */
	public static <T> int binarySearch( List<? extends T> list, Comparable<? super T> key) {
		
		int low = 0;
		int high = list.size()-1;
	
		while (low <= high) {
		    int mid = (low + high) >>> 1;
		    T midVal = list.get(mid);
		    int cmp = key.compareTo(midVal);
	
		    if (cmp < 0)
			low = mid + 1;
		    else if (cmp > 0)
			high = mid - 1;
		    else
			return mid; // key found
		}
		
		return -(low + 1);  // key not found
		
	}
	
}