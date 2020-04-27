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

import org.jogamp.java3d.Node;
import org.jogamp.java3d.PickInfo;
import org.jogamp.java3d.Shape3D;

import mgui.geometry.mesh.MeshEngine;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.ToolListener;
import mgui.interfaces.tools.graphics.Tool3D;

import org.jogamp.java3d.utils.pickfast.PickCanvas;


public class ToolSelectMeshBoundaryNodes extends Tool3D {

	public ToolSelectMeshBoundaryNodes(){
		init();
	}
	
	private void init(){
		name = "Select Mesh Boundary Nodes";
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		
		switch(e.getEventType()){
			case ToolConstants.TOOL_MOUSE_CLICKED:
			
				PickCanvas pickNode = targetPanel.getPickCanvasNode();
				if (pickNode == null) return;
				
				pickNode.setShapeLocation(e.getPoint().x, e.getPoint().y);
				PickInfo infoNode = pickNode.pickClosest();
				if (infoNode == null) return;
				Node n = infoNode.getNode();			
				if (n == null) return;
				Shape3DInt shape = null;
				if (n instanceof Shape3D)
					shape = ShapeFunctions.getShape((Shape3D)n);
				if (shape == null || !(shape instanceof Mesh3DInt)) return;
				
				//select boundary nodes
				MeshEngine engine = new MeshEngine();
				Mesh3DInt mesh = (Mesh3DInt)shape;
				engine.selectBoundaryNodes(mesh);
				
				InterfaceSession.log("ToolSelectMeshBoundaryNodes: selected " + mesh.getVertexSelection().getSelectedCount() +
								   " from mesh '" + mesh.getName() + "'.");
				
				deactivate();
				
		}
		
		
	}
	
	@Override
	public boolean isExclusive(){
		return false;
	}
	
	@Override
	public Object clone(){
		ToolSelectMeshBoundaryNodes tool = new ToolSelectMeshBoundaryNodes();
		tool.listeners = new ArrayList<ToolListener>(listeners);
		return tool;
	}
	
	
}