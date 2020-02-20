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

package mgui.interfaces.tools.shapes;

import java.util.ArrayList;

import mgui.geometry.Polygon3D;
import mgui.geometry.mesh.MeshFunctionException;
import mgui.geometry.mesh.MeshFunctions;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.shapes.LPolygon3DInt;
import mgui.interfaces.shapes.PolygonSet3DInt;
import mgui.interfaces.tools.dialogs.DialogTool;
import mgui.interfaces.tools.dialogs.DialogToolDialogBox;


public class ToolGetMeshIntersectionPolylines extends DialogTool {

	public ToolGetMeshIntersectionPolylines(){
		init();
	}
	
	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean doTaskBlocking(InterfaceOptions options, InterfaceProgressBar progress_bar) {
		
		ToolGetMeshIntersectionPolylinesOptions _options = (ToolGetMeshIntersectionPolylinesOptions)options;
		
		if (_options.mesh1 == null || _options.mesh2 == null){
			message = "Meshes not specified!";
			return false;
			}
		
		if (_options.mesh1 == _options.mesh2){
			message = "Both meshes are the same mesh!";
			return false;
			}
		
		if (_options.target_set == null){
			message = "No shape set specified!";
			return false;
			}
		
		try{
			ArrayList<Polygon3D> polygons = MeshFunctions.getIntersectionSegments(_options.mesh1.getMesh(), 
																				  _options.mesh2.getMesh(), 
																				  progress_bar);
			
			if (polygons.size() == 0){
				message = "Meshes do not appear to intersect..";
				return false;
			}
			
			PolygonSet3DInt poly_set = new PolygonSet3DInt();
			for (Polygon3D poly : polygons){
				LPolygon3DInt l_poly = new LPolygon3DInt(poly);
				l_poly.updateShape();
				poly_set.addShape(poly, false, false);
				}
			
			_options.target_set.addShape(poly_set);
			
			message = "Success! " + polygons.size() + " segments found.";
			
			return true;
			
		}catch (MeshFunctionException e){
			e.printStackTrace();
			message = "Error finding intersection!";
			return false;
			}
		
	}

	@Override
	protected DialogToolDialogBox getDialogBox() {
		return new ToolGetMeshIntersectionPolylinesDialog(InterfaceSession.getSessionFrame(), new ToolGetMeshIntersectionPolylinesOptions());
	}

	@Override
	protected String getToolTitle() {
		return "Get Mesh Intersection Polylines";
	}

}