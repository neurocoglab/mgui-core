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

package mgui.interfaces.attributes.video;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.Attribute;
import mgui.interfaces.attributes.AttributeList;
import mgui.interfaces.attributes.AttributeObject;
import mgui.interfaces.graphics.video.VideoTaskDialogPanel;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.InterfaceShape;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.numbers.MguiBoolean;
import mgui.numbers.MguiNumber;
import mgui.numbers.NumberFunctions;

/*********************************************************
 * Panel for defining an {@linkplain AttributeObjectTask}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class AttributeObjectVideoTaskDialogPanel extends VideoTaskDialogPanel {

	//TODO implement tree showing all available attribute objects
	JLabel lblShape = new JLabel("Shape:");
	InterfaceComboBox cmbShape = new InterfaceComboBox(RenderMode.LongestItem, true, 200);
	JLabel lblAttribute = new JLabel("Attribute:");
	InterfaceComboBox cmbAttribute = new InterfaceComboBox(RenderMode.LongestItem, true, 200);
	JLabel lblStartValue = new JLabel("New value:");
	JTextField txtStartValue = new JTextField("");
	JCheckBox chkEndValue = new JCheckBox(" End value:");
	JTextField txtEndValue = new JTextField("");
	JLabel lblStep = new JLabel("Step:");
	JTextField txtStep = new JTextField("0.1");
	JCheckBox chkBooleanValue = new JCheckBox();
	//JCheckBox chkIterate = new JCheckBox();
	
	JComponent value_component;
	
	LineLayoutConstraints value_constraints = new LineLayoutConstraints(3, 3, 0.3, 0.65, 1);
	
	AttributeObjectVideoTask task;
	boolean handleCombo = true;
	
	public AttributeObjectVideoTaskDialogPanel(){
		super();
	}

	public AttributeObjectVideoTaskDialogPanel(AttributeObjectVideoTask task){
		super();
		this.task = task;
		init();
	}
	
	@Override
	protected void init() {
		
		txtEndValue.setVisible(false);
		chkEndValue.setSelected(false);
		chkEndValue.setVisible(false);
		chkBooleanValue.setVisible(false);
		lblStep.setVisible(false);
		txtStep.setVisible(false);
		
		cmbShape.addActionListener(this);
		cmbShape.setActionCommand("Shape Combo Changed");
		cmbAttribute.addActionListener(this);
		cmbAttribute.setActionCommand("Attribute Combo Changed");
		chkEndValue.addActionListener(this);
		chkEndValue.setActionCommand("Iterate Changed");
		
		LineLayout lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		setLayout(lineLayout);
		
		fillCombos();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		add(lblShape, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		add(cmbShape, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.24, 1);
		add(lblAttribute, c);
		c = new LineLayoutConstraints(2, 2, 0.3, 0.65, 1);
		add(cmbAttribute, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.24, 1);
		add(lblStartValue, c);
		c = new LineLayoutConstraints(3, 3, 0.3, 0.65, 1);
		add(txtStartValue, c);
		c = new LineLayoutConstraints(3, 3, 0.3, 0.65, 1);
		add(chkBooleanValue, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.25, 1);
		add(chkEndValue, c);
		c = new LineLayoutConstraints(4, 4, 0.3, 0.27, 1);
		add(txtEndValue, c);
		c = new LineLayoutConstraints(4, 4, 0.59, 0.7, 1);
		add(lblStep, c);
		c = new LineLayoutConstraints(4, 4, 0.68, 0.27, 1);
		add(txtStep, c);
		
		
	}
	
	@Override
	public void updateTask() {
		InterfaceShape shape = (InterfaceShape)cmbShape.getSelectedItem();
		if (shape == null) return;
		
		task.object = shape;
		task.incremental = chkEndValue.isSelected();
		
		Attribute<?> a = (Attribute<?>)cmbAttribute.getSelectedItem();
		if (a == null) return;
		task.attribute = a.getName();
		
		Class<?> c = a.getValue().getClass();
		
		if (c.equals(String.class)){
			task.start_value = txtStartValue.getText();
			task.end_value = txtStartValue.getText();
			}
		
		if (c.equals(MguiBoolean.class)){
			task.start_value = new MguiBoolean(chkBooleanValue.isSelected());
			task.end_value = new MguiBoolean(chkBooleanValue.isSelected());
		}else if (MguiNumber.class.isAssignableFrom(c)){
			MguiNumber start = NumberFunctions.getInstance(c, 0);
			if (start == null) return;
			MguiNumber end = (MguiNumber)start.clone();
			
			task.start_value = start;
			task.end_value = end;
			if (!chkEndValue.isSelected()){
				start.setValue(txtStartValue.getText());
				end.setValue(txtStartValue.getText());
			}else{
				start.setValue(txtStartValue.getText());
				end.setValue(txtEndValue.getText());
				task.step = Double.valueOf(txtStep.getText());
				}
			}
			
		
		
	}
	
	void updateControls(Attribute<?> a, Object value){
		updateControls(a,value,null);
	}
	
	void updateControls(Attribute<?> a, Object start, Object end){
		if (a.getValue() == null){
			chkEndValue.setSelected(false);
			chkEndValue.setVisible(false);
			txtStartValue.setEditable(false);
			txtEndValue.setVisible(false);
			chkBooleanValue.setVisible(false);
			lblStep.setVisible(false);
			txtStep.setVisible(false);
			this.revalidate();
			return;
			}
		
		Class<?> c = a.getValue().getClass();
		
		txtStartValue.setEditable(true);
		
		if (c.equals(String.class)){
			txtStartValue.setText((String)start);
			txtStartValue.setVisible(true);
			txtEndValue.setVisible(false);
			chkEndValue.setSelected(false);
			chkEndValue.setVisible(false);
			chkBooleanValue.setVisible(false);
			lblStep.setVisible(false);
			txtStep.setVisible(false);
			this.revalidate();
			return;
			}
		
		if (c.equals(MguiBoolean.class)){
			boolean b_wtf = ((MguiBoolean)start).getTrue();
			chkBooleanValue.setSelected(b_wtf);
			chkBooleanValue.setVisible(true);
			chkEndValue.setSelected(false);
			chkEndValue.setVisible(false);
			txtStartValue.setVisible(false);
			txtEndValue.setVisible(false);
			lblStep.setVisible(false);
			txtStep.setVisible(false);
			this.revalidate();
			return;
			}
		
		if (MguiNumber.class.isAssignableFrom(c)){
			//chkEndValue.setSelected(true);
			chkEndValue.setVisible(true);
			txtStartValue.setVisible(true);
			String str = ((MguiNumber)start).toString();
			txtStartValue.setText(str);
			txtEndValue.setVisible(true);
			lblStep.setVisible(true);
			txtStep.setVisible(true);
			if (!chkEndValue.isSelected()){
				txtEndValue.setEnabled(false);
				txtStep.setEnabled(false);
			}else{
				txtEndValue.setEnabled(true);
				txtStep.setEnabled(true);
				txtEndValue.setVisible(true);
				if (end != null){
					str = ((MguiNumber)end).toString();
					}
				txtEndValue.setText(str);
				}
			this.revalidate();
			return;
			}
		
		txtStartValue.setVisible(true);
		txtStartValue.setEditable(false);
		txtStartValue.setText("Object type not implemented..");
		this.revalidate();
		
	}
	
	
	
	@Override
	public int getLineCount(){
		return 4;
	}
	
	void fillCombos(){
		
		handleCombo = false;
		
		//get all 3D shapes
		ShapeSet3DInt set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		cmbShape.removeAllItems();
		
		addShapesToCombo(set);
		if (task.object != null){
			chkEndValue.setSelected(task.incremental);
			cmbShape.setSelectedItem(task.object);
			txtStep.setText(task.step + "");
		}else{
			task.object = (AttributeObject)cmbShape.getSelectedItem();
			}
		
		updateAttributeCombo();
		
		handleCombo = true;
	}
	
	void updateAttributeCombo(){
		handleCombo = false;
		AttributeObject object = (AttributeObject)cmbShape.getSelectedItem();
		cmbAttribute.removeAllItems();
		if (object != null){
			AttributeList list = object.getAttributes();
			ArrayList<Attribute<?>> attributes = list.getAsList();
			
			for (int i = 0; i < attributes.size(); i++)
				cmbAttribute.addItem(attributes.get(i));
			}
		
		if (task != null){
			Attribute<?> attr = object.getAttribute(task.attribute);
			cmbAttribute.setSelectedItem(attr);
			if (!task.incremental){
				setCurrentAttribute(attr, task.start_value);
			}else{
				setCurrentAttribute(attr, task.start_value, task.end_value);
				}
			}
		
		handleCombo = true;
		
	}

	void setCurrentAttribute(Attribute<?> attr, Object value){
		
		if (attr == null) return;
		updateControls(attr, value);
		validate();
		updateUI();
	}
	
	void setCurrentAttribute(Attribute<?> attr, Object start, Object end){
		
		if (attr == null) return;
		updateControls(attr, start, end);
		validate();
		updateUI();
	}
	
	void addShapesToCombo(ShapeSet3DInt set){
		cmbShape.addItem(set);
		for (int i = 0; i < set.members.size(); i++)
			if (set.members.get(i) instanceof ShapeSet3DInt)
				addShapesToCombo((ShapeSet3DInt)set.members.get(i));
			else
				cmbShape.addItem(set.members.get(i));
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Iterate Changed")){
			txtEndValue.setEnabled(chkEndValue.isSelected());
			txtStep.setEnabled(chkEndValue.isSelected());
			return;
			}
		
		if (e.getActionCommand().equals("Shape Combo Changed")){
			if (!handleCombo) return;
			updateAttributeCombo();
			return;
			}

		if (e.getActionCommand().equals("Attribute Combo Changed")){
			if (!handleCombo) return;
			Attribute<?> attr = (Attribute<?>)cmbAttribute.getSelectedItem();
			setCurrentAttribute(attr, attr.getValue());
			return;
			}
		
	}


	
}