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

package mgui.io.domestic.printing;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.Mesh3D.MeshFace3D;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.domestic.shapes.SurfaceFileWriter;
import mgui.io.util.EndianCorrectOutputStream;
import mgui.numbers.MguiDouble;

import org.apache.commons.lang3.StringUtils;

/**********************************************************************
 * Writes a {@code Mesh3D} object to STL format, suitable for 3D printing. Implements ASCII and
 * binary formats.
 * 
 * <p>See <a href="http://en.wikipedia.org/wiki/STL_(file_format)">this link</a> for details.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class StlWriter extends SurfaceFileWriter {

	@Override
	public boolean writeSurface(Mesh3DInt mesh, InterfaceIOOptions options, ProgressUpdater progress_bar) {
		
		if (options == null){
			InterfaceSession.log("StlWriter: Options cannot be null.", 
								  LoggingType.Errors);
			return false;
			}
		
		if (!(options instanceof StlOutputOptions)){
			InterfaceSession.log("StlWriter: Bad options class: found " + options.getClass().getCanonicalName() +
					" (must be instance of StlOutputOptions).", 
					LoggingType.Errors);
			return false;
			}
		
		this.options = options;
		StlOutputOptions _options = (StlOutputOptions)options;
		
		switch (_options.format){
			case Ascii:
				return writeAscii(mesh, progress_bar);
				
			case Binary:
				return writeBinary(mesh, progress_bar);
			}
		
		return false;
	}

	
	/**************************************************
	 * Writes {@code mesh} as an ASCII-format STL file.
	 * 
	 * @param mesh
	 * @return
	 */
	public boolean writeAscii(Mesh3DInt mesh, ProgressUpdater progress_bar){
		
		// Open file for writing
		if (this.dataFile == null){
			InterfaceSession.log("StlWriter: No output file specified.", 
					  			 LoggingType.Errors);
			return false;
			}
		
		try{
			BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(dataFile));
		
			// solid name
			writer.write("solid " + mesh.getName() + "\n");
			Mesh3D mesh3d = mesh.getMesh();
			MeshFace3D face;
			Vector3f normal;
			Point3f vertex;
			
			if (progress_bar != null){
				progress_bar.setMinimum(0);
				progress_bar.setMaximum(mesh3d.getFaceCount());
				}
			
			for (int i = 0; i < mesh3d.getFaceCount(); i++){
				face = mesh3d.getFace(i);
				normal = mesh3d.getNormalAtFace(i);
				
				// facet normal ni nj nk
				writer.write("\nfacet normal ");
				writer.write(MguiDouble.getString(normal.getX(), number_format) + " ");
				writer.write(MguiDouble.getString(normal.getY(), number_format) + " ");
				writer.write(MguiDouble.getString(normal.getZ(), number_format));
				
				// outer loop
				writer.write("\n\touter loop");
				
				//		vertex v1x v1y v1z
				//	    vertex v2x v2y v2z
				//	    vertex v3x v3y v3z
				for (int j = 0; j < 3; j++){
					writer.write("\n\t\tvertex ");
					vertex = mesh3d.getVertex(face.getNode(j));
					writer.write(MguiDouble.getString(vertex.getX(), number_format) + " ");
					writer.write(MguiDouble.getString(vertex.getY(), number_format) + " ");
					writer.write(MguiDouble.getString(vertex.getZ(), number_format));
					}
				
				// endloop
				writer.write("\n\tendloop");
				
				// endfacet
				writer.write("\nendfacet");
				
				if (progress_bar != null){
					if (progress_bar.isCancelled()){
						InterfaceSession.log("StlWriter: Operation cancelled by user.", LoggingType.Warnings);
						writer.close();
						return false;
						}
					progress_bar.update(i);
					}
				
				}
			
			//endsolid name
			writer.write("\n\nendsolid " + mesh.getName() + "\n");
		
			writer.close();
			
		}catch (IOException ex){
			InterfaceSession.log("StlWriter: I/O exception encountered: " + ex.getMessage(), 
			 			 		 LoggingType.Errors);
			InterfaceSession.handleException(ex);
			return false;
			}
		
		return true;
	}
	
	
	/**************************************************
	 * Writes {@code mesh} as an binary-format STL file.
	 * 
	 * @param mesh
	 * @return
	 */
	public boolean writeBinary(Mesh3DInt mesh, ProgressUpdater progress_bar){
		
		// Open file for writing
		if (this.dataFile == null){
			InterfaceSession.log("StlWriter: No output file specified.", 
					  			 LoggingType.Errors);
			return false;
			}
		
		try{
			EndianCorrectOutputStream ecos = new EndianCorrectOutputStream(dataFile.getAbsolutePath(), false);
			String header = "Generated by ModelGUI: " + mesh.getName();
			if (header.length() > 80) header = header.substring(0,80);
			if (header.length() < 80) header = StringUtils.rightPad(header, 80, ' ');
			
			Mesh3D mesh3d = mesh.getMesh();
			MeshFace3D face;
			Vector3f normal;
			Point3f vertex;
			
			if (progress_bar != null){
				progress_bar.setMinimum(0);
				progress_bar.setMaximum(mesh3d.getFaceCount());
				}
			
			//UINT8[80] – Header
			byte[] b = header.getBytes(Charset.forName("US-ASCII"));
			ecos.write(b);
			
			//UINT32 – Number of triangles
			ecos.writeIntCorrect(mesh3d.getFaceCount());

			//foreach triangle
			for (int i = 0; i < mesh3d.getFaceCount(); i++){
				face = mesh3d.getFace(i);
				normal = mesh3d.getNormalAtFace(i);
				
				//REAL32[3] – Normal vector
				ecos.writeFloatCorrect(normal.getX());
				ecos.writeFloatCorrect(normal.getY());
				ecos.writeFloatCorrect(normal.getZ());
				
				//REAL32[3] – Vertex 1
				//REAL32[3] – Vertex 2
				//REAL32[3] – Vertex 3
				for (int j = 0; j < 3; j++){
					vertex = mesh3d.getVertex(face.getNode(j));
					ecos.writeFloatCorrect(vertex.getX());
					ecos.writeFloatCorrect(vertex.getY());
					ecos.writeFloatCorrect(vertex.getZ());
					}
				
				//UINT16 – Attribute byte count (this is a dummy value since there are no attributes)
				ecos.writeShortCorrect((short)0);
				
				if (progress_bar != null){
					if (progress_bar.isCancelled()){
						InterfaceSession.log("StlWriter: Operation cancelled by user.", LoggingType.Warnings);
						ecos.close();
						return false;
						}
					progress_bar.update(i);
					}
				}
			
			//end
			ecos.close();
			
		}catch (IOException ex){
			InterfaceSession.log("StlWriter: I/O exception encountered: " + ex.getMessage(), 
			 			 		 LoggingType.Errors);
			InterfaceSession.handleException(ex);
			return false;
			}
		
		return true;
				
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(Mesh3DInt.class);
		return objs;
	}

}