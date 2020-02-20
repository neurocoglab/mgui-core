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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import mgui.interfaces.ProgressUpdater;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiNumber;
import foxtrot.Job;
import foxtrot.Worker;


public class PointSet3DDataLoader extends FileLoader {

	public PointSet3DDataLoader(){
		
	}
	
	public PointSet3DDataLoader(File file){
		setFile(file);
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadValues(progress_bar);
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		boolean success = true;
		if (!(options instanceof PointSet3DDataInputOptions)) return false;
		PointSet3DDataInputOptions opts = (PointSet3DDataInputOptions)options;
		if (opts.pointset == null) return false;
		for (int i = 0; i < opts.files.length; i++){
			setFile(opts.files[i]);
			ArrayList<ArrayList<MguiNumber>> values = null;
			try{
				values = loadValues(progress_bar);
			}catch (IOException e){
				e.printStackTrace();
				}
			if (values == null)
				success = false;
			else{
				for (int j = 0; j < values.size(); j++)
					opts.pointset.addVertexData(opts.names[i], values.get(j));
				}
			}
		opts.pointset.fireShapeModified();
		return success;
	}
	
	

	public ArrayList<ArrayList<MguiNumber>> loadValues(final ProgressUpdater progress_bar) throws IOException{
		
		ArrayList<ArrayList<MguiNumber>> data = null;
		
		if (progress_bar != null){
			data = (ArrayList<ArrayList<MguiNumber>>)Worker.post(new Job(){
				@Override
				public ArrayList<ArrayList<MguiNumber>> run(){
					try{
						return loadValuesBlocking(progress_bar);
					}catch (IOException e){
						e.printStackTrace();
						return null;
						}
				}
			});
		}else{
			data = loadValuesBlocking(progress_bar);
			}
		
		return data;
		
	}
	
	protected ArrayList<ArrayList<MguiNumber>> loadValuesBlocking(ProgressUpdater progress_bar) throws IOException{
		
		if (dataFile == null || !dataFile.exists())
			throw new IOException("PointSet3DDataLoader: Cannot find input file '" + dataFile + "'.");
		
		ArrayList<ArrayList<MguiNumber>> data = new ArrayList<ArrayList<MguiNumber>>();
		
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String line = reader.readLine();
		
		if (line == null) return null;
		StringTokenizer tokens = new StringTokenizer(line);
		
		for (int i = 0; i < tokens.countTokens(); i++)
			data.add(new ArrayList<MguiNumber>());
		
		while (line != null){
			tokens = new StringTokenizer(line);
			int i = 0;
			while (tokens.hasMoreTokens())
				data.get(i++).add(new MguiDouble(tokens.nextToken()));
			line = reader.readLine();
			}
		
		reader.close();
		
		return data;
	}
	
}