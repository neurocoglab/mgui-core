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
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.IndexedTriangleArray;
import org.jogamp.java3d.LineAttributes;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.geometry.GeometryInfo;
import org.jogamp.java3d.utils.geometry.NormalGenerator;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Color4f;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Shape;
import mgui.geometry.Shape3D;
import mgui.geometry.Vector3D;
import mgui.geometry.mesh.MeshFunctions;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.xml.XMLFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiNumber;
import mgui.util.Colours;


/**************************************
 * Interface for a Mesh3D shape.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Mesh3DInt extends PointSet3DInt {

	//scene node stuff
	protected Appearance fill_appearance, edge_appearance;
	
	
	public Mesh3DInt(){
		this (new Mesh3D(), "no-name");
	}
	
	public Mesh3DInt(Mesh3D mesh){
		this(mesh, "no-name");
	}
	
	public Mesh3DInt(Mesh3D mesh, String name){
		mesh.finalize();
		setMesh(mesh);
		init2();
		setName(name);
	}
	
	private void init2(){
		super.init();
		//set up attributes here
		attributes.add(new Attribute<MguiBoolean>("3D.HasFill", new MguiBoolean(true)));
		attributes.add(new Attribute<Color>("3D.FillColour", new Color(153, 153, 153)));
		attributes.add(new Attribute<Color>("2D.LineColour", Color.blue));
		attributes.add(new Attribute<MguiBoolean>("2D.ShowEdges", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("3D.ShowEdges", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiFloat>("3D.EdgeWidth", new MguiFloat(2.0f)));
		attributes.add(new Attribute<MguiFloat>("2D.EdgeWidth", new MguiFloat(2.0f)));
		attributes.add(new Attribute<Stroke>("2D.LineStyle", new BasicStroke(2.0f)));
		attributes.add(new Attribute<MguiBoolean>("3D.FlipNormals", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiBoolean>("3D.BackFlip", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiFloat>("3D.Offset", new MguiFloat(0f)));
		attributes.add(new Attribute<MguiFloat>("3D.OffsetFactor", new MguiFloat(0f)));
		attributes.add(new Attribute<MguiBoolean>("3D.ShowNormals", new MguiBoolean(false)));
		attributes.add(new Attribute<Color>("3D.NormalColour", Color.blue));
		
 		updateShape();
		
	}
	
	/*******************************
	 * Returns this shape's fill colour
	 * 
	 * @return
	 */
	public Color getFillColour(){
		return ((Color)attributes.getValue("3D.FillColour"));
	}
	
	@Override
	public Shape getGeometryInstance(){
		return new Mesh3D();
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/mesh_3d_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/mesh_3d_20.png");
	}
	
	public String getAlphaMode(){
		return (String)attributes.getValue("3D.AlphaMode");
	}
	
	public boolean getFlipNormals(){
		return ((MguiBoolean)attributes.getValue("3D.FlipNormals")).getTrue();
	}
	
	public void setFlipNormals(boolean b){
		attributes.setValue("FlipNormals", new MguiBoolean(b));
	}
	
	public boolean getBackFlip(){
		return ((MguiBoolean)attributes.getValue("3D.BackFlip")).getTrue();
	}
	
	public void setBackFlip(boolean b){
		attributes.setValue("3D.BackFlip", new MguiBoolean(b));
	}
	
	@Override
	public void setShape(Shape3D shape){
		super.setShape(shape);
		((Mesh3D)shape).finalize();
	}
	
	/***************
	 * return an IndexedTriangleArray object representing this mesh. Mesh must be
	 * finalized with Mesh3D.finalize() 
	 */
	@Override
	public void setScene3DObject(boolean make_live){
		super.setScene3DObject(false);
		if (scene3DObject == null) return;
		if (!this.isVisible() || !this.show3D() || shape3d == null){
			if (make_live) setShapeSceneNode();
			return;
		}
		
		AttributeList attributes = getInheritedAttributes();
		
		Mesh3D mesh = (Mesh3D)shape3d;
		if (mesh.n == 0 || mesh.f == 0){
			setShapeSceneNode();
			return;
			}
		
		if (group_node != null){
			group_node.removeAllChildren();
			// Add subnodes if they exist
			if (this.selected_vertices_group != null) group_node.addChild(selected_vertices_group);
			if (this.vertices_group != null) group_node.addChild(vertices_group);
		}else{
			group_node = new BranchGroup();
			group_node.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			group_node.setCapability(Group.ALLOW_CHILDREN_WRITE);
			group_node.setCapability(BranchGroup.ALLOW_DETACH);
			}
		
		if (!ShapeFunctions.nodeHasChild(scene3DObject, group_node))
			scene3DObject.addChild(group_node);
		
		int[] indices = mesh.faces;
		Point3f[] nodes = new Point3f[mesh.n];
		mesh.getVertices().toArray(nodes);
		
		//generate normals for shading
		GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
        gi.setCoordinates(nodes);
        gi.setCoordinateIndices(indices);
       
        VertexDataColumn v_column = this.getCurrentDataColumn();
        
        //set colours if data exists and ShowData is true
        if (((MguiBoolean)attributes.getValue("ShowData")).getTrue() &&
        				v_column != null &&
        				v_column.getColourMap() != null){
        	ArrayList<MguiNumber> currentData = v_column.getData();
            ColourMap colourMap = v_column.getColourMap();
        	//make colour array and send it to GeometryInfo
        	//TODO: allow for choice of colour format (currently 4)
        	Color4f[] colours = colourMap.getColor4fArray(currentData, 
        												  v_column.getColourMin(), 
        												  v_column.getColourMax());
        	gi.setColors(colours);
        	gi.setColorIndices(indices);
        	}
        
        NormalGenerator ng = new NormalGenerator(Math.PI);
        ng.generateNormals(gi);
        
        if (getFlipNormals())
        	gi.setNormals(MeshFunctions.flipNormals(gi.getNormals()));
        
        //show normals
        if (((MguiBoolean)attributes.getValue("3D.ShowNormals")).getTrue()){
        	VectorSet3DInt v_set = new VectorSet3DInt();
        	Vector3f[] normals = gi.getNormals();
        	
        	v_set.setAttribute("LineColour", attributes.getValue("3D.NormalColour"));
        	v_set.setAttribute("ShowArrow", new MguiBoolean(false));
        	
        	for (int i = 0; i < mesh.n; i++){
        		v_set.addVector(new Vector3D(mesh.getVertex(i), normals[i]), false, false);
        		}
        	
        	v_set.isAuxiliaryShape(true);
        	BranchGroup bg = v_set.getScene3DObject();
        	bg.detach();
        	group_node.addChild(bg);
        	}
        
        setFillAppearance();
        
        boolean has_fill = ((MguiBoolean)attributes.getValue("3D.HasFill")).getTrue();
        boolean show_edges = ((MguiBoolean)attributes.getValue("3D.ShowEdges")).getTrue();
        
        //show fill if selected
        if (has_fill){
        	//renders properly
            IndexedTriangleArray triArray = (IndexedTriangleArray)gi.getIndexedGeometryArray(false, true, false, false, false);
            
	        //add geometry to shape node
	        org.jogamp.java3d.Shape3D fillShapeNode = new org.jogamp.java3d.Shape3D(triArray);
	        
	        
		
			//apply appearance settings
			fillShapeNode.setAppearance(fill_appearance);
			fillShapeNode.setUserData(this);
			BranchGroup bg = new BranchGroup();
			bg.setCapability(BranchGroup.ALLOW_DETACH);
			bg.addChild(fillShapeNode);
			group_node.addChild(bg);
        }
        
		//show edges if selected
		if (show_edges){
			IndexedTriangleArray edgeArray = new IndexedTriangleArray(nodes.length,
																	 GeometryArray.COORDINATES |
																	 GeometryArray.NORMALS,
																	 indices.length);
			
			//set geometry
			edgeArray.setCoordinates(0, nodes);
			edgeArray.setCoordinateIndices(0, indices);
			org.jogamp.java3d.Shape3D edgeShapeNode = new org.jogamp.java3d.Shape3D(edgeArray);
			
			setEdgeAppearance();
			
			
			
			edgeShapeNode.setAppearance(edge_appearance);
			edgeShapeNode.setUserData(this);
			edgeShapeNode.setPickable(!has_fill);
			BranchGroup bg = new BranchGroup();
			bg.setCapability(BranchGroup.ALLOW_DETACH);
			bg.addChild(edgeShapeNode);
			group_node.addChild(bg);
			}
		
		//node scaling
		float scale = Math.max(getMesh().getBoundBox().getRDim(),
							   getMesh().getBoundBox().getSDim())
							   * ((MguiFloat)attributes.getValue("3D.VertexScale")).getFloat() / 100f;
		
		//show nodes if selected
		
		// Show vertices, if desired
		updateVertices();
		
		// Show selected vertices, if desired
		updateSelectedVertices();
		
		//show constraint nodes with spheres, if selected
		if (((MguiBoolean)attributes.getValue("3D.ShowConstraints")).getTrue()){
			Sphere thisNode;
			Transform3D transform;
			TransformGroup tg;
			BranchGroup constGroup = new BranchGroup();
			Material m = new Material();
			m.setDiffuseColor(Colours.getColor3f(Color.RED));
			Appearance app = new Appearance();
			app.setMaterial(m);
			for (int i = 0; i < constraints.length; i++)
				if (constraints[i]){
					thisNode = new Sphere(2 * scale, app);
					transform = new Transform3D();
					transform.set(new Vector3f(getMesh().getVertex(i)));
					tg = new TransformGroup(transform);
					tg.addChild(thisNode);
					constGroup.addChild(tg);
					}
			
			constGroup.setCapability(BranchGroup.ALLOW_DETACH);
			group_node.addChild(constGroup);
			}
		
		if (make_live) setShapeSceneNode();
	}	
	
	@Override
	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist, Matrix4d transform){
		//TODO add attributes to allow different display types; i.e.,
		//filled vs. unfilled polygons, surface projection vs. strict intersection
		Shape2DInt shape2D = null;
		
		if (!isVisible()) return null;
		
		VertexDataColumn v_column = this.getCurrentDataColumn();
		ColourMap cmap = null;
		if (v_column != null){
			cmap = v_column.getColourMap();
			}
		
		Mesh3D mesh = getMesh();
		
		if (transform != null){
			mesh = (Mesh3D)mesh.clone();
			GeometryFunctions.transform(mesh, transform);
			}
		
		if (hasData() && (((MguiBoolean)attributes.getValue("ShowData")).getTrue()))
			shape2D = ShapeFunctions.getIntersectionMesh(mesh, 
														 plane, 
														 getCurrentVertexData(),
														 cmap);
		else
			shape2D = ShapeFunctions.getIntersectionMesh(mesh, plane);
		if (shape2D == null) return null;
		
//		ShapeFunctions.setAttributesFrom3DParent(shape2D, this, inheritAttributesFromParent());
		//ShapeFunctions.setAttributesFrom3DParent(shape2D, this, false); // ((MguiBoolean)attributes.getValue("IsOverriding")).getTrue());
		
		shape2D.updateShape();
		
		return shape2D;
	}
	
	@Override
	public boolean isHeritableAttribute(String name){
		if (name.equals("2D.LineStyle")){
			return true;
			}
		if (!super.isHeritableAttribute(name)) return false;
		
		if (name.equals("3D.VertexScale") ||
				name.equals("3D.LineColour") ||
				name.equals("3D.LineStyle")){
			return false;
			}
		return true;
	}
	
	@Override
	public Attribute<?> getInheritingAttribute(Attribute<?> attribute){
		
		String name = attribute.getName();
		
		// 2D line colour inherits from 3D fill colour
		if (name.equals("3D.FillColour")){
			return attributes.getAttribute("2D.LineColour");
			}
		
		return super.getInheritingAttribute(attribute);
		
	}
	
	public Mesh3D getMesh(){
		return (Mesh3D)shape3d;
	}
	
	public void setMesh(Mesh3D mesh){
		shape3d = mesh;
		constraints = new boolean[mesh.n];
		if (scene3DObject != null)
			setScene3DObject();
		fireShapeModified();
	}
	@Override
	public String toString(){
		return "Mesh3d: " + getName();
	}
	
	//AttributeListener stuff
	@Override
	public void attributeUpdated(AttributeEvent e){
		//update attributes
		
		if (e.getAttribute().getName().contains("SelectedVert")){
			updateSelectedVertices();
			}else if
				(e.getAttribute().getName().contains("Vertex") ||
				e.getAttribute().getName().contains("Vertices")){
			updateVertices();
			updateSelectedVertices();
			}
		
		//change fill appearance?
		if ((e.getAttribute().getName().equals("3D.FillColour") ||
			 e.getAttribute().getName().equals("3D.Shininess") ||
			 e.getAttribute().getName().equals("3D.HasAlpha") ||
			 e.getAttribute().getName().equals("3D.Alpha") ||
			 e.getAttribute().getName().equals("TransType"))
				&& fill_appearance != null){
		
			setFillAppearance();
			}
		
		//change edge appearance?
		if ((e.getAttribute().getName().equals("3D.LineColour") ||
			 e.getAttribute().getName().equals("3D.LineStyle") ||
			 e.getAttribute().getName().equals("3D.EdgeWidth"))
				&& edge_appearance != null){
		
			setEdgeAppearance();
			}
		
		super.attributeUpdated(e);
		
	}
	
	protected void setFillAppearance(){
		if (fill_appearance == null){
			fill_appearance = new Appearance();
			fill_appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
			fill_appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
			}
		
		AttributeList attributes = getInheritedAttributes();
		
		Color colour = (Color)attributes.getValue("3D.FillColour");
		
		//turn off back culling
		PolygonAttributes pAtt = new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
													   PolygonAttributes.CULL_NONE,
													   getPolygonOffset(),
													   getBackFlip(),
													   getPolygonOffsetFactor());
		fill_appearance.setPolygonAttributes(pAtt);
		Material m = new Material();
		
		float shininess = ((MguiFloat)attributes.getValue("3D.Shininess")).getFloat();
		
		m.setShininess(shininess * 127f + 1);
		m.setSpecularColor(Colours.getColor3f(colour));
		m.setDiffuseColor(Colours.getColor3f(colour));
		
		fill_appearance.setMaterial(m);
		
		fill_appearance.setTransparencyAttributes(getTransparencyAttributes());
		
		
		RenderingAttributes ra = new RenderingAttributes();
		ra.setDepthBufferEnable(true);
		ra.setDepthTestFunction(RenderingAttributes.LESS_OR_EQUAL);
		
		fill_appearance.setRenderingAttributes(ra);
		
	}
	
	protected TransparencyAttributes getTransparencyAttributes() {
		
		if (!((MguiBoolean)attributes.getValue("3D.HasAlpha")).getTrue()){
			return null;
			}
		
		String trans_type = (String)attributes.getValue("3D.AlphaMode");
		TransparencyAttributes ta = new TransparencyAttributes();
		ta.setTransparency(((MguiFloat)attributes.getValue("3D.Alpha")).getFloat());
		if (trans_type.equals("Screen Door")){
			ta.setTransparencyMode(TransparencyAttributes.SCREEN_DOOR);
		}else if (trans_type.equals("Fastest")){
			ta.setTransparencyMode(TransparencyAttributes.FASTEST);
		}else if (trans_type.equals("Blended")){
			ta.setTransparencyMode(TransparencyAttributes.BLENDED);
		}else{
			ta.setTransparencyMode(TransparencyAttributes.NICEST);
			}
		
		ta.setSrcBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
		
		return ta;
		
	}
	
	protected float getPolygonOffset() {
		return (float)((MguiFloat)attributes.getValue("3D.Offset")).getValue();
	}
	
	protected float getPolygonOffsetFactor() {
		return (float)((MguiFloat)attributes.getValue("3D.OffsetFactor")).getValue();
	}
	
	protected void setEdgeAppearance(){
		if (edge_appearance == null){
			edge_appearance = new Appearance();
			edge_appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
			edge_appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
			edge_appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
			edge_appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
			edge_appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
			}
		
		AttributeList attributes = getInheritedAttributes();
		
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
		
		edge_appearance.setTransparencyAttributes(getTransparencyAttributes());
		
	}
	
	@Override
	public boolean needsRedraw(Attribute<?> a){
		if (!super.needsRedraw(a)) return false;
		
		//fill attributes
		if (a.getName().equals("3D.FillColour") ||
			a.getName().equals("3D.Shininess") ||
			a.getName().equals("3D.HasAlpha") ||
//			a.getName().equals("3D.Alpha") ||
//			a.getName().equals("3D.EdgeWidth") ||
			a.getName().equals("3D.LineColour") ||
			a.getName().equals("3D.LineStyle"))
			if (fill_appearance == null)
				return true;
			else
				return false;
		
		//edge attributes
		if (a.getName().equals("3D.EdgeWidth") ||
			a.getName().equals("3D.LineColour") ||
			a.getName().equals("3D.LineStyle"))
			if (edge_appearance == null)
				return true;
			else
				return false;
		
		return true;
	}
	
	//TreeObject stuff
	
	
	//VariableObject stuff
	@Override
	public int[] getDimensions() {
		//dimensions are # nodes
		return new int[]{getMesh().n};
	}

	//XMLObject stuff
	@Override
	public String getLocalName() {
		return "Mesh3DInt";
	}
	
	//Note: Mesh3D will not write type Full (too big to fit in a String); use writeXML instead
	@Override
	public String getXML(int tab, XMLType type) {
	
		String _tab = XMLFunctions.getTab(tab);
		
		String _type = "full";
		if (type.equals(XMLType.Reference)) _type = "reference";
		
		String xml = _tab + "<" + getLocalName() + " type = '" + _type + "'>\n";
		xml = xml + attributes.getXML(tab + 1);
		
		if (type.equals(XMLType.Full))
			xml = xml + getMesh().getXML(tab + 1);
			
		xml = xml + _tab + "</" + getLocalName() + ">\n";
		return xml;
		
	}

	//PopupMenu stuff
	@Override
	public void setGraphic3DPopupMenu(InterfacePopupMenu menu){
		super.setGraphic3DPopupMenu(menu);
		menu.addMenuItem(new JMenuItem("Flip normals"));
		
	}
	
	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		InterfaceSession.log("Handle mesh3d popup...", LoggingType.Debug);
		
		if (item.getText().equals("Flip normals")){
			setFlipNormals(!getFlipNormals());
			return;
			}
		
		super.handlePopupEvent(e);
	}
	
}