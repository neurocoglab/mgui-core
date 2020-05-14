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

package mgui.interfaces.graphics.video;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import mgui.interfaces.InterfaceSession;
import mgui.interfaces.graphics.View3D;
import mgui.interfaces.gui.InterfaceComboBox;
import mgui.interfaces.gui.InterfaceComboBox.RenderMode;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;

/***********************************************
 * Dialog panel for a {@linkplain SetView3DTask}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class SetView3DTaskDialogPanel extends VideoTaskDialogPanel  {

	JLabel lblView = new JLabel("Target view:");
	InterfaceComboBox cmbView = new InterfaceComboBox(RenderMode.LongestItem, true, 200);
	JCheckBox chkZoom = new JCheckBox(" Set zoom");
	JCheckBox chkTarget = new JCheckBox(" Set target point");
	
	SetView3DTask task;
	boolean handle_combo = true;
	
	public SetView3DTaskDialogPanel(){
		//super();
	}

	
	public SetView3DTaskDialogPanel(SetView3DTask task){
		super();
		this.task = task;
		init();
	}
	
	@Override
	protected void init() {
		//set controls
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		setLayout(lineLayout);
		
		initCombo();
		updateControls();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		add(lblView, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		add(cmbView, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.9, 1);
		add(chkZoom, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.9, 1);
		add(chkTarget, c);
		
	}

	
	void initCombo(){
		
		cmbView.removeAllItems();
		ArrayList<View3D> views = InterfaceSession.getWorkspace().getViews3D();
		
		for (int i = 0; i < views.size(); i++)
			cmbView.addItem(views.get(i));
	
	}
	
	void updateControls(){
		if (task == null) return;
		handle_combo = false;
		
		cmbView.setSelectedItem(task.view_target);
		chkZoom.setSelected(task.set_zoom);
		chkTarget.setSelected(task.set_target);
		
		handle_combo = true;
	}
	
	@Override
	public int getLineCount(){
		return 3;
	}
	
	@Override
	public void updateTask(){
		View3D view = (View3D)cmbView.getSelectedItem();
		if (view != null)
			task.setView(view);
		task.set_zoom = chkZoom.isSelected();
		task.set_target = chkTarget.isSelected();
	}
	
	public void actionPerformed(ActionEvent e) {
		
		
		
	}
	
	
}