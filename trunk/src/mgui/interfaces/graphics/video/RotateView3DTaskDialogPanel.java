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

package mgui.interfaces.graphics.video;

import java.awt.event.ActionEvent;

import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.numbers.MguiDouble;

/**************************************************
 * Dialog panel for a {@linkplain RotateView3DTask}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class RotateView3DTaskDialogPanel extends VideoTaskDialogPanel {

	JLabel lblAngleX = new JLabel("X angle (rad):");
	JTextField txtAngleX = new JTextField("0");
	JLabel lblAngleY = new JLabel("Y angle (rad):");
	JTextField txtAngleY = new JTextField("0");
	
	RotateView3DTask task;
	boolean handle_combo = true;
	
	public RotateView3DTaskDialogPanel(){
		super();
	}

	public RotateView3DTaskDialogPanel(RotateView3DTask task){
		super();
		this.task = task;
		init();
	}
	
	@Override
	protected void init() {
		//set controls
		LineLayout lineLayout = new LineLayout(20, 5, 0);
		setLayout(lineLayout);
		
		updateControls();
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		add(lblAngleX, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		add(txtAngleX, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.24, 1);
		add(lblAngleY, c);
		c = new LineLayoutConstraints(2, 2, 0.3, 0.65, 1);
		add(txtAngleY, c);
		
	}

	@Override
	public void updateTask(){
		String s = txtAngleX.getText();
		if (s.toLowerCase().contains("pi")){
			int i = s.indexOf("pi");
			if (i == 0)
				task.x_angle = Math.PI;
			else
				task.x_angle = Double.valueOf(s.substring(0, i)) * Math.PI;
		}else{
			task.x_angle = Double.valueOf(s);
			}
		s = txtAngleY.getText();
		if (s.toLowerCase().contains("pi")){
			int i = s.indexOf("pi");
			if (i == 0)
				task.y_angle = Math.PI;
			else
				task.y_angle = Double.valueOf(s.substring(0, i)) * Math.PI;
		}else{
			task.y_angle = Double.valueOf(s);
			}
	}
	
	void updateControls(){
		if (task == null) return;
		
		txtAngleX.setText(MguiDouble.getString(task.x_angle, 10));
		txtAngleY.setText(MguiDouble.getString(task.y_angle, 10));
		
	}
	
	@Override
	public int getLineCount(){
		return 2;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		
	}

}