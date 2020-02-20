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

import mgui.interfaces.shapes.Volume3DInt;

/**************************************************
 * 
 * Renders dynamic volumes; i.e., blend a pixel's colour values from values obtained
 * from 1.) a sample from the time series of a 4D volume and 2.) the base volume. The renderer
 * creates a Texture3D with the appropriate ColorModel, based upon the models of both the
 * base and dynamic volumes. E.g.,   
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class Volume4DRenderer extends Volume3DRenderer {

	public Volume4DRenderer(Volume3DInt volume){
		super(volume);
	}
	
	
}