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

import javax.swing.ImageIcon;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.IndexedTriangleArray;
import org.jogamp.java3d.LineAttributes;
import org.jogamp.java3d.LineStripArray;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PointArray;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.geometry.GeometryInfo;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Plane3D;
import mgui.geometry.Polygon3D;
import mgui.geometry.Shape;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.util.Colours;


/****************
 * Interface for a 3D polygon object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class Polygon3DInt extends Shape3DInt {

	protected Appearance edge_appearance;
	protected Appearance cylinder_appearance;
	
	public Polygon3DInt(){
		this(new Polygon3D(), "no-name");
	}
	
	public Polygon3DInt(Polygon3D polygon){
		this(polygon,"no-name");
	}
	
	public Polygon3DInt(Polygon3D polygon, String name){
		shape3d = polygon;
		polygon.finalize();
		init();
		this.setName(name);
	}
	
	@Override
	public Shape getGeometryInstance(){
		return new Polygon3D();
	}
	
	@Override
	protected void init(){
		super.init();
		//set up attributes here
		attributes.add(new Attribute<MguiBoolean>("IsClosed", new MguiBoolean(true)));
		//attributes.add(new Attribute<MguiBoolean>("HasFill", new MguiBoolean(false)));
		//attributes.add(new Attribute("FillColour", Color.WHITE));
		attributes.add(new Attribute<MguiFloat>("3D.FillAlpha", new MguiFloat(0f)));
		attributes.add(new Attribute<MguiBoolean>("3D.AsCylinder", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiFloat>("3D.CylRadius", new MguiFloat(1.0f)));
		attributes.add(new Attribute<MguiInteger>("3D.CylEdges", new MguiInteger(8)));
		attributes.add(new Attribute<MguiBoolean>("3D.Antialiasing", new MguiBoolean(true)));
		
		updateShape();
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/polygon_closed_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/polygon_closed_20.png");
	}
	
	@Override
	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist, Matrix4d transform){
		//TODO add attributes to allow different display types; i.e.,
		//filled vs. unfilled polygons, surface projection vs. strict intersection
		
		if (!isVisible()) return null;
		
		Polygon3D polygon = (Polygon3D)getShape();
		
		if (transform != null){
			polygon = (Polygon3D)polygon.clone();
			GeometryFunctions.transform(polygon, transform);
			}
		
		Polygon2DInt poly2dint = null;
		if (hasData() && (((MguiBoolean)attributes.getValue("ShowData")).getTrue())){
			poly2dint = ShapeFunctions.getProjectedPolygon(polygon, plane, above_dist, below_dist, getCurrentDataColumn());
		}else{
			poly2dint = ShapeFunctions.getProjectedPolygon(polygon, plane, above_dist, below_dist);
			}
		if (poly2dint == null) return null;
		
		ShapeFunctions.setAttributesFrom3DParent(poly2dint, this, inheritAttributesFromParent());
		poly2dint.updateShape();
		
		return poly2dint;
	}
	
	/*************************
	 * Sets whether this polygon will be rendered with anti-aliasing.
	 * 
	 * @param antialiasing
	 */
	public void setAntialiasing(boolean antialiasing) {
		this.setAttribute("3D.Antialiasing", new MguiBoolean(antialiasing));
	}
	
	/*************************
	 * Gets the current anti-aliasing policy.
	 * 
	 * @return {@code true} if anti-aliasing is currently on
	 */
	public boolean getAntialiasing() {
		return ((MguiBoolean)getInheritedAttribute("3D.Antialiasing").getValue()).getTrue();
	}
	
	/*************************
	 * Sets whether this polygon's end vertices are to be connected.
	 * 
	 * @param closed
	 */
	public void setClosed(boolean closed){
		setAttribute("IsClosed", new MguiBoolean(closed));
	}
	
	/*************************
	 * Determines whether this polygon's end vertices are to be connected.
	 * 
	 * @return {@code true} if this polygon is closed
	 */
	public boolean isClosed(){
		return ((MguiBoolean)getInheritedAttribute("IsClosed").getValue()).getTrue();
	}
	
	@Override
	public void setShape(mgui.geometry.Shape3D shape){
		if (!(shape instanceof Polygon3D)) return;
		super.setShape(shape);
	}
	
	public Polygon3D getPolygon(){
		return (Polygon3D)shape3d;
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		
		//change edge appearance?
		if ((e.getAttribute().getName().equals("LineColour") ||
			 e.getAttribute().getName().equals("LineStyle") ||
			 e.getAttribute().getName().equals("EdgeWidth") ||
			 e.getAttribute().getName().equals("3D.Antialiasing"))
				&& edge_appearance != null){
		
			setEdgeAppearance();
			
			//we're all done here
			return;
			}
		
		super.attributeUpdated(e);
		
	}
	
	@Override
	public void setScene3DObject(){
		setScene3DObject(true);
	}
	
	@Override
	public void setScene3DObject(boolean make_live){
		super.setScene3DObject(false);
		if (scene3DObject == null) return;
		if (!this.isVisible() || !this.show3D() || shape3d == null){
			if (make_live) setShapeSceneNode();
			return;
			}
		
		AttributeList attributes = getInheritedAttributes();
		
		BranchGroup sceneGroup = new BranchGroup();
		sceneGroup.setCapability(BranchGroup.ALLOW_DETACH);
		
		Polygon3D polygon = getPolygon();
		
		setEdgeAppearance();
		Color thisColour = (Color)attributes.getValue("3D.LineColour");
		ColoringAttributes cAtt = new ColoringAttributes();
		cAtt.setColor(Colours.getColor3f(thisColour));
		
		if (((MguiBoolean)attributes.getValue("3D.AsCylinder")).getTrue()){
			// Render as set of cylinders
			// TODO: implement colour mapping of cylinders
			setCylinderAppearance();
			sceneGroup = ShapeFunctions.getCylinderPolygon(
							getPolygon(), 
							((MguiFloat)attributes.getValue("3D.CylRadius")).getFloat(), 
							((MguiInteger)attributes.getValue("3D.CylEdges")).getInt(), 
							cylinder_appearance);
			
			sceneGroup.setCapability(BranchGroup.ALLOW_DETACH);
		}else{
			//render as polylines
			int n = polygon.n;
			
			if (n > 1){
				
				float[] nodes = polygon.getCoords();
				
				//if closed, add first node at end
				if (isClosed()){
					float[] temp = new float[nodes.length + 3];
					System.arraycopy(nodes, 0, temp, 0, nodes.length);
					temp[nodes.length] = nodes[0];
					temp[nodes.length + 1] = nodes[1];
					temp[nodes.length + 2] = nodes[2];
					nodes = temp;
					n++;
					}
				
				int[] lineStrip = new int[1];
				lineStrip[0] = n; // getPolygon().n; // + 1;
				
				LineStripArray polyArray = new LineStripArray(n,
						  GeometryArray.COORDINATES | GeometryArray.BY_REFERENCE,
						  lineStrip);
				
				//Point3f[] apparently doesn't require by reference?
				polyArray.setCoordRefFloat(nodes);
				Shape3D thisShapeNode = new Shape3D(polyArray);
				thisShapeNode.setAppearance(edge_appearance);
				thisShapeNode.setUserData(this);
				
				polyArray.setCapability(GeometryArray.ALLOW_REF_DATA_READ);
			    polyArray.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);
			    
			    sceneGroup.addChild(thisShapeNode);
			    
			    if (((MguiBoolean)attributes.getValue("3D.HasFill")).getTrue()){
			    	GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
			        gi.setCoordinates(nodes);
					gi.setStripCounts(new int[]{n});
					gi.setContourCounts(new int[]{1});
					gi.convertToIndexedTriangles();
					IndexedTriangleArray tris = (IndexedTriangleArray)gi.getIndexedGeometryArray();
					
					thisShapeNode = new Shape3D(tris);
					Appearance fillApp = new Appearance();
					Color c = (Color)attributes.getValue("3D.FillColour");
					ColoringAttributes cAtt2 = new ColoringAttributes();
					cAtt2.setColor(Colours.getColor3f(c));
					fillApp.setColoringAttributes(cAtt2);
					fillApp.setMaterial(new Material());
					if (((MguiFloat)attributes.getValue("3D.FillAlpha")).getFloat() > 0){
						String trans_type = (String)attributes.getValue("3D.AlphaMode");
						TransparencyAttributes ta = new TransparencyAttributes();
						ta.setTransparency(((MguiFloat)attributes.getValue("3D.FillAlpha")).getFloat());
						if (trans_type.equals("Screen Door")){
							ta.setTransparencyMode(TransparencyAttributes.SCREEN_DOOR);
						}else if (trans_type.equals("Fastest")){
							ta.setTransparencyMode(TransparencyAttributes.FASTEST);
						}else{
							ta.setTransparencyMode(TransparencyAttributes.NICEST);
							}
						ta.setSrcBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
						fillApp.setTransparencyAttributes(ta);
						}
					
					PolygonAttributes pAtt = new PolygonAttributes();
					pAtt.setCullFace(PolygonAttributes.CULL_NONE);
					pAtt.setBackFaceNormalFlip(true);
					fillApp.setPolygonAttributes(pAtt);
					
					thisShapeNode.setAppearance(fillApp);
					thisShapeNode.setUserData(this);
					sceneGroup.addChild(thisShapeNode);
					}
				}
		}
		
		if (showVertices()){
			float[] nodes = getPolygon().getCoords();
			int n = nodes.length / 3;
			PointArray p_array = new PointArray(n, GeometryArray.COORDINATES); // | GeometryArray.BY_REFERENCE);
			p_array.setCoordinates(0, nodes);

			BranchGroup bg = new BranchGroup();
			bg.setCapability(BranchGroup.ALLOW_DETACH);
			
			// TODO: Map these and update more efficiently
			for (int i = 0; i < n; i++) {
				
				Node node = vertex_transformer.transform(i);
				
				if (node != null) {
					float scale = getVertexScale(i);
					if (((MguiBoolean)attributes.getValue("ScaleVerticesAbs")).getTrue())
						scale = Math.abs(scale);
					if (scale > ShapeFunctions.tolerance) {
						Vector3f pv = new Vector3f(polygon.getVertex(i));
						Transform3D tx = new Transform3D();
						tx.setTranslation(pv);
						tx.setScale(scale);
						TransformGroup tg = new TransformGroup();
						tg.addChild(node);
						tg.setTransform(tx);
						bg.addChild(tg);
						}
					}
				
				}
			
			sceneGroup.addChild(bg);
			
			}
		
		sceneGroup.setUserData(this);
		scene3DObject.removeAllChildren();
		scene3DObject.addChild(sceneGroup);
		
		if (make_live) setShapeSceneNode();
	}
	

	
	@Override
	public String toString(){
		return "Polygon3DInt [" + this.ID + "]";
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
		
		Color3f edgeColour = Colours.getColor3f((Color)getInheritedAttribute("3D.LineColour").getValue());
		Material m = new Material();
		m.setDiffuseColor(edgeColour);
		m.setAmbientColor(edgeColour);
		m.setLightingEnable(true);
		
		ColoringAttributes cAtt = new ColoringAttributes();
		cAtt.setColor(edgeColour);
		
		//TODO: set dash
		float width = ((BasicStroke)getInheritedAttribute("3D.LineStyle").getValue()).getLineWidth();
		boolean antialiasing = this.getAntialiasing();
		LineAttributes lAtt = new LineAttributes(width,
												 LineAttributes.PATTERN_SOLID,
												 antialiasing);
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		pAtt.setBackFaceNormalFlip(true);
		
		edge_appearance.setLineAttributes(lAtt);
		edge_appearance.setColoringAttributes(cAtt);
		edge_appearance.setPolygonAttributes(pAtt);
		edge_appearance.setMaterial(m);
		
		if (((MguiBoolean)getInheritedAttribute("3D.HasAlpha").getValue()).getTrue()){
			TransparencyAttributes ta = new TransparencyAttributes();
			ta.setTransparency(((MguiFloat)getInheritedAttribute("3D.Alpha").getValue()).getFloat());
			ta.setTransparencyMode(TransparencyAttributes.NICEST);
			ta.setSrcBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
			edge_appearance.setTransparencyAttributes(ta);
		}else{
			edge_appearance.setTransparencyAttributes(null);
			}
		
	}
	
	protected void setCylinderAppearance(){
		
		if (cylinder_appearance == null){
			cylinder_appearance = new Appearance();
			cylinder_appearance.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
			cylinder_appearance.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);
			cylinder_appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
			cylinder_appearance.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
			cylinder_appearance.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
			cylinder_appearance.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
			}
		
		AttributeList attributes = getInheritedAttributes();
		
		Color colour = (Color)attributes.getValue("3D.LineColour");
		
		//turn off back culling
		PolygonAttributes poly_attr = new PolygonAttributes(PolygonAttributes.POLYGON_FILL,
													   PolygonAttributes.CULL_BACK,
													   0);
		cylinder_appearance.setPolygonAttributes(poly_attr);
		Material m = new Material();
		
		float shininess = ((MguiFloat)getInheritedAttribute("3D.Shininess").getValue()).getFloat();
		
		m.setShininess(shininess * 127f + 1);
		m.setSpecularColor(Colours.getColor3f(colour));
		m.setDiffuseColor(Colours.getColor3f(colour));
		
		cylinder_appearance.setMaterial(m);
		
		if (((MguiBoolean)getInheritedAttribute("3D.HasAlpha").getValue()).getTrue()){
			String trans_type = (String)getInheritedAttribute("3D.AlphaMode").getValue();
			TransparencyAttributes ta = new TransparencyAttributes();
			ta.setTransparency(((MguiFloat)getInheritedAttribute("3D.Alpha").getValue()).getFloat());
			if (trans_type.equals("Screen Door")){
				ta.setTransparencyMode(TransparencyAttributes.SCREEN_DOOR);
			}else if (trans_type.equals("Fastest")){
				ta.setTransparencyMode(TransparencyAttributes.FASTEST);
			}else{
				ta.setTransparencyMode(TransparencyAttributes.NICEST);
				}
			//ta.setTransparencyMode(TransparencyAttributes.BLENDED);
			ta.setSrcBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
			cylinder_appearance.setTransparencyAttributes(ta);
		}else{
			cylinder_appearance.setTransparencyAttributes(null);
			}
		
		
		RenderingAttributes ra = new RenderingAttributes();
		ra.setDepthBufferEnable(true);
		ra.setDepthTestFunction(RenderingAttributes.LESS_OR_EQUAL);
		
		cylinder_appearance.setRenderingAttributes(ra);
		
		
	}
	
}