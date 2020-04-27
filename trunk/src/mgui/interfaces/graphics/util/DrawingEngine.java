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

package mgui.interfaces.graphics.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import org.jogamp.vecmath.Point2d;
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Vector2f;

import mgui.geometry.Circle2D;
import mgui.geometry.LineSegment2D;
import mgui.geometry.PointSet2D;
import mgui.geometry.PointSet3D;
import mgui.geometry.Polygon2D;
import mgui.geometry.Radius2D;
import mgui.geometry.Rect2D;
import mgui.geometry.Text2D;
import mgui.geometry.Vector2D;
import mgui.geometry.util.GeometryFunctions;
import mgui.geometry.util.NodeShape;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.graphics.Graphic2DGrid;
import mgui.interfaces.maps.Map2D;
import mgui.interfaces.shapes.Text2DInt;
import mgui.interfaces.shapes.attributes.ShapeAttribute;
import mgui.interfaces.shapes.util.Point2DShape;
import mgui.interfaces.util.Engine;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;
import mgui.numbers.MguiNumber;
import mgui.numbers.NumberFunctions;
import mgui.util.Colour;
import mgui.util.MathFunctions;

/**********************
 * Utility class to perform drawing of <code>Shape2DInt</code> shapes on a <code>
 * Graphics2D</code>. Has specific functions for specific shapes. New subclasses of 
 * </code>Shape2DInt</code> should perform their drawing in a method contained in 
 * this class, or subclasses of it.
 * 
 * TODO: Implement a renderer framework
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 */

public class DrawingEngine implements Engine {
	
	protected static HashMap<Integer, String> coordTypes;
	public Graphics2D g2d;

	//attributes
	public AttributeList drawing_attributes = new AttributeList();
	
	//booleans
	private boolean hasMap = false;
	
	//constants
	public static final int DRAW_MAP = 0;
	public static final int DRAW_SCREEN = 1;
	
	public static final int DRAW_NORMAL = 0;
	public static final int DRAW_FAST = 1;
	
	//internal parameters
	private int coordSys = DRAW_MAP;
	private int drawMode = DRAW_NORMAL;
	private Map2D theMap;
	
	public DrawingEngine(){
		init();
	}
	
	public DrawingEngine(Map2D thisMap){
		setMap(thisMap);
		init();
	}
	
	@Override
	public String getName(){
		return "Drawing Engine Instance";
	}
	
	@Override
	public void setName(String name){}
	
	public static HashMap<Integer, String> getCoordTypes(){
		if (coordTypes == null){
			coordTypes = new HashMap<Integer, String>();
			coordTypes.put(DRAW_NORMAL, "Model");
			coordTypes.put(DRAW_SCREEN, "Screen");
			}
		return coordTypes;
	}
	
	private void init(){
		
			
		//set defaults
		drawing_attributes.add(new Attribute<BasicStroke>("2D.LineStyle", new BasicStroke(), false));
		drawing_attributes.add(new Attribute<Color>("2D.LineColour", Color.BLACK, false));			
		drawing_attributes.add(new Attribute<MguiBoolean>("IsMapped", new MguiBoolean(false), false));
		drawing_attributes.add(new Attribute<MguiBoolean>("2D.HasFill", new MguiBoolean(false), false));
		drawing_attributes.add(new Attribute<MguiBoolean>("2D.ShowVertices", new MguiBoolean(false), false));
		drawing_attributes.add(new Attribute<MguiInteger>("2D.VertexScale", new MguiInteger(1), true));
		drawing_attributes.add(new Attribute<Color>("2D.VertexColour", Color.BLUE, true));
		drawing_attributes.add(new Attribute<Color>("2D.VertexOutlineColour", Color.BLACK, true));
		drawing_attributes.add(new Attribute<Color>("2D.FillColour", Color.BLUE, true));
		drawing_attributes.add(new Attribute<String>("2D.FontName", "Arial", true));
		drawing_attributes.add(new Attribute<MguiInteger>("2D.FontStyle", new MguiInteger(Font.PLAIN), true));
		drawing_attributes.add(new Attribute<MguiInteger>("AlignHoriz", new MguiInteger(Text2D.ALIGN_LEFT), true));
		drawing_attributes.add(new Attribute<MguiBoolean>("LabelNodes", new MguiBoolean(false)));
		drawing_attributes.add(new ShapeAttribute<Font>("2D.LabelFont", new Font("Arial", Font.PLAIN, 5), true));
		drawing_attributes.add(new ShapeAttribute<MguiFloat>("2D.LabelScale", new MguiFloat(1f), true));
		drawing_attributes.add(new ShapeAttribute<Color>("2D.LabelColour", Color.BLACK, true));
		drawing_attributes.add(new ShapeAttribute<Color>("2D.LabelOutlineColour", Color.WHITE, true));
		drawing_attributes.add(new ShapeAttribute<MguiFloat>("2D.LabelOutlineWidth", new MguiFloat(0), true));
		drawing_attributes.add(new ShapeAttribute<MguiFloat>("2D.LabelBackgroundAlpha", new MguiFloat(1f), true));
		drawing_attributes.add(new ShapeAttribute<Color>("2D.LabelBackgroundColour", Color.LIGHT_GRAY, true));
		drawing_attributes.add(new ShapeAttribute<String>("2D.LabelFormat", "#0.0", true));
		drawing_attributes.add(new Attribute<MguiFloat>("2D.LabelOffset", new MguiFloat(1.0f)));
		drawing_attributes.add(new Attribute<String>("2D.LabelPosition", "E"));
		drawing_attributes.add(new ShapeAttribute<MguiBoolean>("2D.ShowVertexLabels", new MguiBoolean(false), true));
		drawing_attributes.add(new Attribute<Text2DInt>("LabelObj", new Text2DInt("N", 15, 10)));
		drawing_attributes.add(new Attribute<MguiBoolean>("2D.HasAlpha", new MguiBoolean(false)));
		drawing_attributes.add(new Attribute<MguiFloat>("2D.Alpha", new MguiFloat(1.0f)));
		drawing_attributes.add(new Attribute<MguiBoolean>("InheritFromParent", new MguiBoolean(false)));
		}
	
