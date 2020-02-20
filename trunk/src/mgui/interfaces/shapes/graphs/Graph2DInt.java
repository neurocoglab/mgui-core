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

package mgui.interfaces.shapes.graphs;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point2d;

import mgui.geometry.Graph2D;
import mgui.geometry.Shape2D;
import mgui.geometry.util.NodeShape;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.graphs.AbstractGraphEdge;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.WeightedGraphEdge;
import mgui.interfaces.graphs.util.GraphFunctions;
import mgui.interfaces.graphs.util.GraphFunctions.ArrowVisibility;
import mgui.interfaces.graphs.util.GraphFunctions.EdgeVisibility;
import mgui.interfaces.graphs.util.GraphFunctions.GraphEdgeLabelRenderer;
import mgui.interfaces.graphs.util.GraphFunctions.GraphNodeLabelRenderer;
import mgui.interfaces.graphs.util.GraphFunctions.NodeVisibility;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.shapes.PointSet2DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.attributes.ShapeAttribute;
import mgui.interfaces.shapes.graphs.util.Graph2DLayout;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.util.Colour;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.ObservableGraph;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.visualization.BasicTransformer;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.decorators.AbstractEdgeShapeTransformer;
import edu.uci.ics.jung.visualization.decorators.DirectionalEdgeArrowTransformer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;
import edu.uci.ics.jung.visualization.transform.MutableTransformerDecorator;
import edu.uci.ics.jung.visualization.transform.shape.GraphicsDecorator;
import edu.uci.ics.jung.visualization.util.Caching;

