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

package mgui.interfaces.attributes.tree;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.AttributeSelectionMap;
import mgui.interfaces.gui.ColourButton;
import mgui.interfaces.gui.FileButton;
import mgui.interfaces.io.InterfaceFile;
import mgui.interfaces.maps.TypeMap;
import mgui.interfaces.shapes.util.StrokeSample;
import mgui.interfaces.trees.TreeObject;
import mgui.numbers.MguiBoolean;
import mgui.resources.icons.IconObject;
import mgui.util.OptionList;


//TODO make this a general model renderer
//	   and allow user-defined class types to call specific
//	   renderers
public class AttributeTreeCellRenderer extends DefaultTreeCellRenderer {
		
	protected Color selectedBG;
	public static final int CELL_WIDTH = 270;
	public static final int VALUE_WIDTH = 100;
	
	public AttributeTreeCellRenderer(){
		super();
		selectedBG = new Color(212, 212, 255);
	
	}
	
	class TestComponent extends JPanel{
		
		public TestComponent(JComponent c){
			this.setLayout(new BorderLayout());
			this.add(c, BorderLayout.CENTER);
		}
		
		@Override
		public boolean isVisible(){
			return false;
		}
		
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree,
												    Object value,
												    boolean sel,
												    boolean expanded,
												    boolean leaf,
												    int row,
												    boolean hasFocus){
		
		int height = tree.getRowHeight();
		TestComponent component = null;
		
		if (value instanceof Attribute){
			//Attribute thisAttr = ((AttributeNode)value).getUserObject();
			Attribute<?> thisAttr = (Attribute<?>)value;
			
			if (thisAttr.getValue() instanceof Color){
				ColourButton thisButton = new ColourButton((Color)thisAttr.getValue());
				thisButton.setEnabled(thisAttr.isEditable());
				thisButton.setBackground((Color)thisAttr.getValue());
				thisButton.setPreferredSize(new Dimension(VALUE_WIDTH, height));
				thisButton.setActionCommand("Change Colour");
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel(thisAttr.getName() + ":  ");
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				thisLabel.setFont(tree.getFont());
				
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(thisButton, BorderLayout.EAST);
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(thisButton);
				thisLabel.setPreferredSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				cell.setMaximumSize(new Dimension(CELL_WIDTH, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				//return cell;
				component = new TestComponent(cell);
			}
			
			if (thisAttr.getValue() instanceof InterfaceFile){
				InterfaceFile file = (InterfaceFile)thisAttr.getValue();
				FileButton button = new FileButton(file);
				button.setEnabled(thisAttr.isEditable());
				button.setPreferredSize(new Dimension(VALUE_WIDTH, height));
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel(thisAttr.getName() + ":  ");
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					//thisLabel.setBackground(tree.getBackground());
					thisLabel.setOpaque(false);
				thisLabel.setFont(tree.getFont());
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				cell.setLayout(new BorderLayout());
				cell.add(thisLabel, BorderLayout.WEST);
				cell.add(button, BorderLayout.EAST);
				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				
				//return cell;
				component = new TestComponent(cell);
				}
			
			if (thisAttr.getValue() instanceof BasicStroke){
				StrokeSample sample = new StrokeSample((BasicStroke)thisAttr.getValue());
				//thisBox.setFont(tree.getFont());
				sample.setPreferredSize(new Dimension(VALUE_WIDTH,height));
				sample.setBackground(tree.getBackground());
				//fill
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel(thisAttr.getName());
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					//thisLabel.setBackground(tree.getBackground());
					thisLabel.setOpaque(false);
				thisLabel.setFont(tree.getFont());
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(sample, BorderLayout.EAST);
//				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(sample);
				thisLabel.setPreferredSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				cell.setMaximumSize(new Dimension(CELL_WIDTH, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				//return cell;
				component = new TestComponent(cell);
			}
			
			if (thisAttr.getValue() instanceof MguiBoolean){
				JCheckBox thisBox = new JCheckBox("", ((MguiBoolean)thisAttr.getValue()).getTrue());
				thisBox.setEnabled(thisAttr.isEditable());
				thisBox.setBackground(tree.getBackground());
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel(thisAttr.getName());
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					//thisLabel.setBackground(tree.getBackground());
					thisLabel.setOpaque(false);
				thisLabel.setFont(tree.getFont());
				//thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				cell.setLayout(new BorderLayout());
				cell.add(thisLabel, BorderLayout.EAST);
				cell.add(thisBox, BorderLayout.WEST);
				
				//return cell;
				component = new TestComponent(cell);
			}
			
			if (thisAttr instanceof AttributeSelection){
				JPanel cell = new JPanel();
				JLabel name = new JLabel(thisAttr.getName() + ":");
				if (sel)
					name.setBackground(selectedBG);
				else
					name.setBackground(tree.getBackground());
				JComboBox box = ((AttributeSelection)thisAttr).getComboBox(VALUE_WIDTH);
				box.setEditable(thisAttr.isEditable());
				box.setPreferredSize(new Dimension(VALUE_WIDTH, height));
				cell.setLayout(new BorderLayout());
				cell.add(name, BorderLayout.WEST);
				cell.add(box, BorderLayout.EAST);
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
//				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				name.setFont(tree.getFont());
//				name.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				box.setFont(tree.getFont());
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(name);
				cell.add(box);
				name.setPreferredSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				name.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				cell.setMaximumSize(new Dimension(CELL_WIDTH, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				//return cell;
				component = new TestComponent(cell);
			}
			
			if (thisAttr instanceof AttributeSelectionMap){
				JPanel cell = new JPanel();
				JLabel name = new JLabel(thisAttr.getName() + ":");
				if (sel)
					name.setBackground(selectedBG);
				else
					name.setBackground(tree.getBackground());
				JComboBox box = ((AttributeSelectionMap<?>)thisAttr).getComboBox();
				box.setEditable(thisAttr.isEditable());
				box.setPreferredSize(new Dimension(VALUE_WIDTH, height));
//				cell.setLayout(new BorderLayout());
//				cell.add(name, BorderLayout.WEST);
//				cell.add(box, BorderLayout.EAST);
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				name.setFont(tree.getFont());
				name.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				box.setFont(tree.getFont());
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(name);
				cell.add(box);
				name.setPreferredSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				name.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				cell.setMaximumSize(new Dimension(CELL_WIDTH, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				//return cell;
				component = new TestComponent(cell);
			}
			
			if (thisAttr.getValue() instanceof Font){
				AttributeButton thisButton = new AttributeButton(thisAttr, "Edit");
				thisButton.setFont(new Font(((Font)thisAttr.getValue()).getFontName(), ((Font)thisAttr.getValue()).getStyle(), 13 ));
				thisButton.setPreferredSize(new Dimension(VALUE_WIDTH, height));
				thisButton.setActionCommand("Change Font");
				
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel(thisAttr.getName() + "  ");
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				thisLabel.setFont(tree.getFont());
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(thisButton, BorderLayout.EAST);
//				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(thisButton);
				thisLabel.setPreferredSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				cell.setMaximumSize(new Dimension(CELL_WIDTH, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				//return cell;
				component = new TestComponent(cell);
			}
			
			if (thisAttr.getValue() instanceof TypeMap){
				//JCheckBox thisBox = new JCheckBox("", ((arBoolean)thisAttr.getValue()).value);
				//thisBox.setBackground(tree.getBackground());
				JComboBox thisBox = new JComboBox();
				thisBox.setEditable(thisAttr.isEditable());
				thisBox.setFont(tree.getFont());
				thisBox.setPreferredSize(new Dimension(VALUE_WIDTH,height));
				thisBox.setBackground(tree.getBackground());
				//fill
				ArrayList<String> types = ((TypeMap)thisAttr.getValue()).getTypes();
				for (int i = 0; i < types.size(); i++)
					thisBox.addItem(types.get(i));
				//for (int i = 0; i < ((TypeMap)thisAttr.getValue()).types.items.size(); i++)
				//	thisBox.addItem(((TypeMap)thisAttr.getValue()).types.items.get(i).objValue);
				thisBox.setSelectedItem(((TypeMap)thisAttr.getValue()).getTypeStr());
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel(thisAttr.getName());
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				thisLabel.setFont(tree.getFont());
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(thisBox, BorderLayout.EAST);
//				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(thisBox);
				thisLabel.setPreferredSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				cell.setMaximumSize(new Dimension(CELL_WIDTH, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				//return cell;
				component = new TestComponent(cell);
			}
			
			if (thisAttr.getValue() instanceof OptionList){
				JComboBox thisBox = new JComboBox();
				thisBox.setEditable(thisAttr.isEditable());
				thisBox.setFont(tree.getFont());
				thisBox.setPreferredSize(new Dimension(VALUE_WIDTH,height));
				thisBox.setBackground(tree.getBackground());
				//fill
				OptionList list = (OptionList)thisAttr.getValue();
				Iterator itr = list.options.keySet().iterator();
				while (itr.hasNext())
					thisBox.addItem(itr.next());
				thisBox.setSelectedItem(list.current);
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel(thisAttr.getName());
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				thisLabel.setFont(tree.getFont());
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(thisBox, BorderLayout.EAST);
//				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(thisBox);
				thisLabel.setPreferredSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				cell.setMaximumSize(new Dimension(CELL_WIDTH, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				//return cell;
				component = new TestComponent(cell);
			}
			
			if (component == null && (thisAttr.isNumeric() || String.class.isInstance(thisAttr.getValue()))){
				JTextField thisField = new JTextField(thisAttr.getValueStr("##0.0000"));
				thisField.setEditable(thisAttr.isEditable());
				thisField.setBackground(tree.getBackground());
				thisField.setPreferredSize(new Dimension(VALUE_WIDTH, height));
				thisField.setFont(tree.getFont());
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel(thisAttr.getName() + ": ");
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				thisLabel.setFont(tree.getFont());
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(thisField, BorderLayout.EAST);
//				cell.setPreferredSize(new Dimension(CELL_WIDTH,height));
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(thisField);
				thisLabel.setPreferredSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				thisLabel.setMaximumSize(new Dimension(CELL_WIDTH - VALUE_WIDTH, height));
				cell.setPreferredSize(new Dimension(CELL_WIDTH, height));
				cell.setMaximumSize(new Dimension(CELL_WIDTH, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				//return cell;
				component = new TestComponent(cell);
				}
			
			if (component == null){
			
				JPanel cell = new JPanel();
				JLabel name = new JLabel(thisAttr.getName() + ":");
				if (sel)
					name.setBackground(selectedBG);
				else
					name.setBackground(tree.getBackground());
				JLabel val = new JLabel(value.toString());
				val.setPreferredSize(new Dimension(VALUE_WIDTH, height));
				cell.setLayout(new BorderLayout());
				cell.add(name, BorderLayout.WEST);
				cell.add(val, BorderLayout.EAST);
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				cell.setPreferredSize(new Dimension(CELL_WIDTH,height));
				//cell.setPreferredSize(new Dimension(200, height));
				name.setFont(tree.getFont());
				val.setFont(tree.getFont());
				
				//return cell;
				component = new TestComponent(cell);
				}
			
			return component;
		}
		
		String s = value.toString();
		if (value instanceof TreeObject)
			s = ((TreeObject)value).getTreeLabel();
		JLabel label = new JLabel(s);
		label.setOpaque(true);
		if (value instanceof IconObject){
			Icon icon = ((IconObject)value).getObjectIcon();
			if (icon != null)
				label.setIcon(icon);
			}
		if (sel) //value instanceof InterfaceObject && sel)
			label.setBackground(getSelectedBackground());
		else
			label.setBackground(tree.getBackground());
		
		label.setFont(tree.getFont());
		return label;
	}
	
	public Color getSelectedBackground(){
		return selectedBG;
	}
	
}