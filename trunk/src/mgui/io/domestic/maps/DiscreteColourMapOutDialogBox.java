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
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.io.InterfaceIODialogBox;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.maps.ColourMap;
import mgui.interfaces.maps.DiscreteColourMap;
import mgui.io.InterfaceIOOptions;
import mgui.io.InterfaceIOPanel;


public class DiscreteColourMapOutDialogBox extends InterfaceIODialogBox {

	JLabel lblMap = new JLabel("Colour map:");
	JComboBox cmbMap = new JComboBox();
	JCheckBox chkAlpha = new JCheckBox(" Write alpha");
	
	protected LineLayout lineLayout;
	
	public DiscreteColourMapOutDialogBox(){
		
	}
	
	public DiscreteColourMapOutDialogBox(JFrame frame, InterfaceIOPanel panel, InterfaceIOOptions options){
		super(frame, panel, options);
		_init();
		setLocationRelativeTo(InterfaceSession.getSessionFrame());
	}
	
	private void _init(){
		this.setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		
		super.init();
		
		lineLayout = new LineLayout(20, 5, 0);
		
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,300);
		this.setTitle("Discrete Colour Map Output Options");
				
		fillMapCombo();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		mainPanel.add(lblMap, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(cmbMap, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.9, 1);
		mainPanel.add(chkAlpha, c);
		
	}
	
	void fillMapCombo(){
		cmbMap.removeAllItems();
		if (io_panel == null) return;
		
		ArrayList<ColourMap> maps = InterfaceEnvironment.getColourMaps();
		
		for (int i = 0; i < maps.size(); i++)
			if (maps.get(i) instanceof DiscreteColourMap)
				cmbMap.addItem(maps.get(i));
		
		DiscreteColourMapOutOptions _options = (DiscreteColourMapOutOptions)options;
		if (_options.map != null)
			cmbMap.setSelectedItem(_options.map);
		
		chkAlpha.setSelected(_options.write_alpha);
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			DiscreteColourMap map = (DiscreteColourMap)cmbMap.getSelectedItem();
			DiscreteColourMapOutOptions _options = (DiscreteColourMapOutOptions)options;
			
			if (map != null){
				_options.map = map;
			}else{
				_options.map = null;
				}
				
			_options.write_alpha = chkAlpha.isSelected();
			setVisible(false);
			return;
			}
		
		super.actionPerformed(e);
		
	}
	
}