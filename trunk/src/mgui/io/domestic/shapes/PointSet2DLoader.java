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
import java.util.StringTokenizer;

import mgui.geometry.PointSet2D;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.PointSet2DInt;
import mgui.io.InterfaceIOOptions;
import foxtrot.Job;
import foxtrot.Worker;

/*************************************************************
 * Loader class for {@linkplain PointSet2DInt} objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PointSet2DLoader extends InterfaceShapeLoader {

	public PointSet2DLoader(){
		
	}
	
	public PointSet2DLoader(File file){
		setFile(file);
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar) {
		PointSet2DInputOptions _options = (PointSet2DInputOptions)options;
		boolean success = true;
		
		for (int i = 0; i < _options.getFiles().length; i++){
			setFile(_options.getFiles()[i]);
			try{
				PointSet2DInt set = loadPointSet(progress_bar);
				set.setName(_options.names[i]);
				if (set == null)
					success = false;
				else
					_options.section_set.addShape2D(set, _options.section, true);
			}catch (IOException e){
				e.printStackTrace();
				success = false;
				}
			}
		
		return success;
	}
	
	@Override
	public InterfaceShape loadShape(ShapeInputOptions options, ProgressUpdater progress_bar) throws IOException{
		return loadPointSet(progress_bar);
	}
	
	public PointSet2DInt loadPointSet(final ProgressUpdater progress_bar) throws IOException{
		
		PointSet2DInt point_set = null;
		
		if (progress_bar != null){
			point_set = (PointSet2DInt)Worker.post(new Job(){
				@Override
				public PointSet2DInt run(){
					try{
						return loadPointSetBlocking(progress_bar);
					}catch (IOException e){
						e.printStackTrace();
						return null;
						}
				}
			});
		}else{
			point_set = loadPointSetBlocking(progress_bar);
			}
		
		return point_set;
		
	}
	
	protected PointSet2DInt loadPointSetBlocking(ProgressUpdater progress_bar) throws IOException{
		
		if (dataFile == null || !dataFile.exists())
			throw new IOException("PointSet2DLoader: Cannot find input file '" + dataFile + "'.");
			
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String line = reader.readLine();
		int n = Integer.valueOf(line);
		
		if (progress_bar != null){
			progress_bar.setMessage("Loading 2D point set '" + dataFile.getName() + "': ");
			progress_bar.setMinimum(0);
			progress_bar.setMaximum(n);
			progress_bar.reset();
			}
		
		line = reader.readLine();
		float[] nodes = new float[n * 2];
		for (int i = 0; i < n; i++){
			if (line == null){
				reader.close();
				throw new IOException("PointSet2DLoader: End of file reached unexpectedly.");
				}
			StringTokenizer tokens = new StringTokenizer(line);
			nodes[i * 2] = Float.valueOf(tokens.nextToken());
			nodes[(i * 2) + 1] = Float.valueOf(tokens.nextToken());
			line = reader.readLine();
			}
		
		reader.close();
		
		return new PointSet2DInt(new PointSet2D(nodes));
		
	}

}