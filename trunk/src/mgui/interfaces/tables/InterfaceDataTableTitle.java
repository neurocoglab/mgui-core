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

package mgui.interfaces.tables;

import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic3D;
import mgui.interfaces.graphics.InterfaceGraphicWindow;

public class InterfaceDataTableTitle extends InterfaceGraphicWindow {

	public InterfaceDataTableTitle(){
		super(new InterfaceDataTable());
	}
	
	public InterfaceDataTableTitle(InterfaceDataTable t){
		super(t);
	}
	
	public InterfaceDataTable getTable(){
		return (InterfaceDataTable)panel;
	}
	
	public void setPanel3D(InterfaceGraphic3D p){
		setPanel(p);
	}
	
	public static InterfaceGraphicWindow getInstance(){
		return new InterfaceDataTableTitle();
	}
	
	public static InterfaceGraphicWindow getInstance(InterfaceGraphic g){
		if (!(g instanceof InterfaceDataTable)) return getInstance();
		return new InterfaceDataTableTitle((InterfaceDataTable)g);
	}
	
	@Override
	public String toString(){
		return "Data Table Panel: " + getPanel().getName();
	}
	
}