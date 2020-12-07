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

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.io.FileWriter;
import mgui.io.InterfaceIOOptions;

/**************************************
 * 
 * Represents an abstract surface file writer object. All {@code FileWriter} objects that write surface
 * meshes should extend this class.
 * 
 * 
 * @author Andrew Reid
 * @since 1.0.0
 *
 */
public abstract class SurfaceFileWriter extends FileWriter {
	
	public String number_format = "#0.00000";
	
	@Override
	public boolean write(InterfaceIOOptions options, ProgressUpdater progress_bar){
		if (!(options instanceof SurfaceOutputOptions)) return false;
		SurfaceOutputOptions s_options = (SurfaceOutputOptions)options;
		number_format = s_options.number_format;
		if (s_options.mesh == null) return false;
		boolean success = true;
		for (int i = 0; i < s_options.files.length; i++){
			setFile(s_options.files[i]);
			success &= writeSurface(s_options.mesh, s_options, progress_bar);
			}
		return success;
	}
	
	/******************************************************
	 * Write {@code mesh} with this {@code FileWriter}.
	 * 
	 * @param mesh
	 * @return
	 */
	public boolean writeSurface(Mesh3DInt mesh){
		return writeSurface(mesh, null, null);
	}
	
	/******************************************************
	 * Write {@code meshes} with this {@code FileWriter}.
	 * 
	 * @param mesh
	 * @return
	 */
	public boolean writeSurfaces(ArrayList<Mesh3DInt> meshes){
		return writeSurfaces(meshes, null, null);
	}
	
	/******************************************************
	 * Write {@code mesh} with this {@code FileWriter}.
	 * 
	 * @param mesh
	 * @return
	 */
	public abstract boolean writeSurface(Mesh3DInt mesh, InterfaceIOOptions options, ProgressUpdater progress_bar);
	
	/******************************************************
	 * Write {@code meshes} with this {@code FileWriter}.
	 * 
	 * TODO: this should throw an IOException
	 * 
	 * @param mesh
	 * @return
	 */
	public boolean writeSurfaces(ArrayList<Mesh3DInt> meshes, InterfaceIOOptions options, ProgressUpdater progress_bar){
		boolean success = true;
		for (int i = 0; i < meshes.size(); i++)
			success &= writeSurface(meshes.get(i), options, progress_bar);
		return success;
	}
	
	public void setFormat(String number_format){
		this.number_format = number_format;
	}
	
	@Override
	public String getSuccessMessage(){
		return "Surface(s) written successfully.";
	}
	
	@Override
	public String getFailureMessage(){
		return "Failed to fully write surface(s).";
	}
	
	@Override
	public String getTitle(){
		return "Write surface to file";
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = SurfaceFileWriter.class.getResource("/mgui/resources/icons/mesh_3d_20.png");
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