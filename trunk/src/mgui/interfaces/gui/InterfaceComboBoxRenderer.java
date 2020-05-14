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

package mgui.interfaces.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import mgui.interfaces.InterfaceObject;
import mgui.resources.icons.IconObject;

import org.apache.commons.collections15.Transformer;

/*****************************************
 * Renderer for {@link InterfaceComboBox}. Displays icons and tree text for instances of
 * {@link InterfaceObject}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class InterfaceComboBoxRenderer extends BasicComboBoxRenderer{
	
	protected boolean show_icon = true;
	protected Transformer<Object,String> name_transformer = null;
	protected Icon icon;
	
	public InterfaceComboBoxRenderer(){
		this(true, null);
	}
	
	public InterfaceComboBoxRenderer(boolean show_icon){
		this(show_icon, null);
	}
	
	public InterfaceComboBoxRenderer(boolean show_icon, Transformer<Object,String> name_transformer){
		this(show_icon, name_transformer, null);
	}
	
	public InterfaceComboBoxRenderer(boolean show_icon, Transformer<Object,String> name_transformer, Icon icon){
		super();
		this.show_icon = show_icon;
		setBackground(Color.white);
		setForeground(Color.black);
		this.name_transformer = name_transformer;
		this.icon = icon;
	}
	
	@Override
	public Component getListCellRendererComponent(JList list, 
												  Object value,
												  int index, 
												  boolean isSelected, 
												  boolean cellHasFocus) {
		
		setIcon(null);
		
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if (icon != null){
			setIcon(icon);
		}else if (value instanceof IconObject){
			setIcon(((IconObject)value).getObjectIcon()); 
			}
		
		if (name_transformer != null){
			String text = name_transformer.transform(value);
			if (text != null)
				setText(text);
		}else if (value instanceof InterfaceObject){
			setText(((InterfaceObject)value).getTreeLabel());
			}
		
		return this;
		
	}
	
}