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
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import mgui.interfaces.InterfaceEnvironment;
import mgui.interfaces.InterfaceOptionsDialogBox;
import mgui.interfaces.InterfacePanel;
import mgui.interfaces.layouts.LineLayout;
import mgui.interfaces.layouts.LineLayoutConstraints;
import mgui.util.TimeFunctions;

/****************************************************
 * A dialog to define a {@link VideoTask}.
 * 
 * @author Andrew Reid
 * @version 1.0
 * @since 1.0
 *
 */
public class VideoTaskDialog extends InterfaceOptionsDialogBox {

	JLabel lblTask = new JLabel("Task type:");
	JComboBox cmbTask = new JComboBox();
	JLabel lblStart = new JLabel("Start time:");
	JTextField txtStart = new JTextField("00:00:000");
	JLabel lblStop = new JLabel("Stop time:");
	JTextField txtStop = new JTextField("00:01:000");
	VideoTaskDialogPanel pnlVideoTask;
	
	InterfacePanel panel;
	
	public int video_type;
	
	public VideoTaskDialog(){
		super();
	}
	
	public VideoTaskDialog(JFrame aFrame, InterfacePanel panel, VideoTaskOptions options){
		super(aFrame, options);
		this.panel = panel;
		_init();
	}
	
	private void _init(){
		this.setButtonType(BT_OK_CANCEL);
		super.init();
		
		LineLayout lineLayout = new LineLayout(InterfaceEnvironment.getLineHeight(), 5, 0);
		this.setMainLayout(lineLayout);
		setDialogSize(550,400);
		setTitle("Create/Edit Video Task");
		
		initTaskList();
		updateControls();
		
		cmbTask.addActionListener(this);
		cmbTask.setActionCommand("Task Type Changed");
		
		LineLayoutConstraints c = new LineLayoutConstraints(1, 1, 0.05, 0.24, 1);
		mainPanel.add(lblTask, c);
		c = new LineLayoutConstraints(1, 1, 0.3, 0.65, 1);
		mainPanel.add(cmbTask, c);
		c = new LineLayoutConstraints(2, 2, 0.05, 0.24, 1);
		mainPanel.add(lblStart, c);
		c = new LineLayoutConstraints(2, 2, 0.3, 0.65, 1);
		mainPanel.add(txtStart, c);
		c = new LineLayoutConstraints(3, 3, 0.05, 0.24, 1);
		mainPanel.add(lblStop, c);
		c = new LineLayoutConstraints(3, 3, 0.3, 0.65, 1);
		mainPanel.add(txtStop, c);
		
		updateTaskPanel();
	}
	
	void initTaskList(){
		
		cmbTask.removeAllItems();
		
		HashMap<String, VideoTaskType> tasks = InterfaceEnvironment.getVideoTaskTypes();
		Iterator<VideoTaskType> itr = tasks.values().iterator();
		
		while (itr.hasNext())
			cmbTask.addItem(itr.next());
		
	}
	
	void updateControls(){
		
		VideoTaskOptions options = (VideoTaskOptions)this.options;
		/*
		if (options.task == null || 
		    !((VideoTaskType)cmbTask.getSelectedItem()).task_class.isInstance(options.task)) 
			options.task = getSelectedTaskInstance();
		*/
		
		if (options.task != null){
			txtStart.setText(TimeFunctions.getTimeStr(options.task.start_time));
			txtStop.setText(TimeFunctions.getTimeStr(options.task.stop_time));
			cmbTask.setSelectedItem(VideoTaskType.getTypeForTask(options.task));
		}else{
			txtStart.setText(TimeFunctions.getTimeStr(0));
			txtStop.setText(TimeFunctions.getTimeStr(0));
			options.task = getSelectedTaskInstance();
			}
	}
	
	VideoTask getSelectedTaskInstance(){
		if (cmbTask.getSelectedItem() == null) return null;
	
		try{
			VideoTaskType type = (VideoTaskType)cmbTask.getSelectedItem();
			VideoTask task = type.getTaskInstance();
			task.setStart(TimeFunctions.getTimeFromStr(txtStart.getText()));
			task.setStop(TimeFunctions.getTimeFromStr(txtStop.getText()));
			return task;
			
		}catch (Exception e){
			e.printStackTrace();
			return null;
			}	
	}
	
	void updateTaskPanel(){
		
		if (cmbTask.getSelectedItem() == null) return;
		VideoTaskOptions options = (VideoTaskOptions)this.options;
		if (options == null) return;
		
		if (pnlVideoTask != null) mainPanel.remove(pnlVideoTask);
		
		VideoTaskType type = (VideoTaskType)cmbTask.getSelectedItem();
		pnlVideoTask = type.getDialogPanelInstance(options.task);
		
		LineLayoutConstraints c = new LineLayoutConstraints(5, pnlVideoTask.getLineCount() + 5, 0.0, 1.0, 1);
		mainPanel.add(pnlVideoTask, c);
		
		int add = 0;
		if (pnlVideoTask.getLineCount() > 3){
			add = (pnlVideoTask.getLineCount() - 3) * (InterfaceEnvironment.getLineHeight());
			}
		
		setDialogSize(550,400 + add);
		
		mainPanel.updateUI();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getActionCommand().equals(DLG_CMD_OK)){
			
			VideoTaskOptions options = (VideoTaskOptions)this.options;
			options.task.setStart(TimeFunctions.getTimeFromStr(txtStart.getText()));
			options.task.setStop(TimeFunctions.getTimeFromStr(txtStop.getText()));
			
			if (pnlVideoTask != null)
				pnlVideoTask.updateTask();
			
			panel.updateFromDialog(this);
			
			this.setVisible(false);
			return;
			}
		
		if (e.getActionCommand().equals("Task Type Changed")){
			
			VideoTaskOptions options = (VideoTaskOptions)this.options;
			options.task = getSelectedTaskInstance();
			updateTaskPanel();
			
			return;
		}
		
		super.actionPerformed(e);
	}
	
	
}