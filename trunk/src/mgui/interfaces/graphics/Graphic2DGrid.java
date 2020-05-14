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

package mgui.interfaces.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.ArrayList;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.DisplayListener;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.graphics.util.DrawingEngine;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiDouble;
import mgui.numbers.MguiInteger;

/*************************************************************
 * Defines a 2D grid, for display in an {@link InterfaceGraphic2D} window.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class Graphic2DGrid extends AbstractInterfaceObject implements AttributeObject,
																	  AttributeListener{

	AttributeList attributes = new AttributeList();
	ArrayList<DisplayListener> listeners = new ArrayList<DisplayListener>();
	
	public Graphic2DGrid(){
		init();
	}
	
	protected void init(){
		attributes.add(new Attribute<String>("Name", "default grid"));
		attributes.add(new Attribute<MguiBoolean>("IsVisible", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiDouble>("Min %", new MguiDouble(10)));
		attributes.add(new Attribute<MguiDouble>("Max %", new MguiDouble(25)));
		attributes.add(new Attribute<MguiDouble>("Spacing", new MguiDouble(5)));
		attributes.add(new Attribute<Color>("Colour", Color.GRAY));
		attributes.add(new Attribute<Stroke>("Style", new BasicStroke()));
		attributes.add(new Attribute<MguiBoolean>("IsMajor", new MguiBoolean(true)));
		attributes.add(new Attribute<MguiBoolean>("ShowLabels", new MguiBoolean(true)));
		//attributes.add(new Attribute<MguiInteger>("LabelSize", new MguiInteger(11)));
		attributes.add(new Attribute<Color>("LabelColour", Color.BLUE));
		attributes.add(new Attribute<Font>("LabelFont", new Font("Courier New", Font.PLAIN, 11)));
		
		attributes.addAttributeListener(this);
	}
	
	public void draw2D(Graphics2D g, DrawingEngine de){
		de.drawing_attributes.setUnion(attributes);
		de.drawGrid2D(g, this, getIsMajor());
	}
	
	public void setIsVisible(boolean b){
		attributes.getAttribute("IsVisible").setValue(new MguiBoolean(b));
	}
	
	public void setIsMajor(boolean b){
		attributes.getAttribute("IsMajor").setValue(new MguiBoolean(b));
	}
	
	public boolean getShowLabels(){
		return ((MguiBoolean)attributes.getValue("ShowLabels")).getTrue();
	}
	
	public void setShowLabels(boolean b){
		attributes.setValue("ShowLabels", new MguiBoolean(b));
	}
	
	public int getLabelSize(){
		return ((MguiInteger)attributes.getValue("LabelSize")).getInt();
	}
	
	public void setLabelSize(int b){
		attributes.setValue("ShowLabels", new MguiInteger(b));
	}
	
	public Font getLabelFont(){
		return (Font)attributes.getValue("LabelFont");
	}
	
	public Color getLabelColour(){
		return (Color)attributes.getValue("LabelColour");
	}
	
	public void setLabelColour(Color c){
		attributes.setValue("LabelColour", c);
	}
	
	public void setMin(double min){
		attributes.setValue("Min %", new MguiDouble(min));
	}
	
	public void setMax(double max){
		attributes.setValue("Max %", new MguiDouble(max));
	}
	
	public void setSpacing(double s){
		attributes.setValue("Spacing", new MguiDouble(s));
	}
	
	public void setColour(Color c){
		attributes.setValue("Colour", c);
	}
	
	public void setStyle(Stroke s){
		attributes.setValue("Style", s);
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public boolean getIsVisible(){
		return ((MguiBoolean)attributes.getValue("IsVisible")).getTrue();
	}
	
	public boolean getIsMajor(){
		return ((MguiBoolean)attributes.getValue("IsMajor")).getTrue();
	}
	
	public double getMin(){
		return ((MguiDouble)attributes.getValue("Min %")).getValue();
	}
	
	public double getMax(){
		return ((MguiDouble)attributes.getValue("Max %")).getValue();
	}
	
	public double getSpacing(){
		return ((MguiDouble)attributes.getValue("Spacing")).getValue();
	}
	
	public Color getColour(){
		return (Color)attributes.getValue("Colour");
	}
	
	public Stroke getStyle(){
		return (Stroke)attributes.getValue("Style");
	}
	
	public Attribute getAttribute(String attrName) {	
		return attributes.getAttribute(attrName);
	}

	public AttributeList getAttributes() {
		return attributes;
	}

	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}

	@Override
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		treeNode.add(attributes.issueTreeNode());
	}
	
	public void attributeUpdated(AttributeEvent e) {
		fireListeners();
	}
	
	public void addDisplayListener(DisplayListener listener){
		listeners.add(listener);
	}
	
	public void removeDisplayListener(DisplayListener listener){
		listeners.remove(listener);
	}
	
	protected void fireListeners(){
		ArrayList<DisplayListener> currents = new ArrayList<DisplayListener>(listeners);
		for (int i = 0; i < currents.size(); i++)
			currents.get(i).updateDisplay();
	}
	
	@Override
	public String toString(){
		return "Graphic2DGrid: " + getName();
	}
	
}