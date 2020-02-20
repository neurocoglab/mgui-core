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

package mgui.io.domestic.shapes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.volume.VolumeMetadata;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;

/*************************************************************
 * Writes a <code>Volume3DInt</code> to file. All volume writers should extend this class. This class
 * also provides default implementations for writing volumes to the domestic standard, NIFTI format, using the
 * jniftilib library. See <a href="http://www.nitrc.org/frs/?group_id=26">NITRIC</a> for details.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class VolumeFileWriter extends FileWriter {

	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar){
		
		VolumeOutputOptions _options = (VolumeOutputOptions)options;
		
		File[] files = _options.getFiles();
		
		if (files == null) return false;
		boolean success = true;
		
		for (int i = 0; i < files.length; i++){
			setFile(files[i]);
			Volume3DInt volume = _options.volumes[i];
			
			try{
				success &= writeVolume(volume, _options);
			}catch (IOException e){
				InterfaceSession.log("VolumeFileWriter: IOException writing to '" + files[i].getAbsolutePath() + "'." +
									 "\nDetails: " + e.getMessage(), 
									 LoggingType.Errors);
				success = false;
				}
			}
		
		return success;
	}
	
	/********************************************
	 * Writes <code>volume</code> to file using default parameters. 
	 * 
	 * @param volume
	 * @param options
	 * @return
	 * @throws IOException
	 */
	public boolean writeVolume(Volume3DInt volume) throws IOException{

		VolumeOutputOptions options = new VolumeOutputOptions();
		return writeVolume(volume, options);
	
	}
	
	/********************************************
	 * Writes a volume to file using the specified options.
	 * 
	 * @param volume
	 * @param options
	 * @return
	 * @throws IOException
	 */
	public abstract boolean writeVolume(Volume3DInt volume, VolumeOutputOptions options) throws IOException;
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = VolumeFileWriter.class.getResource("/mgui/resources/icons/volume_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/volume_3d_20.png");
		return null;
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(Volume3DInt.class);
		return objs;
	}
	
	/***********************************************
	 * Sets the metadata for this volume.
	 * 
	 * @param metadata
	 */
	public abstract void setVolumeMetadata(VolumeMetadata metadata);
	
}