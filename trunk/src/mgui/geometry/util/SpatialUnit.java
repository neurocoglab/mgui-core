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

package mgui.geometry.util;

import mgui.util.ScaledUnit;
import mgui.util.Unit;
import mgui.util.UnitConversionException;

/********************************************************
 * Specifies a spatial unit, with respect to the default Java3D unit (meter). All subclasses must
 * implement the <code>convert</code> method from the <code>{@link Unit}</code> interface. 
 * <code>SpatialUnit</code> specifies no setter functions; the unit should be set from the constructor 
 * and not changed.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SpatialUnit extends ScaledUnit {

	public SpatialUnit(String long_name, String short_name, double factor){
		super(long_name, short_name, factor);
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
		
		if (!(unit instanceof SpatialUnit))
			throw new UnitConversionException("Spatial unit can not be converted from an instance of " + unit.getClass().getName());
		
		return super.convert(unit, value); 
		
	}
	
}