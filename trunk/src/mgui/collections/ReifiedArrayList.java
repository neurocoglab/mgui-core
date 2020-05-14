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

package mgui.collections;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;


/************************************************
 * Implements a version of {@link ArrayList} which is reified (meaning that its generic type is retrievable.
 * This list can be type-checked against other {@code ReifiedArrayList} instantiations.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 * @param <E>
 */

public class ReifiedArrayList<E> extends ArrayList<E> {

	Class<?> generic_type; 
	
	public ReifiedArrayList(){
		super();
		
		generic_type = (Class<?>)((ParameterizedType)this.getClass().getGenericSuperclass())
						.getActualTypeArguments()[0];
		int a = 0;
	}
	
	public ReifiedArrayList(ArrayList<E> list){
		super(list);
		
		generic_type = (Class<?>)((ParameterizedType)this.getClass().getGenericSuperclass())
						.getActualTypeArguments()[0];
		int a = 0;
	}
	
	public Class<?> getGenericType(){
		return generic_type;
	}
	
	public boolean isAssignableFrom(ReifiedArrayList<E> list){
		return generic_type.isAssignableFrom(list.getGenericType());
	}
	
}