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

package mgui.io.domestic.maps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.io.InterfaceIOOptions;
import mgui.numbers.MguiDouble;
import mgui.util.Colour4f;

/************************************************************
 * Loads a comma-delimited continuous colour map. Each line has four values (all from [0,1]):
 * anchor position, red, green, blue.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ContinuousColourMapLoader extends ColourMapLoader {

	public ContinuousColourMapLoader(){
		
	}
	
	public ContinuousColourMapLoader(File file){
		setFile(file);
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		return false;
	}
	
	@Override
	public ContinuousColourMap loadMap() throws IOException{
		return loadMap((ProgressUpdater)null);
	}

	public ContinuousColourMap loadMap(ProgressUpdater progress_bar) throws IOException{
		
		if (dataFile == null)
			throw new IOException("No input file set for continuous colour map..");
		
		if (!dataFile.exists())
			throw new IOException("Input file " + dataFile.getAbsolutePath() + " can't be found for continuous colour map..");
		
		BufferedReader br = new BufferedReader(new FileReader(dataFile)); 
		String name = dataFile.getName();
		if (name.contains("."))
			name = name.substring(0, name.indexOf("."));
		ContinuousColourMap map = new ContinuousColourMap(name);
		String input = br.readLine();
		
		while (input != null){
			MguiDouble val = new MguiDouble();
			Colour4f c = new Colour4f();
			StringTokenizer tokens = new StringTokenizer(input, ",");
			
			if (tokens.countTokens() >= 4){
				val.setValue(Double.valueOf(tokens.nextToken()));
				c.setRed(Float.valueOf(tokens.nextToken()));
				c.setGreen(Float.valueOf(tokens.nextToken()));
				c.setBlue(Float.valueOf(tokens.nextToken()));
				if (tokens.hasMoreTokens())
					c.setAlpha(Float.valueOf(tokens.nextToken()));
				else
					c.setAlpha(1f);
				map.addAnchor(val, c);
			}else{
				//throw exception?
				InterfaceSession.log("Invalid entry for continuous colour map: " + input);
				}
			input = br.readLine();
			}
		
		br.close();
	
		return map;
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = ContinuousColourMapLoader.class.getResource("/mgui/resources/icons/continuous_cmap_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/continuous_cmap_20.png");
		return null;
	}
	
}