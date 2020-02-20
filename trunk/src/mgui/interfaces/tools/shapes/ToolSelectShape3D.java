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

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PickInfo;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.graphics.Tool3D;

import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.pickfast.PickCanvas;
import com.sun.j3d.utils.pickfast.PickIntersection;

/******************************************************
 * 3D tool for selecting/deselcting shapes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ToolSelectShape3D extends Tool3D {
	
	public static final int SELECT = 0;
	public static final int DESELECT = 1;

	public ShapeSelectionSet selectionSet;
	public Point3d selectedPt;
	public Shape3D selectedShape;
	public int[] selectedNodes;
	
	public int mode = SELECT;
	
	//highlight selection
	BranchGroup spheres;
	public boolean displayNodes = true;
	
	public ToolSelectShape3D(ShapeSelectionSet set){
		this(set, SELECT);
	}
	
	public ToolSelectShape3D(ShapeSelectionSet set, int mode){
		init();
		selectionSet = set;
		setMode(mode);
	}
	
	protected void init(){
		name = "Select Shape 3D";
	}
	
	public void setMode(int m){
		mode = m;
		if (m == SELECT)
			name = "Select Shape 3D";
		else
			name = "Deselect Shape 3D";
	}
	
	public int getMode(){
		return mode;
	}
	
	@Override
	public void handleToolEvent(ToolInputEvent e){
		
		switch(e.getEventType()){
			case ToolConstants.TOOL_MOUSE_CLICKED:
				//initiate pick
				PickCanvas pickNode = targetPanel.getPickCanvasNode();
				if (pickNode == null) return;
				
				pickNode.setShapeLocation(e.getPoint().x, e.getPoint().y);
				PickInfo infoNode = pickNode.pickClosest();
				if (infoNode == null) return;
				
				selectedPt = infoNode.getClosestIntersectionPoint();
				
				Node n = infoNode.getNode();				
				if (n == null) return;
				Shape3DInt shape = null;
				if (n instanceof Shape3D)
					shape = getShape((Shape3D)n);
				if (shape == null) return;
					
				if (mode == SELECT)
					selectionSet.addShape(shape);
				else
					selectionSet.removeShape(shape);
				
				//display nearest nodes if flag set
				if (displayNodes){
					PickCanvas pickGeom = targetPanel.getPickCanvasGeom();
					if (pickGeom == null) return;
					pickGeom.setShapeLocation(e.getPoint().x, e.getPoint().y);
					PickInfo infoGeom = pickGeom.pickClosest();
					
					Transform3D t3d = new Transform3D();
					n.getLocalToVworld(t3d);
					
					PickInfo.IntersectionInfo intInfo = infoGeom.getIntersectionInfos()[0];
					Geometry geom = intInfo.getGeometry();
					PickIntersection intersect = new PickIntersection(t3d, intInfo);
					
					//selectedNodes = intersect.getPrimitiveVertexIndices();
					displaySelectedNode(intersect.getClosestVertexCoordinates());
					
					if (geom != null && geom instanceof GeometryArray) 
						displaySelectedNodes((GeometryArray)geom);
					}
				
				break;
		
			}
	}
	
	protected void displaySelectedNode(Point3d p){
		clearNodes();
	
		spheres = new BranchGroup();
		spheres.setCapability(BranchGroup.ALLOW_DETACH);
	
		float distance = (float)targetPanel.getCamera().getDistance();
		float node_size = distance / 300f;		//1% of camera distance..
		
		Vector3f v = new Vector3f(p);
		Transform3D t3d = new Transform3D();
		t3d.setTranslation(v);
		TransformGroup tg = new TransformGroup(t3d);
		Sphere sphere = new Sphere(node_size);
		Appearance app = new Appearance();
		Material m = new Material();
		m.setSpecularColor(new Color3f(Color.red));
		m.setDiffuseColor(new Color3f(Color.red));
		app.setMaterial(m);
		sphere.setAppearance(app);
		tg.addChild(sphere);
		spheres.addChild(tg);
	
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
		
		/*
		ar.geometry.Shape3D shape3d = shape.getShape();
		
		if (shape3d instanceof Mesh3D)
			for (int i = 0; i < selectedNodes.length; i++){
				Vector3f v = new Vector3f(((Mesh3D)shape3d).getNode(selectedNodes[i]));
				Transform3D t3d = new Transform3D();
				t3d.setTranslation(v);
				TransformGroup tg = new TransformGroup(t3d);
				tg.addChild(new Sphere(2f));
				spheres.addChild(tg);
				}
				*/
		
		targetPanel.currentScene.addChild(spheres);
		
	}
	
	protected Shape3DInt getShape(Shape3D shape){
		Object obj = shape.getUserData();
		if (obj instanceof Shape3DInt) return (Shape3DInt)obj;
		return null;
	}
	
	@Override
	public Object clone(){
		return new ToolSelectShape3D(selectionSet, mode);
	}
	
	
}