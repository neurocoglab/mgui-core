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

package mgui.interfaces.tools.shapes;

import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.PointSet2DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.Shape2DInt;
import mgui.interfaces.shapes.ShapeSet2DInt;
import mgui.interfaces.tools.dialogs.DialogToolDialogBox;


public class ToolGetConvexHull2DDialog extends DialogToolDialogBox {

	JLabel lblSectionSet = new JLabel("Section set:");
	JComboBox cmbSectionSet = new JComboBox();
	JLabel lblPointSet = new JLabel("Point set:");
	JComboBox cmbPointSet = new JComboBox();
	JLabel lblName = new JLabel("Name of hull:");
	JTextField txtName = new JTextField("convex hull");
	
	public ToolGetConvexHull2DDialog(JFrame frame, InterfaceOptions options){
		super(frame, options);
		_init();
	}
	
	private void _init(){
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		init();
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,300);
		this.setTitle("Create Mesh3D From Polylines");
		
		cmbSectionSet.addActionListener(this);
		cmbSectionSet.setActionCommand("Section Set Changed");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.35, 1);
		mainPanel.add(lblSectionSet, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.55, 1);
		mainPanel.add(cmbSectionSet, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.35, 1);
		mainPanel.add(lblPointSet, c);
		c = new LineLayoutConstraints(2, 2, 0.4, 0.55, 1);
		mainPanel.add(cmbPointSet, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.35, 1);
		mainPanel.add(lblName, c);
		c = new LineLayoutConstraints(3, 3, 0.4, 0.55, 1);
		mainPanel.add(txtName, c);
		
		updateSectionSetCombo();
		
	}
	
	void updateSectionSetCombo(){
		
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbSectionSet, new SectionSet3DInt());
		
	}
	
	void updatePointSetCombo(){
		
		cmbPointSet.removeAllItems();
		SectionSet3DInt section_set = (SectionSet3DInt)cmbSectionSet.getSelectedItem();
		if (section_set == null) return;
		
		ShapeSet2DInt shape_set = section_set.getShape2DType(new PointSet2DInt(), true);
		for (Shape2DInt shape : shape_set.members)
			cmbPointSet.addItem(shape);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			ToolGetConvexHull2DOptions _options = (ToolGetConvexHull2DOptions)options;
			
			_options.section_set = (SectionSet3DInt)cmbSectionSet.getSelectedItem();
			_options.point_set = (PointSet2DInt)cmbPointSet.getSelectedItem();
			if (_options.section_set != null && _options.point_set != null)
				_options.section = _options.section_set.getSectionForShape(_options.point_set);
			_options.name = txtName.getText();
			
			setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals("Section Set Changed")){
			updatePointSetCombo();
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
	
}