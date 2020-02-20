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

package mgui.interfaces.trees;

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.AbstractCellEditor;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;

import mgui.interfaces.attributes.tree.AttributeTreeCellEditor;


public class InterfaceTreeCellEditor extends AbstractCellEditor implements TreeCellEditor {

	public HashMap<Class, TreeCellEditor> editors = new HashMap<Class, TreeCellEditor>();
	TreeCellEditor defEditor; // = new AttributeTreeCellEditor();
	TreeCellEditor currentEditor;
	
	public InterfaceTreeCellEditor(){
		setDefaultEditor(new AttributeTreeCellEditor());
	}
	
	public void setDefaultEditor(TreeCellEditor e){
		defEditor = e;
	}
	
	public void addEditor(Class c, TreeCellEditor e){
		//editors.put(c, new DefaultTreeCellEditor(tree, renderer, e ));
		editors.put(c, e);
	}
	
	public void removeEditor(TreeCellEditor e){
		editors.remove(e);
	}
	
	public Component getTreeCellEditorComponent(JTree tree,
									            Object value,
									            boolean isSelected,
									            boolean expanded,
									            boolean leaf,
									            int row){
		
		TreeCellEditor editor = getEditorForObject(value);
		currentEditor = editor;
		
		if (editor != null)
			return editor.getTreeCellEditorComponent(tree, value, isSelected, 
													 expanded, leaf, row);
		return null;
	}

	public Object getCellEditorValue() {
		if (currentEditor == null) return null;
		return currentEditor.getCellEditorValue();
	}

	protected TreeCellEditor getEditorForObject(Object value){
		//return the appropriate editor for this class
		Iterator itr = editors.keySet().iterator();
		TreeCellEditor editor = null;
		Class c;
		
		while (itr.hasNext()){
			c = (Class)itr.next();
			if (c.isInstance(value)){
				editor = editors.get(c);
				break;
				}
			}
		if (editor == null) editor = defEditor;
		return editor;
	}
	
}