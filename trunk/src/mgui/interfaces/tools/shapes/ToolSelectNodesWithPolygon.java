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

import java.awt.Color;
import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PickInfo;
import javax.media.j3d.PickInfo.IntersectionInfo;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import mgui.geometry.Polygon2D;
import mgui.geometry.mesh.MeshFunctions;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.gui.InterfaceProgressBar;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Polygon2DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.mesh.VertexSelection;
import mgui.interfaces.shapes.util.LocalToWindow;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.ToolListener;
import mgui.interfaces.tools.graphics.Tool3D;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.pickfast.PickCanvas;
import com.sun.j3d.utils.pickfast.PickIntersection;


/********************************************
 * Tool specifies options with a dialog and then calls an inner class instance of Tool3D
 * to allow the user to select nodes on the Canvas3D. 
 * 
 * @author Andrew Reid
 *
 */
public class ToolSelectNodesWithPolygon extends Tool3D {

	//Idea is to combine the picked points with the eye point to construct a 
	//BoundingPolytope which encloses the selection.
	ArrayList<Point3d> points;
	BranchGroup spheres;
	Shape3DInt selected_shape;
	ArrayList<Integer> selected_indices;
	int last_index = -1;
	
	public ToolSelectNodesWithPolygon(){
		init();
		selected_indices = new ArrayList<Integer> ();
	}
	
	private void init(){
		name = "Select Nodes With Polygon";
		points = new ArrayList<Point3d>();
		selected_indices = new ArrayList<Integer> ();
	}
	
	@Override
	public boolean isExclusive(){
		return false;
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		
		IntersectionInfo[] infos;
		PickIntersection intersect;
		Transform3D t3d;
		PickInfo.IntersectionInfo intInfo;
		Node n;
		PickInfo infoNode;
		PickCanvas pickNode;
		PickCanvas pickGeom;
		PickInfo infoGeom;
		int index = -1;
		int[] face;
		Point3d point;
		
		switch(e.getEventType()){
			case ToolConstants.TOOL_MOUSE_CLICKED:
				
				pickNode = targetPanel.getPickCanvasNode();
				if (pickNode == null) return;
				
				pickNode.setShapeLocation(e.getPoint().x, e.getPoint().y);
				infoNode = pickNode.pickClosest();
				if (infoNode == null) return;
				n = infoNode.getNode();			
				if (n == null) return;
				Shape3DInt shape = null;
				if (n instanceof Shape3D)
					shape = ShapeFunctions.getShape((Shape3D)n);
				if (shape == null) return;
				
				if (selected_shape == null)
					selected_shape = shape;
				else
					if (shape != selected_shape) return;
				
				pickGeom = targetPanel.getPickCanvasGeom();
				if (pickGeom == null) return;
				pickGeom.setShapeLocation(e.getPoint().x, e.getPoint().y);
				infoGeom = pickGeom.pickClosest();
				if (infoGeom == null) return;
				
				t3d = new Transform3D();
				infoNode.getNode().getLocalToVworld(t3d);
				infos = infoGeom.getIntersectionInfos();
				if (infos == null) return;
				
				intInfo = infos[0];
				intersect = new PickIntersection(t3d, intInfo);
				
				index = intersect.getClosestVertexIndex();
				face = intersect.getPrimitiveCoordinateIndices();
				index = face[index];
				//if (index == null || index.length == 0) return;
				selected_indices.add(index);
				point = new Point3d(selected_shape.getShape().getVertex(index));
				
				if (point != null) points.add(point);
				displayNode(point, Color.red);
				
				last_index = index;
				
				return;
				
			case ToolConstants.TOOL_MOUSE_DCLICKED:
				
				if (points.size() > 2 && last_index > -1){
					
					//do a flood fill type thingee
					selected_shape.setSelectedVertices(selected_indices);
					
					//now fill middle if this is a mesh
					if (selected_shape instanceof Mesh3DInt){
						Mesh3DInt mesh = (Mesh3DInt)selected_shape;
						VertexSelection selection = mesh.getVertexSelection();
						InterfaceProgressBar progress = new InterfaceProgressBar("Selecting vertices: ");
						progress.register();
						MeshFunctions.selectFloodFill(mesh.getMesh(), selection, last_index, progress);
						progress.deregister();
						}
					
					selected_shape.setAttribute("ShowSelectedNodes", new MguiBoolean(true));
					}
				
				deactivate();
				
				return;
				
				case ToolConstants.TOOL_MOUSE_RCLICKED:
					//no flood filling
					if (selected_shape != null && selected_indices != null){
						selected_shape.setSelectedVertices(selected_indices);
						selected_shape.setAttribute("ShowSelectedNodes", new MguiBoolean(true));
						deactivate();
						}
					return;
				
			}
		
	
	}
	
	
	@Override
	public void deactivate(){
		targetPanel.canvas3D.clearPostRenderShapes();
		if (spheres != null) spheres.detach();
		points = new ArrayList<Point3d>();
		last_index = -1;
		if (selected_shape != null){
			selected_shape.setShowSelectedVertices(true);
			selected_shape.updateSelectedVertices();
			}
		super.deactivate();
	}
	
