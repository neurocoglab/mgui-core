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

package mgui.io.domestic.shapes;

import java.awt.event.ActionEvent;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;


public class MeshOptionsDialogBox extends InterfaceIODialogBox {

	JLabel lblMesh = new JLabel("Apply to mesh:");
	JComboBox cmbMesh = new JComboBox();
		
	protected LineLayout lineLayout;
	protected Mesh3DInt currentMesh;
	boolean doUpdate;
	
	public MeshOptionsDialogBox(){
		
	}
	
	public MeshOptionsDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
	}

	@Override
	protected void init(){
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,300);
		
		cmbMesh.setActionCommand("Mesh Changed");
		cmbMesh.addActionListener(this);
		
		fillMeshCombo();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		mainPanel.add(lblMesh, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(cmbMesh, c);
		
		
	}
	
	protected void fillMeshCombo(){
		
		doUpdate = false;
		cmbMesh.removeAllItems();
		
		ShapeSet3DInt meshes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
		for (int i = 0; i < meshes.members.size(); i++)
			cmbMesh.addItem(meshes.members.get(i));
		
		if (currentMesh != null)
			currentMesh = (Mesh3DInt)cmbMesh.getSelectedItem();
		else
			cmbMesh.setSelectedItem(currentMesh);
		doUpdate = true;
	}
	
	protected void meshChanged(){
		currentMesh = (Mesh3DInt)cmbMesh.getSelectedItem();
	}
	
	public Mesh3DInt getMesh(){
		if (cmbMesh.getSelectedItem() != null)
			return (Mesh3DInt)cmbMesh.getSelectedItem();
		return null;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		super.actionPerformed(e);
		
		if (e.getActionCommand().equals("Mesh Changed") && doUpdate)
			meshChanged();
		
		
		
	}
	
}