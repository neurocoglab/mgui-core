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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import foxtrot.Job;
import foxtrot.Worker;

/**********************************************************
 * Loads vertex-wise data from tabular text files into shapes. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeDataLoader extends FileLoader {

	protected ShapeDataInputOptions options;
	protected int f_pos;
	
	public ShapeDataLoader(){
		
	}
	
	public ShapeDataLoader(File file){
		setFile(file);
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		ShapeDataInputOptions _options = (ShapeDataInputOptions)options;
		
		File[] files = _options.getFiles();
		InterfaceShape shape = _options.shape;
		boolean success = true;
		f_pos = 0;
		
		for (int i = 0; i < files.length; i++){
			setFile(files[i]);
			HashMap<String, ArrayList<MguiNumber>> data = loadData(_options, progress_bar);
			
			if (data != null){
				f_pos++; // += data.size();
				Iterator<String> itr = data.keySet().iterator();
				while (itr.hasNext()){
					String key = itr.next();
					if (data.get(key).size() != shape.getVertexCount()){
						InterfaceSession.log("Value count " + data.get(key).size() + " not equal to vertex count " + shape.getVertexCount() +
								" in file " + files[i].getAbsolutePath(), 
								LoggingType.Errors);
						success = false;
					}else{
						shape.addVertexData(key, data.get(key));
						}
					}
			}else{
				success = false;
				}
			}
		
		return success;
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadData(progress_bar);
	}
	
	protected int getCurrentLineCount(){
		if (dataFile == null) return -1;
		try{
			BufferedReader br = new BufferedReader(new FileReader(dataFile));
			int count = 0;
			String line = br.readLine();
			while (line != null){
				line = br.readLine();
				count++;
				}
			br.close();
			return count;
				
		}catch (IOException e){
			e.printStackTrace();
			return -1;
			}
	}
	
	public HashMap<String, ArrayList<MguiNumber>> loadData(ProgressUpdater progress_bar){
		return loadData(options, progress_bar);
	}
	
	public HashMap<String, ArrayList<MguiNumber>> loadData(final ShapeDataInputOptions options, final ProgressUpdater progress_bar){
		
		if (progress_bar == null)
			return loadDataBlocking(options, null);
		
		int size = getCurrentLineCount();
		progress_bar.setMinimum(0);
		progress_bar.setMaximum(size);
		
		return (HashMap<String, ArrayList<MguiNumber>>)Worker.post(new Job(){
			@Override
			public HashMap<String, ArrayList<MguiNumber>> run(){
				return loadDataBlocking(options, progress_bar);
			}
		});
	}
	
	public HashMap<String, ArrayList<MguiNumber>> loadDataBlocking(ShapeDataInputOptions options, ProgressUpdater progress_bar){
		
		if (options == null || options.columns == null) return null;
		
		int row = 0;
		
		try{
			BufferedReader reader = new BufferedReader(new FileReader(dataFile));
			HashMap<String, ArrayList<MguiNumber>> map = new HashMap<String, ArrayList<MguiNumber>>();
			ArrayList<String> names = new ArrayList<String>();
			ArrayList<ArrayList<MguiNumber>> data = new ArrayList<ArrayList<MguiNumber>>();
			String line = reader.readLine();
			row++;
			if (line == null) return map;
			if (options.skip_header){
				line = reader.readLine();
				row++;
				}
			
			String[] header = options.columns[f_pos];
			boolean[] load = options.load_column[f_pos];
			int cols = header.length;
			ShapeDataInputOptions.Format[] formats = options.formats[f_pos];
			
			for (int i = 0; i < cols; i++)
				if (load[i]){
					names.add(header[i]);
					data.add(new ArrayList<MguiNumber>());
					}
					//map.put(header[i], new ArrayList<arNumber>());
			
			while (line != null){
				StringTokenizer tokens = new StringTokenizer(line);
				if (options.as_one_column){
					while (tokens.hasMoreTokens())
						data.get(0).add(getValue(tokens.nextToken(), formats[0]));
				}else{
					for (int i = 0; i < cols; i++){
						String token = tokens.nextToken();
						data.get(i).add(getValue(token, formats[i]));
						//map.get(header[i]).add(getValue(token, formats[i]));
						}
					}
				
				line = reader.readLine();
				row++;
				if (progress_bar != null)
					progress_bar.update(row);
				}
			
			reader.close();
			
			for (int i = 0; i < names.size(); i++)
				map.put(names.get(i), data.get(i));
			
			return map;
			
		}catch (IOException e){
			InterfaceSession.log("ShapeDataLoader: IOException loading file '" + dataFile.getAbsolutePath() + 
								 "'.\nDetails: " + e.getMessage(), 
								 LoggingType.Errors);
			return null;
		}catch (NoSuchElementException e){
			InterfaceSession.log("ShapeDataLoader: Not enough elements in line " + row + "..");
			return null;
		}
		
	}
	
	protected MguiNumber getValue(String s, ShapeDataInputOptions.Format format){
		
		switch (format){
			case Double:
				return new MguiDouble(s);
			case Float:
				return new MguiFloat(s);
			case Integer:
			case Short:
				return new MguiInteger(s);
			case Boolean:
				return new MguiBoolean(s);
		}
		
		return null;
		
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = ShapeDataLoader.class.getResource("/mgui/resources/icons/vector_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/vector_20.png");
		return null;
	}
	

}