	public void reset(Graphics2D g){
		g.setStroke(new BasicStroke());
		g.setPaint(Color.BLACK);
	}
	
	@Override
	public AttributeList getAttributes(String operation, String method){
		return drawing_attributes;
	}
	
	@Override
	public ArrayList<String> getOperations(){
		return null;
	}
	
	@Override
	public ArrayList<String> getMethods(String operation){
		
		return null;
	}
	
	@Override
	public boolean callMethod(String operation, String method, ProgressUpdater progress){
		return false;
	}
	
	@Override
	public boolean callMethod(String operation, String method, ArrayList<?> params, ProgressUpdater progress){
		return false;
	}
	
	public void setAttributes(AttributeList attr){
		drawing_attributes.setUnion(attr);
	}
	
	public void setDrawMode(int mode){
		drawMode = mode;
	}
	
	public void setCoordSys(int newSys){
		if (newSys > 1 || newSys < 0) return;
		coordSys = newSys;
	}
	
	public void setGraphics2D(Graphics2D g){
		g2d = g;
	}
	
	public void setMap(Map2D thisMap){
		theMap = thisMap;
		if (theMap != null)
			hasMap = true;
	}
	
	/**********************
	 * Returns the current {@linkplain Map2D} for this drawing engine.
	 * 
	 * @return
	 */
	public Map2D getMap(){
		return theMap;
	}
	
	public boolean hasMap(){
		return hasMap;
	}
	
	/******************************
	 * Returns the screen coordinate of {@code point}, a 2D location in model space.
	 * 
	 * @param thisPoint
	 * @return
	 */
	public Point getScreenPoint(Point2d thisPoint){
		if (coordSys == DRAW_MAP && hasMap)
			return theMap.getScreenPoint(new Point2f(thisPoint));
		return new Point((int)thisPoint.x, (int)thisPoint.y);
	}
	
	/******************************
	 * Returns the screen coordinate of {@code point}, a 2D location in model space.
	 * 
	 * @param thisPoint
	 * @return
	 */
	public Point getScreenPoint(Point2f thisPoint){
		return getScreenPoint(new Point2d(thisPoint));
	}
	
	/******************************
	 * Returns the screen bounds of {@code bounds}, a 2D rectangular boundary in model space.
	 * 
	 * @param thisPoint
	 * @return
	 */
	public Rectangle getScreenBounds(Rect2D bounds){
		if (coordSys == DRAW_MAP && hasMap)
			return theMap.getScreenBounds(bounds);
		return new Rectangle((int)Math.min(bounds.corner1.x, bounds.corner2.x),
							 (int)Math.max(bounds.corner1.y, bounds.corner2.y),
							 (int)(Math.max(bounds.corner1.x, bounds.corner2.x) - Math.min(bounds.corner1.x, bounds.corner2.x)), 
							 (int)(Math.max(bounds.corner1.y, bounds.corner2.y) - Math.min(bounds.corner1.y, bounds.corner2.y)));
	}
	
	public int getScreenDist(double coordDist){
		if (coordSys == DRAW_MAP && hasMap)
			return theMap.getScreenDist(coordDist);
		return (int)coordDist;
	}
	
	public float getMapDist(int screen_dist){
		if (coordSys == DRAW_MAP && hasMap)
			return theMap.getMapDist(screen_dist);
		return screen_dist;
		
	}
	
	private int getRectCorner(int corner){
		if (this.coordSys != DrawingEngine.DRAW_SCREEN)
			return corner;
		switch (corner){
			case Rect2D.CNR_BL:
				return Rect2D.CNR_TL;
			case Rect2D.CNR_TL:
				return Rect2D.CNR_BL;
			case Rect2D.CNR_BR:
				return Rect2D.CNR_TR;
			case Rect2D.CNR_TR:
				return Rect2D.CNR_BR;
		}
		return -1;
	}
	