/*****************************************************
 * Graph shape represented in R2. A 3D graph will render on a section by first selecting only
 * those vertices contained by the section extents.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Graph2DInt extends PointSet2DInt implements GraphEventListener<AbstractGraphNode, AbstractGraphEdge>{

	protected HashMap<AbstractGraphNode, Integer> vertex_map = new HashMap<AbstractGraphNode, Integer>();
	protected DrawingTransformer drawing_transformer;
	protected RenderServer view_server;
	protected Graph2DLayout<AbstractGraphNode, AbstractGraphEdge> graph_layout;
	protected ObservableGraph<AbstractGraphNode, AbstractGraphEdge> observable_graph;
	
	protected EdgeVisibility show_edges = GraphFunctions.getEdgeVisibilityPredicate(true);
	protected ArrowVisibility show_arrows = GraphFunctions.getArrowVisibilityPredicate(true);
	protected NodeVisibility show_vertices = GraphFunctions.getNodeVisibilityPredicate(true);
	protected Position label_position = Position.SE;
	protected GraphNodeLabelRenderer vertex_label_renderer;
	protected GraphEdgeLabelRenderer edge_label_renderer;
	protected DefaultVertexLabelRenderer vertex_label_color_renderer;
	
	public Graph2DInt(){
		super();
		init();
	}
	
	public Graph2DInt(Graph2D graph, Graph2DLayout<AbstractGraphNode, AbstractGraphEdge> layout){
		this(graph, layout, null);
	}
	
	public Graph2DInt(Graph2D graph, Graph2DLayout<AbstractGraphNode, AbstractGraphEdge> layout,
					  HashMap<AbstractGraphNode,Integer> index_map){
		super();
		this.setGraph(graph);
		// Set parent index map
		if (index_map != null){
			ArrayList<AbstractGraphNode> keys = new ArrayList<AbstractGraphNode> (index_map.keySet());
			map_idx_to_parent = new HashMap<Integer,Integer>();
			for (int i = 0; i < keys.size(); i++){
				AbstractGraphNode node = keys.get(i);
				int parent_idx = index_map.get(node);
				int child_idx = vertex_map.get(node);
				map_idx_to_parent.put(child_idx, parent_idx);
				}
			}
		graph_layout = layout;
		init();
	}
	
	public void setShape(Shape2D shape){
		if (!(shape instanceof Graph2D)) return;
		setGraph((Graph2D)shape);
	}
	
	public void setGraph(Graph2D graph2d){
		this.thisShape=graph2d;
		this.observable_graph = new ObservableGraph<AbstractGraphNode, AbstractGraphEdge>(graph2d.getGraph());
		observable_graph.addGraphEventListener(this);
		vertex_map.clear();
		
		ArrayList<AbstractGraphNode> nodes = graph2d.getJungVertices();
		for (int i = 0; i < nodes.size(); i++)
			vertex_map.put(nodes.get(i), i);
	}
	
	private void init(){
		// Add attributes here
		attributes.add(new ShapeAttribute<MguiDouble>("EdgeWeightScale", new MguiDouble(0), true));
		attributes.add(new ShapeAttribute<MguiDouble>("EdgeWeightScaleExp", new MguiDouble(1), true));
		attributes.add(new ShapeAttribute<MguiBoolean>("EdgeWeightColour", new MguiBoolean(false), true));
		attributes.add(new ShapeAttribute<MguiDouble>("EdgeWeightCmapMin", new MguiDouble(0.0), true));
		attributes.add(new ShapeAttribute<MguiDouble>("EdgeWeightCmapMax", new MguiDouble(1.0), true));
		ArrayList<ColourMap> cmaps = InterfaceEnvironment.getColourMaps();
		AttributeSelection<ColourMap> sel = new AttributeSelection<ColourMap>("EdgeWeightCmap", cmaps, ColourMap.class, cmaps.get(0));
		attributes.add(sel);
		attributes.add(new ShapeAttribute<MguiFloat>("2D.EdgeScale", new MguiFloat(1f), true));
		attributes.add(new ShapeAttribute<MguiBoolean>("ShowEdges", new MguiBoolean(true), true));
		attributes.add(new ShapeAttribute<MguiFloat>("2D.ArrowScale", new MguiFloat(1f), true, false));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.ShowArrows", new MguiBoolean(true), true, false));
		attributes.add(new ShapeAttribute<MguiFloat>("2D.EdgeOffset", new MguiFloat(20f), true, false));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.ExcludeToSection", new MguiBoolean(true), true,false));

		// Set up view server
		view_server = new RenderServer(graph_layout);
		
		RenderContext<AbstractGraphNode, AbstractGraphEdge> rc = view_server.getRenderContext();
		
		// Node stuff
		rc.setVertexIncludePredicate(show_vertices);
		vertex_label_renderer = new GraphNodeLabelRenderer();
		vertex_label_renderer.show(((MguiBoolean)attributes.getValue("2D.ShowVertexLabels")).getTrue());
		view_server.getRenderer().setVertexLabelRenderer(vertex_label_renderer);
		rc.setVertexLabelTransformer(vertex_label_transformer);
		view_server.setForeground((Color)attributes.getValue("2D.LabelColour"));
		rc.setVertexFontTransformer(vertex_font_transformer);
		rc.setVertexFillPaintTransformer(vertex_fill_transformer);
		rc.setVertexShapeTransformer(vertex_shape_transformer);
		
		// Edge stuff
		rc.setEdgeIncludePredicate(show_edges);
		edge_label_renderer = new GraphEdgeLabelRenderer();
		edge_label_renderer.show(false);
		view_server.getRenderer().setEdgeLabelRenderer(edge_label_renderer);
		rc.setEdgeStrokeTransformer(edge_stroke_transformer);
		rc.setEdgeLabelTransformer(edge_label_transformer);
		rc.setEdgeDrawPaintTransformer(edge_colour_transformer);
		rc.setEdgeArrowPredicate(show_arrows);
		rc.setArrowFillPaintTransformer(arrow_colour_transformer);
		rc.setArrowDrawPaintTransformer(arrow_colour_transformer);
		rc.setEdgeArrowTransformer(arrow_shape_transformer);
		edge_shape_transformer = (AbstractEdgeShapeTransformer<AbstractGraphNode,AbstractGraphEdge>)rc.getEdgeShapeTransformer();
		
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine d){
		if (!isVisible()) return;
		
		if (drawing_transformer == null){
			drawing_transformer = new DrawingTransformer(d);
			view_server.getRenderContext().setMultiLayerTransformer(drawing_transformer);
		}else{
			drawing_transformer.setDrawingEngine(d);
			}
		
		// Called here because draw should be called by the paintComponent sequence of the
		// drawing window.
		view_server.render(g);
		
	}
	
	
	@Override
	public void attributeUpdated(AttributeEvent event){
		
		Attribute<?> attribute = event.getAttribute();
		
		if (attribute.getName().equals("2D.ShowVertices")){
			show_vertices.show(((MguiBoolean)attribute.getValue()).getTrue());
			}
		
		if (attribute.getName().equals("ShowEdges")){
			show_edges.show(((MguiBoolean)attribute.getValue()).getTrue());
			}
		
		if (attribute.getName().equals("2D.ShowArrows")){
			show_arrows.show(((MguiBoolean)attribute.getValue()).getTrue());
			}
		
		if (attribute.getName().equals("2D.EdgeOffset")){
			edge_shape_transformer.setControlOffsetIncrement(((MguiFloat)attribute.getValue()).getFloat());
			}
		
		if (attribute.getName().equals("2D.ShowVertexLabels")){
			vertex_label_renderer.show(((MguiBoolean)attribute.getValue()).getTrue());
			}
		
		if (attribute.getName().equals("2D.ExcludeToSection")){
			if (parentShape != null)
				parentShape.fireShapeModified();
			}
		
		if (attribute.getName().equals("2D.LabelPosition")){
			String pos = (String)attribute.getValue();
			Position position = GraphFunctions.getLabelPosition(pos); // getLabelPosition();
			vertex_label_renderer.setPosition(position);
			}
		
		if (attribute.getName().equals("2D.LabelColour")){
			view_server.setForeground((Color)attribute.getValue());
			}
		
		super.attributeUpdated(event);
	}
	
	
	@Override
	public void updateShape(){
		// Set predicates, etc. from attributed
		show_vertices.show(((MguiBoolean)attributes.getValue("2D.ShowVertices")).getTrue());
		show_edges.show(((MguiBoolean)attributes.getValue("ShowEdges")).getTrue());
		show_arrows.show(((MguiBoolean)attributes.getValue("2D.ShowArrows")).getTrue());
		edge_shape_transformer.setControlOffsetIncrement(((MguiFloat)attributes.getValue("2D.EdgeOffset")).getFloat());
		vertex_label_renderer.show(((MguiBoolean)attributes.getValue("2D.ShowVertexLabels")).getTrue());
		String pos = (String)attributes.getValue("2D.LabelPosition");
		Position position = GraphFunctions.getLabelPosition(pos);
		vertex_label_renderer.setPosition(position);
		view_server.setForeground((Color)attributes.getValue("2D.LabelColour"));
		super.updateShape();
	}
	
	@Override
	public void handleGraphEvent(GraphEvent<AbstractGraphNode, AbstractGraphEdge> evt) {
		// TODO Auto-generated method stub
		
	}

	public ColourMap getEdgeWeightColourMap(){
		ColourMap map = (ColourMap)attributes.getValue("EdgeWeightCmap");
		return map;
	}
	
	
	protected Stroke getEdgeStroke(AbstractGraphEdge edge){
	
		return (Stroke)edge_stroke_transformer.transform(edge);
	}
	
	protected NodeShape getVertexShape(AbstractGraphNode vertex){
		
		return (NodeShape)vertex_shape_transformer.transform(vertex);
	}
	
	
	public float getVertexScale(AbstractGraphNode node){
		boolean scale_vertices = ((MguiBoolean)attributes.getValue("ScaleVertices")).getTrue();
		float general_scale = ((MguiFloat)attributes.getValue("2D.VertexScale")).getFloat();
		float exp_scale = ((MguiFloat)attributes.getValue("2D.VertexScaleExp")).getFloat();
		
		if (!scale_vertices)
			return general_scale;
		
		String column = (String)attributes.getValue("ScaleData");
		if (column == null) return general_scale;
		
		float value = (float)getDatumAtVertex(column, vertex_map.get(node)).getValue();
		return (float)Math.pow(general_scale * value, exp_scale);
		
	}
	
	public Color getVertexColour(AbstractGraphNode node){
		if (!this.showData())
			return getVertexColour();
		String column = getCurrentColumn();
		if (column == null) return getVertexColour();
		ColourMap cmap = getColourMap(column);
		if (cmap == null) return getVertexColour();
		float value = (float)getDatumAtVertex(column, vertex_map.get(node)).getValue();
		
		return cmap.getColour(value).getColor();
	}
	
	public Color getEdgeColour(AbstractGraphEdge edge){
		return (Color)edge_colour_transformer.transform(edge);
	}
	
	
	//Transformers n' stuff
	
	private Transformer<AbstractGraphNode,Font> vertex_font_transformer = 
		new Transformer<AbstractGraphNode,Font>(){

			@Override
			public Font transform(AbstractGraphNode vertex) {
				return getLabelFont();
			}
			
		};
	
	private Transformer<AbstractGraphNode,Paint> vertex_fill_transformer = 
		new Transformer<AbstractGraphNode,Paint>(){

			@Override
			public Paint transform(AbstractGraphNode vertex) {
				
				return getVertexColour(vertex);
				
			}
			
		};
		
	private Shape getDefaultVertexShape(){
		return new Ellipse2D.Float(-10,-10,20,20);
	}
		
	private Transformer<AbstractGraphNode,Shape> vertex_shape_transformer = 
		new Transformer<AbstractGraphNode,Shape>(){

			@Override
			public Shape transform(AbstractGraphNode vertex) {
				NodeShape gshape = (NodeShape)attributes.getValue("2D.VertexShape");
				Shape shape = null;
				if (gshape == null) 
					shape = getDefaultVertexShape();
				else
					shape = gshape.getShape();
				//if (shape == null) shape = getDefaultVertexShape();
				double scale = getVertexScale(vertex);
				
				if (scale != 1.0 && scale > 0){
					AffineTransform trans = new AffineTransform();
					trans.setToScale(scale, scale);
					shape = trans.createTransformedShape(shape);
					}
				return shape;
			}
			
		};
		
		private DirectionalEdgeArrowTransformer<AbstractGraphNode,AbstractGraphEdge> arrow_shape_transformer = 
				new DirectionalEdgeArrowTransformer<AbstractGraphNode,AbstractGraphEdge>(10, 8, 4){

					@Override
					public Shape transform(Context<Graph<AbstractGraphNode,AbstractGraphEdge>,AbstractGraphEdge> context){
						Shape shape = super.transform(context);
						double scale = ((MguiFloat)attributes.getValue("2D.ArrowScale")).getValue();
						
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
					return (Color)attributes.getValue("2D.LineColour");
				double weight = ((WeightedGraphEdge)edge).getWeight();
				ColourMap cmap = getEdgeWeightColourMap();
				if (cmap == null) 
					return (Color)attributes.getValue("2D.LineColour");
				
				Colour colour = cmap.getColour(weight, 
											  ((MguiDouble)attributes.getValue("EdgeWeightCmapMin")).getValue(),
											  ((MguiDouble)attributes.getValue("EdgeWeightCmapMax")).getValue());
				if (colour == null)
					return (Color)attributes.getValue("2D.LineColour");
				return colour.getColor();
			}
			
		};
		
		private Transformer<AbstractGraphEdge,Stroke> edge_stroke_transformer = 
			new Transformer<AbstractGraphEdge,Stroke>(){

				@Override
				public Stroke transform(AbstractGraphEdge edge) {
					double scale = ((MguiDouble)attributes.getValue("EdgeWeightScale")).getValue();
					
					if (scale <= 0)
						return new BasicStroke((float)((MguiFloat)attributes.getValue("2D.EdgeScale")).getValue());
					scale = scale * ((MguiFloat)attributes.getValue("2D.EdgeScale")).getValue();
					double weight = ((WeightedGraphEdge)edge).getWeight();
					double exp = ((MguiDouble)attributes.getValue("EdgeWeightScaleExp")).getValue();
					return new BasicStroke((float)Math.pow(weight * scale, exp));
				}
				
			};
			
			private AbstractEdgeShapeTransformer<AbstractGraphNode,AbstractGraphEdge> edge_shape_transformer;
		
		
	private Transformer<AbstractGraphEdge,Paint> arrow_colour_transformer = 
		new Transformer<AbstractGraphEdge,Paint>(){

			@Override
			public Paint transform(AbstractGraphEdge node) {
				return (Color)attributes.getValue("2D.LineColour");
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

		
	protected class DrawingTransformer extends BasicTransformer implements MutableTransformer{
		
		protected DrawingEngine drawing_engine;
		IdentityTransform identity = new IdentityTransform();
		
		public void setDrawingEngine(DrawingEngine de){
			this.drawing_engine = de;
		};
		
		public DrawingTransformer(DrawingEngine de){
			drawing_engine = de;
			
//			this.setLayoutTransformer(new MutableAffineTransformer(){
//				public AffineTransform getTransform(){
//					return drawing_engine.getMap().getTransform();
//				}
//			});
		}
		
		@Override
		public Point2D transform(Point2D p) {
			Point p2 = drawing_engine.getScreenPoint(new Point2d(p.getX(), p.getY()));
			return new Point2D.Float(p2.x, p2.y);
		}
		
		@Override
		public Point2D transform(Layer layer, Point2D p) {
			if(layer == Layer.LAYOUT) return transform(p);
			if(layer == Layer.VIEW) return identity.transform(p);
			return null;
		}
		
		@Override
		public MutableTransformer getTransformer(Layer layer){
			if(layer == Layer.LAYOUT) return this;
			if(layer == Layer.VIEW) return identity;
			return null;
		}
		
		class IdentityTransform extends MutableTransformerDecorator{

			public IdentityTransform(){
				super(null);
			}
			
			@Override
			public Point2D transform(Point2D p) {
				return new Point2D.Double(p.getX(),p.getY());
			}

			@Override
			public Point2D inverseTransform(Point2D p) {
				return new Point2D.Double(p.getX(),p.getY());
			}

			@Override
			public Shape transform(Shape shape) {
				return shape;
			}

			@Override
			public Shape inverseTransform(Shape shape) {
				return shape;
			}
			
		}
		
		@Override
		public void translate(double dx, double dy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setTranslate(double dx, double dy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void scale(double sx, double sy, Point2D point) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setScale(double sx, double sy, Point2D point) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void rotate(double radians, Point2D point) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void rotate(double radians, double x, double y) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void shear(double shx, double shy, Point2D from) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void concatenate(AffineTransform transform) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void preConcatenate(AffineTransform transform) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public double getScaleX() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getScaleY() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getScale() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getTranslateX() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getTranslateY() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getShearX() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double getShearY() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public AffineTransform getTransform() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double getRotation() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		
		
	};
		
	
	protected class RenderServer extends BasicVisualizationServer<AbstractGraphNode, AbstractGraphEdge>{
		
		Dimension current_size = new Dimension(600,600);
		
		public RenderServer(Graph2DLayout<AbstractGraphNode, AbstractGraphEdge> layout){
			super(layout);
		}
		
		public void render(Graphics2D g2d){
			//this.renderGraph(g2d);
			
			Rectangle bounds = g2d.getDeviceConfiguration().getBounds();
			current_size = new Dimension(bounds.getSize());
			
			if(renderContext.getGraphicsContext() == null) {
		        renderContext.setGraphicsContext(new GraphicsDecorator(g2d));
	        } else {
	        	renderContext.getGraphicsContext().setDelegate(g2d);
	        }
	        renderContext.setScreenDevice(this);
		    Layout<AbstractGraphNode, AbstractGraphEdge> layout = model.getGraphLayout();

			AffineTransform oldXform = g2d.getTransform();
	        AffineTransform newXform = new AffineTransform(oldXform);
	        newXform.concatenate(
	        		renderContext.getMultiLayerTransformer().getTransformer(Layer.VIEW).getTransform());
			
	        g2d.setTransform(newXform);

			// if there are  preRenderers set, paint them
			for(Paintable paintable : preRenderers) {

			    if(paintable.useTransform()) {
			        paintable.paint(g2d);
			    } else {
			        g2d.setTransform(oldXform);
			        paintable.paint(g2d);
	                g2d.setTransform(newXform);
			    }
			}
			
	        if(layout instanceof Caching) {
	        	((Caching)layout).clear();
	        }
	        
	        renderer.render(renderContext, layout);
			
			// if there are postRenderers set, do it
			for(Paintable paintable : postRenderers) {

			    if(paintable.useTransform()) {
			        paintable.paint(g2d);
			    } else {
			        g2d.setTransform(oldXform);
			        paintable.paint(g2d);
	                g2d.setTransform(newXform);
			    }
			}
			g2d.setTransform(oldXform);
			
		}
		
		@Override
		public Dimension getSize() {
			return current_size;
		}
		
	}
	
}