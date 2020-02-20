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

package mgui.interfaces.shapes;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ImageIcon;

import mgui.geometry.PointSet2D;
import mgui.geometry.util.NodeShape;
import mgui.geometry.util.NodeShapeComboRenderer;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.AttributeSelectionMap;
import mgui.interfaces.attributes.AttributeSelectionMap.ComboMode;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.graphs.AbstractGraphNode;
import mgui.interfaces.graphs.util.GraphFunctions;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.NameMap;
import mgui.interfaces.shapes.util.Point2DShape;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiNumber;
import mgui.util.Colour;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/*******************************************************************
 * Shape interface for a set of 2D vertices.
 * 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class PointSet2DInt extends Shape2DInt {

	//public PointSet2D point_set;
	//public HashMap<String, ArrayList<MguiNumber>> nodeData = new HashMap<String, ArrayList<MguiNumber>>();
	public HashMap<String, NameMap> nameMaps = new HashMap<String, NameMap>();
	public boolean[] constraints;
	
	public PointSet2DInt(){
		super();
		init();
	}
	
	public PointSet2DInt(PointSet2D point_set){
		super();
		setShape(point_set);
		init();
	}
	
	private void init(){
		//add attributes here
		HashMap<String, NodeShape> node_shapes = InterfaceEnvironment.getVertexShapes();
		AttributeSelectionMap<NodeShape> shapes = 
			new AttributeSelectionMap<NodeShape>("2D.VertexShape", node_shapes, NodeShape.class);
		shapes.setComboMode(ComboMode.AsValues);
		shapes.setComboRenderer(new NodeShapeComboRenderer());
		shapes.setComboWidth(100);
		attributes.add(shapes);
		AttributeSelection<String> pos = 
				new AttributeSelection<String>("2D.LabelPosition", GraphFunctions.getLabelPositions(), String.class, "SE");
		attributes.add(pos);
		
	}
	
	@Override
	public boolean isInheritingAttribute(Attribute<?> attribute){
		if (attribute.getName().equals("2D.VertexShape") ||
				attribute.getName().equals("2D.LabelPosition"))
			return true;
		return (attribute.getName().startsWith("3D."));
	}
	
	@Override
	protected void setIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/point_set_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/point_set_20.png");
	}
	
	protected NodeShape getVertexShape(AbstractGraphNode vertex){
		
		return (NodeShape)attributes.getValue("2D.VertexShape");
	}
	
	protected Position getLabelPosition(){
		String pos = (String)attributes.getValue("2D.LabelPosition");
		return GraphFunctions.getLabelPosition(pos);
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine d){
		if (!((MguiBoolean)attributes.getValue("IsVisible")).getTrue()) return;
		
		//TODO implement point shapes
		//Point2DShape point_shape = getNodeShape();
		float alpha = -1;
		if (((MguiBoolean)attributes.getValue("2D.HasAlpha")).getTrue())
			alpha = ((MguiFloat)attributes.getValue("2D.Alpha")).getFloat();
		float size = ((MguiFloat)attributes.getValue("2D.VertexScale")).getFloat();
		//Point2DShape point_shape = Point2DShape.getFilledCircle(size);
		NodeShape point_shape = (NodeShape)attributes.getValue("2D.VertexShape");
		
		ArrayList<String> labels = null;
		PointSet2D point_set = this.getPointSet();
		
		// Label vertices
		if (map_idx_to_parent != null && ((MguiBoolean)attributes.getValue("2D.ShowVertexLabels")).getTrue()){
			String to_label = (String)attributes.getValue("LabelData");
			labels = new ArrayList<String>(point_set.getSize());
			String format = null;
			VertexDataColumn v_column = null;
			NameMap n_map = null;
			if (to_label != null){
				v_column = getVertexDataColumn(to_label);
				if (v_column.hasNameMap()){
					n_map = v_column.getNameMap();
				}else{
					format = (String)attributes.getValue("2D.LabelFormat");
					}
				}
			for (int i = 0; i < point_set.getSize(); i++){
				int parent_idx = this.map_idx_to_parent.get(i);
				String label = "";
				if (v_column == null){
					label = "" + parent_idx;
				}else{
					if (format != null){
						label = v_column.getValueAtVertex(parent_idx).toString(format);
					}else{
						label = n_map.get((int)v_column.getValueAtVertex(parent_idx).getValue());
						}
					}
				if (label == null) label = "" + parent_idx;
				labels.add(label);
				}
			}
		
		d.drawing_attributes.setIntersection(attributes);
		
		//if show data, pass list of colours
		if (hasData() && showData()){
			ArrayList<Colour> colours = new ArrayList<Colour>();
			ColourMap cmap = getColourMap();
			ArrayList<MguiNumber> data = getCurrentVertexData();
			//if (data == null) InterfaceSession.log("data null..");
			for (int i = 0; i < map_idx_to_parent.size(); i++){
				int idx = map_idx_to_parent.get(i);
				colours.add(cmap.getColour(data.get(idx)));
				}
			//d.drawing_attributes.setValue("2D.VertexOutlineColour", attributes.getValue("2D.VertexOutlineColour"));
			d.drawPointSet2D(g, point_set, size * 2, point_shape, colours, alpha, labels);
		}else{
			//d.drawing_attributes.setValue("2D.VertexColour", attributes.getValue("2D.VertexColour"));
			//d.drawing_attributes.setValue("2D.VertexOutlineColour", attributes.getValue("2D.VertexOutlineColour"));
			d.drawPointSet2D(g, point_set, size * 2, point_shape, null, alpha, labels);
			}
		
		
		
	}
	
//	@Override
//	public boolean hasData(){
//		return nodeData.keySet().size() > 0; 
//	}
	
	public void setColourMap(ColourMap cm){
		attributes.setValue("ColourMap", cm, true);
	}
	
	public void setColourMap(ColourMap cm, boolean update){
		attributes.setValue("ColourMap", cm, update);
	}
	
	@Override
	public ColourMap getColourMap(){
		return (ColourMap)attributes.getValue("ColourMap");
	}
	
//	@Override
//	public ArrayList<MguiNumber> getCurrentVertexData(){
//		return nodeData.get(getCurrentColumn());
//	}
	
	@Override
	public String getCurrentColumn(){
		AttributeSelection a = (AttributeSelection)attributes.getAttribute("CurrentData");
		return (String)a.getValue();
	}
	
	@Override
	public double getDataMin(){
		return ((MguiDouble)attributes.getValue("DataMin")).getValue();
	}
	
	@Override
	public double getDataMax(){
		return ((MguiDouble)attributes.getValue("DataMax")).getValue();
	}
	
	@Override
	public float getVertexScale(){
		return ((MguiFloat)attributes.getValue("2D.VertexScale")).getFloat();
	}
	
	@Override
	public void setDataMin(double d){
		attributes.setValue("DataMin", new MguiDouble(d));
	}
	
	@Override
	public void setDataMax(double d){
		attributes.setValue("DataMax", new MguiDouble(d));
	}
	
//	public void addNodeData(String key){
//		ArrayList<MguiNumber> vals = new ArrayList<MguiNumber>(getPointSet().n);
//		for (int i = 0; i < getPointSet().n; i++)
//			vals.add(new MguiDouble(0));
//		addNodeData(key, vals);
//	}
//	
//	public void addNodeData(String key, ArrayList<MguiNumber> data){
//		addNodeData(key, data, null);
//	}
//	
//	public void addNodeData(String key, ArrayList<MguiNumber> data, NameMap map){
//		nodeData.put(key, data);
//		if (map != null) nameMaps.put(key, map);
//		
//		AttributeSelection a = (AttributeSelection)attributes.getAttribute("CurrentData");
//		String currentData = getCurrentColumn();
//		a.setList(getVertexDataColumnNames());
//		a.setValue(currentData, false);
//		//a.listWidth = 300;
//		
//	}
//	
//	@Override
//	public ArrayList<String> getVertexDataColumnNames(){
//		if (nodeData.size() == 0) return null;
//		ArrayList<String> list = new ArrayList<String>(nodeData.size());
//		Iterator<String> itr = nodeData.keySet().iterator();
//		while (itr.hasNext())
//			list.add(itr.next());
//		Collections.sort(list);
//		return list;
//	}
	
	public PointSet2D getPointSet(){
		return (PointSet2D)thisShape;
	}
	
	@Override
	public boolean needsRedraw(Attribute<?> a){
		//is it visible?
		if (a.getName().equals("IsVisible")) return true;
		if (!isVisible()) return false;
		if (a.getName().equals("2D.Show")) return true;
		if (!show2D()) return false;
		
		if (a.getName().equals("2D.VertexColour") ||
			a.getName().equals("2D.VertexShape") ||
			a.getName().equals("2D.VertexScale")) 
			return true;
		
		return super.needsRedraw(a);
	}
	
}