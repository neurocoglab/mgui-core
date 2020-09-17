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
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.LineAttributes;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector2f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Box3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Rect2D;
import mgui.geometry.Sphere3D;
import mgui.geometry.Vector3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiNumber;
import mgui.util.Colours;


public class VectorSet3DInt extends Shape3DInt {

	protected BranchGroup scene_group;
	protected Appearance fill_appearance, edge_appearance;
	
	public ArrayList<Vector3DInt> vectors = new ArrayList<Vector3DInt>();
	public HashMap<String, Integer> data_sizes = new HashMap<String, Integer>();
	
	public VectorSet3DInt(){
		init();
	}
	
	public VectorSet3DInt(String name){
		init();
		setName(name);
	}
	
	@Override
	protected void init(){
		super.init();
		Vector3DInt vector = new Vector3DInt();
		attributes.setUnion(vector.getAttributes());
		attributes.add(new Attribute("ShowLabels", new MguiBoolean(true)));
		attributes.add(new Attribute("RotateLabels", new MguiBoolean(true)));
		attributes.add(new Attribute("LabelFont", new Font("Courier New", Font.PLAIN, 10)));
		attributes.add(new Attribute("LabelColour", Color.black));
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/vector_3d_set_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/vector_3d_set_20.png");
	}
	
