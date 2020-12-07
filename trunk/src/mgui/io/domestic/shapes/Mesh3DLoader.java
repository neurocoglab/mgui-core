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

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;

/***********************************************************
 * Deafult loader for loading a {@linkplain Mesh3DInt} object. The format is a .tri file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Mesh3DLoader extends SurfaceFileLoader {

	public Mesh3DLoader(){
		
	}
	
	public Mesh3DLoader(File file){
		setFile(file);
	}
	
	@Override
	public InterfaceShape loadShape(ShapeInputOptions options, ProgressUpdater progress_bar) throws IOException{
		return loadSurface(progress_bar, options);
	}
	
	
	@Override
	public Mesh3DInt loadSurface(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		/***************************
		 * Ascii Format:
		 * 
		 * n [# of nodes]
		 * 1 x_1 y_1 z_1
		 * ...
		 * i x_i y_i z_i
		 * ...
		 * n x_n y_n z_n
		 * 
		 * note: indices for faces are not read from file,
		 * 		 but taken from line position
		 * 
		 * f [# of faces]
		 * 1 a_1 b_1 c_1
		 * ...
		 * i a_i b_i c_i
		 * ...
		 * f a_f b_f b_f
		 * 
		 ***********************/
		
		if (dataFile == null){
			InterfaceSession.log("Mesh3DLoader: No input file specified..");
			return null;
			}
			
		if (!dataFile.exists()){
			InterfaceSession.log("Mesh3DLoader: Cannot find file '" + dataFile.getAbsolutePath() + "'");
			return null;
			}
		
		String line;
		StringTokenizer tokens;
		Mesh3D mesh = new Mesh3D();
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(dataFile));
			line = br.readLine().trim();
			
			//first line is # nodes
			int n = Integer.valueOf(line).intValue();
			
			for (int i = 0; i < n; i++){
				line = br.readLine();
				tokens = new StringTokenizer(line);
				//skip index
				tokens.nextToken();
				mesh.addVertex(new Point3f(Float.valueOf(tokens.nextToken()).floatValue(),
										 Float.valueOf(tokens.nextToken()).floatValue(),
										 Float.valueOf(tokens.nextToken()).floatValue()));
				}
				
			//next line is # faces
			line = br.readLine().trim();
			//skip any blank lines
			while (line.length() == 0)
				line = br.readLine();
			
			int f = Integer.valueOf(line).intValue();
			
			for (int i = 0; i < f; i++){
				line = br.readLine();
				tokens = new StringTokenizer(line);
				//skip index
				tokens.nextToken();
				mesh.addFace(Integer.valueOf(tokens.nextToken()).intValue() - 1,
							 Integer.valueOf(tokens.nextToken()).intValue() - 1, 
							 Integer.valueOf(tokens.nextToken()).intValue() - 1);
				}
			
			br.close();
			
			mesh.finalize();
			
			InterfaceSession.log("Tri file '" + dataFile.getAbsolutePath() + "' loaded.", LoggingType.Verbose);
			InterfaceSession.log("Faces: " + mesh.f + " (" + mesh.faces.length / 3f + ")", LoggingType.Debug);
			InterfaceSession.log("Nodes: " + mesh.n + " (" + mesh.nodes.length / 3f + ")", LoggingType.Debug);
			
			Mesh3DInt mesh_int = new Mesh3DInt(mesh);
			mesh_int.setFileLoader(getIOType());
			mesh_int.setUrlReference(dataFile.toURI().toURL());
			return mesh_int;
			
		}catch (IOException e){
			//e.printStackTrace();
			InterfaceSession.log("Mesh3DLoader: Error loading mesh\nDetails: " + e.getMessage(), 
								 LoggingType.Errors);
			}
		
		return null;
	}
	
	@Override
	public InterfaceIOType getWriterComplement(){
		return (new Mesh3DWriter()).getIOType();
	}

}