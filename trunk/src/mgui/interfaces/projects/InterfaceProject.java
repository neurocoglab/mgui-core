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

package mgui.interfaces.projects;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.ProgressUpdater;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.io.InterfaceFile;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.menus.InterfacePopupMenu;
import mgui.interfaces.menus.PopupMenuObject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.xml.XMLFunctions;
import mgui.interfaces.xml.XMLObject;
import mgui.interfaces.xml.XMLObject.XMLType;
import mgui.io.FileLoader;
import mgui.io.standard.xml.XMLOutputOptions;
import mgui.io.util.IoFunctions;
import mgui.numbers.MguiBoolean;
import mgui.resources.icons.IconObject;
import mgui.util.StringFunctions;

/************************************************************************
 * Represents a modelGUI project, which specifies an organizational structure for persistent data
 * related to a common project.
 * 
 * <p>See <a href="http://mgui.wikidot.com/projects">http://mgui.wikidot.com/projects</a> 
 * for details.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */

public class InterfaceProject extends AbstractInterfaceObject implements AttributeListener,
																		 AttributeObject,
																		 IconObject,
																		 PopupMenuObject,
																		 Cloneable,
																		 XMLObject{

	AttributeList attributes = new AttributeList();
	ArrayList<ProjectInstance> instances;
	ArrayList<ProjectDirectory> subdirs = new ArrayList<ProjectDirectory>();
	ArrayList<ProjectInstanceGroup> groups = new ArrayList<ProjectInstanceGroup>();
	
	public InterfaceProject(){
		
	}

	public InterfaceProject(String name, File root, String project_dir, String instance_dir, 
							boolean create_dirs) throws ProjectIOException{
		init();
		setName(name);
		if (!setRootDir(root)){
			if (!create_dirs)
				throw new ProjectIOException("Root directory '" + root.getAbsolutePath() + 
											 "' does not exist.");
			if (!root.mkdir())
				throw new ProjectIOException("Root directory '" + root.getAbsolutePath() + 
											 "' could not be created.");
			}
		
		if (!setProjectDir(project_dir)){
			if (!create_dirs)
				throw new ProjectIOException("Project directory '" + root.getAbsolutePath() +
											 File.separator + project_dir +
											 "' does not exist.");
			
			File dir = new File(root.getAbsolutePath() + File.separator + project_dir);
			if (!dir.mkdir())
				throw new ProjectIOException("Project directory '" + dir.getAbsolutePath() + 
											 "' could not be created.");
			}
		
		if (!setInstanceDir(instance_dir)){
			if (!create_dirs)
				throw new ProjectIOException("Instance directory '" + root.getAbsolutePath() +
											 File.separator + instance_dir +
											 "' does not exist.");
			File dir = new File(root.getAbsolutePath() + File.separator + instance_dir);
			if (!dir.mkdir())
				throw new ProjectIOException("Instance directory '" + dir.getAbsolutePath() + 
											 "' could not be created.");
			}
		
		setInstances();
		setSubdirs();
	}
	
	protected void init(){
		
		attributes.addAttributeListener(this);
		attributes.add(new Attribute<String>("Name", "No-name"));
		attributes.add(new Attribute<InterfaceFile>("RootDir", new InterfaceFile(InterfaceEnvironment.getCurrentDir(), 
																  InterfaceFile.getDirFilter())));
		attributes.add(new Attribute<String>("InstanceDir", "instances"));
		attributes.add(new Attribute<String>("ProjectDir", "project"));
		attributes.add(new Attribute<MguiBoolean>("HasInstances", new MguiBoolean(true)));
		attributes.add(new Attribute<String>("InstancePrefix", ""));
		attributes.add(new Attribute<String>("InstanceSuffix", ""));
		
	}
	
	public Icon getObjectIcon(){
		java.net.URL imgURL = InterfaceProject.class.getResource("/mgui/resources/icons/projects/project_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/projects/project_20.png");
		return null;
	}
	
	public String getProjectDir(){
		return (String)attributes.getValue("ProjectDir");
	}
	
	public boolean setProjectDir(String dir){
		
		File file = new File(getRootDir().getAbsolutePath() + File.separator + dir);
		if (file.exists()){
			attributes.setValue("ProjectDir", dir);
			return true;
			}
		
		return false;
		
	}
	
	public ProjectInstance getInstance(int index){
		return instances.get(index);
	}
	
	public ProjectInstance getInstance(String name){
		//Inefficient...
		for (int i = 0; i <  instances.size(); i++)
			if (instances.get(i).getName().equals(name))
				return instances.get(i);
		return null;
	}
	
	public String getInstanceDir(){
		return (String)attributes.getValue("InstanceDir");
	}
	
	public boolean setInstanceDir(String dir){
		File file = new File(getRootDir().getAbsolutePath() + File.separator + dir);
		if (file.exists()){
			attributes.setValue("InstanceDir", dir);
			return true;
		}
	
		return false;
	}
	
	public String getInstancePrefix(){
		return (String)attributes.getValue("InstancePrefix");
	}
	
	public void setInstancePrefix(String prefix){
		attributes.setValue("InstancePrefix", prefix);
	}
	
	public String getInstanceSuffix(){
		return (String)attributes.getValue("InstanceSuffix");
	}
	
	public void setInstanceSuffix(String suffix){
		attributes.setValue("InstanceSuffix", suffix);
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public boolean hasInstances(){
		return ((MguiBoolean)attributes.getValue("HasInstances")).getTrue();
	}
	
	public void hasInstances(boolean b){
		attributes.setValue("HasInstances", new MguiBoolean(b));
	}
	
	/******************************************
	 * Returns a (copied) array of the instances in this project 
	 * 
	 * @return
	 */
	public ArrayList<ProjectInstance> getInstances(){
		if (!hasInstances()) return null;
		return new ArrayList<ProjectInstance>(instances);
	}
	
	public ArrayList<ProjectDirectory> getSubdirs(){
		return subdirs;
	}
	
	public boolean setRootDir(File file){
		if (file == null || !file.isDirectory())
			return false;
		
		InterfaceFile i_file = (InterfaceFile)attributes.getValue("RootDir");
		i_file.setFile(file);
		attributes.setValue("RootDir", i_file);
		
		return true;
	}
	
	public File getRootDir(){
		return ((InterfaceFile)attributes.getValue("RootDir")).getFile();
	}
	
	public void attributeUpdated(AttributeEvent e){
		
	}
	
	@Override
	public String getTreeLabel(){
		return getName();
	}
	
	@Override
	public void setTreeNode(InterfaceTreeNode node){
		super.setTreeNode(node);
		
		node.addChild(attributes.issueTreeNode());
		
		InterfaceTreeNode project_data_node = new InterfaceTreeNode("Project Data");
		//TODO: populate project data node
		
		node.addChild(project_data_node);
		
		if (hasInstances()){
			InterfaceTreeNode instance_data_node = new InterfaceTreeNode("Instance Data");
			InterfaceTreeNode instances_node = new InterfaceTreeNode("Instances");
			InterfaceTreeNode instance_subdir_node = new InterfaceTreeNode("Subdirs");
			instance_data_node.add(instances_node);
			instance_data_node.add(instance_subdir_node);
			
			//TODO: replace with an ProjectInstance class?
			for (int i = 0; i < instances.size(); i++)
				instances_node.add(new InterfaceTreeNode(instances.get(i)));
			
			for (int i = 0; i < subdirs.size(); i++)
				instance_subdir_node.addChild(subdirs.get(i).issueTreeNode());
			node.addChild(instance_data_node);
			}
		
	}
	
	public ArrayList<String> getInstanceNames(){
		ArrayList<String> names = new ArrayList<String>(instances.size());
		for (int i = 0; i < instances.size(); i++)
			names.add(instances.get(i).getName());
		return names;
	}
	
	public boolean applyInstances(boolean remove){
		if (!hasInstances() || getRootDir() == null) return false;
		File dir = getAbsoluteInstanceDir();
		String[] list = dir.list(IoFunctions.getDirFilter());
		
		TreeSet<String> set = new TreeSet<String>(Arrays.asList(list));
		ArrayList<String> to_add = new ArrayList<String>();
		ArrayList<String> to_remove = new ArrayList<String>();
		
		ArrayList<String> names = getInstanceNames();
		
		for (int i = 0; i < names.size(); i++)
			if (!set.contains(names.get(i)))
				to_add.add(names.get(i));
		
		if (remove){
			set = new TreeSet<String>(names);
			for (int i = 0; i < list.length; i++)
				if (!set.contains(list[i]))
					to_remove.add(list[i]);
			}
		
		boolean success = true;
		for (int i = 0; i < to_add.size(); i++){
			File subdir = new File(dir.getAbsolutePath() + File.separator + to_add.get(i));
			if (!subdir.exists()) 
				success &= subdir.mkdir();
			}
		
		for (int i = 0; i < to_remove.size(); i++){
			File subdir = new File(dir.getAbsolutePath() + File.separator + to_remove.get(i));
			if (subdir.exists()) 
				success &= IoFunctions.deleteDir(subdir);
			}
		
		return success;
	}
	
	public boolean applySubdirs(boolean remove){
		if (getRootDir() == null) return false;
		//File root_directory = getRootDir();
		File instances_dir = getAbsoluteInstanceDir();
	
		boolean success = true;
		ArrayList<ProjectDirectory> subdir_list = getAllSubdirectories();
		TreeSet<ProjectDirectory> project_subdirs = new TreeSet<ProjectDirectory>(subdir_list);
		
		
		if (hasInstances()){
			//apply subdirs to all instances
			//since TreeSet is sorted
			for (int i = 0; i < instances.size(); i++){
				File instance_dir = new File(instances_dir.getAbsolutePath() + File.separator + instances.get(i).getName());
				if (instance_dir.exists()){
					TreeSet<String> existing_subdirs = new TreeSet<String>(IoFunctions.getSubdirs(instance_dir, true));
					for (int j = 0; j < subdir_list.size(); j++)
						if (!existing_subdirs.contains(subdir_list.get(j).getPath())){
							File f = new File(instance_dir.getAbsolutePath() + File.separator + subdir_list.get(j).getPath());
							success &= f.mkdir();
							}
					if (remove){
						for (String this_dir : existing_subdirs)
							if (!project_subdirs.contains(new ProjectDirectory(this_dir))){
								File f = new File(instances_dir.getAbsoluteFile() + File.separator + instances.get(i) + File.separator + this_dir);
								if (f.exists() && f.isDirectory()) 
									success &= IoFunctions.deleteDir(f);
								}
						}
					}
				}
		}else{
			//apply subdirs to root directory
			
			if (!instances_dir.exists()){
				success = instances_dir.mkdir();
				}
			
			if (success){
				TreeSet<String> existing_subdirs = new TreeSet<String>(IoFunctions.getSubdirs(instances_dir, true));
				for (int j = 0; j < subdirs.size(); j++)
					if (!existing_subdirs.contains(subdirs.get(j))){
						File f = new File(instances_dir.getAbsolutePath() + File.separator + subdirs.get(j));
						success &= f.mkdir();
						}
				if (remove){
					for (String this_dir : existing_subdirs)
						if (!project_subdirs.contains(this_dir)){
							File f = new File(instances_dir.getAbsolutePath() + File.separator + this_dir);
							if (f.exists() && f.isDirectory()) 
								success &= IoFunctions.deleteDir(f);
							}
					}
				}
				
			}
		
		return success;
		
	}
	
	/***********************************************
	 * Returns a list of all directories and their subdirectories contained in this project.
	 * 
	 * @return All subdirectories and their subdirectories, etc., contained in this project
	 */
	public ArrayList<ProjectDirectory> getAllSubdirectories(){
		return getAllSubdirectories(subdirs);
	}
	
	/***********************************************
	 * Returns a list of all directories and their subdirectories contained in this list of directories. Can be used recursively.
	 * 
	 * @param dirs The list of directories
	 * @return All subdirectories and their subdirectories, etc., contained in this list
	 */
	protected ArrayList<ProjectDirectory> getAllSubdirectories(ArrayList<ProjectDirectory> dirs){
		ArrayList<ProjectDirectory> list = new ArrayList<ProjectDirectory>(dirs);
		for (int i = 0; i < dirs.size(); i++)
			list.addAll(getAllSubdirectories(dirs.get(i).getSubdirectories())); 
		return list;
	}
	
	public boolean updateFileSystem(boolean remove){
		return applyInstances(remove) && applySubdirs(remove);
	}
	
	public void setInstancesFromNames(ArrayList<String> names){
		if (!hasInstances()) return;
		this.instances = new ArrayList<ProjectInstance>();
		for (int i = 0; i < names.size(); i++)
			instances.add(new ProjectInstance(names.get(i)));
		Collections.sort(instances);
	}
	
	public void setInstances(ArrayList<ProjectInstance> instances){
		if (!hasInstances()) return;
		this.instances = instances;
		Collections.sort(this.instances);
	}
	
	public void setInstances(){
		if (!hasInstances() || getRootDir() == null) return;
		File directory = getAbsoluteInstanceDir();
		String[] list = directory.list(IoFunctions.getDirFilter());
		instances = new ArrayList<ProjectInstance>();
		
		if (list != null){
			for (int i = 0; i < list.length; i++)
				instances.add(new ProjectInstance(list[i]));
			}
		
		Collections.sort(instances);
	}
	
	protected File getAbsoluteInstanceDir(){
		return new File(getRootDir().getAbsolutePath() + File.separator + getInstanceDir());
	}
	
	public void setSubdirs(){
		if (getRootDir() == null) return;
		if (hasInstances() && instances == null) setInstances();
		String sep = File.separator;
		TreeSet<String> set = new TreeSet<String>();
		File instance_directory = getAbsoluteInstanceDir();
		
		if (hasInstances()){
			for (int i = 0; i < instances.size(); i++){
				File subdir = new File(instance_directory.getAbsolutePath() + sep + instances.get(i).getName());
				ArrayList<String> subdirs = IoFunctions.getSubdirs(subdir, true);
				set.addAll(subdirs);
				}
		}else{
			ArrayList<String> subdirs = IoFunctions.getSubdirs(instance_directory, true);
			set.addAll(subdirs);
			}
		
		setSubdirectoriesFromPaths(new ArrayList<String>(set));
	}
	
	public void setSubdirectoriesFromPaths(ArrayList<String> paths){
		ArrayList<ProjectDirectory> dirs = new ArrayList<ProjectDirectory>();
		for (int i = 0; i < paths.size(); i++)
			dirs.add(new ProjectDirectory(paths.get(i)));
		setSubdirectories(dirs);
	}
	
	/****************************************************
	 * Sets the subdirectory list from a list of paths.
	 * 
	 * @param paths
	 */
	public void setSubdirectories(ArrayList<ProjectDirectory> paths){
		
		ArrayList<ProjectDirectory> sorted = new ArrayList<ProjectDirectory>(paths);
			
		//sort directories by tree hierarchy
		Collections.sort(sorted, new Comparator<ProjectDirectory>(){
			public int compare(ProjectDirectory path1, ProjectDirectory path2){
				int c1 = StringFunctions.countOccurrences(path1.getPath(), File.separator);
				int c2 = StringFunctions.countOccurrences(path2.getPath(), File.separator);
				if (c1 < c2) return -1;
				if (c1 > c2) return 1;
				return 0;
			}
		});
		
		HashMap<String, ProjectDirectory> dirs = new HashMap<String, ProjectDirectory>();
		
		//now we can always add higher paths first
		for (int i = 0; i < sorted.size(); i++){
			ProjectDirectory path = sorted.get(i);
			String p = IoFunctions.getParentPath(path.getPath());
			ProjectDirectory dir = dirs.get(p);
			if (dir == null){
				dirs.put(path.getPath(), path);
			}else{
				dir.addSubdirectory(path);
				}
			}
		
		subdirs = new ArrayList<ProjectDirectory>(dirs.values());
		Collections.sort(subdirs);
		
	}
	
	/*****************************************
	 * Sets this project from another project. Destroys the current state of the project.
	 * 
	 * @param project
	 */
	public void setFromProject(InterfaceProject project){
		this.setAttributes((AttributeList)project.getAttributes().clone());
		subdirs = new ArrayList<ProjectDirectory>();
		ArrayList<ProjectDirectory> new_dirs = project.getSubdirs();
		for (int i = 0; i < new_dirs.size(); i++)
			subdirs.add((ProjectDirectory)new_dirs.get(i).clone());
		instances = new ArrayList<ProjectInstance>();
		instances.addAll(project.getInstances());
		updateTreeNodes();
	}
	
	@Override
	public Attribute getAttribute(String attrName) {
		return attributes.getAttribute(attrName);
	}

	@Override
	public AttributeList getAttributes() {
		return attributes;
	}

	@Override
	public void setAttribute(String attrName, Object newValue) {
		attributes.setValue(attrName, newValue);
	}

	@Override
	public void setAttributes(AttributeList thisList) {
		attributes = thisList;
	}
	
	/*********************************************************
	 * Attempts to load a data instance with the given data item specification.
	 * 
	 * @param instance
	 * @param item
	 * @param progress
	 * @throws ProjectIOException If load operation failed
	 * @return
	 */
	public Object loadDataInstance(String instance, ProjectDataItem item, ProgressUpdater progress) throws IOException{
		return loadDataInstance(instance, (Integer)null, item, progress);
	}
	
	/*********************************************************
	 * Attempts to load a data instance with the given data item specification.
	 * 
	 * @param instance
	 * @param series
	 * @param item
	 * @param progress
	 * @throws ProjectIOException If load operation failed
	 * @return
	 */
	public Object loadDataInstance(String instance, Integer series, ProjectDataItem item, ProgressUpdater progress) throws IOException{
		
		FileLoader loader = item.getFileLoader();
		if (loader == null)
			throw new ProjectIOException("InterfaceProject.loadDataInstance: Data Item has no loader specified!");
		
		File file = new File(item.getFilenameForInstance(instance));
		InterfaceIOType io_type = loader.getIOType();
		loader.setFile(file);
		Object obj = loader.loadObject(progress, io_type.getOptionsInstance());
		
		return obj;
		
	}
	
	@Override
	public InterfacePopupMenu getPopupMenu() {
		
		InterfacePopupMenu menu = new InterfacePopupMenu(this);
		menu.addMenuItem(new JMenuItem("Project '" + getName() + "'", getObjectIcon()));
		
		menu.add(new JSeparator(), 1);
		menu.add(new JSeparator(), 1);
				
		menu.addMenuItem(new JMenuItem("Edit.."));
		
		return menu;
	}

	@Override
	public void handlePopupEvent(ActionEvent e) {
		
		InterfaceProject project = InterfaceProjectDialogBox.showDialog(this);
		if (project == null) return;
		setFromProject(project);
		
	}

	@Override
	public void showPopupMenu(MouseEvent e) {
		InterfacePopupMenu menu = getPopupMenu();
		menu.show(e);
	}
	
	@Override
	public Object clone(){
		InterfaceProject project = new InterfaceProject();
		project.setFromProject(this);
		return project;
	}
	
	// ***************************** XML STUFF *****************************
	
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
		
		
	}

	@Override
	public void handleXMLElementEnd(String localName) throws SAXException {
		
		
	}

	@Override
	public void handleXMLString(String s) throws SAXException {
		
		
	}

	@Override
	public String getLocalName() {
		return "InterfaceProject";
	}

	@Override
	public void writeXML(int tab, Writer writer, XMLOutputOptions options,
						 ProgressUpdater progress_bar) throws IOException {
		
		String _tab = XMLFunctions.getTab(tab);
		String _tab2 = XMLFunctions.getTab(tab+1);
		
		String root_dir = this.getRootDir().getAbsolutePath();
		String instance_dir = this.getInstanceDir().substring(root_dir.length()+1);
		String project_dir = this.getProjectDir().substring(root_dir.length()+1);
		
		// Header
		String xml = _tab + "<" + getLocalName() + "\n" +
					 _tab2 + "name='" + this.getName() + "'\n" +
					 _tab2 + "root='" + root_dir + "'\n" +
					 _tab2 + "instance_dir='" + instance_dir + "'\n" +
					 _tab2 + "project_dir='" + project_dir + "'\n" +
					 _tab2 + "instance_prefix='" + this.getInstancePrefix() + "'\n" +
					 _tab2 + "instance_suffix='" + this.getInstanceSuffix() + "'\n" +
					 _tab + ">\n";
		
		// Subdirectories
		xml = xml + _tab2 + "<Subdirectories>\n";
		
		for (int i = 0; i < subdirs.size(); i++){
			subdirs.get(i).writeXML(tab+2, writer, options, progress_bar);
			}
		xml = xml + _tab2 + "</Subdirectories>\n";
		
		// Instances
		xml = xml + _tab2 + "<Instances>\n";
		
		for (int i = 0; i < instances.size(); i++){
			ProjectInstance instance = instances.get(i);
			xml = xml + instance.getXML(tab+2) + "\n";
			}
		xml = xml + _tab2 + "</Instances>\n";
		
		xml = xml + _tab + "</" + getLocalName() + ">";
		
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