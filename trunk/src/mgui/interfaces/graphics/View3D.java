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

import java.io.IOException;
import java.io.Writer;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.maps.Camera3D;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.shapes.util.ShapeEvent;
import mgui.interfaces.shapes.util.ShapeListener;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;

import org.xml.sax.Attributes;

/****************************************************
 * Represents a specific 3D view, defined by a frozen {@link Camera3D} object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class View3D extends AbstractInterfaceObject implements Comparable<View3D>,
															   XMLObject,
															   ShapeListener,
															   IconObject{

	AttributeList attributes = new AttributeList();
	public Camera3D camera = new Camera3D();
	public boolean isEditable = true;
	
	public View3D(){
		init();
	}
	
	public View3D(String name){
		init();
		setName(name);
	}
	
	public View3D(Camera3D camera){
		init();
		setViewFromCamera(camera);
	}
	
	public View3D(String name, Camera3D camera){
		this(name, camera, true);
	}
	
	public View3D(String name, Camera3D camera, boolean editable){
		init();
		setName(name);
		setViewFromCamera(camera);
		isEditable = editable;
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = ShapeSet3DInt.class.getResource("/mgui/resources/icons/view_3d_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/view_3d_20.png");
		return null;
	}
	
	void init(){
		attributes.add(new Attribute("Name", "no name"));
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	public boolean setViewFromCamera(Camera3D camera){
		if (!isEditable) return false;
		this.camera.setFromCamera(camera);
		return true;
	}
	
	public Camera3D getCameraClone(){
		Camera3D c = new Camera3D();
		c.setFromCamera(camera);
		return c;
	}
	
	@Override
	public String toString(){
		return "View3D: " + getName();
	}
	
	public int compareTo(View3D o) {
		return getName().compareTo(o.getName());
	}

	public String getDTD(){
		//TODO: retrieve from file
		return "";
	}
	
	public String getXMLSchema(){
		//TODO: retrieve from file
		return "";
	}
	
	public String getXML(int tab){
		//get schema or dtd
		
		String _tab = XMLFunctions.getTab(tab);
		
		String xml = "\n" + _tab + "<View3D";
		xml = xml + " name = '" + getName() + "'";		//name
		xml = xml + ">\n";
		
		xml = xml + camera.getXML(tab + 1);
		
		xml = xml + "\n" + _tab + "</View3D>";
		return xml;
	}
	
	public String getXML(){
		return getXML(0);
	}
	
	public String getShortXML(int tab){
		return XMLFunctions.getTab(tab) + "<View3D name = '" + getName() + "' />\n";
	}
	
	public void handleXMLElementStart(String localName, Attributes attributes, XMLType type){
		
		if (localName.equals(getLocalName())){
			setName(attributes.getValue("name"));
			return;
			}
		
		if (localName.equals(camera.getLocalName())){
			camera.handleXMLElementStart(localName, attributes, null);
			return;
			}
		
	}
	
	public void handleXMLElementEnd(String localName){
		
	}
	
	public void handleXMLString(String s){
		
	}
	
	public String getLocalName(){
		return "View3D";
	}
	
	public static XMLObject getXMLInstance(Attributes attributes){
		return new View3D(attributes.getValue("name"));
	}
	
	@Override
	public void writeXML(int tab, Writer writer) throws IOException{
		writeXML(tab, writer, null);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar) throws IOException{
		this.writeXML(tab, writer, new XMLOutputOptions(), progress_bar);
	}
	
	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options, ProgressUpdater progress_bar) throws IOException{
		//XML string should be small, so get it directly
		writer.write(getXML(tab));
	}
	
	public void shapeUpdated(ShapeEvent e){
		
		if (!(e.getShape() instanceof InterfaceView3DObject)) return;
		
		View3D v = ((InterfaceView3DObject)e.getShape()).getView3D();
		camera.setFromCamera(v.camera);
		
		
	}
	
}