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

package mgui.interfaces.shapes.video;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.InterfaceGraphic2D;
import mgui.interfaces.graphics.InterfaceGraphicWindow;
import mgui.interfaces.graphics.video.VideoTaskDialogPanel;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

/*************************************************
 * Dialog panel for a {@linkplain ChangeSectionVideoTask}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class ChangeSectionVideoTaskDialogPanel extends VideoTaskDialogPanel {

	JLabel lblWindow = new JLabel("Graphic2D Window:");
	InterfaceComboBox cmbWindow = new InterfaceComboBox(RenderMode.LongestItem, true, 200);
	JLabel lblStartSection = new JLabel("Start section:");
	JTextField txtStartSection = new JTextField("0");
	JLabel lblEndSection = new JLabel("End section:");
	JTextField txtEndSection = new JTextField("1");
	
	ChangeSectionVideoTask task;
	boolean handleCombo = true;
	
	public ChangeSectionVideoTaskDialogPanel(){
		super();
	}

	public ChangeSectionVideoTaskDialogPanel(ChangeSectionVideoTask task){
		super();
		this.task = task;
		init();
	}
	
	@Override
	protected void init() {
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		setLayout(lineLayout);
		
		fillCombos();
		if (this.task != null){
			txtStartSection.setText(task.start_section + "");
			txtEndSection.setText(task.end_section + "");
		}
		
		cmbWindow.addActionListener(this);
		cmbWindow.setActionCommand("Window Changed");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		add(lblWindow, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		add(cmbWindow, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.24, 1);
		add(lblStartSection, c);
		c = new LineLayoutConstraints(2, 2, 0.3, 0.65, 1);
		add(txtStartSection, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.24, 1);
		add(lblEndSection, c);
		c = new LineLayoutConstraints(3, 3, 0.3, 0.65, 1);
		add(txtEndSection, c);
		
	}
	
	void fillCombos(){
		
		handleCombo = false;
		
		// Windows
		cmbWindow.removeAllItems();
		ArrayList<InterfaceGraphicWindow> windows = InterfaceSession.getDisplayPanel().getAllWindows();
		
		for (int i = 0; i < windows.size(); i++){
			InterfaceGraphicWindow window = windows.get(i);
			if (window.getPanel() instanceof InterfaceGraphic2D){
				cmbWindow.addItem(window.getPanel());
				}
			}
		
		if (task.graphic_2d != null)
			cmbWindow.setSelectedItem(task.graphic_2d);
		
		handleCombo = true;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals("Window Changed")){
			if (!handleCombo) return;
			//windowUpdated();
			return;
			}
	}

	@Override
	public int getLineCount() {
		return 3;
	}

	@Override
	public void updateTask() {
		
		if (task == null) return;
			task.graphic_2d = (InterfaceGraphic2D)cmbWindow.getSelectedItem();
		try{
			task.start_section = Integer.valueOf(txtStartSection.getText());
			task.end_section = Integer.valueOf(txtEndSection.getText());
			
		}catch (NumberFormatException ex){
			JOptionPane.showMessageDialog(InterfaceSession.getSessionFrame(), 
										 "Sections must be valid integers!", 
										 "Change Section Video Task", 
										 JOptionPane.ERROR_MESSAGE);
			}
		
	}

	

}