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

import java.io.File;
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
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLObject;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.resources.icons.IconObject;

/************************************************
 * Specifies a project directory, including its subdirectories and all prespecified data items that it contains. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ProjectDirectory extends AbstractInterfaceObject implements Comparable<ProjectDirectory>,
																		 IconObject,
																		 Cloneable,
																		 XMLObject{

	String path;
	HashMap<String, ProjectDirectory> subdirectories = new HashMap<String, ProjectDirectory>();
	HashMap<String, ProjectDataItem> data_items = new HashMap<String, ProjectDataItem>();
	
	public ProjectDirectory(String path){
		setPath(path);
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceProject.class.getResource("/mgui/resources/icons/folder_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/folder_20.png");
		return null;
	}
	
	public String getPath(){
		return path;
	}
	
	public void setPath(String path){
		this.path = replaceSeparators(path);
	}
	
	/******************************
	 * Replaces the separators (/ or \) with the file-system-specific separator
	 * 
	 * @param path
	 * @return
	 */
	protected String replaceSeparators(String path){
		String sep = "/";
		if (File.separator.equals(sep))
			sep = "\\";
		return path.replace(sep, File.separator);
	}
	
	public String getParentPath(){
		if (!path.contains(File.separator)) return "";
		return path.substring(0, path.lastIndexOf(File.separator));
	}
	
	public String getDirectoryName(){
		if (!path.contains(File.separator)) return path;
		return path.substring(path.lastIndexOf(File.separator) + 1);
	}
	
	public void addDataItem(ProjectDataItem item){
		data_items.put(item.getName(), item);
	}
	
	public ProjectDataItem getDataItem(String name){
		return data_items.get(name);
	}
	
	public void setFromDirectory(ProjectDirectory dir){
		setDataItems(dir.getDataItems());
	}
	
	public void setDataItems(ArrayList<ProjectDataItem> list){
		data_items.clear();
		for (int i = 0; i < list.size(); i++)
			data_items.put(list.get(i).getName(), list.get(i));
	}
	
	/*********************************************
	 * Returns a sorted list of this directory's prespecified data items
	 * 
	 * @return
	 */
	public ArrayList<ProjectDataItem> getDataItems(){
		ArrayList<String> keys = new ArrayList<String>(data_items.keySet());
		ArrayList<ProjectDataItem> items = new ArrayList<ProjectDataItem>();
		Collections.sort(keys);
		for (int i = 0; i < keys.size(); i++)
			items.add(data_items.get(keys.get(i)));
		return items;
	}
	
	/*********************************************
	 * Returns a sorted list of this directory's subdirectories
	 * 
	 * @return
	 */
	public ArrayList<ProjectDirectory> getSubdirectories(){
		ArrayList<String> subdirs = new ArrayList<String>(subdirectories.keySet());
		ArrayList<ProjectDirectory> p_dirs = new ArrayList<ProjectDirectory>();
		Collections.sort(subdirs);
		for (int i = 0; i < subdirs.size(); i++)
			p_dirs.add(subdirectories.get(subdirs.get(i)));
		return p_dirs;
	}
	
	public void addSubdirectory(ProjectDirectory dir){
		subdirectories.put(dir.getPath(), dir);
	}
	
	public ProjectDirectory getSubdirectory(String name){
		return subdirectories.get(name);
	}
	
	@Override
	public String getTreeLabel(){
		return getDirectoryName();
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode node){
		super.setTreeNode(node);
		
		//add data items
		ArrayList<String> items = new ArrayList<String>(data_items.keySet());
		Collections.sort(items);
		
		for (int i = 0; i < items.size(); i++)
			node.addChild(data_items.get(items.get(i)).issueTreeNode());
		
		//add sub-directories
		ArrayList<String> subdirs = new ArrayList<String>(subdirectories.keySet());
		Collections.sort(subdirs);
		
		for (int i = 0; i < subdirs.size(); i++)
			node.addChild(subdirectories.get(subdirs.get(i)).issueTreeNode());
		
	}
	
	public int compareTo(ProjectDirectory dir) {
		return this.getDirectoryName().compareTo(dir.getDirectoryName());
	}
	
	@Override
	public String toString(){
		return path;
	}
	
	@Override
	public Object clone(){
		ProjectDirectory directory = new ProjectDirectory(path);
		ArrayList<ProjectDirectory> subdirs = new ArrayList<ProjectDirectory>(subdirectories.values());
		for (int i = 0; i < subdirs.size(); i++)
			directory.addSubdirectory((ProjectDirectory)subdirs.get(i).clone());
		ArrayList<ProjectDataItem> items = new ArrayList<ProjectDataItem>(data_items.values());
		for (int i = 0; i < items.size(); i++)
			directory.addDataItem((ProjectDataItem)items.get(i).clone());
		return directory;
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
		return null;
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
		// TODO Auto-generated method stub
		
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