	/*********************************************************
	 * Draw a {@code Graphic2DGrid} object.
	 * 
	 * @param g
	 * @param grid
	 * @param isMajor
	 * 
	 */
	public void drawGrid2D(Graphics2D g, Graphic2DGrid grid, boolean isMajor){
		if (drawMode == DRAW_FAST && !isMajor) return;
		g.setColor(grid.getColour());
		g.setStroke(grid.getStyle());
		
		Rect2D bounds = theMap.getMapBounds();
		double x_min = bounds.getCorner(Rect2D.CNR_BL).x;
		double y_min = bounds.getCorner(Rect2D.CNR_BL).y;
		double x_max = bounds.getCorner(Rect2D.CNR_BR).x;
		double y_max = bounds.getCorner(Rect2D.CNR_TR).y;
		
		//max space
		double min_space = grid.getMin() * theMap.getZoom() / 100;
		double max_space = grid.getMax() * theMap.getZoom() / 100;
		
		//determine spacing
		double space = grid.getSpacing();
		while (max_space < space)
			space /= 2;
		while (min_space > space)
			space *= 2;
		
		double x_pos = MathFunctions.ceil(x_min, space);
		double y_pos = MathFunctions.ceil(y_min, space);
		
		//InterfaceSession.log("Space: " + space + "; Start x: " + x_pos);
		
		//draw x lines
		for (; x_pos < x_max; x_pos += space)
			drawLine2D(g, new mgui.geometry.LineSegment2D(new Point2f((float)x_pos, (float)y_min), new Point2f((float)x_pos, (float)y_max)), false);
		
		//draw y lines
		for (; y_pos < y_max; y_pos += space)
			drawLine2D(g, new mgui.geometry.LineSegment2D(new Point2f((float)x_min, (float)y_pos), new Point2f((float)x_max, (float)y_pos)), false);
		
		//draw labels
		if (grid.getShowLabels()){
			x_pos = MathFunctions.ceil(x_min, space);
			y_pos = MathFunctions.ceil(y_min, space);
			//int size = grid.getLabelSize();
			//Font font = new Font("Courier New", Font.PLAIN, size);
			Font font = grid.getLabelFont(); // (Font)drawingAttr.getValue("LabelFont");
			Color c = grid.getLabelColour();
			g.setColor(c);
			g.setFont(font);
			FontRenderContext frc = g.getFontRenderContext();
			int margin = 2;
			int offset = 1;
			String format = NumberFunctions.getReasonableFormat(space);
			
			AffineTransform transform_old = g.getTransform();
			
			for (; x_pos < x_max; x_pos += space){
				Point pt = getScreenPoint(new Point2f((float)x_pos, (float)y_min ));
				pt.y -= margin;
				pt.x -= offset;
				TextLayout tl = new TextLayout(MguiDouble.getString(x_pos, format), font, frc);
				AffineTransform transform = new AffineTransform();
				transform.rotate(-Math.PI / 2.0, pt.x, pt.y);
				//g.setTransform(transform);
				g.setTransform(transform_old);
				g.transform(transform);
				tl.draw(g, pt.x, pt.y);
				}
			
			g.setTransform(transform_old);
			
			for (; y_pos < y_max; y_pos += space){
				Point pt = getScreenPoint(new Point2f((float)x_min, (float)y_pos ));
				pt.x += margin;
				pt.y -= offset;
				TextLayout tl = new TextLayout(MguiDouble.getString(y_pos, format), font, frc);
				tl.draw(g, pt.x, pt.y);
				}
			}
		
	}
	
	/*
	public void DrawPolygon2D(Graphics2D g, Polygon2D thisPoly){
		DrawPolygon2D(g, thisPoly, true);
	}
	*/
	
	/*****************************
	 * Draws a {@linkplain Polygon2D} object.
	 * 
	 * @param g
	 * @param thisPoly
	 */
	public void drawPolygon2D(Graphics2D g, Polygon2D thisPoly){
		if (thisPoly.getSize() == 0) return;
		
		drawPolygon2DLine(g, thisPoly);
		if (((MguiBoolean)drawing_attributes.getValue("2D.ShowVertices")).getTrue())
			drawPolygon2DNodes(g, thisPoly);
		if (((MguiBoolean)drawing_attributes.getValue("2D.ShowVertexLabels")).getTrue()){
			Text2DInt thisText = (Text2DInt)drawing_attributes.getValue("LabelObj");
			g.setFont((Font)thisText.getAttributes().getValue("2D.LabelFont"));
			g.setPaint((Color)thisText.getAttributes().getValue("2D.LabelColour"));
			String preStr = thisText.getText();
			drawPolygon2DLabels(g, thisPoly, preStr);
			}
	}
	
	public void drawPolygon2DLine(Graphics2D g, Polygon2D thisPoly){
	
		//for each node, add to GeneralPath
		//then close path
		GeneralPath drawPoly = new GeneralPath(Path2D.WIND_EVEN_ODD, thisPoly.vertices.size());
		Point thisPt;
		
		thisPt = getScreenPoint(thisPoly.vertices.get(0));
		drawPoly.moveTo(thisPt.x, thisPt.y);
		
		for (int i = 1; i < thisPoly.vertices.size(); i++){
			// Only draw visible segments
			thisPt = getScreenPoint(thisPoly.vertices.get(i));
			if (thisPoly.segments.get(i-1)){
				drawPoly.lineTo(thisPt.x, thisPt.y);
			}else{
				drawPoly.moveTo(thisPt.x, thisPt.y);
				}
			}
		
		if (((MguiBoolean)(drawing_attributes.getValue("2D.HasFill"))).getTrue() && drawMode != DRAW_FAST){
			
			Composite c = g.getComposite();
			
			if (((MguiBoolean)drawing_attributes.getValue("2D.HasAlpha")).getTrue())
				g.setComposite(makeAlphaComposite(((MguiFloat)drawing_attributes.getValue("2D.Alpha")).getFloat()));
			g.setPaint((Color)drawing_attributes.getValue("2D.FillColour"));
			g.fill(drawPoly);
			g.setComposite(c);
			}
		g.setPaint((Color)drawing_attributes.getValue("2D.LineColour"));
		g.setStroke((Stroke)drawing_attributes.getValue("2D.LineStyle"));
		g.draw(drawPoly);
	}
	
