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

package mgui.interfaces.graphics.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.LineMetrics;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.Font3D;
import org.jogamp.java3d.FontExtrusion;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.J3DGraphics2D;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.OrientedShape3D;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Text3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector2f;
import org.jogamp.vecmath.Vector3f;

import mgui.geometry.Plane3D;
import mgui.geometry.Text2D;
import mgui.geometry.Vector2D;
import mgui.geometry.Vector3D;
import mgui.geometry.util.GeometryFunctions;
import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.graphics.InterfaceGraphic3D;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.Vector3DInt;
import mgui.interfaces.shapes.util.Point2DShape;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.resources.icons.IconObject;
import mgui.util.Colours;

/************************************************
 * Renders 3D axes on a 3D window.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Axes3D extends AbstractInterfaceObject implements AttributeListener,
															   IconObject{

	AttributeList attributes = new AttributeList();
	ArrayList<AxesListener> listeners = new ArrayList<AxesListener>();
	
	protected Icon icon;
	
	protected float axes_min_scale = 0.5f, axes_max_scale = 1.3f;
	BranchGroup axes_group;
	Vector2f last_scales = new Vector2f(1,1);
	InterfaceGraphic3D panel;
	protected Vector2f[] axis_vectors = new Vector2f[3];
	
	public Axes3D(){
		init();
	}
	
	public Axes3D(InterfaceGraphic3D panel){
		this.panel = panel;
		init();
	}
	
	private void init(){
		
		attributes.add(new Attribute<String>("Name", "3D Axes"));
		attributes.add(new Attribute<MguiBoolean>("IsVisible", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowBorder", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowLabels", new MguiBoolean(true)));
		attributes.add(new Attribute<Color>("BorderColour", Color.BLACK));
		attributes.add(new Attribute<Color>("LabelColour", Color.BLACK));
		attributes.add(new Attribute<Color>("AxisColourX", Color.BLUE));
		attributes.add(new Attribute<Color>("AxisColourY", Color.GREEN));
		attributes.add(new Attribute<Color>("AxisColourZ", Color.RED));
		attributes.add(new Attribute<String>("AxisLabelX", "X"));
		attributes.add(new Attribute<String>("AxisLabelY", "Y"));
		attributes.add(new Attribute<String>("AxisLabelZ", "Z"));
		attributes.add(new Attribute<MguiFloat>("AxesSize", new MguiFloat(100)));
		attributes.add(new Attribute<MguiFloat>("ArrowSize", new MguiFloat(8f)));
		attributes.add(new Attribute<Font>("LabelFont", new Font("Courier New", Font.PLAIN, 17)));
		
		attributes.addAttributeListener(this);
	}
	
	public void addListener(AxesListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(AxesListener listener){
		listeners.remove(listener);
	}
	
	public void setPanel(InterfaceGraphic3D panel){
		this.panel = panel;
	}
	
	public float getAxesSize(){
		return ((MguiFloat)attributes.getValue("AxesSize")).getFloat();
	}
	
	public boolean isVisible(){
		return ((MguiBoolean)attributes.getValue("IsVisible")).getTrue();
	}
	
	public void setVisible(boolean b){
		attributes.setValue("IsVisible", new MguiBoolean(b));
	}
	
	public void setAxesSize(float s){
		attributes.setValue("AxesSize", new MguiFloat(s));
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e) {
		
		if (e.getAttribute().getName().equals("Name")) return;
		if (panel == null) return;
		panel.axesChanged(new AxesEvent(this));
		
	}
	
	public boolean getShowBorder(){
		return ((MguiBoolean)attributes.getValue("ShowBorder")).getTrue();
	}
	
	public boolean getShowLabels(){
		return ((MguiBoolean)attributes.getValue("ShowLabels")).getTrue();
	}
	
	public Color getBorderColour(){
		return (Color)attributes.getValue("BorderColour");
	}
	
	public void setFromCamera(Camera3D camera){
		
		//y axis is up vector
		Vector3f y_axis = new Vector3f(camera.getUpVector());
		
		//x axis is cross of up and l.o.s. vectors
		Vector3f x_axis = new Vector3f(camera.getLineOfSight());
		x_axis.scale(-1);
		x_axis.cross(x_axis, y_axis);
		
		Plane3D plane = new Plane3D(new Point3f(), x_axis, y_axis);
		
		//get projection of axes onto this plane
		Vector3f axis3D = new Vector3f(1, 0, 0);
		axis_vectors[0] = GeometryFunctions.getProjectedToPlane2D(axis3D, plane);
		if (!GeometryFunctions.isValidVector(axis_vectors[0]))
			axis_vectors[0].set(0, 0);
		axis3D.set(0, 1, 0);
		axis_vectors[1] = GeometryFunctions.getProjectedToPlane2D(axis3D, plane);
		if (!GeometryFunctions.isValidVector(axis_vectors[1]))
			axis_vectors[1].set(0, 0);
		axis3D.set(0, 0, 1);
		axis_vectors[2] = GeometryFunctions.getProjectedToPlane2D(axis3D, plane);
		if (!GeometryFunctions.isValidVector(axis_vectors[2]))
			axis_vectors[2].set(0, 0);
		
	}
	
	public void render(Canvas3D canvas){
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		
		J3DGraphics2D g2d = canvas.getGraphics2D();
		
		//set margin based on size and screen dims
		float margin = (width) * 0.05f;
		margin = Math.min(margin, (height) * 0.05f);
		
		int box_size = (int)getAxesSize();
		box_size = (int)Math.min(box_size, width - (2 * margin));
		box_size = (int)Math.min(box_size, height - (2 * margin));
		
		//draw box and axes
		Point2DShape arrow = Point2DShape.getFilledArrow(getArrowSize());
		DrawingEngine de = new DrawingEngine();
		de.setCoordSys(DrawingEngine.DRAW_SCREEN);
		
		int start_x = (int)(width - margin - box_size);
		int start_y = (int)margin;
		
		int font_margin = getLabelHeight(g2d);
		
		if (getShowBorder()){
			g2d.setColor(getBorderColour());
			g2d.drawRect(start_x, start_y, box_size, box_size);
			}
		
		float scale = (box_size - (2f * font_margin)) / 2f;
		Vector2f axis = new Vector2f();
		Vector2f text_offset = new Vector2f(1, 0);
		Point2f p = new Point2f();
		Point2f p2 = new Point2f();
		de.setAttribute("2D.LabelColour", getLabelColour());
		de.setAttribute("2D.LabelFont", getLabelFont());
		
		axis.set(getAxisX());
		axis.y = -axis.y;		//flip y
		axis.scale(scale);
		Point2f mid_pt = new Point2f(start_x + font_margin + (int)scale, start_y + font_margin + (int)scale);
		Vector2D v = new Vector2D(mid_pt, axis);
		de.drawing_attributes.setValue("2D.LineColour", getAxisColourX());
		de.drawVector2D(g2d, v, null, arrow, -1);
		
		if (getShowLabels()){
			axis.scale(1.05f);
			p.set(mid_pt);
			p.add(axis);
			int label_width = getLabelWidth(g2d, getAxisLabelX());
			if (axis.x < 0) p.x += label_width * axis.x / axis.length();
			if (axis.y > 0) p.y += font_margin * axis.y / axis.length();
			p2.set(p);
			p2.add(text_offset);
			Text2D text = new Text2D(getAxisLabelX(), p, p2);
			de.drawText2D(g2d, text);
			}
		
		axis.set(getAxisY());
		axis.y = -axis.y;		//flip y
		axis.scale(scale);
		v = new Vector2D(mid_pt, axis);
		de.drawing_attributes.setValue("2D.LineColour", getAxisColourY());
		de.drawVector2D(g2d, v, null, arrow, -1);
		
		if (getShowLabels()){
			axis.scale(1.05f);
			p.set(mid_pt);
			p.add(axis);
			int label_width = getLabelWidth(g2d, getAxisLabelY());
			if (axis.x < 0) p.x += label_width * axis.x / axis.length();
			if (axis.y > 0) p.y += font_margin * axis.y / axis.length();
			p2.set(p);
			p2.add(text_offset);
			Text2D text = new Text2D(getAxisLabelY(), p, p2);
			de.drawText2D(g2d, text);
			}
		
		axis.set(getAxisZ());
		axis.y = -axis.y;		//flip y
		axis.scale(scale);
		v = new Vector2D(mid_pt, axis);
		de.drawing_attributes.setValue("2D.LineColour", getAxisColourZ());
		de.drawVector2D(g2d, v, null, arrow, -1);
		
		if (getShowLabels()){
			axis.scale(1.05f);
			p.set(mid_pt);
			p.add(axis);
			int label_width = getLabelWidth(g2d, getAxisLabelZ());
			if (axis.x < 0) p.x += label_width * axis.x / axis.length();
			if (axis.y > 0) p.y += font_margin * axis.y / axis.length();
			p2.set(p);
			p2.add(text_offset);
			Text2D text = new Text2D(getAxisLabelZ(), p, p2);
			de.drawText2D(g2d, text);
			}
		
	}
	
	private int getLabelHeight(Graphics2D g){
		LineMetrics lm = getLabelFont().getLineMetrics("?", g.getFontRenderContext());
		return (int)lm.getAscent();
	}
	
	private int getLabelWidth(Graphics2D g, String label){
		FontMetrics fm = g.getFontMetrics(getLabelFont());
		return fm.stringWidth(label);
	}
	
	public Vector2f getAxisX(){
		return axis_vectors[0];
	}
	
	public Vector2f getAxisY(){
		return axis_vectors[1];
	}
	
	public Vector2f getAxisZ(){
		return axis_vectors[2];
	}
	
	public Color getAxisColourX(){
		return (Color)attributes.getValue("AxisColourX");
	}
	
	public Color getAxisColourY(){
		return (Color)attributes.getValue("AxisColourY");
	}
	
	public Color getAxisColourZ(){
		return (Color)attributes.getValue("AxisColourZ");
	}
	
	public String getAxisLabelX(){
		return (String)attributes.getValue("AxisLabelX");
	}
	
	public String getAxisLabelY(){
		return (String)attributes.getValue("AxisLabelY");
	}
	
	public String getAxisLabelZ(){
		return (String)attributes.getValue("AxisLabelZ");
	}
	
	public float getArrowSize(){
		return ((MguiFloat)attributes.getValue("ArrowSize")).getFloat();
	}

	public void updateAxesNode(){
		updateAxesNode(last_scales);
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public void updateAxesNode(Vector2f scales){
		if (axes_group == null) return;
		
		last_scales = scales;
		
		axes_group.removeAllChildren();
		if (!isVisible()) return;
		
		BranchGroup lower_group = new BranchGroup();
		lower_group.setCapability(BranchGroup.ALLOW_DETACH);
		
		float axes_size = getAxesSize();
		float screen_scale = Math.min(last_scales.getX(), last_scales.getY());
		screen_scale = Math.max(axes_min_scale, screen_scale);
		float arrow_size = axes_size * 2f * screen_scale * getArrowSize();
		float axis_size = axes_size * screen_scale;
			
		//Z
		Vector3DInt vector = new Vector3DInt(new Vector3D(new Point3f(0,0,0), new Vector3f(0,0,axis_size)));
		vector.setArrowScale(arrow_size / 10f);
		vector.setAttribute("2D.LineColour", getAxisColourZ());
		BranchGroup bg = vector.getScene3DObject();
		bg.detach();
		Point3f label_pt = new Point3f(0f, 0f, axis_size);
		TransformGroup label = getLabel(getAxisLabelZ(), label_pt, Colours.getColor3f(getAxisColourZ()), arrow_size);
		lower_group.addChild(label);
		lower_group.addChild(bg);
		//X
		vector = new Vector3DInt(new Vector3D(new Point3f(0,0,0), new Vector3f(axis_size,0,0)));
		vector.setArrowScale(arrow_size / 10f);
		vector.setAttribute("2D.LineColour", getAxisColourX());
		bg = vector.getScene3DObject();
		bg.detach();
		label_pt = new Point3f(axis_size, 0f ,0f);
		label = getLabel(getAxisLabelX(), label_pt, Colours.getColor3f(getAxisColourX()), arrow_size);
		lower_group.addChild(label);
		lower_group.addChild(bg);
		//Y
		vector = new Vector3DInt(new Vector3D(new Point3f(0,0,0), new Vector3f(0,axis_size,0)));
		vector.setArrowScale(arrow_size / 10f);
		vector.setAttribute("2D.LineColour", getAxisColourY());
		bg = vector.getScene3DObject();
		bg.detach();
		label_pt = new Point3f(0f, axis_size, 0f);
		label = getLabel(getAxisLabelY(), label_pt, Colours.getColor3f(getAxisColourY()), arrow_size);
		lower_group.addChild(label);
		lower_group.addChild(bg);
		
		axes_group.addChild(lower_group);
	}
	
	public BranchGroup getScene3DNode(){
		if (axes_group == null){
			axes_group = new BranchGroup();
			axes_group.setCapability(BranchGroup.ALLOW_DETACH);
			axes_group.setCapability(Group.ALLOW_CHILDREN_WRITE);
			axes_group.setCapability(Group.ALLOW_CHILDREN_EXTEND);
			}
		
		return axes_group;
	}
	
	private TransformGroup getLabel(String label, Point3f point, Color3f colour, float size) {
		TransformGroup tg = new TransformGroup();
		try{
		
			tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			Transform3D t3d = new Transform3D();
			t3d.setTranslation(new Vector3f(point));
			tg.setTransform(t3d);
			 
			Appearance text_appear = new Appearance();
			ColoringAttributes text_color = new ColoringAttributes();
			text_color.setColor(colour);
			text_appear.setColoringAttributes(text_color);
			Material material = new Material();
			
		    material.setDiffuseColor(colour);
		    material.setEmissiveColor(colour);
		    material.setSpecularColor(colour);
			text_appear.setMaterial(material);
			
			// Create a simple shape leaf node, add it to the scene graph.
			Font3D font_3d = new Font3D(getLabelFont(), new FontExtrusion());
			Text3D text_geom = new Text3D(font_3d, label);
			text_geom.setAlignment(Text3D.ALIGN_FIRST);           
			Shape3D text_shape = new Shape3D();
			text_shape.setGeometry(text_geom);                    
			OrientedShape3D os3d = new OrientedShape3D();       
			os3d.setGeometry(text_shape.getGeometry());
			os3d.setAppearance(text_appear);
		    os3d.setAlignmentMode(OrientedShape3D.ROTATE_ABOUT_POINT);                  
		    tg.addChild(os3d);
		}catch (IllegalArgumentException ex){
			InterfaceSession.log("Problem setting axis label '" + label + "'.. check font size?");
			}
	    
	    return tg;
	}
	
	public Font getLabelFont(){
		return (Font)attributes.getValue("LabelFont");
	}
	
	public Color getLabelColour(){
		return (Color)attributes.getValue("LabelColour");
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		treeNode.addChild(attributes.issueTreeNode());
		
	}
	
	@Override
	public String getTreeLabel(){
		return getName();
	}
	
	//override to set a specific icon
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/axes_3d_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/axes_3d_20.png");
	}
	
	@Override
	public Icon getObjectIcon() {
		if (icon == null) setIcon();
		return icon;
	}
	
}