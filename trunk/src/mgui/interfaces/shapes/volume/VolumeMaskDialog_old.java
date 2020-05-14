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

import java.awt.event.ActionEvent;
import java.util.HashMap;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceDialogUpdater;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.maps.ValueMap;
import mgui.numbers.MguiDouble;


/********************************
 * Provides several dialog box for setting volume masking parameters. These include
 * paramters describing the following mask shapes:
 * [ul]
 * [li]Entire volume[/li]
 * [li]Box - a box constrained by six parameters: x_min, x_max, etc.[/li]
 * [li]Wedge - a wedge constrained by ?[/li]
 * [li]Shere - a sphere constrained by x, y, z and radius[/li]
 * [/ul]
 * 
 * Edge smoothing is also supported, by specifying a width, in pixels, and a decay
 * exponent.
 * 
 * @author Andrew Reid
 * @version 1.0
 */

public class VolumeMaskDialog_old extends InterfaceDialogBox {
	
	protected VolumeMaskPanel pMaskAll;
	protected VolumeMaskPanel pMaskBox;
	protected VolumeMaskPanel pMaskWedge;
	protected VolumeMaskPanel pMaskSphere;
	protected VolumeMaskPanel pMaskAxis1;
	protected VolumeMaskPanel pMaskAxis2;
	protected VolumeMaskPanel pMaskAxis3;
	
	public VolumeMaskOptions_old options;
	protected int[] dataDims;
	
	ValueMap panels = new ValueMap();
	
	//general parameter components
	JLabel lblGeneralParams = new JLabel("GENERAL PARAMETERS");
	JLabel lblInputType = new JLabel("Input type:");
	JComboBox cmbInputType = new JComboBox();
	JLabel lblInputChannel = new JLabel("Input channel:");
	JComboBox cmbInputChannel = new JComboBox();
	JLabel lblInputValue = new JLabel("Input value (0 to 1):");
	JTextField txtInputValue = new JTextField("0.0");
	JLabel lblInputOp = new JLabel("Input operation:");
	JComboBox cmbInputOp = new JComboBox();
	JLabel lblInputFactor = new JLabel("Input factor:");
	JTextField txtInputFactor = new JTextField("1.0");
	JLabel lblOutputChannel = new JLabel("Output channel:");
	JComboBox cmbOutputChannel = new JComboBox();
	JLabel lblMaskShape = new JLabel("Mask shape:");
	JComboBox cmbMaskShape = new JComboBox();
	JLabel lblMinThreshold = new JLabel("Min threshold (0 to 1):");
	JTextField txtMinThreshold = new JTextField("0.0");
	JLabel lblMaxThreshold = new JLabel("Max threshold (0 to 1):");
	JTextField txtMaxThreshold = new JTextField("1.0");
	JLabel lblSmoothingExp = new JLabel("Smoothing exp:");
	JTextField txtSmoothingExp = new JTextField("1.0");
	JLabel lblInvert = new JLabel("Invert");
	JCheckBox chkInvertData = new JCheckBox();
	JCheckBox chkInvertShape = new JCheckBox();
	JCheckBox chkInvertMin = new JCheckBox();
	JCheckBox chkInvertMax = new JCheckBox();
	
	JLabel lblShapeParams = new JLabel("SHAPE PARAMETERS");
	
	//Axis Controls
	JLabel lblAxis1 = new JLabel("Axis 1:");
	JComboBox cmbAxis1 = new JComboBox();
	JLabel lblAxisPos1 = new JLabel("Pos 1:");
	JTextField txtAxisPos1 = new JTextField("0");
	JLabel lblAxisWidth1 = new JLabel("Width 1:");
	JTextField txtAxisWidth1 = new JTextField("1");
	JLabel lblAxis2 = new JLabel("Axis 2:");
	JComboBox cmbAxis2 = new JComboBox();
	JLabel lblAxisPos2 = new JLabel("Pos 2:");
	JTextField txtAxisPos2 = new JTextField("0");
	JLabel lblAxisWidth2 = new JLabel("Width 2:");
	JTextField txtAxisWidth2 = new JTextField("1");
	JLabel lblAxis3 = new JLabel("Axis 3:");
	JComboBox cmbAxis3 = new JComboBox();
	JLabel lblAxisPos3 = new JLabel("Pos 3:");
	JTextField txtAxisPos3 = new JTextField("0");
	JLabel lblAxisWidth3 = new JLabel("Width 3:");
	JTextField txtAxisWidth3 = new JTextField("1");
	
