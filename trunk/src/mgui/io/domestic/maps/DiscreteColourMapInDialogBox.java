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

package mgui.io.domestic.maps;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;

public class DiscreteColourMapInDialogBox extends InterfaceIODialogBox {

	JCheckBox chkAsDiscrete = new JCheckBox(" As discrete map");
	JCheckBox chkAsContinuous = new JCheckBox(" As continuous map");
	JLabel lblAnchors = new JLabel("No. anchors:");
	JTextField txtAnchors = new JTextField("5");
	JLabel lblFormat = new JLabel("Format:");
	JComboBox cmbFormat = new JComboBox();
	JCheckBox chkNormalized = new JCheckBox(" Values are normalized");
	
	boolean do_update = true;
	
	public DiscreteColourMapInDialogBox(){
		
	}
	
	public DiscreteColourMapInDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		_init();
		
	}
	
	void _init(){
		super.init();
		
		this.setDialogSize(450,280);
		this.setTitle("Input Discrete Colour Map - Options");
		
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		
		chkAsDiscrete.addActionListener(this);
		chkAsDiscrete.setActionCommand("Discrete Changed");
		chkAsContinuous.addActionListener(this);
		chkAsContinuous.setActionCommand("Continuous Changed");
		chkNormalized.setSelected(true);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.9, 1);
		mainPanel.add(chkAsDiscrete, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.9, 1);
		mainPanel.add(chkAsContinuous, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.35, 1);
		mainPanel.add(lblAnchors, c);
		c = new LineLayoutConstraints(3, 3, 0.4, 0.45, 1);
		mainPanel.add(txtAnchors, c);
		c = new LineLayoutConstraints(4, 4, 0.05, 0.35, 1);
		mainPanel.add(lblFormat, c);
		c = new LineLayoutConstraints(4, 4, 0.4, 0.55, 1);
		mainPanel.add(cmbFormat, c);
		c = new LineLayoutConstraints(5, 5, 0.4, 0.55, 1);
		mainPanel.add(chkNormalized, c);
		
		updateDialog();
		updateControls();
		
	}
	
	@Override
	public boolean updateDialog(){
		
		DiscreteColourMapInOptions _options = (DiscreteColourMapInOptions)options;
		chkAsDiscrete.setSelected(_options.as_discrete);
		txtAnchors.setText("" + _options.no_anchors);
		chkNormalized.setSelected(_options.normalized);
		
		updateFormats();
		
		return true;
	}
	
	void updateFormats(){
		cmbFormat.removeAllItems();
		cmbFormat.addItem("Ascii");
		cmbFormat.addItem("XML");
		
		DiscreteColourMapInOptions _options = (DiscreteColourMapInOptions)options;
		switch (_options.format){
			case Ascii:
				cmbFormat.setSelectedItem("Ascii");
			default:
				cmbFormat.setSelectedItem("XML");
			}
		
	}
	
	void updateControls(){
		//boolean is_discrete = chkAsDiscrete.isSelected();
		boolean is_continuous = chkAsContinuous.isSelected();
		
		txtAnchors.setEnabled(is_continuous);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals("Discrete Changed")){
			if (!do_update) return;
			do_update = false;
			chkAsContinuous.setSelected(!chkAsDiscrete.isSelected());
			updateControls();
			do_update = true;
			return;
			}
		
		if (e.getActionCommand().equals("Continuous Changed")){
			if (!do_update) return;
			do_update = false;
			chkAsDiscrete.setSelected(!chkAsContinuous.isSelected());
			updateControls();
			do_update = true;
			return;
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			DiscreteColourMapInOptions _options = (DiscreteColourMapInOptions)options;
			_options.normalized = chkNormalized.isSelected();
			_options.as_discrete = chkAsDiscrete.isSelected();
			if (!_options.as_discrete)
				_options.no_anchors = Integer.valueOf(txtAnchors.getText());
			if (cmbFormat.getSelectedItem().equals("Ascii")){
				_options.format = DiscreteColourMapInOptions.Format.Ascii;
			}else{
				_options.format = DiscreteColourMapInOptions.Format.XML;
				}
			this.setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
	}
	
}