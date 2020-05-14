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

package mgui.io.domestic.shapes;

/**************************************
 * Extracts volume data from a vol file (Sun Microsystems). Adapted from Sun's
 * VolFile class.
 * 
 * @author Andrew Reid
 *
 */

import java.io.IOException;

import mgui.geometry.Grid3D;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.io.DataFileInterface;
import mgui.io.foreign.vol.VolFileLoader;


public class VolumeFileInterface extends DataFileInterface {

	public VolumeFileInterface(){
		
	}
	
	//return a Volume3DInt object obtained from the input file
	public Volume3DInt getVolume3DInt(Volume3DInt volume) throws IOException{
		
		if (inputFile == null) return null;
		if (!inputFile.exists()) 
			throw new IOException("Volume file '" + inputFile.getName() + "' does not exist...");
		
		try{
		//get file data
			VolFileLoader vol;
			if (volume == null){
				 return null;
			}else{
				vol = new VolFileLoader(inputFile.toURI().toURL());
				vol.setVolume3D(volume, null);
				}
		return volume;
		
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return null;
		
	}
	
	
	
}