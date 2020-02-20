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

package mgui.morph.sections;

import java.util.Comparator;

public class MapWeightComparator implements Comparator {

	public static final int SORT_ASC = 1;
	public static final int SORT_DSC = -1;
	
	public int sortBy;
	
	public MapWeightComparator(){
		sortBy = SORT_ASC;
	}
	
	public MapWeightComparator(int sort){
		sortBy = sort;
		if (sortBy != SORT_DSC)
			sortBy = SORT_ASC;
	}
	
	public int compare(Object obj1, Object obj2) {
		
		/**@TODO have this throw a ClassCastException **/
		if (obj1 instanceof MapWeight && obj2 instanceof MapWeight){
			if (((MapWeight)obj1).weight > ((MapWeight)obj2).weight) return 1 * sortBy;
			if (((MapWeight)obj1).weight == ((MapWeight)obj2).weight) return 0;
			return -1 * sortBy;
		}
		
		return 0;
	}

}