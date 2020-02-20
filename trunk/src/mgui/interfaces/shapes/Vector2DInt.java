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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;

import mgui.geometry.Polygon2D;
import mgui.geometry.Vector2D;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.shapes.util.Point2DShape;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiNumber;


public class Vector2DInt extends Shape2DInt {

	float start_ratio = 0, end_ratio = 1; 
	
	HashMap<String, Double> column_maxes = new HashMap<String, Double>();
	HashMap<String, Double> column_mins = new HashMap<String, Double>();
	
	public Vector2DInt(){
		super();
		thisShape = new Polygon2D();
		init();
	}
	
	public Vector2DInt(Vector2D vector){
		super();
		setShape(vector);
		init();
	}
	
	private void init(){
		//set attributes
		attributes.add(new Attribute<MguiBoolean>("ShowArrow", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowStartPt", new MguiBoolean(false)));
		attributes.add(new Attribute<Color>("DataLineColour", Color.BLACK));
		attributes.add(new Attribute<MguiFloat>("DataLineOffset", new MguiFloat(1f)));
		attributes.add(new Attribute<MguiFloat>("DataLineHeight", new MguiFloat(4f)));
		
		ArrayList<Point2DShape> list = new ArrayList<Point2DShape>();
		Point2DShape circle = Point2DShape.getFilledCircle(1);
		list.add(circle);
		attributes.add(new AttributeSelection<Point2DShape>("StartPtShape", list, Point2DShape.class, circle));
		attributes.add(new Attribute<MguiFloat>("StartPtScale", new MguiFloat(1.0f)));
		list = new ArrayList<Point2DShape>();
		Point2DShape arrow = Point2DShape.getFilledArrow(1);
		list.add(arrow);
		attributes.add(new AttributeSelection<Point2DShape>("ArrowShape", list, Point2DShape.class, arrow));
		attributes.add(new Attribute<MguiFloat>("ArrowScale", new MguiFloat(1.0f)));
		
		
		//bounds
		updateShape();
	}
	
	public Vector2D getVector(){
		return (Vector2D)thisShape;
	}
	
	public void setStartRatio(float ratio){
		start_ratio = ratio;
	}
	
	public void setEndRatio(float ratio){
		end_ratio = ratio;
	}
	
	public float getStartRatio(){
		return Math.max(0, Math.min(start_ratio, end_ratio));
	}
	
	public float getEndRatio(){
		return Math.min(1, Math.max(start_ratio, end_ratio));
	}
	
	public Color getDataLineColour(){
		return (Color)attributes.getValue("DataLineColour");
	}
	
	public void setDataLineColour(Color c){
		attributes.setValue("DataLineColour", c);
	}
	
	public float getDataLineOffset(){
		return ((MguiFloat)attributes.getValue("DataLineOffset")).getFloat();
	}
	
	public void setDataLineOffset(float offset){
		attributes.setValue("DataLineOffset", new MguiFloat(offset));
	}
	
	public float getDataLineHeight(){
		return ((MguiFloat)attributes.getValue("DataLineHeight")).getFloat();
	}
	
	public void setDataLineHeight(float height){
		attributes.setValue("DataLineHeight", new MguiFloat(height));
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		
		//if this shape has a parent, it will take care of this update
		if (hasParentShape()) return;
	
		super.attributeUpdated(e);
		
	}
	
	public void setArrowScale(float scale){
		attributes.setValue("ArrowScale", new MguiFloat(scale));
	}
	
	public void setStartPtScale(float scale){
		attributes.setValue("StartPtScale", new MguiFloat(scale));
	}
	
	@Override
	public void draw(Graphics2D g, DrawingEngine d){
		
		float arrow_scale = d.getScreenDist(((MguiFloat)attributes.getValue("ArrowScale")).getValue()); // * getVector().vector.length());
			float start_scale = d.getScreenDist(((MguiFloat)attributes.getValue("StartPtScale")).getValue()); // * getVector().vector.length() / 5f);
			
		float alpha = -1;
		if (((MguiBoolean)attributes.getValue("HasTransparency")).getTrue())
			alpha = ((MguiFloat)attributes.getValue("Alpha")).getFloat();
			
		Point2DShape arrow = null;
		if (((MguiBoolean)attributes.getValue("ShowArrow")).getTrue())
			arrow = Point2DShape.getFilledArrow(arrow_scale);
		
		Point2DShape start = null;
		if (((MguiBoolean)attributes.getValue("ShowStartPt")).getTrue())
			start = Point2DShape.getFilledCircle(start_scale);
		
		d.drawVector2D(g, getVector(), start, arrow, alpha);
		
		//show data from parent?
		if (showData() && hasParentShape()){
			//Vector3DInt parent = (Vector3DInt)getParentShape();
			//String current = parent.getCurrentColumn();
			ArrayList<MguiNumber> column_data = getCurrentVertexData();
			if (column_data != null){
				//ArrayList<arNumber> column_data = getData(current);
				
				if (column_data != null){
					
					int size = column_data.size();
					int start_index = (int)(getStartRatio() * (double)size);
					int end_index = (int)(getEndRatio() * (double)size);
					ArrayList<MguiNumber> plotted_data = new ArrayList<MguiNumber>();
					for (int i = start_index; i < end_index; i++)
						plotted_data.add(column_data.get(i));
					
					if (plotted_data.size() > 0){
						d.drawing_attributes.setValue("LineColour", getDataLineColour());
						d.drawing_attributes.setValue("LineStyle", new BasicStroke(1f));
						
						d.drawSegmentData2D(g, getVector().asLineSegment(), plotted_data, 
											(float)getDataMin(), 
											(float)getDataMax(),
											getDataLineOffset(), 
											getDataLineHeight());
						}
					}
				}
			
			}
		
	}
	
	@Override
	public ArrayList<MguiNumber> getCurrentVertexData(){
		if (!hasParentShape()) return super.getCurrentVertexData();
		return getParentShape().getCurrentVertexData();
	}
	
	@Override
	public double getDataMin(){
		if (!hasParentShape()) return super.getDataMin();
		return getParentShape().getDataMin();
	}
	
	@Override
	public double getDataMax(){
		if (!hasParentShape()) return super.getDataMin();
		return getParentShape().getDataMax();
	}
	
	/*
	protected ArrayList<arNumber> getNormalizedData(String key, boolean regen){
		if (!hasParentShape()) return null;
		double max = getColumnMax(key, regen);
		if (Double.isNaN(max)) return null;
		ArrayList<arNumber> data = getParentShape().getData(key);
		if (data == null) return null;
		ArrayList<arNumber> copy = new ArrayList<arNumber>(data);
		for (int i = 0; i < copy.size(); i++)
			copy.get(i).divide(max);
		return copy;
	}
	
	protected double getColumnMax(String key, boolean regen){
		if (!hasParentShape()) return Double.NaN;
		Double max = column_maxes.get(key);
		if (regen || max == null){
			ArrayList<arNumber> data = getParentShape().getData(key);
			if (data == null) return Double.NaN;
			max = -Double.MAX_VALUE;
			for (int i = 0; i < data.size(); i++)
				max = Math.max(data.get(i).getValue(), max);
			column_maxes.put(key, max);
			}
		return max;
	}
	
	protected double getColumnMin(String key, boolean regen){
		if (!hasParentShape()) return Double.NaN;
		Double min = column_mins.get(key);
		if (regen || min == null){
			ArrayList<arNumber> data = getParentShape().getData(key);
			if (data == null) return Double.NaN;
			min = Double.MAX_VALUE;
			for (int i = 0; i < data.size(); i++)
				min = Math.min(data.get(i).getValue(), min);
			column_mins.put(key, min);
			}
		return min;
	}
	*/
	
}