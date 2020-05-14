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

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import mgui.geometry.polygon.PolygonFunctions;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.dialogs.DialogTool;
import mgui.interfaces.tools.dialogs.DialogToolDialogBox;
import mgui.morph.sections.MorphEngine;


public class ToolCreateMeshFromPolylines extends DialogTool {

	public ToolCreateMeshFromPolylines(){
		init();
	}
	
	@Override
	public Object clone() {
		return null;
	}

	@Override
	protected boolean doTaskBlocking(InterfaceOptions options, InterfaceProgressBar progress_bar) {
		if (options == null || !(options instanceof ToolCreateMeshFromPolylinesOptions)) return false;
		ToolCreateMeshFromPolylinesOptions _options = (ToolCreateMeshFromPolylinesOptions)options;
		
		if (_options.source_set == null){
			message = "No source section set specified!";
			return false;
			}
		
		if (_options.target_set == null){
			message = "No target shape set specified!";
			return false;
			}
		
		//set polygons all clockwise
		if (_options.redirect)
			PolygonFunctions.setPolygonDirections(_options.source_set, PolygonFunctions.PolyDirection.Clockwise);
		
		MorphEngine morph_engine = new MorphEngine();
		Mesh3DInt mesh = morph_engine.getMorphMesh(_options.source_set, _options.weight_threshold);
		if (mesh == null){
			message = "Error creating mesh!";
			return false;
			}
		
		mesh.getAttributes().setIntersection(_options.attributes);
		mesh.setName(_options.name);
		_options.target_set.addShape(mesh);
		
		message = "Mesh '" + _options.name + "' created.";
		
		return true;
	}

	@Override
	protected String getToolTitle(){
		return "Create Mesh From Polylines";
	}
	
	@Override
	protected DialogToolDialogBox getDialogBox(){
		return getDialogBox(InterfaceSession.getSessionFrame());
	}
	
	protected DialogToolDialogBox getDialogBox(JFrame frame) {
		return new ToolCreateMeshFromPolylinesDialog(frame, new ToolCreateMeshFromPolylinesOptions());
	}
	
	@Override
	protected void setIcon(){
		
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/tools/create_mesh_from_polylines_30.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/tools/create_mesh_from_polylines_30.png");
			
	}

}