	/*******************************
	 * Draw a mesh intersection with the current line colour
	 * @param g
	 * @param edges
	 */
	public void drawMesh2D(Graphics2D g, ArrayList<Point2f[]> edges, float alpha){
		g.setPaint((Color)drawing_attributes.getValue("2D.LineColour"));
		BasicStroke bs = (BasicStroke)drawing_attributes.getValue("2D.LineStyle");
		g.setStroke(bs);
		//g.setStroke(new BasicStroke(5.0f));
		Point[] pts = new Point[2];
		boolean has_alpha = (alpha >= 0f && alpha <= 1f);
		
		Composite originalComposite = g.getComposite();
		
		if (has_alpha){
			AlphaComposite comp = makeAlphaComposite(alpha);
			g.setComposite(comp);
			}
			
		for (int i = 0; i < edges.size(); i++){
			pts[0] = getScreenPoint(edges.get(i)[0]);
			pts[1] = getScreenPoint(edges.get(i)[1]);
			g.draw(new java.awt.geom.Line2D.Float(pts[0].x, pts[0].y, pts[1].x, pts[1].y));
			}
		
		if (has_alpha)
			g.setComposite(originalComposite);

	}
	
	/*******************************
	 * Draw a mesh intersection, by painting the line according to colours, where colours
	 * is an n * 2 array specifying the colours for each edge's endpoints
	 * @param g
	 * @param edges
	 * @param colours
	 */
	public void drawMesh2D(Graphics2D g, ArrayList<Point2f[]> edges, ArrayList<Colour> colours, float alpha){
		Paint p;
		//g.setPaint((Color)drawingAttr.getValue("LineColour"));
		BasicStroke bs = (BasicStroke)drawing_attributes.getValue("2D.LineStyle");
		g.setStroke(bs);
		Point[] pts = new Point[2];
		
		boolean has_alpha = (alpha >= 0f && alpha <= 1f);
		
		Composite originalComposite = g.getComposite();
		
		if (has_alpha){
			AlphaComposite comp = makeAlphaComposite(alpha);
			g.setComposite(comp);
			}
		
		for (int i = 0; i < edges.size(); i++){
			pts[0] = getScreenPoint(edges.get(i)[0]);
			pts[1] = getScreenPoint(edges.get(i)[1]);
			p = new GradientPaint(pts[0].x, pts[0].y, colours.get(i * 2).getColor(),
								  pts[1].x, pts[1].y, colours.get((i * 2) + 1).getColor());
			g.setPaint(p);
			g.draw(new java.awt.geom.Line2D.Float(pts[0].x, pts[0].y, pts[1].x, pts[1].y));
			}
		
		if (has_alpha)
			g.setComposite(originalComposite);

	}
	
	public void drawPolygon2DNodes(Graphics2D g, Polygon2D thisPoly){
		
		g.setPaint((Color)drawing_attributes.getValue("2D.VertexColour"));
		float nodeSize = ((MguiFloat)drawing_attributes.getValue("2D.VertexScale")).getFloat();
		
		Point thisPt;
		for (int i = 0; i < thisPoly.vertices.size(); i++){
			if (thisPoly.render_vertex.get(i)){
				thisPt = getScreenPoint(thisPoly.vertices.get(i));
				g.fill(new Rectangle2D.Float((thisPt.x - (nodeSize / 2)), (thisPt.y - (nodeSize / 2)),
											 nodeSize, nodeSize));
				}
		}
		
	}
	
	/*************************
	 * Draws a {@linkplain PointSet3D} with the specified parameters
	 * 
	 * @param g
	 * @param point_set
	 * @param size
	 * @param shape
	 * @param colours
	 * @param alpha
	 */
	public void drawPointSet2D(Graphics2D g, PointSet2D point_set, float size, 
							   NodeShape shape, ArrayList<Colour> colours, float alpha){
		drawPointSet2D(g, point_set, size, shape, colours, alpha, null);
	}
	
