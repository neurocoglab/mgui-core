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

package mgui.interfaces.plots;

import java.awt.GridLayout;
import java.util.ArrayList;

import mgui.interfaces.attributes.Attribute;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;


/*************************************
 * Allows the display of multiple plots in a single InterfacePlot panel.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfacePlotMulti extends InterfacePlot<MguiNumber> {

	public ArrayList<InterfacePlot<?>> plots = new ArrayList<InterfacePlot<?>>(); 
	
	public InterfacePlotMulti(){
		init();
	}
	
	@Override
	protected void init(){
		super.init();
	
		attributes.add(new Attribute<MguiInteger>("Cols", new MguiInteger(1)));
		
		resetLayout();
	}
	
	@Override
	public InterfacePlotDialog getPlotDialog(){
		return null;
	}
	
	public int getCols(){
		return ((MguiInteger)attributes.getValue("Cols")).getInt();
	}
	
	public void setCols(int c){
		attributes.setValue("Cols", new MguiInteger(c));
		resetLayout();
	}
	
	protected void resetLayout(){
		setLayout(new GridLayout(0, getCols()));
	}
	
	public void addPlot(InterfacePlot<?> p){
		add(p);
	}
	
	public void removePlot(InterfacePlot<?> p){
		remove(p);
	}
	
	
}