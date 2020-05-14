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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import mgui.interfaces.ProgressUpdater;
import mgui.numbers.MguiNumber;

public class VectorDataLoader extends ShapeDataLoader {

	public VectorDataLoader(){
	}
	
	public VectorDataLoader(File file){
		setFile(file);
	}
	
	@Override
	public HashMap<String, ArrayList<MguiNumber>> loadDataBlocking(ShapeDataInputOptions options, ProgressUpdater progress_bar){
	
		//load each column into a vector
		int row = 0;
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			String line = reader.readLine();
			ShapeDataInputOptions.Format format = options.formats[0][0];
			
			ArrayList<MguiNumber> data = new ArrayList<MguiNumber>();
			
			while (line != null){
				StringTokenizer tokens = new StringTokenizer(line);
				
				while (tokens.hasMoreTokens())
					data.add(getValue(tokens.nextToken(), format));
				
				row++;
				if (progress_bar != null)
					progress_bar.update(row);
				}
			
			reader.close();
			
			HashMap<String, ArrayList<MguiNumber>> map = new HashMap<String, ArrayList<MguiNumber>>();
			map.put(options.names[0], data);
			
			return map;
			
		}catch (IOException e){
			e.printStackTrace();
			return null;
		}
	}
	
	
}