	int generalSize = 12;
	
	public VolumeMaskDialog_old(){
		super();
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		init();
		this.setLocation(300, 300);
	}
	
	//TODO implement InterfaceDialogUpdater method
	public VolumeMaskDialog_old(JFrame aFrame, InterfaceDialogUpdater parent){
		super(aFrame, parent);
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		init();
		setLocationRelativeTo(aFrame);
		this.setLocation(300, 300);
		if (updater instanceof InterfaceVolumePanel){
			parentPanel = (InterfaceVolumePanel)updater;
		//if (parentPanel instanceof InterfaceVolumePanel)
			options = ((InterfaceVolumePanel)parentPanel).maskOptions;
			}
	}
	
	protected void init(){
		super.init();
		
		//get data dimensions if parent panel is set
		setDataDims();
		setGeneral();
		
		
		//listeners
		cmbInputType.addActionListener(this);
		cmbInputType.setActionCommand("Type changed");
		cmbMaskShape.addActionListener(this);
		cmbMaskShape.setActionCommand("Shape changed");
		
		//set up specific panels
		int start = generalSize;
		
		//ENTIRE
		pMaskAll = new VolumeMaskPanel();
		pMaskAll.setLayout(new LineLayout(20, 5, 0));
		
		//BOX
		pMaskBox = new VolumeMaskPanel();
		pMaskBox.setLayout(new LineLayout(20, 5, 0));
		
		JLabel lblMinCoords = new JLabel("Minimum");
		JLabel lblXMin = new JLabel("X:");
		JTextField txtXMin = new JTextField("0");
		JLabel lblYMin = new JLabel("Y:");
		JTextField txtYMin = new JTextField("0");
		JLabel lblZMin = new JLabel("Z:");
		JTextField txtZMin = new JTextField("0");
		
		JLabel lblMaxCoords = new JLabel("Maximum");
		JLabel lblXMax = new JLabel("X:");
		JTextField txtXMax = new JTextField("0");
		JLabel lblYMax = new JLabel("Y:");
		JTextField txtYMax = new JTextField("0");
		JLabel lblZMax = new JLabel("Z:");
		JTextField txtZMax = new JTextField("0");
		
		//add components
		LineLayoutConstraints c = new LineLayoutConstraints(start + 1, start + 1, 0.05, 0.5, 1);
		pMaskBox.add(lblMinCoords, c);
		c = new LineLayoutConstraints(start + 2, start + 2, 0.06, 0.1, 1);
		pMaskBox.add(lblXMin, c);
		c = new LineLayoutConstraints(start + 2, start + 2, 0.15, 0.17, 1);
		pMaskBox.addControl("txtXMin", txtXMin, c);
		c = new LineLayoutConstraints(start + 2, start + 2, 0.35, 0.1, 1);
		pMaskBox.add(lblYMin, c);
		c = new LineLayoutConstraints(start + 2, start + 2, 0.45, 0.17, 1);
		pMaskBox.addControl("txtYMin", txtYMin, c);
		c = new LineLayoutConstraints(start + 2, start + 2, 0.65, 0.1, 1);
		pMaskBox.add(lblZMin, c);
		c = new LineLayoutConstraints(start + 2, start + 2, 0.75, 0.17, 1);
		pMaskBox.addControl("txtZMin", txtZMin, c);
		
		c = new LineLayoutConstraints(start + 3, start + 3, 0.05, 0.5, 1);
		pMaskBox.add(lblMaxCoords, c);
		c = new LineLayoutConstraints(start + 4, start + 4, 0.06, 0.1, 1);
		pMaskBox.add(lblXMax, c);
		c = new LineLayoutConstraints(start + 4, start + 4, 0.15, 0.17, 1);
		pMaskBox.addControl("txtXMax", txtXMax, c);
		c = new LineLayoutConstraints(start + 4, start + 4, 0.35, 0.1, 1);
		pMaskBox.add(lblYMax, c);
		c = new LineLayoutConstraints(start + 4, start + 4, 0.45, 0.17, 1);
		pMaskBox.addControl("txtYMax", txtYMax, c);
		c = new LineLayoutConstraints(start + 4, start + 4, 0.65, 0.1, 1);
		pMaskBox.add(lblZMax, c);
		c = new LineLayoutConstraints(start + 4, start + 4, 0.75, 0.17, 1);
		pMaskBox.addControl("txtZMax", txtZMax, c);
		
		//TODO make this work...
		//set max to data dims by default
		if (dataDims != null){
			((JTextField)pMaskBox.getControl("txtXMax")).setText(String.valueOf(dataDims[0]));
			((JTextField)pMaskBox.getControl("txtYMax")).setText(String.valueOf(dataDims[1]));
			((JTextField)pMaskBox.getControl("txtZMax")).setText(String.valueOf(dataDims[2]));
			}
		
		//1-AXIS
		pMaskAxis1 = new VolumeMaskPanel();
		pMaskAxis1.setLayout(new LineLayout(20, 5, 0));
		
		//2-AXIS
		pMaskAxis2 = new VolumeMaskPanel();
		pMaskAxis2.setLayout(new LineLayout(20, 5, 0));
		
		//3-AXIS
		pMaskAxis3 = new VolumeMaskPanel();
		pMaskAxis3.setLayout(new LineLayout(20, 5, 0));
		
		
		updatePanel();
	}
	
