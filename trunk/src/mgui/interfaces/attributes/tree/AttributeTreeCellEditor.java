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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.AttributeSelectionMap;
import mgui.interfaces.gui.ColourButton;
import mgui.interfaces.gui.FileButton;
import mgui.interfaces.gui.FontDialog;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.LineStyleDialog;
import mgui.interfaces.io.InterfaceFile;
import mgui.interfaces.maps.TypeMap;
import mgui.interfaces.shapes.util.StrokeSample;
import mgui.interfaces.trees.InterfaceTreeNode;
import mgui.interfaces.trees.TreeObject;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiNumber;
import mgui.resources.icons.IconObject;
import mgui.util.OptionList;


/**************************************
 * Cell editor for an attribute tree node. 
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class AttributeTreeCellEditor extends AbstractCellEditor implements TreeCellEditor,
																		   ItemListener,
																		   ActionListener {

	protected Color selectedBG;
	
	//Colour chooser
	ColourButton colourButton;
	Color currentColour;
	String currentText;
	JColorChooser colourChooser;
	
	//Font chooser
	AttributeButton fontButton;
	FontDialog fontChooser;
	Font currentFont;
	
	//stroke
	BasicStroke currentStroke;
	StrokeSample strokeSample;
	
	//arBoolean
	JCheckBox booleanBox;
	MguiBoolean currentBool;
	JTextField valueBox;
	
	//Type combo
	InterfaceComboBox cmbType;
	InterfaceComboBox cmbOption;
	InterfaceComboBox cmbInterface;
	TypeMap currentType;
	
	//File chooser
	FileButton file_button;
	
	Attribute currentValue;
	
	boolean updateItem = true;
	
	Component editorComponent;
	
	public final String CMD_CHANGE_COLOUR = "Change Colour";
	public final String CMD_CHANGE_BOOL = "Change Boolean";
	public final String CMD_CHANGE_TEXT = "Change Text";
	public final String CMD_CHANGE_TYPE = "Change Type";
	public final String CMD_CHANGE_STROKE = "Change Stroke";
	
//	public int cell_width = 375;
//	public int value_width = 125;
	
	public AttributeTreeCellEditor(){
		
		colourButton = new ColourButton();
		colourButton.setActionCommand(CMD_CHANGE_COLOUR);
		colourButton.addActionListener(this);		
		colourChooser = new JColorChooser();
		
		fontButton = new AttributeButton("Edit");
		fontButton.setActionCommand("Change Font");
		fontButton.addActionListener(this);
		fontChooser = new FontDialog();
		
		booleanBox = new JCheckBox();
		booleanBox.setActionCommand(CMD_CHANGE_BOOL);
		booleanBox.addActionListener(this);
		
		strokeSample = new StrokeSample(new BasicStroke(1));
		strokeSample.setActionCommand(CMD_CHANGE_STROKE);
		strokeSample.addActionListener(this);
		
		valueBox = new JTextField();
		valueBox.setActionCommand(CMD_CHANGE_TEXT);
		valueBox.addActionListener(this);
		
		file_button = new FileButton();
		file_button.setActionCommand("File Changed");
		file_button.addActionListener(this);
		
		cmbType = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
		cmbType.addItemListener(this);
		cmbOption = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
		cmbOption.addActionListener(this);
		selectedBG = new Color(212, 212, 255);
	}
	
	public Color getSelectedBackground(){
		return selectedBG;
	}
	
	public Component getTreeCellEditorComponent(JTree tree,
									            Object value,
									            boolean sel,
									            boolean expanded,
									            boolean leaf,
									            int row){

		int cell_width = AttributeTreeCellRenderer.CELL_WIDTH;
		int value_width = AttributeTreeCellRenderer.VALUE_WIDTH;
		
		Font font = tree.getFont().deriveFont(Font.BOLD);
		Color fore = Color.BLUE;
		
		int height = tree.getRowHeight();
		
		if (value instanceof AttributeTreeNode){
			Attribute<?> attribute = ((AttributeTreeNode)value).getUserObject();
				
			if (attribute.getValue() instanceof Color){
				colourButton.setColour((Color)attribute.getValue());
				currentColour = (Color)attribute.getValue();
				currentValue = attribute;
				//colourButton.setBackground((Color)attribute.getValue());
				colourButton.setPreferredSize(new Dimension(value_width, height));
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel("<html><u>" + attribute.getName() + "</u>");
				thisLabel.setFont(font);
				thisLabel.setForeground(fore);
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(colourButton, BorderLayout.EAST);
//				cell.setPreferredSize(new Dimension(cell_width, height));
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(colourButton);
				thisLabel.setPreferredSize(new Dimension(cell_width - value_width, height));
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				cell.setPreferredSize(new Dimension(cell_width, height));
				cell.setMaximumSize(new Dimension(cell_width, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				return cell;
				}
			
			if (attribute.getValue() instanceof InterfaceFile){
				file_button.setInterfaceFile((InterfaceFile)attribute.getValue());
				currentValue = attribute;
				file_button.setPreferredSize(new Dimension(value_width, height));
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel("<html><u>" + attribute.getName() + "</u>");
				thisLabel.setFont(font);
				thisLabel.setForeground(fore);
				//cell.setBackground(tree.getBackground());
				cell.setLayout(new BorderLayout());
				cell.add(thisLabel, BorderLayout.WEST);
				cell.add(file_button, BorderLayout.EAST);
				cell.setPreferredSize(new Dimension(cell_width, height));
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				
				return cell;
				}
			
			if (attribute.getValue() instanceof Font){
				fontButton.setAttribute(attribute);
				currentFont = (Font)attribute.getValue();
				currentValue = attribute;
				fontButton.setFont(new Font(((Font)attribute.getValue()).getFontName(), ((Font)attribute.getValue()).getStyle(), 13 ));
				fontButton.setPreferredSize(new Dimension(value_width, height));
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel("<html><u>" + attribute.getName() + "</u>");
				thisLabel.setFont(font);
				thisLabel.setForeground(fore);
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				//cell.setBackground(tree.getBackground());
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(fontButton, BorderLayout.EAST);
//				cell.setPreferredSize(new Dimension(cell_width, height));
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(fontButton);
				thisLabel.setPreferredSize(new Dimension(cell_width - value_width, height));
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				cell.setPreferredSize(new Dimension(cell_width, height));
				cell.setMaximumSize(new Dimension(cell_width, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				return cell;
				}
			
			if (attribute.getValue() instanceof BasicStroke){
				currentStroke = (BasicStroke)attribute.getValue();
				strokeSample.setStroke(currentStroke);
				currentValue = attribute;
				//thisBox.setFont(tree.getFont());
				strokeSample.setPreferredSize(new Dimension(value_width,height));
				strokeSample.setBackground(tree.getBackground());
				//fill
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel("<html><u>" + attribute.getName() + "</u>");
				thisLabel.setForeground(fore);
				thisLabel.setFont(font);
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				//cell.setBackground(tree.getBackground());
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(strokeSample, BorderLayout.EAST);
//				cell.setPreferredSize(new Dimension(cell_width, height));
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(strokeSample);
				thisLabel.setPreferredSize(new Dimension(cell_width - value_width, height));
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				cell.setPreferredSize(new Dimension(cell_width, height));
				cell.setMaximumSize(new Dimension(cell_width, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				return cell;
			}
			
			//TypeMap is a selection of possible types
			if (attribute.getValue() instanceof TypeMap){
				TypeMap m = (TypeMap)attribute.getValue();
				updateItem = false;
				//cmbInterface = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem, true, 500);
				cmbType.removeAllItems();
				cmbType.setPreferredSize(new Dimension(value_width, height));
				cmbType.setFont(tree.getFont());
				
				//for (int i = 0; i < m.types.items.size(); i++)
				//	cmbType.addItem(m.types.items.get(i).objValue);
				ArrayList<String> types = m.getTypes();
				for (String type : types)
					cmbType.addItem(type);
				cmbType.setSelectedItem(m.getTypeStr());
				updateItem = true;
				currentValue = attribute;
				if (!attribute.isEditable())
					cmbType.setEditable(false);
				else
					cmbOption.setEditable(true);
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel("<html><u>" + attribute.getName() + "</u>");
				thisLabel.setBackground(new Color(210, 210, 210));
				thisLabel.setFont(font);
				thisLabel.setForeground(fore);
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				//thisLabel.setBackground(back);
				//cell.setBackground(tree.getBackground());
				cell.setLayout(new BorderLayout());
				cell.add(thisLabel, BorderLayout.WEST);
				cell.add(cmbType, BorderLayout.EAST);
				cell.setPreferredSize(new Dimension(cell_width, height));
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				
				return cell;
			}
			
			if (attribute.getValue() instanceof OptionList){
				OptionList list = (OptionList)attribute.getValue();
				updateItem = false;
				//InterfaceComboBox combo = new InterfaceComboBox();
				list.setCombo(cmbOption);
				//combo.setSelectedItem(list.getCurrent());
				
				currentValue = attribute;
				//cmbOption = combo;
				cmbOption.setPreferredSize(new Dimension(value_width, height));
				cmbOption.setFont(tree.getFont());
				cmbOption.setActionCommand("Option Changed");
				cmbOption.addActionListener(this);
				if (!attribute.isEditable())
					cmbOption.setEditable(false);
				else
					cmbOption.setEditable(true);
				updateItem = true;
				
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel("<html><u>" + attribute.getName() + "</u>");
				thisLabel.setBackground(new Color(210, 210, 210));
				thisLabel.setFont(font);
				thisLabel.setForeground(fore);
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				//thisLabel.setBackground(back);
				//cell.setBackground(tree.getBackground());
				cell.setLayout(new BorderLayout());
				cell.add(thisLabel, BorderLayout.WEST);
				cell.add(cmbOption, BorderLayout.EAST);
				cell.setPreferredSize(new Dimension(cell_width, height));
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				
				return cell;
			}
			
			if (attribute instanceof AttributeSelection){
				AttributeSelection s = (AttributeSelection)attribute;
				
				JComboBox cmbSel = s.getComboBox();  
				cmbSel.setPreferredSize(new Dimension(value_width, height));
				cmbSel.setFont(tree.getFont());
				cmbSel.setEditable(attribute.isEditable());
				
				currentValue = attribute;
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel("<html><u>" + attribute.getName() + "</u>");
				//thisLabel.setBackground(new Color(210, 210, 210));
				thisLabel.setFont(font);
				thisLabel.setForeground(fore);
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				//cell.setBackground(tree.getBackground());
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(cmbSel, BorderLayout.EAST);
//				cell.setPreferredSize(new Dimension(cell_width, height));
				
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(cmbSel);
				thisLabel.setPreferredSize(new Dimension(cell_width - value_width, height));
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				cell.setPreferredSize(new Dimension(cell_width, height));
				cell.setMaximumSize(new Dimension(cell_width, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				return cell;
			}
			
			if (attribute instanceof AttributeSelectionMap){
				AttributeSelectionMap<?> s = (AttributeSelectionMap<?>)attribute;
				
				JComboBox cmbSel = s.getComboBox();  
				cmbSel.setPreferredSize(new Dimension(value_width, height));
				cmbSel.setFont(tree.getFont());
				cmbSel.setEditable(attribute.isEditable());
				
				currentValue = attribute;
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel("<html><u>" + attribute.getName() + "</u>");
				thisLabel.setFont(font);
				thisLabel.setForeground(fore);
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(cmbSel, BorderLayout.EAST);
//				cell.setPreferredSize(new Dimension(cell_width, height));
				
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(strokeSample);
				thisLabel.setPreferredSize(new Dimension(cell_width - value_width, height));
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				cell.setPreferredSize(new Dimension(cell_width, height));
				cell.setMaximumSize(new Dimension(cell_width, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
				
				return cell;
			}
			
			if (attribute.getValue() instanceof MguiBoolean){
				booleanBox.setSelected(((MguiBoolean)attribute.getValue()).getTrue());
				currentValue = attribute;
				if (!attribute.isEditable())
					booleanBox.setEnabled(false);
				else
					booleanBox.setEnabled(true);
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel("<html><u>" + attribute.getName() + "</u>");
				thisLabel.setFont(font);
				thisLabel.setForeground(fore);
				//thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				//cell.setBackground(tree.getBackground());
				cell.setLayout(new BorderLayout());
				cell.add(thisLabel, BorderLayout.EAST);
				cell.add(booleanBox, BorderLayout.WEST);
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				
				return cell;
				}
			
			//numbers or text edited in JTextField
			if (attribute.isNumeric() || String.class.isInstance(attribute.getValue())){
				valueBox.setText(attribute.getValueStr("##0.0000"));
				valueBox.setFont(tree.getFont());
				valueBox.setBackground(new Color(210, 210, 210));
				valueBox.setPreferredSize(new Dimension(value_width, height));
				if (!attribute.isEditable())
					valueBox.setEditable(false);
				else
					valueBox.setEditable(true);
				currentValue = attribute;
				currentText = attribute.toString();
				JPanel cell = new JPanel();
				JLabel thisLabel = new JLabel("<html><u>" + attribute.getName() + "</u>" + ": ");
				thisLabel.setFont(font);
				thisLabel.setForeground(fore);
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				//cell.setBackground(tree.getBackground());
//				cell.setLayout(new BorderLayout());
//				cell.add(thisLabel, BorderLayout.WEST);
//				cell.add(valueBox, BorderLayout.EAST);
//				cell.setPreferredSize(new Dimension(cell_width, height));
				if (sel)
					thisLabel.setBackground(selectedBG);
				else
					thisLabel.setBackground(tree.getBackground());
				if (sel)
					cell.setBackground(selectedBG);
				else
					cell.setBackground(tree.getBackground());
				
				cell.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
				cell.add(thisLabel);
				cell.add(valueBox);
				thisLabel.setPreferredSize(new Dimension(cell_width - value_width, height));
				thisLabel.setMaximumSize(new Dimension(cell_width - value_width, height));
				cell.setPreferredSize(new Dimension(cell_width, height));
				cell.setMaximumSize(new Dimension(cell_width, height));
				cell.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
			
				return cell;
				}
			
			JPanel cell = new JPanel();
			JLabel name = new JLabel("<html><u>" + attribute.getName() + "</u>:</html>");
			
			JLabel val = new JLabel(value.toString());
			val.setPreferredSize(new Dimension(value_width, height));
			cell.setLayout(new BorderLayout());
			cell.add(name, BorderLayout.WEST);
			cell.add(val, BorderLayout.EAST);
			cell.setBackground(tree.getBackground());
			cell.setPreferredSize(new Dimension(cell_width, height));
			if (sel)
				name.setBackground(selectedBG);
			else
				name.setBackground(tree.getBackground());
			if (sel)
				cell.setBackground(selectedBG);
			else
				cell.setBackground(tree.getBackground());
			
			name.setForeground(fore);
			name.setFont(font);
			val.setFont(tree.getFont());
			return cell;
			}
		
		if (value instanceof InterfaceTreeNode) value = ((InterfaceTreeNode)value).getUserObject();
		String s = value.toString();
		
		if (value instanceof TreeObject)
			s = ((TreeObject)value).getTreeLabel();
		JLabel label = new JLabel(s);
		label.setPreferredSize(new Dimension(value_width, height));
		label.setOpaque(true);
		if (value instanceof IconObject){
			Icon icon = ((IconObject)value).getObjectIcon();
			//if (icon != null)
			label.setIcon(icon);
			}
		label.setFont(font);
		label.setForeground(fore);
		label.setBackground(getSelectedBackground());
		
		return label;
	}
	
	public Object getCellEditorValue() {
		return currentValue;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (currentValue == null) return;		//urg, unpretty (required for OptionList; TODO: improve this)
		
		//wtf is this necessary?
		if (!currentValue.isEditable()){
			fireEditingCanceled();
			return;
			}
		
		if (e.getActionCommand().equals("Option Changed")){
			OptionList list = (OptionList)currentValue.getValue();
			list.setCurrent((String)cmbOption.getSelectedItem());
			currentValue.fireAttributeListeners();
			fireEditingStopped();
			return;
			}
		
		if (e.getActionCommand().equals("File Changed")){
			file_button.showChooser();
			currentValue.setValue(file_button.getInterfaceFile());
			fireEditingStopped();
			return;
			}
		
		if (CMD_CHANGE_COLOUR.equals(e.getActionCommand())){
			
			colourChooser.setColor(currentColour);
			Color c = JColorChooser.showDialog(null, "Select Colour", currentColour);
			//currentColour = 
			if (c != null){
				currentColour = c;
				if (currentValue.getValue() instanceof Color)
					currentValue.setValue(currentColour);
				//colourButton.setBackground(currentColour);
				colourButton.setColour(currentColour);
				}
			fireEditingStopped();
			return;
			}
		
		if (e.getActionCommand().equals("Change Font")){
			
			Font font = FontDialog.showDialog(currentFont);
			
			if (font != null){
				currentFont = font;
				currentValue.setValue(font);
				fontButton.setFont(new Font(font.getFontName(), font.getStyle(), 13));
				}
			
			//fontButton.setFont(currentFont);
			fireEditingStopped();
			return;
			}
		
		if (CMD_CHANGE_STROKE.equals(e.getActionCommand())){
			currentStroke = LineStyleDialog.showDialog(null, 
													   "Line Style Editor", 
													   currentStroke);
			if (currentStroke != null){
				currentValue.setValue(currentStroke);
				strokeSample.setStroke(currentStroke);
				}
			fireEditingStopped();
			return;
			}
		
		if (CMD_CHANGE_BOOL.equals(e.getActionCommand())){
			currentBool = new MguiBoolean(((JCheckBox)e.getSource()).isSelected());
			//if (currentValue.getValue() instanceof arBoolean)
			currentValue.setValue(currentBool);
			fireEditingStopped();
			return;
		}
		
		if (CMD_CHANGE_TEXT.equals(e.getActionCommand())){
			if (currentValue.isNumeric()){
				MguiNumber n = (MguiNumber)currentValue.getValue();
				if (!n.setValue(((JTextField)e.getSource()).getText()))
					InterfaceSession.log("Invalid number: " + ((JTextField)e.getSource()).getText());
				currentValue.setValue(n);
				((JTextField)e.getSource()).setText(currentValue.getValueStr());
				//((arNumber)currentValue.value).setValue(((JTextField)e.getSource()).getText());
			}else
				currentValue.setValue(((JTextField)e.getSource()).getText());
			fireEditingStopped();
			return;
		}
		
		if (e.getSource().equals(cmbOption) && updateItem){
			if (currentValue == null) return;
			if (currentValue.getValue() instanceof OptionList){
				((OptionList)currentValue.getValue()).setCurrent((String)cmbOption.getSelectedItem());
				//forces a call to listeners
				currentValue.setValue(currentValue.getValue());
				}
			fireEditingStopped();
			return;
		}

	}
	
	public void itemStateChanged(ItemEvent e){
		
		if (currentValue == null) return;
		
		if (currentValue.getValue() instanceof AttributeSelection){
			((AttributeSelection)currentValue.getValue()).setValue(e.getItem());
			fireEditingStopped();
			return;
			}
		
		if (e.getSource().equals(cmbType) && updateItem && e.getStateChange() == ItemEvent.SELECTED){
			if (currentValue == null) return;
			//try{
			if (currentValue.getValue() instanceof TypeMap){
				((TypeMap)currentValue.getValue()).setType((String)cmbType.getSelectedItem());
				}
			fireEditingStopped();
			return;
		}
		
		int debug_1 = e.getStateChange();
		
	}
	
	
}