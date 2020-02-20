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

package mgui.interfaces.plots.sgt;

import gov.noaa.pmel.sgt.Axis;
import gov.noaa.pmel.sgt.Graph;
import gov.noaa.pmel.sgt.SGLabel;
import gov.noaa.pmel.util.Point2D;
import gov.noaa.pmel.util.SoTPoint;
import gov.noaa.pmel.util.SoTRange;
import gov.noaa.pmel.util.SoTValue;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.plots.PlotException;
import mgui.interfaces.plots.sgt.SgtPlotFunctions.AxisType;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiFloat;
import mgui.numbers.MguiInteger;

/*********************************************************
 * Interface for displaying an axis on a plot. Holds an SGT axis objects and allows
 * display attributes to be specified for it. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SgtAxis<A extends Axis> extends AbstractInterfaceObject
									 implements AttributeListener{

	protected A axis;
	protected AttributeList attributes = new AttributeList();
	protected AxisType type;
	
	public SgtAxis(String name, A axis, AxisType type){
		this.type = type;
		this.axis = axis;
		_init();
		setName(name);
		
		
	}
	
	private void _init(){
		attributes.add(new Attribute<String>("Name", "No-name"));
		attributes.add(new Attribute<Font>("Label Font", new Font("Arial", Font.PLAIN, 5)));
		attributes.add(new Attribute<Color>("Label Colour", Color.black));
		attributes.add(new Attribute<String>("Label", "X"));
		attributes.add(new Attribute<MguiFloat>("Label Size", new MguiFloat(0.05f)));
		attributes.add(new Attribute<MguiFloat>("Large Tick", new MguiFloat(0.05f)));
		attributes.add(new Attribute<MguiFloat>("Small Tick", new MguiFloat(0.05f)));
		
		attributes.add(new Attribute<String>("Range", "0.0 10.0 1.0"));
		attributes.add(new Attribute<MguiBoolean>("Auto Range", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiInteger>("Auto Interval", new MguiInteger(10)));
		attributes.add(new Attribute<String>("Ordinates Format", "0.00"));
		
		attributes.add(new Attribute<MguiBoolean>("IsReversed", new MguiBoolean(false)));
		attributes.add(new Attribute<MguiBoolean>("IsFlushToMain", new MguiBoolean(true)));
		
		ArrayList<String> positions = new ArrayList<String>();
		String default_pos = "Left"; 
		switch (type){
			case Y:
				positions.add("Left");
				positions.add("Right");
				break;
			case X:
				positions.add("Top");
				positions.add("Bottom");
				default_pos = "Bottom";
			}
		positions.add("Free");
		AttributeSelection<String> selection = 
			new AttributeSelection<String>("Position", positions, String.class, default_pos);
		attributes.add(selection);
		
		attributes.addAttributeListener(this);
		
		try{
			update();
		}catch (PlotException ex){
			InterfaceSession.log("SgtAxis: Could not create axis.", 
								 LoggingType.Errors);
			InterfaceSession.handleException(ex);
			}
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e){
		
	}
	
	/*****************************************************
	 * Updates the appearance of this axis from its attributes.
	 * 
	 */
	public void update() throws PlotException{
		Font font = (Font)attributes.getValue("Label Font");
        SGLabel title = new SGLabel(getName() + " title", getLabel(), new Point2D.Double(0.0, 0.0));
        title.setFont(font);
        title.setHeightP(((MguiFloat)attributes.getValue("Label Size")).getValue());
        
        SoTRange range = getRange();
         
        if (range == null){
        	throw new PlotException("Invalid data ranges.");
        	}
       
        double height = ((MguiFloat)attributes.getValue("Label Size")).getValue();
        double tick = ((MguiFloat)attributes.getValue("Large Tick")).getValue();
        //double ord = ((MguiFloat)attributes.getValue("Ordinates Size")).getValue();
        Color colour = (Color)attributes.getValue("Label Colour");
        axis.setRangeU(range);
        axis.setTitle(title);
        axis.setLabelColor(colour);
        //axis.setLabelHeightP(ord);
        axis.setLargeTicHeightP(tick);
        title.setHeightP(height);

        //set orientations
        String pos = getPosition();
        switch (type){
	        case X:
	        	if (pos.equals("Top")){
	        		axis.setTicPosition(Axis.POSITIVE_SIDE);
	        		axis.setLabelPosition(Axis.POSITIVE_SIDE);
	        	}else{
	        		axis.setTicPosition(Axis.NEGATIVE_SIDE);
	        		axis.setLabelPosition(Axis.NEGATIVE_SIDE);
	        		}
	        	break;
	        case Y:
	        	if (pos.equals("Right")){
	        		axis.setTicPosition(Axis.POSITIVE_SIDE);
	        		axis.setLabelPosition(Axis.POSITIVE_SIDE);
	        	}else{
	        		axis.setTicPosition(Axis.NEGATIVE_SIDE);
	        		axis.setLabelPosition(Axis.NEGATIVE_SIDE);
	        		}
	        	break;
        	}
        
	}
	
	/****************************************************
	 * Determines whether the ordinates on this axis are reversed.
	 * 
	 * @return
	 */
	public boolean isReversed(){
		return ((MguiBoolean)attributes.getValue("IsReversed")).getTrue();
	}
	
	/****************************************************
	 * Sets whether this axis' ordinates are reversed.
	 * 
	 * @param b
	 */
	public void setReversed(boolean b){
		attributes.setValue("IsReversed", new MguiBoolean(b));
	}
	
	/****************************************************
	 * Determines whether this axis should appear flush with the main opposing axis.
	 * 
	 * @return
	 */
	public boolean isFlushToMain(){
		return ((MguiBoolean)attributes.getValue("IsFlushToMain")).getTrue();
	}
	
	/****************************************************
	 * Sets whether this axis should appear flush with the main opposing axis.
	 * 
	 * @param b
	 */
	public void setFlushToMain(boolean b){
		attributes.setValue("IsFlushToMain", new MguiBoolean(b));
	}
	
	/****************************************************
	 * Sets the origin of this axis, in physical coordinates.
	 * 
	 * @param x
	 * @param y
	 */
	public void setLocation(double x, double y){
		axis.setLocationU(new SoTPoint(x,y));
	}
	
	/****************************************************
	 * Sets the origin of this axis, in physical coordinates.
	 * 
	 * @param x
	 * @param y
	 */
	public void setLocation(SoTPoint p){
		axis.setLocationU(p);
	}
	
	/**************************************************
	 * Returns the type of this axis; one of:
	 * 
	 * <ul>
	 * <li>X
	 * <li>Y
	 * </ul>
	 * 
	 * @return
	 */
	public AxisType getType(){
		return type;
	}
	
	/**************************************************
	 * Returns the position of this axis, relative to the graph.
	 * 
	 * @return
	 */
	public String getPosition(){
		return (String)attributes.getValue("Position");
	}
	
	public void setPosition(String pos){
		attributes.setValue("Position", pos);
	}
	
	/**************************************************
	 * Returns the origin of this axis, which depends on its range and its
	 * position.
	 * 
	 * @return
	 */
	public SoTValue getOrigin(){
		String pos = getPosition();
		SoTRange range = getRange();
		
		switch(type){
			case X:
				if (pos.equals("Top")) return range.getEnd();
				if (pos.equals("Bottom")) return range.getStart();
				break;
			case Y:
				if (pos.equals("Right")) return range.getEnd();
				if (pos.equals("Left")) return range.getStart();
				break;
			}
		
		//TODO: implement free positioning
		return new SoTValue.Double(0); 
	}
	
	@Override
	public String getTreeLabel(){
		return "Axis: " + getName();
	}
	
	/**************************************************
	 * Returns the SGT axis.
	 * 
	 * @return
	 */
	public A getAxis(){
		return axis;
	}
	
	/**************************************************
	 * Sets this axis's range from <code>range</code>.
	 * 
	 * @param range
	 */
	public void setRange(SoTRange range){
		setRange(range, true);
	}
	
	/**************************************************
	 * Sets this axis's range from <code>range</code>.
	 * 
	 * @param range
	 * @param whether to fire attribute listeners
	 */
	public void setRange(SoTRange range, boolean listeners){
		range = Graph.computeRange(range, getAutoInterval());
		axis.setRangeU(range);
		String s = "" + range.getStart() + " " + range.getEnd() + " " + range.getDelta();
		attributes.setValue("Range", s, listeners);
	}

	/******************************************
	 * Returns the X axis range, from a string of the form
	 * 
	 * <p>[min] [max] [step]
	 * 
	 * @return
	 */
	public SoTRange getRange(){
		String range = (String)attributes.getValue("Range");
		String[] parts = range.split(" ");
		if (parts.length != 3){
			InterfaceSession.log("SgtAxis: invalid range '" + range + "'", 
								 LoggingType.Errors);
			return null;
			}
		SoTRange new_range = new SoTRange.Double(Double.valueOf(parts[0]),
											     Double.valueOf(parts[1]),
											     Double.valueOf(parts[2]));
		if (isReversed())
			new_range.flipStartAndEnd();
		
		return new_range;
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		//Add attributes as direct children
		ArrayList<Attribute<?>> list = attributes.getAsList();
		
		for (int i = 0; i < list.size(); i++){
			treeNode.addChild(list.get(i).issueTreeNode());
			}
		
	}
	
	/**************************************************
	 * Set the label for the axis.
	 * 
	 * @param label
	 */
	public void setLabel(String label){
		attributes.setValue("Label", label);
	}
	
	public String getLabel(){
		return (String)attributes.getValue("Label");
	}
	
	/*************************************************
	 * Indicates whether this axis automatically adjusts its range to its data.
	 * 
	 * @return
	 */
	public boolean isAutoRange(){
		return ((MguiBoolean)attributes.getValue("Auto Range")).getTrue();
	}
	
	/************************************************
	 * Returns the number of intervals to use when constructing an automatic range.
	 * 
	 * @return
	 */
	public int getAutoInterval(){
		return ((MguiInteger)attributes.getValue("Auto Interval")).getInt();
	}
	
}