	public void setDataDims(){
		if (this.parentPanel == null) return;
		if (!(parentPanel instanceof InterfaceVolumePanel)) return;
		InterfaceVolumePanel p = (InterfaceVolumePanel)parentPanel;
		
		dataDims = p.getDataDims();
	}
	
	public boolean hasOptions(String type){
		if (type.equals(VolumeMaskOptions_old.SHAPE_ALL)) return pMaskAll != null;
		if (type.equals(VolumeMaskOptions_old.SHAPE_BOX)) return pMaskBox != null;
		if (type.equals(VolumeMaskOptions_old.SHAPE_WEDGE)) return pMaskWedge != null;
		if (type.equals(VolumeMaskOptions_old.SHAPE_SPHERE)) return pMaskSphere != null;
		return false;
	}
	
	public void setMaskPanel(String s){
		
		int pType = VolumeMaskOptions_old.getShape(s); 
		
		if (pType == VolumeMaskOptions_old.SHAPE_ALL){
			setGeneralPanel(pMaskAll);
			setMainPanel(pMaskAll);
			this.setDialogSize(500,400);
			this.setTitle("Mask Options - Entire surface");
			return;
			}
		if (pType == VolumeMaskOptions_old.SHAPE_BOX){
			setGeneralPanel(pMaskBox);
			setMainPanel(pMaskBox);
			this.setDialogSize(500,550);
			this.setTitle("Mask Options - Box");
			return;
			}
		if (pType == VolumeMaskOptions_old.SHAPE_AXIS1){
			setAxisPanel(1);
			setGeneralPanel(pMaskAxis1);
			setMainPanel(pMaskAxis1);
			this.setDialogSize(500,500);
			this.setTitle("Mask Options - 1-Axis");
			return;
			}
		if (pType == VolumeMaskOptions_old.SHAPE_AXIS2){
			setAxisPanel(2);
			setGeneralPanel(pMaskAxis2);
			setMainPanel(pMaskAxis2);
			this.setDialogSize(500,525);
			this.setTitle("Mask Options - 2-Axis");
			return;
			}
		if (pType == VolumeMaskOptions_old.SHAPE_AXIS3){
			setAxisPanel(3);
			setGeneralPanel(pMaskAxis3);
			setMainPanel(pMaskAxis3);
			this.setDialogSize(500,550);
			this.setTitle("Mask Options - 3-Axis");
			return;
			}
		
		mainPanel = null;
	}
	
