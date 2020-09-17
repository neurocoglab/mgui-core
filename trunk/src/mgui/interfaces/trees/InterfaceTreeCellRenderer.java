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

package mgui.interfaces.trees;

import java.awt.Component;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.InputMap;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import mgui.interfaces.InterfaceObject;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.tree.AttributeTreeCellRenderer;

/*********************************************
 * Renderer for instances of {@link InterfaceObject}. Holds a map of renderers which
 * associates a given renderer with a given class of user object.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceTreeCellRenderer extends DefaultTreeCellRenderer {

	public HashMap<Class, TreeCellRenderer> renderers = new HashMap<Class, TreeCellRenderer>();
	TreeCellRenderer default_renderer;
	
	public InterfaceTreeCellRenderer(){
		setDefaultRenderer(new AttributeTreeCellRenderer());
	}
	
	public void setDefaultRenderer(TreeCellRenderer r){
		default_renderer = r;
		//defRenderer.setInputMap(WHEN_FOCUSED, getInputMap());
	}
	
	public void addRenderer(Class c, TreeCellRenderer r){
		renderers.put(c, r);
	//	//r.setInputMap(WHEN_FOCUSED, getInputMap());
	}
	
	public void removeRenderer(DefaultTreeCellRenderer r){
		renderers.remove(r);
	}
	
	//because of more idiotic final methods.. wtf?
	public void setInputMap2(int condition, InputMap map){
		setInputMap(condition, map);
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree,
											      Object value,
											      boolean sel,
											      boolean expanded,
											      boolean leaf,
											      int row,
											      boolean hasFocus){
		
		Iterator itr = renderers.keySet().iterator();
		TreeCellRenderer renderer = null;
		Class<?> c;
		
		if (value instanceof InterfaceTreeNode){
			value = ((InterfaceTreeNode)value).getUserObject();
			}
		
		if (value instanceof Attribute){
			int a = 0;
		}
		
		while (itr.hasNext()){
			c = (Class<?>)itr.next();
			if (c.isInstance(value)){
				renderer = renderers.get(c);
				break;
				}
			}
	
		if (renderer != null) return renderer.getTreeCellRendererComponent(tree, value, sel, 
																		   expanded, leaf, 
																		   row, hasFocus);
		
		if (default_renderer != null) return default_renderer.getTreeCellRendererComponent(tree, value, sel, 
																		   expanded, leaf, 
																		   row, hasFocus);
		
		return null;
	}
	
}