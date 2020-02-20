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
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.ImageIcon;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;

import mgui.geometry.Plane3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Polygon3D;
import mgui.geometry.Shape;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;

import com.sun.j3d.utils.geometry.GeometryInfo;


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
	
	public void setClosed(boolean b){
		attributes.setValue("IsClosed", new MguiBoolean(b));
	}
	
	public boolean isClosed(){
		return ((MguiBoolean)attributes.getValue("IsClosed")).getTrue();
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
			 e.getAttribute().getName().equals("EdgeWidth"))
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
		
		BranchGroup sceneGroup = new BranchGroup();
		sceneGroup.setCapability(BranchGroup.ALLOW_DETACH);
		
		setEdgeAppearance();
		Color thisColour = (Color)this.getAttribute("3D.LineColour").getValue();
		ColoringAttributes cAtt = new ColoringAttributes();
		cAtt.setColor(new Color3f(thisColour));
		
		if (((MguiBoolean)attributes.getValue("3D.AsCylinder")).getTrue()){
			//render as cylinder
			sceneGroup = ShapeFunctions.getCylinderPolygon(
							getPolygon(), 
							((MguiFloat)attributes.getValue("3D.CylRadius")).getFloat(), 
							((MguiInteger)attributes.getValue("3D.CylEdges")).getInt(), 
							edge_appearance);
			
			sceneGroup.setCapability(BranchGroup.ALLOW_DETACH);
		}else{
			//render as polylines
			int n = getPolygon().n;
			
			if (n > 1){
				
				float[] nodes = getPolygon().getCoords();
				
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
					cAtt2.setColor(new Color3f(c));
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
			if (!showData()){
				Appearance p_app = new Appearance();
	
			    // enlarge the points
				p_app.setPointAttributes(new PointAttributes(getVertexScale(), true));
				p_app.setColoringAttributes(new ColoringAttributes(new Color3f(getVertexColour()), ColoringAttributes.FASTEST));
	
				javax.media.j3d.Shape3D shape3D = new javax.media.j3d.Shape3D(p_array, p_app);
				shape3D.setUserData(this);
				BranchGroup bg = new BranchGroup();
				bg.setCapability(BranchGroup.ALLOW_DETACH);
				bg.addChild(shape3D);
				sceneGroup.addChild(bg);
			}else{
				ArrayList<MguiNumber> data = getCurrentVertexData();
				ColourMap cmap = getColourMap();
				
				for (int i = 0; i < n; i++)
					p_array.setColor(i, cmap.getColour(data.get(i)).getColor4f());
					
				Appearance p_app = new Appearance();
				p_app.setPointAttributes(new PointAttributes(getVertexScale(), true));
	
				javax.media.j3d.Shape3D shape3D = new javax.media.j3d.Shape3D(p_array, p_app);
				shape3D.setUserData(this);
				BranchGroup bg = new BranchGroup();
				bg.setCapability(BranchGroup.ALLOW_DETACH);
				bg.addChild(shape3D);
				sceneGroup.addChild(bg);
				
				}
			}
		
		sceneGroup.setUserData(this);
		scene3DObject.removeAllChildren();
		scene3DObject.addChild(sceneGroup);
		
		if (make_live) setShapeSceneNode();
	}
	
	/**
	public void setScene3DObject(){
		/**@TODO update to include colour (line style, transparency?)***/
		/**@TODO draw nodes? ***
		int[] lineStrip = new int[1];
		lineStrip[0] = getPolygon().nodes.size() + 1;
		
		LineStripArray polyArray = new LineStripArray(getPolygon().nodes.size() + 1,
													  LineStripArray.COORDINATES,
													  lineStrip);
		//polyArray.setCoordinates(0, getPolygon().toArray());
		Point3f[] nodeArray = new Point3f[getPolygon().nodes.size() + 1];
		for (int i = 0; i < getPolygon().nodes.size(); i++)
			nodeArray[i] = getPolygon().nodes.get(i);
		//add first node to close polygon
		nodeArray[getPolygon().nodes.size()] = getPolygon().nodes.get(0);
		polyArray.setCoordinates(0, nodeArray);
		
		Shape3D thisShapeNode = new Shape3D(polyArray);
		Appearance thisAppNode = new Appearance();
		Color thisColour = (Color)attributes.getValue("LineColour");
		ColoringAttributes cAtt = new ColoringAttributes();
		cAtt.setColor(new Color3f(thisColour));
		thisAppNode.setColoringAttributes(cAtt);
		thisAppNode.setMaterial(new Material());
		thisShapeNode.setAppearance(thisAppNode);
		
		//Set colour
		//Color thisColour = (Color)attributes.getValue("LineColour");
		//for (int i = 0; i < getPolygon().nodes.size(); i++)
		//	polyArray.setColor(i, new Color3f(thisColour));
		BranchGroup thisNode = new BranchGroup();
		thisNode.addChild(thisShapeNode);
		
		scene3DObject = thisNode;
		//return thisNode;
	} **/
	
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
			}
		
		Color3f edgeColour = new Color3f((Color)getAttribute("3D.LineColour").getValue());
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
	
}