	protected void setAxisPanel(int n){
		JPanel p = null;
		switch (n){
		case 1:
			p = pMaskAxis1;
			break;
		case 2:
			p = pMaskAxis2;
			break;
		case 3:
			p = pMaskAxis3;
			break;
		//etc.
		}
		if (p == null) return;
		p.removeAll();
		
		cmbAxis1.setEnabled(true);
		cmbAxis3.setEnabled(true);
		cmbAxis3.setEnabled(true);
		
		int start = generalSize;
		LineLayoutConstraints c = new LineLayoutConstraints(start + 1, start + 1, 0.05, 0.1, 1);
		p.add(lblAxis1, c);
		c = new LineLayoutConstraints(start + 1, start + 1, 0.15, 0.2, 1);
		p.add(cmbAxis1, c);
		c = new LineLayoutConstraints(start + 1, start + 1, 0.35, 0.1, 1);
		p.add(lblAxisPos1, c);
		c = new LineLayoutConstraints(start + 1, start + 1, 0.45, 0.2, 1);
		p.add(txtAxisPos1, c);
		c = new LineLayoutConstraints(start + 1, start + 1, 0.65, 0.1, 1);
		p.add(lblAxisWidth1, c);
		c = new LineLayoutConstraints(start + 1, start + 1, 0.75, 0.2, 1);
		p.add(txtAxisWidth1, c);
		
		if (p == pMaskAxis1) return;
		//add other axes controls next...
		c = new LineLayoutConstraints(start + 2, start + 2, 0.05, 0.1, 1);
		p.add(lblAxis2, c);
		c = new LineLayoutConstraints(start + 2, start + 2, 0.15, 0.2, 1);
		p.add(cmbAxis2, c);
		c = new LineLayoutConstraints(start + 2, start + 2, 0.35, 0.1, 1);
		p.add(lblAxisPos2, c);
		c = new LineLayoutConstraints(start + 2, start + 2, 0.45, 0.2, 1);
		p.add(txtAxisPos2, c);
		c = new LineLayoutConstraints(start + 2, start + 2, 0.65, 0.1, 1);
		p.add(lblAxisWidth2, c);
		c = new LineLayoutConstraints(start + 2, start + 2, 0.75, 0.2, 1);
		p.add(txtAxisWidth2, c);
		
		if (p == pMaskAxis2) return;
		//add other axes controls next...
		c = new LineLayoutConstraints(start + 3, start + 3, 0.05, 0.1, 1);
		p.add(lblAxis3, c);
		c = new LineLayoutConstraints(start + 3, start + 3, 0.15, 0.2, 1);
		p.add(cmbAxis3, c);
		c = new LineLayoutConstraints(start + 3, start + 3, 0.35, 0.1, 1);
		p.add(lblAxisPos3, c);
		c = new LineLayoutConstraints(start + 3, start + 3, 0.45, 0.2, 1);
		p.add(txtAxisPos3, c);
		c = new LineLayoutConstraints(start + 3, start + 3, 0.65, 0.1, 1);
		p.add(lblAxisWidth3, c);
		c = new LineLayoutConstraints(start + 3, start + 3, 0.75, 0.2, 1);
		p.add(txtAxisWidth3, c);
		
		//set axes automatically for 3-axis
		cmbAxis1.setSelectedItem("X");
		cmbAxis1.setEnabled(false);
		cmbAxis2.setSelectedItem("Y");
		cmbAxis2.setEnabled(false);
		cmbAxis3.setSelectedItem("Z");
		cmbAxis3.setEnabled(false);
		
	}
	
