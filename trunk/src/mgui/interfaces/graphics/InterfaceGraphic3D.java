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

package mgui.interfaces.graphics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.PickInfo;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.View;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.pickfast.PickCanvas;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.Viewer;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.Matrix3d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector2d;
import org.jogamp.vecmath.Vector2f;
import org.jogamp.vecmath.Vector3f;
import org.xml.sax.Attributes;

import mgui.geometry.Sphere3D;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.events.CameraEvent;
import mgui.interfaces.graphics.util.Axes3D;
import mgui.interfaces.graphics.util.AxesEvent;
import mgui.interfaces.graphics.util.AxesListener;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.maps.Camera3DListener;
import mgui.interfaces.maps.Map;
import mgui.interfaces.maps.Map3D;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeModel3DListener;
import mgui.interfaces.shapes.ShapeModelEvent;
import mgui.interfaces.shapes.ShapeSceneNode;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.ToolBehavior3DAdapter;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolEvent;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.graphics.Tool3D;
import mgui.interfaces.tools.graphics.ToolMouseOrbit3D;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.io.util.IoFunctions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.util.Colours;

/********************
 * Interface class for Java3D display. Each InterfaceGraphic3D object contains its own
 * SimpleUniverse object for display of Shape3DInt objects. Shape3DInt objects are
 * represented as ShapeSceneNode objects (i.e., Shape2DSceneNode and Shape3DSceneNode),
 * which should be specified by-reference and be updateable.
 * 
 * todo: Allow for multiple directional light sources
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 * 
 */
