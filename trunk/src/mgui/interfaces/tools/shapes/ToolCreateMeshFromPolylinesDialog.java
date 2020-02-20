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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.attributes.table.InterfaceAttributeTable;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.dialogs.DialogToolDialogBox;


public class ToolCreateMeshFromPolylinesDialog extends DialogToolDialogBox {

	JLabel lblPolylinesSectionSet = new JLabel("SectionSet3DInt:");
	JComboBox cmbPolylinesSectionSet = new JComboBox();
	
	JLabel lblMeshName = new JLabel("Name of new mesh:");
	JTextField txtMeshName = new JTextField("mesh from polylines");
	
	JCheckBox chkResample = new JCheckBox(" Resample to min segment:");
	JTextField txtResample = new JTextField("5");
	
	JLabel lblShapeSet = new JLabel("Add to Shape Set:");
	JComboBox cmbShapeSet = new JComboBox();
	
	JLabel lblAttributes = new JLabel("Mesh attributes:");
	InterfaceAttributeTable attrMesh;
	JScrollPane scrMesh;
	
	boolean update = true;
	
	public ToolCreateMeshFromPolylinesDialog(JFrame frame, InterfaceOptions options){
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
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.35, 1);
		mainPanel.add(lblPolylinesSectionSet, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.55, 1);
		mainPanel.add(cmbPolylinesSectionSet, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.35, 1);
		mainPanel.add(lblMeshName, c);
		c = new LineLayoutConstraints(2, 2, 0.4, 0.55, 1);
		mainPanel.add(txtMeshName, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.35, 1);
		mainPanel.add(lblShapeSet, c);
		c = new LineLayoutConstraints(3, 3, 0.4, 0.55, 1);
		mainPanel.add(cmbShapeSet, c);
		
		updateCombos();
		
	}
	
	void updateCombos(){
		
		//display_panel.populateShapeSetCombo(cmbPolylinesShapeSet);
		InterfaceSession.getWorkspace().populateShapeSetCombo(cmbShapeSet);
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbPolylinesSectionSet, new SectionSet3DInt());
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			//if (options == null) return;
			//set options..
			ToolCreateMeshFromPolylinesOptions _options = (ToolCreateMeshFromPolylinesOptions)options;
			
			_options.source_set = (SectionSet3DInt)cmbPolylinesSectionSet.getSelectedItem();
			_options.target_set = (ShapeSet3DInt)cmbShapeSet.getSelectedItem();
			_options.name = txtMeshName.getText();
			
			setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
	
}