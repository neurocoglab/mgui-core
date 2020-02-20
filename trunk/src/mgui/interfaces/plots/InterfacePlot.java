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

package mgui.interfaces.plots;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.graphics.util.MouseRelayListener;
import mgui.interfaces.io.DataInputStream;
import mgui.interfaces.io.InterfaceDataSensor;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.trees.TreeObject;
import mgui.numbers.MguiNumber;
import mgui.resources.icons.IconObject;

/****************************************************
 * Represents an abstract plot object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * 
 * @param <T> Number type for this plot.
 */
public abstract class InterfacePlot<T extends MguiNumber> extends InterfacePanel implements InterfaceDataSensor<T>,
																							  AttributeObject,
																							  TreeObject,
																							  InterfaceObject,
																							  AttributeListener,
																							  IconObject{

	protected Icon icon;
	protected boolean isDestroyed = false;
	protected ArrayList<DataInputStream<T>> inputs = new ArrayList<DataInputStream<T>>();
	protected InterfaceTreeNode treeNode;
	protected ArrayList<MouseRelayListener> relay_listeners = new ArrayList<MouseRelayListener>();
	
	protected void init(){
		this.setLayout(new BorderLayout());
		
		attributes.add(new Attribute<String>("Title", "No title"));
		
		attributes.addAttributeListener(this);
	}
	
	@Override
	public Icon getObjectIcon(){
		if (icon == null) setIcon();
		return icon;
	}
	
	protected void setIcon(){
		java.net.URL imgURL = InterfacePlot.class.getResource("/mgui/resources/icons/plot_panel_20.png");
		if (imgURL != null)
			icon = new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: mgui/resources/icons/plot_panel_20.png");
	}
	
	public String getTitle(){
		return (String)attributes.getValue("Title");
	}
	
	public void setTitle(String title){
		attributes.setValue("Title", title);
	}
	
	public String getTreeLabel(){
		return toString();
	}
	
	/*********************************************
	 * Subclasses can use this method to supply a dialog box which defines
	 * this plot object.
	 * 
	 * @return
	 */
	public InterfacePlotDialog<?> getPlotDialog(){
		//TODO: show generic plot dialog
		return null;
		
	}
	
	public void addMouseRelayListener(MouseRelayListener listener){
		relay_listeners.add(listener);
	}
	
	public void removeMouseRelayListener(MouseRelayListener listener){
		relay_listeners.remove(listener);
	}
	
	/******************************************************
	 * Returns an instance of <code>InterfacePlotOptions</code> with which to specify 
	 * this plot object. Subclasses should override to provide specific option instances.
	 * 
	 * @return an instance of <code>InterfacePlotOptions</code>, or <code>null</code> if
	 * 		   this class does not provide one.
	 */
	public InterfacePlotOptions<?> getOptionsInstance(){
		return null;
	}
	
	/******************************************************
	 * Sets up this plot object from <code>options</code>. Subclasses should override 
	 * this method to set up the plot.  
	 * 
	 * @param options
	 */
	public void setFromOptions(InterfacePlotOptions<?> options){
		
	}
	
	//methods which override this should call super.dataInputEvent()
	public void dataInputEvent(DataInputStream<T> s) {
		//clean up if destroyed
		if (isDestroyed){
			s.removeInputStreamListener(this);
			return;
			}
		updateUI();
	}
	
	public void addInputStream(DataInputStream<T> s){
		try{
			setInputStream(s);
		}catch (PlotInputException e){
			e.printStackTrace();
			}
	}
	
	protected void setInputStream(DataInputStream<T> s) throws PlotInputException {
		inputs.add(s);
		s.addInputStreamListener(this);
	}
	
	public ArrayList<DataInputStream<T>> getInputStreams(){
		return this.inputs;
	}
	
	public void removeInputStream(DataInputStream<T> s){
		inputs.remove(s);
		s.removeInputStreamListener(this);
	}
	
	public Attribute<?> getAttribute(String attrName) {	
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
	
	public void setTreeNode(InterfaceTreeNode treeNode){
		super.setTreeNode(treeNode);
		
		treeNode.addChild(attributes.issueTreeNode());
		
	}

	public void attributeUpdated(AttributeEvent e) {
		//attributes.setValue(e.getAttribute());
		updateUI();
	}
	
	public void destroy() {
		isDestroyed = true;
	}

	
	public boolean isDestroyed(){
		return isDestroyed;
	}
	
	public String toString(){
		return getPlotType() + ": " + getTitle();
	}
	
	public String getPlotType(){
		return "Generic Plot";
	}
	
}