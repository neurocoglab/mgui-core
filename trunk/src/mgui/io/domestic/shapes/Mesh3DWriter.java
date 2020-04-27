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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.numbers.MguiDouble;
import foxtrot.Job;
import foxtrot.Worker;

/****************************************************************
 * Default writer for a {@code Mesh3DInt} object. Writes as an Ascii tri file.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Mesh3DWriter extends SurfaceFileWriter {

	
	public Mesh3DWriter(){
		
	}
	
	public Mesh3DWriter(File surfaceFile){
		dataFile = surfaceFile;
	}
	
	@Override
	public boolean writeSurface(final Mesh3DInt mesh, final InterfaceIOOptions options, final ProgressUpdater progress_bar) {
		
		if (progress_bar != null){
			
			return ((Boolean)Worker.post(new Job(){
				
				@Override
				public Boolean run(){
					return writeSurfaceBlocking(mesh, progress_bar);
				}}));
		}
		
		return writeSurfaceBlocking(mesh, null);
		
		
	}
	
	public boolean writeSurfaceBlocking(final Mesh3DInt mesh, final ProgressUpdater progress_bar) {
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
		 * 		 but taken from line position + 1
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
			InterfaceSession.log("Mesh3DWriter: No output file specified..");
			return false;
			}
		
			Mesh3D mesh3d = mesh.getMesh();
			
			try{
				
				//if data file exists, kill it
				if (dataFile.exists() && !dataFile.delete()){
					InterfaceSession.log("Mesh3DWriter: Cannot overwrite existing output file '" +
						dataFile.getAbsolutePath() + "'..");
					return false;
					}
				
				if (progress_bar != null){
					progress_bar.setMessage("Writing minc surface '" + mesh.getName() +"': ");
					progress_bar.setMinimum(0);
					progress_bar.setMaximum(mesh3d.n + mesh3d.f);
					progress_bar.reset();
					}
				
				BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile)); 
				
				//# of nodes
				bw.write(mesh3d.n + "\n");
				
				//coordinates
				for (int i = 0; i < mesh3d.n; i++){
					Point3f pt = mesh3d.getVertex(i);
					bw.write(Integer.valueOf(i + 1).intValue() + " " + 
							 MguiDouble.getString(pt.x, number_format) + " " + 
							 MguiDouble.getString(pt.y, number_format) + " " +
							 MguiDouble.getString(pt.z, number_format) + "\n");
					if (progress_bar != null)
						progress_bar.update(i);
						
					}
				
				//# of faces
				bw.write(mesh3d.f + "\n");
				
				//faces
				for (int i = 0; i < mesh3d.f; i++){
					Mesh3D.MeshFace3D face = mesh3d.getFace(i);
					bw.write(Integer.valueOf(i + 1).intValue() + " " + 
							 Integer.valueOf(face.A + 1) + " " + 
							 Integer.valueOf(face.C + 1) + " " +
							 Integer.valueOf(face.B + 1) + "\n");
					if (progress_bar != null)
						progress_bar.update(i + mesh3d.n);
					}
				
				bw.close();
				return true;
				
			}catch (IOException e){
				e.printStackTrace();
				return false;
				}
		
		
	}
	
	@Override
	public InterfaceIOType getLoaderComplement(){
		return (new Mesh3DLoader()).getIOType();
	}
	
}