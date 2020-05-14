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

package mgui.interfaces.maps;

import java.util.ArrayList;
import java.util.Collections;

/***********
 * Maps an integer key value with an assoicated object
 * @author Andrew Reid
 *
 */

public class ValueMap {

	public ArrayList<ValueMapItem> items = new ArrayList<ValueMapItem>();
	private ValueItemComparator itemComparator = new ValueItemComparator();

	public ValueMap(){
		
	}
	
	public boolean addItem(ValueMapItem thisItem){
		if (Collections.binarySearch(items, thisItem, itemComparator) >= 0)
			return false;
		items.add(thisItem);
		Collections.sort(items, itemComparator);
		return true;
	}
	
	public boolean addItem(int keyVal, Object objVal){
		return addItem(new ValueMapItem(keyVal, objVal));
	}
	
	public boolean removeItem(int keyVal){
		int index = Collections.binarySearch(items, new ValueMapItem(keyVal), itemComparator);
		if (index < 0)
			return false;
		items.remove(index);
		return true;
	}
	
	public Object getValue(int keyVal){
		int index = Collections.binarySearch(items, new ValueMapItem(keyVal), itemComparator);
		if (index < 0)
			return null;
		return items.get(index).objValue;
	}
	
	public ValueMapItem getItem(int keyVal){
		int index = Collections.binarySearch(items, new ValueMapItem(keyVal), itemComparator);
		if (index < 0)
			return null;
		return items.get(index);
	}

	/***************************
	 * Find item in map whose value is o. Returns Integer.MIN_VALUE if not found
	 * @param o
	 * @return
	 */
	public ValueMapItem findItem(Object o){
		if (o == null) return null;
		for (int i = 0; i < items.size(); i++)
			if (o.equals(items.get(i).objValue)) return items.get(i);
		return null;
	}
	
	public int getSize(){
		return items.size();
	}
	
}