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

package mgui.models.updaters;

import mgui.interfaces.attributes.Attribute;
import mgui.models.dynamic.DynamicModelEnvironment;
import mgui.numbers.MguiDouble;

public class LinearUpdater extends SimpleEnvironmentUpdater {

	//public double offset = 0.0;
	//public double factor = 1.0;
	//public double step = 0.0;
	
	public LinearUpdater(){
		init();
	}
	
	public LinearUpdater(double offset, double factor){
		init();
		setOffset(offset);
		setFactor(factor);
	}
	
	@Override
	protected void init(){
		super.init();
		attributes.add(new Attribute<MguiDouble>("Offset", new MguiDouble(0)));
		attributes.add(new Attribute<MguiDouble>("Factor", new MguiDouble(1)));
		attributes.add(new Attribute<MguiDouble>("Step", new MguiDouble(0)));
	}
	
	public void reset(){
		
	}
	
	public void setOffset(double offset){
		attributes.setValue("Offset", new MguiDouble(offset));
	}
	
	public void setFactor(double factor){
		attributes.setValue("Factor", new MguiDouble(factor));
	}
	
	public void setStep(double step){
		attributes.setValue("Step", new MguiDouble(step));
	}
	
	public double getOffset(){
		return ((MguiDouble)attributes.getValue("Offset")).getValue();
	}
	
	public double getFactor(){
		return ((MguiDouble)attributes.getValue("Factor")).getValue();
	}
	
	public double getStep(){
		return ((MguiDouble)attributes.getValue("Step")).getValue();
	}
	
	@Override
	protected boolean doUpdate(DynamicModelEnvironment<?> c, double timeStep){
		if (getStep() <= 0) return true;
		double[] state = c.getInputState();
		double time = timeStep / getStep();
		for (int i = 0; i < state.length; i++){
			state[i] *= getFactor() * time;
			state[i] += getOffset() * time;
			}
		return true;
	}
	
}