	//set controls for general parameters for panel p
	//returns the line on which to start adding new controls
	protected int setGeneralPanel(VolumeMaskPanel p){
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.01, 0.5, 1);
		p.add(lblGeneralParams, c);
		c = new LineLayoutConstraints(1, 1, 0.88, 0.2, 1);
		p.add(lblInvert, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.35, 1);
		p.add(lblInputType, c);
		c = new LineLayoutConstraints(2, 2, 0.42, 0.43, 1);
		p.add(cmbInputType, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.35, 1);
		p.add(lblInputChannel, c);
		c = new LineLayoutConstraints(3, 3, 0.42, 0.43, 1);
		p.add(cmbInputChannel, c);
		c = new LineLayoutConstraints(3, 3, 0.89, 0.04, 1);
		p.add(chkInvertData, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.35, 1);
		p.add(lblInputValue, c);
		c = new LineLayoutConstraints(3, 3, 0.42, 0.43, 1);
		p.add(txtInputValue, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.35, 1);
		p.add(lblInputOp, c);
		c = new LineLayoutConstraints(4, 4, 0.42, 0.43, 1);
		p.add(cmbInputOp, c);
		c = new LineLayoutConstraints(5, 5, 0.05, 0.35, 1);
		p.add(lblInputFactor, c);
		c = new LineLayoutConstraints(5, 5, 0.42, 0.43, 1);
		p.add(txtInputFactor, c);
		c = new LineLayoutConstraints(6, 6, 0.05, 0.35, 1);
		p.add(lblOutputChannel, c);
		c = new LineLayoutConstraints(6, 6, 0.42, 0.43, 1);
		p.add(cmbOutputChannel, c);
		c = new LineLayoutConstraints(7, 7, 0.05, 0.35, 1);
		p.add(lblMaskShape, c);
		c = new LineLayoutConstraints(7, 7, 0.42, 0.43, 1);
		p.add(cmbMaskShape, c);
		c = new LineLayoutConstraints(7, 7, 0.89, 0.04, 1);
		p.add(chkInvertShape, c);
		c = new LineLayoutConstraints(8, 8, 0.05, 0.35, 1);
		p.add(lblMinThreshold, c);
		c = new LineLayoutConstraints(8, 8, 0.42, 0.43, 1);
		p.add(txtMinThreshold, c);
		c = new LineLayoutConstraints(8, 8, 0.89, 0.04, 1);
		p.add(chkInvertMin, c);
		c = new LineLayoutConstraints(9, 9, 0.05, 0.35, 1);
		p.add(lblMaxThreshold, c);
		c = new LineLayoutConstraints(9, 9, 0.42, 0.43, 1);
		p.add(txtMaxThreshold, c);
		c = new LineLayoutConstraints(9, 9, 0.89, 0.04, 1);
		p.add(chkInvertMax, c);
		c = new LineLayoutConstraints(10, 10, 0.05, 0.35, 1);
		p.add(lblSmoothingExp, c);
		c = new LineLayoutConstraints(10, 10, 0.42, 0.43, 1);
		p.add(txtSmoothingExp, c);
		c = new LineLayoutConstraints(12, 12, 0.01, 0.5, 1);
		p.add(lblShapeParams, c);
		
		upateGeneral();
		
