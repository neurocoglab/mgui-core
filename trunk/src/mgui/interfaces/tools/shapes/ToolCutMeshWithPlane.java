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

package mgui.interfaces.tools.shapes;

import java.util.ArrayList;

import javax.swing.ImageIcon;

import mgui.geometry.Mesh3D;
import mgui.geometry.Plane3D;
import mgui.geometry.mesh.MeshFunctionException;
import mgui.geometry.mesh.MeshFunctions;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.dialogs.DialogTool;
import mgui.interfaces.tools.dialogs.DialogToolDialogBox;
import mgui.numbers.MguiBoolean;

/*******************************************************
 * Tool to cut a mesh into parts by intersecting it with a {@linkplain Plane3D} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolCutMeshWithPlane extends DialogTool {

	public ToolCutMeshWithPlane(){
		init();
	}
	
	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean doTaskBlocking(InterfaceOptions options, InterfaceProgressBar progress_bar) {
		if (options == null || !(options instanceof ToolCutMeshWithPlaneOptions)) return false;
		ToolCutMeshWithPlaneOptions _options = (ToolCutMeshWithPlaneOptions)options;
		
		if (_options.cut_plane == null){
			message = "No cutting plane specified!";
			return false;
			}
		
		if (_options.target_set == null){
			message = "No target shape set specified!";
			return false;
			}
		
		if (_options.mesh == null){
			message = "No mesh specified!";
			return false;
			}
		
		Mesh3D mesh = _options.mesh.getMesh();
		Plane3D plane = _options.cut_plane;
		
		try{
			
			ArrayList<Mesh3D> results = MeshFunctions.cutMeshWithPlane(mesh, plane, progress_bar);
			int count = 0;
			for (int m = 0; m < results.size(); m++)
				if (results.get(m) != null){
					Mesh3DInt mesh3D = new Mesh3DInt(results.get(m));
					mesh3D.getAttributes().setIntersection(_options.mesh.getAttributes());
					String above = "above";
					if (m == 1) above = "below";
					if (m == 2){
						mesh3D.setName(_options.prefix + "_debug_points");
						mesh3D.setAttribute("ShowNodes", new MguiBoolean(true));
					}else{
						mesh3D.setName(_options.prefix + "_" + above + "_plane");
						}
					_options.target_set.addShape(mesh3D);
					count++;
					}
			if (count == 0){
				message = "No meshes were returned!";
				return false;
				}
			
			message = "Success. " + count + " meshes added to '" + _options.target_set.getName() +"'.";
			return true;
			
		}catch (MeshFunctionException e){
			e.printStackTrace();
			message = "Cut operation failed!";
		}
		
		return false;
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/tools/cut_mesh_with_plane_30.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/tools/cut_mesh_with_plane_30.png");
	}
	
	@Override
	protected String getToolTitle(){
		return "Cut Mesh With Plane";
	}

	@Override
	protected DialogToolDialogBox getDialogBox() {
		return new ToolCutMeshWithPlaneDialog(InterfaceSession.getSessionFrame(), new ToolCutMeshWithPlaneOptions());

	}

}