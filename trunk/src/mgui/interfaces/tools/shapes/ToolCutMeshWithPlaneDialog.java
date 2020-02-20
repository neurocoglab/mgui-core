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
import javax.swing.JTextField;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.SectionSet3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.interfaces.tools.dialogs.DialogToolDialogBox;


public class ToolCutMeshWithPlaneDialog extends DialogToolDialogBox {

	JLabel lblMesh = new JLabel("Mesh:");
	JComboBox cmbMesh = new JComboBox();
	JLabel lblSectionSet = new JLabel("Section Set:");
	JComboBox cmbSectionSet = new JComboBox();
	JLabel lblSection = new JLabel("Section:");
	JTextField txtSection = new JTextField("0");
	JLabel lblPrefix = new JLabel("Name prefix:");
	JTextField txtPrefix = new JTextField("mesh_cut_by_plane");
	JCheckBox chkRetainOriginal = new JCheckBox(" Retain original");
	JLabel lblShapeSet = new JLabel("Add to Shape Set:");
	JComboBox cmbShapeSet = new JComboBox();
	
	public ToolCutMeshWithPlaneDialog(JFrame frame, InterfaceOptions options){
		super(frame, options);
		_init();
	}
	
	private void _init(){
		
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		init();
		LineLayout lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,300);
		this.setTitle("Cut Mesh3D with Plane");
		
		cmbMesh.addActionListener(this);
		cmbMesh.setActionCommand("Mesh Changed");
		
		chkRetainOriginal.setSelected(true);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.35, 1);
		mainPanel.add(lblMesh, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.55, 1);
		mainPanel.add(cmbMesh, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.35, 1);
		mainPanel.add(lblSectionSet, c);
		c = new LineLayoutConstraints(2, 2, 0.4, 0.55, 1);
		mainPanel.add(cmbSectionSet, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.35, 1);
		mainPanel.add(lblSection, c);
		c = new LineLayoutConstraints(3, 3, 0.4, 0.55, 1);
		mainPanel.add(txtSection, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.35, 1);
		mainPanel.add(lblPrefix, c);
		c = new LineLayoutConstraints(4, 4, 0.4, 0.55, 1);
		mainPanel.add(txtPrefix, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.9, 1);
		mainPanel.add(chkRetainOriginal, c);
		c = new LineLayoutConstraints(6, 6, 0.05, 0.35, 1);
		mainPanel.add(lblShapeSet, c);
		c = new LineLayoutConstraints(6, 6, 0.4, 0.55, 1);
		mainPanel.add(cmbShapeSet, c);
				
		updateCombos();
		
	}
	
	void updateCombos(){
		
		//display_panel.populateShapeSetCombo(cmbPolylinesShapeSet);
		InterfaceSession.getWorkspace().populateShapeSetCombo(cmbShapeSet);
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbSectionSet, new SectionSet3DInt());
		InterfaceSession.getWorkspace().populateShapeTypeCombo(cmbMesh, new Mesh3DInt());
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Mesh Changed")){
			if (cmbMesh.getSelectedItem() == null) return;
			txtPrefix.setText(((Mesh3DInt)cmbMesh.getSelectedItem()).getName() + "_cut");
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			ToolCutMeshWithPlaneOptions _options = (ToolCutMeshWithPlaneOptions)options;
			
			_options.mesh = (Mesh3DInt)cmbMesh.getSelectedItem();
			_options.target_set = (ShapeSet3DInt)cmbShapeSet.getSelectedItem();
			_options.prefix = txtPrefix.getText();
			_options.retain_original = chkRetainOriginal.isSelected();
			SectionSet3DInt set = (SectionSet3DInt)cmbSectionSet.getSelectedItem();
			int section = Integer.valueOf(txtSection.getText());
			_options.cut_plane = set.getPlaneAt(section);
			
			setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
	
}