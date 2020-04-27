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

package mgui.interfaces.attributes.table;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import org.jogamp.vecmath.Matrix4d;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeSelection;
import mgui.interfaces.attributes.tree.AttributeButton;
import mgui.interfaces.gui.ColourButton;
import mgui.interfaces.gui.FontDialog;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.LineStyleDialog;
import mgui.interfaces.gui.MatrixEditorDialog;
import mgui.interfaces.maps.TypeMap;
import mgui.interfaces.math.VariableObject;
import mgui.interfaces.shapes.util.StrokeSample;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiNumber;


public class AttributeCellEditor extends AbstractCellEditor implements
															TableCellEditor,
															ActionListener {

	//Colour chooser
	ColourButton colourButton;
	AttributeButton matrixButton;
	Color currentColour;
	BasicStroke currentStroke;
	StrokeSample strokeSample;
	String currentText;
	JColorChooser colourChooser;
	LineStyleDialog styleDialog;
	
	//arBoolean
	JCheckBox booleanBox;
	MguiBoolean currentBool;
	JTextField valueBox;
	
//	Type combo
	JComboBox cmbType;
	TypeMap currentType;
	
	Object current_obj;
	
	//Attribute selection
	AttributeSelection<?> currentSel;
	
	Attribute<?> currentValue;
	
	public static final String CMD_CHANGE_COLOUR = "Change Colour";
	public static final String CMD_CHANGE_BOOL = "Change Boolean";
	public static final String CMD_CHANGE_TEXT = "Change Text";
	public static final String CMD_CHANGE_TYPE = "Change Type";
	public static final String CMD_CHANGE_STROKE = "Change Stroke";
	public static final String CMD_CHANGE_SELECTION = "Change Selection";
	
	public AttributeCellEditor(){
		colourButton = new ColourButton();
		colourButton.setActionCommand(CMD_CHANGE_COLOUR);
		colourButton.addActionListener(this);		
		//colourChooser = new JColorChooser();
		
		strokeSample = new StrokeSample(new BasicStroke(1));
		strokeSample.setActionCommand(CMD_CHANGE_STROKE);
		strokeSample.addActionListener(this);

		booleanBox = new JCheckBox();
		booleanBox.setActionCommand(CMD_CHANGE_BOOL);
		booleanBox.addActionListener(this);
		
		valueBox = new JTextField();
		valueBox.setActionCommand(CMD_CHANGE_TEXT);
		valueBox.addActionListener(this);
		
		matrixButton = new AttributeButton();
		matrixButton.setText("Edit..");
		matrixButton.setActionCommand("Edit Matrix");
		matrixButton.addActionListener(this);	
		
	}
	
	
	public Component getTableCellEditorComponent(JTable thisTable, Object thisObj,
												 boolean isSel, int row, int col) {
		
		if (!(thisObj instanceof Attribute))
			return new JTextField(thisObj.toString());
		
		Attribute<?> attr = (Attribute<?>)thisObj;
		currentValue = (Attribute<?>)attr.clone();
		
		if (attr.getObjectClass() != null && 
			VariableObject.class.isAssignableFrom(attr.getObjectClass())){
				
				JButton variable_button = new JButton("Variable..");
				variable_button.setActionCommand("Change Variable");
				variable_button.setEnabled(attr.isEditable());
				variable_button.addActionListener(this);
				return variable_button;
				
				}
		
		if (Color.class.isInstance(attr.getValue())){
			currentColour = (Color)attr.getValue();
			colourButton.setEnabled(attr.isEditable());
			colourButton.setColour(currentColour);
			return colourButton;
		}
		
		if (attr.getValue() instanceof Matrix4d){
			current_obj = attr.getValue();
			return matrixButton;
		}
		
		if (attr.getValue() instanceof Font){
			current_obj = attr.getValue();
			JButton button = new JButton("Edit..");
			button.setActionCommand("Change Font");
			button.addActionListener(this);
			return button;
		}
		
		if (attr.getValue() instanceof BasicStroke){
			currentStroke = (BasicStroke)attr.getValue();
			strokeSample.setEnabled(attr.isEditable());
			return strokeSample;
		}
		
		if (MguiBoolean.class.isInstance(attr.getValue())){
			currentBool = (MguiBoolean)attr.getValue();
			booleanBox.setEnabled(attr.isEditable());
			booleanBox.setSelected(currentBool.getTrue());
			return booleanBox;
		}
		
		if (attr instanceof AttributeSelection){
			AttributeSelection<?> s = (AttributeSelection<?>)attr;
			
			InterfaceComboBox cmbSel = s.getComboBox();
			InterfaceComboBox copy = cmbSel.copy();
//			for (int i = 0; i < cmbSel.getItemCount(); i++)
//				copy.addItem(cmbSel.getItemAt(i));
			
			copy.setSelectedItem(s.getValue());
			copy.setEditable(attr.isEditable());
			copy.setActionCommand(CMD_CHANGE_SELECTION);
			copy.addActionListener(this);
			
			currentValue = attr;
			return copy;
		}
		
		if (TypeMap.class.isInstance(attr.getValue())){
			currentType = (TypeMap)attr.getValue();
			cmbType = new JComboBox();
			
			ArrayList<String> types = currentType.getTypes();
			for (int i = 0; i < types.size(); i++)
				cmbType.addItem(types.get(i));
			cmbType.setSelectedItem(currentType.getTypeStr());
			cmbType.setActionCommand(CMD_CHANGE_TYPE);
			cmbType.addActionListener(this);
			cmbType.setEnabled(attr.isEditable());
			return cmbType;
		}
		
		if (attr.isNumeric())
			valueBox.setText(attr.getValueStr("#,##0.00000"));
		else
			valueBox.setText(attr.getValue().toString());
		valueBox.setEnabled(attr.isEditable());
		return valueBox;
	}
	
	public Object getCellEditorValue() {
		return currentValue;
	}

	public void actionPerformed(ActionEvent e) {
		
		//TODO: implement this
		if (e.getActionCommand().equals("Change Variable")){
			VariableObject obj = (VariableObject)currentValue.getValue();
			//VariableObjectDialog.showDialog()
			
			return;
			}
		
		if (CMD_CHANGE_COLOUR.equals(e.getActionCommand())){
			colourButton.setColour(currentColour);
			Color c = JColorChooser.showDialog(null, "Select Colour", currentColour);
			if (c != null )
			currentValue.setValue(c);
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
			currentBool.setTrue(((JCheckBox)e.getSource()).isSelected());
			currentValue.setValue(currentBool);
			fireEditingStopped();
			return;
			}
		
		if (CMD_CHANGE_TYPE.equals(e.getActionCommand())){
			currentType.setType((String)((JComboBox)e.getSource()).getSelectedItem());
			fireEditingStopped();
			return;
		}
		
		if (CMD_CHANGE_SELECTION.equals(e.getActionCommand())){
			currentValue.setValue(((JComboBox)e.getSource()).getSelectedItem());
			fireEditingStopped();
			return;
			}
		
		if (CMD_CHANGE_TEXT.equals(e.getActionCommand())){
			if (currentValue.isNumeric())
				((MguiNumber)currentValue.getValue()).setValue(((JTextField)e.getSource()).getText());
			else
				currentValue.setValue(((JTextField)e.getSource()).getText(), false);
			fireEditingStopped();
			return;
		}
		
		if (e.getActionCommand().equals("Edit Matrix")){
			Matrix4d matrix = MatrixEditorDialog.showDialog(InterfaceEnvironment.getFrame(), (Matrix4d)current_obj);
			if (matrix != null)
				currentValue.setValue(matrix);
			fireEditingStopped();
			return;
			}
		
		if (e.getActionCommand().equals("Change Font")){
			Font font = FontDialog.showDialog((Font)current_obj);
			if (font != null)
				currentValue.setValue(font);
			fireEditingStopped();
			return;
			}

	}

}