public class InterfaceGraphic3D extends InterfaceGraphic<Tool3D> implements ShapeModel3DListener,
																			ShapeListener,
																			Camera3DListener,
																			ActionListener,
																			AxesListener{

	//TODO: make most of these protected or private
	public Tool3D currentTool;
	public Tool3D defaultTool;
	public ShapeSelectionSet currentSelection;
	public BoundingSphere tempBounds;
	public InterfaceCanvas3D canvas3D;
	public AmbientLight ambientLight;
	public ArrayList<DirectionalLight> lights = new ArrayList<DirectionalLight>();
	public DirectionalLight lightSource; //, lightSource2;
	public ShapeSceneNode currentScene;
	public ShapeSet3DInt currentSet;
	public ViewingPlatform viewingPlatform;
	Viewer viewer;
	public PickCanvas pickCanvasGeom, pickCanvasNode;
	public Background background;
	
	protected ToolBehavior3DAdapter toolInput3DAdapter;
	protected BranchGroup lightNode;
	protected ShapeModel3D model;
	protected TransformGroup axes_transform;
	protected float min_screen_dim = 1000;
	protected Transform3D R_temp = new Transform3D();
	protected Transform3D R_temp2 = new Transform3D();
	protected Matrix3d M3_temp = new Matrix3d();
	protected Axes3D axes = new Axes3D(this);
	
	private Timer resize_timer;
	protected BranchGroup temp_popup_shape;
	
	
	public InterfaceGraphic3D(String theName){
		super();
		setName(theName);
		init();
	}
	
	public InterfaceGraphic3D(){	
		super();
		init();
	}
	
	public InterfaceGraphic3D(ShapeModel3D m){	
		super();
		init();
		setModel(m);
	}
	
	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/window_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/window_3d_20.png");
		return null;
	}
	
	@Override
	public void destroy(){
		super.destroy();
		if (model != null){
			model.removeGraphics3D(this);
			}
	}
	
	@Override
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	@Override
	public void init(){
		if (init_once) return;
		super.init();
		
		resize_timer = new Timer(500, this);
		resize_timer.setActionCommand("Window Resized");
		resize_timer.setRepeats(false);
		
		//axes.setPanel(this);
		
		addComponentListener(this);
		
		attributes.add(new Attribute<MguiFloat>("BoundsScale", new MguiFloat(3f)));
		attributes.add(new Attribute<MguiBoolean>("AutoCenter", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiFloat>("PickTolerance", new MguiFloat(4f)));
		attributes.add(new Attribute<MguiBoolean>("ShowAxes", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiFloat>("SnapshotScale", new MguiFloat(1f)));
		
		attributes.add(new Attribute<MguiDouble>("ClipDistanceFront", new MguiDouble(10)));
		attributes.add(new Attribute<MguiDouble>("ClipDistanceBack", new MguiDouble(1000)));
		attributes.add(new Attribute<MguiBoolean>("AutoBackClip", new MguiBoolean(true)));
		
		type = "Graphic3D";
	
		//set up stuff here
		//panel display
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		setLayout(new BorderLayout());
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		canvas3D = new InterfaceCanvas3D(config);
		canvas3D.setGraphic3D(this);
		
		background = new Background();
		background.setApplicationBounds(new BoundingSphere(new Point3d(0,0,0), 1000000));
		background.setCapability(Background.ALLOW_COLOR_WRITE);
		//setBackgroundColour(new Color(205, 205, 255));
		setBackgroundColour(Color.white);
		
		this.add(BorderLayout.CENTER, canvas3D.getCanvas());
		
		/**@TODO make it possible to enable/disable adapter based upon tool **/
		
		viewer = new Viewer(canvas3D.getCanvas());
		viewingPlatform = new ViewingPlatform(1);
		viewer.setViewingPlatform(viewingPlatform);
		
		//viewer.getView().setTransparencySortingPolicy(View.TRANSPARENCY_SORT_GEOMETRY);
		
		TransformGroup thisTarget = viewingPlatform.getViewPlatformTransform();
		
		Map3D map3D = new Map3D();
		setMap(map3D);
		
		map3D.setTargetTransform(thisTarget);
		map3D.getCamera().setDistance(20);
		
		map3D.updateTargetTransform();
		map3D.setView(viewer.getView());
		
		viewer.getView().setDepthBufferFreezeTransparent (false);
		
		toolInput3DAdapter = new ToolBehavior3DAdapter(canvas3D.getCanvas(), map3D);
		viewingPlatform.setViewPlatformBehavior(toolInput3DAdapter);
		
		lightSource = new DirectionalLight();
		lightSource.setCapability(DirectionalLight.ALLOW_DIRECTION_WRITE);
		lightSource.setCapability(Light.ALLOW_COLOR_READ);
		lightSource.setCapability(Light.ALLOW_COLOR_WRITE);
		lightSource.setCapability(Light.ALLOW_STATE_READ);
		lightSource.setCapability(Light.ALLOW_STATE_WRITE);
		
		//add this light source to the map (we want it to move with the camera)
		map3D.addLightSource(lightSource);
		//map3D.addLightSource(lightSource, new Vector2d(0,-Math.PI / 2.0));
		//map3D.addLightSource(lightSource2, new Vector2d(Math.PI / 2.0,0));
		
		ambientLight = new AmbientLight();
		lightSource.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), Double.POSITIVE_INFINITY));
		lightSource.setDirection(new Vector3f(0, 0, 0));
		//lightSource2.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), Double.POSITIVE_INFINITY));
		//lightSource2.setDirection(new Vector3f(0, 0, 0));
		ambientLight.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), Double.POSITIVE_INFINITY));
		ambientLight.setEnable(true);
		ambientLight.setColor(Colours.getColor3f(Color.WHITE));
		lightNode = new BranchGroup();
		lightNode.setCapability(Group.ALLOW_CHILDREN_WRITE);
		lightNode.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		lightNode.setCapability(BranchGroup.ALLOW_DETACH);
		lightNode.setCapability(BranchGroup.ALLOW_BOUNDS_READ);
		lightNode.setBoundsAutoCompute(true);
		
		lightNode.addChild(lightSource);
		lightNode.addChild(ambientLight);
		//lightNode.compile();
		viewingPlatform.addChild(lightNode);
		viewingPlatform.addChild(background);
		updateScene();
		double[] clip_dists = getClipDistances();
		this.getView().setFrontClipDistance(clip_dists[0]);
		//this.getView().setBackClipDistance(clip_dists[1]);
		updateAxes();
		
		setDefaultTool(new ToolMouseOrbit3D(this));
		
	}
	
	public double[] getClipDistances() {
		
		return new double[] {((MguiDouble)attributes.getValue("ClipDistanceFront")).getValue(),
							 ((MguiDouble)attributes.getValue("ClipDistanceBack")).getValue()};
		
	}
	
	
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Window Resized")){
			updateAxes();
			resize_timer.stop();
			return;
			}
		
	}
	
	public void axesChanged(AxesEvent event){
		Axes3D axes = (Axes3D)event.getSource();
		setShowAxes(axes.isVisible());
		updateAxes();
		
	}
	
	public void setAxes(Axes3D axes){
		if (axes != null)
			axes.removeListener(this);
		this.axes = axes;
		
	}
	
	public void setMap(Map map){
		if (!(map instanceof Map3D)) return;
		if (theMap != null){
			((Map3D)theMap).getCamera().removeListener(this);
			}
		theMap = map;
		((Map3D)theMap).getCamera().addListener(this);
		
		
		
	}
	
	public View getView(){
		return viewer.getView();
	}
	
	public InterfaceCanvas3D getInterfaceCanvas3D(){
		return canvas3D;
	}
	
	public boolean getShowAxes(){
		//return ((MguiBoolean)attributes.getValue("ShowAxes")).getTrue();
		return axes.isVisible();
	}
	
	public void setShowAxes(boolean b){
		if (b != ((MguiBoolean)attributes.getValue("ShowAxes")).getTrue())
			attributes.setValue("ShowAxes",new MguiBoolean(b));
	}
	
	public float getAxesSize(){
		return axes.getAxesSize();
	}
	
	public void setAxesSize(float size){
		axes.setAxesSize(size);
	}
	
	public void repaintAxes(){
		getMap3D().updateTargetTransform();
	}
	
	public Map3D getMap3D(){
		return (Map3D)theMap;
	}
	
	public float getBoundsScale(){
		return ((MguiFloat)attributes.getValue("BoundsScale")).getFloat();
	}
	
	public void setBoundsScale(float scale){
		attributes.setValue("BoundsScale", new MguiFloat(scale));
	}
	
	public boolean getAutoCenter(){
		return ((MguiBoolean)attributes.getValue("AutoCenter")).getTrue();
	}
	
	public void setAutoCenter(boolean b){
		attributes.setValue("AutoCenter", new MguiBoolean(b));
	}
	
	@Override
	public boolean setDefaultTool(Tool3D tool){
		defaultTool = tool;
		defaultTool.setTargetPanel(this);
		toolInput3DAdapter.addListener(defaultTool);
		return true;
	}
	
	public void setBackgroundColour(Color c){
		attributes.setValue("Background", c);
		//updateBackground();
	}
	
	public void updateBackground(){
		Color c = getBackgroundColour();
		background.setColor(Colours.getColor3f(c));
		this.setBackground(c);
		canvas3D.canvas3D.setBackground(c);
	}
	
	public Camera3D getCamera(){
		if (getMap() != null) 
			return ((Map3D)getMap()).getCamera();
		return null;
	}
	
	public void setModel(ShapeModel3D m){
		//remove platform from current model if one exists
		if (model != null)
			model.removeGraphics3D(this);
			
		model = m;
		model.addGraphics3D(this);
		
		pickCanvasGeom = new PickCanvas(canvas3D.getCanvas(), model.getLocale());
		pickCanvasGeom.setFlags(PickInfo.CLOSEST_GEOM_INFO);
		pickCanvasGeom.setMode(PickInfo.PICK_GEOMETRY);
		pickCanvasGeom.setTolerance(getPickTolerance());
		pickCanvasNode = new PickCanvas(canvas3D.getCanvas(), model.getLocale());
		pickCanvasNode.setMode(PickInfo.PICK_GEOMETRY); 
		pickCanvasNode.setFlags(PickInfo.NODE | PickInfo.CLOSEST_INTERSECTION_POINT); 
		pickCanvasNode.setTolerance(getPickTolerance());
		
		updateTreeNodes();
	}
	
	protected float getPickTolerance(){
		return (float)((MguiFloat)attributes.getValue("PickTolerance")).getValue();
	}
	
	protected void setPickTolerance(float d){
		attributes.setValue("PickTolerance", new MguiFloat(d));
	}
	
	protected Vector2f getMinScreenScale(){
		return new Vector2f(getMinScreenScaleWidth(), getMinScreenScaleHeight());
	}
	
	protected float getMinScreenScaleWidth(){
		float dim = (float)this.getWidth();
		if (dim > min_screen_dim || dim <= 0) return 1;
		return dim / min_screen_dim;
	}
	
	protected float getMinScreenScaleHeight(){
		float dim = (float)this.getHeight();
		if (dim > min_screen_dim || dim <= 0) return 1;
		return dim / min_screen_dim;
	}
	
	public void updateAxes(){
		
		//axes.setVisible(getShowAxes());
		axes.setFromCamera(this.getCamera());
		
	}
	
	private TransformGroup getSphere(Point3f p){
		TransformGroup tg = new TransformGroup();
		Transform3D t3d = new Transform3D();
		t3d.setTranslation(new Vector3f(p));
		tg.setTransform(t3d);
		Sphere sphere = new Sphere(0.3f);
		tg.addChild(sphere);
		return tg;
	}
	
	/****************************************
	 * Resize axes on window resize, after given delay (to avoid excessive updates)
	 * 
	 */
	public void componentResized(ComponentEvent e) {
		resize_timer.restart();
	}
	
	public void cameraAngleChanged(CameraEvent e) {
		
		if (this.temp_popup_shape != null)
			this.model.removeTempShape(temp_popup_shape);
		
		//update axis rotation
		if (axes != null){
			updateAxes();
			return;
			}
		
	}

	public void cameraChanged(CameraEvent e) {
		// TODO Auto-generated method stub
		if (this.temp_popup_shape != null)
			this.model.removeTempShape(temp_popup_shape);
		
	}

	
	
	public ShapeModel3D getModel(){
		return model;
	}
	
	public PickCanvas getPickCanvasGeom(){
		return pickCanvasGeom;
	}
	
	public PickCanvas getPickCanvasNode(){
		return pickCanvasNode;
	}
	
	public ViewingPlatform getViewingPlatform(){
		return viewingPlatform;
	}
	
	//override mouse listeners to add to canvas3D instead
	public void addMouseListener(MouseListener m){
		canvas3D.addMouseListener(m);
	}
	
	public void addMouseMotionListener(MouseMotionListener m){
		canvas3D.addMouseMotionListener(m);
	}
	
	public void addMouseWheelListener(MouseWheelListener m){
		canvas3D.addMouseWheelListener(m);
	}
	
	/*
	public void setDisplayPanel(InterfaceDisplayPanel panel){
		displayPanel = panel;
		
	}
	*/
	
	public void attributeUpdated(AttributeEvent e) {
		Attribute<?> attribute = e.getAttribute();
		if (attribute.getName().equals("Background")){
			updateBackground();
			return;
			}
		if (attribute.getName().equals("BoundsScale") ||
				e.getAttribute().getName().equals("AutoCenter")	){
			updateScene();
			return;
			}
		if (attribute.getName().equals("ShowAxes")){
			axes.setVisible(((MguiBoolean)attribute.getValue()).getTrue());
			return;
			}
		if (attribute.getName().startsWith("ClipDistance")) {
			updateClipDistances();
			return;
			}
		if (attribute.getName().equals("AutoBackClip")) {
			this.getMap3D().setUpdateClipBounds(isAutoBackClip());
			updateClipDistances();
			return;
			}
			
	}
	
	protected void updateClipDistances() {
		
		this.getView().setFrontClipDistance(((MguiDouble)attributes.getValue("ClipDistanceFront")).getValue());
		if (isAutoBackClip()) {
			this.getMap3D().updateClipBounds();
			return;
			}
		
		this.getView().setBackClipDistance(((MguiDouble)attributes.getValue("ClipDistanceBack")).getValue());
		
	}
	
	public boolean isAutoBackClip() {
		return ((MguiBoolean)attributes.getValue("AutoBackClip")).getTrue();
	}
	
	public void updateDisplay(){
		
		this.getCamera().fireCameraAngleChanged();
		
	}
	
	public void updateScene(){
		if (model == null) return;
		updateScene(model.getModelSet());
	}
	
	public void updateScene(ShapeSet3DInt shapeSet){
		
		if (model.getExcludeToSelection())
			shapeSet = model.getExclusionFilter().getFilteredShapeSet3D(shapeSet);
		
		Sphere3D sphere = shapeSet.getBoundSphere(); 
		if (sphere == null || sphere.radius == 0) return;
		
		Map3D map = (Map3D)getMap();
		map.setBounds(new Sphere3D(sphere.center, sphere.radius * getBoundsScale()));
		if (((MguiBoolean)attributes.getValue("AutoCenter")).getTrue())
			map.setCenter(shapeSet.getCenterPoint());
		
		map.updateClipBounds();
		
	}
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		treeNode.add(attributes.issueTreeNode());
		//treeNode.add(new DefaultMutableTreeNode("Source: " + InterfaceSession.getDisplayPanel().getCurrentShapeSet().toString()));
		ShapeModel3D model = this.getModel();
		if (model != null) 
			treeNode.add(new InterfaceTreeNode("Source: " + this.getModel().getName()));
		else
			treeNode.add(new InterfaceTreeNode("Source: none"));
		
		treeNode.add(getCamera().issueTreeNode());
		treeNode.add(axes.issueTreeNode());
		
	}
	
	@Override
	public void shapeModelChanged(ShapeModelEvent event){
		if (event == null || model == null) return;
		
		switch (event.type){
		
			case NameChanged:
				if (title_panel != null)
					title_panel.updateTitle();
				break;
		
			case ModelDestroyed:
				model.removeGraphics3D(this);
				model = null;
				pickCanvasNode = null;
				pickCanvasGeom = null;
				this.canvas3D.canvas3D.repaint();
				break;
			
			}
		
		
	}
	
	public void shapeUpdated(ShapeEvent e){
		if (e == null || model == null) return;
		ShapeSet3DInt set = model.getModelSet();
		
		switch (e.eventType){
		
			case ShapeRemoved:
			case ShapeAdded:
			case ShapeModified:
			case ShapeSetModified:
				updateScene(set);
				
				//fires camera angle to update a camera listener
				//if (e.getSource() instanceof Camera3DListener){
				//InterfaceSession.log("Graphic3D: handling Volume3DInt modified.");
				getCamera().fireCameraListeners(CameraEvent.EventType.SceneChanged);
				getCamera().fireCameraAngleChanged();
				//}
				break;
				
			case TextureModified:
				canvas3D.getCanvas().addNotify();
				break;
				
//			case ModelDestroyed:
//				model.removeGraphics3D(this);
//				model = null;
//				pickCanvasNode = null;
//				pickCanvasGeom = null;
//				this.canvas3D.canvas3D.repaint();
//				break;
		}
		
	}
	
	@Override
	public boolean setCurrentTool(Tool3D thisTool){
		if (getToolLock()) return false;
		if (thisTool == null){
			if (currentTool != null && currentTool != defaultTool){
				toolInput3DAdapter.removeListener(currentTool);
				currentTool.removeListener(this);
				currentTool.deactivate();
				currentTool = defaultTool;
				defaultTool.addListener(this);
				}
			return true;
		}
		//if (!(thisTool instanceof Tool3D)) return false;
		
		Tool3D newTool = (Tool3D)thisTool.clone();
		if (newTool.isImmediate()){
			newTool.setTargetPanel(this);
			newTool.handleToolEvent(new ToolInputEvent(this,
									ToolConstants.TOOL_IMMEDIATE,
									new Point()));
			return true;
			}
		//Tool3D oldTool = null;
		if (currentTool != null && currentTool != defaultTool){
			toolInput3DAdapter.removeListener(currentTool);
			currentTool.removeListener(this);
			currentTool.deactivate();
			}
			
		currentTool = newTool;
		currentTool.setTargetPanel(this);
		toolInput3DAdapter.addListener(currentTool);
		if (defaultTool != null){
			if (currentTool.isExclusive())
				defaultTool.removeListener(this);
			else
				defaultTool.addListener(this);
			}
		currentTool.activate();
		//propertyChange.firePropertyChange("Current Tool", oldTool, currentTool);
		return true;
	}
	
	public void toolDeactivated(ToolEvent e){
		if (e.getTool() != currentTool) return;
		toolInput3DAdapter.removeListener(currentTool);
		currentTool.removeListener(this);
		setCurrentTool(defaultTool);
		
	}
	
	public Tool getTool(){
		return currentTool;
	}
	
	@Override
	public boolean isToolable(Tool tool){
		return tool instanceof Tool3D;
	}
	
	public boolean getToolLock(){
		return InterfaceSession.getDisplayPanel().getToolLock();
	}
	
	public void setToolLock(boolean val){
		InterfaceSession.getDisplayPanel().setToolLock(val);
	}
	
	//selection set for 2D shapes 
	public void setCurrentSelection2D(ShapeSelectionSet selSet, boolean exclude){
		currentSelection = selSet;
		excludeToSelection = exclude;
	}
	
	public String toString(){
		return "3D Panel: " + getName();
	}
	
	public String getTitle(){
		if (model == null)
			return getName();
		return getName() + " [" + model.getName() + "]";
	}
	
	public boolean setSource(Object source){
		if (!(source instanceof ShapeModel3D)) return false;
				
		setModel((ShapeModel3D)source);
		updateScene();
		((Map3D)getMap()).updateClipBounds();
		if (title_panel != null)
			title_panel.updateTitle();
		
		return true;
	}
	
	@Override
	public Object getSource(){
		return this.getModel();
	}
	

	public DefaultMutableTreeNode getDisplayObjectsNode(){
		//return all models in p
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Models");
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		for (int i = 0; i < models.size(); i++)
			node.add(new DefaultMutableTreeNode(models.get(i)));	
			
		return node;
	}
	
	public InterfacePopupMenu getPopupMenu(){
		
		InterfacePopupMenu menu = super.getPopupMenu();
		int start = super.getPopupLength();
		
		menu.add(new JSeparator(), start);
		menu.add(new JSeparator(), start);
		menu.addMenuItem(new JMenuItem("Graphic3D Window", getObjectIcon()));
		menu.add(new JSeparator(), start + 3);
		menu.add(new JSeparator(), start + 3);
		
		JMenu submenu = new JMenu("Set source");
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		int add = 0;
		if (models.size() > 0) add = 1;
		for (int i = 0; i < models.size(); i++){
			JMenuItem item = new JMenuItem(models.get(i).getName(), models.get(i).getObjectIcon());
			item.setActionCommand("Set source." + models.get(i).getName());
			submenu.add(item);
			}
		if (add == 1)
			menu.addSubmenu(submenu);
		
		JMenuItem item = new JMenuItem("Edit attributes..");
		item.setActionCommand("Window attributes");
		menu.addMenuItem(item);
		menu.addMenuItem(new JMenuItem("Orbit mode"));
		menu.addMenuItem(new JMenuItem("Draw mode"));
		menu.addMenuItem(new JMenuItem("Select mode"));
		//menu.add(new JSeparator(), start + 9);
		menu.addMenuItem(new JMenuItem("Center on model"));
		
		MguiInteger node = new MguiInteger(-1);
		
		// Query for shape and add its menu to this menu
		Shape3DInt shape = ShapeFunctions.getPickedShape(getPickCanvasNode(), 
													   	 getPickCanvasGeom(), 
													   	 last_click_point,
													   	 node);
		
		if (shape == null) return menu;
		
		if (node != null && node.getInt() > -1 && model != null) {
			float distance = (float)getCamera().getDistance();
			float node_size = distance / 300f;
			BranchGroup sphere = new BranchGroup();
			sphere.setCapability(BranchGroup.ALLOW_DETACH);
			
			sphere.addChild(ShapeFunctions.getSphereAtPoint(node_size, shape.getVertex(node.getInt())));
			setPopupShape(sphere);
			
			}
		
		menu.add(new JSeparator(), start + 10 + add);
		menu.add(new JSeparator(), start + 10 + add);
		menu.addMenuItem(new JMenuItem(shape.getName(), shape.getObjectIcon()));
		menu.add(new JSeparator(), start + 13 + add);
		menu.add(new JSeparator(), start + 13 + add);
		
		menu.addMenuItem(new JMenuItem("Center on vertex (" + node.getInt() + ")"));
		menu.addMenuItem(new JMenuItem("Center on shape"));
		menu.addMenuItem(new JMenuItem("Move sections to vertex (" + node.getInt() + ")"));
		
		shape.setGraphic3DPopupMenu(menu);
		
		return menu;
	}
	
	protected void setPopupShape(BranchGroup shape){
		if (temp_popup_shape != null && model != null){
			model.removeTempShape(temp_popup_shape);
			}
		model.addTempShape(shape);
		this.temp_popup_shape = shape;
	}
	
	@Override
	public Tool3D getCurrentTool() {
		return currentTool;
	}
	
	@Override
	public int updateStatusBox(InterfaceGraphicTextBox box, MouseEvent e){
		
		int index = super.updateStatusBox(box, e);
		if (index <= 0) return index;
		
		switch (index){
		
			case 1:
				Tool tool = getCurrentTool();
				if (tool == null)
					box.setText("Current tool: None");
				else
					box.setText("Current tool: " + tool.getName());
				break;
				
			case 2:
				box.setText("Zoom: " + MguiDouble.getString(getMap().getZoom(), "###,##0.00"));
				break;
		
			case 3:
				Map3D map = (Map3D)getMap();
				if (map == null) {
					box.setText("No map set.");
					return index;
					}
				box.setText("Center of rotation: " + mgui.numbers.NumberFunctions.getPoint3dStr(
													 map.getCamera().getCenterOfRotation(), "##0.000"));
				break;
			default:
				box.setText("");	
			}
		
		return index;
	}
	
	public void centerOnPoint(Point3f p){
		Map3D map = (Map3D)getMap();
		getCamera().setListen(true);
		map.setCenter(p);
		//getCamera().setCenterOfRotation(p);
		getCamera().setTranslateXY(new Vector2d(0,0));
		map.updateTargetTransform();
	}
	
	public void handlePopupEvent(ActionEvent e) {
	
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		
		if (item.getText().startsWith("Center on vertex")){
			if (last_click_point == null) return;
			MguiInteger node = new MguiInteger(-1);
			Shape3DInt picked_shape = ShapeFunctions.getPickedShape(getPickCanvasNode(), 
																   	getPickCanvasGeom(), 
																   	last_click_point,
																   	node);
			if (picked_shape == null || node.getInt() < 0) return;
			Point3f new_center = picked_shape.getShape().getVertex(node.getInt());
			if (new_center != null) centerOnPoint(new_center);
			return;
			}
		
		if (item.getActionCommand().startsWith("Set source")){
			String name = item.getActionCommand().substring(11);
			ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
			for (int i = 0; i < models.size(); i++)
				if (models.get(i).getName().equals(name)){
					this.setSource(models.get(i));
					return;
					}
			InterfaceSession.log("InterfaceGraphic3D: Could not set model '" + name + "' as source..", 
								 LoggingType.Errors);
			return;
			}
		
		if (item.getActionCommand().startsWith("Edit attributes")){
			InterfaceSession.getWorkspace().showAttributeDialog(this);
			return;
			}
		
		if (item.getActionCommand().equals("Edit Shape Attributes")){
			if (last_click_point == null) return;
			Shape3DInt picked_shape = ShapeFunctions.getPickedShape(getPickCanvasNode(), 
																   	getPickCanvasGeom(), 
																   	last_click_point,
																   	null);
			if (picked_shape == null)
				return;
			InterfaceSession.getWorkspace().showAttributeDialog(picked_shape);
			return;
			}
		
		if (item.getText().equals("Center on shape")){
			if (last_click_point == null) return;
			Shape3DInt picked_shape = ShapeFunctions.getPickedShape(getPickCanvasNode(), 
																   	getPickCanvasGeom(), 
																   	last_click_point,
																   	null);
			if (picked_shape == null)
				return;
			
			Point3f new_center = picked_shape.getCenterOfGravity();
			if (new_center != null) centerOnPoint(new_center);
			return;
		}
		
		if (item.getText().equals("Center on model")){
			Point3f new_center = this.getModel().getModelSet().getCenterOfGravity();
			if (new_center != null) centerOnPoint(new_center);
			return;
		}
		
		if (item.getText().startsWith("Move sections to vertex")){
			MguiInteger node = new MguiInteger(-1);
			Shape3DInt picked_shape = ShapeFunctions.getPickedShape(getPickCanvasNode(), 
																   	getPickCanvasGeom(), 
																   	last_click_point,
																   	node);
			if (picked_shape == null || node.getInt() < 0) return;
			Point3f point = picked_shape.getShape().getVertex(node.getInt());
			
			if (last_click_point == null || point == null)
				return;
			
			ArrayList<InterfaceGraphic2D> windows = picked_shape.getModel().getModelSet().getSectionWindows();
			for (int i = 0; i < windows.size(); i++){
				windows.get(i).moveSectionTo(point);
				}
			
			return;
			}
		
		Shape3DInt picked_shape = ShapeFunctions.getPickedShape(getPickCanvasNode(), 
			   	getPickCanvasGeom(), 
			   	last_click_point,
			   	null);
		
		if (picked_shape != null) picked_shape.handlePopupEvent(e);
		
		if (item.getText().equals("Snapshot..")){
			
			JFileChooser jc = null;
			if (last_screen_shot_file != null)
				jc = new JFileChooser(last_screen_shot_file.getParentFile());
			else
				jc = new JFileChooser();
				
			ArrayList<String> ext = new ArrayList<String>();
			ext.add("png");
			jc.setFileFilter(IoFunctions.getFileChooserFilter(ext, "Portable Network Graphics files (*.png)"));
			jc.showSaveDialog(InterfaceSession.getSessionFrame());
			
			File file = jc.getSelectedFile();
			if (file == null) return;
			
			switch (InterfaceEnvironment.getSnapshot3DMode()){
				case ScreenCapture:
					writeSnapshotToFile(file);
					break;
				case OffscreenBuffer:
					if (!writeToFileBuffer(file)){
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Error writing to '" + file.getAbsolutePath() + "'..", 
													  "Error taking snapshot",
													  JOptionPane.ERROR_MESSAGE);
					}else{
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
						  		  "Snapshot written to '" + file.getAbsolutePath() + "'..", 
						  		  "Window Snapshot",
						  		  JOptionPane.INFORMATION_MESSAGE);
						last_screen_shot_file = file;
						}
					break;
				}
			
			
			
			return;
			}
		
		
		
		super.handlePopupEvent(e);
		
	}
	
	/*********************************
	 * Writes this window to a png image file.
	 * @return true if successful
	 */
	public boolean writeToFileBuffer(File file){
		
		InterfaceCanvas3D int_canvas = getInterfaceCanvas3D();
		Canvas3D canvas = int_canvas.getCanvas();
		Dimension size = canvas.getSize();
		
		float scale = ((MguiFloat)attributes.getValue("SnapshotScale")).getFloat();
//		int_canvas.setOffScreenCanvas((int)((float)size.width * scale), 
//									  (int)((float)size.height * scale));
		
		BufferedImage buffer_image = int_canvas.getScreenShot(scale);
		
		try{
			ImageIO.write(buffer_image, "png", file);
		}catch (IOException e){
			InterfaceSession.handleException(e);
			return false;
			}
		
		return true;
	}
	
	/*************************
	 * Temp screenshot while off screen rendering doesn't work...
	 */
	public boolean writeSnapshotToFile(final File file){
		
		try{
			SnapShotTask task = new SnapShotTask(file);
			post_render_tasks.put("Snapshot", task);
			return true;
		}catch (Exception e){
			e.printStackTrace();
			return false;
			}
	}
	
	protected boolean writeToFile(BufferedImage image, File file){
		try{
			ImageIO.write(image, "png", file);
			return true;
		}catch (IOException e){
			e.printStackTrace();
			return false;
			}
	}
	
	private class SnapShotTask{
		public File file;
		public SnapShotTask(File file){
			this.file = file;
		}
	}
	
	
	/***********************************
	 * Executes any post-rendering tasks, called through the <code>postSwap</code> method of
	 * {@link PostRenderingCanvas3D}.
	 * 
	 */
	public void postRender(){
		if (post_render_tasks.size() > 0) {
		
			Iterator<String> itr = post_render_tasks.keySet().iterator();
			
			while (itr.hasNext()){
				
				String key = itr.next();
				if (key.equals("Snapshot")){
					SnapShotTask task = (SnapShotTask)post_render_tasks.get(key);
					try{
						//take snapshot now
						BufferedImage image = null;
						Canvas3D canvas = canvas3D.getCanvas();
						Point p = new Point();
						p = canvas.getLocationOnScreen();
						Rectangle bounds = new Rectangle(p.x, p.y, canvas.getWidth(), canvas.getHeight());
						Robot robot = new Robot(getGraphicsConfiguration().getDevice());
						image = robot.createScreenCapture(bounds);
						ImageIO.write(image, "png", task.file);
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
											  		  "Snapshot written to '" + task.file.getAbsolutePath() + "'..", 
											  		  "Window Snapshot",
											  		  JOptionPane.INFORMATION_MESSAGE);
						last_screen_shot_file = task.file;
					}catch (Exception e){
						//e.printStackTrace();
						InterfaceSession.handleException(e);
						JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
													  "Error writing to '" + task.file.getAbsolutePath() + "'..", 
													  "Error taking snapshot",
													  JOptionPane.ERROR_MESSAGE);
						}
					}
				
				}
			}
		
		//draw axes
		if (getShowAxes()){
			
			Canvas3D canvas = canvas3D.getCanvas();
			axes.render(canvas);
			
			}
		
		//clear tasks
		post_render_tasks = new HashMap<String, Object>();
	}
	
	
	
	//************************ XML Stuff *****************************************
	
	@Override
	public String getDTD() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getShortXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXML(int tab) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getXMLSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleXMLElementEnd(String localName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String model_str = "null";
		if (model != null)
			model_str = model.getName();
		
		writer.write(_tab + "<InterfaceGraphic3D\n" + _tab2 +
							" name='" + getName() + "'\n" + _tab2 +
							" source='" + model_str + "'\n" + _tab +
							">\n");
		
		Camera3D camera = this.getCamera();
		camera.writeXML(tab + 1, writer, options, progress_bar);
		
		attributes.writeXML(tab + 1, writer, options, progress_bar);
		
		writer.write(_tab + "</InterfaceGraphic3D>\n");
		
	}
	
}