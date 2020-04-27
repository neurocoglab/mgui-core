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

package mgui.interfaces.shapes.volume;

import org.jogamp.java3d.ImageComponent3D;

import mgui.geometry.Grid3D;
import mgui.geometry.volume.VolumeFunctions;


public class VolumeMaskUpdater extends Volume3DUpdater {

	public VolumeMaskOptions_old params;
	public Grid3D grid;
	
	public VolumeMaskUpdater(VolumeMaskOptions_old o){
		setParams(o);
	}
	
	public VolumeMaskUpdater(VolumeMaskOptions_old o, Grid3D g){
		setParams(o);
		setGrid(g);
	}
	
	@Override
	public void updateData(ImageComponent3D imageComponent,
				           int index,
				           int x,
				           int y,
				           int width,
				           int height){
		
		if (params == null) return;
		VolumeFunctions.applyMask(imageComponent, index, x, y, width, height, params);
		
		}
	
	public void setGrid(Grid3D g){
		grid = g;
	}
	
	public void setParams(VolumeMaskOptions_old p){
		params = p;
	}
	
}