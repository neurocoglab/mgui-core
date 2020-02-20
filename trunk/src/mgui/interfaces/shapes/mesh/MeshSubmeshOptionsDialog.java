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

package mgui.interfaces.shapes.mesh;

import mgui.interfaces.InterfaceOptionsDialogBox;


public class MeshSubmeshOptionsDialog extends InterfaceOptionsDialogBox {

	/*
	InterfaceDialogUpdater updater;
	LineLayout lineLayout;
	
	JCheckBox chkRetain = new JCheckBox("Retain value:"); 
	JCheckBox chkRemove = new JCheckBox("Remove value:");
	JTextField txtValue = new JTextField("0"); 
	
	JLabel lblSubVal = new JLabel("Sub value:");
	JTextField txtSubVal = new JTextField("-1"); 
	JLabel lblMidVal = new JLabel("Mid value:");
	JTextField txtMidVal = new JTextField("0"); 
	JLabel lblSuperVal = new JLabel("Supra value:");
	JTextField txtSuperVal = new JTextField("1"); 
	
	boolean doUpdate = true;
	
	public MeshSubmeshOptionsDialog(){
		super();
	}
	
	public MeshSubmeshOptionsDialog(JFrame aFrame, MeshDataSubmeshOptions options){
		super(aFrame, options);
		
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		init();
		setLocationRelativeTo(aFrame);
		
	}
	
	protected void init(){
		
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,300);
		this.setTitle("Mesh Threshold Operation Options");
		
		chkMin.setActionCommand("Min");
		chkMin.addActionListener(this);
		chkMin.setSelected(true);
		chkMax.setActionCommand("Max");
		chkMax.addActionListener(this);
		chkMax.setSelected(true);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.3, 1);
		mainPanel.add(chkMin, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.55, 1);
		mainPanel.add(txtMin, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.3, 1);
		mainPanel.add(chkMax, c);
		c = new LineLayoutConstraints(2, 2, 0.4, 0.55, 1);
		mainPanel.add(txtMax, c);
		
		c = new LineLayoutConstraints(3, 3, 0.05, 0.3, 1);
		mainPanel.add(lblSubVal, c);
		c = new LineLayoutConstraints(3, 3, 0.4, 0.55, 1);
		mainPanel.add(txtSubVal, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.3, 1);
		mainPanel.add(lblMidVal, c);
		c = new LineLayoutConstraints(4, 4, 0.4, 0.55, 1);
		mainPanel.add(txtMidVal, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.3, 1);
		mainPanel.add(lblSuperVal, c);
		c = new LineLayoutConstraints(5, 5, 0.4, 0.55, 1);
		mainPanel.add(txtSuperVal, c);
		
		updateMaxMin();
	}
	
	protected void updateMaxMin(){
		txtMax.setEnabled(chkMax.isSelected());
		txtMin.setEnabled(chkMin.isSelected());
	}
	
	public void actionPerformed(ActionEvent e){
		
		super.actionPerformed(e);
		
		if (e.getActionCommand().equals("Max") && doUpdate){
			doUpdate = false;
			if (!chkMax.isSelected()) chkMin.setSelected(true);
			updateMaxMin();
			doUpdate = true;
			}
		
		if (e.getActionCommand().equals("Min") && doUpdate){
			doUpdate = false;
			if (!chkMin.isSelected()) chkMax.setSelected(true);
			updateMaxMin();
			doUpdate = true;
			}
		
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			MeshDataThresholdOptions ops = (MeshDataThresholdOptions)options;
			ops.max = Double.valueOf(txtMax.getText());
			ops.min = Double.valueOf(txtMin.getText());
			ops.subVal = Double.valueOf(txtSubVal.getText());
			ops.midVal = Double.valueOf(txtMidVal.getText());
			ops.superVal = Double.valueOf(txtSuperVal.getText());
			setVisible(false);
			}
		
	}
	*/
	
	
}