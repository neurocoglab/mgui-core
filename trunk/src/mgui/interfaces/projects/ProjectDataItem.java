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

import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import mgui.interfaces.AbstractInterfaceObject;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeEvent;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeListener;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.io.InterfaceIOType;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.io.FileLoader;
import mgui.io.FileWriter;
import mgui.numbers.MguiInteger;
import mgui.resources.icons.IconObject;

/***************************************************************
 * Specifies a data item in a modelGUI project, including its name, its I/O classes, and its filename
 * form (a pattern identifying the location or URL of the item).
 * 
 * <p> TODO: Also specify options for the item
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ProjectDataItem extends AbstractInterfaceObject implements AttributeListener,
																		IconObject,
																		Cloneable{

	AttributeList attributes = new AttributeList();

	
	public ProjectDataItem(String name, String filename_form){
		init();
		setName(name);
		setFilenameForm(filename_form);
	}
	
	protected void init(){
		
		attributes.addAttributeListener(this);
		attributes.add(new Attribute<String>("Name", "No-name"));
		attributes.add(new Attribute<String>("FilenameForm", ""));
	
		ArrayList<InterfaceIOType> types = new ArrayList<InterfaceIOType>(InterfaceEnvironment.getIOTypes().values());
		ArrayList<InterfaceIOType> loaders = new ArrayList<InterfaceIOType>();
		ArrayList<InterfaceIOType> writers = new ArrayList<InterfaceIOType>();
		
		for (int i = 0; i < types.size(); i++){
			if (types.get(i).getType() == InterfaceIOType.TYPE_INPUT)
				loaders.add(types.get(i));
			if (types.get(i).getType() == InterfaceIOType.TYPE_OUTPUT)
				writers.add(types.get(i));
			}
		
		AttributeSelection<InterfaceIOType> attr = new AttributeSelection<InterfaceIOType>("FileLoader", loaders, InterfaceIOType.class, InterfaceEnvironment.getIOType("ShapeModel3D_in"));
		attributes.add(attr);
		attr = new AttributeSelection<InterfaceIOType>("FileWriter", writers, InterfaceIOType.class, InterfaceEnvironment.getIOType("ShapeModel3D_out"));
		attributes.add(attr);
		
	}
	
	public Icon getObjectIcon(){
		
		java.net.URL imgURL = ProjectDataItem.class.getResource("/mgui/resources/icons/projects/project_data_item_20.png");
		if (imgURL != null)
			return new ImageIcon(imgURL);
		else
			InterfaceSession.log("Cannot find resource: /mgui/resources/icons/project_data_item_20.png");
		return null;
		
	}
	
	/*********************************************
	 * Constructs a filename from the given parameters and the file name form. 
	 * 
	 * @param instance Instance ID, or <code>null</code>
	 * @return
	 */
	public String getFilenameForInstance(String instance){
		return getFilenameForInstance(instance, (Integer)null);
	}
	
	/*********************************************
	 * Constructs a filename from the given parameters and the file name form. 
	 * 
	 * @param instance Instance ID, or <code>null</code>
	 * @param series Series number, or <code>null</code> if none
	 * @return
	 */
	public String getFilenameForInstance(String instance, Integer series){
		String filename = this.getFilenameForm();
		filename = filename.replace("{instance}", instance);
		if (series != null)
			filename = getSeriesReplaced(filename, series);
		return filename;
	}
	
	protected String getSeriesReplaced(String filename, int series){
		
		int a = filename.indexOf("{series");
		if (a < 0) return filename;
		
		int b = filename.indexOf("}");
		String s = null;
		if (b > a + 7){
			String end = "";
			if (b + 1 < filename.length())
				end = filename.substring(b + 1);
			String pattern = filename.substring(a + 7, b);
			s = filename.substring(0, a) + MguiInteger.getString(series, pattern) + end;
		}else{
			s = filename.replace("{series}", MguiInteger.getString(series, "0"));
			}
		
		return getSeriesReplaced(s, series);
	}
	
	@Override
	public void setName(String name){
		attributes.setValue("Name", name);
	}
	
	@Override
	public String getName(){
		return (String)attributes.getValue("Name");
	}
	
	public void setFileLoader(FileLoader loader){
		attributes.setValue("FileLoader", InterfaceEnvironment.getIOTypeForInstance(loader));
	}
	
	public InterfaceIOType getLoaderIOType(){
		return (InterfaceIOType)attributes.getValue("FileLoader");
	}
	
	public FileLoader getFileLoader(){
		InterfaceIOType type = (InterfaceIOType)attributes.getValue("FileLoader");
		if (type == null) return null;
		return (FileLoader)type.getIOInstance();
	}
	
	public InterfaceIOType getWriterIOType(){
		return (InterfaceIOType)attributes.getValue("FileWriter");
	}
	
	public void setFileWriter(FileWriter writer){
		attributes.setValue("FileWriter", InterfaceEnvironment.getIOTypeForInstance(writer));
	}
	
	public FileWriter getFileWriter(){
		InterfaceIOType type = (InterfaceIOType)attributes.getValue("FileWriter");
		if (type == null) return null;
		return (FileWriter)type.getIOInstance();
	}
	
	public void setFilenameForm(String form){
		attributes.setValue("FilenameForm", form);
	}
	
	public String getFilenameForm(){
		return (String)attributes.getValue("FilenameForm");
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
	}
	
	@Override
	public Object clone(){
		ProjectDataItem item = new ProjectDataItem(getName(), getFilenameForm());
		//item.setFilenameForm(this.getFilenameForm());
		item.setFileLoader(this.getFileLoader());
		item.setFileWriter(this.getFileWriter());
		return item;
	}
	
}