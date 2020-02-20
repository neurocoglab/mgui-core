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

package mgui.interfaces.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import mgui.geometry.Box3D;
import mgui.geometry.PointSet3D;
import mgui.geometry.Polygon3D;
import mgui.geometry.Shape;
import mgui.geometry.Sphere3D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;

/****************************************************
 * Interface for a set of 3D polygons.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PolygonSet3DInt extends Shape3DInt {

	protected BranchGroup scene_group;
	protected Appearance fill_appearance, edge_appearance;
	
	public ArrayList<LPolygon3DInt> polygons = new ArrayList<LPolygon3DInt>();
	
	public PolygonSet3DInt(){
		init();
	}
	
	@Override
	public Shape getGeometryInstance(){
		return new Polygon3D();
	}
	
	@Override
	protected void init(){
		super.init();
		Polygon3DInt poly = new Polygon3DInt();
		attributes = poly.getAttributes();
		attributes.removeAttributeListener(poly);
		attributes.addAttributeListener(this);
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		if (!notifyListeners) return;
		
		Attribute a = e.getAttribute();
		
		if (a.getName() == "3D.ShowBounds" ||
			a.getName() == "3D.BoundsColour"){
				setBoundBoxNode();
			}
		
		if (needsRedraw(e.getAttribute()) && scene3DObject != null)
			setScene3DObject();
		
		if (needsRedraw(e.getAttribute()))
			fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.AttributeModified));
		
		updateChildren2D(a);
	}
	
	public void addShape(Polygon3D poly){
		addShape(poly, true, true);
	}
	
	public void addShape(Polygon3D poly, boolean updateShape, boolean updateListeners){
		LPolygon3DInt l_poly = new LPolygon3DInt(poly);
		addShape(l_poly, updateShape, updateListeners);
	}
	
	public void addShape(Shape3DInt thisShape, boolean updateShape, boolean updateListeners){
		//TODO throw exception
		if (!(thisShape instanceof LPolygon3DInt)) return;
				
		LPolygon3DInt poly = (LPolygon3DInt)thisShape;
		poly.setAttributes(attributes);
		
		polygons.add(poly);
		if (updateShape){
			thisShape.updateShape();
			updateShape();
			if (scene3DObject == null)
				setScene3DObject();
			else{
				setEdgeAppearance();
				scene_group.addChild(getPolygonNode(poly, edge_appearance));
				}
				
			}
		
		if (updateListeners){
			ShapeEvent e = new ShapeEvent(this, ShapeEvent.EventType.ShapeModified);
			fireShapeListeners(e);
			}
	}
	
	public int getSize(){
		return polygons.size();
	}
	
	//creates a new scene3D object
	@Override
	public void setScene3DObject(boolean make_live){
		
		super.setScene3DObject(false);
		if (scene3DObject == null) return;
		if (!this.isVisible() || !this.show3D()){
			if (make_live) setShapeSceneNode();
			return;
			}
		
		if (scene_group != null)
			scene_group.removeAllChildren();
		else{
			scene_group = new BranchGroup();
			scene_group.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			scene_group.setCapability(Group.ALLOW_CHILDREN_WRITE);
			scene_group.setCapability(BranchGroup.ALLOW_DETACH);
			}
		
		if (!ShapeFunctions.nodeHasChild(scene3DObject, scene_group))
			scene3DObject.addChild(scene_group);
		
		setFillAppearance();
		setEdgeAppearance();
		
		//add nodes for each member object
		for (LPolygon3DInt polygon : polygons){
			BranchGroup bg = getPolygonNode(polygon, edge_appearance);
			scene_group.addChild(bg);
			}
		
		if (make_live) setShapeSceneNode();
	}
	
	public boolean needsRedraw3D(Attribute a){
		if (a.getName().equals("Show2D") ||
			a.getName().equals("Name") ||
			a.getName().equals("ShowBounds3D") ||
			a.getName().equals("BoundsColour")) return false;
		return true;
	}
	
	protected void setFillAppearance(){
		if (fill_appearance == null){
			fill_appearance = new Appearance();
			fill_appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
			}
		
		Color colour = (Color)attributes.getValue("3D.LineColour");
		
		//turn off back culling
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		pAtt.setBackFaceNormalFlip(false);
		fill_appearance.setPolygonAttributes(pAtt);
		Material m = new Material();
		
		m.setShininess(90f);
		m.setSpecularColor(new Color3f(colour));
		m.setDiffuseColor(new Color3f(colour));
		
		fill_appearance.setMaterial(m);
		
		if (((MguiBoolean)attributes.getValue("3D.HasAlpha")).getTrue()){
			TransparencyAttributes ta = new TransparencyAttributes();
			ta.setTransparency(((MguiFloat)attributes.getValue("3D.Alpha")).getFloat());
			ta.setTransparencyMode(TransparencyAttributes.BLENDED);
			ta.setSrcBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
			fill_appearance.setTransparencyAttributes(ta);
		}else{
			fill_appearance.setTransparencyAttributes(null);
			}
		
	}
	
	protected void setEdgeAppearance(){
		if (edge_appearance == null){
			edge_appearance = new Appearance();
			edge_appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
			edge_appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
			edge_appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
			edge_appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
			}
		
		Color3f edgeColour = new Color3f((Color)attributes.getValue("3D.LineColour"));
		Material m = new Material();
		m.setDiffuseColor(edgeColour);
		m.setAmbientColor(edgeColour);
		m.setLightingEnable(true);
		
		ColoringAttributes cAtt = new ColoringAttributes();
		cAtt.setColor(edgeColour);
		
		//TODO: set dash
		float width = ((BasicStroke)attributes.getValue("3D.LineStyle")).getLineWidth();
		LineAttributes lAtt = new LineAttributes(width,
												 LineAttributes.PATTERN_SOLID,
												 true);
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		pAtt.setBackFaceNormalFlip(true);
		
		edge_appearance.setLineAttributes(lAtt);
		edge_appearance.setColoringAttributes(cAtt);
		edge_appearance.setPolygonAttributes(pAtt);
		edge_appearance.setMaterial(m);
		
	}
	
	public BranchGroup getPolygonNode(LPolygon3DInt polygon, Appearance app){
		
		return polygon.getScene3DObject();
		
//		Polygon3D poly3d = polygon.getPolygon();
//		int n = poly3d.n;
//		
//		float[] nodes = poly3d.nodes;
//	    int[] lineStrip = new int[1];
//	    BranchGroup bg = new BranchGroup();
//	    bg.setCapability(BranchGroup.ALLOW_DETACH);
//	    
//	    if (((MguiBoolean)attributes.getValue("3D.AsCylinder")).getTrue()){
//			//render as cylinder
//			BranchGroup cyl = ShapeFunctions.getCylinderPolygon(polygon.getPolygon(), 
//																((MguiFloat)attributes.getValue("3D.CylRadius")).getFloat(), 
//																((MguiInteger)attributes.getValue("3D.CylEdges")).getInt(),
//																fill_appearance);
//			//cyl.setCapability(BranchGroup.ALLOW_DETACH);
//			bg.addChild(cyl);
//		}else{
//			lineStrip[0] = n;
//			
//			LineStripArray polyArray = new LineStripArray(n,
//														  GeometryArray.COORDINATES | GeometryArray.BY_REFERENCE,
//														  lineStrip);
//			
//			polyArray.setCoordRefFloat(nodes);
//			polyArray.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
//		    polyArray.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
//		    bg.addChild(new Shape3D(polyArray, app));
//			}
//	    
//	    if (showVertices()){
//	    	PointArray p_array = new PointArray(n, GeometryArray.COORDINATES);
//			p_array.setCoordinates(0, nodes);
//			Appearance p_app = new Appearance();
//
//		    // enlarge the points
//			p_app.setPointAttributes(new PointAttributes(getVertexScale(), true));
//			p_app.setColoringAttributes(new ColoringAttributes(new Color3f(getVertexColour()), ColoringAttributes.FASTEST));
//			bg.addChild(new Shape3D(p_array, p_app));
//			}
//		
//		
//	    return bg;
		
	}
	
	@Override
	public void updateShape(){
		
		if (polygons.size() == 0){
			boundSphere = new Sphere3D(new Point3f(0,0,0), 0.1f);
			boundBox = null;
			centerPt = new Point3f(0,0,0);
			setBoundBoxNode();
			return;
		}
		
		Point3f min_pt = new Point3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Point3f max_pt = new Point3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		
		for (int i = 0; i < polygons.size(); i++){
			Polygon3D poly = (polygons.get(i)).getPolygon();
			for (int j = 0; j < poly.n; j++){
				min_pt.x = Math.min(min_pt.x, poly.nodes[j * 3]);
				min_pt.y = Math.min(min_pt.y, poly.nodes[(j * 3) + 1]);
				min_pt.z = Math.min(min_pt.z, poly.nodes[(j * 3) + 2]);
				max_pt.x = Math.max(max_pt.x, poly.nodes[j * 3]);
				max_pt.y = Math.max(max_pt.y, poly.nodes[(j * 3) + 1]);
				max_pt.z = Math.max(max_pt.z, poly.nodes[(j * 3) + 2]);
				}
			}
		
		max_pt.sub(min_pt);
		this.boundBox = new Box3D(min_pt, 
								  new Vector3f(max_pt.x, 0, 0),
								  new Vector3f(0, max_pt.y, 0),
								  new Vector3f(0, 0, max_pt.z));
		
		if (centerPt == null) centerPt = new Point3f();
		centerPt = new Point3f(min_pt);
		max_pt.scale(0.5f);
		centerPt.add(max_pt);
		
		boundSphere = new Sphere3D(centerPt, centerPt.distance(boundBox.getBasePt()));
		setBoundBoxNode();
		
	}
	
	/*
	public void setTreeNode(InterfaceTreeNode treeNode){
		setSuperTreeNode(treeNode);

	}
	*/

	
	@Override
	public String toString(){
		return getName() + "(n = " + polygons.size() + ")";
	}
	
}