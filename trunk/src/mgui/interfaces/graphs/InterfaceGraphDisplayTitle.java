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

package mgui.interfaces.graphs;

import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic3D;
import mgui.interfaces.graphics.InterfaceGraphicWindow;

public class InterfaceGraphDisplayTitle extends InterfaceGraphicWindow {

	public InterfaceGraphDisplayTitle(){
		super(new InterfaceGraphDisplay());
	}
	
	public InterfaceGraphDisplayTitle(InterfaceGraphDisplay t){
		super(t);
	}
	
	public InterfaceGraphDisplay getDisplay(){
		return (InterfaceGraphDisplay)panel;
	}
	
	public void setPanel3D(InterfaceGraphic3D p){
		setPanel(p);
	}
	
	public static InterfaceGraphicWindow getInstance(){
		return new InterfaceGraphDisplayTitle();
	}
	
	public static InterfaceGraphicWindow getInstance(InterfaceGraphic g){
		if (!(g instanceof InterfaceGraphDisplay)) return getInstance();
		return new InterfaceGraphDisplayTitle((InterfaceGraphDisplay)g);
	}
	
	@Override
	public void setName(String thisName){
		super.setName(thisName);
		title_button.setText("Graph Window: " + thisName);
	}
	
	@Override
	public String toString(){
		return "Graph Display Panel: " + getPanel().getName();
	}
	
	
	
}