	@Override
	public Object clone(){
		ToolSelectNodesWithPolygon tool = new ToolSelectNodesWithPolygon();
		tool.listeners = new ArrayList<ToolListener>(listeners);
		return tool;
	}
	
	protected void renderPolygon(){
		
		if (spheres == null || points.size() < 3) return;
		
		//get a polygon shape which is the convex hull of the selected points
		LocalToWindow ltw = new LocalToWindow(spheres, targetPanel.canvas3D.getCanvas());
		
		ArrayList<Point2d> pts2d = new ArrayList<Point2d>();
		
		for (Point3d p : points){
			Point2d p2 = new Point2d();
			ltw.transformPt(p, p2);
			pts2d.add(p2);
			}
		
		Polygon2D hull = GeometryFunctions.getConvexHull(pts2d);
		targetPanel.canvas3D.clearPostRenderShapes();
		
		if (hull == null) return;
		
		Polygon2DInt hull_int = new Polygon2DInt(hull);
		hull_int.setAttribute("HasTransparency", new MguiBoolean(true));
		hull_int.setAttribute("Alpha", new MguiFloat(0.7f));
		hull_int.setAttribute("HasFill", new MguiBoolean(true));
		hull_int.setAttribute("FillColour", Color.red);
		hull_int.setAttribute("LineColour", Color.BLUE);
		targetPanel.canvas3D.addPostRenderShape(hull_int);
		
	}
	
	protected void displayNode(Point3d point, Color c){
		
		if (spheres == null) displaySelectedNodes();
		BranchGroup bg = new BranchGroup();
		bg.addChild(getSphereForPoint(point, c));
		spheres.addChild(bg);
		
	}
	
	protected void displaySelectedNodes(){
		
		if (spheres != null)
			spheres.detach();
		
		spheres = new BranchGroup();
		spheres.setCapability(BranchGroup.ALLOW_DETACH);
		spheres.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		
		for (Point3d pt : points)
			spheres.addChild(getSphereForPoint(pt, Color.red));
		
		targetPanel.getModel().addTempShape(spheres);
		
	}
	
	private TransformGroup getSphereForPoint(Point3d point, Color colour){
		
		float distance = (float)targetPanel.getCamera().getDistance();
		float node_size = distance / 300f;		//1% of camera distance..
		
		Vector3f v = new Vector3f(point);
		Transform3D t3d = new Transform3D();
		t3d.setTranslation(v);
		TransformGroup tg = new TransformGroup(t3d);
		Sphere sphere = new Sphere(node_size);
		Appearance app = new Appearance();
		Material m = new Material();
		m.setSpecularColor(new Color3f(colour));
		m.setDiffuseColor(new Color3f(colour));
		app.setMaterial(m);
		sphere.setAppearance(app);
		tg.addChild(sphere);
		
		return tg;
		
	}
	
}