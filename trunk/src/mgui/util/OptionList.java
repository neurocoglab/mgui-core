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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComboBox;

public class OptionList {

	public HashMap<String, Object> options = new HashMap<String, Object>();
	public String current;
	
	public void setCombo(JComboBox combo){
		if (combo == null) return;
		combo.removeAllItems();
		Set set = options.keySet();
		Iterator itr = set.iterator();
		while (itr.hasNext())
			combo.addItem(itr.next());
	}
	
	public void add(String str, Object obj){
		options.put(str, obj);
	}
	
	public void remove(String str){
		options.remove(str);
	}
	
	public Object get(String str){
		return options.get(str);
	}
	
	public void setCurrent(String s){
		current = s;
	}
	
	public Object getCurrent(){
		return options.get(current);
	}
}