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

package mgui.interfaces.attributes.table;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.jogamp.vecmath.Matrix4d;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.tree.AttributeButton;
import mgui.interfaces.gui.ColourButton;
import mgui.interfaces.maps.TypeMap;
import mgui.interfaces.math.VariableObject;
import mgui.interfaces.shapes.util.StrokeSample;
import mgui.numbers.MguiBoolean;


public class AttributeCellRenderer implements TableCellRenderer {

	public Component getTableCellRendererComponent(JTable thisTable, Object thisObj,
												   boolean isSel, boolean hasFocus, 
												   int row, int col) {
		
		if (!(thisObj instanceof Attribute))
			return new JLabel(thisObj.toString());
		
		//InterfaceAttributeTable attrTable = (InterfaceAttributeTable)thisTable;
		Attribute<?> attribute = (Attribute<?>)thisObj;
		
		//if (col == 0)
		//	return new JLabel(attribute.getName());
		
		if (attribute.getObjectClass() != null && 
			VariableObject.class.isAssignableFrom(attribute.getObjectClass())){
			
			JButton variable_button = new JButton("Variable..");
			variable_button.setActionCommand("Change Variable");
			variable_button.setEnabled(attribute.isEditable());
			return variable_button;
			
			}
		
		if (attribute.getValue() instanceof Color){
			ColourButton thisButton = new ColourButton((Color)attribute.getValue());
			thisButton.setActionCommand("Change Colour");
			thisButton.setEnabled(attribute.isEditable());
			return thisButton;
			}
		
		if (attribute.getValue() instanceof BasicStroke){
			StrokeSample sample = new StrokeSample((BasicStroke)attribute.getValue());
			sample.setEnabled(attribute.isEditable());
			return sample;
		}
		
		if (attribute.getValue() instanceof MguiBoolean){
			JCheckBox thisBox = new JCheckBox("", ((MguiBoolean)attribute.getValue()).getTrue());
			thisBox.setEnabled(attribute.isEditable());
			return thisBox;
		}
		
		if (attribute.getValue() instanceof Matrix4d){
			if (thisTable instanceof InterfaceAttributeTable){
				AttributeButton button = new AttributeButton(((InterfaceAttributeTable)thisTable).getAttribute(row));
				button.setText("Edit..");
				button.setActionCommand("Edit Matrix");
				return button;
				}
		}
		
		if (attribute instanceof AttributeSelection){
			JComboBox box = ((AttributeSelection<?>)attribute).getComboBox();
			box.setEnabled(attribute.isEditable());
			return box;
		}
		
		if (attribute.getValue() instanceof Font){
			AttributeButton button = new AttributeButton(((InterfaceAttributeTable)thisTable).getAttribute(row));
			button.setText("Edit..");
			Font font = (Font)attribute.getValue();
			button.setFont(new Font(font.getFontName(), font.getStyle(), 13 ));
			button.setActionCommand("Change Font");
			return button;
		}
		
		if (attribute.getValue() instanceof TypeMap){
			return new JLabel(((TypeMap)attribute.getValue()).getTypeStr());
		}
		
		if (attribute.isNumeric())
			return new JLabel(attribute.getValueStr("#,##0.00000"));
		
		return new JLabel(attribute.getValueStr());
	}

}