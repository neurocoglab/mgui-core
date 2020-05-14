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

import java.awt.Color;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

import org.apache.commons.collections15.Transformer;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PointArray;
import org.jogamp.java3d.PointAttributes;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Plane3D;
import mgui.geometry.PointSet3D;
import mgui.geometry.Shape;
import mgui.geometry.util.GeometryFunctions;
import mgui.geometry.util.NodeShape;
import mgui.geometry.util.NodeShapeComboRenderer;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeSelectionMap;
import mgui.interfaces.attributes.AttributeSelectionMap.ComboMode;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.shapes.util.ShapeVertexObject;
import mgui.interfaces.xml.XMLFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.numbers.MguiShort;
import mgui.util.Colour;
import mgui.util.Colours;

/***************************************************
 * Shape interface for a set of 3D points.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PointSet3DInt extends Shape3DInt {

	Shape3DInt me;
	
	public PointSet3DInt(){
		init2();
	}
	
	public PointSet3DInt(PointSet3D set){
		this(set, "no-name");
		init2();
	}
	
	public PointSet3DInt(PointSet3D set, String name){
		setShape(set);
		init2();
		setName(name);
	}
	
	// ------------ VERTEX TRANSFORMER -----------------
	Transformer<Integer,Node> vertex_transformer = new Transformer<Integer,Node>() {

		public Node transform(Integer index) {
			MguiBoolean show = (MguiBoolean)attributes.getValue("3D.ShowVertices");
			if (!show.getTrue())
				return null;
			Sphere sphere = new Sphere(getVertexScale(index), 
									   Sphere.GENERATE_NORMALS | 
									   Sphere.ENABLE_GEOMETRY_PICKING |
									   Sphere.ENABLE_APPEARANCE_MODIFY, 
									   getVertexAppearance(index));
			sphere.getShape().setUserData(new ShapeVertexObject(me, index));
			sphere.setCapability(Primitive.ENABLE_APPEARANCE_MODIFY);
			TransformGroup tg = new TransformGroup();
			Transform3D pos = new Transform3D();
			pos.setTranslation(new Vector3f(me.getVertex(index)));
			tg.setTransform(pos);
			tg.addChild(sphere);
			return tg;
		}};
	
	protected Appearance getVertexAppearance(int index){
		//if (vertex_appearance == null)
		Appearance appearance = new Appearance();
		Color3f colour = Colours.getColor3f(getVertexColour(index));
		Material m = new Material();
		float shininess = ((MguiFloat)attributes.getValue("3D.Shininess")).getFloat();
		m.setShininess(shininess * 127f + 1);
		m.setSpecularColor(colour);
		m.setDiffuseColor(colour);
		appearance.setMaterial(m);
		
		if (((MguiBoolean)attributes.getValue("3D.HasAlpha")).getTrue()){
			String trans_type = (String)attributes.getValue("3D.AlphaMode");
			TransparencyAttributes ta = new TransparencyAttributes();
			ta.setTransparency(1-this.getVertexAlpha(index));
			if (trans_type.equals("Screen Door")){
				ta.setTransparencyMode(TransparencyAttributes.SCREEN_DOOR);
			}else if (trans_type.equals("Fastest")){
				ta.setTransparencyMode(TransparencyAttributes.FASTEST);
			}else{
				ta.setTransparencyMode(TransparencyAttributes.NICEST);
				}
			ta.setSrcBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
			appearance.setTransparencyAttributes(ta);
		}else{
			appearance.setTransparencyAttributes(null);
			}
		
		return appearance;
	}
	
	public Color getVertexColour(int index){
		if (!this.showData())
			return getVertexColour();
		String column = getCurrentColumn();
		if (column == null) return getVertexColour();
		ColourMap cmap = getColourMap(column);
		if (cmap == null) return getVertexColour();
		VertexDataColumn vcolumn = this.getVertexDataColumn(column);
		float value = (float)vcolumn.getDoubleValueAtVertex(index); //(float)((MguiNumber)getDatumAtVertex(column, index)).getValue();
		
		Colour clr = cmap.getColour(value, vcolumn.getColourMin(), vcolumn.getColourMax());
		return clr.getColor();
	}
	
	protected float getVertexAlpha(int index) {
		if (!this.showData())
			return getAlpha();
		String column = getCurrentColumn();
		if (column == null) return getAlpha();
		ColourMap cmap = getColourMap(column);
		if (cmap == null) return getAlpha();
		VertexDataColumn vcolumn = this.getVertexDataColumn(column);
		float value = (float)vcolumn.getDoubleValueAtVertex(index);
		Colour clr = cmap.getColour(value, vcolumn.getColourMin(), vcolumn.getColourMax());
		return clr.getAlpha();

	}
	
	
	protected Appearance getLabelAppearance(Integer index){
		Appearance appearance = new Appearance();
		Color3f colour = Colours.getColor3f((Color)attributes.getValue("3D.LabelColour"));
		Material m = new Material();
		m.setDiffuseColor(colour);
		m.setAmbientColor(colour);
		m.setEmissiveColor(colour);
		m.setSpecularColor(colour);
		appearance.setMaterial(m);
		return appearance;
	}
	
	@Override
	public Shape getGeometryInstance(){
		return new PointSet3D();
	}
	
	private void init2(){
		super.init();
		
		me = this;
		
		HashMap<String, NodeShape> node_shapes = InterfaceEnvironment.getVertexShapes();
		AttributeSelectionMap<NodeShape> shapes = 
			new AttributeSelectionMap<NodeShape>("2D.VertexShape", node_shapes, NodeShape.class);
		shapes.setComboMode(ComboMode.AsValues);
		shapes.setComboRenderer(new NodeShapeComboRenderer());
		shapes.setComboWidth(100);
		attributes.add(shapes);
		attributes.setValue("3D.ShowVertices", new MguiBoolean(true));
		attributes.add(new Attribute<MguiBoolean>("3D.VerticesAsSpheres", new MguiBoolean(true)));
		
		updateShape();
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/point_set_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/point_set_20.png");
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		
		super.attributeUpdated(e);
		
	}
	
	public PointSet3D getPointSet(){
		return (PointSet3D)this.getShape();
	}
	
	@Override
	public void setScene3DObject(boolean make_live){
		boolean is_local_class = shape3d instanceof PointSet3D;
		super.setScene3DObject(!is_local_class);
		if (scene3DObject == null) return;
		
		//bypass if sub class is calling
		if (shape3d != null && !is_local_class){
			return;
			}
		
		if (!this.isVisible() || !this.show3D() || shape3d == null){
			if (make_live) setShapeSceneNode();
			return;
		}
		
		scene3DObject.removeAllChildren();
		
		PointSet3D point_set = (PointSet3D)shape3d;
		if (point_set.n == 0){
			setShapeSceneNode();
			return;
			}
		
		if (((MguiBoolean)attributes.getValue("3D.ShowVertices")).getTrue()){
			
			Node shape3D = null;
			
			if (((MguiBoolean)attributes.getValue("3D.VerticesAsSpheres")).getTrue()) {
				shape3D = getVerticesAsSpheres();
			}else {
				shape3D = getVerticesAsPoints();
				}
			
			BranchGroup bg = new BranchGroup();
			
			bg.setCapability(BranchGroup.ALLOW_DETACH);
			bg.addChild(shape3D);
			scene3DObject.addChild(bg);
			
			}
		
		if (make_live) setShapeSceneNode();
		
	}
	
	@Override
	public String getXML(int tab, XMLType type) {
	
		String _tab = XMLFunctions.getTab(tab);
		
		String _type = "full";
		if (type.equals(XMLType.Reference)) _type = "reference";
		
		String xml = _tab + "<" + getLocalName() + " type = '" + _type + "'>\n";
		xml = xml + attributes.getXML(tab + 1);
		
		if (type.equals(XMLType.Full))
			xml = xml + getPointSet().getXML(tab + 1);
			
		xml = xml + _tab + "</" + getLocalName() + ">\n";
		return xml;
		
	}
	
	protected org.jogamp.java3d.Shape3D getVerticesAsPoints() {
		
		PointSet3D point_set = (PointSet3D)shape3d;
		float[] nodes = point_set.nodes;
		
		if (!showData()){
			PointArray p_array = new PointArray(point_set.n, GeometryArray.COORDINATES); // | GeometryArray.BY_REFERENCE);
			p_array.setCoordinates(0, nodes);
			Appearance p_app = new Appearance();

		    // enlarge the points
			p_app.setPointAttributes(new PointAttributes(getVertexScale(), true));
			p_app.setColoringAttributes(new ColoringAttributes(Colours.getColor3f(getVertexColour()), ColoringAttributes.FASTEST));

			return new org.jogamp.java3d.Shape3D(p_array, p_app);
			
		}else{
			PointArray p_array = new PointArray(point_set.n, GeometryArray.COORDINATES | GeometryArray.COLOR_4); // | GeometryArray.BY_REFERENCE);
			//p_array.setCoordinates(0, nodes);
			ArrayList<MguiNumber> data = getCurrentVertexData();
			ColourMap cmap = getColourMap(getCurrentColumn());
			
			for (int i = 0; i < point_set.n; i++){
				p_array.setCoordinate(i, point_set.getVertex(i));
				p_array.setColor(i, cmap.getColour(data.get(i)).getColor4f());
				}
			
			Appearance p_app = new Appearance();

		    // enlarge the points
			p_app.setPointAttributes(new PointAttributes(getVertexScale(), true));

			return new org.jogamp.java3d.Shape3D(p_array, p_app);

			}
	}
	
	protected Node getVerticesAsSpheres() {
		
		BranchGroup bg = new BranchGroup();
		
		for (int i = 0; i < this.getVertexCount(); i++) {
			bg.addChild(vertex_transformer.transform(i));
			}
		
		return bg;
		
	}
	

	@Override
	public boolean needsRedraw(Attribute<?> a){
		if (!super.needsRedraw(a)) return false;
		
		if (a.getName().equals("IsVisible")) return true;
		if (a.getName().equals("3D.Show")) return true;
		if (a.getName().equals("ShowData")) return true;
		if (a.getName().equals("3D.VertexScale")) return true;
		
		if (!showData()){
			return !(a.getName().equals("CurrentData") ||
					 a.getName().equals("ColourMap") ||
					 a.getName().equals("DataMin") ||
					 a.getName().equals("DataMax"));
			}
		if (getColourMap() == null){
			return !(a.getName().equals("CurrentData") ||
					 a.getName().equals("DataMin") ||
					 a.getName().equals("DataMax"));
			}
		
		return true;
	}
	
	@Override
	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist, Matrix4d matrix){
		//TODO add attributes to allow different display types; i.e.,
		//filled vs. unfilled polygons, surface projection vs. strict intersection
		Shape2DInt shape2D = null;
		
		if (!isVisible()) return null;
		
		ColourMap cmap = getColourMap();
		
		PointSet3D point_set = getPointSet();
		
		if (matrix != null){
			point_set = (PointSet3D)point_set.clone();
			GeometryFunctions.transform(point_set, matrix);
			}
		
		if (hasData() && showData())
			shape2D = ShapeFunctions.getIntersectionPointSet(point_set, 
														   plane,
														   above_dist,
														   below_dist,
														   getVertexDataMap(),
														   cmap);
		else
			shape2D = ShapeFunctions.getIntersectionPointSet(point_set, plane, above_dist, below_dist, null, null);
		if (shape2D == null){
			InterfaceSession.log("PointSet3DInt: section has no points");
			return null;
		}
		
		shape2D.attributes.setIntersection(attributes);
		shape2D.updateShape();
		
		return shape2D;
	}
	
	
	//VariableObject stuff
	@Override
	public int[] getDimensions() {
		//dimensions are # nodes
		return new int[]{getPointSet().n};
	}
	
	
	
	@Override
	public String toString(){
		return "PointSet3d: " + getName();
	}
	
	@Override
	public String getLocalName(){
		return "PointSet3D";
	}
	
	/*********************************
	 * Appends {@code points} to this point set. If {@code points} has vertex data with the same
	 * name as this point set, transfers those values. Note that this may result in truncation if the
	 * data types do not match (e.g., mapping {@code MguiDouble} to {@code MguiInteger}).
	 * 
	 * @param points
	 */
	public void appendPoints(PointSet3DInt points){
		appendPoints(points, true);
	}
	
	/*********************************
	 * Appends {@code points} to this point set. If {@code points} has vertex data with the same
	 * name as this point set, transfers those values. Note that this may result in truncation if the
	 * data types do not match (e.g., mapping {@code MguiDouble} to {@code MguiInteger}). Name maps
	 * will also be transferred: (1) if they exist already in this shape; and (2) only for indices not
	 * already in the current name map.
	 * 
	 * @param points
	 */
	public void appendPoints(PointSet3DInt points, boolean update){
		
		PointSet3D this_pset = this.getPointSet();
		PointSet3D new_pset = points.getPointSet();
		
		ArrayList<String> new_columns = points.getVertexDataColumnNames();
		ArrayList<String> to_map = new ArrayList<String>(new_columns.size());
		
		for (int i = 0; i < new_columns.size(); i++){
			if (this.hasColumn(new_columns.get(i)))
				to_map.add(new_columns.get(i));
			}
		
		// Append vertices
		this_pset.addVertices(new_pset.getVertices());
		
		// Update data columns, mapping values if necessary
		int n_new = points.getVertexCount();
		ArrayList<String> these_columns = getVertexDataColumnNames();
		for (int i = 0; i < these_columns.size(); i++){
			String column = these_columns.get(i);
			VertexDataColumn v_column = this.getVertexDataColumn(column);
			ArrayList<MguiNumber> values = v_column.getData();
			ArrayList<MguiNumber> new_values = null;
			NameMap this_nmap = v_column.getNameMap();
			
			if (points.hasColumn(column)){
				new_values = points.getVertexData(column);
				
				// Try to transfer name map
				if (this_nmap != null && points.getVertexDataColumn(column).hasNameMap()){
					NameMap new_nmap = points.getVertexDataColumn(column).getNameMap();
					ArrayList<Integer> indices = new_nmap.getIndices();
					for (int j = 0; j < indices.size(); j++){
						int idx = indices.get(j);
						if (this_nmap.get(idx) == null){
							this_nmap.add(idx, new_nmap.get(idx));
							}
						}
					}
				
				}
				
			int type = v_column.getDataTransferType();
			
			for (int j = 0; j < n_new; j++){
				double value = 0;
				if (new_values != null)
					value = new_values.get(j).getValue();
				switch (type){
					case DataBuffer.TYPE_INT:
						values.add(new MguiInteger(value));
						break;
					case DataBuffer.TYPE_SHORT:
					case DataBuffer.TYPE_USHORT:
						values.add(new MguiShort(value));
						break;
					case DataBuffer.TYPE_FLOAT:
						values.add(new MguiFloat(value));
						break;
					case DataBuffer.TYPE_DOUBLE:
						values.add(new MguiDouble(value));
						break;
					}
				}
			
			}
		
		if (update){
			this.fireShapeModified();
			}
		
	}
	
}