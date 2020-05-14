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
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.dialogs.DialogToolDialogBox;


public class ToolGetMeshIntersectionPolylinesDialog extends DialogToolDialogBox {

	JLabel lblMesh1 = new JLabel("Mesh 1:");
	JComboBox cmbMesh1 = new JComboBox();
	JLabel lblMesh2 = new JLabel("Mesh 2:");
	JComboBox cmbMesh2 = new JComboBox();
	JLabel lblName = new JLabel("Name of polygon set:");
	JTextField txtName = new JTextField("intersection_polylines");
	JLabel lblShapeSet = new JLabel("Add to Shape Set:");
	JComboBox cmbShapeSet = new JComboBox();
	
	public ToolGetMeshIntersectionPolylinesDialog(JFrame frame, InterfaceOptions options){
		super(frame, options);
		_init();
	}
	
	private void _init(){
		
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		init();
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,300);
		this.setTitle("Get Mesh Intersection Polylines");
		
		cmbMesh1.addActionListener(this);
		cmbMesh1.setActionCommand("Mesh Changed");
		cmbMesh2.addActionListener(this);
		cmbMesh2.setActionCommand("Mesh Changed");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.35, 1);
		mainPanel.add(lblMesh1, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.55, 1);
		mainPanel.add(cmbMesh1, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.35, 1);
		mainPanel.add(lblMesh2, c);
		c = new LineLayoutConstraints(2, 2, 0.4, 0.55, 1);
		mainPanel.add(cmbMesh2, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.35, 1);
		mainPanel.add(lblName, c);
		c = new LineLayoutConstraints(3, 3, 0.4, 0.55, 1);
		mainPanel.add(txtName, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.35, 1);
		mainPanel.add(lblShapeSet, c);
		c = new LineLayoutConstraints(4, 4, 0.4, 0.55, 1);
		mainPanel.add(cmbShapeSet, c);
	
		updateCombos();
	}
	
	void updateCombos(){
		
		InterfaceSession.getWorkspace().populateShapeSetCombo(cmbShapeSet);
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbMesh1, new Mesh3DInt());
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbMesh2, new Mesh3DInt());
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Mesh Changed")){
			if (cmbMesh1.getSelectedItem() == null || cmbMesh2.getSelectedItem() == null) return;
			txtName.setText("intersection_polylines_" + ((Mesh3DInt)cmbMesh1.getSelectedItem()).getName() + 
							"_" + ((Mesh3DInt)cmbMesh2.getSelectedItem()).getName() );
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			ToolGetMeshIntersectionPolylinesOptions _options = (ToolGetMeshIntersectionPolylinesOptions)options;
			_options.mesh1 = (Mesh3DInt)cmbMesh1.getSelectedItem();
			_options.mesh2 = (Mesh3DInt)cmbMesh2.getSelectedItem();
			_options.target_set = (ShapeSet3DInt)cmbShapeSet.getSelectedItem();
			_options.name = txtName.getText();
			
			setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
	
}