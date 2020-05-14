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

package mgui.interfaces.shapes.volume;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceOptions;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;


public class VolumeMaskDialog extends InterfaceOptionsDialogBox {

	JLabel lblCurrentVolume = new JLabel("Current volume:");
	JTextField txtCurrentVolume = new JTextField();
	JLabel lblMask = new JLabel("Mask:");
	JComboBox cmbMask = new JComboBox();
	JLabel lblName = new JLabel("Name:");
	JTextField txtName = new JTextField();
	JCheckBox chkActive = new JCheckBox(" Active");
	
	JCheckBox chkInvert = new JCheckBox(" Invert");
	JLabel lblMergeFile = new JLabel("Merge with file:");
	JButton cmdMergeFileBrowse = new JButton("Browse..");
	JButton cmdMergeFileApply = new JButton("Apply");
	JLabel lblMergeOther = new JLabel("Merge with volume:");
	JLabel lblMergeOtherVolume = new JLabel("Volume: ");
	JButton cmdMergeOtherApply = new JButton("Apply");
	JComboBox cmbMergeOtherVolume = new JComboBox();
	JCheckBox chkMergeOtherMask = new JCheckBox("Mask:");
	JComboBox cmbMergeOtherMask = new JComboBox();
	JLabel lblMergeShape = new JLabel("Merge with shape:");
	JComboBox cmbMergeShape = new JComboBox();
	JButton cmdMergeShapeApply = new JButton("Apply");
	
	JButton cmdMaskClear = new JButton("Clear mask");
	JButton cmdMaskInvert = new JButton("Invert mask");
	JButton cmdMaskRemove = new JButton("Remove mask");
	JButton cmdMaskAddUpdate = new JButton("Add mask");
	
	boolean[][][] current_mask;
	
	public VolumeMaskDialog(){
		
	}
	
	public VolumeMaskDialog(JFrame aFrame, InterfaceOptions options){
		super(aFrame, options);
		_init();
	}
	
	void _init(){
		this.setButtonType(InterfaceDialogBox.BT_OK);
		super.init();
		
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		setDialogSize(450,500);
		setTitle("Create/Edit Volume3D Masks");
		
		txtCurrentVolume.setEnabled(false);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		mainPanel.add(lblCurrentVolume, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(txtCurrentVolume, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.24, 1);
		mainPanel.add(lblMask, c);
		c = new LineLayoutConstraints(2, 2, 0.3, 0.65, 1);
		mainPanel.add(cmbMask, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.24, 1);
		mainPanel.add(lblName, c);
		c = new LineLayoutConstraints(3, 3, 0.3, 0.65, 1);
		mainPanel.add(txtName, c);
		c = new LineLayoutConstraints(4, 4, 0.35, 0.6, 1);
		mainPanel.add(chkActive, c);
		
		c = new LineLayoutConstraints(5, 5, 0.05, 0.3, 1);
		mainPanel.add(lblMergeFile, c);
		c = new LineLayoutConstraints(5, 5, 0.35, 0.3, 1);
		mainPanel.add(cmdMergeFileBrowse, c);
		c = new LineLayoutConstraints(5, 5, 0.65, 0.3, 1);
		mainPanel.add(cmdMergeFileApply, c);
		
		c = new LineLayoutConstraints(6, 6, 0.05, 0.3, 1);
		mainPanel.add(lblMergeOther, c);
		c = new LineLayoutConstraints(6, 6, 0.65, 0.3, 1);
		mainPanel.add(cmdMergeOtherApply, c);
		c = new LineLayoutConstraints(7, 7, 0.1, 0.2, 1);
		mainPanel.add(lblMergeOtherVolume, c);
		lblMergeOtherVolume.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(7, 7, 0.35, 0.6, 1);
		mainPanel.add(cmbMergeOtherVolume, c);
		c = new LineLayoutConstraints(8, 8, 0.1, 0.2, 1);
		mainPanel.add(chkMergeOtherMask, c);
		chkMergeOtherMask.setHorizontalAlignment(SwingConstants.RIGHT);
		c = new LineLayoutConstraints(8, 8, 0.35, 0.6, 1);
		mainPanel.add(cmbMergeOtherMask, c);
		
		c = new LineLayoutConstraints(9, 9, 0.05, 0.3, 1);
		mainPanel.add(lblMergeShape, c);
		c = new LineLayoutConstraints(9, 9, 0.35, 0.3, 1);
		mainPanel.add(cmbMergeShape, c);
		c = new LineLayoutConstraints(9, 9, 0.65, 0.3, 1);
		mainPanel.add(cmdMergeShapeApply, c);
		
		//room for shape parameters
		
		c = new LineLayoutConstraints(13, 13, 0.35, 0.3, 1);
		mainPanel.add(cmdMaskClear, c);
		c = new LineLayoutConstraints(13, 13, 0.65, 0.3, 1);
		mainPanel.add(cmdMaskInvert, c);
		c = new LineLayoutConstraints(14, 14, 0.35, 0.3, 1);
		mainPanel.add(cmdMaskRemove, c);
		c = new LineLayoutConstraints(14, 14, 0.65, 0.3, 1);
		mainPanel.add(cmdMaskAddUpdate, c);
		
		
	}
	
	
	
	
	
	
}