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

import java.awt.Color;
import java.awt.Paint;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.collections15.BidiMap;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.bidimap.DualHashBidiMap;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Font3D;
import org.jogamp.java3d.FontExtrusion;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.LineArray;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.OrientedShape3D;
import org.jogamp.java3d.Text3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.java3d.utils.geometry.Cylinder;
import org.jogamp.java3d.utils.geometry.Primitive;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Color4f;
import org.jogamp.vecmath.Matrix4d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3f;

import edu.uci.ics.jung.algorithms.layout.util.VisRunner;
import edu.uci.ics.jung.algorithms.layout3d.Layout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization3d.EdgeGroup;
import edu.uci.ics.jung.visualization3d.PluggableRenderContext;
import edu.uci.ics.jung.visualization3d.RenderContext;
import edu.uci.ics.jung.visualization3d.VertexGroup;
import edu.uci.ics.jung.visualization3d.layout.LayoutEventBroadcaster;
import mgui.geometry.Graph3D;
import mgui.geometry.Plane3D;
import mgui.geometry.Shape;
import mgui.geometry.Shape3D;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.AttributeSelectionMap;
import mgui.interfaces.graphs.AbstractGraphEdge;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.GraphException;
import mgui.interfaces.graphs.InterfaceAbstractGraph;
import mgui.interfaces.graphs.WeightedGraphEdge;
import mgui.interfaces.graphs.layouts.Coordinate3DLayout;
import mgui.interfaces.graphs.util.GraphFunctions;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.shapes.PointSet3DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.attributes.ShapeAttribute;
import mgui.interfaces.shapes.util.ShapeFunctions;
import mgui.interfaces.shapes.util.ShapeVertexObject;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiNumber;
import mgui.util.Colour;
import mgui.util.Colours;

