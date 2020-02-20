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

/********************************************************
 * Specifies an abstract unit, with a conversion factor to some base unit.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class Unit implements Comparable<Unit>{

	public abstract String getName();
	public abstract String getShortName();
	
	/**************************************
	 * Converts <code>value</code>, specified with <code>unit</code>, to its equivalent in this
	 * <code>Unit</code>.
	 * 
	 * @param unit 		the unit in which <code>value</code> is currently specified
	 * @param value		the value to convert
	 * @return			the equivalent of <code>value</code> in this unit
	 */
	public abstract double convert(Unit unit, double value) throws UnitConversionException;
	
	public int compareTo(Unit unit){
		return getName().compareTo(unit.getName());
	}
	
	@Override
	public String toString(){
		return getName() + " (" + getShortName() + ")";
	}
	
}