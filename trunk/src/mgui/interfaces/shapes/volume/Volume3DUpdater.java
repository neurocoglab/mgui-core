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

package mgui.interfaces.shapes.volume;

import org.jogamp.java3d.ImageComponent3D;
import org.jogamp.java3d.ImageComponent3D.Updater;

/*******************************
 * Class implementing the ImageComponent3D.Updater interface, to allow modification
 * of volume data represented in a live Java3D node. 
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */
public abstract class Volume3DUpdater implements Updater {

	public abstract void updateData(ImageComponent3D imageComponent,
				           int index,
				           int x,
				           int y,
				           int width,
				           int height);
	
}