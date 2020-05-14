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

import mgui.geometry.mesh.MeshFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.ToolListener;
import mgui.interfaces.tools.graphics.Tool3D;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;

import org.jogamp.java3d.utils.pickfast.PickCanvas;


/********************************************
 * Tool selects region boundary nodes of a mesh region (defined by its vertex values) based upon a vertex selected 
 * by a mouse click. (TODO) Dialog appears to specify the data column to use and whether to select all regions in mesh
 * or only those in the region containing the selected vertex.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolSelectRegionBoundaryNodes extends Tool3D {

	public ToolSelectRegionBoundaryNodes(){
		init();
	}
	
	private void init(){
		name = "Select Region Boundary Nodes";
		
	}
	
	@Override
	public boolean isExclusive(){
		return false;
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		
		switch(e.getEventType()){
			case ToolConstants.TOOL_MOUSE_CLICKED:
				
				//get selected vertex, then call getBoundaryNodes
				PickCanvas pickNode = targetPanel.getPickCanvasNode();
				if (pickNode == null) return;
				PickCanvas pickGeom = targetPanel.getPickCanvasGeom();
				if (pickGeom == null) return;
				MguiInteger node = new MguiInteger(-1);
				
				Shape3DInt shape = ShapeFunctions.getPickedShape(pickNode, pickGeom, e.getPoint(), node);
				if (shape == null || !(shape instanceof Mesh3DInt) ||  node.getValue() < 0){
					InterfaceSession.log("ToolSelectRegionBoundaryNodes: No shape or node chosen...");
					return;
				}
				
				Mesh3DInt mesh_int = (Mesh3DInt)shape;
				ArrayList<MguiNumber> values = mesh_int.getCurrentVertexData();
				if (values == null){
					InterfaceSession.log("ToolSelectRegionBoundaryNodes: No current data found...");
					return;
					}
				int value = (int)values.get(node.getInt()).getValue();
				
				ArrayList<Integer> b_nodes = MeshFunctions.getRegionBoundaryNodeIndices(mesh_int.getMesh(), values, value);
				if (b_nodes.size() == 0){
					InterfaceSession.log("ToolSelectRegionBoundaryNodes: No boundary nodes found...");
					return;
					}
				
				mesh_int.setSelectedVertices(b_nodes);
				mesh_int.setAttribute("ShowSelectedNodes", new MguiBoolean(true));
				
				return;
			}
		
		
	}
	
	@Override
	public void deactivate(){
		super.deactivate();
	}
	
	@Override
	public Object clone(){
		ToolSelectRegionBoundaryNodes tool = new ToolSelectRegionBoundaryNodes();
		tool.listeners = new ArrayList<ToolListener>(listeners);
		return tool;
	}
	
	
	
	
}