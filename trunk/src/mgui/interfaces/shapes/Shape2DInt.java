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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.TransferHandler.TransferSupport;
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3f;

import mgui.geometry.Plane3D;
import mgui.geometry.Rect2D;
import mgui.geometry.Shape;
import mgui.geometry.Shape2D;
import mgui.geometry.Shape3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.geometry.util.NodeShape;
import mgui.geometry.util.NodeShapeComboRenderer;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.AttributeSelectionMap;
import mgui.interfaces.attributes.AttributeSelectionMap.ComboMode;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.attributes.ShapeAttribute;
import mgui.interfaces.shapes.mesh.VertexSelection;
import mgui.interfaces.shapes.trees.Shape2DTreeNode;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiNumber;

/**********************************
 * Base class for all interfaces to 2D geometrical shapes. Specifies functions and fields
 * common to all shapes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public abstract class Shape2DInt extends InterfaceShape 
								 implements ShapeListener, 
								 			Cloneable {

	public Shape2D thisShape;
	public DrawingEngine drawEngine;
	public String idStr = "";
	public Rect2D bounds;
	protected ShapeListener nodeListener;
	public Point2f centerPt;
	public ShapeSceneNode sceneNode;
	public Shape3DInt parentShape;
	public Shape3DInt child3D;
	Attribute<?> modified_attribute;
	protected HashMap<Integer,Integer> map_idx_to_parent;
	
	public Shape2DInt(){
		init();
	}
	
	@Override
	public Shape getGeometryInstance(){
		return null;
	}
	
	private void init(){
		_init();
		
		
		
	}
	
	@Override
	protected Attribute<?> getParentAttribute(String attrName){
		if (!this.hasParentShape() || !isHeritableAttribute(attrName)) {
			return attributes.getAttribute(attrName);
			}
		return ((ShapeSet2DInt)parent_set).getAttribute(attrName);
	}
	
	public void setMapIdxToParent(HashMap<Integer,Integer> map_idx_to_parent){
		this.map_idx_to_parent = map_idx_to_parent;
	}
	
	public HashMap<Integer,Integer> getMapIdxToParent(){
		return map_idx_to_parent;
	}
	
	@Override
	public Shape getGeometry(){
		return this.getShape();
	}
	
	@Override
	public boolean setGeometry(Shape geometry){
		if (!(geometry instanceof Shape2D)) return false;
		setShape((Shape2D)geometry);
		return true;
	}
	
	public NodeShape getVertexShape(){
		return (NodeShape)attributes.getValue("2D.VertexShape");
	}
	
	public void setVertexShape(NodeShape shape){
		AttributeSelectionMap<NodeShape> shapes = (AttributeSelectionMap<NodeShape>)attributes.getAttribute("VertexShape");
		shapes.setValue(shape);
		
	}
	
	public boolean isLabelShape(){
		return false;
	}
	
	@Override
	public int getVertexCount(){
		if (getVertices() == null) return -1;
		return getVertices().size();
	}
	
	public int[] getDimensions() {
		return new int[]{thisShape.getVertices().size()};
	}
	
	public ArrayList<Point2f> getVertices(){
		if (thisShape == null) return null;
		return thisShape.getVertices();
	}
	
	public Point2f getVertex(int i){
		if (thisShape == null) return null;
		return thisShape.getVertex(i);
	}
	
	public boolean hasParentShape(){
		return this.parentShape != null;
	}
	
	@Override
	public Attribute<?> getModifiedAttribute(){
		return modified_attribute;
	}
	
	/**********************************
	 * TODO: implement me
	 */
	@Override
	public VertexSelection getVertexSelection(){
		return null;
	}
	
	@Override
	public void setVertexSelection(VertexSelection selection){
		
	}
		
	
	/**********************************
	 * Returns the index of the vertex in this shape which is closest to <code>point</code>.
	 * 
	 * @param point
	 * @return
	 */
	public int getClosestVertex(Point2f point){
		//default calls the GeometryFunctions method
		return GeometryFunctions.getClosestVertex(thisShape, point);
	}
	
	/**********************************
	 * If this shape has a parent {@link Shape3DInt} object, this method calls the getClosestVertex 
	 * method of the parent. 
	 * 
	 * @param point The point to test 
	 * @param plane The plane on which this 2D object is described
	 * @return
	 */
	public int getClosestVertex3D(Point2f point, Plane3D plane){
		if (this.parentShape == null) return -1;
		Point3f p3d = GeometryFunctions.getPointFromPlane(point, plane);
		return parentShape.getClosestVertex(p3d);
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/shape_2d_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/shape_2d_20.png");
	}
	
	@Override
	public String getTreeLabel(){
		return getName();
	}
	
	@Override
	public void destroy(){
		destroy(null); //new ShapeEvent(this, ShapeEvent.EventType.ShapeDestroyed));
	}
	
	public void destroy(ShapeEvent event){
		isDestroyed = true;
		//remove pointer in parent shape if one exists
		if (parentShape != null)
			parentShape.removeShape2DChild(this);
		if (event != null)
			fireShapeListeners(event);
	}
	
	public Shape3DInt getParentShape(){
		return parentShape;
	}
	
	public void setChild3D(Shape3DInt child){
		if (child3D != null) child3D.setParent2D(this);
		child3D = child;
	}
	
	public Shape3DInt getChild3D(){
		return child3D;
	}
	
	/****************************************
	 * Returns a 3D representation of this shape, assuming it belongs to <code>plane</code>.
	 * 
	 * @param plane			Plane which gives this shape 3D coordinates
	 * @return				The 3D representation
	 */
	public Shape3DInt getShape3DInt(Plane3D plane){
		return getShape3DInt(plane, true);
	}
	
	/****************************************
	 * Returns a 3D representation of this shape, assuming it belongs to <code>plane</code>.
	 * 
	 * @param plane			Plane which gives this shape 3D coordinates
	 * @param set_parent	Whether to register this shape as a parent on the issued shape
	 * @return				The 3D representation
	 */
	public Shape3DInt getShape3DInt(Plane3D plane, boolean set_parent){
		if (!isVisible() || !show2D()) return null;
		
		Shape3DInt shape3D = getShape3D(plane);
		if (shape3D == null) return null;
		
		setChild3D(shape3D);
		return shape3D;
	}
	
	//Subclasses should override to provide their 3D representations
	protected Shape3DInt getShape3D(Plane3D plane){
		return null;
	}
	
	public void setParentShape(Shape3DInt shape){
		if (parentShape != null) 
			parentShape.removeShape2DChild(this);
		parentShape = shape;
		parentShape.addShape2DChild(this);
		ShapeFunctions.setAttributesFrom3DParent(this, shape, false); // inheritAttributesFromParent());
		
	}
	
	public Point2f getCenterPoint(){
		return centerPt;
	}
	
	@Override
	public void drawShape2D(Graphics2D g, DrawingEngine d) {
		if (!((MguiBoolean)attributes.getValue("IsVisible")).getTrue()) return;
		d.setAttributes(attributes);
		if (isOverridden)
			d.setAttributes(overrideAttr);
		draw(g, d);
		d.reset(g);
	}
	
	//override this method to draw the object
	protected void draw(Graphics2D g, DrawingEngine d){
		InterfaceSession.log("Shape2DInt [" + this.getClass().getCanonicalName() + "]: draw method not implemented...", 
							 LoggingType.Warnings);
	}
	
	@Override
	public boolean needsRedraw(Attribute<?> a){
		if (a instanceof ShapeAttribute){
			return ((ShapeAttribute<?>)a).needsRedraw2D();
			}
		if (a.getName().equals("IsVisible")) return true;
		if (!isVisible()) return false;
		if (a.getName().equals("2D.Show")) return true;
		if (!show2D()) return false;
		if (a.getName().equals("Name")) return false;
		return true;
	}
	
	@Override
	public void fireShapeListeners(ShapeEvent e){
		if (!notifyListeners) return;
		for (int i = 0; i < shapeListeners.size(); i++)
			if (shapeListeners.get(i).isDestroyed())
				shapeListeners.remove(i);
		ArrayList<ShapeListener> currentListeners = new ArrayList<ShapeListener>(shapeListeners);
		for (int i = 0; i < currentListeners.size(); i++)
			currentListeners.get(i).shapeUpdated(e);
		
		ArrayList<InterfaceTreeNode> temp = new ArrayList<InterfaceTreeNode>(tree_nodes);
		for (int i = 0; i < temp.size(); i++)
			if (temp.get(i).isDestroyed)
				tree_nodes.remove(temp.get(i));
			else
				((Shape2DTreeNode)temp.get(i)).shapeUpdated(e);
	}
	
	@Override
	public boolean isHeritableAttribute(String name){
		return name.startsWith("2D.");
	}
	
	@Override
	public boolean isInheritingAttribute(Attribute<?> attribute){
		return (attribute.getName().startsWith("3D."));
	}
	
	@Override
	public Font getLabelFont(){
		return (Font)attributes.getValue("2D.LabelFont");
	}
	
	@Override
	public void setLabelFont(Font font){
		attributes.setValue("2D.LabelFont", font);
	}
	
	@Override
	public float getLabelScale(){
		return ((MguiFloat)attributes.getValue("2D.LabelScale")).getFloat();
	}
	
	@Override
	public void setLabelScale(float scale){
		attributes.setValue("2D.LabelScale", new MguiFloat(scale));
	}
	
	@Override
	public Color getLabelColour(){
		return (Color)attributes.getValue("2D.LabelColour");
	}
	
	@Override
	public void setLabelColour(Color colour){
		attributes.setValue("2D.LabelColour", colour);
	}
	
	@Override
	public boolean hasAlpha(){
		return ((MguiBoolean)attributes.getValue("2D.HasAlpha")).getTrue();
	}
	
	@Override
	public void hasAlpha(boolean b){
		attributes.setValue("2D.HasAlpha", new MguiBoolean(b));
	}
	
	@Override
	public boolean showVertices(){
		return ((MguiBoolean)attributes.getValue("2D.ShowVertices")).getTrue();
	}

	@Override
	public void showVertices(boolean b){
		attributes.setValue("2D.ShowVertices", new MguiBoolean(b));
	}
	
	@Override
	public float getVertexScale(){
		return ((MguiFloat)attributes.getValue("2D.VertexScale")).getFloat();
	}
	
	@Override
	public Color getVertexColour(){
		return (Color)attributes.getValue("2D.VertexColour");
	}
	
	@Override
	public float getAlpha(){
		return ((MguiFloat)attributes.getValue("2D.Alpha")).getFloat();
	}
	
	@Override
	public void setAlpha(float f){
		attributes.setValue("2D.Alpha", new MguiFloat(f));
	}
	
	@Override
	public Color getLineColour(){
		return (Color)attributes.getValue("2D.LineColour");
	}
	
	@Override
	public Stroke getLineStyle(){
		return (Stroke)attributes.getValue("2D.LineStyle");
	}
	
	@Override
	public void setLineStyle(Stroke s){
		attributes.setValue("2D.LineStyle", s);
	}
	
	public void setShape(mgui.geometry.Shape2D newShape){thisShape = newShape;}
	
	public Shape2D getShape(){return thisShape;}

	public Shape getBoundShape(){
		return getBounds();
	}
	
	public Rect2D getBounds(){
		if (bounds == null) updateShape();
		if (bounds == null) return null;
		return new Rect2D(bounds);
	}
	
	public Rect2D getExtBounds(){
		return getBounds();
	}
	
	public double getProximity(Point2f thisPoint){
		return thisShape.getProximity(thisPoint);
	}
	
	public void shapeUpdated(ShapeEvent e){
		
		if (e.getSource() instanceof Shape3DInt)
			fireShapeListeners(e);
	}
	
	/**@deprecated **/
	@Deprecated
	public boolean getVisibility(){
		return ((MguiBoolean)attributes.getValue("IsVisible")).getTrue();
	}
	
	@Override
	public InterfaceTreeNode issueTreeNode(){
		Shape2DTreeNode treeNode = new Shape2DTreeNode(this);
		setTreeNode(treeNode);
		tree_nodes.add(treeNode);
		return treeNode;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		treeNode.add(attributes.issueTreeNode());
	}
		
	@Override
	public void updateShape(){
		//recalculate bounds
		//for each node, if is max or min
		float maxX = -Float.MAX_VALUE, minX = Float.MAX_VALUE;
		float maxY = -Float.MAX_VALUE, minY = Float.MAX_VALUE;
		float xSum = 0, ySum = 0;
		
		ArrayList<Point2f> nodes = thisShape.getVertices();
		if (nodes.size() == 0){
			bounds = new Rect2D(0, 0, 0, 0);
			centerPt = new Point2f();
			return;
		}
		for (int i = 0; i < nodes.size(); i++){
			maxX = Math.max(nodes.get(i).x, maxX);
			maxY = Math.max(nodes.get(i).y, maxY);
			minX = Math.min(nodes.get(i).x, minX);
			minY = Math.min(nodes.get(i).y, minY);
			xSum += nodes.get(i).x;
			ySum += nodes.get(i).y;
		}
		
		if (centerPt == null) centerPt = new Point2f();
		centerPt.x = xSum / nodes.size();
		centerPt.y = ySum / nodes.size();
		
		if (bounds == null )
			bounds = new Rect2D(minX, minY, maxX, maxY);
		else{
			bounds.corner1.x = minX;
			bounds.corner1.y = minY;
			bounds.corner2.x = maxX;
			bounds.corner2.y = maxY;
		}
		//fireShapeListeners();
	}
	
	public boolean validateNodes(){
		ArrayList<Point2f> nodeList = thisShape.getVertices();
		for (int i = 0; i < nodeList.size(); i++)
			if (Float.isNaN(nodeList.get(i).x) || Float.isNaN(nodeList.get(i).y))
				return false;
		return true;
	}
	
	public String printNodes(){
		ArrayList<Point2f> nodeList = thisShape.getVertices();
		String retStr = "Nodes:\n";
		for (int i = 0; i < nodeList.size(); i++)
			retStr += "x: " + MguiFloat.getString(nodeList.get(i).x, "#0.0") +
					  ", y: " + MguiFloat.getString(nodeList.get(i).y, "#0.0");
		return retStr;
	}
	
	public boolean contains(Point2f thisPoint){
		//if (bounds == null) updateShape();
		return bounds.contains(thisPoint);
	}
	
	@Override
	public String toString(){
		return "Shape 2D Interface [" + String.valueOf(ID) + "]";
	}
	
	@Override
	public Object clone(){
		return null;
	}
	
	/**********************************************
	 * Responds to a change in a specific attribute by notifying this ShapeInt's shape listeners with an
	 * <code>AttributeModified</code> shape event. If overriding, this super method should be called
	 * AFTER the special handling has been performed. 
	 * 
	 */
	public void attributeUpdated(AttributeEvent e){
		
		if (needsRedraw(e.getAttribute())){
			modified_attribute = e.getAttribute();
			fireShapeListeners(new ShapeEvent(this, ShapeEvent.EventType.AttributeModified));
			modified_attribute = null;
			}
		
	}
	
	public String getDTD() {
		
		return null;
	}

	public String getLocalName() {
		return "Shape2DInt";
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab + 1);
		
		String _type = "full";
		XMLType type = options.type;
		if (type.equals(XMLType.Reference)) _type = "reference";
		
		String encoding = "";
		if (options.encoding != null)
			encoding = XMLFunctions.getEncodingStr(options.encoding);
		
		writer.write(_tab + "<Shape3DInt \n" + 
						_tab2 + "class = '" + getClass().getCanonicalName() + "'\n" +
						_tab2 + "name = '" + getName() + "'\n" +
						_tab2 + "type = '" + _type + "'\n" + 
						_tab2 + "encoding = '" + encoding + "'\n" +
						_tab + ">\n");
		
		if (attributes != null){
			writer.write(_tab2 + "<AttributeList>\n");
			attributes.writeXML(tab + 2, writer, progress_bar);
			writer.write(_tab2 + "</AttributeList>\n");
			}
		
		//subclasses write their XML here
		switch (type){
			case Full:
				getShape().writeXML(tab + 1, writer, progress_bar);
				break;
			
			}
		
		//close up
		writer.write(_tab + "</Shape2DInt>\n");
		
	}
	
	protected void writeShapeToXML(int tab, Writer writer, ProgressUpdater progress_bar, XMLType type) throws IOException{

	}
	
	public String getShortXML(int tab){
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = _tab + "<Shape2DInt class = '" + getClass().getCanonicalName() + "' name = '" + getName() + "' />\n";
		
		return xml;
		
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu() {
		return getPopupMenu(null);
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu(List<Object> selected) {
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Shape2DInt Menu Item 1"));
		menu.addMenuItem(new JMenuItem("Shape2DInt Menu Item 2"));
		
		return menu;
	}

	/****************************
	 * Subclasses should override this if necessary and call super.getGraphic3DPopupMenu() to get
	 * this top-level menu and add items to it. 
	 * 
	 */
	public void setGraphic2DPopupMenu(InterfacePopupMenu menu){
		
		JMenuItem item = new JMenuItem("Edit attributes..");	
		menu.addMenuItem(item);
		item.setActionCommand("Shape");
		item = new JMenuItem("Hide");
		menu.addMenuItem(item);
		item.setActionCommand("Shape");
		item = new JMenuItem("Move up");
		menu.addMenuItem(item);
		item.setActionCommand("Shape");
		item = new JMenuItem("Move down");
		menu.addMenuItem(item);
		item.setActionCommand("Shape");
		item = new JMenuItem("Move to top");
		menu.addMenuItem(item);
		item.setActionCommand("Shape");
		item = new JMenuItem("Move to bottom");
		menu.addMenuItem(item);
		item.setActionCommand("Shape");
		item = new JMenuItem("Clear selection");
		menu.addMenuItem(item);
		item.setActionCommand("Shape");
		
	}
	
	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		if (this.hasParentShape()) {
			this.parentShape.handlePopupEvent(e);
			return;
			}
		
		if (!(e.getSource() instanceof JMenuItem)) return;
		JMenuItem item = (JMenuItem)e.getSource();
		
		if (item.getText().equals("Edit attributes..")){
			InterfaceSession.getWorkspace().showAttributeDialog(this);
			return;
			}
		
		if (item.getText().equals("Hide")){
			this.setVisible(false);
			return;
			}
		
		if (item.getText().startsWith("Move")){
			//TODO: implement move shape on popup
			//ShapeSet2DInt parent = (ShapeSet2DInt)getParentSet();
			
			
			
			return;
			}
		
		if (item.getText().equals("Clear selection")){
			//TODO: implement clear selection on popup
			return;
			}
		
	}

	public void showPopupMenu(MouseEvent e) {
		InterfacePopupMenu menu = getPopupMenu();
		if (menu == null) return;
		menu.show(e);
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		DataFlavor[] flavors = new DataFlavor[1];
		String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + Shape2DInt.class.getName();
		try{
			flavors[0] = new DataFlavor(mimeType);
		}catch (ClassNotFoundException cnfe){
			//if this happens then hell freezes over :-/
			cnfe.printStackTrace();
			return new DataFlavor[0];
			}
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean performTransfer(TransferSupport support){
		
		return false;
	}

	
	// *********************** VERTEX DATA **************************************
	// Reroutes vertex data methods to parent if one exists; otherwise calls super
	// methods.
	
	@Override
	public ArrayList<String> getVertexDataColumnNames(){
		if (this.parentShape != null)
			return parentShape.getVertexDataColumnNames();
		return super.getVertexDataColumnNames();
	}
	
	@Override
	public ArrayList<VertexDataColumn> getVertexDataColumns(){
		if (this.parentShape != null)
			return parentShape.getVertexDataColumns();
		return super.getVertexDataColumns();
	}
	
	@Override
	public VertexDataColumn getVertexDataColumn(String name){
		if (this.parentShape != null)
			return parentShape.getVertexDataColumn(name);
		return super.getVertexDataColumn(name);
	}
	
	@Override
	public boolean addVertexData(String key, ArrayList<MguiNumber> data){
		if (this.parentShape != null)
			return parentShape.addVertexData(key, data);
		return super.addVertexData(key, data);
	}
	
	@Override
	public boolean addVertexData(String key, ArrayList<MguiNumber> data, NameMap map){
		if (this.parentShape != null)
			return parentShape.addVertexData(key, data, map);
		return super.addVertexData(key, data, map);
	}
	
	@Override
	public boolean addVertexData(String key){
		if (this.parentShape != null)
			return parentShape.addVertexData(key);
		return super.addVertexData(key);
	}
	
	@Override
	public boolean addVertexData(String key, int dataType){
		if (this.parentShape != null)
			return parentShape.addVertexData(key, dataType);
		return super.addVertexData(key, dataType);
	}
	
	@Override
	public void removeVertexData(String key){
		if (this.parentShape != null)
			parentShape.removeVertexData(key);
		super.removeVertexData(key);
	}
	
	@Override
	public String getCurrentColumn(){
		if (this.parentShape != null)
			return parentShape.getCurrentColumn();
		return super.getCurrentColumn();
	}
	
	@Override
	public HashMap<String, ArrayList<MguiNumber>> getVertexDataMap(){
		if (this.parentShape != null)
			return parentShape.getVertexDataMap();
		return super.getVertexDataMap();
	}
	
	@Override
	public void setVertexDataMap(HashMap<String, ArrayList<MguiNumber>> data){
		if (this.parentShape != null)
			parentShape.setVertexDataMap(data);
		super.setVertexDataMap(data);
		
	}
	
	@Override
	public ArrayList<MguiNumber> getCurrentVertexData(){
		if (this.parentShape != null)
			return parentShape.getCurrentVertexData();
		return super.getCurrentVertexData();
	}
	
	@Override
	public MguiNumber getLinkedVertexDatum(String linked_column, int index){
		if (map_idx_to_parent != null)
			index = this.map_idx_to_parent.get(index);
		if (this.parentShape != null)
			return parentShape.getLinkedVertexDatum(linked_column, index);
		return super.getLinkedVertexDatum(linked_column, index);
	}
	
	@Override
	public ArrayList<MguiNumber> getLinkedVertexData(String linked_column){
		if (this.parentShape != null)
			return parentShape.getLinkedVertexData(linked_column);
		return super.getLinkedVertexData(linked_column);
	}
	
	@Override
	public boolean hasColumn(String s){
		if (this.parentShape != null)
			return parentShape.hasColumn(s);
		return super.hasColumn(s);
	}
	
	@Override
	public void setCurrentColumn(String key){
		if (this.parentShape != null)
			parentShape.setCurrentColumn(key);
		super.setCurrentColumn(key);
	}
	
	@Override
	public void setCurrentColumn(String key, boolean update){
		if (this.parentShape != null)
			parentShape.setCurrentColumn(key, update);
		super.setCurrentColumn(key, update);
	}
	
	@Override
	public ArrayList<MguiNumber> getVertexData(String column){
		if (this.parentShape != null)
			return parentShape.getVertexData(column);
		return super.getVertexData(column);
	}
	
	@Override
	public ArrayList<ArrayList<MguiNumber>> getAllVertexData(){
		if (this.parentShape != null)
			return parentShape.getAllVertexData();
		return super.getAllVertexData();
	}
	
	@Override
	public MguiNumber getDatumAtVertex(String column, int index){
		if (map_idx_to_parent != null)
			index = this.map_idx_to_parent.get(index);
		if (this.parentShape != null)
			return parentShape.getDatumAtVertex(column, index);
		return super.getDatumAtVertex(column, index);
	}
	
	@Override
	public boolean hasData(){
		if (this.parentShape != null)
			return parentShape.hasData();
		return super.hasData();
	}
	
	@Override
	public ArrayList<String> getNonLinkedDataColumns(){
		if (this.parentShape != null)
			return parentShape.getNonLinkedDataColumns();
		return super.getNonLinkedDataColumns();
	}
	
	@Override
	protected void updateDataColumns(){
		if (this.parentShape != null)
			parentShape.updateDataColumns();
		super.updateDataColumns();
	}
	
	
	
}