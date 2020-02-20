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

package mgui.io.foreign.duff;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;

import javax.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileLoader;
import mgui.io.util.EndianCorrectInputStream;


public class DuffSurfaceLoader extends SurfaceFileLoader {

	
	public DuffSurfaceLoader(){
		
	}
	
	public DuffSurfaceLoader(File file){
		setFile(file);
	}
	
	
	@Override
	public Mesh3DInt loadSurface(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		
		//for format, refer to http://neuroimage.usc.edu/forums/showthread.php?t=83
		//reader header
		DuffSurfaceHeader header = new DuffSurfaceHeader(dataFile);
		if (!header.isSet){
			InterfaceSession.log("DuffSurfaceLoader: Problem reading header from file '" + dataFile.getAbsolutePath() +"'.");
			return null;
			}
		
		try{
			
			EndianCorrectInputStream in = new EndianCorrectInputStream(dataFile.getAbsolutePath(), 
																	   header.byte_order == ByteOrder.BIG_ENDIAN);
			
			in.skip(header.hdr_size);
			Mesh3D mesh = new Mesh3D();
			
			//read faces
			for (int i = 0; i < header.n_faces; i++)
				mesh.addFace(in.readIntCorrect(), in.readIntCorrect(), in.readIntCorrect());
			
			//read nodes (should directly follow)
			for (int i = 0; i < header.n_nodes; i++)
				mesh.addVertex(new Point3f(in.readFloatCorrect(), in.readFloatCorrect(), in.readFloatCorrect()));
			
			//TODO: read node data?
			
			//TODO: transform with header.orientation matrix
			
			in.close();
			
			InterfaceSession.log("DuffSurfaceLoader: Resulting mesh:");
			InterfaceSession.log("Faces: " + mesh.f);
			InterfaceSession.log("Nodes: " + mesh.n);
			
			return new Mesh3DInt(mesh);
			
		}catch (Exception e){
			e.printStackTrace();
			}
		
		return null;
	}
	

}