	/*************************
	 * Draws a {@linkplain PointSet3D} with the specified parameters
	 * 
	 * @param g
	 * @param point_set
	 * @param size
	 * @param shape
	 * @param colours
	 * @param alpha
	 */
	public void drawPointSet2D(Graphics2D g, PointSet2D point_set, float size, 
							   NodeShape shape, ArrayList<Colour> colours, float alpha,
							   ArrayList<String> labels){
		
		boolean has_alpha = (alpha >= 0f && alpha <= 1f);
		
		Composite originalComposite = g.getComposite();
		
		if (has_alpha){
			AlphaComposite comp = makeAlphaComposite(1f-alpha);
			g.setComposite(comp);
			}
		
		AffineTransform transform_old = g.getTransform();
		AffineTransform transform;
		
		Shape g_shape = shape.getShape();
		Rectangle2D bounds = g_shape.getBounds2D();
		// Size is the maximal dimension in x or y
		double max_dim = Math.max(bounds.getWidth(), bounds.getHeight());
		double scale = size / max_dim;
		
		
		for (int i = 0; i < point_set.n; i++){
			Point point = getScreenPoint(point_set.getVertex(i));
			if (colours != null)
				g.setPaint(colours.get(i).getColor());
			
			transform = new AffineTransform();
			transform.translate(point.getX(), point.getY());
			transform.scale(scale, scale);
			g.setTransform(transform);
			g.setPaint((Color)drawing_attributes.getValue("2D.VertexColour"));
			g.fill(g_shape);
			g.setPaint((Color)drawing_attributes.getValue("2D.VertexOutlineColour"));
			g.draw(g_shape);
			}
		
		
		if (labels != null){
			bounds.setRect(0,0, bounds.getWidth()*scale, bounds.getHeight()*scale);
			drawLabels2D(g, bounds, point_set.getVertices(), labels, alpha);
			}
		
		g.setTransform(transform_old);
		
		if (has_alpha)
			g.setComposite(originalComposite);
		
	}
	
	/*********************************
	 * Draws labels at the specified vertices, given the current attributes.
	 * 
	 * @param g The Graphics context
	 * @param node_shape The shape for vertex rendering
	 * @param size The size of the vertex shape
	 * @param vertices List of vertices to 
	 * @param labels
	 */
	public void drawLabels2D(Graphics2D g, Rectangle2D vertex_bounds, ArrayList<Point2f> vertices, ArrayList<String> labels){
		drawLabels2D(g, vertex_bounds, vertices, labels, 1);
	}
	
