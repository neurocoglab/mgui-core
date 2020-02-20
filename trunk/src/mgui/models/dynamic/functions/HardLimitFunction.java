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

package mgui.models.dynamic.functions;

import mgui.interfaces.attributes.Attribute;
import mgui.numbers.MguiDouble;

public class HardLimitFunction extends Function {

	//double highLimit = 1;
	//double lowLimit = -1;
	//double midLimit = 0;
	
	public HardLimitFunction (double low, double mid, double high){
		init();
		setHighLimit(high);
		setLowLimit(low);
		setMidLimit(mid);
	}
	
	@Override
	protected void init(){
		super.init();
		attributes.add(new Attribute("HighLimit", new MguiDouble(1)));
		attributes.add(new Attribute("MidLimit", new MguiDouble(0)));
		attributes.add(new Attribute("LowLimit", new MguiDouble(-1)));
	}
	
	public void setHighLimit(double v){
		attributes.setValue("HighLimit", new MguiDouble(v));
	}
	
	public void setMidLimit(double v){
		attributes.setValue("MidLimit", new MguiDouble(v));
	}
	
	public void setLowLimit(double v){
		attributes.setValue("LowLimit", new MguiDouble(v));
	}
	
	public double getHighLimit(){
		return ((MguiDouble)attributes.getValue("HighLimit")).getValue();
	}
	
	public double getMidLimit(){
		return ((MguiDouble)attributes.getValue("MidLimit")).getValue();
	}
	
	public double getLowLimit(){
		return ((MguiDouble)attributes.getValue("LowLimit")).getValue();
	}
	
	@Override
	public double[] evaluate(double[] input) {
		return null;
	}
	
	@Override
	public double evaluate(double input) {
		if (input >= getMidLimit()) return getHighLimit();
		return getLowLimit();
	}

}