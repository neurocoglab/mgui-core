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

import mgui.geometry.util.SpatialUnit;

/********************************************************
 * Specifies a simple unit, scaled by a constant factor with respect to a base unit. All subclasses must
 * implement the <code>convert</code> method from the <code>{@link Unit}</code> interface. 
 * <code>SpatialUnit</code> specifies no setter functions; the unit should be set from the constructor 
 * and not changed.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ScaledUnit extends Unit {

	protected String name = "?";				//long name of unit (e.g., "kilometer")
	protected String short_name = "?";			//short name to use with numbers (e.g., "km")
	protected double conversion_factor = 1;		//factor which converts this unit to base unit (e.g., for km, 1000)
	
	public ScaledUnit(String long_name, String short_name, double factor){
		this.name = long_name;
		this.short_name = short_name;
		this.conversion_factor = factor;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public String getShortName(){
		return short_name;
	}
	
	public double getConversionToMeter(){
		return this.conversion_factor;
	}
	
	/**************************************
	 * Converts <code>value</code>, specified with <code>unit</code>, to its equivalent in this
	 * <code>Unit</code>.
	 * 
	 * @param unit 		the unit in which <code>value</code> is currently specified
	 * @param value		the value to convert
	 * @return			the equivalent of <code>value</code> in this unit
	 */
	@Override
	public double convert(Unit unit, double value) throws UnitConversionException{
		
		if (!(unit instanceof ScaledUnit))
			throw new UnitConversionException("Scaled unit can not be converted from an instance of " + unit.getClass().getName());
		
		SpatialUnit _unit = (SpatialUnit)unit;
		//conversion is value -> meter, meter -> this unit
		double new_value = _unit.getConversionToMeter() * value;
		return new_value / conversion_factor;
		
	}

}