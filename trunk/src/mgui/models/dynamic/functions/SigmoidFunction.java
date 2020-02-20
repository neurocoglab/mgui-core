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

/*************************
 * As the name suggests... :P
 * Evaluates:
 * <p><code>y = (1 - e^-kx) / (1 + e^-kx)</code></P>
 * 
 * where k is a constant determining the shape, and <code>evaluate(0) = 0</code>
 * 
 * @author Andrew Reid
 *
 */
public class SigmoidFunction extends Function {

	public SigmoidFunction(){
		init();
	}
	
	public SigmoidFunction(double k){
		init();
		setK(k);
	}
	
	@Override
	protected void init(){
		super.init();
		attributes.add(new Attribute<MguiDouble>("K", new MguiDouble(1)));
	}
	
	public double getK(){
		return ((MguiDouble)attributes.getValue("K")).getValue();
	}
	
	public void setK(double k){
		attributes.setValue("K", new MguiDouble(k));
	}
	
	@Override
	public double evaluate(double d) {
		return (1 - Math.exp(-d * getK())) / (1 + Math.exp(-d * getK()));
	}

	@Override
	public double[] evaluate(double[] d) {
		double[] result = new double[d.length]; 
		for (int i = 0; i < d.length; i++)
			result[i] = evaluate(d[i]);
		return result;
	}
	
	@Override
	public String toString(){
		return "Sigmoid";
	}
	
}