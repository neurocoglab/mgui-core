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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector2f;
import org.xml.sax.Attributes;

import mgui.geometry.Plane3D;
import mgui.geometry.Rect2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.graphics.GraphicEvent.EventType;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.graphics.util.PickInfoShape2D;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.Map;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.maps.MapEvent;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.selection.ShapeSelectionSet;
import mgui.interfaces.shapes.util.ClipPlane;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolEvent;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.graphics.Tool2D;
import mgui.interfaces.tools.graphics.ToolDZoom2D;
import mgui.interfaces.tools.graphics.ToolWindowZoom2D;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;


/**************************
 * Graphical interface window for display 2D shapes (Shape2DInt). Shapes must be contained
 * by a SectionSet3DInt object to be displayed.
 * 
 * <p>TODO: implement using a {@link JLayeredPane}, allowing annotations, etc.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class InterfaceGraphic2D extends InterfaceGraphic<Tool2D> implements ShapeListener{

	//declare globals here
	protected DrawingEngine drawEngine;
	public Tool2D currentTool, defaultTool;
	protected ArrayList<Shape2DInt> temp_shapes = new ArrayList<Shape2DInt>();
	private boolean drawMouseOnly;
	private PropertyChangeSupport propertyChange = new PropertyChangeSupport(this);
	public ShapeSelectionSet currentSelection;
	public SectionSet3DInt currentSections;
	private boolean isDestroyed = false;
	protected ShapeSet2DInt shape3DObjects;
	protected ArrayList<ShapeSet2DInt> childSets = new ArrayList<ShapeSet2DInt>();	//keeps track of all child shape sets so that they can be properly destroyed 
	public ArrayList<Graphic2DGrid> grids = new ArrayList<Graphic2DGrid>();
	protected ArrayList<InterfaceGraphicListener> graphicListeners = new ArrayList<InterfaceGraphicListener>();

	ArrayList<ClipPlane> clip_planes = new ArrayList<ClipPlane>();
	
	protected transient boolean needs_update = false;
	protected transient boolean needs_regen = false;
	protected transient boolean section_changed = false;
	
	//mouse coordinates
	Point mouseCoords;
	
	//MouseListeners
	public ArrayList<Shape2DInt> shapeList;
	
	/******************************************
	 * 
	 * Instantiate and initialize a new 2D graphics window called {@code name}.
	 * 
	 * @param name
	 */
	public InterfaceGraphic2D(String name){
		super();
		if (InterfaceSession.isInit()){
			init();
			setName(name);
			}
	}
	
	/******************************************
	 * 
	 * Instantiate and initialize a new 2D graphics window.
	 * 
	 */
	public InterfaceGraphic2D(){	
		if (InterfaceSession.isInit())
			init();
	}
	
	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceGraphic2D.class.getResource("/mgui/resources/icons/window_2d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/window_2d_20.png");
		return null;
	}
	
	@Override
	protected void init(){
		if (init_once) return;
		super.init();
		_init();
		
		setDefaultTool(new ToolDZoom2D());
		
		//type
		type = "Graphic2D";
		
		//set up stuff here
		//panel display
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.setBackground(Color.WHITE);
		
		//tool input adapter
		this.addMouseListener(toolInputAdapter.getMouseAdapter());
		this.addMouseMotionListener(toolInputAdapter.getMouseAdapter());
		this.addMouseWheelListener(toolInputAdapter.getMouseAdapter());
		
		//set default grid
		addGrid(new Graphic2DGrid());
		
		//set critical stuff
		setMap(new Map2D(getSize(), 200.0, new Point2f(0.0f, 0.0f)));
		drawEngine = new DrawingEngine((Map2D)getMap());
		shapeList = new ArrayList<Shape2DInt>();
		
		attributes.add(new Attribute<SectionSet3DInt>("CurrentSectSet", SectionSet3DInt.class));
		attributes.add(new Attribute<MguiInteger>("CurrentSect", new MguiInteger(0)));
		attributes.add(new Attribute<MguiDouble>("CurrentWidth", new MguiDouble(-1)));
		attributes.add(new Attribute<ShapeSet2DInt>("CurrentShpSet", new ShapeSet2DInt()));
		attributes.add(new Attribute<MguiBoolean>("ShowBorder", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowGrids", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiInteger>("BorderWidth", new MguiInteger(2)));
		attributes.add(new Attribute<MguiFloat>("PickTolerance", new MguiFloat(5)));
		attributes.add(new Attribute<String>("ZoomLimits", "0.01 1000"));
		
	}
	
	/***********************************
	 * 
	 * Return a list of {@linkplain PickInfoShape2D} objects containing information about
	 * the shapes intersecting {@code point}. Shapes will be sorted according to render order.
	 * 
	 * @param point
	 * @return
	 */
	public ArrayList<PickInfoShape2D> getPickShapes(Point point){
		
		if (getCurrentSectionSet() == null) return null;
		
		ArrayList<PickInfoShape2D> info = new ArrayList<PickInfoShape2D>();
		float tolerance = ((Map2D)this.getMap()).getMapDist(((MguiFloat)attributes.getValue("PickTolerance")).getValue());
		Point2f p2f = ((Map2D)this.getMap()).getMapPoint(point);
		
		ArrayList<Shape2DInt> shapes = new ArrayList<Shape2DInt>(shapeList);
		if (shape3DObjects != null) {
			shapes.addAll(this.shape3DObjects.get2DShapes(true));
			}
		
		//search list
		for (int i = 0; i < shapes.size(); i++){
			Shape2DInt shape = shapes.get(i);
			if (shape.isVisible() && shape.show2D() && shape.isSelectable()){
				//get expanded bounds
				Rect2D bounds = shape.getBounds();
				
				if (bounds != null){
					Vector2f diag = new Vector2f(bounds.corner2);
					diag.sub(bounds.corner1);
					diag.normalize();
					diag.scale(tolerance);
					bounds.corner2.add(diag);
					bounds.corner1.sub(diag);
					if (bounds.contains(p2f)){
						int closest = -1;
						float distance = -1;
						if (shape.hasParentShape()){
							closest = shape.getClosestVertex3D(p2f, getCurrentPlane());
							if (closest > -1){
								Point2f vertex = GeometryFunctions.getProjectedPoint(shape.getParentShape().getVertex(closest), getCurrentPlane());
								distance = vertex.distance(p2f);
								}
						}else{
							closest = shape.getClosestVertex(p2f);
							if (closest > -1){
								distance = p2f.distance(shape.getVertex(closest));
								}
							}
						if (distance >= 0){
							if (distance > tolerance){
								//TODO: determine if point is internal
								//		or close to an edge
							//	if (shape.getShape().contains(p2f))
								info.add(new PickInfoShape2D(shape, closest, distance));
							}else{
								info.add(new PickInfoShape2D(shape, closest, distance));
								}
							}
						}
					}
				}
			}
		
		//Collections.sort(info);
		return info;
		
	}
	
	/***************************************************
	 * Set the current section to the one which is closest to {@code point}
	 * 
	 * @param point
	 */
	public void moveSectionTo(Point3f point){
		
		SectionSet3DInt set = getCurrentSectionSet();
		int section = set.getClosestSection(point);
		this.setCurrentSection(section);
		
	}
	
	/*******************************************
	 * Centers the section at the specified 2D point
	 * 
	 * @param point
	 */
	public void centerSectionAt(Point2f point){
		
		Map2D map = (Map2D)this.getMap();
		map.centerOnPoint(point);
		needs_update = true;
		this.updateDisplay();
		
	}
	
	/*******************************************
	 * Centers the section at the specified 3D point, projected onto
	 * its plane
	 * 
	 * @param point
	 */
	public void centerSectionAt(Point3f point){
		
		Plane3D plane = this.getCurrentPlane();
		if (plane == null) return;
		Point2f p2d = GeometryFunctions.getProjectedPoint(point, plane);
		if (p2d == null) return;
		centerSectionAt(p2d);
		
	}
	
	public ShapeModel3D getShapeModel(){
		return getCurrentSectionSet().getModel();
	}
	
	@Override
	public void destroy(){
		super.destroy();
		if (this.currentSections != null)
			this.currentSections.removeShapeListener(this);
		//update window listeners
		fireGraphicListeners(new GraphicEvent(this, EventType.Destroyed));
	}
	
	@Override
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	void updateClipPlanes(){
		ArrayList<ClipPlane> planes = new ArrayList<ClipPlane>(clip_planes);
		for (int i = 0; i < planes.size(); i++){
			if (planes.get(i).isDestroyed())
				clip_planes.remove(planes.get(i));
			else
				clip_planes.get(i).setFromSectionSet(getCurrentSectionSet(), getCurrentSection());
		}
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		
		if (e.getAttribute().getName().equals("CurrentSectSet")){
			fireGraphicListeners(new GraphicEvent(this, EventType.NewSource));
			return;
			}
		
		if (e.getAttribute().getName().equals("CurrentSect")){
			
			set3DObjects();
			needs_update = true;
			updateDisplay();
			
			if (section_changed){
				section_changed = false;
				fireGraphicListeners(new GraphicEvent(this, EventType.Modified));
				}
			fireDisplayListeners();
			return;
			}
		
		if (e.getAttribute().getName().equals("LineColour")){
			//line colour refers to 3D section node
			fireGraphicListeners(new GraphicEvent(this, EventType.Modified));
			return;
			}
		
		if (e.getAttribute().getName().equals("Background")){
			this.setBackground(getBackgroundColour());
			this.regenerateDisplay();
			return;
			}
		
		if (e.getAttribute().getName().contains("Border")){
			updateBorder();
			needs_update = true;
			}
		
		if (e.getAttribute().getName().equals("ShowGrids")){
			needs_update = true;
			}
		
		if (needsRedraw(e.getAttribute())) updateDisplay();
	}
	
	void updateBorder(){
		if (((MguiBoolean)attributes.getValue("ShowBorder")).getTrue() && getCurrentSectionSet() != null){
			this.setBorder(BorderFactory.createLineBorder(
					(Color)getCurrentSectionSet().getInheritedAttributeValue("3D.LineColour"),
					((MguiInteger)attributes.getValue("BorderWidth")).getInt()));
		}else{
			this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			}
	}
	
	public boolean needsRedraw(Attribute<?> a){
		if (a.getName().equals("Background") ||
			a.getName().startsWith("Current") ||
			a.getName().contains("Border") ||
			a.getName().equals("ShowGrids"))
			return true;
		return false;
	}
	
	@Override
	public void paintComponent(Graphics g){
		needs_update = false;
		
		((Map2D)getMap()).setScreenBounds(getSize());
		Graphics2D g2 = (Graphics2D)g;
		
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		        			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    						RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (drawMouseOnly){
			for (int i = 0; i < this.status_listeners.size(); i++){
				if (status_listeners.get(i).isShape()){
					g2.setClip(drawEngine.getScreenBounds(((Shape2DInt)status_listeners.get(i)).getBounds()));
					super.paintComponent(g2);
					((Shape2DInt)status_listeners.get(i)).drawShape2D(g2, drawEngine);
					}
				}
			drawMouseOnly = false;
			return;
			}

		Border border = this.getBorder();
		if (is_snapshot) {
			// Don't paint border for snapshot
			this.setBorder(null);
			}
		
		super.paintComponent(g2);
		
		if (is_snapshot) {
			this.setBorder(border);
			}
		
		if (this.getCurrentSectionSet() == null) return;
		
		SectionSet3DInt sectionSet = getCurrentSectionSet();
		if (sectionSet.getModel() != null){
			excludeToSelection = sectionSet.getModel().getExcludeToSelection();
			if (excludeToSelection)
				currentSelection = sectionSet.getModel().getExclusionFilter();
			}
		
		
		/**@todo set this panel to a specific SectionSet3DInt object **/
		//3D stuff
		//first pass for images
		if (shape3DObjects == null){
			shape3DObjects = new ShapeSet2DInt();
			shape3DObjects.addShapeListener(this);
			}
		
		for (int i = shape3DObjects.members.size() - 1; i > -1 ; i--)
			if (shape3DObjects.members.size() > i &&  				// Concurrency issues
				shape3DObjects.members.get(i).isImageShape() &&
				shape3DObjects.members.get(i).isVisible() &&
				shape3DObjects.members.get(i).show2D()){
				shape3DObjects.members.get(i).drawShape2D(g2, drawEngine);
				}
		
		//2D stuff
		if (sectionSet.isVisible() && sectionSet.show2D()){
			ShapeSet2DInt set2d = getShapeSet2D();
			for (int i = set2d.getSize() - 1; i >-1 ; i--){
				if (set2d.isVisible(i) && set2d.show2D())
					if (!excludeToSelection)
						set2d.drawMember2D(i, g2, drawEngine);
					else {
						if (currentSelection != null){
							if (currentSelection.hasShape(set2d.getMember(i)))
								set2d.drawMember2D(i, g2, drawEngine);
						}
					}
				}
			}
		
		//second pass 3D shapes; non-images and non-labels
		for (int i = shape3DObjects.members.size() - 1; i > -1 ; i--)
			if (shape3DObjects.members.size() > i && 			// Concurrency issues
					!shape3DObjects.members.get(i).isImageShape() &&
					!shape3DObjects.members.get(i).isLabelShape() &&
					shape3DObjects.members.get(i).isVisible() &&
					shape3DObjects.members.get(i).show2D()){
				shape3DObjects.members.get(i).drawShape2D(g2, drawEngine);
				}
		
		//third pass 3D shapes; label shapes
		for (int i = shape3DObjects.members.size() - 1; i > -1 ; i--)
			if (shape3DObjects.members.get(i).isLabelShape() &&
					shape3DObjects.members.get(i).isVisible() &&
					shape3DObjects.members.get(i).show2D()){
				shape3DObjects.members.get(i).drawShape2D(g2, drawEngine);
				}
		
		//temporary shape(s)
		if (temp_shapes.size() > 0){
			for (int i = temp_shapes.size() - 1; i > -1; i--){
				temp_shapes.get(i).drawShape2D(g2, drawEngine);
				}
			}
		
		//draw local stuff
		for (int i = 0; i < shapeList.size(); i++)
			if (shapeList.get(i).isVisible() && shapeList.get(i).show2D())
				shapeList.get(i).drawShape2D(g2, drawEngine);
		
		//draw grids if necessary
		if (((MguiBoolean)attributes.getValue("ShowGrids")).getTrue()){
			for (int i = 0; i < grids.size(); i++)
				if (grids.get(i).getIsVisible())
					grids.get(i).draw2D(g2, drawEngine);
			}
		
		//draw border last
		
		
		//sectionChanged = false;
		g2.setPaint(Color.BLACK);
		g2.setStroke(new BasicStroke());

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
				Map2D map = (Map2D)getMap();
				if (map == null) {
					box.setText("No map set.");
					break;
					}
				
				if (e == null){
					box.setText("Mouse coords: ?, ?");
					break;
					}
				
				Point2f theseCoords = map.getMapPoint(e.getPoint());
				box.setText("Mouse coords: " + new MguiDouble(theseCoords.x).toString("##0.000") + ", "
											 + new MguiDouble(theseCoords.y).toString("##0.000"));
				break;
			default:
				box.setText("");	
			}
		
		return index;
	}
	
	public void addGrid(Graphic2DGrid grid){
		grids.add(grid);
		grid.addDisplayListener(this);
	}
	
	public void removeGrid(Graphic2DGrid grid){
		grids.remove(grid);
		grid.removeDisplayListener(this);
	}
	
	/******************************
	 * 
	 * Sets the 2D section-projected shapes from the 3D objects in the current model.
	 * 
	 */
	protected void set3DObjects(){
		SectionSet3DInt section_set = getCurrentSectionSet();
		if (shape3DObjects != null){
			shape3DObjects.removeShapeListener(this);
			shape3DObjects.destroy();
			}
		destroyChildSets();
		shape3DObjects = new ShapeSet2DInt();
		shape3DObjects.addShapeListener(this);
		
		if (section_set == null || section_set.getModel() == null) return;
		ShapeSet2DInt set3D = (ShapeSet2DInt)section_set.getModel().getModelSet().getShape2DInt(getCurrentPlane(), 
																							    section_set.getClipDistUp(), 
																							    section_set.getClipDistDown(), 
																							    false);
		
		if (set3D == null) return;
		addShapes3D(set3D);
		
	}
	
	void destroyChildSets(){
		if (childSets == null) return;
		for (int i = 0; i < this.childSets.size(); i++){
			childSets.get(i).removeShapeListener(this);
			childSets.get(i).destroy();
			}
		childSets.clear();
	}
	
	void addShapes3D(ShapeSet2DInt shapes){
		childSets.add(shapes);
		shapes.addShapeListener(this);
		ArrayList<InterfaceShape> members = shapes.getMembers();
		//add members from bottom to top to respect render order
		for (int i = members.size() - 1; i >= 0; i--){
			Shape2DInt shape = (Shape2DInt)members.get(i);
			if (shape instanceof ShapeSet2DInt){
				addShapes3D((ShapeSet2DInt)shape);
				
			}else{
				insertShape3D(shape);
				}
			}
	}
	
	//inserts a 3D shape (images first, otherwise last) to the list of drawables	
	public void insertShape3D(Shape2DInt shape){
		if (shape == null) return;
		if (shape instanceof ShapeSet2DInt){
			((ShapeSet2DInt)shape).notifyListeners = false;
			addShapes3D((ShapeSet2DInt)shape);
			((ShapeSet2DInt)shape).notifyListeners = true;
			return;
			}
		if (shape.isImageShape()) 
			shape3DObjects.addShape(shape, 0, true, false);
		else
			shape3DObjects.addShape(shape, true, false);
		
	}
	
	public void removeShape3D(Shape2DInt shape){
		shape3DObjects.removeShape(shape, false, false);
		if (shape.getParentShape() != null)
			shape.getParentShape().removeShape2DChild(shape);
	}
	
	public void addGraphicListener(InterfaceGraphicListener l){
		for (int i = 0; i < graphicListeners.size(); i++)
			if (graphicListeners.get(i) == l) return;
		graphicListeners.add(l);
	}
	
	public void removeGraphicListener(InterfaceGraphicListener l){
		graphicListeners.remove(l);
	}
	
	@Override
	public void updateDisplay(){
		if (!needs_update && !needs_regen) return;
		if (needs_regen){
			regenerateDisplay();
			return;
			}
		needs_update = false;
		/**@TODO turn off fills, images, etc., for a fast dynamic update here
		         using a boolean switch **/
		Graphics2D g2 = (Graphics2D)this.getGraphics();
		
		if (g2 != null){
			repaint();
			}
		else{
			return;
		}
	}
	
	@Override
	public void updateDisplays(){
		InterfaceSession.getDisplayPanel().updateDisplays();
	}
	
	public void updateCurrentSectionSet(){
		updateCurrentSectionSet(false);
	}
	
	public void updateCurrentSectionSet(boolean update){
		SectionSet3DInt thisSet = getCurrentSectionSet();
		if (thisSet == null) return;
		thisSet.updateShape();
		//thisSet.resetModel();
		
		if (update){
			
			updateDisplays();
			}
	}
	
	public Plane3D getCurrentPlane(){
		SectionSet3DInt set = getCurrentSectionSet();
		if (set == null) return null;
		return set.getPlaneAt(getCurrentSection());
	}
	
	/****************************************
	 * Append a temporary shape to the bottom of the stack (renders first)
	 * 
	 * @param shape
	 */
	public void appendTempShape(Shape2DInt shape){
		temp_shapes.add(shape);
	}
	
	/****************************************
	 * Insert a temporary shape at the top of the stack (renders last)
	 * 
	 * @param shape
	 */
	public void insertTempShape(Shape2DInt shape){
		temp_shapes.add(0, shape);
	}
	
	/****************************************
	 * Insert a temporary shape at {@code pos} in the stack
	 * 
	 * @param shape
	 */
	public void insertTempShape(Shape2DInt shape, int pos){
		temp_shapes.add(pos, shape);
	}
	
	/****************************************
	 * Remove all temporary shapes from the stack
	 * 
	 */
	public void clearTempShapes(){
		temp_shapes.clear();
	}
	
	/****************************************
	 * Remove temporary shape {@code shape} from the stack
	 * 
	 * @param shape
	 */
	public void removeTempShape(Shape2DInt shape){
		temp_shapes.remove(shape);
	}
	
	public void drawShape2D(Shape2DInt thisShape){
		Graphics2D g2 = (Graphics2D)this.getGraphics();
		if (g2 == null || drawEngine == null) return;
		//drawEngine.setCoordSys(((arInteger)thisShape.getAttribute("CoordSys").getValue()).value);
		g2.setClip(drawEngine.getScreenBounds(thisShape.getExtBounds()));
		paintComponent(g2);
		g2.setClip(null);
		paintBorder(g2);
	}
	
	public void drawMouseShapes(){
		Graphics2D g2 = (Graphics2D)this.getGraphics();
		drawMouseOnly = true;
		paintComponent(g2);
	}
	
	public void addShapeInt(Shape2DInt thisShape){
		shapeList.add(thisShape);
		thisShape.updateShape();
		thisShape.idStr = String.valueOf(shapeList.size() - 1);
		//thisShape.setParent(this);
	}
	
	public void addShapeInt(Shape2DInt thisShape, int pos){
		shapeList.add(pos, thisShape);
		thisShape.updateShape();
		thisShape.idStr = String.valueOf(pos);
		//thisShape.setParent(this);
	}
	
	public void addModelShape2D(Shape2DInt thisShape, boolean update){
		addModelShape2D(thisShape, update, true);
	}

	public void addModelShape2D(Shape2DInt thisShape, boolean updateShape, boolean updateListeners){
		//thisShape.updateShape();
		addShape2D(thisShape, updateShape, updateListeners);
	}
	
	public void drawMouseObjects(){

	}
	
	public void addPropertyChangeObject(GraphicPropertyListener thisObj){
		addPropertyChangeListener(thisObj.getPropertyListener());
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener thisListener){
		if (propertyChange == null) return;
		propertyChange.addPropertyChangeListener(thisListener);
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener thisListener){
		propertyChange.removePropertyChangeListener(thisListener);
	}
	
	public boolean setDefaultTool(Tool2D tool){
		if (defaultTool != null){
			defaultTool.removeListener(this);
			toolInputAdapter.removeListener(defaultTool);
			}
		defaultTool = tool;
		tool.addListener(this);
		toolInputAdapter.addListener(tool);
		tool.setTargetPanel(this);
		return true;
	}
	
	@Override
	public boolean setCurrentTool(Tool2D thisTool){
		if (getToolLock()) return false;
		if (thisTool == null || getCurrentSectionSet() == null) return false;
		
		Tool2D newTool = (Tool2D)thisTool.clone();
		if (newTool.isImmediate()){
			newTool.setTargetPanel(this);
			newTool.handleToolEvent(new ToolInputEvent(this,
														ToolConstants.TOOL_IMMEDIATE,
														new Point()));
			return true;
			}
		Tool2D oldTool = null;
		
		if (currentTool != null && currentTool != defaultTool){
			toolInputAdapter.removeListener(currentTool);
			currentTool.removeListener(this);
			currentTool.deactivate();
			oldTool = (Tool2D)currentTool.clone();
			}
		currentTool = newTool;
		currentTool.setTargetPanel(this);
		currentTool.addListener(this);
		toolInputAdapter.addListener(currentTool);
		propertyChange.firePropertyChange("Current Tool", oldTool, currentTool);
		if (defaultTool != null){
			if (currentTool.isExclusive()){
				defaultTool.removeListener(this);
			}else{
				defaultTool.addListener(this);
				}
			}
		currentTool.activate();
		return true;
	}
	
	@Override
	public void toolStateChanged(ToolEvent event){
		
		switch(event.getType()){
		
			case ToolDeactivated:
				Tool tool = event.getTool();
				if (tool == currentTool)
					finishTool();
				
				break;
		
			default:
				
				break;
				
			}
		
	}
	
	@Override
	public boolean isToolable(Tool tool){
		return tool instanceof Tool2D;
	}
	
	public void finishTool(){
		if (getToolLock())
			setToolLock(false);
		
		if (currentTool != null && currentTool != defaultTool){
			currentTool.deactivate();
			toolInputAdapter.removeListener(currentTool);
			}
		
		if (currentTool != defaultTool){
			Tool2D oldTool = currentTool;
			currentTool.removeListener(this);
			currentTool.deactivate();
			if (currentTool.getPreviousTool() != null)
				currentTool = (Tool2D)currentTool.getPreviousTool();
			else
				currentTool = defaultTool;
			if (defaultTool != null){
				defaultTool.addListener(this);
				defaultTool.addListener(this);
				}
			propertyChange.firePropertyChange("Current Tool", oldTool, currentTool);
			}
		
		updateDisplay();
	}
	
	@Override
	public Tool2D getCurrentTool(){
		return currentTool;
	}
	
	public Shape2DInt pickShape(Point pickPt, int pickRadius){
		//search only objects whose bounds include pickPt
		Map2D map = (Map2D)getMap();
		Point2f mapPt = map.getMapPoint(pickPt);
		double mapRadius = map.getMapDist(pickRadius);
		double thisProx;
		double minProx = Double.MAX_VALUE;
		Shape2DInt retShape = null;
		//for (int i = 0; i < displayPanel.getShapeSet2D().getSize(); i++)
		int i;
		try{
			
		for (i = 0; i < getShapeSet2D().getSize(); i++)
			if (((MguiInteger)getShapeSet2D().getShape(i).getAttribute("CoordSys").getValue()).getInt() != DrawingEngine.DRAW_SCREEN)
				if (getShapeSet2D().getShape(i).contains(mapPt)){
					thisProx = getShapeSet2D().getShape(i).getProximity(mapPt);
					if (thisProx < minProx && thisProx < mapRadius && thisProx > 0)
						retShape = getShapeSet2D().getShape(i);
					}
		}catch (Exception e){
			e.printStackTrace();
			}
		return retShape;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		treeNode.add(attributes.issueTreeNode());
		InterfaceTreeNode gridNode = new InterfaceTreeNode("Grids");
		
		for (int i = 0; i < grids.size(); i++)
			gridNode.add(grids.get(i).issueTreeNode());
		
		treeNode.add(gridNode);
	}
	
	@Override
	public String toString(){
		return "2D Panel: " + getName();
	}
	
	@Override
	public String getTitle(){
		SectionSet3DInt section_set = getCurrentSectionSet();
		if (section_set == null)
			return getName();
		
		int section = this.getCurrentSection();
		String dist = "" + MguiFloat.getString(section * section_set.getSpacing(), 3) + " " + section_set.getUnit().getShortName();
		return getName() + " [" + dist + "]";
	}
	
	public boolean getToolLock(){
		InterfaceDisplayPanel displayPanel = InterfaceSession.getDisplayPanel();
		if (displayPanel != null)
			return displayPanel.getToolLock();
		return false;
	}
	
	public void setToolLock(boolean val){
		InterfaceDisplayPanel displayPanel = InterfaceSession.getDisplayPanel();
		if (displayPanel != null)
			displayPanel.setToolLock(val);
	}
	
	public void setCurrentSelection(ShapeSelectionSet thisSet, boolean exclude){
		currentSelection = thisSet;
		excludeToSelection = exclude;
	}
	
	public void setCurrentSectionSet(SectionSet3DInt set){
		SectionSet3DInt cSet = (SectionSet3DInt)attributes.getAttribute("CurrentSectSet").getValue();
		if (cSet != null && cSet == set) return;
		if (cSet != null)
			cSet.removeShapeListener(this);
		if (set == null){
			attributes.getAttribute("CurrentSectSet").setValue(null);
			return;
			}
		attributes.getAttribute("CurrentSectSet").setValue(set);
		setCurrentSection(0);
		set.addShapeListener(this);
	}
	
	/**************************
	 * 
	 * Sets the current section set for this graphic object.
	 * 
	 * @return
	 */
	public SectionSet3DInt getCurrentSectionSet(){
		return (SectionSet3DInt)attributes.getValue("CurrentSectSet");
	}
	
	/********************************
	 * 
	 * Sets the current section for the section set to be rendered in this graphic object.
	 * 
	 * @param i
	 */
	public void setCurrentSection(int i){
		updateBorder();
		section_changed = true;
		attributes.setValue("CurrentSect", new MguiInteger(i));
		title_panel.updateTitle();
	}
	
	/********************************
	 * 
	 * Gets the current section for the section set being rendered.
	 * 
	 * @return
	 */
	public int getCurrentSection(){
		return ((MguiInteger)attributes.getValue("CurrentSect")).getInt();
	}
	
	@Override
	public boolean setSource(Object obj){
		ShapeModel3D old_model = null;
		SectionSet3DInt set = getCurrentSectionSet();
		if (set != null)
			old_model = set.getModel();
		if (!isDisplayable(obj)) return false;
		set = (SectionSet3DInt)obj;
		
		if (old_model != null && old_model != set.getModel()){
			old_model.removeShapeListener(this);
			old_model.windowUpdated(new WindowEvent(title_panel, WindowEvent.EventType.Destroyed));
			removeGraphicListener(old_model);
			}
		
		setCurrentSectionSet(set);
		attributes.setValue("CurrentSect", new MguiInteger(0), false);
		
		updateBorder();
		
		if (title_panel != null)
			title_panel.updateTitle();
		
		if (this.getCurrentTool() == null)
			setCurrentTool(this.defaultTool);
		
		addGraphicListener(set.getModel());
		fireGraphicSourceChanged();
		
		return true;
	}
	
	@Override
	public Object getSource(){
		return this.getCurrentSectionSet();
	}
	
	@Override
	public void setExcludeToSelection(boolean exclude){
		excludeToSelection = exclude;
	}
	
	@Override
	public boolean isDisplayable(Object obj){
		return (new SectionSet3DInt()).getClass().isInstance(obj);
	}

	protected void fireGraphicSourceChanged(){
		GraphicEvent e = new GraphicEvent(this, EventType.NewSource);
		for (int i = 0; i < graphicListeners.size(); i++)
			graphicListeners.get(i).graphicUpdated(e);
	}
	
	protected void fireGraphicListeners(GraphicEvent e){
		for (int i = 0; i < graphicListeners.size(); i++)
			graphicListeners.get(i).graphicUpdated(e);
	}
	
	//methods for shape sets, moved from display panel
	public ShapeSet2DInt getShapeSet2D(){
		//setShapeSet2D();
		return (ShapeSet2DInt)attributes.getValue("CurrentShpSet");
	}
	
	public void addShape2D(Shape2DInt thisShape){
		addShape2D(thisShape, true, true);
	}
	
	public void addShape2D(Shape2DInt thisShape, boolean updateShape, boolean updateListeners){
		SectionSet3DInt thisSet = getCurrentSectionSet();
		if (thisSet == null) return;
		int section = getCurrentSection();
		thisSet.addShape2D(thisShape, section, updateShape, updateListeners);
		setShapeSet2D();
	}
	
	public void setShapeSet2D(){
		SectionSet3DInt set3D = getCurrentSectionSet();
		if (set3D == null) return;
		int thisSection = getCurrentSection();
		ShapeSet2DInt set2D = set3D.getShapeSet(thisSection);
		if (set2D == null)
			set2D = new ShapeSet2DInt();
		setCurrentShapeSet(set2D);
	}
	
	protected void setCurrentShapeSet(ShapeSet2DInt set){
		attributes.setValue("CurrentShpSet", set);
	}
	
	/*****************
	 * Returns a tree node containing a list of objects displayable by this class of
	 * InterfaceGraphic. Thus, a hierarchical list of SectionSet3DInt objects from the
	 * data model.
	 * @param p InterfaceDisplayPanel containing displayable data objects
	 * @return DefaultMutableTreeNode with a tree list of SectionSet3DInt objects
	 */
	@Override
	public DefaultMutableTreeNode getDisplayObjectsNode(){
		return getTreeNode(InterfaceSession.getDisplayPanel().getCurrentShapeSet());
	}
	
	@Override
	protected DefaultMutableTreeNode getTreeNode(ShapeSet3DInt set){
		DefaultMutableTreeNode thisNode = new DefaultMutableTreeNode(set);
		for (int i = 0; i < set.members.size(); i++){
			if (ShapeSet3DInt.class.isInstance(set.members.get(i)))
				thisNode.add(getTreeNode((ShapeSet3DInt)set.members.get(i)));
			else if (SectionSet3DInt.class.isInstance(set.members.get(i)))
				thisNode.add(new DefaultMutableTreeNode(set.members.get(i)));
			}
		return thisNode;
	}
	
	@Override
	public void mapUpdated(MapEvent e){
		needs_update = true;
	}
	
	/*****************************************************
	 * Regenerates all the shapes in this display.
	 */
	public void regenerateDisplay(){
		needs_update = false;
		needs_regen = false;
		set3DObjects();
		
		if (section_changed){
			section_changed = false;
			fireGraphicListeners(new GraphicEvent(this, EventType.Modified));
			}
		
		repaint();
		fireDisplayListeners();
	}
	
	/**************************************
	 * Required for the <code>ShapeListener</code> interface. This method responds to a shape update event
	 * by adding, removing, or swapping drawables, or simply triggering a redraw request by setting
	 * the <code>needs_update</code> flag to <code>true</code> - as necessary. 
	 * 
	 */
	@Override
	public void shapeUpdated(ShapeEvent e){
	
		if (e.alreadyResponded(this)) return;
		e.responded(this);
		
		//is there a current section set?
		SectionSet3DInt section_set = getCurrentSectionSet();
		if (section_set == null) return;
		
		if (e.getShape() instanceof SectionSet3DInt){
			
			switch (e.eventType){
				case AttributeModified:
				case ShapeModified:
					//regen the current section to reflect the changes in the section set
					regenerateDisplay();
					
					return;
				case ShapeRemoved:
				case ShapeDestroyed:
					setCurrentSectionSet(null);
					return;
				}
			
			return;
			}
		
		//first check shape type
		if (e.getShape() instanceof Shape2DInt){
			Shape2DInt shape = (Shape2DInt)e.getShape();
			
			//does current set contain shape?
			ShapeSet2DInt set = section_set.getShapeSet(getCurrentSection());
			
			switch (e.eventType){
				case AttributeModified:
				case VertexColumnChanged:
					if (!(set.hasShape(shape) || shape3DObjects.hasShape(shape))) return;
					needs_update = true;
					if (shape.getModifiedAttribute().getName().equals("InheritFromParent") &&
							shape.hasParentShape())
						needs_regen = true;
					updateDisplay();
					return;
			
				case ShapeModified:
				case ShapeAdded:
					if (!(set.hasShape(shape) || shape3DObjects.hasShape(shape))) return;
					needs_regen = true;
					updateDisplay();
					return;
					
				case ShapeRemoved:
					if (set.getLastRemoved() == null) return;
					if (!(set.getLastRemoved().equals(shape)) || 
						(shape3DObjects.getLastRemoved() != null && 
								shape3DObjects.getLastRemoved().equals(shape))) return;
					needs_update = true;
					return;
				}
			
			}
		
		if (e.getShape() instanceof Shape3DInt){
			Shape3DInt shape = (Shape3DInt)e.getShape();
			
			if (shape instanceof ShapeSet3DInt){
				ShapeSet3DInt set3d = (ShapeSet3DInt)shape;
				switch (e.eventType){
					case AttributeModified:
						//regen the display if a parent set's attributes have changed
						regenerateDisplay();
						return;
					case ShapeRemoved:
					case ShapeDestroyed:
						if (set3d.getLastRemoved() == section_set){
							setCurrentSectionSet(null);
							return;
							}
						shape = (Shape3DInt)set3d.getLastRemoved();
						break;
					case ShapeAdded:
						shape = (Shape3DInt)set3d.getLastAdded();
						break;
					case ShapeInserted:
						shape = (Shape3DInt)set3d.getLastInserted();
						break;
					case ShapeModified:
					case ShapeMoved:
						shape = (Shape3DInt)set3d.getLastModified();
						break;
					default:
						return;
					}
				if (shape == null) return;
				}
			
			//if (shape instanceof SectionSet3DInt) return;
			Shape2DInt shape2D;
			switch (e.eventType){
				case AttributeModified:
					//some parent of a shape2D with this window as a listener has changed its attribute
					//so reflect the change in this window
					updateDisplay();
					return;
			
				case ShapeModified:
				case VertexColumnChanged:
					//does current set contain a shape for which this shape is a parent?
					shape2D = find3DChild(shape);
					if (shape2D != null){
						//swap old drawable for modified one
						removeShape3D(shape2D);
						}
					
					//now treat like an added shape
					if (!GeometryFunctions.crossesPlane(shape.getBoundBox(), getCurrentPlane())){
						if (shape2D != null) updateDisplay();
						return;
						}
					//insert new drawable
					insertShape3D(shape.getShape2DInt(getCurrentPlane(), 
													  section_set.getClipDistUp(), 
													  section_set.getClipDistDown(), 
													  false));
					fireGraphicListeners(new GraphicEvent(this, EventType.Updated));
					needs_update = true;
					return;
					
				case ShapeAdded:
					//does this object cross the current plane?
					if (!GeometryFunctions.crossesPlane(shape.getBoundBox(), getCurrentPlane())) return;
					//insert new drawable
					insertShape3D(shape.getShape2DInt(getCurrentPlane(), 
								  section_set.getClipDistUp(), 
								  section_set.getClipDistDown(), 
								  false));
					//fireGraphicListeners(new GraphicEvent(this, EventType.Modified));
					needs_update = true;
					//updateDisplay();
					return;
				
				case ShapeRemoved:
				case ShapeDestroyed:
					//does current set contain a shape for which this shape is a parent?
					ArrayList<Shape2DInt> children = find3DChildren(shape);
					//shape2D = find3DChild(shape);
					//if (shape2D == null) return;
					if (children.size() == 0) return;
					//remove object from drawables
					for (int i = 0; i < children.size(); i++)
						removeShape3D(children.get(i));
					fireGraphicListeners(new GraphicEvent(this, EventType.Updated));
					needs_update = true;
					//updateDisplay();
					return;
					
				}
			}
		
	}
	
	public ArrayList<Shape2DInt> find3DChildren(Shape3DInt shape){
		ArrayList<Shape2DInt> children = new ArrayList<Shape2DInt>();
		for (int i = 0; i < shape3DObjects.members.size(); i++)
			if (shape.equals(shape3DObjects.members.get(i).getParentShape()))
				children.add(shape3DObjects.members.get(i));
		return children;
	}
	
	public Shape2DInt find3DChild(Shape3DInt shape){
		for (int i = 0; i < shape3DObjects.members.size(); i++)
			if (shape.equals(shape3DObjects.members.get(i).getParentShape()))
				return shape3DObjects.members.get(i);
		return null;
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu(){
		InterfacePopupMenu menu = super.getPopupMenu();
		int start = super.getPopupLength();	//can we get this from the menu itself?
		
		menu.add(new JSeparator(), start);
		menu.add(new JSeparator(), start);
		menu.addMenuItem(new JMenuItem("Graphic2D Window", getObjectIcon()));
		menu.add(new JSeparator(), start + 3);
		menu.add(new JSeparator(), start + 3);
		
		JMenu submenu = new JMenu("Set source");
		boolean has_source = false;
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		for (int i = 0; i < models.size(); i++){
			ShapeModel3D model = models.get(i);
			//ShapeSet3DInt shapes = model.getModelSet().getShapeType(new SectionSet3DInt(), true);
			
			List<Shape3DInt> shapes = model.getModelSet().getShapeType(new SectionSet3DInt());
			for (Shape3DInt shape : shapes) {
				has_source = true;
				String comp_name = models.get(i).getName() + "." + shape.getName();
				JMenuItem item = new JMenuItem(comp_name);
				item.setActionCommand("Set source." + comp_name);
				submenu.add(item);
				}
			}
		int add = 0;
		if (has_source){
			menu.addSubmenu(submenu);
			add = 1;
			}
		
		menu.addMenuItem(new JMenuItem("Edit attributes.."));
		menu.addMenuItem(new JMenuItem("Dynamic zoom/pan"));
		menu.addMenuItem(new JMenuItem("Zoom window"));
		menu.addMenuItem(new JMenuItem("Zoom extents"));
		menu.addMenuItem(new JMenuItem("Zoom out"));
		menu.addMenuItem(new JMenuItem("Move sections here"));
		menu.addMenuItem(new JMenuItem("Select mode"));
		menu.add(new JSeparator(), start + 11 + add);
		menu.addMenuItem(new JMenuItem("Regen window"));
		menu.addMenuItem(new JMenuItem("Set background"));
		
		if (((MguiBoolean)attributes.getValue("ShowGrids")).getTrue())
			menu.addMenuItem(new JMenuItem("Hide grids"));
		else
			menu.addMenuItem(new JMenuItem("Show grids"));
		
		
		//Query for shape and add its menu to this menu
		ArrayList<PickInfoShape2D> infos = getPickShapes(last_click_point);
		
		if (infos != null && infos.size() > 0) {
			// Use top shape
			
			PickInfoShape2D info = infos.get(0);
			Shape2DInt shape = info.shape;
			
			
			
			menu.add(new JSeparator(), menu.getSize()); // start + 17 + add);
			menu.add(new JSeparator(), menu.getSize()); //, start + 17 + add);
			menu.addMenuItem(new JMenuItem(shape.getName(), shape.getObjectIcon()));
			menu.add(new JSeparator(), menu.getSize()); //, start + 19 + add);
			menu.add(new JSeparator(), menu.getSize()); //, start + 19 + add);
			
			shape.setGraphic2DPopupMenu(menu);
			
			menu.addMenuItem(new JMenuItem("Center on shape"));
			menu.addMenuItem(new JMenuItem("Zoom to shape extents"));
			
			}
		
		return menu;
	}
	
	@Override
	protected int getPopupLength(){
		return super.getPopupLength() + 15; 
	}
	
	/*
	@Override
	public boolean writeSnapshotToFile(File file){
		
		BufferedImage image = new  BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
		paintComponent(image.getGraphics());
		return ImagingIOFunctions.writeImageToPng(image, file);
		
	}
	*/
	
	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getActionCommand().startsWith("Edit attributes")){
			InterfaceSession.getWorkspace().showAttributeDialog(this);
			return;
			}
		
		if (item.getActionCommand().equals("Hide grids")){
			attributes.setValue("ShowGrids",new MguiBoolean(false));
			return;
			}
		
		if (item.getActionCommand().equals("Show grids")){
			attributes.setValue("ShowGrids",new MguiBoolean(true));
			return;
			}
		
		if (item.getActionCommand().equals("Set background")){
			Color bg = getBackgroundColour();
			Color colour = JColorChooser.showDialog(InterfaceSession.getSessionFrame(), 
													"Choose background colour", 
													bg); 
			if (colour == null || colour.equals(bg)) return;
			setBackgroundColour(colour);
			return;
			}
		
		if (item.getActionCommand().startsWith("Set source")){
			
			String comp = item.getActionCommand().substring(11);
			int a = comp.indexOf(".");
			String model = comp.substring(0,a);
			String set = comp.substring(a + 1);
			
			ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
			for (int i = 0; i < models.size(); i++){
				if (models.get(i).getName().equals(model)){
					//ShapeSet3DInt shapes = models.get(i).getModelSet().getShapeType(new SectionSet3DInt(), true);
					
					List<Shape3DInt> shapes = models.get(i).getModelSet().getShapeType(new SectionSet3DInt(), true);
					
					for (Shape3DInt shape : shapes) {
						if (shape.getName().equals(set)) {
							this.setSource(shape);
							return;
							}
						
						}
					
//					for (int j = 0; j < shapes.size(); j++){
//						SectionSet3DInt sections = (SectionSet3DInt)shapes.getShape(set);
//						if (sections != null){
//							this.setSource(sections);
//							return;
//							}
//						}
					}
				}
			
			InterfaceSession.log("InterfaceGraphic3D: Could not set section set '" + comp + "' as source..", 
					 			 LoggingType.Errors);
			return;
		}
		
		if (item.getText().equals("Zoom window")){
			
			ToolWindowZoom2D tool = new ToolWindowZoom2D(this.getCurrentTool());
			setTool(tool);
			return;
			
		}
		
		if (item.getText().equals("Zoom out")){
			// Zoom out 100%
			Map2D map = (Map2D)getMap();
			double zoom = map.getZoom();
			Point2f p = map.getMapCenterPt();
			map.setZoom(zoom * 2);
			map.centerOnPoint(p);
			updateDisplay();
			
			return;
		}
		
		if (item.getText().equals("Zoom extents")){
			
			if (shape3DObjects.getSize() == 0 &&
					shapeList.size() == 0) 
				return;
			
			Rect2D bounds = shape3DObjects.getBounds();
			
			for (int i = 0; i < shapeList.size(); i++){
				bounds = GeometryFunctions.getUnionBounds(shapeList.get(i).getBounds(), bounds);
				}
			
			Map2D map = (Map2D)getMap();
			map.zoomToMapWindow(bounds);
			double zoom = map.getZoom();
			String[] zoom_limits = ((String)attributes.getValue("ZoomLimits")).split(" ");
			double zoom_min = Math.max(Double.valueOf(zoom_limits[0]), 0.00001);
			double zoom_max = Math.min(Double.valueOf(zoom_limits[1]), Math.pow(10, 7));
			if (zoom < zoom_min) zoom = zoom_min;
			if (zoom > zoom_max) zoom = zoom_max;
			
			map.setZoom(zoom * 1.05); // Zoom out 5%
			
			Point2f p = bounds.getCenterPt();
			if (GeometryFunctions.isValidPoint(p))
				map.centerOnPoint(p);
			else
				map.centerOnPoint(new Point2f());
			updateDisplay();
			
			return;
			}
		
		if (item.getText().equals("Regen window")){
			regenerateDisplay();
			return;
			}
		
		if (item.getText().equals("Move sections here")){
			
			SectionSet3DInt s_set = this.getCurrentSectionSet();
			if (s_set == null) return;
			
			Map2D map = (Map2D)getMap();
			Point2f p2d = map.getMapPoint(this.last_click_point);
			Point3f p3d = GeometryFunctions.getPointFromPlane(p2d, getCurrentPlane());
			
			ArrayList<InterfaceGraphic2D> windows = s_set.getModel().getModelSet().getSectionWindows();
			for (int i = 0; i < windows.size(); i++){
				windows.get(i).moveSectionTo(p3d);
				//windows.get(i).centerSectionAt(p3d);
				}
			return;
			}
		
		ArrayList<PickInfoShape2D> infos = getPickShapes(last_click_point);
		
		if (infos != null && infos.size() > 0) {
			// Use top shape
			
			PickInfoShape2D info = infos.get(0);
			Shape2DInt shape = info.shape;
			shape.handlePopupEvent(e);
			
			}
		
		super.handlePopupEvent(e);
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (currentTool != null)
			currentTool.handleToolEvent(new ToolInputEvent(this, ToolConstants.TOOL_KEY_PRESSED, e.getKeyCode()));
		
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
		
		SectionSet3DInt set = this.getCurrentSectionSet();
		String set_str = "null";
		int current = this.getCurrentSection();
		
		
		if (set != null)
			set_str = set.getName();
		
		writer.write(_tab + "<InterfaceGraphic2D\n" +
				_tab2 + "name='" + getName() + "'\n" +
				_tab2 + "source='" + set_str + "'\n" +
				_tab2 + "section='" + current + "'\n" +
				_tab + ">\n");
		
		Map map = this.getMap();
		map.writeXML(tab + 1, writer, options, progress_bar);
		
		attributes.writeXML(tab + 1, writer, options, progress_bar);
		
		writer.write(_tab + "</InterfaceGraphic2D>\n");
		
	}

	public DrawingEngine getDrawingEngine() {
		return this.drawEngine;
	}
	
	
}