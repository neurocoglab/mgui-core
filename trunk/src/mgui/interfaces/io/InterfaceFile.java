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

package mgui.interfaces.io;

import java.io.File;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.util.InterfaceFunctions;

public class InterfaceFile implements InterfaceObject {

	protected File file;
	protected FileFilter filter;
	
	protected boolean destroyed = false;
	ArrayList<InterfaceTreeNode> tree_nodes = new ArrayList<InterfaceTreeNode>();
	
	public InterfaceFile(File file){
		this.file = file;
	}
	
	public InterfaceFile(File file, FileFilter filter){
		this.file = file;
		this.filter = filter;
	}
	
	public void setFile(File file){
		this.file = file;
	}
	
	public File getFile(){
		return file;
	}
	
	public void setFileFilter(FileFilter filter){
		this.filter = filter;
	}
	
	public FileFilter getFileFilter(){
		return filter;
	}
	
	@Override
	public void clean(){
		InterfaceFunctions.cleanInterfaceObject(this);
	}
	
	@Override
	public void destroy() {
		for (InterfaceTreeNode node : tree_nodes)
			node.destroy();
		destroyed = true;
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public void setName(String name) {
		
	}

	@Override
	public String getTreeLabel() {
		return getName();
	}

	@Override
	public InterfaceTreeNode issueTreeNode() {
		InterfaceTreeNode node = new InterfaceTreeNode(this);
		tree_nodes.add(node);
		setTreeNode(node);
		return node;
	}

	@Override
	public void setTreeNode(InterfaceTreeNode node) {
		
	}

	public static FileFilter getDirFilter(){
		return new FileFilter(){
			 @Override
			public boolean accept(File f){
				 return f.isDirectory();
			 }
			 
			 @Override
			public String getDescription(){
				 return "Directories";
			 }
		};
	}
	
	public static FileFilter getFilter(final String[] ext, final String description){
		
		return new FileFilter(){
			 @Override
			public boolean accept(File f){
				 if (!f.isFile()) return false;
				 String path = f.getAbsolutePath();
				 String end = "";
				 int e = path.lastIndexOf(".");
				 if (e > 0)
					 end = path.substring(e + 1);
				 for (int i = 0; i < ext.length; i++)
					 if (end.equals(ext)) return true;
				 return false;
			 }
			 
			 @Override
			public String getDescription(){
				 return description;
			 }
		};
	}
	
}