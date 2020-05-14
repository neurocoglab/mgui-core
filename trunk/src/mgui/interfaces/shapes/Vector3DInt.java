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

package mgui.interfaces.shapes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BadTransformException;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.LineArray;
import org.jogamp.java3d.LineAttributes;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.geometry.Cone;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Plane3D;
import mgui.geometry.Polygon3D;
import mgui.geometry.Vector3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.util.Colours;


public class Vector3DInt extends Shape3DInt {

	protected Appearance fill_appearance, edge_appearance;
	protected BranchGroup scene_node;
	protected VectorSet3DInt vector_set;
	protected int vector_set_index = -1;
	
	public Vector3DInt(){
		shape3d = new Polygon3D();
		init();
	}
	
	public Vector3DInt(Vector3D vector){
		shape3d = vector;
		init();
	}
	
	@Override
	protected void init(){
		super.init();
		//set up attributes here
		attributes.add(new Attribute<MguiBoolean>("ShowArrow", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowStartPt", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiFloat>("StartPtScale", new MguiFloat(1f)));
		attributes.add(new Attribute<MguiFloat>("ArrowScale", new MguiFloat(1f)));
		attributes.add(new Attribute<MguiBoolean>("AsCylinder", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiFloat>("CylRadius", new MguiFloat(1.0f)));
		attributes.add(new Attribute<MguiInteger>("CylEdges", new MguiInteger(8)));
		
		attributes.add(new Attribute<Color>("DataLineColour", Color.BLACK));
		attributes.add(new Attribute<MguiFloat>("DataLineOffset", new MguiFloat(1f)));
		attributes.add(new Attribute<MguiFloat>("DataLineHeight", new MguiFloat(4f)));
		
		updateShape();
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/vector_3d_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/vector_3d_20.png");
	}
	
	public Vector3D getVector(){
		return (Vector3D)shape3d;
	}
	
	public void setVectorSet(VectorSet3DInt set, int index){
		this.vector_set = set;
		this.vector_set_index = index;
	}
	
	public VectorSet3DInt getVectorSet(){
		return this.vector_set;
	}
	
	@Override
	public ArrayList<MguiNumber> getVertexData(String key){
		if (vector_set != null) return getDataFromSet(key);
		return super.getVertexData(key);
	}
	
	protected ArrayList<MguiNumber> getDataFromSet(String key){
		if (vector_set == null) return null;
		return vector_set.getDataForVector(key, vector_set_index);
	}
	
	@Override
	public ArrayList<ArrayList<MguiNumber>> getAllVertexData(){
		if (vector_set != null) return getDataFromSet();
		return super.getAllVertexData();
	}
	
	protected ArrayList<ArrayList<MguiNumber>> getDataFromSet(){
		if (vector_set == null) return null;
		return vector_set.getDataForVector(vector_set_index);
	}
	
	@Override
	public ArrayList<MguiNumber> getCurrentVertexData(){
		if (vector_set != null) return getCurrentDataFromSet();
		return super.getCurrentVertexData();
	}
	
	protected ArrayList<MguiNumber> getCurrentDataFromSet(){
		if (vector_set == null) return null;
		String key = getCurrentColumn();
		if (key == null) return null;
		return vector_set.getDataForVector(key, vector_set_index);
	}
	
	@Override
	public double getDataMin(){
		if (vector_set != null) return getDataMinFromSet();
		return super.getDataMin();
	}
	
	@Override
	public double getDataMax(){
		if (vector_set != null) return getDataMaxFromSet();
		return super.getDataMax();
	}
	
	protected double getDataMinFromSet(){
		if (vector_set == null) return Double.NaN;
		return vector_set.getDataMin();
	}
	
	protected double getDataMaxFromSet(){
		if (vector_set == null) return Double.NaN;
		return vector_set.getDataMax();
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		
		if (vector_set != null)
			super.attributeUpdated(e);
		else
			updateChildren2D(e.getAttribute(), false);
		
	}
	
	protected void updateChildren2D(Attribute a, boolean update){
		for (int i = 0; i < children2D.size(); i++)
			children2D.get(i).attributes.setValue(a.getName(), a.getValue(), update);
	}
	
	public void setArrowScale(float scale){
		attributes.setValue("ArrowScale", new MguiFloat(scale));
	}
	
	public void setStartPtScale(float scale){
		attributes.setValue("StartPtScale", new MguiFloat(scale));
	}
	
	@Override
	public void setScene3DObject(boolean make_live){
		
		super.setScene3DObject(false);
		if (scene3DObject == null) return;
		if (!this.isVisible() || !this.show3D() || shape3d == null){
			if (make_live) setShapeSceneNode();
			return;
			}
		
		if (scene_node != null)
			scene_node.detach();
		
		scene_node = new BranchGroup();
		scene_node.setCapability(BranchGroup.ALLOW_DETACH);
		
		Vector3D vector = getVector();
		
		setEdgeAppearance();
		setFillAppearance();
		
		//draw start point
		if (((MguiBoolean)attributes.getValue("ShowStartPt")).getTrue()){
			Transform3D translate = new Transform3D();
			translate.setTranslation(new Vector3f(vector.start));
			//float radius = ((arFloat)attributes.getValue("StartPtScale")).value * vector.vector.length() * 1.25f / 25f;
			float radius = ((MguiFloat)attributes.getValue("StartPtScale")).getFloat();
			try{
	            TransformGroup tg = new TransformGroup(translate);
	            Sphere sphere = new Sphere(radius, fill_appearance);
	            tg.addChild(sphere);
	            scene_node.addChild(tg);
            }catch (BadTransformException ex){
            	//ex.printStackTrace();
            	InterfaceSession.log("Vector3DInt - Bad transform:\n" + translate.toString());
            	}
			}
		
		//draw line
		//TODO: as cylinder
		
		
		float[] coords = vector.getCoords();
		LineArray lineArray = new LineArray(2, GeometryArray.COORDINATES | GeometryArray.BY_REFERENCE);
		lineArray.setCoordRefFloat(coords);
		Shape3D shape_node = new Shape3D(lineArray);
		shape_node.setAppearance(edge_appearance);
		scene_node.addChild(shape_node);
		
		//draw arrow (thanks to Gleb Bezgin)
		if (((MguiBoolean)attributes.getValue("ShowArrow")).getTrue()){
			
			Transform3D rotate = GeometryFunctions.getVectorRotation(vector);
		    //float height = ((arFloat)attributes.getValue("ArrowScale")).value * vector.vector.length() / 25f;
			float height = ((MguiFloat)attributes.getValue("ArrowScale")).getFloat();
		    float radius = height * 1.25f / 5f;
		    Point3f location;
		    float half_height = height / 2;
		    Point3f a = new Point3f(vector.start);
		    Point3f b = new Point3f(a);
		    b.add(vector.vector);
		    float ab = (float)Math.sqrt(vector.vector.x * vector.vector.x + 
		    					  		vector.vector.y * vector.vector.y + 
		    					  		vector.vector.z * vector.vector.z);
            
		    float ratio = half_height / ab;              
		    location = new Point3f(b.x + ratio * (a.x - b.x), b.y + ratio * (a.y - b.y), b.z + ratio * (a.z - b.z));
            rotate.setTranslation(new Vector3f(location));
            try{
	            TransformGroup tg = new TransformGroup(rotate);
	            Cone cone = new Cone(radius, height, Primitive.GENERATE_NORMALS |
	                    							 Primitive.ENABLE_APPEARANCE_MODIFY |
	                                                 Primitive.ENABLE_GEOMETRY_PICKING,
	                                                 10,2, fill_appearance);
	            tg.addChild(cone);
	            scene_node.addChild(tg);
            }catch (BadTransformException ex){
            	//ex.printStackTrace();
            	InterfaceSession.log("Vector3DInt - bad transform:\n" + rotate.toString());
            	}
           
			}
		
		scene3DObject.addChild(scene_node);
		
		if (make_live) setShapeSceneNode();
		
	}
	
	
	
	@Override
	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist, Matrix4d transform){
		
		if (!isVisible()) return null;
		
		Shape2DInt shape2D = ShapeFunctions.getIntersectionVector(this, plane, above_dist, below_dist, transform);
		if (shape2D == null) return null;
		
		shape2D.attributes.setIntersection(attributes);
		float arrow_scale = ((MguiFloat)attributes.getValue("ArrowScale")).getFloat();
		float start_scale = ((MguiFloat)attributes.getValue("StartPtScale")).getFloat();
		shape2D.setAttribute("ArrowScale", new MguiFloat(arrow_scale));
		shape2D.setAttribute("StartPtScale", new MguiFloat(start_scale));
		shape2D.updateShape();
		
		return shape2D;
	}
	
	protected void setEdgeAppearance(){
		if (edge_appearance == null){
			edge_appearance = new Appearance();
			edge_appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
			edge_appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
			edge_appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
			edge_appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
			}
		
		Color3f edgeColour = Colours.getColor3f((Color)attributes.getValue("LineColour"));
		Material m = new Material();
		m.setDiffuseColor(edgeColour);
		m.setAmbientColor(edgeColour);
		m.setLightingEnable(true);
		
		ColoringAttributes cAtt = new ColoringAttributes();
		cAtt.setColor(edgeColour);
		
		//TODO: set dash
		float width = ((BasicStroke)attributes.getValue("LineStyle")).getLineWidth();
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
	
	protected void setFillAppearance(){
		if (fill_appearance == null){
			fill_appearance = new Appearance();
			fill_appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
			}
		
		Color colour = (Color)attributes.getValue("LineColour");
		
		//turn off back culling
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		pAtt.setBackFaceNormalFlip(false);
		fill_appearance.setPolygonAttributes(pAtt);
		Material m = new Material();
		
		m.setShininess(95f);
		m.setSpecularColor(Colours.getColor3f(colour));
		m.setDiffuseColor(Colours.getColor3f(colour));
		
		fill_appearance.setMaterial(m);
		
		if (((MguiBoolean)attributes.getValue("HasTransparency")).getTrue()){
			TransparencyAttributes ta = new TransparencyAttributes();
			ta.setTransparency(((MguiFloat)attributes.getValue("Alpha")).getFloat());
			ta.setTransparencyMode(TransparencyAttributes.BLENDED);
			ta.setSrcBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
			fill_appearance.setTransparencyAttributes(ta);
		}else{
			fill_appearance.setTransparencyAttributes(null);
			}
		
	}
	
}