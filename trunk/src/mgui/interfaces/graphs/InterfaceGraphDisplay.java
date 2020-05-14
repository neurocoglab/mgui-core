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

package mgui.interfaces.graphs;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jogamp.vecmath.Point2f;

import mgui.geometry.Plane3D;
import mgui.geometry.util.NodeShape;
import mgui.geometry.util.NodeShapeComboRenderer;
import mgui.interfaces.InterfaceDisplayPanel;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.AttributeSelectionMap;
import mgui.interfaces.attributes.AttributeSelectionMap.ComboMode;
import mgui.interfaces.graphics.GraphicPropertyListener;
import mgui.interfaces.graphics.InterfaceGraphic;
import mgui.interfaces.graphics.InterfaceGraphicTextBox;
import mgui.interfaces.graphs.layouts.CoordinateLayout;
import mgui.interfaces.graphs.util.GraphFunctions;
import mgui.interfaces.graphs.util.GraphFunctions.ArrowVisibility;
import mgui.interfaces.graphs.util.GraphFunctions.EdgeVisibility;
import mgui.interfaces.graphs.util.GraphFunctions.GraphEdgeLabelRenderer;
import mgui.interfaces.graphs.util.GraphFunctions.GraphNodeLabelRenderer;
import mgui.interfaces.graphs.util.GraphFunctions.NodeVisibility;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.util.Plane3DDialog;
import mgui.interfaces.tools.Tool;
import mgui.interfaces.tools.ToolConstants;
import mgui.interfaces.tools.ToolInputEvent;
import mgui.interfaces.tools.graphs.ToolGraph;
import mgui.interfaces.tools.graphs.ToolGraphImage;
import mgui.interfaces.tools.graphs.ToolGraphTransform;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.io.imaging.ImagingIOFunctions;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.util.Colour;

import org.apache.commons.collections15.Transformer;
import org.xml.sax.Attributes;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationViewer.GraphMouse;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;


/********************************************
 * Displays graphs and allows user interaction with them. Interfaces with the JUNG API
 * (http://jung.sourceforge.net/) 
 * 
 * @author Andrew Reid
 * @version 1.0
 *
 */
