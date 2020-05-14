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
import java.util.HashMap;
import java.util.Iterator;

/******************************
 * Holds a static list of constants representing distinct types, and an integer field 
 * indicating the type of the present instance.
 *  
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class TypeMap {

	protected int type;
	protected HashMap<Integer, String> types = new HashMap<Integer, String>();
	
	public void addType(int value, String typeStr) throws TypeExistsException{
		if (types.get(value) != null)
			throw new TypeExistsException(value);
		types.put(value, typeStr);
	}
	
	public void setType(int value, String typeStr){
		types.put(value, typeStr);
		
	}
	
	public int getType(){
		return type;
	}
	
	public ArrayList<String> getTypes(){
		if (types == null) return new ArrayList<String>();
		return new ArrayList<String>(types.values());
	}
	
	public void setTypes(HashMap<Integer, String> types){
		this.types = types;
	}
	
	public void setType(int t){
		type = t;
	}
	
	public void setType(String typeStr){ 
		String s = typeStr;
		if (s.contains("("))
			s = s.substring(0, s.indexOf("("));
		Iterator<Integer> itr = types.keySet().iterator();
		
		while (itr.hasNext()){
			int i = itr.next();
			if (types.get(i).equals(s))
				type = i;
		}
		
	}
	
	public String getTypeStr(){
		return types.get(type);
	}
	
	public String toString(){
		return types.get(type);
	}
	
}