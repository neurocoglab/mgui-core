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
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.geometry.Vector3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.VectorSet3DInt;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.graphs.GraphFileLoader;
import foxtrot.Job;
import foxtrot.Worker;


public class VectorSet3DLoader extends FileLoader {

	public VectorSet3DLoader(){
		
	}
	
	public VectorSet3DLoader(File file){
		super.setFile(file);
	}
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		VectorSet3DInputOptions _options = (VectorSet3DInputOptions)options;
		boolean success = true;
		
		for (int i = 0; i < _options.getFiles().length; i++){
			setFile(_options.getFiles()[i]);
			try{
				VectorSet3DInt set = loadVectorSet(progress_bar);
				set.setName(_options.names[i]);
				if (set == null)
					success = false;
				else{
					System.out.print("Adding Vector3D set..");
					_options.shapeSet.addShape(set);
					InterfaceSession.log("done.");
					}
			}catch (IOException e){
				e.printStackTrace();
				success = false;
				}
			}
		
		return success;
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadVectorSet(progress_bar);
	}
	
	public VectorSet3DInt loadVectorSet(final ProgressUpdater progress_bar) throws IOException{
		
		if (dataFile == null)
			throw new IOException(getClass().getSimpleName() + ": No input file set.");
		
		VectorSet3DInt vector_set = null;
		
		if (progress_bar != null){
			vector_set = (VectorSet3DInt)Worker.post(new Job(){
				@Override
				public VectorSet3DInt run(){
					try{
						return loadVectorSetBlocking(progress_bar);
					}catch (IOException e){
						e.printStackTrace();
						return null;
						}
				}
			});
		}else{
			vector_set = loadVectorSetBlocking(progress_bar);
			}
		
		return vector_set;
	}

	protected VectorSet3DInt loadVectorSetBlocking(ProgressUpdater progress_bar) throws IOException{
		
		//load in domestic format
		BufferedReader reader = new BufferedReader(new FileReader(dataFile));
		String line = reader.readLine();
		if (line == null)
			throw new IOException("VectorSet3DLoader: unexpected end of file in '" + dataFile.getAbsolutePath() + "'.");
		
		int n = Integer.valueOf(line);
		
		if (progress_bar != null){
			progress_bar.setMessage("Loading vectors from '" + dataFile.getName() + "': ");
			progress_bar.setMinimum(0);
			progress_bar.setMaximum(n);
			progress_bar.reset();
			}
		
		VectorSet3DInt vectors = new VectorSet3DInt();
		
		line = reader.readLine();
		for (int i = 0; i < n; i++){
			if (line == null)
				throw new IOException("VectorSet3DLoader: unexpected end of file in '" + dataFile.getAbsolutePath() + "'.");
			StringTokenizer tokens = new StringTokenizer(line);
			vectors.addVector(new Vector3D(Float.valueOf(tokens.nextToken()),
										   Float.valueOf(tokens.nextToken()),
										   Float.valueOf(tokens.nextToken()),
										   Float.valueOf(tokens.nextToken()),
										   Float.valueOf(tokens.nextToken()),
										   Float.valueOf(tokens.nextToken())));
			line = reader.readLine();
			if (progress_bar != null)
				progress_bar.update(i);
			}
		
		
		reader.close();
		return vectors;
		
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = VectorSet3DLoader.class.getResource("/mgui/resources/icons/vector_3d_set_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/vector_3d_set_20.png");
		return null;
	}
	
}