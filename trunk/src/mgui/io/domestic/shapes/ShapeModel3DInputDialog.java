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
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.shapes.ShapeModel3D;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;

/*******************************************************
 * Dialog box for specifying options to load a {@code ShapeModel3D} object into the workspace.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ShapeModel3DInputDialog extends InterfaceIODialogBox {

	JCheckBox chkMergeModel = new JCheckBox(" Merge with existing model:");
	InterfaceComboBox cmbMergeModel = new InterfaceComboBox(RenderMode.LongestItem,
															true,
															100);
	
	JLabel lblConflicting = new JLabel("For conflicting shape names:");
	JComboBox cmbConflicting = new JComboBox();
	
	ShapeModel3DInputOptions temp_options = new ShapeModel3DInputOptions();
	
	public ShapeModel3DInputDialog(){
		super();
	}
	
	public ShapeModel3DInputDialog(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		temp_options = (ShapeModel3DInputOptions)options;
		init();
		setLocationRelativeTo(frame);
	}

	@Override
	protected void init(){
		this.setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		
		super.init();
		
		chkMergeModel.addActionListener(this);
		chkMergeModel.setActionCommand("Merge Model Changed");
				
		LineLayout lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		
		this.setMainLayout(lineLayout);
		this.setDialogSize(600,200);
		this.setTitle("Shape Model 3D Input Options");
		
		initCombos();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.4, 1);
		mainPanel.add(chkMergeModel, c);
		c = new LineLayoutConstraints(1, 1, 0.48, 0.47, 1);
		mainPanel.add(cmbMergeModel, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.4, 1);
		mainPanel.add(lblConflicting, c);
		c = new LineLayoutConstraints(2, 2, 0.48, 0.47, 1);
		mainPanel.add(cmbConflicting, c);
		
		updateDialog();
	}
	
	@Override
	public boolean updateDialog(){
		
		chkMergeModel.setSelected(temp_options.merge_model_set);
		if (temp_options.merge_with_model != null)
			cmbMergeModel.setSelectedItem(temp_options.merge_with_model);
		else
			cmbMergeModel.setSelectedItem(0);
		
		cmbConflicting.setSelectedItem(temp_options.existing_shapes);
		
		updateControls();

		return true;
	}
	
	protected void updateControls(){
		
		cmbMergeModel.setEnabled(chkMergeModel.isSelected());
		
	}
	
	private void initCombos(){
		
		cmbMergeModel.removeAllItems();
		ArrayList<ShapeModel3D> models = InterfaceSession.getWorkspace().getShapeModels();
		for (int i = 0; i < models.size(); i++)
			cmbMergeModel.addItem(models.get(i));
		
		cmbConflicting.removeAllItems();
		
		cmbConflicting.addItem("Rename");
		cmbConflicting.addItem("Overwrite");
		cmbConflicting.addItem("Skip");
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			if (temp_options == null){
				setVisible(false);
				return;
				}
			
			temp_options.existing_shapes = (String)cmbConflicting.getSelectedItem();
			temp_options.merge_model_set = chkMergeModel.isSelected();
			temp_options.merge_with_model = (ShapeModel3D)cmbMergeModel.getSelectedItem();
			setVisible(false);
			return;
			
			}
		
		if (e.getActionCommand().equals("Merge Model Changed")){
			updateControls();
			return;
		}
		
		
		
		super.actionPerformed(e);
	}
	
}