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

package mgui.interfaces.shapes.volume;

import java.util.ArrayList;
import java.util.HashMap;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.ImageComponent3D;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.OrderedGroup;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.QuadArray;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Switch;
import org.jogamp.java3d.TexCoordGeneration;
import org.jogamp.java3d.TextureAttributes;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.geometry.GeometryInfo;
import org.jogamp.java3d.utils.geometry.NormalGenerator;
import org.jogamp.java3d.utils.geometry.Stripifier;
import org.jogamp.vecmath.Matrix4f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.SingularMatrixException;
import org.jogamp.vecmath.TexCoord3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;
import org.jogamp.vecmath.Vector4f;

import mgui.geometry.Box3D;
import mgui.geometry.Grid3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Vector3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.image.util.WindowedColourModel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.events.CameraEvent;
import mgui.interfaces.graphics.GraphicEvent;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.graphics.InterfaceGraphicListener;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.maps.Camera3DListener;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.Volume3DInt;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.numbers.MguiFloat;


/*******************************
 * Class providing functions for rendering a Volume3DInt object. Based on the rendering 
 * code provided in the VolRend package: org.jscience.medicine.volumetric
 * 
 * 
 * 
 * @author Andrew Reid
 *
 */

public class Volume3DRenderer implements Camera3DListener,
										 InterfaceGraphicListener{

	protected Volume3DInt volume3D;
	protected Volume3DTexture texture3D;
	protected Switch axisSwitch;
	protected Vector3d prevAngle = new Vector3d();	//previous camera angle; allows a buffer for switching
	public double angleBuffer = 0.4;				//buffer prevents flicker (higher value = less switching)
	int[][] axisIndex = new int[3][2];
	int currentAxis = 0;
	public boolean setAlpha = false;
	public float alpha;
	protected HashMap<InterfaceGraphic2D, Shape3D> window_nodes = new HashMap<InterfaceGraphic2D, Shape3D>();
	protected Camera3D ref_camera = null;
	
	BranchGroup sections_group;
	HashMap<InterfaceGraphic2D, BranchGroup> section_nodes = new HashMap<InterfaceGraphic2D, BranchGroup>();
	
	public static enum Mode{
		AsVolume,				//as a Texture3D
		AsSections,				//as 2D images draw on all section sets
		AsCubes;				//voxels as individual cubes (not implemented)
	}
	
	public Mode render_mode = Mode.AsVolume;		//defines how this renderer will render the volume
	
	//attributes
	TransparencyAttributes transparencyAtt;
	
	//for adapting to power-of-two requirement
	float xBuffer;
	float yBuffer;
	float zBuffer;
	
	//constants
	public static final int X_AXIS = 0;
	public static final int Y_AXIS = 1;
	public static final int Z_AXIS = 2;
	public static final int FRONT = 0;
	public static final int BACK = 1;
	
	public static final int X_AXIS_POS = 0;
	public static final int Y_AXIS_POS = 1;
	public static final int Z_AXIS_POS = 2;
	public static final int X_AXIS_NEG = 3;
	public static final int Y_AXIS_NEG = 4;
	public static final int Z_AXIS_NEG = 5;
	
	public Volume3DRenderer(Volume3DInt volume){
	//set up renderer from grid
		setVolume(volume);
		axisIndex[X_AXIS][FRONT] = 0;
		axisIndex[X_AXIS][BACK] = 1;
		axisIndex[Y_AXIS][FRONT] = 2;
		axisIndex[Y_AXIS][BACK] = 3;
		axisIndex[Z_AXIS][FRONT] = 4;
		axisIndex[Z_AXIS][BACK] = 5;
	}
	
	public static ArrayList<String> getRenderModes(){
		
		ArrayList<String> modes = new ArrayList<String>();
		modes.add("As volume");
		modes.add("As sections");
		
		return modes;
	}
	
	/********************************
	 * Sets the reference camera for this renderer. This is used to determine the correct
	 * axis for 3D volume rendering.
	 * 
	 * @param camera
	 */
	public void setReferenceCamera(Camera3D camera) {
		this.ref_camera = camera;
	}
	
	/*****************************
	 * Sets the current render mode for this renderer; one of "As volume" or "As sections".
	 * 
	 * @param mode
	 */
	public void setRenderMode(String mode){
		if (mode.equals("As volume"))
			setRenderMode(Mode.AsVolume);
		if (mode.equals("As sections"))
			setRenderMode(Mode.AsSections);
	}
	
	/****************************
	 * Sets the current render mode for this renderer; one of: 
	 * 
	 * <p>{@code Mode.AsVolume}: render as a 3D volume
	 * 
	 * <p>{@code Mode.AsSections}: render as 2D slices (one for each visible section set in the parent model.
	 * 
	 * @param mode
	 */
	public void setRenderMode(Mode mode){
		render_mode = mode;
		section_nodes.clear();
		if (mode != Mode.AsSections && sections_group != null){
			sections_group.removeAllChildren();
			sections_group.detach();
			ShapeSet3DInt set = volume3D.getModel().getModelSet();
			ArrayList<InterfaceGraphic2D> windows = set.getSectionWindows();
			for (InterfaceGraphic2D window : windows) {
				window.removeGraphicListener(this);
				}
			}
	}
	
	/**************************
	 * Get the current render mode as a {@code String}.
	 * 
	 * @return
	 */
	public String getRenderModeStr(){
		switch (render_mode){
			case AsVolume:
				return "As volume";
			case AsSections:
				return "As sections";
			default:
				return "?";
			}
	}
	
	/*********************************************
	 * Sets the current texture for this volume's renderer.
	 * 
	 */
	public void setTexture(){
		if (volume3D == null) return;
		texture3D = new Volume3DTexture(volume3D);
	}
	
	/******************************
	 * Sets a Volume3DTexture object from the current grid and data size
	 *
	 */
	public void setTexture(ColourMap cmap){
		if (volume3D == null) return;
		texture3D = new Volume3DTexture(volume3D, cmap);
	}
	
	public Volume3DTexture getTexture(){
		return texture3D;
	}
	
	public ImageComponent3D getImageComponent(){
		return texture3D.iComp;
	}
	
	public void setVolume(Volume3DInt volume){
		volume3D = volume;

	}
	
	public boolean setTransparency(float alpha){
		if (transparencyAtt == null) return false;
		transparencyAtt.setTransparency(alpha);
		return true;
	}
	
	/******************************
	 * Sets and returns a Java3D node from the given Grid3D and Texture3D
	 * @return Shape3D node
	 */
	public BranchGroup getNode(){
		switch (render_mode){
			case AsVolume:
				return getNodeAsVolume();
				
			case AsSections:
				return getNodeAsSections();
			
			default:
				return getNodeAsVolume();
			}
		
	}
	
	/******************************
	 * Sets and returns a Java3D node from the given Grid3D and Texture3D; node will be a 3D volume texture
	 * 
	 * @return Shape3D node
	 */
	public BranchGroup getNodeAsVolume(){
		//6 sets of quads, for 3 axes and 2 directions
		axisSwitch = new Switch();
        axisSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
        axisSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        axisSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
        axisSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
        
        for (int i = 0; i < 6; i++)
        	axisSwitch.addChild(getOrderedGroup());
       
		//scale box to fit power-of-two dimensions
        Grid3D grid3D = volume3D.getGrid();
        float slice_scale = ((MguiFloat)volume3D.getAttribute("3D.SliceScale").getValue()).getFloat();
		Box3D box = (Box3D)grid3D.clone();
		int x_size = grid3D.getSizeS();
		int y_size = grid3D.getSizeT();
		int z_size = grid3D.getSizeR();
		
		//yTrans.sub(box.tAxis);
		Vector3f thisAxis = box.getSAxis();
		thisAxis.scale((float)getTexture().sDim / (float)x_size);
		box.setSAxis(thisAxis);
		thisAxis = box.getTAxis();
		thisAxis.scale((float)getTexture().tDim / (float)y_size);
		box.setTAxis(thisAxis);
		thisAxis = box.getRAxis();
		thisAxis.scale((float)getTexture().rDim / (float)z_size);
		box.setRAxis(thisAxis);
		
		//offset y...
		Vector3f yTrans = new Vector3f();
		yTrans.set(box.getTAxis());
		yTrans.scale(((float)getTexture().tDim - (float)y_size) / getTexture().tDim);
		Point3f bp = box.getBasePt();
		bp.sub(yTrans);
		box.setBasePt(bp);
		
		//points
		ArrayList<Point3f> points = box.getVertices();
		
		Point3f[] coords = new Point3f[4];
		coords[0] = new Point3f();
		coords[1] = new Point3f();
		coords[2] = new Point3f();
		coords[3] = new Point3f();
		
		//offset vector
		Vector3f offset = new Vector3f();
		int quadSize = 0;
		String texCoords = null;
		
		//set up appearance for all nodes
		Appearance appNode = new Appearance();
		Material m = new Material();
		//m.setLightingEnable(false);
		appNode.setMaterial(m);
		appNode.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
		appNode.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		
		//set texture here
		appNode.setTexture(texture3D.texture);
		
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		appNode.setPolygonAttributes(pAtt);
		transparencyAtt = new TransparencyAttributes();
		transparencyAtt.setTransparencyMode(TransparencyAttributes.BLENDED);
		transparencyAtt.setDstBlendFunction(TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA);
		transparencyAtt.setCapability(TransparencyAttributes.ALLOW_VALUE_READ);
		transparencyAtt.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
		//if (setAlpha) transparencyAtt.setTransparency(alpha);
		appNode.setTransparencyAttributes(transparencyAtt);

		RenderingAttributes rAtt = new RenderingAttributes();
		rAtt.setDepthBufferWriteEnable(true);
		appNode.setRenderingAttributes(rAtt);
		
		TextureAttributes texAtt = new TextureAttributes();
		texAtt.setTextureMode(TextureAttributes.REPLACE);
		appNode.setTextureAttributes(texAtt);
		
		for (int axis = 0; axis < 3; axis++){
			switch (axis){
				case X_AXIS:
					//x-axis
					points = box.getSide(0, 0);
					coords[0].set(points.get(0));
					coords[1].set(points.get(1));
					coords[2].set(points.get(2));
					coords[3].set(points.get(3));
					offset.set(box.getSAxis());
					//offset.scale(1f / (getTexture().sDim * slice_scale));
					quadSize = (int)Math.ceil(getTexture().sDim * slice_scale);
					offset.scale(1f / (float)quadSize);
					texCoords = "X";
					break;
				case Y_AXIS:
					//y-axis
					points = box.getSide(0, 1);
					coords[0].set(points.get(0));
					coords[1].set(points.get(1));
					coords[2].set(points.get(2));
					coords[3].set(points.get(3));
					
					offset.set(box.getTAxis());
					
					quadSize = (int)Math.ceil(getTexture().tDim * slice_scale);
					offset.scale(1f / (float)quadSize);
					texCoords = "Y";
					break;
				case Z_AXIS:
					//z-axis
					points = box.getSide(0, 2);
					coords[0].set(points.get(0));
					coords[1].set(points.get(1));
					coords[2].set(points.get(2));
					coords[3].set(points.get(3));
					offset.set(box.getRAxis());
					//offset.scale(1f / (getTexture().rDim * slice_scale));
					quadSize = (int)Math.ceil(getTexture().rDim * slice_scale);
					offset.scale(1f / (float)quadSize);
					texCoords = "Z";
				}
			
			//add quads to array
			for (int i = 0; i < quadSize; i++){
				QuadArray thisQuad = new QuadArray(4, 
												   GeometryArray.COORDINATES |
												   GeometryArray.TEXTURE_COORDINATE_3);
				thisQuad.setCoordinates(0, coords);
				for (int j = 0; j < 4; j++)
					coords[j].add(offset);
				TexCoord3f[] texArray = getTexCoords(texCoords, i, slice_scale);
				if (texArray != null){
					thisQuad.setTextureCoordinates(0, 0, texArray);
	
					OrderedGroup frontGroup = (OrderedGroup)axisSwitch.getChild(axisIndex[axis][FRONT]);
					OrderedGroup backGroup = (OrderedGroup)axisSwitch.getChild(axisIndex[axis][BACK]);
					
					Shape3D frontShape = new Shape3D(thisQuad, appNode);
					frontShape.setUserData(volume3D);
	
				    BranchGroup frontShapeGroup = new BranchGroup();
				    frontShapeGroup.setCapability(BranchGroup.ALLOW_DETACH);
				    frontShapeGroup.addChild(frontShape);
				    frontGroup.addChild(frontShapeGroup);
	
				    Shape3D backShape = new Shape3D(thisQuad, appNode);
				    backShape.setUserData(volume3D);
	
				    BranchGroup backShapeGroup = new BranchGroup();
				    backShapeGroup.setCapability(BranchGroup.ALLOW_DETACH);
				    backShapeGroup.addChild(backShape);
				    backGroup.insertChild(backShapeGroup, 0);
					}
				}
			}
		currentAxis = this.axisIndex[Z_AXIS][BACK];
		setAxis();
		BranchGroup bg = new BranchGroup();
		bg.addChild(axisSwitch);
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		return bg;
		//return axisSwitch;
	}
	
	/******************************
	 * Sets and returns a Java3D node from the given Grid3D and Texture3D; node will be a set of 2D planes
	 * corresponding the the section sets in the volume's parent set
	 * 
	 * @return Shape3D node
	 */
	public BranchGroup getNodeAsSections(){
		
		//destroy existing sections
		section_nodes.clear();
		window_nodes.clear();
		
		if (sections_group == null){
			sections_group = new BranchGroup();
			sections_group.setCapability(BranchGroup.ALLOW_DETACH);
			sections_group.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			sections_group.setCapability(Group.ALLOW_CHILDREN_WRITE);
		}else{
			sections_group.removeAllChildren();
			}
		
		ShapeSet3DInt set = volume3D.getModel().getModelSet();
		ArrayList<InterfaceGraphic2D> windows = set.getSectionWindows();
		
		for (int i = 0; i < windows.size(); i++){
			windows.get(i).addGraphicListener(this);
			
			//for each member, get intersection with volume bounds and render
			volume3D.getBoundBox();
			InterfaceGraphic2D window = windows.get(i);
			updateSection(window);
			}
		
		return sections_group;
	}
	
	//update the texture for this window's current section
	protected void updateSection(InterfaceGraphic2D window){
		
		if (window.isDestroyed()){
			destroySectionNode(window);
			return;
			}
		
		//removeSectionNode(window);
	
		SectionSet3DInt section_set = window.getCurrentSectionSet();
		if (section_set == null || !section_set.isVisible()){
			removeSectionNode(window);
			return;
			}
		
		Plane3D plane = section_set.getPlaneAt(window.getCurrentSection());
		Polygon2D poly2D = ShapeFunctions.getIntersectionPolygon(volume3D.getBox(), plane);
		if (poly2D == null){
			removeSectionNode(window);
			return;
			}
		
		Appearance appNode = new Appearance();
		Material m = new Material();
		m.setLightingEnable(false);
		appNode.setMaterial(m);
		
		TextureAttributes texAtt = new TextureAttributes();
		texAtt.setTextureMode(TextureAttributes.REPLACE);
		appNode.setTextureAttributes(texAtt);
		
		PolygonAttributes pAtt = new PolygonAttributes();
		pAtt.setCullFace(PolygonAttributes.CULL_NONE);
		appNode.setPolygonAttributes(pAtt);
		
		WindowedColourModel colour_model = volume3D.getColourModel();
		
		// No transparency due to z-order issue
		
		if (colour_model != null && colour_model.hasAlpha()){
			appNode.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
			appNode.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
			TransparencyAttributes tAtt = new TransparencyAttributes();
			tAtt.setTransparencyMode(TransparencyAttributes.BLENDED);
			tAtt.setDstBlendFunction(TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA);
			tAtt.setCapability(TransparencyAttributes.ALLOW_VALUE_READ);
			tAtt.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
			if (setAlpha) tAtt.setTransparency(alpha);
			appNode.setTransparencyAttributes(tAtt);
			}
		
		//set texture here
		appNode.setTexture(texture3D.texture);
		Grid3D box = volume3D.getGrid();
		
		//Java3D plane equations are apparently not actual plane equations...
		Vector4f planeS = GeometryFunctions.getPlaneEquation(box.getBasePt(), box.getSAxis());
		double d = planeS.w / Math.sqrt(planeS.x * planeS.x + planeS.y * planeS.y + planeS.z * planeS.z);
		double factor_s = (double)box.getSizeS() / (double)box.getSDim();
		planeS.w = 0;
		planeS.normalize();
		planeS.w = (float)d;
		planeS.scale((float)(factor_s / texture3D.sDim));
		
		//Y image coordinates and power-of-two ...
		Vector4f planeT = GeometryFunctions.getPlaneEquation(box.getBasePt(), box.getTAxis());
		d = planeT.w / Math.sqrt(planeT.x * planeT.x + planeT.y * planeT.y + planeT.z * planeT.z);
		double factor_t = (double)box.getSizeT() / (double)box.getTDim();
		planeT.w = 0;
		planeT.normalize();
		planeT.w = (float)d;
		planeT.scale((float)(factor_t / texture3D.tDim));
		//planeT.w += (double)(texture3D.tDim - box.getSizeT()) / (double)texture3D.tDim;	//p-o-2 offset in object coords
		
		Vector4f planeR = GeometryFunctions.getPlaneEquation(box.getBasePt(), box.getRAxis());
		d = planeR.w / Math.sqrt(planeR.x * planeR.x + planeR.y * planeR.y + planeR.z * planeR.z);
		double factor_r = (double)box.getSizeR() / (double)box.getRDim();
		planeR.w = 0;
		planeR.normalize();
		planeR.w = (float)d;
		planeR.scale((float)(factor_r / texture3D.rDim));
		
		TexCoordGeneration tex_gen = new TexCoordGeneration(TexCoordGeneration.OBJECT_LINEAR,
															TexCoordGeneration.TEXTURE_COORDINATE_3,
															planeS,
															planeT,
															planeR);
		tex_gen.setEnable(true);
		
		appNode.setTexCoordGeneration(tex_gen);
		
		ArrayList<Point3f> nodes = GeometryFunctions.getPointsFromPlane(poly2D.getVertices(), plane);
		
		Point3f[] coords = new Point3f[nodes.size()];
		for (int i = 0; i < nodes.size(); i++)
			coords[i] = nodes.get(i);
		
		GeometryInfo gi = new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
		gi.setCoordinates(coords);
		gi.setStripCounts(new int[]{nodes.size()});
		NormalGenerator ng = new NormalGenerator();
        ng.generateNormals(gi);
        Stripifier st = new Stripifier();
        st.stripify(gi);
		
		GeometryArray geom_array = gi.getGeometryArray();
		Shape3D quad_shape = window_nodes.get(window);
		
		if (quad_shape == null){
			quad_shape = new Shape3D(geom_array, appNode);
			quad_shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
			quad_shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
			window_nodes.put(window, quad_shape);
			window.addGraphicListener(this);
			
			BranchGroup section_group = new BranchGroup();
			section_group.setCapability(BranchGroup.ALLOW_DETACH);
			section_group.setCapability(Group.ALLOW_CHILDREN_READ);
			section_group.addChild(quad_shape);
			section_nodes.put(window, section_group);
			sections_group.addChild(section_group);
		}else{
			quad_shape.setGeometry(geom_array);
			quad_shape.setAppearance(appNode);
			}
		
	}
	
	//get texture coordinates for the nodes, relative to the current volume
	protected TexCoord3f[] getTexCoordsForPoly(ArrayList<Point3f> nodes){
		
		Box3D box = volume3D.getBox();
		Vector3f edge = new Vector3f();
		ArrayList<Point3f> side;
		TexCoord3f[] coords = new TexCoord3f[nodes.size()];
		
		//for each node, determine which edge it intersects
		for (int i = 0; i < nodes.size(); i++){
			for (int dir = 0; dir < 3; dir++){
				side = box.getSide(0, dir);
				
				if (dir == 0) edge.set(box.getSAxis());
				if (dir == 1) edge.set(box.getTAxis());
				if (dir == 2) edge.set(box.getRAxis());
				
				for (int j = 0; j < 4; j++){
					Vector3D v = new Vector3D(side.get(j), edge);
					if (GeometryFunctions.intersects(v, nodes.get(i), 0.1f)){
						//node intersects this edge, so set texture coordinate
						double pdist = (double)v.getStart().distance(nodes.get(i)) / (double)v.getLength(); 
						switch (dir){
							case 0:
								//axis is S-axis
								coords[i] = new TexCoord3f((float)pdist,
														   ((j == 0 || j == 4) ? 0f : 1f),
														   ((j == 0 || j == 1) ? 0f : 1f));
								break;
							case 1:
								//axis is T-axis
								coords[i] = new TexCoord3f(((j == 0 || j == 4) ? 0f : 1f),
														   (float)pdist,
														   ((j == 0 || j == 1) ? 0f : 1f));
								break;
							case 2:
								//axis is R-axis
								coords[i] = new TexCoord3f(((j == 0 || j == 4) ? 0f : 1f),
														   ((j == 0 || j == 1) ? 0f : 1f),
														   (float)pdist);
								break;
							}
						}
					}
				}
			}
		
		return coords;
	}
	
	/****************
	 * Respond to a window event; this is relevant for section nodes
	 * 
	 * @param g
	 */
	@Override
	public void graphicUpdated(GraphicEvent e) {
		
		InterfaceGraphic<?> panel = e.getGraphic();
		
		if (panel instanceof InterfaceGraphic2D){
		
			switch (render_mode){
				case AsSections:
					InterfaceGraphic2D window = (InterfaceGraphic2D)panel;
					switch (e.getType()){
						case Modified:
							updateSection(window);
							return;
							
						case NewSource:
							//if new source is not a member of this model, destroy old section node
							SectionSet3DInt set = ((InterfaceGraphic2D)e.getSource()).getCurrentSectionSet();
							if (!set.getModel().equals(volume3D.getModel()))
								destroySectionNode((InterfaceGraphic2D)e.getSource());
							
							return;
					
						case Destroyed:
							//if this window has been destroyed, destroy its section node
							destroySectionNode((InterfaceGraphic2D)e.getSource());
							return;
							
						default:
							return;
							
						}
					
				default:
					return;
				}
		
			}
		
	}
	
	public void destroySectionNode(InterfaceGraphic2D window){
		
		section_nodes.remove(window);
		removeSectionNode(window);
		window.removeGraphicListener(this);
		
	}
	
	public void removeSectionNode(InterfaceGraphic2D window){
		
		BranchGroup bg = section_nodes.get(window);
		if (bg == null) return;
		bg.detach();
		section_nodes.remove(window);
		
	}
	
	//get p as a proportion of this volume's bounds
	protected TexCoord3f getTexCoord(Point3f p){
		
		Box3D bounds = volume3D.getBoundBox();
		//if (!bounds.contains(p)) return null;
		
		//get p in box's coordinate system
		Vector3f v = new Vector3f(p);
		v.sub(bounds.getBasePt());
		Matrix4f M = bounds.getBasisTransform();
		v = GeometryFunctions.getMatrixProduct(v, M);
		
		//now as a fraction of bounds dimensions
		v.x /= bounds.getSDim();
		v.y /= bounds.getTDim();
		v.z /= bounds.getRDim();
		
		return new TexCoord3f(v);
	}
	
	
	
	//flip quad coordinates
	protected Point3f[] flipCoords(Point3f[] coords){
		Point3f[] retPts = new Point3f[4];
		retPts[0] = coords[3];
		retPts[1] = coords[2];
		retPts[2] = coords[1];
		retPts[3] = coords[0];
		return coords;
	}
	
	//flip tex coordinates
	protected void flipCoords(TexCoord3f[] coords){
		TexCoord3f t = coords[0];
		coords[0] = coords[3];
		coords[3] = t;
		t = coords[1];
		coords[1] = coords[2];
		coords[2] = t;
	}
	
	protected OrderedGroup getOrderedGroup() {
		OrderedGroup og = new OrderedGroup();
		og.setCapability(Group.ALLOW_CHILDREN_READ);
		og.setCapability(Group.ALLOW_CHILDREN_WRITE);
		og.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		return og;
	    }
	
	protected TexCoord3f[] getTexCoords(String coords, int i, float scale){
		if (coords.equals("X")) return getTexCoordsX(i, scale);
		if (coords.equals("Y")) return getTexCoordsY(i, scale);
		if (coords.equals("Z")) return getTexCoordsZ(i, scale);
		return null;
	}
	
	protected TexCoord3f[] getTexCoordsX(int i, float scale){
		
		float x = (float)i / ((float)getTexture().sDim * scale);
		
		TexCoord3f[] texPts = {new TexCoord3f(x, 0.0f, 0.0f),
							   new TexCoord3f(x, 0.0f, 1.0f),
							   new TexCoord3f(x, 1.0f, 1.0f),
							   new TexCoord3f(x, 1.0f, 0.0f)};
		
		return texPts;
	}
	
	protected TexCoord3f[] getTexCoordsY(int i, float scale){
		float y = (float)i / ((float)getTexture().tDim * scale);
		
		TexCoord3f[] texPts = {new TexCoord3f(0.0f, y, 0.0f),
							   new TexCoord3f(1.0f, y, 0.0f),
							   new TexCoord3f(1.0f, y, 1.0f),
							   new TexCoord3f(0.0f, y, 1.0f)};
		
		return texPts;
	}
	
	protected TexCoord3f[] getTexCoordsZ(int i, float scale){
		
		float z = (float)i / ((float)getTexture().rDim * scale);
		
		TexCoord3f[] texPts = {new TexCoord3f(0.0f, 0.0f, z),
							   new TexCoord3f(1.0f, 0.0f, z),
							   new TexCoord3f(1.0f, 1.0f, z),
							   new TexCoord3f(0.0f, 1.0f, z)};
		
		return texPts;
	}
	
    public void cameraChanged(CameraEvent e){
    	switch (e.getType()){
	    	case SceneChanged:
	    		//force axis update
	    		switch (render_mode){
		    		case AsVolume:
			    		currentAxis = getNearestAxis(e.getCamera().getLineOfSight());
			    		setAxis();
		    		}
	    		break;
	    	}
    }
    
    @Override
    public void cameraAngleChanged(CameraEvent e){
    	switch (render_mode){
			case AsVolume:
		    	updateAxis(e.getCamera());
		    	
		    default:
	    	}
    }
    
    /***********************
     * Update the rendering planes of this renderer, based on the reference camera angle,
     * if one is set.
     * 
     */
    public void updateAxis() {
    	if (this.ref_camera == null) return;
    	updateAxis(ref_camera);
    }
    
    /***********************
     * Update the rendering planes of this renderer, based on the current camera angle
     * 
     */
    public void updateAxis(Camera3D camera) {
    	
    	switch (render_mode){
			case AsVolume:
				
		    	int axis = getNearestAxis(camera.getLineOfSight());
		    	if (axis == currentAxis) return;
		    	currentAxis = axis;
		    	setAxis();
		    
			default:
	    	}
    	
    }
    
    protected Vector3d getAxisVector(int axis){
    	//inverse transform gives us world coordinates from grid coordinates
    	Grid3D grid3D = volume3D.getGrid();
    	Matrix4f M = grid3D.getBasisTransform();
		Transform3D trans = new Transform3D();
		trans.set(M);
		try{
			trans.invert();
		}catch (SingularMatrixException e){
			//e.printStackTrace();
			InterfaceSession.log("Volume3DRenderer: Matrix can't be inverted for volume '" + volume3D.getName() + "'.", LoggingType.Errors);
			trans = null;
			}
    	//Transform3D trans = grid3D.getInverseBasisTransform();
    	
    	Vector3d v = null;
    	switch (axis){
    		case X_AXIS_POS:
    			v = new Vector3d(1,0,0);
    			break;
    		case X_AXIS_NEG:
    			v = new Vector3d(-1,0,0);
    			break;
    		case Y_AXIS_POS:
    			v = new Vector3d(0,1,0);
    			break;
    		case Y_AXIS_NEG:
    			v = new Vector3d(0,-1,0);
    			break;
    		case Z_AXIS_POS:
    			v = new Vector3d(0,0,1);
    			break;
    		case Z_AXIS_NEG:
    			v = new Vector3d(0,0,-1);
    			break;
    		}
    	if (trans != null) trans.transform(v);
		return v;
    }
    
    protected int getNearestAxis(Vector3d v){
    	return getNearestAxis(v, true);
    }
    
    protected int getNearestAxis(Vector3d v, boolean buffer){
    	if (buffer && v.angle(prevAngle) < angleBuffer) return currentAxis;
    	int axis = axisIndex[X_AXIS][FRONT];
    	double angle1 = v.angle(getAxisVector(X_AXIS_POS));
    	double angle2 = v.angle(getAxisVector(X_AXIS_NEG));
    	if (angle2 < angle1) axis = axisIndex[X_AXIS][BACK];
    	angle1 = Math.min(angle1, angle2);
    	angle2 = v.angle(getAxisVector(Y_AXIS_POS));
    	if (angle2 < angle1) axis = axisIndex[Y_AXIS][FRONT];
    	angle1 = Math.min(angle1, angle2);
    	angle2 = v.angle(getAxisVector(Y_AXIS_NEG));
    	if (angle2 < angle1) axis = axisIndex[Y_AXIS][BACK];
    	angle1 = Math.min(angle1, angle2);
    	angle2 = v.angle(getAxisVector(Z_AXIS_POS));
    	if (angle2 < angle1) axis = axisIndex[Z_AXIS][FRONT];
    	angle1 = Math.min(angle1, angle2);
    	angle2 = v.angle(getAxisVector(Z_AXIS_NEG));
    	if (angle2 < angle1) axis = axisIndex[Z_AXIS][BACK];
    	if (axis != currentAxis)
    		prevAngle.set(v);
    	return axis;
    }
    
    protected void setAxis(){
    	if (axisSwitch == null) return;
    	axisSwitch.setWhichChild(currentAxis);
    }
       
    protected int powerOfTwo(int value) {
		int retval = 2;
		while (retval < value)
		    retval *= 2;
		return retval;
	}
    
    public void updateData(Grid3D grid,
					       int index,
					       int x, int y,
					       int width, int height){
    	
    	
    }

	@Override
	public void graphicSourceChanged(GraphicEvent e) {
		// TODO Auto-generated method stub
		
	}
    
    
}