	/*********************************
	 * Draws labels at the specified vertices, given the current attributes.
	 * 
	 * @param g The Graphics context
	 * @param node_shape The shape for vertex rendering
	 * @param size The size of the vertex shape
	 * @param vertices List of vertices to 
	 * @param labels
	 */
	public void drawLabels2D(Graphics2D g, Rectangle2D vertex_bounds, ArrayList<Point2f> vertices, ArrayList<String> labels, float alpha){
		
		Font label_font = (Font)drawing_attributes.getValue("2D.LabelFont");
		String label_pos = (String)drawing_attributes.getValue("2D.LabelPosition");
		float label_offset = ((MguiFloat)drawing_attributes.getValue("2D.LabelOffset")).getFloat();
		g.setFont(label_font);
		FontMetrics font_metrics = g.getFontMetrics(label_font);
		Color font_colour = (Color)drawing_attributes.getValue("2D.LabelColour");
		float outline_width = ((MguiFloat)drawing_attributes.getValue("2D.LabelOutlineWidth")).getFloat();
		float background_alpha = ((MguiFloat)drawing_attributes.getValue("2D.LabelBackgroundAlpha")).getFloat();
		background_alpha *= alpha;
		Color background_colour = (Color)drawing_attributes.getValue("2D.LabelBackgroundColour");
		Color outline_colour = (Color)drawing_attributes.getValue("2D.LabelOutlineColour");
		FontRenderContext frc = g.getFontRenderContext();
			
		g.setFont(label_font);
		g.setColor(font_colour);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		        		   RenderingHints.VALUE_ANTIALIAS_ON);
		
		
		for (int i = 0; i < vertices.size(); i++){
			AffineTransform transform = new AffineTransform();
			Point point = getScreenPoint(vertices.get(i));
			
			String label = labels.get(i);
			Rectangle2D text_bounds = font_metrics.getStringBounds(label, g);
			LineMetrics line_metrics = label_font.getLineMetrics(label, frc);
			
			double trans_x = point.getX()-text_bounds.getWidth()/2.0; 
			double trans_y = point.getY()+line_metrics.getAscent()/2.0;
			if (label_pos.contains("E")){
				trans_x = point.getX()+vertex_bounds.getWidth()/2f+label_offset;
				}
			if (label_pos.contains("W")){
				trans_x = point.getX()-vertex_bounds.getWidth()/2f-label_offset-text_bounds.getWidth();
				}
			if (label_pos.contains("N") && !label_pos.equals("CNTR")){
				trans_y = point.getY()-vertex_bounds.getHeight()/2f-label_offset; //-text_bounds.getHeight()/2.0;
				}
			if (label_pos.contains("S")){
				trans_y = point.getY()+vertex_bounds.getHeight()/2f+label_offset+line_metrics.getAscent();
				}
			if (label_pos.equals("CNTR")){
				//trans_y = point.getY()+scale*bounds.getHeight()/2f+label_offset+line_metrics.getAscent();
				}
			
			transform.translate(trans_x, trans_y);
			
			g.setTransform(transform);
			
			// Background?
			if (background_alpha > 0){
				g.setColor(background_colour);
				Composite originalComposite = g.getComposite();
				
				if (background_alpha < 1){
					AlphaComposite comp = makeAlphaComposite(1f-background_alpha);
					g.setComposite(comp);
					}
				
				g.fill(text_bounds);
				
				if (background_alpha < 1){
					g.setComposite(originalComposite);
					}
				}
			
			// Get glyphs and render accordingly
			GlyphVector gv = label_font.createGlyphVector(frc, label);
	        int length = gv.getNumGlyphs();
	        
	        for (int j = 0; j < length; j++){
				Shape glyph = gv.getGlyphOutline(j); 
				
				g.setColor(font_colour);
				g.fill(glyph);
				if (outline_width > 0){
					g.setColor(outline_colour);
					g.setStroke(new BasicStroke(outline_width));
					g.draw(glyph);
					}
				
		        }
			
			//g.drawString(label, 0, 0);
			}
		
		
	}
	
	/*********************************
	 * Draws a {@linkplain Point2D} object.
	 * 
	 * @param g
	 * @param pt
	 * @param size
	 */
	public void drawPoint2D(Graphics2D g, mgui.geometry.Point2D pt, float size){
		Point thisPt = getScreenPoint(pt.point);
		
		Ellipse2D.Double drawCircle = new Ellipse2D.Double((double)thisPt.x - size,
														   (double)thisPt.y - size,
														   size * 2,
														   size * 2);
		
		
		g.setPaint((Color)drawing_attributes.getValue("2D.FillColour"));
		g.fill(drawCircle);
		
	}
	
	public void drawVector2D(Graphics2D g, Vector2D v, 
							 Point2DShape start_point, 
							 Point2DShape arrow,
							 float alpha){
		
		g.setPaint((Color)drawing_attributes.getValue("2D.LineColour"));
		
		boolean has_alpha = (alpha >= 0f && alpha <= 1f);
		Composite originalComposite = g.getComposite();
		
		Point2f start_pt = new Point2f(v.getStart());
		Point2f arrow_pt = new Point2f(v.getStart());
		Vector2f vect = new Vector2f(v.getVector());
		arrow_pt.add(vect);
		
		//draw line
		mgui.geometry.LineSegment2D line = new mgui.geometry.LineSegment2D(start_pt, arrow_pt);
		drawLine2D(g, line);
		
		//draw arrow
		//if (!Float.isNaN(arrow_scale)){
		if (arrow != null){
			//account for size of arrow (we want the tip to be the end)
			vect.normalize();
			vect.scale(getMapDist((int)(arrow.scale / 2f)));
			arrow_pt.sub(vect);
			
			if (has_alpha){
				AlphaComposite comp = makeAlphaComposite(alpha);
				g.setComposite(comp);
				}
			
			//rotate to match vector angle (add PI because of graphics y flip?????)
			float angle = (-GeometryFunctions.getVectorAngle(v.getVector()));
			if (this.coordSys == DRAW_SCREEN) angle = -angle;
			Point point = getScreenPoint(arrow_pt);
			//float screen_scale = theMap.getScreenDist(arrow_scale);
			arrow.draw(g, point, 1, angle);
			//arrow.draw(g, point, 1, v.getVector());
			}
		
		g.setPaint((Color)drawing_attributes.getValue("2D.VertexColour"));
		//draw start point
		//if (!Float.isNaN(start_scale)){
		if (start_point != null){
			Point point = getScreenPoint(start_pt);
			start_point.draw(g, point, 1, 0);
			}
		
		if (has_alpha)
			g.setComposite(originalComposite);
		
	}
	
	/***********************************************************
	 * Draws data as a line plot beside <code>segment</code>. Data will be plotted between <code>min_value</code> and
	 * <code>max_value</code>.
	 * 
	 * TODO: label axes
	 * 
	 * @param g
	 * @param segment
	 * @param plotted_data The data to plot
	 * @param min_value Value at bottom of plot
	 * @param max_value Value at top of plot
	 * @param offset The offset from the segment to the plot
	 * @param height The height of the plot
	 */
	public void drawSegmentData2D(Graphics2D g, LineSegment2D segment, ArrayList<MguiNumber> plotted_data, 
								  float min_value, float max_value, float offset, float height){
		
		
		
		//get transform from x-axis
		Vector2f v_s = new Vector2f(segment.pt2);
		v_s.sub(segment.pt1);
		Vector2f v_o = GeometryFunctions.getRotatedVector2D(v_s, (float)(Math.PI / 2.0));
		v_o.normalize();
		Vector2f v_x = new Vector2f(v_s);	//vector for determining plot x locations
		v_x.normalize();
		v_x.scale(segment.getLength() / plotted_data.size());
		Vector2f v_y = new Vector2f(v_o);	//vector for determining plot y locations
		v_y.normalize();
		v_y.scale(height / (max_value - min_value));
		v_o.scale(offset);					//vector for determining origin offset
		
		//draw axes
		g.setPaint(Color.RED);
		
		Point2f plot_pt = new Point2f(segment.pt1);
		plot_pt.add(v_o);
		Point2f x_pt = new Point2f(plot_pt);
		
		Point screen_pt = getMap().getScreenPoint(plot_pt);
		Point2f test_pt = new Point2f(plot_pt);
		Vector2f axis = new Vector2f(v_y);
		axis.scale(max_value - min_value);
		test_pt.add(axis);
		Point p2 = getMap().getScreenPoint(test_pt);
		g.drawLine(screen_pt.x, screen_pt.y, p2.x, p2.y);
		axis = new Vector2f(v_x);
		axis.scale(plotted_data.size());
		test_pt.set(plot_pt);
		test_pt.add(axis);
		p2 = getMap().getScreenPoint(test_pt);
		g.drawLine(screen_pt.x, screen_pt.y, p2.x, p2.y);
		
		//draw data plot
		g.setPaint((Color)drawing_attributes.getValue("2D.LineColour"));
		Vector2f plot_vector = new Vector2f();
		GeneralPath plot_line = new GeneralPath(Path2D.WIND_EVEN_ODD, plotted_data.size());
		
		//plot_line.moveTo(screen_pt.x, screen_pt.y);
		
		//start plotting
		for (int i = 0; i < plotted_data.size(); i++){
			plot_pt.set(x_pt);
			float value = (float)plotted_data.get(i).getValue();
			plot_vector.set(v_y);
			plot_vector.scale(value - min_value);
			plot_pt.add(plot_vector);
			screen_pt = getMap().getScreenPoint(plot_pt);
			if (i == 0)
				plot_line.moveTo(screen_pt.x, screen_pt.y);
			else
				plot_line.lineTo(screen_pt.x, screen_pt.y);
			x_pt.add(v_x);
		}
		
		g.draw(plot_line);
		
	}
	
	/*****************************************************
	 * Draws labels on the vertices of a polygon object, offset to the right (East).
	 * 
	 * TODO: specify the label position
	 * 
	 * @param g
	 * @param thisPoly
	 * @param preStr
	 */
	public void drawPolygon2DLabels(Graphics2D g, Polygon2D thisPoly, String preStr){
		ArrayList<String> labelStr = (ArrayList<String>)drawing_attributes.getValue("LabelStrings");
		boolean useStrings = (labelStr.size() > 0);
		int offsetX = getScreenDist(((MguiDouble)drawing_attributes.getValue("2D.LabelOffset")).getValue());
		int offsetY = 0; // getScreenDist(((MguiDouble)drawingAttr.getValue("2D.LabelOffset")).getValue());
		
		Point thisPt;
		for (int i = 0; i < thisPoly.vertices.size(); i++){
			thisPt = getScreenPoint(thisPoly.vertices.get(i));
			thisPt.x += offsetX;
			thisPt.y -= offsetY;
			if (useStrings)
				g.drawString(preStr + labelStr.get(i), thisPt.x, thisPt.y);
			else
				g.drawString(preStr + String.valueOf(i), thisPt.x, thisPt.y);
		}
		
	}
	
	/*********************************
	 * Draws a {@code Text2D} object.
	 * 
	 * @param g
	 * @param text
	 */
	public void drawText2D(Graphics2D g, Text2D text){
		
		//Rectangle prevClip = g.getClipBounds();
		//set font size to fill bound box (this may depend on theMap)
		
		Color color = (Color)drawing_attributes.getValue("2D.LabelColour");
		g.setPaint(color);
		Font font = (Font)drawing_attributes.getValue("2D.LabelFont");
		Point corner = getScreenPoint(text.getBounds().getCorner(getRectCorner(Rect2D.CNR_BL)));
		
		//rotate
		FontRenderContext frc = g.getFontRenderContext();
			
		AffineTransform transform_old = g.getTransform();
		Vector2f v = text.getBounds().getRotation();
		//flip y to get screen coords
		v.y = -v.y;
		
		float angle = GeometryFunctions.getVectorAngle(v);
		Point pivot = new Point(corner);
		TextLayout tl = new TextLayout(text.getText(), font, frc);
		
		AffineTransform transform = new AffineTransform();
		if (angle > 0.5 * Math.PI && angle < 1.5 * Math.PI){
			//transform.translate(-tl.getAdvance(), 0);
			corner.x -= tl.getAdvance();
			angle = (float)GeometryFunctions.getMinimalAngle(angle + Math.PI);
			}
		transform.rotate(angle, pivot.x, pivot.y);
		g.transform(transform);
		tl.draw(g, corner.x, corner.y);
			
		g.setTransform(transform_old);
		
	}
	
	/*****************************
	 * Draws a {@code Circle2D} object
	 * 
	 * @param g
	 * @param thisCircle
	 */
	public void drawCircle2D(Graphics2D g, Circle2D thisCircle){
		//fill?
		Point screenPt = getScreenPoint(thisCircle.centerPt);
		double screenDist = getScreenDist(thisCircle.radius);
		
		Ellipse2D.Double drawCircle = new Ellipse2D.Double(screenPt.x - screenDist,
														   screenPt.y - screenDist,
														   screenDist * 2,
														   screenDist * 2);
		
		if (((MguiBoolean)(drawing_attributes.getValue("2D.HasFill"))).getTrue() && drawMode != DRAW_FAST){
			//g.setPaint(fillColour);
			g.setPaint((Color)drawing_attributes.getValue("2D.FillColour"));
			g.fill(drawCircle);
		}
		
		g.setPaint((Color)drawing_attributes.getValue("2D.LineColour"));
		g.setStroke((Stroke)drawing_attributes.getValue("2D.LineStyle"));
		g.draw(drawCircle);
		
	}
	
	/***************************
	 * Draws a 2D line segment.
	 * 
	 * @param g
	 * @param thisLine
	 */
	public void drawLine2D(Graphics2D g, mgui.geometry.LineSegment2D thisLine){
		drawLine2D(g, thisLine, true);
	}
	
	/*****************************
	 * Draws a 2D line segment.
	 * 
	 * @param g
	 * @param thisLine
	 * @param setAttr
	 */
	public void drawLine2D(Graphics2D g, mgui.geometry.LineSegment2D thisLine, boolean setAttr){
		
		if (setAttr){
			g.setPaint((Color)drawing_attributes.getValue("2D.LineColour"));
			g.setStroke((Stroke)drawing_attributes.getValue("2D.LineStyle"));
			}
		Point pt1 = getScreenPoint(thisLine.pt1);
		Point pt2 = getScreenPoint(thisLine.pt2);
		g.drawLine(pt1.x, pt1.y, pt2.x, pt2.y);
		
	}
	
	public void drawRadius2D(Graphics g, Radius2D thisRadius, Point2f centerPt){
		drawRadius2D(g, thisRadius, centerPt, false);
	}
	
	public void drawRadius2D(Graphics g, Radius2D thisRadius, Point2f centerPt, boolean showNodes){
		//attributes should be set by calling routine
		
		//draw line from center point to end point
		Point screenPt1 = getScreenPoint(centerPt);
		Point screenPt2 = getScreenPoint(thisRadius.getEndpoint(centerPt));
		g.drawLine(screenPt1.x, screenPt1.y, screenPt2.x, screenPt2.y);
		
	}
	
	public void drawRadius2DNode(Graphics g, Radius2D thisRadius, Point2f centerPt, int nodeSize){
		Point screenPt = getScreenPoint(thisRadius.getEndpoint(centerPt));
		g.fillRect(screenPt.x - (nodeSize / 2), screenPt.y - (nodeSize / 2), nodeSize, nodeSize);
	}
	
	/***************************************
	 * Draws and fills a rectangle.
	 * 
	 * @param g
	 * @param r
	 */
	public void drawRect2D(Graphics2D graphics, Rect2D rectangle){
		Point corner = getScreenPoint(rectangle.getCorner(Rect2D.CNR_TL));
		int w = getScreenDist(rectangle.getWidth());
		int h = getScreenDist(rectangle.getHeight());
		
		Rectangle2D.Double drawRect = new Rectangle2D.Double(corner.x, corner.y, w, h);
		
		if (((MguiBoolean)(drawing_attributes.getValue("2D.HasFill"))).getTrue() && drawMode != DRAW_FAST){
			graphics.setPaint((Color)drawing_attributes.getValue("2D.FillColour"));
			graphics.fill(drawRect);
			}
		
		graphics.setPaint((Color)drawing_attributes.getValue("2D.LineColour"));
		graphics.setStroke((Stroke)drawing_attributes.getValue("2D.LineStyle"));
		graphics.draw(drawRect);
		
	}

	/**************************
	 * Draws a 2D ellipse.
	 * 
	 * @param g
	 * @param ellipse
	 */
	public void drawEllipse2D(Graphics2D g, mgui.geometry.Ellipse2D ellipse){
		
		if (drawMode == DRAW_FAST){
			drawing_attributes.setValue("2D.HasFill", new MguiBoolean(false));
			}
		
		Rect2D bounds = ellipse.getBounds();
		Point corner = theMap.getScreenPoint(bounds.getCorner(Rect2D.CNR_TL));
		
		Ellipse2D jellipse = new Ellipse2D.Float(corner.x, corner.y, 
												 theMap.getScreenDist(bounds.getWidth()), 
												 theMap.getScreenDist(bounds.getHeight()));
		
		
		//rotate ellipse
		double theta = GeometryFunctions.getVectorAngle(ellipse.getAxisA());
		AffineTransform trans = AffineTransform.getRotateInstance(theta);
		Shape shape = trans.createTransformedShape(jellipse);
		
		if (((MguiBoolean)drawing_attributes.getValue("2D.HasFill")).getTrue()){
			g.setColor((Color)drawing_attributes.getValue("2D.FillColour"));
			g.fill(shape);
			}
		
		g.setColor((Color)drawing_attributes.getValue("2D.LineColour"));
		g.setStroke((BasicStroke)drawing_attributes.getValue("2D.LineStyle"));
		g.draw(shape);
		
		
		
	}
	
	/************************************************
	 * Draws an image to fit a given rectangular bounds.
	 * 
	 * @param graphics
	 * @param bounds
	 * @param image
	 */
	public void drawImage2D(Graphics2D graphics, Rect2D bounds, BufferedImage image){
		if (drawMode == DRAW_FAST){
			drawing_attributes.setValue("2D.HasFill", new MguiBoolean(false));
			drawRect2D(graphics, bounds);
			return;
			}
		
		Point corner = getScreenPoint(bounds.getCorner(Rect2D.CNR_TL));
		int w = getScreenDist(bounds.getWidth());
		int h = getScreenDist(bounds.getHeight());
		
		float t = 0.5f;
		BufferedImage image2;

		image2 = image;

		graphics.drawImage(image2, corner.x, corner.y, w, h, null);	
	}
	
	protected AlphaComposite makeAlphaComposite(float alpha) throws IllegalArgumentException {
	    int type = AlphaComposite.SRC_OVER;
	    return(AlphaComposite.getInstance(type, 1f - alpha));
	}
	
	@Override
	public Attribute getAttribute(String attrName) {
		return drawing_attributes.getAttribute(attrName);
	}

	@Override
	public AttributeList getAttributes() {
		return drawing_attributes;
	}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		drawing_attributes.setValue(attrName, newValue);
	}

	
}