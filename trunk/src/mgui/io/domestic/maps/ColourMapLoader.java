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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.maps.ColourMap;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;

/***************************************
 * Base class for loading a colour map from file. Determines whether map is continuous or
 * discrete and loads it with the corresponding loader. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class ColourMapLoader extends FileLoader {

	public ColourMapLoader(){
		
	}
	
	public ColourMapLoader(File file){
		setFile(file);
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadMap(options);
	}
	
	public ColourMap loadMap() throws IOException{
		return loadMap(null);
	}
	
	public ColourMap loadMap(InterfaceIOOptions options) throws IOException{

		if (dataFile == null) return null;
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));

			String line = reader.readLine();
			
			//open xml
			if (line.startsWith("<?xml")){
				while (line != null && !line.startsWith("<ColourMap"))
					line = reader.readLine();
				if (line == null){
					InterfaceSession.log("ColourMapLoader: error reading colour map file '" + dataFile.getAbsolutePath() +"'.");
					return null;
					}
				if (line.contains("discrete")){
					DiscreteColourMapLoader loader = new DiscreteColourMapLoader(dataFile);
					return loader.loadMap();
					}
				if (line.contains("continuous")){
					ContinuousColourMapLoader loader = new ContinuousColourMapLoader(dataFile);
					return loader.loadMap();
					}
				}
			
			//open continuous old format
			//TODO convert all to xml
			ContinuousColourMapLoader loader = new ContinuousColourMapLoader(dataFile);
			return loader.loadMap();
			
		}catch (IOException e){
			e.printStackTrace();
			return null;
			}
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(ColourMap.class);
		return objs;
	}
	
}