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

package mgui.interfaces.graphs.util;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jogamp.vecmath.Point3f;

import mgui.geometry.Mesh3D;
import mgui.geometry.mesh.NeighbourhoodMesh;
import mgui.geometry.util.NodeShape;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.Utility;
import mgui.interfaces.graphs.AbstractGraphEdge;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.DefaultGraph;
import mgui.interfaces.graphs.DefaultGraphEdge;
import mgui.interfaces.graphs.DefaultGraphNode;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.graphs.InterfaceGraphDisplay;
import mgui.interfaces.graphs.layouts.Coordinate3DLayout;
import mgui.interfaces.graphs.layouts.CoordinateLayout;
import mgui.interfaces.logs.LoggingType;
import mgui.util.IDFactory;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import Jama.Matrix;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.renderers.BasicEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.BasicVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/*******************************************
 * Provides utility functions for Graphs.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class GraphFunctions extends Utility {

	public static Factory<AbstractGraphNode> getNodeFactory(){
		return new NodeFactory();
	}
	
	public static <T extends AbstractGraphNode> 
			Factory<AbstractGraphNode> getNodeFactory(Class<T> node_class, String prefix, 
													  boolean add_ids, boolean has_locations){
		return new NodeFactory(node_class, prefix, add_ids, has_locations);
	}
	
	public static Factory<AbstractGraphEdge> getEdgeFactory(){
		return new EdgeFactory();
	}
	
	public static <T extends AbstractGraphEdge> 
			Factory<AbstractGraphEdge> getEdgeFactory(Class<T> edge_class){
		return new EdgeFactory(edge_class);
	}
	
	
	/***************************************
	 * Factory to create graph nodes.
	 * 
	 * @author Andrew Reid
	 *
	 */
	public static class NodeFactory implements Factory<AbstractGraphNode>{

		public boolean add_ids = true;
		public boolean has_locations = true;
		public String prefix = "N";
		Class<?> node_class = DefaultGraphNode.class;
		
		IDFactory ids = new IDFactory();
		
		public NodeFactory(){
			this(DefaultGraphNode.class, "N", true, true);
		}
		
		public <T extends AbstractGraphNode> NodeFactory(Class<T> node_class){
			this(node_class, "N", true, true);
		}
		
		public NodeFactory(String prefix){
			this(DefaultGraphNode.class, prefix, true, true);
		}
		
		public NodeFactory(String prefix, boolean add_ids, boolean has_locations){
			this(DefaultGraphNode.class, prefix, add_ids, has_locations);
		}
		
		public <T extends AbstractGraphNode> NodeFactory(Class<T> node_class, String prefix, boolean add_ids, boolean has_locations){
			this.node_class = node_class;
			this.prefix = prefix;
			this.add_ids = add_ids;
			this.has_locations = has_locations;
		}
		
		@Override
		public AbstractGraphNode create() {
			
			String label = prefix;
			if (label.length() == 0) label = "N";
			
			if (add_ids){
				long id = ids.getID();
				label = prefix + String.valueOf(id);
				}
			
			try{
				AbstractGraphNode node = (AbstractGraphNode)node_class.newInstance();
				node.setLabel(label);
				return node;
			}catch (Exception e){
				InterfaceSession.log("GraphFunctions.NodeFactory: Error instantiating graph " +
						"node from class '" + node_class.getCanonicalName() + "'.", 
						LoggingType.Errors);
				return null;
				}
			
		}
		
	}
	
	/***************************************
	 * Factory to create graph edges.
	 * 
	 * @author Andrew Reid
	 *
	 */
	public static class EdgeFactory implements Factory<AbstractGraphEdge>{

		public boolean add_ids = true;
		public boolean has_locations = true;
		public String prefix = "N";
		Class<?> edge_class = DefaultGraphEdge.class;
		
		IDFactory ids = new IDFactory();
		
		public EdgeFactory(){
			this(DefaultGraphEdge.class);
		}
		
		public <T extends AbstractGraphEdge> EdgeFactory(Class<T> edge_class){
			this.edge_class = edge_class;
		}
		
		@Override
		public AbstractGraphEdge create() {
			
			String label = prefix;
			if (label.length() == 0) label = "N";
			
			if (add_ids){
				long id = ids.getID();
				prefix = prefix + String.valueOf(id);
				}
			
			try{
				AbstractGraphEdge edge = (AbstractGraphEdge)edge_class.newInstance();
				
				return edge;
			}catch (Exception e){
				InterfaceSession.log("GraphFunctions.EdgeFactory: Error instantiating graph " +
						"edge from class '" + edge_class.getCanonicalName() + "'.", 
						LoggingType.Errors);
				return null;
				}
			
		}
		
	}
	
	/********************************************
	 * Returns list of available graph layouts.
	 * 
	 * <p>TODO: load these into a GraphEnvironment.
	 * 
	 * @return a HashMap of available layout types, and their corresponding classes
	 */
	public static HashMap<String, Class<?>> getLayoutTypes(){
		HashMap<String, Class<?>> list = new HashMap<String, Class<?>>();
		list.put("Coordinates", CoordinateLayout.class);
		list.put("Circle Layout", CircleLayout.class);
		list.put("KK Layout", KKLayout.class);
		list.put("FR Layout", FRLayout.class);
		list.put("Spring Layout", SpringLayout.class);
		list.put("ISOM Layout", ISOMLayout.class);
		return list;
	}
	
	/********************************************
	 * Returns list of available graph layouts. Unparameterized, because generics can be
	 * a pain in the ass sometimes...
	 * 
	 * <p>TODO: load these into a GraphEnvironment.
	 * 
	 * @return a HashMap of available layout types, and their corresponding classes
	 */
	public static HashMap<String, Class> getLayoutTypes2(){
		HashMap<String, Class> list = new HashMap<String, Class>();
		list.put("Coordinates", CoordinateLayout.class);
		list.put("Circle Layout", CircleLayout.class);
		list.put("KK Layout", KKLayout.class);
		list.put("FR Layout", FRLayout.class);
		list.put("Spring Layout", SpringLayout.class);
		list.put("ISOM Layout", ISOMLayout.class);
		return list;
	}
	
	/********************************************
	 * Returns list of available 3D graph layouts. Revised because generics can be a bitch.
	 * 
	 * <p>TODO: load these into a GraphEnvironment.
	 * 
	 * @return a HashMap of available layout types, and their corresponding classes
	 */
	public static HashMap<String, Class> getLayout3DTypes2(){
		HashMap<String, Class> list = new HashMap<String, Class>();
		list.put("Coordinates", Coordinate3DLayout.class);
		list.put("Spring Layout", edu.uci.ics.jung.algorithms.layout3d.SpringLayout.class);
		list.put("FR Layout", edu.uci.ics.jung.algorithms.layout3d.FRLayout.class);
		list.put("ISOM Layout", edu.uci.ics.jung.algorithms.layout3d.ISOMLayout.class);
		return list;
	}
	
	/********************************************
	 * Returns list of available 3D graph layouts.
	 * 
	 * <p>TODO: load these into a GraphEnvironment.
	 * 
	 * @return a HashMap of available layout types, and their corresponding classes
	 */
	public static HashMap<String, Class<?>> getLayout3DTypes(){
		HashMap<String, Class<?>> list = new HashMap<String, Class<?>>();
		list.put("Coordinates", Coordinate3DLayout.class);
		list.put("Spring Layout", edu.uci.ics.jung.algorithms.layout3d.SpringLayout.class);
		list.put("FR Layout", edu.uci.ics.jung.algorithms.layout3d.FRLayout.class);
		list.put("ISOM Layout", edu.uci.ics.jung.algorithms.layout3d.ISOMLayout.class);
		return list;
	}
	
	/***********************************************
	 * Returns a {@link Transformer} object which produces a label for a vertex.
	 * 
	 * @return
	 */
	public static Transformer<AbstractGraphNode, String> getVertexLabeller(){
		return vertex_transformer; 
	}
	
	private static Transformer<AbstractGraphNode, String> vertex_transformer = 
		new Transformer<AbstractGraphNode, String>(){
			@Override
			public String transform(AbstractGraphNode node) {
				return node.getLabel();
			}
		};
	
	/***********************************************
	 * Returns a {@link Transformer} object which produces a label for an edge.
	 * 
	 * @return
	 */
	public static Transformer<AbstractGraphEdge, String> getEdgeLabeller(){
		return edge_transformer; 
	}
	
	private static Transformer<AbstractGraphEdge, String> edge_transformer = 
		new Transformer<AbstractGraphEdge, String>(){
			@Override
			public String transform(AbstractGraphEdge edge) {
				return edge.getLabel();
			}
		};
	
	/*****************************************************
	 * Creates a {@link Shape} instance, used to render graph nodes, by parsing a line
	 * of text, with the format:
	 * 
	 * <p>[name] [is-fillable] [qualified-class-name] [space delimited parameter list]
	 * 
	 * <p>Or, to load an path:
	 * 
	 * <p>[name] [is-fillable] java.awt.Path2D$[Float|Double] [SVG-like M (move) and L (line) commands;
	 * 				  e.g., M 1,5 L 10,5 M 5,1 L 5,10 ]
	 * 
	 * @param line
	 * @return the shape, or <code>null</code> if none was created
	 */
	public static NodeShape createNodeShape(String line){
		
		String[] tokens = line.split(" ");
		if (tokens.length < 3) return null;
		
		try{
			Class<?> _class = Class.forName(tokens[2]);
			String type = "Float";
			if (tokens[2].contains("$"))
				type = tokens[2].substring(tokens[2].indexOf("$") + 1);
			else
				tokens[2] = tokens[2].concat("$Float");
			if (tokens[2].startsWith("java.awt.geom.Path2D")){
				// This is a path; parse and instantiate
				int args = tokens.length - 3;
				if (args % 2 != 0){
					throw new IOException("GraphFunctions.createNodeShape: Path arguments must have two elements each!");
					}
				Path2D path;
				if (type.equals("Float"))
					path = new Path2D.Float();
				else
					path = new Path2D.Double();
				
				for (int i = 0; i < args; i+=2){
					String command = tokens[i+3].toLowerCase();
					String[] params = tokens[i+4].split(",");
					if (params.length != 2){
						throw new IOException("GraphFunctions.createNodeShape: Path parameters must be of the form 'x,y'!");
						}
					if (command.equals("m")){
						// Move to
						path.moveTo(Double.valueOf(params[0]), Double.valueOf(params[1]));
					}else if (command.equals("l")){
						path.lineTo(Double.valueOf(params[0]), Double.valueOf(params[1]));
						}
					}
				
				return new NodeShape(tokens[0], path, tokens[1].equals("1"));
				
			}else if (tokens.length > 3){
				//try to construct with parameters
				Object[] arguments = new Object[tokens.length - 3];
				Class<?>[] arg_classes = new Class<?>[tokens.length - 3];
				
				if (type.equals("Float"))
					Arrays.fill(arg_classes, float.class);
				else
					Arrays.fill(arg_classes, double.class);
				for (int i = 3; i < tokens.length; i++){
					if (type.equals("Float"))
						arguments[i - 3] = Float.valueOf(tokens[i]);
					else
						arguments[i - 3] = Double.valueOf(tokens[i]);
					}
				Constructor<Shape> constr = (Constructor<Shape>) _class.getConstructor(arg_classes);
				Shape shape = constr.newInstance(arguments);
				return new NodeShape(tokens[0], shape, tokens[1].equals("1"));
			}else{
				//try to construct with no parameters
				Shape shape = Shape.class.newInstance();
				return new NodeShape(tokens[0], shape, tokens[1].equals("1"));
				}
			
		
		}catch (Exception e){
			//e.printStackTrace();
			InterfaceSession.handleException(e);
			InterfaceSession.log("GraphFunctions: Could not create node shape for class '" +
								 tokens[1] +"' and parameters '" + tokens.toString() + ".", 
								 LoggingType.Errors);
			}
		
		return null;
	}
	
	/***********************
	 * Loads a node shape from an svg file
	 * 
	 * @param filename
	 */
	protected static NodeShape loadNodeShapeFromSvg(String filename){
		
		return null;
	}
	
	/*****************************************************
	 * Determines the current extents of this display's graph and scales/translates to it
	 * 
	 * @param display
	 * @return
	 */
	public static boolean scaleToGraphExtents(InterfaceGraphDisplay display){
		
		VisualizationViewer<AbstractGraphNode, AbstractGraphEdge> viewer = display.getViewer();
		if (viewer == null) return false;
		
		viewer.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.LAYOUT).setToIdentity();
		viewer.getRenderContext().getMultiLayerTransformer().getTransformer(Layer.VIEW).setToIdentity();
	
		
		return true;
	}
	
	static HashMap<String,Position> label_positions;
	
	/*********************************************
	 * Get a list of possible node label positions, as Strings.
	 * 
	 * @return
	 */
	public static ArrayList<String> getLabelPositions(){
		if (label_positions == null){
			label_positions = new HashMap<String,Position>();
			label_positions.put("N", Position.N);
			label_positions.put("S", Position.S);
			label_positions.put("E", Position.E);
			label_positions.put("W", Position.W);
			label_positions.put("NE", Position.NE);
			label_positions.put("NW", Position.NW);
			label_positions.put("SE", Position.SE);
			label_positions.put("SW", Position.SW);
			label_positions.put("CNTR", Position.CNTR);
			label_positions.put("AUTO", Position.AUTO);
			}
		return new ArrayList<String>(label_positions.keySet());
	}
	
	/**********************************************
	 * Returns the label position associated with the String <code>pos</code>.
	 * 
	 * @return
	 */
	public static Position getLabelPosition(String pos){
		if (pos == null) return Position.AUTO;
		return label_positions.get(pos);
	}
	
	/************************************************
	 * Returns a predicate object to control the visibility of edges in a graph.
	 * 
	 * @param initial
	 * @return
	 */
	public static EdgeVisibility getEdgeVisibilityPredicate(boolean initial){
		
		return new EdgeVisibility(initial);
		
	}
	
	public static class EdgeVisibility implements 
		Predicate<Context<Graph<AbstractGraphNode,AbstractGraphEdge>,AbstractGraphEdge>>{
   
		protected boolean show;
    
		public EdgeVisibility(boolean show){
	        this.show = show;
		}
    
		public void show(boolean b){
			this.show = b;
		}
		
		public boolean evaluate(Context<Graph<AbstractGraphNode,AbstractGraphEdge>,AbstractGraphEdge> context){
			return show;
		}
	}
	
	/************************************************
	 * Returns a predicate object to control the visibility of nodes in a graph.
	 * 
	 * @param initial
	 * @return
	 */
	public static NodeVisibility getNodeVisibilityPredicate(boolean initial){
		
		return new NodeVisibility(initial);
		
	}
	
	public static class NodeVisibility implements 
		Predicate<Context<Graph<AbstractGraphNode,AbstractGraphEdge>,AbstractGraphNode>>{
   
		protected boolean show;
    
		public NodeVisibility(boolean show){
	        this.show = show;
		}
    
		public void show(boolean b){
			this.show = b;
		}
		
		public boolean evaluate(Context<Graph<AbstractGraphNode,AbstractGraphEdge>,AbstractGraphNode> context){
			return show;
		}
	}
	
	
	/************************************************
	 * Returns a predicate object to control the visibility of edge arrows in a graph.
	 * 
	 * @param initial
	 * @return
	 */
	public static ArrowVisibility getArrowVisibilityPredicate(boolean initial){
		
		return new ArrowVisibility(initial);
		
	}
	
	public static class ArrowVisibility implements 
		Predicate<Context<Graph<AbstractGraphNode,AbstractGraphEdge>,AbstractGraphEdge>>{
   
		protected boolean show;
    
		public ArrowVisibility(boolean show){
	        this.show = show;
		}
    
		public void show(boolean b){
			this.show = b;
		}
		
		public boolean evaluate(Context<Graph<AbstractGraphNode,AbstractGraphEdge>,AbstractGraphEdge> context){
			return show;
		}
	}
	
	/*******************
	 * Returns a node label renderer which can be turned off.
	 * 
	 * @param position
	 * @return
	 */
	public static GraphNodeLabelRenderer getNodeLabelRenderer(Position position){
		return new GraphNodeLabelRenderer(position);
	}
	
	/******************
	 * Allows labels to be turned off.
	 * 
	 * @author Andrew Reid
	 * @version 1.0
	 * @since 1.0
	 *
	 */
	public static class GraphNodeLabelRenderer extends BasicVertexLabelRenderer<AbstractGraphNode,AbstractGraphEdge>{
		
		protected boolean show = true;
		
		public GraphNodeLabelRenderer() {
			super();
		}

		public GraphNodeLabelRenderer(Position position) {
			super(position);
		}
		
		public void labelVertex(RenderContext<AbstractGraphNode,AbstractGraphEdge> rc, 
								Layout<AbstractGraphNode,AbstractGraphEdge> layout, 
								AbstractGraphNode v, String label) {
			if (!show) return;
			
			super.labelVertex(rc, layout, v, label);
		}
		
		public boolean show() {
			return show;
		}

		public void show(boolean show) {
			this.show = show;
		}
		
	}
	
	
	/*******************
	 * Returns an edge label renderer which can be turned off.
	 * 
	 * @param position
	 * @return
	 */
	public static GraphEdgeLabelRenderer getEdgeLabelRenderer(){
		return new GraphEdgeLabelRenderer();
	}
	
	/******************
	 * Allows labels to be turned off.
	 * 
	 * @author Andrew Reid
	 * @version 1.0
	 * @since 1.0
	 *
	 */
	public static class GraphEdgeLabelRenderer extends BasicEdgeLabelRenderer<AbstractGraphNode,AbstractGraphEdge>{
		
		protected boolean show = true;
		
		public GraphEdgeLabelRenderer() {
			super();
		}

		public void labelEdge(RenderContext<AbstractGraphNode,AbstractGraphEdge> rc, 
								Layout<AbstractGraphNode,AbstractGraphEdge> layout, 
								AbstractGraphEdge e, String label) {
			if (!show) return;
			super.labelEdge(rc, layout, e, label);
		}
		
		public boolean show() {
			return show;
		}

		public void show(boolean show) {
			this.show = show;
		}
		
	}
	
	/********************************************************
	 * Returns an N x N matrix with the shortest paths lengths between each pair of vertices
	 * i and j, in {@code graph}.
	 * 
	 * @param graph
	 * @return
	 */
	public static Matrix getShortestPaths(InterfaceAbstractGraph graph){
		ArrayList<Integer> vertices = new ArrayList<Integer>(graph.getVertexCount());
		for (int i = 0; i < vertices.size(); i++)
			vertices.add(i);
		return getShortestPaths(graph, vertices);
	}
	
	/********************************************************
	 * Returns an N x N matrix with the shortest paths lengths between each pair of vertices
	 * i and j, in {@code vertices}.
	 * 
	 * @param graph
	 * @param vertices 			The list of vertices for which to obtain distances
	 * @return
	 */
	public static Matrix getShortestPaths(InterfaceAbstractGraph graph, ArrayList<Integer> vertices){
		
		DijkstraDistance<AbstractGraphNode, AbstractGraphEdge> d_dist = 
				new DijkstraDistance<AbstractGraphNode, AbstractGraphEdge>(graph, DefaultGraphEdge.getWeightTransformer());
		
		ArrayList<AbstractGraphNode> nodes = graph.getNodes();
		Matrix M = new Matrix(vertices.size(), vertices.size());
		
		for (int i = 0; i < vertices.size(); i++){
			Map<AbstractGraphNode, Number> dist_i = d_dist.getDistanceMap(nodes.get(vertices.get(i)));
			for (int j = 0; j < vertices.size(); j++)
				M.set(i, j, dist_i.get(nodes.get(vertices.get(j))).doubleValue());
			}
		
		return M;
	}
	
	/********************************************************
	 * Converts {@code mesh} to a weighted graph.
	 * 
	 * @param mesh
	 * @return
	 */
	public static InterfaceAbstractGraph getDistanceWeightedGraphForMesh(Mesh3D mesh){
		
		InterfaceAbstractGraph graph = new DefaultGraph();
		
		// Add nodes
		for (int i = 0; i < mesh.n; i++)
			graph.addVertex(new DefaultGraphNode("" + i));
		
		ArrayList<AbstractGraphNode> nodes = graph.getNodes();
		
		// Add edges with neighbourhood
		NeighbourhoodMesh n_mesh = new NeighbourhoodMesh(mesh);
		int i = 0;
		boolean[] processed = new boolean[mesh.n];
		while (i < mesh.n){
			if (!processed[i]){
				AbstractGraphNode v1 = nodes.get(i);
				Point3f p1 = mesh.getVertex(i);
				int[] nbrs = n_mesh.getNeighbourhood(i).getNeighbourList();
				processed[i] = true;
				
				for (int j = 0; j < nbrs.length; j++){
					if (!processed[nbrs[j]]){
						AbstractGraphNode v2 = nodes.get(nbrs[j]);
						Point3f p2 = mesh.getVertex(nbrs[j]);
						DefaultGraphEdge edge = new DefaultGraphEdge(v1, v2, p1.distance(p2));
						graph.addEdge(edge, EdgeType.UNDIRECTED);
						//processed[nbrs[j]] = true;
						}
					}
				// Reset i
				i = 0;
			}else{
				i++;
				}
			}
		
		return graph;
		
	}
	
}