	public ArrayList<Vector3DInt> getVectors(){
		return new ArrayList<Vector3DInt>(vectors);
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		
		if (e.getAttribute().getName().contains("Label")){
			fireShapeModified();
			updateChildren2D(e.getAttribute());
			return;
			}
		
		if (e.getAttribute().getName().equals("ArrowScale")){
			
			}
		
		if (e.getAttribute().getName().equals("StartPtScale")){
		
			}
		
		for (int i = 0; i < vectors.size(); i++)
			vectors.get(i).attributeUpdated(e);
		
		if (e.getAttribute().getName().startsWith("DataLine")){
			//3D shape currently doesn't display data, so don't regen 
			fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.AttributeModified));
			updateChildren2D(e.getAttribute());
		}else{
			super.attributeUpdated(e);
			}
	}
	
	public float getArrowScale(){
		return ((MguiFloat)attributes.getValue("ArrowScale")).getFloat();
	}
	
	public float getStartPtScale(){
		return ((MguiFloat)attributes.getValue("StartPtScale")).getFloat();
	}
	
	protected void updateArrowScale(){
		
		float scale = getArrowScale();
		for (int i = 0; i < vectors.size(); i++)
			vectors.get(i).setArrowScale(scale);
		fireShapeModified();
	}
	
	protected void updateStartPtScale(){
		float scale = getStartPtScale();
		for (int i = 0; i < vectors.size(); i++)
			vectors.get(i).setStartPtScale(scale);
		fireShapeModified();
	}
	
	@Override
	protected void updateChildren2D(Attribute a){
		ShapeSet3DInt parent = (ShapeSet3DInt)this.getParentSet();
		if (parent == null)
			parent = new ShapeSet3DInt();
		ShapeEvent event = new ShapeEvent(parent, ShapeEvent.EventType.AttributeModified);
		for (int i = 0; i < children2D.size(); i++){
			
			ShapeSet2DInt set = (ShapeSet2DInt)children2D.get(i);
			set.fireShapeListeners(event);
			}
	}
	
	public void addVector(Vector3D vector){
		addVector(vector, true, true);
	}
	
	public void addVector(Vector3D vector, boolean updateShape, boolean updateListeners){
		Vector3DInt v = new Vector3DInt(vector);
		addVector(v, updateShape, updateListeners);
	}
	
	public void addVector(Shape3DInt thisShape, boolean updateShape, boolean updateListeners){
		//TODO throw exception
		if (!(thisShape instanceof Vector3DInt)) return;
				
		Vector3DInt vector = (Vector3DInt)thisShape;
		vector.setAttributes(attributes);
		attributes.removeAttributeListener(vector);
		vector.setVectorSet(this, vectors.size());
		
		vectors.add(vector);
		if (updateShape){
			thisShape.updateShape();
			updateShape();
			if (scene3DObject == null)
				setScene3DObject();
			else{
				setEdgeAppearance();
				vector.getShapeSceneNode().detach();
				scene_group.addChild(vector.getShapeSceneNode());
				}
			}
		
		if (updateListeners){
			ShapeEvent e = new ShapeEvent(this, ShapeEvent.EventType.ShapeModified);
			fireShapeListeners(e);
			}
	}
	
	@Override
	public boolean addVertexData(String key, ArrayList<MguiNumber> data, NameMap map){
		if (!super.addVertexData(key, data, map)) return false;
		int size = data.size() / vectors.size();
		data_sizes.put(key, size);
		return true;
	}
	
	public ArrayList<ArrayList<MguiNumber>> getDataForVector(int index){
		
		ArrayList<ArrayList<MguiNumber>> data = new ArrayList<ArrayList<MguiNumber>>();
		ArrayList<String> columns = this.getVertexDataColumnNames();
		
		for (int i = 0; i < columns.size(); i++)
			data.add(getDataForVector(columns.get(i), index));
		
		return data;
	}
	
	public ArrayList<MguiNumber> getDataForVector(String key, int index){
		
		ArrayList<MguiNumber> data = getVertexData(key);
		if (data == null) return null;
		
		Integer size = data_sizes.get(key);
		if (size == null) return null;
		
		int start = index * size;
		
		ArrayList<MguiNumber> sublist = new ArrayList<MguiNumber>(data.subList(start, start + size));
		return sublist;
		
	}
	
	public boolean getShowLabels(){
		return ((MguiBoolean)attributes.getValue("ShowLabels")).getTrue();
	}
	
	@Override
	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist){
		
		if (!isVisible()) return null;
		
		ShapeSet2DInt set = new ShapeSet2DInt();
		
		for (int i = 0; i < vectors.size(); i++){
			Vector3DInt vector = vectors.get(i);
			Vector2DInt shape2D = (Vector2DInt)vector.getShape2D(plane, above_dist, below_dist); 
			if (shape2D != null){
				set.addShape(shape2D);
				if (getShowLabels()){
					Font label_font = (Font)attributes.getValue("LabelFont");
					Color label_colour = (Color)attributes.getValue("LabelColour");
					Point2f pt2 = shape2D.getVector().getEndPoint();
					//TODO: label with data also?
					String label_text = "" + i;
					Vector2f base = null;
					
					if (((MguiBoolean)attributes.getValue("RotateLabels")).getTrue())
						base = shape2D.getVector().getVector();
					else
						base = new Vector2f(1,0);
					
					//base.normalize();
					//base.scale(label_text.length());
					Vector2f diag = GeometryFunctions.getRotatedVector2D(base, (float)Math.PI/2);
					diag.add(base);
					pt2.add(diag);
					Rect2D text_bounds = new Rect2D(shape2D.getVector().getEndPoint(), pt2, base);
					Text2DInt label = new Text2DInt(label_text, text_bounds);
					label.setFont(label_font);
					label.setLabelColour(label_colour);
					set.addShape(label);
					}
				}
			}
		
		if (set.getSize() == 0) return null;
		return set;
	}
	
	public int getSize(){
		return vectors.size();
	}
	
	//creates a new scene3D object
	@Override
	public void setScene3DObject(boolean make_live){
		
		super.setScene3DObject(false);
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
		for (Vector3DInt vector : vectors){
			vector.getShapeSceneNode().detach();
			vector.setScene3DObject();
			scene_group.addChild(vector.getShapeSceneNode());
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
		m.setSpecularColor(Colours.getColor3f(colour));
		m.setDiffuseColor(Colours.getColor3f(colour));
		
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
		
		Color3f edgeColour = Colours.getColor3f((Color)attributes.getValue("3D.LineColour"));
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
	
	
	
	@Override
	public void updateShape(){
		
		if (vectors.size() == 0){
			boundSphere = new Sphere3D(new Point3f(0,0,0), 0.1f);
			boundBox = null;
			centerPt = new Point3f(0,0,0);
			setBoundBoxNode();
			return;
		}
		
		Point3f min_pt = new Point3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		Point3f max_pt = new Point3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		
		for (Vector3DInt vector : vectors){
			float[] coords = vector.getVector().getCoords();
			for (int j = 0; j < 2 ; j++){
				min_pt.x = Math.min(min_pt.x, coords[j * 3]);
				min_pt.y = Math.min(min_pt.y, coords[(j * 3) + 1]);
				min_pt.z = Math.min(min_pt.z, coords[(j * 3) + 2]);
				max_pt.x = Math.max(max_pt.x, coords[j * 3]);
				max_pt.y = Math.max(max_pt.y, coords[(j * 3) + 1]);
				max_pt.z = Math.max(max_pt.z, coords[(j * 3) + 2]);
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
		return getName() + "(n = " + vectors.size() + ")";
	}
	
	
	
}