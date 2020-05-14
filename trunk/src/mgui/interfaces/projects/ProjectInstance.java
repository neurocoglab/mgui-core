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

package mgui.interfaces.projects;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;
import mgui.resources.icons.NamedIcon;

/****************************************************
 * Specifies an element instance in a project (e.g., a subject). The instance can be minimally
 * specified by a name and a type, but other implementations can expand its attributes.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ProjectInstance extends AbstractInterfaceObject 
									implements IconObject,
											   AttributeListener,
											   Comparable<ProjectInstance>,
											   XMLObject{

	

	String icon_type = "Instance";
	
	static HashMap<String, NamedIcon> icon_types;
	protected AttributeList attributes;
	
	public ProjectInstance(String name){
		this(name, "Instance");
	}
	
	public ProjectInstance(String name, String type){
		init();
		setName(name);
		setType(type);
	}
	
	private void init(){
		
		attributes = new AttributeList();
		attributes.add(new Attribute<String>("Name", "No name"));
		
		HashMap<String, NamedIcon> icon_map = getIconTypes();
		ArrayList<NamedIcon> icons = new ArrayList<NamedIcon>(icon_map.values());
		Collections.sort(icons);
		AttributeSelection<NamedIcon> attr = new AttributeSelection<NamedIcon>("Type", icons, NamedIcon.class, icons.get(0));
		attributes.add(attr);
		
		attributes.addAttributeListener(this);
		
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public void setType(String type){
		attributes.setValue("Type", icon_types.get(type));
	}
	
	public String getType(){
		return (String)attributes.getValue("Type");
	}
	
	@Override
	public void attributeUpdated(AttributeEvent e) {
		
		Attribute attribute = e.getAttribute();
		
		if (attribute.getName().equals("Type")){
			//do what?
			
			}
		
	}
	
	@Override
	public String toString(){
		return "Project Instance: " + getName();
	}
	
	@Override
	public String getTreeLabel(){
		return getName();
	}
	
	@Override
	public int compareTo(ProjectInstance instance) {
		return getName().compareTo(instance.getName());
	}
	
	public static HashMap<String, NamedIcon> getIconTypes(){
		if (icon_types != null) return icon_types;
		
		icon_types = new HashMap<String, NamedIcon>();
		icon_types.put("Instance", new NamedIcon("Instance", getIcon("Instance")));
		icon_types.put("Subject", new NamedIcon("Subject", getIcon("Subject")));
		return icon_types;
	}
	
	public Icon getObjectIcon(){
		
		AttributeSelection selection = (AttributeSelection)attributes.getAttribute("Type");
		NamedIcon n_icon = (NamedIcon)selection.getValue();
		if (n_icon == null) return getInstanceIcon();
		
		Icon icon = n_icon.getObjectIcon();
		if (icon != null) return icon;
		
		return getInstanceIcon();
		
	}
	
	protected static Icon getIcon(String name){
		
		if (name.toLowerCase().equals("instance"))
			return getInstanceIcon();
		
		if (name.toLowerCase().equals("subject"))
			return getSubjectIcon();
		
		return getInstanceIcon();
	}
	
	public static Icon getInstanceIcon(){
		java.net.URL imgURL = ProjectInstance.class.getResource("/mgui/resources/icons/projects/project_instance_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/projects/project_instance_20.png");
		return null;
	}
	
	public static Icon getSubjectIcon(){
		java.net.URL imgURL = ProjectInstance.class.getResource("/mgui/resources/icons/projects/project_subject_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/projects/project_subject_20.png");
		return null;
	}
	
	
	
	@Override
	public String getDTD() {
		return null;
	}

	@Override
	public String getXMLSchema() {
		return null;
	}

	@Override
	public String getXML() {
		return getXML(0);
	}

	@Override
	public String getXML(int tab) {
		return XMLFunctions.getTab(tab) + "<" + getLocalName() + " type='" + this.getType() + "'" + 
																 " name='" + this.getName() + "' />";
	}

	@Override
	public void handleXMLElementStart(String localName, Attributes attributes,
			XMLType type) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLElementEnd(String localName) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLocalName() {
		return "ProjectInstance";
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options,
			ProgressUpdater progress_bar) throws IOException {
		writer.write(getXML());
	}

	@Override
	public void writeXML(int tab, Writer writer, ProgressUpdater progress_bar)
						 throws IOException {
		writeXML(tab,writer,null,progress_bar);
	}

	@Override
	public void writeXML(int tab, Writer writer) throws IOException {
		writeXML(tab, writer, null, null);
	}

	@Override
	public String getShortXML(int tab) {
		return null;
	}
	
}