/*************************************************************
 * Represents a graph as a 3D shape. Uses modified Jung code.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Graph3DInt extends PointSet3DInt {
	
	BranchGroup group_node, graph_branch;
	TransformGroup group_trans;
	Appearance grayLook;
	Layout<AbstractGraphNode, AbstractGraphEdge> layout;
	Transformer<AbstractGraphNode,Node> vertexShapeTransformer;
	
	Shape3DInt me;
	
	protected Appearance vertex_appearance;
	protected Appearance edge_appearance;
	
	protected HashMap<AbstractGraphNode, Integer> node_map = new HashMap<AbstractGraphNode, Integer>();
	
	protected RenderContext<AbstractGraphNode, AbstractGraphEdge> renderContext = 
		new PluggableRenderContext<AbstractGraphNode, AbstractGraphEdge>();
	
	BidiMap<AbstractGraphNode,VertexGroup<AbstractGraphNode>> vertexMap = 
		new DualHashBidiMap<AbstractGraphNode,VertexGroup<AbstractGraphNode>>();
	Map<AbstractGraphNode,Node> vertexNodeMap = 
			new HashMap<AbstractGraphNode,Node>();
	Map<AbstractGraphEdge,EdgeGroup<AbstractGraphEdge>> edgeMap = 
		new HashMap<AbstractGraphEdge,EdgeGroup<AbstractGraphEdge>>();
	Map<AbstractGraphEdge,Node> edgeNodeMap = 
			new HashMap<AbstractGraphEdge,Node>();
	
	public Graph3DInt(){
		this(new Graph3D());
	}
	
	public Graph3DInt(Graph3D graph){
		this(graph, "No-name");
	}
	
	public Graph3DInt(Graph3D graph, String name){
		super();
		setShape(graph);
		init2();
		setName(name);
	}
	
	public Graph3DInt(InterfaceAbstractGraph graph){
		super();
		setShape(new Graph3D(graph));
		init2();
		setName(graph.getName());
	}
	
	private void init2(){
		me = this;
		
		ArrayList<ColourMap> cmaps = InterfaceEnvironment.getColourMaps();
		
		attributes.add(new ShapeAttribute<MguiBoolean>("3D.EdgeAsCylinder", new MguiBoolean(false), true));
		attributes.add(new ShapeAttribute<MguiDouble>("EdgeWeightScale", new MguiDouble(0), true));
		attributes.add(new ShapeAttribute<MguiDouble>("EdgeWeightScaleExp", new MguiDouble(1), true));
		attributes.add(new ShapeAttribute<MguiBoolean>("EdgeWeightColour", new MguiBoolean(false), true));
		attributes.add(new ShapeAttribute<MguiDouble>("EdgeWeightCmapMin", new MguiDouble(0.0), true));
		attributes.add(new ShapeAttribute<MguiDouble>("EdgeWeightCmapMax", new MguiDouble(1.0), true));
		attributes.add(new ShapeAttribute<MguiDouble>("3D.EdgeCylinderMin", new MguiDouble(0.5), true));
		AttributeSelection<ColourMap> sel = new AttributeSelection<ColourMap>("EdgeWeightCmap", cmaps, ColourMap.class, cmaps.get(0));
		attributes.add(sel);
		attributes.add(new ShapeAttribute<MguiFloat>("3D.EdgeScale", new MguiFloat(1f), true));
		
		attributes.add(new ShapeAttribute<MguiBoolean>("ShowEdges", new MguiBoolean(true), true));
		attributes.add(new ShapeAttribute<MguiFloat>("2D.EdgeScale", new MguiFloat(1f), true));
		
		attributes.add(new ShapeAttribute<MguiFloat>("2D.ArrowScale", new MguiFloat(1f), true, false));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.ShowArrows", new MguiBoolean(true), true, false));
		attributes.add(new ShapeAttribute<MguiFloat>("2D.EdgeOffset", new MguiFloat(20f), true, false));
		attributes.add(new ShapeAttribute<MguiBoolean>("2D.ExcludeToSection", new MguiBoolean(true), true,false));
		
		attributes.setValue("3D.ShowVertices",new MguiBoolean(true));
		attributes.setValue("2D.ShowVertices",new MguiBoolean(true));
		
		//Transformers
		
		// ------------ VERTEX TRANSFORMER -----------------
		Transformer<AbstractGraphNode,Node> graph_vertex_transformer = 
			new Transformer<AbstractGraphNode,Node>() {

			public Node transform(AbstractGraphNode node) {
				MguiBoolean show = (MguiBoolean)attributes.getValue("3D.ShowVertices");
				if (!show.getTrue())
					return null;
				Sphere sphere = new Sphere(1f, //getVertexScale(node), 
										   Sphere.GENERATE_NORMALS | 
										   Sphere.ENABLE_GEOMETRY_PICKING |
										   Sphere.ENABLE_APPEARANCE_MODIFY, 
										  	getVertexAppearance(node));
				sphere.getShape().setUserData(new ShapeVertexObject(me, node_map.get(node)));
				sphere.setCapability(Primitive.ENABLE_APPEARANCE_MODIFY);
				return sphere;
			}};
		
		this.renderContext.setVertexShapeTransformer(graph_vertex_transformer);
		
		// ------------ EDGE TRANSFORMER -----------------
		Transformer<Context<Graph<AbstractGraphNode, AbstractGraphEdge>, AbstractGraphEdge>, Node> 
		
		graph_edge_transformer = 
			new Transformer<Context<Graph<AbstractGraphNode,AbstractGraphEdge>, AbstractGraphEdge>, Node>() {

			public Node transform(Context<Graph<AbstractGraphNode,AbstractGraphEdge>, AbstractGraphEdge> ec) {
				MguiBoolean show = (MguiBoolean)attributes.getValue("ShowEdges");
				if (!show.getTrue())
					return null;
				
				AbstractGraphEdge edge = ec.element;
				float scale = getEdgeScale(edge);
				boolean as_cylinder = ((MguiBoolean)attributes.getValue("3D.EdgeAsCylinder")).getTrue();
				Color4f colour = Colours.getColor4f((Color)edge_colour_transformer.transform(edge));
				
				double cyl_min = ((MguiDouble)attributes.getValue("3D.EdgeCylinderMin")).getValue();
				
				if (!as_cylinder || scale < cyl_min){
					LineArray lineArray = new LineArray(2, LineArray.COORDINATES | LineArray.COLOR_4);
					lineArray.setCoordinates(0, new Point3f[]{new Point3f(0,-.5f,0),new Point3f(0,.5f,0)});
					lineArray.setColor(0, colour);
					lineArray.setColor(1, colour);
					lineArray.setCapability(LineArray.ALLOW_COLOR_READ);
					lineArray.setCapability(LineArray.ALLOW_COLOR_WRITE);
					org.jogamp.java3d.Shape3D shape = new org.jogamp.java3d.Shape3D();
					shape.setGeometry(lineArray);
					shape.setAppearance(getEdgeAppearance(edge));
					shape.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_READ);
					shape.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_WRITE);
					return shape;
				}else{
					Cylinder cylinder =  new Cylinder(scale, 1, 
													  Cylinder.GENERATE_NORMALS |
													  Cylinder.ENABLE_GEOMETRY_PICKING,
													  26, 26, getEdgeAppearance(edge));
					cylinder.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_READ);
					cylinder.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_WRITE);
					return cylinder;
					}
			}};
			
		this.renderContext.setEdgeShapeTransformer(graph_edge_transformer);
	
		// ------------ VERTEX LABEL TRANSFORMER -----------------
		Transformer<AbstractGraphNode, String> vertex_stringer = new Transformer<AbstractGraphNode, String>(){
			public String transform(AbstractGraphNode node){
				MguiBoolean show = (MguiBoolean)attributes.getValue("3D.ShowVertexLabels");
				if (!show.getTrue())
					return null;
				
				String column = (String)attributes.getValue("LabelData");
				if (column == null)
					return node.getLabel();
				NameMap nmap = me.getNameMap(column);
				MguiNumber value = me.getDatumAtVertex(column, node_map.get(node));
				if (nmap != null)
					return nmap.get((int)value.getValue());
				return value.toString((String)attributes.getValue("3D.LabelFormat"));
			}
		};
		
		this.renderContext.setVertexStringer(vertex_stringer);
			
		//Set layouts
		HashMap<String, Class> layout_types = GraphFunctions.getLayout3DTypes2();
		AttributeSelectionMap<Class> layouts = 
			new AttributeSelectionMap<Class>("Layout", layout_types, Class.class, "Coordinates");
		
		attributes.add(layouts);
		setLayout();
		
	}
	
	private Transformer<AbstractGraphEdge,Paint> edge_colour_transformer = 
		new Transformer<AbstractGraphEdge,Paint>(){

			@Override
			public Paint transform(AbstractGraphEdge edge) {
				boolean cmapped = ((MguiBoolean)attributes.getValue("EdgeWeightColour")).getTrue();
				if (!cmapped || !(edge instanceof WeightedGraphEdge))
					return (Color)attributes.getValue("3D.LineColour");
				double weight = ((WeightedGraphEdge)edge).getWeight();
				ColourMap cmap = getEdgeWeightColourMap();
				if (cmap == null) 
					return (Color)attributes.getValue("3D.LineColour");
				
				Colour colour = cmap.getColour(weight, 
											  ((MguiDouble)attributes.getValue("EdgeWeightCmapMin")).getValue(),
											  ((MguiDouble)attributes.getValue("EdgeWeightCmapMax")).getValue());
				if (colour == null)
					return (Color)attributes.getValue("3D.LineColour");
				return colour.getColor();
			}
			
		};
	
	public float getLabelOffset(){
		return ((MguiFloat)attributes.getValue("3D.LabelOffset")).getFloat();
	}
	
	public float getEdgeScale(AbstractGraphEdge edge){
		double scale = ((MguiDouble)attributes.getValue("EdgeWeightScale")).getValue();
		
		if (scale <= 0)
			return ((MguiFloat)attributes.getValue("3D.EdgeScale")).getFloat();
		scale = scale * ((MguiFloat)attributes.getValue("3D.EdgeScale")).getValue();
		double weight = ((WeightedGraphEdge)edge).getWeight();
		double exp = ((MguiDouble)attributes.getValue("EdgeWeightScaleExp")).getValue();
		return (float)Math.pow(weight * scale, exp);
		
	}
	
	public ColourMap getEdgeWeightColourMap(){
		ColourMap map = (ColourMap)attributes.getValue("EdgeWeightCmap");
		return map;
	}
	
	@Override
	protected Shape2DInt getShape2D(Plane3D plane, float above_dist, float below_dist, Matrix4d transform){
				
		if (!((MguiBoolean)attributes.getValue("2D.ExcludeToSection")).getTrue()){
			above_dist = Float.POSITIVE_INFINITY;
			below_dist = Float.POSITIVE_INFINITY;
			}
		
		InterfaceAbstractGraph graph = getGraph();
		
		Graph2DInt graph2d = ShapeFunctions.getIntersectedGraph(graph, node_map, plane, above_dist, below_dist, transform);
		
		graph2d.setParentShape(this);
		
		return graph2d;
	}
	
	@Override
	public void addShape2DChild(Shape2DInt shape){
		super.addShape2DChild(shape);
		
		AttributeSelection<String> attr = (AttributeSelection<String>)shape.getAttribute("ScaleData");
		attr.setList(data_columns);
		
	}
	
	@Override
	public boolean isHeritableAttribute(Attribute<?> attribute){
		if (!super.isHeritableAttribute(attribute)) return false;
		String name = attribute.getName();
		if (name.equals("3D.VertexScale") ||
				name.equals("3D.EdgeScale") ||
				name.equals("3D.ShowVertexLabels")){
			return false;
			}
		return true;
	}
	
	@Override
	public Attribute<?> getInheritingAttribute(Attribute<?> attribute){
		
		String name = attribute.getName();
		
		// Exceptions to inheritance
		if (name.equals("3D.VertexScale") ||
				name.equals("3D.EdgeScale") ||
				name.equals("3D.ShowVertexLabels")){
			return null; //attributes.getAttribute(name);
			}
		
		return super.getInheritingAttribute(attribute);
		
	}
	
	public float getVertexScale(AbstractGraphNode node){
		boolean scale_vertices = ((MguiBoolean)attributes.getValue("ScaleVertices")).getTrue();
		float general_scale = ((MguiFloat)attributes.getValue("3D.VertexScale")).getFloat();
		float exp_scale = ((MguiFloat)attributes.getValue("3D.VertexScaleExp")).getFloat();
		
		if (!scale_vertices)
			return general_scale;
		
		String column = (String)attributes.getValue("ScaleData");
		if (column == null) return general_scale;
		
		float value = (float)getDatumAtVertex(column, node_map.get(node)).getValue();
		return (float)Math.pow(general_scale * value, exp_scale);
		
	}
	
	public Color getVertexColour(AbstractGraphNode node){
		if (!this.showData())
			return getVertexColour();
		String column = getCurrentColumn();
		if (column == null) return getVertexColour();
		ColourMap cmap = getColourMap(column);
		if (cmap == null) return getVertexColour();
		float value = (float)getDatumAtVertex(column, node_map.get(node)).getValue();
		
		return cmap.getColour(value).getColor();
	}
	
	public Color getEdgeColour(AbstractGraphEdge edge){
		return (Color)edge_colour_transformer.transform(edge);
	}
	
	protected Appearance getEdgeAppearance(AbstractGraphEdge edge){
		//if (edge_appearance == null)
		Appearance appearance = new Appearance();
		Color4f colour = Colours.getColor4f(getEdgeColour(edge));
		Material m = new Material();
		Color3f c3f = Colours.getColor3f(colour);
		m.setDiffuseColor(c3f);
		if (hasAlpha()){
			String trans_type = (String)attributes.getValue("3D.AlphaMode");
			TransparencyAttributes ta = new TransparencyAttributes();
			float alpha = 1.0f - ((MguiFloat)attributes.getValue("3D.Alpha")).getFloat();
			float alpha2 = colour.getW();
			alpha = 1f - (alpha * alpha2);
			ta.setTransparency(alpha);
			if (trans_type.equals("Screen Door")){
				ta.setTransparencyMode(TransparencyAttributes.SCREEN_DOOR);
			}else if (trans_type.equals("Fastest")){
				ta.setTransparencyMode(TransparencyAttributes.FASTEST);
			}else{
				ta.setTransparencyMode(TransparencyAttributes.NICEST);
				}
			//ta.setTransparencyMode(TransparencyAttributes.BLENDED);
			ta.setSrcBlendFunction(TransparencyAttributes.BLEND_SRC_ALPHA);
			appearance.setTransparencyAttributes(ta);
		}else{
			appearance.setTransparencyAttributes(null);
			}
		appearance.setMaterial(m);
		return appearance;
	}
	
	protected Appearance getVertexAppearance(AbstractGraphNode node){
		//if (vertex_appearance == null)
		Appearance appearance = new Appearance();
		Color3f colour = Colours.getColor3f(getVertexColour(node));
		Material m = new Material();
		m.setDiffuseColor(colour);
		appearance.setMaterial(m);
		return appearance;
	}
	
	protected Appearance getLabelAppearance(AbstractGraphNode node){
		Appearance appearance = new Appearance();
		Color3f colour = Colours.getColor3f((Color)attributes.getValue("3D.LabelColour"));
		Material m = new Material();
		m.setDiffuseColor(colour);
		m.setAmbientColor(colour);
		m.setEmissiveColor(colour);
		m.setSpecularColor(colour);
		appearance.setMaterial(m);
		return appearance;
	}
	
	protected Layout<AbstractGraphNode, AbstractGraphEdge> getLayout(){
		return layout;
	}
	
	protected Class<?> getLayoutClass(){
		return (Class<?>)attributes.getValue("Layout");
	}
	
	public InterfaceAbstractGraph getGraph(){
		if (shape3d == null) return null;
		return ((Graph3D)this.getShape()).getGraph();
	}
	
	public void setGraph(Graph3D graph){
		super.setShape(graph);
		//Set node map to indices
		this.node_map.clear();
		
		ArrayList<AbstractGraphNode> nodes = graph.getJungVertices();
		for (int i = 0; i < nodes.size(); i++)
			node_map.put(nodes.get(i), i);
	}
	
	@Override
	public void setShape(Shape3D shape) {
		if (!(shape instanceof Graph3D)) return;
		setGraph((Graph3D)shape);
	}
	
	protected Layout<AbstractGraphNode, AbstractGraphEdge> getGraphLayoutInstance() throws GraphException{
		
		InterfaceAbstractGraph graph = getGraph();
		Layout<AbstractGraphNode, AbstractGraphEdge> layout;
		
		try{
			Class<?> layoutC = getLayoutClass();
			if (layoutC == null) return null;
			
			Object[] constructorArgs = {graph};
	            Constructor<?> constructor = layoutC
	                    .getConstructor(new Class[] {Graph.class});
	            Object o = constructor.newInstance(constructorArgs);
	            layout = (Layout<AbstractGraphNode, AbstractGraphEdge>) o;
        }catch (Exception e){
            //e.printStackTrace();
        	
            return new Coordinate3DLayout<AbstractGraphNode, AbstractGraphEdge>(graph);
        	}
		return layout;
	}
	
	@Override
	public void setScene3DObject(boolean make_live){
		
		super.setScene3DObject(false);
		if (scene3DObject == null) return;
		if (!this.isVisible() || !this.show3D() || shape3d == null){
			if (make_live) setShapeSceneNode();
			return;
			}
		
		if (group_node != null)
			group_trans.removeAllChildren();
		else{
			group_trans = new TransformGroup();
			group_trans.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			group_trans.setCapability(Group.ALLOW_CHILDREN_WRITE);
			group_node = new BranchGroup();
			group_node.setCapability(BranchGroup.ALLOW_DETACH);
			group_node.addChild(group_trans);
			}
		
		Layout<AbstractGraphNode, AbstractGraphEdge> layout = getLayout();
		if (layout == null){
			return;
			}
		
		if (!ShapeFunctions.nodeHasChild(scene3DObject, group_node))
			scene3DObject.addChild(group_node);
		
		Graph3D graph_shape = (Graph3D)shape3d;
		final InterfaceAbstractGraph graph = graph_shape.getGraph();
		
		graph_branch = new BranchGroup();
		graph_branch.setCapability(BranchGroup.ALLOW_DETACH);
		
		final LayoutEventBroadcaster<AbstractGraphNode, AbstractGraphEdge> elayout =
			new LayoutEventBroadcaster<AbstractGraphNode, AbstractGraphEdge>(layout);
		
		for(AbstractGraphNode v : graph.getVertices()) {
			Node node = renderContext.getVertexShapeTransformer().transform(v);
			VertexGroup<AbstractGraphNode> vg = new VertexGroup<AbstractGraphNode>(v, node);
			if (vg != null){
				vg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
				vg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
				vg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
				vg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
				Transform3D tx = new Transform3D();
				tx.setScale(getVertexScale(v));
				vg.setTransform(tx);
				vertexMap.put(v, vg);
				vertexNodeMap.put(v, node);
				graph_branch.addChild(vg);
				String label = renderContext.getVertexStringer().transform(v);
				if(label != null) {
					float offset = getVertexScale(v);
					offset *= Math.cos(Math.PI / 4.0);
					Font3D f3d = new Font3D(this.getLabelFont(), new FontExtrusion());
					offset += getLabelOffset();
					Text3D txt = new Text3D(f3d, label, new Point3f(offset, offset,0));
					OrientedShape3D textShape = new OrientedShape3D();
					textShape.setGeometry(txt);
					textShape.setAppearance(getLabelAppearance(v));
					textShape.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);
					textShape.setRotationPoint(new Point3f());
					Transform3D tt = new Transform3D();
					tt.setScale(getLabelScale());
					TransformGroup tg = new TransformGroup(tt);
					tg.addChild(textShape);
					BranchGroup bg = new BranchGroup();
					bg.addChild(tg);
					vg.getLabelNode().addChild(bg);
					
					}
				}

		}
		
		for(AbstractGraphEdge edge : graph.getEdges()) {
			Node node = renderContext.getEdgeShapeTransformer().transform(
					Context.<Graph<AbstractGraphNode, AbstractGraphEdge>,AbstractGraphEdge>getInstance(graph, edge));
			node.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_READ);
			node.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_WRITE);
			if (node instanceof Cylinder){
				org.jogamp.java3d.Shape3D shape = ((Cylinder)node).getShape(Cylinder.BODY);
				shape.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_READ);
				shape.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_WRITE);
				shape = ((Cylinder)node).getShape(Cylinder.TOP);
				shape.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_READ);
				shape.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_WRITE);
				shape = ((Cylinder)node).getShape(Cylinder.BOTTOM);
				shape.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_READ);
				shape.setCapability(org.jogamp.java3d.Shape3D.ALLOW_APPEARANCE_WRITE);
				}
			EdgeGroup<AbstractGraphEdge> eg = new EdgeGroup<AbstractGraphEdge>(edge, node);
			eg.setCapability(TransformGroup.ALLOW_CHILDREN_READ);
			eg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
			eg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			eg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
			
			edgeMap.put(edge, eg);
			edgeNodeMap.put(edge, node);
			graph_branch.addChild(eg);
			}

		group_trans.addChild(graph_branch);
		elayout.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				for(AbstractGraphNode v : vertexMap.keySet()) {
					Point3f p = elayout.transform(v);
					Vector3f pv = new Vector3f(p.getX(), p.getY(), p.getZ());
					Transform3D tx = new Transform3D();
					tx.setTranslation(pv);
					tx.setScale(getVertexScale());
					vertexMap.get(v).setTransform(tx);
					//TODO: update bounds from these values
					}

				for(AbstractGraphEdge edge : graph.getEdges()) {
					Pair<AbstractGraphNode> endpoints = graph.getEndpoints(edge);
					AbstractGraphNode start = endpoints.getFirst();
					AbstractGraphNode end = endpoints.getSecond();
					EdgeGroup<AbstractGraphEdge> eg = edgeMap.get(edge);
					eg.setEndpoints(elayout.transform(start), elayout.transform(end));
					}
			}});

		elayout.setSize(new BoundingSphere(new Point3d(), 200));
		elayout.initialize();
		VisRunner runner = new VisRunner((IterativeContext)elayout);
		runner.relax();
		
	}
	
	/************************************
	 * Update the colours of this graph's edges, without regenerating it.
	 * 
	 */
	protected void updateEdgeColours(){
		
		if (scene3DObject == null) return;
		
		Graph3D graph_shape = (Graph3D)shape3d;
		InterfaceAbstractGraph graph = graph_shape.getGraph();
		
		for(AbstractGraphEdge edge : graph.getEdges()) {
			Node node = edgeNodeMap.get(edge);
			Color4f colour = Colours.getColor4f((Color)edge_colour_transformer.transform(edge));
			if (node instanceof org.jogamp.java3d.Shape3D){
				LineArray line_array = (LineArray)((org.jogamp.java3d.Shape3D)node).getGeometry();
				line_array.setColor(0, colour);
				line_array.setColor(1, colour);
			}else if (node instanceof Cylinder){
				Appearance appearance = getEdgeAppearance(edge);
				Cylinder cylinder = (Cylinder)node;
				cylinder.setAppearance(appearance);
				}
			}
	}
	
	/************************************
	 * Update the shapes of this graph's edges, without regenerating it.
	 * 
	 */
	protected void updateEdgeShapes(){
		
	}
	
	/************************************
	 * Update the colours of this graph's vertices, without regenerating it.
	 * 
	 */
	protected void updateVertexColours(){
		
		if (scene3DObject == null) return;
		
		Graph3D graph_shape = (Graph3D)shape3d;
		InterfaceAbstractGraph graph = graph_shape.getGraph();
		
		for(AbstractGraphNode v : graph.getVertices()) {
			Node node = vertexNodeMap.get(v);
			
			if (node instanceof Primitive){
				((Primitive)node).setAppearance(getVertexAppearance(v));
				}
			}
	}
	
	/************************************
	 * Update the shapes of this graph's vertices, without regenerating it.
	 * 
	 */
	protected void updateVertexShapes(){
		
		if (scene3DObject == null || graph_branch == null) return;
		
		Graph3D graph_shape = (Graph3D)shape3d;
		InterfaceAbstractGraph graph = graph_shape.getGraph();
		
		for(AbstractGraphNode v : graph.getVertices()) {
			Node node = vertexNodeMap.get(v);
			
			if (node instanceof Primitive){
				((Primitive)node).setAppearance(getVertexAppearance(v));
				}
			
			// Scale
			VertexGroup<AbstractGraphNode> vg = vertexMap.get(v);
			Transform3D tx = new Transform3D();
			vg.getTransform(tx);
			tx.setScale(getVertexScale(v));
			vg.setTransform(tx);
			
			}
		
	}
	
	
	@Override
	public void updateShape() {
		super.updateShape();
		if (layout != null)
			layout.reset(); // .setSize(new BoundingSphere(new Point3d(boundSphere.center), boundSphere.radius));
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu() {
		
		InterfacePopupMenu menu = super.getPopupMenu();
		
		//TODO: add stuff here
		
		return menu;
		
	}
	
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		
		Attribute<?> attribute = e.getAttribute();
		if (attribute.getName().equals("Layout")){
			setLayout();
			return;
			}
		
		// Most attribute changes don't require a regeneration
		
		// Edge colours
		if (attribute.getName().startsWith("EdgeWeightColour") ||
				attribute.getName().startsWith("EdgeWeightCmap")){
			updateEdgeColours();
			return;
			}
		
		// Vertex colour/scale
		if (attribute.getName().equals("ScaleVertices") ||
				attribute.getName().equals("ScaleData") ||
				attribute.getName().startsWith("3D.Vertex")){
			updateVertexShapes();
			return;
			}
		
		if (attribute.getName().startsWith("3D") &&
				(attribute.getName().startsWith("3D.Vertex") ||
				attribute.getName().contains("Label") ||
				attribute.getName().startsWith("3D.Show") ||
				attribute.getName().startsWith("3D.Line") ||
				attribute.getName().startsWith("3D.Edge") ||
				attribute.getName().startsWith("3D.Scale"))){
			if (!(attribute instanceof ShapeAttribute) ||
					((ShapeAttribute<?>)attribute).needsRedraw3D()){
				setScene3DObject();
				}
			
			}
		
		super.attributeUpdated(e);
	}
	
	protected void setLayout(){
		try{
			this.layout = getGraphLayoutInstance();
			this.setShapeSceneNode();
		}catch (Exception ex){
			ex.printStackTrace();
			InterfaceSession.log("Graph3DInt: could not set 3D layout.");
			}
	}
	
	@Override
	public Shape getGeometryInstance(){
		return new Graph3D();
	}
	
	@Override
	public String toString(){
		return "Graph3D: " + getName();
	}
	
	@Override
	public Icon getObjectIcon() {
		java.net.URL imgURL = Graph3DInt.class.getResource("/mgui/resources/icons/graph_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/volume_3d_20.png");
		return null;
	}
	
	@Override
	public String getLocalName(){
		return "Graph3DInt";
	}
	
	@Override
	public void finalizeAfterXML(){
		
		if (shape3d == null || !InterfaceSession.isInit()) return;
		InterfaceSession.getWorkspace().addGraph(getGraph());
		
	}
}