		return 12;
	}
	
	public void setGeneral(){
		
		/*
		VolumeMaskOptions_old.setInputTypeList(cmbInputType);
		VolumeMaskOptions_old.setInputOpList(cmbInputOp);
		VolumeMaskOptions_old.setMaskShapeList(cmbMaskShape);
		VolumeMaskOptions_old.setAxesList(cmbAxis1);
		VolumeMaskOptions_old.setAxesList(cmbAxis2);
		VolumeMaskOptions_old.setAxesList(cmbAxis3);
		
		//no. channels
		if (this.parentPanel == null) return;
		if (!(parentPanel instanceof InterfaceVolumePanel)) return;
		InterfaceVolumePanel p = (InterfaceVolumePanel)parentPanel;
		
		cmbInputChannel.removeAllItems();
		cmbOutputChannel.removeAllItems();
		
		int channels = p.getDataSize();
		for (int i = 1; i <= channels; i++){
			cmbInputChannel.addItem("Channel " + i);
			cmbOutputChannel.addItem("Channel " + i);
			}
		
		*/
	}
	
	public void upateGeneral(){
		if (cmbInputType.getSelectedItem() == null) return;
		boolean t = (cmbInputType.getSelectedItem().equals("Data channel"));
		lblInputChannel.setVisible(t);
		cmbInputChannel.setVisible(t);
		chkInvertData.setVisible(t);
		lblInputValue.setVisible(!t);
		txtInputValue.setVisible(!t);
	}
	
	public boolean hasPanel(){
		return mainPanel != null;
	}
	
	public void updatePanel(){
		if (cmbMaskShape.getSelectedItem() == null) return;
		setMaskPanel((String)cmbMaskShape.getSelectedItem());
		
		//set parameters from parent
		//setOptions();
		
		this.repaint();
	}
	
	//update dialog with options as set in parent
	public void setOptions(){
		if (this.parentPanel == null) return;
		if (!(parentPanel instanceof InterfaceVolumePanel)) return;
		InterfaceVolumePanel p = (InterfaceVolumePanel)parentPanel;
		
		String maskShape = p.maskOptions.getShapeString();
		
		cmbInputType.setSelectedItem(p.maskOptions.getTypeString());
		cmbInputOp.setSelectedItem(p.maskOptions.getOpString());
		cmbMaskShape.setSelectedItem(maskShape);
		cmbInputChannel.setSelectedItem("Channel " + (p.maskOptions.inputChannel + 1));
		cmbOutputChannel.setSelectedItem("Channel " + (p.maskOptions.outputChannel + 1));
		txtInputFactor.setText(MguiDouble.getString(p.maskOptions.inputFactor, "#0.0000"));
		txtInputValue.setText(MguiDouble.getString(p.maskOptions.inputValue, "#0.0000"));
		txtMinThreshold.setText(MguiDouble.getString(p.maskOptions.minThreshold, "#0.0000"));
		txtMaxThreshold.setText(MguiDouble.getString(p.maskOptions.maxThreshold, "#0.0000"));
		txtSmoothingExp.setText(MguiDouble.getString(p.maskOptions.smoothingExp, "#0.0000"));
		chkInvertData.setSelected(p.maskOptions.invertData);
		chkInvertShape.setSelected(p.maskOptions.invertShape);
		chkInvertMin.setSelected(p.maskOptions.invertMin);
		chkInvertMax.setSelected(p.maskOptions.invertMax);
		
		if (maskShape.equals("Box")){
			((JTextField)pMaskBox.getControl("txtXMin")).setText("" + p.maskOptions.x1);
			((JTextField)pMaskBox.getControl("txtYMin")).setText("" + p.maskOptions.y1);
			((JTextField)pMaskBox.getControl("txtZMin")).setText("" + p.maskOptions.z1);
			((JTextField)pMaskBox.getControl("txtXMax")).setText("" + p.maskOptions.x2);
			((JTextField)pMaskBox.getControl("txtYMax")).setText("" + p.maskOptions.y2);
			((JTextField)pMaskBox.getControl("txtZMax")).setText("" + p.maskOptions.z2);
			}
		
		if (maskShape.equals("1-Axis")){
			cmbAxis1.setSelectedItem(VolumeMaskOptions_old.getAxisString(p.maskOptions.a1));
			txtAxisPos1.setText("" + p.maskOptions.x1);
			txtAxisWidth1.setText("" + p.maskOptions.p1);
			}
		
		if (maskShape.equals("2-Axis")){
			cmbAxis1.setSelectedItem(VolumeMaskOptions_old.getAxisString(p.maskOptions.a1));
			txtAxisPos1.setText("" + p.maskOptions.x1);
			txtAxisWidth1.setText("" + p.maskOptions.p1);
			
			cmbAxis2.setSelectedItem(VolumeMaskOptions_old.getAxisString(p.maskOptions.a2));
			txtAxisPos2.setText("" + p.maskOptions.y1);
			txtAxisWidth2.setText("" + p.maskOptions.p2);
			}
		
		if (maskShape.equals("3-Axis")){
			cmbAxis1.setSelectedItem(VolumeMaskOptions_old.getAxisString(p.maskOptions.a1));
			txtAxisPos1.setText("" + p.maskOptions.x1);
			txtAxisWidth1.setText("" + p.maskOptions.p1);
			
			cmbAxis2.setSelectedItem(VolumeMaskOptions_old.getAxisString(p.maskOptions.a2));
			txtAxisPos2.setText("" + p.maskOptions.y1);
			txtAxisWidth2.setText("" + p.maskOptions.p2);
			
			cmbAxis3.setSelectedItem(VolumeMaskOptions_old.getAxisString(p.maskOptions.a3));
			txtAxisPos3.setText("" + p.maskOptions.z1);
			txtAxisWidth3.setText("" + p.maskOptions.p3);
			}
	}
	
	//update parent options with this dialog box
	protected void updateOptions(){
		if (this.parentPanel == null) return;
		if (!(parentPanel instanceof InterfaceVolumePanel)) return;
		InterfaceVolumePanel p = (InterfaceVolumePanel)parentPanel;
		
		String maskShape = (String)cmbMaskShape.getSelectedItem();
		
		//set em
		p.maskOptions.setInputType((String)cmbInputType.getSelectedItem());
		p.maskOptions.setInputOp((String)cmbInputOp.getSelectedItem());
		p.maskOptions.setMaskShape(maskShape);
		p.maskOptions.setInputChannel((String)cmbInputChannel.getSelectedItem());
		p.maskOptions.inputFactor = Double.valueOf(txtInputFactor.getText()).doubleValue();
		p.maskOptions.inputValue = Double.valueOf(txtInputValue.getText()).doubleValue();
		p.maskOptions.setOutputChannel((String)cmbOutputChannel.getSelectedItem());
		p.maskOptions.minThreshold = Double.valueOf(txtMinThreshold.getText()).doubleValue();
		p.maskOptions.maxThreshold = Double.valueOf(txtMaxThreshold.getText()).doubleValue();
		p.maskOptions.smoothingExp = Double.valueOf(txtSmoothingExp.getText()).doubleValue();
		p.maskOptions.invertData = chkInvertData.isSelected();
		p.maskOptions.invertShape = chkInvertShape.isSelected();
		p.maskOptions.invertMin = chkInvertMin.isSelected();
		p.maskOptions.invertMax = chkInvertMax.isSelected();
		
		//set shape specific parameters
		if (maskShape.equals("Box")){
			p.maskOptions.x1 = Integer.valueOf(pMaskBox.getString("txtXMin")).intValue();
			p.maskOptions.y1 = Integer.valueOf(pMaskBox.getString("txtYMin")).intValue();
			p.maskOptions.z1 = Integer.valueOf(pMaskBox.getString("txtZMin")).intValue();
			p.maskOptions.x2 = Integer.valueOf(pMaskBox.getString("txtXMax")).intValue();
			p.maskOptions.y2 = Integer.valueOf(pMaskBox.getString("txtYMax")).intValue();
			p.maskOptions.z2 = Integer.valueOf(pMaskBox.getString("txtZMax")).intValue();
			return;
			}
		
		if (maskShape.equals("1-Axis")){
			p.maskOptions.x1 = Integer.valueOf(txtAxisPos1.getText()).intValue();
			p.maskOptions.p1 = Integer.valueOf(txtAxisWidth1.getText()).intValue();
			p.maskOptions.a1 = VolumeMaskOptions_old.getAxis((String)cmbAxis1.getSelectedItem());
			return;
			}
		
		if (maskShape.equals("2-Axis")){
			p.maskOptions.x1 = Integer.valueOf(txtAxisPos1.getText()).intValue();
			p.maskOptions.p1 = Integer.valueOf(txtAxisWidth1.getText()).intValue();
			p.maskOptions.a1 = VolumeMaskOptions_old.getAxis((String)cmbAxis1.getSelectedItem());
			p.maskOptions.y1 = Integer.valueOf(txtAxisPos2.getText()).intValue();
			p.maskOptions.p2 = Integer.valueOf(txtAxisWidth2.getText()).intValue();
			p.maskOptions.a2 = VolumeMaskOptions_old.getAxis((String)cmbAxis2.getSelectedItem());
			return;
			}
		
		if (maskShape.equals("3-Axis")){
			p.maskOptions.x1 = Integer.valueOf(txtAxisPos1.getText()).intValue();
			p.maskOptions.p1 = Integer.valueOf(txtAxisWidth1.getText()).intValue();
			p.maskOptions.a1 = 1;
			p.maskOptions.y1 = Integer.valueOf(txtAxisPos2.getText()).intValue();
			p.maskOptions.p2 = Integer.valueOf(txtAxisWidth2.getText()).intValue();
			p.maskOptions.a2 = 2;
			p.maskOptions.z1 = Integer.valueOf(txtAxisPos3.getText()).intValue();
			p.maskOptions.p3 = Integer.valueOf(txtAxisWidth3.getText()).intValue();
			p.maskOptions.a3 = 3;
			return;
			}
		
	}
	
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Type changed")){
			upateGeneral();
			return;
			}
		
		if (e.getActionCommand().equals("Shape changed")){
			updatePanel();
			return;
		}
		
		//update options with this dialog
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			updateOptions();
			this.setVisible(false);
		}
		
		super.actionPerformed(e);
	}
	
	
	class VolumeMaskPanel extends JPanel{
		public String type = "";
		public HashMap<String, JComponent> controls = new HashMap<String, JComponent>();
		
		public void addControl(String id, JComponent control, LineLayoutConstraints c){
			add(control, c);
			controls.put(id, control);
			}
		
		public JComponent getControl(String id){
			return controls.get(id);
		}
		
		public String getString(String id){
			JComponent c = getControl(id);
			if (c == null) return null;
			if (c instanceof JTextField)
				return ((JTextField)c).getText();
			if (c instanceof JComboBox)
				return (String)((JComboBox)c).getSelectedItem();
			return null;
		}
	}
}