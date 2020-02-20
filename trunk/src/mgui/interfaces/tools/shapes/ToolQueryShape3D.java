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

import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Node;
import javax.media.j3d.PickInfo;
import javax.media.j3d.PickInfo.IntersectionInfo;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import mgui.interfaces.queries.InterfaceQuery;
import mgui.interfaces.queries.ObjectNotQueriableException;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.queries.InterfaceShapeQuery;
import mgui.interfaces.shapes.queries.InterfaceShapeQuery.QueryMode;
import mgui.interfaces.shapes.queries.QueryShapeVertex;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.ToolListener;
import mgui.interfaces.tools.graphics.Tool3D;
import mgui.interfaces.tools.queries.QueryTool;
import mgui.numbers.MguiInteger;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.pickfast.PickCanvas;
import com.sun.j3d.utils.pickfast.PickIntersection;

/**************************************************************
 * Tool allowing point-and-click vertex-wise querying of 3D objects, for a {@linkplain InterfaceGraphic3D} window.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolQueryShape3D extends Tool3D implements QueryTool {

	protected transient ShapeSelectionSet selectionSet;
	protected transient Point3d selectedPt;
	protected transient Shape3D selectedShape;
	protected transient int[] selectedNodes;
	
	//protected ArrayList<InterfaceQuery> results;
	protected transient InterfaceShapeQuery current_query;
	
	QueryMode mode;
	BranchGroup spheres;
	
	
	public ToolQueryShape3D(InterfaceShapeQuery query){
		this(QueryMode.SingleObject, query);
	}
	
	public ToolQueryShape3D(QueryMode mode, InterfaceShapeQuery query){
		setMode(mode);
		current_query = query;
		init();
	}
	
	@Override
	public boolean isExclusive(){
		return false;
	}
	
	public void setMode(QueryMode mode){
		this.mode = mode;
		
		//TODO: other stuff depending on mode
		
		
	}
	
	private void init(){
		name = "Query Shape 3D";
	}
	
	public InterfaceQuery getQuery(){
		return current_query;
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		
		switch(e.getEventType()){
			case ToolConstants.TOOL_MOUSE_CLICKED:
				//initiate pick
				PickCanvas pickNode = targetPanel.getPickCanvasNode();
				PickCanvas pickGeom = targetPanel.getPickCanvasGeom();
				if (pickGeom == null) return;
				MguiInteger node = new MguiInteger(-1);
				Shape3DInt picked_shape = ShapeFunctions.getPickedShape(pickNode, 
																		pickGeom, 
																	   	e.getPoint(),
																	   	node);
				if (picked_shape == null || node.getInt() < 0) return;
				
				PickInfo infoNode = pickNode.pickClosest();
				
				switch (mode){
					case SingleObject:
						selectedPt = infoNode.getClosestIntersectionPoint();
						current_query.clearVertices();
						
						//query and fire listeners
						try{
							current_query.query(picked_shape);
						}catch (ObjectNotQueriableException ex){
							//won't happen
							ex.printStackTrace();
							}
						break;
						
					case SingleNode:
						//PickCanvas pickGeom = targetPanel.getPickCanvasGeom();
						int index = -1;
						if (node.getValue() < 0) {
							//Picked shape is not a vertex shape
							pickGeom.setShapeLocation(e.getPoint().x, e.getPoint().y);
							
							PickInfo[] infoGeom = pickGeom.pickAllSorted();
							
							Transform3D t3d = new Transform3D();
							infoNode.getNode().getLocalToVworld(t3d);
							IntersectionInfo[] infos = infoGeom[0].getIntersectionInfos();
							if (infos == null) return;
							
							PickInfo.IntersectionInfo intInfo = infos[0];
							PickIntersection intersect = new PickIntersection(t3d, intInfo);
							int[] face = intersect.getPrimitiveCoordinateIndices();
							index = intersect.getClosestVertexIndex();
							index = face[index];
						}else{
							//Picked shape is a vertex shape
							index = node.getInt();
							}
						
						current_query.clearVertices();
						current_query.addQueryVertex(new QueryShapeVertex(picked_shape, index));
						displaySelectedNode(new Point3d(picked_shape.getShape().getVertex(index)));
						
						try{
							current_query.query(picked_shape);
						}catch (ObjectNotQueriableException ex){
							//won't happen
							ex.printStackTrace();
							}
						break;
						
					case MultiNode:
						//TODO: implement me
						
						break;
						
					}
				
				return;
			}
		
		
	}
	
	@Override
	public Object clone(){
		ToolQueryShape3D tool = new ToolQueryShape3D(mode, current_query);
		tool.listeners = new ArrayList<ToolListener>(listeners);
		return tool;
	}
	
	protected void displaySelectedNode(Point3d point){
		clearNodes();
	
		spheres = new BranchGroup();
		spheres.setCapability(BranchGroup.ALLOW_DETACH);
	
		float distance = (float)targetPanel.getCamera().getDistance();
		float node_size = distance / 300f;		//1% of camera distance..
		
		spheres.addChild(ShapeFunctions.getSphereAtPoint(node_size, point));
	
		targetPanel.getModel().addTempShape(spheres);
		
	}
	
	public void clearNodes(){
		if (spheres != null)
			spheres.detach();
	}
	
	protected void displaySelectedNodes(GeometryArray geom){
		if (selectedNodes == null) return;
		if (spheres != null)
				spheres.detach();
		
		spheres = new BranchGroup();
		
		for (int i = 0; i < selectedNodes.length; i++){
			Point3f pt = new Point3f();
			geom.getCoordinate(selectedNodes[i], pt);
			Vector3f v = new Vector3f(pt);
			Transform3D t3d = new Transform3D();
			t3d.setTranslation(v);
			TransformGroup tg = new TransformGroup(t3d);
			tg.addChild(new Sphere(2f));
			spheres.addChild(tg);
			}
		
		targetPanel.currentScene.addChild(spheres);
		
	}
	
}