public class InterfaceGraphDisplay extends InterfaceGraphic<ToolGraph>
								   implements InterfaceGraphListener,
								   			  GraphEventListener<AbstractGraphNode, AbstractGraphEdge>{

	protected VisualizationViewer<AbstractGraphNode, AbstractGraphEdge> viewer;
	protected GraphZoomScrollPane scrollPane;
	protected InterfaceAbstractGraph graph;
	protected ObservableGraph<AbstractGraphNode, AbstractGraphEdge> observable_graph;
	
	protected ToolGraph currentTool, defaultTool;
	protected boolean toolLock;
	protected ViewerMouseListener viewer_adapter;
	private PropertyChangeSupport propertyChange = new PropertyChangeSupport(this);
		
	//predicates n' stuff
	protected EdgeVisibility show_edges = GraphFunctions.getEdgeVisibilityPredicate(true);
	protected NodeVisibility show_nodes = GraphFunctions.getNodeVisibilityPredicate(true);
	protected ArrowVisibility show_arrows = GraphFunctions.getArrowVisibilityPredicate(true);
	protected Position label_position = Position.SE;
	protected GraphNodeLabelRenderer node_label_renderer;
	protected GraphEdgeLabelRenderer edge_label_renderer;
	
	Layout<AbstractGraphNode, AbstractGraphEdge> current_layout;
	
	private Shape getDefaultNodeShape(){
		return new Ellipse2D.Float(-10,-10,20,20);
	}
		
	public InterfaceGraphDisplay(){
		init2();
	}

	public InterfaceGraphDisplay(InterfaceAbstractGraph graph, String layout){
		init2();
		setGraphLayout(layout);
		try{
			setGraph(graph);
		}catch (GraphException ex){
			InterfaceSession.log("InterfaceGraphDisplay: Error constructing graph.",
								 LoggingType.Errors);
			}
		//setViewer(graph, layout);
	}
		
	/********************************
	 * Gets the {@link VisualizationViewer} for this window.
	 * 
	 * @return the viewer, or <code>null</code> if none is set.
	 */
	public VisualizationViewer<AbstractGraphNode, AbstractGraphEdge> getViewer(){
		return viewer;
	}
	
	@Override
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/graph_display_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/graph_display_20.png");
		return null;
	}
	
	private void init2(){
		super.init();
		_init();
		type = "Graph Display";
		
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		//tool input adapter
		this.addMouseListener(toolInputAdapter.getMouseAdapter());
		this.addMouseMotionListener(toolInputAdapter.getMouseAdapter());
		this.addMouseWheelListener(toolInputAdapter.getMouseAdapter());
		
		viewer_adapter = new ViewerMouseListener(this);
		
		ArrayList<ColourMap> cmaps = InterfaceEnvironment.getColourMaps();
		
		node_label_renderer = GraphFunctions.getNodeLabelRenderer(Position.SE);
		edge_label_renderer = GraphFunctions.getEdgeLabelRenderer();
		
		//add some attributes?
		HashMap<String, Class> layout_types = GraphFunctions.getLayoutTypes2();
		AttributeSelectionMap<Class> layouts = 
			new AttributeSelectionMap<Class>("Layout", layout_types, Class.class);
	
		attributes.add(layouts);
		attributes.add(new Attribute<Font>("NodeLabelFont", new Font("Courier New", Font.PLAIN, 10)));
		attributes.add(new Attribute<Color>("NodeLabelColour", Color.BLACK));
		attributes.add(new Attribute<MguiBoolean>("NodeLabelBack", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowNodes", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowEdges", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowArrows", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("LabelEdges", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("LabelNodes", new MguiBoolean(true)));
		attributes.add(new Attribute<Color>("NodeColour", Color.RED));
		attributes.add(new Attribute<Color>("EdgeColour", Color.BLACK));
		attributes.add(new Attribute<MguiDouble>("EdgeScale", new MguiDouble(1.0)));
		attributes.add(new Attribute<MguiFloat>("EdgeOffset", new MguiFloat(20f)));
		attributes.add(new Attribute<MguiDouble>("EdgeWeightScale", new MguiDouble(0)));
		attributes.add(new Attribute<MguiDouble>("EdgeWeightScaleExp", new MguiDouble(1)));
		attributes.add(new Attribute<MguiBoolean>("EdgeWeightColour", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiDouble>("EdgeWeightCmapMin", new MguiDouble(0.0)));
		attributes.add(new Attribute<MguiDouble>("EdgeWeightCmapMax", new MguiDouble(1.0)));
		AttributeSelection<ColourMap> sel = 
			new AttributeSelection<ColourMap>("EdgeWeightCmap", cmaps, ColourMap.class, cmaps.get(0));
		attributes.add(sel);

		AttributeSelection<String> pos = 
			new AttributeSelection<String>("NodeLabelPos", GraphFunctions.getLabelPositions(), String.class, "SE");
		
		attributes.add(pos);
		
		
		attributes.add(new Attribute<MguiDouble>("NodeScale", new MguiDouble(1.0)));
		attributes.add(new Attribute<MguiDouble>("NodeValueScale", new MguiDouble(0)));
		attributes.add(new Attribute<MguiBoolean>("NodeValueColour", new MguiBoolean(false)));
		sel = new AttributeSelection<ColourMap>("NodeValueCmap", cmaps, ColourMap.class, cmaps.get(0));
		attributes.add(sel);
		
		HashMap<String, NodeShape> node_shapes = InterfaceEnvironment.getVertexShapes();
		AttributeSelectionMap<NodeShape> shapes = 
			new AttributeSelectionMap<NodeShape>("NodeShape", node_shapes, NodeShape.class);
		shapes.setComboMode(ComboMode.AsValues);
		shapes.setComboRenderer(new NodeShapeComboRenderer());
		shapes.setComboWidth(100);
		attributes.add(shapes);
		
		//set default tool
		setDefaultTool(new ToolGraphTransform());
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
				if (this.graph == null)
					box.setText("Current graph: None");
				else
					box.setText("Current graph: " + graph.getName());
				break;
			case 3:
				Point2f coords = getMouseCoords(e.getPoint());
				if (coords == null){
					box.setText("Mouse at: ?");
				}else{
					box.setText("Mouse at: " + MguiDouble.getString(coords.x, 2) + ", "
											 + MguiDouble.getString(coords.y, 2));
					}
				break;
			default:
				box.setText("");	
			}
		
		return index;
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener thisListener){
		if (propertyChange == null || thisListener == null) return;
		propertyChange.addPropertyChangeListener(thisListener);
	}
	
	@Override
	public void removePropertyChangeListener(PropertyChangeListener thisListener){
		if (propertyChange == null || thisListener == null) return;
		propertyChange.removePropertyChangeListener(thisListener);
	}

	public Font getNodeLabelFont(){
		return (Font)attributes.getValue("NodeLabelFont");
	}
	
	@Override
	public boolean isDisplayable(Object obj){
		return obj instanceof InterfaceAbstractGraph;
	}
	
	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		String source = e.getActionCommand();
		
		if (source.startsWith("Set source")){
			String name = source.substring(11);
			ArrayList<InterfaceAbstractGraph> graphs = InterfaceSession.getWorkspace().getGraphs();
			for (int i = 0; i < graphs.size(); i++)
				if (graphs.get(i).getName().equals(name)){
					this.setSource(graphs.get(i));
					return;
					}
			InterfaceSession.log("InterfaceGraphDisplay: Could not set graph '" + name + "' as source..", 
								 LoggingType.Errors);
			return;
			}
		
		if (source.startsWith("Edit attributes")){
			InterfaceSession.getWorkspace().showAttributeDialog(this);
			return;
			}
		
		if (source.startsWith("Mouse mode")){
			if (!(currentTool instanceof ToolGraphTransform)){
				this.setCurrentTool(new ToolGraphTransform());
				}
			//if (currentTool != null && currentTool instanceof ToolGraphTransform)
			((ToolGraphTransform)currentTool).handlePopupEvent(e);
			return;
			}
		
		if (source.startsWith("Layout")){
			HashMap<String, Class<?>> layouts = GraphFunctions.getLayoutTypes();
			String key = source.substring(source.indexOf(".") + 1);
			if (layouts.containsKey(key)){
				setGraphLayout(key);
				return;
				}
			}
		
		if (source.startsWith("Append")){
			String what = source.substring(source.indexOf(".") + 1);
			if (what.equals("Image")){
				ToolGraphImage tool = new ToolGraphImage(ToolGraphImage.Type.Append);
				setCurrentTool(tool);
				return;
				}
			return;
			}
		
		if (source.startsWith("Insert")){
			String what = source.substring(source.indexOf(".") + 1);
			if (what.equals("Image")){
				ToolGraphImage tool = new ToolGraphImage(ToolGraphImage.Type.Insert);
				setCurrentTool(tool);
				return;
				}
			return;
			}
		
		if (source.startsWith("Set plane")){
			CoordinateLayout layout = (CoordinateLayout)current_layout;
			Plane3D plane = layout.getProjectionPlane();
			plane = Plane3DDialog.getPlane3D(plane);
			if (plane == null) return;
			layout.setProjectionPlane(plane);
			return;
			}
		
		super.handlePopupEvent(e);
		
	}
	
	@Override
	public boolean writeSnapshotToFile(File file){
		Component component =  scrollPane.getComponent(0);
		Rectangle2D bounds = component.getBounds();
		BufferedImage image = new  BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = image.createGraphics();
		paint(g);
		g.dispose();
		// We only want the visible graph, not the scroll bars
		int left = this.getInsets().left;
		image = image.getSubimage((int)bounds.getX() + this.getInsets().left, 
								  (int)bounds.getY() + this.getInsets().top, 
								  (int)bounds.getWidth(), 
								  (int)bounds.getHeight());
		return ImagingIOFunctions.writeImageToPng(image, file);
	}
	
	public InterfacePopupMenu getPopupMenu(){
		InterfacePopupMenu menu = super.getPopupMenu();
		
		int start = super.getPopupLength();
		
		menu.add(new JSeparator(), start);
		menu.add(new JSeparator(), start);
		menu.addMenuItem(new JMenuItem("Graph Window", getObjectIcon()));
		menu.add(new JSeparator(), start + 3);
		menu.add(new JSeparator(), start + 3);
		
		JMenu submenu = new JMenu("Set source");
		ArrayList<InterfaceAbstractGraph> graphs = InterfaceSession.getWorkspace().getGraphs();
		int add = 0;
		if (graphs.size() > 0) add = 1;
		for (int i = 0; i < graphs.size(); i++){
			JMenuItem item = new JMenuItem(graphs.get(i).getName());
			item.setActionCommand("Set source." + graphs.get(i).getName());
			submenu.add(item);
			}
		if (add == 1)
			menu.addSubmenu(submenu);
		
		JMenuItem item = new JMenuItem("Edit attributes..");
		item.setActionCommand("Window attributes");
		menu.addMenuItem(item);
		
		submenu = new JMenu("Display mode");
		submenu.add(new JMenuItem("2D"));
		submenu.add(new JMenuItem("3D"));
		menu.addSubmenu(submenu);
		
		if (currentTool != null && currentTool instanceof ToolGraphTransform){
			submenu = new JMenu("Mouse mode");
			submenu.add(new JMenuItem("Pan/zoom"));
			submenu.add(new JMenuItem("Picking"));
			submenu.add(new JMenuItem("Annotating"));
			menu.addSubmenu(submenu);
			}
		
		submenu = new JMenu("Layout");
		HashMap<String, Class<?>> types = GraphFunctions.getLayoutTypes();
		ArrayList<String> names = new ArrayList<String>(types.keySet());
		
		for (int i = 0; i < names.size(); i++)
			submenu.add(new JMenuItem(names.get(i)));
		menu.addSubmenu(submenu);
		
		submenu = new JMenu("Insert");
		submenu.add(new JMenuItem("Image"));
		menu.addSubmenu(submenu);
		
		submenu = new JMenu("Append");
		submenu.add(new JMenuItem("Image"));
		menu.addSubmenu(submenu);
				
		if (viewer != null && current_layout instanceof CoordinateLayout){
			menu.addMenuItem(new JMenuItem("Set plane.."));
			}
		
		return menu;
	}
	
	@Override
	public Point2f getMouseCoords(Point p){
		if (viewer == null) return null;
		Point2D tp = viewer.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, 
        		new Point2D.Double(p.x,p.y));
		return new Point2f((float)tp.getX(), (float)tp.getY());
	}
	
	
	
	public Layout<AbstractGraphNode, AbstractGraphEdge> getGraphLayoutInstance() throws GraphException{
		
		if (graph == null || observable_graph == null)
			throw new GraphException("InterfaceGraphDisplay: Layout requires that a graph" +
									 " be specified.");
		
		Layout<AbstractGraphNode, AbstractGraphEdge> layout;
		
		try{
			Class<?> layoutC = getLayoutClass();
			if (layoutC == null) return null;
			//TODO throw Exception here
			if (layoutC == null){
				layout = null;
				return null;
				}
			
			Object[] constructorArgs = {observable_graph};
	            Constructor<?> constructor = layoutC
	                    .getConstructor(new Class[] {Graph.class});
	            Object o = constructor.newInstance(constructorArgs);
	            layout = (Layout<AbstractGraphNode, AbstractGraphEdge>) o;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        	}
		return layout;
	}
	
	public Class<?> getLayoutClass(){
		return (Class<?>)attributes.getValue("Layout");
	}
	
	@Override
	public DefaultMutableTreeNode getDisplayObjectsNode(){
		
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Graphs");
		ArrayList<InterfaceAbstractGraph> graphs = InterfaceSession.getWorkspace().getGraphs();
		for (int i = 0; i < graphs.size(); i++)
			node.add(new InterfaceTreeNode(graphs.get(i)));
		return node;
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e) {
		if (e.getAttribute().getName().equals("Background")){
			viewer.setBackground((Color)attributes.getValue("Background"));
			viewer.repaint();
			return;
			}
		if (e.getAttribute().getName().equals("NodeLabelPos")){
			Position position = getLabelPosition();
			this.node_label_renderer.setPosition(position);
			updateViewer();
			return;
			}
		if (e.getAttribute().getName().equals("Layout")){
			try{
				setViewer();
			}catch (GraphException ex){
				InterfaceSession.log("InterfaceGraphDisplay: Error setting layout.", 
									 LoggingType.Errors);
				}
			return;
			}
		if (e.getAttribute().getName().contains("Colour")){
			updateViewer();
			return;
			}
		if (e.getAttribute().getName().equals("LabelNodes")){
			this.node_label_renderer.show(((MguiBoolean)attributes.getValue("LabelNodes")).getTrue());
			updateViewer();
			return;
			}
		if (e.getAttribute().getName().equals("LabelEdges")){
			this.edge_label_renderer.show(((MguiBoolean)attributes.getValue("LabelEdges")).getTrue());
			updateViewer();
			return;
			}
		if (e.getAttribute().getName().contains("Label")){
			updateViewer();
			return;
			}
		if (e.getAttribute().getName().contains("Scale")){
			updateViewer();
			return;
			}
		if (e.getAttribute().getName().contains("Shape")){
			updateViewer();
			return;
			}
		if (e.getAttribute().getName().equals("ShowEdges")){
			this.show_edges.show(((MguiBoolean)attributes.getValue("ShowEdges")).getTrue());
			updateViewer();
			return;
			}
		if (e.getAttribute().getName().equals("ShowNodes")){
			this.show_nodes.show(((MguiBoolean)attributes.getValue("ShowNodes")).getTrue());
			updateViewer();
			return;
			}
		if (e.getAttribute().getName().equals("ShowArrows")){
			this.show_arrows.show(((MguiBoolean)attributes.getValue("ShowArrows")).getTrue());
			updateViewer();
			return;
			}
		if (e.getAttribute().getName().equals("EdgeOffset")){
			AbstractEdgeShapeTransformer<AbstractGraphNode,AbstractGraphEdge> aesf = 
	            (AbstractEdgeShapeTransformer<AbstractGraphNode,AbstractGraphEdge>)viewer.getRenderContext().getEdgeShapeTransformer();
			aesf.setControlOffsetIncrement(((MguiFloat)attributes.getValue("EdgeOffset")).getFloat());
			updateViewer();
			return;
			}
		
	}
	
	protected void updateViewer(){
		if (viewer == null) return;
		viewer.updateUI();
	}
	
	public void setViewer() throws GraphException{
		Layout<AbstractGraphNode, AbstractGraphEdge> layout = getGraphLayoutInstance();
		current_layout = null;
		if (layout == null)
			throw new GraphException("InterfaceGraphDisplay: No layout defined.");
		
		current_layout = layout;
	
		if (viewer == null){
			viewer = new VisualizationViewer<AbstractGraphNode, AbstractGraphEdge>(layout);
			
			viewer.setBackground((Color)attributes.getValue("Background"));
			scrollPane = new GraphZoomScrollPane(viewer);
			add(scrollPane, BorderLayout.CENTER);
			if (this.currentTool != null)
				currentTool.setTargetPanel(this);
			
			viewer.addMouseListener(viewer_adapter);
			viewer.addMouseMotionListener(viewer_adapter);
			viewer.addMouseWheelListener(viewer_adapter);
			
			// Node stuff
			viewer.getRenderContext().setVertexIncludePredicate(show_nodes);
			viewer.getRenderer().setVertexLabelRenderer(node_label_renderer);
			viewer.getRenderContext().setVertexLabelTransformer(vertex_label_transformer);
			viewer.getRenderContext().setVertexFontTransformer(node_font_transformer);
			viewer.getRenderContext().setVertexFillPaintTransformer(node_fill_transformer);
			viewer.getRenderContext().setVertexShapeTransformer(vertex_shape_transformer);
			
			// Edge stuff
			viewer.getRenderContext().setEdgeIncludePredicate(show_edges);
			viewer.getRenderer().setEdgeLabelRenderer(edge_label_renderer);
			viewer.getRenderContext().setEdgeStrokeTransformer(edge_stroke_transformer);
			viewer.getRenderContext().setEdgeLabelTransformer(edge_label_transformer);
			viewer.getRenderContext().setEdgeDrawPaintTransformer(edge_colour_transformer);
			viewer.getRenderContext().setEdgeArrowPredicate(show_arrows);
			viewer.getRenderContext().setArrowFillPaintTransformer(arrow_colour_transformer);
			
			viewer.setGraphLayout(layout);
		}else{
			viewer.setGraphLayout(layout);
			}
		
		GraphFunctions.scaleToGraphExtents(this);
		viewer.repaint();
		this.repaint();
	}
	
	protected Position getLabelPosition(){
		String pos = (String)attributes.getValue("NodeLabelPos");
		return GraphFunctions.getLabelPosition(pos);
	}
	
	public void setGraph(InterfaceAbstractGraph graph) throws GraphException{
		if (this.graph != null)
			this.graph.removeGraphListener(this);
		this.graph = graph;
		this.observable_graph = new ObservableGraph<AbstractGraphNode, AbstractGraphEdge>(graph);
		observable_graph.addGraphEventListener(this);
		graph.addGraphListener(this);
		
		//set edge weight colour map limits if applicable
		ArrayList<AbstractGraphEdge> edges = new ArrayList<AbstractGraphEdge>(graph.getEdges());
		if (edges.size() > 0 && edges.get(0) instanceof WeightedGraphEdge){
			double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
			for (int i = 0; i < edges.size(); i++){
				double weight = ((WeightedGraphEdge)edges.get(i)).getWeight();
				min = Math.min(min, weight);
				max = Math.max(max, weight);
				}
			attributes.setValue("EdgeWeightCmapMin", new MguiDouble(min));
			attributes.setValue("EdgeWeightCmapMax", new MguiDouble(max));
		}else{
			attributes.setValue("EdgeWeightCmapMin", new MguiDouble(0));
			attributes.setValue("EdgeWeightCmapMax", new MguiDouble(1));
			}
		
		setViewer();
	}
	
	//TODO throw exceptions
	public boolean setGraphLayout(String name){
		
		AttributeSelectionMap<Layout<AbstractGraphNode, AbstractGraphEdge>> map = 
			(AttributeSelectionMap<Layout<AbstractGraphNode, AbstractGraphEdge>>)attributes.getAttribute("Layout");
		
		map.setValue(name);
        return true;
	}
	
	@Override
	public boolean setSource(Object obj){
		if (!isDisplayable(obj)) return false;
		try{
			setGraph((InterfaceAbstractGraph)obj);
			//setName(getName());
			title_panel.updateTitle();
		}catch (GraphException ex){
			InterfaceSession.log("InterfaceDisplayPanel: Error setting source graph.", 
								 LoggingType.Errors);
			return false;
			}
		
		this.fireDisplayListeners();
		return true;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		treeNode.add(attributes.issueTreeNode());
	}
	
	public boolean getToolLock(){
		return toolLock;
	}
	
	public void setToolLock(boolean lock){
		toolLock = lock;
	}
	
	public void addPropertyChangeObject(GraphicPropertyListener thisObj){
		addPropertyChangeListener(thisObj.getPropertyListener());
	}
	
	@Override
	public ToolGraph getCurrentTool(){
		return currentTool;
	}
	
	@Override
	public boolean setCurrentTool(ToolGraph tool){
		if (getToolLock()) return false;
		if (tool == null) return false;	//TODO remove current tool if this is null
		
		ToolGraph newTool = tool;
		if (newTool.isImmediate()){
			newTool.setTargetPanel(this);
			newTool.handleToolEvent(new ToolInputEvent(this,
													   ToolConstants.TOOL_IMMEDIATE,
													   new Point()));
			return true;
			}
		
		if (currentTool != null)
			toolInputAdapter.removeListener(currentTool);
		
		currentTool = newTool;
		toolInputAdapter.addListener(currentTool);
			
		currentTool.setTargetPanel(this);
		return true;
	}
	
	public void finishTool(){
		if (getToolLock())
			setToolLock(false);
		
		if (currentTool != null && currentTool != defaultTool){
			currentTool.deactivate();
			toolInputAdapter.removeListener(currentTool);
			}
		
		//setTool(defaultTool);
		if (currentTool != defaultTool){
			ToolGraph oldTool = currentTool;
			currentTool.removeListener(this);
			currentTool.deactivate();
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
	public boolean setDefaultTool(ToolGraph tool){
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
	public boolean isToolable(Tool tool) {
		return tool instanceof ToolGraph;
	}

	
	public DefaultMutableTreeNode getDisplayObjectsNode(InterfaceDisplayPanel p){
		DefaultMutableTreeNode node = new DefaultMutableTreeNode("Graphs");
		ArrayList<InterfaceAbstractGraph> graphs = InterfaceSession.getWorkspace().getGraphs();
		for (int i = 0; i < graphs.size(); i++)
			node.add(new InterfaceTreeNode(graphs.get(i)));
		return node;
	}
	
	public void updateFromGraph(){
		updateFromGraph(0);
	}
	
	public void updateFromGraph(int code){
		if (graph == null || viewer == null) return;
		
		//labels?
		if (code == InterfaceGraphEvent.GE_GENERAL ||
			code ==	InterfaceGraphEvent.GE_LABELS){
			
			}
		
	}
	
	public void appendPaintable(Paintable paintable){
		if (viewer == null) return;
		viewer.addPostRenderPaintable(paintable);
	}
	
	public void insertPaintable(Paintable paintable){
		if (viewer == null) return;
		viewer.addPostRenderPaintable(paintable);
	}

	/***********************************************
	 * Handle a graph event on the current ObservableGraph.
	 */
	@Override
	public void handleGraphEvent(GraphEvent<AbstractGraphNode, AbstractGraphEdge> evt) {
		
		
		
	}
	
	public void graphUpdated(InterfaceGraphEvent e) {
		if (graph == null || !e.getSource().equals(graph)) return;
		updateFromGraph(e.eventCode);
		viewer.repaint();
	}
	
	@Override
	public String toString(){
		return "Graph Display Panel: " + getName();
	}
	
	@Override
	public String getTitle(){
		if (this.graph == null) return getName();
		return getName() + " [" + graph.getName() + "]";
	}
	
	public ColourMap getEdgeWeightColourMap(){
		ColourMap map = (ColourMap)attributes.getValue("EdgeWeightCmap");
		//if (map == null) return null;
		//map.mapMin = ((MguiDouble)attributes.getValue("EdgeWeightCmapMin")).getValue();
		//map.mapMax = ((MguiDouble)attributes.getValue("EdgeWeightCmapMax")).getValue();
		return map;
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
		
		String source = "";
		
		if (graph != null)
			source = graph.getName();
		
		writer.write(_tab + "<InterfaceGraphDisplay\n" +
					_tab2 + "name='" + getName() + "'\n" + 
					_tab2 + "source='" + source + "'\n" +
					_tab + ">\n");
		
		//TODO: write stuff
		
		attributes.writeXML(tab + 1, writer, options, progress_bar);
		
		writer.write(_tab + "</InterfaceGraphDisplay>\n");
		
	}
	
	
	//Transformers n' stuff
	
	private Transformer<AbstractGraphNode,Font> node_font_transformer = 
		new Transformer<AbstractGraphNode,Font>(){

			@Override
			public Font transform(AbstractGraphNode node) {
				return getNodeLabelFont();
			}
			
		};
	
	private Transformer<AbstractGraphNode,Paint> node_fill_transformer = 
		new Transformer<AbstractGraphNode,Paint>(){

			@Override
			public Paint transform(AbstractGraphNode node) {
				
				if (((MguiBoolean)attributes.getValue("NodeValueColour")).getTrue()){
					//get colour from colour map
					ColourMap cmap = (ColourMap)attributes.getValue("NodeValueCmap");
					return cmap.getColour(node.getCurrentValue().getValue()).getColor();
				}else{
					return (Color)attributes.getValue("NodeColour");
					}
				
			}
			
		};
		
	private Transformer<AbstractGraphNode,Shape> vertex_shape_transformer = 
		new Transformer<AbstractGraphNode,Shape>(){

			@Override
			public Shape transform(AbstractGraphNode node) {
				NodeShape gshape = (NodeShape)attributes.getValue("NodeShape");
				Shape shape = gshape.getShape();
				if (shape == null) shape = getDefaultNodeShape();
				double scale = ((MguiDouble)attributes.getValue("NodeScale")).getValue();
				if (scale != 1.0){
					AffineTransform trans = new AffineTransform();
					trans.setToScale(scale, scale);
					shape = trans.createTransformedShape(shape);
					}
				return shape;
			}
			
		};
		
	private Transformer<AbstractGraphEdge,Paint> edge_colour_transformer = 
		new Transformer<AbstractGraphEdge,Paint>(){

			@Override
			public Paint transform(AbstractGraphEdge edge) {
				boolean cmapped = ((MguiBoolean)attributes.getValue("EdgeWeightColour")).getTrue();
				if (!cmapped || !(edge instanceof WeightedGraphEdge))
					return (Color)attributes.getValue("EdgeColour");
				double weight = ((WeightedGraphEdge)edge).getWeight();
				ColourMap cmap = getEdgeWeightColourMap();
				if (cmap == null) 
					return (Color)attributes.getValue("EdgeColour");
				
				Colour colour = cmap.getColour(weight, 
											  ((MguiDouble)attributes.getValue("EdgeWeightCmapMin")).getValue(),
											  ((MguiDouble)attributes.getValue("EdgeWeightCmapMax")).getValue());
				if (colour == null)
					return (Color)attributes.getValue("EdgeColour");
				return colour.getColor();
			}
			
		};
		
		private Transformer<AbstractGraphEdge,Stroke> edge_stroke_transformer = 
			new Transformer<AbstractGraphEdge,Stroke>(){

				@Override
				public Stroke transform(AbstractGraphEdge edge) {
					double scale = ((MguiDouble)attributes.getValue("EdgeWeightScale")).getValue();
					
					if (scale <= 0)
						return new BasicStroke((float)((MguiDouble)attributes.getValue("EdgeScale")).getValue());
					double weight = ((WeightedGraphEdge)edge).getWeight();
					double exp = ((MguiDouble)attributes.getValue("EdgeWeightScaleExp")).getValue();
					return new BasicStroke((float)Math.pow(weight * scale, exp));
				}
				
			};
		
		
	private Transformer<AbstractGraphEdge,Paint> arrow_colour_transformer = 
		new Transformer<AbstractGraphEdge,Paint>(){

			@Override
			public Paint transform(AbstractGraphEdge node) {
				return (Color)attributes.getValue("EdgeColour");
			}
			
		};
	
	private Transformer<AbstractGraphNode, String> vertex_label_transformer = 
		new Transformer<AbstractGraphNode, String>(){
			@Override
			public String transform(AbstractGraphNode node) {
				return node.getLabel();
			}
		};
		
	private Transformer<AbstractGraphEdge, String> edge_label_transformer = 
		new Transformer<AbstractGraphEdge, String>(){
			@Override
			public String transform(AbstractGraphEdge edge) {
				return edge.getLabel();
			}
		};
	
	//relay mouse events from vis viewer to this panel
	class ViewerMouseListener extends MouseInputAdapter implements GraphMouse {
		
		InterfaceGraphDisplay parent;
		
		public ViewerMouseListener(InterfaceGraphDisplay parent){
			this.parent = parent;
		}
		
		MouseEvent getEventClone(MouseEvent e){
			return new MouseEvent(parent, e.getID(), e.getWhen(), 
								  e.getModifiers(),	e.getX(), e.getY(), 
								  e.getClickCount(), e.isPopupTrigger(), 
								  e.getButton());
		}
		
		MouseWheelEvent getWheelEventClone(MouseWheelEvent e){
			return new MouseWheelEvent(parent, e.getID(), e.getWhen(), 
									   e.getModifiers(), e.getX(), e.getY(), 
									   e.getClickCount(), e.isPopupTrigger(), 
									   e.getScrollType(), e.getScrollAmount(),
									   e.getWheelRotation());
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			MouseEvent me = getEventClone(e);
			parent.dispatchEvent(me);
			}
		
		@Override
		public void mouseDragged(MouseEvent e) {
			MouseEvent me = getEventClone(e);
			parent.dispatchEvent(me);
			}
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			MouseWheelEvent me = getWheelEventClone(e);
			parent.dispatchEvent(me);
			}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			MouseEvent me = getEventClone(e);
			parent.dispatchEvent(me);
		} 
		
		@Override
		public void mousePressed(MouseEvent e){
			MouseEvent me = getEventClone(e);
			parent.dispatchEvent(me);
		}
		
		@Override
		public void mouseClicked(MouseEvent e){
			MouseEvent me = getEventClone(e);
			parent.dispatchEvent(me);
		}
	} 						
}