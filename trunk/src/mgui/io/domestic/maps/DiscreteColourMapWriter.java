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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.maps.ContinuousColourMap;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;
import mgui.util.Colour;

/********************************************************************
 * Writer for discrete colour maps. Writes as comma-delimited ASCII.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class DiscreteColourMapWriter extends FileWriter {

	public DiscreteColourMapWriter(){
		
	}
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		DiscreteColourMapOutOptions _options = (DiscreteColourMapOutOptions)options;
		if (_options.map == null) return false;
		if (_options.getFiles() == null ||
			_options.getFiles().length == 0) return false;
		
		setFile(_options.getFiles()[0]);
		return writeMap(_options.map, _options.write_alpha);
	}
	
	public boolean writeMap(DiscreteColourMap map){
		return writeMap(map, true);
	}

	public boolean writeMap(DiscreteColourMap map, boolean alpha){
		
		if (dataFile == null) return false;
		//Iterator<Integer> itr = map.colours.keySet().iterator();
		
		try{
			BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
			
			ArrayList<Integer> indexes = new ArrayList<Integer>(map.colours.keySet());
			Collections.sort(indexes);
			
			for (int i = 0; i < indexes.size(); i++){
				int index = indexes.get(i);
				Colour c = map.colours.get(index);
				
				writer.write(index + "," + c.getRed() + "," + c.getGreen() + "," + c.getBlue());
				if (alpha)
					writer.write("," + c.getAlpha() + "\n");
				else
					writer.write("\n");
				}
			
			writer.close();
			return true;
			
		}catch (IOException e){
			e.printStackTrace();
			return false;
			}
		
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = DiscreteColourMapWriter.class.getResource("/mgui/resources/icons/discrete_cmap_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/discrete_cmap_20.png");
		return null;
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(DiscreteColourMap.class);
		return objs;
	}
}