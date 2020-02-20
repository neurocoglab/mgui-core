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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import mgui.interfaces.InterfaceDialogBox;
import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.interfaces.maps.NameMap;


public class MeshDataMaskOptionsDialog extends InterfaceOptionsDialogBox {

	LineLayout lineLayout;
	
	JCheckBox chkVals = new JCheckBox("Integers:");
	JTextField txtVals = new JTextField("0"); 
	JCheckBox chkNames = new JCheckBox("Names:");
	JComboBox cmbNameMaps = new JComboBox();
	JScrollPane lstNames; // = new JScrollPane();
	JList list;
	DefaultListModel listModel = new DefaultListModel();
	ListSelectionModel selModel;
	
	JLabel lblIn = new JLabel("Included value:");
	JTextField txtIn = new JTextField("1"); 
	JLabel lblOut = new JLabel("Excluded value:");
	JTextField txtOut = new JTextField("0"); 
	
	boolean doUpdate = true;
	
	public MeshDataMaskOptionsDialog(){
		super();
	}
	
	public MeshDataMaskOptionsDialog(JFrame aFrame, MeshDataMaskOptions options, InterfacePanel panel){
		super(aFrame, options);
		parentPanel = panel;
		setButtonType(InterfaceDialogBox.BT_OK_CANCEL);
		init();
		setLocationRelativeTo(aFrame);
	}
	
	@Override
	protected void init(){
		
		super.init();
		lineLayout = new LineLayout(20, 5, 0);
		this.setMainLayout(lineLayout);
		this.setDialogSize(450,380);
		this.setTitle("Mesh Mask Operation Options");
		
		list = new JList(listModel);
		//list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		lstNames = new JScrollPane(list);
		selModel = list.getSelectionModel();
		
		chkVals.setActionCommand("Vals");
		chkVals.addActionListener(this);
		chkNames.setActionCommand("Names");
		chkNames.addActionListener(this);
		cmbNameMaps.setActionCommand("Maps");
		cmbNameMaps.addActionListener(this);
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.3, 1);
		mainPanel.add(chkVals, c);
		c = new LineLayoutConstraints(1, 1, 0.4, 0.55, 1);
		mainPanel.add(txtVals, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.3, 1);
		mainPanel.add(chkNames, c);
		c = new LineLayoutConstraints(2, 2, 0.4, 0.55, 1);
		mainPanel.add(cmbNameMaps, c);
		
		c = new LineLayoutConstraints(3, 7, 0.05, 0.9, 1);
		mainPanel.add(lstNames, c);
		
		c = new LineLayoutConstraints(8, 8, 0.05, 0.3, 1);
		mainPanel.add(lblIn, c);
		c = new LineLayoutConstraints(8, 8, 0.4, 0.55, 1);
		mainPanel.add(txtIn, c);
		c = new LineLayoutConstraints(9, 9, 0.05, 0.3, 1);
		mainPanel.add(lblOut, c);
		c = new LineLayoutConstraints(9, 9, 0.4, 0.55, 1);
		mainPanel.add(txtOut, c);
		
		
		fillNameMapCombo();
		MeshDataMaskOptions ops = (MeshDataMaskOptions)options;
		txtIn.setText("" + ops.in);
		txtOut.setText("" + ops.out);
		chkVals.setSelected(ops.nameMap == null);
		chkNames.setSelected(!chkVals.isSelected());
		updateControls();
		if (ops.nameMap != null){
			cmbNameMaps.setSelectedItem(ops.nameMap);
			updateNames();
			list.setSelectedIndices(ops.indices);
			}
		list.updateUI();
	}
	
	protected void fillNameMapCombo(){
		cmbNameMaps.removeAllItems();
		if (parentPanel == null) return;
		
		ArrayList<NameMap> maps = InterfaceEnvironment.getNameMaps();
		
		for (int i = 0; i < maps.size(); i++)
			cmbNameMaps.addItem(maps.get(i));
	}
	
	protected void updateNames(){
		//if names is selected fill list
		if (cmbNameMaps.getSelectedItem() == null) return;
		
		listModel.removeAllElements();
		NameMap map = (NameMap)cmbNameMaps.getSelectedItem();
		
		ArrayList<String> s = new ArrayList<String>(map.getNames());
		Collections.sort(s);
		for (int i = 0; i < s.size(); i++)
			listModel.addElement(s.get(i));
		lstNames.updateUI();
	}
	
	protected void updateControls(){
		list.setEnabled(chkNames.isSelected());
		lstNames.setEnabled(chkNames.isSelected());
		cmbNameMaps.setEnabled(chkNames.isSelected());
		txtVals.setEnabled(chkVals.isSelected());
	}
	
	protected int[] getVals(){
		if (chkVals.isSelected()){
			StringTokenizer tokens = new StringTokenizer(txtVals.getText(),",");
			int[] vals = new int[tokens.countTokens()];
			int i = 0;
			while (tokens.hasMoreTokens())
				vals[i++] = Integer.valueOf(tokens.nextToken());
			return vals;
			}
		
		int min = selModel.getMinSelectionIndex();
		int max = selModel.getMaxSelectionIndex();
		ArrayList<Integer> vals = new ArrayList<Integer>();
		NameMap map = (NameMap)cmbNameMaps.getSelectedItem();
		
		//System.out.print("Values: ");
		for (int i = min; i <= max; i++)
			if (selModel.isSelectedIndex(i)){
				int x = map.get((String)listModel.getElementAt(i));
				//InterfaceSession.log((String)listModel.getElementAt(i) + ", " + x);
				if (x > -Integer.MAX_VALUE) vals.add(new Integer(x));
				}
		
		int[] ret = new int[vals.size()];
		   
		//System.out.print("Values: ");
		for (int i = 0; i < vals.size(); i++){
			ret[i] = vals.get(i);
			//System.out.print(ret[i] + ",");
			}
		
		return ret;
	}
	
	@Override
	public void actionPerformed(ActionEvent e){
		
		super.actionPerformed(e);
		
		if (e.getActionCommand().equals("Names") && doUpdate){
			doUpdate = false;
			chkVals.setSelected(!chkNames.isSelected());
			updateControls();
			doUpdate = true;
			}
		
		if (e.getActionCommand().equals("Vals") && doUpdate){
			doUpdate = false;
			chkNames.setSelected(!chkVals.isSelected());
			updateControls();
			doUpdate = true;
			}
		
		if (e.getActionCommand().equals("Maps")){
			updateNames();
			}
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			MeshDataMaskOptions ops = (MeshDataMaskOptions)options;
			ops.vals = getVals();
			ops.in = Double.valueOf(txtIn.getText());
			ops.out = Double.valueOf(txtOut.getText());
			if (chkNames.isSelected()){
				ops.nameMap = (NameMap)cmbNameMaps.getSelectedItem();
				ops.indices = list.getSelectedIndices();
			}else{
				ops.nameMap = null;
				ops.indices = null;
			}
			setVisible(false);
			}
		
	}
	
}