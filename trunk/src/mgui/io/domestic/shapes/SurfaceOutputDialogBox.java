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

package mgui.io.domestic.shapes;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.Mesh3DInt;
import mgui.interfaces.shapes.Shape3DInt;
import mgui.interfaces.shapes.ShapeSet3DInt;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;


/*****************
 * @author Andrew Reid
 *
 */
public class SurfaceOutputDialogBox extends InterfaceIODialogBox {

	//controls
	JLabel lblMesh = new JLabel("Mesh to output:");
	protected JComboBox cmbMesh = new JComboBox();
		
	LineLayout lineLayout;
	
	public SurfaceOutputDialogBox(){
		super();
		//init();
	}
	
	public SurfaceOutputDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions opts){
		super(frame, panel, opts);
		s_init();
	}
	
	protected void s_init(){
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,300);
		this.setTitle("Surface Out Options Dialog");
		
		fillMeshCombo();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		mainPanel.add(lblMesh, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(cmbMesh, c);
		
	}
	
	protected void fillMeshCombo(){
		cmbMesh.removeAllItems();
		
		if (options == null) return;
		
		SurfaceOutputOptions _options = (SurfaceOutputOptions)options;
		
		List<Shape3DInt> meshes = InterfaceSession.getDisplayPanel().getCurrentShapeSet().getShapeType(new Mesh3DInt());
		for (Shape3DInt mesh : meshes) {
			cmbMesh.addItem(mesh);
			}
		
		if (_options.mesh != null)
			cmbMesh.setSelectedItem(_options.mesh);
	}
	
	public Mesh3DInt getMesh(){
		if (cmbMesh.getSelectedItem() != null)
			return (Mesh3DInt)cmbMesh.getSelectedItem();
		return null;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			((SurfaceOutputOptions)options).mesh = (Mesh3DInt)cmbMesh.getSelectedItem();
			cmbMesh.removeAllItems();
			this.setVisible(false);
			}
		
		super.actionPerformed(e);
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
}