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

package mgui.io.domestic.maps;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.io.InterfaceIOOptions;
import mgui.numbers.MguiFloat;

/************************************************************
 * Writes a comma-delimited continuous colour map. Each line has four values (all from [0,1]):
 * anchor position, red, green, blue.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ContinuousColourMapWriter extends mgui.io.FileWriter {

	public ContinuousColourMapWriter(){
		
	}
	
	public ContinuousColourMapWriter(File file){
		setFile(file);
	}
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar){
		return false;
	}

	public void writeMap(ContinuousColourMap map) throws IOException{
		writeMap(map, "0.0000####");
	}
	
	public void writeMap(ContinuousColourMap map, String pattern) throws IOException{
		
		if (dataFile == null)
			throw new IOException("No output file set for continuous colour map..");
		
		if (dataFile.exists() && !dataFile.delete())
			throw new IOException("Cannot remove existing output file for continuous colour map..");
		
		if (!dataFile.createNewFile())
			throw new IOException("Unable to create output file " + dataFile.getAbsolutePath() + " for continuous colour map..");
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile));
		
		for (int i = 0; i < map.anchors.size(); i++){
			ContinuousColourMap.Anchor anchor = map.anchors.get(i);
			bw.write(anchor.value.toString(pattern) + ",");
			bw.write(MguiFloat.getString(anchor.colour.getRed(), pattern) + ",");
			bw.write(MguiFloat.getString(anchor.colour.getGreen(), pattern) + ",");
			bw.write(MguiFloat.getString(anchor.colour.getBlue(), pattern) + ",");
			bw.write(MguiFloat.getString(anchor.colour.getAlpha(), pattern) + "\n");
			}	
		
		bw.close();
		
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = ContinuousColourMapWriter.class.getResource("/mgui/resources/icons/continuous_cmap_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/continuous_cmap_20.png");
		return null;
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(ContinuousColourMap.class);
		return objs;
	}

}