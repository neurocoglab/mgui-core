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

package mgui.interfaces.shapes.mesh;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;


public class MeshPlaneMaskOptionsDialog extends InterfaceOptionsDialogBox {

	JLabel lblSectionSet = new JLabel("Section set:");
	JComboBox cmbSectionSet = new JComboBox();
	JLabel lblSection = new JLabel("Section:");
	JTextField txtSection = new JTextField("0");
	
	JLabel lblAbove = new JLabel("Value above:");
	JTextField txtAbove = new JTextField("1"); 
	JLabel lblBelow = new JLabel("Value below:");
	JTextField txtBelow = new JTextField("0"); 
	
	public MeshPlaneMaskOptionsDialog(JFrame aFrame, MeshPlaneMaskOptions options, InterfacePanel panel){
		super(aFrame, options);
		parentPanel = panel;
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		_init();
	}
	
	private void _init(){
		super.init();
		
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,380);
		this.setTitle("Mesh Mask With Plane Options");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.3, 1);
		mainPanel.add(lblSectionSet, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.55, 1);
		mainPanel.add(cmbSectionSet, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.3, 1);
		mainPanel.add(lblSection, c);
		c = new LineLayoutConstraints(2, 2, 0.4, 0.55, 1);
		mainPanel.add(txtSection, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.3, 1);
		mainPanel.add(lblAbove, c);
		c = new LineLayoutConstraints(3, 3, 0.4, 0.55, 1);
		mainPanel.add(txtAbove, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.3, 1);
		mainPanel.add(lblBelow, c);
		c = new LineLayoutConstraints(4, 4, 0.4, 0.55, 1);
		mainPanel.add(txtBelow, c);
		
		updateCombo();
		updatePanel();
		
	}
	
	void updatePanel(){
		MeshPlaneMaskOptions _options = (MeshPlaneMaskOptions)options;
		
		if (_options.section_set != null)
			cmbSectionSet.setSelectedItem(_options.section_set);
		txtSection.setText("" + _options.section);
		txtAbove.setText("" + _options.above);
		txtBelow.setText("" + _options.below);
	}
	
	void updateCombo(){
		
		cmbSectionSet.removeAllItems();
		
		ShapeSet3DInt model_set = InterfaceSession.getDisplayPanel().getCurrentShapeSet();
		List<Shape3DInt> section_sets = model_set.getShapeType(new SectionSet3DInt());
		
		for (Shape3DInt set : section_sets) {
			cmbSectionSet.addItem(set);
			}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		super.actionPerformed(e);
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			MeshPlaneMaskOptions _options = (MeshPlaneMaskOptions)options;
			
			_options.section_set = (SectionSet3DInt)cmbSectionSet.getSelectedItem();
			_options.section = Integer.valueOf(txtSection.getText());
			_options.above = Integer.valueOf(txtAbove.getText());
			_options.below = Integer.valueOf(txtBelow.getText());
			this.setVisible(false);
			return;
			}
		
	}
	
}