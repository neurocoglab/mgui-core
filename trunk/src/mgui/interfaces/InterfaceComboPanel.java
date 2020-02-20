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

package mgui.interfaces;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JScrollPane;

import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.logs.LoggingType;
import mgui.interfaces.shapes.util.ShapeEvent;


/***************************
 * Interface panel allowing for the display of multiple interface panels, selected by a
 * combo box.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 */

public class InterfaceComboPanel extends InterfacePanel implements ItemListener {

	//combine a combo box and a selected interface panel
	protected ArrayList<InterfacePanel> panels = new ArrayList<InterfacePanel>();
	public InterfacePanel currentPanel;
	public JScrollPane scrollPane = new JScrollPane();
	//public InterfaceComboBox cmbPanels = new JComboBox(new DefaultComboBoxModel(new InterfacePanel[0]));
	InterfaceComboBox cmbPanels = new InterfaceComboBox(InterfaceComboBox.RenderMode.LongestItem,
														true, 1000);
	
	public InterfaceComboPanel(){
		super();
		setLayout(new BorderLayout());
		add(cmbPanels, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		// TODO: disable autoscrolling?
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);
		cmbPanels.addItemListener(this);
		cmbPanels.setPreferredSize(new Dimension(500, 30));
		cmbPanels.setFont(new Font("Courier New", Font.BOLD, 13));
	}
	
	@Override
	protected void init(){
		
	}
	
	public ArrayList<InterfacePanel> getPanels(){
		return panels;
	}
	
	public void addPanel(InterfacePanel thisPanel){
		panels.add(thisPanel);
		cmbPanels.addItem(thisPanel);
	}
	
	public void showPanel(int i){
		if (currentPanel!= null){
			scrollPane.remove(currentPanel);
			scrollPane.validate();
			currentPanel.cleanUpPanel();
			}
		currentPanel = panels.get(i);
		scrollPane.setViewportView(currentPanel);
		//scrollPane.updateUI();
		updateUI();
		super.repaint();
		repaint();
	}
	
	public void showPanel(InterfacePanel thisPanel){
		
		if (currentPanel != null)
			currentPanel.cleanUpPanel();
		
		currentPanel = thisPanel;
		//try{
			currentPanel.showPanel();
		//}catch (Exception ex){
		//	InterfaceSession.log("InterfaceComboPanel: Exception setting up panel: " + ex.getMessage(), LoggingType.Errors);
		//	}
		scrollPane.setViewportView(currentPanel);
		
		scrollPane.updateUI();
		updateUI();
		super.repaint();
		repaint();
	}
	
	public void showPanel(String name){
		for (int i = 0; i < panels.size(); i++)
			if (panels.get(i).getName().equals(name)){
				showPanel(panels.get(i));
				return;
				}
	}
	
	public void itemStateChanged(ItemEvent e){
		if (e.getStateChange() == ItemEvent.SELECTED)
			showPanel((InterfacePanel)e.getItem());
	}
	
	@Override
	public void shapeUpdated(ShapeEvent e){
		switch (e.eventType){
			//tell all panels to release any destroyed objects
			case ShapeRemoved:
				for (int i = 0; i < panels.size(); i++)
					panels.get(i).cleanUpPanel();
		
		}
	}
	
}