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

import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.geometry.Mesh3D;
import mgui.geometry.mesh.MeshFunctionException;
import mgui.geometry.mesh.MeshFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.FileLoader;
import mgui.io.InterfaceIOOptions;

/*************************************************************
 * Abstract loader class for all loaders which load {@linkplain Mesh3DInt} objects.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public abstract class SurfaceFileLoader extends InterfaceShapeLoader {
	
	
	@Override
	public boolean load(InterfaceIOOptions options, ProgressUpdater progress_bar){
		if (!(options instanceof SurfaceInputOptions)) return false;
		SurfaceInputOptions s_options = (SurfaceInputOptions)options;
		if (s_options.shapeSet == null || s_options.files == null) return false;
		ArrayList<Mesh3D> merge_shapes = null;
		if (s_options.merge_shapes)
			merge_shapes = new ArrayList<Mesh3D>();
		for (int i = 0; i < s_options.files.length; i++){
			setFile(s_options.files[i]);
			try{
				Mesh3DInt mesh = loadSurface(progress_bar, s_options);
				if (mesh == null) return false;
				
				if (!s_options.merge_shapes){
					mesh.setName(s_options.names[i]);
					s_options.shapeSet.addShape(mesh);
				}else{
					merge_shapes.add(mesh.getMesh());
					}
			}catch (IOException ex){
				return false;
				}
			}
		if (s_options.merge_shapes && merge_shapes.size() > 1){
			try{
				Mesh3DInt mesh = new Mesh3DInt(MeshFunctions.mergeMeshes(merge_shapes));
				mesh.setName(s_options.merge_name);
				s_options.shapeSet.addShape(mesh);
			}catch (MeshFunctionException ex){
				InterfaceSession.log("SurfaceFileLoader: Could not merge meshes.\nDetails:" +
									 ex.getMessage(), 
									 LoggingType.Errors);
				return false;
				}
			}
		return true;
	}
	
	@Override
	public String getSuccessMessage(){
		return "Surface(s) loaded successfully.";
	}
	
	@Override
	public String getFailureMessage(){
		return "Failed to fully load surface(s).";
	}
	
	@Override
	public String getTitle(){
		return "Load surface file";
	}
	
	@Override
	public Object loadObject(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException{
		return loadSurface(progress_bar, options);
	}
	
	@Override
	public InterfaceShape loadShape(ShapeInputOptions options, ProgressUpdater progress_bar) throws IOException{
		return loadSurface(progress_bar, options);
	}
	
	/***************************************************************
	 * Loads a single surface from the appropriate source. Returns a Mesh3DInt object if load was successful,
	 * or null otherwise.
	 * This method should also set the file loader and reference URL for the object.
	 * 
	 * @return
	 * @throws IOException
	 */
	public Mesh3DInt loadSurface() throws IOException{
		return loadSurface(null, null);
	}
	
	/***************
	 * Loads a single surface from the appropriate source. Returns a Mesh3DInt object if load was successful,
	 * or null otherwise. This method should also set the file loader and reference URL for the object.
	 * 
	 * @return Mesh3DInt mesh representing the surface being loaded
	 */
	public Mesh3DInt loadSurface(ProgressUpdater progress_bar) throws IOException{
		return loadSurface(progress_bar, null);
	}
	
	/***************
	 * Loads a single surface from the appropriate source. Returns a Mesh3DInt object if load was successful,
	 * or null otherwise. This method should also set the file loader and reference URL for the object.
	 * 
	 * @return Mesh3DInt mesh representing the surface being loaded
	 */
	public abstract Mesh3DInt loadSurface(ProgressUpdater progress_bar, InterfaceIOOptions options) throws IOException;
	
	/***************
	 * Loads a set of surfaces from the appropriate source. Returns an ArrayList of Mesh3DInt objects if load 
	 * was successful, or null otherwise.
	 * This method should also set the file loader and reference URL for the object.
	 * 
	 * @return Mesh3DInt mesh representing the surface being loaded
	 */
	public ArrayList<Mesh3DInt> loadSurfaces() throws IOException{
		return null;
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = SurfaceFileLoader.class.getResource("/mgui/resources/icons/mesh_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/mesh_3d_20.png");
		return null;
	}
	
	@Override
	public ArrayList<Class<?>> getSupportedObjects(){
		ArrayList<Class<?>> objs = new ArrayList<Class<?>>();
		objs.add(Mesh3DInt.class);
		return objs;
	}
	
}